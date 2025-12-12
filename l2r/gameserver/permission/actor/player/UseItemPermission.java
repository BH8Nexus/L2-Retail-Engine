package l2r.gameserver.permission.actor.player;

import l2r.gameserver.model.Playable;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.permission.PlayablePermission;

public interface UseItemPermission extends PlayablePermission
{
	boolean canUseItem(Playable p0, ItemInstance p1, boolean p2);
	
	void sendPermissionDeniedError(Playable p0, ItemInstance p1, boolean p2);
}
