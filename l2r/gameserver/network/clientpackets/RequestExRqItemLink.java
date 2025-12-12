package l2r.gameserver.network.clientpackets;

import l2r.gameserver.cache.ItemInfoCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInfo;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends L2GameClientPacket //FIXME TEST CODE 
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		ItemInfo item;
		if ((item = ItemInfoCache.getInstance().get(_objectId)) == null)
		{
			// Nik: Support for question mark listeners. Used for party find and other shits. objectId is used as the questionMarkId. Use with caution.
			getClient().getActiveChar().getListeners().onQuestionMarkClicked(_objectId);
			
			if (_objectId >= 5000000 && _objectId < 6000000)
			{
				Player player = getClient().getActiveChar();
				String varName = "DisabledAnnounce" + _objectId;
				if (!player.containsQuickVar(varName))
				{
					player.addQuickVar(varName, "true");
					player.sendMessage("Announcement Disabled!");
				}
			}
			sendPacket(ActionFail.STATIC);
		}
		else
			sendPacket(new ExRpItemLink(item));
	}
}
//package l2r.gameserver.network.clientpackets;
//
//import l2r.gameserver.cache.ItemInfoCache;
//import l2r.gameserver.cache.Msg;
//import l2r.gameserver.model.GameObjectsStorage;
//import l2r.gameserver.model.Party;
//import l2r.gameserver.model.Player;
//import l2r.gameserver.model.base.PcCondOverride;
//import l2r.gameserver.model.items.ItemInfo;
//import l2r.gameserver.model.items.ItemInstance;
//import l2r.gameserver.network.serverpackets.ActionFail;
//import l2r.gameserver.network.serverpackets.ExRpItemLink;
//import l2r.gameserver.network.serverpackets.Say2;
//import l2r.gameserver.network.serverpackets.components.ChatType;
//import l2r.gameserver.network.serverpackets.components.CustomMessage;
//import l2r.gameserver.network.serverpackets.components.IStaticPacket;
//import l2r.gameserver.utils.Util;
//
//public class RequestExRqItemLink extends L2GameClientPacket
//{
//	private int _objectId;
//
//	@Override
//	protected void readImpl()
//	{
//		_objectId = readD();
//	}
//
//	@Override
//	public void runImpl()
//	{
//		Player player = getClient().getActiveChar();
//		
//		ItemInfo item = ItemInfoCache.getInstance().get(_objectId);
//		if (item != null)
//			sendPacket(new ExRpItemLink(item));
//		else
//		{
//			int clickInfo = RequestClickInfo(_objectId);
//			if(player == null)
//				return;
//
//			switch(clickInfo)
//			{
//				case 0: // Item link
//					ItemInstance item2 = GameObjectsStorage.getAsItem(_objectId);
//					if(item2 != null && item2.isPublished())
//						player.sendPacket(new ExRpItemLink(item2));
//					else
//						sendPacket(ActionFail.STATIC);
//					break;
//				case 1: // Party request for players
//					Player sender = GameObjectsStorage.getPlayer(_objectId - 100000000);
//
//					if(sender == null)
//					{
//						player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message1", player));
//						return;
//					}
//
//					if(sender.isInParty())
//					{
//						player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message2", player).addString(sender.getName()));
//						sender.removePartyRequest(player.getObjectId());
//						return;
//					}
//
//					if(!sender.isPartyRequestValid(player.getObjectId()))
//					{
//						player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message1", player));
//						return;
//					}
//
//					IStaticPacket problem = sender.canJoinParty(player);
//					if(problem != null)
//					{
//						player.sendPacket(problem);
//						return;
//					}
//					
//					if(player.getParty() == null)
//					{
//						if(player.getObjectId() == _objectId)
//							return;
//
//						player.setParty(new Party(player, 1));
//						sender.joinParty(player.getParty());
//					}
//					else
//					{
//						if(player.getParty().size() >= Party.MAX_SIZE)
//						{
//							sender.sendPacket(Msg.PARTY_IS_FULL);
//							return;
//						}
//						sender.joinParty(player.getParty());
//					}
//					sender.removePartyRequest(player.getObjectId());
//					break;
//				case 2: // Player requested for party
//					Player partyLeader = GameObjectsStorage.getPlayer(_objectId);
//
//					if(partyLeader == null)
//						return;
//
//					if(!partyLeader.isPartyFindValid())
//					{
//						player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message1", player));
//						return;
//					}
//
//					if(!player.isInParty())
//					{
//						if(player.getObjectId() == partyLeader.getObjectId())
//							return;
//
//						if(partyLeader.getTeam() != player.getTeam())
//						{
//							player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message3", player));
//							return;
//						}
//
//						if(!player.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && !player._antiFlood.canShout())
//						{
//							player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message4", player));
//							return;
//						}
//						
//						String gear = "";
//						int playerGearScore = Util.getGearPoints(player);
//						// A grade = ~ 800
//						// S grade = ~1300
//						// S80 grade = ~2000
//						// S84 vesper = ~ 3500
//						// S84 vesper with boss jews = ~ 5800
//						// S84 with boss jewels and +5 = ~ 6700
//						if (playerGearScore <= 800)
//							gear = "Poor";
//						else if (playerGearScore > 800 && playerGearScore < 1300)
//							gear = "Newbie..";
//						else if (playerGearScore > 1300 && playerGearScore < 2200)
//							gear = "Around middle..";
//						else if (playerGearScore > 2200 && playerGearScore <= 4000)
//							gear = "Middle gear";
//						else if (playerGearScore > 4000 && playerGearScore < 6000)
//							gear = "Top gear";
//						else if (playerGearScore > 6000 && playerGearScore < 7500)
//							gear = "Killer";
//						else if (playerGearScore > 7500)
//							gear = "Haxor gear!";
//						
//						partyLeader.sendPacket(new Say2(player.getObjectId(), ChatType.TELL, player.getName(), "	Type=1 	ID=" + (player.getObjectId() + 100000000) + " Color=0 	Underline=0 	Title=" + player.getLevel() + "lvl - " + player.getTemplate().className + ", Gear: " + gear + " - looking for party "));
//						player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExRqItemLink.message5", player).addString(partyLeader.getName()));
//						player.addPartyRequest(partyLeader.getObjectId());
//						return;
//					}
//					break;
//			}
//		}
//	}
//
//	private int RequestClickInfo(int obj)
//	{
//		if(ItemInfoCache.getInstance().get(obj) != null)
//			return 0;
//		if(GameObjectsStorage.getPlayer(obj - 100000000) != null)
//			return 1;
//		if(GameObjectsStorage.getPlayer(obj) != null)
//			return 2;
//		return -1;
//	}
//}