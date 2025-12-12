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

import java.util.ArrayList;
import java.util.HashMap;

import l2r.gameserver.forum.ForumParser.Type;

public class ForumTheme
{
	int id;
	String name;
	String icon;
	String description;
	Type type;
	ArrayList<String> prefixes;
	int _sectionId;
	HashMap<Integer, ForumTopic> topics;
	
	public ForumTheme(int _id, String _name, String _icon, String _description, Type _type, ArrayList<String> _prefixes, int sectionId, HashMap<Integer, ForumTopic> _topics)
	{
		id = _id;
		name = _name;
		icon = _icon;
		description = _description;
		type = _type;
		prefixes = _prefixes;
		_sectionId = sectionId;
		topics = _topics;
	}
	
	public int getThemeId()
	{
		return id;
	}

	public String getThemeName()
	{
		return name;
	}

	public String getThemeIcon()
	{
		return icon;
	}

	public String getThemeDescription()
	{
		return description;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public ArrayList<String> getPrefixes()
	{
		return prefixes;
	}

	public int getSectionId() { return _sectionId; }
	
	public HashMap<Integer, ForumTopic> getThemeTopics()
	{
		return topics;
	}
}