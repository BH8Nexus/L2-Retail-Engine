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

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.Functions;

/**
 * Un voiced para poder setear buff stores
 * @author Prims
 */
public class Offlinebuff extends Functions implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"buffstore"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		try
		{
			// Check if the player can set a store
			if (!Config.BUFF_STORE_ALLOWED_CLASS_LIST.contains(activeChar.getClassId().getId()))
			{
				activeChar.sendMessage("Your profession is not allowed to set an Buff Store");
				return false;
			}
			
			if ((activeChar.getOlympiadObserveGame() != null) || (activeChar.getOlympiadGame() != null) || Olympiad.isRegisteredInComp(activeChar) || (activeChar.getKarma() > 0) || activeChar.isInJail())
			{
				activeChar.sendMessage("You cannot do it right now!");
				activeChar.sendActionFailed();
				return false;
			}
			
//			
//			if (!activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
//			{
//				activeChar.sendMessage("You cannot set offline store in this area!");
//				return false;
//			}
//			
//			if (activeChar.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE))
//			{
//				activeChar.sendMessage("You cannot set offline store in this area!");
//				return false;
//			}
			
			// Shows the initial buff store window
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("command/buffstore/buff_store.htm");
			if (activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_BUFF)
			{
				html.replace("%link%", "Stop Store");
				html.replace("%bypass%", "bypass -h BuffStore stopstore");
			}
			else
			{
				html.replace("%link%", "Create Store");
				html.replace("%bypass%", "bypass -h player_help command/buffstore/buff_store_create.htm");
			}
			activeChar.sendPacket(html);
			
			return true;
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Use: .buffstore");
		}
		
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
