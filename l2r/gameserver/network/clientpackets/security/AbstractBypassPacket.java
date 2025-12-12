package l2r.gameserver.network.clientpackets.security;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.clientpackets.L2GameClientPacket;
import l2r.gameserver.utils.Log;

public abstract class AbstractBypassPacket extends L2GameClientPacket
{
	
	public static boolean checkReuseTime(Player player, String bypass)
	{
		try
		{
			if ((player.getLastBypassTime() + Config.PACKET_FLOOD_PROTECTION_IN_MS) > System.currentTimeMillis())
			{
				if ((player.getLastBypassAbuseTime() + Config.PACKET_FLOOD_PROTECTION_IN_MS) > System.currentTimeMillis())
				{
					player.kick();
					if (bypass != null)
					{
						Log.logIllegalActivity("Player " + player.getName() + " tried to spam bypass " + bypass + ".");
					}
					else
					{
						Log.logIllegalActivity("Player " + player.getName() + " is trying to flood clientpackets.");
					}
				}
				else
				{
					player.setLastBypassAbuseTime(System.currentTimeMillis());
				}
				return false;
			}
			return true;
		}
		finally
		{
			player.setLastBypassTime(System.currentTimeMillis());
		}
	}
}
