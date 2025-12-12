package l2r.gameserver.model.items.listeners;

import l2r.gameserver.model.items.ItemInstance;

public interface PaperdollListener
{
	public void notifyEquipped(int slot, ItemInstance inst, boolean update_icon);
	
	public void notifyUnequipped(int slot, ItemInstance inst, boolean update_icon);
}
