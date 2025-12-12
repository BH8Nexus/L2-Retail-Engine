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

import java.util.HashMap;

public class ForumTopic
{
	String name, mainMessage, authorName;
	Poster poster;
	int views, id, forumId;
	long time, lastPost;
	boolean isClosed, isSticked;
	String prefix;
	TopicMessage lastMessage;
	HashMap<Integer, TopicMessage> messages;
	
	public ForumTopic(int _id, int _forumId, String _name, String _mainMessage, Poster _poster, int _views, long _time, long _lastPost, String _authorName, boolean _isClosed, boolean _isSticked, String _prefix, TopicMessage _lastMessage, HashMap<Integer, TopicMessage> _messages)
	{
		id = _id;
		forumId = _forumId;
		name = _name;
		mainMessage = _mainMessage;
		poster = _poster;
		views = _views;
		time = _time;
		lastPost = _lastPost;
		isClosed = _isClosed;
		isSticked = _isSticked;
		messages = _messages;
		prefix = _prefix;
		lastMessage = _lastMessage;
		authorName = _authorName;
	}
	
	public int getTopicId()
	{
		return id;
	}
	
	public int getForumId()
	{
		return forumId;
	}
	
	public String getTopicName()
	{
		return name;
	}
	
	public int getTopicViews()
	{
		return views;
	}
	
	public void addViews(int count)
	{
		views += count;
	}
	
	public String getTopicMainMessage()
	{
		return mainMessage;
	}
	
	public Poster getMainPoster()
	{
		return poster;
	}
	
	public long getPostDate()
	{
		return time;
	}
	
	public long getLastPost()
	{
		return lastPost;
	}
	
	public String getAuthorName()
	{
		return authorName;
	}
	
	public boolean isClosed()
	{
		return isClosed;
	}
	
	public boolean isSticked()
	{
		return isSticked;
	}
	
	public String getPrefix()
	{
		return prefix;
	}
	
	public void isClosed(boolean _isClosed)
	{
		isClosed = _isClosed;
	}
	
	public void isSticked(boolean _isSticked)
	{
		isSticked = _isSticked;
	}
	
	public void setLastPost(long _lastPost)
	{
		lastPost = _lastPost;
	}
	
	public TopicMessage getLastMessage()
	{
		return lastMessage;
	}
	
	public void setLastMessage(TopicMessage _lastMessage)
	{
		lastMessage = _lastMessage;
	}
	
	public HashMap<Integer, TopicMessage> getTopicPosts()
	{
		return messages;
	}
}