package l2r.gameserver.taskmanager.actionrunner.tasks;

import l2r.gameserver.Config;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.GameClient.GameClientState;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.RestartResponse;

public class DualboxOutsidePeaceTask extends AutomaticTask
{
	public static String VAR_NAME = "LastTimeDualboxedOutsidePeace";
	public static int DUALBOX_PENALTY_TIME = 300_000; // 5 min.
	
	public DualboxOutsidePeaceTask()
	{
		super();
	}
	
	@Override
	public void doTask()
	{
		if ((Config.DUALBOX_NUMBER_IP_OUTSIDE_PEACE > 0) || (Config.HWID_DUALBOX_NUMBER_OUTSIDE_PEACE > 0))
		{
			for (Player plr : GameObjectsStorage.getAllPlayers())
			{
				if (((plr != null) && plr.isInOfflineMode()) || plr.isInStoreMode())
				{
					if (((Config.DUALBOX_NUMBER_IP_OUTSIDE_PEACE > 0) && plr.isDualbox(Config.DUALBOX_NUMBER_IP_OUTSIDE_PEACE, true, GameObjectsStorage.getAllPlayersForIterate())) || ((Config.HWID_DUALBOX_NUMBER_OUTSIDE_PEACE > 0) && plr.isDualbox(Config.HWID_DUALBOX_NUMBER_OUTSIDE_PEACE, false, GameObjectsStorage.getAllPlayersForIterate())))
					{
						// Check if player is detected to dualbox outside peace in the last 5 min.
						if ((plr.getVarLong(VAR_NAME, 0) + DUALBOX_PENALTY_TIME) > System.currentTimeMillis())
						{
							plr.setVar(VAR_NAME, System.currentTimeMillis());
							plr.sendMessage("You have too many characters outside peace zone. If you do not get back to a peace zone, you will be kicked.");
							plr.sendPacket(new ExShowScreenMessage("You have too many characters outside peace zone. If you do not get back to a peace zone, you will be kicked.", 50000));
						}
						else
						{
							plr.setVar(VAR_NAME, System.currentTimeMillis());
							GameClient client = plr.getClient();
							if (client != null)
							{
								client.setState(GameClientState.AUTHED);
							}
							plr.restart();
							// send char list
							CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
							client.sendPacket(RestartResponse.OK, cl);
							client.setCharSelection(cl.getCharInfo());
							
							NpcHtmlMessage html = new NpcHtmlMessage(0);
							html.setHtml("<html><body><title>Dualbox detected!</title><br>You have reached the maximum amount of characters you can have currently in-game outside a peace zone. You will be allowed to use this character after a short period of time.</body></html>");
							client.sendPacket(html);
						}
					}
				}
			}
		}
	}
	
	public static boolean checkPenalty(Player player)
	{
		if ((player.getVarLong(VAR_NAME, 0) + DUALBOX_PENALTY_TIME) > System.currentTimeMillis())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml("<html><body><title>Dualbox detected!</title><br>You have reached the maximum amount of characters you can have currently in-game outside a peace zone. You will be allowed to use this character after a short period of time.</body></html>");
			player.sendPacket(html);
			return true;
		}
		
		return false;
	}
	
	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 60000L; // Check every minute
	}
}
