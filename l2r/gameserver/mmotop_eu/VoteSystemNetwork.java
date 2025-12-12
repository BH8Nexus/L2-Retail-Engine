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

public class VoteSystemNetwork
{
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteSystemNetwork.class);
	private static int checkTime  = 60 * 1000 * Config.L2NETWORK_REWARD_CHECK_TIME;
	private static int lastVotes = 0;
	private static int voteRewardVotes = Config.L2NETWORK_REWARD_VOTES;
	private static FastMap<String, Integer> playerIps = new FastMap<>();
	
	public static void getInstance()
	{
		LOGGER.info("l2network.eu: Vote reward System initialized");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (Config.ALLOW_L2NETWORK_VOTE_REWARD)
				{
					reward();
				}
				else
				{
					return;
				}
			}
		}, checkTime / 2, checkTime);
	}
	
	
	private static void reward()
	{
		int currentVotes = getVotes();
		
		if(currentVotes == -1)
		{
			if (currentVotes == -1)
			{
				LOGGER.info("l2network.eu: There was a problem on getting server votes. ");
			}
		}
		
		if(lastVotes == 0)
		{
			lastVotes = currentVotes;
			
			Announcements.getInstance().announceToAll("l2network.eu: Current votes: " + currentVotes + " . " );
			Announcements.getInstance().announceToAll("l2network.eu: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			if (Config.L2NETWORK_REPORT_LOG)
			{	
				LOGGER.info("Server vote on l2network.eu: " + currentVotes);
				LOGGER.info("Votes needed for reward: " + ((lastVotes + voteRewardVotes) -currentVotes));
			}
			return;
		}
		
		if (currentVotes >= (lastVotes + voteRewardVotes))
		{
			if(Config.L2NETWORK_REPORT_LOG)
			{
				LOGGER.info("Server vote on l2network.eu " + currentVotes);
				LOGGER.info("Vote needed for next reward:  ");
				LOGGER.info("Votes needed for next reward: " + ((currentVotes + voteRewardVotes)-currentVotes));
			}
			Announcements.getInstance().announceToAll("l2network.eu: Thanks for voting! Players rewarded!.");
			Announcements.getInstance().announceToAll("l2network.eu: Current votes:  " + currentVotes + " . ");
			
			for (Player player : GameObjectsStorage.getAllPlayers())
			{
				boolean canReward = false;
				String pIp = player.getIP();
				
				if(playerIps.containsKey(pIp))
				{
					int count = playerIps.get(pIp);
					if(count < Config.L2NETWORK_DUAL_BOX)
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
					addItem(player,Config.L2NETWORK_ITEM_REWARD,Config.L2NETWORK_ITEM_COUNT);
					player.sendMessage("You have received an award for his voice to the l2network.eu in the amount of " + Config.L2NETWORK_ITEM_COUNT);
				}
				else {
					player.sendMessage("Already " + Config.L2NETWORK_DUAL_BOX + "character(s) of your ip have been rewarded,so this character won't be rewarded.");
				}
			}
			playerIps.clear();
			lastVotes = currentVotes;
		}
		else
		{
			if(currentVotes <= (lastVotes + voteRewardVotes))
			{
				if(Config.L2NETWORK_REPORT_LOG)
				{
					LOGGER.info("Server vote on l2network.eu " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("l2network.eu: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("l2network.eu: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			}
			else {
				if(Config.L2NETWORK_REPORT_LOG)
				{
					LOGGER.info("Server vote on l2network.eu " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("l2network.eu: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("l2network.eu: Next reward at  " + ((lastVotes + voteRewardVotes) - currentVotes));

			}
		}
	}
	
	private static void addItem(Player player, int i, int j) {
		// TODO Auto-generated method stub
		Functions.addItem(player, i, j, "Reward Vote");
		
	}


	public static int getVotes()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			final URLConnection con = new URL(Config.L2NETWORK_SERVER_LINK).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("<div class=\"tls-in-sts\"><b style"))
				{
					return Integer.parseInt(line.split(">")[2].replace("</b", ""));
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
	
	public static String getSiteName()
	{
		return "L2network.eu";
	}
}
