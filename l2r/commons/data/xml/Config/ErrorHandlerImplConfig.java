package l2r.commons.data.xml.Config;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Author: VISTALL Date: 20:43/30.11.2010
 */
public class ErrorHandlerImplConfig implements ErrorHandler
{
	private final AbstractParserConfig<?> _parser;
	
	public ErrorHandlerImplConfig(AbstractParserConfig<?> parser)
	{
		_parser = parser;
	}
	
	@Override
	public void warning(SAXParseException exception)
	{
		_parser.warn("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " warning: " + exception.getMessage());
	}
	
	@Override
	public void error(SAXParseException exception)
	{
		_parser.error("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " error: " + exception.getMessage());
	}
	
	@Override
	public void fatalError(SAXParseException exception)
	{
		_parser.error("File: " + _parser.getCurrentFileName() + ":" + exception.getLineNumber() + " fatal: " + exception.getMessage());
	}
}
