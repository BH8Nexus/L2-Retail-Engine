package l2r.loginserver.clientpackets;

import l2r.loginserver.Config;
import l2r.loginserver.GameServerManager;
import l2r.loginserver.L2LoginClient;
import l2r.loginserver.SessionKey;
import l2r.loginserver.accounts.Account;
import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.GameServer.HostInfo;
import l2r.loginserver.serverpackets.LoginFail;
import l2r.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2r.loginserver.serverpackets.PlayOk;
import l2r.loginserver.serverpackets.ServerList;

/**
 * Format: ddc d: fist part of session id d: second part of session id c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int _loginOkID1;
	private int _loginOkID2;
	private int _unk;
	
	@Override
	protected void readImpl()
	{
		_loginOkID1 = readD();
		_loginOkID2 = readD();
		_unk = readC();
	}
	
	@Override
	protected void runImpl()
	{
		L2LoginClient client = getClient();
		SessionKey skey = client.getSessionKey();
		if ((skey == null) || !skey.checkLoginPair(_loginOkID1, _loginOkID2))
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		
		int serversCount = 0;
		int serverId = -1;
		for (GameServer gs : GameServerManager.getInstance().getGameServers())
		{
			for (HostInfo host : gs.getHosts())
			{
				if (gs.isOnline())
				{
					serverId = host.getId();
				}
				
				serversCount++;
			}
		}
		
		if (Config.DONT_SEND_SERVER_LIST_IF_ONE_SERVER && (serversCount == 1) && (serverId > 0))
		{
			Account account = client.getAccount();
			GameServer gs = GameServerManager.getInstance().getGameServerById(serverId);
			if ((gs == null) || !gs.isAuthed() || (gs.isGmOnly() && (account.getAccessLevel() < 100)) || ((gs.getOnline() >= gs.getMaxPlayers()) && (account.getAccessLevel() < 50)))
			{
				client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
				return;
			}
			
			account.setLastServer(serverId);
			account.update();
			
			client.close(new PlayOk(skey, serverId));
		}
		else
		{
			client.sendPacket(new ServerList(client.getAccount()));
		}
	}
}
/*
 * package l2r.loginserver.clientpackets; import l2r.loginserver.L2LoginClient; import l2r.loginserver.SessionKey; import l2r.loginserver.serverpackets.LoginFail.LoginFailReason; import l2r.loginserver.serverpackets.ServerList; import l2r.loginserver.serverpackets.ServerListFake; /** Format: ddc d:
 * fist part of session id d: second part of session id c: ?
 */
/*
 * public class RequestServerList extends L2LoginClientPacket { private int _loginOkID1; private int _loginOkID2; private boolean _loginFake = false; public RequestServerList(boolean login) { _loginFake = login; }
 * @Override protected void readImpl() { _loginOkID1 = readD(); _loginOkID2 = readD(); }
 * @Override protected void runImpl() { L2LoginClient client = getClient(); if (_loginFake) { client.sendPacket(new ServerListFake(client.getAccount())); return; } SessionKey skey = client.getSessionKey(); if ((skey == null) || !skey.checkLoginPair(_loginOkID1, _loginOkID2)) {
 * client.close(LoginFailReason.REASON_ACCESS_FAILED); return; } client.sendPacket(new ServerList(client.getAccount())); } }
 */