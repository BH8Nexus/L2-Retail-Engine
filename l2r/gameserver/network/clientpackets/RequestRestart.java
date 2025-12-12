package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.custom.StriderRace;
import l2r.gameserver.custom.StriderRaceState;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.network.GameClient.GameClientState;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2r.gameserver.network.serverpackets.RestartResponse;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class RequestRestart extends L2GameClientPacket
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
		
		if (activeChar.isInObserverMode())
		{
			activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if (!activeChar.isGM() && activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if (activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2, RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if (activeChar.isInJail())
		{
			activeChar.standUp();
			activeChar.unblock();
		}
		
		else if (activeChar.isBlocked()) // Π Π°Π·Ρ€ΠµΡ�Π°ΠµΠΌ Π²Ρ‹Ρ…ΠΎΠ΄ΠΈΡ‚Ρ� ΠΈΠ· ΠΈΠ³Ρ€Ρ‹ ΠµΡ�Π»ΠΈ ΠΈΡ�ΠΏΠΎΠ»Ρ�Π·ΡƒΠµΡ‚Ρ�Ρ� Ρ�ΠµΡ€Π²ΠΈΡ� HireWyvern. Π’ΠµΡ€Π½ΠµΡ‚ Π² Π½Π°Ρ‡Π°Π»Ρ�Π½ΡƒΡ� Ρ‚ΠΎΡ‡ΠΊΡƒ.
		{
			if (!activeChar.isFlying())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestRestart.OutOfControl", activeChar));
				activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
				return;
			}
		}
		
		if (activeChar.getVar("isPvPevents") != null)
		{
			// Original Message: You can't do that, while participating in Event.
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestRestart.CannotUse", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestRestart.Olympiad", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if (activeChar.isInStoreMode() && !activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestRestart.OfflineNoTradeZoneOnlyOffshore", activeChar));
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if (StriderRace.getInstance().getStriderRaceState() == StriderRaceState.ATIVED)
		{
			if (StriderRace.getInstance().containsPlayer(activeChar))
			{
				activeChar.sendMessage("[Strider Race]: You can't restart in strider race!");
				/// sendPacket(RestartResponse.valueOf(0));
				sendPacket(RestartResponse.FAIL);
				return;
			}
		}
		
		if (activeChar.isInFightClub()) // fightclub
		{
			activeChar.sendMessage("You need to leave Fight Club first!");
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (activeChar.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestRestart.Festival", activeChar));
				activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
				return;
			}
		}
		
		if (!activeChar.getPermissions().canLogOut(true, true))
		{
			activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
			return;
		}
		
		if (getClient() != null)
		{
			getClient().setState(GameClientState.AUTHED);
		}
		activeChar.restart();
		// send char list
		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(RestartResponse.OK, cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}