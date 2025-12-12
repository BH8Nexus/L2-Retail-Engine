package l2r.gameserver.network.loginservercon.gspackets;

import l2r.gameserver.network.loginservercon.SendablePacket;

public class ChangePassword extends SendablePacket
{
	private final String account;
	private final String oldPass;
	private final String nel2rass;
	private final String hwid;
	
	public ChangePassword(String account, String oldPass, String nel2rass, String hwid)
	{
		this.account = account;
		this.oldPass = oldPass;
		this.nel2rass = nel2rass;
		this.hwid = hwid;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x08);
		writeS(account);
		writeS(oldPass);
		writeS(nel2rass);
		writeS(hwid);
	}
}
