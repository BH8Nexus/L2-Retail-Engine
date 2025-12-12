package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.DimensionalRift;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			// Original Message: Currently you cannot leave the party. //TODO [G1ta0] custom message
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestWithDrawalParty.CannotLeave", activeChar));
			return;
		}

		Reflection r = activeChar.getParty().getReflection();
		if(r != null && r instanceof DimensionalRift && activeChar.getReflection().equals(r))
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestWithDrawalParty.Rift", activeChar));
		else if(r != null && activeChar.isInCombat())
			// Original Message: Currently you cannot leave the party. //TODO [G1ta0] custom message
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestWithDrawalParty.CannotLeave", activeChar));
		else
			activeChar.leaveParty();
	}
}