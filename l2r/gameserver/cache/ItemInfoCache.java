package l2r.gameserver.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInfo;
import l2r.gameserver.model.items.ItemInstance;

public class ItemInfoCache
{
	private final static ItemInfoCache _instance = new ItemInfoCache();
	
	public final static ItemInfoCache getInstance()
	{
		return _instance;
	}
	
	// private Cache cache;
	private static Map<Integer, ItemInfo> _cache;
	
	private ItemInfoCache()
	{
		// cache = CacheManager.getInstance().getCache(this.getClass().getName());
		_cache = new ConcurrentHashMap<>();
	}
	
	public void put(ItemInstance item)
	{
		// cache.put(new Element(item.getObjectId(), new ItemInfo(item)));
		_cache.put(item.getObjectId(), new ItemInfo(item));
	}
	
	/**
	 * Получить информацию из кеша, по objecId предмета. Если игрок онлайн и все еще владеет этим предметом информация будет обновлена.
	 * @param objectId - идентификатор предмета
	 * @return возвращает описание вещи, или null если описания нет, или уже удалено из кеша
	 */
	public ItemInfo get(int objectId)
	{
		// Element element = cache.get(objectId);
		if (!_cache.containsKey(objectId))
		{
			return null;
		}
		
		// ItemInfo info = null;
		// if(element != null)
		// info = (ItemInfo) element.getObjectValue();
		ItemInfo info = _cache.get(objectId);
		
		Player player = null;
		
		if (info != null)
		{
			player = World.getPlayer(info.getOwnerId());
			
			ItemInstance item = null;
			
			if (player != null)
			{
				item = player.getInventory().getItemByObjectId(objectId);
			}
			
			if (item != null)
			{
				if (item.getItemId() == info.getItemId())
				{
					// cache.put(new Element(item.getObjectId(), info = new ItemInfo(item)));
					_cache.put(item.getObjectId(), info = new ItemInfo(item));
				}
			}
		}
		
		return info;
	}
}
