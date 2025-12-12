package l2r.gameserver.network.clientpackets;

import org.apache.commons.lang3.ArrayUtils;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcFreight;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.items.Warehouse;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Log;

/**
 * @author VISTALL
 * @date 20:42/16.05.2011
 */
public class RequestPackageSend extends L2GameClientPacket
{
	private static final long _FREIGHT_FEE = 1000; // TODO [VISTALL] hardcode price
	
	private int _objectId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
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
		if (Config.ENABLE_CUSTOM_AUCTION)
		{
			Player player = getClient().getActiveChar();
			PcInventory inventory = player.getInventory();
			
			if ((player == null) || (_count == 0) || (_items == null))
			{
				return;
			}
			
			if (player.isActionsDisabled())
			{
				player.sendActionFailed();
				return;
			}
			
			if (player.isInStoreMode())
			{
				player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}
			
			// Alt game - Karma punishment
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
			{
				// Original Message: You cannot do this while having Karma.
				player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestPackageSend.message1", player));
				return;
			}
			
			if (player.isInTrade())
			{
				player.sendActionFailed();
				return;
			}
			
			if (_items.length > 1)
			{
				// Original Message: You can select only 1 item at a time.
				player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestPackageSend.message2", player));
				return;
			}
			
			Warehouse warehouse;
			if (player.getUsingWarehouseType() != null)
			{
				switch (player.getUsingWarehouseType())
				{
					case CASTLE:
					case CLAN:
						if ((player.getClan() == null) || (player.getClan().getLevel() == 0))
						{
							player.sendPacket(SystemMsg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
							return;
						}
						
						warehouse = player.getClan().getWarehouse();
						break;
					case FREIGHT:
						warehouse = player.getFreight();
						break;
					default:
						warehouse = player.getWarehouse();
				}
			}
			else
			{
				warehouse = null;
			}
			
			try
			{
				inventory.writeLock();
				if (warehouse != null)
				{
					warehouse.writeLock();
				}
				
				for (int i = 0; i < _count; i++)
				{
					ItemInstance item = inventory.getItemByObjectId(_items[i]);
					long itemQ = _itemQ[i];
					if ((item == null) || item.isEquipped())
					{
						return;
					}
					
					if (item.getCount() <= 0)
					{
						return;
					}
					
					AuctionManager.itemSlectedForAuction(player, item.getObjectId(), itemQ);
				}
			}
			catch (ArithmeticException ae)
			{
				// TODO audit
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			finally
			{
				if (warehouse != null)
				{
					warehouse.writeUnlock();
				}
				inventory.writeUnlock();
			}
		}
		else
		{
			Player player = getClient().getActiveChar();
			if ((player == null) || (_count == 0))
			{
				return;
			}
			
			if (player.isActionsDisabled())
			{
				player.sendActionFailed();
				return;
			}
			
			// Alt game - Karma punishment
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
			{
				// Original Message: You cannot do this while having Karma.
				player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestPackageSend.message1", player));
				return;
			}
			
			if (player.isInStoreMode())
			{
				player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}
			
			if (player.isInTrade())
			{
				player.sendActionFailed();
				return;
			}
			
			// Π�Ρ€ΠΎΠ²ΠµΡ€Ρ�ΠµΠΌ Π½Π°Π»ΠΈΡ‡ΠΈΠµ npc ΠΈ Ρ€Π°Ρ�Ρ�Ρ‚ΠΎΡ�Π½ΠΈΠµ Π΄ΠΎ Π½ΠµΠ³ΠΎ
			NpcInstance whkeeper = player.getLastNpc();
			if ((whkeeper == null) || !player.isInRangeZ(whkeeper, Creature.INTERACTION_DISTANCE))
			{
				return;
			}
			
			if (!player.getAccountChars().containsKey(_objectId))
			{
				return;
			}
			
			PcInventory inventory = player.getInventory();
			PcFreight freight = new PcFreight(_objectId);
			freight.restore();
			
			inventory.writeLock();
			freight.writeLock();
			try
			{
				int slotsleft = 0;
				long adenaDeposit = 0;
				
				slotsleft = Config.FREIGHT_SLOTS - freight.getSize();
				
				int items = 0;
				
				// Π΅ΠΎΠ·Π΄Π°ΠµΠΌ Π½ΠΎΠ²Ρ‹ΠΉ Ρ�ΠΏΠΈΡ�ΠΎΠΊ ΠΏΠµΡ€ΠµΠ΄Π°Π²Π°ΠµΠΌΡ‹Ρ… ΠΏΡ€ΠµΠ΄ΠΌΠµΡ‚ΠΎΠ², Π½Π° ΠΎΡ�Π½ΠΎΠ²Πµ ΠΏΠΎΠ»ΡƒΡ‡ΠµΠ½Π½Ρ‹Ρ… Π΄Π°Π½Π½Ρ‹Ρ…
				for (int i = 0; i < _count; i++)
				{
					ItemInstance item = inventory.getItemByObjectId(_items[i]);
					if ((item == null) || (item.getCount() < _itemQ[i]) || !item.getTemplate().isFreightable())
					{
						_items[i] = 0; // Π�Π±Π½ΡƒΠ»Ρ�ΠµΠΌ, Π²ΠµΡ‰Ρ� Π½Πµ Π±ΡƒΠ΄ΠµΡ‚ ΠΏΠµΡ€ΠµΠ΄Π°Π½Π°
						_itemQ[i] = 0L;
						continue;
					}
					
					if (!item.isStackable() || (freight.getItemByItemId(item.getItemId()) == null)) // Π²ΠµΡ‰Ρ� Ρ‚Ρ€ΠµΠ±ΡƒΠµΡ‚ Ρ�Π»ΠΎΡ‚Π°
					{
						if (slotsleft <= 0) // ΠµΡ�Π»ΠΈ Ρ�Π»ΠΎΡ‚Ρ‹ ΠΊΠΎΠ½Ρ‡ΠΈΠ»ΠΈΡ�Ρ� Π½ΠµΡ�Ρ‚ΠµΠΊΡƒΠµΠΌΡ‹Πµ Π²ΠµΡ‰ΠΈ ΠΈ ΠΎΡ‚Ρ�ΡƒΡ‚Ρ�Ρ‚Π²ΡƒΡ�Ρ‰ΠΈΠµ Ρ�Ρ‚ΠµΠΊΡƒΠµΠΌΡ‹Πµ ΠΏΡ€ΠΎΠΏΡƒΡ�ΠΊΠ°ΠµΠΌ
						{
							_items[i] = 0; // Π�Π±Π½ΡƒΠ»Ρ�ΠµΠΌ, Π²ΠµΡ‰Ρ� Π½Πµ Π±ΡƒΠ΄ΠµΡ‚ ΠΏΠµΡ€ΠµΠ΄Π°Π½Π°
							_itemQ[i] = 0L;
							continue;
						}
						slotsleft--; // ΠµΡ�Π»ΠΈ Ρ�Π»ΠΎΡ‚ ΠµΡ�Ρ‚Ρ� Ρ‚ΠΎ ΠµΠ³ΠΎ ΡƒΠ¶Πµ Π½ΠµΡ‚
					}
					
					if (item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					{
						adenaDeposit = _itemQ[i];
					}
					
					items++;
				}
				
				// Π΅ΠΎΠΎΠ±Ρ‰Π°ΠµΠΌ ΠΎ Ρ‚ΠΎΠΌ, Ρ‡Ρ‚ΠΎ Ρ�Π»ΠΎΡ‚Ρ‹ ΠΊΠΎΠ½Ρ‡ΠΈΠ»ΠΈΡ�Ρ�
				if (slotsleft <= 0)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				}
				
				if (items == 0)
				{
					player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				
				// Π�Ρ€ΠΎΠ²ΠµΡ€Ρ�ΠµΠΌ, Ρ…Π²Π°Ρ‚ΠΈΡ‚ Π»ΠΈ Ρƒ Π½Π°Ρ� Π΄ΠµΠ½ΠµΠ³ Π½Π° ΡƒΠΏΠ»Π°Ρ‚Ρƒ Π½Π°Π»ΠΎΠ³Π°
				long fee = SafeMath.mulAndCheck(items, _FREIGHT_FEE);
				
				if ((fee + adenaDeposit) > player.getAdena())
				{
					player.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
					return;
				}
				
				if (!player.reduceAdena(fee, true, ""))
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				
				for (int i = 0; i < _count; i++)
				{
					if (_items[i] == 0)
					{
						continue;
					}
					// ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
					ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i], "Freight");
					Log.item(Log.FreightDeposit, player, whkeeper, item, "Packet Hash: " + hashCode());
					// freight.addItem(item);
					freight.addItem(item, "Freight " + player.toString(), "Freight");
				}
			}
			catch (ArithmeticException ae)
			{
				// TODO audit
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			finally
			{
				freight.writeUnlock();
				inventory.writeUnlock();
			}
			
			// Π�Π±Π½ΠΎΠ²Π»Ρ�ΠµΠΌ ΠΏΠ°Ρ€Π°ΠΌΠµΡ‚Ρ€Ρ‹ ΠΏΠµΡ€Ρ�ΠΎΠ½Π°Π¶Π°
			player.sendChanges();
			player.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
		}
	}
}
