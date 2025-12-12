package l2r.loginserver.gameservercon.lspackets;

import l2r.loginserver.Config;
import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.GameServer.HostInfo;
import l2r.loginserver.gameservercon.SendablePacket;

public class AuthResponse extends SendablePacket
{
	private final HostInfo[] _hosts;
	
	public AuthResponse(GameServer gs)
	{
		_hosts = gs.getHosts();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		writeC(0x00); // ServerId
		writeS(""); // ServerName
		writeC(_hosts.length);
		for (HostInfo host : _hosts)
		{
			writeC(host.getId());
			writeS(Config.SERVER_NAMES.get(host.getId()));
		}
	}
}