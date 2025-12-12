package l2r.gameserver.network.serverpackets;

import l2r.gameserver.utils.Location;

/**
 * format   dddddd
 */
public class TargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _targetId;
	private final Location _loc;

	public TargetSelected(int objectId, int targetId, Location loc)
	{
		_objectId = objectId;
		_targetId = targetId;
		_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x23);
		writeD(_objectId);
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(0x00);
	}
}