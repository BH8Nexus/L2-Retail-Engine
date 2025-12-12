package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Request;
import l2r.gameserver.model.Request.L2RequestType;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.network.serverpackets.AskJoinParty;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;
	
	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_itemDistribution = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		Player target = World.getPlayer(_name);
		if (target == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}
		
		if (target.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && target.getMessageRefusal())
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}
		
		if (target == activeChar)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			activeChar.sendActionFailed();
			return;
		}
		
		if (target.isBusy())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
			return;
		}
		
		if (target.isInJail() || activeChar.isInJail())
		{
			// Original Message: You cannot invite a player while is in Jail.
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestJoinParty.message2", activeChar));
			return;
		}
		
		if ((activeChar.getTeamEvents() != target.getTeamEvents()) && activeChar.isChecksForTeam())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}
		
//		IStaticPacket problem = target.canJoinParty(activeChar);
//		if (problem != null)
//		{
//			activeChar.sendPacket(problem);
//			
//			// Support for GM forcing his way in a party like a scumbag :)
//			if (activeChar.isGM() && target.isInParty())
//			{
//				new Request(L2RequestType.PARTY, target, activeChar).setTimeout(10000L).set("itemDistribution", _itemDistribution);
//				
//				activeChar.sendPacket(new AskJoinParty(target.getName(), _itemDistribution));
//				activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_BEEN_INVITED_TO_THE_PARTY).addName(activeChar));
//			}
//			
//			return;
//		}
		
		IStaticPacket problem = target.canJoinParty(activeChar);
		if (problem != null)
		{
			activeChar.sendPacket(problem);
			return;
		}
		
		if (activeChar.isGM())
		{
			if (activeChar.isInParty())
			{
				if (activeChar.getParty().isFull())
				{
					activeChar.sendMessage("This party is full.");
					return;
				}
				if (target.isInParty())
				{
					target.leaveParty();
				}
				target.joinParty(activeChar.getParty());
				return;
			}
			else
			{
				Party GMParty = new Party(activeChar, Party.ITEM_LOOTER);
				activeChar.setParty(GMParty);
				if (target.isInParty())
				{
					target.leaveParty();
				}
				target.joinParty(activeChar.getParty());
				return;
			}
		}
		
		if (activeChar.isInParty())
		{
			if (activeChar.getParty().isFull())
			{
				activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
				return;
			}
			
			// Î Î†Î ÎŽÎ Â»Î¡ï¿½Î ÎŠÎ ÎŽ Party Leader Î ÎŒÎ ÎŽÎ Â¶Î ÂµÎ¡â€š Î Î�Î¡â‚¬Î ÎˆÎ Â³Î Â»Î Â°Î¡ï¿½Î Â°Î¡â€šÎ¡ï¿½ Î Â½Î ÎŽÎ Â²Î¡â€¹Î¡â€¦ Î¡â€¡Î Â»Î ÂµÎ Â½Î ÎŽÎ Â²
			if (Config.PARTY_LEADER_ONLY_CAN_INVITE && !activeChar.getParty().isLeader(activeChar))
			{
				activeChar.sendPacket(SystemMsg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
				return;
			}
			
			if (activeChar.getParty().isInDimensionalRift())
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestJoinParty.InDimensionalRift", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}
		
		new Request(L2RequestType.PARTY, activeChar, target).setTimeout(10000L).set("itemDistribution", _itemDistribution);
		
		target.sendPacket(new AskJoinParty(activeChar.getName(), _itemDistribution));
		activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_BEEN_INVITED_TO_THE_PARTY).addName(target));
	}
}