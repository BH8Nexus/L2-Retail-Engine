package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.items.ItemInstance;

public class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot;
	private int _page;
	
	/**
	 * packet type id 0x3F format: cd
	 */
	@Override
	protected void readImpl()
	{
		int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.AUTO_POTIONS) // Disable auto item use when removing from shortcut.
		{
			ShortCut sc = activeChar.getShortCut(_slot, _page);
			if ((sc != null) && (sc.getType() == ShortCut.TYPE_ITEM))
			{
				ItemInstance item = activeChar.getInventory().getItemByObjectId(sc.getId());
				if (item != null)
				{
					if (activeChar.getAutoItemUse(item.getItemId()))
					{
						activeChar.stopAutoItemUse(item.getItemId());
					}
				}
			}
		}
		
		// client dont needs confirmation. this packet is just to inform the server
		activeChar.deleteShortCut(_slot, _page);
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}