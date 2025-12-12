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

public class Poster
{
	int objectId, posts;
	String name;
	
	public Poster(int _objectId, String _name, int _posts)
	{
		objectId = _objectId;
		name = _name;
		posts = _posts;
	}
	
	public int getId()
	{
		return objectId;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getPostsCount()
	{
		return posts;
	}
	
	public int setPostsCount(int _posts)
	{
		posts = _posts;
		return posts;
	}
}