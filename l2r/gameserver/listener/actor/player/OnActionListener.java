package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.NpcListener;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;

public interface OnActionListener extends NpcListener
{
	/**
	 * 
	 * @param player
	 * @param obj
	 * @param shift
	 * @return true to block action.
	 */
	public boolean onAction(Player player, GameObject obj, boolean shift);
}
