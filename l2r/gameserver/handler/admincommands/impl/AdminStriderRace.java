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
package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.custom.StriderRace;
import l2r.gameserver.custom.StriderRaceState;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;

public class AdminStriderRace implements IAdminCommandHandler
{
	// private static final String[] ADMIN_COMMANDS = {"admin_startsr"};
	private static enum Commands
	{
		admin_startsr
	}
	
	@Override
	// public boolean useAdminCommand(String command, Player activeChar)
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		if (StriderRace.getInstance().getStriderRaceState() == StriderRaceState.DESACTIVED)
		{
			initEvent();
		}
		else
		{
			activeChar.sendMessage("[Strider Race]: The event this already in progress!");
		}
		
		return true;
	}
	
	private static void initEvent()
	{
		ThreadPoolManager.getInstance().schedule(() -> StriderRace.getInstance().startEvent(), 1);
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}