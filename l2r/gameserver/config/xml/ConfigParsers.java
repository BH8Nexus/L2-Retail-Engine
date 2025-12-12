package l2r.gameserver.config.xml;

import l2r.gameserver.config.xml.parser.HostsConfigParser;

/**
 * @author Bonux
 **/
public abstract class ConfigParsers
{
	public static void parseAll()
	{
		HostsConfigParser.getInstance().load();
	}
}