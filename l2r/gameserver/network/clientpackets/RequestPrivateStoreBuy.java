package l2r.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import l2r.commons.math.SafeMath;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.TradeItem;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.TradeHelper;

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private int _sellerId;
	private int _count;
	private int[] _items; // object id
	private long[] _itemQ; // count
	private long[] _itemP; // price
	
	@Override
	protected void readImpl()
	{
		_sellerId = readD();
		_count = readD();
		if (((_count * 20) > _buf.remaining()) || (_count > Short.MAX_VALUE) || (_count <= 0)) // check aga
		{
			// _count = 0;
			_items = null;
			return;
		}
		
		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];
		
		for (int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			_itemP[i] = readQ();
			
			if ((_itemQ[i] < 1) || (_itemP[i] < 1) || (ArrayUtils.indexOf(_items, _items[i]) < i))
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
		
		Player buyer = getClient().getActiveChar();
		if ((buyer == null) || (_count == 0))
		{
			return;
		}
		
		if (buyer.isActionsDisabled() || buyer.isBlocked())// || !Config.ALLOW_PRIVATE_STORES)
		{
			buyer.sendActionFailed();
			return;
		}
		
		if (buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}
		
		if (buyer.isInStoreMode())
		{
			buyer.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		if (buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}
		
		if (buyer.isFishing())
		{
			buyer.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}
		
		if (!buyer.getAccessLevel().allowTransaction())
		{
			buyer.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_____);
			return;
		}
		
		Player seller = (Player) buyer.getVisibleObject(_sellerId);
		if ((seller == null) || ((seller.getPrivateStoreType() != Player.STORE_PRIVATE_SELL) && (seller.getPrivateStoreType() != Player.STORE_PRIVATE_SELL_PACKAGE)) || !seller.isInRangeZ(buyer, Creature.INTERACTION_DISTANCE))
		{
			buyer.sendPacket(SystemMsg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
			buyer.sendActionFailed();
			return;
		}
		
		List<TradeItem> sellList = seller.getSellList();
		if (sellList.isEmpty())
		{
			buyer.sendPacket(SystemMsg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
			buyer.sendActionFailed();
			return;
		}
		
		List<TradeItem> buyList = new ArrayList<>();
		
		long totalCost = 0;
		int slots = 0;
		long weight = 0;
		
		buyer.getInventory().writeLock();
		seller.getInventory().writeLock();
		try
		{
			loop: for (int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];
				
				TradeItem bi = null;
				
				for (TradeItem si : sellList)
				{
					if (si.getObjectId() == objectId)
					{
						if (si.getOwnersPrice() == price)
						{
							if (count > si.getCount())
							{
								break loop;
							}
							
							ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
							if ((item == null) || (item.getCount() < count) || !item.canBeTraded(seller))
							{
								break loop;
							}
							
							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
							weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
							if (!item.isStackable() || (buyer.getInventory().getItemByItemId(item.getItemId()) == null))
							{
								slots++;
							}
							
							bi = new TradeItem();
							bi.setObjectId(objectId);
							bi.setItemId(item.getItemId());
							bi.setCount(count);
							bi.setOwnersPrice(price);
							
							buyList.add(bi);
							break;
						}
					}
				}
			}
		}
		catch (ArithmeticException ae)
		{
			// TODO audit
			buyList.clear();
			seller.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			try
			{
				// Î Î�Î¡â‚¬Î ÎŽÎ Â²Î ÂµÎ¡â‚¬Î¡ï¿½Î ÂµÎ ÎŒ, Î¡â€¡Î¡â€šÎ ÎŽ Î Â²Î¡ï¿½Î Âµ Î Â²Î ÂµÎ¡â€°Î Îˆ Î Î„Î ÎŽÎ¡ï¿½Î¡â€šÎ¡Æ’Î Î�Î Â½Î¡â€¹ Î Î„Î Â»Î¡ï¿½ Î Î�Î ÎŽÎ ÎŠÎ¡Æ’Î Î�Î ÎŠÎ Îˆ, Î¡ï¿½Î Â»Î¡Æ’Î¡â€¡Î Â°Î Âµ Î Î�Î¡â‚¬Î ÎŽÎ Î„Î Â°Î Â¶Î Îˆ Î¡Æ’Î Î�Î Â°Î ÎŠÎ ÎŽÎ Â²Î ÎŠÎ ÎŽÎ Î‰, Î Î�Î¡â‚¬Î ÎŽÎ Â²Î ÂµÎ¡â‚¬Î¡ï¿½Î ÂµÎ ÎŒ, Î¡â€¡Î¡â€šÎ ÎŽ Î Î�Î ÎŽÎ ÎŠÎ¡Æ’Î Î�Î Â°Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Â²Î¡ï¿½Î¡ï¿½ Î¡Æ’Î Î�Î Â°Î ÎŠÎ ÎŽÎ Â²Î ÎŠÎ Â°
				if ((buyList.size() != _count) || ((seller.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE) && (buyList.size() != sellList.size())))
				{
					buyer.sendPacket(SystemMsg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
					buyer.sendActionFailed();
					return;
				}
				
				if (!buyer.getInventory().validateWeight(weight))
				{
					buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
					buyer.sendActionFailed();
					return;
				}
				
				if (!buyer.getInventory().validateCapacity(slots))
				{
					buyer.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
					buyer.sendActionFailed();
					return;
				}
				
				if (!buyer.reduceAdena(totalCost, ""))
				{
					buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					buyer.sendActionFailed();
					return;
				}
				
				ItemInstance item;
				for (TradeItem bi : buyList)
				{
					item = seller.getInventory().removeItemByObjectId(bi.getObjectId(), bi.getCount(), "");
					for (TradeItem si : sellList)
					{
						if (si.getObjectId() == bi.getObjectId())
						{
							si.setCount(si.getCount() - bi.getCount());
							if (si.getCount() < 1L)
							{
								sellList.remove(si);
							}
							break;
						}
					}
					Log.item(Log.PrivateStoreSell, seller, buyer, item, "Total Cost: " + totalCost + " Packet Hash: " + hashCode());
					buyer.getInventory().addItem(item, "");
					TradeHelper.purchaseItem(buyer, seller, bi);
				}
				
				final long tax = TradeHelper.getTax(seller, totalCost);
				if (tax > 0)
				{
					totalCost -= tax;
					seller.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestPrivateStoreBuy.HavePaidTax", seller).addNumber(tax));
				}
				
				seller.addAdena(totalCost, "");
				seller.saveTradeList();
			}
			finally
			{
				seller.getInventory().writeUnlock();
				buyer.getInventory().writeUnlock();
			}
		}
		
		if (sellList.isEmpty())
		{
			TradeHelper.cancelStore(seller);
		}
		
		seller.sendChanges();
		buyer.sendChanges();
		
		buyer.sendActionFailed();
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}