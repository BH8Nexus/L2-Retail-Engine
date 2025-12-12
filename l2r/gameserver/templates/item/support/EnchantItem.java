package l2r.gameserver.templates.item.support;

import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import gnu.trove.map.hash.TIntIntHashMap;
import l2r.gameserver.templates.item.ItemTemplate;

public class EnchantItem
{
	private final int _itemId;
	private final int _chance;
	private final int _maxEnchant;
	private final EnchantType _type;
	private final ItemTemplate.Grade _grade;
	private IntSet _items = Containers.EMPTY_INT_SET;
	private TIntIntHashMap _enchantChancePerLevel;
	
	public EnchantItem(int itemId, int chance, int maxEnchant, EnchantType type, ItemTemplate.Grade grade)
	{
		_itemId = itemId;
		_chance = chance;
		_maxEnchant = maxEnchant;
		_type = type;
		_grade = grade;
	}
	
	public void addItemId(int id)
	{
		if (_items.isEmpty())
		{
			_items = new HashIntSet();
		}
		
		_items.add(id);
	}
	
	public void addEnchantChance(int level, int chance)
	{
		if (_enchantChancePerLevel == null)
		{
			_enchantChancePerLevel = new TIntIntHashMap();
		}
		
		_enchantChancePerLevel.put(level, chance);
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getChance()
	{
		return getChance(-1);
	}
	
	public int getChance(int level)
	{
		if ((level > 0) && (_enchantChancePerLevel != null) && _enchantChancePerLevel.contains(level))
		{
			return _enchantChancePerLevel.get(level);
		}
		
		return _chance;
	}
	
	public int getMaxEnchant()
	{
		return _maxEnchant;
	}
	
	public ItemTemplate.Grade getGrade()
	{
		return _grade;
	}
	
	public IntSet getItems()
	{
		return _items;
	}
	
	public EnchantType getType()
	{
		return _type;
	}
}
