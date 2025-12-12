package l2r.gameserver.model.entity.tournament.permission;

import l2r.gameserver.data.StringHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.permission.actor.player.JoinFightClubPermission;

public class TournamentJoinFightClubPermission implements JoinFightClubPermission
{
	@Override
	public boolean joinSignFightClub(Player actor)
	{
		return false;
	}
	
	@Override
	public String getPermissionDeniedError(Player actor)
	{
		return StringHolder.getNotNull(actor, "Tournament.NotAllowed.JoinFightClub", new Object[0]);
	}
}
