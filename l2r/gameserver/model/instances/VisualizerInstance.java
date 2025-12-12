package l2r.gameserver.model.instances;

import java.util.Map.Entry;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ModelCharInfo;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.randoms.Visualizer;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Language;

/**
 * @author Midnex
 * @author promo(htmls)
 */
public class VisualizerInstance extends NpcInstance
{
	
	boolean _inUse;
	public int _tcolor = Integer.decode("0xa4e598");
	public int _ncolor = Integer.decode("0xFFFFFF");
	
	public int _face = 0;
	
	public int _up = 0;
	public int _pageup = 1;
	public int _totalpageup = (Visualizer.getAll(0).size() / 4) + 1;
	
	public int _lower = 0;
	public int _pagelower = 1;
	public int _totalpagelower = (Visualizer.getAll(1).size() / 4) + 1;
	
	public int _gloves = 0;
	public int _pagegloves = 1;
	public int _totalpagegloves = (Visualizer.getAll(2).size() / 4) + 1;
	
	public int _boots = 0;
	public int _pageboots = 1;
	public int _totalpageboots = (Visualizer.getAll(3).size() / 4) + 1;
	
	public int _accessory = 0;
	public int _pageaccessory = 1;
	public int _totalpageaccessory = (Visualizer.getAll(4).size() / 4) + 1;
	
	public int _weapons = 0;
	public int _pageweapons = 1;
	public int _totalpageweapons = (Visualizer.getAll(5).size() / 4) + 1;
	
	public int _shields = 0;
	public int _pageshields = 1;
	public int _totalpageshields = (Visualizer.getAll(6).size() / 4) + 1;
	
	public VisualizerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if (player.getVar("jailed") != null)
		{
			player.sendMessageS("Your all property is sealed.", 3);
			player.sendActionFailed();
		}
		else
		{
			super.onAction(player, shift);
		}
	}
	
	public void setItems(int chest, int legs, int gloves, int boots, int accessorys, int weapons, int shields)
	{
		_up = chest;
		_lower = legs;
		_gloves = gloves;
		_boots = boots;
		_accessory = accessorys;
		_weapons = weapons;
		_shields = shields;
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showHtml(player, true);
	}
	
	private void showHtml(Player player, boolean first)
	{
		String htmlToSend = HtmCache.getInstance().getNotNull("mods/visualizer/visualizer.htm", player);
		
		int page = 1;
		
		// chest
		page = first && (_up != 0) ? Visualizer.getPageIn(0, _up) : _pageup;
		htmlToSend = htmlToSend.replaceAll("%itemsUP%", getHtml(0, page, _up));
		
		// legs
		page = first && (_lower != 0) ? Visualizer.getPageIn(1, _lower) : _pagelower;
		htmlToSend = htmlToSend.replaceAll("%itemsLOWER%", getHtml(1, page, _lower));
		
		// gloves
		page = first && (_gloves != 0) ? Visualizer.getPageIn(2, _gloves) : _pagegloves;
		htmlToSend = htmlToSend.replaceAll("%itemsGLOVES%", getHtml(2, page, _gloves));
		
		// boots
		page = first && (_boots != 0) ? Visualizer.getPageIn(3, _boots) : _pageboots;
		htmlToSend = htmlToSend.replaceAll("%itemsBOOTS%", getHtml(3, page, _boots));
		
		// accessory
		page = first && (_accessory != 0) ? Visualizer.getPageIn(4, _accessory) : _pageaccessory;
		htmlToSend = htmlToSend.replaceAll("%itemsACCESSORY%", getHtml(4, page, _accessory));
		
		// weapons
		page = first && (_weapons != 0) ? Visualizer.getPageIn(5, _weapons) : _pageweapons;
		htmlToSend = htmlToSend.replaceAll("%itemsWEAPONS", getHtml(5, page, _weapons));
		
		// shields
		page = first && (_shields != 0) ? Visualizer.getPageIn(6, _shields) : _pageshields;
		htmlToSend = htmlToSend.replaceAll("%itemsSHIELDS%", getHtml(6, page, _shields));
		
		htmlToSend = htmlToSend.replaceAll("%buttonas%", _inUse ? "<td width=\"32\" valign=\"top\"><button value=\"\" action=\"bypass -h npc_%objectId%_DONTUSE\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\"></td>" : "<td width=\"32\" valign=\"top\"><button value=\"\" action=\"bypass -h npc_%objectId%_USEIT\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Over\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\"></td>");
		
		htmlToSend = htmlToSend.replaceAll("%objectId%", "" + getObjectId());
		
		// NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		// npcHtml.setHtml(htmlToSend);
		// player.sendPacket(npcHtml);
		ShowBoard.separateAndSend(htmlToSend, player);
	}
	
	private String getHtml(int type, int page, int cuuitem)
	{
		String itemToReplace = "";
		
		int i = 0;
		
		for (Entry<Integer, String> lol : Visualizer.get(type, (page * 4) - 4, page * 4).entrySet())
		{
			i++;
			String itemOne = HtmCache.getInstance().getNotNull("mods/visualizer/one.htm", Language.ENGLISH);
			String temp = "";
			temp = itemOne;
			
			if (lol.getKey() == cuuitem)
			{
				temp = temp.replaceAll("%isSelected%", "bgcolor=\"LEVEL\"");
			}
			else
			{
				temp = temp.replaceAll("%isSelected%", "");
			}
			
			switch (type)
			{
				case 0:
					temp = temp.replaceAll("%type%", "up");
					break;
				case 1:
					temp = temp.replaceAll("%type%", "lower");
					break;
				case 2:
					temp = temp.replaceAll("%type%", "gloves");
					break;
				case 3:
					temp = temp.replaceAll("%type%", "boots");
					break;
				case 4:
					temp = temp.replaceAll("%type%", "accessory");
					break;
				case 5:
					temp = temp.replaceAll("%type%", "weapons");
					break;
				case 6:
					temp = temp.replaceAll("%type%", "shields");
					break;
			}
			
			temp = temp.replaceAll("%id%", "" + lol.getKey());
			temp = temp.replaceAll("%icon%", lol.getValue());
			temp = temp.replaceAll("%objectId%", "" + getObjectId());
			itemToReplace += temp;
		}
		
		if (i < 4)
		{
			for (int a = 4 - i; a != 0; a--)
			{
				String itemOne = HtmCache.getInstance().getNotNull("mods/visualizer/one.htm", Language.ENGLISH);
				String temp = "";
				temp = itemOne;
				temp = temp.replaceAll("%isSelected%", "");
				temp = temp.replaceAll("%type%", "none");
				temp = temp.replaceAll("%id%", "0");
				temp = temp.replaceAll("%icon%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
				temp = temp.replaceAll("%objectId%", "" + getObjectId());
				itemToReplace += temp;
			}
		}
		return itemToReplace;
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
		{
			return;
		}
		
		if (command.equals("pageplusUP"))
		{
			if (_totalpageup == _pageup)
			{
				return;
			}
			
			_pageup++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusUP"))
		{
			if (_pageup == 1)
			{
				return;
			}
			
			_pageup--;
			showHtml(player, false);
		}
		else if (command.equals("pageplusLOWER"))
		{
			if (_totalpagelower == _pagelower)
			{
				return;
			}
			
			_pagelower++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusLOWER"))
		{
			if (_pagelower == 1)
			{
				return;
			}
			
			_pagelower--;
			showHtml(player, false);
		}
		
		else if (command.equals("pageplusGLOVES"))
		{
			if (_totalpagegloves == _pagegloves)
			{
				return;
			}
			
			_pagegloves++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusGLOVES"))
		{
			if (_pagegloves == 1)
			{
				return;
			}
			
			_pagegloves--;
			showHtml(player, false);
		}
		else if (command.equals("pageplusBOOTS"))
		{
			if (_totalpageboots == _pageboots)
			{
				return;
			}
			
			_pageboots++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusBOOTS"))
		{
			if (_pageboots == 1)
			{
				return;
			}
			
			_pageboots--;
			showHtml(player, false);
		}
		else if (command.equals("pageplusACCESSORY"))
		{
			if (_totalpageaccessory == _pageaccessory)
			{
				return;
			}
			
			_pageaccessory++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusACCESSORY"))
		{
			if (_pageaccessory == 1)
			{
				return;
			}
			
			_pageaccessory--;
			showHtml(player, false);
		}
		else if (command.equals("pageplusWEAPONS"))
		{
			if (_totalpageweapons == _pageweapons)
			{
				return;
			}
			
			_pageweapons++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusWEAPONS"))
		{
			if (_pageweapons == 1)
			{
				return;
			}
			
			_pageweapons--;
			showHtml(player, false);
		}
		else if (command.equals("pageplusSHIELDS"))
		{
			if (_totalpageshields == _pageshields)
			{
				return;
			}
			
			_pageshields++;
			showHtml(player, false);
		}
		else if (command.equals("pageminusSHIELDS"))
		{
			if (_pageshields == 1)
			{
				return;
			}
			
			_pageshields--;
			showHtml(player, false);
		}
		
		else if (command.startsWith("up"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(0, tempitem))
			{
				if (_up == tempitem)
				{
					_up = 0;
				}
				else
				{
					_up = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			
			showHtml(player, false);
		}
		
		else if (command.startsWith("lower"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(1, tempitem))
			{
				if (_lower == tempitem)
				{
					_lower = 0;
				}
				else
				{
					_lower = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			
			showHtml(player, false);
		}
		else if (command.startsWith("gloves"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(2, tempitem))
			{
				if (_gloves == tempitem)
				{
					_gloves = 0;
				}
				else
				{
					_gloves = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			
			showHtml(player, false);
		}
		else if (command.startsWith("boots"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(3, tempitem))
			{
				if (_boots == tempitem)
				{
					_boots = 0;
				}
				else
				{
					_boots = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if (command.startsWith("accessory"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(4, tempitem))
			{
				if (_accessory == tempitem)
				{
					_accessory = 0;
				}
				else
				{
					_accessory = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if (command.startsWith("weapons"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(5, tempitem))
			{
				if (_weapons == tempitem)
				{
					_weapons = 0;
				}
				else
				{
					_weapons = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		else if (command.startsWith("shields"))
		{
			String[] idd = command.split(" ");
			
			int tempitem = Integer.parseInt(idd[1]);
			if (Visualizer.isAvaible(6, tempitem))
			{
				if (_shields == tempitem)
				{
					_shields = 0;
				}
				else
				{
					_shields = tempitem;
				}
			}
			
			player.sendPacket(new ModelCharInfo(this, player));
			showHtml(player, false);
		}
		
		else if (command.equals("USEIT"))
		{
			// if(player.getVisualItems() != null)
			// {
			// player.sendMessageS("First disable current set.", 4);
			// return;
			// }
			
			// TODO: player.setVisualItems(new int[] { _up, _lower, _gloves, _boots, _accessory, _weapons, _shields });
			player.sendMessageS("Now you use '" + getTitle() + "' visual set.", 4);
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new UserInfo(player));
			
			_tcolor = Integer.decode("0xD4F212");
			_ncolor = Integer.decode("0xD4F212");
			_inUse = true;
			player.sendPacket(new ModelCharInfo(this, player));
		}
		else if (command.equals("DONTUSE"))
		{
			// TODO: player.setVisualItems(null);
			player.sendMessageS("You disabled '" + getTitle() + "' visual set.", 4);
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new UserInfo(player));
			
			_tcolor = Integer.decode("0xa4e598");
			_ncolor = Integer.decode("0xFFFFFF");
			_inUse = false;
			player.sendPacket(new ModelCharInfo(this, player));
		}
		// TODO: sex,hair,face, cloak, transformation, bighead?
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public String getSaveString()
	{
		return _up + "," + _lower + "," + _gloves + "," + _boots + "," + _accessory + "," + _weapons + "," + _shields + ";";
	}
}
