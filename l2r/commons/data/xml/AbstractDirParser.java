package l2r.commons.data.xml;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

public abstract class AbstractDirParser<H extends AbstractHolder> extends AbstractParser<H>
{
	protected AbstractDirParser(H holder)
	{
		super(holder);
	}
	
	public abstract File getXMLDir();
	
	public abstract boolean isIgnored(File f);
	
	@Override
	protected final void parse()
	{
		final File dir = getXMLDir();
		
		if (!dir.exists())
		{
			warn("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		
		try
		{
			final Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());
			
			for (final File f : files)
			{
				try
				{
					if (!f.isHidden() && !isIgnored(f))
					{
						parseDocument(f);
					}
				}
				catch (final Exception e)
				{
					info("Exception: " + e + " in file: " + f.getName(), e);
				}
			}
		}
		catch (final Exception e)
		{
			warn("Exception: " + e, e);
		}
	}
}
