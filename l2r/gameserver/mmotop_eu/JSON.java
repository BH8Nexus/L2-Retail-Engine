package l2r.gameserver.mmotop_eu;

import java.util.HashMap;
import java.util.Map;

public class JSON
{
	private Map<String, String> data = new HashMap<>();
	
	public JSON(String text)
	{
		for(String s : text.replaceAll("[{}\"]", "").replace("result:", "").split(","))
		{
			String[] d = s.split(":");
			if(d[0] != null)
				data.put(d[0], d[1]);
		}
	}
	
	public String getString(String key)
	{
		return data.get(key);
	}
	
	public Integer getInteger(String key)
	{
		try
		{
			return Integer.valueOf(data.get(key));
		}
		catch(Exception e)
		{
			return -1;
		}
	}
}
