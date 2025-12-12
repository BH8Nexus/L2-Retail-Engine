package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.boat.Boat;
import l2r.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket
{
	private final int _moveSpeed, _rotationSpeed;
	private final int _boatObjId;
	private final Location _loc;
	
	public VehicleDeparture(Boat boat)
	{
		_boatObjId = boat.getObjectId();
		_moveSpeed = (int) boat.getMoveSpeed();
		_rotationSpeed = boat.getRotationSpeed();
		_loc = boat.getDestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6c);
		writeD(_boatObjId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}