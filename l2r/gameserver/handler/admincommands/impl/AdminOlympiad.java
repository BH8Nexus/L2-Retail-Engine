package l2r.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2r.gameserver.model.entity.olympiad.OlympiadEndTask;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.model.entity.olympiad.ValidationTask;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.templates.StatsSet;


public class AdminOlympiad implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_oly_save,
		admin_add_oly_points,
		admin_oly_start,
		admin_add_hero,
		admin_oly_stop,
		admin_olympiad_stop_period,
		admin_olympiad_start_period
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		//if (activeChar.getPlayerAccess().CanGmEdit)

		switch (command)
		{
			case admin_oly_save:
			{
				if (!Config.ENABLE_OLYMPIAD)
					return false;

				try
				{
					OlympiadDatabase.save();
				}
				catch (Exception e)
				{

				}
				activeChar.sendMessage("olympaid data saved.");
				break;
			}
			case admin_add_oly_points:
			{
				if (wordList.length < 3)
				{
					activeChar.sendMessage("Command syntax: //add_oly_points <char_name> <point_to_add>");
					activeChar.sendMessage("This command can be applied only for online players.");
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if (player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}

				int pointToAdd;

				try
				{
					pointToAdd = Integer.parseInt(wordList[2]);
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage("Please specify integer value for olympiad points.");
					return false;
				}

				int curPoints = Olympiad.getNoblePoints(player.getObjectId());
				Olympiad.manualSetNoblePoints(player.getObjectId(), curPoints + pointToAdd);
				int newPoints = Olympiad.getNoblePoints(player.getObjectId());

				activeChar.sendMessage("Added " + pointToAdd + " points to character " + player.getName());
				activeChar.sendMessage("Old points: " + curPoints + ", new points: " + newPoints);
				break;
			}
			case admin_oly_start:
			{
				Olympiad._manager = new OlympiadManager();
				Olympiad._inCompPeriod = true;

				new Thread(Olympiad._manager).start();

				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_STARTED));
				break;
			}
			case admin_oly_stop:
			{
				Olympiad._inCompPeriod = false;
				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_ENDED));
				try
				{
					OlympiadDatabase.save();
				}
				catch (Exception e)
				{

				}

				break;
			}
			case admin_add_hero:
			{
				if (wordList.length < 2)
				{
					activeChar.sendMessage("Command syntax: //add_hero <char_name>");
					activeChar.sendMessage("This command can be applied only for online players.");
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if (player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}

				StatsSet hero = new StatsSet();
				hero.set(Olympiad.CLASS_ID, player.getBaseClassId());
				hero.set(Olympiad.CHAR_ID, player.getObjectId());
				hero.set(Olympiad.CHAR_NAME, player.getName());

				List<StatsSet> heroesToBe = new ArrayList<StatsSet>();
				heroesToBe.add(hero);

				Hero.getInstance().computeNewHeroes(heroesToBe);

				activeChar.sendMessage("Hero status added to player " + player.getName());
				break;
			}
			case admin_olympiad_stop_period:
			{
				Olympiad.cancelPeriodTasks();
				ThreadPoolManager.getInstance().execute(new OlympiadEndTask());
				break;
			}
			case admin_olympiad_start_period:
			{
				Olympiad.cancelPeriodTasks();
				ThreadPoolManager.getInstance().execute(new ValidationTask());
				break;
			}
		}

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
//package l2r.gameserver.handler.admincommands.impl;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.lang3.math.NumberUtils;
//
//import l2r.gameserver.Announcements;
//import l2r.gameserver.Config;
//import l2r.gameserver.ThreadPoolManager;
//import l2r.gameserver.dao.CharacterDAO;
//import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
//import l2r.gameserver.model.Player;
//import l2r.gameserver.model.World;
//import l2r.gameserver.model.entity.Hero;
//import l2r.gameserver.model.entity.olympiad.CompEndTask;
//import l2r.gameserver.model.entity.olympiad.CompStartTask;
//import l2r.gameserver.model.entity.olympiad.CompType;
//import l2r.gameserver.model.entity.olympiad.Olympiad;
//import l2r.gameserver.model.entity.olympiad.OlympiadDatabase;
//import l2r.gameserver.model.entity.olympiad.OlympiadGame;
//import l2r.gameserver.model.entity.olympiad.OlympiadManager;
//import l2r.gameserver.network.serverpackets.SystemMessage;
//import l2r.gameserver.network.serverpackets.components.CustomMessage;
//import l2r.gameserver.network.serverpackets.components.SystemMsg;
//import l2r.gameserver.templates.StatsSet;
//
//public class AdminOlympiad implements IAdminCommandHandler
//{
//	private static enum Commands
//	{
//		admin_oly_save,
//		admin_oly_addpoints,
//		admin_oly_start,
//		admin_oly_setpoints,
//		admin_oly_addhero,
//		admin_oly_delhero,
//		admin_oly_stop,
//		admin_oly_end,
//		admin_oly_reg,
//		admin_oly_register,
//		admin_oly_unreg,
//		admin_oly_unregister,
//		admin_oly_stat,
//		admin_oly_skip,
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see wp.gameserver.handler.admincommands.IAdminCommandHandler#useAdminCommand(java.lang.Enum, java.lang.String[], java.lang.String, wp.gameserver.model.Player)
//	 */
//	@Override
//	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
//	{
//		Commands command = (Commands) comm;
//		
//		switch (command)
//		{
//			case admin_oly_save:
//			{
//				if (!Config.ENABLE_OLYMPIAD)
//				{
//					return false;
//				}
//				
//				activeChar.sendMessage("Saving olympiad database:");
//				try
//				{
//					OlympiadDatabase.save();
//					activeChar.sendMessage("Noblese data saved.");
//					activeChar.sendMessage("Current Cycle: " + Olympiad._currentCycle);
//					activeChar.sendMessage("Olympiad Period: " + Olympiad._period);
//					activeChar.sendMessage("Olympiad End: " + new Date(Olympiad._olympiadEnd));
//					activeChar.sendMessage("Validation End: " + new Date(Olympiad._validationEnd));
//					activeChar.sendMessage("Next Weekly Change: " + new Date(Olympiad._nextWeeklyChange));
//				}
//				catch (Exception e)
//				{
//					
//				}
//				break;
//			}
//			case admin_oly_addpoints:
//			{
//				if (wordList.length < 3)
//				{
//					activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message2", activeChar));
//					activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message3", activeChar));
//					return false;
//				}
//				
//				Player player = World.getPlayer(wordList[1]);
//				if (player == null)
//				{
//					activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message4", activeChar).addString(wordList[1]));
//					return false;
//				}
//				
//				int pointToAdd;
//				
//				try
//				{
//					pointToAdd = Integer.parseInt(wordList[2]);
//				}
//				catch (NumberFormatException e)
//				{
//					activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message5", activeChar));
//					return false;
//				}
//				
//				int curPoints = Olympiad.getNoblePoints(player.getObjectId());
//				Olympiad.manualSetNoblePoints(player.getObjectId(), curPoints + pointToAdd);
//				int newPoints = Olympiad.getNoblePoints(player.getObjectId());
//				
//				activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message6", activeChar).addNumber(pointToAdd).addString(player.getName()));
//				activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message7", activeChar).addNumber(curPoints).addNumber(newPoints));
//				break;
//			}
//			case admin_oly_setpoints:
//			{
//				Player player = (activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer() ? null : activeChar.getTarget().getPlayer();
//				if ((player == null) && (wordList.length < 3))
//				{
//					activeChar.sendMessage("Select a targer player or type player name in command.");
//					return false;
//				}
//				else if (player == null)
//				{
//					player = World.getPlayer(wordList[1]);
//				}
//				
//				if (player == null)
//				{
//					activeChar.sendMessage("Player " + wordList[1] + " not found.");
//					return false;
//				}
//				
//				int oldPoints = Olympiad.getNoblePoints(player.getObjectId());
//				int pointsToSet = NumberUtils.toInt(wordList[2], oldPoints);
//				activeChar.sendMessage(player.getName() + "'s olympiad points have changed from " + oldPoints + " to " + pointsToSet);
//				player.sendMessage("Your olympiad points have changed from " + oldPoints + " to " + pointsToSet);
//				break;
//			}
//			case admin_oly_start:
//			{
//				Olympiad._manager = new OlympiadManager();
//				Olympiad._inCompPeriod = true;
//
//				new Thread(Olympiad._manager).start();
//
//				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_STARTED));
//				break;
//			}
////				if (Olympiad._inCompPeriod)
////				{
////					activeChar.sendMessage("Olympiad competition is already active.");
////					return false;
////				}
////				activeChar.sendMessage("Olympiad competition started.");
////				Olympiad._scheduledComeptitionStartTask = ThreadPoolManager.getInstance().schedule(new CompStartTask(), 1000);
////				break;
////			}
//			case admin_oly_stop:
//			{
//				Olympiad._inCompPeriod = false;
//				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_ENDED));
//				try
//				{
//					OlympiadDatabase.save();
//				}
//				catch (Exception e)
//				{
//
//				}
//
//				break;
////				activeChar.sendMessage("Olympiad competition has been forced to stop.");
////				ThreadPoolManager.getInstance().execute(new CompEndTask());
////				break;
//			}
//			case admin_oly_addhero:
//			{
//				Player player;
//				if (wordList.length < 2)
//				{
//					player = (activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer() ? null : activeChar.getTarget().getPlayer();
//				}
//				else
//				{
//					player = World.getPlayer(wordList[1]);
//				}
//				
//				if (player == null)
//				{
//					String playerName = wordList.length < 2 ? "Selected Target" : wordList[1];
//					activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message10", activeChar).addString(playerName));
//					return false;
//				}
//				else if (Hero.getInstance().isHero(player.getObjectId()))
//				{
//					activeChar.sendMessage(player.getName() + " is already a hero.");
//					return false;
//				}
//				
//				StatsSet hero = new StatsSet();
//				hero.set(Olympiad.CLASS_ID, player.getBaseClassId());
//				hero.set(Olympiad.CHAR_ID, player.getObjectId());
//				hero.set(Olympiad.CHAR_NAME, player.getName());
//				
//				List<StatsSet> heroesToBe = new ArrayList<>();
//				heroesToBe.add(hero);
//				
//				Hero.getInstance().computeNewHeroes(heroesToBe);
//				
//				player.updatePledgeClass();
//				player.broadcastUserInfo(true);
//				
//				activeChar.sendMessage(new CustomMessage("wp.gameserver.handler.admincommands.impl.adminolympiad.message11", activeChar).addString(player.getName()));
//				break;
//			}
//			case admin_oly_delhero:
//			{
//				int targetId = 0;
//				if (wordList.length < 2)
//				{
//					targetId = activeChar.getTargetId();
//				}
//				else
//				{
//					targetId = CharacterDAO.getInstance().getObjectIdByName(wordList[1]);
//				}
//				
//				String playerName = wordList.length < 2 ? "Selected Target" : wordList[1];
//				if (!Hero.getInstance().isHero(targetId))
//				{
//					activeChar.sendMessage(playerName + " is not a hero.");
//					return false;
//				}
//				
//				Hero.deleteHero(targetId);
//				Player player = World.getPlayer(targetId);
//				if (player != null)
//				{
//					player.updatePledgeClass();
//					player.broadcastUserInfo(true);
//					player.unsetVar("HeroPeriod");
//					Hero.removeSkills(player);
//				}
//				
//				activeChar.sendMessage((player != null ? player.getName() : playerName) + " is no longer a hero.");
//				break;
//			}
//			case admin_oly_end:
//			{
//				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminolympiad.message12", activeChar));
//				OlympiadDatabase.sortHerosToBe();
//				OlympiadDatabase.saveNobleData();
//				if (Hero.getInstance().computeNewHeroes(Olympiad._heroesToBe))
//				{
//					Olympiad._log.warn("Olympiad: Error while computing new heroes!");
//				}
//				Announcements.getInstance().announceToAll("Olympiad Validation Period has ended.");
//				Olympiad._period = 0;
//				Olympiad._currentCycle++;
//				OlympiadDatabase.cleanupNobles();
//				OlympiadDatabase.loadNoblesRank();
//				OlympiadDatabase.setNewOlympiadEnd();
//				Olympiad.init();
//				OlympiadDatabase.save();
//				break;
//			}
//			case admin_oly_reg:
//			case admin_oly_register:
//			{
//				if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
//				{
//					activeChar.sendMessage("Please target a player.");
//					return false;
//				}
//				
//				Player noble = activeChar.getTarget().getPlayer();
//				if (!Olympiad.isNoble(noble.getObjectId()))
//				{
//					Olympiad.addNoble(noble);
//				}
//				
//				Olympiad.registerNoble(noble, CompType.NON_CLASSED);
//				activeChar.sendMessage(noble.getName() + " has been registered for non-class competative olympiad match.");
//				noble.sendPacket(SystemMsg.YOU_HAVE_BEEN_REGISTERED_FOR_THE_GRAND_OLYMPIAD_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH);
//				break;
//			}
//		}
////			case admin_oly_unreg:
////			case admin_oly_unregister:
////			{
////				if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
////				{
////					activeChar.sendMessage("Please target a player.");
////					return false;
////				}
////				
////				Player noble = activeChar.getTarget().getPlayer();
////				Olympiad.removeNoble(noble);
////				OlympiadGame game = noble.getOlympiadGame();
////				if (game != null)
////				{
////					activeChar.sendMessage(game.getGameStatus());
////					noble.getOlympiadGame().endGame(0, true);
////				}
////				
////				activeChar.sendMessage(noble.getName() + " has been removed from olympiad match.");
////				if (game != null)
////				{
////					activeChar.sendMessage(game.getGameStatus());
////				}
////				break;
////			}
////			case admin_oly_stat:
////			{
////				activeChar.sendMessage("Olympiad System: Current Cycle: " + Olympiad._currentCycle);
////				activeChar.sendMessage("Olympiad System: Olympiad Period: " + Olympiad._period);
////				activeChar.sendMessage("Olympiad System: Olympiad End: " + new Date(Olympiad._olympiadEnd));
////				activeChar.sendMessage("Olympiad System: Validation End: " + new Date(Olympiad._validationEnd));
////				activeChar.sendMessage("Olympiad System: Next Weekly Change: " + new Date(Olympiad._nextWeeklyChange));
////				
////				if (Olympiad._period == 0)
////				{
////					activeChar.sendMessage("Olympiad System: Currently in Olympiad Period");
////				}
////				else
////				{
////					activeChar.sendMessage("Olympiad System: Currently in Validation Period");
////				}
////				
////				activeChar.sendMessage("Olympiad System: Period Ends....");
////				activeChar.sendMessage(Olympiad.getPeriodEndLog());
////				activeChar.sendMessage("Olympiad System: Next Weekly Change is in....");
////				activeChar.sendMessage(Olympiad.getWeeklyChangeTimeLog());
////				
////				if (Olympiad._manager != null)
////				{
////					activeChar.sendMessage("Olympiad System: Current Battles: ");
////					for (OlympiadGame game : Olympiad._manager.getOlympiadGames().values())
////					{
////						if (game != null)
////						{
////							activeChar.sendMessage(game.getGameStatus());
////						}
////					}
////				}
////				
////				break;
////			}
////			case admin_oly_skip:
////			{
////				if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
////				{
////					activeChar.sendMessage("Please target a player.");
////					return false;
////				}
////				
////				Player noble = activeChar.getTarget().getPlayer();
////				OlympiadGame game = noble.getOlympiadGame();
////				if (game != null)
////				{
////					//game.skipCountdownAndPortPlayers();
////					activeChar.sendMessage("Olympiad match time skipped and players will be teleported to the arena.");
////				}
////				else
////				{
////					activeChar.sendMessage("Player is not in an olympiad game.");
////					return false;
////				}
////				
////				break;
////			}
////		}
////		
//		return true;
//	}
//	
//	@Override
//	public Enum<?>[] getAdminCommandEnum()
//	{
//		return Commands.values();
//	}
//}