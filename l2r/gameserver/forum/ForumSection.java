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

public class ForumSection
{
	int _id;
	String _name;
	HashMap<Integer, ForumTheme> _themes;

	public ForumSection(int id, String name, HashMap<Integer, ForumTheme> themes)
	{
		_id = id;
		_name = name;
		_themes = themes;
	}
	
	public int getSectionId()
	{
		return _id;
	}

	public String getSectionName()
	{
		return _name;
	}

	public HashMap<Integer, ForumTheme> getThemes()
	{
		return _themes;
	}
}