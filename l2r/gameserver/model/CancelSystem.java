/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;

/**
 * @author Nik, horato
 */

public class CancelSystem
{
	private static final Logger LOG = LoggerFactory.getLogger(CancelSystem.class);
	
	private Map<Integer, BuffRestore> _playerBuffRestore;
	private ScheduledFuture<?> _thread;
	
	private static final CancelSystem _instance = new CancelSystem();
	
	public static final CancelSystem getInstance()
	{
		return _instance;
	}
	
	public CancelSystem()
	{
		_playerBuffRestore = new ConcurrentHashMap<>();
		
		stopCancelSystem();
		
		_thread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new BuffRestoreImpl(), 10000, 10000);
	}
	
	public void onCancel(Creature cha, Effect eff)
	{
		if (Config.CANCEL_SYSTEM_RESTORE_DELAY <= 0)
		{
			return;
		}
		
		if (cha == null)
		{
			return;
		}
		
		if (eff.getSkill().getSkillType() != SkillType.BUFF)
		{
			return;
		}
		
		// Check if player/summon is in events or oly.
		if (cha.getPlayer() != null)
		{
			if (cha.getPlayer().isInOlympiadMode() || Olympiad.isRegistered(cha.getPlayer()) || Olympiad.isRegisteredInComp(cha.getPlayer()))
			{
				return;
			}
		}
		
		if (_playerBuffRestore.containsKey(cha.getObjectId()))
		{
			_playerBuffRestore.get(cha.getObjectId()).addEffect(eff);
		}
		else
		{
			_playerBuffRestore.put(cha.getObjectId(), new BuffRestore(cha, eff));
		}
	}
	
	private class BuffRestoreImpl extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			List<Integer> restored = new ArrayList<>();
			
			for (Entry<Integer, BuffRestore> n3 : _playerBuffRestore.entrySet())
			{
				int owenerObjId = n3.getKey();
				BuffRestore restore = n3.getValue();
				
				if (restore.tryRestore() || restore._cancelledEffects.isEmpty())
				{
					restored.add(owenerObjId);
				}
			}
			
			for (Integer objId : restored)
			{
				_playerBuffRestore.remove(objId);
			}
		}
		
	}
	
	private class BuffRestore
	{
		private final HardReference<? extends Creature> _owner;
		private final Map<Skill, Integer[]> _cancelledEffects = new HashMap<>();
		private long _startTime;
		private long _restoreTime;
		
		protected BuffRestore(Creature owner, Effect... effects)
		{
			_owner = owner.getRef();
			for (Effect effect : effects)
			{
				addEffect(effect);
			}
		}
		
		protected boolean tryRestore()
		{
			if (_restoreTime <= System.currentTimeMillis())
			{
				restoreBuffs();
				return true;
			}
			
			return false;
		}
		
		protected void addEffect(Effect eff)
		{
			Creature owner = _owner.get();
			if ((owner == null) || (owner.getPlayer() == null) || !owner.getPlayer().isOnline())
			{
				_cancelledEffects.clear();
				return;
			}
			
			// If this is the 1st canceled effect, begin the start time counter.
			if (_cancelledEffects.isEmpty())
			{
				_startTime = System.currentTimeMillis();
				// Original Message: ]An effect has been cancelled and it will be restored in 1 minute. If you get cancelled again before the time expires, it will be reset to 1 minute again.
				owner.sendMessage(new CustomMessage("l2r.gameserver.model.CancelSystem.message1", owner.getPlayer(), Config.CANCEL_SYSTEM_RESTORE_DELAY));
			}
			
			int classId = ((eff.getEffected() != null) && eff.getEffected().isPlayer()) ? eff.getEffected().getPlayer().getClassId().getId() : -1;
			_cancelledEffects.put(eff.getSkill(), new Integer[]
			{
				eff.getTimeLeft(),
				classId
			});
			_restoreTime = System.currentTimeMillis() + (Config.CANCEL_SYSTEM_RESTORE_DELAY * 1000);
		}
		
		private void restoreBuffs()
		{
			if (_cancelledEffects.isEmpty())
			{
				return;
			}
			
			Creature owner = _owner.get();
			if ((owner == null) || (owner.getPlayer() == null) || !owner.getPlayer().isOnline())
			{
				_cancelledEffects.clear();
				return;
			}
			
			// Check if player/summon is in events or oly.
			if (owner.getPlayer() != null)
			{
				if (owner.getPlayer().isInOlympiadMode() || Olympiad.isRegistered(owner.getPlayer()) || Olympiad.isRegisteredInComp(owner.getPlayer()))
				{
					_cancelledEffects.clear();
					return;
				}
			}
			
			boolean shieldBlock = owner.getEffectList().containEffectFromSkills(1358, 1360); // Block Shield, Mass Block Shield
			boolean wwBlock = owner.getEffectList().containEffectFromSkills(1359, 1361); // Block Wind Walk, Mass Block Wind Walk
			List<Integer> delayedSkills = new ArrayList<>();
			
			for (Entry<Skill, Integer[]> n3 : _cancelledEffects.entrySet())
			{
				Skill skill = n3.getKey();
				long duration = n3.getValue()[0] * 1000;
				int classId = n3.getValue()[1];
				
				if (Config.CANCEL_SYSTEM_KEEP_TICKING)
				{
					long timeDiff = _restoreTime - _startTime;
					duration -= timeDiff;
					if (duration < 1000)
					{
						continue;
					}
				}
				
				if ((classId > 0) && owner.isPlayer() && (owner.getPlayer().getClassId().getId() != classId))
				{
					continue;
				}
				
				// If owner already has the buffs from this effect, do not return the buff.
				if (owner.getEffectList().getEffectsBySkill(skill) != null)
				{
					owner.sendMessage("The canceled effect " + skill.getName() + " cannot be restored because you already possess it.");
					continue;
				}
				
				for (EffectTemplate et : skill.getEffectTemplates())
				{
					if ((shieldBlock && "pDef".equalsIgnoreCase(et._stackType2)) || (wwBlock && "SpeedUp".equalsIgnoreCase(et._stackType2)))
					{
						delayedSkills.add(skill.getId());
						break;
					}
					
					// final Env env = Env.valueOf(owner, owner, skill);
					final Env env = new Env(owner, owner, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(duration);
					
					int slotType = EffectList.getSlotType(effect);
					int buffsCount = (int) owner.getEffectList().getAllEffects().stream().map(eff -> EffectList.getSlotType(eff)).filter(subType -> subType == slotType).count();
					int limit = Integer.MAX_VALUE;
					switch (slotType)
					{
						case EffectList.BUFF_SLOT_TYPE:
							limit = owner.getBuffLimit();
							break;
						case EffectList.MUSIC_SLOT_TYPE:
							limit = Config.ALT_MUSIC_LIMIT;
							break;
						case EffectList.DEBUFF_SLOT_TYPE:
							limit = Config.ALT_DEBUFF_LIMIT;
							break;
						case EffectList.TRIGGER_SLOT_TYPE:
							limit = Config.ALT_TRIGGER_LIMIT;
							break;
					}
					
					if (limit < (buffsCount + 1)) // +1 cause of the new effect.
					{
						owner.sendMessage("The canceled effect " + skill.getName() + " cannot be restored because you exceed the limit.");
						break;
					}
					
					owner.getEffectList().addEffect(effect);
					owner.sendChanges();
					owner.updateEffectIcons();
				}
			}
			
			// Remove all Effects except the delayed effects.
			if (!delayedSkills.isEmpty())
			{
				for (Skill skill : _cancelledEffects.keySet())
				{
					if (!delayedSkills.contains(skill.getId()))
					{
						_cancelledEffects.remove(skill);
					}
				}
			}
			else
			{
				_cancelledEffects.clear();
			}
			
			_playerBuffRestore.remove(owner.getObjectId());
			// Original Message: Your cancelled buffs have been restored.
			owner.getPlayer().sendMessage(new CustomMessage("l2r.gameserver.model.CancelSystem.message2", owner.getPlayer()));
		}
	}
	
	public void stopCancelSystem()
	{
		if (_thread != null)
		{
			_thread.cancel(false);
			_thread = null;
		}
	}
	
	public void startCancelSystem()
	{
		stopCancelSystem();
		
		_playerBuffRestore = new ConcurrentHashMap<>();
		_thread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new BuffRestoreImpl(), 10000, 10000);
	}
}
