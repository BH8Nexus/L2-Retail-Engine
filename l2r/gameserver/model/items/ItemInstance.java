package l2r.gameserver.model.items;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import l2r.commons.collections.LazyArrayList;
import l2r.commons.dao.JdbcEntity;
import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.attachment.FlagItemAttachment;
import l2r.gameserver.model.items.attachment.ItemAttachment;
import l2r.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2r.gameserver.network.serverpackets.ExUseSharedGroupItem;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SpawnItem;
import l2r.gameserver.scripts.Events;
import l2r.gameserver.skills.TimeStamp;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.funcs.Func;
import l2r.gameserver.stats.funcs.FuncTemplate;
import l2r.gameserver.tables.PetDataTable;
import l2r.gameserver.taskmanager.ItemsAutoDestroy;
import l2r.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2r.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.item.ItemTemplate.Grade;
import l2r.gameserver.templates.item.ItemTemplate.ItemClass;
import l2r.gameserver.templates.item.ItemType;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;

public final class ItemInstance extends GameObject implements JdbcEntity
{
	public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];
	
	private static final long serialVersionUID = 3162753878915133228L;
	
	private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
	
	/** Enumeration of locations for item */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		PET_INVENTORY,
		PET_PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT, // Î Â²Î ÎŽÎ¡ï¿½Î¡â€šÎ Â°Î Â½Î ÎŽÎ Â²Î Â»Î ÂµÎ Â½, Î ÎˆÎ¡ï¿½Î Î�Î ÎŽÎ Â»Î¡ï¿½Î Â·Î¡Æ’Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Â² Dimension Manager
		LEASE,
		MAIL
	}
	
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	
	public static final int FLAG_NO_DROP = 1 << 0;
	public static final int FLAG_NO_TRADE = 1 << 1;
	public static final int FLAG_NO_TRANSFER = 1 << 2;
	public static final int FLAG_NO_CRYSTALLIZE = 1 << 3;
	public static final int FLAG_NO_ENCHANT = 1 << 4;
	public static final int FLAG_NO_DESTROY = 1 << 5;
	public static final int FLAG_NO_UNEQUIP = 1 << 6;
	// public static final int FLAG_ALWAYS_DROP_ON_DIE = 1 << 7;
	public static final int FLAG_EQUIP_ON_PICKUP = 1 << 7;
	// public static final int FLAG_NO_RIDER_PICKUP = 1 << 9;
	// public static final int FLAG_PET_EQUIPPED = 1 << 10;
	public static final int FLAG_NO_SELL = 1 << 11;
	
	/** ID of the previous owner, if any. */
	private int oldOwnerId = -1;
	/** ID of the owner */
	private int ownerId;
	/** ID of the item */
	private int itemId;
	private int visualItemId;
	/** Quantity of the item before it was changed */
	private final long oldCount = -1;
	/** Quantity of the item */
	private long count;
	/** Level of enchantment of the item */
	private int enchantLevel = -1;
	/** Location of the item */
	private ItemLocation loc;
	/** Slot where item is stored */
	private int locData;
	/** Custom item types (used loto, race tickets) */
	private int customType1;
	private int customType2;
	/** Î â€™Î¡â‚¬Î ÂµÎ ÎŒÎ¡ï¿½ Î Â¶Î ÎˆÎ Â·Î Â½Î Îˆ Î Â²Î¡â‚¬Î ÂµÎ ÎŒÎ ÂµÎ Â½Î Â½Î¡â€¹Î¡â€¦ Î Â²Î ÂµÎ¡â€°Î ÂµÎ Î‰ */
	private int lifeTime;
	/** Î Î…Î Î�Î ÂµÎ¡â€ Î¡â€žÎ Â»Î Â°Î Â³Î Îˆ Î Î„Î Â»Î¡ï¿½ Î ÎŠÎ ÎŽÎ Â½Î ÎŠÎ¡â‚¬Î ÂµÎ¡â€šÎ Â½Î ÎŽÎ Â³Î ÎŽ Î ÎˆÎ Â½Î¡ï¿½Î¡â€šÎ Â°Î Â½Î¡ï¿½Î Â° */
	private int customFlags;
	/** Î ï¿½Î¡â€šÎ¡â‚¬Î ÎˆÎ Â±Î¡Æ’Î¡â€šÎ¡â€¹ Î Â²Î ÂµÎ¡â€°Î Îˆ */
	private ItemAttributes attrs = new ItemAttributes();
	/** Î ï¿½Î¡Æ’Î Â³Î ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°Î¡â€ Î ÎˆÎ¡ï¿½ Î Â²Î ÂµÎ¡â€°Î Îˆ */
	private int[] _enchantOptions = EMPTY_ENCHANT_OPTIONS;
	
	/** Object L2Item associated to the item */
	private ItemTemplate template;
	/** Î Â¤Î Â»Î Â°Î Â³, Î¡â€¡Î¡â€šÎ ÎŽ Î Â²Î ÂµÎ¡â€°Î¡ï¿½ Î ÎŽÎ Î„Î ÂµÎ¡â€šÎ Â°, Î Â²Î¡â€¹Î¡ï¿½Î¡â€šÎ Â°Î Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Â² Î ÎˆÎ Â½Î Â²Î ÂµÎ Â½Î¡â€šÎ Â°Î¡â‚¬Î Âµ **/
	private boolean isEquipped;
	
	/** Item drop time for autodestroy task */
	private long timeToDeleteAfterDrop;
	
	private IntSet _dropPlayers = Containers.EMPTY_INT_SET;
	private long _dropTimeOwner;
	
	private int _chargedSoulshot = CHARGED_NONE;
	private int _chargedSpiritshot = CHARGED_NONE;
	
	private boolean _chargedFishtshot = false;
	private int _augmentationId;
	private int[] _augmentations = EMPTY_AUGMENTATIONS;
	public static final int[] EMPTY_AUGMENTATIONS = new int[2];
	private int _agathionEnergy;
	private int _itemLevel;
	
	private ItemAttachment _attachment;
	private JdbcEntityState _state = JdbcEntityState.CREATED;
	
	public ItemInstance(long objectId)
	{
		super((int) objectId);
	}
	
	/**
	 * Constructor<?> of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public ItemInstance(long objectId, int itemId)
	{
		super((int) objectId);
		setItemId(itemId);
		setLifeTime(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + (getTemplate().getDurability() * 60) : getTemplate().getDurability());
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(0);
		setItemLevel(0);
	}
	
	public ItemInstance(int itemId)
	{
		super(IdFactory.getInstance().getNextId());
		setItemId(itemId);
		setLifeTime(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + (getTemplate().getDurability() * 60) : getTemplate().getDurability());
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(0);
		setItemLevel(0);
		setLocation(ItemLocation.VOID);
		setCount(1L);
	}
	
	public ItemInstance(int itemId, long count)
	{
		super(IdFactory.getInstance().getNextId());
		setItemId(itemId);
		setLifeTime(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + (getTemplate().getDurability() * 60) : getTemplate().getDurability());
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(0);
		setItemLevel(0);
		setLocation(ItemLocation.VOID);
		setCount(count);
	}
	
	/**
	 * Constructor<?> of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setItemId(itemId);
		setLifeTime(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + (getTemplate().getDurability() * 60) : getTemplate().getDurability());
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(0);
	}
	
	/**
	 * Creates a new item with the given characteristics. Copies everything from enchants and attributes to lifetime and customFlags.
	 * @param itemToCopyFrom
	 */
	public ItemInstance(ItemInstance itemToCopyFrom)
	{
		super(IdFactory.getInstance().getNextId());
		setItemId(itemToCopyFrom.getItemId());
		setLifeTime(itemToCopyFrom.getLifeTime());
		setAgathionEnergy(itemToCopyFrom.getTemplate().getAgathionEnergy());
		setLocData(itemToCopyFrom.getLocData());
		setEnchantLevel(itemToCopyFrom.getEnchantLevel());
		setItemLevel(itemToCopyFrom.getItemLevel());
		setAttributes(itemToCopyFrom.getAttributes());
		setAugmentationId(itemToCopyFrom.getAugmentationId());
		setCount(itemToCopyFrom.getCount());
		setCustomFlags(itemToCopyFrom.getCustomFlags());
	}
	
	public int getOwnerId()
	{
		return ownerId;
	}
	
	public void setOwnerId(int ownerId)
	{
		if (((this.ownerId != 0) && (oldOwnerId == -1)) || (oldOwnerId != -1))
		{
			oldOwnerId = this.ownerId;
		}
		
		this.ownerId = ownerId;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public void setItemId(int id)
	{
		itemId = id;
		template = ItemHolder.getInstance().getTemplate(id);
		setCustomFlags(getCustomFlags());
	}
	
	public int getVisualItemId()
	{
		return visualItemId;
	}
	
	public void setVisualItemId(int visualItemId)
	{
		this.visualItemId = visualItemId;
	}
	
	public int getAugmentationMineralId()
	{
		return _augmentationId;
	}
	
	public void setAugmentation(int mineralId, int[] augmentations)
	{
		_augmentationId = mineralId;
		_augmentations = augmentations;
	}
	
	public int[] getAugmentations()
	{
		return _augmentations;
	}
	
	public long getCount()
	{
		return count;
	}
	
	public void setCount(long count)
	{
		// if (((this.count != 0) && (oldCount == -1)) || (oldCount != -1))
		// {
		// oldCount = this.count;
		// }
		
		if (count < 0)
		{
			count = 0;
		}
		
		if (!isStackable() && (count > 1L))
		{
			this.count = 1L;
			Log.IllegalPlayerAction(getPlayer(), "tried to stack unstackable item " + getItemId(), 0);
			return;
		}
		
		this.count = count;
	}
	
	public int getEnchantLevel()
	{
		return enchantLevel;
	}
	
	public int getOlyEnchantLevel()
	{
		if (!Config.OLY_ENCH_LIMIT_ENABLE)
		{
			return enchantLevel;
		}
		
		Player player = GameObjectsStorage.getPlayer(ownerId);
		if ((player != null) && player.isInOlympiadMode())
		{
			if (isWeapon())
			{
				return Math.min(enchantLevel, Config.OLY_ENCHANT_LIMIT_WEAPON);
			}
			if (isArmor())
			{
				return Math.min(enchantLevel, Config.OLY_ENCHANT_LIMIT_ARMOR);
			}
			if (isAccessory())
			{
				return Math.min(enchantLevel, Config.OLY_ENCHANT_LIMIT_JEWEL);
			}
		}
		
		return enchantLevel;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		final int old = this.enchantLevel;
		
		this.enchantLevel = enchantLevel;
		
		if ((old != this.enchantLevel) && (getTemplate().getEnchantOptions().size() > 0))
		{
			Player player = GameObjectsStorage.getPlayer(ownerId);
			
			if (isEquipped() && (player != null))
			{
				ItemEnchantOptionsListener.getInstance().onUnequip(getEquipSlot(), this, player);
			}
			
			int[] enchantOptions = getTemplate().getEnchantOptions().get(this.enchantLevel);
			
			_enchantOptions = enchantOptions == null ? EMPTY_ENCHANT_OPTIONS : enchantOptions;
			
			if (isEquipped() && (player != null))
			{
				ItemEnchantOptionsListener.getInstance().onEquip(getEquipSlot(), this, player);
			}
		}
	}
	
	public void setLocName(String loc)
	{
		this.loc = ItemLocation.valueOf(loc);
	}
	
	public String getLocName()
	{
		return loc.name();
	}
	
	public void setLocation(ItemLocation loc)
	{
		this.loc = loc;
	}
	
	public ItemLocation getLocation()
	{
		return loc;
	}
	
	public void setLocData(int locData)
	{
		this.locData = locData;
	}
	
	public int getLocData()
	{
		return locData;
	}
	
	public int getCustomType1()
	{
		return customType1;
	}
	
	public void setCustomType1(int newtype)
	{
		customType1 = newtype;
	}
	
	public int getCustomType2()
	{
		return customType2;
	}
	
	public void setCustomType2(int newtype)
	{
		customType2 = newtype;
	}
	
	public int getLifeTime()
	{
		return lifeTime;
	}
	
	public void setLifeTime(int lifeTime)
	{
		this.lifeTime = Math.max(0, lifeTime);
	}
	
	public int getCustomFlags()
	{
		return customFlags;
	}
	
	public void setCustomFlags(int flags)
	{
		customFlags = flags;
	}
	
	public ItemAttributes getAttributes()
	{
		return attrs;
	}
	
	public void setAttributes(ItemAttributes attrs)
	{
		this.attrs = attrs;
	}
	
	public int getShadowLifeTime()
	{
		if (!isShadowItem())
		{
			return 0;
		}
		return getLifeTime();
	}
	
	public int getTemporalLifeTime()
	{
		if (!isTemporalItem())
		{
			return 0;
		}
		return getLifeTime() - (int) (System.currentTimeMillis() / 1000L);
	}
	
	private ScheduledFuture<?> _timerTask;
	
	public void startTimer(Runnable r)
	{
		_timerTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0, 60000L);
	}
	
	public void stopTimer()
	{
		if (_timerTask != null)
		{
			_timerTask.cancel(false);
			_timerTask = null;
		}
	}
	
	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return template.isEquipable();
	}
	
	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return isEquipped;
	}
	
	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}
	
	public int getBodyPart()
	{
		return template.getBodyPart();
	}
	
	public int getdisplayId()
	{
		return template.getdisplayId();
	}
	
	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getEquipSlot()
	{
		return getLocData();
	}
	
	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public ItemTemplate getTemplate()
	{
		return template;
	}
	
	public void setTimeToDeleteAfterDrop(long time)
	{
		timeToDeleteAfterDrop = time;
	}
	
	public long getTimeToDeleteAfterDrop()
	{
		return timeToDeleteAfterDrop;
	}
	
	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}
	
	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public ItemType getItemType()
	{
		return template.getItemType();
	}
	
	public boolean isArmor()
	{
		return template.isArmor();
	}
	
	public boolean isAccessory()
	{
		return template.isAccessory();
	}
	
	public boolean isNoEnchant()
	{
		return template.isNoEnchant();
	}
	
	public boolean isShieldNoEnchant()
	{
		return template.isShieldNoEnchant();
	}
	
	public boolean isSigelNoEnchant()
	{
		return template.isSigelNoEnchant();
	}
	
	public boolean isWeapon()
	{
		return template.isWeapon();
	}
	
	public boolean isNotAugmented()
	{
		return template.isNotAugmented();
	}
	
	public boolean isArrow()
	{
		return template.isArrow();
	}
	
	public boolean isUnderwear()
	{
		return template.isUnderwear();
	}
	
	public boolean isBelt()
	{
		return template.isBelt();
	}
	
	/**
	 * Returns the reference price of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return template.getReferencePrice();
	}
	
	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return template.isStackable();
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if (Events.onAction(player, this, shift))
		{
			return;
		}
		
		if (player.isCursedWeaponEquipped() && CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			return;
		}
		
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}
	
	public boolean isAugmented()
	{
		return getAugmentationId() != 0;
	}
	
	public int getAugmentationId()
	{
		return _augmentationId;
	}
	
	public void setAugmentationId(int val)
	{
		_augmentationId = val;
	}
	
	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}
	
	/**
	 * Returns the type of charge with SpiritShot of the item
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}
	
	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}
	
	/**
	 * Sets the type of charge with SoulShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}
	
	/**
	 * Sets the type of charge with SpiritShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}
	
	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}
	
	public class FuncAttack extends Func
	{
		private final Element element;
		
		public FuncAttack(Element element, int order, Object owner)
		{
			super(element.getAttack(), order, owner);
			this.element = element;
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += getAttributeElementValue(element, true);
		}
	}
	
	public class FuncDefence extends Func
	{
		private final Element element;
		
		public FuncDefence(Element element, int order, Object owner)
		{
			super(element.getDefence(), order, owner);
			this.element = element;
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += getAttributeElementValue(element, true);
		}
	}
	
	/**
	 * This function basically returns a set of functions from L2Item/L2Armor/L2Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
	 * @return Func[]
	 */
	public Func[] getStatFuncs()
	{
		Func[] result = Func.EMPTY_FUNC_ARRAY;
		
		LazyArrayList<Func> funcs = LazyArrayList.newInstance();
		
		if (template.getAttachedFuncs().length > 0)
		{
			for (FuncTemplate t : template.getAttachedFuncs())
			{
				Func f = t.getFunc(this);
				if (f != null)
				{
					funcs.add(f);
				}
			}
		}
		
		for (Element e : Element.VALUES)
		{
			if (isWeapon())
			{
				funcs.add(new FuncAttack(e, 0x40, this));
			}
			if (isArmor())
			{
				funcs.add(new FuncDefence(e, 0x40, this));
			}
		}
		
		if (!funcs.isEmpty())
		{
			result = funcs.toArray(new Func[funcs.size()]);
		}
		
		LazyArrayList.recycle(funcs);
		
		return result;
	}
	
	/**
	 * Return true if item is hero-item
	 * @return boolean
	 */
	public boolean isHeroWeapon()
	{
		return template.isHeroWeapon();
	}
	
	/**
	 * Return true if item can be destroyed
	 */
	public boolean canBeDestroyed(Player player)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_DESTROY_CONDITIONS))
		{
			return true;
		}
		
		if ((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
		{
			return false;
		}
		
		if (isHeroWeapon())
		{
			return false;
		}
		
		if (PetDataTable.isPetControlItem(this) && player.isMounted())
		{
			return false;
		}
		
		if (player.getPetControlItem() == this)
		{
			return false;
		}
		
		if (player.getEnchantScroll() == this)
		{
			return false;
		}
		
		if (isCursed())
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return template.isDestroyable();
	}
	
	/**
	 * Return true if item can be dropped
	 */
	public boolean canBeDropped(Player player, boolean pk)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_DROP_CONDITIONS))
		{
			return true;
		}
		
		if ((customFlags & FLAG_NO_DROP) == FLAG_NO_DROP)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (isAugmented() && (!pk || !Config.DROP_ITEMS_AUGMENTED) && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if (!template.isDropable())
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return true;
	}
	
	public boolean canBeTraded(Player player)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
		{
			return true;
		}
		
		if (isEquipped())
		{
			return false;
		}
		
		if ((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if (!template.isTradeable() && !Config.CAN_BE_TRADED_NO_TARADEABLE)
		{
			return false;
		}
		
		if (!template.isSellable() && !Config.CAN_BE_TRADED_NO_SELLABLE)
		{
			return false;
		}
		
		if (!template.isStoreable() && !Config.CAN_BE_TRADED_NO_STOREABLE)
		{
			return false;
		}
		
		if (isShadowItem() && !Config.CAN_BE_TRADED_SHADOW_ITEM)
		{
			return false;
		}
		
		if (isHeroWeapon() && !Config.CAN_BE_TRADED_HERO_WEAPON)
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Î ï¿½Î ÎŽÎ Â¶Î Â½Î ÎŽ Î Â»Î Îˆ Î Î�Î¡â‚¬Î ÎŽÎ Î„Î Â°Î¡â€šÎ¡ï¿½ Î Â² Î ÎŒÎ Â°Î Â³Î Â°Î Â·Î ÎˆÎ Â½ NPC
	 */
	public boolean canBeSold(Player player)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
		{
			return true;
		}
		
		if ((customFlags & FLAG_NO_SELL) == FLAG_NO_SELL)
		{
			return false;
		}
		
		if (getItemId() == ItemTemplate.ITEM_ID_ADENA)
		{
			return false;
		}
		
		if (template.getReferencePrice() == 0)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		
		if (isEquipped())
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if (!template.isTradeable())
		{
			return false;
		}
		
		if (!template.isSellable())
		{
			return false;
		}
		
		if (!template.isStoreable())
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Î ï¿½Î ÎŽÎ Â¶Î Â½Î ÎŽ Î Â»Î Îˆ Î Î�Î ÎŽÎ Â»Î ÎŽÎ Â¶Î ÎˆÎ¡â€šÎ¡ï¿½ Î Â½Î Â° Î ÎŠÎ Â»Î Â°Î Â½Î ÎŽÎ Â²Î¡â€¹Î Î‰ Î¡ï¿½Î ÎŠÎ Â»Î Â°Î Î„
	 */
	public boolean canBeStored(Player player, boolean privatewh)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
		{
			return true;
		}
		
		if ((customFlags & FLAG_NO_TRANSFER) == FLAG_NO_TRANSFER)
		{
			return false;
		}
		
		if (!getTemplate().isStoreable())
		{
			return false;
		}
		
		if (!privatewh && (isShadowItem() || isTemporalItem()))
		{
			return false;
		}
		
		if (!privatewh && isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		
		if (isEquipped())
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if (!privatewh && isAugmented() && !Config.CAN_BE_CWH_IS_AUGMENTED)
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return privatewh || template.isTradeable();
	}
	
	public boolean canBeCrystallized(Player player)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_DESTROY_CONDITIONS))
		{
			return true;
		}
		
		if ((customFlags & FLAG_NO_CRYSTALLIZE) == FLAG_NO_CRYSTALLIZE)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return template.isCrystallizable();
	}
	
	public boolean canBeEnchanted(boolean gradeCheck)
	{
		if ((customFlags & FLAG_NO_ENCHANT) == FLAG_NO_ENCHANT)
		{
			return false;
		}
		
		return template.canBeEnchanted(gradeCheck);
	}
	
	public boolean canBeAugmented(Player player, boolean isAccessoryLifeStone)
	{
		if (!canBeEnchanted(true))
		{
			return false;
		}
		
		if (isAugmented())
		{
			return false;
		}
		
		if (isCommonItem())
		{
			return false;
		}
		
		if (isTerritoryAccessory())
		{
			return false;
		}
		
		if (getTemplate().getItemGrade().ordinal() < Grade.C.ordinal())
		{
			return false;
		}
		
		if (!getTemplate().isAugmentable())
		{
			return false;
		}
		
		if (isAccessory())
		{
			return isAccessoryLifeStone;
		}
		
		if (isArmor())
		{
			return Config.ALT_ALLOW_AUGMENT_ALL;
		}
		
		if (isWeapon())
		{
			return !isAccessoryLifeStone;
		}
		
		return true;
	}
	
	public boolean canBeAugmented()
	{
		if (!canBeEnchanted(true))
		{
			return false;
		}
		
		if (isAugmented())
		{
			return false;
		}
		
		if (isCommonItem())
		{
			return false;
		}
		
		if (isTerritoryAccessory())
		{
			return false;
		}
		
		if (getTemplate().getItemGrade().ordinal() < Grade.C.ordinal())
		{
			return false;
		}
		
		if (!getTemplate().isAugmentable())
		{
			return false;
		}
		
		if (isAccessory())
		{
			return false;
		}
		
		if (isArmor())
		{
			return Config.ALT_ALLOW_AUGMENT_ALL;
		}
		
		return true;
	}
	
	public boolean canBeExchanged(Player player)
	{
		if ((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isHeroWeapon())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if ((getAttachment() != null) && (getAttachment() instanceof FlagItemAttachment) && !((FlagItemAttachment) getAttachment()).canBeLost())
		{
			return false;
		}
		
		if (!player.getPermissions().canLoseItem(this, false))
		{
			return false;
		}
		
		return template.isDestroyable();
	}
	
	public boolean isTerritoryAccessory()
	{
		return template.isTerritoryAccessory();
	}
	
	public boolean isShadowItem()
	{
		return template.isShadowItem();
	}
	
	public boolean isTemporalItem()
	{
		return template.isTemporal();
	}
	
	public boolean isCommonItem()
	{
		return template.isCommonItem();
	}
	
	public boolean isAltSeed()
	{
		return template.isAltSeed();
	}
	
	public boolean isAdena()
	{
		return template.isAdena();
	}
	
	public boolean isCursed()
	{
		return template.isCursed();
	}
	
	/**
	 * Ð‘Ñ€Ð¾Ñ�Ð°ÐµÑ‚ Ð½Ð° Ð·ÐµÐ¼Ð»ÑŽ Ð»ÑƒÑ‚ Ñ� NPC
	 * @param lastAttacker
	 * @param fromNpc
	 */
	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc)
	{
		Creature dropper = fromNpc;
		if (dropper == null)
		{
			dropper = lastAttacker;
		}
		
		Location pos = Location.findAroundPosition(dropper, 100);
		
		// activate non owner penalty
		if (lastAttacker != null) // lastAttacker Ð² Ð´Ð°Ð½Ð½Ð¾Ð¼ Ñ�Ð»ÑƒÑ‡Ð°Ðµ top damager
		{
			_dropPlayers = new HashIntSet(1, 2);
			for (Player $member : lastAttacker.getPlayerGroup())
			{
				_dropPlayers.add($member.getObjectId());
			}
			
			_dropTimeOwner = System.currentTimeMillis() + ((fromNpc != null) && fromNpc.isRaid() ? Config.NONOWNER_ITEM_PICKUP_DELAY_RAIDS : Config.NONOWNER_ITEM_PICKUP_DELAY);
		}
		
		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		dropMe(dropper, pos);
		
		// Add drop to auto destroy item task
		if (isHerb())
		{
			ItemsAutoDestroy.getInstance().addHerb(this);
		}
		else if ((Config.AUTODESTROY_ITEM_AFTER > 0) && !isCursed() && (_attachment == null))
		{
			ItemsAutoDestroy.getInstance().addItem(this, Config.AUTODESTROY_ITEM_AFTER * 1000L);
		}
	}
	
	/**
	 * Î â€˜Î¡â‚¬Î ÎŽÎ¡ï¿½Î Â°Î ÂµÎ¡â€š Î Â½Î Â° Î Â·Î ÂµÎ ÎŒÎ Â»Î¡ï¿½ Î Â»Î¡Æ’Î¡â€š Î¡ï¿½ NPC
	 */
	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc, boolean dropProtected)
	{
		Creature dropper = fromNpc;
		if (dropper == null)
		{
			dropper = lastAttacker;
		}
		
		Location pos = Location.findAroundPosition(dropper, 100);
		
		// activate non owner penalty
		if ((lastAttacker != null) && dropProtected) // lastAttacker Î Â² Î Î„Î Â°Î Â½Î Â½Î ÎŽÎ ÎŒ Î¡ï¿½Î Â»Î¡Æ’Î¡â€¡Î Â°Î Âµ top damager
		{
			_dropPlayers = new HashIntSet(1, 2);
			for (Player $member : lastAttacker.getPlayerGroup())
			{
				_dropPlayers.add($member.getObjectId());
			}
			
			_dropTimeOwner = System.currentTimeMillis() + Config.NONOWNER_ITEM_PICKUP_DELAY + ((fromNpc != null) && fromNpc.isRaid() ? 285000 : 0);
		}
		
		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		dropMe(dropper, pos);
		
		// Add drop to auto destroy item task
		if (isHerb())
		{
			ItemsAutoDestroy.getInstance().addHerb(this);
		}
		else if ((Config.AUTODESTROY_ITEM_AFTER > 0) && !isCursed() && (_attachment == null))
		{
			ItemsAutoDestroy.getInstance().addItem(this, Config.AUTODESTROY_ITEM_AFTER * 1000L);
		}
	}
	
	/**
	 * Î â€˜Î¡â‚¬Î ÎŽÎ¡ï¿½Î Â°Î ÂµÎ¡â€š Î Â²Î ÂµÎ¡â€°Î¡ï¿½ Î Â½Î Â° Î Â·Î ÂµÎ ÎŒÎ Â»Î¡ï¿½ Î¡â€šÎ¡Æ’Î Î„Î Â°, Î Â³Î Î„Î Âµ Î ÂµÎ Âµ Î ÎŒÎ ÎŽÎ Â¶Î Â½Î ÎŽ Î Î�Î ÎŽÎ Î„Î Â½Î¡ï¿½Î¡â€šÎ¡ï¿½
	 */
	public void dropToTheGround(Creature dropper, Location dropPos)
	{
		if (GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
		{
			dropMe(dropper, dropPos);
		}
		else
		{
			dropMe(dropper, dropper.getLoc());
		}
	}
	
	/**
	 * Î â€˜Î¡â‚¬Î ÎŽÎ¡ï¿½Î Â°Î ÂµÎ¡â€š Î Â²Î ÂµÎ¡â€°Î¡ï¿½ Î Â½Î Â° Î Â·Î ÂµÎ ÎŒÎ Â»Î¡ï¿½ Î ÎˆÎ Â· Î ÎˆÎ Â½Î Â²Î ÂµÎ Â½Î¡â€šÎ Â°Î¡â‚¬Î¡ï¿½ Î¡â€šÎ¡Æ’Î Î„Î Â°, Î Â³Î Î„Î Âµ Î ÂµÎ Âµ Î ÎŒÎ ÎŽÎ Â¶Î Â½Î ÎŽ Î Î�Î ÎŽÎ Î„Î Â½Î¡ï¿½Î¡â€šÎ¡ï¿½
	 */
	public void dropToTheGround(Playable dropper, Location dropPos)
	{
		setLocation(ItemLocation.VOID);
		if (getJdbcState().isPersisted())
		{
			setJdbcState(JdbcEntityState.UPDATED);
			update();
		}
		
		if (GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
		{
			dropMe(dropper, dropPos);
		}
		else
		{
			dropMe(dropper, dropper.getLoc());
		}
		
		// Add drop to auto destroy item task from player items.
		if ((Config.AUTODESTROY_PLAYER_ITEM_AFTER > 0) && (_attachment == null))
		{
			ItemsAutoDestroy.getInstance().addItem(this, Config.AUTODESTROY_PLAYER_ITEM_AFTER * 1000L);
		}
	}
	
	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion</li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2ItemInstance</li>
	 * <li>_worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop item</li>
	 * <li>Call Pet</li><BR>
	 * @param dropper Char that dropped item
	 * @param loc drop coordinates
	 */
	public void dropMe(Creature dropper, Location loc)
	{
		if (dropper != null)
		{
			setReflection(dropper.getReflection());
		}
		
		spawnMe0(loc, dropper);
		if (isHerb())
	    {
	      ItemsAutoDestroy.getInstance().addHerb(this);
	    }
	    else if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (!isCursed()))
	    {
	      ItemsAutoDestroy.getInstance().addItem(this, 100000L);
	    }
	}
	
	public final void pickupMe()
	{
		decayMe();
		setReflection(ReflectionManager.DEFAULT);
	}
	
	public ItemClass getItemClass()
	{
		return template.getItemClass();
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	private int getDefence(Element element)
	{
		return isArmor() ? getAttributeElementValue(element, true) : 0;
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°: Î ÎŽÎ Â³Î ÎŽÎ Â½Î¡ï¿½.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	public int getDefenceFire()
	{
		return getDefence(Element.FIRE);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°: Î Â²Î ÎŽÎ Î„Î Â°.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	public int getDefenceWater()
	{
		return getDefence(Element.WATER);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°: Î Â²Î ÎŽÎ Â·Î Î„Î¡Æ’Î¡â€¦.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	public int getDefenceWind()
	{
		return getDefence(Element.WIND);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°: Î Â·Î ÂµÎ ÎŒÎ Â»Î¡ï¿½.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	public int getDefenceEarth()
	{
		return getDefence(Element.EARTH);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°: Î¡ï¿½Î Â²Î ÂµÎ¡â€š.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	public int getDefenceHoly()
	{
		return getDefence(Element.HOLY);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡Æ’ Î ÎŽÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°: Î¡â€šÎ¡ï¿½Î ÎŒÎ Â°.
	 * @return Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î Â·Î Â°Î¡â€°Î ÎˆÎ¡â€šÎ¡â€¹
	 */
	public int getDefenceUnholy()
	{
		return getDefence(Element.UNHOLY);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î Â·Î Â½Î Â°Î¡â€¡Î ÂµÎ Â½Î ÎˆÎ Âµ Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€šÎ Â°.
	 * @return
	 */
	public int getAttributeElementValue(Element element, boolean withBase)
	{
		return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
	}
	
	/**
	 * Î â€™Î ÎŽÎ Â·Î Â²Î¡â‚¬Î Â°Î¡â€°Î Â°Î ÂµÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€š Î Â°Î¡â€šÎ¡â‚¬Î ÎˆÎ Â±Î¡Æ’Î¡â€ Î ÎˆÎ Îˆ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ Â°.<br>
	 */
	public Element getAttributeElement()
	{
		return attrs.getElement();
	}
	
	public int getAttributeElementValue()
	{
		return attrs.getValue();
	}
	
	public Element getAttackElement()
	{
		Element element = isWeapon() ? getAttributeElement() : Element.NONE;
		if (element == Element.NONE)
		{
			for (Element e : Element.VALUES)
			{
				if (template.getBaseAttributeValue(e) > 0)
				{
					return e;
				}
			}
		}
		return element;
	}
	
	public int getAttackElementValue()
	{
		return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
	}
	
	/**
	 * Î Â£Î¡ï¿½Î¡â€šÎ Â°Î Â½Î Â°Î Â²Î Â»Î ÎˆÎ Â²Î Â°Î ÂµÎ¡â€š Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€š Î Â°Î¡â€šÎ¡â‚¬Î ÎˆÎ Â±Î¡Æ’Î¡â€ Î ÎˆÎ Îˆ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ Â°.<br>
	 * Element (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -1 - None)
	 * @param element Î¡ï¿½Î Â»Î ÂµÎ ÎŒÎ ÂµÎ Â½Î¡â€š
	 * @param value
	 */
	public void setAttributeElement(Element element, int value)
	{
		attrs.setValue(element, value);
	}
	
	/**
	 * Î ï¿½Î¡â‚¬Î ÎŽÎ Â²Î ÂµÎ¡â‚¬Î¡ï¿½Î ÂµÎ¡â€š, Î¡ï¿½Î Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î Â»Î Îˆ Î Î„Î Â°Î Â½Î Â½Î¡â€¹Î Î‰ Î ÎˆÎ Â½Î¡ï¿½Î¡â€šÎ Â°Î Â½Î¡ï¿½ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€šÎ Â° Î¡â€¦Î ÂµÎ¡â‚¬Î Â±Î ÎŽÎ ÎŒ
	 * @return true Î ÂµÎ¡ï¿½Î Â»Î Îˆ Î Î�Î¡â‚¬Î ÂµÎ Î„Î ÎŒÎ ÂµÎ¡â€š Î¡ï¿½Î Â²Î Â»Î¡ï¿½Î ÂµÎ¡â€šÎ¡ï¿½Î¡ï¿½ Î¡â€¦Î ÂµÎ¡â‚¬Î Â±Î ÎŽÎ ÎŒ
	 */
	public boolean isHerb()
	{
		if (getTemplate().isHerb())
		{
			return true;
		}
		return false;
	}
	
	public Grade getCrystalType()
	{
		return template.getCrystalType();
	}
	
	@Override
	public String getName()
	{
		return getTemplate().getName();
	}
	
	@Override
	public void save()
	{
		_itemsDAO.save(this);
	}
	
	@Override
	public void update()
	{
		_itemsDAO.update(this);
	}
	
	@Override
	public void delete()
	{
		_itemsDAO.delete(this);
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer)
	{
		// FIXME Î ÎŠÎ Â°Î Â¶Î ÎˆÎ¡ï¿½Î¡ï¿½ Î Î„Î¡â‚¬Î ÎŽÎ Î�Î Î�Î ÂµÎ¡â‚¬ Î¡Æ’ Î Â½Î Â°Î¡ï¿½ Î ÂµÎ¡ï¿½Î¡â€šÎ¡ï¿½ Î Â² Î ÎˆÎ¡â€šÎ ÂµÎ ÎŒÎ Âµ Î ÎŠÎ Â°Î ÎŠ Î Î�Î ÂµÎ¡â‚¬Î ÂµÎ ÎŒÎ ÂµÎ Â½Î Â½Î Â°Î¡ï¿½, Î¡â€šÎ ÎŽÎ ÎŠ Î Î�Î¡â‚¬Î ÎŽÎ Â²Î ÂµÎ¡â‚¬Î ÎˆÎ¡â€šÎ¡ï¿½ Î Â²Î¡â‚¬Î ÂµÎ ÎŒÎ¡ï¿½? [VISTALL]
		L2GameServerPacket packet = new SpawnItem(this);
		return Collections.singletonList(packet);
	}
	
	/**
	 * Returns the item in String format
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder().append("{Item: [").append(getObjectId()).append("] ").append(getTemplate().getName()).append("(").append(getTemplate().getItemId()).append(")");
		if (getEnchantLevel() > 0)
		{
			sb.append(" +").append(getEnchantLevel());
		}
		if (!getTemplate().getAdditionalName().isEmpty())
		{
			sb.append(" ").append("\\").append(getTemplate().getAdditionalName()).append("\\");
		}
		sb.append(" Count: (").append(getCount());
		long diff = getCount() - oldCount;
		if ((oldCount > -1) && (diff != 0))
		{
			sb.append(" <- ").append(diff > 0 ? "+" : "").append(diff);
		}
		sb.append(") Owner: ").append(getOwnerId());
		if (oldOwnerId > -1)
		{
			sb.append(" OldOwner: ").append(oldOwnerId);
		}
		sb.append("}");
		
		return sb.toString();
		
	}
	
	@Override
	public void setJdbcState(JdbcEntityState state)
	{
		_state = state;
	}
	
	@Override
	public JdbcEntityState getJdbcState()
	{
		return _state;
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
	
	public ItemAttachment getAttachment()
	{
		return _attachment;
	}
	
	public void setAttachment(ItemAttachment attachment)
	{
		ItemAttachment old = _attachment;
		_attachment = attachment;
		if (_attachment != null)
		{
			_attachment.setItem(this);
		}
		if (old != null)
		{
			old.setItem(null);
		}
	}
	
	public int getAgathionEnergy()
	{
		return _agathionEnergy;
	}
	
	public void setAgathionEnergy(int agathionEnergy)
	{
		_agathionEnergy = agathionEnergy;
	}
	
	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}
	
	public IntSet getDropPlayers()
	{
		return _dropPlayers;
	}
	
	public boolean canBeAuctioned(Player player)
	{
		if (player.canOverrideCond(PcCondOverride.ITEM_TRADE_CONDITIONS))
		{
			return true;
		}
		
		if ((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
		{
			return false;
		}
		
		if (isHeroWeapon())
		{
			return false;
		}
		
		if (getTemplate().isQuest())
		{
			return false;
		}
		
		if ((getItemType() == EtcItemType.PET_COLLAR) && player.isMounted())
		{
			return false;
		}
		
		if (isCursed())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isEquipped())
		{
			return false;
		}
		
		if (template.getReferencePrice() == 0)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if (!template.isSellable())
		{
			return false;
		}
		
		if (!template.isStoreable())
		{
			return false;
		}
		if (isArrow())
		{
			return false;
		}
		
		return template.isTradeable();
	}
	
	public boolean canbeSealed(Player player)
	{
		if (isHeroWeapon())
		{
			return false;
		}
		
		if (getTemplate().isQuest())
		{
			return false;
		}
		
		if ((getItemType() == EtcItemType.PET_COLLAR) && player.isMounted())
		{
			return false;
		}
		
		if (isCursed())
		{
			return false;
		}
		
		if (isTemporalItem())
		{
			return false;
		}
		
		if (template.getReferencePrice() == 0)
		{
			return false;
		}
		
		if (isShadowItem())
		{
			return false;
		}
		
		if (!ItemFunctions.checkIfCanDiscard(player, this))
		{
			return false;
		}
		
		if (!template.isSellable())
		{
			return false;
		}
		
		if (!template.isStoreable())
		{
			return false;
		}
		
		return template.isTradeable();
	}
	
	public int getItemLevel()
	{
		return _itemLevel;
	}
	
	public void setItemLevel(int val)
	{
		_itemLevel = val;
	}
	
	private boolean _published = false;
	
	public boolean isPublished()
	{
		return _published;
	}
	
	public void publish()
	{
		_published = true;
	}
	
	/**
	 * Tries to use the item if the item has a bound handler to it.
	 * @param owner
	 * @param item
	 * @param ctrlPressed
	 * @return true if the item has been used and cooldown has been applied.
	 */
	public static boolean useItem(Playable owner, ItemInstance item, boolean ctrlPressed)
	{
		boolean success = item.getTemplate().getHandler().useItem(owner, item, ctrlPressed);
		if (success)
		{
			long nextTimeUse = item.getTemplate().getReuseType().next(item);
			if (nextTimeUse > System.currentTimeMillis())
			{
				TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
				owner.getPlayer().addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);
				
				if (item.getTemplate().getReuseDelay() > 0)
				{
					owner.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
				}
			}
		}
		
		return success;
	}
	
	// Savable in db
	private boolean _savableInDatabase = true;
	
	public void setSavableInDatabase(boolean savableInDatabase)
	{
		_savableInDatabase = savableInDatabase;
	}
	
	public boolean isSavableInDatabase()
	{
		return _savableInDatabase;
	}
	
}