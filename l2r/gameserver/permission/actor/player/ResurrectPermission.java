package l2r.gameserver.permission.actor.player;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.permission.PlayerPermission;

public interface ResurrectPermission extends PlayerPermission
{
	boolean canResurrect(Player p0, Creature p1, boolean p2, boolean p3);
	
	void sendPermissionDeniedError(Player p0, Creature p1, boolean p2, boolean p3);
}
