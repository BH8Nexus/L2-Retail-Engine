package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2r.commons.data.xml.AbstractFileParser;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.DressCloakHolder;
import l2r.gameserver.model.dress.DressCloakData;

public final class DressCloakParser extends AbstractFileParser<DressCloakHolder>
{
	private static final DressCloakParser _instance = new DressCloakParser();
	
	public static DressCloakParser getInstance()
	{
		return _instance;
	}
	
	private DressCloakParser()
	{
		super(DressCloakHolder.getInstance());
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/dress/cloak.xml");
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for (Iterator<Element> iterator = rootElement.elementIterator("cloak"); iterator.hasNext();)
		{
			String name = null;
			int id, number, itemId;
			long itemCount;
			Element dress = iterator.next();
			number = Integer.parseInt(dress.attributeValue("number"));
			id = Integer.parseInt(dress.attributeValue("id"));
			name = dress.attributeValue("name");
			
			Element price = dress.element("price");
			itemId = Integer.parseInt(price.attributeValue("id"));
			itemCount = Long.parseLong(price.attributeValue("count"));
			
			getHolder().addCloak(new DressCloakData(number, id, name, itemId, itemCount));
		}
	}
}