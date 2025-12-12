package l2r.gameserver.model.quest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.cache.ImagesCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.SpawnManager;
import l2r.gameserver.listener.actor.OnDeathListener;
import l2r.gameserver.listener.actor.OnKillListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExShowQuestMark;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.QuestList;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialEnableClientEvent;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.TutorialShowQuestionMark;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.spawn.PeriodOfDay;
import l2r.gameserver.utils.AddonsConfig;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;

public final class QuestState
{
	public class OnDeathListenerImpl implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			Player player = actor.getPlayer();
			if (player == null)
			{
				return;
			}
			
			player.removeListener(this);
			
			_quest.notifyDeath(killer, actor, QuestState.this);
		}
	}
	
	public class PlayerOnKillListenerImpl implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			if (!victim.isPlayer())
			{
				return;
			}
			
			Player actorPlayer = (Player) actor;
			List<Player> players = null;
			switch (_quest.getParty())
			{
				case Quest.PARTY_NONE:
					players = Collections.singletonList(actorPlayer);
					break;
				case Quest.PARTY_ALL:
					if (actorPlayer.getParty() == null)
					{
						players = Collections.singletonList(actorPlayer);
					}
					else
					{
						players = new ArrayList<>(actorPlayer.getParty().size());
						for (Player $member : actorPlayer.getParty())
						{
							if ($member.isInRange(actorPlayer, Creature.INTERACTION_DISTANCE))
							{
								players.add($member);
							}
						}
					}
					break;
				default:
					players = Collections.emptyList();
					break;
			}
			
			for (Player player : players)
			{
				QuestState questState = player.getQuestState(_quest.getClass());
				if ((questState != null) && !questState.isCompleted())
				{
					_quest.notifyKill((Player) victim, questState);
				}
			}
		}
		
		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}
	
	private static final Logger _log = LoggerFactory.getLogger(QuestState.class);
	
	public static final int RESTART_HOUR = 6;
	public static final int RESTART_MINUTES = 30;
	public static final String VAR_COND = "cond";
	
	public final static QuestState[] EMPTY_ARRAY = new QuestState[0];
	
	private final Player _player;
	private final Quest _quest;
	private int _state;
	private Integer _cond = null;
	private final Map<String, String> _vars = new ConcurrentHashMap<>();
	private final Map<String, QuestTimer> _timers = new ConcurrentHashMap<>();
	private OnKillListener _onKillListener = null;
	
	/**
	 * Constructor<?> of the QuestState : save the quest in the list of quests of the player.<BR/>
	 * <BR/>
	 * <p/>
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
	 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
	 * <LI>Add drops gotten by the quest</LI> <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : L2Player pointing out the player
	 * @param state : state of the quest
	 */
	public QuestState(Quest quest, Player player, int state)
	{
		_quest = quest;
		_player = player;
		
		// Save the state of the quest for the player in the player's list of quest onwed
		player.setQuestState(this);
		
		// set the state of the quest
		_state = state;
		quest.notifyCreate(this);
	}
	
	/**
	 * Add XP and SP as quest reward <br>
	 * <br>
	 * Î ï¿½Î ÂµÎ¡â€šÎ ÎŽÎ Î„ Î¡Æ’Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ¡â€¹! 3-Î ÎˆÎ Î‰ Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬ true/false Î Î�Î ÎŽÎ ÎŠÎ Â°Î Â·Î¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î¡ï¿½Î Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Â»Î Îˆ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€š Î Â½Î Â° Î Î�Î¡â‚¬Î ÎŽÎ¡â€žÎ ÂµÎ¡ï¿½Î¡ï¿½Î ÎˆÎ¡ï¿½ Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ¡â€¹ Î¡Æ’Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î¡ï¿½Î¡â€šÎ¡ï¿½Î¡ï¿½ Î Â² Î Â·Î Â°Î Â²Î ÎˆÎ ÎŒÎ ÎˆÎ¡ï¿½Î ÎŽÎ ÎŒÎ¡â€šÎ Îˆ Î ÎŽÎ¡â€š Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î Â° RateQuestsRewardOccupationChange
	 */
	public void addExpAndSp(long exp, long sp)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		if (exp > 0)
		{
			player.addExpAndSp((long) (exp * getRateQuestsRewardExpSp()), 0);
		}
		if (sp > 0)
		{
			player.addExpAndSp(0, (long) (sp * getRateQuestsRewardExpSp()));
		}
	}
	
	/**
	 * Add player to get notification of characters death
	 * @param player : L2Character of the character to get notification of death
	 */
	public void addNotifyOfDeath(Player player, boolean withPet)
	{
		OnDeathListenerImpl listener = new OnDeathListenerImpl();
		player.addListener(listener);
		if (withPet)
		{
			Summon summon = player.getPet();
			if (summon != null)
			{
				summon.addListener(listener);
			}
		}
	}
	
	public void addPlayerOnKillListener()
	{
		if (_onKillListener != null)
		{
			throw new IllegalArgumentException("Cant add twice kill listener to player");
		}
		
		_onKillListener = new PlayerOnKillListenerImpl();
		_player.addListener(_onKillListener);
	}
	
	public void removePlayerOnKillListener()
	{
		if (_onKillListener != null)
		{
			_player.removeListener(_onKillListener);
		}
	}
	
	public void addRadar(int x, int y, int z)
	{
		Player player = getPlayer();
		if (player != null)
		{
			player.addRadar(x, y, z);
		}
	}
	
	public void addRadarWithMap(int x, int y, int z)
	{
		Player player = getPlayer();
		if (player != null)
		{
			player.addRadarWithMap(x, y, z);
		}
	}
	
	/**
	 * Î ï¿½Î¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î¡Æ’Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Î„Î Â»Î¡ï¿½ Î ÎŽÎ Î„Î Â½Î ÎŽÎ Î„Î Â½Î ÂµÎ Â²Î Â½Î¡â€¹Î¡â€¦ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ ÎŽÎ Â²
	 */
	public void exitCurrentQuest(Quest quest)
	{
		Player player = getPlayer();
		exitCurrentQuest(true);
		quest.newQuestState(player, Quest.DELAYED);
		QuestState qs = player.getQuestState(quest.getClass());
		qs.setRestartTime();
	}
	
	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 * @return QuestState
	 */
	public QuestState exitCurrentQuest(boolean repeatable)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return this;
		}
		
		removePlayerOnKillListener();
		// Clean drops
		for (int itemId : _quest.getItems())
		{
			// Get [item from] / [presence of the item in] the inventory of the player
			ItemInstance item = player.getInventory().getItemByItemId(itemId);
			if ((item == null) || (itemId == 57))
			{
				continue;
			}
			long count = item.getCount();
			// If player has the item in inventory, destroy it (if not gold)
			// player.getInventory().destroyItemByItemId(itemId, count);
			// player.getWarehouse().destroyItemByItemId(itemId, count);// TODO [G1ta0] analyze this
			player.getInventory().destroyItemByItemId(itemId, count, "Exiting Quest " + _quest.getName());
			player.getWarehouse().destroyItemByItemId(itemId, count, "WH " + player.toString(), "Exiting Quest " + _quest.getName());// TODO [G1ta0] analyze this
		}
		
		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		if (repeatable)
		{
			player.removeQuestState(_quest.getName());
			Quest.deleteQuestInDb(this);
			_vars.clear();
		}
		else
		{
			// Otherwise, delete variables for quest and update database (quest CANNOT be created again => not repeatable)
			for (String var : _vars.keySet())
			{
				if (var != null)
				{
					unset(var);
				}
			}
			
			setState(Quest.COMPLETED);
			Quest.updateQuestInDb(this); // FIXME: Î ÎŽÎ Â½Î ÎŽ Î Â²Î¡â‚¬Î ÎŽÎ Î„Î Âµ Î Â½Î Âµ Î Â½Î¡Æ’Î Â¶Î Â½Î ÎŽ?
		}
		player.sendPacket(new QuestList(player));
		return this;
	}
	
	public void abortQuest()
	{
		_quest.onAbort(this);
		exitCurrentQuest(true);
	}
	
	/**
	 * <font color=red>Î ï¿½Î Âµ Î ÎˆÎ¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î ÎŽÎ Â²Î Â°Î¡â€šÎ¡ï¿½ Î Î„Î Â»Î¡ï¿½ Î Î�Î ÎŽÎ Â»Î¡Æ’Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î ÎŠÎ ÎŽÎ Â½Î Î„Î ÎŽÎ Â²!</font><br>
	 * <br>
	 * <p/>
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return Object
	 */
	public String get(String var)
	{
		return _vars.get(var);
	}
	
	public Map<String, String> getVars()
	{
		return _vars;
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ ÎŒÎ ÂµÎ Â½Î Â½Î¡Æ’Î¡ï¿½ Î Â² Î Â²Î ÎˆÎ Î„Î Âµ Î¡â€ Î ÂµÎ Â»Î ÎŽÎ Â³Î ÎŽ Î¡â€¡Î ÎˆÎ¡ï¿½Î Â»Î Â°.
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
	public int getInt(String var)
	{
		int varint = 0;
		try
		{
			String val = get(var);
			if (val == null)
			{
				return 0;
			}
			varint = Integer.parseInt(val);
		}
		catch (Exception e)
		{
			_log.error(getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint, e);
		}
		return varint;
	}
	
	/**
	 * Return item number which is equipped in selected slot
	 * @return int
	 */
	public int getItemEquipped(int loc)
	{
		return getPlayer().getInventory().getPaperdollItemId(loc);
	}
	
	/**
	 * @return L2Player
	 */
	public Player getPlayer()
	{
		return _player;
	}
	
	/**
	 * Return the quest
	 * @return Quest
	 */
	public Quest getQuest()
	{
		return _quest;
	}
	
	public boolean checkQuestItemsCount(int... itemIds)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return false;
		}
		for (int itemId : itemIds)
		{
			if (player.getInventory().getCountOf(itemId) <= 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public long getSumQuestItemsCount(int... itemIds)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return 0;
		}
		long count = 0;
		for (int itemId : itemIds)
		{
			count += player.getInventory().getCountOf(itemId);
		}
		return count;
	}
	
	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId : ID of the item wanted to be count
	 * @return int
	 */
	public long getQuestItemsCount(int itemId)
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getInventory().getCountOf(itemId);
	}
	
	public long getQuestItemsCount(int... itemsIds)
	{
		long result = 0;
		for (int id : itemsIds)
		{
			result += getQuestItemsCount(id);
		}
		return result;
	}
	
	public boolean haveQuestItem(int itemId, int count)
	{
		if (getQuestItemsCount(itemId) >= count)
		{
			return true;
		}
		return false;
	}
	
	public boolean haveQuestItem(int itemId)
	{
		return haveQuestItem(itemId, 1);
	}
	
	public int getState()
	{
		return _state == Quest.DELAYED ? Quest.CREATED : _state;
	}
	
	public String getStateName()
	{
		return Quest.getStateName(_state);
	}
	
	/**
	 * Î â€�Î ÎŽÎ Â±Î Â°Î Â²Î ÎˆÎ¡â€šÎ¡ï¿½ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€š Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ¡Æ’ By default if item is adena rates 'll be applyed, else no
	 * @param itemId
	 * @param count
	 */
	public void giveItems(int itemId, long count)
	{
		if (itemId == ItemTemplate.ITEM_ID_ADENA)
		{
			giveItems(itemId, count, true);
		}
		else
		{
			giveItems(itemId, count, false);
		}
	}
	
	/**
	 * Î â€�Î ÎŽÎ Â±Î Â°Î Â²Î ÎˆÎ¡â€šÎ¡ï¿½ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€š Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ¡Æ’
	 * @param itemId
	 * @param count
	 * @param rate - Î¡Æ’Î¡â€¡Î ÂµÎ¡â€š Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ ÎŽÎ Â²Î¡â€¹Î¡â€¦ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ ÎŽÎ Â²
	 */
	public void giveItems(int itemId, long count, boolean rate)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (count <= 0)
		{
			count = 1;
		}
		
		if (rate)
		{
			if (itemId != ItemTemplate.ITEM_ID_ADENA)
			{
				count = (long) (count * getRateQuestsReward());
			}
			else if (rate && (itemId == ItemTemplate.ITEM_ID_ADENA))
			{
				count = (long) (count * Config.RATE_QUESTS_REWARD_ADENA);
			}
		}
		
		if (itemId == 57)
		{
			Log.add("Quest|" + getQuest().getQuestIntId() + "|" + count + "|" + player.getName(), "adena");
		}
		
		// player.getInventory().addItem(itemId, count, true);
		ItemFunctions.addItem(player, itemId, count, true, "Quest " + _quest.getName());
		player.sendChanges();
	}
	
	
	public void giveItems(final int itemId, long count, boolean rate, boolean isOnKill)
	{
		final Player player = this.getPlayer();
		final ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		
		if (player == null || item == null)
		{
			return;
		}
		
		if (count <= 0L)
		{
			count = 1L;
		}
		
		final int QId = this.getQuest().getId();
		
		if (rate && isOnKill)
		{
			if (item.isAdena())
			{
				count *= (long)this.getRateQuestsDrop();
			}
			else {
				count *= (long)this.getRateQuestsReward();
			}
		}
		
		ItemFunctions.addItem(player, itemId, count, true,"Items");
		
		
//		if (!this.getQuest().isQuestItem(itemId))
//		{
//            Log.LogEvent(player.getName(), player.getIP(), "QuestFinish" + this.getQuest().getId() + "", "player got " + itemId + " count: " + count);
//		}
		player.sendChanges();
	}
	
	public void giveItems(int itemId, long count, Element element, int power)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (count <= 0)
		{
			count = 1;
		}
		
		// Get template of item
		ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
		if (template == null)
		{
			return;
		}
		
		for (int i = 0; i < count; i++)
		{
			ItemInstance item = new ItemInstance(itemId);
			
			if (element != Element.NONE)
			{
				item.setAttributeElement(element, power);
			}
			
			// Add items to player's inventory
			// player.getInventory().addItem(item);
			player.getInventory().addItem(item, "Quest " + _quest.getName());
		}
		
		player.sendPacket(SystemMessage2.obtainItems(template.getItemId(), count, 0));
		player.sendChanges();
	}
	
	public void dropItem(NpcInstance npc, int itemId, long count)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		ItemInstance item = new ItemInstance(itemId);
		item.setCount(count);
		item.dropToTheGround(player, npc, true);
	}
	
	/**
	 * Î Â­Î¡â€šÎ ÎŽÎ¡â€š Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Î„ Î¡â‚¬Î Â°Î¡ï¿½Î¡ï¿½Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â½Î¡Æ’Î¡â€šÎ¡â€¹Î¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î Â·Î Â°Î Â²Î ÎˆÎ¡ï¿½Î ÎˆÎ ÎŒÎ ÎŽÎ¡ï¿½Î¡â€šÎ Îˆ Î ÎŽÎ¡â€š Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ ÎŽÎ Â². <br>
	 * <br>
	 * Î Î…Î Â»Î ÂµÎ Î„Î¡Æ’Î ÂµÎ¡â€š Î¡Æ’Î¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ¡ï¿½, Î¡â€¡Î¡â€šÎ ÎŽ Î ÎŠÎ ÎŽÎ Â½Î¡â€šÎ¡â‚¬Î ÎŽÎ Â»Î¡ï¿½ Î Â·Î Â° Î Â²Î ÂµÎ¡â‚¬Î¡â€¦Î Â½Î ÎˆÎ ÎŒ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÎŽÎ ÎŒ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â°Î¡â€¦, Î Â² Î ÎŠÎ ÎŽÎ¡â€šÎ ÎŽÎ¡â‚¬Î¡â€¹Î¡â€¦ Î Â½Î¡Æ’Î Â¶Î Â½Î ÎŽ Î Â½Î Â°Î Â±Î ÎˆÎ¡â€šÎ¡ï¿½ Î ÎŽÎ Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÂµÎ Â½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Â² Î Â½Î Âµ Î ÎŽÎ¡ï¿½Î¡Æ’Î¡â€°Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½. <br>
	 * <br>
	 * Î ï¿½Î Îˆ Î ÎŽÎ Î„Î ÎˆÎ Â½ Î ÎˆÎ Â· Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ Î„Î Â°Î Â²Î Â°Î ÂµÎ ÎŒÎ¡â€¹Î¡â€¦ Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î ÎŽÎ Â² Î Â½Î Âµ Î Î„Î ÎŽÎ Â»Î Â¶Î ÂµÎ Â½ Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ Î¡â‚¬Î Â°Î Â²Î ÂµÎ Â½ 0
	 * @param count Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param calcChance Î¡ï¿½Î Â°Î Â½Î¡ï¿½ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦, Î Â² Î Î�Î¡â‚¬Î ÎŽÎ¡â€ Î ÂµÎ Â½Î¡â€šÎ Â°Î¡â€¦
	 * @return Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Î„Î Â»Î¡ï¿½ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â°, Î ÎŒÎ ÎŽÎ Â¶Î ÂµÎ¡â€š Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ 0
	 */
	public int rollDrop(int count, double calcChance)
	{
		if ((calcChance <= 0) || (count <= 0))
		{
			return 0;
		}
		return rollDrop(count, count, calcChance);
	}
	
	/**
	 * Î Â­Î¡â€šÎ ÎŽÎ¡â€š Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Î„ Î¡â‚¬Î Â°Î¡ï¿½Î¡ï¿½Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â½Î¡Æ’Î¡â€šÎ¡â€¹Î¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î Â·Î Â°Î Â²Î ÎˆÎ¡ï¿½Î ÎˆÎ ÎŒÎ ÎŽÎ¡ï¿½Î¡â€šÎ Îˆ Î ÎŽÎ¡â€š Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ ÎŽÎ Â². <br>
	 * <br>
	 * Î Î…Î Â»Î ÂµÎ Î„Î¡Æ’Î ÂµÎ¡â€š Î¡Æ’Î¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ¡ï¿½, Î¡â€¡Î¡â€šÎ ÎŽ Î ÎŠÎ ÎŽÎ Â½Î¡â€šÎ¡â‚¬Î ÎŽÎ Â»Î¡ï¿½ Î Â·Î Â° Î Â²Î ÂµÎ¡â‚¬Î¡â€¦Î Â½Î ÎˆÎ ÎŒ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÎŽÎ ÎŒ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â°Î¡â€¦, Î Â² Î ÎŠÎ ÎŽÎ¡â€šÎ ÎŽÎ¡â‚¬Î¡â€¹Î¡â€¦ Î Â½Î¡Æ’Î Â¶Î Â½Î ÎŽ Î Â½Î Â°Î Â±Î ÎˆÎ¡â€šÎ¡ï¿½ Î ÎŽÎ Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÂµÎ Â½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Â² Î Â½Î Âµ Î ÎŽÎ¡ï¿½Î¡Æ’Î¡â€°Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½. <br>
	 * <br>
	 * Î ï¿½Î Îˆ Î ÎŽÎ Î„Î ÎˆÎ Â½ Î ÎˆÎ Â· Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ Î„Î Â°Î Â²Î Â°Î ÂµÎ ÎŒÎ¡â€¹Î¡â€¦ Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î ÎŽÎ Â² Î Â½Î Âµ Î Î„Î ÎŽÎ Â»Î Â¶Î ÂµÎ Â½ Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ Î¡â‚¬Î Â°Î Â²Î ÂµÎ Â½ 0
	 * @param min Î ÎŒÎ ÎˆÎ Â½Î ÎˆÎ ÎŒÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param max Î ÎŒÎ Â°Î ÎŠÎ¡ï¿½Î ÎˆÎ ÎŒÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param calcChance Î¡ï¿½Î Â°Î Â½Î¡ï¿½ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦, Î Â² Î Î�Î¡â‚¬Î ÎŽÎ¡â€ Î ÂµÎ Â½Î¡â€šÎ Â°Î¡â€¦
	 * @return Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Î„Î Â»Î¡ï¿½ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â°, Î ÎŒÎ ÎŽÎ Â¶Î ÂµÎ¡â€š Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ 0
	 */
	public int rollDrop(int min, int max, double calcChance)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0))
		{
			return 0;
		}
		int dropmult = 1;
		calcChance *= getRateQuestsDrop();
		if (getQuest().getParty() > Quest.PARTY_NONE)
		{
			Player player = getPlayer();
			if (player.getParty() != null)
			{
				calcChance *= Config.ALT_PARTY_BONUS[player.getParty().getMemberCountInRange(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) - 1];
			}
		}
		if (calcChance > 100)
		{
			if ((int) Math.ceil(calcChance / 100) <= (calcChance / 100))
			{
				calcChance = Math.nextUp(calcChance);
			}
			dropmult = (int) Math.ceil(calcChance / 100);
			calcChance = calcChance / dropmult;
		}
		return Rnd.chance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}
	
	public double getRateQuestsDrop()
	{
		Player player = getPlayer();
		double Bonus = player == null ? 1. : player.getBonus().getQuestDropRate();
		if (Config.ALLOW_ADDONS_CONFIG)
		{
			return Config.RATE_QUESTS_DROP * Bonus * AddonsConfig.getQuestDropRates(getQuest());
		}
		return Config.RATE_QUESTS_DROP * Bonus;
	}
	
	public double getRateQuestsReward()
	{
		Player player = getPlayer();
		double Bonus = player == null ? 1. : player.getBonus().getQuestRewardRate();
		if (Config.ALLOW_ADDONS_CONFIG)
		{
			return Config.RATE_QUESTS_REWARD * Bonus * AddonsConfig.getQuestRewardRates(getQuest());
		}
		return Config.RATE_QUESTS_REWARD * Bonus;
	}
	
	public double getRateQuestsRewardExpSp()
	{
		return Config.RATE_QUESTS_REWARD_EXPSP;
	}
	
	/**
	 * Î Â­Î¡â€šÎ ÎŽÎ¡â€š Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Î„ Î¡â‚¬Î Â°Î¡ï¿½Î¡ï¿½Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â½Î¡Æ’Î¡â€šÎ¡â€¹Î¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î Â·Î Â°Î Â²Î ÎˆÎ¡ï¿½Î ÎˆÎ ÎŒÎ ÎŽÎ¡ï¿½Î¡â€šÎ Îˆ Î ÎŽÎ¡â€š Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ ÎŽÎ Â² Î Îˆ Î Î„Î Â°Î ÂµÎ¡â€š Î ÎˆÎ¡â€¦, Î Î�Î¡â‚¬Î ÎŽÎ Â²Î ÂµÎ¡â‚¬Î¡ï¿½Î ÂµÎ¡â€š Î ÎŒÎ Â°Î ÎŠÎ¡ï¿½Î ÎˆÎ ÎŒÎ¡Æ’Î ÎŒ, Î Â° Î¡â€šÎ Â°Î ÎŠ Î Â¶Î Âµ Î Î�Î¡â‚¬Î ÎŽÎ ÎˆÎ Â³Î¡â‚¬Î¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î Â·Î Â²Î¡Æ’Î ÎŠ Î Î�Î ÎŽÎ Â»Î¡Æ’Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î Â²Î ÂµÎ¡â€°Î Îˆ. <br>
	 * <br>
	 * Î ï¿½Î Îˆ Î ÎŽÎ Î„Î ÎˆÎ Â½ Î ÎˆÎ Â· Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ Î„Î Â°Î Â²Î Â°Î ÂµÎ ÎŒÎ¡â€¹Î¡â€¦ Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î ÎŽÎ Â² Î Â½Î Âµ Î Î„Î ÎŽÎ Â»Î Â¶Î ÂµÎ Â½ Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ Î¡â‚¬Î Â°Î Â²Î ÂµÎ Â½ 0
	 * @param itemId id Î Â²Î ÂµÎ¡â€°Î Îˆ
	 * @param min Î ÎŒÎ ÎˆÎ Â½Î ÎˆÎ ÎŒÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param max Î ÎŒÎ Â°Î ÎŠÎ¡ï¿½Î ÎˆÎ ÎŒÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param limit Î ÎŒÎ Â°Î ÎŠÎ¡ï¿½Î ÎˆÎ ÎŒÎ¡Æ’Î ÎŒ Î¡â€šÎ Â°Î ÎŠÎ ÎˆÎ¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰
	 * @param calcChance
	 * @return true Î ÂµÎ¡ï¿½Î Â»Î Îˆ Î Î�Î ÎŽÎ¡ï¿½Î Â»Î Âµ Î Â²Î¡â€¹Î Î�Î ÎŽÎ Â»Î Â½Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î„Î ÎŽÎ¡ï¿½Î¡â€šÎ ÎˆÎ Â³Î Â»Î ÎŽ Î Â»Î ÎˆÎ ÎŒÎ ÎˆÎ¡â€šÎ Â°
	 */
	public boolean rollAndGive(int itemId, int min, int max, int limit, double calcChance)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0) || (limit <= 0) || (itemId <= 0))
		{
			return false;
		}
		long count = rollDrop(min, max, calcChance);
		if (count > 0)
		{
			long alreadyCount = getQuestItemsCount(itemId);
			if ((alreadyCount + count) > limit)
			{
				count = limit - alreadyCount;
			}
			if (count > 0)
			{
				giveItems(itemId, count, false);
				if ((count + alreadyCount) < limit)
				{
					playSound(Quest.SOUND_ITEMGET);
				}
				else
				{
					playSound(Quest.SOUND_MIDDLE);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Î Â­Î¡â€šÎ ÎŽÎ¡â€š Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Î„ Î¡â‚¬Î Â°Î¡ï¿½Î¡ï¿½Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â½Î¡Æ’Î¡â€šÎ¡â€¹Î¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î Â·Î Â°Î Â²Î ÎˆÎ¡ï¿½Î ÎˆÎ ÎŒÎ ÎŽÎ¡ï¿½Î¡â€šÎ Îˆ Î ÎŽÎ¡â€š Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ ÎŽÎ Â² Î Îˆ Î Î„Î Â°Î ÂµÎ¡â€š Î ÎˆÎ¡â€¦, Î Â° Î¡â€šÎ Â°Î ÎŠ Î Â¶Î Âµ Î Î�Î¡â‚¬Î ÎŽÎ ÎˆÎ Â³Î¡â‚¬Î¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î Â·Î Â²Î¡Æ’Î ÎŠ Î Î�Î ÎŽÎ Â»Î¡Æ’Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î Â²Î ÂµÎ¡â€°Î Îˆ. <br>
	 * <br>
	 * Î Î…Î Â»Î ÂµÎ Î„Î¡Æ’Î ÂµÎ¡â€š Î¡Æ’Î¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ¡ï¿½, Î¡â€¡Î¡â€šÎ ÎŽ Î ÎŠÎ ÎŽÎ Â½Î¡â€šÎ¡â‚¬Î ÎŽÎ Â»Î¡ï¿½ Î Â·Î Â° Î Â²Î ÂµÎ¡â‚¬Î¡â€¦Î Â½Î ÎˆÎ ÎŒ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÎŽÎ ÎŒ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â°Î¡â€¦, Î Â² Î ÎŠÎ ÎŽÎ¡â€šÎ ÎŽÎ¡â‚¬Î¡â€¹Î¡â€¦ Î Â½Î¡Æ’Î Â¶Î Â½Î ÎŽ Î Â½Î Â°Î Â±Î ÎˆÎ¡â€šÎ¡ï¿½ Î ÎŽÎ Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÂµÎ Â½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Â² Î Â½Î Âµ Î ÎŽÎ¡ï¿½Î¡Æ’Î¡â€°Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½. <br>
	 * <br>
	 * Î ï¿½Î Îˆ Î ÎŽÎ Î„Î ÎˆÎ Â½ Î ÎˆÎ Â· Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ Î„Î Â°Î Â²Î Â°Î ÂµÎ ÎŒÎ¡â€¹Î¡â€¦ Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î ÎŽÎ Â² Î Â½Î Âµ Î Î„Î ÎŽÎ Â»Î Â¶Î ÂµÎ Â½ Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ Î¡â‚¬Î Â°Î Â²Î ÂµÎ Â½ 0
	 * @param itemId id Î Â²Î ÂµÎ¡â€°Î Îˆ
	 * @param min Î ÎŒÎ ÎˆÎ Â½Î ÎˆÎ ÎŒÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param max Î ÎŒÎ Â°Î ÎŠÎ¡ï¿½Î ÎˆÎ ÎŒÎ Â°Î Â»Î¡ï¿½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param calcChance
	 */
	public void rollAndGive(int itemId, int min, int max, double calcChance)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0) || (itemId <= 0))
		{
			return;
		}
		int count = rollDrop(min, max, calcChance);
		if (count > 0)
		{
			giveItems(itemId, count, false);
			playSound(Quest.SOUND_ITEMGET);
		}
	}
	
	/**
	 * Î Â­Î¡â€šÎ ÎŽÎ¡â€š Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Î„ Î¡â‚¬Î Â°Î¡ï¿½Î¡ï¿½Î¡â€¡Î ÎˆÎ¡â€šÎ¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Â½Î¡Æ’Î¡â€šÎ¡â€¹Î¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î Â·Î Â°Î Â²Î ÎˆÎ¡ï¿½Î ÎˆÎ ÎŒÎ ÎŽÎ¡ï¿½Î¡â€šÎ Îˆ Î ÎŽÎ¡â€š Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ ÎŽÎ Â² Î Îˆ Î Î„Î Â°Î ÂµÎ¡â€š Î ÎˆÎ¡â€¦, Î Â° Î¡â€šÎ Â°Î ÎŠ Î Â¶Î Âµ Î Î�Î¡â‚¬Î ÎŽÎ ÎˆÎ Â³Î¡â‚¬Î¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š Î Â·Î Â²Î¡Æ’Î ÎŠ Î Î�Î ÎŽÎ Â»Î¡Æ’Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î Â²Î ÂµÎ¡â€°Î Îˆ. <br>
	 * <br>
	 * Î Î…Î Â»Î ÂµÎ Î„Î¡Æ’Î ÂµÎ¡â€š Î¡Æ’Î¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ¡ï¿½, Î¡â€¡Î¡â€šÎ ÎŽ Î ÎŠÎ ÎŽÎ Â½Î¡â€šÎ¡â‚¬Î ÎŽÎ Â»Î¡ï¿½ Î Â·Î Â° Î Â²Î ÂµÎ¡â‚¬Î¡â€¦Î Â½Î ÎˆÎ ÎŒ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÎŽÎ ÎŒ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ Î Â² Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â°Î¡â€¦, Î Â² Î ÎŠÎ ÎŽÎ¡â€šÎ ÎŽÎ¡â‚¬Î¡â€¹Î¡â€¦ Î Â½Î¡Æ’Î Â¶Î Â½Î ÎŽ Î Â½Î Â°Î Â±Î ÎˆÎ¡â€šÎ¡ï¿½ Î ÎŽÎ Î�Î¡â‚¬Î ÂµÎ Î„Î ÂµÎ Â»Î ÂµÎ Â½Î Â½Î ÎŽÎ Âµ Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Â² Î Â½Î Âµ Î ÎŽÎ¡ï¿½Î¡Æ’Î¡â€°Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½. <br>
	 * <br>
	 * Î ï¿½Î Îˆ Î ÎŽÎ Î„Î ÎˆÎ Â½ Î ÎˆÎ Â· Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ Î„Î Â°Î Â²Î Â°Î ÂµÎ ÎŒÎ¡â€¹Î¡â€¦ Î Î�Î Â°Î¡â‚¬Î Â°Î ÎŒÎ ÂµÎ¡â€šÎ¡â‚¬Î ÎŽÎ Â² Î Â½Î Âµ Î Î„Î ÎŽÎ Â»Î Â¶Î ÂµÎ Â½ Î Â±Î¡â€¹Î¡â€šÎ¡ï¿½ Î¡â‚¬Î Â°Î Â²Î ÂµÎ Â½ 0
	 * @param itemId id Î Â²Î ÂµÎ¡â€°Î Îˆ
	 * @param count Î ÎŠÎ ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î Î�Î¡â‚¬Î Îˆ Î¡â‚¬Î ÂµÎ Î‰Î¡â€šÎ Â°Î¡â€¦ 1Î¡â€¦
	 * @param calcChance
	 */
	public boolean rollAndGive(int itemId, int count, double calcChance)
	{
		if ((calcChance <= 0) || (count <= 0) || (itemId <= 0))
		{
			return false;
		}
		int countToDrop = rollDrop(count, calcChance);
		if (countToDrop > 0)
		{
			giveItems(itemId, countToDrop, false);
			playSound(Quest.SOUND_ITEMGET);
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if quest completed, false otherwise
	 * @return boolean
	 */
	public boolean isCompleted()
	{
		return getState() == Quest.COMPLETED;
	}
	
	/**
	 * Return true if quest started, false otherwise
	 * @return boolean
	 */
	public boolean isStarted()
	{
		return getState() == Quest.STARTED;
	}
	
	/**
	 * Return true if quest created, false otherwise
	 * @return boolean
	 */
	public boolean isCreated()
	{
		return getState() == Quest.CREATED;
	}
	
	public void killNpcByObjectId(int _objId)
	{
		NpcInstance npc = GameObjectsStorage.getNpc(_objId);
		if (npc != null)
		{
			npc.doDie(null);
		}
		else
		{
			_log.warn("Attemp to kill object that is not npc in quest " + getQuest().getQuestIntId());
		}
	}
	
	public String set(String var, String val)
	{
		return set(var, val, true);
	}
	
	public String set(String var, int intval)
	{
		return set(var, String.valueOf(intval), true);
	}
	
	/**
	 * <font color=red>Î ï¿½Î¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î ÎŽÎ Â²Î Â°Î¡â€šÎ¡ï¿½ Î ÎŽÎ¡ï¿½Î¡â€šÎ ÎŽÎ¡â‚¬Î ÎŽÎ Â¶Î Â½Î ÎŽ! Î Î…Î Â»Î¡Æ’Î Â¶Î ÂµÎ Â±Î Â½Î Â°Î¡ï¿½ Î¡â€žÎ¡Æ’Î Â½Î ÎŠÎ¡â€ Î ÎˆÎ¡ï¿½!</font><br>
	 * <br>
	 * <p/>
	 * Î Â£Î¡ï¿½Î¡â€šÎ Â°Î Â½Î Â°Î Â²Î Â»Î ÎˆÎ Â²Î Â°Î ÂµÎ¡â€š Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ ÎŒÎ ÂµÎ Â½Î Â½Î¡Æ’Î¡ï¿½ Î Îˆ Î¡ï¿½Î ÎŽÎ¡â€¦Î¡â‚¬Î Â°Î Â½Î¡ï¿½Î ÂµÎ¡â€š Î Â² Î Â±Î Â°Î Â·Î¡Æ’, Î ÂµÎ¡ï¿½Î Â»Î Îˆ Î¡Æ’Î¡ï¿½Î¡â€šÎ Â°Î Â½Î ÎŽÎ Â²Î Â»Î ÂµÎ Â½ Î¡â€žÎ Â»Î Â°Î Â³. Î â€¢Î¡ï¿½Î Â»Î Îˆ Î Î�Î ÎŽÎ Â»Î¡Æ’Î¡â€¡Î ÂµÎ Â½ cond Î ÎŽÎ Â±Î Â½Î ÎŽÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€š Î¡ï¿½Î Î�Î ÎˆÎ¡ï¿½Î ÎŽÎ ÎŠ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ ÎŽÎ Â² Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ Â° (Î¡â€šÎ ÎŽÎ Â»Î¡ï¿½Î ÎŠÎ ÎŽ Î¡ï¿½ Î¡â€žÎ Â»Î Â°Î Â³Î ÎŽÎ ÎŒ).
	 * @param var : String pointing out the name of the variable for quest
	 * @param val : String pointing out the value of the variable for quest
	 * @param store : Î Î…Î ÎŽÎ¡â€¦Î¡â‚¬Î Â°Î Â½Î¡ï¿½Î ÂµÎ¡â€š Î Â² Î Â±Î Â°Î Â·Î¡Æ’ Î Îˆ Î ÂµÎ¡ï¿½Î Â»Î Îˆ var Î¡ï¿½Î¡â€šÎ ÎŽ cond Î ÎŽÎ Â±Î Â½Î ÎŽÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€š Î¡ï¿½Î Î�Î ÎˆÎ¡ï¿½Î ÎŽÎ ÎŠ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ ÎŽÎ Â² Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ Â°.
	 * @return String (equal to parameter "val")
	 */
	public String set(String var, String val, boolean store)
	{
		if (val == null)
		{
			val = StringUtils.EMPTY;
		}
		
		_vars.put(var, val);
		
		if (store)
		{
			Quest.updateQuestVarInDb(this, var, val);
		}
		
		return val;
	}
	
	/**
	 * Return state of the quest after its initialization.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Remove drops from previous state</LI>
	 * <LI>Set new state of the quest</LI>
	 * <LI>Add drop for new state</LI>
	 * <LI>Update information in database</LI>
	 * <LI>Send packet QuestList to client</LI>
	 * @param state
	 * @return object
	 */
	public Object setState(int state)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return null;
		}
		
		_state = state;
		
		if (getQuest().isVisible() && isStarted())
		{
			player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
		}
		
		Quest.updateQuestInDb(this);
		player.sendPacket(new QuestList(player));
		return state;
	}
	
	public Object setStateAndNotSave(int state)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return null;
		}
		
		_state = state;
		
		if (getQuest().isVisible() && isStarted())
		{
			player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
		}
		
		player.sendPacket(new QuestList(player));
		return state;
	}
	
	/**
	 * Send a packet in order to play sound at client terminal <br>
	 * Quest.<b>SOUND_ITEMGET</b> = "ItemSound.quest_itemget";<br>
	 * Quest.<b>SOUND_ACCEPT</b> = "ItemSound.quest_accept";<br>
	 * Quest.<b>SOUND_MIDDLE</b> = "ItemSound.quest_middle";<br>
	 * Quest.<b>SOUND_FINISH</b> = "ItemSound.quest_finish";<br>
	 * Quest.<b>SOUND_GIVEUP</b> = "ItemSound.quest_giveup";<br>
	 * Quest.<b>SOUND_TUTORIAL</b> = "ItemSound.quest_tutorial";<br>
	 * Quest.<b>SOUND_JACKPOT</b> = "ItemSound.quest_jackpot";<br>
	 * Quest.<b>SOUND_HORROR2</b> = "SkillSound5.horror_02";<br>
	 * Quest.<b>SOUND_BEFORE_BATTLE</b> = "Itemsound.quest_before_battle";<br>
	 * Quest.<b>SOUND_FANFARE_MIDDLE</b> = "ItemSound.quest_fanfare_middle";<br>
	 * Quest.<b>SOUND_FANFARE2</b> = "ItemSound.quest_fanfare_2";<br>
	 * Quest.<b>SOUND_BROKEN_KEY</b> = "ItemSound2.broken_key";<br>
	 * Quest.<b>SOUND_ENCHANT_SUCESS</b> = "ItemSound3.sys_enchant_sucess";<br>
	 * Quest.<b>SOUND_ENCHANT_FAILED</b> = "ItemSound3.sys_enchant_failed";<br>
	 * Quest.<b>SOUND_ED_CHIMES05</b> = "AmdSound.ed_chimes_05";<br>
	 * Quest.<b>SOUND_ARMOR_WOOD_3</b> = "ItemSound.armor_wood_3";<br>
	 * Quest.<b>SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH</b> = "ItemSound.item_drop_equip_armor_cloth";<br>
	 * @param sound
	 */
	public void playSound(String sound)
	{
		Player player = getPlayer();
		if (player != null)
		{
			player.sendPacket(new PlaySound(sound));
		}
	}
	
	public void playTutorialVoice(String voice)
	{
		Player player = getPlayer();
		if (player != null)
		{
			player.sendPacket(new PlaySound(PlaySound.Type.VOICE, voice, 0, 0, player.getLoc()));
		}
	}
	
	public void onTutorialClientEvent(int number)
	{
		Player player = getPlayer();
		if (player != null)
		{
			player.sendPacket(new TutorialEnableClientEvent(number));
		}
	}
	
	public void showQuestionMark(int number)
	{
		Player player = getPlayer();
		if (player != null)
		{
			player.sendPacket(new TutorialShowQuestionMark(number));
		}
	}
	
	public void showTutorialPage(String html)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		String text = HtmCache.getInstance().getNotNull("quests/_255_Tutorial/" + html, player);
		player.sendPacket(new TutorialShowHtml(text));
	}
	
	public void closeTutorial()
	{
		onTutorialClientEvent(0);
		if (_player != null)
		{
			_player.sendPacket(TutorialCloseHtml.STATIC);
			_player.deleteQuickVar("watchingTutorial");
			Quest q = QuestManager.getQuest(255);
			if (q != null)
			{
				_player.processQuestEvent(q.getName(), "onTutorialClose", null);
			}
		}
	}
	
	public void showTutorialHTML(String html)
	{
		if (_player != null)
		{
			// Synerge - Added support for showing crest images on tutorial windows
			html = ImagesCache.getInstance().sendUsedImages(html, _player);
			
			// Synerge - If the html has crests then we should delay the tutorial html so the images reach their destination before the htm
			if (html.startsWith("CREST"))
			{
				ThreadPoolManager.getInstance().schedule(new TutorialShowThread(html.substring(5)), 200);
			}
			else
			{
				_player.sendPacket(new TutorialShowHtml(html));
				_player.addQuickVar("watchingTutorial", true);
			}
		}
	}
	
	private class TutorialShowThread implements Runnable
	{
		private final String _html;
		
		public TutorialShowThread(String html)
		{
			_html = html;
		}
		
		@Override
		public void run()
		{
			if (_player == null)
			{
				return;
			}
			
			_player.sendPacket(new TutorialShowHtml(_html));
			_player.addQuickVar("watchingTutorial", true);
		}
	}
	
	/**
	 * Start a timer for quest.<BR>
	 * <BR>
	 * @param name<BR>
	 *            The name of the timer. Will also be the value for event of onEvent
	 * @param time<BR>
	 *            The milisecond value the timer will elapse
	 */
	public void startQuestTimer(String name, long time)
	{
		startQuestTimer(name, time, null);
	}
	
	/**
	 * Add a timer to the quest.<BR>
	 * <BR>
	 * @param name: name of the timer (also passed back as "event" in notifyEvent)
	 * @param time: time in ms for when to fire the timer
	 * @param npc: npc associated with this timer (can be null)
	 */
	public void startQuestTimer(String name, long time, NpcInstance npc)
	{
		QuestTimer timer = new QuestTimer(name, time, npc);
		timer.setQuestState(this);
		QuestTimer oldTimer = getTimers().put(name, timer);
		if (oldTimer != null)
		{
			oldTimer.stop();
		}
		timer.start();
	}
	
	public boolean isRunningQuestTimer(String name)
	{
		return getTimers().get(name) != null;
	}
	
	public boolean cancelQuestTimer(String name)
	{
		QuestTimer timer = removeQuestTimer(name);
		if (timer != null)
		{
			timer.stop();
		}
		return timer != null;
	}
	
	QuestTimer removeQuestTimer(String name)
	{
		QuestTimer timer = getTimers().remove(name);
		if (timer != null)
		{
			timer.setQuestState(null);
		}
		return timer;
	}
	
	public void pauseQuestTimers()
	{
		getQuest().pauseQuestTimers(this);
	}
	
	public void stopQuestTimers()
	{
		for (QuestTimer timer : getTimers().values())
		{
			timer.setQuestState(null);
			timer.stop();
		}
		_timers.clear();
	}
	
	public void resumeQuestTimers()
	{
		getQuest().resumeQuestTimers(this);
	}
	
	Map<String, QuestTimer> getTimers()
	{
		return _timers;
	}
	
	/**
	 * Î Â£Î Î„Î Â°Î Â»Î¡ï¿½Î ÂµÎ¡â€š Î¡Æ’Î ÎŠÎ Â°Î Â·Î Â°Î Â½Î Â½Î¡â€¹Î Âµ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ¡â€¹ Î ÎˆÎ Â· Î ÎˆÎ Â½Î Â²Î ÂµÎ Â½Î¡â€šÎ Â°Î¡â‚¬Î¡ï¿½ Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ Â°, Î Îˆ Î ÎŽÎ Â±Î Â½Î ÎŽÎ Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€š Î ÎˆÎ Â½Î Â²Î ÂµÎ Â½Î¡â€šÎ Â°Î¡â‚¬Î¡ï¿½
	 * @param itemId : id Î¡Æ’Î Î„Î Â°Î Â»Î¡ï¿½Î ÂµÎ ÎŒÎ ÎŽÎ Â³Î ÎŽ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ Â°
	 * @param count : Î¡â€¡Î ÎˆÎ¡ï¿½Î Â»Î ÎŽ Î¡Æ’Î Î„Î Â°Î Â»Î¡ï¿½Î ÂµÎ ÎŒÎ¡â€¹Î¡â€¦ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Â²<br>
	 *            Î â€¢Î¡ï¿½Î Â»Î Îˆ count Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ Î„Î Â°Î¡â€šÎ¡ï¿½ -1, Î¡â€šÎ ÎŽ Î Â±Î¡Æ’Î Î„Î¡Æ’Î¡â€š Î¡Æ’Î Î„Î Â°Î Â»Î ÂµÎ Â½Î¡â€¹ Î Â²Î¡ï¿½Î Âµ Î¡Æ’Î ÎŠÎ Â°Î Â·Î Â°Î Â½Î Â½Î¡â€¹Î Âµ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ¡â€¹.
	 * @return Î ï¿½Î ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î¡Æ’Î Î„Î Â°Î Â»Î ÂµÎ Â½Î Â½Î¡â€¹Î¡â€¦ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ ÎŽÎ Â²
	 */
	public long takeItems(int itemId, long count)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return 0;
		}
		
		// Get object item from player's inventory list
		ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return 0;
		}
		// Tests on count value in order not to have negative value
		if ((count < 0) || (count > item.getCount()))
		{
			count = item.getCount();
		}
		
		// Destroy the quantity of items wanted
		// player.getInventory().destroyItemByItemId(itemId, count);
		player.getInventory().destroyItemByItemId(itemId, count, "Quest " + _quest.getName());
		// Send message of destruction to client
		player.sendPacket(SystemMessage2.removeItems(itemId, count));
		
		return count;
	}
	
	public long takeAllItems(int itemId)
	{
		return takeItems(itemId, -1);
	}
	
	public long takeAllItems(int... itemsIds)
	{
		long result = 0;
		for (int id : itemsIds)
		{
			result += takeAllItems(id);
		}
		return result;
	}
	
	public long takeAllItems(Collection<Integer> itemsIds)
	{
		long result = 0;
		for (int id : itemsIds)
		{
			result += takeAllItems(id);
		}
		return result;
	}
	
	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U> Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(String var)
	{
		if (var == null)
		{
			return null;
		}
		String old = _vars.remove(var);
		if (old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}
		return old;
	}
	
	private boolean checkPartyMember(Player member, int state, int maxrange, GameObject rangefrom)
	{
		if (member == null)
		{
			return false;
		}
		if ((rangefrom != null) && (maxrange > 0) && !member.isInRange(rangefrom, maxrange))
		{
			return false;
		}
		QuestState qs = member.getQuestState(getQuest().getName());
		if ((qs == null) || (qs.getState() != state))
		{
			return false;
		}
		return true;
	}
	
	public List<Player> getPartyMembers(int state, int maxrange, GameObject rangefrom)
	{
		List<Player> result = new ArrayList<>();
		Party party = getPlayer().getParty();
		if (party == null)
		{
			if (checkPartyMember(getPlayer(), state, maxrange, rangefrom))
			{
				result.add(getPlayer());
			}
			return result;
		}
		
		for (Player _member : party)
		{
			if (checkPartyMember(_member, state, maxrange, rangefrom))
			{
				result.add(getPlayer());
			}
		}
		
		return result;
	}
	
	public Player getRandomPartyMember(int state, int maxrangefromplayer)
	{
		return getRandomPartyMember(state, maxrangefromplayer, getPlayer());
	}
	
	public Player getRandomPartyMember(int state, int maxrange, GameObject rangefrom)
	{
		List<Player> list = getPartyMembers(state, maxrange, rangefrom);
		if (list.size() == 0)
		{
			return null;
		}
		return list.get(Rnd.get(list.size()));
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 */
	public NpcInstance addSpawn(int npcId)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, 0);
	}
	
	public NpcInstance addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, despawnDelay);
	}
	
	public NpcInstance addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, 0, 0);
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Return object id of newly spawned npc
	 */
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, 0, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 */
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, int randomOffset, int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
	}
	
	public NpcInstance findTemplate(int npcId)
	{
		for (Spawner spawn : SpawnManager.getInstance().getSpawners(PeriodOfDay.NONE.name()))
		{
			if ((spawn != null) && (spawn.getCurrentNpcId() == npcId))
			{
				return spawn.getLastSpawn();
			}
		}
		return null;
	}
	
	public int calculateLevelDiffForDrop(int mobLevel, int player)
	{
		if (!Config.DEEPBLUE_DROP_RULES)
		{
			return 0;
		}
		return Math.max(player - mobLevel - Config.DEEPBLUE_DROP_MAXDIFF, 0);
	}
	
	public int getCond()
	{
		if (_cond == null)
		{
			int val = getInt(VAR_COND);
			if ((val & 0x80000000) != 0)
			{
				val &= 0x7fffffff;
				for (int i = 1; i < 32; i++)
				{
					val = (val >> 1);
					if (val == 0)
					{
						val = i;
						break;
					}
				}
			}
			_cond = val;
		}
		
		return _cond.intValue();
	}
	
	public String setCond(int newCond)
	{
		return setCond(newCond, true);
	}
	
	public String setCond(int newCond, boolean store)
	{
		if (newCond == getCond())
		{
			return String.valueOf(newCond);
		}
		
		int oldCond = getInt(VAR_COND);
		_cond = newCond;
		
		if ((oldCond & 0x80000000) != 0)
		{
			// second format already in use
			if (newCond > 2) // If the stage is less than 3, then we return to the first option.
			{
				oldCond &= 0x80000001 | ((1 << newCond) - 1);
				newCond = oldCond | (1 << (newCond - 1));
			}
		}
		else
		{
			// The second option is now always used if the stage is more than 2
			if (newCond > 2)
			{
				newCond = 0x80000001 | (1 << (newCond - 1)) | ((1 << oldCond) - 1);
			}
		}
		
		final String sVal = String.valueOf(newCond);
		final String result = set(VAR_COND, sVal, false);
		if (store)
		{
			Quest.updateQuestVarInDb(this, VAR_COND, sVal);
		}
		
		final Player player = getPlayer();
		if (player != null)
		{
			player.sendPacket(new QuestList(player));
			if ((newCond != 0) && getQuest().isVisible() && isStarted())
			{
				player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
			}
		}
		return result;
	}
	
	/**
	 * Sets the time when the quest will be available to the character. The method is used for quests that take place once a day.
	 */
	public void setRestartTime()
	{
		Calendar reDo = Calendar.getInstance();
		if (reDo.get(Calendar.HOUR_OF_DAY) >= RESTART_HOUR)
		{
			reDo.add(Calendar.DATE, 1);
		}
		reDo.set(Calendar.HOUR_OF_DAY, RESTART_HOUR);
		reDo.set(Calendar.MINUTE, RESTART_MINUTES);
		set("restartTime", String.valueOf(reDo.getTimeInMillis()));
	}
	
	public long getRestartTime()
	{
		String val = get("restartTime");
		if (val == null)
		{
			return 0;
		}
		
		return Long.parseLong(val);
	}
	
	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€Ñ�ÐµÑ‚, Ð½Ð°Ñ�Ñ‚ÑƒÐ¿Ð¸Ð»Ð¾ Ð»Ð¸ Ð²Ñ€ÐµÐ¼Ñ� Ð´Ð»Ñ� Ð²Ñ‹Ð¿Ð¾Ð»Ð½ÐµÐ½Ð¸Ñ� ÐºÐ²ÐµÑ�Ñ‚Ð°. ÐœÐµÑ‚Ð¾Ð´ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� Ð´Ð»Ñ� ÐºÐ²ÐµÑ�Ñ‚Ð¾Ð², ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð¿Ñ€Ð¾Ñ…Ð¾Ð´Ñ�Ñ‚Ñ�Ñ� Ð¾Ð´Ð¸Ð½ Ñ€Ð°Ð· Ð² Ð´ÐµÐ½ÑŒ.
	 * @return boolean
	 */
	public boolean isNowAvailable()
	{
		String val = get("restartTime");
		if (val == null)
		{
			return true;
		}
		
		long restartTime = Long.parseLong(val);
		return restartTime <= System.currentTimeMillis();
	}
	
	/**
	 * Check if a given variable is set for this quest.
	 * @param variable the variable to check
	 * @return {@code true} if the variable is set, {@code false} otherwise
	 * @see #get(String)
	 * @see #getInt(String)
	 * @see #getCond()
	 */
	public boolean isSet(String variable)
	{
		return (get(variable) != null);
	}
}