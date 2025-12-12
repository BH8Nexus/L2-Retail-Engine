package l2r.gameserver.stats.conditions;

import l2r.gameserver.stats.Env;

public class ConditionPlayerDivision extends Condition
{
	private final int _division;

	/**
	 * If env.skill is present, it will check skill damage instead of autoattack damage.
	 * @param damage
	 */
	public ConditionPlayerDivision(int divisionOrdinal)
	{
		_division = divisionOrdinal;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;

		if (env.character.getPlayer().getDivision().ordinal() >= _division)
			return true;

		return false;
	}
}
