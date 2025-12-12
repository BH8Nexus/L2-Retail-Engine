package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;

/**
 * Opens the community board for the main page of the facebook system
 * @author Synerge
 */
public class FacebookVoice implements IVoicedCommandHandler
{
	private static final String[] COMMANDS =
	{
		"fb",
		"facebook"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		
		if(!Config.COMMAND_FACEBOOK_ENABLE)
		{
			return false;
		}
		
		final ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler("_bbsfacebook");
		if (handler != null)
		{
			handler.onBypassCommand(player, "_bbsfacebook_main");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}