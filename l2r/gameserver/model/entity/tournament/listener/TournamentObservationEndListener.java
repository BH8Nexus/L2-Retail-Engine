package l2r.gameserver.model.entity.tournament.listener;

import l2r.gameserver.listener.actor.player.OnLeaveObserverModeListener;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.tournament.BattleInstance;
import l2r.gameserver.model.entity.tournament.BattleObservationManager;

public class TournamentObservationEndListener implements OnLeaveObserverModeListener
{
	private final BattleInstance _battle;
	
	public TournamentObservationEndListener(BattleInstance battle)
	{
		_battle = battle;
	}
	
	@Override
	public void onLeaveObserverMode(Player player)
	{
		BattleObservationManager.onLeaveObservation(_battle, player);
	}
}
