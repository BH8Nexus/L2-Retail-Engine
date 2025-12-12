package l2r.gameserver.randoms;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javolution.util.FastMap;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.VisualizerInstance;
import l2r.gameserver.utils.Location;

/**
 * @author Midnex
 */
public class Visualizer
{
	/**
	 * 0-chest 1-legs 2-gloves 3-boots
	 */
	
	private static FastMap<Integer, FastMap<Integer, String>> _avaibleItems;
	
	private static List<NpcInstance> _npcs = new ArrayList<>();
	
	public static FastMap<Integer, String> getAll(int type)
	{
		return _avaibleItems.get(type);
	}
	
	public static FastMap<Integer, String> get(int type, int from, int to)
	{
		FastMap<Integer, String> _temp = new FastMap<>();
		int i = -1;
		for (Entry<Integer, String> lol : _avaibleItems.get(type).entrySet())
		{
			i++;
			if (from > i)
			{
				continue;
			}
			if (to == i)
			{
				break;
			}
			_temp.put(lol.getKey(), lol.getValue());
		}
		return _temp;
	}
	
	public static int getPageIn(int type, int id)
	{
		int page = 1;
		int c = 0;
		for (int lol : _avaibleItems.get(type).keySet())
		{
			c++;
			if (c > 4)
			{
				c = 1;
				page++;
			}
			if (lol == id)
			{
				break;
			}
		}
		return page;
	}
	
	public static boolean isAvaible(int type, int item)
	{
		return _avaibleItems.get(type).containsKey(item);
	}
	
	public static void load()
	{
		_avaibleItems = new FastMap<>();
		
		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/data/xml/visualSets.xml");
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			Document doc1 = factory1.newDocumentBuilder().parse(file);
			for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n1.getNodeName()))
				{
					for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
					{
						if ("set".equalsIgnoreCase(d1.getNodeName()))
						{
							int slot = Integer.parseInt(d1.getAttributes().getNamedItem("slot").getNodeValue());
							int id = Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue());
							
							String icon = "icon." + ItemHolder.getInstance().getTemplate(id).getIcon();
							
							if (_avaibleItems.get(slot) == null)
							{
								_avaibleItems.put(slot, new FastMap<Integer, String>());
								_avaibleItems.get(slot).put(id, icon);
							}
							else
							{
								_avaibleItems.get(slot).put(id, icon);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void spawnVisual(Player player)
	{
		// TODO: Infern0, conditions where to spawn, and when...
		String[] models = new String[]
		{
			"0,0,0,0,0,0,0",
			"0,0,0,0,0,0,0",
			"0,0,0,0,0,0,0"
		};
		
		// models
		_npcs.add(spawn(new Location(-85912, 243496, -3720), 603, player.getReflectionId(), "model", "Set One", models[0]));
		_npcs.add(spawn(new Location(-85976, 243544, -3720), 603, player.getReflectionId(), "model", "Set Two", models[1]));
		_npcs.add(spawn(new Location(-86056, 243624, -3720), 603, player.getReflectionId(), "model", "Set Three", models[2]));
	}
	
	private static NpcInstance spawn(Location loc, int npcId, int ref, String name, String title, String items)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(npcId).getNewInstance();
			String[] item = items.split(",");
			((VisualizerInstance) npc).setItems(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Integer.parseInt(item[2]), Integer.parseInt(item[3]), Integer.parseInt(item[4]), Integer.parseInt(item[5]), Integer.parseInt(item[6]));
			npc.setName(name);
			npc.setTitle(title);
			npc.setReflection(ref);
			npc.setSpawnedLoc(loc.correctGeoZ());
			npc.spawnMe(npc.getSpawnedLoc());
			
			return npc;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void destroyVisual()
	{
		for (NpcInstance npc : _npcs)
		{
			if (npc != null)
			{
				npc.deleteMe();
			}
		}
		
		_npcs = new ArrayList<>();
	}
}
