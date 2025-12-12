package l2r.gameserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import l2r.gameserver.custom.FloodProtectorConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import javolution.util.FastMap;
import l2r.commons.lang.StatsUtils;
import l2r.commons.listener.Listener;
import l2r.commons.listener.ListenerList;
import l2r.commons.net.IPv4Filter;
import l2r.commons.net.nio.impl.SelectorStats;
import l2r.commons.net.nio.impl.SelectorThread;
import l2r.gameserver.achievements.AchievementNotification;
import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.achievements.PlayerCounters;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.cache.CrestCache;
import l2r.gameserver.cache.ImagesCache;
import l2r.gameserver.config.templates.HostInfo;
import l2r.gameserver.config.xml.ConfigParsers;
import l2r.gameserver.config.xml.holder.HostsConfigHolder;
import l2r.gameserver.custom.balancer.main.MyMain;
import l2r.gameserver.dao.ChampionTemplateTable;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.EmotionsTable;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.dao.OfflineBuffersTable;
import l2r.gameserver.dao.PromotionCheck;
import l2r.gameserver.data.BoatHolder;
import l2r.gameserver.data.xml.Parsers;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.data.xml.holder.StaticObjectHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.database.EnchantNamesTable;
import l2r.gameserver.facebook.ActionsExtractingManager;
import l2r.gameserver.facebook.CompletedTasksHistory;
import l2r.gameserver.facebook.FacebookAutoAnnouncement;
import l2r.gameserver.facebook.FacebookProfilesHolder;
import l2r.gameserver.facebook.OfficialPostsHolder;
import l2r.gameserver.forum.ForumParser;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.handler.usercommands.UserCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.AutoAnnounce;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.instancemanager.AutoSpawnManager;
import l2r.gameserver.instancemanager.BloodAltarManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.instancemanager.PlayerMessageStack;
import l2r.gameserver.instancemanager.PremiumWelcome;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.instancemanager.SoIManager;
import l2r.gameserver.instancemanager.SpawnManager;
import l2r.gameserver.instancemanager.VoteManager;
import l2r.gameserver.instancemanager.games.ActivityReward;
import l2r.gameserver.instancemanager.games.DonationBonusDay;
import l2r.gameserver.instancemanager.games.FishingChampionShipManager;
import l2r.gameserver.instancemanager.games.LotteryManager;
import l2r.gameserver.instancemanager.games.MiniGameScoreManager;
import l2r.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2r.gameserver.instancemanager.naia.NaiaCoreManager;
import l2r.gameserver.instancemanager.naia.NaiaTowerManager;
import l2r.gameserver.listener.GameListener;
import l2r.gameserver.listener.game.OnConfigsReloaded;
import l2r.gameserver.listener.game.OnShutdownListener;
import l2r.gameserver.listener.game.OnStartListener;
import l2r.gameserver.mmotop_eu.Mmotop_vote;
import l2r.gameserver.mmotop_eu.VoteSystemHopzone;
import l2r.gameserver.mmotop_eu.VoteSystemL2Brazil;
import l2r.gameserver.mmotop_eu.VoteSystemL2Top;
import l2r.gameserver.model.AcademyList;
import l2r.gameserver.model.AcademyRewards;
import l2r.gameserver.model.PhantomPlayers;
import l2r.gameserver.model.World;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.MonsterRace;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.tournament.ActiveBattleManager;
import l2r.gameserver.model.entity.tournament.BattleScheduleManager;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.GamePacketHandler;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.randoms.CaptchaImage;
import l2r.gameserver.randoms.PlayerKill;
import l2r.gameserver.randoms.PlayerKillsLogManager;
import l2r.gameserver.randoms.Visualizer;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.tables.AugmentationData;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.tables.EnchantHPBonusTable;
import l2r.gameserver.tables.FishTable;
import l2r.gameserver.tables.LevelUpTable;
import l2r.gameserver.tables.PetSkillsTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.taskmanager.ItemsAutoDestroy;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2r.gameserver.utils.Debug;
import l2r.gameserver.utils.Strings;

public class GameServer
{
	public static boolean DEVELOP = false;
	public static final int AUTH_SERVER_PROTOCOL = 3;
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	private static final Map<String, String> _args = new FastMap<>();
	
	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for (Listener<GameServer> listener : getListeners())
			{
				if (OnStartListener.class.isInstance(listener))
				{
					((OnStartListener) listener).onStart();
				}
			}
		}
		
		public void onShutdown()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
		
//		public void onShutdown(Shutdown.ShutdownMode shutdownMode)
//		{
//			for (Listener<GameServer> listener : getListeners())
//			{
//				if (OnShutdownListener.class.isInstance(listener))
//				{
//					((OnShutdownListener) listener).onShutdown(shutdownMode);
//				}
//			}
//		}
//		
//		public void onAbortShutdown(Shutdown.ShutdownMode oldMode, int cancelledOnSecond)
//		{
//			for (Listener<GameServer> listener : getListeners())
//			{
//				if (OnAbortShutdownListener.class.isInstance(listener))
//				{
//					((OnAbortShutdownListener) listener).onAbortShutdown(oldMode, cancelledOnSecond);
//				}
//			}
//		}
//		
//		public void onShutdownScheduled()
//		{
//			for (Listener<GameServer> listener : getListeners())
//			{
//				if (OnShutdownCounterStartListener.class.isInstance(listener))
//				{
//					((OnShutdownCounterStartListener) listener).onCounterStart();
//				}
//			}
//		}
		
		public void onConfigsReloaded()
		{
			for (Listener<GameServer> listener : getListeners())
			{
				if (OnConfigsReloaded.class.isInstance(listener))
				{
					((OnConfigsReloaded) listener).onConfigsReloaded();
				}
			}
		}
	}
	
	public static GameServer _instance;
	public static Date server_started_date;
	private final List<SelectorThread<GameClient>> _selectorThreads = new ArrayList<>();
	private final GameServerListenerList _listeners;
	private boolean _hasLoaded;
	
	private final int _serverStarted;
	private final SelectorStats _selectorStats = new SelectorStats();
	private final String _licenseHost;
	private final int _onlineLimit;
	
	public List<SelectorThread<GameClient>> getSelectorThreads()
	{
		return _selectorThreads;
	}
	
	public int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}
	
	public int uptime()
	{
		return time() - _serverStarted;
	}
	
	public String getLicenseHost()
	{
		return _licenseHost;
	}
	
	public int getOnlineLimit()
	{
		return _onlineLimit;
	}
	
	public GameServer() throws Exception
	{
		_hasLoaded = false;
		long startMs = System.currentTimeMillis();
		_instance = this;
		_serverStarted = time();
		_listeners = new GameServerListenerList();
		
		new File("./log/").mkdir();
		
		// showVersion();
		// showLogo();
		
		// Initialize config
		printSection("Config");
		ConfigParsers.parseAll();
		Config.load();
		printSection("FloodProtectionConfig");
		FloodProtectorConfigs.loard();
		
		ConfigHolder.getInstance().reload();
		Debug.initListeners();
		
		printSection("");
		// Check binding address
		HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
		if (hosts.length == 0)
		{
			throw new Exception("Server hosts list is empty!");
		}
		
		final TIntSet ports = new TIntHashSet();
		for (HostInfo host : hosts)
		{
			if ((host.getIP() != null) || (host.getInnerIP() != null))
			{
				ports.add(host.getPort());
			}
		}
		
		int[] portsArray = ports.toArray();
		
		if (portsArray.length == 0)
		{
			throw new Exception("Server ports list is empty!");
		}
		
		// checkFreePorts();
		checkFreePorts(portsArray);
		_licenseHost = Config.EXTERNAL_HOSTNAME;
		_onlineLimit = Config.MAXIMUM_ONLINE_USERS;
		if (_onlineLimit == 0)
		{
			throw new Exception("Server online limit is zero!");
		}
		
		// Initialize database
		
//		Class.forName(Config.DATABASE_DRIVER).newInstance();
//		DatabaseFactory.getInstance().getConnection().close();
		DatabaseFactory.getInstance().doStart();
		
		IdFactory _idFactory = IdFactory.getInstance();
		if (!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		printSection("Scripts");
		Scripts.getInstance();
		printSection("Geodata");
		GeoEngine.load();
		printSection("");
		Strings.reload();
		printSection("World");
		GameTimeController.getInstance();
		World.init();
		printSection("Parsers");
		Parsers.parseAll();
		printSection("Skills");
		SkillTreeTable.getInstance();
		EnchantNamesTable.getInstance();
		printSection("Items");
		ItemsDAO.getInstance();
		printSection("");
		printSection("Clan Crests");
		CrestCache.getInstance();
		printSection("Loading Images");
		ImagesCache.getInstance();
		printSection("");
		CharacterDAO.getInstance();
		printSection("Clans");
		ClanTable.getInstance();
		if (Config.ENABLE_COMMUNITY_ACADEMY)
		{
			AcademyList.restore();
			AcademyRewards.getInstance().load();
		}
		printSection("FishTable");
		FishTable.getInstance();
		printSection("Augments");
		AugmentationData.getInstance();
		printSection("EnchantHPBonusTable");
		EnchantHPBonusTable.getInstance();
		printSection("LevelUpTable");
		LevelUpTable.getInstance();
		printSection("PetSkillsTable");
		PetSkillsTable.getInstance();
		printSection("ItemAuctionManager");
		ItemAuctionManager.getInstance();
		printSection("Scripts Initialization");
		Scripts.getInstance().init();
		printSection("Spawns");
		SpawnManager.getInstance().spawnAll();
		printSection("BoatHolder");
		BoatHolder.getInstance().spawnAll();
		printSection("StaticObjectHolder");
		StaticObjectHolder.getInstance().spawnAll();
		printSection("RaidBoss");
		RaidBossSpawnManager.getInstance();
		if (Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			ItemsAutoDestroy.getInstance();
		}
		printSection("MonsterRace");
		MonsterRace.getInstance();
		printSection("Seven Signs");
		DimensionalRiftManager.getInstance();
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().updateFestivalScore();
		AutoSpawnManager.getInstance();
		SevenSigns.getInstance().spawnSevenSignsNPC();
		
		printSection("Olympiad");
		if (Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}
		
		printSection("Handlers/Managers");
		if (Config.DEADLOCKCHECK_INTERVAL > 0)
		{
			new DeadlockDetector().start();
		}
		printSection("Announcements");
		Announcements.getInstance();
		printSection("LotteryManager");
		LotteryManager.getInstance();
		printSection("PlayerMessageStack");
		PlayerMessageStack.getInstance();
		printSection("PetitionManager");
		PetitionManager.getInstance();
		printSection("CursedWeaponsManager");
		CursedWeaponsManager.getInstance();
		if (!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionShipManager.getInstance();
		}
		ItemHandler.getInstance();
		MiniGameScoreManager.getInstance();
		
		printSection("CommandHandler");
		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		TaskManager.getInstance();
		AutoHuntingManager.getInstance();
		printSection("Residence/Events");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		CastleManorManager.getInstance();
		printSection("");
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		printSection("Hellbound");
		HellboundManager.getInstance();
		NaiaTowerManager.getInstance();
		NaiaCoreManager.getInstance();
		
		printSection("Gracia");
		SoDManager.getInstance();
		SoIManager.getInstance();
		BloodAltarManager.getInstance();
		printSection("");
		
		printSection("Loading Images");
		ImagesCache.getInstance();
		printSection("");
		
		printSection("Custom Shits");
		
		_log.info("Loading Player Tops...");
		// PlayerTops.getInstance();
		
		if (Config.ENABLE_PLAYER_COUNTERS)
		{
			PlayerCounters.checkTable();
			AchievementNotification.getInstance();
			
			if (Config.ENABLE_ACHIEVEMENTS)
			{
				Achievements.getInstance();
			}
		}
		
		if (Config.ENABLE_CUSTOM_AUCTION)
		{
			AuctionManager.init();
		}
		
		if (Config.ENABLE_POLL_SYSTEM)
		{
			VoteManager.getInstance();
			_log.info("Poll System loaded.");
		}
		ChampionTemplateTable.getInstance();
		
		if (Config.ACTIVITY_REWARD_ENABLED)
		{
			ActivityReward.getInstance();
			_log.info("Activity Reward loaded.");
		}
		
		_log.info("Loading Visualizer...");
		Visualizer.load();
		
		if (Config.ENABLE_EMOTIONS)
		{
			EmotionsTable.init();
			_log.info("Emotions Loaded....");
		}
		
		if (Config.ENABLE_PLAYER_KILL_SYSTEM)
		{
			PlayerKill.getInstance().init();
		}
		
		if (Config.ENABLE_PVP_PK_LOG)
		{
			PlayerKillsLogManager.getInstance();
			_log.info("PlayerPvPpkLog Manger started...");
		}
		
		if (Config.ENABLE_CAPTCHA)
		{
			new CaptchaImage();
			_log.info("Captcha system loaded.");
		}
		
		PromotionCheck.getInstance().loadHwid();
		_log.info("Promotion Hwid Check loaded.");
		
		if (!Config.DONTAUTOANNOUNCE)
		{
			ThreadPoolManager.getInstance().scheduleAtFixedRate(AutoAnnounce.getInstance(), 60000, 60000);
		}
		
		if (Config.PHANTOM_PLAYERS_ENABLED)
		{
			PhantomPlayers.init();
		}
		
		if (Config.PREMIUMWC)
		{
			PremiumWelcome.getInstance();
		}
		
		// If there is no such var in server var create such with default false.
		if (ServerVariables.getString("DonationBonusActive", "").isEmpty())
		{
			ServerVariables.set("DonationBonusActive", false);
		}
		
		if (ServerVariables.getBool("DonationBonusActive", true))
		{
			DonationBonusDay.getInstance().continuePormotion();
		}
		else
		{
			DonationBonusDay.getInstance().stopPromotion();
		}
		
		printSection("Offline Buffers");
		if (Config.BUFF_STORE_ENABLED)
		{
			printSection("Offline Buffers");
			OfflineBuffersTable.getInstance().restoreOfflineBuffers();
		}
		printSection("Offline Buffers");
		
		printSection("Forum Manager");
		ForumParser.getInstance();
		printSection("Forum Manager");
		
		if (Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
		{
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 30000L);
		}
		
		printSection("Loaded Facebook System");
		FacebookProfilesHolder.getInstance();
		OfficialPostsHolder.getInstance();
		CompletedTasksHistory.getInstance();
		ActionsExtractingManager.getInstance().load();
		FacebookAutoAnnouncement.load();
		printSection("Loaded Facebook System");
		
		printSection("Balancer System");
		MyMain.getInstance();
		
		
		printSection("Mmotop");
		Mmotop_vote.getInstance();
		printSection("L2Top.co");
		VoteSystemL2Top.getInstance();
		printSection("l2jbrasil.com");
		VoteSystemL2Brazil.getInstance();
		printSection("Hopzone.net");
		VoteSystemHopzone.getInstance();
//		printSection("l2network.eu");
//		VoteSystemNetwork.getInstance();
		
		printSection("Shutdown System");
		//Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.ShutdownMode.RESTART, false);
		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.RESTART);
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		_log.info("GameServer Started");
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		
		CharacterDAO.getInstance().markTooOldChars();
		
		printSection("DataBase Cleaner Loaded");
		CharacterDAO.getInstance().checkCharactersToDelete();
		
		printSection("Event System");
		FightClubEventManager.getInstance();
		BattleScheduleManager.getInstance();
		ActiveBattleManager.startScheduleThread();
		
		printSection("Memory");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for (String line : memUsage.split("\n"))
		{
			_log.info(line);
		}
		printSection("");
		server_started_date = new Date();
		_log.info("Server started in: " + ((System.currentTimeMillis() - startMs) / 1000) + " seconds.");
		
		registerSelectorThreads(ports);
		getListeners().onStart();
		
		AuthServerCommunication.getInstance().start();
		_hasLoaded = true;
	}
	
	public GameServerListenerList getListeners()
	{
		return _listeners;
	}
	
	public static GameServer getInstance()
	{
		return _instance;
	}
	
	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}
	
	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}
	
	private void checkFreePorts(int[] ports)
	{
		for (int port : ports)
		{
			while (!checkFreePort(port))
			{
				_log.warn("Port " + port + " is allready binded. Please free it and restart server.");
				try
				{
					Thread.sleep(1000L);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}
	
	private static boolean checkFreePort(int port)
	{
		ServerSocket ss = null;
		try
		{
			ss = new ServerSocket(port);
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				ss.close();
			}
			catch (Exception e)
			{
			}
		}
		
		return true;
	}
	
	private void registerSelectorThreads(TIntSet ports)
	{
		final GamePacketHandler gph = new GamePacketHandler();
		
		for (int port : ports.toArray())
		{
			registerSelectorThread(gph, null, port);
		}
	}
	
	private void registerSelectorThread(GamePacketHandler gph, String ip, int port)
	{
		try
		{
			SelectorThread<GameClient> selectorThread = new SelectorThread<>(Config.SELECTOR_CONFIG, _selectorStats, gph, gph, gph, new IPv4Filter());
			selectorThread.openServerSocket(ip == null ? null : InetAddress.getByName(ip), port);
			selectorThread.start();
			_selectorThreads.add(selectorThread);
		}
		catch (Exception e)
		{
			//
		}
	}
	
	public static void printSection(String s)
	{
		if (s.isEmpty())
		{
			s = "==============================================================================";
		}
		else
		{
			s = "=[ " + s + " ]";
			while (s.length() < 78)
			{
				s = "-" + s;
			}
		}
		_log.info(s);
	}
	
	public static void main(String[] args) throws Exception
	{
		
		new GameServer();
	}
	
	/**
	 * @param argument : the program argument which is unput on launch options.
	 * @return argument value or null if argument not found.
	 */
	public static String getArgumentValue(String argument)
	{
		return _args.get(argument);
	}
	
	/**
	 * @param argument : the program argument which is unput on launch options.
	 * @param def : default value if argument not found.
	 * @return argument value or default value if argument not found.
	 */
	public static String getArgumentValue(String argument, String def)
	{
		String ret = _args.get(argument);
		if (ret == null)
		{
			return def;
		}
		
		return ret;
	}
	
	public boolean hasLoaded()
	{
		return _hasLoaded;
	}
}