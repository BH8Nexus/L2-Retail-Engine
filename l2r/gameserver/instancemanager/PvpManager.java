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
package l2r.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Map;

import l2r.commons.util.Rnd;
import l2r.gameserver.instancemanager.games.PvpHolder;

/**
 * @author DEV][ATLAS
 */
public class PvpManager
{
	Map<String, PvpHolder> _pvplist;
	
	public static final PvpManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected PvpManager()
	{
		_pvplist = new HashMap<>();
	}
	
	public void addToList(PvpHolder team, long newtimer, boolean increaseTeamKill)
	{
		if (_pvplist.containsKey(team.getId()))
		{
			_pvplist.remove(team);
		}
		team.setTime(newtimer);
		team.setTeamKills(increaseTeamKill ? (team.getTeamKills() + 1) : 0);
		
		team.getKiller().sendMessage("You can kill " + team.getVictim().getName() + " in " + ((newtimer - System.currentTimeMillis()) / 1000) + " seconds");
		
		_pvplist.put(team.getId(), team);
	}
	
	public PvpHolder getTeam(String team)
	{
		return _pvplist.get(team);
	}
	
	public int evaluateKill(PvpHolder team)
	{
		int addKill = 1;
		int randomTime = Rnd.get(30, 50);// special checks for random time!!
		
		PvpHolder noobs = getTeam(team.getId());
		if (noobs != null)
		{
			if ((noobs.getTeamKills() > 5) || (noobs.isRemovePvPKills() && ((System.currentTimeMillis() - noobs.getTime()) < 0)))
			{
				if (noobs.getKiller().getPvpKills() >= noobs.getTeamKills())
				{
					addKill = -noobs.getTeamKills();
					noobs.setTeamKills(0);
				}
				else if (noobs.getKiller().getPvpKills() > 0)
				{
					addKill = -1;
				}
				else
				{
					addKill = 0;
				}
				noobs.setRemovePvPKills(true);
			}
			else
			{
				addKill = 1;
			}
			
			addToList(noobs, System.currentTimeMillis() + (randomTime * 1000), ((System.currentTimeMillis() - noobs.getTime()) < 0)); // got you. time didn't passed!!
		}
		else
		{
			addKill = 1;
			addToList(team, System.currentTimeMillis() + randomTime, false);
		}
		
		return addKill;
	}
	
	private static class SingletonHolder
	{
		protected static final PvpManager _instance = new PvpManager();
	}
}
