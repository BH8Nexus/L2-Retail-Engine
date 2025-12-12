package l2r.gameserver.taskmanager.tasks;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.Creature;

/** Task of Checking Skill Cast Landing **/
public class MagicGeoCheckTask extends RunnableImpl
{
	private final HardReference<? extends Creature> _charRef;

	public MagicGeoCheckTask(Creature cha)
	{
		_charRef = cha.getRef();
	}

	@Override
	public void runImpl()
	{
		Creature character = _charRef.get();
		if (character == null)
		{
			return;
		}
		Creature castingTarget = character.getCastingTarget();
		if (castingTarget == null)
		{
			return;
		}
		if (!GeoEngine.canSeeTarget(character, castingTarget, character.isFlying()))
		{
			return;
		}

		character._skillGeoCheckTask = null;
	}
}
