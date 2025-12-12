package l2r.gameserver.network.serverpackets;

import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.events.impl.AbstractFightClub;
import l2r.gameserver.templates.npc.NpcTemplate;

public class NpcInfoPoly extends L2GameServerPacket
{
	// ddddddddddddddddddffffdddcccccSSddd dddddccffddddccd
	private final Creature _obj;
	private final int _objId, _x, _y, _z, _heading;
	private final int _npcId;
	private final boolean _isSummoned, _isRunning, _isInCombat, _isAlikeDead;
	private final int _mAtkSpd, _pAtkSpd;
	private final int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _rhand, _lhand;
	private String _name, _title;
	private final int _abnormalEffect, _abnormalEffect2;
	private final double colRadius, colHeight;
	private final TeamType _team;
	
	public NpcInfoPoly(Player cha)
	{
		_obj = cha;
		_objId = cha.getObjectId();
		_npcId = cha.getPolyId();
		NpcTemplate template = NpcHolder.getInstance().getTemplate(_npcId);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = false;
		colRadius = template.getCollisionRadius();
		colHeight = template.getCollisionHeight();
		_x = _obj.getX();
		_y = _obj.getY();
		_z = _obj.getZ();
		_rhand = template.rhand;
		_lhand = template.lhand;
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_runSpd = (int) cha.getRunSpeed();
		_walkSpd = (int) cha.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_isRunning = cha.isRunning();
		_isInCombat = cha.isInCombat();
		_isAlikeDead = cha.isAlikeDead();
		_name = cha.getVisibleName();
		_title = cha.getVisibleTitle();
		_abnormalEffect = cha.getAbnormalEffect();
		_abnormalEffect2 = cha.getAbnormalEffect2();
		_team = cha.getTeam();
		
		if (cha.isInFightClub())
		{
			AbstractFightClub fightClubEvent = cha.getFightClubEvent();
			_name = fightClubEvent.getVisibleName(cha, _name, false);
			_title = fightClubEvent.getVisibleTitle(cha, _title, false);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		// Vars that need activeChar.
		_name = _obj.getVisibleName(getClient().getActiveChar());
		_title = _obj.getVisibleTitle(getClient().getActiveChar());
		
		writeC(0x0c);
		writeD(_objId);
		writeD(_npcId + 1000000); // npctype id
		writeD(0x00);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/* 0x32 */); // swimspeed
		writeD(_swimWalkSpd/* 0x32 */); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1/* _cha.getProperMultiplier() */);
		writeF(1/* _cha.getAttackSpeedMultiplier() */);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(_isRunning ? 1 : 0);
		writeC(_isInCombat ? 1 : 0);
		writeC(_isAlikeDead ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0000); // hmm karma ??
		
		writeD(_abnormalEffect);
		
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
		writeC(_team.ordinal());
		writeF(colRadius); // Ñ‚ÑƒÑ‚ Ñ‡Ñ‚Ð¾-Ñ‚Ð¾ Ñ�Ð²Ñ�Ð·Ð°Ð½Ð½Ð¾Ðµ Ñ� colRadius
		writeF(colHeight); // Ñ‚ÑƒÑ‚ Ñ‡Ñ‚Ð¾-Ñ‚Ð¾ Ñ�Ð²Ñ�Ð·Ð°Ð½Ð½Ð¾Ðµ Ñ� colHeight
		writeD(0x00); // C4
		writeD(0x00); // ÐºÐ°Ðº-Ñ‚Ð¾ Ñ�Ð²Ñ�Ð·Ð°Ð½Ð¾ Ñ� Ð²Ñ‹Ñ�Ð¾Ñ‚Ð¾Ð¹
		writeD(0x00);
		writeD(0x00); // maybe show great wolf type ?
		
		writeC(0x00); // ?GraciaFinal
		writeC(0x00); // ?GraciaFinal
		writeD(_abnormalEffect2);
	}
}