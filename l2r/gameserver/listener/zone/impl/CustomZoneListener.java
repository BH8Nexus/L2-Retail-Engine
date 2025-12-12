// package l2r.gameserver.listener.zone.impl;
//
// import java.util.HashMap;
// import java.util.Map;
//
// import l2r.commons.util.Rnd;
// import l2r.gameserver.listener.actor.player.OnDieWindowListener;
// import l2r.gameserver.listener.zone.OnZoneEnterLeaveListener;
// import l2r.gameserver.model.Creature;
// import l2r.gameserver.model.Zone;
// import l2r.gameserver.model.base.RestartType;
// import l2r.gameserver.network.serverpackets.StatusUpdate;
// import l2r.gameserver.utils.Location;
//
// public class CustomZoneListener implements OnZoneEnterLeaveListener
// {
// public static final OnZoneEnterLeaveListener STATIC = new CustomZoneListener();
//
// private static final Map<Zone, OnDieWindowListener> _dieWindowListeners = new HashMap<>();
//
// @Override
// public void onZoneEnter(Zone zone, Creature actor)
// {
// if (!actor.isPlayable())
// {
// return;
// }
//
// if (zone.getParams().getBool("pvpflag_on_enter", false))
// {
// actor.getPlayer().startPvPFlag(null, Integer.MAX_VALUE); // Perma pvpflag
// }
// if (zone.getParams().getBool("keep_buffs_on_death", false)) // Dont lose buffs on death
// {
// actor.setIsBlessedByNoblesse(true);
// }
// if (zone.getParams().getBool("hide_player_identity", false))
// {
// actor.getPlayer().addVar("hide_player_identity_by_classid", "true");
// actor.getPlayer().addVar("visible_clan", 0);
// }
// if (!zone.getParams().getBool("can_party", true))
// {
// actor.getPlayer().addVar("can_party", "false");
// actor.getPlayer().leaveParty();
// }
//
// if (!zone.getRestartPoints().isEmpty())
// {
// OnDieWindowListener listener = _dieWindowListeners.getOrDefault(zone, null);
// if (listener == null)
// {
// listener = makeListener(zone);
// _dieWindowListeners.put(zone, listener);
// }
//
// if (listener != null)
// {
// actor.getPlayer().addListener(listener);
// }
// }
//
// actor.getPlayer().broadcastCharInfo();
// actor.getPlayer().sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);
// actor.getPlayer().broadcastRelationChanged();
// }
//
// @Override
// public void onZoneLeave(Zone zone, Creature actor)
// {
// if (!actor.isPlayable())
// {
// return;
// }
//
// if (zone.getParams().getBool("pvpflag_on_enter", false))
// {
// actor.getPlayer().stopPvPFlag(); // Remove perma pvpflag
// actor.getPlayer().startPvPFlag(null); // Default pvpflag
// }
// if (zone.getParams().getBool("keep_buffs_on_death", false)) // Dont lose buffs on death
// {
// actor.setIsBlessedByNoblesse(false);
// }
// if (zone.getParams().getBool("hide_player_identity", false))
// {
// actor.getPlayer().unsetVar("hide_player_identity_by_classid");
// actor.getPlayer().unsetVar("visible_clan");
// }
// if (!zone.getParams().getBool("can_party", true))
// {
// actor.getPlayer().unsetVar("can_party");
// }
//
// if (!zone.getRestartPoints().isEmpty())
// {
// OnDieWindowListener listener = _dieWindowListeners.getOrDefault(zone, null);
// if (listener != null)
// {
// actor.getPlayer().removeListener(listener);
// }
// }
//
// actor.getPlayer().broadcastCharInfo();
// actor.getPlayer().sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);
// actor.getPlayer().broadcastRelationChanged();
// }
//
// private static OnDieWindowListener makeListener(Zone zone)
// {
// return new OnDieWindowListener()
// {
// private final Zone _zone = zone;
//
// @Override
// public void showDieWindow(Map<RestartType, Boolean> resTypes)
// {
// if (_zone != null)
// {
// resTypes.put(RestartType.TO_FLAG, true);
// }
// }
//
// @Override
// public Location getRessurectPoint(RestartType resType)
// {
// if ((_zone != null) && (resType == RestartType.TO_FLAG))
// {
// return Rnd.get(_zone.getRestartPoints());
// }
//
// return null;
// }
// };
// }
//
// /*
// * (non-Javadoc)
// * @see l2r.gameserver.listener.zone.OnZoneEnterLeaveListener#onEquipChanged(l2r.gameserver.model.Zone, l2r.gameserver.model.Creature)
// */
// @Override
// public void onEquipChanged(Zone zone, Creature actor)
// {
// // TODO Auto-generated method stub
//
// }
// }
