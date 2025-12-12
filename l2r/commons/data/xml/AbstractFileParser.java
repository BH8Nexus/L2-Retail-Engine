package l2r.commons.data.xml;

import java.io.File;

public abstract class AbstractFileParser<H extends AbstractHolder> extends AbstractParser<H>
{
	protected AbstractFileParser(H holder)
	{
		super(holder);
	}
	
	public abstract File getXMLFile();
	
	@Override
	protected final void parse()
	{
		final File file = getXMLFile();
		
		if (!file.exists())
		{
			warn("file " + file.getAbsolutePath() + " not exists");
			return;
		}
		
		try
		{
			parseDocument(file);
		}
		catch (final Exception e)
		{
			warn("Exception: " + e, e);
		}
	}
}
