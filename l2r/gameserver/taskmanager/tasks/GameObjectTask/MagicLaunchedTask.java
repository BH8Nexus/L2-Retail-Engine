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

import java.util.List;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;

/**
 * @author Flash
 *
 */
/** MagicLaunchedTask */
public class MagicLaunchedTask extends RunnableImpl
{
	public boolean _forceUse;
	private final HardReference<? extends Creature> _charRef;
	
	public MagicLaunchedTask(Creature cha, boolean forceUse)
	{
		_charRef = cha.getRef();
		_forceUse = forceUse;
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
		List<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
		character.broadcastPacket(new MagicSkillLaunched(character.getObjectId(), castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), targets));
	}
}
