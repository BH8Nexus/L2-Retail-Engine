/*
 * Copyright (C) 2004-2020 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.taskmanager.tasks.GameObjectTask;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;

/**
 * @author Flash
 *
 */
/** Checks if the skill conditions are met during casttime, if not, skill will be aborted */
public class MagicCheckTask extends RunnableImpl
{
	private final HardReference<? extends Creature> _charRef;
	
	public MagicCheckTask(Creature cha)
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
		Skill castingSkill = character.getCastingSkill();
		Creature castingTarget = character.getCastingTarget();
		if ((castingSkill == null) || (castingTarget == null))
		{
			character.clearCastVars();
			return;
		}
		
		// if ((character.getCastingTime() >= Config.GEODATA_SKILL_CHECK_TASK_INTERVAL) && !GeoEngine.canSeeTarget(character, castingTarget, false))
		if (!GeoEngine.canSeeTarget(character, castingTarget, false) && (character.getAnimationEndTime60() > System.currentTimeMillis()))
		{
			character.abortCast(true, false); // Retail doesnt send abort cast message.
		}
	}
}
