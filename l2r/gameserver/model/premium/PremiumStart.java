package l2r.gameserver.model.premium;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.PremiumHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2r.gameserver.utils.TimeUtils;

public class PremiumStart
{
	private static PremiumStart _instance = new PremiumStart();
	
	public static PremiumStart getInstance()
	{
		return _instance;
	}
	
	public void start(Player player)
	{
		if (Config.PREMIUM_ACCOUNT_TYPE != Bonus.NO_BONUS)
		{
			GameClient client = player.getClient();
			if (client == null)
			{
				return;
			}
			int bonusExpire = client.getBonusExpire();
			int id = client.getBonus();
			Bonus bonus = player.getBonus();
			PremiumAccount premium = PremiumHolder.getInstance().getPremium(id);
			if ((premium != null) && (bonusExpire > (System.currentTimeMillis() / 1000L)))
			{
				bonus.setRateXp(premium.getExp());
				bonus.setRateSp(premium.getSp());
				bonus.setDropSiege(premium.getEpaulette());
				bonus.setDropAdena(premium.getAdena());
				bonus.setDropItems(premium.getItems());
				bonus.setDropSpoil(premium.getSpoil());
				bonus.setWeight(premium.getWeight());
				bonus.setCraftChance(premium.getCraftChance());
				bonus.setMasterWorkChance(premium.getMasterWorkChance());
				bonus.setAttributeChance(premium.getAttributeChance());
				
				bonus.setBonusExpire(bonusExpire);
				if (Config.ENTER_WORLD_SHOW_HTML_PREMIUM_ACTIVE)
				{
					String msg = "Premium Account is ACTIVE, Expires at: " + TimeUtils.formatTime((int) (bonusExpire - (System.currentTimeMillis() / 1000)));
					player.sendPacket(new ExShowScreenMessage(msg, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
					player.sendMessage(msg);
				}
				if (player.getExpiration() == null)
				{
					player.setExpiration(LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(player));
				}
				player.deleteQuickVar("PremiumEnd");
			}
			else
			{
				PremiumEnd.getInstance().done(player);
			}
		}
	}
	
	public void updateItems(boolean remove, Player player)
	{
		if (remove)
		{
			PremiumRemoveItems.getInstance().remove(player);
		}
		else
		{
			PremiumAddItems.getInstance().add(player);
		}
	}
}
