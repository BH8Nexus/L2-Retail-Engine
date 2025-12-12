package l2r.gameserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantNamesTable
{
	private static final Logger _log = LoggerFactory.getLogger(EnchantNamesTable.class);
	
	private final Map<String, String> _enchantNames = new HashMap<>();
	
	public EnchantNamesTable()
	{
		load();
	}
	
	public void load()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM enchant_names WHERE skill_id > 0");
			ResultSet rs = st.executeQuery())
		{
			while (rs.next())
			{
				_enchantNames.put(rs.getInt("skill_id") + "-" + (rs.getInt("skill_lvl") / 100), rs.getString("enchant_name"));
			}
			
			_log.info(getClass().getSimpleName() + ": Loaded: " + _enchantNames.size() + " skill enchant names");
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while loading skill enchant names: ", e);
		}
	}
	
	/**
	 * @param skillId
	 * @param enchantType
	 * @return Get the enchant name of a certain enchant path of that skill
	 */
	public String getEnchantName(int skillId, int enchantType)
	{
		return _enchantNames.get(skillId + "-" + enchantType);
	}
	
	public static EnchantNamesTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantNamesTable _instance = new EnchantNamesTable();
	}
}
