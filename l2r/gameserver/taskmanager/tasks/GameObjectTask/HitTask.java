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
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.Creature;

/**
 * @author Flash
 *
 */

/** HitTask */
public class HitTask extends RunnableImpl
{
	boolean _crit, _miss, _shld, _soulshot, _unchargeSS, _notify;
	int _damage;
	private final HardReference<? extends Creature> _charRef, _targetRef;
	
	public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify)
	{
		_charRef = cha.getRef();
		_targetRef = target.getRef();
		_damage = damage;
		_crit = crit;
		_shld = shld;
		_miss = miss;
		_soulshot = soulshot;
		_unchargeSS = unchargeSS;
		_notify = notify;
	}
	
	@Override
	public void runImpl()
	{
		Creature character, target;
		if (((character = _charRef.get()) == null) || ((target = _targetRef.get()) == null))
		{
			return;
		}
		
		if (character.isAttackAborted())
		{
			return;
		}
		
		character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);
		
		if (_notify)
		{
			character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
		}
	}
}
