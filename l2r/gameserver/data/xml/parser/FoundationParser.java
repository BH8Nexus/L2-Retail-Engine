package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2r.commons.data.xml.AbstractFileParser;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.FoundationHolder;

public final class FoundationParser extends AbstractFileParser<FoundationHolder>
{
	private static final FoundationParser _instance = new FoundationParser();
	
	public static FoundationParser getInstance()
	{
		return _instance;
	}
	
	private FoundationParser()
	{
		super(FoundationHolder.getInstance());
	}
	
	@Override
	public File getXMLFile()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/stats/foundation/foundation.xml");
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for (Iterator<Element> iterator = rootElement.elementIterator("foundation"); iterator.hasNext();)
		{
			Element foundation = iterator.next();
			int simple = Integer.parseInt(foundation.attributeValue("simple"));
			int found = Integer.parseInt(foundation.attributeValue("found"));
			
			getHolder().addFoundation(simple, found);
		}
	}
}