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

package l2r.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2r.gameserver.model.reward.RewardItem;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.DocumentParser;

/**
 * @author UnAfraid
 */
public class ExtractableItemsData extends DocumentParser
{
	private static final Logger _log = LoggerFactory.getLogger(ExtractableItemsData.class);
	
	private final Map<Integer, List<RewardItem>> _items = new HashMap<>();
	
	protected ExtractableItemsData()
	{
	}
	
	@Override
	public final void load()
	{
		parseDatapackFile("data/xml/extractableItems.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _items.size() + " extractable items!");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						final List<RewardItem> products = new LinkedList<>();
						NamedNodeMap attrs = d.getAttributes();
						
						final int itemId = parseInteger(d.getAttributes(), "id");
						ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
						if (template == null)
						{
							_log.warn(getClass().getSimpleName() + ": Missing item template with id: " + itemId + " skipping!");
							continue;
						}
						
						boolean ok = true;
						for (Node bean = d.getFirstChild(); bean != null; bean = bean.getNextSibling())
						{
							if ("product".equalsIgnoreCase(bean.getNodeName()))
							{
								attrs = bean.getAttributes();
								final int productItemId = parseInteger(attrs, "id");
								final int min = parseInteger(attrs, "min");
								final int max = parseInteger(attrs, "max");
								final double chance = parseDouble(attrs, "chance");
								template = ItemHolder.getInstance().getTemplate(productItemId);
								if (template == null)
								{
									_log.warn(getClass().getSimpleName() + ": Missing item template with id: " + productItemId + " skipping!");
									ok = false;
									break;
								}
								
								products.add(new RewardItem(productItemId, min, max, chance));
							}
						}
						
						if (ok && !products.isEmpty())
						{
							_items.put(itemId, products);
						}
					}
				}
			}
		}
	}
	
	public List<RewardItem> getExtractableItem(Integer itemId)
	{
		return _items.get(itemId);
	}
	
	public Set<Integer> getExtractableItems()
	{
		return _items.keySet();
	}
	
	public static ExtractableItemsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ExtractableItemsData _instance = new ExtractableItemsData();
	}
}
