package l2r.gameserver.model.entity.tournament.permission;

import l2r.gameserver.model.Player;
import l2r.gameserver.permission.actor.player.LogOutPermission;

public class TournamentLogOutPermission implements LogOutPermission
{
	@Override
	public boolean canLogOut(Player actor, boolean isRestart)
	{
		return false;
	}
	
	@Override
	public void sendPermissionDeniedError(Player actor, boolean isRestart)
	{
		actor.sendCustomMessage("Tournament.NotAllowed.LogOut", new Object[0]);
	}
}
