package l2r.gameserver.handler.admincommands.impl;

import java.awt.Color;

import l2r.commons.geometry.Point3D;
import l2r.gameserver.Config;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.ExServerPrimitive;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AdminGeodata implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_geo_z,
		admin_geo_type,
		admin_geo_nswe,
		admin_geo_los,
		admin_geo_load,
		admin_geo_dump,
		admin_geo_trace,
		admin_geo_map,
		admin_geogrid
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch (command)
		{
			case admin_geo_z:
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message1", activeChar).addNumber(GeoEngine.getHeight(activeChar.getLoc(), activeChar.getReflectionId())).addNumber(activeChar.getZ()));
				break;
			case admin_geo_type:
				int type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getReflectionId());
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message2", activeChar).addNumber(type));
				break;
			case admin_geo_nswe:
				String result = "";
				byte nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getReflectionId());
				if ((nswe & 8) == 0)
				{
					result += " N";
				}
				if ((nswe & 4) == 0)
				{
					result += " S";
				}
				if ((nswe & 2) == 0)
				{
					result += " W";
				}
				if ((nswe & 1) == 0)
				{
					result += " E";
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message3", activeChar).addNumber(nswe).addString(result));
				break;
			case admin_geo_los:
				if (activeChar.getTarget() != null)
				{
					if (GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false))
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message4", activeChar));
					}
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message5", activeChar));
					}
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message6", activeChar));
				}
				break;
			case admin_geo_load:
				if (wordList.length != 3)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message7", activeChar));
				}
				else
				{
					try
					{
						byte rx = Byte.parseByte(wordList[1]);
						byte ry = Byte.parseByte(wordList[2]);
						if (GeoEngine.LoadGeodataFile(rx, ry))
						{
							GeoEngine.LoadGeodataFile(rx, ry);
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message8", activeChar).addNumber(rx).addNumber(ry));
						}
						else
						{
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message9", activeChar).addNumber(rx).addNumber(ry));
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.AdminGeodata.Error", activeChar));
					}
				}
				break;
			case admin_geo_dump:
				if (wordList.length > 2)
				{
					GeoEngine.DumpGeodataFileMap(Byte.parseByte(wordList[1]), Byte.parseByte(wordList[2]));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message10", activeChar).addString(wordList[1]).addString(wordList[2]));
				}
				GeoEngine.DumpGeodataFile(activeChar.getX(), activeChar.getY());
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message11", activeChar));
				break;
			case admin_geo_trace:
				if (wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message12", activeChar));
					return false;
				}
				if (wordList[1].equalsIgnoreCase("on"))
				{
					activeChar.setVar("trace", "1", -1);
				}
				else if (wordList[1].equalsIgnoreCase("off"))
				{
					activeChar.unsetVar("trace");
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message13", activeChar));
				}
				break;
			case admin_geo_map:
				int x = ((activeChar.getX() - World.MAP_MIN_X) >> 15) + Config.GEO_X_FIRST;
				int y = ((activeChar.getY() - World.MAP_MIN_Y) >> 15) + Config.GEO_Y_FIRST;
				
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingeodata.message14", activeChar).addNumber(x).addNumber(y));
				break;
			case admin_geogrid:
				if (activeChar.getSessionVar("debug_geogrid") != null)
				{
					activeChar.sendMessage("Debug geogrid: off");
					activeChar.setSessionVar("debug_geogrid", null);
					// activeChar.sendPacket(new ExServerPrimitive());
				}
				else
				{
					activeChar.sendMessage("Debug geogrid: on");
					activeChar.setSessionVar("debug_geogrid", "true");
				}
				debugGrid(activeChar);
				break;
		}
		
		return true;
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	public static void debugGrid(Player player)
	{
		int geoRadius = 10;
		int blocksPerPacket = 49;
		if (geoRadius < 0)
		{
			throw new IllegalArgumentException("geoRadius < 0");
		}
		
		int iBlock = blocksPerPacket;
		int iPacket = 0;
		
		ExServerPrimitive exsp = null;
		
		int playerGx = GeoEngine.getGeoX(player.getX());
		int playerGy = GeoEngine.getGeoY(player.getY());
		for (int dx = -geoRadius; dx <= geoRadius; ++dx)
		{
			for (int dy = -geoRadius; dy <= geoRadius; ++dy)
			{
				if (iBlock >= blocksPerPacket)
				{
					iBlock = 0;
					if (exsp != null)
					{
						++iPacket;
						player.sendPacket(exsp);
					}
					exsp = new ExServerPrimitive("DebugGrid_" + iPacket, player.getX(), player.getY(), -16000);
				}
				
				if (exsp == null)
				{
					throw new IllegalStateException();
				}
				
				int gx = playerGx + dx;
				int gy = playerGy + dy;
				
				int x = GeoEngine.getWorldX(gx);
				int y = GeoEngine.getWorldY(gy);
				int z = player.getZ() + 20;// GeoEngine.getNearestZ(gx, gy, player.getZ());
				
				// north arrow
				Color col = getDirectionColor(gx, gy, z, GeoEngine.NORTH);
				exsp.addLine(col, new Point3D(x - 1, y - 7, z), new Point3D(x + 1, y - 7, z));
				exsp.addLine(col, new Point3D(x - 2, y - 6, z), new Point3D(x + 2, y - 6, z));
				exsp.addLine(col, new Point3D(x - 3, y - 5, z), new Point3D(x + 3, y - 5, z));
				exsp.addLine(col, new Point3D(x - 4, y - 4, z), new Point3D(x + 4, y - 4, z));
				
				// east arrow
				col = getDirectionColor(gx, gy, z, GeoEngine.EAST);
				exsp.addLine(col, new Point3D(x + 7, y - 1, z), new Point3D(x + 7, y + 1, z));
				exsp.addLine(col, new Point3D(x + 6, y - 2, z), new Point3D(x + 6, y + 2, z));
				exsp.addLine(col, new Point3D(x + 5, y - 3, z), new Point3D(x + 5, y + 3, z));
				exsp.addLine(col, new Point3D(x + 4, y - 4, z), new Point3D(x + 4, y + 4, z));
				
				// south arrow
				col = getDirectionColor(gx, gy, z, GeoEngine.SOUTH);
				exsp.addLine(col, new Point3D(x - 1, y + 7, z), new Point3D(x + 1, y + 7, z));
				exsp.addLine(col, new Point3D(x - 2, y + 6, z), new Point3D(x + 2, y + 6, z));
				exsp.addLine(col, new Point3D(x - 3, y + 5, z), new Point3D(x + 3, y + 5, z));
				exsp.addLine(col, new Point3D(x - 4, y + 4, z), new Point3D(x + 4, y + 4, z));
				
				col = getDirectionColor(gx, gy, z, GeoEngine.WEST);
				exsp.addLine(col, new Point3D(x - 7, y - 1, z), new Point3D(x - 7, y + 1, z));
				exsp.addLine(col, new Point3D(x - 6, y - 2, z), new Point3D(x - 6, y + 2, z));
				exsp.addLine(col, new Point3D(x - 5, y - 3, z), new Point3D(x - 5, y + 3, z));
				exsp.addLine(col, new Point3D(x - 4, y - 4, z), new Point3D(x - 4, y + 4, z));
				
				++iBlock;
			}
		}
		
		player.sendPacket(exsp);
	}
	
	private static Color getDirectionColor(int x, int y, int z, int dir)
	{
		
		if (GeoEngine.canMoveWithCollision(x, y, z, x, y, z, dir))
		{
			return Color.GREEN;
		}
		return Color.RED;
	}
}