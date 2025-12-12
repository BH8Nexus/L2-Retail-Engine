package l2r.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CharacterAI;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.ChampionTemplateTable;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.NpcListener;
import l2r.gameserver.model.AggroList;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.TeleportLocation;
import l2r.gameserver.model.Territory;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.Mods.NpcAndRaidMod;
import l2r.gameserver.model.actor.listener.NpcListenerList;
import l2r.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.DimensionalRift;
import l2r.gameserver.model.entity.L2Event;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Dominion;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.SubUnit;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestEventType;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.AcquireSkillDone;
import l2r.gameserver.network.serverpackets.AcquireSkillList;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.AutoAttackStart;
import l2r.gameserver.network.serverpackets.ExChangeNpcState;
import l2r.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2r.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.network.serverpackets.RadarControl;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.PlayerKill;
import l2r.gameserver.scripts.Events;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.taskmanager.DecayTaskManager;
import l2r.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2r.gameserver.taskmanager.actionrunner.tasks.NotifyAITask;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.templates.npc.Faction;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.templates.npc.PrivatesList;
import l2r.gameserver.templates.spawn.SpawnRange;
import l2r.gameserver.utils.GCSArray;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.NpcUtils;
import l2r.gameserver.utils.Strings;

public class NpcInstance extends Creature
{
	
	public int i_ai0;
	public int i_ai1;
	public int i_ai2;
	public int i_ai3;
	public int i_ai4;
	public int i_ai5;
	public int i_ai6;
	public static final String NO_CHAT_WINDOW = "noChatWindow";
	public static final String NO_RANDOM_WALK = "noRandomWalk";
	public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
	public static final String TARGETABLE = "TargetEnabled";
	public static final String SHOW_NAME = "showName";
	public static final String SHOW_BOARD = "showBoard";
	
	private final String _showBoard;
	
	protected static final Logger _log = LoggerFactory.getLogger(NpcInstance.class);
	
	private int _personalAggroRange = -1;
	private int _level = 0;
	
	private long _dieTime = 0L;
	
	private int _aiSpawnParam;
	protected int _spawnAnimation = 2;
	
	private int _currentLHandId;
	private int _currentRHandId;
	
	private double _currentCollisionRadius;
	private double _currentCollisionHeight;
	
	private int npcState = 0;
	
	protected boolean _hasRandomAnimation;
	protected boolean _hasRandomWalk;
	protected boolean _hasChatWindow;
	
	private Future<?> _decayTask;
	private Future<?> _animationTask;
	
	private final AggroList _aggroList;
	
	private boolean _isTargetable;
	
	private boolean _showName;
	
	private Castle _nearestCastle;
	private Fortress _nearestFortress;
	private ClanHall _nearestClanHall;
	private ClanHall _nearestCustomClanHall;
	private Dominion _nearestDominion;
	
	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;
	
	private Spawner _spawn;
	private Location _spawnedLoc = new Location();
	private SpawnRange _spawnRange;
	
	private MultiValueSet<String> _parameters = StatsSet.EMPTY;
	
	// param
	private PrivatesList _privatesList = null;
	private int _param2;
	private int _param3;
	private Creature _param4;
	private NpcInstance _master = null;
	private boolean _ignoreLeaderAction;
	private ScheduledFuture<?> _minionRespawnTask;
	
	public boolean isEventMob = false;

	public NpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		if (template == null)
		{
			throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");
		}
		
		setParameters(template.getAIParams());
		
		_hasRandomAnimation = !getParameter(NO_RANDOM_ANIMATION, false) && (Config.MAX_NPC_ANIMATION > 0);
		_hasRandomWalk = !getParameter(NO_RANDOM_WALK, false);
		setHasChatWindow(!getParameter(NO_CHAT_WINDOW, false));
		setTargetable(getParameter(TARGETABLE, true));
		setShowName(getParameter(SHOW_NAME, true));
		_showBoard = getParameter(SHOW_BOARD, "");
		
		// if (template.getSkills().size() > 0)
		// {
		// for (TIntObjectIterator<Skill> iterator = template.getSkills().iterator(); iterator.hasNext();)
		// {
		// iterator.advance();
		// addSkill(iterator.value());
		// }
		// }
		
		if (!template.getSkills().isEmpty())
		{
			template.getSkills().valueCollection().forEach(this::addSkill);
		}
		
		setName(template.name);
		
		String customTitle = template.title;
		if (isMonster() && Config.ALT_SHOW_MONSTERS_LVL && canShowLevelInTitle())
		{
			customTitle = "LvL: " + getLevel();
			if (Config.ALT_SHOW_MONSTERS_AGRESSION && isAggressive())
			{
				customTitle += " A";
			}
		}
		setTitle(customTitle);
		
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		
		setCollisionHeight(getTemplate().getCollisionHeight());
		setCollisionRadius(getTemplate().getCollisionRadius());
		
		_aggroList = new AggroList(this);
		
		setFlying(getParameter("isFlying", false));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public HardReference<NpcInstance> getRef()
	{
		return (HardReference<NpcInstance>) super.getRef();
	}
	
	@Override
	public CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = getTemplate().getNewAI(this);
				}
			}
		}
		
		return _ai;
	}

	/**
	 * Return the position of the spawned point.<BR>
	 * <BR>
	 */
	public Location getSpawnedLoc()
	{
		// return _spawnedLoc;
		return getLeader() != null ? getLeader().getLoc() : _spawnedLoc;
	}
	
	public void setSpawnedLoc(Location loc)
	{
		_spawnedLoc = loc;
	}
	
	public int getRightHandItem()
	{
		return _currentRHandId;
	}
	
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}
	
	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}
	
	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}
	
	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public void setCollisionHeight(double offset)
	{
		_currentCollisionHeight = offset;
	}
	
	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	public void setCollisionRadius(double collisionRadius)
	{
		_currentCollisionRadius = collisionRadius;
	}
	
	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if (attacker.isPlayable())
		{
			getAggroList().addDamageHate(attacker, (int) damage, 0);
		}
		// setDamageTaken(getAbnormalEffect() + (int) damage);
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		_dieTime = System.currentTimeMillis();
		
		if (isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled()))
		{
			startDecay(20000L);
		}
		else if (isBoss())
		{
			startDecay(20000L);
		}
		else if (isFlying())
		{
			startDecay(4500L);
		}
		else
		{
			startDecay(8500L);
		}
		
		// Î¡Æ’Î¡ï¿½Î¡â€šÎ Â°Î Â½Î ÎŽÎ Â²Î ÎŠÎ Â° Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î ÎŽÎ Â² Î ÎŽÎ¡â‚¬Î¡Æ’Î Â¶Î ÎˆÎ¡ï¿½ Î Îˆ Î ÎŠÎ ÎŽÎ Â»Î Â»Î ÎˆÎ Â·Î ÎˆÎ Î‰ Î Î�Î ÎŽ Î¡Æ’Î ÎŒÎ ÎŽÎ Â»Î¡â€¡Î Â°Î Â½Î ÎˆÎ¡ï¿½
		setLHandId(getTemplate().lhand);
		setRHandId(getTemplate().rhand);
		setCollisionHeight(getTemplate().getCollisionHeight());
		setCollisionRadius(getTemplate().getCollisionRadius());
		
		getAI().stopAITask();
		stopAttackStanceTask();
		stopRandomAnimation();
		stopMinionRespawnTask();
		
		// Quest notifyKill()
		if ((killer != null) && (killer.getPlayer() != null))
		{
			Map<Playable, AggroList.HateInfo> aggroMap = getAggroList().getPlayableMap();
			
			Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
			if ((quests != null) && (quests.length > 0))
			{
				List<Player> players = null; // Î ÎŒÎ Â°Î¡ï¿½Î¡ï¿½Î ÎˆÎ Â² Î¡ï¿½ Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ Â°Î ÎŒÎ Îˆ, Î ÎŠÎ ÎŽÎ¡â€šÎ ÎŽÎ¡â‚¬Î¡â€¹Î Âµ Î ÎŒÎ ÎŽÎ Â³Î¡Æ’Î¡â€š Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ Î Â·Î Â°Î ÎˆÎ Â½Î¡â€šÎ ÂµÎ¡â‚¬Î ÂµÎ¡ï¿½Î ÎŽÎ Â²Î Â°Î Â½Î¡â€¹ Î Â² Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â°Î¡â€¦
				if (isRaid() && Config.ALT_NO_LASTHIT) // Î â€�Î Â»Î¡ï¿½ Î Â°Î Â»Î¡ï¿½Î¡â€šÎ Â° Î Â½Î Â° Î Â»Î Â°Î¡ï¿½Î¡â€šÎ¡â€¦Î ÎˆÎ¡â€š Î Â±Î ÂµÎ¡â‚¬Î ÂµÎ ÎŒ Î Â²Î¡ï¿½Î ÂµÎ¡â€¦ Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ ÎŽÎ Â² Î Â²Î ÎŽÎ ÎŠÎ¡â‚¬Î¡Æ’Î Â³
				{
					players = new ArrayList<>();
					for (Playable pl : aggroMap.keySet())
					{
						if (!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						{
							players.add(pl.getPlayer());
						}
					}
				}
				else if (killer.getPlayer().getParty() != null) // Î ÂµÎ¡ï¿½Î Â»Î Îˆ Î Î�Î Â°Î¡â€šÎ Îˆ Î¡â€šÎ ÎŽ Î¡ï¿½Î ÎŽÎ Â±Î ÎˆÎ¡â‚¬Î Â°Î ÂµÎ ÎŒ Î Â²Î¡ï¿½Î ÂµÎ¡â€¦ Î ÎŠÎ¡â€šÎ ÎŽ Î Î�Î ÎŽÎ Î„Î¡â€¦Î ÎŽÎ Î„Î ÎˆÎ¡â€š
				{
					players = new ArrayList<>(killer.getPlayer().getParty().size());
					for (Player pl : killer.getPlayer().getParty())
					{
						if (!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						{
							players.add(pl);
						}
					}
				}
				
				for (Quest quest : quests)
				{
					Player toReward = killer.getPlayer();
					if ((quest.getParty() != Quest.PARTY_NONE) && (players != null))
					{
						if (isRaid() || (quest.getParty() == Quest.PARTY_ALL)) // Î ÂµÎ¡ï¿½Î Â»Î Îˆ Î¡â€ Î ÂµÎ Â»Î¡ï¿½ Î¡â‚¬Î ÂµÎ Î‰Î Î„ Î ÎˆÎ Â»Î Îˆ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€š Î Î„Î Â»Î¡ï¿½ Î Â²Î¡ï¿½Î ÂµÎ Î‰ Î Î�Î Â°Î¡â€šÎ Îˆ Î Â½Î Â°Î Â³Î¡â‚¬Î Â°Î Â¶Î Î„Î Â°Î ÂµÎ ÎŒ Î Â²Î¡ï¿½Î ÂµÎ¡â€¦ Î¡Æ’Î¡â€¡Î Â°Î¡ï¿½Î¡â€šÎ Â½Î ÎˆÎ ÎŠÎ ÎŽÎ Â²
						{
							for (Player pl : players)
							{
								QuestState qs = pl.getQuestState(quest.getName());
								if ((qs != null) && !qs.isCompleted())
								{
									quest.notifyKill(this, qs);
								}
							}
							toReward = null;
						}
						else
						{ // Î ÎˆÎ Â½Î Â°Î¡â€¡Î Âµ Î Â²Î¡â€¹Î Â±Î ÎˆÎ¡â‚¬Î Â°Î ÂµÎ ÎŒ Î ÎŽÎ Î„Î Â½Î ÎŽÎ Â³Î ÎŽ
							List<Player> interested = new ArrayList<>(players.size());
							for (Player pl : players)
							{
								QuestState qs = pl.getQuestState(quest.getName());
								if ((qs != null) && !qs.isCompleted())
								{
									interested.add(pl);
								}
							}
							
							if (interested.isEmpty())
							{
								continue;
							}
							
							toReward = interested.get(Rnd.get(interested.size()));
							if (toReward == null)
							{
								toReward = killer.getPlayer();
							}
						}
					}
					
					if (toReward != null)
					{
						QuestState qs = toReward.getQuestState(quest.getName());
						if ((qs != null) && !qs.isCompleted())
						{
							quest.notifyKill(this, qs);
						}
					}
				}
			}
		}
		if (getLeader() != null)
		{
			minionDie(this, getParameter("respawn_minion", 0));
		}
		
		super.onDeath(killer);
	}
	
	public long getDeadTime()
	{
		if (_dieTime <= 0L)
		{
			return 0L;
		}
		return System.currentTimeMillis() - _dieTime;
	}
	
	public AggroList getAggroList()
	{
		return _aggroList;
	}
	
	// public MinionList getMinionList()
	// {
	// return null;
	// }
	
	public boolean hasMinions()
	{
		return false;
	}
	
	public void dropItem(Player lastAttacker, int itemId, long itemCount)
	{
		dropItem(lastAttacker, itemId, itemCount, 0);
	}
	
	public void dropItem(Player lastAttacker, int itemId, long itemCount, int enchantLvl)
	{
		if ((itemCount == 0) || (lastAttacker == null))
		{
			return;
		}
		
		ItemInstance item;
		
		// Apply Special Item drop with random(rnd) quantity(qty) for champions.
		if (getChampionTemplate() != null)
		{
			for (ChampionTemplateTable.RewardItem ri : getChampionTemplate().rewards)
			{
				if (Rnd.get(100) < ri.getDropChance()) // We dont use <= since the random gets one from 0 to 99
				{
					int count = Rnd.get(ri.getMinCount(), ri.getMaxCount());
					
					item = new ItemInstance(ri.getItemId());
					item.setCount(count);
					lastAttacker.doAutoLootOrDrop(item, this);
				}
			}
		}
		
		for (long i = 0; i < itemCount; i++)
		{
			item = ItemFunctions.createItem(itemId);
			if (item == null)
			{
				continue;
			}
			
			// Synerge - Support for dropping enchanted items
			if (enchantLvl > 0)
			{
				item.setEnchantLevel(enchantLvl);
			}
			
			for (GlobalEvent e : getEvents())
			{
				item.addEvent(e);
			}
			
			// Set the Item quantity dropped if L2ItemInstance is stackable
			if (item.isStackable())
			{
				i = itemCount; // Set so loop won't happent again
				item.setCount(itemCount); // Set item count
			}
			
			if (isChampion())
			{
				if (item.isAdena())
				{
					item.setCount(itemCount * (long) Config.RATE_CHAMPION_DROP_ADENA);
				}
				else if (!item.isAdena())
				{
					item.setCount(itemCount * (long) Config.RATE_DROP_CHAMPION);
				}
			}
			
			if (isRaid() || (this instanceof ReflectionBossInstance))
			{
				SystemMessage2 sm;
				if (itemId == 57)
				{
					sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
					sm.addName(this);
					sm.addLong(item.getCount());
				}
				else
				{
					sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
					sm.addName(this);
					sm.addItemName(itemId);
					sm.addLong(item.getCount());
				}
				broadcastPacket(sm);
			}
			
			lastAttacker.doAutoLootOrDrop(item, this);
		}
	}
	
	public void dropItem(Player lastAttacker, ItemInstance item)
	{
		if (item.getCount() == 0)
		{
			return;
		}
		
		if (isRaid() || (this instanceof ReflectionBossInstance))
		{
			SystemMessage2 sm;
			if (item.getItemId() == 57)
			{
				sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
				sm.addName(this);
				sm.addLong(item.getCount());
			}
			else
			{
				sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
				sm.addName(this);
				sm.addItemName(item.getItemId());
				sm.addLong(item.getCount());
			}
			broadcastPacket(sm);
		}
		
		lastAttacker.doAutoLootOrDrop(item, this);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		
		_dieTime = 0L;
		_spawnAnimation = 0;
		
		if (getAI().isGlobalAI() || ((getCurrentRegion() != null) && getCurrentRegion().isActive()))
		{
			getAI().startAITask();
			startRandomAnimation();
		}
		
		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_SPAWN));
		
		getListeners().onSpawn();
		
		final String Privates = this.getParameter("Privates", null);
		if (Privates != null)
		{
			if (this.hasPrivates())
			{
				this.getPrivatesList().useSpawnPrivates();
			}
			else
			{
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					@Override
					public void runImpl()
					{
						getPrivatesList().createPrivates(Privates, true);
					}
				}, 1500L);
			}
		}
	}
	
	@Override
	protected void onDespawn()
	{
		getAggroList().clear();
		getAI().onEvtDeSpawn();
		getAI().stopAITask();
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		stopRandomAnimation();
		
		super.onDespawn();
	}
	
	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) _template;
	}
	
	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	protected boolean _unAggred = false;
	
	public void setUnAggred(boolean state)
	{
		_unAggred = state;
	}
	
	/**
	 * Return True if the L2NpcInstance is aggressive (ex : L2MonsterInstance in function of aggroRange).<BR>
	 * <BR>
	 */
	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}
	
	public int getAggroRange()
	{
		if (_unAggred)
		{
			return 0;
		}
		
		if (_personalAggroRange >= 0)
		{
			return _personalAggroRange;
		}
		
		return getTemplate().aggroRange;
	}
	
	/**
	 * Î Â£Î¡ï¿½Î¡â€šÎ Â°Î Â½Î Â°Î Â²Î Â»Î ÎˆÎ Â²Î Â°Î ÂµÎ¡â€š Î Î„Î Â°Î Â½Î Â½Î ÎŽÎ ÎŒÎ¡Æ’ npc Î Â½Î ÎŽÎ Â²Î¡â€¹Î Î‰ aggroRange. Î â€¢Î¡ï¿½Î Â»Î Îˆ Î¡Æ’Î¡ï¿½Î¡â€šÎ Â°Î Â½Î ÎŽÎ Â²Î Â»Î ÂµÎ Â½Î¡â€¹Î Î‰ aggroRange < 0, Î¡â€šÎ ÎŽ Î Â±Î¡Æ’Î Î„Î ÂµÎ¡â€š Î Â±Î¡â‚¬Î Â°Î¡â€šÎ¡ï¿½Î¡ï¿½ Î Â°Î Â³Î Â³Î¡â‚¬Î ÎŽÎ¡â‚¬Î ÂµÎ Î‰Î Â½Î Î„Î Â¶ Î¡ï¿½ Î¡â€šÎ ÂµÎ ÎŒÎ Î�Î Â»Î ÂµÎ Î‰Î¡â€šÎ Â°.
	 * @param aggroRange Î Â½Î ÎŽÎ Â²Î¡â€¹Î Î‰ agrroRange
	 */
	public void setAggroRange(int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â³Î¡â‚¬Î¡Æ’Î Î�Î Î�Î¡Æ’ Î¡ï¿½Î ÎŽÎ¡â€ Î ÎˆÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ¡ï¿½Î¡â€šÎ Îˆ
	 */
	public Faction getFaction()
	{
		return getTemplate().getFaction();
	}
	
	public boolean isInFaction(NpcInstance npc)
	{
		return getFaction().equals(npc.getFaction()) && !getFaction().isIgnoreNpcId(npc.getNpcId());
	}
	
	@Override
	public int getMAtk(Creature target, Skill skill)
	{
	    double mul = isRaid() ? NpcAndRaidMod.RaidMAtkMul(getLevel()) : NpcAndRaidMod.NpcMAtkMul(getLevel());

		if (getChampionTemplate() != null)
		{
			return (int) (super.getMAtk(target, skill) * mul * getChampionTemplate().matkMultiplier);
		}
		
		return (int) (super.getMAtk(target, skill) * mul);
	}

	@Override
	public int getPAtk(Creature target)
	{
		double mul = isRaid() ? NpcAndRaidMod.RaidPAtkMul(getLevel()) : NpcAndRaidMod.NpcPAtkMul(getLevel());

		if (getChampionTemplate() != null)
		{
			return (int) (super.getPAtk(target) * mul * getChampionTemplate().patkMultiplier);
		}
		
		return (int) (super.getPAtk(target) * mul);
	}
	
	public int getPDef(Creature target)
	{
		double mul = isRaid() ? NpcAndRaidMod.RaidPDefMul(getLevel()) : NpcAndRaidMod.NpcPDefMul(getLevel());
		return (int) (super.getPDef(target) * mul);
	}
	
	public int getMDef(Creature target, Skill skill)
	{
		// TODO Auto-generated method stub
		double mul = isRaid() ? NpcAndRaidMod.RaidMDefMul(getLevel()) : NpcAndRaidMod.NpcMDefMul(getLevel());
		return (int) (super.getMDef(target, skill) * mul);
	}

	@Override
	public int getMaxHp()
	{
	    double mul = isRaid() ? NpcAndRaidMod.RaidMaxHpMul(getLevel()) : NpcAndRaidMod.NpcMaxHpMul(getLevel());

		if (getChampionTemplate() != null)
		{
			return (int) (super.getMaxHp() * mul * getChampionTemplate().hpMultiplier);
		}
		
		return (int) (super.getMaxHp() * mul);
	}

	@Override
	public int getMaxMp()
	{
	    double mul = isRaid() ? NpcAndRaidMod.RaidMaxMpMul(getLevel()) : NpcAndRaidMod.NpcMaxMpMul(getLevel());

		return (int) (super.getMaxMp() * mul);
	}

	public double getRewardRate(Player player)
	{
		return Config.RATE_DROP_ITEMS * player.getRateItems();
	}

	
	public long getExpReward()
	{
		if (getChampionTemplate() != null)
		{
			return (long) calcStat(Stats.EXP, getTemplate().rewardExp * getChampionTemplate().expMultiplier, null, null);
		}
		
		return (long) calcStat(Stats.EXP, getTemplate().rewardExp, null, null);
	}
	
	public long getSpReward()
	{
		if (getChampionTemplate() != null)
		{
			return (long) calcStat(Stats.SP, getTemplate().rewardSp * getChampionTemplate().spMultiplier, null, null);
		}
		
		return (long) calcStat(Stats.SP, getTemplate().rewardSp, null, null);
	}
	
	@Override
	protected void onDelete()
	{
		stopDecay();
		if (_spawn != null)
		{
			_spawn.stopRespawn();
		}
		setSpawn(null);
		
		if (hasPrivates())
		{
			getPrivatesList().deletePrivates();
		}
		
		super.onDelete();
	}
	
	public Spawner getSpawn()
	{
		return _spawn;
	}
	
	public void setSpawn(Spawner spawn)
	{
		_spawn = spawn;
	}
	
	public final void decayOrDelete()
	{
		onDecay();
	}
	
	@Override
	protected void onDecay()
	{
		super.onDecay();
		
		_spawnAnimation = 2;
		
		if (_spawn != null)
		{
			_spawn.decreaseCount(this);
		}
		else if (!isMinion())
		{
			deleteMe(); // Î â€¢Î¡ï¿½Î Â»Î Îˆ Î¡ï¿½Î¡â€šÎ ÎŽÎ¡â€š Î ÎŒÎ ÎŽÎ Â± Î Â·Î Â°Î¡ï¿½Î Î�Î Â°Î Â²Î Â½Î ÂµÎ Â½ Î Â½Î Âµ Î¡â€¡Î ÂµÎ¡â‚¬Î ÂµÎ Â· Î¡ï¿½Î¡â€šÎ Â°Î Â½Î Î„Î Â°Î¡â‚¬Î¡â€šÎ Â½Î¡â€¹Î Î‰ Î ÎŒÎ ÂµÎ¡â€¦Î Â°Î Â½Î ÎˆÎ Â·Î ÎŒ Î¡ï¿½Î Î�Î Â°Î Â²Î Â½Î Â° Î Â·Î Â½Î Â°Î¡â€¡Î ÎˆÎ¡â€š Î Î�Î ÎŽÎ¡ï¿½Î ÎŒÎ ÂµÎ¡â‚¬Î¡â€šÎ ÎˆÎ Âµ Î ÂµÎ ÎŒÎ¡Æ’ Î Â½Î Âµ Î Î�Î ÎŽÎ Â»Î ÎŽÎ Â¶Î ÂµÎ Â½Î ÎŽ Î Îˆ Î ÎŽÎ Â½ Î¡Æ’Î ÎŒÎ ÎˆÎ¡â‚¬Î Â°Î ÂµÎ¡â€š Î Â½Î Â°Î¡ï¿½Î ÎŽÎ Â²Î¡ï¿½Î ÂµÎ ÎŒ
		}
	}
	
	/**
	 * Î â€”Î Â°Î Î�Î¡Æ’Î¡ï¿½Î¡â€šÎ ÎˆÎ¡â€šÎ¡ï¿½ Î Â·Î Â°Î Î„Î Â°Î¡â€¡Î¡Æ’ "Î ÎˆÎ¡ï¿½Î¡â€¡Î ÂµÎ Â·Î Â½Î ÎŽÎ Â²Î ÂµÎ Â½Î ÎˆÎ¡ï¿½" Î Î�Î ÎŽÎ¡ï¿½Î Â»Î Âµ Î¡ï¿½Î ÎŒÎ ÂµÎ¡â‚¬Î¡â€šÎ Îˆ
	 */
	protected void startDecay(long delay)
	{
		stopDecay();
		_decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
	}
	
	/**
	 * Î ï¿½Î¡â€šÎ ÎŒÎ ÂµÎ Â½Î ÎˆÎ¡â€šÎ¡ï¿½ Î Â·Î Â°Î Î„Î Â°Î¡â€¡Î¡Æ’ "Î ÎˆÎ¡ï¿½Î¡â€¡Î ÂµÎ Â·Î Â½Î ÎŽÎ Â²Î ÂµÎ Â½Î ÎˆÎ¡ï¿½" Î Î�Î ÎŽÎ¡ï¿½Î Â»Î Âµ Î¡ï¿½Î ÎŒÎ ÂµÎ¡â‚¬Î¡â€šÎ Îˆ
	 */
	public void stopDecay()
	{
		if (_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
	}
	
	/**
	 * Î ï¿½Î¡â€šÎ ÎŒÎ ÂµÎ Â½Î ÎˆÎ¡â€šÎ¡ï¿½ Î Îˆ Î Â·Î Â°Î Â²Î ÂµÎ¡â‚¬Î¡ï¿½Î ÎˆÎ¡â€šÎ¡ï¿½ Î Â·Î Â°Î Î„Î Â°Î¡â€¡Î¡Æ’ "Î ÎˆÎ¡ï¿½Î¡â€¡Î ÂµÎ Â·Î Â½Î ÎŽÎ Â²Î ÂµÎ Â½Î ÎˆÎ¡ï¿½" Î Î�Î ÎŽÎ¡ï¿½Î Â»Î Âµ Î¡ï¿½Î ÎŒÎ ÂµÎ¡â‚¬Î¡â€šÎ Îˆ
	 */
	public void endDecayTask()
	{
		if (_decayTask != null)
		{
			_decayTask.cancel(false);
			_decayTask = null;
		}
		doDecay();
	}
	
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	@Override
	public int getLevel()
	{
		return _level == 0 ? getTemplate().level : _level;
	}
	
	private int _displayId = 0;
	
	public void setDisplayId(int displayId)
	{
		_displayId = displayId;
	}
	
	public int getDisplayId()
	{
		return _displayId > 0 ? _displayId : getTemplate().displayId;
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}
	
	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().rhand;
		
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equipped in the right hand of the L2NpcInstance
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().rhand);
		
		if (!(item instanceof WeaponTemplate))
		{
			return null;
		}
		
		return (WeaponTemplate) item;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instances
		return null;
	}
	
	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().lhand;
		
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equipped in the right hand of the L2NpcInstance
		ItemTemplate item = ItemHolder.getInstance().getTemplate(getTemplate().lhand);
		
		if (!(item instanceof WeaponTemplate))
		{
			return null;
		}
		
		return (WeaponTemplate) item;
	}
	
	@Override
	public void sendChanges()
	{
		if (isFlying())
		{
			return;
		}
		super.sendChanges();
	}
	
	private ScheduledFuture<?> _broadcastCharInfoTask;
	
	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}
	
	@Override
	public void broadcastCharInfo()
	{
		if (!isVisible())
		{
			return;
		}
		
		if (_broadcastCharInfoTask != null)
		{
			return;
		}
		
		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}
	
	public void broadcastCharInfoImpl()
	{
		for (Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(new NpcInfo(this, player).update());
		}
	}
	
	// I always NPC 2
	public void onRandomAnimation()
	{
		if ((System.currentTimeMillis() - _lastSocialAction) > 10000L)
		{
			broadcastPacket(new SocialAction(getObjectId(), 2));
			_lastSocialAction = System.currentTimeMillis();
		}
	}
	
	public void startRandomAnimation()
	{
		if (!hasRandomAnimation())
		{
			return;
		}
		_animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
	}
	
	public void stopRandomAnimation()
	{
		if (_animationTask != null)
		{
			_animationTask.cancel(false);
			_animationTask = null;
		}
	}
	
	public boolean hasRandomAnimation()
	{
		return _hasRandomAnimation;
	}
	
	public boolean hasRandomWalk()
	{
		return _hasRandomWalk;
	}
	
	public Castle getCastle()
	{
		if ((getReflection() == ReflectionManager.PARNASSUS) && Config.SERVICES_PARNASSUS_NOTAX)
		{
			return null;
		}
		if (Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && (getReflection() == ReflectionManager.GIRAN_HARBOR))
		{
			return null;
		}
		if (Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && (getReflection() == ReflectionManager.PARNASSUS))
		{
			return null;
		}
		if (Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && isInZone(ZoneType.offshore))
		{
			return null;
		}
		if (_nearestCastle == null)
		{
			_nearestCastle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
		}
		return _nearestCastle;
	}
	
	public Castle getCastle(Player player)
	{
		return getCastle();
	}
	
	public Fortress getFortress()
	{
		if (_nearestFortress == null)
		{
			_nearestFortress = ResidenceHolder.getInstance().findNearestResidence(Fortress.class, getX(), getY(), getZ(), getReflection(), 32768);
		}
		
		return _nearestFortress;
	}
	
	public ClanHall getClanHall()
	{
		if (_nearestClanHall == null)
		{
			_nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768);
		}
		
		return _nearestClanHall;
	}
	
	public ClanHall getCustomClanHall()
	{
		if (_nearestCustomClanHall == null)
		{
			_nearestCustomClanHall = ResidenceHolder.getInstance().getResidence(getTemplate().getCustomClanHallId());
		}
		
		return _nearestCustomClanHall;
	}
	
	public Dominion getDominion()
	{
		if (getReflection() != ReflectionManager.DEFAULT)
		{
			return null;
		}
		
		if (_nearestDominion == null)
		{
			if (getTemplate().getCastleId() == 0)
			{
				return null;
			}
			
			Castle castle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
			_nearestDominion = castle.getDominion();
		}
		
		return _nearestDominion;
	}
	
	protected long _lastSocialAction;
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if (!isTargetable())
		{
			player.sendActionFailed();
			return;
		}
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			if (player.getTarget() == this)
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
			}
			
			player.sendPacket(new ValidateLocation(this), ActionFail.STATIC);
			return;
		}
		
		if (Events.onAction(player, this, shift))
		{
			player.sendPacket(new ValidateLocation(this));
			player.sendActionFailed();
			return;
		}
		
		// Synerge - Send event to fight club to see if the event handles this npc selection
		if (player.isInFightClub())
		{
			if (player.getFightClubEvent().onTalkNpc(player, this))
			{
				player.sendActionFailed();
				return;
			}
		}
		
		if (isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}
		
		if (!getTemplate().noInterractionDistance && !isInRangeZ(player, INTERACTION_DISTANCE)) // In fucking range Z because players can target baium from downstairs and wake him up!
		{
			if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			}
			return;
		}
		
		if (!Config.ALLOW_TALK_TO_NPCS)
		{
			player.sendActionFailed();
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getKarma() > 0) && !player.canOverrideCond(PcCondOverride.ITEM_SHOP_CONDITIONS) && !(this instanceof WarehouseInstance))
		{
			player.sendActionFailed();
			return;
		}
		
		if ((!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) || player.isAlikeDead())
		{
			return;
		}
		
		if (hasRandomAnimation())
		{
			onRandomAnimation();
		}
		
		player.sendActionFailed();
		player.stopMove(false);
		
		if ((player._event == null) || !player._event.talkWithNpc(player, this))
		{
			if (_isBusy)
			{
				showBusyWindow(player);
			}
			else if (isHasChatWindow())
			{
				boolean flag = false;
				Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
				if ((qlst != null) && (qlst.length > 0))
				{
					for (Quest element : qlst)
					{
						QuestState qs = player.getQuestState(element.getName());
						if (((qs == null) || !qs.isCompleted()) && element.notifyFirstTalk(this, player))
						{
							flag = true;
						}
					}
				}
				if (!flag)
				{
					showChatWindow(player, 0);
				}
				if (Config.NPC_DIALOG_PLAYER_DELAY > 0)
				{
					player.setNpcDialogEndTime((int) (System.currentTimeMillis() / 1000L) + Config.NPC_DIALOG_PLAYER_DELAY);
				}
			}
		}
	}
	
	public void showQuestWindow(Player player, String questId)
	{
		if (!player.isQuestContinuationPossible(true))
		{
			return;
		}
		
		int count = 0;
		for (QuestState quest : player.getAllQuestsStates())
		{
			if ((quest != null) && quest.getQuest().isVisible() && quest.isStarted() && (quest.getCond() > 0))
			{
				count++;
			}
		}
		
		if (count > 40)
		{
			showChatWindow(player, "quest-limit.htm");
			return;
		}
		
		try
		{
			// Get the state of the selected quest
			QuestState qs = player.getQuestState(questId);
			if (qs != null)
			{
				if (qs.isCompleted())
				{
					showChatWindow(player, "completed-quest.htm");
					return;
				}
				if (qs.getQuest().notifyTalk(this, qs))
				{
					return;
				}
			}
			else
			{
				Quest q = QuestManager.getQuest(questId);
				if (q != null)
				{
					// check for start point
					Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if ((qlst != null) && (qlst.length > 0))
					{
						for (Quest element : qlst)
						{
							if (element == q)
							{
								qs = q.newQuestState(player, Quest.CREATED);
								if (qs.getQuest().notifyTalk(this, qs))
								{
									return;
								}
								break;
							}
						}
					}
				}
			}
			
			showChatWindow(player, "no-quest.htm");
		}
		catch (Exception e)
		{
			_log.warn("problem with npc text(questId: " + questId + ") " + e);
			_log.error("", e);
		}
		
		player.sendActionFailed();
	}
	
	public static boolean canBypassCheck(Player player, NpcInstance npc)
	{
		if ((npc == null) || player.isActionsDisabled() || (!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) || (!npc.getTemplate().noInterractionDistance && !npc.isInRange(player, INTERACTION_DISTANCE)))
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}
	
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
		{
			return;
		}
		
		if ((getTemplate().getTeleportList().size() > 0) && checkForDominionWard(player))
		{
			return;
		}
		
		try
		{
			if (command.equalsIgnoreCase("TerritoryStatus"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("merchant/territorystatus.htm");
				html.replace("%npcname%", getName());
				
				Castle castle = getCastle(player);
				if ((castle != null) && (castle.getId() > 0))
				{
					html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
					html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));
					
					if (castle.getOwnerId() > 0)
					{
						Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
						if (clan != null)
						{
							html.replace("%clanname%", clan.getName());
							html.replace("%clanleadername%", clan.getLeaderName());
						}
						else
						{
							html.replace("%clanname%", "unexistant clan");
							html.replace("%clanleadername%", "None");
						}
					}
					else
					{
						html.replace("%clanname%", "NPC");
						html.replace("%clanleadername%", "None");
					}
				}
				else
				{
					html.replace("%castlename%", "Open");
					html.replace("%taxpercent%", "0");
					
					html.replace("%clanname%", "No");
					html.replace("%clanleadername%", getName());
				}
				
				player.sendPacket(html);
			}
			else if (command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if (quest.length() == 0)
				{
					showQuestWindow(player);
				}
				else
				{
					showQuestWindow(player, quest);
				}
			}
			else if (command.startsWith("Chat"))
			{
				try
				{
					int val = Integer.parseInt(command.substring(5));
					showChatWindow(player, val);
				}
				catch (NumberFormatException nfe)
				{
					String filename = command.substring(5).trim();
					if (filename.length() == 0)
					{
						showChatWindow(player, "npcdefault.htm");
					}
					else
					{
						showChatWindow(player, filename);
					}
				}
			}
			else if (command.startsWith("AttributeCancel"))
			{
				player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
			}
			else if (command.startsWith("NpcLocationInfo"))
			{
				int val = Integer.parseInt(command.substring(16));
				NpcInstance npc = GameObjectsStorage.getByNpcId(val);
				if (npc != null)
				{
					// Î Â£Î Â±Î ÎˆÎ¡â‚¬Î Â°Î ÂµÎ ÎŒ Î¡â€žÎ Â»Î Â°Î Â¶Î ÎŽÎ ÎŠ Î Â½Î Â° Î ÎŠÎ Â°Î¡â‚¬Î¡â€šÎ Âµ Î Îˆ Î¡ï¿½Î¡â€šÎ¡â‚¬Î ÂµÎ Â»Î ÎŠÎ¡Æ’ Î Â½Î Â° Î ÎŠÎ ÎŽÎ ÎŒÎ Î�Î Â°Î¡ï¿½Î Âµ
					player.sendPacket(new RadarControl(2, 2, npc.getLoc()));
					// Î Î…Î¡â€šÎ Â°Î Â²Î ÎˆÎ ÎŒ Î¡â€žÎ Â»Î Â°Î Â¶Î ÎŽÎ ÎŠ Î Â½Î Â° Î ÎŠÎ Â°Î¡â‚¬Î¡â€šÎ Âµ Î Îˆ Î¡ï¿½Î¡â€šÎ¡â‚¬Î ÂµÎ Â»Î ÎŠÎ¡Æ’ Î Â½Î Â° Î ÎŠÎ ÎŽÎ ÎŒÎ Î�Î Â°Î¡ï¿½Î Âµ
					player.sendPacket(new RadarControl(0, 1, npc.getLoc()));
				}
			}
			else if (command.startsWith("Multisell") || command.startsWith("multisell"))
			{
				String listId = command.substring(9).trim();
				Castle castle = getCastle(player);
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, castle != null ? castle.getTaxRate() : 0);
			}
			else if (command.startsWith("EnterRift"))
			{
				if (checkForDominionWard(player))
				{
					return;
				}
				
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken(); // no need for "enterRift"
				
				Integer b1 = Integer.parseInt(st.nextToken()); // type
				
				DimensionalRiftManager.getInstance().start(player, b1, this);
			}
			else if (command.startsWith("ChangeRiftRoom"))
			{
				if (player.isInParty() && player.getParty().isInReflection() && (player.getParty().getReflection() instanceof DimensionalRift))
				{
					((DimensionalRift) player.getParty().getReflection()).manualTeleport(player, this);
				}
				else
				{
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
				}
			}
			else if (command.startsWith("ExitRift"))
			{
				if (player.isInParty() && player.getParty().isInReflection() && (player.getParty().getReflection() instanceof DimensionalRift))
				{
					((DimensionalRift) player.getParty().getReflection()).manualExitRift(player, this);
				}
				else
				{
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
				}
			}
			else if (command.equalsIgnoreCase("SkillList"))
			{
				showSkillList(player);
			}
			else if (command.equalsIgnoreCase("ClanSkillList"))
			{
				showClanSkillList(player);
			}
			else if (command.startsWith("SubUnitSkillList"))
			{
				showSubUnitSkillList(player);
			}
			else if (command.equalsIgnoreCase("TransformationSkillList"))
			{
				showTransformationSkillList(player, AcquireType.TRANSFORMATION);
			}
			else if (command.equalsIgnoreCase("CertificationSkillList"))
			{
				showTransformationSkillList(player, AcquireType.CERTIFICATION);
			}
			else if (command.equalsIgnoreCase("CollectionSkillList"))
			{
				showCollectionSkillList(player);
			}
			else if (command.equalsIgnoreCase("BuyTransformation"))
			{
				showTransformationMultisell(player);
			}
			else if (command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				if (cmdChoice == 1)
				{
					player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
				}
				else if (cmdChoice == 2)
				{
					player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
				}
			}
			else if (command.startsWith("Link"))
			{
				showChatWindow(player, command.substring(5));
			}
			else if (command.startsWith("Teleport"))
			{
				if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getKarma() > 0)) // karma
				{
					player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.NpcInstance.message1", player));
					return;
				}
				
				int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
				if (list != null)
				{
					showTeleportList(player, list);
				}
				else
				{
					player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.NpcInstance.message2", player));
				}
			}
			else if (command.startsWith("Tele20Lvl"))
			{
				int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
				TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
				if (player.getLevel() > 20)
				{
					showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm");
				}
				else if (list != null)
				{
					showTeleportList(player, list);
				}
				else
				{
					player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.NpcInstance.message3", player));
				}
			}
			else if (command.startsWith("open_gate"))
			{
				int val = Integer.parseInt(command.substring(10));
				ZoneHolder.getDoor(val).openMe();
				player.sendActionFailed();
			}
			else if (command.equalsIgnoreCase("TransferSkillList"))
			{
				showTransferSkillList(player);
			}
			else if (command.equalsIgnoreCase("CertificationCancel"))
			{
				SubClass.cancelCertification(this, player);
			}
			else if (command.equalsIgnoreCase("reciveSMS"))
			{
				if (player.getPremiumItemList().isEmpty())
				{
					player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.NpcInstance.message4", player));
					player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.NpcInstance.message5", player));
					return;
				}
				
				player.sendPacket(new ExGetPremiumItemList(player));
			}
			else if (command.startsWith("RemoveTransferSkill"))
			{
				AcquireType type = AcquireType.transferType(player.getActiveClassId());
				if (type == null)
				{
					return;
				}
				
				Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(null, type);
				if (skills.isEmpty())
				{
					player.sendActionFailed();
					return;
				}
				
				boolean reset = false;
				for (SkillLearn skill : skills)
				{
					if (player.getKnownSkill(skill.getId()) != null)
					{
						reset = true;
						break;
					}
				}
				
				if (!reset)
				{
					player.sendActionFailed();
					return;
				}
				
				if (!player.reduceAdena(10000000L, true, ""))
				{
					showChatWindow(player, "common/skill_share_healer_no_adena.htm");
					return;
				}
				
				for (SkillLearn skill : skills)
				{
					if (player.removeSkill(skill.getId(), true) != null)
					{
						// player.getInventory().addItem(skill.getItemId(), skill.getItemCount(), true,"");
						ItemFunctions.addItem(player, skill.getItemId(), skill.getItemCount(), true, "RemoveTransferSkill");
					}
				}
			}
			else if (command.startsWith("ExitFromQuestInstance"))
			{
				Reflection r = player.getReflection();
				r.startCollapseTimer(60000);
				player.teleToLocation(r.getReturnLoc(), 0);
				if (command.length() > 22)
				{
					try
					{
						int val = Integer.parseInt(command.substring(22));
						showChatWindow(player, val);
					}
					catch (NumberFormatException nfe)
					{
						String filename = command.substring(22).trim();
						if (filename.length() > 0)
						{
							showChatWindow(player, filename);
						}
					}
				}
			}
			else if (command.equalsIgnoreCase("event_participate"))
			{
				L2Event.registerPlayer(player);
			}
			else if (command.equalsIgnoreCase("event_unregister"))
			{
				L2Event.removeAndResetPlayer(player);
			}
		}
		catch (StringIndexOutOfBoundsException sioobe)
		{
			_log.info("Incorrect htm bypass! npcId=" + getTemplate().npcId + " command=[" + command + "]");
		}
		catch (NumberFormatException nfe)
		{
			_log.info("Invalid bypass to Server command parameter! npcId=" + getTemplate().npcId + " command=[" + command + "]");
		}
	}
	
	public void showTeleportList(Player player, TeleportLocation[] list)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("&$556;").append("<br><br>");
		
		if (list != null)
		{
			for (TeleportLocation tl : list)
			{
				if (tl.getItem().getItemId() == ItemTemplate.ITEM_ID_ADENA)
				{
					double pricemod = player.getLevel() <= Config.GATEKEEPER_FREE ? 0. : Config.GATEKEEPER_MODIFIER;
					if ((tl.getPrice() > 0) && (pricemod > 0))
					{
						// On Saturdays and Sundays from 8 PM to 12 AM, gatekeeper teleport fees decrease by 50%.
						Calendar calendar = Calendar.getInstance();
						int day = calendar.get(Calendar.DAY_OF_WEEK);
						int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
						if (((day == Calendar.SUNDAY) || (day == Calendar.SATURDAY)) && ((hour >= 20) && (hour <= 12)))
						{
							pricemod /= 2;
						}
					}
					sb.append("[scripts_Util:Gatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ());
					if (tl.getCastleId() != 0)
					{
						sb.append(" ").append(tl.getCastleId());
					}
					if (Config.ALLOW_MULTILANG_GATEKEEPER)
					{
						if (player.getVar("tplangg") == "ru")
						{
							sb.append(" ").append((long) (tl.getPrice() * pricemod)).append(" @811;F;").append(tl.getName()).append("|").append(tl.getStringNameLang());
						}
						else
						{
							sb.append(" ").append((long) (tl.getPrice() * pricemod)).append(" @811;F;").append(tl.getName()).append("|").append(tl.getStringName());
						}
					}
					else
					{
						sb.append(" ").append((long) (tl.getPrice() * pricemod)).append(" @811;F;").append(tl.getName()).append("|").append(tl.getStringName());
					}
					// sb.append(" ").append((long) (tl.getPrice() * pricemod)).append(" @811;F;").append(tl.getName()).append("|").append(HtmlUtils.htmlNpcString(tl.getName()));
					if ((tl.getPrice() * pricemod) > 0)
					{
						sb.append(" - ").append((long) (tl.getPrice() * pricemod)).append(" ").append(HtmlUtils.htmlItemName(ItemTemplate.ITEM_ID_ADENA));
					}
					sb.append("]<br1>\n");
				}
				else
				{
					if (Config.ALLOW_MULTILANG_GATEKEEPER)
					{
						if (player.getVar("tplangg") == "ru")
						{
							sb.append("[scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append("|").append(tl.getStringNameLang()).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId())).append("]<br1>\n");
						}
						else
						{
							sb.append("[scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append("|").append(tl.getStringName()).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId())).append("]<br1>\n");
						}
					}
					else
					{
						sb.append("[scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append("|").append(tl.getStringName()).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId())).append("]<br1>\n");
					}
				}
			}
		}
		else
		{
			sb.append("No teleports available for you.");
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(Strings.bbParse(sb.toString()));
		player.sendPacket(html);
	}
	
	public void showQuestWindow(Player player)
	{
		// collect awaiting quests and start points
		List<Quest> options = new ArrayList<>();
		
		List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);
		
		if (awaits != null)
		{
			for (QuestState x : awaits)
			{
				if (!options.contains(x.getQuest()))
				{
					if (x.getQuest().getQuestIntId() > 0)
					{
						options.add(x.getQuest());
					}
				}
			}
		}
		
		if (starts != null)
		{
			for (Quest x : starts)
			{
				if (!options.contains(x))
				{
					if (x.getQuestIntId() > 0)
					{
						options.add(x);
					}
				}
			}
		}
		
		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}
	
	public void showQuestChooseWindow(Player player, Quest[] quests)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body><title>Talk about:</title><br>");
		
		for (Quest q : quests)
		{
			if (!q.isVisible())
			{
				continue;
			}
			
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
		}
		
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public void showChatWindow(Player player, int val, Object... replace)
	{
		if ((getTemplate().getTeleportList().size() > 0) && checkForDominionWard(player))
		{
			return;
		}
		
		if (isEventMob)
		{
			L2Event.showEventHtml(player, String.valueOf(getObjectId()));
			return;
		}
		
		// Custom for deadmanChest. TODO: Infern0, hardcore this npc.
		if (Config.ENABLE_PLAYER_KILL_SYSTEM && (getNpcId() == 660))
		{
			PlayerKill.getInstance().deadmanChestFuncs(this, player);
			return;
		}
		
		if (!_showBoard.isEmpty())
		{
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(_showBoard);
			if (handler != null)
			{
				handler.onBypassCommand(player, _showBoard);
			}
			return;
		}
		
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int npcId = getNpcId();
		switch (npcId)
		{
			case 31111: // Gatekeeper Spirit (Disciples)
				int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
				int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				int compWinner = SevenSigns.getInstance().getCabalHighestScore();
				if ((playerCabal == sealAvariceOwner) && (playerCabal == compWinner))
				{
					switch (sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
							filename += "spirit_dawn.htm";
							break;
						case SevenSigns.CABAL_DUSK:
							filename += "spirit_dusk.htm";
							break;
						case SevenSigns.CABAL_NULL:
							filename += "spirit_null.htm";
							break;
					}
				}
				else
				{
					filename += "spirit_null.htm";
				}
				break;
			case 31112: // Gatekeeper Spirit (Disciples)
				filename += "spirit_exit.htm";
				break;
			case 30298:
				if (player.getPledgeType() == Clan.SUBUNIT_ACADEMY)
				{
					filename = getHtmlPath(npcId, 1, player);
				}
				else
				{
					filename = getHtmlPath(npcId, 0, player);
				}
				break;
			default:
				if (((npcId >= 31093) && (npcId <= 31094)) || ((npcId >= 31172) && (npcId <= 31201)) || ((npcId >= 31239) && (npcId <= 31254)))
				{
					return;
				}
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, val, player);
				break;
		}
		
		NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, val);
		if ((replace.length % 2) == 0)
		{
			for (int i = 0; i < replace.length; i += 2)
			{
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
			}
		}
		player.sendPacket(packet);
	}
	
	public void showChatWindow(Player player, String filename, Object... replace)
	{
		if (!_showBoard.isEmpty())
		{
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(_showBoard);
			if (handler != null)
			{
				handler.onBypassCommand(player, _showBoard);
			}
			return;
		}
		
		NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, 0);
		if ((replace.length % 2) == 0)
		{
			for (int i = 0; i < replace.length; i += 2)
			{
				packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
			}
		}
		player.sendPacket(packet);
	}
	
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		if (getTemplate().getHtmRoot() != null)
		{
			return getTemplate().getHtmRoot() + pom + ".htm";
		}
		
		String temp = "default/" + pom + ".htm";
		if (HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		
		temp = "trainer/" + pom + ".htm";
		if (HtmCache.getInstance().getNullable(temp, player) != null)
		{
			return temp;
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "npcdefault.htm";
	}
	
	private boolean _isBusy;
	private String _busyMessage = "";
	
	public final boolean isBusy()
	{
		return _isBusy;
	}
	
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}
	
	public final String getBusyMessage()
	{
		return _busyMessage;
	}
	
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}
	
	public void showBusyWindow(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("npcbusy.htm");
		html.replace("%npcname%", getName());
		html.replace("%playername%", player.getName());
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}
	
	public void showSkillList(Player player)
	{
		ClassId classId = player.getClassId();
		
		if (classId == null)
		{
			return;
		}
		
		int npcId = getTemplate().npcId;
		
		if (getTemplate().getTeachInfo().isEmpty())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			if (player.isLangRus())
			{
				sb.append("Î â€• Î Â½Î Âµ Î ÎŒÎ ÎŽÎ Â³Î¡Æ’ Î ÎŽÎ Â±Î¡Æ’Î¡â€¡Î ÎˆÎ¡â€šÎ¡ï¿½ Î¡â€šÎ ÂµÎ Â±Î¡ï¿½. Î â€�Î Â»Î¡ï¿½ Î¡â€šÎ Â²Î ÎŽÎ ÂµÎ Â³Î ÎŽ Î ÎŠÎ Â»Î Â°Î¡ï¿½Î¡ï¿½Î Â° Î ÎŒÎ ÎŽÎ Î‰ Î¡ï¿½Î Î�Î ÎˆÎ¡ï¿½Î ÎŽÎ ÎŠ Î Î�Î¡Æ’Î¡ï¿½Î¡â€š.<br> Î Î…Î Â²Î¡ï¿½Î Â¶Î ÎˆÎ¡ï¿½Î¡ï¿½ Î¡ï¿½ Î Â°Î Î„Î ÎŒÎ ÎˆÎ Â½Î ÎŽÎ ÎŒ Î Î„Î Â»Î¡ï¿½ Î¡â€žÎ ÎˆÎ ÎŠÎ¡ï¿½Î Â° Î¡ï¿½Î¡â€šÎ ÎŽÎ Â³Î ÎŽ. <br>NpcId:" + npcId + ", Î¡â€šÎ Â²Î ÎŽÎ Î‰ classId:" + player.getClassId().getId() + "<br>");
			}
			else
			{
				sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. <br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			}
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			
			return;
		}
		
		if (!(getTemplate().canTeach(classId) || getTemplate().canTeach(classId.getParent(player.getSex()))))
		{
			if (this instanceof WarehouseInstance)
			{
				showChatWindow(player, "warehouse/" + getNpcId() + "-noteach.htm");
			}
			else if (this instanceof TrainerInstance)
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head><body>");
				sb.append(new CustomMessage("l2r.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
			return;
		}
		
		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL);
		
		final AcquireSkillList asl = new AcquireSkillList(AcquireType.NORMAL, skills.size());
		int counts = 0;
		
		for (SkillLearn s : skills)
		{
			if (s.isClicked())
			{
				continue;
			}
			
			Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if ((sk == null) || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
			{
				continue;
			}
			
			counts++;
			
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
		}
		
		if (counts == 0)
		{
			int minlevel = SkillAcquireHolder.getInstance().getMinLevelForNewSkill(player, AcquireType.NORMAL);
			
			if (minlevel > 0)
			{
				SystemMessage2 sm = new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addInteger(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
			}
			player.sendPacket(AcquireSkillDone.STATIC);
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendActionFailed();
	}
	
	public void showTransferSkillList(Player player)
	{
		ClassId classId = player.getClassId();
		if (classId == null)
		{
			return;
		}
		
		if ((player.getLevel() < 76) || (classId.getLevel() < 4))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		
		AcquireType type = AcquireType.transferType(player.getActiveClassId());
		if (type == null)
		{
			return;
		}
		
		showAcquireList(type, player);
	}
	
	public static void showCollectionSkillList(Player player)
	{
		showAcquireList(AcquireType.COLLECTION, player);
	}
	
	public void showTransformationMultisell(Player player)
	{
		if (!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST)
		{
			if (!player.isQuestCompleted("_136_MoreThanMeetsTheEye"))
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-nobuy.htm");
				return;
			}
		}
		
		Castle castle = getCastle(player);
		MultiSellHolder.getInstance().SeparateAndSend(32323, player, castle != null ? castle.getTaxRate() : 0);
		player.sendActionFailed();
	}
	
	public void showTransformationSkillList(Player player, AcquireType type)
	{
		if (!Config.ALLOW_LEARN_TRANS_SKILLS_WO_QUEST)
		{
			if (!player.isQuestCompleted("_136_MoreThanMeetsTheEye"))
			{
				showChatWindow(player, "trainer/" + getNpcId() + "-noquest.htm");
				return;
			}
		}
		
		showAcquireList(type, player);
	}
	
	public static void showFishingSkillList(Player player)
	{
		showAcquireList(AcquireType.FISHING, player);
	}
	
	public static void showClanSkillList(Player player)
	{
		if ((player.getClan() == null) || !player.isClanLeader())
		{
			player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			player.sendActionFailed();
			return;
		}
		
		showAcquireList(AcquireType.CLAN, player);
	}
	
	public static void showAcquireList(AcquireType t, Player player)
	{
		final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);
		
		final AcquireSkillList asl = new AcquireSkillList(t, skills.size());
		
		skills.forEach(s -> asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0));
		
		if (skills.size() == 0)
		{
			player.sendPacket(AcquireSkillDone.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendActionFailed();
	}
	
	public static void showSubUnitSkillList(Player player)
	{
		Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		
		if ((player.getClanPrivileges() & Clan.CP_CL_TROOPS_FAME) != Clan.CP_CL_TROOPS_FAME)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		Set<SkillLearn> learns = new TreeSet<>();
		for (SubUnit sub : player.getClan().getAllSubUnits())
		{
			learns.addAll(SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.SUB_UNIT, sub));
		}
		
		final AcquireSkillList asl = new AcquireSkillList(AcquireType.SUB_UNIT, learns.size());
		
		for (SkillLearn s : learns)
		{
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 1, Clan.SUBUNIT_KNIGHT4);
		}
		
		if (learns.size() == 0)
		{
			player.sendPacket(AcquireSkillDone.STATIC);
			player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendActionFailed();
	}
	
	/**
	 * Î ï¿½Î¡Æ’Î Â¶Î Â½Î ÎŽ Î Î„Î Â»Î¡ï¿½ Î ÎŽÎ¡â€šÎ ÎŽÎ Â±Î¡â‚¬Î Â°Î Â¶Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î Â°Î Â½Î ÎˆÎ ÎŒÎ Â°Î¡â€ Î ÎˆÎ Îˆ Î¡ï¿½Î Î�Î Â°Î¡Æ’Î Â½Î Â°, Î ÎˆÎ¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î¡Æ’Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Â² Î Î�Î Â°Î ÎŠÎ ÂµÎ¡â€šÎ Âµ NpcInfo: 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	public int getSpawnAnimation()
	{
		return _spawnAnimation;
	}
	
	@Override
	public double getColRadius()
	{
		return getCollisionRadius();
	}
	
	@Override
	public double getColHeight()
	{
		return getCollisionHeight();
	}
	
	public int calculateLevelDiffForDrop(int charLevel)
	{
		return calculateLevelDiffForDrop(getLevel(), charLevel, this instanceof RaidBossInstance);
	}
	
	public static int calculateLevelDiffForDrop(int mobLevel, int charLevel, boolean boss)
	{
		if (!Config.DEEPBLUE_DROP_RULES)
		{
			return 0;
		}
		// According to official data (Prima), deep blue mobs are 9 or more levels below players
		int deepblue_maxdiff = boss ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;
		
		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}
	
	public boolean isSevenSignsMonster()
	{
		return getFaction().getName().equalsIgnoreCase("c_dungeon_clan");
	}
	
	@Override
	public String toString()
	{
		return getNpcId() + " " + getName();
	}
	
	public void refreshID()
	{
		objectId = IdFactory.getInstance().getNextId();
		_storedId = GameObjectsStorage.refreshId(this);
	}
	
	private boolean _isUnderground = false;
	
	public void setUnderground(boolean b)
	{
		_isUnderground = b;
	}
	
	public boolean isUnderground()
	{
		return _isUnderground;
	}
	
	public boolean isTargetable()
	{
		return _isTargetable;
	}
	
	public void setTargetable(boolean value)
	{
		_isTargetable = value;
	}
	
	public boolean isShowName()
	{
		return _showName;
	}
	
	public void setShowName(boolean value)
	{
		_showName = value;
	}
	
	@Override
	public NpcListenerList getListeners()
	{
		if (listeners == null)
		{
			synchronized (this)
			{
				if (listeners == null)
				{
					listeners = new NpcListenerList(this);
				}
			}
		}
		
		return (NpcListenerList) listeners;
	}
	
	public <T extends NpcListener> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}
	
	public <T extends NpcListener> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}
	
	@Override
	public NpcStatsChangeRecorder getStatsRecorder()
	{
		if (_statsRecorder == null)
		{
			synchronized (this)
			{
				if (_statsRecorder == null)
				{
					_statsRecorder = new NpcStatsChangeRecorder(this);
				}
			}
		}
		
		return (NpcStatsChangeRecorder) _statsRecorder;
	}
	
	public void setNpcState(int stateId)
	{
		broadcastPacket(new ExChangeNpcState(getObjectId(), stateId));
		npcState = stateId;
	}
	
	public int getNpcState()
	{
		return npcState;
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer)
	{
		List<L2GameServerPacket> list = new ArrayList<>(3);
		list.add(new NpcInfo(this, forPlayer));
		
		if (isInCombat())
		{
			list.add(new AutoAttackStart(getObjectId()));
		}
		
		if (isMoving || isFollow)
		{
			list.add(movePacket());
		}
		
		return list;
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
	
	@Override
	public int getGeoZ(Location loc)
	{
		if (isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
		{
			return loc.z;
		}
		if (isNpc())
		{
			if (_spawnRange instanceof Territory)
			{
				return GeoEngine.getHeight(loc, getGeoIndex());
			}
			return loc.z;
		}
		
		return super.getGeoZ(loc);
	}
	
	@Override
	public Clan getClan()
	{
		Dominion dominion = getDominion();
		if (dominion == null)
		{
			return null;
		}
		int lordObjectId = dominion.getLordObjectId();
		return lordObjectId == 0 ? null : dominion.getOwner();
	}
	
	public NpcString getNameNpcString()
	{
		return _nameNpcString;
	}
	
	public NpcString getTitleNpcString()
	{
		return _titleNpcString;
	}
	
	public void setNameNpcString(NpcString nameNpcString)
	{
		_nameNpcString = nameNpcString;
	}
	
	public void setTitleNpcString(NpcString titleNpcString)
	{
		_titleNpcString = titleNpcString;
	}
	
	public boolean isMerchantNpc()
	{
		return false;
	}
	
	public SpawnRange getSpawnRange()
	{
		return _spawnRange;
	}
	
	public void setSpawnRange(SpawnRange spawnRange)
	{
		_spawnRange = spawnRange;
	}
	
	public boolean checkForDominionWard(Player player)
	{
		ItemInstance item = getActiveWeaponInstance();
		if ((item != null) && (item.getAttachment() instanceof TerritoryWardObject))
		{
			showChatWindow(player, "flagman.htm");
			return true;
		}
		return false;
	}
	
	public void removeParameter(final Object key)
	{
		if (_parameters.isEmpty())
		{
			return;
		}
		_parameters.remove(key);
	}
	
	// public void removeParameter(final Parameter parameter) {
	// if (_parameters.isEmpty()) {
	// return;
	// }
	// _parameters.remove(parameter.name().toLowerCase());
	// }
	
	public void setParameter(String str, Object val)
	{
		if (_parameters == StatsSet.EMPTY)
		{
			_parameters = new StatsSet();
		}
		
		_parameters.set(str, val);
	}
	
	public void setParameters(MultiValueSet<String> set)
	{
		if (set.isEmpty())
		{
			return;
		}
		
		if (_parameters == StatsSet.EMPTY)
		{
			_parameters = new MultiValueSet<>(set.size());
		}
		
		_parameters.putAll(set);
	}
	
	public int getParameter(String str, int val)
	{
		return _parameters.getInteger(str, val);
	}
	
	public long getParameter(String str, long val)
	{
		return _parameters.getLong(str, val);
	}
	
	public boolean getParameter(String str, boolean val)
	{
		return _parameters.getBool(str, val);
	}
	
	public String getParameter(String str, String val)
	{
		return _parameters.getString(str, val);
	}
	
	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}
	
	public boolean isHasChatWindow()
	{
		return _hasChatWindow;
	}
	
	public void setHasChatWindow(boolean hasChatWindow)
	{
		_hasChatWindow = hasChatWindow;
	}
	
	public void onMenuSelect(Player player, int ask, int reply)
	{
		if(getAI() !=null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_MENU_SELECTED,player,Integer.valueOf(ask),Integer.valueOf(reply));
		}
	}
	
	public String getTypeName()
	{
		return getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "");
	}
	
	public int getAISpawnParam()
	{
		return _aiSpawnParam;
	}
	
	public NpcInstance scheduleDespawn(long delay)
	{
		ThreadPoolManager.getInstance().schedule(() ->
		{
			if (!isDeleted())
			{
				deleteMe();
			}
		}, delay);
		return this;
	}
	
	private Map<String, String> _npcSession;
	
	public String getSession(String key)
	{
		if (_npcSession == null)
		{
			return null;
		}
		return _npcSession.get(key);
	}
	
	public void setSession(String key, String val)
	{
		if (_npcSession == null)
		{
			_npcSession = new ConcurrentHashMap<>();
		}
		
		if ((val == null) || val.isEmpty())
		{
			_npcSession.remove(key);
		}
		else
		{
			_npcSession.put(key, val);
		}
	}
	
	public boolean getVarB(String name, boolean defaultVal)
	{
		String pv = getSession(name);
		
		if (pv == null)
		{
			return defaultVal;
		}
		
		return Boolean.valueOf(pv);
	}
	
	public boolean getVarB(String name)
	{
		return getVarB(name, false);
	}
	
	public long getVarLong(String name)
	{
		return getVarLong(name, 0L);
	}
	
	public long getVarLong(String name, long defaultVal)
	{
		long result = defaultVal;
		String var = getSession(name);
		if (var != null)
		{
			result = Long.parseLong(var);
		}
		return result;
	}
	
	public int getVarInt(String name)
	{
		return getVarInt(name, 0);
	}
	
	public int getVarInt(String name, int defaultVal)
	{
		int result = defaultVal;
		String var = getSession(name);
		if (var != null)
		{
			result = Integer.parseInt(var);
		}
		return result;
	}
	
	public NpcInstance createOnePrivate(int npcId, int x, int y, int z)
	{
		return NpcUtils.spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0);
	}
	
	public void lookNeighbor(int range)
	{
		lookNeighbor(range, false);
	}
	
	protected GCSArray<Long> neighbors;
	private long lastNeighborsClean;
	
	public void lookNeighbor(int range, boolean force)
	{
		if (neighbors == null)
		{
			neighbors = new GCSArray<>();
		}
		else if (force || ((lastNeighborsClean + 30000) < System.currentTimeMillis()))
		{
			lastNeighborsClean = System.currentTimeMillis();
			neighbors.clear();
		}
		
		for (Creature cha : World.getAroundCharacters(this))
		{
			if (!cha.isPlayer() || (!cha.isHide() && !((Player) cha).isInvisible()))
			{
				if (isInRange(cha, range) && !neighbors.contains(cha.getStoredId()))
				{
					getAI().notifyEvent(CtrlEvent.EVT_SEE_CREATURE, cha);
					neighbors.add(cha.getStoredId());
				}
			}
		}
		
		for (Long storedId : neighbors)
		{
			Creature c0 = GameObjectsStorage.getAsCharacter(storedId);
			if ((c0 == null) || !isInRange(c0, range))
			{
				neighbors.remove(storedId);
				getAI().notifyEvent(CtrlEvent.EVT_CREATURE_LOST, c0, GameObjectsStorage.getStoredObjectId(storedId));
			}
		}
	}
	
	// New code to test
	
	public int getParam2()
	{
		return _param2;
	}
	
	public void setParam2(final int i0)
	{
		_param2 = i0;
	}
	
	public int getParam3()
	{
		return _param3;
	}
	
	public void setParam3(final int i1)
	{
		_param3 = i1;
	}
	
	public Creature getParam4()
	{
		return _param4;
	}
	
	public void setParam4(final Creature arg)
	{
		_param4 = arg;
	}
	
	public boolean isTeleportNpc()
	{
		return false;
	}
	
	public PrivatesList getPrivatesList()
	{
		if (_privatesList == null)
		{
			_privatesList = new PrivatesList(this);
		}
		
		return _privatesList;
	}
	
	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ ÐµÑ�Ñ‚ÑŒ Ð»Ð¸ Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð·Ð°Ñ�Ð¿Ð°Ð²Ð½ÐµÐ½Ð½Ñ‹Ñ… Ð¼Ð¸Ð½Ð¸Ð¾Ð½Ð¾Ð².
	 * @return Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ ÐµÑ�Ñ‚ÑŒ Ð»Ð¸ Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð·Ð°Ñ�Ð¿Ð°Ð²Ð½ÐµÐ½Ð½Ñ‹Ñ… Ð¼Ð¸Ð½Ð¸Ð¾Ð½Ð¾Ð².
	 */
	public boolean hasPrivates()
	{
		return (_privatesList != null) && _privatesList.hasMinions();
	}
	
	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ ÐµÑ�Ñ‚ÑŒ Ð»Ð¸ Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð·Ð°Ñ�Ð¿Ð°Ð²Ð½ÐµÐ½Ð½Ñ‹Ñ… Ð¼Ð¸Ð½Ð¸Ð¾Ð½Ð¾Ð².
	 * @return Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ ÐµÑ�Ñ‚ÑŒ Ð»Ð¸ Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð·Ð°Ñ�Ð¿Ð°Ð²Ð½ÐµÐ½Ð½Ñ‹Ñ… Ð¼Ð¸Ð½Ð¸Ð¾Ð½Ð¾Ð².
	 */
	public boolean hasOnePrivateEx()
	{
		return (_privatesList != null) && _privatesList.hasOnePrivateEx();
	}
	
	public void setLeader(final NpcInstance leader, final boolean ignoreLeaderAction)
	{
		_master = leader;
		_ignoreLeaderAction = ignoreLeaderAction;
	}
	
	public NpcInstance getLeader()
	{
		return _master;
	}
	
	public void setLeader(final NpcInstance leader)
	{
		_master = leader;
	}
	
	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}
	
	/**
	 * Ð˜Ð³Ð½Ð¾Ñ€Ð¸Ñ€ÑƒÐµÑ‚ Ð¾Ð¿Ð¾Ð²ÐµÑ‰ÐµÐ½Ð¸Ñ� Ð¾Ð± Ð°Ñ‚Ð°ÐºÐµ, Ñ€Ð°Ñ�Ñ�Ñ‚Ð¾Ñ�Ð½Ð¸Ðµ Ð¾Ñ‚ Ð±Ð¾Ñ�Ñ�Ð° Ð´Ð¾ Ð¼Ð¸Ð½Ð¸Ð¾Ð½Ð° Ð¸ Ñ‚Ð´.
	 * @return
	 */
	public boolean isIgnoreLeaderAction()
	{
		return _ignoreLeaderAction;
	}
	
	/**
	 * Ð¡Ð¿Ð°Ð²Ð½Ð¸Ñ‚ Ð¼Ð¸Ð½Ð¸Ð¾Ð½Ð°
	 * @param minion - npcId Ð¼Ð¸Ð½ÑŒÐ¾Ð½Ð°
	 */
	public void spawnMinion(final NpcInstance minion)
	{
		minion.setReflection(getReflection());
		minion.setHeading(getHeading());
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp(), true);
		minion.spawnMe(getMinionPosition());
		if (isRunning())
		{
			minion.setRunning();
		}
	}
	
	public void minionDie(final NpcInstance minion, final int respawn)
	{
		if (respawn > 0)
		{
			_minionRespawnTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl()
				{
					if (!_master.isAlikeDead() && _master.isVisible() && !minion.isVisible())
					{
							minion.refreshID();
							_master.spawnMinion(minion);
					}
				}
			}, respawn);
		}
	}
	
	public void stopMinionRespawnTask()
	{
		if (_minionRespawnTask != null)
		{
			_minionRespawnTask.cancel(false);
			_minionRespawnTask = null;
		}
	}
	
	@Override
	public void setReflection(final Reflection reflection)
	{
		super.setReflection(reflection);
		
		if (hasPrivates())
		{
			for (final NpcInstance m : getPrivatesList().getAlivePrivates())
			{
				m.setReflection(reflection);
			}
		}
	}
	
	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ñ€Ð°Ð½Ð´Ð¾Ð¼Ð½ÑƒÑŽ Ñ‚Ð¾Ñ‡ÐºÑƒ Ð´Ð»Ñ� Ñ�Ð¿Ð°Ð²Ð½Ð° Ð² Ñ€Ð°Ð´Ð¸ÑƒÑ�Ðµ
	 * @return Ð³ÐµÐ½ÐµÑ€Ð¸Ñ€ÑƒÐµÐ¼ Ñ€Ð°Ð½Ð´Ð¾Ð¼Ð½ÑƒÑŽ Ñ‚Ð¾Ñ‡ÐºÑƒ Ð´Ð»Ñ� Ñ�Ð¿Ð°Ð²Ð½Ð° Ð² Ñ€Ð°Ð´Ð¸ÑƒÑ�Ðµ
	 */
	public Location getMinionPosition()
	{
		return Location.findPointToStay(this, 30, 50);
	}
}