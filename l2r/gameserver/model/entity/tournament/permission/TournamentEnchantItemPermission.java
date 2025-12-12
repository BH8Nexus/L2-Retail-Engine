package l2r.gameserver.model.entity.tournament.permission;

import l2r.gameserver.ConfigHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.permission.actor.player.EnchantItemPermission;

public class TournamentEnchantItemPermission implements EnchantItemPermission
{
	@Override
	public boolean canEnchantItem(Player actor, ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
	{
		return ConfigHolder.getBool("TournamentAllowEnchanting");
	}
	
	@Override
	public void sendPermissionDeniedError(Player actor, ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
	{
		actor.sendCustomMessage("Tournament.NotAllowed.AttributeItem", new Object[0]);
	}
}
