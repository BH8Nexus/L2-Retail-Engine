package l2r.commons.data.xml;

import java.io.File;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import l2r.commons.logging.LoggerObject;

public abstract class AbstractParser<H extends AbstractHolder> extends LoggerObject
{
	protected final H _holder;
	protected String _currentFile;
	protected SAXReader _reader;
	
	protected AbstractParser(H holder)
	{
		_holder = holder;
		_reader = new SAXReader(false);
	}
	
	protected void parseDocument(File file) throws Exception
	{
		_currentFile = file.getName();
		readData(_reader.read(file).getRootElement());
	}
	
	protected abstract void readData(Element rootElement) throws Exception;

	protected abstract void parse();
	
	protected H getHolder()
	{
		return _holder;
	}
	
	public String getCurrentFileName()
	{
		return _currentFile;
	}
	
	public void load()
	{
		parse();
		_holder.process();
		_holder.log();
	}
	
	public void reload()
	{
		info("reload start...");
		_holder.clear();
		load();
	}
}
