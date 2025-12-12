package l2r.gameserver.mmotop_eu;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import l2r.commons.configuration.ExProperties;

public class VoteConfig
{
	private static Logger _log = Logger.getLogger(VoteConfig.class.getName());

	public static boolean MMOTOP_USE_VOTE_MANAGER;
	public static String MMOTOP_API_KEY;
	public static long MMOTOP_VOTE_CHECK_DELAY;
	public static int MMOTOP_VOTE_STEP;
	public static int MMOTOP_REWARD_ITEM;
	public static int MMOTOP_REWARD_COUNT;
	public static boolean MMOTOP_REWARD_EACH_STEP;
	
	
	public static final ExProperties initProperties(String filename)
	{
		ExProperties result = new ExProperties();		
		try { result.load(new File(filename)); }
		catch(IOException e) { _log.warning("VoteConfig: Error loading \"" + filename + "\" config."); }		
		return result;
	}
	
	public static void Load()
	{
		ExProperties ep = initProperties("./config/mmotop_eu.properties");
		
		MMOTOP_USE_VOTE_MANAGER = ep.getProperty("UseMmotopVoteManager", false);
		MMOTOP_API_KEY = ep.getProperty("ApiKey", "0123456789abcdef");
		MMOTOP_VOTE_CHECK_DELAY = ep.getProperty("CheckDelay", 300);
		if(MMOTOP_VOTE_CHECK_DELAY < 300)
			MMOTOP_VOTE_CHECK_DELAY = 300;
		MMOTOP_VOTE_STEP = ep.getProperty("RewardInterval", 5);
		MMOTOP_REWARD_ITEM = ep.getProperty("RewardItemId", 57);
		MMOTOP_REWARD_COUNT = ep.getProperty("RewardCount", 1);
		MMOTOP_REWARD_EACH_STEP = ep.getProperty("RewardEachStep", false);
	}
}
