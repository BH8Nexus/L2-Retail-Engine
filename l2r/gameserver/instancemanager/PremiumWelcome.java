package l2r.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class PremiumWelcome
{
	private static final Logger _log = LoggerFactory.getLogger(PremiumWelcome.class);
	
	private static PremiumWelcome _instance;
	
	public static final PremiumWelcome getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing PremiumWelcome");
			_instance = new PremiumWelcome();
		}
		return _instance;
	}
	
	private PremiumWelcome()
	{
		loadMessages();
	}
	
	private static Map<Integer, String> MESSAGES = new HashMap<>();
	
	private static void loadMessages()
	{
		if (Config.DEBUG)
		{
			_log.debug("Loading data ...");
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		String path = Config.DATAPACK_ROOT + "/config/services/premium.xml";
		File XMLFile = new File(path);
		if (!XMLFile.exists())
		{
			_log.error(path + " could not be loaded. File not found!");
			return;
		}
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(XMLFile), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(XMLFile);
			for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if (na.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node n = na.getFirstChild(); n != null; n = n.getNextSibling())
					{
						if (n.getNodeName().equals("message"))
						{
							NamedNodeMap ndm = n.getAttributes();
							
							int id = Integer.valueOf(ndm.getNamedItem("id").getNodeValue());
							StringBuilder out = new StringBuilder();
							
							out.append(ndm.getNamedItem("text").getNodeValue());
							out.append("`");
							out.append(ndm.getNamedItem("sendtype").getNodeValue());
							out.append("`");
							out.append(ndm.getNamedItem("sendfrom").getNodeValue());
							
							String str = out.toString();
							
							if (Config.DEBUG)
							{
								_log.debug("Out: " + str);
							}
							
							if (!MESSAGES.containsKey(id) /* && !out.isEmpty() */)
							{
								MESSAGES.put(id, str);
							}
						}
					}
				}
			}
			_log.info("Loaded " + MESSAGES.size() + " premium welcome messages");
		}
		catch (Exception e)
		{
			_log.error("Error on loading data from: " + path);
		}
	}
	
	public static void sendTo(Player pc)
	{
		Iterator<String> itr = MESSAGES.values().iterator();
		while (itr.hasNext())
		{
			String msg = itr.next().toString();
			
			if (msg.contains("`"))
			{
				String[] unp = msg.split("`");
				
				if (unp[1].equals("SM"))
				{
					pc.sendMessage(unp[0]);
				}
				else if (unp[1].equals("PM"))
				{
					pc.sendPacket(new Say2(1, ChatType.ALL, unp[2], unp[0]));
				}
				else if (unp[1].equals("GM"))
				{
					pc.sendPacket(new Say2(10, ChatType.ANNOUNCEMENT, unp[2], unp[0]));
				}
				else if (unp[1].equals("HERO"))
				{
					pc.sendPacket(new Say2(17, ChatType.HERO_VOICE, unp[2], unp[0]));
				}
			}
			else
			{
				_log.warn("Welcome message #" + itr.toString() + "cannot be split!");
			}
		}
	}
}