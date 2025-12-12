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

public class VoteSystemL2Brazil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteSystemL2Brazil.class);
	private static int checkTime  = 60 * 1000 * Config.L2JBRASIL_REWARD_CHECK_TIME;
	private static int lastVotes = 0;
	private static int voteRewardVotes = Config.L2JBRASIL_REWARD_VOTES;
	private static FastMap<String, Integer> playerIps = new FastMap<>();
	
	public static void getInstance()
	{
		LOGGER.info("l2jbrasil.com: Vote reward System initialized");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (Config.ALLOW_L2JBRASIL_VOTE_REWARD)
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
				LOGGER.info("L2jbrasil.com: There was a problem on getting server votes. ");
			}
		}
		
		if(lastVotes == 0)
		{
			lastVotes = currentVotes;
			
			Announcements.getInstance().announceToAll("l2jbrasil.com: Current votes: " + currentVotes + " . " );
			Announcements.getInstance().announceToAll("l2jbrasil.com: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			if (Config.L2JBRASIL_REPORT_LOG)
			{	
				LOGGER.info("Server vote on l2jbrasil.com: " + currentVotes);
				LOGGER.info("Votes needed for reward: " + ((lastVotes + voteRewardVotes) -currentVotes));
			}
			return;
		}
		
		if (currentVotes >= (lastVotes + voteRewardVotes))
		{
			if(Config.L2JBRASIL_REPORT_LOG)
			{
				LOGGER.info("Server vote on l2jbrasil.com " + currentVotes);
				LOGGER.info("Vote needed for next reward:  ");
				LOGGER.info("Votes needed for next reward: " + ((currentVotes + voteRewardVotes)-currentVotes));
			}
			Announcements.getInstance().announceToAll("l2jbrasil.com: Thanks for voting! Players rewarded!.");
			Announcements.getInstance().announceToAll("l2jbrasil.com: Current votes:  " + currentVotes + " . ");
			
			for (Player player : GameObjectsStorage.getAllPlayers())
			{
				boolean canReward = false;
				String pIp = player.getIP();
				
				if(playerIps.containsKey(pIp))
				{
					int count = playerIps.get(pIp);
					if(count < Config.L2JBRASIL_DUAL_BOX)
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
					addItem(player,Config.L2JBRASIL_ITEM_REWARD,Config.L2JBRASIL_ITEM_COUNT);
					player.sendMessage("You have received an award for his voice to the l2jbrasil.com in the amount of " + Config.L2JBRASIL_ITEM_COUNT);
				}
				else {
					player.sendMessage("Already " + Config.L2JBRASIL_DUAL_BOX + "character(s) of your ip have been rewarded,so this character won't be rewarded.");
				}
			}
			playerIps.clear();
			lastVotes = currentVotes;
		}
		else
		{
			if(currentVotes <= (lastVotes + voteRewardVotes))
			{
				if(Config.L2JBRASIL_REPORT_LOG)
				{
					LOGGER.info("Server vote on l2jbrasil.com " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("l2jbrasil.com: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("l2jbrasil.com: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			}
			else {
				if(Config.L2JBRASIL_REPORT_LOG)
				{
					LOGGER.info("Server vote on l2jbrasil.com " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("l2jbrasil.com: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("l2jbrasil.com: Next reward at  " + ((lastVotes + voteRewardVotes) - currentVotes));

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
			URLConnection con = new URL(Config.L2JBRASIL_SERVER_LINK).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("<b>Entradas(Total):</b"))
				{
					String votesResult = String.valueOf(line.split(">")[2].replace("<br /", ""));
					int votes = Integer.valueOf(votesResult.replace(" ", ""));
					return votes;
				}
			}
			
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOGGER.warn("VoteSystemL2Brazil: Error while getting server vote count from " + getSiteName() + ".");
		}
		
		return -1;
	}
	
	private static  String getSiteName() {
		// TODO Auto-generated method stub
		return "L2JBrasil";
	}
}
