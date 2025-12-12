package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(System.currentTimeMillis() - activeChar.getLastRequestPrivateStoreQuitBuyPacket() < Config.REQUESTPRIVATESTOREQUITBUYPACKETDELAY)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setLastRequestPrivateStoreQuitBuyPacket();

		if(!activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_BUY)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		activeChar.standUp();
		activeChar.broadcastCharInfo();
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}