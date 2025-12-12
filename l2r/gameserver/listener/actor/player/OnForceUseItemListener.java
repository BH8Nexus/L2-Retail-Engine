package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.NpcListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;

public interface OnForceUseItemListener extends NpcListener
{
	public boolean useItemForce(Player player, ItemInstance item);
}
