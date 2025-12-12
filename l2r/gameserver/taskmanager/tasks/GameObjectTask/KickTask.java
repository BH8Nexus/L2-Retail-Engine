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

/**
 * @author Flash
 *
 */
/** KickTask */
public class KickTask extends RunnableImpl
{
	private final HardReference<Player> _playerRef;
	
	public KickTask(Player player)
	{
		_playerRef = player.getRef();
	}
	
	@Override
	public void runImpl()
	{
		Player player = _playerRef.get();
		if (player == null)
		{
			return;
		}
		player.setOfflineMode(false);
		player.kick();
	}
}
