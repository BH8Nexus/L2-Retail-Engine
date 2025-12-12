package l2r.loginserver.gameservercon.gspackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.loginserver.gameservercon.ReceivablePacket;
import l2r.loginserver.gameservercon.lspackets.AccountDataResponse;

public class AccountDataRequested extends ReceivablePacket
{
	public static final Logger _log = LoggerFactory.getLogger(AccountDataRequested.class);
	
	private String _account;
	
	@Override
	protected void readImpl()
	{
		_account = readS();
	}
	
	@Override
	protected void runImpl()
	{
		sendPacket(new AccountDataResponse(_account));
	}
}