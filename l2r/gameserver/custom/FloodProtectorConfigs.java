package l2r.gameserver.custom;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import l2r.commons.configuration.ExProperties;
import l2r.gameserver.Config;

public class FloodProtectorConfigs
{
	public static final String FLOOD_PROTECTOR_FILE = "config/flood_protector.properties";
	public static final List<FloodProtectorConfig> FLOOD_PROTECTORS = new ArrayList<>();
	
	public static void loard()
	{
		ExProperties floodProtectors = Config.load(FLOOD_PROTECTOR_FILE);
		
		String[] floodProtectorType = floodProtectors.getProperty("FLOOD_PROTECTORS_TYPES", "").split(";");
		
		for (String type : floodProtectorType)
		{
			if (!StringUtils.isEmpty(type))
			{
				FloodProtectorConfig floodProtector = FloodProtectorConfig.load(type, floodProtectors);
				if (floodProtector != null)
				{
					FLOOD_PROTECTORS.add(floodProtector);
				}
			}
		}
	}
}
