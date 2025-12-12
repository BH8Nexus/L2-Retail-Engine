package l2r.gameserver.mmotop_eu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ItemTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mmotop_vote extends Functions implements ScriptFile, Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(Mmotop_vote.class);
	
		private static volatile Mmotop_vote _instance = null;
	
	public static Mmotop_vote getInstance()
	{
		if(_instance == null)
		{
			synchronized(Mmotop_vote.class)
			{
				if(_instance == null)
				{
					Mmotop_vote _local = new Mmotop_vote();
					_instance = _local;
				}
			}
		}
		return _instance;
	}
	
	
	private int saved = 0;
	
	private Mmotop_vote()
	{
		VoteConfig.Load();
		if(VoteConfig.MMOTOP_USE_VOTE_MANAGER)
		{
			template = ItemHolder.getInstance().getTemplate(VoteConfig.MMOTOP_REWARD_ITEM);
			if(template == null)
			{
				_log.info("[l2jtop.com] Failed to start VoteManager! Item with ID = " + VoteConfig.MMOTOP_REWARD_ITEM + " not found! Please check mmotop_eu.properties file!");
				return;
			}
			int votes = getVotes();
			saved = (votes - (votes % VoteConfig.MMOTOP_VOTE_STEP));
			ThreadPoolManager.getInstance().scheduleAtFixedRate(this, VoteConfig.MMOTOP_VOTE_CHECK_DELAY * 1000, VoteConfig.MMOTOP_VOTE_CHECK_DELAY * 1000);
		}
	}
	
	private static int getVotes()
	{		
		int votes = -1;
		
		try
		{
			final URL obj = new URL("https://l2jtop.com/api/" + VoteConfig.MMOTOP_API_KEY + "/info/");
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("User-Agent", "MMOTOP_EU VoteManager v1");
			con.setConnectTimeout(5000);
			
			int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					StringBuilder sb = new StringBuilder();
					String text = null;
					while((text = in.readLine()) != null)
						sb.append(text);
					
					JSON json = new JSON(sb.toString());
					if(json.getInteger("error") == 0)
						votes = json.getInteger("monthly_votes");
					else if(json.getString("description") != null)
						throw new Exception(json.getString("description"));
				}
			}
			else throw new Exception("Server returned unexpected answer!");
		}
		catch (Exception e)
		{
			_log.info("[l2jtop.com] Failed to get votes count! " + e.getMessage());
		}
		return votes;
	}
	
	private ItemTemplate template = null;
	
	@Override
	public void run()
	{
		int votes = getVotes();
		//int saved = ServerVariables.getInt("mmotop_eu_votes", -1);
		
		if(saved == -1)
		{
			ServerVariables.set("mmotop_eu_votes", votes);
			return;
		}
		
		if(votes != -1)
		{
			if(saved > votes)
			{
				saved = votes;
				ServerVariables.set("mmotop_eu_votes", saved);
			}
			
			Announcements.getInstance().announceToAll("[l2jtop.com] Current votes: " + votes, ChatType.CRITICAL_ANNOUNCE);
			
			if (votes >= saved + VoteConfig.MMOTOP_VOTE_STEP)
			{
				int count = VoteConfig.MMOTOP_REWARD_COUNT;
				
				if(VoteConfig.MMOTOP_REWARD_EACH_STEP)
				{
					int mul = 0;
					
					while(votes >= saved + VoteConfig.MMOTOP_VOTE_STEP)
					{
						saved += VoteConfig.MMOTOP_VOTE_STEP;
						mul++;
					}
					
					count = mul * VoteConfig.MMOTOP_REWARD_COUNT;
				}
				else saved = votes;
				
				final List<String> ips = new ArrayList<>();
				
				for(Player player : GameObjectsStorage.getAllPlayers())
				{
					String ip = null;
					try
					{
						ip = player.getIP();
					}
					catch(Exception e)
					{
						
					}
					
					if(ip == null || ips.contains(ip))
						continue;
					
					try
					{
						Functions.addItem(player, VoteConfig.MMOTOP_REWARD_ITEM, count,"");
						ips.add(ip);
					}
					catch(Exception e)
					{
						_log.info("[l2jtop.com] Failed to reward player " + player.getName() + ". Reason: " + e.getMessage());
					}
				}

				Announcements.getInstance().announceToAll("[l2jtop.com] Thanks for voting! Players rewarded!", ChatType.CRITICAL_ANNOUNCE);
				ServerVariables.set("mmotop_eu_votes", saved);
			}
		}
		Announcements.getInstance().announceToAll("[l2jtop.com] Next reward at " + (saved + VoteConfig.MMOTOP_VOTE_STEP) + " votes!", ChatType.CRITICAL_ANNOUNCE);
	}
	
	@Override
	public void onLoad()
	{
		VoteConfig.Load();
		if(VoteConfig.MMOTOP_USE_VOTE_MANAGER)
		{
			template = ItemHolder.getInstance().getTemplate(VoteConfig.MMOTOP_REWARD_ITEM);
			if(template == null)
			{
				_log.info("[l2jtop.com] Failed to start VoteManager! Item with ID = " + VoteConfig.MMOTOP_REWARD_ITEM + " not found! Please check mmotop_eu.properties file!");
				return;
			}
			ThreadPoolManager.getInstance().scheduleAtFixedRate(this, VoteConfig.MMOTOP_VOTE_CHECK_DELAY * 1000, VoteConfig.MMOTOP_VOTE_CHECK_DELAY * 1000);
		}
	}

	@Override
	public void onReload() { }

	@Override
	public void onShutdown() { }
}
