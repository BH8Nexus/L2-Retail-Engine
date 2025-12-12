/*
 * Copyright (C) 2004-2020 L2J Server
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
package l2r.gameserver.taskmanager.tasks.GameObjectTask;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;

/**
 * @author Flash
 *
 */
/** UnJailTask */
public class UnJailTask extends RunnableImpl
{
	private final HardReference<Player> _playerRef;
	private final boolean _msg;
	
	public UnJailTask(Player player, boolean msg)
	{
		_playerRef = player.getRef();
		_msg = msg;
	}
	
	@Override
	public void runImpl()
	{
		Player player = _playerRef.get();
		
		if (player == null)
		{
			return;
		}
		
		// String[] re = player.getVar("jailedFrom").split(";");
		player.unsetVar("jailedFrom");
		player.unsetVar("jailed");
		
		if (player.isBlocked())
		{
			player.unblock();
		}
		
		// lets port them to floran on unjail.
		player.teleToLocation(17836, 170178, -3507, 0);
		
		// player.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
		// player.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
		
		if (_msg)
		{
			player.sendPacket(new Say2(0, ChatType.TELL, "Server", "Your time in jail is over. Try to not come back here :)"));
		}
		
		player.standUp();
	}
}