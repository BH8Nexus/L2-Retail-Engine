/*
 * Copyright (C) 2004-2016 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.ScriptFile;

/**
 * @author DevAtlas
 */
public class DonateVC implements ScriptFile, IVoicedCommandHandler
{

	@Override
	public String[] getVoicedCommandList()
	{
		return new String[] { "donate" };
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		CommunityBoardManager.getInstance().getCommunityHandler("_donate").onBypassCommand(activeChar, "_donate");
		return true;
	}

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{
	}

}
