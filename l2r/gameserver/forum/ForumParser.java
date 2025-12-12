/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2r.gameserver.forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;

public class ForumParser
{
	public enum Type
	{
		Normal,
		ForGMS,
		SeeOnlyAuthors,
		PostOnlyGMS
	}
	
	private final HashMap<Integer, ForumTheme> forumThemes;
	private final HashMap<Integer, ForumSection> forumSections;
	private final HashMap<Integer, Poster> posters;
	boolean updateForums = false;
	boolean updateSections = false;
	boolean updateMessages = false;
	boolean updateTopics = false;
	boolean updatePlayers = false;
	HashMap<String, Poster> changedPlayers;
	HashMap<String, TopicMessage> changedPosts;
	HashMap<String, ForumTopic> changedTopics;
	HashMap<String, ForumTheme> changedForums;
	HashMap<String, ForumSection> changedSections;
	int lastMessageId = 0;
	int lastTopicId = 0;
	int lastForumId = 0;
	int lastSectionId = 0;
	ArrayList<ForumTopic> recentPosts;
	
	public ForumParser()
	{
		forumThemes = new HashMap<>();
		forumSections = new HashMap<>();
		posters = new HashMap<>();
		changedPosts = new HashMap<>();
		changedTopics = new HashMap<>();
		changedPlayers = new HashMap<>();
		changedForums = new HashMap<>();
		changedSections = new HashMap<>();
		recentPosts = new ArrayList<>();
		loadForum();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new UpdateDatabase(true), Config.FORUM_INFORMATION_MANAGEMENT * 1000, Config.FORUM_INFORMATION_MANAGEMENT * 1000);
	}
	
	public Poster getPoster(int userId)
	{
		if (posters.containsKey(userId))
		{
			return posters.get(userId);
		}
		return null;
	}
	
	public void loadForum()
	{
		forumThemes.clear();
		posters.clear();
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT authorId, name, posts FROM forum_characters");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				posters.put(rset.getInt("authorId"), new Poster(rset.getInt("authorId"), rset.getString("name"), rset.getInt("posts")));
			}
			rset.close();
			statement.close();
			
			statement = con.prepareStatement("SELECT id, name FROM forum_sections");
			
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				int id = rset.getInt("id");
				if (id > lastSectionId)
				{
					lastSectionId = id;
				}
				String name = rset.getString("name");
				forumSections.put(id, new ForumSection(id, name, new HashMap<Integer, ForumTheme>()));
			}
			rset.close();
			statement.close();
			
			statement = con.prepareStatement("SELECT id, name, icon, description, forumType, prefixes, section_id FROM forum_forums");
			
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				int id = rset.getInt("id");
				int sectionId = rset.getInt("section_id");
				if (id > lastForumId)
				{
					lastForumId = id;
				}
				String name = rset.getString("name");
				String icon = rset.getString("icon");
				String description = rset.getString("description");
				if ((icon == null) || (icon.length() < 1))
				{
					icon = "icon.NOIMAGE";
				}
				String forumType = rset.getString("forumType");
				String pref = rset.getString("prefixes");
				String _prefixes[] = pref != null ? pref.split(",") : new String[0];
				
				Type type = Type.Normal;
				if (forumType != null)
				{
					if (forumType.equalsIgnoreCase("Normal"))
					{
						type = Type.Normal;
					}
					else if (forumType.equalsIgnoreCase("ForGMS"))
					{
						type = Type.ForGMS;
					}
					else if (forumType.equalsIgnoreCase("SeeOnlyAuthors"))
					{
						type = Type.SeeOnlyAuthors;
					}
					else if (forumType.equalsIgnoreCase("PostOnlyGMS"))
					{
						type = Type.PostOnlyGMS;
					}
				}
				ArrayList<String> prefixes = new ArrayList<>();
				for (String prefix : _prefixes)
				{
					if (prefix.startsWith(" "))
					{
						prefix = prefix.substring(1);
					}
					if (prefix.endsWith(" "))
					{
						prefix = prefix.substring(0, prefix.length() - 1);
					}
					if (prefix.equalsIgnoreCase("none") || prefix.equalsIgnoreCase("empty") || prefix.equalsIgnoreCase("null"))
					{
						continue;
					}
					prefixes.add(prefix);
				}
				
				PreparedStatement statement1 = con.prepareStatement("SELECT id, name, text, author, views, createTime, isClosed, isSticked, prefix FROM forum_topics WHERE forum_id=" + id);
				ResultSet rset1 = statement1.executeQuery();
				HashMap<Integer, ForumTopic> topics = new HashMap<>();
				while (rset1.next())
				{
					long lastPost = 0;
					int topicId = rset1.getInt("id");
					String topicName = rset1.getString("name");
					String mainMessage = rset1.getString("text");
					Poster poster = getPoster(rset1.getInt("author"));
					if (poster == null)
					{
						statement = con.prepareStatement("SELECT authorId, name, posts FROM forum_characters WHERE authorId = " + rset1.getInt("author"));
						rset = statement.executeQuery();
						
						while (rset.next())
						{
							poster = new Poster(rset.getInt("authorId"), rset.getString("name"), rset.getInt("posts"));
							posters.put(rset.getInt("authorId"), poster);
						}
					}
					int views = rset1.getInt("views");
					long topicTime = rset1.getLong("createTime");
					boolean isClosed = rset1.getBoolean("isClosed");
					boolean isSticked = rset1.getBoolean("isSticked");
					String prefix = rset1.getString("prefix");
					if (prefix != null)
					{
						if (prefix.startsWith(" "))
						{
							prefix = prefix.substring(1);
						}
						if (prefix != null)
						{
							if (prefix.endsWith(" "))
							{
								prefix = prefix.substring(0, prefix.length() - 1);
							}
						}
					}
					String authorName = poster == null ? "Anonymous" : poster.getName();
					if (lastPost < topicTime)
					{
						lastPost = topicTime;
					}
					
					HashMap<Integer, TopicMessage> messages = new HashMap<>();
					TopicMessage lastMessage = null;
					
					PreparedStatement statement2 = con.prepareStatement("SELECT id, author, postTime, text FROM forum_messages WHERE forum_id=" + id + " AND topic_id=" + topicId + " ORDER BY id");
					ResultSet rset2 = statement2.executeQuery();
					while (rset2.next())
					{
						int messageId = rset2.getInt("id");
						Poster messagePoster = getPoster(rset2.getInt("author"));
						long postedTime = rset2.getLong("postTime");
						String message = rset2.getString("text");
						String posterName = messagePoster.getName();
						boolean putMessage = false;
						if (postedTime > lastPost)
						{
							lastPost = postedTime;
							putMessage = true;
						}
						TopicMessage mess = new TopicMessage(messageId, id, topicId, messagePoster, postedTime, message, posterName);
						messages.put(messageId, mess);
						if (putMessage)
						{
							lastMessage = mess;
						}
						if (messageId > lastMessageId)
						{
							lastMessageId = messageId;
						}
					}
					rset2.close();
					statement2.close();
					topics.put(topicId, new ForumTopic(topicId, id, topicName, mainMessage, poster, views, topicTime, lastPost, authorName, isClosed, isSticked, prefix, lastMessage, messages));
					if (topicId > lastTopicId)
					{
						lastTopicId = topicId;
					}
				}
				rset1.close();
				statement1.close();
				ForumTheme theme = new ForumTheme(id, name, icon, description, type, prefixes, sectionId, topics);
				forumThemes.put(id, theme);
				if (forumSections.containsKey(sectionId))
				{
					forumSections.get(sectionId).getThemes().put(id, theme);
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
				{
					con.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		int topics = 0;
		for (ForumTheme theme : getForumThemes().values())
		{
			if (theme.getType().equals(Type.SeeOnlyAuthors) || theme.getType().equals(Type.ForGMS))
			{
				continue;
			}
			for (ForumTopic topic : theme.getThemeTopics().values())
			{
				if (topic == null)
				{
					continue;
				}
				topics++;
			}
		}
		HashMap<Integer, ForumTopic> latestPosts = new HashMap<>();
		for (int i = 0; i < Math.min(topics, 5); i++)
		{
			for (ForumTheme theme : getForumThemes().values())
			{
				if (theme.getType().equals(Type.SeeOnlyAuthors) || theme.getType().equals(Type.ForGMS))
				{
					continue;
				}
				for (ForumTopic topic : theme.getThemeTopics().values())
				{
					if (topic == null)
					{
						continue;
					}
					if (!latestPosts.containsKey(i))
					{
						if ((i == 0) || ((i > 0) && (latestPosts.get(i - 1).getLastPost() > topic.getLastPost())))
						{
							latestPosts.put(i, topic);
							continue;
						}
						continue;
					}
					if (latestPosts.get(i).getLastPost() < topic.getLastPost())
					{
						if ((i == 0) || ((i > 0) && (latestPosts.get(i - 1).getLastPost() > topic.getLastPost())))
						{
							latestPosts.remove(i);
							latestPosts.put(i, topic);
						}
					}
				}
			}
		}
		recentPosts.clear();
		for (int i = 0; i < 5; i++)
		{
			recentPosts.add(latestPosts.get(i));
		}
	}
	
	public HashMap<Integer, ForumTheme> getForumThemes()
	{
		return forumThemes;
	}
	
	public HashMap<Integer, ForumSection> getForumSections()
	{
		return forumSections;
	}
	
	public void updateDatabase()
	{
		ThreadPoolManager.getInstance().schedule(new UpdateDatabase(false), 0);
	}
	
	public class UpdateDatabase implements Runnable
	{
		boolean needLoad;
		
		public UpdateDatabase(boolean _needLoad)
		{
			needLoad = _needLoad;
		}
		
		@Override
		public void run()
		{
			if (updateMessages)
			{
				for (Map.Entry<String, TopicMessage> message : changedPosts.entrySet())
				{
					if (message.getKey().split(";")[1].equalsIgnoreCase("add"))
					{
						TopicMessage newMessage = message.getValue();
						int messageId = newMessage.getPostId();
						int author = newMessage.getMessagePoster().getId();
						long postedTime = System.currentTimeMillis();
						int forumId = newMessage.getForumId();
						int topicId = newMessage.getTopicId();
						String messageText = newMessage.getPost();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("INSERT INTO forum_messages (id, forum_id, topic_id, author, text, postTime) VALUES (?, ?, ?, ?, ?, ?)");
							
							statement.setInt(1, messageId);
							statement.setInt(2, forumId);
							statement.setInt(3, topicId);
							statement.setInt(4, author);
							statement.setString(5, messageText);
							statement.setLong(6, postedTime);
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not write reply: " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (message.getKey().split(";")[1].equalsIgnoreCase("remove"))
					{
						TopicMessage oldMessage = message.getValue();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("DELETE FROM forum_messages WHERE id=? AND topic_id=? AND forum_id=?");
							
							statement.setInt(1, oldMessage.getPostId());
							statement.setInt(2, oldMessage.getTopicId());
							statement.setInt(3, oldMessage.getForumId());
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not remove post (" + oldMessage.getPostId() + "): " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						
					}
				}
				updateMessages = false;
				changedPosts.clear();
			}
			if (updateTopics)
			{
				for (Map.Entry<String, ForumTopic> topic : changedTopics.entrySet())
				{
					if (topic.getKey().split(";")[1].equalsIgnoreCase("add"))
					{
						ForumTopic newTopic = topic.getValue();
						int topicId = newTopic.getTopicId();
						int forumId = newTopic.getForumId();
						int author = newTopic.getMainPoster().getId();
						String topicName = newTopic.getTopicName();
						String topicMessage = newTopic.getTopicMainMessage();
						String prefix = newTopic.getPrefix() != null ? newTopic.getPrefix() : "";
						long postedTime = newTopic.getPostDate();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("INSERT INTO forum_topics (id, forum_id, author, name, text, createTime, isSticked, isClosed, prefix, views) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
							
							statement.setInt(1, topicId);
							statement.setInt(2, forumId);
							statement.setInt(3, author);
							statement.setString(4, topicName);
							statement.setString(5, topicMessage);
							statement.setLong(6, postedTime);
							statement.setInt(7, 0);
							statement.setInt(8, 0);
							statement.setString(9, prefix);
							statement.setInt(10, 0);
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not create topic: " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (topic.getKey().split(";")[1].equalsIgnoreCase("remove"))
					{
						ForumTopic oldTopic = topic.getValue();
						int topicId = oldTopic.getTopicId();
						int forumId = oldTopic.getForumId();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("DELETE FROM forum_topics WHERE id=? AND forum_id=?");
							
							statement.setInt(1, topicId);
							statement.setInt(2, forumId);
							
							statement.executeUpdate();
							statement.close();
							
							statement = con.prepareStatement("DELETE FROM forum_messages WHERE topic_id=?");
							
							statement.setInt(1, topicId);
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not remove topic (" + topicId + "): " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (topic.getKey().split(";")[1].equalsIgnoreCase("increaseViews"))
					{
						ForumTopic editingTopic = topic.getValue();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("UPDATE forum_topics SET views=? WHERE id=?");
							statement.setInt(1, editingTopic.getTopicViews());
							statement.setInt(2, editingTopic.getTopicId());
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not increase views to topic (" + editingTopic.getTopicId() + "): " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (topic.getKey().split(";")[1].equalsIgnoreCase("modify"))
					{
						ForumTopic editingTopic = topic.getValue();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("UPDATE forum_topics SET isClosed=?, isSticked=? WHERE id=?");
							
							statement.setBoolean(1, editingTopic.isClosed());
							statement.setBoolean(2, editingTopic.isSticked());
							statement.setInt(3, editingTopic.getTopicId());
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not modify topic (" + editingTopic.getTopicId() + "): " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				updateTopics = false;
				changedTopics.clear();
			}
			if (updatePlayers)
			{
				for (Map.Entry<String, Poster> changedPlayer : changedPlayers.entrySet())
				{
					String info = changedPlayer.getKey();
					Poster poster = changedPlayer.getValue();
					if (info.equalsIgnoreCase("Insert"))
					{
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("INSERT INTO forum_characters (authorId, name, posts) VALUES (?, ?, ?)");
							
							statement.setInt(1, poster.getId());
							statement.setString(2, poster.getName());
							statement.setInt(3, poster.getPostsCount());
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not instert new character info (" + poster.getId() + "): " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (info.equalsIgnoreCase("Update"))
					{
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("UPDATE forum_characters SET posts=? WHERE authorId=?");
							statement.setInt(1, poster.getPostsCount());
							statement.setInt(2, poster.getId());
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not change players posts count (" + poster.getId() + "): " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				updatePlayers = false;
				changedPlayers.clear();
			}
			if (updateForums)
			{
				for (Map.Entry<String, ForumTheme> forum : changedForums.entrySet())
				{
					if (forum.getKey().split(";")[1].equalsIgnoreCase("add"))
					{
						ForumTheme newForum = forum.getValue();
						int forumId = newForum.getThemeId();
						String forumName = newForum.getThemeName();
						String icon = newForum.getThemeIcon();
						String description = newForum.getThemeDescription();
						String forumType = newForum.getType().name();
						String prefixes = "";
						for (String prefix : newForum.getPrefixes())
						{
							prefixes += prefix + ",";
						}
						if (prefixes.length() > 0)
						{
							prefixes = prefixes.substring(0, prefixes.length() - 1);
						}
						int sectionId = newForum.getSectionId();
						
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("INSERT INTO forum_forums (id, name, icon, description, forumType, prefixes, section_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
							
							statement.setInt(1, forumId);
							statement.setString(2, forumName);
							statement.setString(3, icon);
							statement.setString(4, description);
							statement.setString(5, forumType);
							statement.setString(6, prefixes);
							statement.setInt(7, sectionId);
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not create forum: " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (forum.getKey().split(";")[1].equalsIgnoreCase("remove"))
					{
						ForumTheme oldSection = forum.getValue();
						int forumId = oldSection.getThemeId();
						Connection con = null;
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("DELETE FROM forum_forums WHERE id=?");
							
							statement.setInt(1, forumId);
							
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							System.out.println("Could not create forum: " + e);
						}
						finally
						{
							try
							{
								if (con != null)
								{
									con.close();
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					updateForums = false;
				}
				changedForums.clear();
				
				if (updateSections)
				{
					for (Map.Entry<String, ForumSection> section : changedSections.entrySet())
					{
						if (section.getKey().split(";")[1].equalsIgnoreCase("add"))
						{
							ForumSection newSection = section.getValue();
							int sectionId = newSection.getSectionId();
							String sectionName = newSection.getSectionName();
							
							Connection con = null;
							try
							{
								con = DatabaseFactory.getInstance().getConnection();
								PreparedStatement statement = con.prepareStatement("INSERT INTO forum_sections (id, name) VALUES (?, ?)");
								
								statement.setInt(1, sectionId);
								statement.setString(2, sectionName);
								
								statement.executeUpdate();
								statement.close();
							}
							catch (Exception e)
							{
								System.out.println("Could not create section: " + e);
							}
							finally
							{
								try
								{
									if (con != null)
									{
										con.close();
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
						else if (section.getKey().split(";")[1].equalsIgnoreCase("remove"))
						{
							ForumSection oldSection = section.getValue();
							int sectionId = oldSection.getSectionId();
							Connection con = null;
							try
							{
								con = DatabaseFactory.getInstance().getConnection();
								PreparedStatement statement = con.prepareStatement("DELETE FROM forum_sections WHERE id=?");
								
								statement.setInt(1, sectionId);
								
								statement.executeUpdate();
								statement.close();
							}
							catch (Exception e)
							{
								System.out.println("Could not delete section: " + e);
							}
							finally
							{
								try
								{
									if (con != null)
									{
										con.close();
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
						updateSections = false;
					}
					changedSections.clear();
				}
			}
			if (needLoad)
			{
				loadForum();
			}
		}
	}
	
	public void writeReply(Player player, int forumId, int topicId, String message)
	{
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return;
		}
		ForumTheme theme = getForumThemes().get(forumId);
		if (!theme.getThemeTopics().containsKey(topicId))
		{
			player.sendMessage("Topic does not exist!");
			return;
		}
		if (theme.getThemeTopics().get(topicId).isClosed())
		{
			player.sendMessage("Topic is closed!");
			return;
		}
		updateMessages = true;
		String info = "Update";
		Poster poster = getPoster(player.getObjectId());
		if (poster == null)
		{
			poster = new Poster(player.getObjectId(), player.getName(), 0);
			posters.put(player.getObjectId(), poster);
			info = "Insert";
		}
		long postTime = System.currentTimeMillis();
		int messageId = ++lastMessageId;
		TopicMessage newPost = new TopicMessage(messageId, forumId, topicId, poster, postTime, message, player.getName());
		changedPosts.put(messageId + ";add", newPost);
		poster.setPostsCount(poster.getPostsCount() + 1);
		getForumThemes().get(forumId).getThemeTopics().get(topicId).getTopicPosts().put(messageId, newPost);
		getForumThemes().get(forumId).getThemeTopics().get(topicId).setLastPost(postTime);
		updatePlayers = true;
		changedPlayers.put(info, poster);
		ForumTopic topic = getForumThemes().get(forumId).getThemeTopics().get(topicId);
		topic.setLastMessage(newPost);
		if (!theme.getType().equals(Type.SeeOnlyAuthors) && !theme.getType().equals(Type.ForGMS))
		{
			ArrayList<ForumTopic> posts = new ArrayList<>();
			posts.addAll(recentPosts);
			recentPosts.clear();
			recentPosts.add(topic);
			for (int i = 0; i < 4; i++)
			{
				recentPosts.add(posts.get(i));
			}
		}
	}
	
	public void removePost(Player player, int forumId, int topicId, int postId)
	{
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return;
		}
		ForumTheme theme = getForumThemes().get(forumId);
		if (!theme.getThemeTopics().containsKey(topicId))
		{
			player.sendMessage("Topic does not exist!");
			return;
		}
		ForumTopic topic = theme.getThemeTopics().get(topicId);
		if (!topic.getTopicPosts().containsKey(postId))
		{
			player.sendMessage("This post does not exist!");
			return;
		}
		if (topic.isClosed())
		{
			player.sendMessage("Topic is closed!");
			return;
		}
		TopicMessage message = getForumThemes().get(forumId).getThemeTopics().get(topicId).getTopicPosts().get(postId);
		Poster poster = message.getMessagePoster();
		if ((poster.getId() != player.getObjectId()) && !player.isGM())
		{
			player.sendMessage("You can't remove not your post!");
			return;
		}
		poster.setPostsCount(poster.getPostsCount() - 1);
		getForumThemes().get(forumId).getThemeTopics().get(topicId).getTopicPosts().remove(postId);
		changedPosts.put(message.getPostId() + ";remove", message);
		updateMessages = true;
		updatePlayers = true;
		changedPlayers.put("Update", poster);
		long lastPost = 0;
		for (TopicMessage post : topic.getTopicPosts().values())
		{
			if (post.getPostDate() > lastPost)
			{
				lastPost = post.getPostDate();
				topic.setLastMessage(post);
			}
		}
		if (lastPost == 0)
		{
			topic.setLastMessage(null);
		}
	}
	
	public int createTopic(String topicMessage, String topicName, String prefix, Player player, int forumId)
	{
		if (player == null)
		{
			return -1;
		}
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return -1;
		}
		ForumTheme theme = getForumThemes().get(forumId);
		updateTopics = true;
		String info = "Update";
		Poster poster = getPoster(player.getObjectId());
		if (poster == null)
		{
			poster = new Poster(player.getObjectId(), player.getName(), 0);
			posters.put(player.getObjectId(), poster);
			info = "Insert";
		}
		int topicId = ++lastTopicId;
		long postedTime = System.currentTimeMillis();
		HashMap<Integer, TopicMessage> messages = new HashMap<>();
		ForumTopic topic = new ForumTopic(topicId, forumId, topicName, topicMessage, poster, 0, postedTime, postedTime, player.getName(), false, false, prefix, null, messages);
		poster.setPostsCount(poster.getPostsCount() + 1);
		getForumThemes().get(forumId).getThemeTopics().put(topicId, topic);
		changedTopics.put(topicId + ";add", topic);
		updatePlayers = true;
		changedPlayers.put(info, poster);
		if (!theme.getType().equals(Type.SeeOnlyAuthors) && !theme.getType().equals(Type.ForGMS))
		{
			ArrayList<ForumTopic> posts = new ArrayList<>();
			posts.addAll(recentPosts);
			recentPosts.clear();
			recentPosts.add(topic);
			for (int i = 0; i < 4; i++)
			{
				recentPosts.add(posts.get(i));
			}
		}
		return topicId;
	}
	
	public void removeTopic(Player player, int forumId, int topicId)
	{
		if (!player.isGM())
		{
			return;
		}
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return;
		}
		if (!getForumThemes().get(forumId).getThemeTopics().containsKey(topicId))
		{
			player.sendMessage("Topic does not exist!");
			return;
		}
		ForumTopic topic = getForumThemes().get(forumId).getThemeTopics().get(topicId);
		Poster poster = topic.getMainPoster();
		poster.setPostsCount(poster.getPostsCount() - 1);
		updatePlayers = true;
		changedPlayers.put("Update", poster);
		for (TopicMessage message : topic.getTopicPosts().values())
		{
			message.getMessagePoster().setPostsCount(message.getMessagePoster().getPostsCount() - 1);
			changedPlayers.put("Update", message.getMessagePoster());
		}
		getForumThemes().get(forumId).getThemeTopics().remove(topicId);
		updateTopics = true;
		changedTopics.put(topicId + ";remove", topic);
		
		int topics = 0;
		for (ForumTheme theme : getForumThemes().values())
		{
			if (theme.getType().equals(Type.SeeOnlyAuthors) || theme.getType().equals(Type.ForGMS))
			{
				continue;
			}
			for (ForumTopic tpc : theme.getThemeTopics().values())
			{
				if (tpc == null)
				{
					continue;
				}
				topics++;
			}
		}
		
		HashMap<Integer, ForumTopic> latestPosts = new HashMap<>();
		for (int i = 0; i < Math.min(topics, 5); i++)
		{
			for (ForumTheme theme : getForumThemes().values())
			{
				if (theme.getType().equals(Type.SeeOnlyAuthors) || theme.getType().equals(Type.ForGMS))
				{
					continue;
				}
				for (ForumTopic top : theme.getThemeTopics().values())
				{
					if (top == null)
					{
						continue;
					}
					if (!latestPosts.containsKey(i))
					{
						if (((i > 0) && (latestPosts.get(i - 1).getLastPost() > top.getLastPost())) || (i == 0))
						{
							latestPosts.put(i, top);
							continue;
						}
						continue;
					}
					if (latestPosts.get(i).getLastPost() < top.getLastPost())
					{
						if (((i > 0) && (latestPosts.get(i - 1).getLastPost() > top.getLastPost())) || (i == 0))
						{
							latestPosts.remove(i);
							latestPosts.put(i, top);
						}
					}
				}
			}
		}
		recentPosts.clear();
		for (int i = 0; i < 5; i++)
		{
			recentPosts.add(latestPosts.get(i));
		}
	}
	
	public void removeForum(Player player, int forumId)
	{
		if (!player.isGM())
		{
			return;
		}
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return;
		}
		ForumTheme theme = getForumThemes().get(forumId);
		updateForums = true;
		changedForums.put(forumId + ";remove", theme);
		
		if (forumSections.containsKey(theme.getSectionId()))
		{
			forumSections.get(theme.getSectionId()).getThemes().remove(theme.getThemeId());
			if (forumSections.get(theme.getSectionId()).getThemes().size() < 1)
			{
				updateSections = true;
				changedSections.put(theme.getSectionId() + ";remove", forumSections.get(theme.getSectionId()));
				forumSections.remove(theme.getSectionId());
			}
		}
		
		for (ForumTopic tpc : theme.getThemeTopics().values())
		{
			if (tpc == null)
			{
				continue;
			}
			removeTopic(player, forumId, tpc.getTopicId());
		}
		getForumThemes().remove(forumId);
	}
	
	public void increaseViews(Player player, int forumId, int topicId)
	{
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return;
		}
		if (!getForumThemes().get(forumId).getThemeTopics().containsKey(topicId))
		{
			player.sendMessage("Topic does not exist!");
			return;
		}
		ForumTopic topic = getForumThemes().get(forumId).getThemeTopics().get(topicId);
		if ((topic.getMainPoster().getId() == player.getObjectId()) && !Config.FORUM_INCREASE_VIEWS_FOR_AUTHOR_VIEW)
		{
			return;
		}
		topic.addViews(1);
		updateTopics = true;
		changedTopics.put(topicId + ";increaseViews", topic);
	}
	
	public void modifyTopic(Player player, int forumId, int topicId, int modify)
	{
		if (!getForumThemes().containsKey(forumId))
		{
			player.sendMessage("Forum does not exist!");
			return;
		}
		if (!getForumThemes().get(forumId).getThemeTopics().containsKey(topicId))
		{
			player.sendMessage("Topic does not exist!");
			return;
		}
		ForumTopic topic = getForumThemes().get(forumId).getThemeTopics().get(topicId);
		if ((modify != 1) && !player.isGM())
		{
			return;
		}
		else if ((modify == 1) && !player.isGM() && ((player.getObjectId() != topic.getMainPoster().getId()) || !Config.FORUM_AUTHOR_CAN_CLOSE_TOPIC))
		{
			return;
		}
		if (topic.isClosed() && (modify == 1))
		{
			player.sendMessage("Topic already is closed!");
			return;
		}
		else if (!topic.isClosed() && (modify == 0))
		{
			player.sendMessage("Topic already is opened!");
			return;
		}
		else if (topic.isSticked() && (modify == 3))
		{
			player.sendMessage("Topic already is sticked!");
			return;
		}
		else if (!topic.isSticked() && (modify == 2))
		{
			player.sendMessage("Topic isn't sticked!");
			return;
		}
		updateTopics = true;
		switch (modify)
		{
			case 1:
				getForumThemes().get(forumId).getThemeTopics().get(topicId).isClosed(true);
				break;
			case 0:
				getForumThemes().get(forumId).getThemeTopics().get(topicId).isClosed(false);
				break;
			case 3:
				getForumThemes().get(forumId).getThemeTopics().get(topicId).isSticked(true);
				break;
			case 2:
				getForumThemes().get(forumId).getThemeTopics().get(topicId).isSticked(false);
				break;
		}
		changedTopics.put(topicId + ";modify", topic);
	}
	
	public int createForum(String forumName, String icon, String description, String forumType, String _prefixes[], String sectionName, Player player)
	{
		if (player == null)
		{
			return -1;
		}
		
		updateForums = true;
		int forumId = ++lastForumId;
		forumType = forumType.replaceAll(" ", "");
		Type type = Type.Normal;
		if (forumType.equalsIgnoreCase("Normal"))
		{
			type = Type.Normal;
		}
		else if (forumType.equalsIgnoreCase("ForGMS"))
		{
			type = Type.ForGMS;
		}
		else if (forumType.equalsIgnoreCase("SeeonlyAuthors"))
		{
			type = Type.SeeOnlyAuthors;
		}
		else if (forumType.equalsIgnoreCase("PostOnlyGMS"))
		{
			type = Type.PostOnlyGMS;
		}
		
		ArrayList<String> prefixes = new ArrayList<>();
		for (String prefix : _prefixes)
		{
			if (prefix == null)
			{
				continue;
			}
			if (prefix.length() < 1)
			{
				continue;
			}
			if (prefix.startsWith(" "))
			{
				prefix = prefix.substring(1);
			}
			if (prefix.endsWith(" "))
			{
				prefix = prefix.substring(0, prefix.length() - 1);
			}
			if (prefix.equalsIgnoreCase("none") || prefix.equalsIgnoreCase("empty") || prefix.equalsIgnoreCase("null"))
			{
				continue;
			}
			prefixes.add(prefix);
		}
		
		int sectionId = -1;
		for (ForumSection section : forumSections.values())
		{
			if (section.getSectionName().trim().equalsIgnoreCase(sectionName.trim()))
			{
				sectionId = section.getSectionId();
				break;
			}
		}
		
		HashMap<Integer, ForumTopic> topics = new HashMap<>();
		ForumTheme forum = new ForumTheme(forumId, forumName, icon, description, type, prefixes, sectionId, topics);
		getForumThemes().put(forumId, forum);
		changedForums.put(forumId + ";add", forum);
		if (forumSections.containsKey(sectionId))
		{
			forumSections.get(sectionId).getThemes().put(forumId, forum);
		}
		return forumId;
	}
	
	public int createSection(String sectionName, Player player)
	{
		if (player == null)
		{
			return -1;
		}
		
		updateSections = true;
		int sectionId = ++lastSectionId;
		
		HashMap<Integer, ForumTheme> themes = new HashMap<>();
		ForumSection section = new ForumSection(sectionId, sectionName, themes);
		forumSections.put(sectionId, section);
		changedSections.put(sectionId + ";add", section);
		return sectionId;
	}
	
	public ArrayList<ForumTopic> getRecentPosts()
	{
		return recentPosts;
	}
	
	public static final ForumParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ForumParser _instance = new ForumParser();
	}
}