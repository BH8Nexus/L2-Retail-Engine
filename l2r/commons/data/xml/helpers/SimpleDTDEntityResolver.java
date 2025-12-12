package l2r.commons.data.xml.helpers;

import java.io.File;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class SimpleDTDEntityResolver implements EntityResolver
{
	private final String _fileName;
	
	public SimpleDTDEntityResolver(File f)
	{
		_fileName = f.getAbsolutePath();
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
	{
		return new InputSource(_fileName);
	}
}
