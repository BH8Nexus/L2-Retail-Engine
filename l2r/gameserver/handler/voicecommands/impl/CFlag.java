package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.RadarControl;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;


/**
 * @Author: Abaddon
 */
public class CFlag extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList =
	{
		"cflag"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if ((!Config.ALLOW_CFLAG))
		{
			// Original Message: Feature is disabled!
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.cflag.FeatureDisabled", activeChar));
			return false;
		}
		
		if (command.equals("cflag"))
		{
			if(activeChar.getClan() == null)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.cflag.NotInClan", activeChar));
				return false;
			}
			
			for(Player p : activeChar.getClan().getOnlineMembers(activeChar.getObjectId()))
			{
				if(p != activeChar)
				{
					RadarControl rc = new RadarControl(0, 2, p.getLoc());	// showRadar?? 0 = showRadar; 1 = delete radar; // 1 - только стрелка над головой, 2 - флажок на карте
					activeChar.sendPacket(rc);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}