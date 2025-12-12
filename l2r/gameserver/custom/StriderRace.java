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
package l2r.gameserver.custom;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.utils.Location;

/**
 * @author Bluur
 * @version 1.1
 */
public class StriderRace
{
	private static StriderRace INSTANCE;
	private StriderRaceState srState = StriderRaceState.DESACTIVED;
	private final static List<Player> players = new ArrayList<>();
	
	private StriderRace()
	{
	}
	
	public void startEvent()
	{
		srState = StriderRaceState.REGISTER;
		announceEvent("The event started! Commands .joinsr or .leavesr");
		announceEvent("Registration time: 5 minutes!");
		sleep(60);
		announceEvent("[4] minutes to the end of the record!");
		sleep(60);
		announceEvent("[3] minutes to the end of the record!");
		sleep(60);
		announceEvent("[2] minutes to the end of the record!");
		sleep(60);
		announceEvent("[1] minutes to the end of the record!");
		sleep(60);
		if (!minPlayers())
		{
			abortEvent();
			return;
		}
		srState = StriderRaceState.WAIT;
		announceEvent("closed registration! Total number of registered players: " + players.size());
		announceEvent("The players will be teleported in 10 seconds!");
		sleep(10);
		teleportPlayers();
		mountPlayer(true);
		paralizedPlayer(true);
		announceEvent("The event will start in 15 seconds!");
		sleep(15);
		srState = StriderRaceState.ATIVED;
		announceEvent("The race is on! Go Go Go!");
		spawnNpc(Config.EVENT_SR_LOC_ID_NPC);
		paralizedPlayer(false);
		sleep(300); // duracao do evento em segundos
		if (srState == StriderRaceState.ATIVED)
		{
			finishEvent();
		}
	}
	
	private void paralizedPlayer(boolean value)
	{
		if (value)
		{
			for (Player player : players)
			{
				// player.isParalyzed(true);
				player.startParalyzed();
				player.startAbnormalEffect(AbnormalEffect.HOLD_2);
			}
		}
		else
		{
			for (Player player : players)
			{
				// player.setIsParalyzed(false);
				player.stopParalyzed();
				player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			}
		}
	}
	
	private void mountPlayer(boolean value)
	{
		if (value)
		{
			for (Player player : players)
			{
				if (player != null)
				{
					player.getRadar().addMarker(Config.EVENT_SR_LOC_ARRIVAL_X, Config.EVENT_SR_LOC_ARRIVAL_Y, Config.EVENT_SR_LOC_ARRIVAL_Z);
					player.setMount(Config.EVENT_NPC_PET, 0, 87);
				}
			}
		}
		else
		{
			for (Player player : players)
			{
				if (player != null)
				{
					player.getRadar().removeMarker(Config.EVENT_SR_LOC_ARRIVAL_X, Config.EVENT_SR_LOC_ARRIVAL_Y, Config.EVENT_SR_LOC_ARRIVAL_Z);
					// player.dismount();
					player.setMount(0, 0, 0);
					// player.mountPlayer(null);
				}
			}
		}
	}
	
	private void abortEvent()
	{
		srState = StriderRaceState.DESACTIVED;
		announceEvent("The event was terminated for lack of participants!");
		players.clear();
	}
	
	public void finishEvent()
	{
		srState = StriderRaceState.DESACTIVED;
		announceEvent("The event duration time is up! Thank all...");
		teleportPlayersToTown();
		mountPlayer(false);
		players.clear();
	}
	
	private static void announceEvent(String sendMessage)
	{
		// Broadcast.announceToOnlinePlayers("[Strider Race]: " + sendMessage, true);
		Announcements.getInstance().announceToAll("[Strider Race]: " + sendMessage);
	}
	
	private static void teleportPlayers()
	{
		
		for (Player player : players)
		{
			player.teleToLocation(Config.EVENT_SR_LOC_PLAYER_X, Config.EVENT_SR_LOC_PLAYER_Y, Config.EVENT_SR_LOC_PLAYER_Z, 0);
		}
	}
	
	private void teleportPlayersToTown()
	{
		for (Player player : players)
		{
			player.teleToClosestTown();
		}
	}
	
	private boolean minPlayers()
	{
		if (players.size() < Config.EVENT_SR_MINIMUM_PLAYERS)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean maxPlayers()
	{
		if (players.size() >= Config.EVENT_SR_MAXIMUM_PLAYERS)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean containsPlayer(Player player)
	{
		return players.contains(player);
	}
	
	public void registerPlayer(Player player)
	{
		players.add(player);
	}
	
	public void removePlayer(Player player)
	{
		players.remove(player);
	}
	
	public static Location getSpawnNpc()
	{
		return new Location(Config.EVENT_SR_LOC_ARRIVAL_X, Config.EVENT_SR_LOC_ARRIVAL_Y, Config.EVENT_SR_LOC_ARRIVAL_Z);
	}
	
	protected NpcInstance spawnNpc(int id)
	{
		final SimpleSpawner spawn = new SimpleSpawner(id);
		spawn.setLoc(getSpawnNpc());
		spawn.setAmount(1);
		spawn.setHeading(0);
		// spawn.setRespawnDelay(Math.max(0, respawnInSeconds));
		spawn.setRespawnDelay(0);
		// spawn.setReflection(getReflection());
		spawn.setReflection(ReflectionManager.DEFAULT);
		final List<NpcInstance> npcs = spawn.initAndReturn();
		
		spawn.stopRespawn();
		
		return npcs.get(0);
	}
	
	/*
	 * private static void spawnNpc() { NpcTemplate tp = NpcHolder.getInstance().getTemplate(Config.EVENT_SR_LOC_ID_NPC); try { // L2Spawn spawn = null; // spawn = new L2Spawn(tp); SimpleSpawner spawn = new SimpleSpawner(tp); spawn.setLocx(Config.EVENT_SR_LOC_ARRIVAL_X);
	 * spawn.setLocy(Config.EVENT_SR_LOC_ARRIVAL_Y); spawn.setLocz(Config.EVENT_SR_LOC_ARRIVAL_Z); spawn.setHeading(0); SpawnTable.getInstance().addNewSpawn(spawn); spawn.stopRespawn(); spawn.init(); } catch (Exception e) { e.printStackTrace(); } }
	 */
	
	public StriderRaceState getStriderRaceState()
	{
		return srState;
	}
	
	private static void sleep(int value)
	{
		try
		{
			Thread.sleep(1000 * value);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static StriderRace getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new StriderRace();
		}
		
		return INSTANCE;
	}
}