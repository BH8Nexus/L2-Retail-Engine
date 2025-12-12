package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.StringTokenizer;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.templates.item.ItemTemplate;

/**
 * @author Infern0
 */
public class AcademyRewards
{
	private static ArrayList<AcademyReward> _academyRewards = new ArrayList<>();
	
	public void load()
	{
		_academyRewards.clear();
		StringTokenizer st = new StringTokenizer(Config.SERVICES_ACADEMY_REWARD, ";");
		if (st.hasMoreTokens())
		{
			int itemId = Integer.parseInt(st.nextToken());
			String itemName = "No Name";
			
			ItemTemplate tmp = ItemHolder.getInstance().getTemplate(itemId);
			if (tmp != null)
			{
				itemName = tmp.getName();
			}
			
			_academyRewards.add(new AcademyReward(itemName, itemId));
		}
	}
	
	public void reload()
	{
		load();
	}
	
	public int getItemId(String itemName)
	{
		int id = -1;
		for (AcademyReward item : _academyRewards)
		{
			if (item.getName().equalsIgnoreCase(itemName))
			{
				id = item.getItemId();
			}
		}
		
		return id;
	}
	
	public String toList()
	{
		String list = "";
		for (AcademyReward a : _academyRewards)
		{
			list += a.getName() + ";";
		}
		
		return list;
	}
	
	public class AcademyReward
	{
		private final String _itemName;
		private final int _itemId;
		
		public AcademyReward(String name, int id)
		{
			_itemName = name;
			_itemId = id;
		}
		
		public String getName()
		{
			return _itemName;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
	}
	
	public static AcademyRewards getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AcademyRewards _instance = new AcademyRewards();
	}
}
