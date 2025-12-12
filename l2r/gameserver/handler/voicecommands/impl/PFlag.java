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
public class PFlag extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList =
	{
		"pflag"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if ((!Config.ALLOW_PFLAG))
		{
			// Original Message: Feature is disabled!
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.pflag.FeatureDisabled", activeChar));
			return false;
		}
		
		if (command.equals("pflag"))
		{
			if(!activeChar.isInParty())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.pflag.NotInParty", activeChar));
				return false;
			}
			
			for(Player p : activeChar.getParty())
			{
				if(p != activeChar)
				{
					RadarControl rc = new RadarControl(0, 2, p.getLoc()); // showRadar?? 0 = showRadar; 1 = delete radar; // 1 - только стрелка над головой, 2 - флажок на карте
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