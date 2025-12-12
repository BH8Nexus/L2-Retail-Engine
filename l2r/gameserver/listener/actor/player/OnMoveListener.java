package l2r.gameserver.listener.actor.player;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Location;

public interface OnMoveListener extends PlayerListener
{
	void onMove(Player actor, Location tPos);
}
