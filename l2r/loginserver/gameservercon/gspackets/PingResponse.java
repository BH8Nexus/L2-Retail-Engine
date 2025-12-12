package l2r.loginserver.gameservercon.gspackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.ReceivablePacket;

public class PingResponse extends ReceivablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(PingResponse.class);
	
	private long _serverTime;
	
	@Override
	protected void readImpl()
	{
		_serverTime = readQ();
	}
	
	@Override
	protected void runImpl()
	{
		GameServer gameServer = getGameServer();
		if (!gameServer.isAuthed())
		{
			return;
		}
		
		gameServer.getConnection().onPingResponse();
		
		long diff = _serverTime - System.currentTimeMillis();
		
		if (Math.abs(diff) > 2999)
		{
			// _log.warn("Gameserver " + gameServer.getId() + " [" + gameServer.getName() + "] : time offset " + diff + " ms.");
			_log.warn("Gameserver IP[" + gameServer.getConnection().getIpAddress() + "]: time offset " + diff + " ms.");
		}
	}
}