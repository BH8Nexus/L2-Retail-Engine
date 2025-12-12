package l2r.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import l2r.commons.dbutils.DbUtils;
import l2r.commons.logging.LogUtils;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.commons.util.TroveUtils;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.olympiad.OlympiadGame;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.ExNpcQuestHtmlMessage;
import l2r.gameserver.network.serverpackets.ExQuestNpcLogList;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.NpcUtils;

public class Quest
{
	private static final Logger _log = LoggerFactory.getLogger(Quest.class);
	
	public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
	public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
	public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
	public static final String SOUND_FINISH = "ItemSound.quest_finish";
	public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
	public static final String SOUND_TUTORIAL = "ItemSound.quest_tutorial";
	public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
	public static final String SOUND_HORROR2 = "SkillSound5.horror_02";
	public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
	public static final String SOUND_FANFARE_MIDDLE = "ItemSound.quest_fanfare_middle";
	public static final String SOUND_FANFARE2 = "ItemSound.quest_fanfare_2";
	public static final String SOUND_BROKEN_KEY = "ItemSound2.broken_key";
	public static final String SOUND_ENCHANT_SUCESS = "ItemSound3.sys_enchant_sucess";
	public static final String SOUND_ENCHANT_FAILED = "ItemSound3.sys_enchant_failed";
	public static final String SOUND_ED_CHIMES05 = "AmdSound.ed_chimes_05";
	public static final String SOUND_ARMOR_WOOD_3 = "ItemSound.armor_wood_3";
	public static final String SOUND_ITEM_DROP_EQUIP_ARMOR_CLOTH = "ItemSound.item_drop_equip_armor_cloth";
	
	public static final String NO_QUEST_DIALOG = "no-quest";
	public static final String TUTORIAL = "_255_Tutorial";
	public static final int ADENA_ID = 57;
	
	public static final int PARTY_NONE = 0;
	public static final int PARTY_ONE = 1;
	public static final int PARTY_ALL = 2;
	
	// card with paused quest timers for each player
	private final Map<Integer, Map<String, QuestTimer>> _pausedQuestTimers = new ConcurrentHashMap<>();
	
	private final TIntHashSet _questItems = new TIntHashSet();
	private TIntObjectHashMap<List<QuestNpcLogInfo>> _npcLogList = TroveUtils.emptyIntObjectMap();
	
	/**
	 * This method is for registering quest items that will be deleted upon termination of the quest, regardless of whether it was completed or interrupted. <strong> You cannot add rewards here </strong>.
	 */
	public void addQuestItem(int... ids)
	{
		for (int id : ids)
		{
			if (id != 0)
			{
				ItemTemplate i = null;
				i = ItemHolder.getInstance().getTemplate(id);
				
				if (_questItems.contains(id))
				{
					_log.warn("Item " + i + " multiple times in quest drop in " + getName());
				}
				
				_questItems.add(id);
			}
		}
	}
	
	public int[] getItems()
	{
		return _questItems.toArray();
	}
	
	public boolean isQuestItem(int id)
	{
		return _questItems.contains(id);
	}
	
	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", qs.getStateName());
	}
	
	/**
	 * Insert in the database the quest for the player.
	 * @param qs : QuestState pointing out the state of the quest
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		Player player = qs.getPlayer();
		if (player == null)
		{
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuest().getName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * Delete the player's quest from database.
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(QuestState qs)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuest().getName());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param qs : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuest().getName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.error("could not delete char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * Add quests to the L2Player.<BR>
	 * <BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, drops and variables for quests in the HashMap _quest of L2Player
	 * @param player : Player who is entering the world
	 */
	public static void restoreQuestStates(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement invalidQuestData = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?");
			statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rset = statement.executeQuery();
			while (rset.next())
			{
				String questId = rset.getString("name");
				String state = rset.getString("value");
				
				if (state.equalsIgnoreCase("Start")) // Î Â½Î ÂµÎ Â²Î Â·Î¡ï¿½Î¡â€šÎ¡â€¹Î Î‰ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€š
				{
					invalidQuestData.setInt(1, player.getObjectId());
					invalidQuestData.setString(2, questId);
					invalidQuestData.executeUpdate();
					continue;
				}
				
				// Search quest associated with the ID
				Quest q = QuestManager.getQuest(questId);
				if (q == null)
				{
					if (!Config.DONTLOADQUEST)
					{
						_log.warn("Unknown quest " + questId + " for player " + player.getName());
					}
					continue;
				}
				
				// Create a new QuestState for the player that will be added to the player's list of quests
				new QuestState(q, player, getStateId(state));
			}
			
			DbUtils.close(statement, rset);
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while (rset.next())
			{
				String questId = rset.getString("name");
				String var = rset.getString("var");
				String value = rset.getString("value");
				// Get the QuestState saved in the loop before
				QuestState qs = player.getQuestState(questId);
				if (qs == null)
				{
					continue;
				}
				// Î Â·Î Â°Î¡â€šÎ¡â€¹Î¡â€¡Î ÎŠÎ Â° Î Â½Î Â° Î Î�Î¡â‚¬Î ÎŽÎ Î�Î¡Æ’Î¡â€°Î ÂµÎ Â½Î Â½Î¡â€¹Î Î‰ Î Î�Î ÂµÎ¡â‚¬Î Â²Î¡â€¹Î Î‰ Î ÎŠÎ ÎŽÎ Â½Î Î„
				if (var.equals("cond") && (Integer.parseInt(value) < 0))
				{
					value = String.valueOf(Integer.parseInt(value) | 1);
				}
				// Add parameter to the quest
				qs.set(var, value, false);
			}
		}
		catch (Exception e)
		{
			_log.error("could not insert char quest:", e);
		}
		finally
		{
			DbUtils.closeQuietly(invalidQuestData);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	protected final String _name;
	
	protected final int _party;
	
	protected final int _questId;
	
	public final static int CREATED = 1;
	public final static int STARTED = 2;
	public final static int COMPLETED = 3;
	public final static int DELAYED = 4;
	
	public static String getStateName(int state)
	{
		switch (state)
		{
			case CREATED:
				return "Start";
			case STARTED:
				return "Started";
			case COMPLETED:
				return "Completed";
			case DELAYED:
				return "Delayed";
		}
		return "Start";
	}
	
	public static int getStateId(String state)
	{
		if (state.equalsIgnoreCase("Start"))
		{
			return CREATED;
		}
		else if (state.equalsIgnoreCase("Started"))
		{
			return STARTED;
		}
		else if (state.equalsIgnoreCase("Completed"))
		{
			return COMPLETED;
		}
		else if (state.equalsIgnoreCase("Delayed"))
		{
			return DELAYED;
		}
		return CREATED;
	}
	
	public Quest()
	{
		_name = getClass().getSimpleName();
		_questId = getQuestIntId();
		_party = 0;
		QuestManager.addQuest(this);
	}
	
	/**
	 * Deprecated.
	 */
	public Quest(boolean party)
	{
		this(party ? 1 : 0);
	}
	
	/**
	 * 0 - Î Î�Î ÎŽ Î Â»Î Â°Î¡ï¿½Î¡â€šÎ¡â€¦Î ÎˆÎ¡â€šÎ¡Æ’, 1 - Î¡ï¿½Î Â»Î¡Æ’Î¡â€¡Î Â°Î Î‰Î Â½Î ÎŽ Î Î�Î ÎŽ Î Î�Î Â°Î¡â€šÎ Îˆ, 2 - Î Â²Î¡ï¿½Î ÂµÎ Î‰ Î Î�Î Â°Î¡â€šÎ Îˆ.
	 */
	public Quest(int party)
	{
		_name = getClass().getSimpleName();
		_questId = Integer.parseInt(_name.split("_")[1]);
		_party = party;
		QuestManager.addQuest(this);
	}
	
	public List<QuestNpcLogInfo> getNpcLogList(int cond)
	{
		return _npcLogList.get(cond);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.<BR>
	 * <BR>
	 * @param attackIds
	 */
	public void addAttackId(int... attackIds)
	{
		for (int attackId : attackIds)
		{
			addEventId(attackId, QuestEventType.ATTACKED_WITH_QUEST);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.<BR>
	 * <BR>
	 * @param npcId : id of the NPC to register
	 * @param eventType : type of event being registered
	 * @return int : npcId
	 */
	public NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			NpcTemplate t = NpcHolder.getInstance().getTemplate(npcId);
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			return t;
		}
		catch (Exception e)
		{
			_log.error("", e);
			return null;
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.<BR>
	 * <BR>
	 * @param killIds
	 * @return int : killId
	 */
	public void addKillId(int... killIds)
	{
		for (int killid : killIds)
		{
			addEventId(killid, QuestEventType.MOB_KILLED_WITH_QUEST);
		}
	}
	
	/**
	 * Î â€�Î ÎŽÎ Â±Î Â°Î Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€š Î Â½Î Î�Î¡â€  Î ÎŒÎ Â°Î¡ï¿½Î ÎˆÎ Â² Î Î„Î Â»Î¡ï¿½ Î¡ï¿½Î Â»Î¡Æ’Î¡ï¿½Î Â°Î¡â€šÎ ÂµÎ Â»Î¡ï¿½ Î Î�Î¡â‚¬Î Îˆ Î ÎˆÎ¡â€¦ Î¡Æ’Î Â±Î ÎˆÎ Î‰Î¡ï¿½Î¡â€šÎ Â²Î Âµ, Î Îˆ Î ÎŽÎ Â±Î Â½Î ÎŽÎ Â²Î Â»Î ÂµÎ Â½Î ÎˆÎ Îˆ Î Î�Î Â°Î ÎŠÎ ÂµÎ¡â€šÎ ÎŽÎ ÎŒ {@link l2r.gameserver.network.serverpackets.ExQuestNpcLogList}
	 * @param cond
	 * @param varName
	 * @param killIds
	 */
	public void addKillNpcWithLog(int cond, String varName, int max, int... killIds)
	{
		if (killIds.length == 0)
		{
			throw new IllegalArgumentException("Npc list cant be empty!");
		}
		
		addKillId(killIds);
		if (_npcLogList.isEmpty())
		{
			_npcLogList = new TIntObjectHashMap<>(5);
		}
		
		List<QuestNpcLogInfo> vars = _npcLogList.get(cond);
		if (vars == null)
		{
			_npcLogList.put(cond, (vars = new ArrayList<>(5)));
		}
		
		vars.add(new QuestNpcLogInfo(killIds, varName, max));
	}
	
	public boolean updateKill(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		if (player == null)
		{
			return false;
		}
		List<QuestNpcLogInfo> vars = getNpcLogList(st.getCond());
		if (vars == null)
		{
			return false;
		}
		boolean done = true;
		boolean find = false;
		for (QuestNpcLogInfo info : vars)
		{
			int count = st.getInt(info.getVarName());
			if (!find && ArrayUtils.contains(info.getNpcIds(), npc.getNpcId()))
			{
				find = true;
				if (count < info.getMaxCount())
				{
					st.set(info.getVarName(), ++count);
					player.sendPacket(new ExQuestNpcLogList(st));
				}
			}
			
			if (count != info.getMaxCount())
			{
				done = false;
			}
		}
		
		return done;
	}
	
	public void addKillId(Collection<Integer> killIds)
	{
		for (int killid : killIds)
		{
			addKillId(killid);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-Use Events.<BR>
	 * <BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addSkillUseId(int npcId)
	{
		return addEventId(npcId, QuestEventType.MOB_TARGETED_BY_SKILL);
	}
	
	public void addStartNpc(int... npcIds)
	{
		for (int talkId : npcIds)
		{
			addStartNpc(talkId);
		}
	}
	
	/**
	 * Add the quest to the NPC's startQuest Î â€™Î¡â€¹Î Â·Î¡â€¹Î Â²Î Â°Î ÂµÎ¡â€š addTalkId
	 * @param npcId
	 * @return L2NpcTemplate : Start NPC
	 */
	public NpcTemplate addStartNpc(int npcId)
	{
		addTalkId(npcId);
		return addEventId(npcId, QuestEventType.QUEST_START);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param npcIds
	 * @return L2NpcTemplate : Start NPC
	 */
	public void addFirstTalkId(int... npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(npcId, QuestEventType.NPC_FIRST_TALK);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.<BR>
	 * <BR>
	 * @param talkIds : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public void addTalkId(int... talkIds)
	{
		for (int talkId : talkIds)
		{
			addEventId(talkId, QuestEventType.QUEST_TALK);
		}
	}
	
	public void addTalkId(Collection<Integer> talkIds)
	{
		for (int talkId : talkIds)
		{
			addTalkId(talkId);
		}
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â½Î Â°Î Â·Î Â²Î Â°Î Â½Î ÎˆÎ Âµ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â° (Î â€˜Î ÂµÎ¡â‚¬Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î¡ï¿½ npcstring-*.dat) state 1 = "" state 2 = "In Progress" state 3 = "Done"
	 */
	public String getDescr(Player player)
	{
		if (!isVisible())
		{
			return null;
		}
		
		QuestState qs = player.getQuestState(getName());
		int state = 2;
		if ((qs == null) || (qs.isCreated() && qs.isNowAvailable()))
		{
			state = 1;
		}
		else if (qs.isCompleted() || !qs.isNowAvailable())
		{
			state = 3;
		}
		
		int fStringId = getQuestIntId();
		if (fStringId >= 10000)
		{
			fStringId -= 5000;
		}
		fStringId = (fStringId * 100) + state;
		return HtmlUtils.htmlNpcString(fStringId);
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName()
	{
		return _name;
	}
	
	public int getId()
	{
		return _questId;
	}
	
	/**
	 * Return ID of the quest
	 * @return int
	 */
	public int getQuestIntId()
	{
		return _questId;
	}
	
	/**
	 * Return party state of quest
	 * @return String
	 */
	public int getParty()
	{
		return _party;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 * @param player
	 * @param state
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(Player player, int state)
	{
		QuestState qs = new QuestState(this, player, state);
		Quest.updateQuestInDb(qs);
		return qs;
	}
	
	public QuestState newQuestStateAndNotSave(Player player, int state)
	{
		return new QuestState(this, player, state);
	}
	
	public void notifyAttack(NpcInstance npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onAttack(npc, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyDeath(Creature killer, Creature victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(null, qs.getPlayer(), res);
	}
	
	public void notifyEvent(String event, QuestState qs, NpcInstance npc)
	{
		String res = null;
		try
		{
			// if(npc == null || npc.getTemplate().canTalkThisQuest(this))
			res = onEvent(event, qs, npc);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyKill(NpcInstance npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onKill(npc, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyKill(Player target, QuestState qs)
	{
		String res = null;
		try
		{
			res = onKill(target, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return;
		}
		showResult(null, qs.getPlayer(), res);
	}
	
	/**
	 * Override the default NPC dialogs when a quest defines this for the given NPC
	 */
	public final boolean notifyFirstTalk(NpcInstance npc, Player player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			showError(player, e);
			return true;
		}
		// if the quest returns text to display, display it. Otherwise, use the default npc text.
		return showResult(npc, player, res, true);
	}
	
	public boolean notifyTalk(NpcInstance npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		return showResult(npc, qs.getPlayer(), res);
	}
	
	public boolean notifySkillUse(NpcInstance npc, Skill skill, QuestState qs)
	{
		String res = null;
		try
		{
			res = onSkillUse(npc, skill, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
			return true;
		}
		return showResult(npc, qs.getPlayer(), res);
	}
	
	public void notifyCreate(QuestState qs)
	{
		try
		{
			onCreate(qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
		}
	}
	
	public void onCreate(QuestState qs)
	{
	}
	
	public String onAttack(NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onDeath(Creature killer, Creature victim, QuestState qs)
	{
		return null;
	}
	
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		return null;
	}
	
	public String onKill(NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onKill(Player killed, QuestState st)
	{
		return null;
	}
	
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		return null;
	}
	
	public String onTalk(NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onSkillUse(NpcInstance npc, Skill skill, QuestState qs)
	{
		return null;
	}
	
	public void onOlympiadEnd(OlympiadGame og, QuestState qs)
	{
	}
	
	public void onAbort(QuestState qs)
	{
	}
	
	public boolean canAbortByPacket()
	{
		return true;
	}
	
	/**
	 * Show message error to player who has an access level greater than 0
	 * @param player : L2Player
	 * @param t : Throwable
	 */
	private void showError(Player player, Throwable t)
	{
		_log.error("", t);
		if ((player != null) && player.canOverrideCond(PcCondOverride.DEBUG_CONDITIONS))
		{
			String res = "<html><body><title>Script error</title>" + LogUtils.dumpStack(t).replace("\n", "<br>") + "</body></html>";
			showResult(null, player, res);
		}
	}
	
	protected void showHtmlFile(Player player, String fileName, boolean showQuestInfo)
	{
		showHtmlFile(player, fileName, showQuestInfo, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}
	
	protected void showHtmlFile(Player player, String fileName, boolean showQuestInfo, Object... arg)
	{
		if (player == null)
		{
			return;
		}
		
		GameObject target = player.getTarget();
		NpcHtmlMessage npcReply = showQuestInfo ? new ExNpcQuestHtmlMessage(target == null ? 5 : target.getObjectId(), getQuestIntId()) : new NpcHtmlMessage(target == null ? 5 : target.getObjectId());
		npcReply.setFile("quests/" + getClass().getSimpleName() + "/" + fileName);
		
		if ((arg.length % 2) == 0)
		{
			for (int i = 0; i < arg.length; i += 2)
			{
				npcReply.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}
		
		player.sendPacket(npcReply);
	}
	
	protected void showSimpleHtmFile(Player player, String fileName)
	{
		if (player == null)
		{
			return;
		}
		
		NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setFile(fileName);
		player.sendPacket(npcReply);
	}
	
	/**
	 * Show a message to player.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with tag "html" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>"res" is null :</U> do not show any message</LI>
	 * <LI><U>"res" is empty string :</U> show default message</LI>
	 * <LI><U>otherwise :</U> the message hold in "res" is shown in chat box</LI>
	 * @param npc
	 * @param player
	 * @param res : String pointing out the message to show at the player
	 */
	private boolean showResult(NpcInstance npc, Player player, String res)
	{
		return showResult(npc, player, res, false);
	}
	
	private boolean showResult(NpcInstance npc, Player player, String res, boolean isFirstTalk)
	{
		boolean showQuestInfo = showQuestInfo(player);
		if (isFirstTalk)
		{
			showQuestInfo = false;
		}
		if ((res == null) || (npc == null) || (npc.getNpcId() == 160) || (npc.getNpcId() == 161) || (npc.getNpcId() == 162) || (npc.getNpcId() == 163))
		{
			return true;
		}
		if (res.isEmpty())
		{
			return false;
		}
		if (res.startsWith("no_quest") || res.equalsIgnoreCase("noquest") || res.equalsIgnoreCase("no-quest"))
		{
			showSimpleHtmFile(player, "no-quest.htm");
		}
		else if (res.equalsIgnoreCase("completed"))
		{
			showSimpleHtmFile(player, "completed-quest.htm");
		}
		else if (res.endsWith(".htm"))
		{
			showHtmlFile(player, res, showQuestInfo);
		}
		else
		{
			// NpcHtmlMessage npcReply = showQuestInfo ? new ExNpcQuestHtmlMessage(npc == null ? 5 : npc.getObjectId(), getQuestIntId()) : new NpcHtmlMessage(npc == null ? 5 : npc.getObjectId());
			final NpcHtmlMessage npcReply = showQuestInfo ? new ExNpcQuestHtmlMessage(npc.getObjectId(), getQuestIntId()) : new NpcHtmlMessage(npc.getObjectId());
			npcReply.setHtml(res);
			// if(getQuestIntId() > 0 && getQuestIntId() < 20000)
			// if(getQuestIntId() != 999)
			// npcReply.setQuest(getQuestIntId());
			player.sendPacket(npcReply);
		}
		return true;
	}
	
	// Î ï¿½Î¡â‚¬Î ÎŽÎ Â²Î ÂµÎ¡â‚¬Î¡ï¿½Î ÂµÎ ÎŒ, Î Î�Î ÎŽÎ ÎŠÎ Â°Î Â·Î¡â€¹Î Â²Î Â°Î¡â€šÎ¡ï¿½ Î Â»Î Îˆ Î ÎˆÎ Â½Î¡â€žÎ ÎŽÎ¡â‚¬Î ÎŒÎ Â°Î¡â€ Î ÎˆÎ¡ï¿½ Î ÎŽ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Âµ Î Â² Î Î„Î ÎˆÎ Â°Î Â»Î ÎŽÎ Â³Î Âµ.
	private boolean showQuestInfo(Player player)
	{
		QuestState qs = player.getQuestState(getName());
		if ((qs != null) && (qs.getState() != CREATED))
		{
			return false;
		}
		if (!isVisible())
		{
			return false;
		}
		
		return true;
	}
	
	// Î ï¿½Î¡ï¿½Î¡â€šÎ Â°Î Â½Î Â°Î Â²Î Â»Î ÎˆÎ Â²Î Â°Î ÂµÎ ÎŒ Î Îˆ Î¡ï¿½Î ÎŽÎ¡â€¦Î¡â‚¬Î Â°Î Â½Î¡ï¿½Î ÂµÎ ÎŒ Î¡â€šÎ Â°Î Î‰Î ÎŒÎ ÂµÎ¡â‚¬Î¡â€¹ (Î Î�Î¡â‚¬Î Îˆ Î Â²Î¡â€¹Î¡â€¦Î ÎŽÎ Î„Î Âµ Î ÎˆÎ Â· Î ÎˆÎ Â³Î¡â‚¬Î¡â€¹)
	void pauseQuestTimers(QuestState qs)
	{
		if (qs.getTimers().isEmpty())
		{
			return;
		}
		
		for (QuestTimer timer : qs.getTimers().values())
		{
			timer.setQuestState(null);
			timer.pause();
		}
		
		_pausedQuestTimers.put(qs.getPlayer().getObjectId(), qs.getTimers());
	}
	
	// Î â€™Î ÎŽÎ¡ï¿½Î¡ï¿½Î¡â€šÎ Â°Î Â½Î Â°Î Â²Î Â»Î ÎˆÎ Â²Î Â°Î ÂµÎ ÎŒ Î¡â€šÎ Â°Î Î‰Î ÎŒÎ ÂµÎ¡â‚¬Î¡â€¹ (Î Î�Î¡â‚¬Î Îˆ Î Â²Î¡â€¦Î ÎŽÎ Î„Î Âµ Î Â² Î ÎˆÎ Â³Î¡â‚¬Î¡Æ’)
	void resumeQuestTimers(QuestState qs)
	{
		Map<String, QuestTimer> timers = _pausedQuestTimers.remove(qs.getPlayer().getObjectId());
		if (timers == null)
		{
			return;
		}
		
		qs.getTimers().putAll(timers);
		
		for (QuestTimer timer : qs.getTimers().values())
		{
			timer.setQuestState(qs);
			timer.start();
		}
	}
	
	protected String str(long i)
	{
		return String.valueOf(i);
	}
	
	// =========================================================
	// QUEST SPAWNS
	// =========================================================
	
	public class DeSpawnScheduleTimerTask extends RunnableImpl
	{
		NpcInstance _npc = null;
		
		public DeSpawnScheduleTimerTask(NpcInstance npc)
		{
			_npc = npc;
		}
		
		@Override
		public void runImpl()
		{
			if (_npc != null)
			{
				if (_npc.getSpawn() != null)
				{
					_npc.getSpawn().deleteAll();
				}
				else
				{
					_npc.deleteMe();
				}
			}
		}
	}
	
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, int randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, new Location(x, y, z, heading), randomOffset, despawnDelay);
	}
	
    public NpcInstance addSpawn(final int npcId, final Location loc, final int randomOffset, final int despawnDelay) {
        return NpcUtils.spawnSingle(npcId, (randomOffset > 50) ? Location.findPointToStay(loc, 50, randomOffset, ReflectionManager.DEFAULT.getGeoIndex()) : loc, despawnDelay);
    }
	
//	public NpcInstance addSpawn(int npcId, Location loc, int randomOffset, int despawnDelay)
//	{
//		
//		// New add Test
//		// NpcInstance result = Functions.spawn(randomOffset > 50 ? loc.findPointToStay(randomOffset) : loc, npcId);
//		NpcInstance result = Functions.spawn(randomOffset > 50 ? Location.findPointToStay(loc, 0, randomOffset, ReflectionManager.DEFAULT.getGeoIndex()) : loc, npcId);
//		
//		if ((despawnDelay > 0) && (result != null))
//		{
//			ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(result), despawnDelay);
//		}
//		return result;
//	}
	
	/**
	 * Î â€�Î ÎŽÎ Â±Î Â°Î Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€š Î¡ï¿½Î Î�Î Â°Î¡Æ’Î Â½ Î¡ï¿½ Î¡â€¡Î ÎˆÎ¡ï¿½Î Â»Î ÎŽÎ Â²Î¡â€¹Î ÎŒ Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ ÂµÎ ÎŒ Î¡â‚¬Î Â°Î Â·Î Â±Î¡â‚¬Î ÎŽÎ¡ï¿½Î Â° - Î ÎŽÎ¡â€š 50 Î Î„Î ÎŽ randomOffset. Î â€¢Î¡ï¿½Î Â»Î Îˆ randomOffset Î¡Æ’Î ÎŠÎ Â°Î Â·Î Â°Î Â½ Î ÎŒÎ ÂµÎ Â½Î Âµ 50, Î¡â€šÎ ÎŽ Î ÎŠÎ ÎŽÎ ÎŽÎ¡â‚¬Î Î„Î ÎˆÎ Â½Î Â°Î¡â€šÎ¡â€¹ Î Â½Î Âµ Î ÎŒÎ ÂµÎ Â½Î¡ï¿½Î¡ï¿½Î¡â€šÎ¡ï¿½Î¡ï¿½.
	 */
	public static NpcInstance addSpawnToInstance(int npcId, int x, int y, int z, int heading, int randomOffset, int refId)
	{
		return addSpawnToInstance(npcId, new Location(x, y, z, heading), randomOffset, refId);
	}
	
	public static NpcInstance addSpawnToInstance(int npcId, Location loc, int randomOffset, int refId)
	{
		try
		{
			NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
			if (template != null)
			{
				NpcInstance npc = NpcHolder.getInstance().getTemplate(npcId).getNewInstance();
				npc.setReflection(refId);
				// npc.setSpawnedLoc(randomOffset > 50 ? loc.setR(npc).findPointToStay(50) : loc);
				npc.setSpawnedLoc(randomOffset > 50 ? Location.findPointToStay(loc, 50, randomOffset, npc.getGeoIndex()) : loc);
				npc.spawnMe(npc.getSpawnedLoc());
				return npc;
			}
		}
		catch (Exception e1)
		{
			_log.warn("Could not spawn Npc " + npcId);
		}
		return null;
	}
	
	public boolean isVisible()
	{
		return true;
	}
	
	/**
	 * Gets the reset hour for a daily quest.
	 * @return the reset hour
	 */
	public int getResetHour()
	{
		return QuestState.RESTART_HOUR;
	}
	
	/**
	 * Gets the reset minutes for a daily quest.
	 * @return the reset minutes
	 */
	public int getResetMinutes()
	{
		return QuestState.RESTART_MINUTES;
	}
	
	/**
	 * Gets a random integer number from 0 (inclusive) to {@code max} (exclusive).<br>
	 * Use this method instead importing {@link com.l2jserver.util.Rnd} utility.
	 * @param max this parameter represents the maximum value for randomization.
	 * @return a random integer number from 0 to {@code max} - 1.
	 */
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * Gets a random integer number from {@code min} (inclusive) to {@code max} (inclusive).<br>
	 * Use this method instead importing {@link com.l2jserver.util.Rnd} utility.
	 * @param min this parameter represents the minimum value for randomization.
	 * @param max this parameter represents the maximum value for randomization.
	 * @return a random integer number from {@code min} to {@code max} .
	 */
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
}