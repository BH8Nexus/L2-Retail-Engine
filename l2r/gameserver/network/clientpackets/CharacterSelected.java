package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.GameClient.GameClientState;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.CharSelected;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.taskmanager.actionrunner.tasks.DualboxOutsidePeaceTask;
import l2r.gameserver.utils.AutoBan;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;
	
	/**
	 * Format: cdhddd
	 */
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		
		if (Config.SECOND_AUTH_ENABLED && !client.getSecondaryAuth().isAuthed())
		{
			client.getSecondaryAuth().openDialog();
			return;
		}
		
		if (client.getActiveChar() != null)
		{
			return;
		}
		
		Player activeChar = client.loadCharFromDisk(_charSlot);
		if (activeChar == null)
		{
			sendPacket(ActionFail.STATIC);
			return;
		}
		
		int objId = client.getObjectIdForSlot(_charSlot);
		if (AutoBan.checkIsBanned(objId))
		{
			/*
			 * if (Config.SHOW_BAN_INFO_IN_CHARACTER_SELECT) { String htmlban = HtmCache.getInstance().getNotNull("baninfo.htm", activeChar); String bannedby = AutoBan.getBannedBy(objId); if (bannedby.isEmpty()) { bannedby = "Missing Data"; } String reason = AutoBan.getBanReason(objId); if
			 * (reason.isEmpty()) { reason = "Missing Reason"; } String enddate = TimeUtils.convertDateToString(AutoBan.getEndBanDate(objId) * 1000); if (enddate.isEmpty()) { enddate = "Bad Date"; } NpcHtmlMessage html = new NpcHtmlMessage(0); html.setHtml(htmlban); html.replace("%bannedby%",
			 * bannedby); html.replace("%endDate%", enddate); html.replace("%reason%", reason); activeChar.sendPacket(html); }
			 */
			
			sendPacket(ActionFail.STATIC);
			return;
		}
		
		if (((Config.HWID_DUALBOX_NUMBER > 0) && activeChar.isDualbox(Config.HWID_DUALBOX_NUMBER, true, GameObjectsStorage.getAllPlayersForIterate())) || ((Config.DUALBOX_NUMBER_IP > 0) && activeChar.isDualbox(Config.DUALBOX_NUMBER_IP, false, GameObjectsStorage.getAllPlayersForIterate())))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml("<html><body><title>Dualbox detected!</title><br>You have reached the maximum amount of characters you can have currently in-game. You are currently unable to use this character.</body></html>");
			activeChar.sendPacket(html);
			return;
		}
		
		if (DualboxOutsidePeaceTask.checkPenalty(activeChar))
		{
			return;
		}
		
		if (activeChar.getAccessLevel().getLevel() < 0)
		{
			activeChar.setAccessLevel(0);
		}
		
		client.setState(GameClientState.IN_GAME);
		activeChar.setOnlineStatus(true);  // Plug for MA, TODO: Translate to another MA or MA translate to xml-rpc.
		
		sendPacket(new CharSelected(activeChar, client.getSessionKey().playOkID1));
	}
}