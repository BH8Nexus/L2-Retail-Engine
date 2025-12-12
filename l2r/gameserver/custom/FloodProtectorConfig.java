package l2r.gameserver.custom;

import l2r.commons.configuration.ExProperties;

public class FloodProtectorConfig
{
	public final String FLOOD_PROTECTOR_TYPE;
	public int FLOOD_PROTECTION_INTERVAL;
	public boolean LOG_FLOODING;
	public int PUNISHMENT_LIMIT;
	public String PUNISHMENT_TYPE;
	public long PUNISHMENT_TIME;
	
	
	public FloodProtectorConfig(String floodProtectorType)
	{
		FLOOD_PROTECTOR_TYPE =floodProtectorType;
	}
	
	public static FloodProtectorConfig load(String type,ExProperties properties)
	{
		FloodProtectorConfig config = new FloodProtectorConfig(type.toUpperCase());
		
		config.FLOOD_PROTECTION_INTERVAL = properties.getProperty(type + "_FLOOD_PROTECTION_INTERVAL" , 1000);
		config.LOG_FLOODING = properties.getProperty(type + "_LOG_FLOODING", false);
		config.PUNISHMENT_LIMIT = properties.getProperty(type + "_PUNISHMENT_LIMIT", 100);
		config.PUNISHMENT_TYPE = properties.getProperty(type + "_PUNISHMENT_TYPE", "none");
		config.PUNISHMENT_TIME = properties.getProperty(type + "_PUNISHMENT_TIME", 0) * 60000;
		return config;
	}
	
}
