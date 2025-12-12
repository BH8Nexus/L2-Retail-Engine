package l2r.gameserver.network.clientpackets;

import java.util.Arrays;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.AccessLevel;
import l2r.gameserver.model.base.Division;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.network.serverpackets.NickNameChanged;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.Util;

public class RequestGiveNickName extends L2GameClientPacket
{
	private String _target;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
		_title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!_title.isEmpty() && !Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE))
		{
			// Original Message: Incorrect title.
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestGiveNickName.message1", activeChar));
			return;
		}
		
		// Check if this title is reserved by other access level than yours.
		for (AccessLevel al : AdminTable.getInstance().getAccessLevels())
		{
			if ((al == null) || (al == activeChar.getAccessLevel()))
			{
				continue;
			}
			
			for (String reserved : al.getReservedTitles())
			{
				if (_title.equalsIgnoreCase(reserved))
				{
					// Original Message: The title " + reserved + " is reserved for people with another access level.
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestGiveNickName.message2", activeChar).addString(reserved));
					return;
				}
			}
			
			if (Arrays.stream(Division.values()).anyMatch(div -> (activeChar.getDivision() != div) && _title.equals(div.getReservedTitle()))) // Title is reserved by another division
			{
				// Original Message: The title " + _title + " is reserved for people with another division.
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestGiveNickName.message3", activeChar).addString(_title));
				return;
			}
		}
		
		// Дворяне могут устанавливать/менять себе title
		if (activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(SystemMsg.YOUR_TITLE_HAS_BEEN_CHANGED);
			activeChar.broadcastPacket(new NickNameChanged(activeChar));
			return;
		}
		// Can the player change/give a title?
		else if ((activeChar.getClanPrivileges() & Clan.CP_CL_MANAGE_TITLES) != Clan.CP_CL_MANAGE_TITLES)
		{
			return;
		}
		
		if (activeChar.getClan().getLevel() < 3)
		{
			activeChar.sendPacket(SystemMsg.A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE);
			return;
		}
		
		// if (activeChar.isSellBuff())
		// {
		// // Original Message: You can't change title when sell buffs.
		// activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestGiveNickName.message4", activeChar));
		// return;
		// }
		//
		UnitMember member = activeChar.getClan().getAnyMember(_target);
		if (member != null)
		{
			member.setTitle(_title);
			if (member.isOnline())
			{
				member.getPlayer().sendPacket(SystemMsg.YOUR_TITLE_HAS_BEEN_CHANGED);
				member.getPlayer().sendChanges();
			}
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestGiveNickName.NotInClan", activeChar));
		}
	}
}