package l2r.gameserver.listener.actor.player;

import java.util.Map;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.base.RestartType;
import l2r.gameserver.utils.Location;

public interface OnDieWindowListener extends PlayerListener
{
	void showDieWindow(Map<RestartType, Boolean> resTypes);
	
	Location getRessurectPoint(RestartType resType);
}
