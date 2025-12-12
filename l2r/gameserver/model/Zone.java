package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import l2r.gameserver.instancemanager.ReflectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.listener.Listener;
import l2r.commons.listener.ListenerList;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.EventTrigger;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.FuncAdd;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.ZoneTemplate;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.PositionUtils;

public class Zone
{
	@SuppressWarnings("unused")
	private static final Logger _log = LoggerFactory.getLogger(Zone.class);
	
	public static final Zone[] EMPTY_L2ZONE_ARRAY = new Zone[0];
	
	public static enum ZoneType
	{
		AirshipController,
		
		SIEGE,
		RESIDENCE,
		HEADQUARTER,
		FISHING,
		UnderGroundColiseum,
		water,
		battle_zone,
		damage,
		instant_skill,
		mother_tree,
		peace_zone,
		poison,
		ssq_zone,
		swamp,
		no_escape,
		no_landing,
		no_restart,
		no_transform,
		no_summon,
		dummy,
		offshore,
		epic,
		buff_store_only,
		special_pvp,
		fix_beleth,
		global_pvp_zone,
		world_pvp
	}
	
	public enum ZoneTarget
	{
		pc,
		npc,
		only_pc
	}
	
	public static final String BLOCKED_ACTION_PRIVATE_STORE = "open_private_store";
	public static final String BLOCKED_ACTION_PRIVATE_WORKSHOP = "open_private_workshop";
	public static final String BLOCKED_ACTION_DROP_MERCHANT_GUARD = "drop_merchant_guard";
	public static final String BLOCKED_ACTION_SAVE_BOOKMARK = "save_bookmark";
	public static final String BLOCKED_ACTION_USE_BOOKMARK = "use_bookmark";
	public static final String BLOCKED_ACTION_MINIMAP = "open_minimap";
	
	// FandC - Better implementation for zone effects and damage threads
	private Future<?> _effectThread = null;
	private Future<?> _damageThread = null;
	
	private final class SkillTimer implements Runnable
	{
		private final Skill _skill;
		private final int _zoneTime;
		private final int _randomTime;
		private long _activateTime = 0;
		
		protected SkillTimer()
		{
			_skill = getZoneSkill();
			_zoneTime = getTemplate().getUnitTick() * 1000;
			_randomTime = getTemplate().getRandomTick() * 1000;
		}
		
		@Override
		public void run()
		{
			if (!isActive())
			{
				return;
			}
			
			if (_skill == null)
			{
				return;
			}
			
			for (Creature target : getObjects())
			{
				if ((target == null) || target.isDead())
				{
					continue;
				}
				
				if (!checkTarget(target))
				{
					continue;
				}
				
				if (Rnd.chance(getTemplate().getSkillProb()) && !target.isDead())
				{
					_skill.getEffects(target, target, false, false);
				}
			}
			
			// TODO: This is not the same as in l2j, as we dont have on, off times, only unit ticks, so we use the same for both
			if (_activateTime == 0)
			{
				_activateTime = System.currentTimeMillis() + (_zoneTime + Rnd.get(-_randomTime, _randomTime));
			}
			// If the zone is activated and over time, deactivate the zone and the next activation seteamos
			else if (isActive())
			{
				if (_activateTime < System.currentTimeMillis())
				{
					setActive(false);
					_activateTime = System.currentTimeMillis() + (_zoneTime + Rnd.get(-_randomTime, _randomTime));
				}
			}
			// If the zone is disabled and the time and way, activate the area and seteamos the next deactivation
			else
			{
				if (_activateTime < System.currentTimeMillis())
				{
					setActive(true);
					_activateTime = System.currentTimeMillis() + (_zoneTime + Rnd.get(-_randomTime, _randomTime));
				}
			}
		}
	}
	
	private final class DamageTimer implements Runnable
	{
		private final int _hp;
		private final int _mp;
		private final int _message;
		private final int _zoneTime;
		private final int _randomTime;
		private long _activateTime = 0;
		
		protected DamageTimer()
		{
			_hp = getDamageOnHP();
			_mp = getDamageOnMP();
			_message = getDamageMessageId();
			_zoneTime = getTemplate().getUnitTick() * 1000;
			_randomTime = getTemplate().getRandomTick() * 1000;
		}
		
		@Override
		public void run()
		{
			if (!isActive())
			{
				return;
			}
			
			if ((_hp == 0) || (_mp == 0))
			{
				return;
			}
			
			for (Creature target : getObjects())
			{
				if ((target == null) || target.isDead())
				{
					continue;
				}
				
				if (!checkTarget(target))
				{
					continue;
				}
				
				if (_hp > 0)
				{
					target.reduceCurrentHp(_hp, target, null, false, false, true, false, false, false, true);
					if (_message > 0)
					{
						target.sendPacket(new SystemMessage2(SystemMsg.valueOf(_message)).addInteger(_hp));
					}
				}
				
				if (_mp > 0)
				{
					target.reduceCurrentMp(_mp, null);
					if (_message > 0)
					{
						target.sendPacket(new SystemMessage2(SystemMsg.valueOf(_message)).addInteger(_mp));
					}
				}
			}
			
			// TODO: This is not the same as in l2j, as we dont have on, off times, only unit ticks, so we use the same for both
			if (_activateTime == 0)
			{
				_activateTime = System.currentTimeMillis() + (_zoneTime + Rnd.get(-_randomTime, _randomTime));
			}
			else if (isActive())
			{
				if (_activateTime < System.currentTimeMillis())
				{
					setActive(false);
					_activateTime = System.currentTimeMillis() + (_zoneTime + Rnd.get(-_randomTime, _randomTime));
				}
			}
			else
			{
				if (_activateTime < System.currentTimeMillis())
				{
					setActive(true);
					_activateTime = System.currentTimeMillis() + (_zoneTime + Rnd.get(-_randomTime, _randomTime));
				}
			}
		}
	}
	
	public class ZoneListenerList extends ListenerList<Zone>
	{
		public void onEnter(Creature actor)
		{
			if (!getListeners().isEmpty())
			{
				for (Listener<Zone> listener : getListeners())
				{
					((OnZoneEnterLeaveListener) listener).onZoneEnter(Zone.this, actor);
				}
			}
		}
		
		public void onLeave(Creature actor)
		{
			if (!getListeners().isEmpty())
			{
				for (Listener<Zone> listener : getListeners())
				{
					((OnZoneEnterLeaveListener) listener).onZoneLeave(Zone.this, actor);
				}
			}
		}
		
		public void onEquipChanged(Creature actor)
		{
			if (!getListeners().isEmpty())
			{
				for (Listener<Zone> listener : getListeners())
				{
					((OnZoneEnterLeaveListener) listener).onEquipChanged(Zone.this, actor);
				}
			}
		}
	}
	
	private ZoneType _type;
	private String _name;
	private boolean _active;
	private final MultiValueSet<String> _params;
	
	private final ZoneTemplate _template;
	
	private Reflection _reflection;
	
	private final ZoneListenerList listeners = new ZoneListenerList();
	
	private final List<Creature> _objects = new CopyOnWriteArrayList<>();
	
	/**
	 * ÐžÑ€Ð´ÐµÑ€ Ð² Ð·Ð¾Ð½Ð°Ñ…, Ñ� Ð½Ð¸Ð¼ Ð¼Ñ‹ Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÐ¼/ÑƒÐ±Ð¸Ñ€Ð°ÐµÐ¼ Ñ�Ñ‚Ð°Ñ‚Ñ‹. TODO: Ñ�Ñ€Ð°Ð²Ð½Ð¸Ñ‚ÑŒ Ð¾Ñ€Ð´ÐµÑ€ Ñ� Ð¾Ñ„Ñ„Ð¾Ð¼, Ð¿Ð¾ÐºÐ° Ð¾Ñ‚ Ñ„Ð¾Ð½Ð°Ñ€Ñ�
	 */
	public final static int ZONE_STATS_ORDER = 0x40;
	
	public Zone(ZoneTemplate template)
	{
		this(template.getType(), template);
	}
	
	public Zone(ZoneType type, ZoneTemplate template)
	{
		_type = type;
		_template = template;
		_params = template.getParams();
	}
	
	public Zone(String name, ZoneType type, Territory terr)
	{
		StatsSet set = new StatsSet();
		set.set("name", name);
		set.set("type", type);
		set.set("territory", terr);
		set.set("restart_points", Collections.emptyList());
		set.set("PKrestart_points", Collections.emptyList());
		ZoneTemplate template = new ZoneTemplate(set);
		
		_type = type;
		_template = template;
		_params = template.getParams();
	}
	
	public ZoneTemplate getTemplate()
	{
		return _template;
	}
	
	public final String getName()
	{
		if (_name != null)
		{
			return _name;
		}
		
		return getTemplate().getName();
	}
	
	public final void setName(String name)
	{
		_name = name;
	}
	
	public ZoneType getType()
	{
		return _type;
	}
	
	public void setType(ZoneType type)
	{
		_type = type;
	}
	
	public Territory getTerritory()
	{
		return getTemplate().getTerritory();
	}
	
	public final int getEnteringMessageId()
	{
		return getTemplate().getEnteringMessageId();
	}
	
	public final int getLeavingMessageId()
	{
		return getTemplate().getLeavingMessageId();
	}
	
	public Skill getZoneSkill()
	{
		return getTemplate().getZoneSkill();
	}
	
	public ZoneTarget getZoneTarget()
	{
		return getTemplate().getZoneTarget();
	}
	
	public Race getAffectRace()
	{
		return getTemplate().getAffectRace();
	}
	
	/**
	 * Ð�Ð¾Ð¼ÐµÑ€ Ñ�Ð¸Ñ�Ñ‚ÐµÐ¼Ð½Ð¾Ð³Ð¾ Ð²Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ñ� ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ðµ Ð±ÑƒÐ´ÐµÑ‚ Ð¾Ñ‚Ð¾Ñ�Ð»Ð°Ð½Ð¾ Ð¸Ð³Ñ€Ð¾ÐºÑƒ Ð¿Ñ€Ð¸ Ð½Ð°Ð½ÐµÑ�ÐµÐ½Ð¸Ð¸ ÑƒÑ€Ð¾Ð½Ð° Ð·Ð¾Ð½Ð¾Ð¹
	 * @return SystemMessage ID
	 */
	public int getDamageMessageId()
	{
		return getTemplate().getDamageMessageId();
	}


	public boolean getDualBox(Player pl)
	{
		//if ()
		return getTemplate().checkMultiBox(pl);
	}
	
	/**
	 * Ð¡ÐºÐ¾Ð»ÑŒÐºÐ¾ ÑƒÑ€Ð¾Ð½Ð° Ð·Ð¾Ð½Ð° Ð½Ð°Ð½ÐµÑ�ÐµÑ‚ Ð¿Ð¾ Ñ…Ð¿
	 * @return ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ ÑƒÑ€Ð¾Ð½Ð°
	 */
	public int getDamageOnHP()
	{
		return getTemplate().getDamageOnHP();
	}
	
	/**
	 * Ð¡ÐºÐ¾Ð»ÑŒÐºÐ¾ ÑƒÑ€Ð¾Ð½Ð° Ð·Ð¾Ð½Ð° Ð½Ð°Ð½ÐµÑ�ÐµÑ‚ Ð¿Ð¾ Ð¼Ð¿
	 * @return ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ ÑƒÑ€Ð¾Ð½Ð°
	 */
	public int getDamageOnMP()
	{
		return getTemplate().getDamageOnMP();
	}
	
	/**
	 * @return Ð‘Ð¾Ð½ÑƒÑ� Ðº Ñ�ÐºÐ¾Ñ€Ð¾Ñ�Ñ‚Ð¸ Ð´Ð²Ð¸Ð¶ÐµÐ½Ð¸Ñ� Ð² Ð·Ð¾Ð½Ðµ
	 */
	public double getMoveBonus()
	{
		return getTemplate().getMoveBonus();
	}
	
	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð±Ð¾Ð½ÑƒÑ� Ñ€ÐµÐ³ÐµÐ½ÐµÑ€Ð°Ñ†Ð¸Ð¸ Ñ…Ð¿ Ð² Ñ�Ñ‚Ð¾Ð¹ Ð·Ð¾Ð½Ðµ
	 * @return Ð‘Ð¾Ð½ÑƒÑ� Ñ€ÐµÐ³ÐµÐ½Ð°Ñ€Ð°Ñ†Ð¸Ð¸ Ñ…Ð¿ Ð² Ñ�Ñ‚Ð¾Ð¹ Ð·Ð¾Ð½Ðµ
	 */
	public double getRegenBonusHP()
	{
		return getTemplate().getRegenBonusHP();
	}
	
	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð±Ð¾Ð½ÑƒÑ� Ñ€ÐµÐ³ÐµÐ½ÐµÑ€Ð°Ñ†Ð¸Ð¸ Ð¼Ð¿ Ð² Ñ�Ñ‚Ð¾Ð¹ Ð·Ð¾Ð½Ðµ
	 * @return Ð‘Ð¾Ð½ÑƒÑ� Ñ€ÐµÐ³ÐµÐ½Ð°Ñ€Ð°Ñ†Ð¸Ð¸ Ð¼Ð¿ Ð² Ñ�Ñ‚Ð¾Ð¹ Ð·Ð¾Ð½Ðµ
	 */
	public double getRegenBonusMP()
	{
		return getTemplate().getRegenBonusMP();
	}
	
	public long getRestartTime()
	{
		return getTemplate().getRestartTime();
	}
	
	public List<Location> getRestartPoints()
	{
		return getTemplate().getRestartPoints();
	}
	
	public List<Location> getPKRestartPoints()
	{
		return getTemplate().getPKRestartPoints();
	}
	
	public Location getSpawn()
	{
		if (getRestartPoints() == null)
		{
			return null;
		}
		Location loc = getRestartPoints().get(Rnd.get(getRestartPoints().size()));
		return loc.clone();
	}
	
	public Location getPKSpawn()
	{
		if (getPKRestartPoints() == null)
		{
			return getSpawn();
		}
		Location loc = getPKRestartPoints().get(Rnd.get(getPKRestartPoints().size()));
		return loc.clone();
	}
	
	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€Ñ�ÐµÑ‚ Ð½Ð°Ñ…Ð¾Ð´Ñ�Ñ‚Ñ�Ñ� Ð»Ð¸ Ð´Ð°Ð½Ñ‹Ðµ ÐºÐ¾Ð¾Ñ€Ð´Ð¸Ð½Ð°Ñ‚Ñ‹ Ð² Ð·Ð¾Ð½Ðµ. _loc - Ñ�Ñ‚Ð°Ð½Ð´Ð°Ñ€Ñ‚Ð½Ð°Ñ� Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸Ñ� Ð´Ð»Ñ� Ð·Ð¾Ð½Ñ‹
	 * @param x ÐºÐ¾Ð¾Ñ€Ð´Ð¸Ð½Ð°Ñ‚Ð°
	 * @param y ÐºÐ¾Ð¾Ñ€Ð´Ð¸Ð½Ð°Ñ‚Ð°
	 * @return Ð½Ð°Ñ…Ð¾Ð´Ñ�Ñ‚Ñ�Ñ� Ð»Ð¸ ÐºÐ¾Ð¾Ñ€Ð´Ð¸Ð½Ð°Ñ‚Ñ‹ Ð² Ð»Ð¾ÐºÐ°Ñ†Ð¸Ð¸
	 */
	public boolean checkIfInZone(int x, int y)
	{
		return getTerritory().isInside(x, y);
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return checkIfInZone(x, y, z, getReflection());
	}
	
	public boolean checkIfInZone(int x, int y, int z, Reflection reflection)
	{
		return isActive() && (_reflection == reflection) && getTerritory().isInside(x, y, z);
	}
	
	public boolean checkIfInZone(Creature cha)
	{
		return _objects.contains(cha);
	}
	
	public final double findDistanceToZone(GameObject obj, boolean includeZAxis)
	{
		return findDistanceToZone(obj.getX(), obj.getY(), obj.getZ(), includeZAxis);
	}
	
	public final double findDistanceToZone(int x, int y, int z, boolean includeZAxis)
	{
		return PositionUtils.calculateDistance(x, y, z, (getTerritory().getXmax() + getTerritory().getXmin()) / 2, (getTerritory().getYmax() + getTerritory().getYmin()) / 2, (getTerritory().getZmax() + getTerritory().getZmin()) / 2, includeZAxis);
	}
	
	/**
	 * ÐžÐ±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ° Ð²Ñ…Ð¾Ð´Ð° Ð² Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸ÑŽ ÐŸÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ð²Ñ�ÐµÐ³Ð´Ð° Ð´Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð² Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð²Ð½Ðµ Ð·Ð°Ð²Ð¸Ñ�Ð¸Ð¼Ð¾Ñ�Ñ‚Ð¸ Ð¾Ñ‚ Ð°ÐºÑ‚Ð¸Ð²Ð½Ð¾Ñ�Ñ‚Ð¸ Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸Ð¸. Ð•Ñ�Ð»Ð¸ Ð·Ð¾Ð½Ð° Ð°ÐºÐ¸Ð²Ð½Ð°Ñ�, Ñ‚Ð¾ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚Ð°ÐµÑ‚Ñ�Ñ� Ð²Ñ…Ð¾Ð´ Ð² Ð·Ð¾Ð½Ñƒ
	 * @param cha ÐºÑ‚Ð¾ Ð²Ñ…Ð¾Ð´Ð¸Ñ‚
	 */
	public void doEnter(Creature cha)
	{
		boolean added = false;
		
		if (!_objects.contains(cha))
		{
			added = _objects.add(cha);
		}
		
		if (added)
		{
			onZoneEnter(cha);
		}
		
		if ((cha != null) && (cha.isPlayer()) && (cha.getPlayer().isGM()))
		{
			cha.sendMessage("Entered the zone " + getName());
		}
	}
	
	/**
	 * Log processing zone
	 * @param actor who enters
	 */
	protected void onZoneEnter(Creature actor)
	{
		checkEffects(actor, true);
		addZoneStats(actor);
		
		if (actor.isPlayer())
		{
			if (getType() == ZoneType.buff_store_only)
			{
				actor.sendPacket(new Say2(0, ChatType.BATTLEFIELD, "Buff Store", "Here you can sell or buy buffs"));
			}
//			if (getType() == ZoneType.epic)
//			{
//				actor.sendPacket(new Say2(0, ChatType.BATTLEFIELD, "PvP Area", "You entered into attack zone."));
//			}
			if (getEnteringMessageId() != 0)
			{
				actor.sendPacket(new SystemMessage2(SystemMsg.valueOf(getEnteringMessageId())));
			}
			if (getTemplate().getEventId() != 0)
			{
				actor.sendPacket(new EventTrigger(getTemplate().getEventId(), true));
			}
			if (getTemplate().getBlockedActions() != null)
			{
				actor.getPlayer().blockActions(getTemplate().getBlockedActions());
			}
			if (getType() == ZoneType.fix_beleth)
			{
				actor.getPlayer().sendMessage("Anti-beleth exploit");
				actor.getPlayer().teleToClosestTown();
				return;
			}
            if (getType() == ZoneType.peace_zone)
            {
                Player p = (Player) actor;
                DuelEvent duel = p.getEvent(DuelEvent.class);
                if (duel != null)
                {
                    duel.abortDuel(p);
                }
            }
			if (getType() == ZoneType.epic)
			{
				Player p = (Player) actor;
				if (!getDualBox(p))
				{
					actor.sendPacket(new Say2(0, ChatType.BATTLEFIELD, "PvP Area", "You entered into attack zone."));
					if (p.isInParty() && p.getParty().isLeader(p))
					{
						for (Player plr : p.getParty().getMembersInRange(p, 1200))
						{
							actor.sendChatMessage(0,ChatType.CRITICAL_ANNOUNCE.ordinal(), "EpicZone","You have too many client open");
							plr.teleToLocation(82698,148638,-3473);
							plr.sendMessage("Party Leader Mass Teleport");
						}
					}
				}
			}



//			if (getType() == ZoneType.epic)
//			{
//				Player p = (Player) actor;
//				if (!p.checkMultiBox())
//				{
//					//actor.sendMessage("You have too many client open");
//					actor.sendChatMessage(0,ChatType.CRITICAL_ANNOUNCE.ordinal(), "EpicZone","You have too many client open");
//
//					if (p.isInParty() && p.getParty().isLeader(p))
//					{
//						for (Player plr : p.getParty().getMembersInRange(p, 1200))
//						{
//							plr.teleToLocation(82698,148638,-3473);
//							plr.sendMessage("Party Leader Mass Teleport");
//						}
//					}
//					else
//					{
//
//					}
//
//				}
//			}

			
			// Synerge - Solo puede haber 2 pjs por hwid en una zona epica. Si otro trata de entrar, es kickeado. En queen ant solo permitimos 1. No se activa en instancias
			final int maxHwidCount = (getName().equalsIgnoreCase("queen_ant_epic") ? 1 : 2);
			if ((getType() == ZoneType.epic) && ((getReflection() == null) || (getReflection() == ReflectionManager.DEFAULT)))
			{
				int hwidCount = 0;
				for (Creature obj : getObjects())
				{
					if ((obj == null) || !obj.isPlayer())
					{
						continue;
					}

					if (obj.getPlayer().getHWID().equals(actor.getPlayer().getHWID()))
					{
						hwidCount++;
					}
				}

				if (hwidCount >= maxHwidCount)
				{
					actor.getPlayer().sendMessage("You have been kicked out of the zone because you already have " + maxHwidCount + " character(s) inside");
					actor.getPlayer().teleToClosestTown();
					return;
				}
			}
		}
		
		listeners.onEnter(actor);
		actor.getListeners().onZoneEnter(this);
	}
	
	/**
	 * Processing exit zone Object always removed from the list, regardless of the zone If the active zone, it is parsed out of the zone
	 * @param cha
	 */
	public void doLeave(Creature cha)
	{
		boolean removed = false;
		
		removed = _objects.remove(cha);
		
		if (removed)
		{
			onZoneLeave(cha);
		}
		
		if ((cha != null) && (cha.isPlayer()) && (cha.getPlayer().isGM()))
		{
			cha.sendMessage("Left the area " + getName());
		}
	}
	
	/**
	 * Processing exit zone
	 * @param actor who goes out
	 */
	protected void onZoneLeave(Creature actor)
	{
		checkEffects(actor, false);
		removeZoneStats(actor);
		
		if (actor.isPlayer())
		{
			if (getType() == ZoneType.buff_store_only)
			{
				actor.sendPacket(new Say2(0, ChatType.BATTLEFIELD, "Buff Store", "You no longer can sell or buy players buffs"));
			}
			if (getType() == ZoneType.epic)
			{
				actor.sendPacket(new Say2(0, ChatType.BATTLEFIELD, "PvP Area", "You have left attack zone."));
			}
			if ((getLeavingMessageId() != 0) && actor.isPlayer())
			{
				actor.sendPacket(new SystemMessage2(SystemMsg.valueOf(getLeavingMessageId())));
			}
			if ((getTemplate().getEventId() != 0) && actor.isPlayer())
			{
				actor.sendPacket(new EventTrigger(getTemplate().getEventId(), false));
			}
			if (getTemplate().getBlockedActions() != null)
			{
				((Player) actor).unblockActions(getTemplate().getBlockedActions());
			}
            if (actor.isPlayer() && ((Player) actor).isTerritoryFlagEquipped())
            {
                Player player = (Player) actor;
                TerritoryWardObject wardObject = (TerritoryWardObject) player.getActiveWeaponFlagAttachment();
                if ((getType() == ZoneType.SIEGE) && !wardObject.isFlagOut())
                {
                    wardObject.startTerrFlagCountDown(player);
                }
            }
		}
		
		listeners.onLeave(actor);
		actor.getListeners().onZoneLeave(this);
	}
	
	/**
	 * Synerge - Custom listener to check equipment changes inside a zone
	 * @param actor
	 */
	public void onEquipChanged(Creature actor)
	{
		listeners.onEquipChanged(actor);
	}
	
	/**
	 * Ð”Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÑ‚ Ñ�Ñ‚Ð°Ñ‚Ñ‹ Ð·Ð¾Ð½Ðµ
	 * @param cha Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð¼Ñƒ Ð´Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÑ‚Ñ�Ñ�
	 */
	private void addZoneStats(Creature cha)
	{
		// ÐŸÑ€Ð¾Ð²ÐµÑ€ÐºÐ° Ñ†ÐµÐ»Ð¸
		if (!checkTarget(cha))
		{
			return;
		}
		
		// Ð¡ÐºÐ¾Ñ€Ð¾Ñ�Ñ‚ÑŒ Ð´Ð²Ð¸Ð¶ÐµÐ½Ð¸Ñ� Ð½Ð°ÐºÐ»Ð°Ð´Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ� Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð½Ð° L2Playable
		// affectRace Ð² Ð±Ð°Ð·Ðµ Ð½Ðµ ÑƒÐºÐ°Ð·Ð°Ð½, ÐµÑ�Ð»Ð¸ Ð½Ð°Ð´Ð¾ Ð±ÑƒÐ´ÐµÑ‚ Ð²Ð»Ð¸Ñ�Ð½Ð¸Ðµ, Ñ‚Ð¾ Ð¿Ð¾Ð¿Ñ€Ð°Ð²Ð¸Ð¼
		if (getMoveBonus() != 0)
		{
			if (cha.isPlayable())
			{
				cha.addStatFunc(new FuncAdd(Stats.RUN_SPEED, ZONE_STATS_ORDER, this, getMoveBonus()));
				cha.sendChanges();
			}
		}
		
		// Ð•Ñ�Ð»Ð¸ Ñƒ Ð½Ð°Ñ� ÐµÑ�Ñ‚ÑŒ Ñ‡Ñ‚Ð¾ Ñ€ÐµÐ³ÐµÐ½Ð¸Ñ‚ÑŒ
		if (getRegenBonusHP() != 0)
		{
			cha.addStatFunc(new FuncAdd(Stats.REGENERATE_HP_RATE, ZONE_STATS_ORDER, this, getRegenBonusHP()));
		}
		
		// Ð•Ñ�Ð»Ð¸ Ñƒ Ð½Ð°Ñ� ÐµÑ�Ñ‚ÑŒ Ñ‡Ñ‚Ð¾ Ñ€ÐµÐ³ÐµÐ½Ð¸Ñ‚ÑŒ
		if (getRegenBonusMP() != 0)
		{
			cha.addStatFunc(new FuncAdd(Stats.REGENERATE_MP_RATE, ZONE_STATS_ORDER, this, getRegenBonusMP()));
		}
	}
	
	/**
	 * Ð£Ð±Ð¸Ñ€Ð°ÐµÑ‚ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ñ‹Ðµ Ð·Ð¾Ð½Ð¾Ð¹ Ñ�Ñ‚Ð°Ñ‚Ñ‹
	 * @param cha Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ñƒ ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð³Ð¾ ÑƒÐ±Ð¸Ñ€Ð°ÐµÑ‚Ñ�Ñ�
	 */
	private void removeZoneStats(Creature cha)
	{
		if ((getRegenBonusHP() == 0) && (getRegenBonusMP() == 0) && (getMoveBonus() == 0))
		{
			return;
		}
		
		cha.removeStatsOwner(this);
		
		cha.sendChanges();
	}
	
	/**
	 * ÐŸÑ€Ð¸Ð¼ÐµÐ½Ñ�ÐµÑ‚ Ñ�Ñ„Ñ„ÐµÐºÑ‚Ñ‹ Ð¿Ñ€Ð¸ Ð²Ñ…Ð¾Ð´Ðµ/Ð²Ñ‹Ñ…Ð¾Ð´Ðµ Ð¸Ð·(Ð²) Ð·Ð¾Ð½Ñƒ
	 * @param cha Ð¾Ð±ÑŒÐµÐºÑ‚
	 * @param enter Ð²Ð¾ÑˆÐµÐ» Ð¸Ð»Ð¸ Ð²Ñ‹ÑˆÐµÐ»
	 */
	private void checkEffects(Creature cha, boolean enter)
	{
		if (checkTarget(cha))
		{
			// Prims - New implementation of zone effect and damage threads
			if (enter)
			{
				if (getZoneSkill() != null)
				{
					if (_effectThread == null)
					{
						synchronized (this)
						{
							if (_effectThread == null)
							{
								// TODO: Reuse 30 hardcoded
								_effectThread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SkillTimer(), getTemplate().getInitialDelay(), 30000);
							}
						}
					}
				}
				else if ((getDamageOnHP() > 0) || (getDamageOnHP() > 0))
				{
					if (_damageThread == null)
					{
						synchronized (this)
						{
							if (_damageThread == null)
							{
								// TODO: Reuse 30 hardcoded
								_damageThread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DamageTimer(), getTemplate().getInitialDelay(), 30000);
							}
						}
					}
				}
			}
			else
			{
				if (getZoneSkill() != null)
				{
					cha.getEffectList().stopEffect(getZoneSkill());
				}
			}
		}
	}
	
	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€Ñ�ÐµÑ‚ Ð¿Ð¾Ð´Ñ…Ð¾Ð´Ð¸Ñ‚ Ð»Ð¸ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ð´Ð»Ñ� Ð²Ñ‹Ð·Ð²Ð°Ð²ÑˆÐµÐ³Ð¾ Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ�
	 * @param cha Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶
	 * @return Ð¿Ð¾Ð´Ð¾ÑˆÐµÐ» Ð»Ð¸
	 */
	protected boolean checkTarget(Creature cha)
	{
		switch (getZoneTarget())
		{
			case pc:
				if (!cha.isPlayable())
				{
					return false;
				}
				break;
			case only_pc:
				if (!cha.isPlayer())
				{
					return false;
				}
				break;
			case npc:
				if (!cha.isNpc())
				{
					return false;
				}
				break;
		}
		
		// Ð•Ñ�Ð»Ð¸ Ñƒ Ð½Ð°Ñ� Ñ€Ð°Ñ�Ð° Ð½Ðµ "all"
		if (getAffectRace() != null)
		{
			Player player = cha.getPlayer();
			// ÐµÑ�Ð»Ð¸ Ð½Ðµ Ð¸Ð³Ñ€Ð¾Ð²Ð¾Ð¹ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶
			if (player == null)
			{
				return false;
			}
			// ÐµÑ�Ð»Ð¸ Ñ€Ð°Ñ�Ð° Ð½Ðµ Ð¿Ð¾Ð´Ñ…Ð¾Ð´Ð¸Ñ‚
			if (player.getRace() != getAffectRace())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public Creature[] getObjects()
	{
		return _objects.toArray(new Creature[_objects.size()]);
	}
	
	public List<Player> getInsidePlayers()
	{
		final List<Player> result = new ArrayList<>();
		
		Creature cha;
		for (Creature _object : _objects)
		{
			if (((cha = _object) != null) && cha.isPlayer())
			{
				result.add((Player) cha);
			}
		}
		
		return result;
	}
	
	public List<Playable> getInsidePlayables()
	{
		final List<Playable> result = new ArrayList<>();
		
		Creature cha;
		for (Creature _object : _objects)
		{
			if (((cha = _object) != null) && cha.isPlayable())
			{
				result.add((Playable) cha);
			}
		}
		
		return result;
	}
	
	public List<NpcInstance> getInsideNpcs()
	{
		List<NpcInstance> result = new ArrayList<>();
		
		Creature cha;
		for (Creature _object : _objects)
		{
			if (((cha = _object) != null) && cha.isNpc())
			{
				result.add((NpcInstance) cha);
			}
		}
		
		return result;
	}
	
	/**
	 * Ð£Ñ�Ñ‚Ð°Ð½Ð¾Ð²ÐºÐ° Ð°ÐºÑ‚Ð¸Ð²Ð½Ð¾Ñ�Ñ‚Ð¸ Ð·Ð¾Ð½Ñ‹. ÐŸÑ€Ð¸ ÑƒÑ�Ñ‚Ð°Ð½Ð¾Ð²ÐºÐ¸ Ñ„Ð»Ð°Ð³Ð° Ð°ÐºÑ‚Ð¸Ð²Ð½Ð¾Ñ�Ñ‚Ð¸, Ð·Ð¾Ð½Ð° Ð´Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð² Ñ�Ð¾Ð¾Ñ‚Ð²ÐµÑ�Ñ‚Ð²ÑƒÑŽÑ‰Ð¸Ðµ Ñ€ÐµÐ³Ð¸Ð¾Ð½Ñ‹. Ð’ Ñ�Ð»ÑƒÑ‡Ð°Ðµ Ñ�Ð±Ñ€Ð¾Ñ�Ð° - ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ�.
	 * @param value Ð°ÐºÑ‚Ð¸Ð²Ð½Ð° Ð»Ð¸ Ð·Ð¾Ð½Ð°
	 */
	public void setActive(boolean value)
	{
		if (_active == value)
		{
			return;
		}
		
		_active = value;
		
		if (isActive())
		{
			World.addZone(Zone.this);
		}
		else
		{
			World.removeZone(Zone.this);
		}
	}
	
	public boolean isActive()
	{
		return _active;
	}
	
	public void setReflection(Reflection reflection)
	{
		_reflection = reflection;
	}
	
	public Reflection getReflection()
	{
		return _reflection;
	}
	
	public void setParam(String name, String value)
	{
		_params.put(name, value);
	}
	
	public void setParam(String name, Object value)
	{
		_params.put(name, value);
	}
	
	public MultiValueSet<String> getParams()
	{
		return _params;
	}
	
	public <T extends Listener<Zone>> boolean addListener(T listener)
	{
		return listeners.add(listener);
	}
	
	public <T extends Listener<Zone>> boolean removeListener(T listener)
	{
		return listeners.remove(listener);
	}
	
	@Override
	public final String toString()
	{
		return "[Zone " + getType() + " name: " + getName() + "]";
	}
	
	public void broadcastPacket(L2GameServerPacket packet, boolean toAliveOnly)
	{
		List<Player> insideZoners = getInsidePlayers();
		
		if ((insideZoners != null) && !insideZoners.isEmpty())
		{
			for (Player player : insideZoners)
			{
				if (toAliveOnly)
				{
					if (!player.isDead())
					{
						player.broadcastPacket(packet);
					}
				}
				else
				{
					player.broadcastPacket(packet);
				}
			}
		}
	}
}