package l2r.gameserver.donateshop;

public abstract class ShopHolder
{
	public abstract int getId();
	
	public abstract int getPrice();
	
	public abstract int getMainItem();
	
	public abstract int[] getAllItemIds();
	
	public abstract int[] getAllFoundationItemIds();
	
	public abstract String getName();
	
}
