package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.Config;
import l2r.gameserver.GameServer;
import l2r.gameserver.config.templates.HostInfo;
import l2r.gameserver.config.xml.holder.HostsConfigHolder;
import l2r.gameserver.network.loginservercon.SendablePacket;

public class AuthRequest extends SendablePacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		writeD(GameServer.AUTH_SERVER_PROTOCOL);
		writeC(Config.REQUEST_ID);
		writeC(0x00); // ACCEPT_ALTERNATE_ID
		writeD(Config.AUTH_SERVER_SERVER_TYPE);
		writeD(Config.AUTH_SERVER_AGE_LIMIT);
		writeC(Config.AUTH_SERVER_GM_ONLY ? 0x01 : 0x00);
		writeC(Config.AUTH_SERVER_BRACKETS ? 0x01 : 0x00);
		writeC(Config.AUTH_SERVER_IS_PVP ? 0x01 : 0x00);
		writeS(Config.EXTERNAL_HOSTNAME);
		writeS(Config.INTERNAL_HOSTNAME);
		writeH(1); // Ports counts
		writeH(Config.PORT_GAME);
		writeD(GameServer.getInstance().getOnlineLimit());
		
		HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
		writeC(hosts.length);
		for (HostInfo host : hosts)
		{
			writeC(host.getId());
			writeS(host.getIP());
			writeS(host.getInnerIP());
			writeH(host.getPort());
			writeS(host.getKey());
		}
	}
}
// package l2r.gameserver.network.loginservercon.gspackets;
//
// import java.net.InetAddress;
//
// import l2r.commons.util.ServerNetworkConfiguration;
// import l2r.gameserver.Config;
// import l2r.gameserver.GameServer;
// import l2r.gameserver.network.loginservercon.SendablePacket;
//
// public class AuthRequest extends SendablePacket
// {
// @Override
// protected void writeImpl()
// {
// writeC(0x00);
// writeD(GameServer.AUTH_SERVER_PROTOCOL);
//
// writeC(Config.GAME_SERVER_NETWORK_CONFIGURATION.size());
// for (ServerNetworkConfiguration gameServer : Config.GAME_SERVER_NETWORK_CONFIGURATION)
// {
// writeH(gameServer.getId());
// writeInetAddress(gameServer.getListenAddress().getAddress());
// writeH(gameServer.getListenAddress().getPort());
// writeInetAddress(gameServer.getExternalAddress());
//
// writeC(gameServer.getHostnames().size());
// for (String hostname : gameServer.getHostnames())
// {
// writeInetAddress(hostname);
// }
//
// writeC(gameServer.getSubnets().size());
// for (String subnet : gameServer.getSubnets())
// {
// writeS(subnet);
// }
// }
// writeC(Config.ACCEPT_ALTERNATE_ID ? 0x01 : 0x00);
// writeD(Config.AUTH_SERVER_SERVER_TYPE);
// writeD(Config.AUTH_SERVER_AGE_LIMIT);
// writeC(Config.AUTH_SERVER_GM_ONLY ? 0x01 : 0x00);
// writeC(Config.AUTH_SERVER_BRACKETS ? 0x01 : 0x00);
// writeC(Config.AUTH_SERVER_IS_PVP ? 0x01 : 0x00);
// writeD(Config.MAXIMUM_ONLINE_USERS);
// }
//
// private void writeInetAddress(String host)
// {
// try
// {
// writeInetAddress(InetAddress.getByName(host));
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// }
//
// private void writeInetAddress(InetAddress addr)
// {
// byte[] data = addr.getAddress();
// writeC(data.length);
// writeB(data);
// }
// }
