package l2r.gameserver.mmotop_eu;

import javolution.util.FastMap;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.scripts.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VoteSystemHopzone
{
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteSystemHopzone.class);
	private static final int checkTime  = 60 * 1000 * Config.L2HOPZONE_REWARD_CHECK_TIME;
	private static int lastVotes = 0;
	private static final int voteRewardVotes = Config.L2HOPZONE_REWARD_VOTES;
	private static final FastMap<String, Integer> playerIps = new FastMap<>();
	
	public static void getInstance()
	{
		LOGGER.info("Hopzone.net: Vote reward System initialized");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (Config.ALLOW_L2HOPZONE_VOTE_REWARD)
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
				LOGGER.info("Hopzone.net: There was a problem on getting server votes. ");
		}
		
		if(lastVotes == 0)
		{
			lastVotes = currentVotes;
			
			Announcements.getInstance().announceToAll("Hopzone.net: Current votes: " + currentVotes + " . " );
			Announcements.getInstance().announceToAll("Hopzone.net: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			if (Config.L2HOPZONE_REPORT_LOG)
			{	
				LOGGER.info("Server vote on Hopzone.net: " + currentVotes);
				LOGGER.info("Votes needed for reward: " + ((lastVotes + voteRewardVotes) -currentVotes));
			}
			return;
		}
		
		if (currentVotes >= (lastVotes + voteRewardVotes))
		{
			if(Config.L2HOPZONE_REPORT_LOG)
			{
				LOGGER.info("Server vote on Hopzone.net " + currentVotes);
				LOGGER.info("Vote needed for next reward:  ");
				LOGGER.info("Votes needed for next reward: " + ((currentVotes + voteRewardVotes)-currentVotes));
			}
			Announcements.getInstance().announceToAll("Hopzone.net: Thanks for voting! Players rewarded!.");
			Announcements.getInstance().announceToAll("Hopzone.net: Current votes:  " + currentVotes + " . ");
			
			for (Player player : GameObjectsStorage.getAllPlayers())
			{
				boolean canReward = false;
				String pIp = player.getIP();
				
				if(playerIps.containsKey(pIp))
				{
					int count = playerIps.get(pIp);
					if(count < Config.L2HOPZONE_DUAL_BOX)
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
					addItem(player,Config.L2HOPZONE_ITEM_REWARD,Config.L2HOPZONE_ITEM_COUNT);
					player.sendMessage("You have received an award for his voice to the Hopzone.net in the amount of " + Config.L2HOPZONE_ITEM_COUNT);
				}
				else {
					player.sendMessage("Already " + Config.L2HOPZONE_DUAL_BOX + "character(s) of your ip have been rewarded,so this character won't be rewarded.");
				}
			}
			playerIps.clear();
			lastVotes = currentVotes;
		}
		else
		{
			if(currentVotes <= (lastVotes + voteRewardVotes))
			{
				if(Config.L2HOPZONE_REPORT_LOG)
				{
					LOGGER.info("Server vote on Hopzone.net " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("Hopzone.net: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("Hopzone.net: Next reward at " + ((lastVotes + voteRewardVotes) - currentVotes));
			}
			else {
				if(Config.L2HOPZONE_REPORT_LOG)
				{
					LOGGER.info("Server vote on Hopzone.net " + currentVotes);
					LOGGER.info("Votes needed for next reward " + ((lastVotes + voteRewardVotes) - currentVotes));
				}
				Announcements.getInstance().announceToAll("Hopzone.net: Current votes: " + currentVotes + ".");
				Announcements.getInstance().announceToAll("Hopzone.net: Next reward at  " + ((lastVotes + voteRewardVotes) - currentVotes));

			}
		}
	}
	
	private static void addItem(Player player, int i, int j) {
		// TODO Auto-generated method stub
		Functions.addItem(player, i, j, "Reward Vote");
		
	}


	private static int getVotes()
	{
		int votes = -1;

		try
		{
			//final URL obj = new URL("https://mmotop.eu/l2/data/" + VoteConfig.MMOTOP_API_KEY + "/info/");
			final URL obj = new URL (Config.L2HOPZONE_SERVER_LINK );
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			con.setConnectTimeout(5000);

			int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{

					String keyWord = "<span class=\"rank tooltip hidden\" title=\"Total Votes\">";

					String line;
					while ((line = in.readLine()) != null)
					{
						if (line.contains(keyWord))
						{
							votes = Integer.parseInt(line.split(keyWord)[1].split("</span>")[0]);
							break;
						}
					}
//					String inputLine;
//					StringBuilder response = new StringBuilder();
//
//					while((inputLine = in.readLine()) != null)
//					{
//						response.append(inputLine);
//					}
//
//					JSON myre = new JSON(response.toString());
//
//					if(myre.getString("totalvotes") !=null)
//					{
//						//System.out.println("totalVotes " + myre.getString("totalvotes"));
//						votes = myre.getInteger("totalvotes");//myre.getString("totalvotes");
//					}

				}
			}
			else throw new Exception("Server returned unexpected answer!");
		}
		catch (Exception e)
		{
			//  LOGGER.info("[Hopzone] Failed to get votes count! " + e.getMessage());
		}
		return votes;
	}


//	private static int getVotes()
//	{
//		int votes = -1;
//
//		try
//		{
//			//final URL obj = new URL("https://mmotop.eu/l2/data/" + VoteConfig.MMOTOP_API_KEY + "/info/");
//			final URL obj = new URL (Config.L2HOPZONE_SERVER_LINK );
//			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//			con.addRequestProperty("User-Agent", "Mozilla/5.0");
//			con.setConnectTimeout(5000);
//
//			int responseCode = con.getResponseCode();
//			if (responseCode == 200)
//			{
//				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
//				{
//					String inputLine;
//					StringBuilder response = new StringBuilder();
//
//					while((inputLine = in.readLine()) != null)
//					{
//						response.append(inputLine);
//					}
//
//					JSON myre = new JSON(response.toString());
//
//					if(myre.getString("totalvotes") !=null)
//					{
//						//System.out.println("totalVotes " + myre.getString("totalvotes"));
//						votes = myre.getInteger("totalvotes");//myre.getString("totalvotes");
//					}
//
//				}
//			}
//			else throw new Exception("Server returned unexpected answer!");
//		}
//		catch (Exception e)
//		{
//			//  LOGGER.info("[Hopzone] Failed to get votes count! " + e.getMessage());
//		}
//		return votes;
//	}

	public static String getSiteName()
	{
		return "Hopzone.net";
	}
}
