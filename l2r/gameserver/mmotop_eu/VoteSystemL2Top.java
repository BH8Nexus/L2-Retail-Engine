package l2r.gameserver.mmotop_eu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastMap;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;

public class VoteSystemL2Top
{
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteSystemL2Top.class);
	private static final int checkTime  = 60 * 1000 * Config.L2TOP_REWARD_CHECK_TIME;
	private static int lastVotes = 0;
	private static final int voteRewardVotes = Config.L2TOP_REWARD_VOTES;
	private static final FastMap<String, Integer> playerIps = new FastMap<>();
	
	public static void getInstance()
	{
		LOGGER.info("L2Top.co: Vote reward System initialized");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (Config.ALLOW_L2TOP_VOTE_REWARD)
			{
				reward();
			}
		}, checkTime / 2, checkTime);
	}
	
	
	private static void reward()
	{
		int currentVotes = getVotes();
		
		if(currentVotes == -1)
		{
			LOGGER.info("L2Top.co: There was a problem on getting server votes. ");
		}
		
		if(lastVotes == 0)
		{
			lastVotes = currentVotes;
			
			Announcements.getInstance().announceToAll("L2Top.co: Current votes: " + currentVotes + " . " );
			Announcements.getInstance().announceToAll("L2Top.co: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			if (Config.L2TOP_REPORT_LOG)
			{	
				LOGGER.info("Server vote on L2Top.co: " + currentVotes);
				LOGGER.info("Votes needed for reward: " + ((lastVotes + voteRewardVotes) -currentVotes));
			}
			return;
		}
		
		if (currentVotes >= (lastVotes + voteRewardVotes))
		{
			if(Config.L2TOP_REPORT_LOG)
			{
				LOGGER.info("Server vote on L2Top.co " + currentVotes);
				LOGGER.info("Vote needed for next reward:  ");
				LOGGER.info("Votes needed for next reward: " + ((currentVotes + voteRewardVotes)-currentVotes));
			}
			Announcements.getInstance().announceToAll("L2Top: Thanks for voting! Players rewarded!.");
			Announcements.getInstance().announceToAll("L2Top: Current votes:  " + currentVotes + " . ");
			
			for (Player player : GameObjectsStorage.getAllPlayers())
			{
				boolean canReward = false;
				String pIp = player.getIP();
				
				if(playerIps.containsKey(pIp))
				{
					int count = playerIps.get(pIp);
					if(count < Config.L2TOP_DUAL_BOX)
					{
						playerIps.remove(pIp);
						playerIps.put(pIp, count + 1);
						canReward = true;
					}
				}
				else {
					canReward = true;
					playerIps.put(pIp, 1);
				}
				if (canReward)
				{
					addItem(player,Config.L2TOP_ITEM_REWARD,Config.L2TOP_ITEM_COUNT);
					player.sendMessage("You have received an award for his voice to the L2Top.co in the amount of " + Config.L2TOP_ITEM_COUNT);
				}
				else {
					player.sendMessage("Already " + Config.L2TOP_DUAL_BOX + "character(s) of your ip have been rewarded,so this character won't be rewarded.");
				}
			}
			playerIps.clear();
			lastVotes = currentVotes;
		}
		else
		{
			if(currentVotes <= (lastVotes + voteRewardVotes))
			{
				if(Config.L2TOP_REPORT_LOG)
				{
					LOGGER.info("Server vote on L2Top " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("L2Top: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("L2Top: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			}
			else {
				if(Config.L2TOP_REPORT_LOG)
				{
					LOGGER.info("Server vote on L2Top " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("L2Top: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("L2Top: Next reward at  " + ((lastVotes + voteRewardVotes) - currentVotes));

			}
		}
	}
	
	private static void addItem(Player player, int i, int j) {
		// TODO Auto-generated method stub
		Functions.addItem(player, i, j, "Reward Vote");
		
	}


	private static int getVotes()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			final URLConnection con = new URL(Config.L2TOP_SERVER_LINK).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.matches("\\s+\\d+</li>"))
				{
					return Integer.valueOf(line.replace("</li>", "").replaceAll("\t", ""));
				}
			}
			
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			LOGGER.info("VoteSystem: Error while getting server vote count from " + getSiteName() + ".");
		}
		
		return -1;
	}
	private static  String getSiteName() {
		// TODO Auto-generated method stub
		return "l2Top.com";
	}
}
