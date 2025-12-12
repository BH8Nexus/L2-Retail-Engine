package l2r.gameserver.network.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.EnchantItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.network.serverpackets.EnchantResult;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.support.EnchantScroll;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestEnchantItem.class);
	private int _objectId, _catalystObjId;
	
	private static final int SUCCESS_VISUAL_EFF_ID = 5965;
	private static final int FAIL_VISUAL_EFF_ID = 5949;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_catalystObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		player.isntAfk();
		
		if (player.isActionsDisabled() || player.isBlocked())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}
		
		if (player.isInTrade())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}
		
		if (player.isSitting())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResult.CANCEL);
			player.sendMessage("You can't enchant while sitting.");
			player.sendActionFailed();
			return;
		}
		
		if (player.isInStoreMode())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendActionFailed();
			return;
		}
		
		PcInventory inventory = player.getInventory();
		inventory.writeLock();
		try
		{
			ItemInstance item = inventory.getItemByObjectId(_objectId);
			ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
			ItemInstance scroll = player.getEnchantScroll();
			
			if ((item == null) || (scroll == null))
			{
				player.sendActionFailed();
				return;
			}
			
			if (!ItemFunctions.checkCatalyst(item, catalyst))
			{
				catalyst = null;
			}
			else if ((catalyst != null) && (player.getEnchantCatalyst() != catalyst.getItemId()))
			{
				catalyst = null;
			}
			
			EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
			if (enchantScroll == null)
			{
				_log.warn("Missing enchant scroll for item: " + scroll.getName() + "(" + scroll.getItemId() + ")");
				return;
			}
			
			Log.add(player.getName() + "|Trying to enchant|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + item.getObjectId(), "enchants");
			
			if ((enchantScroll.getMaxEnchant() != -1) && (item.getEnchantLevel() >= enchantScroll.getMaxEnchant()))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}
			
			if (enchantScroll.getItems().size() > 0)
			{
				if (!enchantScroll.getItems().contains(item.getItemId()))
				{
					player.sendPacket(EnchantResult.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
			}
			else
			{
				if (enchantScroll.getGrade().externalOrdinal != item.getCrystalType().externalOrdinal)
				{
					player.sendPacket(EnchantResult.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
				int itemType = item.getTemplate().getType2();
				switch (enchantScroll.getType())
				{
					case ARMOR:
						if (itemType != 0)
						{
							break;
						}
						player.sendPacket(EnchantResult.CANCEL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						player.sendActionFailed();
						return;
					case WEAPON:
						if ((itemType != 1) && (itemType != 2))
						{
							break;
						}
						player.sendPacket(EnchantResult.CANCEL);
						player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
						player.sendActionFailed();
						return;
					default:
						break;
				}
			}
			
			if (!item.canBeEnchanted(false))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}
			
			if (!player.getPermissions().canEnchantItem(item, scroll, catalyst, true))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}
			
			// Synerge - Max enchant for olf t shirt
			if ((item.getItemId() == 21580) && (item.getEnchantLevel() >= Config.ENCHANT_MAX_OLF_T_SHIRT))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}
			
			if (!inventory.destroyItem(scroll, 1L, "") || ((catalyst != null) && !inventory.destroyItem(catalyst, 1L, "")))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendActionFailed();
				return;
			}
			
			boolean equipped = item.isEquipped();
			if (equipped)
			{
				inventory.unEquipItem(item);
			}
			
			int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON;
			
			int chance = enchantScroll.getChance(item.getEnchantLevel() + 1);
			if (catalyst != null)
			{
				chance += ItemFunctions.getCatalystPower(catalyst.getItemId());
			}
			
			// Premium System
			// chance *= PremiumAccountsTable.getEnchantBonus(player);
			
			if (item.getEnchantLevel() < safeEnchantLevel)
			{
				chance = 100;
			}
			
			// Test code is need test
			// Olf's T-Shirt Custom Enchant Rates
			if (Config.OLF_TSHIRT_CUSTOM_ENABLED && (item.getItemId() == 21580) && ((enchantScroll.getItemId() == 21581) || (enchantScroll.getItemId() == 21582)))
			{
				chance = 10;
				// chance = (item.getEnchantLevel() >= Config.ENCHANT_OLF_TSHIRT_CHANCES.size()) ? 10 : Config.ENCHANT_OLF_TSHIRT_CHANCES.get(item.getEnchantLevel()); // if item enchant lvl is more than +10 than chance is 10
			}
			
			// Test code is need test
			
			Functions.sendDebugMessage(player, "Enchant chance is " + chance);
			
			if (Rnd.chance(chance))
			{
				if (Config.ENABLE_PLAYER_COUNTERS)
				{
					boolean isBlessedScroll = ItemFunctions.isBlessedEnchantScroll(enchantScroll.getItemId());
					boolean isCrystalScroll = ItemFunctions.isCrystallEnchantScroll(enchantScroll.getItemId());
					
					// if (Config.ENABLE_PLAYER_COUNTERS)
					// {
					// if ((item.getEnchantLevel() == 2) && (player.getCounters().EnchantItem < 2))
					// {
					// player.getCounters().EnchantItem = 2;
					// }
					// else if ((item.getEnchantLevel() == 8) && (player.getCounters().EnchantItem < 8))
					// {
					// player.getCounters().EnchantItem = 8;
					// }
					// else if ((item.getEnchantLevel() == 12) && (player.getCounters().EnchantItem < 12))
					// {
					// player.getCounters().EnchantItem = 12;
					// }
					// else if ((item.getEnchantLevel() == 16) && (player.getCounters().EnchantItem < 16))
					// {
					// player.getCounters().EnchantItem = 16;
					// }
					// }
					
					// success
					if (isBlessedScroll)
					{
						player.getCounters().enchantBlessedSucceeded++;
					}
					else if (!isBlessedScroll && !isCrystalScroll)
					{
						player.getCounters().enchantBlessedSucceeded++;
					}
				}
				
				item.setEnchantLevel(item.getEnchantLevel() + 1);
				item.setJdbcState(JdbcEntityState.UPDATED);
				item.update();
				showEnchantAnimation(player, item.getEnchantLevel());
				
				if (equipped)
				{
					inventory.equipItem(item);
				}
				
				player.sendPacket(new InventoryUpdate().addModifiedItem(item));
				
				player.sendPacket(EnchantResult.SUCESS);
				
				Log.add(player.getName() + "|Succesfully Enchanted|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + item.getObjectId() + "|" + enchantScroll.getItemId() + "|" + chance, "enchants");
				Log.item(Log.EnchantSuccess, player, scroll, item, "Catalyst: " + catalyst);
				
				if (enchantScroll.isHasVisualEffect() && (item.getEnchantLevel() > 3))
				{
					player.broadcastPacket(new MagicSkillUse(player, player, SUCCESS_VISUAL_EFF_ID, 1, 500, 1500));
				}
			}
			else
			{
				Log.add(player.getName() + "|Failed to enchant|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + chance, "enchants");
				
				switch (enchantScroll.getResultType())
				{
					case CRYSTALS:
						if (item.isEquipped())
						{
							player.sendDisarmMessage(item);
						}
						
						Log.item(Log.EnchantFail, player, scroll, item, "Catalyst: " + catalyst);
						
						if (!inventory.destroyItem(item, 1L, "EnchantFail"))
						{
							player.sendActionFailed();
							return;
						}
						
						int crystalId = item.getCrystalType().cry;
						if ((crystalId > 0) && (item.getTemplate().getCrystalCount() > 0) && (item.getCustomFlags() <= 0))
						{
							int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
							if (item.getEnchantLevel() > 3)
							{
								crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
							}
							if (crystalAmount < 1)
							{
								crystalAmount = 1;
							}
							
							player.sendPacket(new EnchantResult(1, crystalId, crystalAmount));
							// player.getInventory().addItem(crystalId, crystalAmount, true);
							ItemFunctions.addItem(player, crystalId, crystalAmount, true, "EnchantFailCrystals");
						}
						else
						{
							player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
						}
						
						if (enchantScroll.isHasVisualEffect())
						{
							player.broadcastPacket(new MagicSkillUse(player, player, FAIL_VISUAL_EFF_ID, 1, 500, 1500));
						}
						break;
					case DROP_ENCHANT:
						item.setEnchantLevel(0);
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();
						
						if (equipped)
						{
							inventory.equipItem(item);
						}
						
						player.sendPacket(new InventoryUpdate().addModifiedItem(item));
						player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
						player.sendPacket(EnchantResult.BLESSED_FAILED);
						break;
					case NOTHING:
						player.sendPacket(EnchantResult.ANCIENT_FAILED);
						break;
				}
			}
		}
		finally
		{
			inventory.writeUnlock();
			
			player.setEnchantScroll(null);
			player.setEnchantCatalyst(0);
			player.updateStats();
		}
	}
	
	/**
	 * @param player
	 * @param enchantLevel : 0 = fail
	 */
	private static void showEnchantAnimation(Player player, int enchantLevel)
	{
		/*
		 * if (player.getVarB("EnchantAnimationDisable")) return;
		 */
		
		enchantLevel = Math.min(enchantLevel, 20);
		int skillId = 23096 + enchantLevel;
		// player.broadcastPacketToOthers(new MagicSkillUse(player, player, skillId, 1, 0, 500));
		final MagicSkillUse msu = new MagicSkillUse(player, player, skillId, 1, 1, 1);
		player.broadcastPacket(msu);
	}
}