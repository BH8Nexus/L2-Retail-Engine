package l2r.loginserver.gameservercon.lspackets;

import l2r.loginserver.gameservercon.SendablePacket;

public class KickPlayer extends SendablePacket
{
	private final String account;
	
	public KickPlayer(String login)
	{
		account = login;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x03);
		writeS(account);
	}
}