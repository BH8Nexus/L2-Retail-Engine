package l2r.gameserver.donateshop;

public class JewelShopHolder extends ShopHolder
{
	private int _id = 0;
	private int _neck = 0;
	private int _earring = 0;
	private int _ring = 0;
	private int _price = 0;
	private String _name = "";
	
	private int jewel_enchantCost;
	private int jewel_foundationCost;
	private int jewel_enchant;
	
	private int _fneck = 0;
	private int _fearring = 0;
	private int _fring = 0;
	
	public JewelShopHolder(int id, int neck, int earring, int ring, String name, int price, int fneck, int fearring, int fring, int enchantCost, int foundationCost, int enchantValue)
	{
		_id = id;
		_neck = neck;
		_earring = earring;
		_ring = ring;
		_price = price;
		_name = name;
		
		_fneck = fneck;
		_fearring = fearring;
		_fring = fring;
		
		setJewelEnchantCost(enchantCost);
		setJewelFoundationCost(foundationCost);
		setJewelEnchantValue(enchantValue);
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
			_neck,
			_earring,
			_ring
		};
	}
	
	@Override
	public int[] getAllFoundationItemIds()
	{
		return new int[]
		{
			_fneck,
			_fearring,
			_fring
		};
	}
	
	public boolean hasFoundation()
	{
		return (_fneck > 0) && (_fearring > 0) && (_fring > 0);
	}
	
	@Override
	public int getMainItem()
	{
		return _neck;
	}
	
	public int getFNeck()
	{
		return _fneck;
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
	
	/**
	 * @return the jewel_enchantCost
	 */
	public int getJewelEnchantCost()
	{
		return jewel_enchantCost;
	}
	
	/**
	 * @param jewel_enchantCost the jewel_enchantCost to set
	 */
	public void setJewelEnchantCost(int jewel_enchantCost)
	{
		this.jewel_enchantCost = jewel_enchantCost;
	}
	
	/**
	 * @return the jewel_foundationCost
	 */
	public int getJewelFoundationCost()
	{
		return jewel_foundationCost;
	}
	
	/**
	 * @param jewel_foundationCost the jewel_foundationCost to set
	 */
	public void setJewelFoundationCost(int jewel_foundationCost)
	{
		this.jewel_foundationCost = jewel_foundationCost;
	}
	
	/**
	 * @return the jewel_enchant
	 */
	public int getJewelEnchantValue()
	{
		return jewel_enchant;
	}
	
	/**
	 * @param jewel_enchant the jewel_enchant to set
	 */
	public void setJewelEnchantValue(int jewel_enchant)
	{
		this.jewel_enchant = jewel_enchant;
	}
}