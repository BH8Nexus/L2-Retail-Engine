package l2r.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2r.commons.math.SafeMath;
import l2r.gameserver.Config;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemActionLog;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.ItemStateLog;
import l2r.gameserver.utils.Log;

public abstract class ItemContainer
{
	protected static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
	
	protected final List<ItemInstance> _items = new ArrayList<>();
	/** Ð‘Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²ÐºÐ° Ð´Ð»Ñ� Ñ‡Ñ‚ÐµÐ½Ð¸Ñ�/Ð·Ð°Ð¿Ð¸Ñ�Ð¸ Ð²ÐµÑ‰ÐµÐ¹ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ° Ð¸ Ð²Ð½ÐµÑˆÐ½Ð¸Ñ… Ð¾Ð¿ÐµÑ€Ð°Ñ†Ð¸Ð¹ */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	protected final Lock readLock = lock.readLock();
	protected final Lock writeLock = lock.writeLock();
	
	protected ItemContainer()
	{
		
	}
	
	public int getSize()
	{
		return _items.size();
	}
	
	public ItemInstance[] getItems()
	{
		readLock();
		try
		{
			return _items.toArray(new ItemInstance[_items.size()]);
		}
		finally
		{
			readUnlock();
		}
	}
	
	public void clear()
	{
		writeLock();
		try
		{
			_items.clear();
		}
		finally
		{
			writeUnlock();
		}
	}
	
	public final void writeLock()
	{
		writeLock.lock();
	}
	
	public final void writeUnlock()
	{
		writeLock.unlock();
	}
	
	public final void readLock()
	{
		readLock.lock();
	}
	
	public final void readUnlock()
	{
		readLock.unlock();
	}
	
	/**
	 * Ð�Ð°Ð¹Ñ‚Ð¸ Ð²ÐµÑ‰ÑŒ Ð¿Ð¾ objectId
	 * @param objectId
	 * @return Ð²ÐµÑ‰ÑŒ, ÐµÑ�Ð»Ð¸ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°, Ð»Ð¸Ð±Ð¾ null ÐµÑ�Ð»Ð¸ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°
	 */
	public ItemInstance getItemByObjectId(int objectId)
	{
		readLock();
		try
		{
			ItemInstance item;
			for (ItemInstance _item : _items)
			{
				item = _item;
				if (item.getObjectId() == objectId)
				{
					return item;
				}
			}
		}
		finally
		{
			readUnlock();
		}
		
		return null;
	}
	
	/**
	 * Ð�Ð°Ð¹Ñ‚Ð¸ Ð¿ÐµÑ€Ð²ÑƒÑŽ Ð²ÐµÑ‰ÑŒ Ð¿Ð¾ itemId
	 * @param itemId
	 * @return Ð²ÐµÑ‰ÑŒ, ÐµÑ�Ð»Ð¸ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°, Ð»Ð¸Ð±Ð¾ null ÐµÑ�Ð»Ð¸ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°
	 */
	public ItemInstance getItemByItemId(int itemId)
	{
		readLock();
		try
		{
			ItemInstance item;
			for (ItemInstance _item : _items)
			{
				item = _item;
				if (item.getItemId() == itemId)
				{
					return item;
				}
			}
		}
		finally
		{
			readUnlock();
		}
		
		return null;
	}
	
	/**
	 * Ð�Ð°Ð¹Ñ‚Ð¸ Ð²Ñ�Ðµ Ð²ÐµÑ‰Ð¸ Ð¿Ð¾ itemId
	 * @param itemId
	 * @return Ð¡Ð¿Ð¸Ñ�Ð¾Ðº Ð½Ð°Ð¹Ð´ÐµÐ½Ñ‹Ñ… Ð²ÐµÑ‰ÐµÐ¹
	 */
	public List<ItemInstance> getItemsByItemId(int itemId)
	{
		List<ItemInstance> result = new ArrayList<>();
		
		readLock();
		try
		{
			ItemInstance item;
			for (int i = 0; i < _items.size(); i++)
			{
				item = _items.get(i);
				if (item.getItemId() == itemId)
				{
					result.add(item);
				}
			}
		}
		finally
		{
			readUnlock();
		}
		
		return result;
	}
	
	public long getCountOf(int itemId)
	{
		long count = 0L;
		readLock();
		try
		{
			ItemInstance item;
			for (ItemInstance _item : _items)
			{
				item = _item;
				if (item.getItemId() == itemId)
				{
					count = SafeMath.addAndLimit(count, item.getCount());
				}
			}
		}
		finally
		{
			readUnlock();
		}
		return count;
	}
	
	/**
	 * Ð¡Ð¾Ð·Ð´Ð°Ñ‚ÑŒ Ð²ÐµÑ‰ÑŒ Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð¸Ñ‚ÑŒ Ð² Ñ�Ð¿Ð¸Ñ�Ð¾Ðº, Ð»Ð¸Ð±Ð¾ ÑƒÐ²ÐµÐ»Ð¸Ñ‡Ð¸Ñ‚ÑŒ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð²ÐµÑ‰Ð¸ Ð² Ð¸Ð½Ð²ÐµÐ½Ñ‚Ð°Ñ€Ðµ
	 * @param itemId - Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ itemId Ð²ÐµÑ‰Ð¸
	 * @param count - ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð´Ð»Ñ� Ñ�Ð¾Ð·Ð´Ð°Ð½Ð¸Ñ�, Ð»Ð¸Ð±Ð¾ ÑƒÐ²ÐµÐ»Ð¸Ñ‡ÐµÐ½Ð¸Ñ�
	 * @return Ñ�Ð¾Ð·Ð´Ð°Ð½Ð½Ð°Ñ� Ð²ÐµÑ‰ÑŒ
	 */
	public ItemInstance addItem(int itemId, long count, String owner, String log)
	{
		if (count < 1)
		{
			return null;
		}
		
		ItemInstance item;
		
		writeLock();
		try
		{
			item = getItemByItemId(itemId);
			
			if ((item != null) && item.isStackable())
			{
				synchronized (item)
				{
					item.setCount(SafeMath.addAndLimit(item.getCount(), count));
					
					// Synerge - Max adena count
					if ((itemId == ItemTemplate.ITEM_ID_ADENA) && (Config.MAX_ADENA > -1) && (item.getCount() > Config.MAX_ADENA))
					{
						item.setCount(Config.MAX_ADENA);
					}
					
					onModifyItem(item);
					if ((owner != null) && (log != null))
					{
						Log.logItemActions(new ItemActionLog(ItemStateLog.ADD, log, owner, item, count));
					}
				}
			}
			else
			{
				item = ItemFunctions.createItem(itemId);
				item.setCount(count);
				
				// Synerge - Max adena count
				if ((itemId == ItemTemplate.ITEM_ID_ADENA) && (Config.MAX_ADENA > -1) && (item.getCount() > Config.MAX_ADENA))
				{
					item.setCount(Config.MAX_ADENA);
				}
				
				_items.add(item);
				onAddItem(item);
				
				if ((owner != null) && (log != null))
				{
					Log.logItemActions(new ItemActionLog(ItemStateLog.ADD, log, owner, item, count));
				}
			}
		}
		finally
		{
			writeUnlock();
		}
		
		return item;
	}
	
	/**
	 * Ð”Ð¾Ð±Ð°Ð²Ð¸Ñ‚ÑŒ Ð²ÐµÑ‰ÑŒ Ð² Ñ�Ð¿Ð¸Ñ�Ð¾Ðº.<br>
	 * ÐŸÑ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½ÐµÑ�ÐºÐ¾Ð»ÑŒÐºÐ¸Ñ… Ð²ÐµÑ‰ÐµÐ¹ Ð¿Ð¾Ð´Ñ€Ñ�Ð´, Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð´Ð¾Ð»Ð¶ÐµÐ½ Ð±Ñ‹Ñ‚ÑŒ Ð·Ð°Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½ Ñ� writeLock() Ð¸ Ñ€Ð°Ð·Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½ Ð¿Ð¾Ñ�Ð»Ðµ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ñ� Ñ� writeUnlock()<br>
	 * <br>
	 * <b><font color="red">Ð”Ð¾Ð»Ð¶Ð½Ð¾ Ð²Ñ‹Ð¿Ð¾Ð»Ð½Ñ�Ñ‚Ñ�Ñ� Ð² Ð±Ð»Ð¾ÐºÐµ synchronized(item)</font></b>
	 * @return Ð²ÐµÑ‰ÑŒ, Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð°Ñ� Ð² Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ðµ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ñ�, null ÐµÑ�Ð»Ð¸ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°
	 */
	public ItemInstance addItem(ItemInstance item, String owner, String log)
	{
		if (item == null)
		{
			return null;
		}
		
		if (item.getCount() < 1)
		{
			return null;
		}
		
		ItemInstance result = null;
		
		writeLock();
		try
		{
			if (getItemByObjectId(item.getObjectId()) != null)
			{
				return null;
			}
			
			long countToAdd = item.getCount();
			if (item.isStackable())
			{
				int itemId = item.getItemId();
				result = getItemByItemId(itemId);
				if (result != null)
				{
					synchronized (result)
					{
						// ÑƒÐ²ÐµÐ»Ð¸Ñ‡Ð¸Ñ‚ÑŒ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð² Ñ�Ñ‚Ð¾Ð¿ÐºÐµ
						result.setCount(SafeMath.addAndLimit(item.getCount(), result.getCount()));
						onModifyItem(result);
						onDestroyItem(item);
						
					}
				}
			}
			
			if (result == null)
			{
				_items.add(item);
				result = item;
				
				onAddItem(result);
			}
			
			if ((owner != null) && (log != null))
			{
				Log.logItemActions(new ItemActionLog(ItemStateLog.ADD, log, owner, result, countToAdd));
			}
		}
		finally
		{
			writeUnlock();
		}
		
		return result;
	}
	
	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°, Ð»Ð¸Ð±Ð¾ ÑƒÐ¼ÐµÐ½ÑŒÑˆÐ°ÐµÑ‚ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð²ÐµÑ‰Ð¸ Ð¿Ð¾ objectId
	 * @param objectId - Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ objectId Ð²ÐµÑ‰Ð¸
	 * @param count - Ð½Ð° ÐºÐ°ÐºÐ¾Ðµ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ ÑƒÐ¼ÐµÐ½ÑŒÑˆÐ¸Ñ‚ÑŒ, ÐµÑ�Ð»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ñ€Ð°Ð²Ð½Ð¾ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð²ÐµÑ‰Ð¸, Ñ‚Ð¾ Ð²ÐµÑ‰ÑŒ ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°
	 * @return Ð²ÐµÑ‰ÑŒ, Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð°Ñ� Ð² Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ðµ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�, null ÐµÑ�Ð»Ð¸ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°
	 */
	public ItemInstance removeItemByObjectId(int objectId, long count, String owner, String log)
	{
		if (count < 1)
		{
			return null;
		}
		
		ItemInstance result;
		
		writeLock();
		try
		{
			ItemInstance item;
			if ((item = getItemByObjectId(objectId)) == null)
			{
				return null;
			}
			
			synchronized (item)
			{
				result = removeItem(item, count, owner, log);
			}
		}
		finally
		{
			writeUnlock();
		}
		
		return result;
	}
	
	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°, Ð»Ð¸Ð±Ð¾ ÑƒÐ¼ÐµÐ½ÑŒÑˆÐ°ÐµÑ‚ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¿ÐµÑ€Ð²Ð¾Ð¹ Ð½Ð°Ð¹Ð´ÐµÐ½Ð½Ð¾Ð¹ Ð²ÐµÑ‰Ð¸ Ð¿Ð¾ itemId
	 * @param itemId - Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ itemId
	 * @param count - Ð½Ð° ÐºÐ°ÐºÐ¾Ðµ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ ÑƒÐ¼ÐµÐ½ÑŒÑˆÐ¸Ñ‚ÑŒ, ÐµÑ�Ð»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ñ€Ð°Ð²Ð½Ð¾ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð²ÐµÑ‰Ð¸, Ñ‚Ð¾ Ð²ÐµÑ‰ÑŒ ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°
	 * @return Ð²ÐµÑ‰ÑŒ, Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð°Ñ� Ð² Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ðµ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�, null ÐµÑ�Ð»Ð¸ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½Ð°
	 */
	public ItemInstance removeItemByItemId(int itemId, long count, String owner, String log)
	{
		if (count < 1)
		{
			return null;
		}
		
		ItemInstance result;
		
		writeLock();
		try
		{
			ItemInstance item;
			if ((item = getItemByItemId(itemId)) == null)
			{
				return null;
			}
			
			synchronized (item)
			{
				result = removeItem(item, count, owner, log);
			}
		}
		finally
		{
			writeUnlock();
		}
		
		return result;
	}
	
	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°, Ð»Ð¸Ð±Ð¾ ÑƒÐ¼ÐµÐ½ÑŒÑˆÐ°ÐµÑ‚ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð²ÐµÑ‰Ð¸.<br>
	 * ÐŸÑ€Ð¸ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ð¸ Ð½ÐµÑ�ÐºÐ¾Ð»ÑŒÐºÐ¸Ñ… Ð²ÐµÑ‰ÐµÐ¹ Ð¿Ð¾Ð´Ñ€Ñ�Ð´, Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð´Ð¾Ð»Ð¶ÐµÐ½ Ð±Ñ‹Ñ‚ÑŒ Ð·Ð°Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½ Ñ� writeLock() Ð¸ Ñ€Ð°Ð·Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½ Ð¿Ð¾Ñ�Ð»Ðµ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ñ� Ñ� writeUnlock()<br>
	 * <br>
	 * <b><font color="red">Ð”Ð¾Ð»Ð¶Ð½Ð¾ Ð²Ñ‹Ð¿Ð¾Ð»Ð½Ñ�Ñ‚Ñ�Ñ� Ð² Ð±Ð»Ð¾ÐºÐµ synchronized(item)</font></b>
	 * @param item - Ð²ÐµÑ‰ÑŒ Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 * @param count - Ð½Ð° ÐºÐ°ÐºÐ¾Ðµ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ ÑƒÐ¼ÐµÐ½ÑŒÑˆÐ¸Ñ‚ÑŒ, ÐµÑ�Ð»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ñ€Ð°Ð²Ð½Ð¾ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð²ÐµÑ‰Ð¸, Ñ‚Ð¾ Ð²ÐµÑ‰ÑŒ ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°
	 * @return Ð²ÐµÑ‰ÑŒ, Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð°Ñ� Ð² Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ðµ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 */
	public ItemInstance removeItem(ItemInstance item, long count, String owner, String log)
	{
		if (item == null)
		{
			return null;
		}
		
		if (count < 1)
		{
			return null;
		}
		
		if (item.getCount() < count)
		{
			return null;
		}
		
		writeLock();
		try
		{
			if (!_items.contains(item))
			{
				return null;
			}
			
			if (item.getCount() > count)
			{
				if ((owner != null) && (log != null))
				{
					Log.logItemActions(new ItemActionLog(ItemStateLog.REMOVE, log, owner, item, count));
				}
				
				item.setCount(item.getCount() - count);
				onModifyItem(item);
				
				ItemInstance newItem = new ItemInstance(IdFactory.getInstance().getNextId(), item.getItemId());
				newItem.setCount(count);
				
				return newItem;
			}
			return removeItem(item, owner, log);
		}
		finally
		{
			writeUnlock();
		}
	}
	
	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°.<br>
	 * ÐŸÑ€Ð¸ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ð¸ Ð½ÐµÑ�ÐºÐ¾Ð»ÑŒÐºÐ¸Ñ… Ð²ÐµÑ‰ÐµÐ¹ Ð¿Ð¾Ð´Ñ€Ñ�Ð´, Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð´Ð¾Ð»Ð¶ÐµÐ½ Ð±Ñ‹Ñ‚ÑŒ Ð·Ð°Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½ Ñ� writeLock() Ð¸ Ñ€Ð°Ð·Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½ Ð¿Ð¾Ñ�Ð»Ðµ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ñ� Ñ� writeUnlock()<br>
	 * <br>
	 * <b><font color="red">Ð”Ð¾Ð»Ð¶Ð½Ð¾ Ð²Ñ‹Ð¿Ð¾Ð»Ð½Ñ�Ñ‚Ñ�Ñ� Ð² Ð±Ð»Ð¾ÐºÐµ synchronized(item)</font></b>
	 * @param item - Ð²ÐµÑ‰ÑŒ Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 * @return Ð²ÐµÑ‰ÑŒ, Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð°Ñ� Ð² Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ðµ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 */
	public ItemInstance removeItem(ItemInstance item, String owner, String log)
	{
		if (item == null)
		{
			return null;
		}
		
		writeLock();
		try
		{
			if (!_items.remove(item))
			{
				return null;
			}
			
			onRemoveItem(item);
			
			if ((owner != null) && (log != null))
			{
				Log.logItemActions(new ItemActionLog(ItemStateLog.DESTROY, log, owner, item, item.getCount()));
			}
			
			return item;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	/**
	 * Ð£Ð½Ð¸Ñ‡Ñ‚Ð¾Ð¶Ð¸Ñ‚ÑŒ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°, Ð»Ð¸Ð±Ð¾ Ñ�Ð½Ð¸Ð·Ð¸Ñ‚ÑŒ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¿Ð¾ Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ñƒ objectId
	 * @param objectId
	 * @param count - ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 * @return true, ÐµÑ�Ð»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð±Ñ‹Ð»Ð¾ Ñ�Ð½Ð¸Ð¶ÐµÐ½Ð¾ Ð¸Ð»Ð¸ Ð²ÐµÑ‰ÑŒ Ð±Ñ‹Ð»Ð° ÑƒÐ½Ð¸Ñ‡Ñ‚Ð¾Ð¶ÐµÐ½Ð°
	 */
	public boolean destroyItemByObjectId(int objectId, long count, String owner, String log)
	{
		writeLock();
		try
		{
			ItemInstance item;
			if ((item = getItemByObjectId(objectId)) == null)
			{
				return false;
			}
			
			synchronized (item)
			{
				return destroyItem(item, count, owner, log);
			}
		}
		finally
		{
			writeUnlock();
		}
	}
	
	/**
	 * Ð£Ð½Ð¸Ñ‡Ñ‚Ð¾Ð¶Ð¸Ñ‚ÑŒ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°, Ð»Ð¸Ð±Ð¾ Ñ�Ð½Ð¸Ð·Ð¸Ñ‚ÑŒ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¿Ð¾ Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ñƒ itemId
	 * @param itemId
	 * @param count - ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 * @return true, ÐµÑ�Ð»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð±Ñ‹Ð»Ð¾ Ñ�Ð½Ð¸Ð¶ÐµÐ½Ð¾ Ð¸Ð»Ð¸ Ð²ÐµÑ‰ÑŒ Ð±Ñ‹Ð»Ð° ÑƒÐ½Ð¸Ñ‡Ñ‚Ð¾Ð¶ÐµÐ½Ð°
	 */
	public boolean destroyItemByItemId(int itemId, long count, String owner, String log)
	{
		writeLock();
		try
		{
			ItemInstance item;
			if ((item = getItemByItemId(itemId)) == null)
			{
				return false;
			}
			
			synchronized (item)
			{
				return destroyItem(item, count, owner, log);
			}
		}
		finally
		{
			writeUnlock();
		}
	}
	
	/**
	 * Ð£Ð½Ð¸Ñ‡Ñ‚Ð¾Ð¶Ð¸Ñ‚ÑŒ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°, Ð»Ð¸Ð±Ð¾ Ñ�Ð½Ð¸Ð·Ð¸Ñ‚ÑŒ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾<br>
	 * <br>
	 * <b><font color="red">Ð”Ð¾Ð»Ð¶Ð½Ð¾ Ð²Ñ‹Ð¿Ð¾Ð»Ð½Ñ�Ñ‚Ñ�Ñ� Ð² Ð±Ð»Ð¾ÐºÐµ synchronized(item)</font></b>
	 * @param count - ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 * @return true, ÐµÑ�Ð»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð±Ñ‹Ð»Ð¾ Ñ�Ð½Ð¸Ð¶ÐµÐ½Ð¾ Ð¸Ð»Ð¸ Ð²ÐµÑ‰ÑŒ Ð±Ñ‹Ð»Ð° ÑƒÐ½Ð¸Ñ‡Ñ‚Ð¾Ð¶ÐµÐ½Ð°
	 */
	public boolean destroyItem(ItemInstance item, long count, String owner, String log)
	{
		if (item == null)
		{
			return false;
		}
		
		if (count < 1)
		{
			return false;
		}
		
		if (item.getCount() < count)
		{
			return false;
		}
		
		writeLock();
		try
		{
			if (!_items.contains(item))
			{
				return false;
			}
			
			if (item.getCount() > count)
			{
				if ((owner != null) && (log != null))
				{
					Log.logItemActions(new ItemActionLog(ItemStateLog.DESTROY, log, owner, item, count));
				}
				
				item.setCount(item.getCount() - count);
				onModifyItem(item);
				
				return true;
			}
			return destroyItem(item, owner, log);
		}
		finally
		{
			writeUnlock();
		}
	}
	
	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²ÐµÑ‰ÑŒ Ð¸Ð· Ñ�Ð¿Ð¸Ñ�ÐºÐ°.<br>
	 * <br>
	 * <b><font color="red">Ð”Ð¾Ð»Ð¶Ð½Ð¾ Ð²Ñ‹Ð¿Ð¾Ð»Ð½Ñ�Ñ‚Ñ�Ñ� Ð² Ð±Ð»Ð¾ÐºÐµ synchronized(item)</font></b>
	 * @param item - Ð²ÐµÑ‰ÑŒ Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 * @return Ð²ÐµÑ‰ÑŒ, Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð°Ñ� Ð² Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ðµ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
	 */
	public boolean destroyItem(ItemInstance item, String owner, String log)
	{
		if (item == null)
		{
			return false;
		}
		
		writeLock();
		try
		{
			if (!_items.remove(item))
			{
				return false;
			}
			
			if ((owner != null) && (log != null))
			{
				Log.logItemActions(new ItemActionLog(ItemStateLog.DESTROY, log, owner, item, item.getCount()));
			}
			onRemoveItem(item);
			onDestroyItem(item);
			
			return true;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	protected abstract void onAddItem(ItemInstance item);
	
	protected abstract void onModifyItem(ItemInstance item);
	
	protected abstract void onRemoveItem(ItemInstance item);
	
	protected abstract void onDestroyItem(ItemInstance item);
}
