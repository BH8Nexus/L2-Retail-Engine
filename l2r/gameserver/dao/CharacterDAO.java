package l2r.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.mysql;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Location;

public class CharacterDAO
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);
	
	private static CharacterDAO _instance = new CharacterDAO();
	
	public static CharacterDAO getInstance()
	{
		return _instance;
	}
	
	public void deleteCharByObjId(int objid)
	{
		if (objid < 0)
		{
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_friends(Ð”Ñ€ÑƒÐ·ÑŒÑ� Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶Ð°) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_hennas(Ð¢Ð°Ñ‚Ñ‚Ñƒ) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_macroses(ÐœÐ°ÐºÑ€Ð¾Ñ�Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_quests(ÐšÐ²ÐµÑ�Ñ‚Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_recipebook(Ð ÐµÑ†ÐµÐ¿Ñ‚Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_shortcuts(Ð¯Ñ€Ð»Ñ‹ÐºÐ¸ - ÐŸÐ°Ð½ÐµÐ»ÑŒ) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_skills(Ð¡ÐºÐ¸Ð»Ð»Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_skills_save(Ð¡ÐºÐ¸Ð»Ð»Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ character_subclasses(Ð¡Ð°Ð±ÐšÐ»Ð°Ñ�Ñ�Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ heroes(HERO) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ olympiad_nobles(ÐžÐ»Ð¸Ð¼Ð¿Ð¸Ð°Ð´Ð°) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ seven_signs(Ð¡ÐµÐ¼ÑŒ ÐŸÐµÑ‡Ð°Ñ‚ÐµÐ¹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ pets(ÐŸÐ¸Ñ‚Ð¾Ð¼Ñ†Ñ‹ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶Ð°) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ item_attributes(Ð’Ñ�Ðµ Ð°Ñ‚Ñ‚Ñ€Ð¸Ð±ÑƒÑ‚Ñ‹) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE object_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ items(Ð’Ñ�Ðµ Ð¸Ñ‚ÐµÐ¼Ñ‹ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶Ð°) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// Ð§Ð¸Ñ�Ñ‚Ð¸Ð¼ Ñ‚Ð°Ð±Ð»Ð¸Ñ†Ñƒ characters(ÐŸÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶) - ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼Ð¾Ð³Ð¾ Ñ‡Ð°Ñ€Ð°
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// achievements
			statement = con.prepareStatement("DELETE FROM character_achievement_levels WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// counters
			statement = con.prepareStatement("DELETE FROM character_counters WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			// buffer
			statement = con.prepareStatement("DELETE FROM scheme_buffer_profiles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public boolean insert(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name, face, hairStyle, hairColor, sex, karma, pvpkills, pkkills, clanid, createtime, deletetime, title, accesslevel, online, leaveclan, deleteclan, nochannel, pledge_type, pledge_rank, lvl_joined_academy, apprentice) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, player.getObjectId());
			statement.setString(3, player.getName());
			statement.setInt(4, player.getFace());
			statement.setInt(5, player.getHairStyle());
			statement.setInt(6, player.getHairColor());
			statement.setInt(7, player.getSex());
			statement.setInt(8, player.getKarma());
			statement.setInt(9, player.getPvpKills());
			statement.setInt(10, player.getPkKills());
			statement.setInt(11, player.getClanId());
			statement.setLong(12, player.getCreateTime() / 1000);
			statement.setInt(13, player.getDeleteTimer());
			statement.setString(14, player.getTitle());
			statement.setInt(15, player.getAccessLevel().getLevel());
			statement.setInt(16, player.isOnline() ? 1 : 0);
			statement.setLong(17, player.getLeaveClanTime() / 1000);
			statement.setLong(18, player.getDeleteClanTime() / 1000);
			statement.setLong(19, player.getNoChannel() > 0 ? player.getNoChannel() / 1000 : player.getNoChannel());
			statement.setInt(20, player.getPledgeType());
			statement.setInt(21, player.getPowerGrade());
			statement.setInt(22, player.getLvlJoinedAcademy());
			statement.setInt(23, player.getApprentice());
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getTemplate().classId.getId());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setDouble(5, player.getTemplate().getBaseHpMax() + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
			statement.setDouble(6, player.getTemplate().getBaseMpMax() + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
			statement.setDouble(7, player.getTemplate().getBaseCpMax() + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
			statement.setDouble(8, player.getTemplate().getBaseHpMax() + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
			statement.setDouble(9, player.getTemplate().getBaseMpMax() + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
			statement.setDouble(10, player.getTemplate().getBaseCpMax() + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
			statement.setInt(11, 1);
			statement.setInt(12, 1);
			statement.setInt(13, 1);
			statement.setInt(14, 0);
			statement.setInt(15, 0);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.error("", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	
	public Location getLocation(String name)
	{
		return getLocation(getObjectIdByName(name));
	}
	
	public Location getLocation(int id)
	{
		if (id == 0)
		{
			return null;
		}
		
		Location result = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT x, y, z FROM characters WHERE obj_Id=?");
			statement.setInt(1, id);
			rset = statement.executeQuery();
			
			if (rset.next())
			{
				result = new Location(rset.getInt(1), rset.getInt(2), rset.getInt(3));
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getLocation(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public void deleteUserVar(String cha, String param)
	{
		deleteUserVar(getObjectIdByName(cha), param);
	}
	
	public void deleteUserVar(int objId, String param)
	{
		if (objId == 0)
		{
			return;
		}
		
		mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", objId, param);
	}
	
	public String getUserVar(String cha, String param)
	{
		return getUserVar(getObjectIdByName(cha), param);
	}
	
	public String getUserVar(int objId, String param)
	{
		if (objId == 0)
		{
			return null;
		}
		
		return (String) mysql.get("SELECT `value` FROM `character_variables` WHERE `obj_id` = " + objId + " AND `type`='user-var' AND `name` = '" + param + "'");
	}
	
	public void setDbLocatio(int objId, int x, int y, int z)
	{
		mysql.set("UPDATE `characters` SET `x`=?, `y`=?, `z`=? WHERE `obj_id`=? LIMIT 1", x, y, z, objId);
	}
	
	public int getObjectIdByName(String name)
	{
		int result = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if (rset.next())
			{
				result = rset.getInt(1);
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getObjectIdByName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public static String getNameByObjectId(int objectId)
	{
		String result = StringUtils.EMPTY;
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?"))
		{
			statement.setInt(1, objectId);
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					result = rset.getString(1);
				}
			}
		}
		catch (SQLException e)
		{
			_log.error("CharNameTable.getObjectIdByName(int): ", e);
		}
		
		return result;
	}
	
	public String getAccountName(String charName)
	{
		String result = StringUtils.EMPTY;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			rset = statement.executeQuery();
			if (rset.next())
			{
				result = rset.getString(1);
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getAccountName(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public int accountCharNumber(String account)
	{
		int number = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if (rset.next())
			{
				number = rset.getInt(1);
			}
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return number;
	}
	
	public String getAccountPassword(String accountName)
	{
		String result = StringUtils.EMPTY;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT password FROM " + Config.LOGINSERVER_DB_NAME + ".accounts WHERE login=?");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			if (rset.next())
			{
				result = rset.getString(1);
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getAccountPassword(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public int getLastServerId(String accountName)
	{
		int result = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT last_server FROM " + Config.LOGINSERVER_DB_NAME + ".accounts WHERE login=?");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			if (rset.next())
			{
				result = rset.getInt(1);
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getLastServerId(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public long getLastAccessTime(String charName)
	{
		long lastAccesss = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT lastAccess FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			rset = statement.executeQuery();
			if (rset.next())
			{
				lastAccesss = rset.getInt(1);
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getLastAccessTime(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return lastAccesss;
	}
	
	public CharacterData getCharacterData(int objectId)
	{
		CharacterData result = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			final String name;
			final String title;
			final int classId;
			final int clanId;
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT c.char_name, c.title, c.clanid, s.class_id FROM characters AS c, character_subclasses AS s WHERE c.obj_id = s.char_obj_id AND s.isBase = 1 AND c.obj_Id=?");
			statement.setInt(1, objectId);
			rset = statement.executeQuery();
			if (rset.next())
			{
				name = rset.getString("char_name");
				title = rset.getString("title");
				classId = rset.getInt("class_id");
				clanId = rset.getInt("clanid");
				result = new CharacterData(name, title, classId, clanId);
			}
		}
		catch (Exception e)
		{
			_log.error("CharacterDAO.getCharacterData(int): " + e, e);
			return null;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return result;
	}
	
	public List<CharacterLoginData> getAllLoginData(int objId, int... limit)
	{
		List<CharacterLoginData> result = new LinkedList<>();
		String query = CharacterLoginData.SELECT_PLAYER_QUERY;
		if (limit != null)
		{
			if (limit.length > 1)
			{
				query += " LIMIT " + limit[0] + ", " + limit[limit.length - 1];
			}
			else if (limit.length == 1)
			{
				query += " LIMIT 0, " + limit[0];
			}
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(query))
		{
			st.setInt(1, objId);
			ResultSet rset = st.executeQuery();
			while (rset.next())
			{
				final long loginDate = rset.getLong("date");
				final String ip = rset.getString("ip");
				final String hwid = rset.getString("hwid");
				final long onlineTime = rset.getLong("onlinetime");
				final String log = rset.getString("log");
				
				result.add(new CharacterLoginData(objId, loginDate, ip, hwid, onlineTime, log));
			}
		}
		catch (SQLException e)
		{
			_log.warn("Failed getting login data for objId: " + objId, e);
		}
		
		return result;
	}
	
	public static class CharacterData
	{
		private final String _name;
		private final String _title;
		private final int _classId;
		private final int _clanId;
		
		protected CharacterData(String name, String title, int classId, int clanId)
		{
			_name = name;
			_title = title;
			_classId = classId;
			_clanId = clanId;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getTitle()
		{
			return _title;
		}
		
		public int getClassId()
		{
			return _classId;
		}
		
		public String getClassName()
		{
			return ClassId.VALUES[_classId].getName();
		}
		
		public int getClanId()
		{
			return _clanId;
		}
		
		public String getClanName()
		{
			Clan clan = ClanTable.getInstance().getClan(_clanId);
			if (clan == null)
			{
				return "No Clan";
			}
			
			return clan.getName();
		}
		
		public String getAllyName()
		{
			Clan clan = ClanTable.getInstance().getClan(_clanId);
			if (clan == null)
			{
				return "No Ally";
			}
			
			Alliance ally = clan.getAlliance();
			if (ally == null)
			{
				return "No Ally";
			}
			
			return ally.getAllyName();
		}
	}
	
	public static class CharacterLoginData
	{
		public static final String SELECT_QUERY = "SELECT (obj_id, date, ip, hwid, onlinetime, log) FROM character_logindata ORDER BY `date` DESC";
		public static final String SELECT_PLAYER_QUERY = "SELECT * FROM character_logindata WHERE obj_id = ? ORDER BY `date` DESC"; // LIMIT 0, 1000
		public static final String DELETE_BY_DATE_QUERY = "DELETE FROM character_logindata WHERE date < ?";
		private static final String INSERT_QUERY = "INSERT INTO character_logindata (obj_id, date, ip, hwid, onlinetime, log) VALUES (?, ?, ?, ?, ?, ?)";
		private static final String UPDATE_ONLINETIME = "UPDATE `character_logindata` SET `onlinetime` = ? WHERE `obj_id` = ? AND `date` = ? LIMIT 1";
		private static final String UPDATE_LOG = "UPDATE `character_logindata` SET `log` = ? WHERE `obj_id` = ? AND `date` = ? LIMIT 1";
		
		private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		private static final SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
		
		private final boolean canChangeData; // Used to mark if this is loaded from 3rd party file for scanning, or its an actual in use data.
		private final int objId;
		private final long loginDate;
		private final String ip;
		private final String hwid;
		private long onlineTime;
		private String log;
		
		/**
		 * Creates a new loginData for the specified player to be saved in the database.
		 * @param player
		 */
		public CharacterLoginData(Player player)
		{
			canChangeData = true;
			objId = player.getObjectId();
			loginDate = System.currentTimeMillis();
			ip = player.getIP() == null ? "null" : player.getIP();
			hwid = player.getHWID() == null ? "null" : player.getHWID();
			log = "";
			insertData();
		}
		
		public CharacterLoginData(int objId, long loginDate, String ip, String hwid, long onlineTime, String log)
		{
			canChangeData = false;
			this.objId = objId;
			this.loginDate = loginDate;
			this.ip = ip == null ? "null" : ip;
			this.hwid = hwid == null ? "null" : hwid;
			this.onlineTime = onlineTime;
			this.log = log == null ? "" : log;
		}
		
		private void insertData()
		{
			if (canChangeData)
			{
				mysql.set(INSERT_QUERY, objId, loginDate, ip, hwid, onlineTime, log);
			}
		}
		
		/** @return The objectId of the player owner. */
		public int getObjectId()
		{
			return objId;
		}
		
		/** @return The unix date in milisec that this player has logged in. */
		public long getLoginDateMilis()
		{
			return loginDate;
		}
		
		/** @return The date that this player has logged in. */
		public String getLoginDate()
		{
			return format.format(new Date(loginDate));
		}
		
		/** @return The ip from which this player has logged in. */
		public String getIP()
		{
			return ip;
		}
		
		/** @return The hwid from which this player has logged in. */
		public String getHWID()
		{
			return hwid;
		}
		
		/** @return Updates the onlinetime of this player and saves to database. */
		public void updateOnlineTime()
		{
			if (!canChangeData)
			{
				return;
			}
			
			onlineTime = System.currentTimeMillis() - loginDate;
			if (!mysql.set(UPDATE_ONLINETIME, onlineTime, objId, loginDate))
			{
				_log.warn("CharacterLoginData: Failed to update onlineTime. objId=" + objId + " loginDate=" + loginDate + " onlineTime=" + onlineTime);
			}
		}
		
		/** @return The time player has been online in miliseconds. */
		public long getOnlineTimeMilis()
		{
			return onlineTime;
		}
		
		public String getLog(int maxLogs)
		{
			String result = "";
			try
			{
				result = log;
				int index = 0;
				int indexEnd = 0;
				while ((maxLogs > 0) && ((index = result.indexOf('[', index)) >= 0) && ((indexEnd = result.indexOf('[', indexEnd)) >= 0))
				{
					maxLogs--;
					index++; // Skip the [ char.
					String date = result.substring(index, indexEnd);
					indexEnd++; // Skip the ] char to not take the same index.
					
					String dateString = format2.format(new Date(Long.parseLong(date)));
					result.replaceFirst(date, dateString);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			return result;
		}
		
		/** @return The time player has been online in HH-MM-SS. */
		public String getOnlineTime()
		{
			final long remainingTime = onlineTime / 1000;
			final int hours = (int) (remainingTime / 3600);
			final int minutes = (int) ((remainingTime % 3600) / 60);
			final int seconds = (int) ((remainingTime % 3600) % 60);
			
			StringBuilder sb = new StringBuilder(10);
			if (hours > 0)
			{
				sb.append(hours).append("H");
			}
			if ((minutes > 0) || (hours > 0))
			{
				sb.append(" ").append(minutes).append("m");
			}
			if ((seconds > 0) || (minutes > 0) || (hours > 0))
			{
				sb.append(" ").append(seconds).append("s");
			}
			if (sb.length() == 0)
			{
				sb.append("none");
			}
			
			return sb.toString();
		}
		
		/** Each new append is seperated by [dateInMilis]. Saves the log to the database. */
		public boolean updateLog(String appendToLog)
		{
			if (canChangeData)
			{
				return false;
			}
			
			if (log == null)
			{
				log = "";
			}
			
			// Will fuck up database
			if ((log.length() + appendToLog.length()) > 65535)
			{
				return false;
			}
			
			log += "[" + System.currentTimeMillis() + "]";
			log += appendToLog;
			
			if (!mysql.set(UPDATE_LOG, log, objId, loginDate))
			{
				_log.warn("CharacterLoginData: Failed to update log. objId=" + objId + " loginDate=" + loginDate + " log=" + log);
			}
			
			return true;
		}
	}
	
	public void markTooOldChars()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET characters.deletetime=1 WHERE characters.onlinetime < 3600 and characters.lastAccess < 1376610861 LIMIT 500"))
		{
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("Error while markTooOldChars! ", e);
		}
	}
	
	public void checkCharactersToDelete()
	{
		List<Integer> idsToDelete = new ArrayList<>();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE deletetime > 0 AND deletetime < ?"))
		{
			statement.setLong(1, ((System.currentTimeMillis() / 1000) - (Config.DELETE_DAYS * 3600 * 24)));
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					idsToDelete.add(rset.getInt("obj_Id"));
				}
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while finding chars to delete!", e);
		}
		
		_log.info("Found " + idsToDelete.size() + " characters to delete!");
		
		for (int i = 0; i < idsToDelete.size(); i += 100)
		{
			int[] ids = new int[Math.min(100, idsToDelete.size() - (i))];
			int index = 0;
			for (int iter = i; iter < (i + ids.length); iter++)
			{
				ids[index] = idsToDelete.get(iter);
				index++;
			}
			deleteCharByObjId(ids);
			_log.info("Deleted " + ids.length + " ids!");
		}
	}
	
	public void deleteCharByObjId(int... objids)
	{
		if ((objids.length == 0) || ((objids.length == 1) && (objids[0] < 0)))
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			StringBuilder queryFinishBuilder = new StringBuilder();
			for (int i = 0; i < objids.length; i++)
			{
				if (i != 0)
				{
					queryFinishBuilder.append(" OR ");
				}
				queryFinishBuilder.append("obj_Id=").append(objids[i]).append(" OR target_Id=").append(objids[i]);
			}
			
			String queryFinish = queryFinishBuilder.toString();
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_blocklist WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("obj_Id", "char_id");
			queryFinish = queryFinish.replace("target_Id", "friend_id");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("char_id", "object_id");
			queryFinish = queryFinish.replace("friend_id", "post_friend");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_post_friends WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinishBuilder.delete(0, queryFinishBuilder.length());
			for (int i = 0; i < objids.length; i++)
			{
				if (i != 0)
				{
					queryFinishBuilder.append(" OR ");
				}
				queryFinishBuilder.append("object_id=").append(objids[i]);
			}
			
			queryFinish = queryFinishBuilder.toString();
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_effects_save WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_group_reuse WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("object_id", "char_obj_id");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_macroses WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills_save WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_subclasses WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM seven_signs WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("char_obj_id", "char_id");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_mail WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM heroes WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("char_id", "obj_id");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_instances WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("obj_id", "obj_Id");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_logs WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM characters WHERE " + queryFinish))
			{
				statement.execute();
			}
			
			queryFinish = queryFinish.replace("obj_Id", "owner_id");
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE " + queryFinish))
			{
				statement.execute();
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while deleting character!", e);
		}
	}
	
	public static void setFacebookId(int playerObjectId, String facebookId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET facebook_id = ? WHERE obj_Id = ?"))
		{
			statement.setString(1, facebookId);
			statement.setInt(2, playerObjectId);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("Error while setFacebookId(" + playerObjectId + ", " + facebookId + ")", e);
		}
	}
	
	public static Language getLanguage(int playerObjectId, boolean checkOnlinePlayer)
	{
		if (checkOnlinePlayer)
		{
			final Player onlinePlayer = GameObjectsStorage.getPlayer(playerObjectId);
			if (onlinePlayer != null)
			{
				return onlinePlayer.getLanguage();
			}
		}
		return getLanguage(playerObjectId);
	}
	
	public static Language getLanguage(int playerObjectId)
	{
		/*
		 * final String value = CharacterVariablesDAO.selectFirst(playerObjectId, "Language"); if (value == null) return ConfigHolder.getEnum("DefaultLanguage", Language.class);
		 */
		return Language.ENGLISH;
	}
}