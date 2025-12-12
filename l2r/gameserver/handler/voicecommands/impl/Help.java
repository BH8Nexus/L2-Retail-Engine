package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;

import java.text.NumberFormat;

/**
 * @Author: Abaddon
 */
public class Help extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "exp" }; // + "help"

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		command = command.intern();
		if(command.equalsIgnoreCase("exp"))
			return exp(command, activeChar, args);

		return false;
	}

	private static NumberFormat df = NumberFormat.getNumberInstance();
	static
	{
		df.setMaximumFractionDigits(2);
	}

	private boolean exp(String command, Player activeChar, String args)
	{
		if(activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()))
			// Original Message: Maximum level!
			show(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.help.MaxLevel", activeChar), activeChar);
		else
		{
			long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
			double count = 0;
			
			String append_message = "" + new CustomMessage("l2r.gameserver.handler.voicecommands.impl.help.ExpLeft", activeChar) + " " + exp;
			if(count > 0)
				append_message += "<br>" + new CustomMessage("l2r.gameserver.handler.voicecommands.impl.help.MonstersLeft", activeChar) + " " + df.format(count);
			show(append_message, activeChar);
		}
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}