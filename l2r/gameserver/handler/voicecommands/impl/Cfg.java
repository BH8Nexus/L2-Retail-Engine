package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.CharacterControlPanel;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.Functions;

public class Cfg extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = new String[]
	{
		"control",
		"cfg"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		String nextPage = CharacterControlPanel.getInstance().useCommand(activeChar, args, "-h user_control ");
		
		if ((nextPage == null) || nextPage.isEmpty())
		{
			return true;
		}
		
		String html = HtmCache.getInstance().getNotNull("command/" + nextPage, activeChar);
		
		String additionalText = args.split(" ").length > 1 ? args.split(" ")[1] : "";
		html = CharacterControlPanel.getInstance().replacePage(html, activeChar, additionalText, "-h user_control ");
		
		// show(html, activeChar);
		ShowBoard.separateAndSend(html, activeChar);
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}