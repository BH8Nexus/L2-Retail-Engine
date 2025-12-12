package l2r.gameserver.model.reward;

import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.templates.item.ItemTemplate;

public class RewardItem
{
	public final int itemId;
	public long count;
	public boolean isAdena;
	public int enchantLvl;
	
	private long _minDrop;
	private long _maxDrop;
	private double _chance;
	
	public RewardItem(int itemId)
	{
		this.itemId = itemId;
		count = 1;
		enchantLvl = 0;
	}
	
	public RewardItem(int itemId, long min, long max, double chance)
	{
		this(itemId);
		_minDrop = min;
		_maxDrop = max;
		_chance = chance;
	}
	
	public long getMinDrop()
	{
		return _minDrop;
	}
	
	public long getMaxDrop()
	{
		return _maxDrop;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public void setMinDrop(long mindrop)
	{
		_minDrop = mindrop;
	}
	
	public void setMaxDrop(long maxdrop)
	{
		_maxDrop = maxdrop;
	}
	
	public void setChance(double chance)
	{
		_chance = chance;
	}


	
	public ItemInstance createItem()
	{
		if (count < 1)
		{
			return null;
		}
		ItemInstance item = new ItemInstance(itemId);
		{
			item.setCount(count);
			return item;
		}
	}

	public boolean isAdena()
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		if(item == null)
		{
			return false;
		}
		return item.isAdena();
	}

	public boolean isHerb()
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
		if(item == null)
		{
			return false;
		}
		return item.isHerb();
	}
}
