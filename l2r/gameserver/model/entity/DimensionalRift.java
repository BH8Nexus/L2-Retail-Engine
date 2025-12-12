package l2r.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SimpleSpawner;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.Location;

public class DimensionalRift extends Reflection
{
	protected static final long seconds_5 = 5000L;
	protected static final int MILLISECONDS_IN_MINUTE = 60000;
	
	protected int _roomType;
	protected List<Integer> _completedRooms = new ArrayList<>();
	protected int jumps_current = 0;
	
	private Future<?> teleporterTask;
	private Future<?> spawnTask;
	private Future<?> killRiftTask;
	
	protected int _choosenRoom = -1;
	protected boolean _hasJumped = false;
	protected boolean isBossRoom = false;
	
	public DimensionalRift(Party party, int type, int room)
	{
		super();
		onCreate();
		startCollapseTimer(7200000); // 120 Ð¼Ð¸Ð½ÑƒÑ‚ Ñ‚Ð°Ð¹Ð¼ÐµÑ€, Ð´Ð»Ñ� Ð·Ð°Ñ‰Ð¸Ñ‚Ñ‹ Ð¾Ñ‚ ÑƒÑ‚ÐµÑ‡ÐµÐº Ð¿Ð°Ð¼Ñ�Ñ‚Ð¸
		setName("DimensionalRift");
		if (this instanceof DelusionChamber)
		{
			InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(type + 120); // Ð”Ð»Ñ� Ñ€Ð°Ð²ÐµÐ½Ñ�Ñ‚Ð²Ð° Ñ‚Ð¸Ð¿Ð° ÐºÐ¾Ð¼Ð½Ð°Ñ‚Ñ‹ Ð¸ Ð˜Ð” Ð¸Ð½Ñ�Ñ‚Ð°Ð½Ñ�Ð°
			setInstancedZone(iz);
			setName(iz.getName());
		}
		_roomType = type;
		//setPlayerGroup(party);
		setParty(party);
		if (!(this instanceof DelusionChamber))
		{
			party.setDimensionalRift(this);
		}
		party.setReflection(this);
		_choosenRoom = room;
		checkBossRoom(_choosenRoom);
		
		Location coords = getRoomCoord(_choosenRoom);
		
		setReturnLoc(party.getLeader().getLoc());
		setTeleportLoc(coords);
		for (Player p : party)
		{
			p.setVar("backCoords", getReturnLoc().toXYZString(), -1);
			//DimensionalRiftManager.teleToLocation(p, coords.setR(this).findPointToStay(50, 100), this);
			DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(coords, 50, 100, getGeoIndex()), this);
			p.setReflection(this);
		}
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer();
	}
	
	public int getType()
	{
		return _roomType;
	}
	
	public int getCurrentRoom()
	{
		return _choosenRoom;
	}
	
	protected void createTeleporterTimer()
	{
		if (teleporterTask != null)
		{
			teleporterTask.cancel(false);
			teleporterTask = null;
		}
		
		teleporterTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				if ((jumps_current < getMaxJumps()) && (getPlayersInside(true) > 0))
				{
					jumps_current++;
					teleportToNextRoom();
					createTeleporterTimer();
				}
				else
				{
					createNewKillRiftTimer();
				}
			}
		}, calcTimeToNextJump()); // Teleporter task, 8-10 minutes
	}
	
	public void createSpawnTimer(int room)
	{
		if (spawnTask != null)
		{
			spawnTask.cancel(false);
			spawnTask = null;
		}
		
		final DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room);
		
		spawnTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				for (SimpleSpawner s : riftRoom.getSpawns())
				{
					SimpleSpawner sp = s.clone();
					sp.setReflection(DimensionalRift.this);
					addSpawn(sp);
					if (!isBossRoom)
					{
						sp.startRespawn();
					}
					for (int i = 0; i < sp.getAmount(); i++)
					{
						sp.doSpawn(true);
					}
				}
				DimensionalRift.this.addSpawnWithoutRespawn(getManagerId(), riftRoom.getTeleportCoords(), 0);
			}
		}, Config.RIFT_SPAWN_DELAY);
	}
	
	public synchronized void createNewKillRiftTimer()
	{
		if (killRiftTask != null)
		{
			killRiftTask.cancel(false);
			killRiftTask = null;
		}
		
		killRiftTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				if (isCollapseStarted())
				{
					return;
				}
				for (Player p : getParty().getMembers())
				{
					if ((p != null) && (p.getReflection() == DimensionalRift.this))
					{
						DimensionalRiftManager.getInstance().teleportToWaitingRoom(p);
					}
				}
				DimensionalRift.this.collapse();
			}
		}, 100L);
	}
	
	public void partyMemberInvited()
	{
		createNewKillRiftTimer();
	}
	
	public void partyMemberExited(Player player)
	{
		if ((getParty().size() < Config.RIFT_MIN_PARTY_SIZE) || (getParty().size() == 1) || (getPlayersInside(true) == 0))
		{
			createNewKillRiftTimer();
		}
	}
	
	public void manualTeleport(Player player, NpcInstance npc)
	{
		if (!player.isInParty() || !player.getParty().isInReflection() || !(player.getParty().getReflection() instanceof DimensionalRift))
		{
			return;
		}
		
		if (!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (!isBossRoom)
		{
			if (_hasJumped)
			{
				DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/AlreadyTeleported.htm", npc);
				return;
			}
			_hasJumped = true;
		}
		else
		{
			manualExitRift(player, npc);
			return;
		}
		
		teleportToNextRoom();
	}
	
	public void manualExitRift(Player player, NpcInstance npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		
		if (!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		
		createNewKillRiftTimer();
	}
	
	protected void teleportToNextRoom()
	{
		_completedRooms.add(_choosenRoom);
		
		for (Spawner s : getSpawns())
		{
			s.deleteAll();
		}
		
		int size = DimensionalRiftManager.getInstance().getRooms(_roomType).size();
		/*
		 * if(jumps_current < getMaxJumps()) size--; // ÐºÐ¾Ð¼Ð½Ð°Ñ‚Ð° Ð±Ð¾Ñ�Ñ�Ð° Ð¼Ð¾Ð¶ÐµÑ‚ Ð±Ñ‹Ñ‚ÑŒ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾Ñ�Ð»ÐµÐ´Ð½ÐµÐ¹
		 */
		
		if ((getType() >= 11) && (jumps_current == getMaxJumps()))
		{
			_choosenRoom = 9; // Ð’ DC Ð¿Ð¾Ñ�Ð»ÐµÐ´Ð½Ð¸Ðµ 2 Ð¿ÐµÑ‡Ð°Ñ‚Ð¸ Ð²Ñ�ÐµÐ³Ð´Ð° ÐºÐ¾Ð½Ñ‡Ð°ÑŽÑ‚Ñ�Ñ� Ñ€ÐµÐ¹Ð´Ð¾Ð¼
		}
		else
		{ // Ð²Ñ‹Ð±Ð¸Ñ€Ð°ÐµÐ¼ ÐºÐ¾Ð¼Ð½Ð°Ñ‚Ñƒ, Ð³Ð´Ðµ ÐµÑ‰Ðµ Ð½Ðµ Ð±Ñ‹Ð»Ð¸
			List<Integer> notCompletedRooms = new ArrayList<>();
			for (int i = 1; i <= size; i++)
			{
				if (!_completedRooms.contains(i))
				{
					notCompletedRooms.add(i);
				}
			}
			_choosenRoom = notCompletedRooms.get(Rnd.get(notCompletedRooms.size()));
		}
		
		checkBossRoom(_choosenRoom);
		setTeleportLoc(getRoomCoord(_choosenRoom));
		
		//for (Player p : getPlayerGroup())
		for (Player p : getParty().getMembers())
		{
			if (p.getReflection() == this)
			{
				DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(getRoomCoord(_choosenRoom), 50, 100, DimensionalRift.this.getGeoIndex()), this);

//				DimensionalRiftManager.teleToLocation(p, getRoomCoord(_choosenRoom).setR(this).findPointToStay(50, 100), this);
			}
		}
		
		createSpawnTimer(_choosenRoom);
	}
	
	@Override
	public void collapse()
	{
		if (isCollapseStarted())
		{
			return;
		}
		
		Future<?> task = teleporterTask;
		if (task != null)
		{
			teleporterTask = null;
			task.cancel(false);
		}
		
		task = spawnTask;
		if (task != null)
		{
			spawnTask = null;
			task.cancel(false);
		}
		
		task = killRiftTask;
		if (task != null)
		{
			killRiftTask = null;
			task.cancel(false);
		}
		
		_completedRooms = null;
		
		//Party party = (getPlayerGroup() instanceof Party) ? (Party) getPlayerGroup() : null;
		Party party = getParty();
		if (party != null)
		{
			party.setDimensionalRift(null);
		}
		
		super.collapse();
	}
	
	protected long calcTimeToNextJump()
	{
		if (isBossRoom)
		{
			return 60 * MILLISECONDS_IN_MINUTE;
		}
		return (Config.RIFT_AUTO_JUMPS_TIME * MILLISECONDS_IN_MINUTE) + Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_RAND);
	}
	
	public void memberDead(Player player)
	{
		if (getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
		}
	}
	
	public void usedTeleport(Player player)
	{
		if (getPlayersInside(false) < Config.RIFT_MIN_PARTY_SIZE)
		{
			createNewKillRiftTimer();
		}
	}
	
	public void checkBossRoom(int room)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room).isBossRoom();
	}
	
	public Location getRoomCoord(int room)
	{
		return DimensionalRiftManager.getInstance().getRoom(_roomType, room).getTeleportCoords();
	}
	
	/** ÐŸÐ¾ ÑƒÐ¼Ð¾Ð»Ñ‡Ð°Ð½Ð¸ÑŽ 4 */
	public int getMaxJumps()
	{
		return Math.max(Math.min(Config.RIFT_MAX_JUMPS, 8), 1);
	}
	
	@Override
	public boolean canChampions()
	{
		return true;
	}
	
	@Override
	public String getName()
	{
		return "DimensionalRift";
	}
	
	protected int getManagerId()
	{
		return 31865;
	}
	
	protected int getPlayersInside(boolean alive)
	{
		if (_playerCount == 0)
		{
			return 0;
		}
		
		int sum = 0;
		
		for (Player p : getPlayers())
		{
			if (!alive || !p.isDead())
			{
				sum++;
			}
		}
		
		return sum;
	}
	
	@Override
	public void removeObject(GameObject o)
	{
		if (o.isPlayer())
		{
			if (_playerCount <= 1)
			{
				createNewKillRiftTimer();
			}
		}
		super.removeObject(o);
	}
}