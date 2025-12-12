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
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

/**
 * @author Flash
 *
 */
/** HourlyTask */
public class HourlyTask extends RunnableImpl
{
	private final HardReference<Player> _playerRef;
	
	public HourlyTask(Player player)
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
		// Каждый час в игре оповещаем персонажу сколько часов он играет.
		int hoursInGame = player.getHoursInGame();
		player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_S1).addInteger(hoursInGame));
		player.sendPacket(new SystemMessage2(SystemMsg.YOU_OBTAINED_S1_RECOMMENDS).addInteger(player.addRecomLeft()));
	}
}
