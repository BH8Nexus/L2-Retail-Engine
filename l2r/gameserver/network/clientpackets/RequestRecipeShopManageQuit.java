package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

public class RequestRecipeShopManageQuit extends L2GameClientPacket
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


		if(System.currentTimeMillis() - activeChar.getLastRequestRecipeShopManageQuitPacket() < Config.REQUEST_RECIPESHOPMANAGE_QUITPACKETDEALAY)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setLastRequestRecipeShopManageQuitPacket();

//		if(activeChar.getDuel() != null)
//		{
//			activeChar.sendActionFailed();
//			return;
//		}

		activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		activeChar.standUp();
		activeChar.broadcastCharInfo();
	}
}