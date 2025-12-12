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
package l2r.gameserver.model.instances;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.custom.StriderRace;
import l2r.gameserver.custom.StriderRaceState;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.templates.npc.NpcTemplate;

public class L2StriderRaceEventInstance extends NpcInstance
{
	public L2StriderRaceEventInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private static int rankPlayer = 0;
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if (player.getTarget() != this)
		{
			player.setTarget(this);
		}
		else
		{
			if (!isInRange(player, INTERACTION_DISTANCE))
			{
				if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				}
				
				return;
			}
			if ((StriderRace.getInstance().getStriderRaceState() == StriderRaceState.ATIVED) && StriderRace.getInstance().containsPlayer(player))
			{
				if (rankPlayer < 2)
				{
					rankPlayer++;
					Announcements.getInstance().announceToAll("[Strider Race]: [" + rankPlayer + "] to arrive was -> " + player.getName());
					rewardPlayer(player);
					StriderRace.getInstance().removePlayer(player);
					// player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					player.teleToClosestTown();
					// player.dismount();
					// player.mountPlayer(getPet());
					player.setMount(0, 0, 0);
					return;
				}
				rankPlayer += 1;
				Announcements.getInstance().announceToAll("[Strider Race]: [3] to arrive was -> " + player.getName());
				rewardPlayer(player);
				StriderRace.getInstance().finishEvent();
				rankPlayer = 0;
				
				GameObject obj = player.getTarget();
				if ((obj != null) && obj.isNpc())
				{
					NpcInstance target = (NpcInstance) obj;
					target.deleteMe();
					
					Spawner spawn = target.getSpawn();
					if (spawn != null)
					{
						spawn.stopRespawn();
						SpawnTable.getInstance().deleteSpawn(getLoc(), VISIBLE);
					}
				}
			}
		}
	}
	
	private static void rewardPlayer(Player player)
	{
		switch (rankPlayer)
		{
			case 1:
				for (int[] item : Config.EVENT_SR_REWARD_TOP1)
				{
					player.getInventory().addItem(item[0], item[1], "");
				}
				break;
			case 2:
				for (int[] item : Config.EVENT_SR_REWARD_TOP2)
				{
					player.getInventory().addItem(item[0], item[1], "");
				}
				break;
			default:
				for (int[] item : Config.EVENT_SR_REWARD_TOP3)
				{
					player.getInventory().addItem(item[0], item[1], "");
				}
		}
	}
}