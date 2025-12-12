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
package l2r.gameserver.model.items;

/**
 * @author JIV
 */
public class L2ExtractableProduct
{
	private int[] _id;
	private int[] _min;
	private int[] _max;
	private int _chance;
	
	public void insertNewItem(int id, int min, int max)
	{
		if (_id == null)
		{
			_id = new int[1];
			_min = new int[1];
			_max = new int[1];
		}
		else
		// resize
		{
			int[] n = new int[_id.length + 1];
			System.arraycopy(_id, 0, n, 0, _id.length);
			_id = n;
			
			n = new int[_min.length + 1];
			System.arraycopy(_min, 0, n, 0, _min.length);
			_min = n;
			
			n = new int[_max.length + 1];
			System.arraycopy(_max, 0, n, 0, _max.length);
			_max = n;
		}
		
		_id[_id.length - 1] = id;
		_min[_min.length - 1] = min;
		_max[_max.length - 1] = max;
	}
	
	public void setChance(int chance)
	{
		_chance = chance;
	}
	
	public int[] getId()
	{
		return _id;
	}
	
	public int[] getMin()
	{
		return _min;
	}
	
	public int[] getMax()
	{
		return _max;
	}
	
	public int getChance()
	{
		return _chance;
	}
}
