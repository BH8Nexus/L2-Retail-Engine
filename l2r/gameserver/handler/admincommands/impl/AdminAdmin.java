package l2r.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.lang.StatsUtils;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.GameServer;
import l2r.gameserver.Shutdown;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.auction.AuctionManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.instancemanager.SoIManager;
import l2r.gameserver.instancemanager.VoteManager;
import l2r.gameserver.instancemanager.games.DonationBonusDay;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.model.CancelSystem;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.EventTrigger;
import l2r.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.CharacterIntro;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.GameStats;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Util;

public class AdminAdmin implements IAdminCommandHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminTournament.class);
	
	// private GeoCollision _collision = new GeoBlock();
	private static enum Commands
	{
		admin_admin,
		admin_play_sounds,
		admin_play_sound,
		admin_silence,
		admin_tradeoff,
		admin_cfg,
		admin_config,
		admin_show_html,
		admin_setnpcstate,
		admin_setareanpcstate,
		admin_showmovie,
		admin_setzoneinfo,
		admin_clienteffect,
		admin_eventtrigger,
		admin_debug,
		admin_uievent,
		admin_opensod,
		admin_closesod,
		admin_setsoistage,
		admin_soinotify,
		admin_forcenpcinfo,
		admin_loc,
		admin_locdump,
		admin_immortal,
		admin_set_immortal,
		admin_checkadena,
		admin_announcepoll,
		admin_donatepromotion,
		admin_donatepromotionpercent,
		admin_getemail,
		admin_ready,
		admin_gmon,
		admin_gmliston,
		admin_gmoff,
		admin_gmlistoff,
		admin_sm,
		admin_smpage,
		admin_trivia,
		admin_startcancelsystem,
		admin_stopcancelsystem,
		admin_removeauction,
		admin_promotionlevelup,
		admin_zonesize,
		admin_loc2
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		StringTokenizer st;
		
		GameObject target = activeChar.getTarget();
		switch (command)
		{
			case admin_admin:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
				break;
			case admin_play_sounds:
				if (wordList.length == 1)
				{
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs.htm"));
				}
				else
				{
					try
					{
						activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
					}
					catch (StringIndexOutOfBoundsException e)
					{
					}
				}
				break;
			case admin_play_sound:
				try
				{
					playAdminSound(activeChar, wordList[1]);
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
				break;
			case admin_silence:
				if (activeChar.getMessageRefusal()) // already in message refusal
				// mode
				{
					activeChar.unsetVar("gm_silence");
					activeChar.setMessageRefusal(false);
					activeChar.sendPacket(SystemMsg.MESSAGE_ACCEPTANCE_MODE);
					activeChar.sendEtcStatusUpdate();
				}
				else
				{
					if (Config.SAVE_GM_EFFECTS)
					{
						activeChar.setVar("gm_silence", "true", -1);
					}
					activeChar.setMessageRefusal(true);
					activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
					activeChar.sendEtcStatusUpdate();
				}
				break;
			case admin_tradeoff:
				try
				{
					if (wordList[1].equalsIgnoreCase("on"))
					{
						activeChar.setTradeRefusal(true);
						Functions.sendDebugMessage(activeChar, "tradeoff enabled");
					}
					else if (wordList[1].equalsIgnoreCase("off"))
					{
						activeChar.setTradeRefusal(false);
						Functions.sendDebugMessage(activeChar, "tradeoff disabled");
					}
				}
				catch (Exception ex)
				{
					if (activeChar.getTradeRefusal())
					{
						Functions.sendDebugMessage(activeChar, "tradeoff currently enabled");
					}
					else
					{
						Functions.sendDebugMessage(activeChar, "tradeoff currently disabled");
					}
				}
				break;
			case admin_show_html:
				String html = wordList[1];
				try
				{
					if (html != null)
					{
						if (Config.GIVE_GM_SHOP_TO_ALL_PLAYERS && !html.startsWith("gmstore/") && (activeChar.getAccessLevel().getLevel() == 8))
						{
							return false;
						}
						
						activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
					}
					else
					{
						Functions.sendDebugMessage(activeChar, "Html page not found");
					}
				}
				catch (Exception npe)
				{
					Functions.sendDebugMessage(activeChar, "Html page not found");
				}
				break;
			case admin_setnpcstate:
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //setnpcstate state");
					return false;
				}
				int state;
				try
				{
					state = Integer.parseInt(wordList[1]);
				}
				catch (final NumberFormatException e)
				{
					Functions.sendDebugMessage(activeChar, "You must specify state");
					return false;
				}
				if (!target.isNpc())
				{
					Functions.sendDebugMessage(activeChar, "You must target an NPC");
					return false;
				}
				NpcInstance npc = (NpcInstance) target;
				npc.setNpcState(state);
				break;
			case admin_setareanpcstate:
				try
				{
					final String val = fullString.substring(15).trim();
					
					String[] vals = val.split(" ");
					int range = NumberUtils.toInt(vals[0], 0);
					int astate = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;
					
					for (NpcInstance n : activeChar.getAroundNpc(range, 200))
					{
						n.setNpcState(astate);
					}
				}
				catch (Exception e)
				{
					Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
				}
				break;
			case admin_showmovie:
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //showmovie id");
					return false;
				}
				int id;
				try
				{
					id = Integer.parseInt(wordList[1]);
				}
				catch (final NumberFormatException e)
				{
					Functions.sendDebugMessage(activeChar, "You must specify id");
					return false;
				}
				activeChar.showQuestMovie(id);
				break;
			case admin_setzoneinfo:
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo id");
					return false;
				}
				int stateid;
				try
				{
					stateid = Integer.parseInt(wordList[1]);
				}
				catch (final NumberFormatException e)
				{
					Functions.sendDebugMessage(activeChar, "You must specify id");
					return false;
				}
				activeChar.broadcastPacket(new ExChangeClientEffectInfo(0, 0, stateid));
				break;
			case admin_clienteffect:
				try
				{
					int type = Integer.parseInt(wordList[1]);
					int key = Integer.parseInt(wordList[2]);
					int value = Integer.parseInt(wordList[3]);
					
					activeChar.broadcastPacket(new ExChangeClientEffectInfo(type, key, value));
				}
				catch (final NumberFormatException e)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //clienteffect [0 changeZoneState | 1 setFog | 2 postEffectData] key value");
					return false;
				}
				break;
			case admin_eventtrigger:
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //eventtrigger id");
					return false;
				}
				int triggerid;
				try
				{
					triggerid = Integer.parseInt(wordList[1]);
				}
				catch (final NumberFormatException e)
				{
					Functions.sendDebugMessage(activeChar, "You must specify id");
					return false;
				}
				activeChar.broadcastPacket(new EventTrigger(triggerid, true));
				break;
			case admin_debug:
				GameObject ob = activeChar.getTarget();
				if (!ob.isPlayer())
				{
					Functions.sendDebugMessage(activeChar, "Only player target is allowed");
					return false;
				}
				Player pl = ob.getPlayer();
				List<String> _s = new ArrayList<>();
				_s.add("==========TARGET STATS:");
				_s.add("==Magic Resist: " + pl.calcStat(Stats.MAGIC_RESIST, null, null));
				_s.add("==Magic Power: " + pl.calcStat(Stats.MAGIC_POWER, 1, null, null));
				_s.add("==Skill Power: " + pl.calcStat(Stats.SKILL_POWER, 1, null, null));
				_s.add("==Cast Break Rate: " + pl.calcStat(Stats.CAST_INTERRUPT, 1, null, null));
				
				_s.add("==========Powers:");
				_s.add("==Bleed: " + pl.calcStat(Stats.BLEED_POWER, 1, null, null));
				_s.add("==Poison: " + pl.calcStat(Stats.POISON_POWER, 1, null, null));
				_s.add("==Stun: " + pl.calcStat(Stats.STUN_POWER, 1, null, null));
				_s.add("==Root: " + pl.calcStat(Stats.ROOT_POWER, 1, null, null));
				_s.add("==Mental: " + pl.calcStat(Stats.MENTAL_POWER, 1, null, null));
				_s.add("==Sleep: " + pl.calcStat(Stats.SLEEP_POWER, 1, null, null));
				_s.add("==Paralyze: " + pl.calcStat(Stats.PARALYZE_POWER, 1, null, null));
				_s.add("==Cancel: " + pl.calcStat(Stats.CANCEL_POWER, 1, null, null));
				_s.add("==Debuff: " + pl.calcStat(Stats.DEBUFF_POWER, 1, null, null));
				
				_s.add("==========PvP Stats:");
				_s.add("==Phys Attack Dmg: " + pl.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null));
				_s.add("==Phys Skill Dmg: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null));
				_s.add("==Magic Skill Dmg: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1, null, null));
				_s.add("==Phys Attack Def: " + pl.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null));
				_s.add("==Phys Skill Def: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null));
				_s.add("==Magic Skill Def: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null));
				
				_s.add("==========Reflects:");
				_s.add("==Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, null, null));
				_s.add("==Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, null, null));
				_s.add("==Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, null, null));
				_s.add("==Counterattack: Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_DAMAGE_PERCENT, null, null));
				_s.add("==Counterattack: Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, null, null));
				_s.add("==Counterattack: Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, null, null));
				
				_s.add("==========MP Consume Rate:");
				_s.add("==Magic Skills: " + pl.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 1, null, null));
				_s.add("==Phys Skills: " + pl.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 1, null, null));
				_s.add("==Music: " + pl.calcStat(Stats.MP_DANCE_SKILL_CONSUME, 1, null, null));
				
				_s.add("==========Shield:");
				_s.add("==Shield Defence: " + pl.calcStat(Stats.SHIELD_DEFENCE, null, null));
				_s.add("==Shield Defence Rate: " + pl.calcStat(Stats.SHIELD_RATE, null, null));
				_s.add("==Shield Defence Angle: " + pl.calcStat(Stats.SHIELD_ANGLE, null, null));
				
				_s.add("==========Etc:");
				_s.add("==Fatal Blow Rate: " + pl.calcStat(Stats.FATALBLOW_RATE, null, null));
				_s.add("==Phys Skill Evasion Rate: " + pl.calcStat(Stats.PSKILL_EVASION, null, null));
				_s.add("==Counterattack Rate: " + pl.calcStat(Stats.COUNTER_ATTACK, null, null));
				_s.add("==Pole Attack Angle: " + pl.calcStat(Stats.POLE_ATTACK_ANGLE, null, null));
				_s.add("==Pole Target Count: " + pl.calcStat(Stats.POLE_TARGET_COUNT, 1, null, null));
				_s.add("==========DONE.");
				
				for (String s : _s)
				{
					Functions.sendDebugMessage(activeChar, s);
				}
				break;
			case admin_uievent:
				if (wordList.length < 5)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
					return false;
				}
				boolean hide;
				boolean increase;
				int startTime;
				int endTime;
				String text;
				try
				{
					hide = Boolean.parseBoolean(wordList[1]);
					increase = Boolean.parseBoolean(wordList[2]);
					startTime = Integer.parseInt(wordList[3]);
					endTime = Integer.parseInt(wordList[4]);
					text = wordList[5];
				}
				catch (final NumberFormatException e)
				{
					Functions.sendDebugMessage(activeChar, "Invalid format");
					return false;
				}
				activeChar.broadcastPacket(new ExSendUIEvent(activeChar, hide, increase, startTime, endTime, text));
				break;
			case admin_opensod:
				if (wordList.length < 1)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //opensod minutes");
					return false;
				}
				SoDManager.openSeed(Integer.parseInt(wordList[1]) * 60 * 1000L);
				break;
			case admin_closesod:
				SoDManager.closeSeed();
				break;
			case admin_setsoistage:
				if (wordList.length < 1)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //setsoistage stage[1-5]");
					return false;
				}
				SoIManager.setCurrentStage(Integer.parseInt(wordList[1]));
				break;
			case admin_soinotify:
				if (wordList.length < 1)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //soinotify [1-3]");
					return false;
				}
				switch (Integer.parseInt(wordList[1]))
				{
					case 1:
						SoIManager.notifyCohemenesKill();
						break;
					case 2:
						SoIManager.notifyEkimusKill();
						break;
					case 3:
						SoIManager.notifyHoEDefSuccess();
						break;
				}
				break;
			case admin_forcenpcinfo:
				GameObject obj2 = activeChar.getTarget();
				if (!obj2.isNpc())
				{
					Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
					return false;
				}
				((NpcInstance) obj2).broadcastCharInfo();
				break;
			case admin_loc:
				Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
				break;
			case admin_zonesize:
				cylindric(activeChar.getLoc());
				break;
			
			case admin_loc2:
				
				String loc_id = "0";
				String loc_name;
				if (wordList.length > 1)
				{
					loc_id = wordList[1];
				}
				if (wordList.length > 2)
				{
					loc_name = wordList[2];
				}
				else
				{
					loc_name = "loc_" + loc_id;
				}
				System.out.println("	(" + loc_id + ",'" + loc_name + "'," + activeChar.getX() + "," + activeChar.getY() + "," + activeChar.getZ() + "," + (activeChar.getZ() + 100) + ",0),");
				activeChar.sendMessage("Point saved.");
				ItemInstance temp = ItemFunctions.createItem(1060);
				temp.dropMe(activeChar, activeChar.getLoc());
				break;
			
			case admin_locdump:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					try
					{
						new File("dumps").mkdir();
						File f = new File("dumps/locdump.txt");
						if (!f.exists())
						{
							f.createNewFile();
						}
						Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
						FileWriter writer = new FileWriter(f, true);
						writer.write("Loc: " + activeChar.getLoc().x + ", " + activeChar.getLoc().y + ", " + activeChar.getLoc().z + "\n");
						writer.close();
					}
					catch (Exception e)
					{
						
					}
				}
				catch (Exception e)
				{
					// Case of wrong monster data
				}
				break;
			case admin_immortal:
				activeChar.setIsImmortal(!activeChar.isImmortal());
				if (activeChar.isImmortal())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message19", activeChar));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message20", activeChar));
				}
				break;
			case admin_set_immortal:
				if (wordList.length > 1)
				{
					target = World.getPlayer(wordList[1]);
				}
				if ((target != null) && target.isCreature())
				{
					((Creature) target).setIsImmortal(!((Creature) target).isImmortal());
				}
				
				if (((Creature) target).isImmortal())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message21", activeChar));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message22", activeChar));
				}
				break;
			case admin_checkadena:
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message1", activeChar).addNumber(GameStats.getAdena()));
				break;
			case admin_announcepoll:
				if (Config.ENABLE_POLL_SYSTEM)
				{
					for (Player player : GameObjectsStorage.getAllPlayersForIterate())
					{
						if ((player != null) && VoteManager.getInstance().pollisActive() && VoteManager.getInstance().canVote(player.getHWID()))
						{
							if (!player.isInOlympiadMode() || !player.isInOfflineMode())
							{
								askForPoll(player);
							}
						}
					}
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message2", activeChar));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message3", activeChar));
				}
				break;
			case admin_donatepromotion:
				if (ServerVariables.getBool("DonationBonusActive", true))
				{
					DonationBonusDay.getInstance().stopPromotion();
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message4", activeChar));
				}
				else
				{
					if (wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //admin_donatepromotion <hours>");
						return false;
					}
					
					int hours = 0;
					try
					{
						hours = Integer.valueOf(wordList[1]);
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message5", activeChar));
						return false;
					}
					
					DonationBonusDay.getInstance().startPromotion(hours);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message6", activeChar).addNumber(hours));
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/events.htm"));
				}
				break;
			case admin_donatepromotionpercent:
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //donatepromotionpercent <number>");
					return false;
				}
				int percent = 0;
				try
				{
					percent = Integer.valueOf(wordList[1]);
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message7", activeChar));
					return false;
				}
				ServerVariables.set("DonationBonusPercent", percent);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message8", activeChar).addNumber(percent));
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/events.htm"));
				break;
			case admin_getemail:
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //getemail <accountname>");
					return false;
				}
				String accountName = "";
				try
				{
					accountName = wordList[1];
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message9", activeChar));
					return false;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message10", activeChar).addString(accountName).addString(CharacterIntro.getEmail(accountName)));
				break;
			case admin_ready:
				try
				{
					String[] message =
					{
						activeChar.getName() + " is now available via Petition System.",
						activeChar.getName() + " теперь доступна через Петиция системе."
					};
					
					Announcements.getInstance().announceToAll(message, ChatType.SCREEN_ANNOUNCE);
					
					for (Player player : GameObjectsStorage.getAllPlayers())
					{
						if ((player == null) || player.isInOfflineMode())
						{
							continue;
						}
						
						if (player.getLanguage() == Language.RUSSIAN)
						{
							player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "Система", activeChar.getName() + " теперь онлайн. Если у вас есть проблемы/вопроса пожалуйста свяжитесь с ним через Петиция системе.");
						}
						else
						{
							player.sendChatMessage(player.getObjectId(), ChatType.TELL.ordinal(), "System", activeChar.getName() + " is now online. If you have problem/issue please contact him via petition.");
						}
						
					}
					
					/*
					 * if(activeChar.isInvisible()) I'd like to petition while invis. { activeChar.setInvisibleType(InvisibleType.NONE); activeChar.broadcastCharInfo(); if(activeChar.getPet() != null) activeChar.getPet().broadcastCharInfo(); }
					 */
					
					AdminTable.getInstance().showGm(activeChar);
					
					PetitionManager.getInstance().sendPendingPetitionList(activeChar);
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message15", activeChar));
					return false;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message16", activeChar));
				break;
			case admin_gmon:
			case admin_gmliston:
				AdminTable.getInstance().showGm(activeChar);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message17", activeChar));
				break;
			case admin_gmoff:
			case admin_gmlistoff:
				AdminTable.getInstance().hideGm(activeChar);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message18", activeChar));
				break;
			case admin_sm:
				getServerInfo(activeChar, 1);
				break;
			case admin_smpage:
			{
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //sm <page>");
					return false;
				}
				int page = 0;
				try
				{
					page = Integer.valueOf(wordList[1]);
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message9", activeChar));
					return false;
				}
				getServerInfo(activeChar, page);
			}
				break;
			case admin_trivia:
			{
				String customHtm = HtmCache.getInstance().getNotNull("admin/events/trivia.htm", activeChar);
				
				NpcHtmlMessage html1 = new NpcHtmlMessage(0);
				html1.setHtml(customHtm);
				activeChar.sendPacket(html1);
			}
				break;
			case admin_startcancelsystem:
			{
				CancelSystem.getInstance().startCancelSystem();
				activeChar.sendMessage("Cancel System Started.");
			}
				break;
			case admin_stopcancelsystem:
			{
				CancelSystem.getInstance().stopCancelSystem();
				activeChar.sendMessage("Cancel System stopped.");
			}
				break;
			case admin_removeauction:
			{
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //removeauction <auctionID>");
					return false;
				}
				
				int aucID = 0;
				try
				{
					aucID = Integer.valueOf(wordList[1]);
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message9", activeChar));
					return false;
				}
				AuctionManager.removeAuction(null, aucID, 0, true);
			}
				break;
			case admin_promotionlevelup:
			{
				if (wordList.length < 2)
				{
					Functions.sendDebugMessage(activeChar, "USAGE: //promotionlevelup <leveltoset>");
					return false;
				}
				
				int leveltoset = 0;
				try
				{
					leveltoset = Integer.valueOf(wordList[1]);
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message9", activeChar));
					return false;
				}
				ServerVariables.set("PromotionLevelUP", leveltoset);
				activeChar.sendChatMessage(-1, ChatType.SHOUT.ordinal(), "System", "Promotion for Level up has been set to " + leveltoset);
			}
				break;
			default:
				break;
		}
		return true;
	}
	
	private void getServerInfo(Player player, int page)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder sb = new StringBuilder("<html><body>");
		int[] stats = World.getStats();
		int TotalOnline = GameObjectsStorage.getAllPlayersCount();
		int Online = GameObjectsStorage.getAllPlayersCount() - GameObjectsStorage.getAllOfflineCount(true);
		int OfflineShop = GameObjectsStorage.getAllOfflineCount(false);
		
		if (page == 1)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"#\" width=70 height=21 back=\"L2UI_ct11button_df\" fore=\"L2UI_ct11button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"bypass -h admin_smpage 2\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=292><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292>");
			sb.append("<tr>");
			sb.append("<td width=292>");
			sb.append("<font color=LEVEL>Server Status: </font>").append("<br1>");
			sb.append("Players:.... ").append(TotalOnline).append("/").append(Config.MAXIMUM_ONLINE_USERS).append("<br1>");
			sb.append("&nbsp; &nbsp;Online: ").append(Online).append("<br1>");
			sb.append("&nbsp; &nbsp;Offline: ").append(OfflineShop).append("<br1>");
			sb.append("GM Online: ").append(AdminTable.getAllGMs().length).append("<br1>");
			sb.append("------------------------------------------<br1>");
			sb.append("Objects:.... ").append(stats[10]).append("<br1>");
			sb.append("Characters:. ").append(stats[11]).append("<br1>");
			sb.append("Summons:.... ").append(stats[18]).append("<br1>");
			sb.append("Npcs:....... ").append(stats[15]).append("/").append(stats[14]).append("<br1>");
			sb.append("Monsters:... ").append(stats[16]).append("<br1>");
			sb.append("Minions:.... ").append(stats[17]).append("<br1>");
			sb.append("Doors:...... ").append(stats[19]).append("<br1>");
			sb.append("Items:...... ").append(stats[20]).append("<br1>");
			sb.append("------------------------------------------<br1>");
			sb.append("Reflections: ").append(ReflectionManager.getInstance().getAll().length).append("<br1>");
			sb.append("Regions:.... ").append(stats[0]).append("<br1>");
			sb.append("Active:..... ").append(stats[1]).append("<br1>");
			sb.append("Inactive:... ").append(stats[2]).append("<br1>");
			sb.append("Null:....... ").append(stats[3]).append("<br1>");
			sb.append("------------------------------------------<br1>");
			sb.append("Uptime:..... ").append(Util.formatTime(GameServer.getInstance().uptime())).append("<br1>");
			sb.append("Shutdown:... ").append(Util.formatTime(Shutdown.getInstance().getSeconds())).append("/").append(Shutdown.getInstance().getMode()).append("<br1>");
			sb.append("------------------------------------------<br1>");
			sb.append("Threads:.... ").append(Thread.activeCount()).append("<br1>");
			sb.append("RAM Used:... ").append(StatsUtils.getMemUsedMb()).append("<br1>");
			sb.append("------------------------------------------<br1>");
			sb.append("Total Adena: ").append(Util.formatAdena(GameStats.getAdena())).append("<br1>");
			sb.append("</td></tr></table>");
			sb.append("<br>");
		}
		else if (page == 2)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"#\" width=60 height=21 back=\"L2UI_ct1.L2UI_ct11button_df\" fore=\"L2UI_ct1.L2UI_ct11button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=290><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 2\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292><tr><td>");
			sb.append("<font color=LEVEL>Memory Status: </font>").append("<br1>");
			sb.append(StatsUtils.getMemUsageHtml().toString());
			sb.append("</td></tr></table>");
			sb.append("<br>");
		}
		else if (page == 3)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"bypass -h admin_smpage 2\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"#\" width=70 height=21 back=\"L2UI_ct1.L2UI_ct11button_df\" fore=\"L2UI_ct1.L2UI_ct11button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=292><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292><tr><td>");
			sb.append("<font color=LEVEL>Thread Status: </font>").append("<br1>");
			sb.append(StatsUtils.getThreadStatsHtml().toString());
			sb.append("</td></tr></table>");
			sb.append("<br>");
		}
		else if (page == 4)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"bypass -h admin_smpage 2\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"#\" width=70 height=21 back=\"L2UI_ct1.L2UI_ct11button_df\" fore=\"L2UI_ct1.L2UI_ct11button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.L2UI_button_dfct1\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=292><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292><tr><td>");
			sb.append("<font color=LEVEL>Garbage Collector Status: </font>").append("<br1>");
			sb.append(StatsUtils.getGCStatsHtml().toString());
			sb.append("</td></tr></table>");
			sb.append("<br>");
			
		}
		else if (page == 5)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"bypass -h admin_smpage 2\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"#\" width=70 height=21 back=\"L2UI_ct1.L2UI_ct11button_df\" fore=\"L2UI_ct1.L2UI_ct11button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=292><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292><tr><td>");
			sb.append("<font color=LEVEL>ThreadPool Status: </font>").append("<br1>");
			sb.append(ThreadPoolManager.getInstance().getStats().toString());
			sb.append("</td></tr></table>");
			sb.append("<br>");
			
		}
		else if (page == 6)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"bypass -h admin_smpage 2\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"#\" width=70 height=21 back=\"L2UI_ct1.L2UI_ct11button_df\" fore=\"L2UI_ct1.L2UI_ct11button_df\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=292><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292><tr><td>");
			sb.append("<font color=LEVEL>Game Storage Status: </font>").append("<br1>");
			sb.append(GameObjectsStorage.getHtmlStats().toString());
			sb.append("</td></tr></table>");
			sb.append("<br>");
			
		}
		else if (page == 7)
		{
			sb.append("<center><table width=292>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"General\" action=\"bypass -h admin_smpage 1\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=60><button value=\"RAM\" action=\"bypass -h admin_smpage 2\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Threads\" action=\"bypass -h admin_smpage 3\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Garbage\" action=\"bypass -h admin_smpage 4\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=70><button value=\"Thread.Task\" action=\"bypass -h admin_smpage 5\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"Storage\" action=\"bypass -h admin_smpage 6\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td width=70><button value=\"AiTask\" action=\"#\" width=70 height=21 back=\"L2UI_ct1.L2UI_ct11button_df\" fore=\"L2UI_ct1.L2UI_ct11button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table></center>");
			sb.append("<br>");
			
			sb.append("<center><table width=292><tr>");
			sb.append("<td align=center><button value=\"Refresh\" action=\"bypass -h admin_smpage 7\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table></center><br>");
			
			sb.append("<table width=292><tr><td>");
			sb.append("<font color=LEVEL>AiTask Manager Status: </font>").append("<br1>");
			sb.append("Disabled Check ThreadPoolManager.<br1>");
			sb.append("Or call trough telnet AiTaskManager.getInstance().getStats()");
			sb.append("</td></tr></table>");
			sb.append("<br>");
			
		}
		else
		{
			sb.append("<br><br><center><font color=LEVEL>Page not found.</font></center>");
		}
		
		sb.append("</body></html>");
		adminReply.setHtml(sb.toString());
		player.sendPacket(adminReply);
	}
	
	private static void askForPoll(final Player activeChar)
	{
		if (activeChar != null)
		{
			activeChar.ask(new ConfirmDlg(SystemMsg.S1, 10000).addString("Do you wanna participate in our poll?"), new OnAnswerListener()
			{
				@Override
				public void sayYes()
				{
					VoteManager.getInstance().sendPoll(activeChar);
				}
				
				@Override
				public void sayNo()
				{
				}
			});
		}
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	public void playAdminSound(Player activeChar, String sound)
	{
		activeChar.broadcastPacket(new PlaySound(sound));
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
		activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminadmin.message14", activeChar).addString(sound));
	}
	
	private List<Location> cylindric(Location loc)
	{
		int ii = 5;
		int radius = 500;
		int perStep = 50;
		int lx = loc.x;
		int ly = loc.y;
		int lz = loc.z;
		
		List<Location> locs = new ArrayList<>();
		int count = (int) ((2 * Math.PI * radius) / perStep);
		double angle = (2 * Math.PI) / count;
		for (int i = 0; i < count; i++)
		{
			int x = (int) (Math.cos(angle * i) * radius);
			int y = (int) (Math.sin(angle * i) * radius);
			locs.add(new Location(lx + x, ly + y, lz));
			if ((ii % 2) == 0)
			{
				LOG.info("29\t" + ii + "\t" + (lx + x) + "\t" + (ly + y) + "\t" + lz + "\t" + 2 + "\tRegular" + "\t300");
			}
			else if ((ii % 3) == 0)
			{
				LOG.info("29\t" + ii + "\t" + (lx - x) + "\t" + (ly - y) + "\t" + lz + "\t" + 1 + "\tRegular" + "\t300");
				
			}
			else
			{
				LOG.info("29\t" + ii + "\t" + (lx - x) + "\t" + (ly - y) + "\t" + lz + "\t" + 3 + "\tRegular" + "\t300");
			}
			ii++;
		}
		
		LOG.info(locs.size() + " size !");
		
		return locs;
	}
}