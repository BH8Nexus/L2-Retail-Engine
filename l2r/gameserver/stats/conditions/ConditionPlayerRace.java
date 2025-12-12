package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.stats.Env;
import l2r.gameserver.utils.Util;
public class ConditionPlayerRace extends Condition
{
	private final Race[] _races;

	public ConditionPlayerRace(Race[] races)
	{
		//_race = Race.valueOf(race.toLowerCase());
		_races = races;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		//return ((Player) env.character).getRace() == _race;
		return Util.contains(_races, ((Player) env.character).getRace());
	}
}