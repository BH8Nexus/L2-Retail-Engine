package l2r.gameserver.network.clientpackets;

import org.apache.commons.lang3.tuple.Pair;

import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.custom.GmEventManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.entity.residence.ResidenceFunction;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.Die;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.TeleportUtils;
import l2r.gameserver.utils.Util;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;
	
	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if ((_restartType == null) || (activeChar == null))
		{
			return;
		}
		
		if (activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}
		
		if (!activeChar.isDead() && !activeChar.isGM())
		{
			Util.handleIllegalPlayerAction(activeChar, "RequestRestartPoint[55]", "Tried to use revive cheat", 1);
			activeChar.sendActionFailed();
			return;
		}
		
		// Prims - If the player is in a Gm Event check if it can resurrect
		if (!GmEventManager.getInstance().canResurrect(activeChar))
		{
			return;
		}
		
		if (activeChar.isFestivalParticipant())
		{
			activeChar.doRevive();
			return;
		}
		
		switch (_restartType)
		{
			case AGATHION:
				if (activeChar.isAgathionResAvailable())
				{
					activeChar.doRevive(100);
				}
				else
				{
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				}
				break;
			case FIXED:
				// if (activeChar.getPlayerAccess().ResurectFixed)
				if (activeChar.getAccessLevel().allowFixedRes())
				{
					activeChar.doRevive(100);
				}
				else if (ItemFunctions.removeItem(activeChar, 13300, 1, true, "RequestRestartPoint") == 1)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else if (ItemFunctions.removeItem(activeChar, 10649, 1, true, "RequestRestartPoint") == 1)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else
				{
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				}
				break;
			default:
				Location loc = null;
				Reflection ref = activeChar.getReflection();
				
				if (ref == ReflectionManager.DEFAULT)
				{
					for (GlobalEvent e : activeChar.getEvents())
					{
						loc = e.getRestartLoc(activeChar, _restartType);
					}
				}
				
				if (loc == null)
				{
					loc = defaultLoc(_restartType, activeChar);
				}
				
				if (activeChar.isInFightClub())
				{
					activeChar.getFightClubEvent().requestRespawn(activeChar, _restartType);
					return;
				}
				
				if (loc != null)
				{
					Pair<Integer, OnAnswerListener> ask = activeChar.getAskListener(false);
					if ((ask != null) && (ask.getValue() instanceof ReviveAnswerListener) && !((ReviveAnswerListener) ask.getValue()).isForPet())
					{
						activeChar.getAskListener(true);
					}
					
					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
				}
				else
				{
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				}
				break;
		}
	}
	
	// FIXME [VISTALL] Π²Ρ‹Π½ΠµΡ�Ρ‚ΠΈ ΠΊΡƒΠ΄Π° Ρ‚ΠΎ?
	// Ρ‚ΠµΠ»ΠµΠΏΠΎΡ€Ρ‚ ΠΊ Ρ„Π»Π°Π³Ρƒ, Π½Πµ ΠΎΠ±Ρ€Π°Π±Π°Ρ‚Ρ‹Π²Π°ΠµΡ‚Ρ�Ρ�, ΠΏΠΎ Π΄ΠµΡ„Π°Π»Ρ‚Ρƒ
	public static Location defaultLoc(RestartType restartType, Player activeChar)
	{
		Location loc = null;
		Clan clan = activeChar.getClan();
		
		switch (restartType)
		{
			case TO_CLANHALL:
				if ((clan != null) && (clan.getHasHideout() != 0))
				{
					ClanHall clanHall = activeChar.getClanHall();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
					if (clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
					{
						activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
					}
				}
				break;
			case TO_CASTLE:
				if ((clan != null) && (clan.getCastle() != 0))
				{
					Castle castle = activeChar.getCastle();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CASTLE);
					if (castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
					{
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
					}
				}
				break;
			case TO_FORTRESS:
				if ((clan != null) && (clan.getHasFortress() != 0))
				{
					Fortress fort = activeChar.getFortress();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_FORTRESS);
					if (fort.getFunction(ResidenceFunction.RESTORE_EXP) != null)
					{
						activeChar.restoreExp(fort.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
					}
				}
				break;
			case TO_VILLAGE:
			default:
				loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
				break;
		}
		return loc;
	}
}
