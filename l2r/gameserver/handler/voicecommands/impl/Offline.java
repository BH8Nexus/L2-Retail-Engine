package l2r.gameserver.handler.voicecommands.impl;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;

public class Offline extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[]
	{
		"offline"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (!Config.SERVICES_OFFLINE_TRADE_ALLOW)
		{
			activeChar.sendMessage("This option is currently disabled!");
			return false;
		}
		
		if ((activeChar.getOlympiadObserveGame() != null) || (activeChar.getOlympiadGame() != null) || Olympiad.isRegisteredInComp(activeChar) || (activeChar.getKarma() > 0) || activeChar.isInJail())
		{
			activeChar.sendMessage("You cannot do it right now!");
			activeChar.sendActionFailed();
			return false;
		}
		
		if (activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			activeChar.sendMessage("Your level is too low!");
			return false;
		}
		
		if (!activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage("You cannot set offline store in this area!");
			return false;
		}
		
		if (!activeChar.isInStoreMode())
		{
			activeChar.sendMessage("You need to place Private Store first!");
			return false;
		}
		
		if (activeChar.getNoChannelRemained() > 0)
		{
			activeChar.sendMessage("You cannot set offline store while having Chat Ban!");
			return false;
		}
		
		if (activeChar.isActionBlocked(Zone.BLOCKED_ACTION_PRIVATE_STORE))
		{
			activeChar.sendMessage("You cannot set offline store in this area!");
			return false;
		}
		
		if ((Config.SERVICES_OFFLINE_TRADE_PRICE > 0) && (Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0))
		{
			if (getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE)
			{
				Functions.show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
				return false;
			}
			removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE, "");
		}
		
		if (Config.LIMIT_OFFLINE_IN_TOWN > 0)
		{
			List<Zone> zones = new ArrayList<>();
			World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
			for (Zone zone : zones)
			{
				int insidePlayersOffline = 0;
				for (Player plr : zone.getInsidePlayers())
				{
					if ((plr != null) && plr.isInOfflineMode() && (plr.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) && isInTown(plr))
					{
						insidePlayersOffline++;
					}
				}
				
				if (zone.checkIfInZone(activeChar) && isInTown(activeChar))
				{
					if (insidePlayersOffline >= Config.LIMIT_OFFLINE_IN_TOWN)
					{
						activeChar.offline();
						
						if (!activeChar.getVarB("OfflineTime"))
						{
							activeChar.setVar("OfflineTime", 1);
						}
						
						return true;
					}
				}
			}
		}
		
		activeChar.offline();
		
		if (Config.ENABLE_BAZAR)
		{
			if (!activeChar.getVarB("OfflineTime"))
			{
				activeChar.setVar("OfflineTime", System.currentTimeMillis());
			}
		}
		
		return true;
	}
	
	private static boolean isInTown(Player player)
	{
		if (!player.isInPeaceZone())
		{
			return false;
		}
		
		// Shops in clanhalls allowed.
		if (player.isInZone(ZoneType.RESIDENCE))
		{
			return false;
		}
		
		for (Zone zone : player.getZones())
		{
			if (zone.getName().contains("talking_island_town_peace_zone") || zone.getName().contains("darkelf_town_peace_zone") || zone.getName().contains("elf_town_peace") || zone.getName().contains("guldiocastle_town_peace") || zone.getName().contains("gludin_town_peace") || zone.getName().contains("dion_town_peace") || zone.getName().contains("floran_town_peace") || zone.getName().contains("giran_town_peace") || zone.getName().contains("orc_town_peace") || zone.getName().contains("dwarf_town_peace") || zone.getName().contains("oren_town_peace") || zone.getName().contains("hunter_town_peace") || zone.getName().contains("aden_town_peace") || zone.getName().contains("speaking_port_peace") || zone.getName().contains("gludin_port") || zone.getName().contains("giran_port") || zone.getName().contains("heiness_peace") || zone.getName().contains("godad_peace") || zone.getName().contains("rune_peace") || zone.getName().contains("gludio_airship_peace") || zone.getName().contains("schuttgart_town_peace") || zone.getName().contains("kamael_village_town_peace") || zone.getName().contains("keucereus_alliance_base_town_peace") || zone.getName().contains("giran_harbor_peace_alt") || zone.getName().contains("parnassus_peace"))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}