package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class Action extends L2GameClientPacket
{
	private int _objectId;
	private int _actionId;
	//@SuppressWarnings("unused")
	//private Location _clientLocation;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
//		_clientLocation = new Location(
//		readD(), // X
//		readD(), // Y
//		readD()); // Z
		readD();
		readD();
		readD();
		_actionId = readC();// 0 for simple click  1 for shift click
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		GameObject obj = activeChar.getVisibleObject(_objectId);
		if(obj == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setActive();
		
		if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != obj)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isLockedTarget())
		{
			if(activeChar.isClanAirShipDriver())
				activeChar.sendPacket(SystemMsg.THIS_ACTION_IS_PROHIBITED_WHILE_STEERING);

			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFrozen())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
			return;
		}
//
//
//		if (obj.isCreature() && !((Creature) obj).isDead() && activeChar.getTarget() == obj)
//		{
//			// Custom INSTANT_JUMP_ATTACK support. Will execute if cant execute rush attack on time.
//			int instantJumpAttack = (int) activeChar.calcStat(Stats.INSTANT_JUMP_ATTACK, 0);
//			int rushAttack = (int) activeChar.calcStat(Stats.RUSH_ATTACK, 0);
//			int dist = (int) activeChar.getDistance(obj);
//			if (instantJumpAttack > 0 && instantJumpAttack > rushAttack && rushAttack < dist && dist >= 100 && dist <= instantJumpAttack)
//			{
//				double radian = Location.convertHeadingToRadian(obj.getHeading());
//				Location loc = new Location(obj.getX() + (int) (Math.sin(radian) * 40), obj.getY() - (int) (Math.cos(radian) * 40), obj.getZ()).correctGeoZ();
//				if(GeoEngine.canMoveToCoord(activeChar.getX(), activeChar.getY(), activeChar.getZ(), loc.x, loc.y, loc.z, activeChar.getGeoIndex()))
//				{
//					activeChar.broadcastPacket(new FlyToLocation(activeChar, loc, FlyType.DUMMY));
//					activeChar.broadcastPacket(new MagicSkillUse(activeChar, (Creature)obj, 821, 1, 100, 0));
//					activeChar.setLoc(loc);
//					activeChar.validateLocation(1);
//				}
//			}
//
//			// Custom RUSH_ATTACK support. Will execute if in range
//			if ((instantJumpAttack < rushAttack || rushAttack >= dist) && rushAttack > 0 && dist >= 100 && dist <= rushAttack)
//			{
//				double radian = Location.convertHeadingToRadian(activeChar.getHeading());
//				Location loc = new Location(obj.getX() - (int) (Math.sin(radian) * 40), obj.getY() + (int) (Math.cos(radian) * 40), obj.getZ()).correctGeoZ();
//				if(GeoEngine.canMoveToCoord(activeChar.getX(), activeChar.getY(), activeChar.getZ(), loc.x, loc.y, loc.z, activeChar.getGeoIndex()))
//				{
//					activeChar.broadcastPacket(new MagicSkillUse(activeChar, (Creature)obj, 793, 1, 300, 0));
//					ThreadPoolManager.getInstance().schedule(() ->
//					{
//						activeChar.broadcastPacket(new FlyToLocation(activeChar, loc, FlyType.CHARGE));
//						activeChar.setLoc(loc);
//						activeChar.validateLocation(1);
//						obj.onAction(activeChar, _actionId == 1);
//					}, 300);
//
//					return;
//				}
//			}
//		}
//
		obj.onAction(activeChar, _actionId == 1);
	}
}