package l2r.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;

public class PromotionCheck
{
	private static final PromotionCheck _instance = new PromotionCheck();
	private static final Logger _log = LoggerFactory.getLogger(PromotionCheck.class);
	
	public static final String SELECT_SQL_QUERY = "SELECT * FROM promotion_hwid";
	public static final String INSERT_SQL_QUERY = "INSERT INTO promotion_hwid(hwid) VALUES (?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM promotion_hwid WHERE hwid=?";
	
	private static List<String> _list = new ArrayList<>();
	
	public static PromotionCheck getInstance()
	{
		return _instance;
	}
	
	public void loadHwid()
	{
		_list.clear();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				_list.add(rset.getString("hwid"));
			}
		}
		catch (Exception e)
		{
			_log.error("PromotionCheck:load(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public void insert(String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setString(1, hwid);
			statement.execute();
			
			_list.add(hwid);
		}
		catch (Exception e)
		{
			_log.error("PromotionCheck:insert(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void delete(String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setString(1, hwid);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("PromotionCheck:delete(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int getCountOfHWIDs()
	{
		return _list.size();
	}
	
	public boolean containsHwid(String hwid)
	{
		if (hwid == null)
		{
			return true;
		}
		
		for (String list : _list)
		{
			if ((list == null) || list.isEmpty())
			{
				continue;
			}
			
			if (list.equalsIgnoreCase(hwid))
			{
				return true;
			}
		}
		
		return false;
	}
}
