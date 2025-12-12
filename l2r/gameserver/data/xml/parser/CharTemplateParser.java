package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import l2r.commons.data.xml.AbstractFileParser;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.CharTemplateHolder;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.item.CreateItem;

public class CharTemplateParser extends AbstractFileParser<CharTemplateHolder>
{
	private static final CharTemplateParser _instance = new CharTemplateParser();
	
	public static CharTemplateParser getInstance()
	{
		return _instance;
	}
	
	protected CharTemplateParser()
	{
		super(CharTemplateHolder.getInstance());
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/char_templates.xml");
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for (final Iterator<?> interator = rootElement.elementIterator(); interator.hasNext();)
		{
			final List<CreateItem> items = new ArrayList<>();
			
			final Element element = (org.dom4j.Element) interator.next();
			final StatsSet set = new StatsSet();
			
			final int classId = Integer.parseInt(element.attributeValue("id"));
			final String name = element.attributeValue("name");
			set.set("name", name);
			
			for (final Iterator<?> template = element.elementIterator(); template.hasNext();)
			{
				final Element templat = (org.dom4j.Element) template.next();
				if (templat.getName().equalsIgnoreCase("set"))
				{
					set.set(templat.attributeValue("name"), templat.attributeValue("value"));
				}
				else if (templat.getName().equalsIgnoreCase("item"))
				{
					try
					{
						final int itemId = Integer.parseInt(templat.attributeValue("id"));
						final int count = Integer.parseInt(templat.attributeValue("count"));
						boolean equipable = false;
						int shortcat = -1;
						if (templat.attributeValue("equipable") != null)
						{
							equipable = Boolean.parseBoolean(templat.attributeValue("equipable"));
						}
						if (templat.attributeValue("shortcut") != null)
						{
							shortcat = Integer.parseInt(templat.attributeValue("shortcut"));
						}
						items.add(new CreateItem(itemId, count, equipable, shortcat));
					}
					catch (Exception e)
					{
						_log.error("Error parsing char_template, add item for classId " + set.get("classId") + ": ", e);
					}
				}
			}
			
			getHolder().addTemplate(classId, set, items);
		}
	}
}
