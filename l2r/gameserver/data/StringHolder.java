package l2r.gameserver.data;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Language;

/**
 * Author: VISTALL Date: 19:27/29.12.2010
 */
public final class StringHolder extends AbstractHolder
{
	private static final Logger LOG = LoggerFactory.getLogger(StringHolder.class);
	private static final StringHolder _instance = new StringHolder();
	private final Map<Language, Map<String, String>> _strings = new HashMap<>();
	
	public static StringHolder getInstance()
	{
		return _instance;
	}
	
	private StringHolder()
	{
		
	}
	
	public String getNullable(Player player, String name)
	{
		Language lang = player == null ? Language.ENGLISH : player.getLanguage();
		return get(lang, name);
	}
	
	public static String getNotNull(int playerId, String name, Object... replacements)
	{
		final Player player = GameObjectsStorage.getPlayer(playerId);
		Language lang;
		if (player == null)
		{
			lang = CharacterDAO.getLanguage(playerId);
		}
		else
		{
			lang = player.getLanguage();
		}
		return getNotNull(lang, name, replacements);
	}
	
	public String getNotNull(Player player, String name)
	{
		Language lang = player == null ? Language.ENGLISH : player.getLanguage();
		
		String text = get(lang, name);
		if ((text == null) && (player != null))
		{
			text = "Not find string: " + name + "; for lang: " + lang;
			_strings.get(lang).put(name, text);
		}
		
		return text;
	}
	
	public static String getNotNull(Player player, String name, Object... replacements)
	{
		final Language lang = player == null ? Language.ENGLISH : player.getLanguage();
		return getNotNull(lang, name, replacements);
	}
	
	public static String getNotNull(Language lang, String name, Object... replacements)
	{
		String text = getInstance().get(lang, name);
		if (text == null)
		{
			LOG.warn("Not found string: " + name + "; for lang: " + lang);
			if (lang == Language.ENGLISH)
			{
				return getInstance().putNotFoundMsg(Language.ENGLISH, name);
			}
			text = getNotNull(Language.ENGLISH, name, new Object[0]);
		}
		if (replacements.length > 0)
		{
			return format(text, replacements);
		}
		return text;
	}
	
	private String putNotFoundMsg(Language lang, String address)
	{
		return _strings.get(lang).put(address, "Text was NOT found! Server Staff was notified!");
	}
	
	private static String format(String messageValue, Object... replacements)
	{
		String newValue = messageValue;
		int index = 0;
		for (Object arg : replacements)
		{
			newValue = newValue.replace("{" + index + "}", arg.toString());
			++index;
		}
		return newValue;
	}
	
	private String get(Language lang, String address)
	{
		Map<String, String> strings = _strings.get(lang);
		
		return strings.get(address);
	}
	
	public void load()
	{
		for (Language lang : Language.VALUES)
		{
			_strings.put(lang, new HashMap<String, String>());
			
			File f = new File(Config.DATAPACK_ROOT, "data/xml/string/strings_" + lang.getShortName() + ".properties");
			if (!f.exists())
			{
				warn("Not find file: " + f.getAbsolutePath());
				continue;
			}
			
			LineNumberReader reader = null;
			try
			{
				reader = new LineNumberReader(new FileReader(f));
				String line = null;
				
				while ((line = reader.readLine()) != null)
				{
					if (line.startsWith("#"))
					{
						continue;
					}
					
					StringTokenizer token = new StringTokenizer(line, "=");
					if (token.countTokens() < 2)
					{
						error("Error on line: " + line + "; file: " + f.getName());
						continue;
					}
					
					String name = token.nextToken();
					String value = token.nextToken();
					while (token.hasMoreTokens())
					{
						value += "=" + token.nextToken();
					}
					
					Map<String, String> strings = _strings.get(lang);
					
					strings.put(name, value);
				}
			}
			catch (Exception e)
			{
				error("Exception: " + e, e);
			}
			finally
			{
				try
				{
					if (reader != null)
					{
						reader.close();
					}
				}
				catch (Exception e)
				{
				}
			}
		}
		
		log();
	}
	
	public void reload()
	{
		clear();
		load();
	}
	
	@Override
	public void log()
	{
		for (Map.Entry<Language, Map<String, String>> entry : _strings.entrySet())
		{
			info("load strings: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public void clear()
	{
		_strings.clear();
	}
}
