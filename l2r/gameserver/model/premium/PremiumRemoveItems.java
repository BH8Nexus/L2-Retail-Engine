package l2r.gameserver.model.premium;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.ItemFunctions;

public class PremiumRemoveItems
{
	private static PremiumRemoveItems _instance = new PremiumRemoveItems();
	
	public static PremiumRemoveItems getInstance()
	{
		return _instance;
	}
	
	private final List<PremiumGift> _list = new ArrayList<>();
	
	protected void remove(Player player)
	{
		boolean removed = false;
		for (PremiumGift gift : _list)
		{
			ItemFunctions.removeItem(player, gift.getId(), gift.getCount(), true, "removed");
		}
		
		if (removed)
		{
			player.sendPacket(new SystemMessage(SystemMsg.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED));
		}
	}
	
	public void add(PremiumGift gift)
	{
		_list.add(gift);
	}
}
