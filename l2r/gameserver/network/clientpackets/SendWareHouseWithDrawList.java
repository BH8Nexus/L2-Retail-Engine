package l2r.gameserver.network.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.Warehouse;
import l2r.gameserver.model.items.Warehouse.WarehouseType;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Log;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);
	
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
			_items[i] = readD(); // item object id
			_itemQ[i] = readQ(); // count
			if ((_itemQ[i] < 1) || (ArrayUtils.indexOf(_items, _items[i]) < i))
			{
				_count = 0;
				break;
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}
		
		Player activeChar = getClient().getActiveChar();
		if ((activeChar == null) || (_count == 0))
		{
			return;
		}
		
		if (activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (activeChar.getKarma() > 0))
		{
			// Original Message: You cannot withdraw items while having Karma.
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.SendWareHouseWithDrawList.CannotWithDrawWhileKarma", activeChar));
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
		
		NpcInstance whkeeper = activeChar.getLastNpc();
		if (!Config.ALLOW_BBS_WAREHOUSE && ((whkeeper == null) || !activeChar.isInRange(whkeeper.getLoc(), Creature.INTERACTION_DISTANCE + whkeeper.BYPASS_DISTANCE_ADD)))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_MOVED_TOO_FAR_AWAY_FROM_THE_WAREHOUSE_TO_PERFORM_THAT_ACTION);
			return;
		}
		if ((System.currentTimeMillis() - activeChar.getLastSendWareHouseWithDrawListPacket()) < Config.SEND_WAREHOUSE_WITH_DRAWLIST_PACKETDELAY)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setLastSendWareHouseWithDrawListPacket();
		
		Warehouse warehouse = null;
		String logType = null;
		
		if (activeChar.getUsingWarehouseType() == WarehouseType.PRIVATE)
		{
			warehouse = activeChar.getWarehouse();
			logType = Log.WarehouseWithdraw;
		}
		else if (activeChar.getUsingWarehouseType() == WarehouseType.CLAN)
		{
			logType = Log.ClanWarehouseWithdraw;
			boolean canWithdrawCWH = false;
			if (activeChar.getClan() != null)
			{
				if (((activeChar.getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH) && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.isClanLeader() || activeChar.getVarB("canWhWithdraw")))
				{
					canWithdrawCWH = true;
				}
			}
			if (!canWithdrawCWH)
			{
				return;
			}
			
			warehouse = activeChar.getClan().getWarehouse();
		}
		else if (activeChar.getUsingWarehouseType() == WarehouseType.FREIGHT)
		{
			warehouse = activeChar.getFreight();
			logType = Log.FreightWithdraw;
		}
		else
		{
			_log.warn("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
			return;
		}
		
		PcInventory inventory = activeChar.getInventory();
		
		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			long weight = 0;
			int slots = 0;
			
			for (int i = 0; i < _count; i++)
			{
				ItemInstance item = warehouse.getItemByObjectId(_items[i]);
				if ((item == null) || (item.getCount() < _itemQ[i]))
				{
					activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getTemplate().getWeight(), _itemQ[i]));
				if (!item.isStackable() || (inventory.getItemByItemId(item.getItemId()) == null))
				{
					slots++;
				}
			}
			
			if (!activeChar.getInventory().validateCapacity(slots))
			{
				activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			
			if (!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			
			for (int i = 0; i < _count; i++)
			{
				// ItemInstance item = warehouse.removeItemByObjectId(_items[i], _itemQ[i]);
				ItemInstance item = warehouse.removeItemByObjectId(_items[i], _itemQ[i], null, null);
				if (item == null)
				{
					Log.item(logType, warehouse, activeChar, item, "Packet: " + hashCode());
				}
				// activeChar.getInventory().addItem(item);
				activeChar.getInventory().addItem(item, logType);
			}
		}
		catch (ArithmeticException ae)
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
}