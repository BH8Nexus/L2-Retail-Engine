package l2r.loginserver.gameservercon.lspackets;

import l2r.loginserver.gameservercon.SendablePacket;

public class LoginServerFail extends SendablePacket
{
	private final String _reason;
	private final boolean _restartConnection;
	
	public LoginServerFail(String reason, boolean restartConnection)
	{
		_reason = reason;
		_restartConnection = restartConnection;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x01);
		writeC(0x00); // Reason ID
		writeS(_reason);
		writeC(_restartConnection ? 0x01 : 0x00);
	}
}