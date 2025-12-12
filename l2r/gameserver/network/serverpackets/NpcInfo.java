package l2r.gameserver.network.serverpackets;

import org.apache.commons.lang3.StringUtils;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.pledge.Alliance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.utils.Location;

public class NpcInfo extends L2GameServerPacket
{
	private boolean can_writeImpl = false;
	private int _npcObjId;
	private final int _npcId;
	private int running;
	private int incombat;
	private int dead;
	private int _showSpawnAnimation;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flyRunSpd, _flyWalkSpd, _mAtkSpd, _pAtkSpd;
	private final int _rhand;
	private final int _lhand;
	private final int _enchantEffect;
	private int karma, pvp_flag, _abnormalEffect, _abnormalEffect2, clan_id, clan_crest_id, ally_id, ally_crest_id, _formId, _titleColor;
	private double move_mult, attack_mult, colHeight, colRadius, currentColHeight, currentColRadius;
	private final boolean _isAttackable;
	private boolean _isNameAbove;
	private boolean isFlying;
	private Location _loc;
	private String _name = StringUtils.EMPTY;
	private String _title = StringUtils.EMPTY;
	private final boolean _showName;
	private int _state;
	private NpcString _nameNpcString = NpcString.NONE;
	private NpcString _titleNpcString = NpcString.NONE;
	private TeamType _team;
	
	public NpcInfo(NpcInstance cha, Creature attacker)
	{
		_npcId = cha.getDisplayId() != 0 ? cha.getDisplayId() : cha.getTemplate().npcId;
		_isAttackable = (attacker != null) && cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_enchantEffect = (cha.getChampionTemplate() != null ? cha.getChampionTemplate().weaponEnchant : 0);
		
		if (Config.SERVER_SIDE_NPC_NAME || (cha.getTemplate().displayId != 0) || (cha.getName() != cha.getTemplate().name))
		{
			_name = cha.getName();
		}
		if (!cha.isRaid() && (Config.SERVER_SIDE_NPC_TITLE || (cha.getTemplate().displayId != 0) || (cha.getTitle() != cha.getTemplate().title)))
		{
			_title = cha.getTitle();
			if (Config.SERVER_SIDE_NPC_TITLE_ETC)
			{
				if (cha.isMonster() && cha.canShowLevelInTitle())
				{
					if (_title.isEmpty())
					{
						_title = "Lv. " + cha.getLevel();
						if (cha.isAggressive())
						{
							_title += "*";
						}
					}
				}
			}
		}
		_showSpawnAnimation = cha.getSpawnAnimation();
		_showName = cha.isShowName();
		_state = cha.getNpcState();
		_nameNpcString = ((cha.getTemplate().displayId != 0) || (cha.getName() != cha.getTemplate().name)) ? NpcString.NONE : cha.getNameNpcString();
		_titleNpcString = ((cha.getTemplate().displayId != 0) || (cha.getName() != cha.getTemplate().name)) ? NpcString.NONE : cha.getTitleNpcString();
		
		common(cha);
	}
	
	public NpcInfo(Summon cha, Creature attacker)
	{
		if ((cha.getPlayer() != null) && cha.getPlayer().isInvisible(attacker))
		{
			setInvisible(true);
		}
		
		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = 0;
		_lhand = 0;
		_enchantEffect = 0;
		_showName = true;
		_name = cha.getVisibleName();
		_title = cha.isInvisible() ? "Invisible" : cha.getTitle();
		_showSpawnAnimation = cha.getSpawnAnimation();
		
		common(cha);
	}
	
	private void common(Creature cha)
	{
		colHeight = cha.getTemplate().getCollisionHeight();
		colRadius = cha.getTemplate().getCollisionRadius();
		currentColHeight = cha.getColHeight();
		currentColRadius = cha.getColRadius();
		_npcObjId = cha.getObjectId();
		_loc = cha.getLoc();
		_mAtkSpd = cha.getMAtkSpd();
		//
		Clan clan = cha.getVisibleClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		//
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		
		move_mult = cha.isRunning() ? cha.getRunSpeedMultiplier() : cha.getWalkSpeedMultiplier();
		attack_mult = cha.getAttackSpeedMultiplier();
		_runSpd = (int) (cha.getRunSpeed() / cha.getRunSpeedMultiplier());
		_walkSpd = (int) (cha.getWalkSpeed() / cha.getWalkSpeedMultiplier());
		_swimRunSpd = (int) (cha.getRunSpeed() / cha.getRunSpeedMultiplier());
		_swimWalkSpd = (int) (cha.getWalkSpeed() / cha.getWalkSpeedMultiplier());
		_flyRunSpd = (int) (cha.getRunSpeed() / cha.getRunSpeedMultiplier());
		_flyWalkSpd = (int) (cha.getWalkSpeed() / cha.getWalkSpeedMultiplier());
		karma = cha.getKarma();
		pvp_flag = cha.getPvpFlag();
		_pAtkSpd = cha.getPAtkSpd();
		running = cha.isRunning() ? 1 : 0;
		incombat = cha.isInCombat() ? 1 : 0;
		dead = cha.isAlikeDead() ? 1 : 0;
		_abnormalEffect = cha.getAbnormalEffect();
		_abnormalEffect2 = cha.getAbnormalEffect2();
		isFlying = cha.isFlying();
		_team = cha.getTeam();
		if (cha.getChampionTemplate() != null)
		{
			if (cha.getChampionTemplate().blueCircle)
			{
				_team = TeamType.BLUE;
			}
			else if (cha.getChampionTemplate().redCircle)
			{
				_team = TeamType.RED;
			}
		}
		_formId = cha.getFormId();
		_isNameAbove = cha.isNameAbove();
		_titleColor = (cha.isSummon() || cha.isPet()) ? 1 : 0;
		
		can_writeImpl = true;
	}
	
	public NpcInfo update()
	{
		_showSpawnAnimation = 1;
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (!can_writeImpl)
		{
			return;
		}
		
		writeC(0x0c);
		// ddddddddddddddddddffffdddcccccSSddddddddccffddddccd
		writeD(_npcObjId);
		writeD(_npcId + 1000000); // npctype id c4
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/* 0x32 */); // swimspeed
		writeD(_swimWalkSpd/* 0x32 */); // swimspeed
		writeD(0/* _flRunSpd */);
		writeD(0/* _flWalkSpd */);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(move_mult); // 1.100000023841858 Ð²Ð·Ñ�Ñ‚Ð¾ Ð¸Ð· ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð°
		writeF(attack_mult); // _pAtkSpd / 277.478340719
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand); // right hand weapon
		writeD(0); // TODO chest
		writeD(_lhand); // left hand weapon
		writeC(_isNameAbove ? 1 : 0); // 2.2: name above char 1=true ... ??; 2.3: 1 - normal, 2 - dead
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeD(_nameNpcString.getId());
		writeS(_name);
		writeD(_titleNpcString.getId());
		writeS(_title);
		writeD(_titleColor); // 0- Ñ�Ð²ÐµÑ‚Ð»Ð¾ Ð·ÐµÐ»ÐµÐ½Ñ‹Ð¹ Ñ‚Ð¸Ñ‚ÑƒÐ»(Ð¼Ð¾Ð±), 1 - Ñ�Ð²ÐµÑ‚Ð»Ð¾ Ñ�Ð¸Ð½Ð¸Ð¹(Ð¿ÐµÑ‚)/Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð¶ÐµÐ½Ð¸Ðµ Ñ‚ÐµÐºÑƒÑ‰ÐµÐ³Ð¾ ÐœÐŸ
		writeD(pvp_flag);
		writeD(karma); // hmm karma ??
		writeD(_abnormalEffect); // C2
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(isFlying ? 2 : 0); // C2
		writeC(_team.ordinal()); // team aura 1-blue, 2-red
		writeF(currentColRadius); // Ñ‚ÑƒÑ‚ Ñ‡Ñ‚Ð¾-Ñ‚Ð¾ Ñ�Ð²Ñ�Ð·Ð°Ð½Ð½Ð¾Ðµ Ñ� colRadius
		writeF(currentColHeight); // Ñ‚ÑƒÑ‚ Ñ‡Ñ‚Ð¾-Ñ‚Ð¾ Ñ�Ð²Ñ�Ð·Ð°Ð½Ð½Ð¾Ðµ Ñ� colHeight
		writeD(_enchantEffect); // C4
		writeD(0x00); // writeD(_npc.isFlying() ? 1 : 0); // C6
		writeD(0x00);
		writeD(_formId);// great wolf type
		writeC(_showName ? 0x01 : 0x00); // show name
		writeC(_showName ? 0x01 : 0x00); // show title
		writeD(_abnormalEffect2);
		writeD(_state);
	}
}