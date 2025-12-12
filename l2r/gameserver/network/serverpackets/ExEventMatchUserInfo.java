package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;

public class ExEventMatchUserInfo extends L2GameServerPacket
{
	public ExEventMatchUserInfo()
	{
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(0x02);
		// TODO dSdddddddd
		Player player = World.getPlayer("Nik");
		writeD(player.getObjectId());
		writeS("lolol");
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}