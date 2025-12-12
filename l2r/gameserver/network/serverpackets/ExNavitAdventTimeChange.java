package l2r.gameserver.network.serverpackets;

public class ExNavitAdventTimeChange extends L2GameServerPacket
{
	private int _active;
	private int _time;

	public ExNavitAdventTimeChange(boolean active, int time)
	{
		this._active = active ? 1 : 0;
		this._time = 14400 - time;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(225);
		writeC(this._active);
		writeD(this._time);
	}
}
