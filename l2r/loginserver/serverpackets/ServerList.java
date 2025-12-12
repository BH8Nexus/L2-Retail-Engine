package l2r.loginserver.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import l2r.commons.net.utils.NetUtils;
import l2r.loginserver.Config;
import l2r.loginserver.GameServerManager;
import l2r.loginserver.accounts.Account;
import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.GameServer.HostInfo;

public class ServerList extends L2LoginServerPacket
{
	protected final List<ServerData> _servers = new ArrayList<>();
	private final List<ServerData> _tempServers = new ArrayList<>();
	private final List<ServerData> _tempServersLatin = new ArrayList<>();
	
	private final int _lastServer;
	private int _paddedBytes;
	private final Random rnd = new Random();
	
	protected static class ServerData
	{
		int serverId;
		InetAddress ip;
		int port;
		int online;
		int maxPlayers;
		boolean status;
		boolean pvp;
		boolean brackets;
		int type;
		int ageLimit;
		int playerSize;
		int[] deleteChars;
		
		ServerData(int serverId, InetAddress ip, int port, boolean pvp, boolean brackets, int type, int online, int maxPlayers, boolean status, int size, int ageLimit, int[] d)
		{
			this.serverId = serverId;
			this.ip = ip;
			this.port = port;
			this.pvp = pvp;
			this.brackets = brackets;
			this.type = type;
			this.online = online;
			this.maxPlayers = maxPlayers;
			this.status = status;
			this.playerSize = size;
			this.ageLimit = ageLimit;
			this.deleteChars = d;
		}
	}
	
	public ServerList(Account account)
	{
		_lastServer = account.getLastServer();
		_paddedBytes = 1;
		
		for (GameServer gs : GameServerManager.getInstance().getGameServers())
		{
			Boolean added = false;
			InetAddress ip;
			for (HostInfo host : gs.getHosts())
			{
				try
				{
					String ipStr = null;
					if (NetUtils.isInternalIP(account.getLastIP()))
					{
						ipStr = host.getInnerIP();
					}
					if (ipStr == null)
					{
						ipStr = host.getIP();
					}
					if (ipStr == null)
					{
						continue;
					}
					if (ipStr.equals("*"))
					{
						ipStr = gs.getConnection().getIpAddress();
					}
					
					ip = InetAddress.getByName(ipStr);
				}
				catch (UnknownHostException e)
				{
					continue;
				}
				
				Pair<Integer, int[]> entry = account.getAccountInfo(host.getId());
				
				_paddedBytes += (3 + (4 * (entry == null ? 0 : entry.getValue().length)));
				
				// _servers.add(new ServerData(host.getId(), ip, host.getPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), entry == null ? 0 : entry.getKey(), gs.getAgeLimit(), entry == null ? ArrayUtils.EMPTY_INT_ARRAY :
				// entry.getValue()));
				
				if (!added)
				{
					if (!Config.HideMainServer)
					{
						_servers.add(new ServerData(host.getId(), ip, host.getPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), entry == null ? 0 : entry.getKey(), gs.getAgeLimit(), entry == null ? ArrayUtils.EMPTY_INT_ARRAY : entry.getValue()));
					}
					if (Config.EnableProxieIps)
					{
						Config.PROXIES.forEach((key, value) ->
						{
							try
							{
								_tempServers.add(new ServerData(host.getId(), InetAddress.getByName(value.split(":")[0]), Integer.parseInt(value.split(":")[1]), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), entry == null ? 0 : entry.getKey(), gs.getAgeLimit(), entry == null ? ArrayUtils.EMPTY_INT_ARRAY : entry.getValue()));
							}
							catch (UnknownHostException uhe)
							{
								uhe.printStackTrace();
							}
						});
						
						Collections.shuffle(_tempServers);
						_servers.add(_tempServers.get(rnd.nextInt(_tempServers.size())));
						
						Config.LATINPROXIES.forEach((key, value) ->
						{
							try
							{
								_tempServersLatin.add(new ServerData(host.getId(), InetAddress.getByName(value.split(":")[0]), Integer.parseInt(value.split(":")[1]), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), entry == null ? 0 : entry.getKey(), gs.getAgeLimit(), entry == null ? ArrayUtils.EMPTY_INT_ARRAY : entry.getValue()));
							}
							catch (UnknownHostException uhe)
							{
								uhe.printStackTrace();
							}
						});
						
						Collections.shuffle(_tempServersLatin);
						_servers.add(_tempServersLatin.get(rnd.nextInt(_tempServersLatin.size())));
						
					}
				}
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		for (ServerData server : _servers)
		{
			writeC(server.serverId);
			InetAddress i4 = server.ip;
			byte[] raw = i4.getAddress();
			writeC(raw[0] & 0xff);
			writeC(raw[1] & 0xff);
			writeC(raw[2] & 0xff);
			writeC(raw[3] & 0xff);
			writeD(server.port);
			writeC(server.ageLimit); // age limit
			writeC(server.pvp ? 0x01 : 0x00);
			writeH(server.online);
			writeH(server.maxPlayers);
			writeC(server.status ? 0x01 : 0x00);
			writeD(server.type);
			writeC(server.brackets ? 0x01 : 0x00);
		}
		
		// writeH(0x00); // -??
		writeH(_paddedBytes);
		writeC(_servers.size());
		for (ServerData server : _servers)
		{
			writeC(server.serverId);
			writeC(server.playerSize); // acc player size
			writeC(server.deleteChars.length);
			for (int t : server.deleteChars)
			{
				writeD((int) (t - (System.currentTimeMillis() / 1000L)));
			}
		}
	}
}