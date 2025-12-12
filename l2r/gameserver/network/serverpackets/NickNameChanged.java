package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Creature;

public class NickNameChanged extends L2GameServerPacket
{
	private final int objectId;
	private String title;
	private final Creature _cha;
	
	public NickNameChanged(Creature cha)
	{
		_cha = cha;
		objectId = cha.getObjectId();
		title = cha.getVisibleTitle();
	}
	
	@Override
	protected void writeImpl()
	{
		// Vars that need activeChar.
		title = _cha.getVisibleTitle(getClient().getActiveChar());
		
		writeC(0xCC);
		writeD(objectId);
		writeS(title);
	}
}