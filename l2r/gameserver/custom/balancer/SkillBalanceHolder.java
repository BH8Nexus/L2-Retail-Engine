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
public class SkillBalanceHolder {
	public enum SkillChangeType {
		Power(0, false, false), CastTime(1, true, true), Reuse(2, true, true), Chance(3, true, false), SkillBlow(4,
				false, false), MCrit(5, false, false), PCrit(6, false, false);

		private final boolean _ForceCheck;
		private final boolean _IsOnlyVsAll;
		private final int _changeId;
		public static final SkillChangeType[] VALUES = values();

		private SkillChangeType(int attackId, boolean ForceCheck, boolean IsOnlyVsAll) {
			_changeId = attackId;
			_ForceCheck = ForceCheck;
			_IsOnlyVsAll = IsOnlyVsAll;
		}

		public boolean isForceCheck() {
			return _ForceCheck;
		}

		public boolean isOnlyVsAll() {
			return _IsOnlyVsAll;
		}

		public int getId() {
			return _changeId;
		}
	}

	private final int _SkillId;
	private final int _target;
	private final Map<SkillChangeType, Double> list = new ConcurrentHashMap<>();
	private final Map<SkillChangeType, Double> olylist = new ConcurrentHashMap<>();

	public SkillBalanceHolder(int SkillId, int target) {
		_SkillId = SkillId;
		_target = target;
	}

	public int getSkillId() {
		return _SkillId;
	}

	public String getSkillIcon() {
		return _SkillId < 1000 ? (_SkillId < 100 ? _SkillId < 10 ? "000" : "00" : "0") + String.valueOf(_SkillId)
				: String.valueOf(_SkillId);
	}

	public int getTarget() {
		return _target;
	}

	public Map<SkillChangeType, Double> getNormalBalance() {
		Map<SkillChangeType, Double> _map2 = new TreeMap<>(new AttackTypeComparator());
		_map2.putAll(list);
		return _map2;
	}

	public Map<SkillChangeType, Double> getOlyBalance() {
		Map<SkillChangeType, Double> _map2 = new TreeMap<>(new AttackTypeComparator());
		_map2.putAll(olylist);
		return _map2;
	}

	private class AttackTypeComparator implements Comparator<SkillChangeType> {
		public AttackTypeComparator() {
		}

		@Override
		public int compare(SkillChangeType l, SkillChangeType r) {
			int left = l.getId();
			int right = r.getId();
			if (left > right) {
				return 1;
			}
			if (left < right) {
				return -1;
			}

			Random x = new Random();

			return (x.nextInt(2) == 1) ? 1 : 1;
		}
	}

	public void remove(SkillChangeType sct) {
		if (list.containsKey(sct)) {
			list.remove(sct);
		}
	}

	public void addSkillBalance(SkillChangeType sct, double value) {
		list.put(sct, value);
	}

	public double getValue(SkillChangeType sct) {
		if (list.containsKey(sct)) {
			return list.get(sct);
		}
		return 1.0;
	}

	public void removeOly(SkillChangeType sct) {
		if (olylist.containsKey(sct)) {
			olylist.remove(sct);
		}
	}

	public void addOlySkillBalance(SkillChangeType sct, double value) {
		olylist.put(sct, value);
	}

	public double getOlyBalanceValue(SkillChangeType sct) {
		if (olylist.containsKey(sct)) {
			return olylist.get(sct);
		}
		return 1.0;

	}
}
