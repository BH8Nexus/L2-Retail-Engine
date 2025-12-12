package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;

public class Appearing extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(System.currentTimeMillis() - activeChar.getLastAppearingPacket() < Config.APPEARINGPACKETDELAY)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setLastAppearingPacket();

		if(activeChar.isLogoutStarted())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getObserverMode() == Player.OBSERVER_STARTING)
		{
			activeChar.appearObserverMode();
			return;
		}

		if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
		{
			activeChar.returnFromObserverMode();
			return;
		}

		if(!activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.onTeleported();
	}
}