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
package l2r.gameserver.instancemanager.games;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;

/**
 * @author DEV][ATLAS
 */
public class PvpHolder
{
	private final int player1;
	private final int player2;
	private long timer = 0;
	private int kills = 0;
	private boolean removePvPKills = false;
	
	public PvpHolder(int killer, int victim)
	{
		player1 = killer;
		player2 = victim;
	}
	
	public long getTime()
	{
		return timer;
	}
	
	public String getId()
	{
		return "id:" + player1 + ":" + player2;
	}
	
	public Player getKiller()
	{
		return World.getPlayer(player1);
	}
	
	public Player getVictim()
	{
		return World.getPlayer(player2);
	}
	
	public void setTime(long time)
	{
		timer = time;
	}
	
	public int getTeamKills()
	{
		return kills;
	}
	
	public void setTeamKills(int kill)
	{
		kills = kill;
	}
	
	/**
	 * @return the removePvPKills
	 */
	public boolean isRemovePvPKills()
	{
		return removePvPKills;
	}
	
	/**
	 * @param removePvPKills the removePvPKills to set
	 */
	public void setRemovePvPKills(boolean removePvPKills)
	{
		this.removePvPKills = removePvPKills;
	}
	
}
