package l2r.gameserver.handler.vote;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.impl.Vote;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;

public class VoteTopzone extends VoteBase
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
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Topzone", "Thank you! Unfortunately, but this time you didn't get reward. Better luck next time."));
			// player.sendMessage("Thank you! Unfortunately, but this time you didn't get reward. Better luck next time.");
		}
		
		Vote.showHtm(player);
	}
	
	@Override
	public String getApiEndpoint(Player player)
	{
		return String.format("https://api.l2topzone.com/v1/vote?token=%s&ip=%s", Config.VOTE_TOPZONE_APIKEY, getPlayerIp(player));
	}
	
	@Override
	public void setVoted(Player player)
	{
		player.setLastTopVote(System.currentTimeMillis());
	}
}