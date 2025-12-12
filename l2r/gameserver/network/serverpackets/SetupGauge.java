package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Creature;

public class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	
	private final int _charId;
	private final int _dat1;
	private final int _currentTime;
	private final int _maxTime;
	
	public SetupGauge(Creature character, int dat1, int currentTime)
	{
		_charId = character.getObjectId();
		_dat1 = dat1;// color 0-blue 1-red 2-cyan 3-
		_currentTime = currentTime;
		_maxTime = currentTime;
	}
	
	public SetupGauge(Creature character, int dat1, int currentTime, int maxTime)
	{
		_charId = character.getObjectId();
		_dat1 = dat1;// color 0-blue 1-red 2-cyan 3-
		_currentTime = currentTime;
		_maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6b);
		writeD(_charId);
		writeD(_dat1);
		writeD(_currentTime);
		writeD(_maxTime); // c2
	}
}