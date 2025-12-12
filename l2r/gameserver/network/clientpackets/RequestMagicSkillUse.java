package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.items.attachment.FlagItemAttachment;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	/**
	 * packet type id 0x39 format: cddc
	 */
	@Override
	protected void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			getClient().sendPacket(ActionFail.STATIC);
			return;
		}
		activeChar.setActive();
		
//		if ((System.currentTimeMillis() - activeChar.getLastRequestMagicSkillUsePacket()) < Config.REQUESTMAGICSKILLUSEPACKETDELAY)
//		{
//			activeChar.sendActionFailed();
//			return;
//		}
//		activeChar.setLastRequestMagicSkillUsePacket();
//

		if (activeChar.isDead())
		{
			//activeChar.sendPacket(ActionFail.STATIC_)
			activeChar.sendActionFailed();
			return;
		}

		if (activeChar.isFakeDeath())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SITTING);
			activeChar.sendActionFailed();
			return;
		}


		if (activeChar.isOutOfControl() && !activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			activeChar.setMacroSkill(null);
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.getMacroSkill() != null)
		{
			this._magicId = activeChar.getMacroSkill().getId();
		}
		
		Skill skill = SkillTable.getInstance().getInfo(_magicId, activeChar.getSkillLevel(_magicId));
		
		if (activeChar.isPendingOlyEnd())
		{
			if ((skill != null) && (skill.isOffensive()))
			{
				activeChar.setMacroSkill(null);
				activeChar.sendActionFailed();
				return;
			}
		}
		if (skill != null)
		{
			if (!(skill.isActive() || skill.isToggle()))
			{
				activeChar.setMacroSkill(null);
				return;
			}
			
			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if (!activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && (skill.getSkillType() == SkillType.RECALL) && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0))
			{
				
				return;
			}
			
			FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
			if ((attachment != null) && !attachment.canCast(activeChar, skill))
			{
				activeChar.setMacroSkill(null);
				activeChar.sendActionFailed();
				return;
			}
			
			if (!activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
			{
				// Ð’ Ñ€ÐµÐ¶Ð¸Ð¼Ðµ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸ Ð´Ð¾Ñ�Ñ‚ÑƒÐ¿Ð½Ñ‹ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ñ�ÐºÐ¸Ð»Ñ‹ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ñ‹
				if (((activeChar.getTransformation() != 0) && !activeChar.getAllSkills().contains(skill)))
				{
					activeChar.setMacroSkill(null);
					activeChar.sendPacket(ActionFail.STATIC);
					return;
				}
			}
			
			if (skill.isToggle() && (activeChar.getEffectList().getEffectsBySkill(skill) != null))
			{
				activeChar.setMacroSkill(null);
				activeChar.getEffectList().stopEffect(skill.getId());
				activeChar.sendActionFailed();
				return;
			}
			
			Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());
			
			activeChar.setGroundSkillLoc(null);
			
			if (activeChar.getMacroSkill() != null) {
				if (skill.getReuseDelay(activeChar) < 9000L) {
					activeChar.setReuseDelay(Math.max(0, skill.getReuseDelay(activeChar) - 3000L));
					activeChar.setMacroSkill(null);
				}
			}
			activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.setMacroSkill(null);
			activeChar.sendActionFailed();
		}
	}
}