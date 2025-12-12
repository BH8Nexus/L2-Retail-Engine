package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2r.commons.data.xml.AbstractFileParser;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.HuntingZoneHolder;
import l2r.gameserver.data.xml.holder.HuntingZoneHolder.HuntingZone;
import l2r.gameserver.templates.StatsSet;

/**
 * @author VISTALL
 * @date 22:21/09.03.2011
 */
public final class HuntingZoneParser extends AbstractFileParser<HuntingZoneHolder>
{
	private static HuntingZoneParser _instance = new HuntingZoneParser();
	
	public static HuntingZoneParser getInstance()
	{
		return _instance;
	}
	
	private HuntingZoneParser()
	{
		super(HuntingZoneHolder.getInstance());
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/huntingzones.xml");
	}
	
	@Override
	protected void readData(Element rootElement)
	{
		for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element staticObjectElement = iterator.next();
			StatsSet set = new StatsSet();
			
			set.set("id", staticObjectElement.attributeValue("id"));
			set.set("name", staticObjectElement.attributeValue("name"));
			set.set("level", staticObjectElement.attributeValue("level"));
			set.set("huntingType", staticObjectElement.attributeValue("huntingType"));
			set.set("x", staticObjectElement.attributeValue("x"));
			set.set("y", staticObjectElement.attributeValue("y"));
			set.set("z", staticObjectElement.attributeValue("z"));
			set.set("affiliated_area_id", staticObjectElement.attributeValue("affiliated_area_id"));
			
			getHolder().addHuntingZone(new HuntingZone(set));
		}
	}
}
