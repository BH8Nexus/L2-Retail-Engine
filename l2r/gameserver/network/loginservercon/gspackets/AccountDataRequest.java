package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.network.loginservercon.SendablePacket;

public class AccountDataRequest extends SendablePacket
{
	private final String _account;
	
	public AccountDataRequest(String account)
	{
		_account = account;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x12);
		writeS(_account);
	}
}