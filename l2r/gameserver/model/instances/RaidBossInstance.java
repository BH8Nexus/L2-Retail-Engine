package l2r.gameserver.model.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.model.AggroList.HateInfo;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.PlayerGroup;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.HeroDiary;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.DeleteTask;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;

public class RaidBossInstance extends MonsterInstance
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// private ScheduledFuture<?> minionMaintainTask;
	private String _killer;
	
	private static final int MINION_UNSPAWN_INTERVAL = 5000; // time to unspawn minions when boss is dead, msec
	
	public RaidBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isRaid()
	{
		return true;
	}
	
	protected int getMinionUnspawnInterval()
	{
		return MINION_UNSPAWN_INTERVAL;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		// if (minionMaintainTask != null)
		// {
		// minionMaintainTask.cancel(false);
		// minionMaintainTask = null;
		// }
		
		final int points = (int) getTemplate().rewardRp;
		if (points > 0)
		{
			calcRaidPointsReward(points);
		}
		
		if (this instanceof ReflectionBossInstance)
		{
			super.onDeath(killer);
			return;
		}
		
		Log.add(Log.LOG_BOSS_KILLED, new Object[]
		{
			getTypeName(),
			getName(),
			getNpcId(),
			killer,
			getX(),
			getY(),
			getZ(),
			"-"
		}, "bosses");
		
		_killer = (killer.getClan() != null ? killer.getClan().getName() : "");
		
		if (killer.isPlayable())
		{
			Player player = killer.getPlayer();
			if (player.isInParty())
			{
				for (Player member : player.getParty().getMembers())
				{
					member.updateRaidKills();
					if (member.isNoble())
					{
						Hero.getInstance().addHeroDiary(member.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
					}
				}
				player.getParty().sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}
			else
			{
				if (player.isNoble())
				{
					Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				}
				
				player.getCounters().raidsKilled++;
				player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
				player.updateRaidKills();
			}

			Quest q = QuestManager.getQuest(508);
			if (q != null)
			{
				String qn = q.getName();
				if ((player.getClan() != null) && player.getClan().getLeader().isOnline() && (player.getClan().getLeader().getPlayer().getQuestState(qn) != null))
				{
					QuestState st = player.getClan().getLeader().getPlayer().getQuestState(qn);
					st.getQuest().onKill(this, st);
				}
			}
		}
		
		if (hasPrivates() && getPrivatesList().hasAlivePrivates())
		{
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl()
				{
					if (isDead())
					{
						getPrivatesList().unspawnPrivates();
					}
				}
			}, getMinionUnspawnInterval());
		}
		
		int boxId = 0;
		switch (getNpcId())
		{
			case 25035: // Shilens Messenger Cabrio
				boxId = 31027;
				break;
			case 25054: // Demon Kernon
				boxId = 31028;
				break;
			case 25126: // Golkonda, the Longhorn General
				boxId = 31029;
				break;
			case 25220: // Death Lord Hallate
				boxId = 31030;
				break;
		}
		
		//make sure after restart such shit as mob is dead won't happen
		if (killer != null && killer == this)
		{
			super.onDeath(killer);
			return;
		}
		
		if (boxId != 0)
		{
			NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
			if (boxTemplate != null)
			{
				final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());
				
				ThreadPoolManager.getInstance().schedule(new DeleteTask(box), 60000);
			}
		}
		
		
		if(killer.getPlayer() != null && Config.RAID_DROP_GLOBAL_ITEMS)
		{
			if(Config.MIN_RAID_LEVEL_TO_DROP > 0 && getLevel() < Config.MIN_RAID_LEVEL_TO_DROP)
			{
				super.onDeath(killer);
				return;
			}
			for(Config.RaidGlobalDrop drop_inf : Config.RAID_GLOBAL_DROP)
			{
				int id = drop_inf.getId();
				long count = drop_inf.getCount();
				double chance = drop_inf.getChance();
				if(Rnd.chance(chance))
					ItemFunctions.addItem(killer.getPlayer(), id, count, true, "Raid Boss global drop");
			}
		}
		
		
		super.onDeath(killer);
	}
	
	private void calcRaidPointsReward(double totalPoints)
	{
		// Object groupkey (L2Party/L2CommandChannel/L2Player) | Long GroupDdamage
		Map<PlayerGroup, Long> participants = new HashMap<>();
		double totalHp = getMaxHp();
		
		// Scatter players to groups. Command Channel â†’ Party â†’ StandAlone. Add damage done for each group, including pets.
		for (HateInfo ai : getAggroList().getPlayableMap().values())
		{
			Player player = ai.attacker.getPlayer();
			Long curDamage = participants.get(player.getPlayerGroup());
			if (curDamage == null)
			{
				curDamage = 0L;
			}
			
			curDamage += ai.damage;
			participants.put(player.getPlayerGroup(), curDamage);
		}
		
		for (Entry<PlayerGroup, Long> groupInfo : participants.entrySet())
		{
			PlayerGroup group = groupInfo.getKey();
			Long damage = groupInfo.getValue();
			List<Player> activePlayers = new ArrayList<>();
			
			for (Player player : group)
			{
				if (player.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				{
					activePlayers.add(player);
				}
			}
			
			final int perPlayer = (int) Math.round((totalPoints * damage) / (totalHp * activePlayers.size()));
			for (Player player : activePlayers)
			{
				int playerReward = (int) Math.round(perPlayer * Experience.penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
				if (playerReward == 0)
				{
					continue;
				}
				
				player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_RAID_POINTS).addNumber(playerReward));
				RaidBossSpawnManager.getInstance().addPoints(player.getObjectId(), getNpcId(), playerReward);
			}
		}
		
		RaidBossSpawnManager.getInstance().updatePointsDb();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}
	
	@Override
	protected void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().setRaidBossDied(getNpcId(), _killer);
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		addSkill(SkillTable.getInstance().getInfo(4045, 1)); // Resist Full Magic Attack
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}
	
	@Override
	public boolean isFearImmune()
	{
		return true;
	}
	
	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return true;
	}
	
	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
	
//	@Override
//	public boolean isHealBlocked()
//	{
//		return true;
//	}
}