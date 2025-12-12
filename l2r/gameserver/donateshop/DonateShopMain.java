package l2r.gameserver.donateshop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2r.gameserver.utils.XMLUtil;

public class DonateShopMain
{
	public Map<Integer, Map<Integer, ArmorShopHolder>> _armorShop = new ConcurrentHashMap<>();
	public Map<Integer, Map<Integer, JewelShopHolder>> _jewelShop = new ConcurrentHashMap<>();
	public Map<Integer, Map<Integer, WeaponShopHolder>> _weapShop = new ConcurrentHashMap<>();
	
	public static DonateShopMain getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DonateShopMain _instance = new DonateShopMain();
	}
	
	public void reload()
	{
		_armorShop.clear();
		_jewelShop.clear();
		_weapShop.clear();
		
		load();
	}
	
	public void load()
	{
		final File localFile = new File("./config/custom/donate_shop.xml");
		if (!localFile.exists())
		{
			System.out.println("File donate_shop.xml not found!");
			return;
		}
		Document localDocument = null;
		try
		{
			final DocumentBuilderFactory localDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			localDocumentBuilderFactory.setValidating(false);
			localDocumentBuilderFactory.setIgnoringComments(true);
			localDocument = localDocumentBuilderFactory.newDocumentBuilder().parse(localFile);
		}
		catch (final Exception e1)
		{
			e1.printStackTrace();
		}
		try
		{
			parser(localDocument);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void parser(Document doc)
	{
		for (Node il = doc.getFirstChild(); il != null; il = il.getNextSibling())
		{
			if ("list".equalsIgnoreCase(il.getNodeName()))
			{
				for (Node area = il.getFirstChild(); area != null; area = area.getNextSibling())
				{
					if ("jewellist".equalsIgnoreCase(area.getNodeName()))
					{
						parseJewelFile(area);
					}
					else if ("armorlist".equalsIgnoreCase(area.getNodeName()))
					{
						parseArmorFile(area);
					}
					else if ("weaponlist".equalsIgnoreCase(area.getNodeName()))
					{
						parseWeaponFile(area);
					}
				}
			}
		}
		
	}
	
	private void parseWeaponFile(Node il)
	{
		for (Node cat = il.getFirstChild(); cat != null; cat = cat.getNextSibling())
		{
			if ("category".equalsIgnoreCase(cat.getNodeName()))
			{
				int categoryId = XMLUtil.getAttributeIntValue(cat, "id", 0);
				if (!_weapShop.containsKey(categoryId))
				{
					_weapShop.put(categoryId, new HashMap<Integer, WeaponShopHolder>());
				}
				for (Node area = cat.getFirstChild(); area != null; area = area.getNextSibling())
				{
					if ("weapon".equalsIgnoreCase(area.getNodeName()))
					{
						
						int id = XMLUtil.getAttributeIntValue(area, "id", -1);
						int weaponId = XMLUtil.getAttributeIntValue(area, "weaponId", -1);
						int fweaponId = XMLUtil.getAttributeIntValue(area, "foundationWeaponId", -1);
						String name = XMLUtil.getAttributeValue(area, "name");
						int price = XMLUtil.getAttributeIntValue(area, "price", -1);
						
						int enchantCost = XMLUtil.getAttributeIntValue(area, "enchantCost", 100);
						int foundationCost = XMLUtil.getAttributeIntValue(area, "foundationCost", 100);
						int attributeCost = XMLUtil.getAttributeIntValue(area, "attributeCost", 100);
						int enchantValue = XMLUtil.getAttributeIntValue(area, "enchantValue", 8);
						
						_weapShop.get(categoryId).put(id, new WeaponShopHolder(id, weaponId, fweaponId, name, price, enchantCost, foundationCost, attributeCost, enchantValue));
					}
				}
			}
			
		}
	}
	
	private void parseJewelFile(Node il)
	{
		for (Node cat = il.getFirstChild(); cat != null; cat = cat.getNextSibling())
		{
			if ("category".equalsIgnoreCase(cat.getNodeName()))
			{
				int categoryId = XMLUtil.getAttributeIntValue(cat, "id", 0);
				if (!_jewelShop.containsKey(categoryId))
				{
					_jewelShop.put(categoryId, new HashMap<Integer, JewelShopHolder>());
				}
				for (Node area = cat.getFirstChild(); area != null; area = area.getNextSibling())
				{
					if ("jewel".equalsIgnoreCase(area.getNodeName()))
					{
						
						int id = XMLUtil.getAttributeIntValue(area, "id", -1);
						int neck = XMLUtil.getAttributeIntValue(area, "neck", -1);
						int erring = XMLUtil.getAttributeIntValue(area, "earring", -1);
						int ring = XMLUtil.getAttributeIntValue(area, "ring", -1);
						String name = XMLUtil.getAttributeValue(area, "name");
						int price = XMLUtil.getAttributeIntValue(area, "price", -1);
						
						int fneck = XMLUtil.getAttributeIntValue(area, "foundationNeck", -1);
						int fearring = XMLUtil.getAttributeIntValue(area, "foundationEarring", -1);
						int fring = XMLUtil.getAttributeIntValue(area, "foundationRing", -1);
						
						int enchantCost = XMLUtil.getAttributeIntValue(area, "enchantCost", 100);
						int foundationCost = XMLUtil.getAttributeIntValue(area, "foundationCost", 100);
						int enchantValue = XMLUtil.getAttributeIntValue(area, "enchantValue", 6);
						
						_jewelShop.get(categoryId).put(id, new JewelShopHolder(id, neck, erring, ring, name, price, fneck, fearring, fring, enchantCost, foundationCost, enchantValue));
					}
				}
			}
			
		}
	}
	
	private void parseArmorFile(Node il)
	{
		for (Node cat = il.getFirstChild(); cat != null; cat = cat.getNextSibling())
		{
			if ("category".equalsIgnoreCase(cat.getNodeName()))
			{
				int categoryId = XMLUtil.getAttributeIntValue(cat, "id", 0);
				if (!_armorShop.containsKey(categoryId))
				{
					_armorShop.put(categoryId, new HashMap<Integer, ArmorShopHolder>());
				}
				for (Node area = cat.getFirstChild(); area != null; area = area.getNextSibling())
				{
					if ("armor".equalsIgnoreCase(area.getNodeName()))
					{
						
						int id = XMLUtil.getAttributeIntValue(area, "id", -1);
						int helmet = XMLUtil.getAttributeIntValue(area, "helmetId", -1);
						int chest = XMLUtil.getAttributeIntValue(area, "chestId", -1);
						int legs = XMLUtil.getAttributeIntValue(area, "legsId", -1);
						int feet = XMLUtil.getAttributeIntValue(area, "feetId", -1);
						int gloves = XMLUtil.getAttributeIntValue(area, "glovesId", -1);
						String name = XMLUtil.getAttributeValue(area, "name");
						int price = XMLUtil.getAttributeIntValue(area, "price", -1);
						
						int fhelmet = XMLUtil.getAttributeIntValue(area, "foundationHelmetId", -1);
						int fchest = XMLUtil.getAttributeIntValue(area, "foundationChestId", -1);
						int flegs = XMLUtil.getAttributeIntValue(area, "foundationLegsId", -1);
						int ffeet = XMLUtil.getAttributeIntValue(area, "foundationFeetId", -1);
						int fgloves = XMLUtil.getAttributeIntValue(area, "foundationGlovesId", -1);
						
						int enchantCost = XMLUtil.getAttributeIntValue(area, "enchantCost", 100);
						int foundationCost = XMLUtil.getAttributeIntValue(area, "foundationCost", 100);
						int enchantValue = XMLUtil.getAttributeIntValue(area, "enchantValue", 8);
						
						_armorShop.get(categoryId).put(id, new ArmorShopHolder(id, helmet, chest, legs, feet, gloves, name, price, fhelmet, fchest, flegs, ffeet, fgloves, enchantCost, foundationCost, enchantValue));
					}
				}
			}
			
		}
		
	}
	
}