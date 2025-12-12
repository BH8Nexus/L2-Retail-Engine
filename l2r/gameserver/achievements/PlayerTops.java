package l2r.gameserver.achievements;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javolution.util.FastTable;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.utils.Util;

public class PlayerTops
{
	public HashMap<String, List<TopScore>> _playersintop = new HashMap<>();
	private static PlayerTops _instance;
	static NumberFormat _nf = NumberFormat.getInstance();
	
	public PlayerTops()
	{
		if (Config.ENABLE_PLAYER_COUNTERS)
		{
			_updateTimer.scheduleAtFixedRate(_updateTask, 1000, 5 * 60 * 1000);
		}
	}
	
	private final TimerTask _updateTask = new TimerTask()
	{
		@Override
		public void run()
		{
			update();
		}
	};
	
	private final Timer _updateTimer = new Timer();
	
	public void usebypass(Player player, String bypass, String[] cm)
	{
		if (player == null)
		{
			return;
		}
		
		update();
		
		if (cm.length == 3)
		{
			if (cm[1].equalsIgnoreCase("lifetimetops"))
			{
				lifeTimeTop(player, cm[2]);
			}
		}
		else if (cm.length == 2)
		{
			showTop(player, Integer.parseInt(cm[1]));
		}
		else
		{
			showTop(player, 1);
		}
	}
	
	public void showTop(Player player, int page)
	{
		if (player == null)
		{
			return;
		}
		
		String html = HtmCache.getInstance().getNotNull("CommunityBoard/Main/top/playerTops.htm", player);
		
		// Generate used fields list.
		List<String> fieldNames = new ArrayList<>();
		for (Field field : PlayerCounters.class.getFields())
		{
			switch (field.getName())
			// Fields that we dont use here.
			{
				case "_activeChar":
				case "_playerObjId":
				case "DUMMY_COUNTER":
				case "LongestKillspree":
				case "KillspreesEnded":
				case "EnchantItem":
					continue;
				default:
					fieldNames.add(field.getName());
			}
		}
		
		int all = 0;
		boolean pagereached = false;
		int totalpages = (int) Math.round(fieldNames.size() / 3.0);
		
		if (page == 1)
		{
			if (totalpages == 1)
			{
				html = html.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbTop " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
			}
			html = html.replaceAll("%back%", "&nbsp;");
		}
		else if (page > 1)
		{
			if (totalpages <= page)
			{
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbTop " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
				html = html.replaceAll("%more%", "&nbsp;");
			}
			else
			{
				html = html.replaceAll("%more%", "<button value=\"\" action=\"bypass _bbTop " + (page + 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">");
				html = html.replaceAll("%back%", "<button value=\"\" action=\"bypass _bbTop " + (page - 1) + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">");
			}
		}
		
		int ccCount = 0;
		
		for (String type : fieldNames)
		{
			all++;
			if ((page == 1) && (ccCount > 3))
			{
				continue;
			}
			if (!pagereached && (all > (page * 3)))
			{
				continue;
			}
			if (!pagereached && (all <= ((page - 1) * 3)))
			{
				continue;
			}
			
			ccCount++;
			String[] color = getCoolor(ccCount);
			int count = 0;
			
			html = html.replaceAll("%title" + ccCount + "%", type);
			
			if (_playersintop.get(type) != null)
			{
				TopScore thispl = getPlayerInTop(player.getName(), type);
				for (TopScore pl : _playersintop.get(type))
				{
					count++;
					if (pl.getPlace() == (11 + 1))
					{
						break;
					}
					
					Player plInstance = World.getPlayer(CharacterDAO.getInstance().getObjectIdByName(pl.getName()));
					
					if ((thispl == null) && (pl.getPlace() == 11))
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", (count % 2) == 0 ? color[0] : color[1]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + pl.getPlace());
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", pl.getName());
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(pl.getTop()) ? _nf.format(Integer.valueOf(pl.getTop())) : pl.getTop()));
						html = html.replaceAll("%" + ccCount + "Online" + count + "%", ((plInstance == null) || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
						break;
					}
					if ((thispl != null) && (pl.getPlace() == 11) && (thispl.getPlace() > (11 - 1)))
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", color[2]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + thispl.getPlace());
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", thispl.getName());
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(thispl.getTop()) ? _nf.format(Integer.valueOf(thispl.getTop())) : thispl.getTop()));
						html = html.replaceAll("%" + ccCount + "Online" + count + "%", ((plInstance == null) || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
						break;
					}
					else if ((thispl != null) && pl.getName().equals(player.getName()))
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", color[2]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + thispl.getPlace());
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", thispl.getName());
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(thispl.getTop()) ? _nf.format(Long.parseLong(thispl.getTop())) : thispl.getTop()));
						html = html.replaceAll("%" + ccCount + "Online" + count + "%", ((plInstance == null) || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
					}
					else
					{
						html = html.replaceAll("%" + ccCount + "BG" + count + "%", (count % 2) == 0 ? color[0] : color[1]);
						html = html.replaceAll("%" + ccCount + "InTop" + count + "%", "" + pl.getPlace());
						html = html.replaceAll("%" + ccCount + "Name" + count + "%", pl.getName());
						html = html.replaceAll("%" + ccCount + "Count" + count + "%", "" + (Util.isDigit(pl.getTop()) ? _nf.format(Long.parseLong(pl.getTop())) : pl.getTop()));
						html = html.replaceAll("%" + ccCount + "Online" + count + "%", ((plInstance == null) || !plInstance.isOnline()) ? "<button fore=\"L2UI_CH3.radar_target\" back=\"L2UI_CH3.radar_target\" width=\"8\" height=\"8\">" : "<button fore=\"L2UI_CH3.radar_party\" back=\"L2UI_CH3.radar_party\" width=\"8\" height=\"8\">");
					}
				}
			}
			
			if (count < 12)
			{
				for (int numeris = count + 1; numeris <= 11; numeris++)
				{
					html = html.replaceAll("%" + ccCount + "BG" + numeris + "%", (numeris % 2) == 0 ? color[0] : color[1]);
					html = html.replaceAll("%" + ccCount + "InTop" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Name" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Count" + numeris + "%", "");
					html = html.replaceAll("%" + ccCount + "Online" + numeris + "%", "");
				}
			}
		}
		ShowBoard.separateAndSend(html, player);
	}
	
	public TopScore getPlayerInTop(String name, String type)
	{
		if (name.isEmpty() || type.isEmpty())
		{
			return null;
		}
		
		for (TopScore pl : _playersintop.get(type))
		{
			if ((pl.getName() != null) && pl.getName().equalsIgnoreCase(name))
			{
				return pl;
			}
		}
		
		return null;
	}
	
	public void update()
	{
		_playersintop = new HashMap<>();
		
		// Generate lifetime top
		generateDataBy("pvpkills");
		generateDataBy("pkkills");
		generateDataBy("onlinetime");
		
		// Generate used fields list.
		for (Field field : PlayerCounters.class.getFields())
		{
			switch (field.getName())
			{
				case "_activeChar":
				case "_playerObjId":
				case "DUMMY_COUNTER":
					continue;
				default:
					generateDataFromAcsBy(field.getName());
			}
		}
	}
	
	// LIMITED TO 50 RESULTS!!!!!!!!!!!!!!!
	public void generateDataBy(String type)
	{
		List<TopScore> _temp = new ArrayList<>();
		
		int i = 0;
		
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT char_name," + type + " FROM characters WHERE " + type + " > 0 AND accessLevel >= 0 ORDER BY " + type + " DESC LIMIT 40");
			while (rset.next())
			{
				i++;
				String character_name = rset.getString("char_name");
				
				long intType = rset.getLong(type);
				String _type = "";
				if (type.equalsIgnoreCase("onlinetime"))
				{
					_type = Util.formatTime((int) intType, 2);
				}
				else
				{
					_type = String.valueOf(intType);
				}
				
				_temp.add(new TopScore(character_name, _type, i));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_playersintop.put(type, _temp);
	}
	
	public void generateDataFromAcsBy(String type)
	{
		List<TopScore> _temp = new ArrayList<>();
		int i = 0;
		
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT " + type + ",char_name FROM character_counters LEFT JOIN characters ON ( character_counters.char_id = characters.obj_Id ) where " + type + " > 0 AND accessLevel >= 0 ORDER BY " + type + " DESC");
			while (rset.next())
			{
				String character_name = rset.getString("char_name");
				String _type = String.valueOf(rset.getLong(type));
				
				if ((character_name != null) && !character_name.equals("") && !character_name.contains(" "))
				{
					i++;
					_temp.add(new TopScore(character_name, _type, i));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_playersintop.put(type, _temp);
	}
	
	public void generateClanTop()
	{
		List<TopScore> _temp = new FastTable<>();
		
		int i = 0;
		
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT clan_id,reputation_score FROM clan_data where reputation_score > 0 ORDER BY reputation_score DESC LIMIT 50");
			while (rset.next())
			{
				i++;
				Clan clanId = ClanTable.getInstance().getClan(rset.getInt("clan_id"));
				String clan_name = clanId.getName();
				String _type = String.valueOf(rset.getInt("reputation_score"));
				_temp.add(new TopScore(clan_name, _type, i));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_playersintop.put("clan_top", _temp);
	}
	
	private String[] getCoolor(int cc)
	{
		
		switch (cc)
		{
			case 1:
				return new String[]
				{
					"0f100f",
					"090908",
					"2E2E2E"
				};
			case 2:
				return new String[]
				{
					"140504",
					"300a06",
					"2E2E2E"
				};
			case 3:
				return new String[]
				{
					"0f100f",
					"090908",
					"2E2E2E"
				};
		}
		return new String[]
		{
			"FFFFFF",
			"FFFFFF",
			"FFFFFF"
		};
	}
	
	public void lifeTimeTop(Player player, String type)
	{
		if (player == null)
		{
			return;
		}
		
		String result = "";
		String fullhtm = HtmCache.getInstance().getNotNull("CommunityBoard/Main/" + "top/LifeTimeTop.htm", player);
		String one = HtmCache.getInstance().getNotNull("CommunityBoard/Main/" + "top/LifeTimeTopO.htm", player);
		
		for (TopScore pl : _playersintop.get(type))
		{
			result += one.replaceAll("%place%", "" + pl.getPlace()).replaceAll("%name%", pl.getName()).replaceAll("%count%", "" + pl.getTop()).replaceAll("%bg%", (pl.getPlace() % 2) == 0 ? "121618" : "070e13");
		}
		
		fullhtm = fullhtm.replace("%tops%", result);
		
		if (getPlayerInTop(player.getName(), type) != null)
		{
			fullhtm = fullhtm.replace("%yourPlace%", "You are on " + getPlayerInTop(player.getName(), type).getPlace() + " place!");
		}
		else
		{
			fullhtm = fullhtm.replace("%yourPlace%", "Sorry you are not in the top :(");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(fullhtm);
		player.sendPacket(msg);
	}
	
	public static PlayerTops getInstance()
	{
		if (_instance == null)
		{
			_instance = new PlayerTops();
		}
		return _instance;
	}
	
	public class TopScore
	{
		String _name = "NULL";
		String _topValue = "NULL";
		int _place = 0;
		
		public TopScore(String name, String topValue, int place)
		{
			_name = name;
			_place = place;
			_topValue = topValue;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getTop()
		{
			return _topValue;
		}
		
		public int getPlace()
		{
			return _place;
		}
	}
	
}