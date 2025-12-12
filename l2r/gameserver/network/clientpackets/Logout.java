package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.custom.StriderRace;
import l2r.gameserver.custom.StriderRaceState;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		activeChar.setOnlineTime(0L);
		activeChar.setUptime(0L);
		
		// Dont allow leaving if player is fighting
		if (activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isBlocked() && !activeChar.isFlying())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Logout.OutOfControl", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Logout.message1", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Logout.Olympiad", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.getVar("isPvPevents") != null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Logout.message2", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isInStoreMode() && !activeChar.isInBuffStore() && !activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Logout.OfflineNoTradeZoneOnlyOffshore", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Logout.Observer", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		
		if (!activeChar.getPermissions().canLogOut(false, true))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isInFightClub())
		{
			activeChar.sendMessage("Leave Fight Club first!");
			activeChar.sendActionFailed();
			return;
		}
		
		// Prims - Support for offline buff stores
		if (activeChar.isInBuffStore())
		{
			activeChar.offlineBuffStore();
		}
		else
		{
			activeChar.kick();
		}
		
		if (StriderRace.getInstance().getStriderRaceState() == StriderRaceState.ATIVED)
		{
			if (StriderRace.getInstance().containsPlayer(activeChar))
			{
				activeChar.sendMessage("[Strider Race]: You can't logout in strider race!");
				activeChar.sendPacket(ActionFail.STATIC);
				return;
			}
		}
		
		//activeChar.logout(true);
	}
}