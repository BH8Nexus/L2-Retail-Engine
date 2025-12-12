package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket
{
	private int _type, _id, _slot, _page, _lvl, _characterType;
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_lvl = readD();
		_characterType = readD();
		
		_slot = slot % 12;
		_page = slot / 12;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_page < 0) || (_page > ShortCut.PAGE_MAX))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		ShortCut shortCut = new ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
		activeChar.sendPacket(new ShortCutRegister(activeChar, shortCut));
		activeChar.registerShortCut(shortCut, true);
		
		if (Config.AUTO_POTIONS) // Show auto item use (if any) when adding shortcut.
		{
			if (shortCut.getType() == ShortCut.TYPE_ITEM)
			{
				ItemInstance item = activeChar.getInventory().getItemByObjectId(shortCut.getId());
				if (item != null)
				{
					if (activeChar.getAutoItemUse(item.getItemId()))
					{
						sendPacket(new ExAutoSoulShot(item.getItemId(), true));
					}
				}
			}
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}