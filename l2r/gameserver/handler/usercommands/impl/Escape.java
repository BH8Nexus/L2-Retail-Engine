package l2r.gameserver.handler.usercommands.impl;

import l2r.gameserver.handler.usercommands.IUserCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.SkillTable;

/**
 * Support for /unstuck command
 */
public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 52 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		if(activeChar.isMovementDisabled() || activeChar.isOutOfControl() || activeChar.isInOlympiadMode() || activeChar.isInJail())
			return false;

		if(activeChar.getTeleMode() != 0)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.usercommands.impl.Escape.TryLater", activeChar));
			return false;
		}

		if(activeChar.isTerritoryFlagEquipped())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}

		if(activeChar.isInDuel() || activeChar.getTeam() != TeamType.NONE || activeChar.getTeamEvents() > 10 && activeChar.getTeamEvents() < 90)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.usercommands.impl.Escape.RecallInDuel", activeChar));
			return false;
		}

		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.stopMove();

		Skill skill;
		if(activeChar.isGM())
			skill = SkillTable.getInstance().getInfo(1050, 2);
		else
			skill = SkillTable.getInstance().getInfo(2099, 1);

		if(skill != null && skill.checkCondition(activeChar, activeChar, false, false, true))
			activeChar.getAI().Cast(skill, activeChar, false, true);

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}