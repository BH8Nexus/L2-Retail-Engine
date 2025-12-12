package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Player;
import l2r.gameserver.stats.Env;

public class ConditionPlayerIsMage extends Condition
{
	private final boolean _value;

	public ConditionPlayerIsMage(boolean v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		
		return ((Player) env.character).isMageClass() == _value;
	}
}