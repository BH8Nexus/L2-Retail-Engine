package l2r.gameserver.handler.admincommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangeAccessLevel;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.utils.AutoBan;

public class AdminCheckBot implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminCheckBot.class);
	
	private static enum Commands
	{
		admin_checkbots,
		admin_readbot,
		admin_markbotreaded,
		admin_punish_bot
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		if (!Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.notenabled", activeChar));
			return false;
		}
		
		Commands command = (Commands) comm;
		
		String[] ids = fullString.split(" ");
		
		switch (command)
		{
			case admin_checkbots:
				sendBotPage(activeChar);
				break;
			case admin_readbot:
				sendBotInfoPage(activeChar, Integer.parseInt(ids[1]));
				break;
			case admin_markbotreaded:
			{
				try
				{
					AutoHuntingManager.getInstance().markAsRead(Integer.parseInt(wordList[1]));
					sendBotPage(activeChar);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_punish_bot:
			{
				if (wordList != null)
				{
					Player target = GameObjectsStorage.getPlayer(wordList[1]);
					if (target != null)
					{
						synchronized (target)
						{
							if (wordList[2].equalsIgnoreCase("CHATBAN"))
							{
								int punishTime = Integer.valueOf(wordList[3]);
								if (punishTime != 0)
								{
									AutoBan.ChatBan(target.getName(), punishTime, "Handled by bot report system.", activeChar.getName());
									target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.target_chatbanned", target).addNumber(punishTime));
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.you_chatbanned", activeChar).addString(target.getName()).addNumber(punishTime));
									introduceNewPunishedBotAndClear(target);
								}
								else
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.error", activeChar));
								}
							}
							else if (wordList[2].equalsIgnoreCase("KICK"))
							{
								target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.kick", target));
								target.kick();
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.kicked", activeChar).addString(target.getName()));
								introduceNewPunishedBotAndClear(target);
							}
							else if (wordList[2].equalsIgnoreCase("BANCHAR"))
							{
								int punishTime = Integer.valueOf(wordList[3]);
								if (punishTime != 0)
								{
									target.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.YoureBannedByGM", target));
									AutoBan.Banned(target.getName(), -100, punishTime, "Handled by botreport system.", activeChar.getName());
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.banned", activeChar).addString(target.getName()).addNumber(punishTime));
									target.kick();
									introduceNewPunishedBotAndClear(target);
								}
								else
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.error", activeChar));
								}
							}
							else if (wordList[2].equalsIgnoreCase("BANACC"))
							{
								String account = target.getAccountName();
								if (account != null)
								{
									AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, -100, 0));
									GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
									if (client != null)
									{
										Player player = client.getActiveChar();
										if (player != null)
										{
											player.kick();
											activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.banned1", activeChar).addString(target.getName()).addString(target.getAccountName()));
											introduceNewPunishedBotAndClear(target);
										}
									}
								}
								else
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.error", activeChar));
								}
							}
						}
					}
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admincheckbot.doesnotexist", activeChar));
					}
				}
			}
		}
		return true;
	}
	
	private static void sendBotPage(Player activeChar)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><table width=260>");
		tb.append("<tr>");
		tb.append("<td width=40>");
		tb.append("<a action=\"bypass -h admin_admin\">Main</a>");
		tb.append("</td>");
		tb.append("<td width=180>");
		tb.append("<center>Bot Report's info</center>");
		tb.append("</td>");
		tb.append("<td width=40>");
		tb.append("<a action=\"bypass -h admin_admin\">Back</a>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<title>Unread Bot List</title><body><center>");
		tb.append("Here's a list of the current <font color=LEVEL>unread</font><br1>bots!<br>");
		
		for (int i : AutoHuntingManager.getInstance().getUnread().keySet())
		{
			tb.append("<a action=\"bypass -h admin_readbot " + i + "\">Ticket #" + i + "</a><br1>");
			
		}
		tb.append("</center></body></html>");
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	private static void sendBotInfoPage(Player activeChar, int botId)
	{
		String[] report = AutoHuntingManager.getInstance().getUnread().get(botId);
		TextBuilder tb = new TextBuilder();
		
		Player reportedTarget = World.getPlayer(report[0]);
		int punishTimes = AutoHuntingManager.getInstance().getPlayerReportsCount(reportedTarget);
		
		if (reportedTarget == null)
		{
			tb.append("<html><title>Bot #" + botId + "</title><body><center><br>");
			tb.append("- Bot report ticket Id: <font color=2554C7>" + botId + "</font><br1>");
			tb.append("- Player reported: <font color=\"FFA500\">" + report[0] + " (Offline)</font><br1>");
			tb.append("- Reported by: <font color=FF0000>" + report[1] + "</font><br1>");
			tb.append("- Reported Times: <font color =6CBB3C>" + punishTimes + "</font><br1>");
			tb.append("- Date: <font color=FF0000>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Long.parseLong(report[2])) + "</font><br>");
			tb.append("- Type: <font color=FFFF00>" + report[3] + "</font><br>");
			tb.append("<a action=\"bypass -h admin_markbotreaded " + botId + "\">Mark Report as Read</a><br>");
			tb.append("Punishment Type <combobox width=100 var=\"typeofreason\" list=\"CHATBAN;BANCHAR;BANACC;KICK\">");
			tb.append("Punishment Time <edit var=\"time\" width=100><br>");
			tb.append("<a action=\"bypass -h admin_punish_bot " + report[0] + " $typeofreason $time\">Punish " + report[0] + "</a><br>");
			tb.append("<a action=\"bypass -h admin_checkbots\">Go Back to bot list</a>");
			tb.append("</center></body></html>");
		}
		else
		{
			tb.append("<html><title>Bot #" + botId + "</title><body><center><br>");
			tb.append("- Bot report ticket Id: <font color=2554C7>" + botId + "</font><br1>");
			tb.append("- Player reported: <font color=\"FFA500\"><a action=\"bypass -h admin_goto_char_menu " + reportedTarget.getName() + "\">" + reportedTarget.getName() + "</a></font><br1>");
			tb.append("- Reported by: <font color=FF0000>" + report[1] + "</font><br1>");
			tb.append("- Reported Times: <font color =6CBB3C>" + punishTimes + "</font><br1>");
			tb.append("- Date: <font color=FF0000>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Long.parseLong(report[2])) + "</font><br>");
			tb.append("- Type: <font color=FFFF00>" + report[3] + "</font><br>");
			tb.append("<a action=\"bypass -h admin_markbotreaded " + botId + "\">Mark Report as Read</a><br>");
			tb.append("Punishment Type <combobox width=100 var=\"typeofreason\" list=\"CHATBAN;BANCHAR;BANACC;KICK\">");
			tb.append("Punishment Time <edit var=\"time\" width=100><br>");
			tb.append("<a action=\"bypass -h admin_punish_bot " + report[0] + " $typeofreason $time\">Punish " + report[0] + "</a><br>");
			tb.append("<a action=\"bypass -h admin_checkbots\">Go Back to bot list</a>");
			tb.append("</center></body></html>");
		}
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	private static void introduceNewPunishedBotAndClear(Player target)
	{
		Connection con = null;
		PreparedStatement delStatement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			// Delete all his reports from database
			delStatement = con.prepareStatement("DELETE FROM bot_report WHERE reported_objectId = ?");
			delStatement.setInt(1, target.getObjectId());
			delStatement.execute();
		}
		catch (Exception e)
		{
			_log.info("AdminCheckBot.introduceNewPunishedBotAndClear(target): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, delStatement);
		}
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}