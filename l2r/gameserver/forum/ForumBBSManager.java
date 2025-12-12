/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.forum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import l2r.gameserver.Config;
import l2r.gameserver.forum.ForumParser.Type;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;

public class ForumBBSManager implements ICommunityBoardHandler
{
	public static String FORUM_BBS_CMD = "_friendlist_0_";
	
	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		String html = "<html noscrollbar><body><center>";
		html += "<table border=0 cellpadding=0 cellspacing=0 width=770 height=38>";
		// html += "<tr>";
		// html += "<td><button value=\"\" action=\"bypass _bbshome\" width=\"70\" height=\"38\" back=\"cb.home_down\" fore=\"cb.home\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbseforum\" width=\"97\" height=\"38\" back=\"cb.services_down\" fore=\"cb.services\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _customvote\" width=\"89\" height=\"38\" back=\"cb.vote_down\" fore=\"cb.vote\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbTop\" width=\"112\" height=\"38\" back=\"cb.ranking_down\" fore=\"cb.ranking\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbsclan\" width=\"95\" height=\"38\" back=\"cb.clan_down\" fore=\"cb.clan\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbsbosslist_\" width=\"108\" height=\"38\" back=\"cb.raidboss_down\" fore=\"cb.raidboss\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbs_Auction\" width=\"106\" height=\"38\" back=\"cb.auction_down\" fore=\"cb.auction\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbspage:donate/donate-index\" width=\"105\" height=\"38\" back=\"cb.donation_down\" fore=\"cb.donation\"/></td>";
		// html += "<tr>";
		// html += "<td><button value=\"\" action=\"bypass _bbshome\" width=\"78\" height=\"33\" back=\"L2Ramona.home_down\" fore=\"L2Ramona.home\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbspage:Service/service\" width=\"93\" height=\"34\" back=\"L2Ramona.services_down\" fore=\"L2Ramona.services\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbsevent\" width=\"78\" height=\"32\" back=\"L2Ramona.Event_down\" fore=\"L2Ramona.Event\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbTop\" width=\"88\" height=\"33\" back=\"L2Ramona.ranking_down\" fore=\"L2Ramona.ranking\"/></td>playerTops";
		// html += "<td><button value=\"\" action=\"bypass _bbsclan\" width=\"77\" height=\"31\" back=\"L2Ramona.clan_down\" fore=\"L2Ramona.clan\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbsbosslist_\" width=\"95\" height=\"32\" back=\"L2Ramona.raidboss_down\" fore=\"L2Ramona.raidboss\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbs_Auction\" width=\"88\" height=\"33\" back=\"L2Ramona.auction_down\" fore=\"L2Ramona.auction\"/></td>";
		// html += "<td><button value=\"\" action=\"bypass _bbspage:DropCalculator/bbs_dropCalcMain\" width=\"99\" height=\"32\" back=\"L2Ramona.database_down\" fore=\"L2Ramona.database\"/></td> ";
		// html += "<td><button value=\"\" action=\"bypass _bbspage:Donate/donate\" width=\"88\" height=\"31\" back=\"L2Ramona.Store_down\" fore=\"L2Ramona.Store\"/></td>";
		// html += "</tr>";
		
		// html += "</tr>";
		html += "</center>";
		html += "</table>";
		html += "<center><br><br>";
		command = command.substring(command.length() > (FORUM_BBS_CMD.length()) ? FORUM_BBS_CMD.length() + 1 : FORUM_BBS_CMD.length());
		StringTokenizer st = new StringTokenizer(command, ";");
		String cmd = "main";
		if (st.hasMoreTokens())
		{
			cmd = st.nextToken();
		}
		if (cmd.startsWith("main"))
		{
			html += showThemes(activeChar);
		}
		else if (cmd.startsWith("theme"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			html += showTopics(activeChar, themeId, pageId);
		}
		else if (cmd.startsWith("topic"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int topicsPageId = Integer.parseInt(st.nextToken());
			int topicId = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			ForumParser.getInstance().increaseViews(activeChar, themeId, topicId);
			html += showTopic(activeChar, themeId, topicId, topicsPageId, pageId);
		}
		else if (command.startsWith("reply"))
		{
			if (st.countTokens() == 5)
			{
				String message = st.nextToken();
				int themeId = Integer.parseInt(st.nextToken());
				int topicsPageId = Integer.parseInt(st.nextToken());
				int topicId = Integer.parseInt(st.nextToken());
				int pageId = Integer.parseInt(st.nextToken());
				
				if (!ForumParser.getInstance().getForumThemes().containsKey(themeId))
				{
					html += showThemes(activeChar);
				}
				else
				{
					ForumTheme forum = ForumParser.getInstance().getForumThemes().get(themeId);
					if (!forum.getThemeTopics().containsKey(topicId))
					{
						html += showTopics(activeChar, themeId, topicsPageId);
					}
					else
					{
						ForumTopic topic = forum.getThemeTopics().get(topicId);
						if (forum.getType().equals(Type.ForGMS) && !activeChar.isGM())
						{
							html += showThemes(activeChar);
						}
						else if (forum.getType().equals(Type.PostOnlyGMS) && !activeChar.isGM())
						{
							html += showTopic(activeChar, themeId, topicId, topicsPageId, pageId);
						}
						else if (forum.getType().equals(Type.SeeOnlyAuthors) && !activeChar.isGM() && !activeChar.getName().equals(topic.getAuthorName()))
						{
							html += showTopics(activeChar, themeId, topicsPageId);
						}
						else
						{
							if (message.length() < 2)
							{
								html += showTopic(activeChar, themeId, topicId, topicsPageId, pageId);
							}
							else
							{
								ForumParser.getInstance().writeReply(activeChar, themeId, topicId, message);
								html += showTopic(activeChar, themeId, topicId, topicsPageId, pageId);
							}
						}
					}
				}
			}
			else
			{
				showThemes(activeChar);
			}
		}
		else if (command.startsWith("ctopic"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			html += createTopic(activeChar, themeId, pageId);
		}
		else if (command.startsWith("createtopic"))
		{
			boolean setPrefix = st.countTokens() == 5;
			String message = st.nextToken().substring(1);
			String topicName = st.nextToken().substring(1);
			String prefix = "";
			if (setPrefix)
			{
				prefix = st.nextToken().substring(1);
			}
			if (prefix.endsWith(" "))
			{
				prefix = prefix.substring(0, prefix.length() - 1);
			}
			int themeId = Integer.parseInt(st.nextToken());
			int topicsPageId = Integer.parseInt(st.nextToken());
			boolean canCreate = true;
			if (!ForumParser.getInstance().getForumThemes().containsKey(themeId))
			{
				html += showThemes(activeChar);
				canCreate = false;
			}
			else
			{
				ForumTheme forum = ForumParser.getInstance().getForumThemes().get(themeId);
				if (forum.getType().equals(Type.ForGMS) && !activeChar.isGM())
				{
					html += showThemes(activeChar);
					canCreate = false;
				}
				else if (forum.getType().equals(Type.PostOnlyGMS) && !activeChar.isGM())
				{
					html += showTopics(activeChar, themeId, topicsPageId);
					canCreate = false;
				}
			}
			if (canCreate)
			{
				ForumTheme forum = ForumParser.getInstance().getForumThemes().get(themeId);
				canCreate = true;
				for (ForumTopic topic : forum.getThemeTopics().values())
				{
					if (topicName.equalsIgnoreCase(topic.getTopicName()))
					{
						activeChar.sendMessage("This Topic Name already exist!");
						html += createTopic(activeChar, themeId, topicsPageId);
						canCreate = false;
					}
				}
				if (canCreate)
				{
					if (topicName.isEmpty() | (topicName == ""))
					{
						activeChar.sendMessage("You haven't putted any Topic Name!");
						html += createTopic(activeChar, themeId, topicsPageId);
						canCreate = false;
					}
					else if (topicName.length() > 30)
					{
						activeChar.sendMessage("Your topic name is too long!");
						html += createTopic(activeChar, themeId, topicsPageId);
						canCreate = false;
					}
					else if ((message.isEmpty() | (message == "")) || (message == "Topic") || message.equalsIgnoreCase("Topic"))
					{
						activeChar.sendMessage("You haven't writed any message! " + message);
						html += createTopic(activeChar, themeId, topicsPageId);
						canCreate = false;
					}
					else if ((prefix != null) && !prefix.equals("") && !forum.getPrefixes().contains(prefix))
					{
						activeChar.sendMessage("Your prefix doesn't exist!");
						html += createTopic(activeChar, themeId, topicsPageId);
						canCreate = false;
					}
				}
				
				if (canCreate)
				{
					int topicId = ForumParser.getInstance().createTopic(message, topicName, prefix, activeChar, themeId);
					html += showTopic(activeChar, themeId, topicId, topicsPageId, 1);
				}
			}
		}
		else if (command.startsWith("closetopic"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int topicId = Integer.parseInt(st.nextToken());
			int close = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			if ((close == 1) && !Config.FORUM_AUTHOR_CAN_CLOSE_TOPIC && !activeChar.isGM())
			{
				html += showTopics(activeChar, themeId, pageId);
			}
			else if ((close == 0) && !activeChar.isGM())
			{
				html += showTopics(activeChar, themeId, pageId);
			}
			else
			{
				ForumParser.getInstance().modifyTopic(activeChar, themeId, topicId, close);
				html += showTopics(activeChar, themeId, pageId);
			}
		}
		else if (command.startsWith("removepost"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int topicId = Integer.parseInt(st.nextToken());
			int postId = Integer.parseInt(st.nextToken());
			int topicsPageId = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			if (activeChar.isGM())
			{
				ForumParser.getInstance().removePost(activeChar, themeId, topicId, postId);
			}
			html += showTopic(activeChar, themeId, topicId, topicsPageId, pageId);
		}
		else if (command.startsWith("sticktopic"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int topicId = Integer.parseInt(st.nextToken());
			int stick = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			if (activeChar.isGM())
			{
				ForumParser.getInstance().modifyTopic(activeChar, themeId, topicId, stick + 2);
			}
			html += showTopics(activeChar, themeId, pageId);
		}
		else if (command.startsWith("removetopic"))
		{
			int themeId = Integer.parseInt(st.nextToken());
			int topicId = Integer.parseInt(st.nextToken());
			int pageId = Integer.parseInt(st.nextToken());
			if (activeChar.isGM())
			{
				ForumParser.getInstance().removeTopic(activeChar, themeId, topicId);
			}
			html += showTopics(activeChar, themeId, pageId);
		}
		else if (command.startsWith("cforum"))
		{
			if (activeChar.isGM())
			{
				html += createForum(activeChar);
			}
			else
			{
				html += showThemes(activeChar);
			}
		}
		else if (command.startsWith("createforum"))
		{
			html = "<html><body><center><br><br>";
			if (st.countTokens() < 6)
			{
				html += createForum(activeChar);
			}
			else
			{
				String forumName = st.nextToken();
				String forumIcon = st.nextToken();
				String description = st.nextToken();
				if (activeChar.isGM())
				{
					boolean nameAlreadyExists = false;
					for (ForumTheme theme : ForumParser.getInstance().getForumThemes().values())
					{
						if (forumName.equalsIgnoreCase(theme.getThemeName()))
						{
							activeChar.sendMessage("This Forum Name already exist!");
							html += createForum(activeChar);
							nameAlreadyExists = true;
						}
					}
					if (nameAlreadyExists)
					{
					}
					else if (forumName.isEmpty() || (forumName == ""))
					{
						activeChar.sendMessage("You haven't putted any Forum Name!");
						html += createForum(activeChar);
					}
					else if (forumName.length() > 30)
					{
						activeChar.sendMessage("Your forum name is too long!");
						html += createForum(activeChar);
					}
					else if (forumIcon.isEmpty() || (forumIcon == ""))
					{
						activeChar.sendMessage("You haven't putted any Forum Icon!");
						html += createForum(activeChar);
					}
					else if (description.isEmpty() || (description == ""))
					{
						activeChar.sendMessage("You haven't putted any description to the section!");
						html += createForum(activeChar);
					}
					else
					{
						String forumType = st.nextToken();
						String prefixes = st.nextToken();
						String forumPrefixes[] = null;
						if (prefixes.indexOf(",") <= 0)
						{
							forumPrefixes = new String[]
							{
								"none"
							};
						}
						else
						{
							forumPrefixes = prefixes.split(",");
						}
						String sectionName = st.nextToken();
						int forumId = ForumParser.getInstance().createForum(forumName, forumIcon.trim(), description, forumType, forumPrefixes, sectionName, activeChar);
						html += showTopics(activeChar, forumId, 1);
					}
				}
				else
				{
					html += showThemes(activeChar);
				}
			}
		}
		else if (command.startsWith("deleteforum"))
		{
			html = "<html><body><center><br><br>";
			int forumId = -1;
			try
			{
				forumId = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException nfe)
			{
				html += showThemes(activeChar);
			}
			if ((forumId != -1) && activeChar.isGM())
			{
				if (!ForumParser.getInstance().getForumThemes().containsKey(forumId))
				{
					activeChar.sendMessage("This forum does not exist!");
				}
				else
				{
					ForumParser.getInstance().removeForum(activeChar, forumId);
					html += showThemes(activeChar);
				}
			}
			else
			{
				activeChar.sendMessage("This forum does not exist!");
			}
		}
		else if (command.startsWith("csection"))
		{
			if (activeChar.isGM())
			{
				html += createSection(activeChar);
			}
			else
			{
				html += showThemes(activeChar);
			}
		}
		else if (command.startsWith("createsection"))
		{
			html = "<html><body><center><br><br>";
			if (st.countTokens() < 1)
			{
				html += createSection(activeChar);
			}
			else
			{
				String sectionName = st.nextToken();
				if (activeChar.isGM())
				{
					boolean nameExists = false;
					for (ForumSection section : ForumParser.getInstance().getForumSections().values())
					{
						if (sectionName.trim().equalsIgnoreCase(section.getSectionName().trim()))
						{
							activeChar.sendMessage("This Section Name already exist!");
							html += createSection(activeChar);
							nameExists = true;
							break;
						}
					}
					if (nameExists)
					{
					}
					else if (sectionName.isEmpty() || (sectionName == ""))
					{
						activeChar.sendMessage("You haven't putted any Section Name!");
						html += createSection(activeChar);
					}
					else if (sectionName.length() > 30)
					{
						activeChar.sendMessage("Your section name is too long!");
						html += createSection(activeChar);
					}
					else
					{
						ForumParser.getInstance().createSection(sectionName, activeChar);
						html += showThemes(activeChar);
					}
				}
				else
				{
					html += showThemes(activeChar);
				}
			}
		}
		html += "</center></body></html>";
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	public String showThemes(Player player)
	{
		Calendar calendar = Calendar.getInstance();
		String html = "";
		html += "<table width=610><tr><td width=500><font color=LEVEL>Main</font></td></tr></table><br>";
		int i = 0;
		for (ForumSection section : ForumParser.getInstance().getForumSections().values())
		{
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<table cellspacing=0 cellpadding=2 bgcolor=353535 width=650>";
			html += "<tr>";
			html += "<td><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"2\"></td>";
			html += "</tr>";
			html += "<tr>";
			html += "<td align=center fixwidth=650><font name=\"ScreenMessageSmall\">" + section.getSectionName() + "</font></td>";
			html += "</tr>";
			html += "</table>";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			for (ForumTheme theme : section.getThemes().values())
			{
				if (i > 0)
				{
					html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
					html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"1\">";
					html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
				}
				if (theme.getType().equals(Type.ForGMS) && !player.isGM())
				{
					continue;
				}
				
				int topics = 0;
				int posts = 0;
				for (ForumTopic topic : theme.getThemeTopics().values())
				{
					if (theme.getType().equals(Type.SeeOnlyAuthors) && !topic.getAuthorName().equals(player.getName()) && !player.isGM())
					{
						continue;
					}
					posts += topic.getTopicPosts().size();
					topics++;
				}
				html += "<table " + ((i % 2) == 0 ? "" : "bgcolor=151515") + " cellspacing=0 cellpadding=2 width=650>";
				html += "<tr>";
				html += "<td FIXWIDTH=10></td>";
				html += "<td FIXWIDTH=50><img src=\"" + theme.getThemeIcon() + "\" width=\"32\" height=\"32\">";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"6\">";
				html += "</td>";
				html += "<td FIXWIDTH=520>";
				html += "<table><tr><td FIXWIDTH=520>";
				html += "<a action=\"bypass " + FORUM_BBS_CMD + ";themes;" + theme.getThemeId() + ";1\"><font name=\"hs9\" color=\"BABABA\">" + theme.getThemeName() + "</font></a>";
				html += "</td></tr>";
				html += "<tr><td FIXWIDTH=520>";
				html += "<font name=\"CreditTextSmall\" color=\"777777\">" + theme.getThemeDescription() + "</font>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\">";
				html += "</td></tr></table>";
				html += "</td>";
				html += "<td FIXWIDTH=70>";
				html += "<table><tr><td width=70>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\">";
				html += "<font name=\"CreditTextSmall\" color=\"AAAAAA\">Topics: " + topics + "</font>";
				html += "</td></tr>";
				html += "<tr><td width=70>";
				html += "<font name=\"CreditTextSmall\" color=\"AAAAAA\">Posts: " + posts + "</font>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\">";
				html += "</td></tr></table>";
				html += "</td>";
				html += "<td FIXWIDTH=20>";
				if (player.isGM())
				{
					html += "<button value=\" \" action=\"bypass " + FORUM_BBS_CMD + ";deleteforum;" + theme.getThemeId() + "\" width=\"15\" height=\"15\" fore=\"L2UI_CT1.frames_df_btn_close\" back=\"L2UI_CT1.frames_df_btn_close_Down\"/>";
				}
				html += "</td>";
				html += "</tr>";
				html += "</table>";
				i++;
			}
		}
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"20\">";
		
		String recentPosts = "";
		
		i = 0;
		for (ForumTopic topic : ForumParser.getInstance().getRecentPosts())
		{
			if (topic == null)
			{
				continue;
			}
			String lastPost = topic.getLastMessage() != null ? topic.getLastMessage().getPosterName() : topic.getAuthorName();
			long lastPostDate = topic.getLastPost();
			calendar.setTimeInMillis(lastPostDate * 1l);
			String date = getTodaysDate(calendar);
			recentPosts += "<table " + ((i % 2) == 0 ? "" : "bgcolor=151515") + " cellspacing=0 cellpadding=2 width=650>";
			recentPosts += "<tr>";
			recentPosts += "<td FIXWIDTH=10></td>";
			recentPosts += "<td FIXWIDTH=350>";
			recentPosts += "<a action=\"bypass " + FORUM_BBS_CMD + ";topic;" + topic.getForumId() + ";1;" + topic.getTopicId() + ";1\">" + topic.getTopicName() + "</a>";
			recentPosts += "</td>";
			recentPosts += "<td FIXWIDTH=100 align=center>by " + lastPost + "</td>";
			recentPosts += "<td FIXWIDTH=180 align=center>" + date + "</td>";
			recentPosts += "<td FIXWIDTH=10></td>";
			recentPosts += "</tr>";
			recentPosts += "</table>";
			recentPosts += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			recentPosts += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"1\">";
			recentPosts += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			i++;
		}
		
		if (i > 0)
		{
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<table border=0 cellspacing=0 cellpadding=2 bgcolor=353535 width=650>";
			html += "<tr>";
			html += "<td FIXWIDTH=5><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"2\"></td>";
			html += "</tr>";
			html += "<tr>";
			html += "<td FIXWIDTH=10></td>";
			html += "<td FIXWIDTH=630>Recent Posts</td>";
			html += "<td FIXWIDTH=10></td>";
			html += "</tr>";
			html += "</table>";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += recentPosts;
		}
		if (player.isGM())
		{
			html += "<br1><table width=610><tr>";
			html += "<td align=right width=340><button value=\"Create Section\" action=\"bypass " + FORUM_BBS_CMD + ";csection\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td>";
			html += "<td align=right><button value=\"Create Post Section\" action=\"bypass " + FORUM_BBS_CMD + ";cforum\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td>";
			html += "</tr></table>";
		}
		return html;
	}
	
	public String showTopics(Player player, int themeId, int pageId)
	{
		if (!ForumParser.getInstance().getForumThemes().containsKey(themeId))
		{
			return showThemes(player);
		}
		ForumTheme forum = ForumParser.getInstance().getForumThemes().get(themeId);
		if (forum.getType().equals(Type.ForGMS) && !player.isGM())
		{
			return showThemes(player);
		}
		boolean showOnlyAuthors = forum.getType().equals(Type.SeeOnlyAuthors);
		boolean havePrefixes = forum.getPrefixes().size() > 0;
		String html = "";
		html += "<br>";
		String main = "<a action=\"bypass " + FORUM_BBS_CMD + "\">Main</a>";
		String theme = ForumParser.getInstance().getForumThemes().get(themeId).getThemeName();
		boolean viewControl = player.isGM() || Config.FORUM_AUTHOR_CAN_CLOSE_TOPIC;
		
		html += "<table width=610><tr><td width=500><font color=LEVEL>" + main + " -> " + theme + "</font></td></tr></table><br>";
		
		html += "<font name=\"CreditTextSmall\">";
		
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		html += "<table cellspacing=0 cellpadding=2 bgcolor=353535 width=650>";
		html += "<tr>";
		html += "<td FIXWIDTH=5><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"2\"></td>";
		html += "</tr>";
		html += "<tr>";
		html += "<td FIXWIDTH=10></td>";
		
		html += havePrefixes ? "<td FIXWIDTH=50 align=center>Prefix</td>" : "";
		
		int control = player.isGM() ? 60 : (viewControl ? 35 : 0);
		int nameSize = (420 - control - (havePrefixes ? 55 : 0));
		
		html += "<td FIXWIDTH=10></td>";
		html += "<td FIXWIDTH=" + nameSize + ">Name</td>";
		html += "<td FIXWIDTH=150>Author</td>";
		html += "<td FIXWIDTH=50 align=center>Views</td>";
		html += "<td FIXWIDTH=50 align=center>Posts</td>";
		
		html += viewControl ? "<td FIXWIDTH=" + control + " align=\"center\">###</td>" : "";
		
		html += "<td FIXWIDTH=10></td>";
		html += "</tr>";
		html += "</table>";
		
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		
		HashMap<Long, ForumTopic> forumTopics = new HashMap<>();
		HashMap<Long, ForumTopic> forumSTopics = new HashMap<>();
		int topicsCount = 0;
		for (ForumTopic topic : forum.getThemeTopics().values())
		{
			if (forum.getType().equals(Type.SeeOnlyAuthors) && !topic.getAuthorName().equals(player.getName()) && !player.isGM())
			{
				continue;
			}
			if (topic.isSticked())
			{
				forumSTopics.put(topic.getLastPost(), topic);
			}
			else
			{
				forumTopics.put(topic.getLastPost(), topic);
			}
			topicsCount++;
		}
		
		ArrayList<Long> sortedList = new ArrayList<>(forumSTopics.keySet());
		Collections.sort(sortedList);
		Collections.reverse(sortedList);
		
		if (sortedList.size() > 0)
		{
			html += showSortedTopics(sortedList, forumSTopics, true, player, showOnlyAuthors, havePrefixes, viewControl, themeId, pageId);
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"10\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		}
		
		sortedList = new ArrayList<>(forumTopics.keySet());
		Collections.sort(sortedList);
		Collections.reverse(sortedList);
		
		html += showSortedTopics(sortedList, forumTopics, false, player, showOnlyAuthors, havePrefixes, viewControl, themeId, pageId);
		
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"20\">";
		
		if (topicsCount == 0)
		{
			html += "No topics in this section!";
		}
		html += "<table width=170><tr>";
		boolean starts = (pageId - 1) > 0;
		html += "<td align=right width=190><button value=\"Prev\" action=\"" + (!starts ? "" : "bypass " + FORUM_BBS_CMD + ";themes;" + themeId + ";" + (pageId - 1)) + "\" width=75 height=21 back=\"L2UI_CT1.Button_DF" + (!starts ? "_Disable" : "") + "\" fore=\"L2UI_CT1.Button_DF" + (!starts ? "_Disable" : "") + "\"></td>";
		boolean ends = (pageId * Config.FORUM_TOPICS_LIMIT_IN_PAGE) >= (topicsCount);
		html += "<td width=190><button value=\"Next\" action=\"" + (ends ? "" : "bypass " + FORUM_BBS_CMD + ";themes;" + themeId + ";" + (pageId + 1)) + "\" width=75 height=21 back=\"L2UI_CT1.Button_DF" + (ends ? "_Disable" : "") + "\" fore=\"L2UI_CT1.Button_DF" + (ends ? "_Disable" : "") + "\"></td>";
		html += "</tr></table>";
		if ((forum.getType().equals(Type.PostOnlyGMS) && player.isGM()) || !forum.getType().equals(Type.PostOnlyGMS))
		{
			html += "<br1><table width=610><tr><td align=right><button value=\"Create Topic\" action=\"bypass " + FORUM_BBS_CMD + ";ctopic;" + themeId + ";" + pageId + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table>";
		}
		html += "<br><button value=\"Back\" action=\"bypass " + FORUM_BBS_CMD + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" />";
		return html;
	}
	
	public String showSortedTopics(ArrayList<Long> times, HashMap<Long, ForumTopic> topics, boolean sticked, Player player, boolean showOnlyAuthors, boolean havePrefixes, boolean viewControl, int themeId, int pageId)
	{
		String html = "";
		int count = 1;
		int i = 0;
		for (long time : times)
		{
			ForumTopic topic = topics.get(time);
			if (showOnlyAuthors && !topic.getAuthorName().equals(player.getName()) && !player.isGM())
			{
				continue;
			}
			if ((count <= (Config.FORUM_TOPICS_LIMIT_IN_PAGE * (pageId - 1))) || (count > (Config.FORUM_TOPICS_LIMIT_IN_PAGE * pageId)))
			{
				count++;
				continue;
			}
			if (i > 0)
			{
				html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
				html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"1\">";
				html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			}
			
			html += "<table " + ((count % 2) == 0 ? "" : "bgcolor=000000") + " cellspacing=0 cellpadding=2 width=650>";
			html += "<tr>";
			html += "<td FIXWIDTH=10></td>";
			
			if (havePrefixes)
			{
				html += "<td FIXWIDTH=65>";
				if ((topic.getPrefix() != null) && (topic.getPrefix().length() > 0))
				{
					String prefix = topic.getPrefix();
					if (prefix.length() > 7)
					{
						prefix = topic.getPrefix().substring(0, 5) + "..";
					}
					html += "<font color=DAA520>[" + prefix + "]</font>";
				}
				html += "</td>";
			}
			
			boolean isGM = player.isGM();
			
			int control = isGM ? 60 : (viewControl ? 35 : 0);
			
			html += "<td FIXWIDTH=" + (420 - control - (havePrefixes ? 55 : 0)) + ">";
			
			html += !havePrefixes ? "<font color=DAA520>[Sticked] </font>" : "";
			
			html += "<a action=\"bypass " + FORUM_BBS_CMD + ";topic;" + themeId + ";" + pageId + ";" + topic.getTopicId() + ";1\">";
			html += topic.isClosed() ? "<font color=8B0000>" + topic.getTopicName() + "</font>" : topic.getTopicName();
			html += "</a>";
			
			html += "</td>";
			
			html += "<td FIXWIDTH=150><font color=6B8E23>" + topic.getMainPoster().getName() + "</font></td>";
			html += "<td FIXWIDTH=50 align=center>" + topic.getTopicViews() + "</td>";
			html += "<td FIXWIDTH=50 align=center>" + topic.getTopicPosts().size() + "</td>";
			
			if (viewControl)
			{
				if (isGM)
				{
					html += "<td FIXWIDTH=12>";
					html += "<button value=\" \" action=\"bypass " + FORUM_BBS_CMD + ";closetopic;" + themeId + ";" + topic.getTopicId() + ";" + (topic.isClosed() ? 0 : 1) + ";" + pageId + "\" width=\"9\" height=\"15\" fore=\"L2UI_CT1.PostWnd_DF_Icon_SafetyTrade" + (topic.isClosed() ? "" : "_Confirmed") + "\" back=\"L2UI_CT1.PostWnd_DF_Icon_SafetyTrade" + (topic.isClosed() ? "_Confirmed" : "") + "\"/>";
					html += "</td>";
					
					html += "<td FIXWIDTH=24>";
					html += "<button value=\" \" action=\"bypass " + FORUM_BBS_CMD + ";sticktopic;" + themeId + ";" + topic.getTopicId() + ";" + (topic.isSticked() ? "0" : "1") + ";" + pageId + "\" width=\"18\" height=\"15\" fore=\"L2UI_CT1.PostWnd_DF_Icon_Accompany" + (topic.isSticked() ? "" : "_Confirmed") + "\" back=\"L2UI_CT1.PostWnd_DF_Icon_Accompany" + (topic.isSticked() ? "_Confirmed" : "") + "\"/>";
					html += "</td>";
					
					html += "<td FIXWIDTH=20 align=\"right\">";
					html += "<button value=\" \" action=\"bypass " + FORUM_BBS_CMD + ";removetopic;" + themeId + ";" + topic.getTopicId() + ";" + pageId + "\" width=\"15\" height=\"15\" fore=\"L2UI_CT1.frames_df_btn_close\" back=\"L2UI_CT1.frames_df_btn_close_Down\"/>";
					html += "</td>";
				}
				else
				{
					boolean canClose = topic.getMainPoster().getName().equalsIgnoreCase(player.getName()) && !(topic.isClosed());
					html += "<td FIXWIDTH=35 align=\"center\">";
					html += "<button value=\" \" action=\"" + (canClose ? "bypass " + FORUM_BBS_CMD + ";closetopic;" + themeId + ";" + topic.getTopicId() + ";" + (topic.isClosed() ? 0 : 1) + ";" + pageId : "") + "\" width=\"9\" height=\"15\" fore=\"L2UI_CT1.PostWnd_DF_Icon_SafetyTrade" + (topic.isClosed() ? "" : "_Confirmed") + "\" back=\"L2UI_CT1.PostWnd_DF_Icon_SafetyTrade" + (topic.isClosed() ? "_Confirmed" : "") + "\"/>";
					html += "</td>";
				}
				
			}
			html += "<td FIXWIDTH=10></td>";
			html += "</tr>";
			html += "</table>";
			count++;
			i++;
		}
		return html;
	}
	
	public String showTopic(Player player, int themeId, int topicId, int topicsPageId, int pageId)
	{
		if (!ForumParser.getInstance().getForumThemes().containsKey(themeId))
		{
			return showThemes(player);
		}
		ForumTheme forum = ForumParser.getInstance().getForumThemes().get(themeId);
		
		if (forum.getType().equals(Type.ForGMS) && !player.isGM())
		{
			return showThemes(player);
		}
		
		if (!forum.getThemeTopics().containsKey(topicId))
		{
			return showTopics(player, themeId, topicsPageId);
		}
		ForumTopic topic = forum.getThemeTopics().get(topicId);
		
		if (forum.getType().equals(Type.SeeOnlyAuthors) && !player.getName().equals(topic.getAuthorName()) && !player.isGM())
		{
			return showTopics(player, themeId, topicsPageId);
		}
		
		String html = "";
		int i = 0;
		Calendar calendar = Calendar.getInstance();
		
		String main = "<a action=\"bypass " + FORUM_BBS_CMD + "\">Main</a>";
		String theme = "<a action=\"bypass " + FORUM_BBS_CMD + ";themes;" + themeId + ";1\">" + ForumParser.getInstance().getForumThemes().get(themeId).getThemeName() + "</a>";
		String prefix = (topic.getPrefix() != null) && (topic.getPrefix().length() > 0) ? "[" + topic.getPrefix() + "] " : "";
		html += "<table width=610><tr><td width=500><font color=LEVEL>" + main + " -> " + theme + " -> " + (topic.isClosed() ? "[Closed] " : "") + prefix + topic.getTopicName() + "</font></td></tr></table><br>";
		calendar.setTimeInMillis(topic.getPostDate());
		
		html += "<table background=\"L2UI_CT1.Windows_DF_TooltipBG\" cellspacing=0 cellpadding=2 width=660>";
		html += "<tr></tr>";
		html += "<tr>";
		html += "<td fixwidth=11></td>";
		html += "<td FIXWIDTH=120>";
		html += "<table background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\">";
		html += "<tr><td width=120 align=center><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/></td></tr>";
		html += "<tr><td width=120 align=center><font color=6B8E23>" + topic.getMainPoster().getName() + "</font></td></tr>";
		html += "<tr><td width=120 align=center>Posts: <font color=DAA520>" + topic.getMainPoster().getPostsCount() + "</font></td></tr>";
		String date[] = getDate(calendar).split(" ");
		html += "<tr><td width=120 align=center><font color=1E90FF>Day: " + date[0] + "</font></td></tr>";
		html += "<tr><td width=120 align=center><font color=1E90FF>Hour: " + date[1] + "</font></td></tr></table></td>";
		html += "<td FIXWIDTH=440>" + topic.getTopicMainMessage().replace("\n", "<br1>") + "<img src=\"L2UI.SquareBlank\" width=200 height=10/></td>";
		html += "</tr>";
		html += "<tr><td></td><td><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"15\"></td></tr>";
		html += "</table>";
		
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"10\">";
		html += "<img src=\"L2UI.SquareGray\" width=\"655\" height=\"2\">";
		html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
		
		HashMap<Long, TopicMessage> forumMessages = new HashMap<>();
		for (TopicMessage message : topic.getTopicPosts().values())
		{
			forumMessages.put(message.getPostDate(), message);
		}
		
		ArrayList<Long> sortedList = new ArrayList<>(forumMessages.keySet());
		Collections.sort(sortedList);
		
		int count = 1;
		i = 0;
		for (long time : sortedList)
		{
			if ((count <= (Config.FORUM_MESSAGES_LIMIT_IN_PAGE * (pageId - 1))) || (count > (Config.FORUM_MESSAGES_LIMIT_IN_PAGE * pageId)))
			{
				count++;
				continue;
			}
			TopicMessage message = forumMessages.get(time);
			calendar.setTimeInMillis(time);
			if (i > 0)
			{
				html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
				html += "<img src=\"L2UI.SquareGray\" width=\"655\" height=\"2\">";
				html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			}
			html += "<table bgcolor=" + ((i % 2) == 0 ? "111111" : "000000") + " cellspacing=0 cellpadding=2 width=660>";
			html += "<tr></tr>";
			html += "<tr>";
			html += "<td fixwidth=5></td>";
			html += "<td FIXWIDTH=120>";
			html += "<table background=\"L2UI_CT1.Windows_DF_TooltipBG\">";
			html += "<tr><td></td><td><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"></td></tr>";
			html += "<tr><td width=120 align=center><font color=6B8E23>" + message.getMessagePoster().getName() + "</font></td></tr>";
			html += "<tr><td width=120 align=center>Posts: <font color=DAA520>" + message.getMessagePoster().getPostsCount() + "</font></td></tr>";
			date = getDate(calendar).split(" ");
			html += "<tr><td width=120 align=center><font color=1E90FF>Day: " + date[0] + "</font></td></tr>";
			html += "<tr><td width=120 align=center><font color=1E90FF>Hour: " + date[1] + "</font></td></tr></table></td>";
			if (player.isGM())
			{
				html += "<td FIXWIDTH=390>" + message.getPost().replace("\n", "<br1>") + "<img src=\"L2UI.SquareBlank\" width=200 height=10/></td>";
				html += "<td FIXWIDTH=50 align=left><button action=\"bypass " + FORUM_BBS_CMD + ";removepost;" + themeId + ";" + topicId + ";" + message.getPostId() + ";" + topicsPageId + ";" + pageId + "\" width=\"14\" height=\"14\" fore=\"L2UI_CH3.FrameCloseBtn\" back=\"L2UI_CH3.FrameCloseBtn\"></td>";
			}
			else
			{
				html += "<td FIXWIDTH=440>" + message.getPost().replace("\n", "<br1>") + "<img src=\"L2UI.SquareBlank\" width=200 height=10/></td>";
			}
			html += "</tr>";
			html += "<tr><td></td><td><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"15\"></td></tr>";
			html += "</table>";
			count++;
			i++;
		}
		if (i > 0)
		{
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareGray\" width=\"650\" height=\"2\">";
			html += "<img src=\"L2UI.SquareBlank\" width=\"650\" height=\"20\">";
		}
		
		html += "<table width=170><tr>";
		
		boolean starts = (pageId - 1) > 0;
		html += "<td align=right width=190><button value=\"Prev\" action=\"" + (!starts ? "" : "bypass " + FORUM_BBS_CMD + ";topic;" + themeId + ";" + topicsPageId + ";" + topic.getTopicId() + ";" + (pageId - 1)) + "\" width=75 height=21 back=\"L2UI_CT1.Button_DF" + (!starts ? "_Disable" : "") + "\" fore=\"L2UI_CT1.Button_DF" + (!starts ? "_Disable" : "") + "\"></td>";
		boolean ends = (pageId * Config.FORUM_MESSAGES_LIMIT_IN_PAGE) >= (count - 1);
		html += "<td width=190><button value=\"Next\" action=\"" + (ends ? "" : "bypass " + FORUM_BBS_CMD + ";topic;" + themeId + ";" + topicsPageId + ";" + topic.getTopicId() + ";" + (pageId + 1)) + "\" width=75 height=21 back=\"L2UI_CT1.Button_DF" + (ends ? "_Disable" : "") + "\" fore=\"L2UI_CT1.Button_DF" + (ends ? "_Disable" : "") + "\"></td>";
		html += ("</tr></table>");
		if (ForumParser.getInstance().getForumThemes().get(themeId).getThemeTopics().isEmpty())
		{
			html += "No answers in this topic!";
		}
		
		if (!topic.isClosed() && ((forum.getType().equals(Type.PostOnlyGMS) && player.isGM()) || !forum.getType().equals(Type.PostOnlyGMS)))
		{
			html += "<br><MultiEdit var=\"Text\" width=500 height=100>";
			html += "<br><button value=\"Reply\" action=\"bypass " + FORUM_BBS_CMD + ";reply; $Text ;" + themeId + ";" + topicsPageId + ";" + topicId + ";" + pageId + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" />";
		}
		html += "<br1><table width=610><tr><td width=350></td><td align=right width=140><button value=\"Back\" action=\"bypass " + FORUM_BBS_CMD + ";themes;" + themeId + ";" + topicsPageId + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td><td align=left width=120><button value=\"Refresh\" action=\"bypass " + FORUM_BBS_CMD + ";topic;" + themeId + ";" + topicsPageId + ";" + topic.getTopicId() + ";" + pageId + "\" width=65 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table>";
		return html;
	}
	
	public String createTopic(Player player, int forumId, int topicsPageId)
	{
		if (!ForumParser.getInstance().getForumThemes().containsKey(forumId))
		{
			return showThemes(player);
		}
		ForumTheme forumTheme = ForumParser.getInstance().getForumThemes().get(forumId);
		if (!player.isGM())
		{
			if (forumTheme.getType().equals(Type.ForGMS))
			{
				return showThemes(player);
			}
			else if (forumTheme.getType().equals(Type.PostOnlyGMS))
			{
				return showTopics(player, forumId, topicsPageId);
			}
		}
		String html = "";
		String main = "<a action=\"bypass " + FORUM_BBS_CMD + "\">Main</a>";
		String forum = "<a action=\"bypass " + FORUM_BBS_CMD + ";themes;" + forumId + ";1\">" + forumTheme.getThemeName() + "</a>";
		String prefixes = "";
		for (String prefix : forumTheme.getPrefixes())
		{
			prefixes += prefix + ";";
		}
		
		html += "<table width=610><tr><td width=500><font color=LEVEL>" + main + " -> " + forum + " -> Create Topic</font></td></tr></table><br>";
		
		html += "<font name=\"CreditTextSmall\" color=\"AAAAAA\">";
		
		html += "<br><table width=610><tr>";
		if (prefixes.length() > 1)
		{
			html += "<td width=36></td>";
			html += "<td align=left width=\"210\">Topic Name:</td>";
			html += "<td align=left width=\"210\">Topic Prefix:</td>";
			html += "</tr><tr>";
			html += "<td width=36></td>";
			html += "<td align=left width=\"210\"><edit var=\"Name\" width=200 length=30></td>";
			html += "<td align=left width=\"210\"><combobox var=\"prefix\" list=\"" + prefixes.substring(0, prefixes.length() - 1) + "\" width=125></td>";
		}
		else
		{
			html += "<td width=16></td>";
			html += "<td align=left width=\"210\">Topic Name:</td>";
			html += "</tr><tr>";
			html += "<td width=16></td>";
			html += "<td align=left width=\"210\"><edit var=\"Name\" width=200 length=30></td>";
		}
		html += "</tr></table>";
		
		html += "<br><table width=610><tr><td width=7></td><td width=100>Topic Message:</td></tr></table><br1><MultiEdit var=\"Text\" width=500 height=100>";
		
		html += "</font>";
		
		html += "<br><button value=\"Create\" action=\"bypass " + FORUM_BBS_CMD + ";createtopic; $Text ; $Name" + (prefixes.length() > 0 ? " ; $prefix" : "") + " ;" + forumId + ";" + topicsPageId + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" />";
		html += "<br1><table width=610><tr><td align=right><button value=\"Back\" action=\"bypass " + FORUM_BBS_CMD + ";themes;" + forumId + ";1\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table>";
		
		return html;
	}
	
	public String createForum(Player activeChar)
	{
		if (ForumParser.getInstance().getForumSections().size() < 1)
		{
			activeChar.sendMessage("You have to create section first!");
			return createSection(activeChar);
		}
		String html = "";
		String main = "<a action=\"bypass " + FORUM_BBS_CMD + "\">Main</a>";
		html += "<table width=610><tr><td width=500><font color=LEVEL>" + main + " -> Create Section</font></td></tr></table><br>";
		
		html += "<br><table width=610><tr>";
		html += "<td align=left width=230><font name=\"CreditTextSmall\" color=\"AAAAAA\">Section Name:</td>";
		html += "<td align=left width=230>Section:</td>";
		html += "</tr><tr>";
		html += "<td align=left width=230><edit var=\"Name\" width=200 length=30></td>";
		String sections = "";
		for (ForumSection section : ForumParser.getInstance().getForumSections().values())
		{
			sections += section.getSectionName() + ";";
		}
		html += "<td align=left width=230><combobox var=\"section\" list=\"" + sections + "\" width=200></td>";
		html += "</tr></table>";
		
		html += "<br><table width=610><tr>";
		html += "<td align=left width=230>Section Prefixes: (Example: WTS,WTB,WTT)</td>";
		html += "<td align=left width=230></td>";
		html += "</tr><tr>";
		html += "<td align=left width=230>Write 'none' or 'null' or 'empty' for no prefixes.</td>";
		html += "<td align=left width=230>Section type:</td>";
		html += "</tr><tr>";
		html += "<td align=left width=230><MultiEdit var=\"prefixes\" width=200 height=15></td>";
		html += "<td align=left width=230><combobox var=\"forumType\" list=\"Normal;For GMS;See only Authors;PostOnlyGMS\" width=200></td>";
		html += "</tr></table>";
		
		html += "<br><table width=610><tr>";
		html += "<td align=left width=230>Section Description:</td>";
		html += "<td align=left width=230>Section Icon:</td>";
		html += "</tr><tr>";
		html += "<td align=left width=230><MultiEdit var=\"description\" width=200 height=60></td>";
		html += "<td align=left width=230><MultiEdit var=\"Icon\" width=200 height=15></td>";
		html += "</tr></table>";
		
		html += "<center>";
		html += "<br><button value=\"Create\" action=\"bypass " + FORUM_BBS_CMD + ";createforum; $Name ; $Icon ; $description ; $forumType ; $prefixes ; $section\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" />";
		html += "<br1><table width=610><tr><td align=right><button value=\"Back\" action=\"bypass " + FORUM_BBS_CMD + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table>";
		return html;
	}
	
	public String createSection(Player activeChar)
	{
		String html = "";
		String main = "<a action=\"bypass " + FORUM_BBS_CMD + "\">Main</a>";
		html += "<table width=610><tr><td width=500><font color=LEVEL>" + main + " -> Create Section</font></td></tr></table><br>";
		
		html += "<br><table width=610><tr>";
		html += "<td align=left width=230><font name=\"CreditTextSmall\" color=\"AAAAAA\">Section Name:</td>";
		html += "</tr><tr>";
		html += "<td align=left width=230><edit var=\"Name\" width=200 length=30></td>";
		html += "</tr></table>";
		
		html += "<center>";
		html += "<br><button value=\"Create\" action=\"bypass " + FORUM_BBS_CMD + ";createsection; $Name\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" />";
		html += "<br1><table width=610><tr><td align=right><button value=\"Back\" action=\"bypass " + FORUM_BBS_CMD + "\" width=135 height=21 back=\"L2UI_CT1.Button_DF_down\" fore=\"L2UI_CT1.Button_DF\" /></td></tr></table>";
		return html;
	}
	
	public String getDate(Calendar calendar)
	{
		String date = "";
		String year = (calendar.get(Calendar.YEAR) + "").substring(2);
		String month = ((calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : (calendar.get(Calendar.MONTH) + 1)) + "";
		String day = (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH)) + "";
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR_OF_DAY)) + "";
		String minute = (calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE)) + "";
		String second = (calendar.get(Calendar.SECOND) < 10 ? "0" + calendar.get(Calendar.SECOND) : calendar.get(Calendar.SECOND)) + "";
		date += year + "." + month + "." + day + " " + hour + ":" + minute + ":" + second;
		return date;
	}
	
	public String getTodaysDate(Calendar calendar)
	{
		Calendar cal = Calendar.getInstance();
		int difference = cal.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH);
		String day = "";
		if (difference == 0)
		{
			day = "Today";
		}
		else if (difference == 1)
		{
			day = "Yesterday";
		}
		else if (difference < 7)
		{
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == 1)
			{
				day = "Sunday";
			}
			else if (dayOfWeek == 2)
			{
				day = "Monday";
			}
			else if (dayOfWeek == 3)
			{
				day = "Tuesday";
			}
			else if (dayOfWeek == 4)
			{
				day = "Wednesday";
			}
			else if (dayOfWeek == 5)
			{
				day = "Thursday";
			}
			else if (dayOfWeek == 6)
			{
				day = "Friday";
			}
			else if (dayOfWeek == 7)
			{
				day = "Saturday";
			}
		}
		else
		{
			String year = calendar.get(Calendar.YEAR) + "";
			String month = (calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : (calendar.get(Calendar.MONTH) + 1) + "";
			String d = calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH) + "";
			day = year + "-" + month + "-" + d;
			
		}
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR_OF_DAY)) + "";
		String minute = (calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE)) + "";
		String second = (calendar.get(Calendar.SECOND) < 10 ? "0" + calendar.get(Calendar.SECOND) : calendar.get(Calendar.SECOND)) + "";
		String date = day + " at " + hour + ":" + minute + ":" + second;
		return date;
		
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			FORUM_BBS_CMD
		};
	}
	
	private static ForumBBSManager _instance = new ForumBBSManager();
	
	public static ForumBBSManager getInstance()
	{
		return _instance;
	}
	
}