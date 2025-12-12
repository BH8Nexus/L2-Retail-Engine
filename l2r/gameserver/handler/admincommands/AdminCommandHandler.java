package l2r.gameserver.handler.admincommands;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.handler.admincommands.impl.AdminAdmin;
import l2r.gameserver.handler.admincommands.impl.AdminAnnouncements;
import l2r.gameserver.handler.admincommands.impl.AdminAttribute;
import l2r.gameserver.handler.admincommands.impl.AdminAugment;
import l2r.gameserver.handler.admincommands.impl.AdminBan;
import l2r.gameserver.handler.admincommands.impl.AdminCamera;
import l2r.gameserver.handler.admincommands.impl.AdminCancel;
import l2r.gameserver.handler.admincommands.impl.AdminChangeAccessLevel;
import l2r.gameserver.handler.admincommands.impl.AdminCheckBot;
import l2r.gameserver.handler.admincommands.impl.AdminClanHall;
import l2r.gameserver.handler.admincommands.impl.AdminClientSupport;
import l2r.gameserver.handler.admincommands.impl.AdminCreateItem;
import l2r.gameserver.handler.admincommands.impl.AdminCursedWeapons;
import l2r.gameserver.handler.admincommands.impl.AdminDelete;
import l2r.gameserver.handler.admincommands.impl.AdminDisconnect;
import l2r.gameserver.handler.admincommands.impl.AdminDoorControl;
import l2r.gameserver.handler.admincommands.impl.AdminEditChar;
import l2r.gameserver.handler.admincommands.impl.AdminEffects;
import l2r.gameserver.handler.admincommands.impl.AdminEnchant;
import l2r.gameserver.handler.admincommands.impl.AdminEvent;
import l2r.gameserver.handler.admincommands.impl.AdminEventEngine;
import l2r.gameserver.handler.admincommands.impl.AdminEventMatch;
import l2r.gameserver.handler.admincommands.impl.AdminEvents;
import l2r.gameserver.handler.admincommands.impl.AdminFacebook;
import l2r.gameserver.handler.admincommands.impl.AdminGeodata;
import l2r.gameserver.handler.admincommands.impl.AdminGlobalEvent;
import l2r.gameserver.handler.admincommands.impl.AdminGm;
import l2r.gameserver.handler.admincommands.impl.AdminGmChat;
import l2r.gameserver.handler.admincommands.impl.AdminHeal;
import l2r.gameserver.handler.admincommands.impl.AdminHellbound;
import l2r.gameserver.handler.admincommands.impl.AdminHelpPage;
import l2r.gameserver.handler.admincommands.impl.AdminIP;
import l2r.gameserver.handler.admincommands.impl.AdminInstance;
import l2r.gameserver.handler.admincommands.impl.AdminKill;
import l2r.gameserver.handler.admincommands.impl.AdminLevel;
import l2r.gameserver.handler.admincommands.impl.AdminMammon;
import l2r.gameserver.handler.admincommands.impl.AdminManor;
import l2r.gameserver.handler.admincommands.impl.AdminMassRecall;
import l2r.gameserver.handler.admincommands.impl.AdminMenu;
import l2r.gameserver.handler.admincommands.impl.AdminMonsterRace;
import l2r.gameserver.handler.admincommands.impl.AdminNochannel;
import l2r.gameserver.handler.admincommands.impl.AdminOlympiad;
import l2r.gameserver.handler.admincommands.impl.AdminPSPoints;
import l2r.gameserver.handler.admincommands.impl.AdminPcCondOverride;
import l2r.gameserver.handler.admincommands.impl.AdminPetition;
import l2r.gameserver.handler.admincommands.impl.AdminPhantoms;
import l2r.gameserver.handler.admincommands.impl.AdminPledge;
import l2r.gameserver.handler.admincommands.impl.AdminPolymorph;
import l2r.gameserver.handler.admincommands.impl.AdminPremium;
import l2r.gameserver.handler.admincommands.impl.AdminQuests;
import l2r.gameserver.handler.admincommands.impl.AdminReload;
import l2r.gameserver.handler.admincommands.impl.AdminRepairChar;
import l2r.gameserver.handler.admincommands.impl.AdminRes;
import l2r.gameserver.handler.admincommands.impl.AdminRide;
import l2r.gameserver.handler.admincommands.impl.AdminSS;
import l2r.gameserver.handler.admincommands.impl.AdminScripts;
import l2r.gameserver.handler.admincommands.impl.AdminServer;
import l2r.gameserver.handler.admincommands.impl.AdminShop;
import l2r.gameserver.handler.admincommands.impl.AdminShutdown;
import l2r.gameserver.handler.admincommands.impl.AdminSkill;
import l2r.gameserver.handler.admincommands.impl.AdminSpawn;
import l2r.gameserver.handler.admincommands.impl.AdminStriderRace;
import l2r.gameserver.handler.admincommands.impl.AdminTarget;
import l2r.gameserver.handler.admincommands.impl.AdminTeam;
import l2r.gameserver.handler.admincommands.impl.AdminTeleport;
import l2r.gameserver.handler.admincommands.impl.AdminTournament;
import l2r.gameserver.handler.admincommands.impl.AdminWipe;
import l2r.gameserver.handler.admincommands.impl.AdminZone;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Log;

public class AdminCommandHandler extends AbstractHolder
{
	private static final AdminCommandHandler _instance = new AdminCommandHandler();
	
	public static AdminCommandHandler getInstance()
	{
		return _instance;
	}
	
	private final Map<String, IAdminCommandHandler> _datatable = new HashMap<>();
	
	private AdminCommandHandler()
	{
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminAttribute());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminCamera());
		registerAdminCommandHandler(new AdminCancel());
		registerAdminCommandHandler(new AdminChangeAccessLevel());
		registerAdminCommandHandler(new AdminClanHall());
		registerAdminCommandHandler(new AdminClientSupport());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminDisconnect());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminEventMatch());
		registerAdminCommandHandler(new AdminGeodata());
		registerAdminCommandHandler(new AdminGlobalEvent());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHellbound());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminInstance());
		registerAdminCommandHandler(new AdminIP());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminMassRecall());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminMonsterRace());
		registerAdminCommandHandler(new AdminNochannel());
		registerAdminCommandHandler(new AdminOlympiad());
		registerAdminCommandHandler(new AdminPcCondOverride());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPhantoms());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminPSPoints());
		registerAdminCommandHandler(new AdminQuests());
		registerAdminCommandHandler(new AdminReload());
		registerAdminCommandHandler(new AdminRepairChar());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminRide());
		registerAdminCommandHandler(new AdminServer());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminShutdown());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminScripts());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminSS());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminTeam());
		// registerAdminCommandHandler(new AdminTest());
		registerAdminCommandHandler(new AdminZone());
		registerAdminCommandHandler(new AdminKill());
		registerAdminCommandHandler(new AdminCheckBot());
		registerAdminCommandHandler(new AdminWipe());
		registerAdminCommandHandler(new AdminPremium());
		registerAdminCommandHandler(new AdminEvent());
		registerAdminCommandHandler(new AdminEventEngine());
		registerAdminCommandHandler(new AdminEvents());
		registerAdminCommandHandler(new AdminAugment());
		registerAdminCommandHandler(new AdminStriderRace());
		registerAdminCommandHandler(new AdminTournament());
		registerAdminCommandHandler(new AdminFacebook());
		
	}
	
	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		for (Enum<?> e : handler.getAdminCommandEnum())
		{
			_datatable.put(e.toString().toLowerCase(), handler);
		}
	}
	
	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}
	
	public void useAdminCommandHandler(Player activeChar, String adminCommand)
	{


		String[] wordList = adminCommand.split(" ");
		IAdminCommandHandler handler = wordList.length != 0 ? _datatable.get(wordList[0]) : null;
		if (handler != null)
		{
			boolean success = false;
			try
			{
				for (Enum<?> e : handler.getAdminCommandEnum())
				{
					if (e.toString().equalsIgnoreCase(wordList[0]))
					{
						success = handler.useAdminCommand(e, wordList, adminCommand, activeChar);
						break;
					}
				}
			}
			catch (Exception e)
			{
				error("", e);
			}
			
			if (activeChar.getAccessLevel().allowAltG())
			{
				Log.LogCommand(activeChar, activeChar.getTarget(), adminCommand, success);
			}
		}
	}
	
	@Override
	public void process()
	{
		
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	@Override
	public void clear()
	{
		_datatable.clear();
	}
	
	/**
	 * ÐŸÐ¾Ð»ÑƒÑ‡ÐµÐ½Ð¸Ðµ Ñ�Ð¿Ð¸Ñ�ÐºÐ° Ð·Ð°Ñ€ÐµÐ³Ð¸Ñ�Ñ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ñ… Ð°Ð´Ð¼Ð¸Ð½ ÐºÐ¾Ð¼Ð°Ð½Ð´
	 * @return Ñ�Ð¿Ð¸Ñ�Ð¾Ðº ÐºÐ¾Ð¼Ð°Ð½Ð´
	 */
	public Set<String> getAllCommands()
	{
		return _datatable.keySet();
	}
}