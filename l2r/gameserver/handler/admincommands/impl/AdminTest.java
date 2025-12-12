// package l2r.gameserver.handler.admincommands.impl;
//
// import java.io.File;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
//
// import com.google.common.io.Files;
//
// import l2r.gameserver.Config;
// import l2r.gameserver.data.xml.holder.EventHolder;
// import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
// import l2r.gameserver.model.Player;
// import l2r.gameserver.model.entity.events.EventType;
// import l2r.gameserver.model.entity.events.impl.L2WKrateisCubeEvent;
// import l2r.gameserver.model.entity.events.impl.L2WorldPvPEvent;
// import l2r.gameserver.network.serverpackets.ExServerPrimitive;
// import l2r.gameserver.utils.Location;
//
// public class AdminTest implements IAdminCommandHandler
// {
// private static enum Commands
// {
// admin_saveloc,
// admin_draw,
// admin_kratei,
// admin_worldpvp
// }
//
// @Override
// public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
// {
// Commands command = (Commands) comm;
//
// switch (command)
// {
// case admin_saveloc:
// File file = Config.findNonCustomResource("saveloc/saveloc.txt");
// if (file == null)
// {
// new File("saveloc/saveloc.txt");
// }
// file = Config.findNonCustomResource("saveloc/saveloc.txt");
// if (file == null)
// {
// activeChar.sendMessage("Unable to write to file.");
// }
//
// StringBuilder sb = new StringBuilder();
// sb.append("new Location(" + activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ")");
// if (wordList.length > 1)
// {
// sb.append(" //");
// for (int i = 1; i < wordList.length; i++)
// {
// sb.append(wordList[i]);
// }
// }
//
// activeChar.sendMessage(sb.toString());
// System.out.println(sb);
//
// try
// {
// Files.write(sb.toString(), file, StandardCharsets.UTF_8);
// }
// catch (IOException e)
// {
// activeChar.sendMessage("Failed to write!!!");
// e.printStackTrace();
// }
//
// break;
// case admin_draw:
// ExServerPrimitive packetLow = new ExServerPrimitive("1", new Location(0, 0, 0));
// packetLow.addPoint(new Location(0, 0, 0));
// activeChar.sendPacket(packetLow);
// /*
// * List<Point3D> pointsLow = GeometryUtils.generateCirclePoints(activeChar.getLoc().changeZ(-2000), 20000, 1); List<Point3D> pointsHigh = GeometryUtils.generateCirclePoints(activeChar.getLoc().changeZ(2000), 20000, 1); Location locRoof = activeChar.getLoc().changeZ(3000); Location
// * centerLoc = activeChar.getLoc().changeZ(50); ExServerPrimitive packetLow = new ExServerPrimitive("", centerLoc); ExServerPrimitive packetHigh = new ExServerPrimitive("", centerLoc); ExServerPrimitive packetConnectLowHi = new ExServerPrimitive("", centerLoc); ExServerPrimitive
// * packetRoof = new ExServerPrimitive("", centerLoc); Point3D lastLocLow = pointsLow.get(pointsLow.size() - 1); Point3D lastLocHigh = pointsHigh.get(pointsHigh.size() - 1); for (Point3D locLow : pointsLow) { packetLow.addLine(lastLocLow, locLow); lastLocLow = locLow; } for (Point3D
// * locHigh : pointsHigh) { packetHigh.addLine(Color.YELLOW, lastLocHigh, locHigh); lastLocHigh = locHigh; } for (int i = 0; i < pointsLow.size(); i++) { Point3D loc1 = pointsLow.get(i); Point3D loc2 = pointsHigh.get(i); packetConnectLowHi.addLine(Color.YELLOW, loc1, loc2); } for
// * (Point3D locHigh : pointsHigh) { packetRoof.addLine(Color.YELLOW, locHigh, locRoof); lastLocHigh = locHigh; } for (int i = 0; i < pointsLow.size(); i++) { Point3D loc1 = pointsLow.get(i); Point3D loc2 = pointsHigh.get(i); packetConnectLowHi.addLine(Color.YELLOW, loc1, loc2); }
// * packetConnectLowHi.addPoint("PEEEE VEEEEE PEEEE ZONE!!!!", centerLoc); L2ObjectsStorage.getAllPlayersStream().forEach(p -> p.sendPacket(packetLow, packetHigh, packetConnectLowHi, packetRoof)); break;
// */break;
// case admin_kratei:
// L2WKrateisCubeEvent event = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, L2WKrateisCubeEvent.EVENT_ID);
// if (event == null)
// {
// activeChar.sendMessage("Event is null.");
// return false;
// }
//
// if (wordList.length > 1)
// {
// if ("start".equalsIgnoreCase(wordList[1]))
// {
// event.startEvent();
// }
// else if ("stop".equalsIgnoreCase(wordList[1]))
// {
// event.stopEvent();
// }
// else if ("recalc".equalsIgnoreCase(wordList[1]))
// {
// event.reCalcNextTime(false);
// }
// else if ("register".equalsIgnoreCase(wordList[1]))
// {
// event.startEventRegistration(wordList.length > 2 ? Integer.parseInt(wordList[2]) : 10);
// }
// else if ("setpoints".equalsIgnoreCase(wordList[1]))
// {
// if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
// {
// activeChar.sendMessage("Target a player");
// }
// else
// {
// activeChar.getTarget().getPlayer().setDivisionPoints(Integer.parseInt(wordList[2]));
// }
// }
// else if ("getpoints".equalsIgnoreCase(wordList[1]))
// {
// if ((activeChar.getTarget() == null) || !activeChar.getTarget().isPlayer())
// {
// activeChar.sendMessage("Target a player");
// }
// else
// {
// activeChar.sendMessage(activeChar.getTarget().getName() + "'s Points =>");
// activeChar.sendMessage("Division Points: " + activeChar.getTarget().getPlayer().getDivisionPoints());
// activeChar.sendMessage("Kratei's Points: " + activeChar.getTarget().getPlayer().getCounters().KrateisCubePoints);
// activeChar.sendMessage("Both Points must be EQUAL!");
// activeChar.sendMessage("=============================");
// }
// }
// }
// break;
// case admin_worldpvp:
// L2WorldPvPEvent pvpEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, L2WorldPvPEvent.EVENT_ID);
// if (pvpEvent == null)
// {
// activeChar.sendMessage("Event is null.");
// return false;
// }
//
// if (wordList.length > 1)
// {
// if ("start".equalsIgnoreCase(wordList[1]))
// {
// pvpEvent.startEvent();
// }
// else if ("stop".equalsIgnoreCase(wordList[1]))
// {
// pvpEvent.stopEvent();
// }
// else if ("recalc".equalsIgnoreCase(wordList[1]))
// {
// pvpEvent.reCalcNextTime(false);
// }
// }
// }
//
// return true;
// }
//
// @Override
// public Enum<?>[] getAdminCommandEnum()
// {
// return Commands.values();
// }
// }