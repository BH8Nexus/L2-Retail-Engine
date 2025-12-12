package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.dao.JdbcEntityState;
import l2r.commons.util.Rnd;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.ShortCutRegister;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminAugment implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_showaugmentmenu,
		admin_augment,
		admin_removeaugment
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch (command)
		{
			case admin_showaugmentmenu:
			{
				showMainPage(activeChar);
				return true;
			}
			case admin_augment:
			{
				if (wordList.length < 2)
				{
					showMainPage(activeChar);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.missingparameters", activeChar));
					return true;
				}
				
				int augmentskillId = 0;
				int lifeStoneLevel = Rnd.get(1, 9);
				int statsCount = Rnd.get(0, 1);
				
				try
				{
					augmentskillId = Integer.parseInt(wordList[1]);
					if (wordList.length > 2)
					{
						lifeStoneLevel = Integer.parseInt(wordList[2]);
					}
					if (wordList.length > 3)
					{
						statsCount = Integer.parseInt(wordList[3]);
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.specifyvalue", activeChar));
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.specifyvalidvalue", activeChar));
				}
				
				augmentWeapon(activeChar, augmentskillId, lifeStoneLevel, statsCount);
				
				// show the enchant menu after an action
				showMainPage(activeChar);
				return true;
			}
			case admin_removeaugment:
			{
				removeAugment(activeChar);
				break;
			}
			
		}
		
		// show the enchant menu after an action
		showMainPage(activeChar);
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param augmentId
	 * @param lifeStoneLevel : the level of the life stone from 1 to 10. Higher level means higher stats
	 * @param statsCount : how many stats it should give. 0 or 1.
	 */
	private void augmentWeapon(Player activeChar, int augmentId, int lifeStoneLevel, int statsCount)
	{
		GameObject target = activeChar.getTarget();
		if (target == null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.notarget", activeChar));
			return;
		}
		if (!target.isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.wrongtarget", activeChar));
			return;
		}
		
		int STAT_SUBBLOCKSIZE = 91;
		int STAT_BLOCKSIZE = 3640;
		
		int offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + (statsCount * STAT_BLOCKSIZE) + (((lifeStoneLevel + 3) / 2) * 10 * STAT_SUBBLOCKSIZE) + 1;
		int stat12 = Rnd.get(offset, (offset + STAT_SUBBLOCKSIZE) - 1);
		int augmentationData = ((augmentId << 16) + stat12);
		ItemInstance targetItem = target.getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		
		if (targetItem == null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.noweapon", activeChar));
			return;
		}
		
		if (targetItem.isNotAugmented())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.cannot_be_augmented", activeChar));
			return;
		}
		
		if (!targetItem.canBeAugmented())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.restrictions", activeChar));
			return;
		}
		
		boolean equipped = targetItem.isEquipped();
		if (equipped)
		{
			target.getPlayer().getInventory().unEquipItem(targetItem);
		}
		
		targetItem.setAugmentationId(augmentationData);
		targetItem.setJdbcState(JdbcEntityState.UPDATED);
		targetItem.update();
		
		if (equipped)
		{
			target.getPlayer().getInventory().equipItem(targetItem);
		}
		
		target.getPlayer().sendPacket(new InventoryUpdate().addModifiedItem(targetItem));
		
		for (ShortCut sc : target.getPlayer().getAllShortCuts())
		{
			if ((sc.getId() == targetItem.getObjectId()) && (sc.getType() == ShortCut.TYPE_ITEM))
			{
				target.getPlayer().sendPacket(new ShortCutRegister(target.getPlayer(), sc));
			}
		}
		target.getPlayer().sendChanges();
		
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.success", activeChar).addString(target.getPlayer().getName()));
	}
	
	private void removeAugment(Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		if (target == null)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.notarget", activeChar));
			return;
		}
		if (!target.isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.wrongtarget", activeChar));
			return;
		}
		
		ItemInstance targetItem = target.getPlayer().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		
		// cannot remove augmentation from a not augmented item
		if ((targetItem == null) || !targetItem.isAugmented())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.noaugment", activeChar));
			return;
		}
		
		boolean equipped = targetItem.isEquipped();
		if (equipped)
		{
			activeChar.getInventory().unEquipItem(targetItem);
		}
		
		// remove the augmentation
		targetItem.setAugmentationId(0);
		targetItem.setJdbcState(JdbcEntityState.UPDATED);
		targetItem.update();
		
		if (equipped)
		{
			target.getPlayer().getInventory().equipItem(targetItem);
		}
		
		target.getPlayer().sendPacket(new InventoryUpdate().addModifiedItem(targetItem));
		
		for (ShortCut sc : target.getPlayer().getAllShortCuts())
		{
			if ((sc.getId() == targetItem.getObjectId()) && (sc.getType() == ShortCut.TYPE_ITEM))
			{
				target.getPlayer().sendPacket(new ShortCutRegister(target.getPlayer(), sc));
			}
		}
		target.getPlayer().sendChanges();
		
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminaugment.success_remove", activeChar));
	}
	
	private void showMainPage(Player activeChar)
	{
		// get the target
		GameObject target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}
		Player player = activeChar;
		if (target.isPlayer())
		{
			player = (Player) target;
		}
		
		String html = HtmCache.getInstance().getNotNull("admin/augment.htm", player);
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		adminReply.setHtml(html);
		adminReply.replace("%playerName", player.getName());
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}