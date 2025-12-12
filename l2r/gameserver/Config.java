package l2r.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import l2r.commons.configuration.ExProperties;
import l2r.commons.net.nio.impl.SelectorConfig;
import l2r.commons.time.cron.SchedulingPattern;
import l2r.commons.util.StringArrayUtils;
import l2r.gameserver.donateshop.DonateShopMain;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.loginservercon.ServerType;
import l2r.gameserver.utils.AddonsConfig;
import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

public class Config
{

    private static final Logger _log = LoggerFactory.getLogger(Config.class);
	
	private static final int NCPUS = Runtime.getRuntime().availableProcessors();
	
	/*
	 * Server Configs
	 */
	public static final String OLYMPIAD = "config/server/olympiad.properties";
	private static final String FORMULAS_CONFIGURATION_FILE = "config/server/formulas.properties";
	private static final String GEODATA_CONFIG_FILE = "config/server/geodata.properties";
	private static final String RATES_FILE = "config/server/rates.properties";
	private static final String RESIDENCE_CONFIG_FILE = "config/server/residence.properties";
	private static final String CONFIGURATION_FILE = "config/server/server.properties";
	private static final String INSTANCES_FILE = "config/server/instances.properties";
	private static final String ALT_SETTINGS_FILE = "config/server/altsettings.properties";
	private static final String AI_CONFIG_FILE = "config/server/ai.properties";

	public static final String SPOIL_CONFIG_FILE = "config/server/spoil.properties";
	
	/*
	 *  Vote config
	 */
	public static final String VOTE_CONFIG_FILE = "config/vote.properties";
	
	/*
	 * Player Configs
	 */
	private static final String CHAT_FILE = "config/chat.properties";
	private static final String PVP_CONFIG_FILE = "config/player/pvp.properties";
	private static final String ITEMS_FILE = "config/player/items.properties";
	private static final String GM_CONFIG_FILE = "config/player/gm.properties";
	
	/*
	 * Services Configs
	 */
	private static final String BOARD_MANAGER_CONFIG_FILE = "config/services/Community.properties";
	private static final String SERVICES_FILE = "config/services/services.properties";
	
	/*
	 * NPC configs
	 */
	private static final String EPIC_BOSS_FILE = "config/npc/epic.properties";
	private static final String NPC_FILE = "config/npc/Npc.properties";
	
	/*
	 * Custom Configs
	 */
	private static final String CUSTOM_CONFIG_FILE = "config/custom/custom.properties";
	//private static final String CUSTOM_SECURITY_FILE = "config/custom/CustomSecurity.properties";
	private static final String EXT_FILE = "config/custom/ext.properties";
	public static final String BUFF_STORE_CONFIG_FILE = "config/mod/OfflineBuffer.ini";
	public static final String DONATION_STORE = "config/services/DonationStore.ini";
	public static final String FORGE_CONFIG_FILE = "config/services/forge.ini";
	
	/*
	 * Event Configs
	 */
	private static final String EVENT_FIGHT_CLUB_FILE = "config/events/FightClub.properties";
	private static final String EVENTS_CONFIG_FILE = "config/events/events.properties";
	private static final String VIKTORINA_CONFIG_FILE = "config/events/Victorina.properties";
	private static final String STRIDER_CONFIG_FILE = "config/events/Strider.properties";
	
	/*
	 * Random Configs
	 */
	private static final String DEVELOP_FILE = "config/develop.properties";
	private static final String ANUSEWORDS_CONFIG_FILE = "config/Abusewords.txt";
	private static final String PHANTOM_FILE = "config/phantom/Phantoms.properties";
	
	/** Community PvP */
	
	/* --------------------------------------------------------- */
	
	public static boolean CHECK_PRIVATE_SHOPS;
	public static int LIMIT_OFFLINE_IN_TOWN;
	public static boolean ENABLE_BAZAR;
	public static boolean ENABLE_LUCKY_PIGS;
	
	public static boolean CLEAN_CLAN_HALLS_ON_TIME;
	public static int MIN_PLAYERS_IN_CLAN_TO_KEEP_CH;
	public static int DAYS_TO_CHECK_FOR_CH_DELETE;
	
	// Fame Reward
	public static boolean ENABLE_ALT_FAME_REWARD;
	public static long ALT_FAME_CASTLE;
	public static long ALT_FAME_FORTRESS;
	public static int INTERVAL_FLAG_DROP;
	public static boolean ALLOW_START_FORTRESS_SIEGE_FEE;
	public static int START_FORTRESS_SIEGE_PRICE_ID;
	public static int START_FORTRESS_SIEGE_PRICE_AMOUNT;
	public static boolean FORTRESS_SIEGE_ALLOW_SINGLE_PLAYERS;
	public static boolean FORTRESS_REMOVE_FLAG_ON_LEAVE_ZONE;
	public static boolean DOMINION_REMOVE_FLAG_ON_LEAVE_ZONE;
	
	public static boolean ENABLE_TRADELIST_VOICE;
	public static int LEVEL_REQUIRED_TO_SEND_MAIL;
	public static boolean SHOW_BAN_INFO_IN_CHARACTER_SELECT;
	
	// Allow player to change his lang via .cfg
	public static boolean ALLOW_PLAYER_CHANGE_LANGUAGE;
	
	// Party distribute of items on premium account
	public static boolean PREMIUM_ACCOUNT_FOR_PARTY;
	
	// Retail macro use bug
	public static boolean ALLOW_MACROS_REUSE_BUG;
	public static boolean ALLOW_MACROS_ENCHANT_BUG;
	
	// Referral system
	public static boolean ENABLE_REFERRAL_SYSTEM;
	
	// Allow custom class-transfer-skills for pvp server
	public static boolean CUSTOM_CLASS_TRANSFER_SKILLS;
	
	public static boolean ENABLE_PVP_PK_LOG;
	public static double RATE_KARMA_LOST;
	
	// Custom player kill system for pvp
	public static boolean ENABLE_PLAYER_KILL_SYSTEM;
	public static boolean PLAYER_KILL_SPAWN_UNIQUE_CHEST;
	public static boolean PLAYER_KILL_INCREASE_ATTRIBUTE;
	public static boolean PLAYER_KILL_GIVE_ENCHANTS;
	public static boolean PLAYER_KILL_GIVE_LIFE_STONE;
	public static boolean PLAYER_KILL_GIVE_MANTRAS;
	public static boolean PLAYER_KILL_AQUIRE_FAME;
	public static boolean PLAYER_KILL_ALLOW_CUSTOM_PVP_ZONES;
	
	// Emoticons on say.
	static boolean ENABLE_EMOTIONS;
	
	// Phantom players
	public static boolean PHANTOM_PLAYERS_ENABLED;
	public static String PHANTOM_PLAYERS_ACCOUNT;
	public static int PHANTOM_MAX_PLAYERS;
	public static int[] PHANTOM_BANNED_CLASSID;
	public static int[] PHANTOM_BANNED_SETID;
	public static int PHANTOM_MAX_WEAPON_GRADE;
	public static int PHANTOM_MAX_ARMOR_GRADE;
	public static int PHANTOM_MAX_JEWEL_GRADE;
	public static int PHANTOM_SPAWN_MAX;
	public static int PHANTOM_SPAWN_DELAY;
	public static int PHANTOM_MAX_LIFETIME;
	public static int CHANCE_TO_ENCHANT_WEAP;
	public static int MAX_ENCH_PHANTOM_WEAP;
	public static int PHANTOM_MAX_DRIFT_RANGE;
	public static boolean ALLOW_PHANTOM_CUSTOM_TITLES;
	public static int PHANTOM_CHANCE_SET_NOBLE_TITLE;
	public static boolean DISABLE_PHANTOM_ACTIONS;
	public static int[] PHANTOM_ALLOWED_NPC_TO_WALK;
	public static int PHANTOM_ROAMING_MAX_WH_CHECKS;
	public static int PHANTOM_ROAMING_MAX_WH_CHECKS_DWARF;
	public static int PHANTOM_ROAMING_MAX_SHOP_CHECKS;
	public static int PHANTOM_ROAMING_MAX_SHOP_CHECKS_DWARF;
	public static int PHANTOM_ROAMING_MAX_NPC_CHECKS;
	public static int PHANTOM_ROAMING_MIN_WH_DELAY;
	public static int PHANTOM_ROAMING_MAX_WH_DELAY;
	public static int PHANTOM_ROAMING_MIN_SHOP_DELAY;
	public static int PHANTOM_ROAMING_MAX_SHOP_DELAY;
	public static int PHANTOM_ROAMING_MIN_NPC_DELAY;
	
	public static int PHANTOM_ROAMING_MIN_PRIVATESTORE_DELAY;
	public static int PHANTOM_ROAMING_MAX_PRIVATESTORE_DELAY;
	public static int PHANTOM_ROAMING_MIN_FREEROAM_DELAY;
	public static int PHANTOM_ROAMING_MAX_FREEROAM_DELAY;
	public static boolean DISABLE_PHANTOM_RESPAWN;
	public static boolean DEBUG_PHANTOMS;
	public static int[] PHANTOM_CLANS;
	
	public static boolean ALLOW_PFLAG;
	public static boolean ALLOW_CFLAG;
	
	public static int[] ITEM_COST_1_ADENA;
	
	public static boolean ALT_SELL_FROM_EVERYWHERE;
	
	// Clan Promotion npc
	public static boolean SERVICES_CLAN_PROMOTION_ENABLE;
	public static int SERVICES_CLAN_PROMOTION_MAX_LEVEL;
	public static int SERVICES_CLAN_PROMOTION_MIN_ONLINE;
	public static int SERVICES_CLAN_PROMOTION_ITEM;
	public static int SERVICES_CLAN_PROMOTION_ITEM_COUNT;
	public static int SERVICES_CLAN_PROMOTION_SET_LEVEL;
	public static int SERVICES_CLAN_PROMOTION_ADD_REP;
	public static boolean SERVICE_CLAN_PRMOTION_ADD_EGGS;
	public static String[] CLAN_PROMOTION_CLAN_EGGS;
	
	// Captcha system
	public static boolean ENABLE_CAPTCHA;
	public static boolean CAPTCHA_UNEQUIP;
	public static int CAPTCHA_MIN_MONSTERS;
	public static int CAPTCHA_MAX_MONSTERS;
	public static int CAPTCHA_ATTEMPTS;
	public static int CAPTCHA_SAME_LOCATION_DELAY;
	public static int CAPTCHA_SAME_LOCATION_MIN_KILLS;
	public static String CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE;
	public static int CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
	
	public static boolean ALLOW_BOARD_NEWS_LEECH;
	
	public static boolean ENABLE_POLL_SYSTEM;
	
	static int DEADLOCKCHECK_INTERVAL;
	
	public static int CANCEL_SYSTEM_RESTORE_DELAY;
	public static boolean CANCEL_SYSTEM_KEEP_TICKING;
	public static boolean AUTO_SHOTS_ON_LOGIN;
	
	/* Voiced Commands Config */
	public static boolean ENABLE_HELLBOUND_COMMAND;
	public static boolean ENABLE_CFG_COMMAND;
	public static boolean ENABLE_CLAN_COMMAND;
	public static boolean ENABLE_OFFLINE_COMMAND;
	public static boolean ENABLE_REPAIR_COMMAND;
	public static boolean ENABLE_WEDDING_COMMAND;
	public static boolean ENABLE_DEBUG_COMMAND;
	public static boolean ENABLE_RANDOM_COMMANDS;
	public static boolean ENABLE_CASTLEINFO_COMMAND;
	public static boolean NOT_USE_USER_VOICED;
	public static boolean ALLOW_TOTAL_ONLINE;
	public static boolean COMMAND_DRESSME_ENABLE;
	public static boolean COMMAND_FACEBOOK_ENABLE;
	
	// Vote System
	public static int PREMIUM_ACCOUNT_TYPE;
	public static int PREMIUM_ACCOUNT_PARTY_GIFT_ID;
	public static boolean ALLOW_PREMIUM_CHANGE;
	public static boolean ENTER_WORLD_SHOW_HTML_PREMIUM_BUY;
	public static boolean ENTER_WORLD_SHOW_HTML_PREMIUM_DONE;
	public static boolean ENTER_WORLD_SHOW_HTML_PREMIUM_ACTIVE;
	public static int SERVICES_RATE_TYPE;
	public static int SERVICES_RATE_CREATE_PA;
	public static int[] SERVICES_RATE_BONUS_PRICE;
	public static int[] SERVICES_RATE_BONUS_ITEM;
	public static double[] SERVICES_RATE_BONUS_VALUE;
	public static int[] SERVICES_RATE_BONUS_DAYS;
	public static int ALT_NEW_CHAR_PREMIUM_ID;
	public static long NONOWNER_ITEM_PICKUP_DELAY_RAIDS;
	public static boolean DEV_UNDERGROUND_COLISEUM;
	public static int UNDERGROUND_COLISEUM_MEMBER_COUNT;
	public static boolean ENABLE_PLAYER_ITEM_LOGS;
	public static boolean PLAYER_ITEM_LOGS_SAVED_IN_DB;
	public static long PLAYER_ITEM_LOGS_MAX_TIME;
	public static String SERVER_NAME;
	public static boolean ALLOW_HWID_ENGINE;
	public static String MAIL_USER;
	public static String MAIL_PASS;
	/* Password changer */
	public static boolean SERVICES_CHANGE_PASSWORD;
	public static int PASSWORD_PAY_ID;
	public static long PASSWORD_PAY_COUNT;
	public static String APASSWD_TEMPLATE;
	public static boolean AUTO_LOOT_PA;
	public static boolean AUTO_SOUL_CRYSTAL_QUEST;
	public static double SERVICES_BONUS_XP;
	public static double SERVICES_BONUS_SP;
	public static double SERVICES_BONUS_ADENA;
	public static double SERVICES_BONUS_ITEMS;
	public static double SERVICES_BONUS_SPOIL;
	public static boolean ALLOW_TALK_TO_NPCS;
	/* DAMAGE ON SCREEN */
	public static boolean ENABLE_DAM_ON_SCREEN;
	public static int DAM_ON_SCREEN_FONT;
	public static int DAM_ON_SCREEN_FONT_COLOR_ATTACKER;
	public static int DAM_ON_SCREEN_FONT_COLOR_TARGET;
	public static boolean ENABLE_SECONDARY_PASSWORD;
	public static boolean ENABLE_SPECIAL_TUTORIAL;
	public static boolean ALT_TELEPORTS_ONLY_FOR_GIRAN;
	public static boolean SERVICES_ANNOUNCE_PK_ENABLED;
	public static boolean SERVICES_ANNOUNCE_PVP_ENABLED;
	public static boolean ENABLE_DAILY_QUESTS;
	// Killing Spree
	public static boolean KILLING_SPREE_ENABLED;
	public static Map<Integer, String> KILLING_SPREE_ANNOUNCEMENTS;
	public static Map<Integer, String> KILLING_SPREE_COLORS;
	
	/** Strider Race */
	public static int EVENT_SR_MINIMUM_PLAYERS;
	public static int EVENT_SR_MAXIMUM_PLAYERS;
	public static int[][] EVENT_SR_REWARD_TOP1;
	public static int[][] EVENT_SR_REWARD_TOP2;
	public static int[][] EVENT_SR_REWARD_TOP3;
	
	public static int EVENT_SR_LOC_ARRIVAL_X;
	public static int EVENT_SR_LOC_ARRIVAL_Y;
	public static int EVENT_SR_LOC_ARRIVAL_Z;
	public static int EVENT_SR_LOC_PLAYER_X;
	public static int EVENT_SR_LOC_PLAYER_Y;
	public static int EVENT_SR_LOC_PLAYER_Z;
	public static int EVENT_SR_LOC_ID_NPC;
	public static int EVENT_NPC_PET;
	
	/** CUSTOM CONFIGS */
	// Character intro
	public static boolean ENABLE_CHARACTER_INTRO;
	// Achievement system
	public static boolean ENABLE_ACHIEVEMENTS;
	public static boolean ENABLE_PLAYER_COUNTERS;
	
	public static boolean DISABLE_ACHIEVEMENTS_FAME_REWARD;
	// Custom Auction system
	public static boolean ENABLE_CUSTOM_AUCTION;
	public static int AUCTION_DUMMY_CHARACTER;
	public static boolean ENABLE_COMMUNITY_RANKING;
	public static boolean ENABLE_EMAIL_VALIDATION;
	public static boolean CHARACTER_NAME_COLORIZATION;
	public static boolean CHARACTER_TITLE_REPRESENTING_RELATION;
	public static boolean AUTO_POTIONS;
	public static boolean AUTO_POTIONS_IN_PVP;
	public static String[] FORBIDDEN_CHAR_NAMES;
	// Adena drop by level
	// public static TIntIntHashMap ADENA_DROP_RATE_BY_LEVEL; FIXME
	// Wyvern settings
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	public static String PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE;
	public static int PINISHMENT_TIME_FOR_WYVERN;
	// Custom things
	public static boolean CUSTOM_SKILLS_LOAD;
	
	public static boolean DISABLE_TUTORIAL;
	
	public static boolean ALLOW_MAMMON_FOR_ALL;
	
	public static boolean ALLOW_FARM_IN_SEVENSIGN_IF_NOT_REGGED;
	
	public static boolean SEVEN_SIGN_DISABLE_BUFF_DEBUFF;
	
	public static boolean SEVEN_SIGN_NON_STOP_ALL_SPAWN;
	public static int SEVEN_SIGN_SET_PERIOD;
	
	public static boolean PARTY_SEARCH_COMMANDS;
	
	// Online table configs
	public static boolean ALLOW_ONLINE_PARSE;
	public static int FIRST_UPDATE;
	public static int DELAY_UPDATE;

	/** 4ipolino */
	public static boolean ALLOW_ADDONS_CONFIG;
	
	/** GameServer ports */
//	public static String DATABASE_DRIVER;
//	public static int DATABASE_MAX_CONNECTIONS;
//	public static int DATABASE_MAX_IDLE_TIMEOUT;
//	public static int DATABASE_IDLE_TEST_PERIOD;
//	public static String DATABASE_URL;
//	public static String DATABASE_LOGIN;
//	public static String DATABASE_PASSWORD;
	public static String LOGINSERVER_DB_NAME;
	public static String MYSQL_DUMP_PATH;
	
	// Database additional options
	public static boolean AUTOSAVE;
	
	public static long USER_INFO_INTERVAL;
	
	public static long BROADCAST_CHAR_INFO_INTERVAL;
	
	public static int EFFECT_TASK_MANAGER_COUNT;
	
	public static int MAXIMUM_ONLINE_USERS;
	public static int ONLINE_PLUS;
	
	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;
	public static boolean DONTLOADEVENTS;
	public static boolean DONTLOADOPTIONDATA;
	public static boolean DONTLOADNPCDROP;
	
	public static boolean DONTLOADMULTISELLS;
	static boolean DONTAUTOANNOUNCE;
	public static int MAX_REFLECTIONS_COUNT;
	static boolean PREMIUMWC;
	
	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;
	
	/** ChatBan */
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean ABUSEWORD_BANCHAT;
	public static int[] BAN_CHANNEL_LIST = new int[18];
	public static boolean ABUSEWORD_REPLACE;
	public static String ABUSEWORD_REPLACE_STRING;
	public static int ABUSEWORD_BANTIME;
	public static Pattern[] ABUSEWORD_LIST = {};
	public static boolean BANCHAT_ANNOUNCE;
	public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean BANCHAT_ANNOUNCE_NICK;
	
	public static int[] CHATFILTER_CHANNELS = new int[18];
	public static int CHATFILTER_MIN_LEVEL = 0;
	public static int CHATFILTER_WORK_TYPE = 1;
	
	public static boolean SAVING_SPS;
	public static boolean MANAHEAL_SPS_BONUS;
	
	public static int ALT_ADD_RECIPES;
	public static int ALT_MAX_ALLY_SIZE;
	public static int ALT_LEVEL_DIFFERENCE_PROTECTION;
	
	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static double[] ALT_PARTY_BONUS;
	public static double ALT_ABSORB_DAMAGE_MODIFIER;
	
	public static double ALT_BOW_PVP_DAMAGE_MODIFIER;
	public static double ALT_BOW_PVE_DAMAGE_MODIFIER;
	public static double ALT_PET_PVP_DAMAGE_MODIFIER;
	public static double ALT_BASE_MCRIT_RATE;
	public static double ALT_BASE_MCRIT_DAMAGE;
	public static int FORMULA_LETHAL_MAX_HP;
	public static int CANCEL_BUFF_MODIFIER;
	public static double MAGIC_DEBUFF_MATK_POW;
	public static int ALT_LETHAL_DIFF_LEVEL;
	public static boolean ALT_LETHAL_PENALTY;
	public static int LIM_FAME;
	public static boolean DEBUFF_PROTECTION_SYSTEM;
	public static int BUFF_RETURN_NORMAL_LOCATIONS_TIME;
	public static double ALT_POLE_DAMAGE_MODIFIER;
	public static int BUFF_RETURN_OLYMPIAD_TIME;
	public static int BUFF_RETURN_AUTO_EVENTS_TIME;
	
	//Update penalty Weapons 
	public static int MIN_NPC_LEVEL_DMG_PENALTY;
	public static double[] NPC_CRIT_DMG_PENALTY;
	public static double[] NPC_SKILL_DMG_PENALTY;
	public static double[] NPC_DMG_PENALTY;
	
	
	
	public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
	public static boolean ALLOW_BBS_WAREHOUSE;
	public static boolean BBS_WAREHOUSE_ALLOW_PK;
	
	public static boolean BBS_PVP_SUB_MANAGER_ALLOW;
	public static boolean BBS_PVP_SUB_MANAGER_PIACE;
	public static boolean BBS_PVP_TELEPORT_ENABLED;
	public static int BBS_PVP_TELEPORT_POINT_PRICE;
	public static int BBS_PVP_TELEPORT_MAX_POINT_COUNT;
	public static boolean BBS_PVP_ALLOW_SELL;
	public static boolean BBS_PVP_ALLOW_BUY;
	public static boolean BBS_PVP_ALLOW_AUGMENT;
	
	public static boolean ENABLE_NEW_CLAN_CB;
	
	public static boolean BOARD_ENABLE_CLASS_MASTER;
	public static boolean ENABLE_NEW_FRIENDS_BOARD;
	public static boolean ENABLE_RETAIL_FRIENDS_BOARD;
	public static boolean ENABLE_MEMO_BOARD;
	public static boolean ENABLE_NEW_MAIL_MANAGER;
	
	public static boolean ENABLE_BOARD_STATS;
	public static boolean ENABLE_OLD_MAIL_MANAGER;
	
	public static final boolean ENABLE_AUTO_HUNTING_REPORT = false;
	
	public static boolean IS_L2WORLD;
	
	public static int TalkGuardChance;
	public static int TalkNormalChance = 0;
	public static int TalkNormalPeriod = 0;
	public static int TalkAggroPeriod = 0;
	
	public static int ALL_CHAT_USE_MIN_LEVEL;
	public static int ALL_CHAT_USE_DELAY;
	public static int SHOUT_CHAT_USE_MIN_LEVEL;
	public static int SHOUT_CHAT_USE_DELAY;
	public static int TRADE_CHAT_USE_MIN_LEVEL;
	public static int TRADE_CHAT_USE_DELAY;
	public static int HERO_CHAT_USE_MIN_LEVEL;
	public static int HERO_CHAT_USE_DELAY;
	public static int PRIVATE_CHAT_USE_MIN_LEVEL;
	public static int PRIVATE_CHAT_USE_DELAY;
	public static int MAIL_USE_MIN_LEVEL;
	public static int MAIL_USE_DELAY;
	public static int PARTY__DELAY_TIME;
	public static int MIN_LEVEL_TO_USE_SHOUT;
	public static int DUALBOX_NUMBER_IP;
	public static int MAX_CHARS_PER_PC;
	public static int HWID_DUALBOX_NUMBER;
	public static int DUALBOX_NUMBER_IP_OUTSIDE_PEACE;
	public static int HWID_DUALBOX_NUMBER_OUTSIDE_PEACE;
	
	public static int _coinID;
	public static boolean ALLOW_UPDATE_ANNOUNCER;
	public static boolean SERVICES_RIDE_HIRE_ENABLED;
	public static boolean CLASS_MASTER_NPC;
	public static boolean SERVICES_DELEVEL_ENABLED;
	public static int SERVICES_DELEVEL_ITEM;
	public static int SERVICES_DELEVEL_COUNT;
	public static int SERVICES_DELEVEL_MIN_LEVEL;
	public static int SERVICES_BUY_ALL_CLAN_SKILLS_PRICE;
	public static int SERVICES_BUY_ALL_CLAN_SKILLS_ITEM_ID;
	public static boolean SERVICES_BUY_ALL_CLAN_PRICE_ONE_SKILL;
	public static boolean ALLOW_MAIL_OPTION;
	
	public static boolean SERVICES_CHANGE_Title_COLOR_ENABLED;
	public static int SERVICES_CHANGE_Title_COLOR_PRICE;
	public static int SERVICES_CHANGE_Title_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_Title_COLOR_LIST;
	
	public static Calendar CASTLE_VALIDATION_DATE;
	
	public static int PERIOD_CASTLE_SIEGE;

	private static Calendar TW_VALIDATION_DATE;
	public static int TW_SELECT_HOURS;
	public static int DOMINION_INTERVAL_WEEKS;
	public static boolean RETURN_WARDS_WHEN_TW_STARTS;
	public static boolean PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE;
	
	public static boolean ALT_PCBANG_POINTS_ENABLED;
	public static double ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int ALT_PCBANG_POINTS_BONUS;
	public static int ALT_PCBANG_POINTS_DELAY;
	public static int ALT_PCBANG_POINTS_MIN_LVL;
	
	public static boolean ALT_DEBUG_ENABLED;
	
	public static int CLAN_HALL_AUCTION_LENGTH;
	
	public static double CRAFT_MASTERWORK_CHANCE;
	public static double CRAFT_DOUBLECRAFT_CHANCE;
	
	/** Thread pools size */
	static int SCHEDULED_THREAD_POOL_SIZE;
	static int EXECUTOR_THREAD_POOL_SIZE;
	
	static boolean ENABLE_RUNNABLE_STATS;
	public static boolean ThreadPoolManagerDebug;
	public static int ThreadPoolManagerDebugInterval;
	public static int ThreadPoolManagerDebugDeflect;
	public static boolean ThreadPoolManagerDebugLogConsol;
	public static int ThreadPoolManagerDebugLogConsolDelay;
	public static boolean ThreadPoolManagerDebugLogFile;
	
	/** Network settings */
	static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();
	
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_ONLY_ADENA;
	public static boolean AUTO_LOOT_INDIVIDUAL;
	public static boolean AUTO_LOOT_FROM_RAIDS;
	
	/** Auto-loot for/from players with karma also? */
	public static boolean AUTO_LOOT_PK;
	
	/** Character name template */
	public static String CNAME_TEMPLATE;
	
	public static int CNAME_MAXLEN = 32;
	
	/** Clan name template */
	public static String CLAN_NAME_TEMPLATE;
	
	/** Clan title template */
	public static String CLAN_TITLE_TEMPLATE;
	
	/** Ally name template */
	public static String ALLY_NAME_TEMPLATE;
	
	/** Global chat state */
	public static boolean GLOBAL_SHOUT;
	public static int PVP_COUNT_SHOUT;
	public static int ONLINE_TIME_SHOUT;
	public static int LEVEL_FOR_SHOUT;
	public static int CHATS_REQUIRED_LEVEL;
	public static int PM_REQUIRED_LEVEL;
	public static int SHOUT_REQUIRED_LEVEL;
	public static boolean RECORD_WROTE_CHAT_MSGS_COUNT;
	public static int ANNOUNCE_VOTE_DELAY;
	
	public static boolean GLOBAL_TRADE_CHAT;
	public static int ONLINE_TIME_TRADE;
	public static int PVP_COUNT_TRADE;
	public static int LEVEL_FOR_TRADE;
	
	public static int CHAT_RANGE;
	public static int SHOUT_OFFSET;
	
	public static boolean USE_TRADE_WORDS_ON_GLOBAL_CHAT;
	
	public static GArray<String> TRADE_WORDS;
	public static boolean TRADE_CHATS_REPLACE_FROM_ALL;
	public static boolean TRADE_CHATS_REPLACE_FROM_SHOUT;
	public static boolean TRADE_CHATS_REPLACE_FROM_TRADE;
	
	/** For test servers - evrybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	
	public static double ALT_RAID_RESPAWN_MULTIPLIER;
	
	public static boolean ALT_ALLOW_AUGMENT_ALL;
	public static boolean ALT_ALLOW_DROP_AUGMENTED;
	
	public static boolean ALT_GAME_UNREGISTER_RECIPE;
	
	/** Delay for announce SS period (in minutes) */
	public static int SS_ANNOUNCE_PERIOD;
	
	/** Petition manager */
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	
	/** Show mob stats/droplist to players? */
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALT_FULL_NPC_STATS_PAGE;
	public static boolean ALLOW_NPC_SHIFTCLICK;
	public static boolean ALT_PLAYER_SHIFTCLICK;
	
	public static boolean ALT_ALLOW_SELL_COMMON;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;
	
	public static int[] ALT_ALLOWED_PET_POTIONS;
	
	public static boolean SKILL_CHANCE_CALCULATED_BY_ENCHANT_LEVEL;
	public static double BACK_BLOW_MULTIPLIER;
	public static double NON_BACK_BLOW_MULTIPLIER;
	public static double CUSTOM_BONUS_SKILL_CHANCE;
	public static boolean SKILLS_CHANCE_OLD_FORMULA;
	public static boolean SKILL_FORCE_H5_FORMULA;
	public static boolean SKILLS_CHANCE_SHOW;
	public static int SKILLS_CHANCE_MIN;
	public static double SKILLS_CHANCE_MOD_MAGE;
	
	public static int SKILLS_CHANCE_CAP;
	public static boolean SKILLS_CHANCE_CAP_ONLY_PLAYERS;
	public static double SKILLS_MOB_CHANCE;
	
	public static boolean SHIELD_SLAM_BLOCK_IS_MUSIC;
	public static boolean ALT_SAVE_UNSAVEABLE;
	public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean ALT_DELETE_SA_BUFFS;
	public static int SKILLS_CAST_TIME_MIN;
	public static int SKILLS_PAST_TIME_MIN;
	public static double PHYS_SKILLS_DAMAGE_POW;
	
	/** Î ï¿½Î ÎŽÎ Â½Î¡â€žÎ ÎˆÎ Â³Î¡Æ’Î¡â‚¬Î Â°Î¡â€ Î ÎˆÎ¡ï¿½ Î ÎˆÎ¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î ÎŽÎ Â²Î Â°Î Â½Î ÎˆÎ¡ï¿½ Î ÎˆÎ¡â€šÎ ÂµÎ ÎŒÎ ÎŽÎ Â² Î Î�Î ÎŽ Î¡Æ’Î ÎŒÎ ÎŽÎ Â»Î¡â€¡Î Â°Î Â½Î ÎˆÎ¡ï¿½ Î Î�Î ÎŽÎ¡Æ’Î¡ï¿½Î ÂµÎ Â½Î¡â€¹ */
	public static int[] ITEM_USE_LIST_ID;
	public static boolean ITEM_USE_IS_COMBAT_FLAG;
	public static boolean ITEM_USE_IS_ATTACK;
	public static boolean ITEM_USE_IS_EVENTS;
	public static long MAX_ADENA;
	
	/** Event Fight Club */
	public static boolean FIGHT_CLUB_ENABLED;
	public static int MINIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_DIFFERENCE;
	public static String[] ALLOWED_RATE_ITEMS;
	public static int PLAYERS_PER_PAGE;
	public static int ARENA_TELEPORT_DELAY;
	public static boolean CANCEL_BUFF_BEFORE_FIGHT;
	public static boolean UNSUMMON_PETS;
	public static boolean UNSUMMON_SUMMONS;
	public static boolean REMOVE_CLAN_SKILLS;
	public static boolean REMOVE_HERO_SKILLS;
	public static int TIME_TO_PREPARATION;
	public static int FIGHT_TIME;
	public static boolean ALLOW_DRAW;
	public static int TIME_TELEPORT_BACK;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN;
	public static boolean FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN;
	public static boolean FIGHT_CLUB_ANNOUNCE_TOP_KILLER;
	public static boolean FIGHT_CLUB_SUMMON_LOSE_BUFFS_ON_DEATH;
	public static boolean EVENT_KOREAN_RESET_REUSE;
	// Fight Club
	public static boolean ALLOW_FIGHT_CLUB;
	public static boolean FIGHT_CLUB_HWID_CHECK;
	public static int FIGHT_CLUB_DISALLOW_EVENT;
	public static boolean FIGHT_CLUB_EQUALIZE_ROOMS;
	
	/** Î Î†Î ÎˆÎ¡â€šÎ¡Æ’Î Â» Î Î�Î¡â‚¬Î Îˆ Î¡ï¿½Î ÎŽÎ Â·Î Î„Î Â°Î Â½Î ÎˆÎ Îˆ Î¡â€¡Î Â°Î¡â‚¬Î Â° */
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	
	/** Î Î†Î Â°Î Î‰Î ÎŒÎ Â°Î¡Æ’Î¡â€š Î Â½Î Â° Î ÎˆÎ¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î ÎŽÎ Â²Î Â°Î Â½Î ÎˆÎ Âµ social action */
	public static boolean ALT_SOCIAL_ACTION_REUSE;
	
	/** Î ï¿½Î¡â€šÎ ÎŠÎ Â»Î¡ï¿½Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î ÎŠÎ Â½Î ÎˆÎ Â³ Î Î„Î Â»Î¡ï¿½ Î ÎˆÎ Â·Î¡Æ’Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ¡ï¿½ Î¡ï¿½Î ÎŠÎ ÎˆÎ Â»Î ÎŽÎ Â² */
	public static boolean ALT_DISABLE_SPELLBOOKS;
	
	/** Alternative gameing - loss of XP on death */
	public static boolean ALT_GAME_DELEVEL;
	public static int ALT_MAIL_MIN_LVL;
	
	/** Î Â Î Â°Î Â·Î¡â‚¬Î ÂµÎ¡ï¿½Î Â°Î¡â€šÎ¡ï¿½ Î Â»Î Îˆ Î Â½Î Â° Î Â°Î¡â‚¬Î ÂµÎ Â½Î Âµ Î Â±Î ÎŽÎ Îˆ Î Â·Î Â° Î ÎŽÎ Î�Î¡â€¹Î¡â€š */
	public static boolean ALT_ARENA_EXP;
	
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM;
	public static int ALT_GAME_START_LEVEL_TO_SUBCLASS;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static int ALT_GAME_SUB_ADD;
	public static boolean ALL_SUBCLASSES_AVAILABLE;
	public static boolean ALT_GAME_SUB_BOOK;
	public static boolean ALT_NO_LASTHIT;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;
	
	public static boolean ALT_SIMPLE_SIGNS;
	public static boolean ALT_TELE_TO_CATACOMBS;
	public static boolean ALT_BS_CRYSTALLIZE;
	
	public static boolean ALT_ALLOW_TATTOO;
	
	public static int ALT_BUFF_LIMIT;
	
	public static int MULTISELL_SIZE;
	
	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static boolean SERVICES_CHANGE_NICK_ALLOW_SYMBOL;
	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;
	
	public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CHANGE_CLAN_NAME_ITEM;
	
	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;
	
	public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
	public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
	public static int SERVICES_EXCHANGE_BABY_PET_ITEM;
	
	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;
	
	public static boolean SERVICES_CHANGE_BASE_ENABLED;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;
	
	public static boolean SERVICES_SEPARATE_SUB_ENABLED;
	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;
	
	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;
	
	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;
	
	public static boolean SERVICES_HERO_SELL_ENABLED;
	public static int[] SERVICES_HERO_SELL_DAY;
	public static int[] SERVICES_HERO_SELL_PRICE;
	public static int[] SERVICES_HERO_SELL_ITEM;
	
	public static boolean SERVICES_WASH_PK_ENABLED;
	public static int SERVICES_WASH_PK_ITEM;
	public static int SERVICES_WASH_PK_PRICE;
	
	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static int SERVICES_EXPAND_INVENTORY_MAX;
	
	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;
	public static int SERVICES_EXPAND_WAREHOUSE_MAX;
	
	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;
	
	public static String SERVICES_SELLPETS;
	
	public static boolean SERVICES_CLAN_REP_POINTS;
	public static int SERVICE_CLAN_REP_ITEM;
	public static int SERVICE_CLAN_REP_COST;
	public static int SERVICE_CLAN_REP_ADD;
	
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	public static boolean TRANSFORM_ON_OFFLINE_TRADE;
	public static int TRANSFORMATION_ID_MALE;
	public static int TRANSFORMATION_ID_FEMALE;
	
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static boolean SERVICES_PARNASSUS_NOTAX;
	public static long SERVICES_PARNASSUS_PRICE;
	
	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static double SERVICES_LOTTERY_5_NUMBER_RATE;
	public static double SERVICES_LOTTERY_4_NUMBER_RATE;
	public static double SERVICES_LOTTERY_3_NUMBER_RATE;
	public static double SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static int[] SERVICES_LOTTERY_STARTING_DATE;
	
	public static boolean SERVICES_ALLOW_ROULETTE;
	public static long SERVICES_ROULETTE_MIN_BET;
	public static long SERVICES_ROULETTE_MAX_BET;
	
	public static long EXPELLED_MEMBER_PENALTY;
	public static long LEAVED_ALLY_PENALTY;
	public static long DISSOLVED_ALLY_PENALTY;
	public static long DISSOLVED_CLAN_PENALTY;
	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_ALLOW_ADENA_DAWN;
	// -------------------------------------------------------------------------------------------------------
	// TODO // PvP MOD
	// -------------------------------------------------------------------------------------------------------
	public static int ATT_MOD_ARMOR;
	public static int ATT_MOD_WEAPON;
	public static int ATT_MOD_WEAPON1;
	public static int ATT_MOD_MAX_ARMOR;
	public static int ATT_MOD_MAX_WEAPON;
	
	public static boolean SPAWN_CITIES_TREE;
	public static boolean SPAWN_NPC_BUFFER;
	public static boolean SPAWN_NPC_CLASS_MASTER;
	public static int MAX_PARTY_SIZE;
	public static boolean SPAWN_scrubwoman;
	public static boolean ALLOW_SPAWN_CUSTOM_HALL_NPC;
	public static boolean ADEPT_ENABLE;
	// By SmokiMo
	public static int HENNA_STATS;
	// add by 4ipolino
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean NEW_CHAR_IS_HERO;
	public static boolean ANNOUNCE_SPAWN_RB;
	public static boolean ANNOUNCE_SPAWN_RB_REGION;
	public static boolean ACC_MOVE_ENABLED;
	public static int ACC_MOVE_ITEM;
	public static int ACC_MOVE_PRICE;
	// XXX BUFFER
	public static boolean BUFFER_ON;
	
	public static boolean BUFFER_PET_ENABLED;
	public static int BUFFER_PRICE;
	public static int BUFFER_MIN_LVL;
	public static int BUFFER_MAX_LVL;
	
	public static boolean BOWTAN_PENALTY;
	public static int NPC_DIALOG_PLAYER_DELAY;
	public static int SEND_WAREHOUSE_WITH_DRAWLIST_PACKETDELAY;
	public static int SEND_WAREHOUSE_DEPOSIT_LIST_PACKETDELAY;
	public static int REQUEST_SETPLEDGE_CRESTLARGE_PACKETDELAY;
	public static int REQUEST_SETPLEGDE_CREAST_PACKETDELAY;
	public static int REQUEST_RECIPESHOPMANAGE_QUITPACKETDEALAY;
	public static int REQUESTBYPASSTOSERVERPACKTDELAY;
	public static int APPEARINGPACKETDELAY;
	public static int REQUESTPRIVATESTOREQUITBUYPACKETDELAY;
	public static int REQUESTRELOADPACKETDELAY;
	public static int REQUESTACTUIONUSEPACKETDELAY;
	public static int REQUSTEXBR_LECTUREMARKPACKETDELAY;
	public static int REQUESTPARTYMATCHLISTPACKETDELAY;
	public static int REQUESTPREVIEWITEMPACKETDELAY;
	public static int REQUESTPRIVATESTOREQUITSELLPACKETDEALY;
	public static int REQUESTRECIPESHOPLISTSETPACKETDELAY;
	public static int REQUESTREFINECANCELPACKETDELAY;
	public static int REQUESTWITHDRAWALPLEDGEPACKETDELAY;
	public static int SETPRIVATESTOREBUYLISTPACKETDELAY;
	public static int SETPRIVATESTORELISTPACKETDELAY;
	public static int REQUESTMAGICSKILLUSEPACKETDELAY;
	public static int BUGUSER_PUNISH;
	public static int DEFAULT_PUNISH;
	public static boolean ALLOW_ITEMS_LOGGING;
	public static long PACKET_FLOOD_PROTECTION_IN_MS;
	public static long ENTER_WORLD_FLOOD_PROECTION_IN_MS;
	public static boolean ANTIFEED_ENABLE;
	public static boolean ANTIFEED_DUALBOX;
	public static boolean ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int ANTIFEED_INTERVAL;
	public static int ANTIFEED_MAX_LVL_DIFFERENCE;
	public static int MAX_ITEM_ENCHANT_KICK;
	public static boolean ALLOW_JUST_MOVING;
	public static boolean PARTY_TELEPORT;
	public static int CUSTOM_TELEPORT_ITEM;
	public static int CUSTOM_TELEPORT_COUNT;
	public static String CUSTOM_MESSAGER_ITEMS;

	public static boolean ALLOW_DUALBOX;
	public static  int ALLOWED_BOXES;

	// CustomSpawnNewChar
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	
	/** Olympiad Compitition Starting time */
	public static int ALT_OLY_START_TIME;
	/** Olympiad Compition Min */
	public static int ALT_OLY_MIN;
	/** Olympaid Comptetition Period */
	public static long ALT_OLY_CPERIOD;
	/** Olympaid Weekly Period */
	public static long ALT_OLY_WPERIOD;
	/** Olympaid Validation Period */
	public static long ALT_OLY_VPERIOD;
	//public static int[] ALT_OLY_DATE_END;
	
	// new
	public static boolean ALT_OLYMP_PERIOD;
	public static List<Integer> ALT_OLY_DATE_END_MONTHLY = new ArrayList<>();
	public static int ALT_OLY_DATE_END_WEEKLY;
	public static int ALT_OLY_WAIT_TIME;
	public static boolean OLY_SHOW_OPPONENT_PERSONALITY;
	/** Olympiad Manager Shout Just One Time CUSTOM MESSAGE */
	public static boolean OLYMPIAD_SHOUT_ONCE_PER_START;

	
	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;
	
	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int TEAM_GAME_MIN;
	
	public static int GAME_MAX_LIMIT;
	public static int GAME_CLASSES_COUNT_LIMIT;
	public static int GAME_NOCLASSES_COUNT_LIMIT;
	public static int GAME_TEAM_COUNT_LIMIT;
	
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_TEAM_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int OLYMPIAD_STADIAS_COUNT;
	public static int OLYMPIAD_BATTLES_FOR_REWARD;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static boolean OLYMPIAD_OLDSTYLE_STAT;
	public static boolean OLYMPIAD_PLAYER_IP;
	public static boolean OLYMPIAD_PLAYER_HWID;
	public static int OLYMPIAD_BEGIN_TIME;
	
	public static boolean OLY_ENCH_LIMIT_ENABLE;
	public static int OLY_ENCHANT_LIMIT_WEAPON;
	public static int OLY_ENCHANT_LIMIT_ARMOR;
	public static int OLY_ENCHANT_LIMIT_JEWEL;
	
	public static boolean OLY_HIDE_PLAYER_IDENTITY;
	public static boolean OLY_SHOW_OPPONENT_INFO;
	public static boolean OLY_SHOW_OPPONENT_INFO_WINS_LOSES;
	public static boolean OLY_ASK_PLAYERS_TO_SKIP_COUNTDOWN;
	
	public static long NONOWNER_ITEM_PICKUP_DELAY;
	
	/** Logging Chat Window */
	public static boolean LOG_CHAT;

	
	/** Player Drop Rate control */
	public static boolean KARMA_NEEDED_TO_DROP;
	
	public static int KARMA_DROP_ITEM_LIMIT;
	
	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;
	
	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;
	
	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;
	
	public static int DELETE_DAYS;
	
	/** Datapack root directory */
	public static File DATAPACK_ROOT;
	
	public static int CLANHALL_BUFFTIME_MODIFIER;
	public static int SONGDANCETIME_MODIFIER;
	
	public static double MAXLOAD_MODIFIER;
	public static double GATEKEEPER_MODIFIER;
	public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;
	
	public static double ALT_CHAMPION_CHANCE1;
	public static double ALT_CHAMPION_CHANCE2;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
	public static boolean ALT_CHAMPION_DROP_HERBS;
	public static boolean ALT_SHOW_MONSTERS_LVL;
	public static boolean ALT_SHOW_MONSTERS_AGRESSION;
	public static int ALT_CHAMPION_TOP_LEVEL;
	public static int ALT_CHAMPION_MIN_LEVEL;
	
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;
	
	public static boolean SELL_ALL_ITEMS_FREE;
	/** Pets */
	public static int SWIMING_SPEED;
	public static boolean SAVE_PET_EFFECT;
	
	/** protocol revision */
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	
	/** random animation interval */
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	
	public static String DEFAULT_LANG;
	static String RESTART_AT_TIME;
	
	public static boolean SECOND_AUTH_ENABLED;
	static boolean SECOND_AUTH_BAN_ACC;
	static boolean SECOND_AUTH_STRONG_PASS;
	static int SECOND_AUTH_MAX_ATTEMPTS;
	static long SECOND_AUTH_BAN_TIME;
	
	public static boolean SERVER_SIDE_NPC_NAME;
	public static boolean SERVER_SIDE_NPC_TITLE;
	public static boolean SERVER_SIDE_NPC_TITLE_ETC;
	public static int DEINONYCHUS_EGG_DROP_CHANCE;
	
	public static List<Integer> NPC_DONTSPAWN_LIST = new ArrayList<>();
	public static List<Integer> TEST_NPC_DMG = new ArrayList<>();
	
	private static String CLASS_MASTERS_PRICE;
	public static int CLASS_MASTERS_PRICE_ITEM;
	public static List<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList<>();
	public static int[] CLASS_MASTERS_PRICE_LIST = new int[4];
	public static boolean ALLOW_EVENT_GATEKEEPER;
	
	public static boolean ITEM_BROKER_ITEM_SEARCH;
	
	/** Inventory slots limits */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int QUEST_INVENTORY_MAXIMUM;
	
	/** Warehouse slots limits */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	
	public static int FREIGHT_SLOTS;
	
	/** Karma System Variables */
	public static int KARMA_MIN_KARMA;
	
	public static int KARMA_LOST_BASE;
	
	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;
	
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	
	public static int PVP_TIME;
	
	/** Karma Punishment */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	
	/** Forum Configs */
	public static boolean FORUM_IN_WHOLE_COMMUNITY_BOARD;
	public static String FORUM_TAB;
	public static int FORUM_INFORMATION_MANAGEMENT;
	public static boolean FORUM_INCREASE_VIEWS_FOR_AUTHOR_VIEW;
	public static boolean FORUM_AUTHOR_CAN_CLOSE_TOPIC;
	public static int FORUM_TOPICS_LIMIT_IN_PAGE;
	public static int FORUM_MESSAGES_LIMIT_IN_PAGE;
	
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static boolean OLF_TSHIRT_CUSTOM_ENABLED;
	public static int ENCHANT_MAX_OLF_T_SHIRT;
	public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
	public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;
	
	public static int FAME_REWARD_FORTRESS;
	public static int FAME_REWARD_CASTLE;
	
	public static int RATE_SIEGE_FAME_MIN;
	public static int RATE_SIEGE_FAME_MAX;
	
	public static int RATE_DOMINION_SIEGE_FAME_MIN;
	public static int RATE_DOMINION_SIEGE_FAME_MAX;
	
	public static boolean REGEN_SIT_WAIT;

	public static int PDAM_TO_MONSTER_SUB_LVL_DIFF;
	public static int PDAM_TO_RAID_SUB_LVL_DIFF;
	public static int MDAM_TO_MONSTER_SUB_LVL_DIFF;
	public static int MDAM_TO_RAID_SUB_LVL_DIFF;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean ALLOW_DROP_CALCULATOR;
	public static boolean PARALIZE_ON_RAID_DIFF;
	public static int[] DROP_CALCULATOR_DISABLED_TELEPORT;
	
	public static double ALT_PK_DEATH_RATE;
	public static int STARTING_ADENA;
	
	public static int STARTING_LVL;
	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean UNSTUCK_SKILL;
	
	/** Percent CP is restore on respawn */
	public static double RESPAWN_RESTORE_CP;
	/** Percent HP is restore on respawn */
	public static double RESPAWN_RESTORE_HP;
	/** Percent MP is restore on respawn */
	public static double RESPAWN_RESTORE_MP;
	
	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;
	
	public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;
	public static double SENDSTATUS_TRADE_MOD;
	public static boolean SHOW_OFFLINE_MODE_IN_ONLINE;
	
	public static boolean ALT_CH_ALL_BUFFS;
	public static boolean ALT_CH_ALLOW_1H_BUFFS;
	
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int PORT_GAME;
	
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;
	
	public static boolean VIKTORINA_ENABLED;// false;
	public static boolean VIKTORINA_REMOVE_QUESTION;// false;;
	public static boolean VIKTORINA_REMOVE_QUESTION_NO_ANSWER;// = false;
	public static int VIKTORINA_START_TIME_HOUR;// 16;
	public static int VIKTORINA_START_TIME_MIN;// 16;
	public static int VIKTORINA_WORK_TIME;// 2;
	public static int VIKTORINA_TIME_ANSER;// 1;
	public static int VIKTORINA_TIME_PAUSE;// 1;
	
	private static void loadVIKTORINAsettings()
	{
		ExProperties VIKTORINASettings = load(VIKTORINA_CONFIG_FILE);
		
		VIKTORINA_ENABLED = VIKTORINASettings.getProperty("Victorina_Enabled", false);
		VIKTORINA_REMOVE_QUESTION = VIKTORINASettings.getProperty("Victorina_Remove_Question", false);
		VIKTORINA_REMOVE_QUESTION_NO_ANSWER = VIKTORINASettings.getProperty("Victorina_Remove_Question_No_Answer", false);
		VIKTORINA_START_TIME_HOUR = VIKTORINASettings.getProperty("Victorina_Start_Time_Hour", 16);
		VIKTORINA_START_TIME_MIN = VIKTORINASettings.getProperty("Victorina_Start_Time_Minute", 16);
		VIKTORINA_WORK_TIME = VIKTORINASettings.getProperty("Victorina_Work_Time", 2);
		VIKTORINA_TIME_ANSER = VIKTORINASettings.getProperty("Victorina_Time_Answer", 1);
		VIKTORINA_TIME_PAUSE = VIKTORINASettings.getProperty("Victorina_Time_Pause", 1);
		
	}
	
	/** AI */
	public static boolean RETAIL_SS;
	public static boolean ALLOW_NPC_AIS;
	public static int AI_TASK_MANAGER_COUNT;
	public static long AI_TASK_ATTACK_DELAY;
	public static long AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;
	
	public static int AGGRO_CHECK_INTERVAL;
	public static long NONAGGRO_TIME_ONTELEPORT;
	
	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;
	
	/** Maximum range mobs can pursue agressor from spawn point */
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;
	
	public static boolean ALT_DEATH_PENALTY;
	public static boolean ALLOW_DEATH_PENALTY_C5;
	public static int ALT_DEATH_PENALTY_C5_CHANCE;
	public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
	public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
	
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FORGOTTEN_SKILLS;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	
	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;
	
	public static boolean USE_BBS_PROF_IS_COMBAT;
	
	public static boolean COMMUNITYBOARD_ENABLED;
	public static boolean COMMUNITY_FAVORITES;
	
	public static String BBS_DEFAULT;
	public static String BBS_HOME_DIR;
	public static String BBS_TITLE;
	
	/** Wedding Options */
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	
	/** Augmentations **/
	public static int AUGMENTATION_NG_SKILL_CHANCE; // Chance to get a skill while using a NoGrade Life Stone
	public static int AUGMENTATION_NG_GLOW_CHANCE; // Chance to get a Glow effect while using a NoGrade Life Stone(only if you get a skill)
	public static int AUGMENTATION_MID_SKILL_CHANCE; // Chance to get a skill while using a MidGrade Life Stone
	public static int AUGMENTATION_MID_GLOW_CHANCE; // Chance to get a Glow effect while using a MidGrade Life Stone(only if you get a skill)
	public static int AUGMENTATION_HIGH_SKILL_CHANCE; // Chance to get a skill while using a HighGrade Life Stone
	public static int AUGMENTATION_HIGH_GLOW_CHANCE; // Chance to get a Glow effect while using a HighGrade Life Stone
	public static int AUGMENTATION_TOP_SKILL_CHANCE; // Chance to get a skill while using a TopGrade Life Stone
	public static int AUGMENTATION_TOP_GLOW_CHANCE; // Chance to get a Glow effect while using a TopGrade Life Stone
	public static int AUGMENTATION_BASESTAT_CHANCE; // Chance to get a BaseStatModifier in the augmentation process
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	
	public static int FOLLOW_RANGE;
	
	public static boolean ALT_ENABLE_MULTI_PROFA;
	
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALT_ITEM_AUCTION_CAN_REBID;
	public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
	public static int ALT_ITEM_AUCTION_BID_ITEM_ID;
	public static long ALT_ITEM_AUCTION_MAX_BID;
	public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;
	
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	
	public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
	public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
	public static boolean ALT_HBCE_FAIR_PLAY;
	public static int ALT_PET_INVENTORY_LIMIT;
	public static int ALT_CLAN_LEVEL_CREATE;
	
	/** limits of stats **/
	public static int LIMIT_PATK;
	public static int LIMIT_MATK;
	public static int LIMIT_PDEF;
	public static int LIMIT_MDEF;
	public static int LIMIT_MATK_SPD;
	public static int LIMIT_PATK_SPD;
	public static int LIMIT_CRIT_DAM;
	public static int LIMIT_CRIT;
	public static int LIMIT_MCRIT;
	public static int LIMIT_ACCURACY;
	public static int LIMIT_EVASION;
	public static int LIMIT_MOVE;
	public static double SKILLS_CHANCE_POW;
	public static double SKILLS_CHANCE_MOD;
	public static TIntIntHashMap LIMIT_REFLECT;
	
	public static TIntDoubleHashMap CUBIC_MATK_MULT;
	
	public static boolean ALT_ELEMENT_FORMULA;

	/** Enchant Config **/
	public static int SAFE_ENCHANT_COMMON;
	public static int SAFE_ENCHANT_FULL_BODY;
	
	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static double FESTIVAL_RATE_PRICE;
	
	/** DimensionalRift Config **/
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY; // Time in ms the party has to wait until the mobs spawn
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	
	public static boolean ALLOW_TALK_WHILE_SITTING;
	
	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
	
	/** Î Â Î Â°Î Â·Î¡â‚¬Î ÂµÎ¡ï¿½Î ÂµÎ Â½Î ÎŽ Î Â»Î Îˆ Î ÎˆÎ Â·Î¡Æ’Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î¡ï¿½Î ÎŠÎ ÎˆÎ Â»Î ÎŽÎ Â² Î¡â€šÎ¡â‚¬Î Â°Î Â½Î¡ï¿½Î¡â€žÎ ÎŽÎ¡â‚¬Î ÎŒÎ Â°Î¡â€ Î ÎˆÎ Îˆ Î Îˆ Î¡ï¿½Î Â°Î Â± Î ÎŠÎ Â»Î Â°Î¡ï¿½Î¡ï¿½Î ÎŽÎ Â² Î Â±Î ÂµÎ Â· Î Â½Î Â°Î Â»Î ÎˆÎ¡â€¡Î ÎˆÎ¡ï¿½ Î Â²Î¡â€¹Î Î�Î ÎŽÎ Â»Î Â½Î ÂµÎ Â½Î Â½Î ÎŽÎ Â³Î ÎŽ Î ÎŠÎ Â²Î ÂµÎ¡ï¿½Î¡â€šÎ Â° */
	public static boolean ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;
	
	// events.properties
	public static boolean ENABLE_DION_ARENA;
	public static boolean ENABLE_GIRAN_ARENA;
	
	public static double EVENT_CofferOfShadowsPriceRate;
	public static double EVENT_CofferOfShadowsRewardRate;
	
	public static double EVENT_APIL_FOOLS_DROP_CHANCE;
	
	public static int ENCHANT_MAX_MASTER_YOGI_STAFF;
	
//	public static boolean AllowCustomDropItems;
//	public static int[] CDItemsId;
//	public static int[] CDItemsCountDropMin;
//	public static int[] CDItemsCountDropMax;
//	public static double[] CustomDropItemsChance;
//	public static boolean CDItemsAllowMinMaxPlayerLvl;
//	public static int CDItemsMinPlayerLvl;
//	public static int CDItemsMaxPlayerLvl;
//	public static boolean CDItemsAllowMinMaxMobLvl;
//	public static int CDItemsMinMobLvl;
//	public static int CDItemsMaxMobLvl;
//	public static boolean CDItemsAllowOnlyRbDrops;
//	
	public static boolean ACTIVITY_REWARD_ENABLED;
	public static int ACTIVITY_REWARD_TIME;
	public static String[] ACTIVITY_REWARD_ITEMS;
	
	// Fight Club
	public static boolean ALLOW_EVENTS_CLUB;
	public static boolean EVENTS_CLUB_HWID_CHECK;
	public static int EVENTS_CLUB_DISALLOW_EVENT;
	public static boolean EVENTS_CLUB_EQUALIZE_ROOMS;
	
	public static boolean ENABLE_GVG_EVENT;
	public static boolean EVENT_GvGDisableEffect;
	
	public static double EVENT_TFH_POLLEN_CHANCE;
	public static double EVENT_GLITTMEDAL_NORMAL_CHANCE;
	public static double EVENT_GLITTMEDAL_GLIT_CHANCE;
	public static double EVENT_L2DAY_LETTER_CHANCE;
	public static String[] L2_DAY_CUSTOM_DROP;
	public static double EVENT_CHANGE_OF_HEART_CHANCE;
	
	public static double EVENT_TRICK_OF_TRANS_CHANCE;
	
	public static double EVENT_MARCH8_DROP_CHANCE;
	public static double EVENT_MARCH8_PRICE_RATE;
	
	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;
	
	public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
	public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;
	
	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	
	public static double SERVICES_TRADE_TAX;
	public static double SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_MIN_LEVEL;
	
	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
	public static int SERVICES_NO_CARRIER_MAX_TIME;
	public static int SERVICES_NO_CARRIER_MIN_TIME;
	
	public static boolean SERVICES_PK_PVP_KILL_ENABLE;
	public static int SERVICES_PVP_KILL_REWARD_ITEM;
	public static long SERVICES_PVP_KILL_REWARD_COUNT;
	public static int SERVICES_PK_KILL_REWARD_ITEM;
	public static long SERVICES_PK_KILL_REWARD_COUNT;
	public static boolean SERVICES_PK_PVP_TIE_IF_SAME_IP;
	
	public static boolean ALT_OPEN_CLOAK_SLOT;
	
	/** Geodata config */
	public static boolean DAMAGE_FROM_FALLING;
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static String GEOFILES_PATTERN;
	public static boolean ALLOW_GEODATA;
	public static File GEODATA_ROOT;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int CLIENT_Z_SHIFT;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	
	/** Geodata (Pathfind) config */
	public static int PATHFIND_BOOST;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;
	public static int GEODATA_SKILL_CHECK_TASK_INTERVAL;
	
	public static boolean DEBUG;
	
	/**
	 * GM Config
	 */
	public static boolean GIVE_GM_SHOP_TO_ALL_PLAYERS;
	public static boolean KARMA_DROP_GM;
	public static int INVENTORY_MAXIMUM_GM;
	public static boolean GM_LOGIN_INVUL;
	public static boolean GM_LOGIN_IMMORTAL;
	public static boolean GM_LOGIN_INVIS;
	public static boolean GM_LOGIN_SILENCE;
	public static boolean GM_LOGIN_TRADEOFF;
	public static boolean HIDE_GM_STATUS;
	
	public static boolean SAVE_GM_EFFECTS; // Silence, gmspeed, etc...
	public static boolean GM_PM_COMMANDS;
	
	/* Item-Mall Configs */
	public static int GAME_POINT_ITEM_ID;
	
	public static int WEAR_DELAY;
	
	public static boolean GOODS_INVENTORY_ENABLED = false;
	public static boolean EX_NEW_PETITION_SYSTEM;
	public static boolean EX_JAPAN_MINIGAME;
	public static boolean EX_LECTURE_MARK;
	
	/* Top's Config */
	
	public static boolean SMS_PAYMENT_MANAGER_ENABLED;
	public static String SMS_PAYMENT_WEB_ADDRESS;
	public static int SMS_PAYMENT_MANAGER_INTERVAL;
	public static int SMS_PAYMENT_SAVE_DAYS;
	public static String SMS_PAYMENT_SERVER_ADDRESS;
	public static int[] SMS_PAYMENT_REWARD;
	
	public static boolean AUTH_SERVER_GM_ONLY;
	public static boolean AUTH_SERVER_BRACKETS;
	public static boolean AUTH_SERVER_IS_PVP;
	public static int AUTH_SERVER_AGE_LIMIT;
	public static int AUTH_SERVER_SERVER_TYPE;
	
	public static long MAX_PLAYER_CONTRIBUTION;
	
	public static boolean ANTHARAS_DIABLE_CC_ENTER;
	
	public static int FIXINTERVALOFANTHARAS_HOUR;
	public static int FIXINTERVALOFBAIUM_HOUR;
	public static int RANDOMINTERVALOFBAIUM;
	public static int FIXINTERVALOFBAYLORSPAWN_HOUR;
	public static int RANDOMINTERVALOFBAYLORSPAWN;
	public static int FIXINTERVALOFBELETHSPAWN_HOUR;
	public static int FIXINTERVALOFSAILRENSPAWN_HOUR;
	public static int RANDOMINTERVALOFSAILRENSPAWN;
	public static int ANTHARAS_LIMITOFWEAK;
	public static int ANTHARAS_LIMITOFNORMAL;
	public static int RANDOM_TIME_OF_ANTHARAS;
	public static int BOSS_BELETH_MIN_COUNT;
	
	// Valakas
	public static SchedulingPattern VALAKAS_RESPAWN_TIME_PATTERN;
	public static int VALAKAS_SPAWN_DELAY;
	public static int VALAKAS_SLEEP_TIME;
	public static int[][] VALAKAS_ENTERANCE_NECESSARY_ITEMS;
	public static boolean VALAKAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS;


	/* Î ï¿½Î ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î ÎŽÎ¡â€¡Î ÎŠÎ ÎŽÎ Â² Î¡â‚¬Î ÂµÎ Î�Î¡Æ’Î¡â€šÎ Â°Î¡â€ Î ÎˆÎ Îˆ Î Â½Î ÂµÎ ÎŽÎ Â±Î¡â€¦Î ÎŽÎ Î„Î ÎˆÎ ÎŒÎ ÎŽÎ Âµ Î Î„Î Â»Î¡ï¿½ Î Î�Î ÎŽÎ Î„Î Â½Î¡ï¿½Î¡â€šÎ ÎˆÎ¡ï¿½ Î¡Æ’Î¡â‚¬Î ÎŽÎ Â²Î Â½Î¡ï¿½ Î ÎŠÎ Â»Î Â°Î Â½Î¡Æ’. */
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	
	/* Î ï¿½Î ÎŽÎ Â»Î ÎˆÎ¡â€¡Î ÂµÎ¡ï¿½Î¡â€šÎ Â²Î ÎŽ Î¡â€¡Î ÂµÎ Â»Î ÎŽÎ Â²Î ÂµÎ ÎŠ Î Â² Î ÎŠÎ Â»Î Â°Î Â½Î Âµ Î Â½Î ÂµÎ ÎŽÎ Â±Î¡â€¦Î ÎŽÎ Î„Î ÎˆÎ ÎŒÎ ÎŽÎ Âµ Î Î„Î Â»Î¡ï¿½ Î Î�Î ÎŽÎ Î„Î Â½Î¡ï¿½Î¡â€šÎ ÎˆÎ¡ï¿½ Î¡Æ’Î¡â‚¬Î ÎŽÎ Â²Î Â½Î¡ï¿½ Î ÎŠÎ Â»Î Â°Î Â½Î¡Æ’. */
	public static int CLAN_LEVEL_6_REQUIREMEN;
	public static int CLAN_LEVEL_7_REQUIREMEN;
	public static int CLAN_LEVEL_8_REQUIREMEN;
	public static int CLAN_LEVEL_9_REQUIREMEN;
	public static int CLAN_LEVEL_10_REQUIREMEN;
	public static int CLAN_LEVEL_11_REQUIREMEN;
	
	public static int BLOOD_OATHS;
	public static int BLOOD_PLEDGES;
	public static int MIN_ACADEM_POINT;
	public static int MAX_ACADEM_POINT;
	
	public static int VITAMIN_PETS_FOOD_ID;
	
	public static boolean ZONE_PVP_COUNT;
	public static boolean SIEGE_PVP_COUNT;
	
	public static boolean EXPERTISE_PENALTY;
	public static boolean ALT_DISPEL_MUSIC;
	
	public static int ALT_MUSIC_LIMIT;
	public static int ALT_DEBUFF_LIMIT;
	public static int ALT_TRIGGER_LIMIT;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	
	public static int MIN_ADENA_TO_EAT;
	public static int TIME_IF_NOT_FEED;
	public static int INTERVAL_EATING;
	public static int CHANCE_GOLD_LAKFI;
	
	public static double ALT_VITALITY_NEVIT_UP_POINT;
	public static double ALT_VITALITY_NEVIT_POINT;
	
	public static boolean SERVICES_LVL_ENABLED;
	public static int SERVICES_LVL_UP_MAX;
	
	public static int SERVICES_LVL_UP_ITEM;
	
	public static int SERVICES_LVL_79_85_PRICE;
	public static int SERVICES_LVL_1_85_PRICE;
	
	public static boolean ALLOW_INSTANCES_LEVEL_MANUAL;
	public static boolean ALLOW_INSTANCES_PARTY_MANUAL;
	public static int INSTANCES_LEVEL_MIN;
	public static int INSTANCES_LEVEL_MAX;
	public static int INSTANCES_PARTY_MIN;
	public static int INSTANCES_PARTY_MAX;
	public static int INSTANCES_MAX_BOXES;
	public static boolean SOLO_KAMALOKA_ENABLED_FOR_ALL;
	public static boolean SOLO_KAMALOKA_CUSTOMS;
	public static boolean ENABLE_CUSTOM_KRATEI_KUBE;
	
	// Items setting
	public static boolean CAN_BE_TRADED_NO_TARADEABLE;
	public static boolean CAN_BE_TRADED_NO_SELLABLE;
	public static boolean CAN_BE_TRADED_NO_STOREABLE;
	public static boolean CAN_BE_TRADED_SHADOW_ITEM;
	public static boolean CAN_BE_TRADED_HERO_WEAPON;
	
	public static boolean CAN_BE_CWH_IS_AUGMENTED;
	
	public static boolean ALLOW_SOUL_SPIRIT_SHOT_INFINITELY;
	public static boolean ALLOW_ARROW_INFINITELY;
	
	public static boolean ALLOW_START_ITEMS;
	public static boolean BIND_NEWBIE_START_ITEMS_TO_CHAR;
	public static int[] START_ITEMS_MAGE;
	public static int[] START_ITEMS_MAGE_COUNT;
	public static int[] START_ITEMS_FITHER;
	public static int[] START_ITEMS_FITHER_COUNT;
	
	public static int[] START_ITEMS_MAGE_BIND_TO_CHAR;
	public static int[] START_ITEMS_MAGE_COUNT_BIND_TO_CHAR;
	public static int[] START_ITEMS_FITHER_BIND_TO_CHAR;
	public static int[] START_ITEMS_FITHER_COUNT_BIND_TO_CHAR;
	
	public static int START_ITEMS_COPY_FROM_OWNER_OBJ_ID;
	
	public static int HELLBOUND_LEVEL;
	public static boolean HELLBOUND_ENTER_NO_QUEST;
	
	public static boolean COMMUNITYBOARD_ENCHANT_ENABLED;
	public static boolean ALLOW_BBS_ENCHANT_ELEMENTAR;
	public static boolean ALLOW_BBS_ENCHANT_ATT;
	public static int COMMUNITYBOARD_ENCHANT_ITEM;
	public static int COMMUNITY_DONATE_PANEL_ITEMS;
	public static int COMMUNITYBOARD_MAX_ENCHANT;
	public static int[] COMMUNITYBOARD_ENCHANT_LVL;
	public static int[] COMMUNITYBOARD_ENCHANT_PRICE_WEAPON;
	public static int[] COMMUNITYBOARD_ENCHANT_PRICE_ARMOR;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR;
	public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR;
	public static boolean COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP;
	public static boolean COMMUNITY_DROP_LIST;
	public static boolean COMMUNITY_ITEM_INFO;
	public static boolean ALLOW_CB_AUGMENTATION;
	public static int COMMUNITY_AUGMENTATION_MIN_LEVEL;
	public static boolean COMMUNITY_AUGMENTATION_ALLOW_JEWELRY;
	// Community Academy
	public static boolean ENABLE_COMMUNITY_ACADEMY;
	public static String SERVICES_ACADEMY_REWARD;
	public static long ACADEMY_MIN_ADENA_AMOUNT;
	public static long ACADEMY_MAX_ADENA_AMOUNT;
	public static long MAX_TIME_IN_ACADEMY;
	public static int ACADEMY_INVITE_DELAY;
	public static int[] BOSSES_TO_NOT_SHOW;
	public static boolean ALLOW_SENDING_IMAGES;
	
	public static boolean ALLOW_MULTILANG_GATEKEEPER;
	
	public static boolean LOAD_CUSTOM_SPAWN;
	public static boolean SAVE_GM_SPAWN;
	public static boolean LOG_DROPLIST_CORRECTIONS;
	
	private static void loadServerConfig()
	{
		ExProperties serverSettings = load(CONFIGURATION_FILE);
		
		IS_L2WORLD = serverSettings.getProperty("L2World", false);
		AUTH_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
		AUTH_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
		AUTH_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
		AUTH_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
		for (String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
		{
			if (a.trim().isEmpty())
			{
				continue;
			}
			
			ServerType t = ServerType.valueOf(a.toUpperCase());
			AUTH_SERVER_SERVER_TYPE |= t.getMask();
		}
		
		SECOND_AUTH_ENABLED = serverSettings.getProperty("SAEnabled", false);
		SECOND_AUTH_BAN_ACC = serverSettings.getProperty("SABanAccEnabled", false);
		SECOND_AUTH_STRONG_PASS = serverSettings.getProperty("SAStrongPass", false);
		SECOND_AUTH_MAX_ATTEMPTS = serverSettings.getProperty("SAMaxAttemps", 5);
		SECOND_AUTH_BAN_TIME = serverSettings.getProperty("SABanTime", 480);
		
		ACCEPT_ALTERNATE_ID = serverSettings.getProperty("AcceptAlternateID", true);
		
		CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
		ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		
		PARALIZE_ON_RAID_DIFF = serverSettings.getProperty("ParalizeOnRaidLevelDiff", true);
		
		AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
		AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
		DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
		
		try
		{
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
		}
		catch (IOException e)
		{
			_log.error("", e);
		}
		
		ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
		ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
		ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
		ALLOW_WATER = serverSettings.getProperty("AllowWater", true);
		ALLOW_CURSED_WEAPONS = serverSettings.getProperty("AllowCursedWeapons", false);
		DROP_CURSED_WEAPONS_ON_KICK = serverSettings.getProperty("DropCursedWeaponsOnKick", false);
		
		MIN_PROTOCOL_REVISION = serverSettings.getProperty("MinProtocolRevision", 267);
		MAX_PROTOCOL_REVISION = serverSettings.getProperty("MaxProtocolRevision", 271);
		
		AUTOSAVE = serverSettings.getProperty("Autosave", true);
		
		MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);
		ONLINE_PLUS = serverSettings.getProperty("OnlineUsersPlus", 1);
		
		ALLOW_ONLINE_PARSE = serverSettings.getProperty("AllowParsTotalOnline", false);
		FIRST_UPDATE = serverSettings.getProperty("FirstOnlineUpdate", 1);
		DELAY_UPDATE = serverSettings.getProperty("OnlineUpdate", 5);
		
//		DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
//		DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
//		DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
//		DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);
//		
//		DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
//		DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
//		DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
		
		LOGINSERVER_DB_NAME = serverSettings.getProperty("LoginServerDBName", "l2jdb");
		MYSQL_DUMP_PATH = serverSettings.getProperty("MySqlDumpPath", "");
		USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
		BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);
		
		EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);
		
		SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
		EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);
		
		ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);
		ThreadPoolManagerDebug = serverSettings.getProperty("ThreadPoolManagerDebug", false);
		ThreadPoolManagerDebugInterval = serverSettings.getProperty("ThreadPoolManagerDebugInterval", 1000);
		ThreadPoolManagerDebugDeflect = serverSettings.getProperty("ThreadPoolManagerDebugDeflect", 0);
		ThreadPoolManagerDebugLogConsol = serverSettings.getProperty("ThreadPoolManagerDebugLogConsol", true);
		ThreadPoolManagerDebugLogConsolDelay = serverSettings.getProperty("ThreadPoolManagerDebugLogConsolDelay", 0);
		ThreadPoolManagerDebugLogFile = serverSettings.getProperty("ThreadPoolManagerDebugLogFile", true);
		
		SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
		SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
		SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
		SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
		SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
		SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);
		
		DEFAULT_LANG = serverSettings.getProperty("DefaultLang", "ru");
		RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");
		SHIFT_BY = serverSettings.getProperty("HShift", 12);
		SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
		MAP_MIN_Z = serverSettings.getProperty("MapMinZ", -32768);
		MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", 32767);
		
		MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
		ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);
		
		MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);
		
		WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);
		
		ALT_VITALITY_NEVIT_UP_POINT = serverSettings.getProperty("AltVitalityNevitUpPoint", 100);
		ALT_VITALITY_NEVIT_POINT = serverSettings.getProperty("AltVitalityNevitPoint", 100);
		
		ALLOW_ADDONS_CONFIG = serverSettings.getProperty("AllowAddonsConfig", false);
	}
	
	private static void loadChatConfig()
	{
		ExProperties chatSettings = load(CHAT_FILE);
		GLOBAL_SHOUT = chatSettings.getProperty("GlobalShout", false);
		GLOBAL_TRADE_CHAT = chatSettings.getProperty("GlobalTradeChat", false);
		CHAT_RANGE = chatSettings.getProperty("ChatRange", 1250);
		SHOUT_OFFSET = chatSettings.getProperty("ShoutOffset", 0);
		TRADE_WORDS = new GArray<>();
		String T_WORLD = chatSettings.getProperty("TradeWords", "trade,sell,selling,buy,exchange,barter,Ãƒâ€žÃ¢â‚¬Å¡Ã„ï¿½Ã…Â¼Ã‹ï¿½Ã„â€šÃ‹ËœÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‹ËœÃƒâ€žÃ¢â‚¬Å¡Ã„ï¿½Ã…Â¼Ã‹ï¿½Ã„â€šÃ¢â‚¬Å¡Ãƒâ€¹Ã¯Â¿Â½Ãƒâ€žÃ¢â‚¬Å¡Ã„ï¿½Ã…Â¼Ã‹ï¿½Ã„â€šÃ¢â‚¬Å¡Ãƒâ€¹Ã¯Â¿Â½,Ãƒâ€žÃ¢â‚¬Å¡Ã„ï¿½Ã…Â¼Ã‹ï¿½Ã„â€šÃ‹ËœÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‹ËœÃƒâ€žÃ¢â‚¬Å¡Ã„ï¿½Ã…Â¼Ã‹ï¿½Ã„â€šÃ¢â‚¬Å¡Ãƒâ€¹Ã¯Â¿Â½S,WTB,WTB,WTT,WTS");
		String[] T_WORLDS = T_WORLD.split(",", -1);
		for (String w : T_WORLDS)
		{
			TRADE_WORDS.add(w);
		}
		_log.info("Loaded " + TRADE_WORDS.size() + " trade words.");
		LOG_CHAT = chatSettings.getProperty("LogChat", false);
		CHAT_MESSAGE_MAX_LEN = chatSettings.getProperty("ChatMessageLimit", 1000);
		ABUSEWORD_BANCHAT = chatSettings.getProperty("ABUSEWORD_BANCHAT", false);
		int counter = 0;
		for (int id : chatSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[]
		{
			0
		}))
		{
			BAN_CHANNEL_LIST[counter] = id;
			counter++;
		}
		ABUSEWORD_REPLACE = chatSettings.getProperty("ABUSEWORD_REPLACE", false);
		ABUSEWORD_REPLACE_STRING = chatSettings.getProperty("ABUSEWORD_REPLACE_STRING", "[censored]");
		BANCHAT_ANNOUNCE = chatSettings.getProperty("BANCHAT_ANNOUNCE", true);
		BANCHAT_ANNOUNCE_FOR_ALL_WORLD = chatSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
		BANCHAT_ANNOUNCE_NICK = chatSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
		ABUSEWORD_BANTIME = chatSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);
		CHATFILTER_MIN_LEVEL = chatSettings.getProperty("ChatFilterMinLevel", 0);
		CHATS_REQUIRED_LEVEL = chatSettings.getProperty("ChatsRequiredLevel", 21);
		PM_REQUIRED_LEVEL = chatSettings.getProperty("PMPlayersInChat", 61);
		SHOUT_REQUIRED_LEVEL = chatSettings.getProperty("ShoutingInChat", 61);
		RECORD_WROTE_CHAT_MSGS_COUNT = chatSettings.getProperty("RecordWroteChatMsgsCount", false);
		ANNOUNCE_VOTE_DELAY = chatSettings.getProperty("AnnounceVoteDelay", 60);
		counter = 0;
		for (int id : chatSettings.getProperty("ChatFilterChannels", new int[]
		{
			1,
			8
		}))
		{
			CHATFILTER_CHANNELS[counter] = id;
			counter++;
		}
		CHATFILTER_WORK_TYPE = chatSettings.getProperty("ChatFilterWorkType", 1);
	}
	
	private static void loadResidenceConfig()
	{
		ExProperties residenceSettings = load(RESIDENCE_CONFIG_FILE);
		
		RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.);
		
		PERIOD_CASTLE_SIEGE = residenceSettings.getProperty("CastleSiegeIntervalWeeks", 2);
		
		int[] tempCastleValidatonTime = residenceSettings.getProperty("CastleValidationDate", new int[]
		{
			2,
			4,
			2003
		});
		CASTLE_VALIDATION_DATE = Calendar.getInstance();
		CASTLE_VALIDATION_DATE.set(Calendar.DAY_OF_MONTH, tempCastleValidatonTime[0]);
		CASTLE_VALIDATION_DATE.set(Calendar.MONTH, tempCastleValidatonTime[1] - 1);
		CASTLE_VALIDATION_DATE.set(Calendar.YEAR, tempCastleValidatonTime[2]);
		CASTLE_VALIDATION_DATE.set(Calendar.HOUR_OF_DAY, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.MINUTE, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.SECOND, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.MILLISECOND, 0);
		
		DOMINION_INTERVAL_WEEKS = residenceSettings.getProperty("DominionIntervalWeeks", 2);
		
		TW_SELECT_HOURS = residenceSettings.getProperty("TwSelectHours", 20);
		int[] tempTwValidatonTime = residenceSettings.getProperty("TwValidationDate", new int[]
		{
			2,
			4,
			2003
		});
		TW_VALIDATION_DATE = Calendar.getInstance();
		TW_VALIDATION_DATE.set(Calendar.DAY_OF_MONTH, tempTwValidatonTime[0]);
		TW_VALIDATION_DATE.set(Calendar.MONTH, tempTwValidatonTime[1] - 1);
		TW_VALIDATION_DATE.set(Calendar.YEAR, tempTwValidatonTime[2]);
		TW_VALIDATION_DATE.set(Calendar.HOUR_OF_DAY, 0);
		TW_VALIDATION_DATE.set(Calendar.MINUTE, 0);
		TW_VALIDATION_DATE.set(Calendar.SECOND, 0);
		TW_VALIDATION_DATE.set(Calendar.MILLISECOND, 0);
		
		RETURN_WARDS_WHEN_TW_STARTS = residenceSettings.getProperty("ReturnWardsWhenTWStarts", false);
		PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE = residenceSettings.getProperty("PlayerWithWardCanBeKilledInPeaceZone", false);
		INTERVAL_FLAG_DROP = residenceSettings.getProperty("MinutesUntillFlagDissapearIfOut", 5);
		
		FAME_REWARD_FORTRESS = residenceSettings.getProperty("FameRewardFortress", 31);
		FAME_REWARD_CASTLE = residenceSettings.getProperty("FameRewardCastle", 125);
		
		RATE_SIEGE_FAME_MIN = residenceSettings.getProperty("RateSiegeFameMin", 10);
		RATE_SIEGE_FAME_MAX = residenceSettings.getProperty("RateSiegeFameMax", 20);
		
		RATE_DOMINION_SIEGE_FAME_MIN = residenceSettings.getProperty("RateDominionSiegeFameMin", 10);
		RATE_DOMINION_SIEGE_FAME_MAX = residenceSettings.getProperty("RateDominionSiegeFameMax", 20);
		
		CLEAN_CLAN_HALLS_ON_TIME = residenceSettings.getProperty("CleanClanHalls", false);
		MIN_PLAYERS_IN_CLAN_TO_KEEP_CH = residenceSettings.getProperty("MinActiveMemberstoNOTDELETE", 11);
		DAYS_TO_CHECK_FOR_CH_DELETE = residenceSettings.getProperty("DaysToCheckForClanHallDelete", 7);
		ENABLE_ALT_FAME_REWARD = residenceSettings.getProperty("AltEnableCustomFame", false);
		ALT_FAME_CASTLE = residenceSettings.getProperty("CastleFame", 125);
		ALT_FAME_FORTRESS = residenceSettings.getProperty("FortressFame", 31);
		ALLOW_START_FORTRESS_SIEGE_FEE = residenceSettings.getProperty("AllowStartFortressSiegeFee", true);
		START_FORTRESS_SIEGE_PRICE_ID = residenceSettings.getProperty("StartFortressSiegePriceId", 57);
		START_FORTRESS_SIEGE_PRICE_AMOUNT = residenceSettings.getProperty("StartFortressSiegePriceAmount", 250000);
		FORTRESS_SIEGE_ALLOW_SINGLE_PLAYERS = residenceSettings.getProperty("FortressSiegeAllowSinglePlayers", false);
		FORTRESS_REMOVE_FLAG_ON_LEAVE_ZONE = residenceSettings.getProperty("FortFlagReturnOnLeaveZone", false);
		DOMINION_REMOVE_FLAG_ON_LEAVE_ZONE = residenceSettings.getProperty("ReturnFlagOnSiegeZoneLeave", false);
	}
	
	private static void loadFightClubSettings()
	{
		ExProperties eventFightClubSettings = load(EVENT_FIGHT_CLUB_FILE);
		
		FIGHT_CLUB_ENABLED = eventFightClubSettings.getProperty("FightClubEnabled", false);
		MINIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MinimumLevel", 1);
		MAXIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MaximumLevel", 85);
		MAXIMUM_LEVEL_DIFFERENCE = eventFightClubSettings.getProperty("MaximumLevelDifference", 10);
		ALLOWED_RATE_ITEMS = eventFightClubSettings.getProperty("AllowedItems", "").trim().replaceAll(" ", "").split(",");
		PLAYERS_PER_PAGE = eventFightClubSettings.getProperty("RatesOnPage", 10);
		ARENA_TELEPORT_DELAY = eventFightClubSettings.getProperty("ArenaTeleportDelay", 5);
		CANCEL_BUFF_BEFORE_FIGHT = eventFightClubSettings.getProperty("CancelBuffs", true);
		UNSUMMON_PETS = eventFightClubSettings.getProperty("UnsummonPets", true);
		UNSUMMON_SUMMONS = eventFightClubSettings.getProperty("UnsummonSummons", true);
		REMOVE_CLAN_SKILLS = eventFightClubSettings.getProperty("RemoveClanSkills", false);
		REMOVE_HERO_SKILLS = eventFightClubSettings.getProperty("RemoveHeroSkills", false);
		TIME_TO_PREPARATION = eventFightClubSettings.getProperty("TimeToPreparation", 10);
		FIGHT_TIME = eventFightClubSettings.getProperty("TimeToDraw", 300);
		ALLOW_DRAW = eventFightClubSettings.getProperty("AllowDraw", true);
		TIME_TELEPORT_BACK = eventFightClubSettings.getProperty("TimeToBack", 10);
		FIGHT_CLUB_ANNOUNCE_RATE = eventFightClubSettings.getProperty("AnnounceRate", false);
		FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceRateToAllScreen", false);
		FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceStartBatleToAllScreen", false);
		FIGHT_CLUB_ANNOUNCE_TOP_KILLER = eventFightClubSettings.getProperty("FightClubAnnounceTopKiller", false);
		FIGHT_CLUB_SUMMON_LOSE_BUFFS_ON_DEATH = eventFightClubSettings.getProperty("SummonLoseBuffsOnDeath", true);
		EVENT_KOREAN_RESET_REUSE = eventFightClubSettings.getProperty("Korean_ResetReuse", false);
	}
	
	
	
	

	/** Rate control */
	public static double RATE_XP;
	public static double RATE_SP;
	public static double RATE_QUESTS_REWARD;
	public static double RATE_QUESTS_DROP;
	public static double RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static double RATE_DROP_ADENA;
	public static boolean ADENA_100_PERCENT;
	public static double RATE_DROP_CHAMPION;
	public static double RATE_CHAMPION_DROP_ADENA;
	public static double RATE_DROP_SPOIL_CHAMPION;
	public static double RATE_DROP_ITEMS;
	public static double RATE_CHANCE_GROUP_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_HERBS;
	public static double RATE_CHANCE_SPOIL;
	public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_EPOLET;
	public static double RATE_ENCHANT_SCROLL;
	public static boolean CHAMPION_DROP_ONLY_ADENA;
	public static double RATE_DROP_HERBS;
	public static double RATE_DROP_ATT;
	public static double RATE_DROP_LIFE_STONE;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static double RATE_DROP_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;
	public static double RATE_DROP_RECIPES;

	public static double RATE_DROP_RAIDBOSS;
	public static double RATE_DROP_SPOIL;
	public static int[] NO_RATE_ITEMS;
	public static double RATE_DROP_SIEGE_GUARD;
	public static double RATE_MANOR;
	public static double RATE_FISH_DROP_COUNT;
	public static boolean RATE_PARTY_MIN;
	public static double RATE_HELLBOUND_CONFIDENCE;
	public static boolean NO_RATE_EQUIPMENT;

	public static int RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;
	
	public static boolean ALT_VITALITY_ENABLED;
	//public static double ALT_VITALITY_RATE;
	public static double ALT_VITALITY_CONSUME_RATE;
	public static int ALT_VITALITY_RAID_BONUS;
	public static final int[] VITALITY_LEVELS =
	{
		240,
		2000,
		13000,
		17000,
		20000
	};
	
	public static boolean FRINTEZZA_ALL_MEMBERS_NEED_SCROLL;
	public static int MAX_CLAN_REPUTATIONS_POINTS;
	public static double RATE_PRICE_ENCHANT_SKILL;
	public static double RATE_QUESTS_REWARD_EXPSP;
	public static double RATE_QUESTS_REWARD_ADENA;
	public static double RATE_DROP_COMMON_ITEMS;
	public static double RATE_CHANCE_DROP_LIFES;
	public static double RATE_CHANCE_DROP_ENCHANT;
	public static double RATE_CHANCE_DROP_BUTESTONE;

	private static void loadRatesConfig()
	{

		ExProperties ratesSettings = load(RATES_FILE);

		RATE_XP = ratesSettings.getProperty("RateXp", 1.);
		RATE_SP = ratesSettings.getProperty("RateSp", 1.);
		RATE_QUESTS_REWARD = ratesSettings.getProperty("RateQuestsReward", 1.);
		RATE_QUESTS_DROP = ratesSettings.getProperty("RateQuestsDrop", 1.);
		RATE_DROP_CHAMPION = ratesSettings.getProperty("RateDropChampion", 1.);
		RATE_CLAN_REP_SCORE = ratesSettings.getProperty("RateClanRepScore", 1.);
		RATE_CLAN_REP_SCORE_MAX_AFFECTED = ratesSettings.getProperty("RateClanRepScoreMaxAffected", 2);
		RATE_DROP_ADENA = ratesSettings.getProperty("RateDropAdena", 1.);
		ADENA_100_PERCENT = ratesSettings.getProperty("Adena100PercentDrop", false);
		RATE_CHAMPION_DROP_ADENA = ratesSettings.getProperty("RateChampionDropAdena", 1.);
		RATE_DROP_SPOIL_CHAMPION = ratesSettings.getProperty("RateSpoilChampion", 1.);
		RATE_DROP_ITEMS = ratesSettings.getProperty("RateDropItems", 1.);
		RATE_CHANCE_GROUP_DROP_ITEMS = ratesSettings.getProperty("RateChanceGroupDropItems", 1.);
		RATE_CHANCE_DROP_ITEMS = ratesSettings.getProperty("RateChanceDropItems", 1.);
		RATE_CHANCE_DROP_HERBS = ratesSettings.getProperty("RateChanceDropHerbs", 0.1);
		RATE_CHANCE_SPOIL = ratesSettings.getProperty("RateChanceSpoil", 1.);
		RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceSpoilWAA", 1.);
		RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceDropWAA", 1.);
		RATE_CHANCE_DROP_EPOLET = ratesSettings.getProperty("RateChanceDropEpolets", 1.);
		CHAMPION_DROP_ONLY_ADENA = ratesSettings.getProperty("ChampionDropOnlyAdena", false);
		RATE_ENCHANT_SCROLL = ratesSettings.getProperty("RateDropEnchantScroll", 1.);
		RATE_DROP_HERBS = ratesSettings.getProperty("RateDropHerbs", 1.);
		RATE_DROP_ATT = ratesSettings.getProperty("RateDropAtt", 1.);
		RATE_DROP_LIFE_STONE = ratesSettings.getProperty("RateDropLifeStone", 1.);
		NO_RATE_KEY_MATERIAL = ratesSettings.getProperty("NoRateKeyMaterial", true);
		RATE_DROP_KEY_MATERIAL = ratesSettings.getProperty("RateDropKeyMaterial", 1.);
		NO_RATE_RECIPES = ratesSettings.getProperty("NoRateRecipes", true);
		RATE_DROP_RECIPES = ratesSettings.getProperty("RateDropRecipes", 1.);
		RATE_DROP_RAIDBOSS = ratesSettings.getProperty("RateRaidBoss", 1.);
		RATE_DROP_SPOIL = ratesSettings.getProperty("RateDropSpoil", 1.);
		NO_RATE_ITEMS = ratesSettings.getProperty("NoRateItemIds", new int[]
			{
			6660,
			6662,
			6661,
			6659,
			6656,
			6658,
			8191,
			6657,
			10170,
			10314,
			16025,
			16026
			});
		NO_RATE_EQUIPMENT = ratesSettings.getProperty("NoRateEquipment", true);
		RATE_DROP_SIEGE_GUARD = ratesSettings.getProperty("RateSiegeGuard", 1.);
		RATE_MANOR = ratesSettings.getProperty("RateManor", 1.);
		RATE_FISH_DROP_COUNT = ratesSettings.getProperty("RateFishDropCount", 1.);
		RATE_PARTY_MIN = ratesSettings.getProperty("RatePartyMin", false);
		RATE_HELLBOUND_CONFIDENCE = ratesSettings.getProperty("RateHellboundConfidence", 1.);

		RATE_MOB_SPAWN = ratesSettings.getProperty("RateMobSpawn", 1);
		RATE_MOB_SPAWN_MIN_LEVEL = ratesSettings.getProperty("RateMobMinLevel", 1);
		RATE_MOB_SPAWN_MAX_LEVEL = ratesSettings.getProperty("RateMobMaxLevel", 100);
		
		ALT_VITALITY_ENABLED = ratesSettings.getProperty("AltVitalityEnabled", true);
		//ALT_VITALITY_RATE = ratesSettings.getProperty("AltVitalityRate", 1.);
		ALT_VITALITY_CONSUME_RATE = ratesSettings.getProperty("AltVitalityConsumeRate", 1.);
		ALT_VITALITY_RAID_BONUS = ratesSettings.getProperty("AltVitalityRaidBonus", 2000);
		
		FRINTEZZA_ALL_MEMBERS_NEED_SCROLL = ratesSettings.getProperty("FrintezzaAllMembersNeedScroll",false);
		MAX_CLAN_REPUTATIONS_POINTS = ratesSettings.getProperty("MaxClanReputationPoints",0);
		
		RATE_PRICE_ENCHANT_SKILL = ratesSettings.getProperty("RatePriceEnchantSkill",1.);
		
		RATE_QUESTS_REWARD_EXPSP = ratesSettings.getProperty("RateQuestsRewardWExSp",1.);
		RATE_QUESTS_REWARD_ADENA = ratesSettings.getProperty("RateQuestsRewardAdena",1.);

		RATE_DROP_COMMON_ITEMS = ratesSettings.getProperty("RateDropCommonItems",1.);
		RATE_CHANCE_DROP_LIFES = ratesSettings.getProperty("RateChanceDropLifes",1.);
		RATE_CHANCE_DROP_ENCHANT  = ratesSettings.getProperty("RateChanceDropEnchant",1.);
		RATE_CHANCE_DROP_BUTESTONE  = ratesSettings.getProperty("RateChanceDropButesStone",1.);

	}
	
	/** Spoil Rates */
	public static double BASE_SPOIL_RATE;
	public static double MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;

	/** Manor Config */
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static int MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static int MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;
	
	/** Allow Manor system */
	public static boolean ALLOW_MANOR;

	/** Manor Refresh Starting time */
	public static int MANOR_REFRESH_TIME;

	/** Manor Refresh Min */
	public static int MANOR_REFRESH_MIN;

	/** Manor Next Period Approve Starting time */
	public static int MANOR_APPROVE_TIME;

	/** Manor Next Period Approve Min */
	public static int MANOR_APPROVE_MIN;

	/** Manor Maintenance Time */
	public static int MANOR_MAINTENANCE_PERIOD;


	
	public static void loadSpoilConfig()
	{
		ExProperties spoilSettings = load(SPOIL_CONFIG_FILE);

		BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.);
		MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.);
		ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
		MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.);
		MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.);
		MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.);
		MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
		MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.);
		MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
		MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.);
		ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
		MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
		MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 00);
		MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
		MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 00);
		MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
	}
	
	public static int L2TOP_REWARD_CHECK_TIME;
	public static int L2TOP_REWARD_VOTES;
	public static boolean ALLOW_L2TOP_VOTE_REWARD;
	public static boolean L2TOP_REPORT_LOG;
	public static int L2TOP_DUAL_BOX;
	public static String L2TOP_SERVER_LINK;
	public static int L2TOP_ITEM_REWARD;
	public static int L2TOP_ITEM_COUNT;
	
	public static int L2JBRASIL_REWARD_CHECK_TIME;
	public static int L2JBRASIL_REWARD_VOTES;
	public static boolean ALLOW_L2JBRASIL_VOTE_REWARD;
	public static boolean L2JBRASIL_REPORT_LOG;
	public static int L2JBRASIL_DUAL_BOX;
	public static String L2JBRASIL_SERVER_LINK;
	public static int L2JBRASIL_ITEM_REWARD;
	public static int L2JBRASIL_ITEM_COUNT;
	
	public static int L2HOPZONE_REWARD_CHECK_TIME;
	public static int L2HOPZONE_REWARD_VOTES;
	public static boolean ALLOW_L2HOPZONE_VOTE_REWARD;
	public static boolean L2HOPZONE_REPORT_LOG;
	public static int L2HOPZONE_DUAL_BOX;
	public static String L2HOPZONE_SERVER_LINK;
	public static int L2HOPZONE_ITEM_REWARD;
	public static int L2HOPZONE_ITEM_COUNT;
	
	public static int L2NETWORK_REWARD_CHECK_TIME;
	public static int L2NETWORK_REWARD_VOTES;
	public static boolean ALLOW_L2NETWORK_VOTE_REWARD;
	public static boolean L2NETWORK_REPORT_LOG;
	public static int L2NETWORK_DUAL_BOX;
	public static String L2NETWORK_SERVER_LINK;
	public static int L2NETWORK_ITEM_REWARD;
	public static int L2NETWORK_ITEM_COUNT;
	
	private static void loadVoteConfig()
	{
		ExProperties voteSettings = load(VOTE_CONFIG_FILE);
		
		L2TOP_REWARD_CHECK_TIME = voteSettings.getProperty("L2TopRewardCheckTime", 30);
		L2TOP_REWARD_VOTES  = voteSettings.getProperty("L2TopRewardVotes", 3);
		ALLOW_L2TOP_VOTE_REWARD  = voteSettings.getProperty("L2TopVoteReward", false);
		L2TOP_REPORT_LOG  = voteSettings.getProperty("L2TopReportLog", false);
		L2TOP_DUAL_BOX  = voteSettings.getProperty("L2TopDualBox", 1);
		L2TOP_SERVER_LINK = voteSettings.getProperty("L2TopServerLink", "");
		L2TOP_ITEM_REWARD = voteSettings.getProperty("L2TopItemReward", 57);
		L2TOP_ITEM_COUNT = voteSettings.getProperty("L2TopItemCount", 1);
		
		L2JBRASIL_REWARD_CHECK_TIME = voteSettings.getProperty("L2jbrasilRewardCheckTime", 30);
		L2JBRASIL_REWARD_VOTES  = voteSettings.getProperty("L2jbrasilRewardVotes", 3);
		ALLOW_L2JBRASIL_VOTE_REWARD  = voteSettings.getProperty("L2jbrasilVoteReward", false);
		L2JBRASIL_REPORT_LOG  = voteSettings.getProperty("L2jbrasilReportLog", false);
		L2JBRASIL_DUAL_BOX  = voteSettings.getProperty("L2jbrasilDualBox", 1);
		L2JBRASIL_SERVER_LINK = voteSettings.getProperty("L2jbrasilServerLink", "");
		L2JBRASIL_ITEM_REWARD = voteSettings.getProperty("L2jbrasilItemReward", 57);
		L2JBRASIL_ITEM_COUNT = voteSettings.getProperty("L2jbrasilItemCount", 1);
		
		L2HOPZONE_REWARD_CHECK_TIME  = voteSettings.getProperty("L2HopzoneRewardCheckTime",30);
		L2HOPZONE_REWARD_VOTES  = voteSettings.getProperty("L2HopzoneRewardVotes",3);
		ALLOW_L2HOPZONE_VOTE_REWARD  = voteSettings.getProperty("L2HopzoneVoteReward",false);
		L2HOPZONE_REPORT_LOG  = voteSettings.getProperty("L2HopzoneReportLog",false);
		L2HOPZONE_DUAL_BOX  = voteSettings.getProperty("L2HopzoneDualBox",1);
		L2HOPZONE_SERVER_LINK  = voteSettings.getProperty("L2HopzoneServerLink","");
		L2HOPZONE_ITEM_REWARD  = voteSettings.getProperty("L2HopzoneItemReward",57);
		L2HOPZONE_ITEM_COUNT  = voteSettings.getProperty("L2HopzoneItemCount",1);
		
		L2NETWORK_REWARD_CHECK_TIME  = voteSettings.getProperty("L2NetworkRewardCheckTime",30);
		L2NETWORK_REWARD_VOTES  = voteSettings.getProperty("L2NetworkRewardVotes",3);
		ALLOW_L2NETWORK_VOTE_REWARD  = voteSettings.getProperty("L2NetworkVoteReward",false);
		L2NETWORK_REPORT_LOG  = voteSettings.getProperty("L2NetworkReportLog",false);
		L2NETWORK_DUAL_BOX  = voteSettings.getProperty("L2NetworkDualBox",1);
		L2NETWORK_SERVER_LINK  = voteSettings.getProperty("L2NetworkServerLink","");
		L2NETWORK_ITEM_REWARD  = voteSettings.getProperty("L2NetworkItemReward",57);
		L2NETWORK_ITEM_COUNT  = voteSettings.getProperty("L2NetworkItemCount",1);
	}
	
	private static void loadInstancesConfig()
	{
		ExProperties instancesSettings = load(INSTANCES_FILE);
		
		ALLOW_INSTANCES_LEVEL_MANUAL = instancesSettings.getProperty("AllowInstancesLevelManual", false);
		ALLOW_INSTANCES_PARTY_MANUAL = instancesSettings.getProperty("AllowInstancesPartyManual", false);
		INSTANCES_LEVEL_MIN = instancesSettings.getProperty("InstancesLevelMin", 1);
		INSTANCES_LEVEL_MAX = instancesSettings.getProperty("InstancesLevelMax", 85);
		INSTANCES_PARTY_MIN = instancesSettings.getProperty("InstancesPartyMin", 2);
		INSTANCES_PARTY_MAX = instancesSettings.getProperty("InstancesPartyMax", 100);
		INSTANCES_MAX_BOXES = instancesSettings.getProperty("InstancesMaxBoxes", -1);
		
		ENABLE_CUSTOM_KRATEI_KUBE = instancesSettings.getProperty("EnableCustomKrateiCube", false);
		
		SOLO_KAMALOKA_ENABLED_FOR_ALL = instancesSettings.getProperty("SoloKamalokaEnabledForAll", false);
		SOLO_KAMALOKA_CUSTOMS = instancesSettings.getProperty("SoloKamalokaCustoms", false);
	}
	
	private static void loadEpicBossConfig()
	{
		ExProperties epicBossSettings = load(EPIC_BOSS_FILE);
		
		ANTHARAS_DIABLE_CC_ENTER = epicBossSettings.getProperty("ValakasDisableCCenter", false);
		FIXINTERVALOFANTHARAS_HOUR = epicBossSettings.getProperty("FWA_FIX_INTERVAL_OF_ANTHARAS_HOUR", 264);
		RANDOM_TIME_OF_ANTHARAS = epicBossSettings.getProperty("RANDOM_TIME_OF_ANTHARAS", 6);
		
		FIXINTERVALOFBAIUM_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_BAIUM_HOUR", 120);
		RANDOMINTERVALOFBAIUM = epicBossSettings.getProperty("RANDOM_INTERVAL_OF_BAIUM", 8);
		
		FIXINTERVALOFBAYLORSPAWN_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_BAYLOR_SPAWN_HOUR", 24);
		RANDOMINTERVALOFBAYLORSPAWN = epicBossSettings.getProperty("RANDOM_INTERVAL_OF_BAYLOR_SPAWN", 24);
		
		FIXINTERVALOFBELETHSPAWN_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_BELETH_SPAWN_HOUR", 48);
		BOSS_BELETH_MIN_COUNT = epicBossSettings.getProperty("BossBelethMinCount", 50);
		
		FIXINTERVALOFSAILRENSPAWN_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_SAILREN_SPAWN_HOUR", 24);
		RANDOMINTERVALOFSAILRENSPAWN = epicBossSettings.getProperty("RANDOM_INTERVAL_OF_SAILREN_SPAWN", 24);
		ANTHARAS_LIMITOFWEAK  = epicBossSettings.getProperty("ANTHARAS_LIMITOFWEAK", 12);
		ANTHARAS_LIMITOFNORMAL  = epicBossSettings.getProperty("ANTHARAS_LIMITOFNORMAL", 12);
		
		// Valakas
		VALAKAS_RESPAWN_TIME_PATTERN = new SchedulingPattern(epicBossSettings.getProperty("VALAKAS_RESPAWN_TIME_PATTERN", "~480:* * +11:* * *"));
		VALAKAS_SPAWN_DELAY = epicBossSettings.getProperty("VALAKAS_SPAWN_DELAY", 10);
		VALAKAS_SLEEP_TIME = epicBossSettings.getProperty("VALAKAS_SLEEP_TIME", 20);
		VALAKAS_ENTERANCE_NECESSARY_ITEMS = StringArrayUtils.stringToIntArray2X(epicBossSettings.getProperty("VALAKAS_ENTERANCE_NECESSARY_ITEMS", "7267-1"), ";", "-");
		VALAKAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS = epicBossSettings.getProperty("VALAKAS_ENTERANCE_CAN_CONSUME_NECESSARY_ITEMS", true);

	}
	
	  public static String[] NPC_PATK_MUL;
	  public static String[] NPC_MATK_MUL;
	  public static String[] NPC_MAX_HP_MUL;
	  public static String[] NPC_MAX_MP_MUL;
	  public static String[] NPC_PDEF_MUL;
	  public static String[] NPC_MDEF_MUL;
	  public static String[] RAID_PATK_MUL;
	  public static String[] RAID_MATK_MUL;  
	  public static String[] RAID_MAX_HP_MUL;  
	  public static String[] RAID_MAX_MP_MUL;  
	  public static String[] RAID_PDEF_MUL;  
	  public static String[] RAID_MDEF_MUL;  
	  public static String[] RAID_REGEN_HP;
	  public static String[] RAID_REGEN_MP;
	//  public static int[] NPC_NO_REFLECT;
	  public static int TREASURECHEST_CHANCE;
	  public static boolean RAID_DROP_GLOBAL_ITEMS;
	  public static int MIN_RAID_LEVEL_TO_DROP;
	  public static List<RaidGlobalDrop> RAID_GLOBAL_DROP = new ArrayList<RaidGlobalDrop>();
	
	private static void loadNpcSetting()
	{
		ExProperties npc = load(NPC_FILE);
	    NPC_PATK_MUL = npc.getProperty("NpcPAtk", "").trim().replaceAll(" ", "").split(";");
	    NPC_MATK_MUL = npc.getProperty("NpcMAtk", "").trim().replaceAll(" ", "").split(";");
	    NPC_MAX_HP_MUL = npc.getProperty("NpcMaxHp", "").trim().replaceAll(" ", "").split(";");
	    NPC_MAX_MP_MUL = npc.getProperty("NpcMaxMp", "").trim().replaceAll(" ", "").split(";");
	    NPC_PDEF_MUL = npc.getProperty("NpcPdef", "").trim().replaceAll(" ", "").split(";");
	    NPC_MDEF_MUL = npc.getProperty("NpcMdef", "").trim().replaceAll(" ", "").split(";");
	    RAID_PATK_MUL = npc.getProperty("RaidPAtk", "").trim().replaceAll(" ", "").split(";");
	    RAID_MATK_MUL = npc.getProperty("RaidMAtk", "").trim().replaceAll(" ", "").split(";");
	    RAID_MAX_HP_MUL = npc.getProperty("RaidMaxHp", "").trim().replaceAll(" ", "").split(";");
	    RAID_MAX_MP_MUL = npc.getProperty("RaidMaxMp", "").trim().replaceAll(" ", "").split(";");
	    RAID_PDEF_MUL = npc.getProperty("RaidPdef", "").trim().replaceAll(" ", "").split(";");
	    RAID_MDEF_MUL = npc.getProperty("RaidMdef", "").trim().replaceAll(" ", "").split(";");
	    RAID_REGEN_HP = npc.getProperty("RaidRegenHp", "").trim().replaceAll(" ", "").split(";");
	    RAID_REGEN_MP = npc.getProperty("RaidRegenMp", "").trim().replaceAll(" ", "").split(";");
	  //  NPC_NO_REFLECT = npc.getProperty("NpcNoReflect", new int[] {});
	    TREASURECHEST_CHANCE = npc.getProperty("TreasureChestChance", 25);
	    
		MIN_RAID_LEVEL_TO_DROP = npc.getProperty("MinRaidLevelToDropItem", 0);
		
		RAID_DROP_GLOBAL_ITEMS = npc.getProperty("AltEnableGlobalRaidDrop", false);
		String[] infos = npc.getProperty("RaidGlobalDrop", new String[0], ";");
		for(String info : infos) 
		{
			if(info.isEmpty())
				continue;

			String[] data = info.split(",");
			int id = Integer.parseInt(data[0]);
			long count = Long.parseLong(data[1]);
			double chance = Double.parseDouble(data[2]);
			RAID_GLOBAL_DROP.add(new RaidGlobalDrop(id, count, chance));
		}
	}
	
	private static void loadFormulasConfig()
	{
		ExProperties formulasSettings = load(FORMULAS_CONFIGURATION_FILE);
		
		SKILL_CHANCE_CALCULATED_BY_ENCHANT_LEVEL = formulasSettings.getProperty("CalculateSkillSuccessByEnchantLevel", false);
		
		CUSTOM_BONUS_SKILL_CHANCE = formulasSettings.getProperty("CustomBonusSkillChance", 1.0);
		
		NON_BACK_BLOW_MULTIPLIER = formulasSettings.getProperty("NonBackSkillModifier", 2.04);
		BACK_BLOW_MULTIPLIER = formulasSettings.getProperty("BackSkillsModifier", 1.5);
		SKILL_FORCE_H5_FORMULA = formulasSettings.getProperty("SkillForceH5Formula", true);
		SKILLS_CHANCE_OLD_FORMULA = formulasSettings.getProperty("SkillsChanceOldFormula", false);
		SKILLS_CHANCE_SHOW = formulasSettings.getProperty("SkillsShowChance", true);
		SKILLS_CHANCE_MOD_MAGE = formulasSettings.getProperty("SkillsChanceModMage", 11.);
		SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 10);
		SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 90);
		SKILLS_CHANCE_CAP_ONLY_PLAYERS = formulasSettings.getProperty("SkillsChanceCapOnlyPlayers", false);
		SKILLS_MOB_CHANCE = formulasSettings.getProperty("SkillsMobChance", 0.5);
		SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);
		SKILLS_PAST_TIME_MIN = formulasSettings.getProperty("SkillsPastTimeMin", 333);
		PHYS_SKILLS_DAMAGE_POW = formulasSettings.getProperty("PhysSkillsDamagePow", 1.0);
		ALT_ABSORB_DAMAGE_MODIFIER = formulasSettings.getProperty("AbsorbDamageModifier", 1.0);
		
		LIMIT_PATK = formulasSettings.getProperty("LimitPatk", 20000);
		LIMIT_MATK = formulasSettings.getProperty("LimitMAtk", 25000);
		LIMIT_PDEF = formulasSettings.getProperty("LimitPDef", 15000);
		LIMIT_MDEF = formulasSettings.getProperty("LimitMDef", 15000);
		LIMIT_PATK_SPD = formulasSettings.getProperty("LimitPatkSpd", 1500);
		LIMIT_MATK_SPD = formulasSettings.getProperty("LimitMatkSpd", 1999);
		LIMIT_CRIT_DAM = formulasSettings.getProperty("LimitCriticalDamage", 2000);
		LIMIT_CRIT = formulasSettings.getProperty("LimitCritical", 500);
		LIMIT_MCRIT = formulasSettings.getProperty("LimitMCritical", 20);
		LIMIT_ACCURACY = formulasSettings.getProperty("LimitAccuracy", 200);
		LIMIT_EVASION = formulasSettings.getProperty("LimitEvasion", 200);
		LIMIT_MOVE = formulasSettings.getProperty("LimitMove", 250);
		SKILLS_CHANCE_MOD = formulasSettings.getProperty("SkillsChanceMod", 11.);
		SKILLS_CHANCE_POW = formulasSettings.getProperty("SkillsChancePow", 0.5);
		
		LIMIT_REFLECT = new TIntIntHashMap();
		Arrays.stream(formulasSettings.getProperty("LimitReflect", "-1,10000").split(";")).map(s -> s.split(",", 2)).forEach(s -> LIMIT_REFLECT.put(Integer.parseInt(s[0]), Integer.parseInt(s[1])));
		
		CUBIC_MATK_MULT = new TIntDoubleHashMap();
		Arrays.stream(formulasSettings.getProperty("CubicMAtkMultiplier", "-1,1.0").split(";")).map(s -> s.split(",", 2)).forEach(s -> CUBIC_MATK_MULT.put(Integer.parseInt(s[0]), Double.parseDouble(s[1])));
		
		ALT_ELEMENT_FORMULA = formulasSettings.getProperty("AltElementFormula", false);

		ALT_BOW_PVP_DAMAGE_MODIFIER = formulasSettings.getProperty("BowPvpDamageModifier", 1.0);
		ALT_BOW_PVE_DAMAGE_MODIFIER = formulasSettings.getProperty("BowPveDamageModifier", 1.0);
		ALT_PET_PVP_DAMAGE_MODIFIER = formulasSettings.getProperty("PetPvpDamageModifier", 1.0);
		
		FORMULA_LETHAL_MAX_HP = formulasSettings.getProperty("LethalImmuneHp", 50000);
		
		ALT_BASE_MCRIT_RATE = formulasSettings.getProperty("BaseMcritRate", 10.0);
		ALT_BASE_MCRIT_DAMAGE = formulasSettings.getProperty("BaseMcriDamage", 2.5);
		
		CANCEL_BUFF_MODIFIER = formulasSettings.getProperty("CancelModifier", 40);
		MAGIC_DEBUFF_MATK_POW = formulasSettings.getProperty("MagicDebuffMatkPow", 0.3);
		ALT_LETHAL_DIFF_LEVEL = formulasSettings.getProperty("SkillLethalDiffLevel", 7);
		ALT_LETHAL_PENALTY = formulasSettings.getProperty("SkillLethalPenalty", true);
		LIM_FAME = formulasSettings.getProperty("LimitFame", 50000);
		DEBUFF_PROTECTION_SYSTEM = formulasSettings.getProperty("DebuffProtectionSystem", false);
		BUFF_RETURN_OLYMPIAD_TIME = formulasSettings.getProperty("BuffReturnOlympiadTime", -1);
		BUFF_RETURN_AUTO_EVENTS_TIME = formulasSettings.getProperty("BuffReturnAutoEventsTime", -1);
		BUFF_RETURN_NORMAL_LOCATIONS_TIME = formulasSettings.getProperty("BuffReturnNormalLocationsTime", -1);
		ALT_POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("AltPoleDamageModifier", 1.0);
		
		MIN_NPC_LEVEL_DMG_PENALTY = formulasSettings.getProperty("MinNPCLevelForDmgPenalty", 78);
		NPC_CRIT_DMG_PENALTY = formulasSettings.getProperty("CritDmgPenaltyForLvLDifferences",new double[]
				{
					0.75	// "0.75, 0.65, 0.6, 0.58");
				});
		NPC_SKILL_DMG_PENALTY = formulasSettings.getProperty("SkillDmgPenaltyForLvLDifferences", new double[]
				{
					0.75	// "0.75, 0.65, 0.6, 0.58");
				});//"0.8, 0.7, 0.65, 0.62"));

		NPC_DMG_PENALTY = formulasSettings.getProperty("DmgPenaltyForLvLDifferences",
				new double[]
						{
							0.75	
						});


		
		// Raid Boss Settings
		PDAM_TO_MONSTER_SUB_LVL_DIFF = formulasSettings.getProperty("PDAM_TO_MONSTER_SUB_LVL_DIFF", 0);
		PDAM_TO_RAID_SUB_LVL_DIFF = formulasSettings.getProperty("PDAM_TO_RAID_SUB_LVL_DIFF", 0);
		MDAM_TO_MONSTER_SUB_LVL_DIFF = formulasSettings.getProperty("MDAM_TO_MONSTER_SUB_LVL_DIFF", 0);
		MDAM_TO_RAID_SUB_LVL_DIFF = formulasSettings.getProperty("MDAM_TO_RAID_SUB_LVL_DIFF", 0);

		RAID_MAX_LEVEL_DIFF = formulasSettings.getProperty("RaidMaxLevelDiff", 8);
	}
	
	private static void loadDevelopSettings()
	{
		ExProperties DevelopSettings = load(DEVELOP_FILE);
		
		CLAN_HALL_AUCTION_LENGTH = DevelopSettings.getProperty("ClanHallAuctionLength", 7);
		
		ALT_DEBUG_ENABLED = DevelopSettings.getProperty("AltDebugEnabled", false);
		DONTLOADSPAWN = DevelopSettings.getProperty("StartWithoutSpawn", false);
		DONTLOADQUEST = DevelopSettings.getProperty("StartWithoutQuest", false);
		DONTLOADEVENTS = DevelopSettings.getProperty("StartWithoutEvents", false);
		DONTLOADOPTIONDATA = DevelopSettings.getProperty("DontLoadOptionData", false);
		DONTLOADNPCDROP = DevelopSettings.getProperty("DontLoadNpcDrop", false);
		DONTLOADMULTISELLS = DevelopSettings.getProperty("DontLoadMultisells", false);
		DONTAUTOANNOUNCE = DevelopSettings.getProperty("DontAutoAnnounce", false);
		PREMIUMWC = DevelopSettings.getProperty("PremiumWelcome", false);
		LOAD_CUSTOM_SPAWN = DevelopSettings.getProperty("LoadAddGmSpawn", false);
		SAVE_GM_SPAWN = DevelopSettings.getProperty("SaveGmSpawn", false);
		LOG_DROPLIST_CORRECTIONS = DevelopSettings.getProperty("LogDroplistCorrections", false);
		ACC_MOVE_ENABLED = DevelopSettings.getProperty("Acc_move_enabled", false);
		ACC_MOVE_ITEM = DevelopSettings.getProperty("Acc_move_item", 57);
		ACC_MOVE_PRICE = DevelopSettings.getProperty("Acc_move_price", 57);
		
		BUFFER_ON = DevelopSettings.getProperty("Buffer", false);
		BUFFER_PET_ENABLED = DevelopSettings.getProperty("Buffer_pet", false);
		BUFFER_PRICE = DevelopSettings.getProperty("Buffer_price", 20);
		BUFFER_MIN_LVL = DevelopSettings.getProperty("Buffer_min_lvl", 1);
		BUFFER_MAX_LVL = DevelopSettings.getProperty("Buffer_max_lvl", 99);
		TalkGuardChance = DevelopSettings.getProperty("TalkGuardChance", 4037);
		TalkNormalChance = DevelopSettings.getProperty("TalkNormalChance", 4037);
		TalkNormalPeriod = DevelopSettings.getProperty("TalkNormalPeriod", 4037);
		TalkAggroPeriod = DevelopSettings.getProperty("TalkAggroPeriod", 4037);
		
		// anti Flood protection
		ALL_CHAT_USE_MIN_LEVEL = DevelopSettings.getProperty("ALL_CHAT_USE_MIN_LEVEL", 1);
		ALL_CHAT_USE_DELAY = DevelopSettings.getProperty("ALL_CHAT_USE_DELAY", 0);
		SHOUT_CHAT_USE_MIN_LEVEL = DevelopSettings.getProperty("SHOUT_CHAT_USE_MIN_LEVEL", 1);
		SHOUT_CHAT_USE_DELAY = DevelopSettings.getProperty("SHOUT_CHAT_USE_DELAY", 0);
		TRADE_CHAT_USE_MIN_LEVEL = DevelopSettings.getProperty("TRADE_CHAT_USE_MIN_LEVEL", 1);
		TRADE_CHAT_USE_DELAY = DevelopSettings.getProperty("TRADE_CHAT_USE_DELAY", 0);
		HERO_CHAT_USE_MIN_LEVEL = DevelopSettings.getProperty("HERO_CHAT_USE_MIN_LEVEL", 1);
		HERO_CHAT_USE_DELAY = DevelopSettings.getProperty("HERO_CHAT_USE_DELAY", 0);
		PRIVATE_CHAT_USE_MIN_LEVEL = DevelopSettings.getProperty("PRIVATE_CHAT_USE_MIN_LEVEL", 1);
		PRIVATE_CHAT_USE_DELAY = DevelopSettings.getProperty("PRIVATE_CHAT_USE_DELAY", 0);
		MAIL_USE_MIN_LEVEL = DevelopSettings.getProperty("MAIL_USE_MIN_LEVEL", 1);
		MAIL_USE_DELAY = DevelopSettings.getProperty("MAIL_USE_DELAY", 0);
		PARTY__DELAY_TIME = DevelopSettings.getProperty("PartyDelayTime", 3);
		HWID_DUALBOX_NUMBER = DevelopSettings.getProperty("HwidDualBoxNumber", 0);
		DUALBOX_NUMBER_IP = DevelopSettings.getProperty("DualBoxNumberIp", 0);
		MAX_CHARS_PER_PC = DevelopSettings.getProperty("MaxChars", 20);
		HWID_DUALBOX_NUMBER_OUTSIDE_PEACE = DevelopSettings.getProperty("HwidDualBoxNumberOutsidePeace", 0);
		DUALBOX_NUMBER_IP_OUTSIDE_PEACE = DevelopSettings.getProperty("DualBoxNumberIpOutsidePeace", 0);
		MIN_LEVEL_TO_USE_SHOUT = DevelopSettings.getProperty("MinLevelToUseShoutChat", 1);
		BOWTAN_PENALTY = DevelopSettings.getProperty("BowPenaltyToTanks", false);
		NPC_DIALOG_PLAYER_DELAY = DevelopSettings.getProperty("NpcDialogPlayerDelay", 1);
		// New system protection packetDealy
		SEND_WAREHOUSE_WITH_DRAWLIST_PACKETDELAY = DevelopSettings.getProperty("SendWareHouseWithDrawListPacketDelay", 1000);
		SEND_WAREHOUSE_DEPOSIT_LIST_PACKETDELAY = DevelopSettings.getProperty("SendWareHouseDepositListPacketDelay", 1000);
		REQUEST_SETPLEDGE_CRESTLARGE_PACKETDELAY = DevelopSettings.getProperty("RequestSetPledgeCrestLargePacketDelay", 10000);
		REQUEST_SETPLEGDE_CREAST_PACKETDELAY = DevelopSettings.getProperty("RequestSetPledgeCrestPacketDelay", 10000);
		REQUEST_RECIPESHOPMANAGE_QUITPACKETDEALAY = DevelopSettings.getProperty("RequestRecipeShopManageQuitPacketDelay", 1500);
		REQUESTBYPASSTOSERVERPACKTDELAY = DevelopSettings.getProperty("RequestBypassToServerPacketDelay", 100);
		APPEARINGPACKETDELAY = DevelopSettings.getProperty("AppearingPacketDelay", 500);
		REQUESTPRIVATESTOREQUITBUYPACKETDELAY = DevelopSettings.getProperty("RequestPrivateStoreQuitBuyPacketDelay", 1500);
		REQUESTRELOADPACKETDELAY = DevelopSettings.getProperty("RequestReloadPacketDelay", 1000);
		REQUESTACTUIONUSEPACKETDELAY = DevelopSettings.getProperty("RequestActionUsePacketDelay", 100);
		REQUSTEXBR_LECTUREMARKPACKETDELAY = DevelopSettings.getProperty("RequestExBR_LectureMarkPacketDelay", 1500);
		REQUESTPARTYMATCHLISTPACKETDELAY = DevelopSettings.getProperty("RequestPartyMatchListPacketDelay", 1500);
		REQUESTPREVIEWITEMPACKETDELAY = DevelopSettings.getProperty("RequestPreviewItemPacketDelay", 1500);
		REQUESTPRIVATESTOREQUITSELLPACKETDEALY = DevelopSettings.getProperty("RequestPrivateStoreQuitSellPacketDelay", 1500);
		REQUESTRECIPESHOPLISTSETPACKETDELAY = DevelopSettings.getProperty("RequestRecipeShopListSetPacketDelay", 1500);
		REQUESTREFINECANCELPACKETDELAY = DevelopSettings.getProperty("RequestRefineCancelPacketDelay", 1500);
		REQUESTWITHDRAWALPLEDGEPACKETDELAY = DevelopSettings.getProperty("RequestWithdrawalPledgePacketDelay", 1500);
		SETPRIVATESTOREBUYLISTPACKETDELAY = DevelopSettings.getProperty("SetPrivateStoreBuyListPacketDelay", 1500);
		SETPRIVATESTORELISTPACKETDELAY = DevelopSettings.getProperty("SetPrivateStoreListPacketDelay", 1500);
		REQUESTMAGICSKILLUSEPACKETDELAY = DevelopSettings.getProperty("RequestMagicSkillUsePacketDelay", 100);
		PACKET_FLOOD_PROTECTION_IN_MS = DevelopSettings.getProperty("PacketFloodProtectionInMs", 50);
		ENTER_WORLD_FLOOD_PROECTION_IN_MS = DevelopSettings.getProperty("EnterWorldFloodProtectionInMs", 50);
		// New system protection packetDealy
		// Punishment if account
		BUGUSER_PUNISH = DevelopSettings.getProperty("BugUserPunishment", 2);
		DEFAULT_PUNISH = DevelopSettings.getProperty("IllegalActionPunishment", 1);
		// Punishment if account
		ALLOW_ITEMS_LOGGING = DevelopSettings.getProperty("AllowItemsLogging", true);
		ANTIFEED_ENABLE = DevelopSettings.getProperty("AntiFeedEnable", false);
		ANTIFEED_DUALBOX = DevelopSettings.getProperty("AntiFeedDualbox", true);
		ANTIFEED_DISCONNECTED_AS_DUALBOX = DevelopSettings.getProperty("AntiFeedDisconnectedAsDualbox", true);
		ANTIFEED_INTERVAL = DevelopSettings.getProperty("AntiFeedInterval", 120) * 1000;
		ANTIFEED_MAX_LVL_DIFFERENCE = DevelopSettings.getProperty("AntiFeedMaxLvlDifference", 0);
		MAX_ITEM_ENCHANT_KICK = DevelopSettings.getProperty("EnchantKick", 0);
		ALLOW_JUST_MOVING = DevelopSettings.getProperty("AllowJustMoving", false);
		PARTY_TELEPORT  = DevelopSettings.getProperty("PartyTeleport", false);
		CUSTOM_TELEPORT_ITEM  = DevelopSettings.getProperty("CustomTeleportItem", 6673);
		CUSTOM_TELEPORT_COUNT  = DevelopSettings.getProperty("CustomTeleportCount", 1);
		CUSTOM_MESSAGER_ITEMS  = DevelopSettings.getProperty("CustomMessagerItems", "");

		ALLOW_DUALBOX = DevelopSettings.getProperty("AllowDualBox",false);
		ALLOWED_BOXES = DevelopSettings.getProperty("AllowedBoxes", 2);
	}
	
	private static void loadExtSettings()
	{
		ExProperties properties = load(EXT_FILE);
		
		EX_NEW_PETITION_SYSTEM = properties.getProperty("NewPetitionSystem", false);
		EX_JAPAN_MINIGAME = properties.getProperty("JapanMinigame", false);
		EX_LECTURE_MARK = properties.getProperty("LectureMark", false);
		
		// Emulation OFF Core (packet SendStatus)
		Random ppc = new Random();
		int z = ppc.nextInt(6);
		if (z == 0)
		{
			z += 2;
		}
		for (int x = 0; x < 8; x++)
		{
			if (x == 4)
			{
				RWHO_ARRAY[x] = 44;
			}
			else
			{
				RWHO_ARRAY[x] = 51 + ppc.nextInt(z);
			}
		}
		RWHO_ARRAY[11] = 37265 + ppc.nextInt((z * 2) + 3);
		RWHO_ARRAY[8] = 51 + ppc.nextInt(z);
		z = 36224 + ppc.nextInt(z * 2);
		RWHO_ARRAY[9] = z;
		RWHO_ARRAY[10] = z;
		RWHO_ARRAY[12] = 1;
		// RWHO_LOG = properties.getProperty("RemoteWhoLog", false);
		RWHO_SEND_TRASH = properties.getProperty("RemoteWhoSendTrash", false);
		RWHO_KEEP_STAT = properties.getProperty("RemoteOnlineKeepStat", 5);
	}
	
	private static void loadItemsSettings()
	{
		ExProperties itemsProperties = load(ITEMS_FILE);
		
		CAN_BE_TRADED_NO_TARADEABLE = itemsProperties.getProperty("CanBeTradedNoTradeable", false);
		CAN_BE_TRADED_NO_SELLABLE = itemsProperties.getProperty("CanBeTradedNoSellable", false);
		CAN_BE_TRADED_NO_STOREABLE = itemsProperties.getProperty("CanBeTradedNoStoreable", false);
		CAN_BE_TRADED_SHADOW_ITEM = itemsProperties.getProperty("CanBeTradedShadowItem", false);
		CAN_BE_TRADED_HERO_WEAPON = itemsProperties.getProperty("CanBeTradedHeroWeapon", false);
		
		CAN_BE_CWH_IS_AUGMENTED = itemsProperties.getProperty("CanBeCwhIsAugmented", false);
		ALLOW_SOUL_SPIRIT_SHOT_INFINITELY = itemsProperties.getProperty("AllowSoulSpiritShotInfinitely", false);
		ALLOW_ARROW_INFINITELY = itemsProperties.getProperty("AllowArrowInfinitely", false);
		ALLOW_START_ITEMS = itemsProperties.getProperty("AllowStartItems", false);
		BIND_NEWBIE_START_ITEMS_TO_CHAR = itemsProperties.getProperty("BindItemsToCharacter", false);
		START_ITEMS_MAGE = itemsProperties.getProperty("StartItemsMageIds", new int[]
		{
			57
		});
		START_ITEMS_MAGE_COUNT = itemsProperties.getProperty("StartItemsMageCount", new int[]
		{
			1
		});
		START_ITEMS_FITHER = itemsProperties.getProperty("StartItemsFigtherIds", new int[]
		{
			57
		});
		START_ITEMS_FITHER_COUNT = itemsProperties.getProperty("StartItemsFigtherCount", new int[]
		{
			1
		});
		
		// bind to acc
		START_ITEMS_MAGE_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsMageIds", new int[]
		{
			57
		});
		START_ITEMS_MAGE_COUNT_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsMageCount", new int[]
		{
			1
		});
		START_ITEMS_FITHER_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsFigtherIds", new int[]
		{
			57
		});
		START_ITEMS_FITHER_COUNT_BIND_TO_CHAR = itemsProperties.getProperty("BindCharStartItemsFigtherCount", new int[]
		{
			1
		});
		
		START_ITEMS_COPY_FROM_OWNER_OBJ_ID = itemsProperties.getProperty("StartItemsCopyFromOwnerObjId", 0);
		
		// UseItems
		ITEM_USE_LIST_ID = itemsProperties.getProperty("ItemUseListId", new int[]
		{
			725,
			726,
			727,
			728
		});
		ITEM_USE_IS_COMBAT_FLAG = itemsProperties.getProperty("ItemUseIsCombatFlag", true);
		ITEM_USE_IS_ATTACK = itemsProperties.getProperty("ItemUseIsAttack", true);
		ITEM_USE_IS_EVENTS = itemsProperties.getProperty("ItemUseIsEvents", true);
		MAX_ADENA = itemsProperties.getProperty("MaxAdena", -1);
		ALLOW_DROP_CALCULATOR = itemsProperties.getProperty("AllowDropCalculator", true);

		DROP_CALCULATOR_DISABLED_TELEPORT = itemsProperties.getProperty("DropCalculatorDisabledTeleport", new int[] {});
	}
	
	/** Ancient Herb */
	public static int ANCIENT_HERB_SPAWN_RADIUS;
	public static int ANCIENT_HERB_SPAWN_CHANCE;
	public static int ANCIENT_HERB_SPAWN_COUNT;
	public static int ANCIENT_HERB_RESPAWN_TIME;
	public static int ANCIENT_HERB_DESPAWN_TIME;
	public static List<Location> HEIN_FIELDS_LOCATIONS = new ArrayList<>();
	
	private static void loadAltSettings()
	{
		ExProperties altSettings = load(ALT_SETTINGS_FILE);
		
		ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
		ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
		ALT_MAIL_MIN_LVL = altSettings.getProperty("MinLevelToSendMail", 0);
		ALT_SAVE_UNSAVEABLE = altSettings.getProperty("AltSaveUnsaveable", false);
		SHIELD_SLAM_BLOCK_IS_MUSIC = altSettings.getProperty("ShieldSlamBlockIsMusic", false);
		ALT_SAVE_EFFECTS_REMAINING_TIME = altSettings.getProperty("AltSaveEffectsRemainingTime", 5);
		ALT_SHOW_REUSE_MSG = altSettings.getProperty("AltShowSkillReuseMessage", true);
		ALT_DELETE_SA_BUFFS = altSettings.getProperty("AltDeleteSABuffs", false);
		AUTO_LOOT = altSettings.getProperty("AutoLoot", false);
		AUTO_LOOT_ONLY_ADENA = altSettings.getProperty("AutoLootOnlyAdena", false);
		AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_INDIVIDUAL = altSettings.getProperty("AutoLootIndividual", false);
		AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
		AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
		
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", true);
		ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = altSettings.getProperty("AltKarmaPlayerCanTeleport", true);
		ALT_GAME_KARMA_PLAYER_CAN_USE_GK = altSettings.getProperty("AltKarmaPlayerCanUseGK", false);
		ALT_GAME_KARMA_PLAYER_CAN_TRADE = altSettings.getProperty("AltKarmaPlayerCanTrade", true);
		ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", true);
		
		SAVING_SPS = altSettings.getProperty("SavingSpS", false);
		MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
		
		CRAFT_MASTERWORK_CHANCE = altSettings.getProperty("CraftMasterworkChance", 3.);
		CRAFT_DOUBLECRAFT_CHANCE = altSettings.getProperty("CraftDoubleCraftChance", 3.);
		
		ALT_RAID_RESPAWN_MULTIPLIER = altSettings.getProperty("AltRaidRespawnMultiplier", 1.0);
		ALT_ALLOW_AUGMENT_ALL = altSettings.getProperty("AugmentAll", false);
		ALT_ALLOW_DROP_AUGMENTED = altSettings.getProperty("AlowDropAugmented", false);
		ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
		ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
		ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
		ALT_PLAYER_SHIFTCLICK = altSettings.getProperty("AllowPlayerShiftClick", false);
		ALT_FULL_NPC_STATS_PAGE = altSettings.getProperty("AltFullStatsPage", false);
		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubClassWithoutQuest", false);
		ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM = altSettings.getProperty("AltAllowSubClassWithoutBaium", true);
		ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
		ALT_GAME_START_LEVEL_TO_SUBCLASS = altSettings.getProperty("AltStartLevelToSubclass", 40);
		ALT_GAME_SUB_ADD = altSettings.getProperty("AltSubAdd", 0);
		ALL_SUBCLASSES_AVAILABLE = altSettings.getProperty("AllSubclassesAvailable", false);
		ALT_GAME_SUB_BOOK = altSettings.getProperty("AltSubBook", false);
		ALT_MAX_LEVEL = Math.min(altSettings.getProperty("AltMaxLevel", 85), Experience.LEVEL.length - 1);
		ALT_MAX_SUB_LEVEL = Math.min(altSettings.getProperty("AltMaxSubLevel", 80), Experience.LEVEL.length - 1);
		ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);
		ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = altSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);
		EXPELLED_MEMBER_PENALTY = altSettings.getProperty("ExpelledMemberPenalty", 24);
		LEAVED_ALLY_PENALTY = altSettings.getProperty("LeavedAllyPenalty", 24);
		DISSOLVED_ALLY_PENALTY = altSettings.getProperty("DissolvedAllyPenalty", 24);
		DISSOLVED_CLAN_PENALTY = altSettings.getProperty("DissolvedClanPenalty", 24);
		
		ALT_GAME_REQUIRE_CLAN_CASTLE = altSettings.getProperty("AltRequireClanCastle", false);
		ALT_GAME_REQUIRE_CASTLE_DAWN = altSettings.getProperty("AltRequireCastleDawn", true);
		ALT_GAME_ALLOW_ADENA_DAWN = altSettings.getProperty("AltAllowAdenaDawn", true);
		ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
		SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
		PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
		AUTO_LEARN_SKILLS = altSettings.getProperty("AutoLearnSkills", false);
		AUTO_LEARN_FORGOTTEN_SKILLS = altSettings.getProperty("AutoLearnForgottenSkills", false);
		AUTO_LEARN_DIVINE_INSPIRATION = altSettings.getProperty("AutoLearnDivineInspiration", false);
		ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);
		ALT_DISABLE_SPELLBOOKS = altSettings.getProperty("AltDisableSpellbooks", false);
		ALT_SIMPLE_SIGNS = altSettings.getProperty("PushkinSignsOptions", false);
		ALT_TELE_TO_CATACOMBS = altSettings.getProperty("TeleToCatacombs", false);
		ALT_BS_CRYSTALLIZE = altSettings.getProperty("BSCrystallize", false);
		
		ALT_ALLOW_TATTOO = altSettings.getProperty("AllowTattoo", false);
		ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
		ALT_DEATH_PENALTY = altSettings.getProperty("EnableAltDeathPenalty", false);
		ALLOW_DEATH_PENALTY_C5 = altSettings.getProperty("EnableDeathPenaltyC5", true);
		ALT_DEATH_PENALTY_C5_CHANCE = altSettings.getProperty("DeathPenaltyC5Chance", 10);
		ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = altSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);
		ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
		ALT_DEATH_PENALTY_C5_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
		ALT_PK_DEATH_RATE = altSettings.getProperty("AltPKDeathRate", 0.);
		NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;
		ALT_NO_LASTHIT = altSettings.getProperty("NoLasthitOnRaid", false);
		ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = altSettings.getProperty("KamalokaNightmaresPremiumOnly", false);
		
		ALT_PET_HEAL_BATTLE_ONLY = altSettings.getProperty("PetsHealOnlyInBattle", true);
		CHAR_TITLE = altSettings.getProperty("CharTitle", false);
		ADD_CHAR_TITLE = altSettings.getProperty("CharAddTitle", "");
		
		ALT_ALLOW_SELL_COMMON = altSettings.getProperty("AllowSellCommon", true);
		ALT_ALLOW_SHADOW_WEAPONS = altSettings.getProperty("AllowShadowWeapons", true);
		ALT_DISABLED_MULTISELL = altSettings.getProperty("DisabledMultisells", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_PRICE_LIMITS = altSettings.getProperty("ShopPriceLimits", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_UNALLOWED_ITEMS = altSettings.getProperty("ShopUnallowedItems", ArrayUtils.EMPTY_INT_ARRAY);
		
		ALT_ALLOWED_PET_POTIONS = altSettings.getProperty("AllowedPetPotions", new int[]
		{
			735,
			1060,
			1061,
			1062,
			1374,
			1375,
			1539,
			1540,
			6035,
			6036
		});
		
		FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
		FESTIVAL_RATE_PRICE = altSettings.getProperty("FestivalRatePrice", 1.0);
		
		RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
		RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 10000);
		RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
		RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
		RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);
		
		RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
		RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
		RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
		RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
		RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
		RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
		ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = altSettings.getProperty("AllowLearnTransSkillsWOQuest", false);
		PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
		ALLOW_TALK_WHILE_SITTING = altSettings.getProperty("AllowTalkWhileSitting", true);
		ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);
		
		CLANHALL_BUFFTIME_MODIFIER = altSettings.getProperty("ClanHallBuffTimeModifier", 0);
		SONGDANCETIME_MODIFIER = altSettings.getProperty("SongDanceTimeModifier", 0);
		MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0);
		GATEKEEPER_MODIFIER = altSettings.getProperty("GkCostMultiplier", 1.0);
		GATEKEEPER_FREE = altSettings.getProperty("GkFree", 40);
		CRUMA_GATEKEEPER_LVL = altSettings.getProperty("GkCruma", 65);
		ALT_IMPROVED_PETS_LIMITED_USE = altSettings.getProperty("ImprovedPetsLimitedUse", false);
		
		ALT_CHAMPION_CHANCE1 = altSettings.getProperty("AltChampionChance1", 0.);
		ALT_CHAMPION_CHANCE2 = altSettings.getProperty("AltChampionChance2", 0.);
		ALT_CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("AltChampionAggro", false);
		ALT_CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("AltChampionSocial", false);
		ALT_CHAMPION_DROP_HERBS = altSettings.getProperty("AltChampionDropHerbs", false);
		ALT_SHOW_MONSTERS_AGRESSION = altSettings.getProperty("AltShowMonstersAgression", false);
		ALT_SHOW_MONSTERS_LVL = altSettings.getProperty("AltShowMonstersLvL", false);
		ALT_CHAMPION_TOP_LEVEL = altSettings.getProperty("AltChampionTopLevel", 75);
		ALT_CHAMPION_MIN_LEVEL = altSettings.getProperty("AltChampionMinLevel", 20);
		
		ALT_PCBANG_POINTS_ENABLED = altSettings.getProperty("AltPcBangPointsEnabled", false);
		ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE = altSettings.getProperty("AltPcBangPointsDoubleChance", 10.);
		ALT_PCBANG_POINTS_BONUS = altSettings.getProperty("AltPcBangPointsBonus", 0);
		ALT_PCBANG_POINTS_DELAY = altSettings.getProperty("AltPcBangPointsDelay", 20);
		ALT_PCBANG_POINTS_MIN_LVL = altSettings.getProperty("AltPcBangPointsMinLvl", 1);
		
		ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
		ALT_LEVEL_DIFFERENCE_PROTECTION = altSettings.getProperty("LevelDifferenceProtection", -100);
		ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
		ALT_PARTY_BONUS = altSettings.getProperty("AltPartyBonus", new double[]
		{
			1.00,
			1.10,
			1.20,
			1.30,
			1.40,
			1.50,
			2.00,
			2.10,
			2.20
		});
		
		ALT_REMOVE_SKILLS_ON_DELEVEL = altSettings.getProperty("AltRemoveSkillsOnDelevel", true);
		ALT_CH_ALL_BUFFS = altSettings.getProperty("AltChAllBuffs", false);
		ALT_CH_ALLOW_1H_BUFFS = altSettings.getProperty("AltChAllowHourBuff", false);
		AUGMENTATION_NG_SKILL_CHANCE = altSettings.getProperty("AugmentationNGSkillChance", 15);
		AUGMENTATION_NG_GLOW_CHANCE = altSettings.getProperty("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_SKILL_CHANCE = altSettings.getProperty("AugmentationMidSkillChance", 30);
		AUGMENTATION_MID_GLOW_CHANCE = altSettings.getProperty("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_SKILL_CHANCE = altSettings.getProperty("AugmentationHighSkillChance", 45);
		AUGMENTATION_HIGH_GLOW_CHANCE = altSettings.getProperty("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_SKILL_CHANCE = altSettings.getProperty("AugmentationTopSkillChance", 60);
		AUGMENTATION_TOP_GLOW_CHANCE = altSettings.getProperty("AugmentationTopGlowChance", 100);
		AUGMENTATION_BASESTAT_CHANCE = altSettings.getProperty("AugmentationBaseStatChance", 1);
		AUGMENTATION_ACC_SKILL_CHANCE = altSettings.getProperty("AugmentationAccSkillChance", 10);
		
		ALT_OPEN_CLOAK_SLOT = altSettings.getProperty("OpenCloakSlot", false);
		
		FOLLOW_RANGE = altSettings.getProperty("FollowRange", 100);
		
		ALT_ENABLE_MULTI_PROFA = altSettings.getProperty("AltEnableMultiProfa", false);
		
		ALT_ITEM_AUCTION_ENABLED = altSettings.getProperty("AltItemAuctionEnabled", true);
		ALT_ITEM_AUCTION_CAN_REBID = altSettings.getProperty("AltItemAuctionCanRebid", false);
		ALT_ITEM_AUCTION_START_ANNOUNCE = altSettings.getProperty("AltItemAuctionAnnounce", true);
		ALT_ITEM_AUCTION_BID_ITEM_ID = altSettings.getProperty("AltItemAuctionBidItemId", 57);
		ALT_ITEM_AUCTION_MAX_BID = altSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
		ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = altSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);
		
		ALT_FISH_CHAMPIONSHIP_ENABLED = altSettings.getProperty("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = altSettings.getProperty("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = altSettings.getProperty("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = altSettings.getProperty("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = altSettings.getProperty("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = altSettings.getProperty("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = altSettings.getProperty("AltFishChampionshipReward5", 100000);
		
		ALT_ENABLE_BLOCK_CHECKER_EVENT = altSettings.getProperty("EnableBlockCheckerEvent", true);
		ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Util.constrain(altSettings.getProperty("BlockCheckerMinTeamMembers", 1), 1, 6);
		ALT_RATE_COINS_REWARD_BLOCK_CHECKER = altSettings.getProperty("BlockCheckerRateCoinReward", 1.);
		
		ALT_HBCE_FAIR_PLAY = altSettings.getProperty("HBCEFairPlay", false);
		
		ALT_PET_INVENTORY_LIMIT = altSettings.getProperty("AltPetInventoryLimit", 12);
		ALT_CLAN_LEVEL_CREATE = altSettings.getProperty("ClanLevelCreate", 0);
		CLAN_LEVEL_6_COST = altSettings.getProperty("ClanLevel6Cost", 5000);
		CLAN_LEVEL_7_COST = altSettings.getProperty("ClanLevel7Cost", 10000);
		CLAN_LEVEL_8_COST = altSettings.getProperty("ClanLevel8Cost", 20000);
		CLAN_LEVEL_9_COST = altSettings.getProperty("ClanLevel9Cost", 40000);
		CLAN_LEVEL_10_COST = altSettings.getProperty("ClanLevel10Cost", 40000);
		CLAN_LEVEL_11_COST = altSettings.getProperty("ClanLevel11Cost", 75000);
		CLAN_LEVEL_6_REQUIREMEN = altSettings.getProperty("ClanLevel6Requirement", 30);
		CLAN_LEVEL_7_REQUIREMEN = altSettings.getProperty("ClanLevel7Requirement", 50);
		CLAN_LEVEL_8_REQUIREMEN = altSettings.getProperty("ClanLevel8Requirement", 80);
		CLAN_LEVEL_9_REQUIREMEN = altSettings.getProperty("ClanLevel9Requirement", 120);
		CLAN_LEVEL_10_REQUIREMEN = altSettings.getProperty("ClanLevel10Requirement", 140);
		CLAN_LEVEL_11_REQUIREMEN = altSettings.getProperty("ClanLevel11Requirement", 170);
		BLOOD_OATHS = altSettings.getProperty("BloodOaths", 150);
		BLOOD_PLEDGES = altSettings.getProperty("BloodPledges", 5);
		MIN_ACADEM_POINT = altSettings.getProperty("MinAcademPoint", 190);
		MAX_ACADEM_POINT = altSettings.getProperty("MaxAcademPoint", 650);
		
		VITAMIN_PETS_FOOD_ID = altSettings.getProperty("AltVitaminPetsFoodId", -1);
		
		HELLBOUND_LEVEL = altSettings.getProperty("HellboundLevel", 0);
		HELLBOUND_ENTER_NO_QUEST = altSettings.getProperty("EnterHellboundWithoutQuest", true);
		
		SIEGE_PVP_COUNT = altSettings.getProperty("SiegePvpCount", false);
		ZONE_PVP_COUNT = altSettings.getProperty("ZonePvpCount", false);
		EXPERTISE_PENALTY = altSettings.getProperty("ExpertisePenalty", true);
		ALT_DISPEL_MUSIC = altSettings.getProperty("AltDispelDanceSong", false);
		ALT_MUSIC_LIMIT = altSettings.getProperty("MusicLimit", 12);
		ALT_DEBUFF_LIMIT = altSettings.getProperty("DebuffLimit", 8);
		ALT_TRIGGER_LIMIT = altSettings.getProperty("TriggerLimit", 12);
		ENABLE_MODIFY_SKILL_DURATION = altSettings.getProperty("EnableSkillDuration", false);
		if (ENABLE_MODIFY_SKILL_DURATION)
		{
			String[] propertySplit = altSettings.getProperty("SkillDurationList", "").split(";");
			SKILL_DURATION_LIST = new HashMap<>(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
				{
					_log.warn("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
				}
				else
				{
					try
					{
						SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
						{
							_log.warn("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
					}
				}
			}
		}
		
		MIN_ADENA_TO_EAT = altSettings.getProperty("MinAdenaLakfiEat", 10000);
		TIME_IF_NOT_FEED = altSettings.getProperty("TimeIfNotFeedDissapear", 10);
		INTERVAL_EATING = altSettings.getProperty("IntervalBetweenEating", 15);
		CHANCE_GOLD_LAKFI = altSettings.getProperty("ChanceGoldLakfi", 15);
		
		ALLOW_WEDDING = altSettings.getProperty("AllowWedding", false);
		WEDDING_PRICE = altSettings.getProperty("WeddingPrice", 500000);
		WEDDING_PUNISH_INFIDELITY = altSettings.getProperty("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT_PRICE = altSettings.getProperty("WeddingTeleportPrice", 500000);
		WEDDING_TELEPORT_INTERVAL = altSettings.getProperty("WeddingTeleportInterval", 120);
		WEDDING_SAMESEX = altSettings.getProperty("WeddingAllowSameSex", true);
		WEDDING_FORMALWEAR = altSettings.getProperty("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = altSettings.getProperty("WeddingDivorceCosts", 20);
		
		DEEPBLUE_DROP_RULES = altSettings.getProperty("UseDeepBlueDropRules", true);
		DEEPBLUE_DROP_MAXDIFF = altSettings.getProperty("DeepBlueDropMaxDiff", 8);
		DEEPBLUE_DROP_RAID_MAXDIFF = altSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);
		
		SWIMING_SPEED = altSettings.getProperty("SwimingSpeedTemplate", 50);
		SAVE_PET_EFFECT = altSettings.getProperty("SavePetEffect", true);
		
		/* All item price 1 adena */
		SELL_ALL_ITEMS_FREE = altSettings.getProperty("SellAllItemsFree", false);
		/* Inventory slots limits */
		INVENTORY_MAXIMUM_NO_DWARF = altSettings.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = altSettings.getProperty("MaximumSlotsForDwarf", 100);
		QUEST_INVENTORY_MAXIMUM = altSettings.getProperty("MaximumSlotsForQuests", 100);
		
		MULTISELL_SIZE = altSettings.getProperty("MultisellPageSize", 10);
		
		/* Warehouse slots limits */
		WAREHOUSE_SLOTS_NO_DWARF = altSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = altSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = altSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
		FREIGHT_SLOTS = altSettings.getProperty("MaximumFreightSlots", 10);
		
		SAFE_ENCHANT_COMMON = altSettings.getProperty("SafeEnchantCommon", 3);
		SAFE_ENCHANT_FULL_BODY = altSettings.getProperty("SafeEnchantFullBody", 4);
		
		ENCHANT_ATTRIBUTE_STONE_CHANCE = altSettings.getProperty("EnchantAttributeChance", 50);
		ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = altSettings.getProperty("EnchantAttributeCrystalChance", 30);
		
		REGEN_SIT_WAIT = altSettings.getProperty("RegenSitWait", false);
		
		STARTING_ADENA = altSettings.getProperty("StartingAdena", 0);
		UNSTUCK_SKILL = altSettings.getProperty("UnstuckSkill", true);
		
		/* Amount of HP, MP, and CP is restored */
		RESPAWN_RESTORE_CP = altSettings.getProperty("RespawnRestoreCP", 0.) / 100;
		RESPAWN_RESTORE_HP = altSettings.getProperty("RespawnRestoreHP", 65.) / 100;
		RESPAWN_RESTORE_MP = altSettings.getProperty("RespawnRestoreMP", 0.) / 100;
		
		/* Maximum number of available slots for pvt stores */
		MAX_PVTSTORE_SLOTS_DWARF = altSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = altSettings.getProperty("MaxPvtStoreSlotsOther", 4);
		MAX_PVTCRAFT_SLOTS = altSettings.getProperty("MaxPvtManufactureSlots", 20);
		
		SENDSTATUS_TRADE_JUST_OFFLINE = altSettings.getProperty("SendStatusTradeJustOffline", false);
		SENDSTATUS_TRADE_MOD = altSettings.getProperty("SendStatusTradeMod", 1.);
		SHOW_OFFLINE_MODE_IN_ONLINE = altSettings.getProperty("ShowOfflineTradeInOnline", false);
		
		ANNOUNCE_MAMMON_SPAWN = altSettings.getProperty("AnnounceMammonSpawn", true);
		
		NORMAL_NAME_COLOUR = Integer.decode("0x" + altSettings.getProperty("NormalNameColour", "FFFFFF"));
		CLANLEADER_NAME_COLOUR = Integer.decode("0x" + altSettings.getProperty("ClanleaderNameColour", "FFFFFF"));
		
		GAME_POINT_ITEM_ID = altSettings.getProperty("GamePointItemId", -1);
		STARTING_LVL = altSettings.getProperty("StartingLvL", 0);
		MAX_PLAYER_CONTRIBUTION = altSettings.getProperty("MaxPlayerContribution", 1000000);
		
		ENCHANT_MAX_WEAPON = altSettings.getProperty("EnchantMaxWeapon", 20);
		ENCHANT_MAX_ARMOR = altSettings.getProperty("EnchantMaxArmor", 20);
		ENCHANT_MAX_JEWELRY = altSettings.getProperty("EnchantMaxJewelry", 20);
		ENCHANT_MAX_OLF_T_SHIRT = altSettings.getProperty("EnchantMaxOlfTShirt", 10);
		OLF_TSHIRT_CUSTOM_ENABLED = Boolean.parseBoolean(altSettings.getProperty("EnableOlfTShirtEnchant", "False"));
		
		FORUM_IN_WHOLE_COMMUNITY_BOARD = altSettings.getProperty("ForumInWholeCommunityBoard", false);
		FORUM_TAB = altSettings.getProperty("ForumTab", "_bbshome");
		FORUM_INFORMATION_MANAGEMENT = altSettings.getProperty("ForumInformationManagement", 60);
		FORUM_INCREASE_VIEWS_FOR_AUTHOR_VIEW = altSettings.getProperty("ForumIncreaseViewsForAuthorView", false);
		FORUM_AUTHOR_CAN_CLOSE_TOPIC = altSettings.getProperty("ForumAuthorCanCloseTopic", false);
		FORUM_TOPICS_LIMIT_IN_PAGE = altSettings.getProperty("ForumTopicsLimitInPage", 10);
		FORUM_MESSAGES_LIMIT_IN_PAGE = altSettings.getProperty("ForumMessagesLimitInPage", 10);
		
		ANCIENT_HERB_SPAWN_RADIUS = altSettings.getProperty("AncientHerbSpawnRadius", 600);
		ANCIENT_HERB_SPAWN_CHANCE = altSettings.getProperty("AncientHerbSpawnChance", 3);
		ANCIENT_HERB_SPAWN_COUNT = altSettings.getProperty("AncientHerbSpawnCount", 5);
		ANCIENT_HERB_RESPAWN_TIME = altSettings.getProperty("AncientHerbRespawnTime", 60) * 1000;
		ANCIENT_HERB_DESPAWN_TIME = altSettings.getProperty("AncientHerbDespawnTime", 60) * 1000;
		final String[] locs = altSettings.getProperty("AncientHerbSpawnPoints", "").split(";");
		if (locs != null)
		{
			for (final String string : locs)
			{
				if (string != null)
				{
					final String[] cords = string.split(",");
					final int x = Integer.parseInt(cords[0]);
					final int y = Integer.parseInt(cords[1]);
					final int z = Integer.parseInt(cords[2]);
					HEIN_FIELDS_LOCATIONS.add(new Location(x, y, z));
				}
			}
		}
	}
	
	private static void loadServicesSettings()
	{
		ExProperties servicesSettings = load(SERVICES_FILE);
		
		_coinID = servicesSettings.getProperty("Id_Item_Mall", 57);
		SERVICES_DELEVEL_ENABLED = servicesSettings.getProperty("AllowDelevel", false);
		SERVICES_DELEVEL_ITEM = servicesSettings.getProperty("DelevelItem", 57);
		SERVICES_DELEVEL_COUNT = servicesSettings.getProperty("DelevelCount", 1000);
		SERVICES_DELEVEL_MIN_LEVEL = servicesSettings.getProperty("DelevelMinLevel", 1);
		
		for (int id : servicesSettings.getProperty("AllowClassMasters", ArrayUtils.EMPTY_INT_ARRAY))
		{
			if (id != 0)
			{
				ALLOW_CLASS_MASTERS_LIST.add(id);
			}
		}
		
		CLASS_MASTERS_PRICE = servicesSettings.getProperty("ClassMastersPrice", "0,0,0");
		if (CLASS_MASTERS_PRICE.length() >= 5)
		{
			int level = 1;
			for (String id : CLASS_MASTERS_PRICE.split(","))
			{
				CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
				level++;
			}
		}
		CLASS_MASTER_NPC = servicesSettings.getProperty("ClassMasterNpc", false);
		SERVICES_RIDE_HIRE_ENABLED = servicesSettings.getProperty("RideHireEnabled", false);
		CLASS_MASTERS_PRICE_ITEM = servicesSettings.getProperty("ClassMastersPriceItem", 57);
		
		SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
		SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
		SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);
		
		SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesSettings.getProperty("BabyPetExchangeEnabled", false);
		SERVICES_EXCHANGE_BABY_PET_PRICE = servicesSettings.getProperty("BabyPetExchangePrice", 100);
		SERVICES_EXCHANGE_BABY_PET_ITEM = servicesSettings.getProperty("BabyPetExchangeItem", 4037);
		
		SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
		SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
		SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);
		
		SERVICES_CHANGE_BASE_ENABLED = servicesSettings.getProperty("BaseChangeEnabled", false);
		SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
		SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 4037);
		
		SERVICES_SEPARATE_SUB_ENABLED = servicesSettings.getProperty("SeparateSubEnabled", false);
		SERVICES_SEPARATE_SUB_PRICE = servicesSettings.getProperty("SeparateSubPrice", 100);
		SERVICES_SEPARATE_SUB_ITEM = servicesSettings.getProperty("SeparateSubItem", 4037);
		
		SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
		SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
		SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
		SERVICES_CHANGE_NICK_COLOR_LIST = servicesSettings.getProperty("NickColorChangeList", new String[]
		{
			"00FF00"
		});
		
		SERVICES_CHANGE_Title_COLOR_ENABLED = servicesSettings.getProperty("TitleColorChangeEnabled", false);
		SERVICES_CHANGE_Title_COLOR_PRICE = servicesSettings.getProperty("TitleColorChangePrice", 100);
		SERVICES_CHANGE_Title_COLOR_ITEM = servicesSettings.getProperty("TitleColorChangeItem", 4037);
		SERVICES_CHANGE_Title_COLOR_LIST = servicesSettings.getProperty("TitleColorChangeList", new String[]
		{
			"00FF00"
		});
		
		SERVICES_HERO_SELL_ENABLED = servicesSettings.getProperty("HeroSellEnabled", false);
		SERVICES_HERO_SELL_DAY = servicesSettings.getProperty("HeroSellDay", new int[]
		{
			30
		});
		SERVICES_HERO_SELL_PRICE = servicesSettings.getProperty("HeroSellPrice", new int[]
		{
			30
		});
		SERVICES_HERO_SELL_ITEM = servicesSettings.getProperty("HeroSellItem", new int[]
		{
			4037
		});
		
		SERVICES_WASH_PK_ENABLED = servicesSettings.getProperty("WashPkEnabled", false);
		SERVICES_WASH_PK_ITEM = servicesSettings.getProperty("WashPkItem", 4037);
		SERVICES_WASH_PK_PRICE = servicesSettings.getProperty("WashPkPrice", 5);
		
		SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
		SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
		SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
		SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);
		
		SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesSettings.getProperty("ExpandWarehouseEnabled", false);
		SERVICES_EXPAND_WAREHOUSE_PRICE = servicesSettings.getProperty("ExpandWarehousePrice", 1000);
		SERVICES_EXPAND_WAREHOUSE_ITEM = servicesSettings.getProperty("ExpandWarehouseItem", 4037);
		SERVICES_EXPAND_WAREHOUSE_MAX = servicesSettings.getProperty("ExpandWarehouseMax", 250);
		
		SERVICES_EXPAND_CWH_ENABLED = servicesSettings.getProperty("ExpandCWHEnabled", false);
		SERVICES_EXPAND_CWH_PRICE = servicesSettings.getProperty("ExpandCWHPrice", 1000);
		SERVICES_EXPAND_CWH_ITEM = servicesSettings.getProperty("ExpandCWHItem", 4037);
		
		SERVICES_SELLPETS = servicesSettings.getProperty("SellPets", "");
		
		SERVICES_OFFLINE_TRADE_ALLOW = servicesSettings.getProperty("AllowOfflineTrade", false);
		SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE = servicesSettings.getProperty("AllowOfflineTradeOnlyOffshore", false);
		SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesSettings.getProperty("OfflineMinLevel", 0);
		SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesSettings.getProperty("OfflineTradeNameColor", "B0FFFF"));
		SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesSettings.getProperty("OfflineTradePriceItem", 0);
		SERVICES_OFFLINE_TRADE_PRICE = servicesSettings.getProperty("OfflineTradePrice", 0);
		SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesSettings.getProperty("OfflineTradeDaysToKick", 14) * 86400L;
		SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesSettings.getProperty("OfflineRestoreAfterRestart", true);
		TRANSFORMATION_ID_MALE = servicesSettings.getProperty("TransformationIdMale", 20005);
		TRANSFORMATION_ID_FEMALE = servicesSettings.getProperty("TransformationIdFemale", 20006);
		TRANSFORM_ON_OFFLINE_TRADE = servicesSettings.getProperty("TransformOnOfflineTrade", false);
		LIMIT_OFFLINE_IN_TOWN = servicesSettings.getProperty("LImitShopsInTown", 0);
		ENABLE_BAZAR = servicesSettings.getProperty("EnableBazar", false);
		
		SERVICES_NO_TRADE_ONLY_OFFLINE = servicesSettings.getProperty("NoTradeOnlyOffline", false);
		SERVICES_TRADE_TAX = servicesSettings.getProperty("TradeTax", 0.0);
		SERVICES_OFFSHORE_TRADE_TAX = servicesSettings.getProperty("OffshoreTradeTax", 0.0);
		SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesSettings.getProperty("TradeTaxOnlyOffline", false);
		SERVICES_OFFSHORE_NO_CASTLE_TAX = servicesSettings.getProperty("NoCastleTaxInOffshore", false);
		SERVICES_TRADE_ONLY_FAR = servicesSettings.getProperty("TradeOnlyFar", false);
		SERVICES_TRADE_MIN_LEVEL = servicesSettings.getProperty("MinLevelForTrade", 0);
		SERVICES_TRADE_RADIUS = servicesSettings.getProperty("TradeRadius", 30);
		
		SERVICES_GIRAN_HARBOR_ENABLED = servicesSettings.getProperty("GiranHarborZone", false);
		SERVICES_PARNASSUS_ENABLED = servicesSettings.getProperty("ParnassusZone", false);
		SERVICES_PARNASSUS_NOTAX = servicesSettings.getProperty("ParnassusNoTax", false);
		SERVICES_PARNASSUS_PRICE = servicesSettings.getProperty("ParnassusPrice", 500000);
		
		SERVICES_ALLOW_LOTTERY = servicesSettings.getProperty("AllowLottery", false);
		SERVICES_LOTTERY_PRIZE = servicesSettings.getProperty("LotteryPrize", 50000);
		SERVICES_LOTTERY_TICKET_PRICE = servicesSettings.getProperty("LotteryTicketPrice", 2000);
		SERVICES_LOTTERY_5_NUMBER_RATE = servicesSettings.getProperty("Lottery5NumberRate", 1.1);
		SERVICES_LOTTERY_4_NUMBER_RATE = servicesSettings.getProperty("Lottery4NumberRate", 4.);
		SERVICES_LOTTERY_3_NUMBER_RATE = servicesSettings.getProperty("Lottery3NumberRate", 6.);
		SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesSettings.getProperty("Lottery2and1NumberPrize", 20.);
		
		SERVICES_LOTTERY_STARTING_DATE = servicesSettings.getProperty("LotteryDate", new int[]
		{
			1,
			19
		});
		
		SERVICES_ALLOW_ROULETTE = servicesSettings.getProperty("AllowRoulette", false);
		SERVICES_ROULETTE_MIN_BET = servicesSettings.getProperty("RouletteMinBet", 1L);
		SERVICES_ROULETTE_MAX_BET = servicesSettings.getProperty("RouletteMaxBet", Long.MAX_VALUE);
		
		SERVICES_ENABLE_NO_CARRIER = servicesSettings.getProperty("EnableNoCarrier", false);
		SERVICES_NO_CARRIER_MIN_TIME = servicesSettings.getProperty("NoCarrierMinTime", 0);
		SERVICES_NO_CARRIER_MAX_TIME = servicesSettings.getProperty("NoCarrierMaxTime", 90);
		SERVICES_NO_CARRIER_DEFAULT_TIME = servicesSettings.getProperty("NoCarrierDefaultTime", 60);
		
		SERVICES_PK_PVP_KILL_ENABLE = servicesSettings.getProperty("PkPvPKillEnable", false);
		SERVICES_PVP_KILL_REWARD_ITEM = servicesSettings.getProperty("PvPkillRewardItem", 4037);
		SERVICES_PVP_KILL_REWARD_COUNT = servicesSettings.getProperty("PvPKillRewardCount", 1L);
		SERVICES_PK_KILL_REWARD_ITEM = servicesSettings.getProperty("PkkillRewardItem", 4037);
		SERVICES_PK_KILL_REWARD_COUNT = servicesSettings.getProperty("PkKillRewardCount", 1L);
		SERVICES_PK_PVP_TIE_IF_SAME_IP = servicesSettings.getProperty("PkPvPTieifSameIP", true);
		
		ITEM_BROKER_ITEM_SEARCH = servicesSettings.getProperty("UseItemBrokerItemSearch", false);
		
		ALLOW_EVENT_GATEKEEPER = servicesSettings.getProperty("AllowEventGatekeeper", false);
		SERVICES_LVL_ENABLED = servicesSettings.getProperty("LevelChangeEnabled", false);
		SERVICES_LVL_UP_MAX = servicesSettings.getProperty("LevelUPChangeMax", 85);
		SERVICES_LVL_UP_ITEM = servicesSettings.getProperty("LevelUPChangeItem", 4037);
		
		SERVICES_LVL_79_85_PRICE = servicesSettings.getProperty("Levelfrom79to85", 1000);
		SERVICES_LVL_1_85_PRICE = servicesSettings.getProperty("Levelfrom1to85", 1500);
		
		SERVICES_CLAN_REP_POINTS = servicesSettings.getProperty("EnableClanRepService", false);
		SERVICE_CLAN_REP_ITEM = servicesSettings.getProperty("ClanRepItem", 13693);
		SERVICE_CLAN_REP_COST = servicesSettings.getProperty("ClanReptCost", 1000);
		SERVICE_CLAN_REP_ADD = servicesSettings.getProperty("AddReputation", 1);
		
		ALLOW_MULTILANG_GATEKEEPER = servicesSettings.getProperty("AllowMultiLangGatekeeper", false);
		ALLOW_UPDATE_ANNOUNCER = servicesSettings.getProperty("AllowUpdateAnnouncer", false);
		SERVICES_BUY_ALL_CLAN_PRICE_ONE_SKILL = servicesSettings.getProperty("PriceOneSkill", false);
		SERVICES_BUY_ALL_CLAN_SKILLS_ITEM_ID = servicesSettings.getProperty("BuyAllClanSkillsIemId", 13693);
		SERVICES_BUY_ALL_CLAN_SKILLS_PRICE = servicesSettings.getProperty("BuyAllClanSkillsPrice", 1);
		ALLOW_MAIL_OPTION = servicesSettings.getProperty("AllowMailOption", false);
	}
	
	private static void loadCommunityPvPboardsettings()
	{
		ExProperties CommunityPvPboardSettings = load(BOARD_MANAGER_CONFIG_FILE);
		
		ENABLE_NEW_CLAN_CB = CommunityPvPboardSettings.getProperty("EnableNewClanBoard", false);
		// ENABLE_OLD_CLAN_BOARD = CommunityPvPboardSettings.getProperty("EnableOLDClanBoard", true);
		
		BOARD_ENABLE_CLASS_MASTER = CommunityPvPboardSettings.getProperty("EnableBoardClassMaster", false);
		ENABLE_NEW_FRIENDS_BOARD = CommunityPvPboardSettings.getProperty("EnableNewFriendsBoard", false);
		ENABLE_RETAIL_FRIENDS_BOARD = CommunityPvPboardSettings.getProperty("EnableRetailFriendsBoard", false);
		ENABLE_MEMO_BOARD = CommunityPvPboardSettings.getProperty("EnableMemoBoard", false);
		ENABLE_NEW_MAIL_MANAGER = CommunityPvPboardSettings.getProperty("EnableBoardNewMailManager", false);
		ENABLE_BOARD_STATS = CommunityPvPboardSettings.getProperty("EnableBoardStats", false);
		ENABLE_OLD_MAIL_MANAGER = CommunityPvPboardSettings.getProperty("EnableBoardOldMailManager", false);
		
		COMMUNITYBOARD_ENABLED = CommunityPvPboardSettings.getProperty("AllowCommunityBoard", true);
		COMMUNITY_FAVORITES = CommunityPvPboardSettings.getProperty("CommunityFavorites", false);
		BBS_DEFAULT = CommunityPvPboardSettings.getProperty("BBSDefault", "_bbshome");
		BBS_HOME_DIR = CommunityPvPboardSettings.getProperty("BBSHomeDir", "scripts/services/community/");
		BBS_TITLE = CommunityPvPboardSettings.getProperty("BBSTitle", "Community Board");
		ALLOW_BBS_WAREHOUSE = CommunityPvPboardSettings.getProperty("AllowBBSWarehouse", true);
		BBS_WAREHOUSE_ALLOW_PK = CommunityPvPboardSettings.getProperty("BBSWarehouseAllowPK", false);
		
		BBS_PVP_SUB_MANAGER_ALLOW = CommunityPvPboardSettings.getProperty("AllowBBSSubManager", false);
		BBS_PVP_SUB_MANAGER_PIACE = CommunityPvPboardSettings.getProperty("AllowBBSSubManagerPiace", false);
		BBS_PVP_ALLOW_BUY = CommunityPvPboardSettings.getProperty("CommunityShopEnable", false);
		BBS_PVP_ALLOW_SELL = CommunityPvPboardSettings.getProperty("CommunitySellEnable", false);
		BBS_PVP_ALLOW_AUGMENT = CommunityPvPboardSettings.getProperty("CommunityAugmentEnable", false);
		BBS_PVP_TELEPORT_ENABLED = CommunityPvPboardSettings.getProperty("BBSPVPTeleportEnabled", false);
		BBS_PVP_TELEPORT_POINT_PRICE = CommunityPvPboardSettings.getProperty("BBSPVPTeleportPointPrice", 200000);
		BBS_PVP_TELEPORT_MAX_POINT_COUNT = CommunityPvPboardSettings.getProperty("BBSPVPTeleportMaxPointCount", 10);
		COMMUNITYBOARD_ENCHANT_ENABLED = CommunityPvPboardSettings.getProperty("AllowCBEnchant", false);
		ALLOW_BBS_ENCHANT_ELEMENTAR = CommunityPvPboardSettings.getProperty("AllowEnchantElementar", false);
		ALLOW_BBS_ENCHANT_ATT = CommunityPvPboardSettings.getProperty("AllowEnchantAtt", false);
		COMMUNITY_DONATE_PANEL_ITEMS = CommunityPvPboardSettings.getProperty("CommunityDonatePanelItems", 4356);
		COMMUNITYBOARD_ENCHANT_ITEM = CommunityPvPboardSettings.getProperty("CBEnchantItem", 4356);
		COMMUNITYBOARD_MAX_ENCHANT = CommunityPvPboardSettings.getProperty("CBMaxEnchant", 25);
		COMMUNITYBOARD_ENCHANT_LVL = CommunityPvPboardSettings.getProperty("CBEnchantLvl", new int[0]);
		COMMUNITYBOARD_ENCHANT_PRICE_WEAPON = CommunityPvPboardSettings.getProperty("CBEnchantPriceWeapon", new int[0]);
		COMMUNITYBOARD_ENCHANT_PRICE_ARMOR = CommunityPvPboardSettings.getProperty("CBEnchantPriceArmor", new int[0]);
		COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON = CommunityPvPboardSettings.getProperty("CBEnchantAtributeLvlWeapon", new int[0]);
		COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON = CommunityPvPboardSettings.getProperty("CBEnchantAtributePriceWeapon", new int[0]);
		COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR = CommunityPvPboardSettings.getProperty("CBEnchantAtributeLvlArmor", new int[0]);
		COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR = CommunityPvPboardSettings.getProperty("CBEnchantAtributePriceArmor", new int[0]);
		COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP = CommunityPvPboardSettings.getProperty("CBEnchantAtributePvP", false);
		ALLOW_CB_AUGMENTATION = CommunityPvPboardSettings.getProperty("EnableCommunityAugmentation", false);
		COMMUNITY_AUGMENTATION_MIN_LEVEL = CommunityPvPboardSettings.getProperty("MinLevelToAugment", 46);
		COMMUNITY_AUGMENTATION_ALLOW_JEWELRY = CommunityPvPboardSettings.getProperty("AllowJewelryyAugmentation", false);
		COMMUNITY_DROP_LIST = CommunityPvPboardSettings.getProperty("EnableCommunityDropList", false);
		COMMUNITY_ITEM_INFO = CommunityPvPboardSettings.getProperty("EnableCommunityItemInfo", false);
		
		ENABLE_COMMUNITY_ACADEMY = CommunityPvPboardSettings.getProperty("EnableAcademyBoard", false);
		SERVICES_ACADEMY_REWARD = CommunityPvPboardSettings.getProperty("AcademyRewards", "57");
		ACADEMY_MIN_ADENA_AMOUNT = CommunityPvPboardSettings.getProperty("MinAcademyPrice", 1);
		ACADEMY_MAX_ADENA_AMOUNT = CommunityPvPboardSettings.getProperty("MaxAcademyPrice", 1000000000);
		MAX_TIME_IN_ACADEMY = CommunityPvPboardSettings.getProperty("KickAcademyAfter", 259200000);
		ACADEMY_INVITE_DELAY = CommunityPvPboardSettings.getProperty("InviteDelay", 5);
		BOSSES_TO_NOT_SHOW = CommunityPvPboardSettings.getProperty("BossesToNotShow", new int[] {});
		ALLOW_SENDING_IMAGES = CommunityPvPboardSettings.getProperty("AllowSendingImages", true);
		
	}
	
	private static void loadPvPSettings()
	{
		ExProperties pvpSettings = load(PVP_CONFIG_FILE);
		
		RATE_KARMA_LOST = pvpSettings.getProperty("RateKarmaLost", 1.);
		ENABLE_PVP_PK_LOG = pvpSettings.getProperty("EnablePvPpkLog", false);
		
		/* KARMA SYSTEM */
		KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
		KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);
		
		KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
		DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", false);
		DROP_ITEMS_AUGMENTED = pvpSettings.getProperty("DropAugmented", false);
		
		KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
		MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 5);
		
		KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);
		
		KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20.);
		KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKsDropMod", 1.);
		NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1.);
		DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
		DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquippment", 17);
		DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);
		
		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
		for (int id : pvpSettings.getProperty("ListOfNonDroppableItems", new int[]
		{
			57,
			1147,
			425,
			1146,
			461,
			10,
			2368,
			7,
			6,
			2370,
			2369,
			3500,
			3501,
			3502,
			4422,
			4423,
			4424,
			2375,
			6648,
			6649,
			6650,
			6842,
			6834,
			6835,
			6836,
			6837,
			6838,
			6839,
			6840,
			5575,
			7694,
			6841,
			8181
		}))
		{
			KARMA_LIST_NONDROPPABLE_ITEMS.add(id);
		}
		
		PVP_TIME = pvpSettings.getProperty("PvPTime", 120000);
		
		// PVP Server system
		
		ATT_MOD_ARMOR = pvpSettings.getProperty("att_mod_Armor", 6);
		ATT_MOD_WEAPON = pvpSettings.getProperty("att_mod_Weapon", 5);
		ATT_MOD_WEAPON1 = pvpSettings.getProperty("att_mod_Weapon1", 20);
		
		ATT_MOD_MAX_ARMOR = pvpSettings.getProperty("att_mod_max_armor", 60);
		ATT_MOD_MAX_WEAPON = pvpSettings.getProperty("att_mod_max_weapon", 150);
		
		HENNA_STATS = pvpSettings.getProperty("HennaStats", 5);
		NEW_CHAR_IS_NOBLE = Boolean.parseBoolean(pvpSettings.getProperty("NewCharIsNoble", "false"));
		NEW_CHAR_IS_HERO = Boolean.valueOf(pvpSettings.getProperty("NewCharIsHero", "false"));
		ANNOUNCE_SPAWN_RB = Boolean.valueOf(pvpSettings.getProperty("AnnounceToSpawnRb", "false"));
		ANNOUNCE_SPAWN_RB_REGION = Boolean.valueOf(pvpSettings.getProperty("AnnounceSpawnRbForRegion", "false"));
		
		SPAWN_CHAR = Boolean.parseBoolean(pvpSettings.getProperty("CustomSpawn", "false"));
		SPAWN_X = Integer.parseInt(pvpSettings.getProperty("SpawnX", ""));
		SPAWN_Y = Integer.parseInt(pvpSettings.getProperty("SpawnY", ""));
		SPAWN_Z = Integer.parseInt(pvpSettings.getProperty("SpawnZ", ""));
		
		ADEPT_ENABLE = pvpSettings.getProperty("ADEPT_ENABLE", true);
		
		SPAWN_CITIES_TREE = pvpSettings.getProperty("SPAWN_CITIES_TREE", true);
		SPAWN_NPC_BUFFER = pvpSettings.getProperty("SPAWN_NPC_BUFFER", true);
		SPAWN_NPC_CLASS_MASTER = pvpSettings.getProperty("SPAWN_NPC_CLASS_MASTER", true);
		SPAWN_scrubwoman = pvpSettings.getProperty("SPAWN_scrubwoman", true);
		MAX_PARTY_SIZE = pvpSettings.getProperty("MaxPartySize", 9);
	}
	
	private static final String ZONE_DRAGONVALLEY_FILE = "config/zones/DragonValley.properties";
	
	public static double DWARRIOR_MS_CHANCE;
	public static double DHUNTER_MS_CHANCE;
	public static int BDRAKE_MS_CHANCE;
	public static int EDRAKE_MS_CHANCE;
	public static int BKARIK_D_M_CHANCE;
	public static int DRAGONKNIGHT_2ND_D_CHANCE;
	public static int NECROMANCER_MS_CHANCE;
	
	// TODO from UCDetector: Method "Config.loadDragonValleyZoneSettings()" has 0 references
	public static void loadDragonValleyZoneSettings() // NO_UCD (unused code)
	{
		final ExProperties properties = load(ZONE_DRAGONVALLEY_FILE);
		NECROMANCER_MS_CHANCE = properties.getProperty("NecromancerMSChance", 0);
		DWARRIOR_MS_CHANCE = properties.getProperty("DWarriorMSChance", 0);
		DHUNTER_MS_CHANCE = properties.getProperty("DHunterMSChance", 0);
		BDRAKE_MS_CHANCE = properties.getProperty("BDrakeMSChance", 0);
		EDRAKE_MS_CHANCE = properties.getProperty("EDrakeMSChance", 0);
		BKARIK_D_M_CHANCE = properties.getProperty("BKarikDMSChance", 15);
		DRAGONKNIGHT_2ND_D_CHANCE = properties.getProperty("DragonKnight2ndDChance", 0);
	}
	
	private static void loadAISettings()
	{
		ExProperties aiSettings = load(AI_CONFIG_FILE);
		
		RETAIL_SS = aiSettings.getProperty("Retail_SevenSigns", true);
		ALLOW_NPC_AIS = aiSettings.getProperty("AllowNpcAIs", true);
		AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
		AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
		AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
		BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
		ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);
		
		RND_WALK = aiSettings.getProperty("RndWalk", true);
		RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
		RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);
		
		AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
		NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);
		MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
		MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
		MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
		MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
		
		MIN_NPC_ANIMATION = aiSettings.getProperty("MinNPCAnimation", 5);
		MAX_NPC_ANIMATION = aiSettings.getProperty("MaxNPCAnimation", 90);
		SERVER_SIDE_NPC_NAME = aiSettings.getProperty("ServerSideNpcName", false);
		SERVER_SIDE_NPC_TITLE = aiSettings.getProperty("ServerSideNpcTitle", false);
		SERVER_SIDE_NPC_TITLE_ETC = aiSettings.getProperty("ServerSideNpcTitleEtc", false);
		DEINONYCHUS_EGG_DROP_CHANCE = aiSettings.getProperty("DeinonychusEggDropChance", 3);
		
		for (int id : aiSettings.getProperty("NpcDontSpawnList", ArrayUtils.EMPTY_INT_ARRAY))
		{
			if (id != 0)
			{
				NPC_DONTSPAWN_LIST.add(id);
			}
		}
		for (int id : aiSettings.getProperty("TestNpcDmg", ArrayUtils.EMPTY_INT_ARRAY))
		{
			if (id != 0)
			{
				TEST_NPC_DMG.add(id);
			}
		}
	}
	
	private static void loadGeodataSettings()
	{
		ExProperties geodataSettings = load(GEODATA_CONFIG_FILE);
		
		DAMAGE_FROM_FALLING = geodataSettings.getProperty("DamageFromFalling", true);
		
		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);
		
		GEOFILES_PATTERN = geodataSettings.getProperty("GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);
		try
		{
			Config.GEODATA_ROOT = new File(geodataSettings.getProperty("GeodataRoot", "./geodata/")).getCanonicalFile();
		}
		catch (IOException e)
		{
			Config._log.error("", e);
		}
		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		CLIENT_Z_SHIFT = geodataSettings.getProperty("ClientZShift", 16);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
		GEODATA_SKILL_CHECK_TASK_INTERVAL = geodataSettings.getProperty("GeodataSkillCheckTaskInterval", 200);
	}
	
	private static void loadGMSettings()
	{
		ExProperties gmSettings = load(GM_CONFIG_FILE);
		
		EVERYBODY_HAS_ADMIN_RIGHTS = gmSettings.getProperty("EverybodyHasAdminRights", false);
		GIVE_GM_SHOP_TO_ALL_PLAYERS = gmSettings.getProperty("GiveGmShopAccessToAllPlayers", false);
		INVENTORY_MAXIMUM_GM = gmSettings.getProperty("MaximumSlotsForGMPlayer", 250);
		KARMA_DROP_GM = gmSettings.getProperty("CanGMDropEquipment", false);
		
		GM_LOGIN_INVUL = gmSettings.getProperty("GMLoginInvul", false);
		GM_LOGIN_IMMORTAL = gmSettings.getProperty("GMLoginImmortal", false);
		GM_LOGIN_INVIS = gmSettings.getProperty("GMLoginInvis", false);
		GM_LOGIN_SILENCE = gmSettings.getProperty("GMLoginSilence", false);
		GM_LOGIN_TRADEOFF = gmSettings.getProperty("GMLoginTradeOff", false);
		HIDE_GM_STATUS = gmSettings.getProperty("HideGMStatus", false);
		SAVE_GM_EFFECTS = gmSettings.getProperty("SaveGMEffects", false);
		GM_PM_COMMANDS = gmSettings.getProperty("GmPmCommands", true);
	}
	
	private static void loadEventsSettings()
	{
		ExProperties eventSettings = load(EVENTS_CONFIG_FILE);
		
		ENABLE_DION_ARENA = eventSettings.getProperty("EnableDionArena", false);
		ENABLE_GIRAN_ARENA = eventSettings.getProperty("EnableGiranArena", false);
		EVENT_CofferOfShadowsPriceRate = eventSettings.getProperty("CofferOfShadowsPriceRate", 1.);
		EVENT_CofferOfShadowsRewardRate = eventSettings.getProperty("CofferOfShadowsRewardRate", 1.);
		
		ENABLE_GVG_EVENT = eventSettings.getProperty("EnableGVGEvent", false);
		
		EVENT_GvGDisableEffect = eventSettings.getProperty("GvGDisableEffect", false);
		
		EVENT_TFH_POLLEN_CHANCE = eventSettings.getProperty("TFH_POLLEN_CHANCE", 5.);
		
		EVENT_GLITTMEDAL_NORMAL_CHANCE = eventSettings.getProperty("MEDAL_CHANCE", 10.);
		EVENT_GLITTMEDAL_GLIT_CHANCE = eventSettings.getProperty("GLITTMEDAL_CHANCE", 0.1);
		
		EVENT_L2DAY_LETTER_CHANCE = eventSettings.getProperty("L2DAY_LETTER_CHANCE", 1.);
		L2_DAY_CUSTOM_DROP = eventSettings.getProperty("L2DAY_CUSTOM_LETTER_CHANCE", "3875,1").replaceAll(" ", "").split(";");
		EVENT_CHANGE_OF_HEART_CHANCE = eventSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5.);
		
		EVENT_APIL_FOOLS_DROP_CHANCE = eventSettings.getProperty("AprilFollsDropChance", 50.);
		
		EVENT_BOUNTY_HUNTERS_ENABLED = eventSettings.getProperty("BountyHuntersEnabled", true);
		
		EVENT_SAVING_SNOWMAN_LOTERY_PRICE = eventSettings.getProperty("SavingSnowmanLoteryPrice", 50000);
		EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = eventSettings.getProperty("SavingSnowmanRewarderChance", 2);
		
		EVENT_TRICK_OF_TRANS_CHANCE = eventSettings.getProperty("TRICK_OF_TRANS_CHANCE", 10.);
		
		EVENT_MARCH8_DROP_CHANCE = eventSettings.getProperty("March8DropChance", 10.);
		EVENT_MARCH8_PRICE_RATE = eventSettings.getProperty("March8PriceRate", 1.);
		
		ENCHANT_MAX_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantMaxWeapon", 28);
		
//
//		AllowCustomDropItems = eventSettings.getProperty("AllowCustomDropItems", true);
//		CDItemsAllowMinMaxPlayerLvl = eventSettings.getProperty("CDItemsAllowMinMaxPlayerLvl", false);
//		CDItemsAllowMinMaxMobLvl = eventSettings.getProperty("CDItemsAllowMinMaxMobLvl", false);
//		CDItemsAllowOnlyRbDrops = eventSettings.getProperty("CDItemsAllowOnlyRbDrops", false);
//		CDItemsId = eventSettings.getProperty("CDItemsId", new int[] { 57 });
//		CDItemsCountDropMin = eventSettings.getProperty("CDItemsCountDropMin", new int[] { 1 });
//		CDItemsCountDropMax = eventSettings.getProperty("CDItemsCountDropMax", new int[] { 1 });
//		CustomDropItemsChance = eventSettings.getProperty("CustomDropItemsChance", new double[] { 1. });
//		CDItemsMinPlayerLvl = eventSettings.getProperty("CDItemsMinPlayerLvl", 20);
//		CDItemsMaxPlayerLvl = eventSettings.getProperty("CDItemsMaxPlayerLvl", 85);
//		CDItemsMinMobLvl = eventSettings.getProperty("CDItemsMinMobLvl", 20);
//		CDItemsMaxMobLvl = eventSettings.getProperty("CDItemsMaxMobLvl", 80);
//
//	
		ACTIVITY_REWARD_ENABLED = eventSettings.getProperty("ActivityRewardEnabled", false);
		ACTIVITY_REWARD_TIME = eventSettings.getProperty("ActivityRewardTime", 21600);
		ACTIVITY_REWARD_ITEMS = eventSettings.getProperty("ActivityRewardItems", "57,1,2,100").replaceAll(" ", "").split(";");
		
		ALLOW_EVENTS_CLUB = eventSettings.getProperty("AllowEventsClub", true);
		EVENTS_CLUB_HWID_CHECK = eventSettings.getProperty("EventsClubHwidCheck", true);
		EVENTS_CLUB_DISALLOW_EVENT = eventSettings.getProperty("EventsClubNotAllowedEvent", -1);
		EVENTS_CLUB_EQUALIZE_ROOMS = eventSettings.getProperty("EventsClubEqualizeRooms", false);
	}
	
	private static void loadOlympiadSettings()
	{
		ExProperties olympSettings = load(OLYMPIAD);
		
		ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
		
		ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);
		ALT_OLY_START_TIME = olympSettings.getProperty("AltOlyStartTime", 18);
		ALT_OLY_MIN = olympSettings.getProperty("AltOlyMin", 0);
		ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 21600000);
		ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000);
//		ALT_OLY_DATE_END = olympSettings.getProperty("AltOlyDateEnd", new int[]
//		{
//			1
//		});
		ALT_OLYMP_PERIOD = olympSettings.getProperty("AltTwoWeeksOlyPeriod", false);
		for (String prop : olympSettings.getProperty("AltOlyDateEndMonthly", "1").split(","))
		{
			ALT_OLY_DATE_END_MONTHLY.add(Integer.parseInt(prop));
		}
		ALT_OLY_DATE_END_WEEKLY = olympSettings.getProperty("AltOlyDateEndWeekly", 0);
		ALT_OLY_WAIT_TIME = olympSettings.getProperty("AltOlyWaitTime", 120);
		OLY_SHOW_OPPONENT_PERSONALITY = olympSettings.getProperty("OlympiadShowOpponentPersonality", false);
		OLYMPIAD_SHOUT_ONCE_PER_START = olympSettings.getProperty("OlyManagerShoutJustOneMessage", false);

		CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 5);
		NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 9);
		TEAM_GAME_MIN = olympSettings.getProperty("TeamGameMin", 4);
		
		GAME_MAX_LIMIT = olympSettings.getProperty("GameMaxLimit", 70);
		GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClassesCountLimit", 30);
		GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClassesCountLimit", 60);
		GAME_TEAM_COUNT_LIMIT = olympSettings.getProperty("GameTeamCountLimit", 10);
		
		ALT_OLY_BATTLE_REWARD_ITEM = olympSettings.getProperty("AltOlyBattleRewItem", 13722);
		ALT_OLY_CLASSED_RITEM_C = olympSettings.getProperty("AltOlyClassedRewItemCount", 50);
		ALT_OLY_NONCLASSED_RITEM_C = olympSettings.getProperty("AltOlyNonClassedRewItemCount", 40);
		ALT_OLY_TEAM_RITEM_C = olympSettings.getProperty("AltOlyTeamRewItemCount", 50);
		ALT_OLY_COMP_RITEM = olympSettings.getProperty("AltOlyCompRewItem", 13722);
		ALT_OLY_GP_PER_POINT = olympSettings.getProperty("AltOlyGPPerPoint", 1000);
		ALT_OLY_HERO_POINTS = olympSettings.getProperty("AltOlyHeroPoints", 180);
		ALT_OLY_RANK1_POINTS = olympSettings.getProperty("AltOlyRank1Points", 120);
		ALT_OLY_RANK2_POINTS = olympSettings.getProperty("AltOlyRank2Points", 80);
		ALT_OLY_RANK3_POINTS = olympSettings.getProperty("AltOlyRank3Points", 55);
		ALT_OLY_RANK4_POINTS = olympSettings.getProperty("AltOlyRank4Points", 35);
		ALT_OLY_RANK5_POINTS = olympSettings.getProperty("AltOlyRank5Points", 20);
		OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("OlympiadStadiasCount", 160);
		OLYMPIAD_BEGIN_TIME = olympSettings.getProperty("OlympiadBeginTime", 120);
		OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("OlympiadBattlesForReward", 15);
		OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 18);
		OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 3);
		OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OlympiadOldStyleStat", false);
		OLYMPIAD_PLAYER_IP = olympSettings.getProperty("OlympiadPlayerIp", false);
		OLYMPIAD_PLAYER_HWID = olympSettings.getProperty("OlympiadPlayerHWID", false);
		
		OLY_ENCH_LIMIT_ENABLE = olympSettings.getProperty("OlyEnchantLimit", false);
		OLY_ENCHANT_LIMIT_WEAPON = olympSettings.getProperty("OlyEnchantLimitWeapon", 0);
		OLY_ENCHANT_LIMIT_ARMOR = olympSettings.getProperty("OlyEnchantLimitArmor", 0);
		OLY_ENCHANT_LIMIT_JEWEL = olympSettings.getProperty("OlyEnchantLimitJewel", 0);
		
		OLY_HIDE_PLAYER_IDENTITY = olympSettings.getProperty("OlyHidePlayerIdentity", false);
		OLY_SHOW_OPPONENT_INFO = olympSettings.getProperty("OlyShowOpponentInfo", false);
		OLY_SHOW_OPPONENT_INFO_WINS_LOSES = olympSettings.getProperty("OlyShowOpponentInfoWinsLoses", false);
		OLY_ASK_PLAYERS_TO_SKIP_COUNTDOWN = olympSettings.getProperty("OlyAskPlayersToSkipCountdown", false);
	}
	
	public static int SERVER_RANKING_REWARD_ITEM_ID;
	public static int[] SERVER_RANKING_REWARD_ITEM_COUNT;
	
	private static void LoadCustom_Config()
	{
		ExProperties custom_Config = load(CUSTOM_CONFIG_FILE);
		
		SERVER_RANKING_REWARD_ITEM_ID = custom_Config.getProperty("ServerRankingRewardItemId", 57);
		SERVER_RANKING_REWARD_ITEM_COUNT = custom_Config.getProperty("ServerRankingRewardItemCount", new int[]
		{
			10,
			3,
			1
		});
		ALLOW_PFLAG = custom_Config.getProperty("AllowPflagVoicedCommand", false);
		ALLOW_CFLAG = custom_Config.getProperty("AllowCflagVoicedCommand", false);
		
		ITEM_COST_1_ADENA = custom_Config.getProperty("ItemsCost1Adena", new int[0]);
		
		ENABLE_CHARACTER_INTRO = custom_Config.getProperty("EnableCharacterIntro", false);
		FORBIDDEN_CHAR_NAMES = custom_Config.getProperty("ForbiddenCharNames", "").split(",");
		ENABLE_ACHIEVEMENTS = custom_Config.getProperty("EnableAchievements", false);
		ENABLE_PLAYER_COUNTERS = custom_Config.getProperty("EnablePlayerCounters", true);
		DISABLE_ACHIEVEMENTS_FAME_REWARD = custom_Config.getProperty("DisableFameRewards", false);
		ENABLE_CUSTOM_AUCTION = custom_Config.getProperty("EnableCustomAuction", false);
		AUCTION_DUMMY_CHARACTER = custom_Config.getProperty("AuctionDummyCharacter", 0);
		ENABLE_COMMUNITY_RANKING = custom_Config.getProperty("EnableCommunityRanking", false);
		ENABLE_EMAIL_VALIDATION = custom_Config.getProperty("EnableEmailValidation", false);
		CHARACTER_NAME_COLORIZATION = custom_Config.getProperty("CustomCharacterColorization", false);
		CHARACTER_TITLE_REPRESENTING_RELATION = custom_Config.getProperty("CharacterTitleRepresentingRelation", false);
		AUTO_POTIONS = custom_Config.getProperty("AutoPotions", false);
		AUTO_POTIONS_IN_PVP = custom_Config.getProperty("AutoPotionsInPvp", true);
		ALLOW_WYVERN_DURING_SIEGE = custom_Config.getProperty("AllowRideWyvernDuringSiege", false);
		PUNISHMENT_FOR_WYVERN_INSIDE_SIEGE = custom_Config.getProperty("PunishmentForWyvern", "");
		PINISHMENT_TIME_FOR_WYVERN = custom_Config.getProperty("PunishmentTimeForWyvern", 120);
		
		ALLOW_SPAWN_CUSTOM_HALL_NPC = custom_Config.getProperty("SpawnCustomHallNPC", true);
		
		CUSTOM_SKILLS_LOAD = custom_Config.getProperty("CustomSkillsLoad", false);
		
		DISABLE_TUTORIAL = custom_Config.getProperty("DisableTutorialQuestOnStart", true);
		ALLOW_MAMMON_FOR_ALL = custom_Config.getProperty("EnableMammonsForAll", true);
		ALLOW_FARM_IN_SEVENSIGN_IF_NOT_REGGED = custom_Config.getProperty("AllowPlayersToFarmIntoSevenSignIfNotRegged", true);
		SEVEN_SIGN_DISABLE_BUFF_DEBUFF = custom_Config.getProperty("DisableDebuffBuffFromSevenSign", false);
		SEVEN_SIGN_NON_STOP_ALL_SPAWN = custom_Config.getProperty("AllowToSetCustomSevenSignMode", false);
		SEVEN_SIGN_SET_PERIOD = custom_Config.getProperty("CustomModeForSevenSign", 0);
		PARTY_SEARCH_COMMANDS = custom_Config.getProperty("AllowPartyFindCommand", true);
		
		ALLOW_BOARD_NEWS_LEECH = custom_Config.getProperty("ForumBoardLeech", false);
		
		ENABLE_POLL_SYSTEM = custom_Config.getProperty("ActivatePollSystem", false);
		
		ALT_SELL_FROM_EVERYWHERE = custom_Config.getProperty("SellItemsEverywhere", false);
		
		// Captcha system
		ENABLE_CAPTCHA = custom_Config.getProperty("EnableCaptchaSystem", false);
		CAPTCHA_UNEQUIP = custom_Config.getProperty("CaptchaUnequipWeapon", false);
		CAPTCHA_MIN_MONSTERS = custom_Config.getProperty("CaptchaMinMonstertokill", 1000);
		CAPTCHA_MAX_MONSTERS = custom_Config.getProperty("CaptchaMaxMonstertokill", 2000);
		CAPTCHA_ATTEMPTS = custom_Config.getProperty("CaptchaAttempts", 3);
		CAPTCHA_SAME_LOCATION_DELAY = custom_Config.getProperty("CaptchaSameLocationDelay", 60);
		CAPTCHA_SAME_LOCATION_MIN_KILLS = custom_Config.getProperty("CaptchaSameLocationMinKills", 5);
		CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE = custom_Config.getProperty("CaptchaPunishmentType", "BANCHAR");
		CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME = custom_Config.getProperty("CaptchaPunishmentTime", -1);
		
		// Clan promotion
		SERVICES_CLAN_PROMOTION_ENABLE = custom_Config.getProperty("EnableClanPromotion", false);
		SERVICES_CLAN_PROMOTION_MAX_LEVEL = custom_Config.getProperty("MaxClanLevel", 6);
		SERVICES_CLAN_PROMOTION_MIN_ONLINE = custom_Config.getProperty("MinOnlineMembers", 10);
		SERVICES_CLAN_PROMOTION_ITEM = custom_Config.getProperty("ClanPromotionItemId", 57);
		SERVICES_CLAN_PROMOTION_ITEM_COUNT = custom_Config.getProperty("ClanPromotionItemCOunt", 1000);
		SERVICES_CLAN_PROMOTION_SET_LEVEL = custom_Config.getProperty("ClanPromotionSetLevel", 5);
		SERVICES_CLAN_PROMOTION_ADD_REP = custom_Config.getProperty("ClanPromotionAddrep", 0);
		SERVICE_CLAN_PRMOTION_ADD_EGGS = custom_Config.getProperty("GiveEggsToNewClans", false);
		CLAN_PROMOTION_CLAN_EGGS = custom_Config.getProperty("ClanEggsToReward", "").replaceAll(" ", "").split(";");
		
		CANCEL_SYSTEM_RESTORE_DELAY = custom_Config.getProperty("CancelSystemRestoreDelay", 120);
		CANCEL_SYSTEM_KEEP_TICKING = custom_Config.getProperty("CancelSystemKeepTicking", false);
		DEADLOCKCHECK_INTERVAL = custom_Config.getProperty("DeadLockCheckerInterval", -1);
		
		AUTO_SHOTS_ON_LOGIN = custom_Config.getProperty("AutoShotsOnLogin", false);
		
		ENABLE_EMOTIONS = custom_Config.getProperty("EnableEmotions", false);
		
		ENABLE_PLAYER_KILL_SYSTEM = custom_Config.getProperty("EnableCustomPlayerKillSystem", false);
		PLAYER_KILL_SPAWN_UNIQUE_CHEST = custom_Config.getProperty("SpawnChestsOnKill", false);
		PLAYER_KILL_INCREASE_ATTRIBUTE = custom_Config.getProperty("AllowRandomAttribute", false);
		PLAYER_KILL_GIVE_ENCHANTS = custom_Config.getProperty("GiveEnchantsonKill", false);
		PLAYER_KILL_GIVE_LIFE_STONE = custom_Config.getProperty("GiveLifeStoneonKill", false);
		PLAYER_KILL_GIVE_MANTRAS = custom_Config.getProperty("GiveMantrasonKill", false);
		PLAYER_KILL_AQUIRE_FAME = custom_Config.getProperty("IncreaseFameonKill", false);
		PLAYER_KILL_ALLOW_CUSTOM_PVP_ZONES = custom_Config.getProperty("AllowCustomZones", false);
		
		CUSTOM_CLASS_TRANSFER_SKILLS = custom_Config.getProperty("AllowCustomTransferSkills", false);
		
		ENABLE_REFERRAL_SYSTEM = custom_Config.getProperty("EnableReferralSystem", false);
		
		ALLOW_MACROS_REUSE_BUG = custom_Config.getProperty("AllowMacrosReuseBug", false);
		ALLOW_MACROS_ENCHANT_BUG = custom_Config.getProperty("AllowMacrosEnchantBug", false);
		
		PREMIUM_ACCOUNT_FOR_PARTY = custom_Config.getProperty("PremiumDistributeDropToAllParty", false);
		
		ALLOW_PLAYER_CHANGE_LANGUAGE = custom_Config.getProperty("AllowPlayerToChangeLang", false);
		
		SHOW_BAN_INFO_IN_CHARACTER_SELECT = custom_Config.getProperty("ShowBanInfoOnCharSelect", false);
		LEVEL_REQUIRED_TO_SEND_MAIL = custom_Config.getProperty("MinLevelToSendMails", 0);
		
		ENABLE_TRADELIST_VOICE = custom_Config.getProperty("EnableTradelistVoice", false);
		ENABLE_LUCKY_PIGS = custom_Config.getProperty("EnableLuckyPigs", false);
		
		CHECK_PRIVATE_SHOPS = custom_Config.getProperty("CheckPrivateStoreShops", false);
		
		ENABLE_HELLBOUND_COMMAND = custom_Config.getProperty("EnableHellbound", true);
		ENABLE_CFG_COMMAND = custom_Config.getProperty("EnableCfg", true);
		ENABLE_CLAN_COMMAND = custom_Config.getProperty("EnableClan", true);
		ENABLE_OFFLINE_COMMAND = custom_Config.getProperty("EnableOffline", true);
		ENABLE_REPAIR_COMMAND = custom_Config.getProperty("EnableRepair", true);
		ENABLE_WEDDING_COMMAND = custom_Config.getProperty("EnableWedding", true);
		ENABLE_DEBUG_COMMAND = custom_Config.getProperty("EnableDebug", true);
		ENABLE_RANDOM_COMMANDS = custom_Config.getProperty("EnableRandomCommands", true);
		ENABLE_CASTLEINFO_COMMAND = custom_Config.getProperty("EnableCastleInfo", true);
		
		NOT_USE_USER_VOICED = custom_Config.getProperty("NotUsePlayerVoiced", false);
		ALLOW_TOTAL_ONLINE = custom_Config.getProperty("AllowVoiceCommandOnline", false);
		
		COMMAND_DRESSME_ENABLE = custom_Config.getProperty("DressMe", true);
		
		COMMAND_FACEBOOK_ENABLE = custom_Config.getProperty("FacebookCommand", true);

		PREMIUM_ACCOUNT_TYPE = custom_Config.getProperty("RateBonusType", 0);
		PREMIUM_ACCOUNT_PARTY_GIFT_ID = custom_Config.getProperty("PartyGift", 1);
		ENTER_WORLD_SHOW_HTML_PREMIUM_BUY = custom_Config.getProperty("PremiumHTML", false);
		ENTER_WORLD_SHOW_HTML_PREMIUM_DONE = custom_Config.getProperty("PremiumDone", false);
		ENTER_WORLD_SHOW_HTML_PREMIUM_ACTIVE = custom_Config.getProperty("PremiumInfo", false);
		ALLOW_PREMIUM_CHANGE = custom_Config.getProperty("AllowPremiumChange", false);
		SERVICES_RATE_TYPE = custom_Config.getProperty("RateBonusType", Bonus.NO_BONUS);
		SERVICES_RATE_CREATE_PA = custom_Config.getProperty("RateBonusCreateChar", 0);
		SERVICES_RATE_BONUS_PRICE = custom_Config.getProperty("RateBonusPrice", new int[]
		{
			1500
		});
		SERVICES_RATE_BONUS_ITEM = custom_Config.getProperty("RateBonusItem", new int[]
		{
			4037
		});
		SERVICES_RATE_BONUS_VALUE = custom_Config.getProperty("RateBonusValue", new double[]
		{
			2.
		});
		SERVICES_RATE_BONUS_DAYS = custom_Config.getProperty("RateBonusTime", new int[]
		{
			30
		});
		
		ALT_NEW_CHAR_PREMIUM_ID = custom_Config.getProperty("AltNewCharPremiumId", 0);
		NONOWNER_ITEM_PICKUP_DELAY_RAIDS = custom_Config.getProperty("NonOwnerItemPickupDelayRaids", 285L) * 1000L;
		DEV_UNDERGROUND_COLISEUM = custom_Config.getProperty("DebugUndergroundColiseum", false);
		UNDERGROUND_COLISEUM_MEMBER_COUNT = custom_Config.getProperty("UndergroundColiseumMemberCount", 7);
		ENABLE_PLAYER_ITEM_LOGS = custom_Config.getProperty("EnablePlayerItemLogs", false);
		PLAYER_ITEM_LOGS_SAVED_IN_DB = custom_Config.getProperty("PlayerItemLogsSavedInDB", false);
		PLAYER_ITEM_LOGS_MAX_TIME = custom_Config.getProperty("PlayerItemLogsMaxTime", 172800000L);
		ALLOW_HWID_ENGINE = custom_Config.getProperty("AllowHWIDEngine", true);
		SERVER_NAME = custom_Config.getProperty("ServerName", "Server");
		MAIL_USER = custom_Config.getProperty("MailUser", "");
		MAIL_PASS = custom_Config.getProperty("MailPass", "");
		SERVICES_CHANGE_PASSWORD = custom_Config.getProperty("ChangePassword", false);
		PASSWORD_PAY_ID = custom_Config.getProperty("ChangePasswordPayId", 0);
		PASSWORD_PAY_COUNT = custom_Config.getProperty("ChangePassowrdPayCount", 0);
		AUTO_LOOT_PA = custom_Config.getProperty("AutoLootPA", false);
		AUTO_SOUL_CRYSTAL_QUEST = custom_Config.getProperty("AutoSoulCrystalQuest", true);
		SERVICES_BONUS_XP = custom_Config.getProperty("RateBonusXp", 1.);
		SERVICES_BONUS_SP = custom_Config.getProperty("RateBonusSp", 1.);
		SERVICES_BONUS_ADENA = custom_Config.getProperty("RateBonusAdena", 1.);
		SERVICES_BONUS_ITEMS = custom_Config.getProperty("RateBonusItems", 1.);
		SERVICES_BONUS_SPOIL = custom_Config.getProperty("RateBonusSpoil", 1.);
		ALLOW_TALK_TO_NPCS = custom_Config.getProperty("AllowTalkToNpcs", true);
		ENABLE_DAM_ON_SCREEN = custom_Config.getProperty("EnableDamageOnScreen", false);
		DAM_ON_SCREEN_FONT = custom_Config.getProperty("DamageOnScreenFontId", 3);
		DAM_ON_SCREEN_FONT_COLOR_ATTACKER = custom_Config.getProperty("OnScreenDamageGiven", 14500915);
		DAM_ON_SCREEN_FONT_COLOR_TARGET = custom_Config.getProperty("OnScreenDamageReceived", 16711680);
		ENABLE_SECONDARY_PASSWORD = custom_Config.getProperty("EnableSecondaryPassword", true);
		ENABLE_SPECIAL_TUTORIAL = custom_Config.getProperty("EnableSpecialTutorial", false);
		ALT_TELEPORTS_ONLY_FOR_GIRAN = custom_Config.getProperty("AllScrollsSoEToGiran", false);
		SERVICES_ANNOUNCE_PK_ENABLED = custom_Config.getProperty("AnnouncePK", false);
		SERVICES_ANNOUNCE_PVP_ENABLED = custom_Config.getProperty("AnnouncePvP", false);
		ENABLE_DAILY_QUESTS = custom_Config.getProperty("EnableDailyQuests", false);
		KILLING_SPREE_ENABLED = custom_Config.getProperty("KillingSpreeEnabled", false);
		
		// vote system test
		
		POP_UP_VOTE_MENU = custom_Config.getProperty("PopUpVoteMenu", false);
		GIVE_REWARD_FOR_VOTERS = custom_Config.getProperty("GiveRewardForVoters", false);
		VOTE_REWARD_ITEM_ID = custom_Config.getProperty("VoteRewardItemId", 6673);
		VOTE_REWARD_ITEM_COUNT_MIN = custom_Config.getProperty("VoteRewardItemCountMin", 1);
		VOTE_REWARD_ITEM_COUNT_MAX = custom_Config.getProperty("VoteRewardItemCountMax", 1);
		VOTE_REWARD_CHANCE = custom_Config.getProperty("VoteRewardChance", 100);
		VOTE_NETWORK_NAME = custom_Config.getProperty("VoteNetworkName", "");
		VOTE_TOPZONE_APIKEY = custom_Config.getProperty("VoteTopzoneApiKey", "");
		VOTE_HOPZONE_APIKEY = custom_Config.getProperty("VoteHopzoneApiKey", "");
		VOTE_L2JBRASIL_NAME = custom_Config.getProperty("VoteL2jBrasilName", "");
		
		ALLOW_FIGHT_CLUB = custom_Config.getProperty("AllowFightClub", true);
		FIGHT_CLUB_HWID_CHECK = custom_Config.getProperty("FightClubHwidCheck", true);
		FIGHT_CLUB_DISALLOW_EVENT = custom_Config.getProperty("FightClubNotAllowedEvent", -1);
		FIGHT_CLUB_EQUALIZE_ROOMS = custom_Config.getProperty("FightClubEqualizeRooms", false);
		
		KILLING_SPREE_ANNOUNCEMENTS = new HashMap<>();
		String[] split = custom_Config.getProperty("KillingSpreeAnnouncements", "").split(";");
		if (!split[0].isEmpty())
		{
			for (String ps : split)
			{
				final String[] pvp = ps.split(",");
				if (pvp.length != 2)
				{
					_log.error("[KillingSpreeAnnouncements]: invalid config property -> KillingSpree \"" + ps + "\"");
				}
				else
				{
					try
					{
						KILLING_SPREE_ANNOUNCEMENTS.put(Integer.parseInt(pvp[0]), pvp[1]);
					}
					catch (final NumberFormatException nfe)
					{
						nfe.printStackTrace();
						if (!ps.equals(""))
						{
							_log.error("[KillingSpreeAnnouncements]: invalid config property -> KillingSpree \"" + Integer.parseInt(pvp[0]) + "\"" + pvp[1]);
						}
					}
				}
			}
		}
		KILLING_SPREE_COLORS = new HashMap<>();
		split = custom_Config.getProperty("KillingSpreeColors", "").split(";");
		if (!split[0].isEmpty())
		{
			for (String ps : split)
			{
				final String[] pvp = ps.split(",");
				if (pvp.length != 2)
				{
					_log.error("[KillingSpreeColors]: invalid config property -> KillingSpree \"" + ps + "\"");
				}
				else
				{
					try
					{
						KILLING_SPREE_COLORS.put(Integer.parseInt(pvp[0]), pvp[1]);
					}
					catch (final NumberFormatException nfe)
					{
						nfe.printStackTrace();
						if (!ps.equals(""))
						{
							_log.error("[KillingSpreeColors]: invalid config property -> KillingSpree \"" + Integer.parseInt(pvp[0]) + "\"" + pvp[1]);
						}
					}
				}
			}
		}
	}
	
	public static boolean POP_UP_VOTE_MENU;
	public static boolean GIVE_REWARD_FOR_VOTERS;
	public static int VOTE_REWARD_ITEM_ID;
	public static int VOTE_REWARD_ITEM_COUNT_MIN;
	public static int VOTE_REWARD_ITEM_COUNT_MAX;
	public static int VOTE_REWARD_CHANCE;
	public static String VOTE_NETWORK_NAME;
	public static String VOTE_TOPZONE_APIKEY;
	public static String VOTE_HOPZONE_APIKEY;
	public static String VOTE_L2JBRASIL_NAME;
	
//	private static void LoadCustomSecurity_Config()
//	{
//		ExProperties custom_security = load(CUSTOM_SECURITY_FILE);
//		
//		SECURITY_ENABLED = custom_security.getProperty("EnableSecurity", false);
//		SECURITY_HERO_HEROVOICE = custom_security.getProperty("EnableSecurityHeroVoice", false);
//		SECURITY_ON_STARTUP_WHEN_SECURED = custom_security.getProperty("EnableOnStartupWhenSecured", true);
//		SECURITY_CFG_ENABLED = custom_security.getProperty("EnableSecurityCfg", false);
//		SECURITY_CANT_PVP_ENABLED = custom_security.getProperty("EnableSecurityCantPvP", false);
//		SECURITY_FORCE = custom_security.getProperty("EnableSecurityForced", false);
//		SECURITY_TRADE_ENABLED = custom_security.getProperty("EnableSecurityTrade", false);
//		SECURITY_ENCHANT_SKILL_ENABLED = custom_security.getProperty("EnableSecurityEnchantSkills", false);
//		SECURITY_ENCHANT_ITEM_ENABLED = custom_security.getProperty("EnableSecurityItemEnchant", false);
//		SECURITY_ENCHANT_ITEM_REMOVE_ENABLED = custom_security.getProperty("EnableSecurityItemEnchantRemove", false);
//		SECURITY_ITEM_AUGMENT = custom_security.getProperty("EnableSecurityItemAugment", false);
//		SECURITY_ITEM_UNEQUIP = custom_security.getProperty("EnableSecurityUnEquipItem", false);
//		SECURITY_ITEM_ATTRIBUTE_REMOVE_ENABLED = custom_security.getProperty("EnableSecurityItemAttributeRemove", false);
//		SECURITY_ITEM_DESTROY_ENABLED = custom_security.getProperty("EnableSecurityItemDestroy", false);
//		SECURITY_ITEM_GIVE_TO_PET_ENABLED = custom_security.getProperty("EnableSecurityItemGiveToPet", false);
//		SECURITY_ITEM_REMOVE_AUGUMENT_ENABLED = custom_security.getProperty("EnableSecurityItemRemoveAugument", false);
//		SECURITY_ITEM_CRYSTALIZE_ENABLED = custom_security.getProperty("EnableSecurityItemCrystalize", false);
//		SECURITY_SENDING_MAIL_ENABLED = custom_security.getProperty("EnableSecuritySendMail", false);
//		SECURITY_DELETE_RECIEVED_MAILS = custom_security.getProperty("EnableSecurityDeleteRecievedMails", false);
//		SECURITY_DELETE_SENT_MAILS = custom_security.getProperty("EnableSecurityDeleteSentMails", false);
//		SECURITY_READ_OWN_MAILS = custom_security.getProperty("EnableSecurityReadOwnMails", false);
//		SECURITY_DELETE_MACRO = custom_security.getProperty("EnableSecurityDeleteMarco", false);
//		SECURITY_ADD_MACRO = custom_security.getProperty("EnableSecurityAddMarco", false);
//		SECURITY_DELETE_BOOKMARK_SLOT = custom_security.getProperty("EnableSecurityDeleteBookmark", false);
//		SECURITY_CLAN_ALLY_ALL = custom_security.getProperty("EnableSecurityClanAllyAll", false);
//		ENABLE_LOCKING_FEATURES = custom_security.getProperty("EnableLockingFeatures", false);
//		ALLOW_IP_LOCK = custom_security.getProperty("AllowLockIP", false);
//		ALLOW_HWID_LOCK = custom_security.getProperty("AllowLockHwid", false);
//		HWID_LOCK_MASK = custom_security.getProperty("HwidLockMask", 10);
//		
//	}
	
	private static void LoadStrider_Config()
	{
		ExProperties vote_config = load(STRIDER_CONFIG_FILE);
		EVENT_SR_LOC_ID_NPC = vote_config.getProperty("EventSRnpcID", 10);
		EVENT_NPC_PET = vote_config.getProperty("EventNpcPet", 12526);
		EVENT_SR_MINIMUM_PLAYERS = vote_config.getProperty("EventSRminimumPlayers", 2);
		EVENT_SR_MAXIMUM_PLAYERS = vote_config.getProperty("EventSRmaximumPlayers", 2);
		EVENT_SR_REWARD_TOP1 = parseItemsList(vote_config.getProperty("EventSRrewardsTop1", "57,300"));
		EVENT_SR_REWARD_TOP2 = parseItemsList(vote_config.getProperty("EventSRrewardsTop2", "57,200"));
		EVENT_SR_REWARD_TOP3 = parseItemsList(vote_config.getProperty("EventSRrewardsTop3", "57,100"));
		EVENT_SR_LOC_PLAYER_X = vote_config.getProperty("EventSRlocPlayerX", 0);
		EVENT_SR_LOC_PLAYER_Y = vote_config.getProperty("EventSRlocPlayerY", 0);
		EVENT_SR_LOC_PLAYER_Z = vote_config.getProperty("EventSRlocPlayerZ", 0);
		EVENT_SR_LOC_ARRIVAL_X = vote_config.getProperty("EventSRlocArrivalX", 0);
		EVENT_SR_LOC_ARRIVAL_Y = vote_config.getProperty("EventSRlocArrivalY", 0);
		EVENT_SR_LOC_ARRIVAL_Z = vote_config.getProperty("EventSRlocArrivalZ", 0);
	}
	
	// RWHO system (off emulation)
	
	public static int RWHO_KEEP_STAT;
	
	public static boolean RWHO_SEND_TRASH;
	
	public static int RWHO_ARRAY[] = new int[13];
	
	private static void loadPhantomsConfig()
	{
		ExProperties settings = load(PHANTOM_FILE);
		
		PHANTOM_PLAYERS_ENABLED = settings.getProperty("PhantomPlayersEnabled", false);
		PHANTOM_PLAYERS_ACCOUNT = settings.getProperty("PhantomPlayersAccount", "PhantomPlayerAI");
		PHANTOM_MAX_PLAYERS = settings.getProperty("PhantomMaxPlayers", 1);
		PHANTOM_BANNED_CLASSID = settings.getProperty("PhantomBannedClassIds", new int[] {});
		PHANTOM_BANNED_SETID = settings.getProperty("PhantomBannedSetIds", new int[] {});
		PHANTOM_MAX_WEAPON_GRADE = settings.getProperty("PhantomMaxWeaponGrade", 5);
		PHANTOM_MAX_ARMOR_GRADE = settings.getProperty("PhantomMaxArmorGrade", 5);
		PHANTOM_MAX_JEWEL_GRADE = settings.getProperty("PhantomMaxJewelGrade", 5);
		PHANTOM_SPAWN_MAX = settings.getProperty("PhantomSpawnMax", 1);
		PHANTOM_SPAWN_DELAY = settings.getProperty("PhantomSpawnDelay", 60);
		PHANTOM_MAX_LIFETIME = settings.getProperty("PhantomMaxLifetime", 120);
		
		CHANCE_TO_ENCHANT_WEAP = settings.getProperty("PhantomChanceEnchantWeap", 0);
		MAX_ENCH_PHANTOM_WEAP = settings.getProperty("PhantomMaxEnchantWeap", 4);
		
		PHANTOM_MAX_DRIFT_RANGE = settings.getProperty("MaxDriftRangeForNpc", 1000);
		
		ALLOW_PHANTOM_CUSTOM_TITLES = settings.getProperty("AllowSetupCustomTitles", false);
		PHANTOM_CHANCE_SET_NOBLE_TITLE = settings.getProperty("ChanceToSetTitle", 30);
		
		DISABLE_PHANTOM_ACTIONS = settings.getProperty("DisablePhantomActions", false);
		
		PHANTOM_ALLOWED_NPC_TO_WALK = settings.getProperty("PhantomRoamingNpcs", new int[] {});
		PHANTOM_ROAMING_MAX_WH_CHECKS = settings.getProperty("PhantomRoamingMaxWhChecks", 2);
		PHANTOM_ROAMING_MAX_WH_CHECKS_DWARF = settings.getProperty("PhantomRoamingMaxWhChecksDwarf", 8);
		PHANTOM_ROAMING_MAX_SHOP_CHECKS = settings.getProperty("PhantomRoamingMaxShopChecks", 2);
		PHANTOM_ROAMING_MAX_SHOP_CHECKS_DWARF = settings.getProperty("PhantomRoamingMaxShopChecksDwarf", 5);
		PHANTOM_ROAMING_MAX_NPC_CHECKS = settings.getProperty("PhantomRoamingMaxNpcChecks", 6);
		PHANTOM_ROAMING_MIN_WH_DELAY = settings.getProperty("PhantomRoamingMinWhDelay", 60);
		PHANTOM_ROAMING_MAX_WH_DELAY = settings.getProperty("PhantomRoamingMaxWhDelay", 300);
		PHANTOM_ROAMING_MIN_SHOP_DELAY = settings.getProperty("PhantomRoamingMinShopDelay", 30);
		PHANTOM_ROAMING_MAX_SHOP_DELAY = settings.getProperty("PhantomRoamingMaxShopDelay", 120);
		PHANTOM_ROAMING_MIN_NPC_DELAY = settings.getProperty("PhantomRoamingMinNpcDelay", 45);
		PHANTOM_ROAMING_MIN_PRIVATESTORE_DELAY = settings.getProperty("PhantomRoamingMinPrivatestoreDelay", 2);
		PHANTOM_ROAMING_MAX_PRIVATESTORE_DELAY = settings.getProperty("PhantomRoamingMaxPrivatestoreDelay", 7);
		PHANTOM_ROAMING_MIN_FREEROAM_DELAY = settings.getProperty("PhantomRoamingMinFreeroamDelay", 10);
		PHANTOM_ROAMING_MAX_FREEROAM_DELAY = settings.getProperty("PhantomRoamingMaxFreeroamDelay", 60);
		DISABLE_PHANTOM_RESPAWN = settings.getProperty("DisablePhantomRespawn", false);
		DEBUG_PHANTOMS = settings.getProperty("DebugPhantoms", false);
		PHANTOM_CLANS = settings.getProperty("PhantomClans", new int[] {});
	}
	
	public static boolean BUFF_STORE_ENABLED;
	public static boolean BUFF_STORE_MP_ENABLED;
	public static double BUFF_STORE_MP_CONSUME_MULTIPLIER;
	public static boolean BUFF_STORE_ITEM_CONSUME_ENABLED;
	public static int BUFF_STORE_NAME_COLOR;
	public static int BUFF_STORE_TITLE_COLOR;
	public static int BUFF_STORE_OFFLINE_NAME_COLOR;
	public static List<Integer> BUFF_STORE_ALLOWED_CLASS_LIST;
	public static List<Integer> BUFF_STORE_FORBIDDEN_SKILL_LIST;
	
	public static void loadBuffStoreConfig()
	{
		ExProperties buffStoreConfig = load(BUFF_STORE_CONFIG_FILE);
		
		// Buff Store
		BUFF_STORE_ENABLED = buffStoreConfig.getProperty("BuffStoreEnabled", false);
		BUFF_STORE_MP_ENABLED = buffStoreConfig.getProperty("BuffStoreMpEnabled", true);
		BUFF_STORE_MP_CONSUME_MULTIPLIER = buffStoreConfig.getProperty("BuffStoreMpConsumeMultiplier", 1.0f);
		BUFF_STORE_ITEM_CONSUME_ENABLED = buffStoreConfig.getProperty("BuffStoreItemConsumeEnabled", true);
		BUFF_STORE_NAME_COLOR = Integer.decode("0x" + buffStoreConfig.getProperty("BuffStoreNameColor", "808080"));
		BUFF_STORE_TITLE_COLOR = Integer.decode("0x" + buffStoreConfig.getProperty("BuffStoreTitleColor", "808080"));
		BUFF_STORE_OFFLINE_NAME_COLOR = Integer.decode("0x" + buffStoreConfig.getProperty("BuffStoreOfflineNameColor", "808080"));
		final String[] classes = buffStoreConfig.getProperty("BuffStoreAllowedClassList", "").split(",");
		BUFF_STORE_ALLOWED_CLASS_LIST = new ArrayList<>();
		if (classes.length > 0)
		{
			for (String classId : classes)
			{
				BUFF_STORE_ALLOWED_CLASS_LIST.add(Integer.parseInt(classId));
			}
		}
		final String[] skills = buffStoreConfig.getProperty("BuffStoreForbiddenSkillList", "").split(",");
		BUFF_STORE_FORBIDDEN_SKILL_LIST = new ArrayList<>();
		if (skills.length > 0)
		{
			for (String skillId : skills)
			{
				BUFF_STORE_FORBIDDEN_SKILL_LIST.add(Integer.parseInt(skillId));
			}
		}
	}
	
	public static int SERVICES_AUGMENTATION_PRICE;
	public static int SERVICES_AUGMENTATION_ITEM;
	public static List<Integer> SERVICES_AUGMENTATION_DISABLED_LIST = new ArrayList<>();
	public static boolean SERVICES_LEVEL_UP_ENABLE;
	public static boolean SERVICES_DELEVEL_ENABLE;
	public static int[] SERVICES_LEVEL_UP;
	public static int[] SERVICES_DELEVEL;
	public static boolean SERVICES_BUY_RECOMMENDS_ENABLED;
	public static int SERVICES_BUY_RECOMMENDS_PRICE;
	public static int SERVICES_BUY_RECOMMENDS_ITEM;
	public static boolean SERVICES_BUY_CLAN_REPUTATION_ENABLED;
	public static int SERVICES_BUY_CLAN_REPUTATION_PRICE;
	public static int SERVICES_BUY_CLAN_REPUTATION_ITEM;
	public static int SERVICES_BUY_CLAN_REPUTATION_COUNT;
	public static boolean SERVICES_BUY_FAME_ENABLED;
	public static int SERVICES_BUY_FAME_PRICE;
	public static int SERVICES_BUY_FAME_ITEM;
	public static int SERVICES_BUY_FAME_COUNT;
	public static boolean DONATE_NOBLESS_ENABLE;
	public static int DONATE_NOBLESS_SELL_ITEM;
	public static long DONATE_NOBLESS_SELL_PRICE;
	public static boolean SERVICES_CLAN_LEVEL_ENABLED;
	public static int SERVICES_CLAN_LEVEL_ITEM;
	public static int SERVICES_CLAN_LEVEL_8_PRICE;
	public static int SERVICES_CLAN_LEVEL_9_PRICE;
	public static int SERVICES_CLAN_LEVEL_10_PRICE;
	public static int SERVICES_CLAN_LEVEL_11_PRICE;
	public static boolean SERVICES_CLAN_SKILLS_ENABLED;
	public static int SERVICES_CLAN_SKILLS_ITEM;
	public static int SERVICES_CLAN_SKILLS_8_PRICE;
	public static int SERVICES_CLAN_SKILLS_9_PRICE;
	public static int SERVICES_CLAN_SKILLS_10_PRICE;
	public static int SERVICES_CLAN_SKILLS_11_PRICE;
	public static boolean SERVICES_OLF_STORE_ENABLED;
	public static int SERVICES_OLF_STORE_ITEM;
	public static int SERVICES_OLF_STORE_0_PRICE;
	public static int SERVICES_OLF_STORE_6_PRICE;
	public static int SERVICES_OLF_STORE_7_PRICE;
	public static int SERVICES_OLF_STORE_8_PRICE;
	public static int SERVICES_OLF_STORE_9_PRICE;
	public static int SERVICES_OLF_STORE_10_PRICE;
	public static boolean SERVICES_OLF_TRANSFER_ENABLED;
	public static int[] SERVICES_OLF_TRANSFER_ITEM;
	public static boolean SERVICES_SOUL_CLOAK_TRANSFER_ENABLED;
	public static int[] SERVICES_SOUL_CLOAK_TRANSFER_ITEM;
	public static boolean SERVICES_EXCHANGE_EQUIP;
	public static int SERVICES_EXCHANGE_EQUIP_ITEM;
	public static int SERVICES_EXCHANGE_EQUIP_ITEM_PRICE;
	public static int SERVICES_EXCHANGE_UPGRADE_EQUIP_ITEM;
	public static int SERVICES_EXCHANGE_UPGRADE_EQUIP_ITEM_PRICE;
	public static int[] SERVICES_UNBAN_ITEM;
	
	public static void loadDonationStore()
	{
		ExProperties DonationStore = load(DONATION_STORE);
		
		SERVICES_AUGMENTATION_PRICE = DonationStore.getProperty("AugmentationPrice", 50);
		SERVICES_AUGMENTATION_ITEM = DonationStore.getProperty("AugmentationItem", 37000);
		final String[] augs = DonationStore.getProperty("AugmentationDisabledList", "0").trim().split(",");
		for (String aug : augs)
		{
			if (!aug.isEmpty())
			{
				SERVICES_AUGMENTATION_DISABLED_LIST.add(Integer.parseInt(aug.trim()));
			}
		}
		SERVICES_CHANGE_NICK_ALLOW_SYMBOL = DonationStore.getProperty("NickChangeAllowSimbol", false);
		SERVICES_CHANGE_NICK_ENABLED = DonationStore.getProperty("NickChangeEnabled", false);
		SERVICES_CHANGE_NICK_PRICE = DonationStore.getProperty("NickChangePrice", 100);
		SERVICES_CHANGE_NICK_ITEM = DonationStore.getProperty("NickChangeItem", 37000);
		SERVICES_CHANGE_CLAN_NAME_ENABLED = DonationStore.getProperty("ClanNameChangeEnabled", false);
		SERVICES_CHANGE_CLAN_NAME_PRICE = DonationStore.getProperty("ClanNameChangePrice", 100);
		SERVICES_CHANGE_CLAN_NAME_ITEM = DonationStore.getProperty("ClanNameChangeItem", 4037);
		SERVICES_LEVEL_UP_ENABLE = DonationStore.getProperty("LevelChangeEnabled", false);
		SERVICES_DELEVEL_ENABLE = DonationStore.getProperty("DeLevelChangeEnabled", false);
		SERVICES_LEVEL_UP = DonationStore.getProperty("LevelUp", new int[]
		{
			37000,
			1
		});
		SERVICES_DELEVEL = DonationStore.getProperty("LevelDown", new int[]
		{
			37000,
			1
		});
		SERVICES_BUY_RECOMMENDS_ENABLED = DonationStore.getProperty("BuyRecommendsEnabled", false);
		SERVICES_BUY_RECOMMENDS_PRICE = DonationStore.getProperty("BuyRecommendsPrice", 50);
		SERVICES_BUY_RECOMMENDS_ITEM = DonationStore.getProperty("BuyRecommendsItem", 37000);
		SERVICES_BUY_CLAN_REPUTATION_ENABLED = DonationStore.getProperty("BuyClanReputationEnabled", false);
		SERVICES_BUY_CLAN_REPUTATION_PRICE = DonationStore.getProperty("BuyClanReputationPrice", 100);
		SERVICES_BUY_CLAN_REPUTATION_ITEM = DonationStore.getProperty("BuyClanReputationItem", 37000);
		SERVICES_BUY_CLAN_REPUTATION_COUNT = DonationStore.getProperty("BuyClanReputationCount", 40000);
		SERVICES_BUY_FAME_ENABLED = DonationStore.getProperty("BuyFameEnabled", false);
		SERVICES_BUY_FAME_PRICE = DonationStore.getProperty("BuyFamePrice", 100);
		SERVICES_BUY_FAME_ITEM = DonationStore.getProperty("BuyFameItem", 37000);
		SERVICES_BUY_FAME_COUNT = DonationStore.getProperty("BuyFameCount", 37000);
		SERVICES_NOBLESS_SELL_ENABLED = DonationStore.getProperty("NoblessSellEnabled", false);
		SERVICES_NOBLESS_SELL_PRICE = DonationStore.getProperty("NoblessSellPrice", 1000);
		SERVICES_NOBLESS_SELL_ITEM = DonationStore.getProperty("NoblessSellItem", 4037);
		DONATE_NOBLESS_ENABLE = DonationStore.getProperty("DonateNoblessEnabled", false);
		DONATE_NOBLESS_SELL_ITEM = DonationStore.getProperty("DonateNoblessItemId", 37000);
		DONATE_NOBLESS_SELL_PRICE = DonationStore.getProperty("DonateNoblessItemCont", 100);
		SERVICES_CLAN_LEVEL_ENABLED = DonationStore.getProperty("ClanLvlService", true);
		SERVICES_CLAN_LEVEL_ITEM = DonationStore.getProperty("ClanLvLItem", 37000);
		SERVICES_CLAN_LEVEL_8_PRICE = DonationStore.getProperty("ClanLvl8Price", 150);
		SERVICES_CLAN_LEVEL_9_PRICE = DonationStore.getProperty("ClanLvl9Price", 400);
		SERVICES_CLAN_LEVEL_10_PRICE = DonationStore.getProperty("ClanLvl10Price", 650);
		SERVICES_CLAN_LEVEL_11_PRICE = DonationStore.getProperty("ClanLvl11Price", 900);
		SERVICES_CLAN_SKILLS_ENABLED = DonationStore.getProperty("ClanSkillsService", true);
		SERVICES_CLAN_SKILLS_ITEM = DonationStore.getProperty("ClanSkillsItem", 37000);
		SERVICES_CLAN_SKILLS_8_PRICE = DonationStore.getProperty("ClanSkillLvl8Price", 150);
		SERVICES_CLAN_SKILLS_9_PRICE = DonationStore.getProperty("ClanSkillLvl9Price", 400);
		SERVICES_CLAN_SKILLS_10_PRICE = DonationStore.getProperty("ClanSkillLvl10Price", 650);
		SERVICES_CLAN_SKILLS_11_PRICE = DonationStore.getProperty("ClanSkillLvl11Price", 900);
		SERVICES_OLF_STORE_ENABLED = DonationStore.getProperty("OlfStoreService", true);
		SERVICES_OLF_STORE_ITEM = DonationStore.getProperty("OlfStoreItem", 37000);
		SERVICES_OLF_STORE_0_PRICE = DonationStore.getProperty("OlfStore0", 100);
		SERVICES_OLF_STORE_6_PRICE = DonationStore.getProperty("OlfStore6", 200);
		SERVICES_OLF_STORE_7_PRICE = DonationStore.getProperty("OlfStore7", 275);
		SERVICES_OLF_STORE_8_PRICE = DonationStore.getProperty("OlfStore8", 350);
		SERVICES_OLF_STORE_9_PRICE = DonationStore.getProperty("OlfStore9", 425);
		SERVICES_OLF_STORE_10_PRICE = DonationStore.getProperty("OlfStore10", 500);
		SERVICES_OLF_TRANSFER_ENABLED = DonationStore.getProperty("OlfTransfer", true);
		SERVICES_OLF_TRANSFER_ITEM = DonationStore.getProperty("OlfTransferItem", new int[]
		{
			10639,
			100
		});
		SERVICES_SOUL_CLOAK_TRANSFER_ENABLED = DonationStore.getProperty("SCTransfer", true);
		SERVICES_SOUL_CLOAK_TRANSFER_ITEM = DonationStore.getProperty("SCTransferItem", new int[]
		{
			37000,
			50
		});
		SERVICES_EXCHANGE_EQUIP = DonationStore.getProperty("ExchangeEquipService", true);
		SERVICES_EXCHANGE_EQUIP_ITEM = DonationStore.getProperty("ExchangeEquipItem", 37000);
		SERVICES_EXCHANGE_EQUIP_ITEM_PRICE = DonationStore.getProperty("ExchangeEquipPrice", 50);
		SERVICES_EXCHANGE_UPGRADE_EQUIP_ITEM = DonationStore.getProperty("ExchangeUpgradeEquipItem", 37000);
		SERVICES_EXCHANGE_UPGRADE_EQUIP_ITEM_PRICE = DonationStore.getProperty("ExchangeUpgradeEquipPrice", 50);
		SERVICES_UNBAN_ITEM = DonationStore.getProperty("UnbanItem", new int[]
		{
			37000,
			150
		});
	}
	
	public static boolean BBS_FORGE_ENABLED;
	public static int BBS_FORGE_ENCHANT_ITEM;
	public static int BBS_FORGE_FOUNDATION_ITEM;
	public static int[] BBS_FORGE_FOUNDATION_PRICE_ARMOR;
	public static int[] BBS_FORGE_FOUNDATION_PRICE_WEAPON;
	public static int[] BBS_FORGE_FOUNDATION_PRICE_JEWEL;
	public static int[] BBS_FORGE_ENCHANT_MAX;
	public static int[] BBS_FORGE_WEAPON_ENCHANT_LVL;
	public static int[] BBS_FORGE_ARMOR_ENCHANT_LVL;
	public static int[] BBS_FORGE_JEWELS_ENCHANT_LVL;
	public static int[] BBS_FORGE_ENCHANT_PRICE_WEAPON;
	public static int[] BBS_FORGE_ENCHANT_PRICE_ARMOR;
	public static int[] BBS_FORGE_ENCHANT_PRICE_JEWELS;
	public static int[] BBS_FORGE_AUGMENT_ITEMS_LIST;
	public static long[] BBS_FORGE_AUGMENT_COUNT_LIST;
	public static int BBS_FORGE_WEAPON_ATTRIBUTE_MAX;
	public static int BBS_FORGE_ARMOR_ATTRIBUTE_MAX;
	public static int[] BBS_FORGE_ATRIBUTE_LVL_WEAPON;
	public static int[] BBS_FORGE_ATRIBUTE_LVL_ARMOR;
	public static int[] BBS_FORGE_ATRIBUTE_PRICE_ARMOR;
	public static int[] BBS_FORGE_ATRIBUTE_PRICE_WEAPON;
	public static boolean BBS_FORGE_ATRIBUTE_PVP;
	public static String[] BBS_FORGE_GRADE_ATTRIBUTE;
	
	public static void loadForgeSettings()
	{
		ExProperties forge = load(FORGE_CONFIG_FILE);
		BBS_FORGE_ENABLED = forge.getProperty("Allow", false);
		BBS_FORGE_ENCHANT_ITEM = forge.getProperty("Item", 4356);
		BBS_FORGE_FOUNDATION_ITEM = forge.getProperty("FoundationItem", 37000);
		BBS_FORGE_FOUNDATION_PRICE_ARMOR = forge.getProperty("FoundationPriceArmor", new int[]
		{
			1,
			1,
			1,
			1,
			1,
			2,
			5,
			10
		});
		BBS_FORGE_FOUNDATION_PRICE_WEAPON = forge.getProperty("FoundationPriceWeapon", new int[]
		{
			1,
			1,
			1,
			1,
			1,
			2,
			5,
			10
		});
		BBS_FORGE_FOUNDATION_PRICE_JEWEL = forge.getProperty("FoundationPriceJewel", new int[]
		{
			1,
			1,
			1,
			1,
			1,
			2,
			5,
			10
		});
		BBS_FORGE_ENCHANT_MAX = forge.getProperty("MaxEnchant", new int[]
		{
			25
		});
		BBS_FORGE_WEAPON_ENCHANT_LVL = forge.getProperty("WValue", new int[]
		{
			5
		});
		BBS_FORGE_ARMOR_ENCHANT_LVL = forge.getProperty("AValue", new int[]
		{
			5
		});
		BBS_FORGE_JEWELS_ENCHANT_LVL = forge.getProperty("JValue", new int[]
		{
			5
		});
		BBS_FORGE_ENCHANT_PRICE_WEAPON = forge.getProperty("WPrice", new int[]
		{
			5
		});
		BBS_FORGE_ENCHANT_PRICE_ARMOR = forge.getProperty("APrice", new int[]
		{
			5
		});
		BBS_FORGE_ENCHANT_PRICE_JEWELS = forge.getProperty("JPrice", new int[]
		{
			5
		});
		BBS_FORGE_AUGMENT_ITEMS_LIST = forge.getProperty("AugmentItems", new int[]
		{
			4037,
			4037,
			4037,
			4037
		});
		BBS_FORGE_AUGMENT_COUNT_LIST = forge.getProperty("AugmentCount", new long[]
		{
			1L,
			3L,
			6L,
			10L
		});
		BBS_FORGE_ATRIBUTE_LVL_WEAPON = forge.getProperty("AtributeWeaponValue", new int[]
		{
			25
		});
		BBS_FORGE_ATRIBUTE_PRICE_WEAPON = forge.getProperty("PriceForAtributeWeapon", new int[]
		{
			25
		});
		BBS_FORGE_ATRIBUTE_LVL_ARMOR = forge.getProperty("AtributeArmorValue", new int[]
		{
			25
		});
		BBS_FORGE_ATRIBUTE_PRICE_ARMOR = forge.getProperty("PriceForAtributeArmor", new int[]
		{
			25
		});
		BBS_FORGE_ATRIBUTE_PVP = forge.getProperty("AtributePvP", true);
		BBS_FORGE_WEAPON_ATTRIBUTE_MAX = forge.getProperty("MaxWAttribute", 25);
		BBS_FORGE_ARMOR_ATTRIBUTE_MAX = forge.getProperty("MaxAAttribute", 25);
		BBS_FORGE_GRADE_ATTRIBUTE = forge.getProperty("AtributeGrade", "NG:NO;D:NO;C:NO;B:NO;A:ON;S:ON;S80:ON;S84:ON").trim().replaceAll(" ", "").split(";");
	}
	
	// Scheme Buffer
	public static boolean NpcBuffer_VIP;
	public static int NpcBuffer_VIP_ALV;
	public static boolean NpcBuffer_EnableBuff;
	public static boolean NpcBuffer_EnableScheme;
	public static boolean NpcBuffer_EnableHeal;
	public static boolean NpcBuffer_EnableBuffs;
	public static boolean NpcBuffer_EnableResist;
	public static boolean NpcBuffer_EnableSong;
	public static boolean NpcBuffer_EnableDance;
	public static boolean NpcBuffer_EnableChant;
	public static boolean NpcBuffer_EnableOther;
	public static boolean NpcBuffer_EnableSpecial;
	public static boolean NpcBuffer_EnableCubic;
	public static boolean NpcBuffer_EnableCancel;
	public static boolean NpcBuffer_EnableBuffSet;
	public static boolean NpcBuffer_EnableBuffPK;
	public static boolean NpcBuffer_EnableFreeBuffs;
	public static boolean NpcBuffer_EnableTimeOut;
	public static int NpcBuffer_TimeOutTime;
	public static int NpcBuffer_MinLevel;
	public static int NpcBuffer_PriceCancel;
	public static int NpcBuffer_PriceHeal;
	public static int NpcBuffer_PriceBuffs;
	public static int NpcBuffer_PriceResist;
	public static int NpcBuffer_PriceSong;
	public static int NpcBuffer_PriceDance;
	public static int NpcBuffer_PriceChant;
	public static int NpcBuffer_PriceOther;
	public static int NpcBuffer_PriceSpecial;
	public static int NpcBuffer_PriceCubic;
	public static int NpcBuffer_PriceSet;
	public static int NpcBuffer_PriceScheme;
	public static int NpcBuffer_MaxScheme;
	public static boolean NpcBuffer_EnablePremiumBuffs;
	public static boolean SCHEME_ALLOW_FLAG;
	public static List<int[]> NpcBuffer_BuffSetMage = new ArrayList<>();
	public static List<int[]> NpcBuffer_BuffSetFighter = new ArrayList<>();
	public static List<int[]> NpcBuffer_BuffSetDagger = new ArrayList<>();
	public static List<int[]> NpcBuffer_BuffSetSupport = new ArrayList<>();
	public static List<int[]> NpcBuffer_BuffSetTank = new ArrayList<>();
	public static List<int[]> NpcBuffer_BuffSetArcher = new ArrayList<>();
	
	public static final String NPCBUFFER_CONFIG_FILE = "config/npcbuffer.ini";
	
	public static void loadSchemeBuffer()
	{
		ExProperties npcbuffer = load(NPCBUFFER_CONFIG_FILE);
		
		NpcBuffer_VIP = npcbuffer.getProperty("EnableVIP", false);
		NpcBuffer_VIP_ALV = npcbuffer.getProperty("VipAccesLevel", 1);
		NpcBuffer_EnableBuff = npcbuffer.getProperty("EnableBuffSection", true);
		NpcBuffer_EnableScheme = npcbuffer.getProperty("EnableScheme", true);
		NpcBuffer_EnableHeal = npcbuffer.getProperty("EnableHeal", true);
		NpcBuffer_EnableBuffs = npcbuffer.getProperty("EnableBuffs", true);
		NpcBuffer_EnableResist = npcbuffer.getProperty("EnableResist", true);
		NpcBuffer_EnableSong = npcbuffer.getProperty("EnableSongs", true);
		NpcBuffer_EnableDance = npcbuffer.getProperty("EnableDances", true);
		NpcBuffer_EnableChant = npcbuffer.getProperty("EnableChants", true);
		NpcBuffer_EnableOther = npcbuffer.getProperty("EnableOther", true);
		NpcBuffer_EnableSpecial = npcbuffer.getProperty("EnableSpecial", true);
		NpcBuffer_EnableCubic = npcbuffer.getProperty("EnableCubic", false);
		NpcBuffer_EnableCancel = npcbuffer.getProperty("EnableRemoveBuffs", true);
		NpcBuffer_EnableBuffSet = npcbuffer.getProperty("EnableBuffSet", true);
		NpcBuffer_EnableBuffPK = npcbuffer.getProperty("EnableBuffForPK", false);
		NpcBuffer_EnableFreeBuffs = npcbuffer.getProperty("EnableFreeBuffs", true);
		NpcBuffer_EnableTimeOut = npcbuffer.getProperty("EnableTimeOut", true);
		SCHEME_ALLOW_FLAG = npcbuffer.getProperty("EnableBuffforFlag", false);
		NpcBuffer_TimeOutTime = npcbuffer.getProperty("TimeoutTime", 10);
		NpcBuffer_MinLevel = npcbuffer.getProperty("MinimumLevel", 20);
		NpcBuffer_PriceCancel = npcbuffer.getProperty("RemoveBuffsPrice", 100000);
		NpcBuffer_PriceHeal = npcbuffer.getProperty("HealPrice", 100000);
		NpcBuffer_PriceBuffs = npcbuffer.getProperty("BuffsPrice", 100000);
		NpcBuffer_PriceResist = npcbuffer.getProperty("ResistPrice", 100000);
		NpcBuffer_PriceSong = npcbuffer.getProperty("SongPrice", 100000);
		NpcBuffer_PriceDance = npcbuffer.getProperty("DancePrice", 100000);
		NpcBuffer_PriceChant = npcbuffer.getProperty("ChantsPrice", 100000);
		NpcBuffer_PriceOther = npcbuffer.getProperty("OtherPrice", 100000);
		NpcBuffer_PriceSpecial = npcbuffer.getProperty("SpecialPrice", 100000);
		NpcBuffer_PriceCubic = npcbuffer.getProperty("CubicPrice", 100000);
		NpcBuffer_PriceSet = npcbuffer.getProperty("SetPrice", 100000);
		NpcBuffer_PriceScheme = npcbuffer.getProperty("SchemePrice", 100000);
		NpcBuffer_MaxScheme = npcbuffer.getProperty("MaxScheme", 4);
		NpcBuffer_EnablePremiumBuffs = npcbuffer.getProperty("EnablePremiumBuffs", false);
		String[] parts;
		String[] skills = npcbuffer.getProperty("BuffSetMage", "192,1").split(";");
		for (String sk : skills)
		{
			parts = sk.split(",");
			NpcBuffer_BuffSetMage.add(new int[]
			{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1])
			});
		}
		
		skills = npcbuffer.getProperty("BuffSetFighter", "192,1").split(";");
		for (String sk : skills)
		{
			parts = sk.split(",");
			NpcBuffer_BuffSetFighter.add(new int[]
			{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1])
			});
		}
		
		skills = npcbuffer.getProperty("BuffSetDagger", "192,1").split(";");
		for (String sk : skills)
		{
			parts = sk.split(",");
			NpcBuffer_BuffSetDagger.add(new int[]
			{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1])
			});
		}
		
		skills = npcbuffer.getProperty("BuffSetSupport", "192,1").split(";");
		for (String sk : skills)
		{
			parts = sk.split(",");
			NpcBuffer_BuffSetSupport.add(new int[]
			{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1])
			});
		}
		
		skills = npcbuffer.getProperty("BuffSetTank", "192,1").split(";");
		for (String sk : skills)
		{
			parts = sk.split(",");
			NpcBuffer_BuffSetTank.add(new int[]
			{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1])
			});
		}
		
		skills = npcbuffer.getProperty("BuffSetArcher", "192,1").split(";");
		for (String sk : skills)
		{
			parts = sk.split(",");
			NpcBuffer_BuffSetArcher.add(new int[]
			{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1])
			});
		}
	}
	
	public static class RaidGlobalDrop
	{
		int _id;
		long _count;
		double _chance;
		
		public RaidGlobalDrop(int id, long count, double chance)
		{
			_id = id;
			_count = count;
			_chance = chance;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public long getCount()
		{
			return _count;
		}
		
		public double getChance()
		{
			return _chance;
		}
	}
	
	public static void load()
	{
		loadSpoilConfig();
		loadVoteConfig();
		loadServerConfig();
		loadResidenceConfig();
		loadFormulasConfig();
		loadNpcSetting();
		loadAltSettings();
		loadServicesSettings();
		loadPvPSettings();
		loadAISettings();
		loadGeodataSettings();
		loadGMSettings();
		loadEventsSettings();
		loadOlympiadSettings();
		loadDevelopSettings();
		loadExtSettings();
		loadForgeSettings();
		loadDonationStore();
		loadBuffStoreConfig();
		loadRatesConfig();
		loadFightClubSettings();
		loadChatConfig();
		loadEpicBossConfig();
		loadInstancesConfig();
		loadItemsSettings();
		loadSchemeBuffer();
		abuseLoad();
		loadVIKTORINAsettings();
		if (ALLOW_ADDONS_CONFIG)
		{
			AddonsConfig.load();
		}
		loadCommunityPvPboardsettings();
		LoadCustom_Config();
		//LoadCustomSecurity_Config();
		LoadStrider_Config();
		loadPhantomsConfig();
		DonateShopMain.getInstance().reload();
	}
	
	private Config()
	{
		
	}
	
	/**
	 * itemId1,itemNumber1;itemId2,itemNumber2... to the int[n][2] = [itemId1][itemNumber1],[itemId2][itemNumber2]...
	 * @param line
	 * @return an array consisting of parsed items.
	 */
	private static final int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
		{
			return null;
		}
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warn("Config: Error parsing entry -> \"" + valueSplit[0] + "\", should be itemId,itemNumber");
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warn("Config: Error parsing item ID -> \"" + valueSplit[0] + "\"");
				return null;
			}
			
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warn("Config: Error parsing item amount -> \"" + valueSplit[1] + "\"");
				return null;
			}
			i++;
		}
		return result;
	}
	
	public static void abuseLoad()
	{
		List<Pattern> tmp = new ArrayList<>();
		
		LineNumberReader lnr = null;
		try
		{
			String line;
			
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(ANUSEWORDS_CONFIG_FILE), "UTF-8"));
			
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
				}
			}
			
			ABUSEWORD_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + ABUSEWORD_LIST.length + " abuse words.");
		}
		catch (IOException e1)
		{
			_log.warn("Error reading abuse: " + e1);
		}
		finally
		{
			try
			{
				if (lnr != null)
				{
					lnr.close();
				}
			}
			catch (Exception e2)
			{
				// nothing
			}
		}
	}
	
	public static boolean containsAbuseWord(String s)
	{
		for (Pattern pattern : ABUSEWORD_LIST)
		{
			if (pattern.matcher(s).matches())
			{
				return true;
			}
		}
		return false;
	}
	
	public static String getField(String fieldName)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);
		
		if (field == null)
		{
			return null;
		}
		
		try
		{
			return String.valueOf(field.get(null));
		}
		catch (IllegalArgumentException e)
		{
			
		}
		catch (IllegalAccessException e)
		{
			
		}
		
		return null;
	}
	
	public static boolean setField(String fieldName, String value)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);
		
		if (field == null)
		{
			return false;
		}
		
		try
		{
			if (field.getType() == boolean.class)
			{
				field.setBoolean(null, BooleanUtils.toBoolean(value));
			}
			else if (field.getType() == int.class)
			{
				field.setInt(null, NumberUtils.toInt(value));
			}
			else if (field.getType() == long.class)
			{
				field.setLong(null, NumberUtils.toLong(value));
			}
			else if (field.getType() == double.class)
			{
				field.setDouble(null, NumberUtils.toDouble(value));
			}
			else if (field.getType() == String.class)
			{
				field.set(null, value);
			}
			else
			{
				return false;
			}
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
		catch (IllegalAccessException e)
		{
			return false;
		}
		
		return true;
	}
	
	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}
	
	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		
		try
		{
			result.load(file);
		}
		catch (IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!", e);
		}
		
		return result;
	}
	
	public static final File findResource(final String path)
	{
		return findNonCustomResource(path);
	}
	
	public static final File findNonCustomResource(final String path)
	{
		File file = new File(DATAPACK_ROOT, path);
		if (!file.exists())
		{
			file = new File(path);
		}
		return file;
	}
	
	/**
	 * @param configMap
	 * @param creature
	 * @return
	 */
	public static int getLimit(TIntIntHashMap configMap, Creature creature)
	{
		// TODO: Cache in ClassId for faster access???
		if (creature.isPlayer())
		{
			ClassId classId = creature.getPlayer().getClassId();
			if (configMap.containsKey(classId.getId()))
			{
				return configMap.get(classId.getId());
			}
			for (ClassId parent = classId.getParent(creature.getPlayer().getSex()); parent != null; parent = parent.getParent(creature.getPlayer().getSex()))
			{
				if (configMap.containsKey(parent.getId()))
				{
					int limit = configMap.get(parent.getId());
					configMap.put(classId.getId(), limit); // Cache here so we don't search like that next time.
					return limit;
				}
			}
		}
		
		return configMap.get(-1);
	}
}