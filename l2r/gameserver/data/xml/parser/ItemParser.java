package l2r.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.data.xml.holder.OptionDataHolder;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.stats.conditions.Condition;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.OptionDataTemplate;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.item.ArmorTemplate;
import l2r.gameserver.templates.item.Bodypart;
import l2r.gameserver.templates.item.EtcItemTemplate;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.WeaponTemplate;

public final class ItemParser extends StatParser<ItemHolder>
{
	private static final ItemParser _instance = new ItemParser();
	
	public static ItemParser getInstance()
	{
		return _instance;
	}
	
	protected ItemParser()
	{
		super(ItemHolder.getInstance());
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/stats/items/");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	protected void readData(org.dom4j.Element rootElement) throws Exception
	{
		for (final Iterator<org.dom4j.Element> itemIterator = rootElement.elementIterator(); itemIterator.hasNext();)
		{
			final org.dom4j.Element itemElement = itemIterator.next();
			
			final StatsSet set = new StatsSet();
			set.set("item_id", itemElement.attributeValue("id"));
			set.set("name", itemElement.attributeValue("name"));
			set.set("add_name", itemElement.attributeValue("add_name", StringUtils.EMPTY));
			
			int displayId = itemElement.attributeValue("displayId") == null ? Integer.parseInt(itemElement.attributeValue("id")) : Integer.parseInt(itemElement.attributeValue("displayId"));
			set.set("displayId", displayId);
			
			int slot = 0;
			for (final Iterator<org.dom4j.Element> subIterator = itemElement.elementIterator(); subIterator.hasNext();)
			{
				final org.dom4j.Element subElement = subIterator.next();
				final String subName = subElement.getName();
				if (subName.equalsIgnoreCase("set"))
				{
					set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
				}
				else if (subName.equalsIgnoreCase("equip"))
				{
					for (final Iterator<org.dom4j.Element> slotIterator = subElement.elementIterator(); slotIterator.hasNext();)
					{
						final org.dom4j.Element slotElement = slotIterator.next();
						final Bodypart bodypart = Bodypart.valueOf(slotElement.attributeValue("id"));
						if (bodypart.getReal() != null)
						{
							slot = bodypart.mask();
						}
						else
						{
							slot |= bodypart.mask();
						}
					}
				}
			}
			
			set.set("bodypart", slot);
			
			ItemTemplate template = null;
			try
			{
				if (itemElement.getName().equalsIgnoreCase("weapon"))
				{
					if (!set.containsKey("class"))
					{
						if ((slot & ItemTemplate.SLOT_L_HAND) > 0)
						{
							set.set("class", ItemTemplate.ItemClass.ARMOR);
						}
						else
						{
							set.set("class", ItemTemplate.ItemClass.WEAPON);
						}
					}
					template = new WeaponTemplate(set);
				}
				else if (itemElement.getName().equalsIgnoreCase("armor"))
				{
					if (!set.containsKey("class"))
					{
						if ((slot & ItemTemplate.SLOTS_ARMOR) > 0)
						{
							set.set("class", ItemTemplate.ItemClass.ARMOR);
						}
						else if ((slot & ItemTemplate.SLOTS_JEWELRY) > 0)
						{
							set.set("class", ItemTemplate.ItemClass.JEWELRY);
						}
						else
						{
							set.set("class", ItemTemplate.ItemClass.ACCESSORY);
						}
					}
					template = new ArmorTemplate(set);
				}
				else
				{
					template = new EtcItemTemplate(set);
				}
			}
			catch (final Exception e)
			{
				// for(Map.Entry<String, Object> entry : set.entrySet())
				// {
				// info("set " + entry.getKey() + ":" + entry.getValue());
				// }
				warn("Fail create item: " + set.get("item_id"), e);
				continue;
			}
			
			for (final Iterator<org.dom4j.Element> subIterator = itemElement.elementIterator(); subIterator.hasNext();)
			{
				final org.dom4j.Element subElement = subIterator.next();
				final String subName = subElement.getName();
				if (subName.equalsIgnoreCase("for"))
				{
					parseFor(subElement, template);
				}
				else if (subName.equalsIgnoreCase("triggers"))
				{
					parseTriggers(subElement, template);
				}
				else if (subName.equalsIgnoreCase("skills"))
				{
					for (final Iterator<org.dom4j.Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						final org.dom4j.Element nextElement = nextIterator.next();
						final int id = Integer.parseInt(nextElement.attributeValue("id"));
						final int level = Integer.parseInt(nextElement.attributeValue("level"));
						
						final Skill skill = SkillTable.getInstance().getInfo(id, level);
						
						if (skill != null)
						{
							template.attachSkill(skill);
						}
						else
						{
							info("Skill not found(" + id + "," + level + ") for item:" + set.getObject("item_id") + "; file:" + getCurrentFileName());
						}
					}
				}
				else if (subName.equalsIgnoreCase("enchant4_skill"))
				{
					final int id = Integer.parseInt(subElement.attributeValue("id"));
					final int level = Integer.parseInt(subElement.attributeValue("level"));
					
					final Skill skill = SkillTable.getInstance().getInfo(id, level);
					if (skill != null)
					{
						template.setEnchant4Skill(skill);
					}
				}
				else if (subName.equalsIgnoreCase("cond"))
				{
					final Condition condition = parseFirstCond(subElement);
					if (condition != null)
					{
						final int msgId = parseNumber(subElement.attributeValue("msgId")).intValue();
						condition.setSystemMsg(msgId);
						
						template.setCondition(condition);
					}
				}
				else if (subName.equalsIgnoreCase("attributes"))
				{
					final int[] attributes = new int[6];
					for (final Iterator<org.dom4j.Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						final org.dom4j.Element nextElement = nextIterator.next();
						Element element;
						if (nextElement.getName().equalsIgnoreCase("attribute"))
						{
							element = Element.getElementByName(nextElement.attributeValue("element"));
							attributes[element.getId()] = Integer.parseInt(nextElement.attributeValue("value"));
						}
					}
					template.setBaseAtributeElements(attributes);
				}
				else if (!Config.DONTLOADOPTIONDATA && subName.equalsIgnoreCase("enchant_options"))
				{
					for (final Iterator<org.dom4j.Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						final org.dom4j.Element nextElement = nextIterator.next();
						
						if (nextElement.getName().equalsIgnoreCase("level"))
						{
							final int val = Integer.parseInt(nextElement.attributeValue("val"));
							
							int i = 0;
							final int[] options = new int[3];
							for (final org.dom4j.Element optionElement : nextElement.elements())
							{
								final OptionDataTemplate optionData = OptionDataHolder.getInstance().getTemplate(Integer.parseInt(optionElement.attributeValue("id")));
								if (optionData == null)
								{
									error("Not found option_data for id: " + optionElement.attributeValue("id") + "; item_id: " + set.get("item_id"));
									continue;
								}
								options[i++] = optionData.getId();
							}
							template.addEnchantOptions(val, options);
						}
					}
				}
			}
			getHolder().addItem(template);
		}
	}
	
	@Override
	protected Object getTableValue(String name)
	{
		return null;
	}
}
