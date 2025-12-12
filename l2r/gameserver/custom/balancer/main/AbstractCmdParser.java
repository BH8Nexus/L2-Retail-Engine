package l2r.gameserver.custom.balancer.main;

import l2r.gameserver.custom.balancer.ClassBalanceGui;
import l2r.gameserver.custom.balancer.SkillBalanceGui;
import l2r.gameserver.model.Player;

public class AbstractCmdParser
{
	
	public static AbstractCmdParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AbstractCmdParser _instance = new AbstractCmdParser();
	}
	
	public void handleCommands(Player activeChar, String command)
	{
		if (activeChar.isGM() && command.contains("balance"))
		{
			if (command.contains("skillbalance"))
			{
				SkillBalanceGui.getInstance().onBypassCommand(activeChar, command);
			}
			else if (command.contains("classbalance") || command.equals("_bbs_balancer"))
			{
				ClassBalanceGui.getInstance().onBypassCommand(activeChar, command);
			}
			return;
		}
	}
}
