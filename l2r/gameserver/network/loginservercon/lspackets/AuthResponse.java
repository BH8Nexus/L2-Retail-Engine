
package l2r.gameserver.network.loginservercon.lspackets;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.ReceivablePacket;
import l2r.gameserver.network.loginservercon.gspackets.OnlineStatus;
import l2r.gameserver.network.loginservercon.gspackets.PlayerInGame;

/**
 * @reworked by Bonux
 **/
public class AuthResponse extends ReceivablePacket
{
	private static class ServerInfo
	{
		private final int _id;
		private final String _name;
		
		public ServerInfo(int id, String name)
		{
			_id = id;
			_name = name;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return _name;
		}
	}
	
	private static final Logger _log = LoggerFactory.getLogger(AuthResponse.class);
	
	private List<ServerInfo> _servers;
	
	@Override
	protected void readImpl()
	{
		int serverId = readC();
		String serverName = readS();
		if (!getByteBuffer().hasRemaining())
		{
			_servers = new ArrayList<>(1);
			_servers.add(new ServerInfo(serverId, serverName));
		}
		else
		{
			int serversCount = readC();
			_servers = new ArrayList<>(serversCount);
			for (int i = 0; i < serversCount; i++)
			{
				_servers.add(new ServerInfo(readC(), readS()));
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		for (ServerInfo info : _servers)
		{
			_log.info("Registered on authserver as " + info.getId() + " [" + info.getName() + "]");
		}
		
		sendPacket(new OnlineStatus(true));
		
		String[] accounts = AuthServerCommunication.getInstance().getAccounts();
		for (String account : accounts)
		{
			sendPacket(new PlayerInGame(account));
		}
	}
}

/*
 * package l2r.gameserver.network.loginservercon.lspackets; import org.slf4j.Logger; import org.slf4j.LoggerFactory; import l2r.gameserver.network.loginservercon.AuthServerCommunication; import l2r.gameserver.network.loginservercon.ReceivablePacket; import
 * l2r.gameserver.network.loginservercon.gspackets.OnlineStatus; import l2r.gameserver.network.loginservercon.gspackets.PlayerInGame; public class AuthResponse extends ReceivablePacket { private static final Logger _log = LoggerFactory.getLogger(AuthResponse.class); private int _serverId; private
 * String _serverName;
 * @Override protected void readImpl() { _serverId = readC(); _serverName = readS(); }
 * @Override protected void runImpl() { _log.info("Registered on authserver as " + _serverId + " [" + _serverName + "]"); sendPacket(new OnlineStatus(true)); String[] accounts = AuthServerCommunication.getInstance().getAccounts(); for (String account : accounts) { sendPacket(new
 * PlayerInGame(account)); } } }
 */
