package l2r.gameserver.donateshop;

public class ArmorShopHolder extends ShopHolder
{
	private int _id = 0;
	private int _helmet = 0;
	private int _chest = 0;
	private int _legs = 0;
	private int _feet = 0;
	private int _gloves = 0;
	private int _price = 0;
	private String _name = "";
	
	private int armor_enchantCost;
	private int armor_foundationCost;
	private int armor_enchant;
	
	private int _fhelmet = 0;
	private int _fchest = 0;
	private int _flegs = 0;
	private int _ffeet = 0;
	private int _fgloves = 0;
	
	public ArmorShopHolder(int id, int helmet, int chest, int legs, int feet, int gloves, String name, int price, int fhelmet, int fchest, int flegs, int ffeet, int fgloves, int enchantCost, int foundationCost, int enchantValue)
	{
		_id = id;
		_helmet = helmet;
		_chest = chest;
		_legs = legs;
		_feet = feet;
		_gloves = gloves;
		_price = price;
		_name = name;
		
		_fhelmet = fhelmet;
		_fchest = fchest;
		_flegs = flegs;
		_ffeet = ffeet;
		_fgloves = fgloves;
		
		setArmorEnchantCost(enchantCost);
		setArmorFoundationCost(foundationCost);
		setArmorEnchantValue(enchantValue);
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	@Override
	public int[] getAllItemIds()
	{
		return new int[]
		{
			_helmet,
			_chest,
			_legs,
			_gloves,
			_feet
		};
	}
	
	@Override
	public int[] getAllFoundationItemIds()
	{
		return new int[]
		{
			_fhelmet,
			_fchest,
			_flegs,
			_fgloves,
			_ffeet
		};
	}
	
	public boolean hasFoundation()
	{
		return (_fhelmet > 0) && (_fchest > 0) && (_flegs > 0) && (_fgloves > 0) && (_ffeet > 0);
	}
	
	@Override
	public String getName()
	{
		return _name;
	}
	
	@Override
	public int getPrice()
	{
		return _price;
	}
	
	@Override
	public int getMainItem()
	{
		return _chest;
	}
	
	/**
	 * @return the armor_enchantCost
	 */
	public int getArmorEnchantCost()
	{
		return armor_enchantCost;
	}
	
	/**
	 * @param armor_enchantCost the armor_enchantCost to set
	 */
	public void setArmorEnchantCost(int armor_enchantCost)
	{
		this.armor_enchantCost = armor_enchantCost;
	}
	
	/**
	 * @return the armor_foundationCost
	 */
	public int getArmorFoundationCost()
	{
		return armor_foundationCost;
	}
	
	/**
	 * @param armor_foundationCost the armor_foundationCost to set
	 */
	public void setArmorFoundationCost(int armor_foundationCost)
	{
		this.armor_foundationCost = armor_foundationCost;
	}
	
	/**
	 * @return the armor_enchant
	 */
	public int getArmorEnchantValue()
	{
		return armor_enchant;
	}
	
	/**
	 * @param armor_enchant the armor_enchant to set
	 */
	public void setArmorEnchantValue(int armor_enchant)
	{
		this.armor_enchant = armor_enchant;
	}
}