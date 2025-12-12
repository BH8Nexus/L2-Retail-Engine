package l2r.gameserver.model;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.util.Rnd;
import l2r.commons.util.concurrent.atomic.AtomicState;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.custom.GmEventManager;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.AggroList.AggroInfo;
import l2r.gameserver.model.Skill.SkillTargetType;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.actor.permission.PlayablePermissionList;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.StaticObjectInstance;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.CharTemplate;
import l2r.gameserver.templates.item.EtcItemTemplate;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;

public abstract class Playable extends Creature
{
	private final AtomicState _isSilentMoving = new AtomicState();
	
	private boolean _isPendingRevive;
	
	/** Î â€˜Î Â»Î ÎŽÎ ÎŠÎ ÎˆÎ¡â‚¬Î ÎŽÎ Â²Î ÎŠÎ Â° Î Î„Î Â»Î¡ï¿½ Î¡â€¡Î¡â€šÎ ÂµÎ Â½Î ÎˆÎ¡ï¿½/Î Â·Î Â°Î Î�Î ÎˆÎ¡ï¿½Î Îˆ Î¡ï¿½Î ÎŽÎ¡ï¿½Î¡â€šÎ ÎŽÎ¡ï¿½Î Â½Î ÎˆÎ Î‰ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ ÎŽÎ Â² */
	protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
	protected final Lock questRead = questLock.readLock();
	protected final Lock questWrite = questLock.writeLock();
	
	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);
		_isPendingRevive = false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<? extends Playable>) super.getRef();
	}
	
	public abstract Inventory getInventory();
	
	public abstract long getWearedMask();
	
	/**
	 * Check if the skill should trigger pvpflag to the player.<BR>
	 * <BR>
	 */
	@Override
	public boolean checkPvP(final Creature target, Skill skill)
	{
		Player player = getPlayer();
		
		// if (isDead() || (target == null) || (player == null) || (target == this) || (target == player) || (target == player.getPet()) || (player.getKarma() > 0))
		// {
		// return false;
		// }
		
		if (isDead())
		{
			return false;
		}
		
		if ((target == null) || (player == null))
		{
			return false;
		}
		
		if (target.equals(this) || target.equals(player) || target.equals(player.getPet()))
		{
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			return false;
		}
		
		if (skill != null)
		{
			if (skill.altUse())
			{
				return false;
			}
			if (skill.getTargetType() == SkillTargetType.TARGET_FEEDABLE_BEAST)
			{
				return false;
			}
			if (skill.getTargetType() == SkillTargetType.TARGET_UNLOCKABLE)
			{
				return false;
			}
			if (skill.getTargetType() == SkillTargetType.TARGET_CHEST)
			{
				return false;
			}
		}
		
		// Checking in a duel ... Member is not a duel flag
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if ((duelEvent != null) && (duelEvent == target.getEvent(DuelEvent.class)))
		{
			return false;
		}
		
		if (isInZonePeace() && target.isInZonePeace())
		{
			return false;
		}
		if (isInZoneBattle() && target.isInZoneBattle())
		{
			return false;
		}
		
		if (isInZonePvP() && target.isInZonePvP())
		{
			return false;
		}
		
		if (getPlayer().isInFightClub())
		{
			return false;
		}
		
		if ((isInZone(Zone.ZoneType.SIEGE)) && (target.isInZone(Zone.ZoneType.SIEGE)))
		{
			return false;
		}
		
		if ((skill == null) || skill.isOffensive())
		{
			if (target.getKarma() > 0)
			{
				return false;
			}
			else if (target.isPlayable())
			{
				return true;
			}
		}
		else if ((target.getPvpFlag() > 0) || (target.getKarma() > 0) || target.isMonster())
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the target can be attacked (for phys attack)
	 */
	public boolean checkTarget(Creature target)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return false;
		}
		
		if ((target == null) || target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		
		if (!isInRange(target, 2000L))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		
		if (!canOverrideCond(PcCondOverride.SKILL_TARGET_CONDITIONS) && target.isDoor() && !target.isAttackable(this))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		
		if (target.paralizeOnAttack(this))
		{
			if (Config.PARALIZE_ON_RAID_DIFF)
			{
				paralizeMe(target);
			}
			return false;
		}
		
		if ((target.isInvisible() && !canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)) || (getReflection() != target.getReflection()) || !GeoEngine.canSeeTarget(this, target, false))
		{
			player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}
		
		// Î â€”Î Â°Î Î�Î¡â‚¬Î ÂµÎ¡â€š Î Â½Î Â° Î Â°Î¡â€šÎ Â°Î ÎŠÎ¡Æ’ Î ÎŒÎ ÎˆÎ¡â‚¬Î Â½Î¡â€¹Î¡â€¦ NPC Î Â² Î ÎŽÎ¡ï¿½Î Â°Î Î„Î Â½Î ÎŽÎ Î‰ Î Â·Î ÎŽÎ Â½Î Âµ Î Â½Î Â° TW. Î ï¿½Î Â½Î Â°Î¡â€¡Î Âµ Î¡â€šÎ Â°Î ÎŠÎ ÎˆÎ ÎŒ Î¡ï¿½Î Î�Î ÎŽÎ¡ï¿½Î ÎŽÎ Â±Î ÎŽÎ ÎŒ Î Â½Î Â°Î Â±Î ÎˆÎ Â²Î Â°Î¡ï¿½Î¡â€š Î ÎŽÎ¡â€¡Î ÎŠÎ Îˆ.
		// if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
		// {
		// player.sendPacket(Msg.INVALID_TARGET);
		// return false;
		// }
		
		if (!canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && (player.isInZone(ZoneType.epic) != target.isInZone(ZoneType.epic)))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		
		if (target.isPlayable())
		{
			// You can not attack someone who is on the scene, if you were not in the arena
			if (!canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && (isInZoneBattle() != target.isInZoneBattle()))
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return false;
			}
			
			// If the target or the attacker is in a peaceful area - you can not attack
			if (!canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && (isInZonePeace() || target.isInZonePeace()))
			{
				player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			if (player.isInOlympiadMode() && !player.isOlympiadCompStart())
			{
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isBetray()
	{
		if (isSummon())
		{
			for (Effect e : getEffectList().getAllEffects())
			{
				if (e.getEffectType() == EffectType.Betray)
				{
					return true;
				}
			}
			
			return false;
		}
		return false;
	}
	
	@Override
	public void doAttack(Creature target)
	{
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!canOverrideCond(PcCondOverride.SKILL_TARGET_CONDITIONS) && (isSummon() || isPet()) && target.isPlayer() && (target.getPlayer() == getPlayer()) && !isBetray())
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.model.Playable.message1", player));
			player.sendActionFailed();
			sendActionFailed();
			return;
		}
		
		if (isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}
		
		if (player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("l2r.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player));
			return;
		}
		
		if (!checkTarget(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}
		
		// Interrupt if the target is not a duel duelist
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if ((duelEvent != null) && ((target.getEvent(DuelEvent.class) == null) || !target.getEvent(DuelEvent.class).equals(duelEvent)))
		{
			duelEvent.abortDuel(getPlayer());
		}
		
		WeaponTemplate weaponItem = getActiveWeaponItem();
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && (weaponItem != null) && ((weaponItem.getItemType() == WeaponType.BOW) || (weaponItem.getItemType() == WeaponType.CROSSBOW)))
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if (bowMpConsume > 0)
			{
				// cheap shot SA
				double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0., target, null);
				if ((chance > 0.0) && Rnd.chance(chance))
				{
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);
				}
				
				if (_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(Msg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}
				
				reduceCurrentMp(bowMpConsume, null);
			}
			
			if (!player.isPhantom() && !player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(player.getActiveWeaponInstance().getItemType() == WeaponType.BOW ? Msg.YOU_HAVE_RUN_OUT_OF_ARROWS : Msg.NOT_ENOUGH_BOLTS);
				player.sendActionFailed();
				return;
			}
		}
		
		super.doAttack(target);
	}
	
	@Override
	public void doCast(final Skill skill, final Creature target, boolean forceUse)
	{
		if (skill == null)
		{
			return;
		}
		
		if (!canOverrideCond(PcCondOverride.SKILL_TARGET_CONDITIONS) && (isSummon() || isPet()) && skill.isOffensive() && target.isPlayer() && (target.getPlayer() == getPlayer()) && (skill.getId() != 1380))
		{
			getPlayer().sendMessage(new CustomMessage("l2r.gameserver.model.Playable.message2", getPlayer()));
			getPlayer().sendActionFailed();
			sendActionFailed();
			return;
		}
		
		// Î ï¿½Î¡â‚¬Î ÂµÎ¡â‚¬Î¡â€¹Î Â²Î Â°Î¡â€šÎ¡ï¿½ Î Î„Î¡Æ’Î¡ï¿½Î Â»Î Îˆ Î ÂµÎ¡ï¿½Î Â»Î Îˆ Î¡â€ Î ÂµÎ Â»Î¡ï¿½ Î Â½Î Âµ Î Î„Î¡Æ’Î¡ï¿½Î Â»Î¡ï¿½Î Â½Î¡â€š
		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if ((duelEvent != null) && (target.getEvent(DuelEvent.class) != duelEvent))
		{
			duelEvent.abortDuel(getPlayer());
		}
		
		// Î Â½Î ÂµÎ Â»Î¡ï¿½Î Â·Î¡ï¿½ Î ÎˆÎ¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î ÎŽÎ Â²Î Â°Î¡â€šÎ¡ï¿½ Î ÎŒÎ Â°Î¡ï¿½Î¡ï¿½ Î¡ï¿½Î ÎŠÎ ÎˆÎ Â»Î Â»Î¡â€¹ Î Â² Î ÎŒÎ ÎˆÎ¡â‚¬Î Â½Î ÎŽÎ Î‰ Î Â·Î ÎŽÎ Â½Î Âµ
		if (!canOverrideCond(PcCondOverride.SKILL_TARGET_CONDITIONS))
		{
			if (skill.isAoE() && isInPeaceZone())
			{
				getPlayer().sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
				return;
			}
			
			if ((skill.getSkillType() == SkillType.DEBUFF) && target.isNpc() && target.isInvul() && !target.isMonster() && !target.isInCombat() && (target.getPvpFlag() == 0))
			{
				getPlayer().sendPacket(Msg.INVALID_TARGET);
				return;
			}
		}
		
		super.doCast(skill, target, forceUse);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if ((attacker == null) || isDead() || (attacker.isDead() && !isDot))
		{
			return;
		}
		
		if (isDamageBlocked() && transferDamage)
		{
			return;
		}
		
		if (isDamageBlocked() && !attacker.equals(this))
		{
			if (sendMessage)
			{
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			return;
		}
		
		if (!attacker.equals(this) && attacker.isPlayable())
		{
			Player player = getPlayer();
			Player pcAttacker = attacker.getPlayer();
			if (!pcAttacker.equals(player))
			{
				if (player.isInOlympiadMode() && !player.isOlympiadCompStart())
				{
					if (sendMessage)
					{
						pcAttacker.sendPacket(Msg.INVALID_TARGET);
					}
					return;
				}
			}
			
			if (isInZoneBattle() != attacker.isInZoneBattle())
			{
				if (sendMessage)
				{
					attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
				}
				return;
			}
			
			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if ((duelEvent != null) && (attacker.getEvent(DuelEvent.class) != duelEvent))
			{
				duelEvent.abortDuel(player);
			}
		}
		
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, true, false);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, false, false);
	}
	
	public boolean isCtrlAttackable(Creature attacker, boolean force, boolean witchCtrl)
	{
		Player player = getPlayer();
		if ((attacker == null) || (player == null) || attacker.equals(this))
			return false;

		if (attacker.equals(this) || attacker.equals(player) && !force)
			return false;

		if (isAlikeDead() || attacker.isAlikeDead())
			return false;

		if (isInvisible() || !getReflection().equals(attacker.getReflection()))
			return false;

		if (isInBoat())
			return false;

		if (getNonAggroTime() > System.currentTimeMillis())
			return false;

		Player pcAttacker = attacker.getPlayer();
		if ((pcAttacker != null) && !pcAttacker.equals(player))
		{
			if (pcAttacker.isInOlympiadMode())
				return true;

			// Synerge - Check if the target is autoattackable in a GM Event
			switch (GmEventManager.getInstance().isAutoAttackable(pcAttacker, getPlayer()))
			{
				case GmEventManager.RETURN_FALSE:
					return false;
				case GmEventManager.RETURN_TRUE:
					return true;
			}

			for (GlobalEvent e : getEvents())
			{
				if (e.checkForAttack(this, attacker, null, force) != null)
				{
					return false;
				}

				// Synerge - Converted can attack condition to a three way comparison, so we can always know if he can or not hit the target, or should not be checked
				final Boolean canAttack = e.canAttack(this, attacker, null, force);
				if (canAttack != null)
				{
					if (canAttack == Boolean.FALSE)
						return false;
					else
						return true;
				}
			}

			if (!attacker.getPermissions().canAttack(this, null, force, false))
				return false;
			if (attacker.getPermissions().canIgnoreAttackBlockades(this, null, force))
				return true;

			if (pcAttacker.isInBoat())
				return false;

			if ((pcAttacker.getBlockCheckerArena() > -1) || (player.getBlockCheckerArena() > -1))
				return false;

			// Player with lvl < 21 can't attack a cursed weapon holder, and a cursed weapon holder can't attack players with lvl < 21
			if ((pcAttacker.isCursedWeaponEquipped() && (player.getLevel() < 21)) || (player.isCursedWeaponEquipped() && (pcAttacker.getLevel() < 21)))
				return false;

			if (player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic))
				return false;

			if ((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && (!player.isInOlympiadMode() || !pcAttacker.isInOlympiadMode() || !player.getOlympiadGame().equals(pcAttacker.getOlympiadGame())))
				return false;
			if (player.isInOlympiadMode() && !player.isOlympiadCompStart())
				return false;
			if (player.isInOlympiadMode() && player.isOlympiadCompStart() && (player.getOlympiadSide() == pcAttacker.getOlympiadSide()) && !force)
				return false;

			if (player.isInDuel() && pcAttacker.isInDuel() && (player.getEvent(DuelEvent.class) == pcAttacker.getEvent(DuelEvent.class)))
			{
				if (player.getTeam() != pcAttacker.getTeam())
					return true;
			}

			if (isInZonePeace())
				return false;

			if (getTeam() != TeamType.NONE && pcAttacker.getTeam() != TeamType.NONE)
				return getTeam() != pcAttacker.getTeam();
			if (!force && player.getPlayerGroup() == pcAttacker.getPlayerGroup()) // Self, Party and Command Channel check.
				return false;
			if (!player.isInOlympiadMode()) // To prevent the need for ctrl to attack clan members in oly.
			{
				if (!force && (player.getClan() != null) && player.getClan().equals(pcAttacker.getClan()))
					return false;
				if (!force && (player.getAllyId() != 0) && (player.getAllyId() == pcAttacker.getAllyId()))
					return false;
			}

			if (isInZoneBattle())
				return true;
			if (isInZonePvP())
				return true;
			if (isInZone(ZoneType.SIEGE))
				return true;

			if (pcAttacker.atMutualWarWith(player))
				return true;
			if ((player.getKarma() > 0) || (player.getPvpFlag() != 0))
				return true;
			if (witchCtrl && (player.getPvpFlag() > 0))
				return true;
			if (player.getPvpFlag() == 0 && pcAttacker.getLevel() - player.getLevel() > Config.ALT_LEVEL_DIFFERENCE_PROTECTION)
				return false;

			return force;
		}

		return true;
	}
	
//	public boolean isCtrlAttackable(Creature attacker, boolean force, boolean witchCtrl)
//	{
//		Player player = getPlayer();
//		if ((attacker == null) || (player == null) || attacker.equals(this))
//		{
//			return false;
//		}
//		
//		if (attacker.equals(this) || (attacker.equals(player) && !force))
//		{
//			return false;
//		}
//		
//		if (isAlikeDead() || attacker.isAlikeDead())
//		{
//			return false;
//		}
//		
//		if (isInvisible() || !getReflection().equals(attacker.getReflection()))
//		{
//			return false;
//		}
//		
//		if (isInBoat())
//		{
//			return false;
//		}
//		
//		if (getNonAggroTime() > System.currentTimeMillis())
//		{
//			return false;
//		}
//		
//		for (GlobalEvent e : getEvents())
//		{
//			if (e.checkForAttack(this, attacker, null, force) != null)
//				return false;
//		}
//		
//		for (GlobalEvent e : player.getEvents())
//		{
//			if (e.canAttack(this, attacker, null, force))
//				return true;
//		}
//
//		
//		Player pcAttacker = attacker.getPlayer();
//		if ((pcAttacker != null) && !pcAttacker.equals(player))
//		{
//			if (pcAttacker.isInOlympiadMode())
//			{
//				return true;
//			}
//			
//			// // Synerge - Check if the target is autoattackable in a GM Event
//			// switch (GmEventManager.getInstance().isAutoAttackable(pcAttacker, getPlayer()))
//			// {
//			// case GmEventManager.RETURN_FALSE:
//			// return false;
//			// case GmEventManager.RETURN_TRUE:
//			// return true;
//			// }
//			
////			for (GlobalEvent e : getEvents())
////			{
////				if (e.checkForAttack(this, attacker, null, force) != null)
////				{
////					return false;
////				}
////				
////				// Synerge - Converted can attack condition to a three way comparison, so we can always know if he can or not hit the target, or should not be checked
////				final Boolean canAttack = e.canAttack(this, attacker, null, force);
////				if (canAttack != null)
////				{
////					if (canAttack == Boolean.FALSE)
////					{
////						return false;
////					}
////					return true;
////				}
////			}
////			
//			if (!attacker.getPermissions().canAttack(this, null, force, false))
//			{
//				return false;
//			}
//			if (attacker.getPermissions().canIgnoreAttackBlockades(this, null, force))
//			{
//				return true;
//			}
//			
//			if (pcAttacker.isInBoat())
//			{
//				return false;
//			}
//			
//			if ((pcAttacker.getBlockCheckerArena() > -1) || (player.getBlockCheckerArena() > -1))
//			{
//				return false;
//			}
//			
//			// Player with lvl < 21 can't attack a cursed weapon holder, and a cursed weapon holder can't attack players with lvl < 21
//			if ((pcAttacker.isCursedWeaponEquipped() && (player.getLevel() < 21)) || (player.isCursedWeaponEquipped() && (pcAttacker.getLevel() < 21)))
//			{
//				return false;
//			}
//			
//			if (player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic))
//			{
//				return false;
//			}
//			
//			if ((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && (!player.isInOlympiadMode() || !pcAttacker.isInOlympiadMode() || !player.getOlympiadGame().equals(pcAttacker.getOlympiadGame())))
//			{
//				return false;
//			}
//			if (player.isInOlympiadMode() && !player.isOlympiadCompStart())
//			{
//				return false;
//			}
//			if (player.isInOlympiadMode() && player.isOlympiadCompStart() && (player.getOlympiadSide() == pcAttacker.getOlympiadSide()) && !force)
//			{
//				return false;
//			}
//			
//			if (player.isInDuel() && pcAttacker.isInDuel() && (player.getEvent(DuelEvent.class) == pcAttacker.getEvent(DuelEvent.class)))
//			{
//				if (player.getTeam() != pcAttacker.getTeam())
//				{
//					return true;
//				}
//			}
//			
//			if ((pcAttacker.getTeamEvents() > 0) && pcAttacker.isChecksForTeam() && (player.getTeamEvents() == 0))
//			{
//				return false;
//			}
//			if ((player.getTeamEvents() > 0) && player.isChecksForTeam() && (pcAttacker.getTeamEvents() == 0))
//			{
//				return false;
//			}
//			if ((player.getTeamEvents() > 0) && player.isChecksForTeam() && (pcAttacker.getTeamEvents() > 0) && pcAttacker.isChecksForTeam() && (player.getTeamEvents() == pcAttacker.getTeamEvents()))
//			{
//				return false;
//			}
//			
//			if (isInZonePeace() && !player.isTerritoryFlagEquipped())
//			{
//				return false;
//			}
//			
//			if ((getTeam() != TeamType.NONE) && (pcAttacker.getTeam() != TeamType.NONE))
//			{
//				return getTeam() != pcAttacker.getTeam();
//			}
//			if (!force && (player.getPlayerGroup() == pcAttacker.getPlayerGroup()))
//			{
//				return false;
//			}
//			if (!player.isInOlympiadMode()) // To prevent the need for ctrl to attack clan members in oly.
//			{
//				if (!force && (player.getClan() != null) && player.getClan().equals(pcAttacker.getClan()))
//				{
//					return false;
//				}
//				if (!force && (player.getAllyId() != 0) && (player.getAllyId() == pcAttacker.getAllyId()))
//				{
//					return false;
//				}
//			}
//			
//			if (isInZoneBattle())
//			{
//				return true;
//			}
//			// if (isInZonePvP())
//			// return true;
//			if (isInZone(ZoneType.SIEGE))
//			{
//				return true;
//			}
//			
//			if (pcAttacker.atMutualWarWith(player))
//			{
//				return true;
//			}
//			if ((player.getKarma() > 0) || (player.getPvpFlag() != 0))
//			{
//				return true;
//			}
//			if (witchCtrl && (player.getPvpFlag() > 0))
//			{
//				return true;
//			}
//			if ((player.getPvpFlag() == 0) && ((pcAttacker.getLevel() - player.getLevel()) > Config.ALT_LEVEL_DIFFERENCE_PROTECTION))
//			{
//				return false;
//			}
//			
//			return force;
//		}
//		
//		return true;
//	}
	
	@Override
	public int getKarma()
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}
	
	@Override
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		// Skill skill = env.skill;
		Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (useActionSkills && !skill.altUse() && !skill.getSkillType().equals(SkillType.BEAST_FEED))
		{
			for (Creature target : targets)
			{
				if (target.isNpc())
				{
					if (skill.isOffensive())
					{
						// mobs will hate on debuff
						if (target.paralizeOnAttack(player))
						{
							if (Config.PARALIZE_ON_RAID_DIFF)
							{
								paralizeMe(target);
							}
							return;
						}
						if (!skill.isAI())
						{
							int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
						}
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
				}
				else if (target.isPlayable() && (target != getPet()) && !((isSummon() || isPet()) && (target == player)))
				{
					int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());
					
					// List<NpcInstance> npcs = World.getAroundNpc(target, 2000, 300);
					List<NpcInstance> npcs = World.getAroundNpc(target);
					for (NpcInstance npc : npcs)
					{
						if (npc.isDead() || !npc.isInRangeZ(this, 2000L))
						{
							continue;
						}
						
						npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
						
						AggroInfo ai = npc.getAggroList().get(target);
						
						// Skip if the target is not in the hatlist
						if (ai == null)
						{
							continue;
						}
						
						if (!skill.isHandler() && npc.paralizeOnAttack(player))
						{
							if (Config.PARALIZE_ON_RAID_DIFF)
							{
								paralizeMe(npc);
							}
							return;
						}
						
						// If the hate is less than 100, skip
						if (ai.hate < 100)
						{
							continue;
						}
						
						if (GeoEngine.canSeeTarget(npc, target, false))
						{
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
						}
					}
				}
				
				// Check for PvP Flagging / Drawing Aggro
				if (checkPvP(target, skill))
				{
					startPvPFlag(target);
				}
			}
		}
		
		super.callSkill(skill, targets, useActionSkills);
	}
	
	/**
	 * Î ï¿½Î Î�Î ÎŽÎ Â²Î ÂµÎ¡â€°Î Â°Î ÂµÎ¡â€š Î Î„Î¡â‚¬Î¡Æ’Î Â³Î ÎˆÎ¡â€¦ Î ÎˆÎ Â³Î¡â‚¬Î ÎŽÎ ÎŠÎ ÎŽÎ Â² Î ÎŽ Î Î�Î ÎŽÎ Î„Î Â½Î¡ï¿½Î¡â€šÎ ÎˆÎ Îˆ Î Â²Î ÂµÎ¡â€°Î Îˆ
	 * @param item Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€š Î ÎŠÎ ÎŽÎ¡â€šÎ ÎŽÎ¡â‚¬Î¡â€¹Î Î‰ Î Â±Î¡â€¹Î Â» Î Î�Î ÎŽÎ Î„Î Â½Î¡ï¿½Î¡â€š
	 */
	public void broadcastPickUpMsg(ItemInstance item)
	{
		Player player = getPlayer();
		
		if ((item == null) || (player == null) || (player.isInvisible() && player.isGM()))
		{
			return;
		}
		
		if (item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate))
		{
			SystemMessage msg = null;
			// String player_name = player.getName();
			if (item.getEnchantLevel() > 0)
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP__S2_S3 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player.getVisibleName(this)).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP_S2 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player.getVisibleName(this)).addItemName(item.getItemId());
			}
			player.broadcastPacket(msg.setInvisible(isInvisible()));
		}
	}
	
	public void paralizeMe(Creature effector)
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(Skill.SKILL_RAID_CURSE, 1);
		revengeSkill.getEffects(effector, this, false, false);
	}
	
	public final void setPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}
	
	/** Sets HP, MP and CP and revives the L2Playable. */
	public void doRevive()
	{
		if (!isTeleporting())
		{
			setPendingRevive(false);
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
			
			if (isSalvation() || isPlayer() && getPlayer().isInFightClub())
			{
				for (Effect e : getEffectList().getAllEffects())
				{
					if (e.getEffectType() == EffectType.Salvation)
					{
						e.exit();
						break;
					}
				}
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
				setCurrentCp(getMaxCp());
			}
			else
			{
				setCurrentHp(Math.max(1, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);
				if (isPlayer() && (Config.RESPAWN_RESTORE_CP > 0))
				{
					setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				}
				
				if (Config.RESPAWN_RESTORE_MP >= 0)
				{
					setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
				}
			}
			
			broadcastPacket(new Revive(this));
		}
		else
		{
			setPendingRevive(true);
		}
	}
	
	public abstract L2GameServerPacket getPartyStatusUpdatePacket();
	
	public abstract void doPickupItem(GameObject object);
	
	public void sitDown(StaticObjectInstance throne, boolean... force)
	{
	}
	
	public void standUp()
	{
	}
	
	private long _nonAggroTime;
	
	public long getNonAggroTime()
	{
		return _nonAggroTime;
	}
	
	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}
	
	/**
	 * @return Previous condition
	 */
	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}
	
	/**
	 * @return Current condition
	 */
	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}
	
	/**
	 * @return True if the Silent Moving mode is active.<BR>
	 *         <BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}
	
	public boolean isInCombatZone()
	{
		return isInZoneBattle();
	}
	
	public boolean isInPeaceZone()
	{
		return isInZonePeace();
	}
	
	@Override
	public boolean isInZoneBattle()
	{
		return super.isInZoneBattle();
	}
	
	public boolean isOnSiegeField()
	{
		return isInZone(ZoneType.SIEGE);
	}
	
	public boolean isInSSQZone()
	{
		return isInZone(ZoneType.ssq_zone);
	}
	
	public boolean isInDangerArea()
	{
		return isInZone(ZoneType.damage) || isInZone(ZoneType.swamp) || isInZone(ZoneType.poison) || isInZone(ZoneType.instant_skill) || isInZone(ZoneType.world_pvp);
	}
	
	public int getMaxLoad()
	{
		return 0;
	}
	
	public int getInventoryLimit()
	{
		return 0;
	}
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}
	
	@Override
	public PlayablePermissionList getPermissions()
	{
		if (getPlayer()._permissions == null)
		{
			synchronized (this)
			{
				if (getPlayer()._permissions == null)
				{
					getPlayer()._permissions = new PlayablePermissionList(this);
				}
			}
		}
		return (PlayablePermissionList) getPlayer()._permissions;
	}
	
	@Override
	public boolean canOverrideCond(PcCondOverride excs)
	{
		if ((isSummon() || isPet()) && (getPlayer() != null))
		{
			return getPlayer().canOverrideCond(excs);
		}
		
		return super.canOverrideCond(excs);
	}


	@Override
	public int getPAtkSpd()
	{
		return Math.max((int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE,_template.getBasePAtkSpd(), null, null), null, null), 1);
	}

	@Override
	public int getPAtk(Creature target)
	{
		double init = getActiveWeaponInstance() == null ? _template.getBasePAtk() : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		if ((skill != null) && (skill.getMatak() > 0))
		{
			return skill.getMatak();
		}
		final double init = getActiveWeaponInstance() == null ? _template.getBaseMAtk() : 0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}

}