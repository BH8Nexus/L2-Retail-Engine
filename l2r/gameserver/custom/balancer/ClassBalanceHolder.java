/*
 * Copyright (C) 2004-2016 L2J Server
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
package l2r.gameserver.custom.balancer;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DevAtlas
 */
public class ClassBalanceHolder
{
	public enum AttackType
	{
		Normal(0),
		Magic(1),
		Crit(2),
		MCrit(3),
		Blow(4),
		PhysicalSkillDamage(5),
		PhysicalSkillCritical(6);
		
		private int _attackId;
		public static final AttackType[] VALUES = values();
		
		private AttackType(int attackId)
		{
			_attackId = attackId;
		}
		
		public int getId()
		{
			return _attackId;
		}
	}
	
	private final int _ActiveClass;
	private final int _TargetClass;
	private final Map<AttackType, Double> _Normalbalance = new ConcurrentHashMap<>();
	private final Map<AttackType, Double> _Olybalance = new ConcurrentHashMap<>();
	
	/**
	 * @param activeClass
	 * @param target
	 */
	public ClassBalanceHolder(int activeClass, int target)
	{
		_ActiveClass = activeClass;
		_TargetClass = target;
	}
	
	public void addNormalBalance(AttackType type, double value)
	{
		_Normalbalance.put(type, value);
	}
	
	public void addOlyBalance(AttackType type, double value)
	{
		_Olybalance.put(type, value);
	}
	
	public int getTargetClass()
	{
		return _TargetClass;
	}
	
	public int getActiveClass()
	{
		return _ActiveClass;
	}
	
	public Map<AttackType, Double> getNormalBalance()
	{
		Map<AttackType, Double> _map2 = new TreeMap<>(new AttackTypeComparator());
		_map2.putAll(_Normalbalance);
		return _map2;
	}
	
	public void removeOlyBalance(AttackType type)
	{
		if (_Olybalance.containsKey(type))
		{
			_Olybalance.remove(type);
		}
	}
	
	public double getOlyBalanceValue(AttackType type)
	{
		if (!_Olybalance.containsKey(type))
		{
			return 1.0;
		}
		return _Olybalance.get(type);
	}
	
	public double getBalanceValue(AttackType type)
	{
		if (!_Normalbalance.containsKey(type))
		{
			return 1.0;
		}
		return _Normalbalance.get(type);
	}
	
	public void remove(AttackType type)
	{
		if (_Normalbalance.containsKey(type))
		{
			_Normalbalance.remove(type);
		}
	}
	
	public Map<AttackType, Double> getOlyBalance()
	{
		Map<AttackType, Double> _map2 = new TreeMap<>(new AttackTypeComparator());
		_map2.putAll(_Olybalance);
		return _map2;
	}
	
	private class AttackTypeComparator implements Comparator<AttackType>
	{
		public AttackTypeComparator()
		{
		}
		
		@Override
		public int compare(AttackType l, AttackType r)
		{
			int left = l.getId();
			int right = r.getId();
			if (left > right)
			{
				return 1;
			}
			if (left < right)
			{
				return -1;
			}
			
			Random x = new Random();
			
			return (x.nextInt(2) == 1) ? 1 : 1;
		}
	}
	
}