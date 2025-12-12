package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.templates.PlayerTemplate;

public class NewCharacterSuccess extends L2GameServerPacket
{
	// dddddddddddddddddddd
	private final List<PlayerTemplate> _chars = new ArrayList<>();
	
	public void addChar(PlayerTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0d);
		writeD(_chars.size());
		
		for (PlayerTemplate temp : _chars)
		{
			writeD(temp.race.ordinal());
			writeD(temp.classId.getId());
			writeD(0x46);
			writeD(temp.getBaseSTR());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseDEX());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseCON());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseINT());//baseINT
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseWIT());//baseWIT
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseMEN());//baseMEN
			writeD(0x0a);
		}
	}
}