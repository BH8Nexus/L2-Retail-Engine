package l2r.gameserver.network.clientpackets;

import org.apache.commons.lang3.ArrayUtils;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.Warehouse;
import l2r.gameserver.model.items.Warehouse.WarehouseType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;

/**
 * Format: cdb, b - array of (dd)
 */
public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final long _WAREHOUSE_FEE = 30; // TODO [G1ta0] hardcode price
	
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		if (((_count * 12) > _buf.remaining()) || (_count > Short.MAX_VALUE) || (_count < 1))
		{
			_count = 0;
			return;
		}
		
		_items = new int[_count];
		_itemQ = new long[_count];
		
		for (int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			if ((_itemQ[i] < 1) || (ArrayUtils.indexOf(_items, _items[i]) < i))
			{
				_count = 0;
				return;
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if ((activeChar == null) || (_count == 0))
		{
			return;
		}
		
		/*
		 * if (!activeChar.getPlayerAccess().UseWarehouse) { activeChar.sendActionFailed(); return; }
		 */
		
		if (activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (activeChar.getKarma() > 0))
		{
			// Original Message: You cannot deposit items while having Karma.
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.SendWareHouseDepositList.CannotDepositWhileKarma", activeChar));
			return;
		}
		
		if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		if (activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.getUsingWarehouseType() == WarehouseType.FREIGHT)
		{
			checkAuctionAdd(activeChar, _items, _itemQ);
			return;
		}
		
		// Check availability npc and distance
		final NpcInstance whkeeper = activeChar.getLastNpc();
//		//if ((!Config.ALLOW_BBS_WAREHOUSE && (whkeeper == null)) || !activeChar.isInRangeZ(whkeeper.getLoc(), Creature.INTERACTION_DISTANCE + whkeeper.BYPASS_DISTANCE_ADD))
//		if(whkeeper == null && (!Config.ALLOW_BBS_WAREHOUSE) || !activeChar.isInRangeZ(whkeeper.getLoc(), Creature.INTERACTION_DISTANCE + whkeeper.BYPASS_DISTANCE_ADD))
//		{
//			activeChar.sendPacket(SystemMsg.YOU_HAVE_MOVED_TOO_FAR_AWAY_FROM_THE_WAREHOUSE_TO_PERFORM_THAT_ACTION);
//		}
		
		if(Config.ALLOW_BBS_WAREHOUSE && (whkeeper == null || !activeChar.checkInteractionDistance(whkeeper)))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
		}
		
		if ((System.currentTimeMillis() - activeChar.getLastSendWareHouseDepositListPacket()) < Config.SEND_WAREHOUSE_DEPOSIT_LIST_PACKETDELAY)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setLastSendWareHouseDepositListPacket();
		
		final PcInventory inventory = activeChar.getInventory();
		final boolean privatewh = activeChar.getUsingWarehouseType() != WarehouseType.CLAN;
		Warehouse warehouse;
		if (privatewh)
		{
			warehouse = activeChar.getWarehouse();
		}
		else
		{
			warehouse = activeChar.getClan().getWarehouse();
		}
		
		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			int slotsleft = 0;
			long adenaDeposit = 0;
			
			if (privatewh)
			{
				slotsleft = activeChar.getWarehouseLimit() - warehouse.getSize();
			}
			else
			{
				slotsleft = (activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN) - warehouse.getSize();
			}
			
			int items = 0;
			
			for (int i = 0; i < _count; i++)
			{
				final ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if ((item == null) || (item.getCount() < _itemQ[i]) || !item.canBeStored(activeChar, privatewh || !privatewh))
				{
					_items[i] = 0;
					_itemQ[i] = 0L;
					continue;
				}
				
				if (!item.isStackable() || (warehouse.getItemByItemId(item.getItemId()) == null))
				{
					if (slotsleft <= 0)
					{
						_items[i] = 0;
						_itemQ[i] = 0L;
						continue;
					}
					slotsleft--;
				}
				
				if (item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
				{
					adenaDeposit = _itemQ[i];
				}
				
				items++;
			}
			
			if (slotsleft <= 0)
			{
				activeChar.sendPacket(SystemMsg.YOUR_WAREHOUSE_IS_FULL);
			}
			
			if (items == 0)
			{
				activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			
			final long fee = SafeMath.mulAndCheck(items, _WAREHOUSE_FEE);
			
			if ((fee + adenaDeposit) > activeChar.getAdena())
			{
				activeChar.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}
			
			if (!activeChar.reduceAdena(fee, true, (privatewh ? "Private" : "Clan")+"WarehouseDepositFee"))
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			
			for (int i = 0; i < _count; i++)
			{
				if (_items[i] == 0)
				{
					continue;
				}
				// final ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i],"");
				ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i], (privatewh ? "Private" : "Clan") + "WarehouseDeposit");
				// Log.LogItem(activeChar, privatewh ? Log.WarehouseDeposit : Log.ClanWarehouseDeposit, item);
				warehouse.addItem(item, null, null);
			}
		}
		catch (final ArithmeticException ae)
		{
			// TODO audit
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			warehouse.writeUnlock();
			inventory.writeUnlock();
		}
		
		activeChar.sendChanges();
		activeChar.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
	}
	
	private void checkAuctionAdd(Player activeChar, int[] _items, long[] _itemQ)
	{
		if ((_items.length != 1) || (_itemQ[0] != 1))
		{
			activeChar.sendMessage("You can add just one item at the time!");
			return;
		}
		
		CommunityBoardManager.getInstance().getCommunityHandler("_sendTimePrice_").onBypassCommand(activeChar, "_sendTimePrice_" + _items[0]);
	}
}