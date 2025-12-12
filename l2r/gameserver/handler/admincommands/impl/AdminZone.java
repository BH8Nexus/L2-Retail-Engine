package l2r.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.residence.Castle;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.mapregion.DomainArea;
import l2r.gameserver.utils.GArray;

public class AdminZone implements IAdminCommandHandler
{
	private static GArray<int[]> create_loc;
	private static int _loc_id = 900521;
	private static int create_loc_id;
	
	private static enum Commands
	{
		admin_zone_check,
		admin_region,
		admin_pos,
		admin_vis_count,
		admin_domain,
		admin_location
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch (command)
		{
			case admin_zone_check:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message1", activeChar).addString(String.valueOf(activeChar.getCurrentRegion())));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message2", activeChar));
				List<Zone> zones = new ArrayList<>();
				World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
				for (Zone zone : zones)
				{
					if (zone.isActive())
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message3", activeChar).addString(zone.getType().toString()).addString(zone.getName()).addString(String.valueOf(zone.checkIfInZone(activeChar))).addString(String.valueOf(zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()))));
					}
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message4", activeChar).addString(zone.getType().toString()).addString(zone.getName()).addString(String.valueOf(zone.checkIfInZone(activeChar))).addString(String.valueOf(zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()))));
					}
					// activeChar.sendMessage(activeChar.isLangRus() ? zone.getType().toString() + ", ΠΈΠΌΡ�: " + zone.getName() + ", Ρ�ΠΎΡ�Ρ‚ΠΎΡ�Π½ΠΈΠµ: " + (zone.isActive() ? "active" : "not active") + ", Π²Π½ΡƒΡ‚Ρ€ΠΈ: " + zone.checkIfInZone(activeChar) + "/" +
					// zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()) : zone.getType().toString() + ", name: " + zone.getName() + ", state: " + (zone.isActive() ? "active" : "not active") + ", inside: " + zone.checkIfInZone(activeChar) + "/" +
					// zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()));
				}
				
				break;
			}
			case admin_region:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message5", activeChar).addString(activeChar.getCurrentRegion().toString()));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message6", activeChar));
				for (GameObject o : activeChar.getCurrentRegion())
				{
					if (o != null)
					{
						activeChar.sendMessage(o.toString());
					}
				}
				break;
			}
			case admin_vis_count:
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message7", activeChar).addString(activeChar.getCurrentRegion().toString()));
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message8", activeChar).addNumber(World.getAroundPlayers(activeChar).size()));
				break;
			}
			case admin_pos:
			{
				String pos = activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " + activeChar.getHeading() + " Geo [" + ((activeChar.getX() - World.MAP_MIN_X) >> 4) + ", " + ((activeChar.getY() - World.MAP_MIN_Y) >> 4) + "] Ref " + activeChar.getReflectionId();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message9", activeChar).addString(pos));
				break;
			}
			case admin_location:
				locationMenu(activeChar);
				break;
			case admin_domain:
			{
				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, activeChar);
				Castle castle = domain != null ? ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId()) : null;
				if (castle != null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message10", activeChar).addString(castle.getName()));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminzone.message11", activeChar));
				}
			}
		}
		return true;
	}
	
	private static void locationMenu(Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuffer replyMSG = new StringBuffer("<html><body><title>Location Create</title>");
		
		replyMSG.append("<center><table width=260><tr>");
		replyMSG.append("<td width=70>Location:</td>");
		replyMSG.append("<td width=50><edit var=\"loc\" width=50 height=12></td>");
		replyMSG.append("<td width=50><button value=\"Show\" action=\"bypass -h admin_showloc $loc\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=90><button value=\"New Location\" action=\"bypass -h admin_loc_begin $loc\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=90><button value=\"New Location End\" action=\"bypass -h admin_loc_begin " + _loc_id + "\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br><br></center>");
		
		if (create_loc != null)
		{
			replyMSG.append("<center><table width=260><tr>");
			replyMSG.append("<td width=80><button value=\"Add Point\" action=\"bypass -h admin_loc_add menu\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=90><button value=\"Reset Points\" action=\"bypass -h admin_loc_reset menu\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("<td width=90><button value=\"End Location\" action=\"bypass -h admin_loc_end menu\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			replyMSG.append("</tr></table></center>");
			
			replyMSG.append("<center><button value=\"Show\" action=\"bypass -h admin_loc_showloc " + create_loc_id + " menu\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
			
			replyMSG.append("<br><br>");
			
			int i = 0;
			for (int[] loc : create_loc)
			{
				replyMSG.append("<button value=\"Remove\" action=\"bypass -h admin_loc_remove " + i + "\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
				replyMSG.append("&nbsp;&nbsp;(" + loc[0] + ", " + loc[1] + ", " + loc[2] + ")<br1>");
				i++;
			}
		}
		
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}