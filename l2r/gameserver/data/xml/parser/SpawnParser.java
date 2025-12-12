package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.data.xml.AbstractDirParser;
import l2r.commons.geometry.Circle;
import l2r.commons.geometry.Polygon;
import l2r.commons.geometry.Rectangle;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.SpawnHolder;
import l2r.gameserver.model.Territory;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.spawn.PeriodOfDay;
import l2r.gameserver.templates.spawn.SpawnNpcInfo;
import l2r.gameserver.templates.spawn.SpawnTemplate;
import l2r.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 18:38/10.12.2010
 */
public final class SpawnParser extends AbstractDirParser<SpawnHolder>
{
	private static final SpawnParser _instance = new SpawnParser();
	
	public static SpawnParser getInstance()
	{
		return _instance;
	}
	
	protected SpawnParser()
	{
		super(SpawnHolder.getInstance());
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/spawn/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Map<String, Territory> territories = new HashMap<>();
		spawner: for (Iterator<Element> spawnIterator = rootElement.elementIterator(); spawnIterator.hasNext();)
		{
			Element spawnElement = spawnIterator.next();
			if (spawnElement.getName().equalsIgnoreCase("territory"))
			{
				String terName = spawnElement.attributeValue("name");
				Territory territory = parseTerritory(terName, spawnElement);
				territories.put(terName, territory);
			}
			else if (spawnElement.getName().equalsIgnoreCase("spawn"))
			{
				String group = spawnElement.attributeValue("group");
				int respawn = spawnElement.attributeValue("respawn") == null ? 60 : Integer.parseInt(spawnElement.attributeValue("respawn"));
				int respawnRandom = spawnElement.attributeValue("respawn_random") == null ? 0 : Integer.parseInt(spawnElement.attributeValue("respawn_random"));
				int count = spawnElement.attributeValue("count") == null ? 1 : Integer.parseInt(spawnElement.attributeValue("count"));
				PeriodOfDay periodOfDay = spawnElement.attributeValue("period_of_day") == null ? PeriodOfDay.NONE : PeriodOfDay.valueOf(spawnElement.attributeValue("period_of_day").toUpperCase());
				if (group == null)
				{
					group = periodOfDay.name();
				}
				SpawnTemplate template = new SpawnTemplate(periodOfDay, count, respawn, respawnRandom);
				
				for (Iterator<Element> subIterator = spawnElement.elementIterator(); subIterator.hasNext();)
				{
					Element subElement = subIterator.next();
					if (subElement.getName().equalsIgnoreCase("point"))
					{
						int x = Integer.parseInt(subElement.attributeValue("x"));
						int y = Integer.parseInt(subElement.attributeValue("y"));
						int z = Integer.parseInt(subElement.attributeValue("z"));
						int h = subElement.attributeValue("h") == null ? -1 : Integer.parseInt(subElement.attributeValue("h"));
						
						template.addSpawnRange(new Location(x, y, z, h));
					}
					else if (subElement.getName().equalsIgnoreCase("rectangle"))
					{
						int x1 = Integer.parseInt(subElement.attributeValue("x1"));
						int y1 = Integer.parseInt(subElement.attributeValue("y1"));
						int x2 = Integer.parseInt(subElement.attributeValue("x2"));
						int y2 = Integer.parseInt(subElement.attributeValue("y2"));
						int zmin = Integer.parseInt(subElement.attributeValue("zmin"));
						int zmax = Integer.parseInt(subElement.attributeValue("zmax"));
						
						Rectangle rectangle = new Rectangle(x1, y1, x2, y2);
						rectangle.setZmin(zmin);
						rectangle.setZmax(zmax);
						
						Territory territory = new Territory();
						territory.add(rectangle);
						
						template.addSpawnRange(territory);
					}
					else if (subElement.getName().equalsIgnoreCase("circle"))
					{
						int x = Integer.parseInt(subElement.attributeValue("x"));
						int y = Integer.parseInt(subElement.attributeValue("y"));
						int zmin = Integer.parseInt(subElement.attributeValue("zmin"));
						int zmax = Integer.parseInt(subElement.attributeValue("zmax"));
						int radius = Integer.parseInt(subElement.attributeValue("radius"));
						
						Circle circle = new Circle(x, y, radius);
						circle.setZmin(zmin);
						circle.setZmax(zmax);
						
						Territory territory = new Territory();
						territory.add(circle);
						
						template.addSpawnRange(territory);
					}
					else if (subElement.getName().equalsIgnoreCase("territory"))
					{
						String terName = subElement.attributeValue("name");
						if (terName != null)
						{
							Territory g = territories.get(terName);
							if (g == null)
							{
								error("Invalid territory name: " + terName + "; " + getCurrentFileName());
								continue;
							}
							template.addSpawnRange(g);
						}
						else
						{
							Territory temp = parseTerritory(null, subElement);
							
							template.addSpawnRange(temp);
							
						}
					}
					else if (subElement.getName().equalsIgnoreCase("territoryName"))
					{
						for (String string : subElement.attributeValue("name").split(";"))
						{
							template.addTerritory(territories.get(string));
						}
						int ter = Rnd.get(1, template.getTerritoris().size());
						Territory g = template.getTerritoris().get(ter - 1);
						template.addSpawnRange(g);
						continue;
					}
					else if (subElement.getName().equalsIgnoreCase("npc"))
					{
						int npcId = Integer.parseInt(subElement.attributeValue("id"));
						if (Config.NPC_DONTSPAWN_LIST.contains(npcId))
						{
							continue spawner;
						}
						int max = subElement.attributeValue("max") == null ? 0 : Integer.parseInt(subElement.attributeValue("max"));
						MultiValueSet<String> parameters = StatsSet.EMPTY;
						for (Element e : subElement.elements())
						{
							if (parameters.isEmpty())
							{
								parameters = new MultiValueSet<>();
							}
							
							parameters.set(e.attributeValue("name"), e.attributeValue("value"));
						}
						template.addNpc(new SpawnNpcInfo(npcId, max, parameters));
					}
				}
				
				if (template.getNpcSize() == 0)
				{
					warn("Npc id is zero! File: " + getCurrentFileName());
					continue;
				}
				
				if (template.getSpawnRangeSize() == 0)
				{
					warn("No points to spawn! File: " + getCurrentFileName());
					continue;
				}
				
				getHolder().addSpawn(group, template);
			}
		}
	}
	
	private Territory parseTerritory(String name, Element e)
	{
		final Territory t = new Territory();
		t.add(parsePolygon0(name, e));
		
		for (final Iterator<Element> iterator = e.elementIterator("banned_territory"); iterator.hasNext();)
		{
			t.addBanned(parsePolygon0(name, iterator.next()));
		}
		
		return t;
	}
	
	private Polygon parsePolygon0(String name, Element e)
	{
		Polygon temp = new Polygon();
		for (Iterator<Element> addIterator = e.elementIterator("add"); addIterator.hasNext();)
		{
			Element addElement = addIterator.next();
			int x = Integer.parseInt(addElement.attributeValue("x"));
			int y = Integer.parseInt(addElement.attributeValue("y"));
			int zmin = Integer.parseInt(addElement.attributeValue("zmin"));
			int zmax = Integer.parseInt(addElement.attributeValue("zmax"));
			temp.add(x, y).setZmin(zmin).setZmax(zmax);
		}
		
		if (!temp.validate())
		{
			error("Invalid polygon: " + name + "{" + temp + "}. File: " + getCurrentFileName());
		}
		return temp;
	}
}
