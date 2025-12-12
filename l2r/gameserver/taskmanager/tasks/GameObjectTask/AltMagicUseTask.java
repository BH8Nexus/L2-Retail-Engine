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
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;

/**
 * @author Flash
 *
 */

// ============================ Таски для L2Character ==============================

/** AltMagicUseTask */
public class AltMagicUseTask extends RunnableImpl
{
	public final Skill _skill;
	private final HardReference<? extends Creature> _charRef, _targetRef;
	
	public AltMagicUseTask(Creature character, Creature target, Skill skill)
	{
		_charRef = character.getRef();
		_targetRef = target.getRef();
		_skill = skill;
	}
	
	@Override
	public void runImpl()
	{
		Creature cha, target;
		if (((cha = _charRef.get()) == null) || ((target = _targetRef.get()) == null))
		{
			return;
		}
		cha.altOnMagicUseTimer(target, _skill);
	}
}
