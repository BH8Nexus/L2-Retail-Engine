package l2r.gameserver.handler.vote;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.impl.Vote;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class VoteHopzone extends VoteBase
{
	@Override
	public void reward(Player player)
	{
		if (Rnd.get(100) < Config.VOTE_REWARD_CHANCE)
		{
			Vote.giveReward(player);
		}
		else
		{
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Hopzone", "Thank you! Unfortunately, but this time you didn't get reward. Better luck next time."));
			// player.sendMessage("Thank you! Unfortunately, but this time you didn't get reward. Better luck next time.");
		}
		
		Vote.showHtm(player);
	}
	
	@Override
	public String getApiEndpoint(Player player)
	{
		return String.format("https://api.hopzone.net/lineage2/vote?token=%s&ip_address=%s", Config.VOTE_HOPZONE_APIKEY, getPlayerIp(player));
	}
	
	@Override
	public void setVoted(Player player)
	{
		player.setLastHopVote(System.currentTimeMillis());
	}
}