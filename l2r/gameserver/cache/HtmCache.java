package l2r.gameserver.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.math.TIntStringHashMap;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Strings;
import l2r.gameserver.utils.UnicodeReader;

public class HtmCache
{
	private static final Logger LOG = LoggerFactory.getLogger(HtmCache.class);
	
	/**
	 * Field _cache.
	 */
	private final Map<Integer, String> _htmCache;
	private final FileFilter _htmFilter;
	
	private final static HtmCache _instance = new HtmCache();
	
	public static HtmCache getInstance()
	{
		return _instance;
	}
	
	/**
	 * Constructor for HtmCache.
	 */
	private HtmCache()
	{
		_htmCache = new HashMap<>();
		_htmFilter = new HtmFilter();
	}
	
	public void reload()
	{
		LOG.warn("HtmCache: Cache cleared, had " + _htmCache.size() + " entries.");
		_htmCache.clear();
	}
	
	/**
	 * Reloads given directory. All sub-directories are parsed, all html files are loaded to HtmCache.
	 * @param path : Directory to be reloaded.
	 */
	public void reloadPath(String path)
	{
		parseDir(new File(path));
		LOG.warn("HtmCache: Reloaded specified " + path + " path.");
	}
	
	private void parseDir(File dir)
	{
		for (File file : dir.listFiles(_htmFilter))
		{
			if (file.isDirectory())
			{
				parseDir(file);
			}
			else
			{
				loadFile(file);
			}
		}
	}
	
	public boolean isLoadable(String path)
	{
		final File file = new File(path);
		
		if (file.exists() && _htmFilter.accept(file) && !file.isDirectory())
		{
			return loadFile(file) != null;
		}
		
		return false;
	}
	
	/**
	 * Loads html file content to HtmCache.
	 * @param file : File to be cached.
	 * @return String : Content of the file.
	 */
	private String loadFile(File file)
	{
		try (FileInputStream fis = new FileInputStream(file);
			UnicodeReader ur = new UnicodeReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(ur))
		{
			final StringBuilder sb = new StringBuilder();
			String line;
			
			while ((line = br.readLine()) != null)
			{
				sb.append(line).append('\n');
			}
			
			final String content = sb.toString().replaceAll("\r\n", "\n");
			
			_htmCache.put(file.getPath().replace("\\", "/").hashCode(), content);
			return content;
		}
		catch (Exception e)
		{
			LOG.warn("HtmCache: problem with loading file " + e);
			return null;
		}
	}
	
	/**
	 * Return content of html message given by filename.
	 * @param filename : Desired html filename.
	 * @param lang
	 * @return String : Returns content if filename exists, otherwise returns null.
	 */
	public String getHtm(String filename, Language lang)
	{
		if (lang.ordinal() == 0)
		{
			// filename = "data/html/html" + "-" + lang.getShortName() + "/" + filename;
			filename = "data/html/html-en/" + filename;
		}
		else if (lang.ordinal() == 1)
		{
			// filename = "data/html/html" + "-" + lang.getShortName() + "/" + filename;
			filename = "data/html/html-ru/" + filename;
		}
		else if (lang.ordinal() == 2)
		{
			filename = "data/html/html-br/" + filename;
		}
		
		if ((filename == null) || filename.isEmpty())
		{
			return "";
		}
		
		String content = _htmCache.get(filename.hashCode());
		if (content == null)
		{
			final File file = new File(filename);
			
			if (file.exists() && _htmFilter.accept(file) && !file.isDirectory())
			{
				content = loadFile(file);
			}
		}
		
		return Strings.bbParse(content);
	}
	
	/**
	 * Return content of html message given by filename. In case filename does not exist, returns notice.
	 * @param player
	 * @param filename : Desired html filename.
	 * @return String : Returns content if filename exists, otherwise returns notice.
	 */
	public String getHtmForce(Player player, String filename)
	{
		String content = getHtm(filename, player.getLanguage());
		if (content == null)
		{
			content = "<html><body>My html is missing:<br>" + filename + "</body></html>";
			LOG.warn("HtmCache: " + filename + " is missing.");
		}
		
		return content;
	}
	
	/**
	 * Method getNotNull.
	 * @param fileName String
	 * @param player Player
	 * @return String
	 */
	public String getNotNull(String fileName, Player player)
	{
		Language lang = player == null ? Language.ENGLISH : player.getLanguage();
		
		// if ((fileName.contains("bbs_regiontpl.htm") || fileName.contains("bbs_region_stpl.htm") || fileName.contains("bbs_region_list.htm") || fileName.contains("bbs_region_sellers.htm") || fileName.contains("bbs_region_storetpl.htm") || (fileName.contains("bbs_region_view.htm.htm") &&
		// player.isLangRus())))
		// {
		// lang = Language.ENGLISH;
		// }
		if (player.isGM())
		{
			Functions.sendDebugMessage(player, "HTML: " + fileName);
		}
		
		// String cache = getHtm(fileName, lang);
		// if (StringUtils.isEmpty(cache))
		// {
		// cache = "Dialog not found: " + fileName + "; Lang: " + lang;
		// }
		
		// if(player.isGM()) Functions.sendDebugMessage(player, "HTML: " + fileName);
		
		// return cache;
		return getNotNull(fileName, lang);
	}
	
//	public String getHtml(String fileName, Player player)
//	{
//		Language language = player == null  ? player.getLanguage();
//	}
//	
	public TIntStringHashMap getTemplate(String fileName,Player player)
	{
		return l2r.gameserver.utils.Util.parseTemplates(getNullable(fileName, player));
	}
	
	public String getNotNull(String fileName, Language lang)
	{
		String cache = getHtm(fileName, lang);
		
		if (StringUtils.isEmpty(cache))
		{
			cache = "Dialog not found: " + fileName + "; Lang: " + lang;
		}
		return cache;
	}
	
	/**
	 * Method getNullable.
	 * @param fileName String
	 * @param player Player
	 * @return String
	 */
	public String getNullable(String fileName, Player player)
	{
		Language lang = player == null ? Language.ENGLISH : player.getLanguage();
		String cache = getHtm(fileName, lang);
		
		if (StringUtils.isEmpty(cache))
		{
			return null;
		}
		
		return cache;
	}
	
	protected class HtmFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			// directories, *.htm and *.html files
			return file.isDirectory() || file.getName().endsWith(".htm") || file.getName().endsWith(".html");
		}
	}
}