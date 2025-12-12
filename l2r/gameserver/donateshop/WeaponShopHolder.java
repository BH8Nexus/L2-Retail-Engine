package l2r.gameserver.donateshop;

public class WeaponShopHolder extends ShopHolder
{
	private int _id = 0;
	private int _weaponId = 0;
	private int _fweaponId = 0;
	
	private int _price = 0;
	private String _name = "";
	
	private int weap_enchantCost;
	private int weap_foundationCost;
	private int weap_attributeCost;
	private int weap_enchant;
	
	public WeaponShopHolder(int id, int weaponId, int fweaponId, String name, int price, int enchantCost, int foundationCost, int attributeCost, int enchantValue)
	{
		_id = id;
		_weaponId = weaponId;
		_fweaponId = fweaponId;
		
		_price = price;
		_name = name;
		
		setWeapEnchantCost(enchantCost);
		setWeapFoundationCost(foundationCost);
		setWeapAttributeCost(attributeCost);
		setWeapEnchantValue(enchantValue);
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
			_weaponId
		};
	}
	
	@Override
	public int[] getAllFoundationItemIds()
	{
		return new int[]
		{
			_fweaponId
		};
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
		return _weaponId;
	}
	
	/**
	 * @return the weap_enchantCost
	 */
	public int getWeapEnchantCost()
	{
		return weap_enchantCost;
	}
	
	/**
	 * @param weap_enchantCost the weap_enchantCost to set
	 */
	public void setWeapEnchantCost(int weap_enchantCost)
	{
		this.weap_enchantCost = weap_enchantCost;
	}
	
	/**
	 * @return the weap_foundationCost
	 */
	public int getWeapFoundationCost()
	{
		return weap_foundationCost;
	}
	
	public boolean hasFoundation()
	{
		return _fweaponId > 0;
	}
	
	/**
	 * @param weap_foundationCost the weap_foundationCost to set
	 */
	public void setWeapFoundationCost(int weap_foundationCost)
	{
		this.weap_foundationCost = weap_foundationCost;
	}
	
	/**
	 * @return the weap_attributeCost
	 */
	public int getWeapAttributeCost()
	{
		return weap_attributeCost;
	}
	
	/**
	 * @param weap_attributeCost the weap_attributeCost to set
	 */
	public void setWeapAttributeCost(int weap_attributeCost)
	{
		this.weap_attributeCost = weap_attributeCost;
	}
	
	/**
	 * @return the weap_enchant
	 */
	public int getWeapEnchantValue()
	{
		return weap_enchant;
	}
	
	/**
	 * @param weap_enchant the weap_enchant to set
	 */
	public void setWeapEnchantValue(int weap_enchant)
	{
		this.weap_enchant = weap_enchant;
	}
}