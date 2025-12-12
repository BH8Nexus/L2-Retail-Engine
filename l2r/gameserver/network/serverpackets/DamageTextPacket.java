package l2r.gameserver.network.serverpackets;

public class DamageTextPacket extends L2GameServerPacket
{
	
	int _font_id;
	int _font_color;
	int _victim;
	int _damage;
	int _crit;
	int _miss;
	int _blocked;
	int _magic;
	String _customString;
	
	public DamageTextPacket(int victim_id, int damage, boolean crit, boolean miss, boolean blocked, boolean magic, int font_id, int font_color, String customString)
	{
		_font_id = font_id;
		_font_color = font_color;
		_victim = victim_id;
		_damage = damage;
		_crit = crit ? 1 : 0;
		_miss = miss ? 1 : 0;
		_blocked = blocked ? 1 : 0;
		_magic = magic ? 1 : 0;
		_customString = customString;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFF);
		writeC(8);
		
		writeD(_font_id);
		writeD(_font_color);
		
		writeD(_victim);
		writeD(_damage);
		
		writeC(_crit);
		writeC(_miss);
		writeC(_blocked);
		writeC(_magic);
		
		writeS(_customString);
	}
	
}