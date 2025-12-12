/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.custom.StriderRace;
import l2r.gameserver.custom.StriderRaceState;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;

/**
 * @author Bluur
 */
public class RegStriderRace implements IVoicedCommandHandler
{
	private static final String[] voiced_commands =
	{
		"joinsr",
		"leavesr"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equals("joinsr"))
		{
			if (register(activeChar))
			{
				StriderRace.getInstance().registerPlayer(activeChar);
				activeChar.sendMessage("[Strider Race]: You have been successfully registered!");
			}
		}
		else if (command.equals("leavesr"))
		{
			remove(activeChar);
		}
		return true;
	}
	
	private static boolean register(Player p)
	{
		if (!StriderRace.getInstance().maxPlayers())
		{
			p.sendMessage("[Strider Race]: Limit of players was reached.");
			return false;
		}
		else if ((StriderRace.getInstance().getStriderRaceState() != StriderRaceState.REGISTER) || (p.getKarma() > 0) || p.isInCombat() || p.isInOlympiadMode() || p.isInObserverMode() || StriderRace.getInstance().containsPlayer(p))
		{
			p.sendMessage("[Strider Race]: conditions for registration are inappropriate !!!");
			return false;
		}
		
		return true;
	}
	
	private static boolean remove(Player p)
	{
		if ((StriderRace.getInstance().getStriderRaceState() == StriderRaceState.REGISTER) && StriderRace.getInstance().containsPlayer(p))
		{
			StriderRace.getInstance().removePlayer(p);
			p.sendMessage("[Strider Race]: you have been successfully removed!");
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return voiced_commands;
	}
}