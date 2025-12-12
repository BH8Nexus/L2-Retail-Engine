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


public class TopicMessage
{
	int id, forumId, topicId;
	Poster poster;
	long time;
	String message, posterName;
	
	public TopicMessage(int _id, int _forumId, int _topicId, Poster _poster, long _time, String _message, String _posterName)
	{
		id = _id;
		forumId = _forumId;
		topicId = _topicId;
		poster = _poster;
		time = _time;
		message = _message;
		posterName = _posterName;
	}
	
	public int getPostId()
	{
		return id;
	}
	
	public int getForumId()
	{
		return forumId;
	}
	
	public int getTopicId()
	{
		return topicId;
	}
	
	public Poster getMessagePoster()
	{
		return poster;
	}
	
	public long getPostDate()
	{
		return time;
	}
	
	public String getPost()
	{
		return message;
	}
	
	public String getPosterName()
	{
		return posterName;
	}
}