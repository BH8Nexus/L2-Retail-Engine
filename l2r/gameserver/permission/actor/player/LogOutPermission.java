package l2r.gameserver.permission.actor.player;

import l2r.gameserver.model.Player;
import l2r.gameserver.permission.PlayerPermission;

public interface LogOutPermission extends PlayerPermission
{
	boolean canLogOut(Player p0, boolean p1);
	
	void sendPermissionDeniedError(Player p0, boolean p1);
}
