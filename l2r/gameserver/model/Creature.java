package l2r.gameserver.model;

import static l2r.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import l2r.gameserver.DebugSystem.CombatEventHook;
import l2r.gameserver.ai.DefaultAI;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.set.hash.TIntHashSet;
import l2r.commons.collections.LazyArrayList;
import l2r.commons.lang.reference.HardReference;
import l2r.commons.lang.reference.HardReferences;
import l2r.commons.listener.Listener;
import l2r.commons.permission.Permission;
import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.commons.util.concurrent.atomic.AtomicState;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CharacterAI;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.ai.PlayableAI.nextAction;
import l2r.gameserver.custom.GmEventManager;
import l2r.gameserver.dao.ChampionTemplateTable;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.geodata.GeoMove;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.model.Skill.SkillTargetType;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.Mods.NpcAndRaidMod;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.actor.permission.CharPermissionList;
import l2r.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.EventType;
import l2r.gameserver.model.entity.events.GlobalEvent;
import l2r.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2r.gameserver.model.entity.olympiad.CompType;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestEventType;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.reference.L2Reference;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.Attack;
import l2r.gameserver.network.serverpackets.AutoAttackStart;
import l2r.gameserver.network.serverpackets.AutoAttackStop;
import l2r.gameserver.network.serverpackets.ChangeMoveType;
import l2r.gameserver.network.serverpackets.CharMoveToLocation;
import l2r.gameserver.network.serverpackets.DamageTextPacket;
import l2r.gameserver.network.serverpackets.FlyToLocation;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillCancelled;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.StopMove;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.TeleportToLocation;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.TimeStamp;
import l2r.gameserver.stats.Calculator;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.stats.Formulas.AttackInfo;
import l2r.gameserver.stats.StatFunctions;
import l2r.gameserver.stats.StatTemplate;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.Func;
import l2r.gameserver.stats.triggers.TriggerInfo;
import l2r.gameserver.stats.triggers.TriggerType;
import l2r.gameserver.taskmanager.GlobalPvPZoneTaskManager;
import l2r.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2r.gameserver.taskmanager.RegenTaskManager;
import l2r.gameserver.taskmanager.actionrunner.tasks.NotifyAITask;
import l2r.gameserver.taskmanager.tasks.MagicGeoCheckTask;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.AltMagicUseTask;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.CastEndTimeTask;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.HitTask;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.MagicLaunchedTask;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.MagicUseTask;
import l2r.gameserver.templates.CharTemplate;
import l2r.gameserver.templates.ZoneTemplate;
import l2r.gameserver.templates.item.WeaponTemplate;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.PositionUtils;

public abstract class Creature extends GameObject
{
	private static final long serialVersionUID = 1L;
	private static final Logger _log = LoggerFactory.getLogger(Creature.class);
	
	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;
	public int BYPASS_DISTANCE_ADD = 150;
	private static final double[] POLE_VAMPIRIC_MOD =
	{
		1,
		0.9,
		0,
		7,
		0.2,
		0.01
	};
	
	private int _moveExceptionsThrown = 0; // For logging purposes.
	
	private Skill _castingSkill;
	
	private long _castInterruptTime;
	private long _animationEndTime;
	
	public int _scheduledCastCount;
	public int _scheduledCastInterval;
	
//	public ScheduledFuture<?> _skillTask;
//	public ScheduledFuture<?> _skillCheckTask;
//	public ScheduledFuture<?> _skillLaunchedTask;
	
	public Future<?> _skillTask;
	public Future<?> _skillLaunchedTask;
	public Future<?> _skillGeoCheckTask;
	
	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;
	
	private long _lastAttackedTime;
	private final Map<Integer, Long[]> receivedDebuffs;
	
	public final static int CLIENT_BAR_SIZE = 352; 
	
	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;
	
	protected double _currentCp = 0;
	protected double _currentHp = 1;
	protected double _currentMp = 1;
	
	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;
	private int _poleAttackCount = 0;
	
	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;
	
	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<>();
	
	protected volatile EffectList _effectList;
	
	protected volatile CharPermissionList _permissions;
	
	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
	
	private List<Stats> _blockedStats;
	
	/** Map 32 bits (0x00000000) containing all abnormal effect in progress */
	private int _abnormalEffects;
	private int _abnormalEffects2;
	private int _abnormalEffects3;
	
	protected AtomicBoolean _isDead = new AtomicBoolean();
	protected AtomicBoolean _isTeleporting = new AtomicBoolean();
	
	private Map<Integer, Integer> _skillMastery;
	
	protected boolean _isInvul;
	protected boolean _isImmortal;
	protected boolean _isRealTargeted;
	
	private boolean _fakeDeath;
	private boolean _isBlessedByNoblesse; // ÃŽ Ã¢â‚¬â„¢ÃŽ ÃŽÅ½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ Ã‚Â²ÃŽ Ã‚Â»ÃŽ ÃŽË†ÃŽ Ã‚Â²ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ Ã‚Â²ÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚Âµ ÃŽ Ã‚Â±ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¾ÃŽÂ¡Ã¢â‚¬Â¹ ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚Â»ÃŽ Ã‚Âµ ÃŽÂ¡Ã¯Â¿Â½ÃŽ ÃŽÅ’ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â€šÂ¬ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ ÃŽË†
	private boolean _isSalvation; // ÃŽ Ã¢â‚¬â„¢ÃŽ ÃŽÅ½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ Ã‚Â²ÃŽ Ã‚Â»ÃŽ ÃŽË†ÃŽ Ã‚Â²ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ Ã‚Â²ÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚Âµ ÃŽ Ã‚Â±ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¾ÃŽÂ¡Ã¢â‚¬Â¹ ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚Â»ÃŽ Ã‚Âµ ÃŽÂ¡Ã¯Â¿Â½ÃŽ ÃŽÅ’ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â€šÂ¬ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ ÃŽË† ÃŽ ÃŽË† ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽ Ã‚Â»ÃŽ Ã‚Â½ÃŽ ÃŽÅ½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ CP, MP, HP
	
	private boolean _meditated;
	private boolean _lockedTarget;
	
	private boolean _blocked;
	
	private final AtomicState _afraid = new AtomicState();
	private final AtomicState _muted = new AtomicState();
	private final AtomicState _pmuted = new AtomicState();
	private final AtomicState _amuted = new AtomicState();
	private final AtomicState _paralyzed = new AtomicState();
	private final AtomicState _rooted = new AtomicState();
	private final AtomicState _sleeping = new AtomicState();
	private final AtomicState _stunned = new AtomicState();
	private final AtomicState _immobilized = new AtomicState();
	private final AtomicState _confused = new AtomicState();
	private final AtomicState _frozen = new AtomicState();
	
	private final AtomicState _healBlocked = new AtomicState();
	private final AtomicState _damageBlocked = new AtomicState();
	private final AtomicState _buffImmunity = new AtomicState(); // ÃŽ Ã¯Â¿Â½ÃŽ ÃŽÅ’ÃŽ ÃŽÅ’ÃŽÂ¡Ã†â€™ÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽÅ  ÃŽ Ã‚Â±ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¾ÃŽ Ã‚Â°ÃŽ ÃŽÅ’
	private final AtomicState _debuffImmunity = new AtomicState(); // ÃŽ Ã¯Â¿Â½ÃŽ ÃŽÅ’ÃŽ ÃŽÅ’ÃŽÂ¡Ã†â€™ÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽÅ  ÃŽ ÃŽâ€žÃŽ Ã‚ÂµÃŽ Ã‚Â±ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¾ÃŽ Ã‚Â°ÃŽ ÃŽÅ’
	private final AtomicState _effectImmunity = new AtomicState(); // ÃŽ Ã¯Â¿Â½ÃŽ ÃŽÅ’ÃŽ ÃŽÅ’ÃŽÂ¡Ã†â€™ÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽÅ ÃŽ ÃŽÅ½ ÃŽ Ã‚Â²ÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚ÂµÃŽ ÃŽÅ’ ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¾ÃŽÂ¡Ã¢â‚¬Å¾ÃŽ Ã‚ÂµÃŽ ÃŽÅ ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ ÃŽÅ’
	
	private final AtomicState _weaponEquipBlocked = new AtomicState();
	
	private InvisibleType _invisibleType = InvisibleType.NONE;
	
	private boolean _flying;
	
	private boolean _running;
	
	public boolean isMoving;
	public boolean isFollow;
	private final Lock moveLock = new ReentrantLock();
	private Future<?> _moveTask;
	private MoveNextTask _moveTaskRunnable;
	private List<Location> _moveList;
	private Location _destination;
	
	/**
	* when moveToLocation is used to store the geo coordinates in which we are moving in order to avoid re-building the same path
	* when followToCharacter is used to store the world coordinates in which the target was last located to track the need to rebuild the path
	*/
	private final Location _movingDestTempPos = new Location();
	private int _offset;
	
	private boolean _forestalling;
	
	private volatile HardReference<? extends GameObject> _target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _castingTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _followTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();
	
	private final List<List<Location>> _targetRecorder = new ArrayList<>();
	private long _followTimestamp, _startMoveTime;
	private double _previousSpeed = 0;
	
	private int _heading;
	
	private final Calculator[] _calculators;
	
	protected CharTemplate _template;
	protected CharTemplate _baseTemplate;
	
	protected volatile CharacterAI _ai;
	private boolean _perfectBlock = false;

	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;
	private boolean _isRegenerating;
	private final Lock regenLock = new ReentrantLock();
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;
	
	private final List<Zone> _zones = new LazyArrayList<>();
	private Zone _dynamicZone; // Used for custom purposes where this Creature has zone around himself.
	
	private final ReadWriteLock _zonesLock = new ReentrantReadWriteLock();
	private final Lock _zonesRead = _zonesLock.readLock();
	private final Lock _zonesWrite = _zonesLock.writeLock();
	
	protected volatile CharListenerList listeners;
	
	private List<Player> _statusListeners;
	private final Lock _statusListenersLock = new ReentrantLock();
	
	protected Long _storedId;
	
	private long _overrideConditions = 0L;
	
	public final Long getStoredId()
	{
		return _storedId;
	}
	
	protected HardReference<? extends Creature> reference;
	
	public Creature(int objectId, CharTemplate template)
	{
		super(objectId);
		
		receivedDebuffs = new HashMap<>();
		
		_template = template;
		_baseTemplate = template;
		
		_calculators = new Calculator[Stats.NUM_STATS];
		
		StatFunctions.addPredefinedFuncs(this);
		
		reference = new L2Reference<>(this);
		
		_storedId = GameObjectsStorage.put(this);
	}
	
	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}
	
	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}
	
	public final void abortAttack(boolean force, boolean message)
	{
		if (isAttackingNow())
		{
			_attackEndTime = 0;
			if (force)
			{
				_isAttackAborted = true;
			}
			
			getAI().setIntention(AI_INTENTION_ACTIVE);
			
			if (isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage2(SystemMsg.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}
	
	public final void abortCast(boolean force, boolean message)
	{
		if (isCastingNow() && (force || canAbortCast()))
		{
			//final Skill castingSkill = _castingSkill;
			final Skill castingSkill = _castingSkill;
			final Future<?> skillTask = _skillTask;
			final Future<?> skillLaunchedTask = _skillLaunchedTask;
			final Future<?> skillGeoCheckTask = _skillGeoCheckTask;
			
			finishFly(); 
			clearCastVars();
			
			if (skillTask != null)
				skillTask.cancel(false); // cancels the skill hit scheduled task
				
			if (skillLaunchedTask != null)
				skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task
				
			if (skillGeoCheckTask != null)
			{
				skillGeoCheckTask.cancel(false); // cancels the skill GeoCheck scheduled task
			}
			
			
//			try
//			{
//				if (_skillTask != null)
//				{
//					_skillTask.cancel(false); // cancels the skill hit scheduled task
//				}
//			}
//			catch (NullPointerException npe)
//			{
//			}
//			
//			try
//			{
//				if (skillLaunchedTask != null)
//				{
//					skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task
//				}
//			}
//			catch (NullPointerException npe)
//			{
//			}
//			
//			try
//			{
//				if (_skillLaunchedTask != null)
//				{
//					_skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task
//				}
//			}
//			catch (NullPointerException npe)
//			{
//			}
//			
//			clearCastVars();
			
			if (castingSkill != null)
			{
				if (castingSkill.isUsingWhileCasting())
				{
					//Creature target = getAI().getAttackTarget();
					Creature target = getAI().getCastTarget();
					if (target != null)
					{
						target.getEffectList().stopEffect(castingSkill.getId());
					}
				}
				
				removeSkillMastery(castingSkill.getId());
			}
			
			broadcastPacket(new MagicSkillCancelled(getObjectId())); // broadcast packet to stop animations client-side
			
			getAI().setIntention(AI_INTENTION_ACTIVE);
			
			if (isPlayer() && message)
			{
				sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
			}
		}
	}
	
	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}
	
	public boolean absorbAndReflect(Creature target, Skill skill, double damage)
	{
		if (target.isDead())
			return false;
		
		boolean bow = getActiveWeaponItem() != null && (getActiveWeaponItem().getItemType() == WeaponType.BOW || getActiveWeaponItem().getItemType() == WeaponType.CROSSBOW);
		
		double value = 0;
		
		if (skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if (skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if (skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, this, null);
		
		// The purpose of all damage recorded
		if (value > 0 && Rnd.chance(value))
		{
			reduceCurrentHp(damage, target, null, true, true, false, false, false, false, true);
			return true;
		}
		
		if (skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0, this, skill);
		else if (skill != null)
		{
			if (skill.getCastRange() >= 0 && skill.getCastRange() <= 40)
				value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0, this, skill);
		}
		else if (!bow)
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, this, null);
		
		if (value > 0)
		{
			// Purpose able to reflect some of the damage
			if (getCurrentHp() + getCurrentCp() > value * 0.01 * damage)
			{
				reduceCurrentHp(value * 0.01 * damage, target, null, true, true, false, false, false, false, true);
			}
			else
			{
				// if (!(this instanceof Player && ((Player)this).isInOlympiadMode()) && !(this instanceof Player && ((Player)this).isInDuel()))
				if (!isPlayer() || !(getPlayer().isInOlympiadMode() || getPlayer().isInDuel()))
				{
					doDie(this);
				}
			}
		}
		
		if (skill != null || bow)
			return false;
		
		// вампирик
		damage = (int) (damage - target.getCurrentCp());
		
		if (damage <= 0)
			return false;
		
		final double poleMod = _poleAttackCount < POLE_VAMPIRIC_MOD.length ? POLE_VAMPIRIC_MOD[_poleAttackCount] : 0;
		double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, target, null);
		double limit;
		if (absorb > 0 && !target.isDamageBlocked())
		{
			limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.;
			if (getCurrentHp() < limit)
				setCurrentHp(Math.min(_currentHp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit), false);
		}
		
		absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0, target, null);
		if (absorb > 0 && !target.isDamageBlocked())
		{
			limit = calcStat(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.;
			if (getCurrentMp() < limit)
				setCurrentMp(Math.min(_currentMp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100., limit));
		}
		
		return false;
	}
	
	public double absorbToEffector(Creature attacker, double damage)
	{
		if (damage == 0)
		{
			return 0;
		}
		
		double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.0D);
		if (transferToEffectorDam > 0)
		{
			Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
			if (effect == null)
			{
				return damage;
			}
			
			Creature effector = effect.getEffector();
			// on a dead char, not an online player - we do not give an absorption, and not on ourselves
			if ((effector == this) || effector.isDead() || !isInRange(effector, 1000L))
			{
				return damage;
			}
			
			Player thisPlayer = getPlayer();
			Player effectorPlayer = effector.getPlayer();
			if ((thisPlayer != null) && (effectorPlayer != null))
			{
				if ((thisPlayer != effectorPlayer) && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || (thisPlayer.getParty() != effectorPlayer.getParty())))
				{
					return damage;
				}
			}
			else
			{
				return damage;
			}
			
			double transferDamage = (damage * transferToEffectorDam) * 0.01D;
			damage -= transferDamage;
			
			effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
		}
		return damage;
	}
	
	public double absorbToMp(Creature attacker, double damage)
	{
		if (damage == 0)
		{
			return 0;
		}
		
		double transferToMpDamPercent = calcStat(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.);
		if (transferToMpDamPercent > 0)
		{
			double transferDamage = (damage * transferToMpDamPercent) * .01;
			
			double currentMp = getCurrentMp();
			if (currentMp > transferDamage)
			{
				setCurrentMp(getCurrentMp() - transferDamage);
				return 0;
			}
			if (currentMp > 0)
			{
				damage -= currentMp;
				setCurrentMp(0);
				sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
			}
			getEffectList().stopEffects(EffectType.AbsorbDamageToMp);
			
			return damage;
		}
		return damage;
	}
	
	public double absorbToSummon(Creature attacker, double damage)
	{
		double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.);
		if (transferToSummonDam > 0)
		{
			Summon summon = getPet();
			double transferDamage = (damage * transferToSummonDam) * .01;
			if ((summon == null) || summon.isDead() || (summon.getCurrentHp() < transferDamage))
			{
				getEffectList().stopEffects(EffectType.AbsorbDamageToSummon);
			}
			else if (summon.isSummon() && summon.isInRangeZ(this, 1200))
			{
				damage -= transferDamage;
				
				summon.reduceCurrentHp(transferDamage, summon, null, false, false, false, false, true, false, true);
			}
		}
		return damage;
	}
	
	public void addBlockStats(List<Stats> stats)
	{
		if (_blockedStats == null)
		{
			_blockedStats = new ArrayList<>();
		}
		_blockedStats.addAll(stats);
	}
	
	public Skill addSkill(Skill newSkill)
	{
		if (newSkill == null)
		{
			return null;
		}
		
		Skill oldSkill = _skills.get(newSkill.getId());
		
		if ((oldSkill != null) && (oldSkill.getLevel() == newSkill.getLevel()))
		{
			return newSkill;
		}
		
		// Replace oldSkill by newSkill or Add the newSkill
		_skills.put(newSkill.getId(), newSkill);
		
		// FIX for /useskill re-use exploit
		if (oldSkill != null)
		{
			TimeStamp sts = _skillReuses.get(oldSkill.hashCode());
			if (sts != null && sts.hasNotPassed())
			{
				_skillReuses.put(newSkill.hashCode(), sts);
			}
		}
		
		if (oldSkill != null)
		{
			removeStatsOwner(oldSkill);
			removeTriggers(oldSkill);
		}
		
		addTriggers(newSkill);
		
		// Add Func objects of newSkill to the calculator set of the L2Character
		addStatFuncs(newSkill.getStatFuncs());
		
		return oldSkill;
	}
	
	public Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final void addStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if (_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator(f.stat, this);
			}
			_calculators[stat].addFunc(f);
		}
	}
	
	public final void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
		{
			addStatFunc(f);
		}
	}
	
	public final void removeStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if (_calculators[stat] != null)
			{
				_calculators[stat].removeFunc(f);
			}
		}
	}
	
	public final void removeStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
		{
			removeStatFunc(f);
		}
	}
	
	public final void removeStatsOwner(Object owner)
	{
		synchronized (_calculators)
		{
			for (Calculator _calculator : _calculators)
			{
				if (_calculator != null)
				{
					_calculator.removeOwner(owner);
				}
			}
		}
	}
	
//	public void altOnMagicUseTimer(Creature aimingTarget, Skill skill)
//	{
//		if (isAlikeDead())
//		{
//			return;
//		}
//		int magicId = skill.getDisplayId();
//		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
//		List<Creature> targets = skill.getTargets(this, aimingTarget, true);
//		broadcastPacket(new MagicSkillLaunched(getObjectId(), magicId, level, targets));
//		double mpConsume2 = skill.getMpConsume2();
//		mpConsume2 = calcStat(skill.isMagic() ? Stats.MP_MAGIC_SKILL_CONSUME : Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
//		
//		if (mpConsume2 > 0)
//		{
//			if (_currentMp < mpConsume2)
//			{
//				sendPacket(SystemMsg.NOT_ENOUGH_MP);
//				return;
//			}
//			
//			reduceCurrentMp(mpConsume2, null);
//		}
//		callSkill(skill, targets, false);
//	}
	
	public void altOnMagicUseTimer(Creature aimingTarget, Skill skill)
	{
		if (isAlikeDead())
			return;
		int magicId = skill.getDisplayId();
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		List<Creature> targets = skill.getTargets(this, aimingTarget, true);
		broadcastPacket(new MagicSkillLaunched(getObjectId(), magicId, level, Collections.unmodifiableList(targets)));
		double mpConsume2 = skill.getMpConsume2();
		if (mpConsume2 > 0)
		{
			if (_currentMp < mpConsume2)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			if (skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
		}
		callSkill(skill, targets, false);
	}
	
	public void altUseSkill(Skill skill, Creature target)
	{
		if (skill == null)
		{
			return;
		}
		
		int magicId = skill.getId();
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			if (isUnActiveSkill(magicId))
			{
				return;
			}
			if (isSkillDisabled(skill))
			{
				sendReuseMessage(skill);
				return;
			}
		}
		
		if (target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if (target == null)
			{
				if (canOverrideCond(PcCondOverride.SKILL_TARGET_CONDITIONS))
				{
					target = this;
				}
				else
				{
					return;
				}
			}
		}
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			// Returns false if character can continue cast.
			if (getListeners().onMagicUse(skill, target, true))
			{
				return;
			}
			
			int itemConsume[] = skill.getItemConsume();
			
			// Phantom players dont use items :)
			if (!getAI().isPhantomPlayerAI() && (itemConsume[0] > 0))
			{
				for (int i = 0; i < itemConsume.length; i++)
				{
					if (!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
					{
						sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						return;
					}
				}
			}
			
			if (skill.getReferenceItemId() > 0)
			{
				if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				{
					return;
				}
			}
			
			if (skill.getSoulsConsume() > getConsumedSouls())
			{
				sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SOULS);
				return;
			}
			
			if (skill.getEnergyConsume() > getAgathionEnergy())
			{
				sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
				return;
			}
			
			if (skill.getSoulsConsume() > 0)
			{
				setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
			}
			if (skill.getEnergyConsume() > 0)
			{
				setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());
			}
		}
		
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skill, this);
		long reuseDelay = canOverrideCond(PcCondOverride.SKILL_REUSE_CONDITIONS) ? 0 : Formulas.calcSkillReuseDelay(this, skill);
		if (!skill.isToggle())
		{
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(this), reuseDelay));
		}
		
		if (!skill.isHideUseMessage())
		{
			if (skill.getSkillType() == SkillType.PET_SUMMON)
			{
				sendPacket(new SystemMessage2(SystemMsg.SUMMONING_YOUR_PET));
			}
			else if (!skill.isHandler())
			{
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
			}
			else
			{
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));
			}
		}
		
		if (!skill.isHandler())
		{
			disableSkill(skill, reuseDelay);
		}
		
		ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, target, skill), skill.getHitTime(this));
	}
	
	public void sendReuseMessage(Skill skill)
	{
	}
	
	public void broadcastPacket(L2GameServerPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}
	
	public void broadcastPacket(List<L2GameServerPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}
	
	public void broadcastPacketToOthers(L2GameServerPacket... packets)
	{
		if (!isVisible() || (packets.length == 0))
		{
			return;
		}
		
		List<Player> players = World.getAroundPlayers(this);
		Player target;
		for (Player player : players)
		{
			target = player;
			target.sendPacket(packets);
		}
	}
	
	public void broadcastPacketToOthers(List<L2GameServerPacket> packets)
	{
		if (!isVisible() || packets.isEmpty())
		{
			return;
		}
		
		List<Player> players = World.getAroundPlayers(this);
		Player target;
		for (Player player : players)
		{
			target = player;
			target.sendPacket(packets);
		}
	}
	
	public void broadcastToStatusListeners(L2GameServerPacket... packets)
	{
		if (!isVisible() || (packets.length == 0))
		{
			return;
		}
		
		_statusListenersLock.lock();
		try
		{
			if ((_statusListeners == null) || _statusListeners.isEmpty())
			{
				return;
			}
			
			Player player;
			for (Player _statusListener : _statusListeners)
			{
				player = _statusListener;
				player.sendPacket(packets);
			}
		}
		finally
		{
			_statusListenersLock.unlock();
		}
	}
	
	public void addStatusListener(Player cha)
	{
		if (cha == this)
		{
			return;
		}
		
		_statusListenersLock.lock();
		try
		{
			if (_statusListeners == null)
			{
				_statusListeners = new LazyArrayList<>();
			}
			if (!_statusListeners.contains(cha))
			{
				_statusListeners.add(cha);
			}
		}
		finally
		{
			_statusListenersLock.unlock();
		}
	}
	
	public void removeStatusListener(Creature cha)
	{
		_statusListenersLock.lock();
		try
		{
			if (_statusListeners == null)
			{
				return;
			}
			_statusListeners.remove(cha);
		}
		finally
		{
			_statusListenersLock.unlock();
		}
	}
	
	public void clearStatusListeners()
	{
		_statusListenersLock.lock();
		try
		{
			if (_statusListeners == null)
			{
				return;
			}
			_statusListeners.clear();
		}
		finally
		{
			_statusListenersLock.unlock();
		}
	}
	
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		for (int field : fields)
		{
			switch (field)
			{
				case StatusUpdate.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdate.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdate.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdate.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, getPvpFlag());
					break;
			}
		}
		return su;
	}
	
	public void broadcastStatusUpdate()
	{
		if (!needStatusUpdate())
		{
			return;
		}
		
		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.MAX_MP, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP);
		broadcastToStatusListeners(su);
		getListeners().onStatucUpdateBroadcasted();
	}
	
	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}
	
	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}
	
	public final double calcStat(Stats stat, double init, Creature target, Skill skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if (c == null)
		{
			return init;
		}
		Env env = new Env();
		env.character = this;
		
		if ((skill != null) && (skill.getId() == 1557))
		{
			env.target = getPlayer().getPet();
		}
		else
		{
			env.target = target;
		}
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}
	
	public final double calcStat(Stats stat, Creature target, Skill skill)
	{
		if ((skill != null) && (skill.getId() == 1557))
		{
			target = getPlayer().getPet();
		}
		Env env = new Env(this, target, skill);
		if (stat == null)
		{
			_log.warn("FIX ME FAST(!) My name IS " + getName() + " and I'm a player?(" + isPlayer() + ") my target is " + target.getName() + " skill:" + skill.getName() + "");
			return 0;
		}
		env.value = stat.getInit();
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if (c != null)
		{
			c.calc(env);
		}
		return env.value;
	}
	
	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}
	
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
	{
		// Skill skill = env.skill;
		// int chargedShot = skill.isSSPossible() ? skill.isMagic() ? getChargedSpiritShot() : getChargedSoulShot() ? ItemInstance.CHARGED_SOULSHOT : ItemInstance.CHARGED_NONE : ItemInstance.CHARGED_NONE;
		
		try
		{
			if (useActionSkills && !skill.isUsingWhileCasting() && (_triggers != null))
			{
				if (skill.isOffensive())
				{
					if (skill.isMagic())
					{
						useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0);
					}
					else
					{
						useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0);
					}
				}
				else if (skill.isMagic())
				{
					final boolean targetSelf = skill.isAoE() || skill.isNotTargetAoE() || (skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF);
					useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0);
				}
			}
			
			final Player player = getPlayer();
			Creature target;
			final Iterator<Creature> itr = targets.iterator();
			while (itr.hasNext())
			{
				target = itr.next();
				
				if (target == null)
				{
					continue;
				}
				
				// Filter invulnerable targets.
				if (skill.isOffensive() && target.isInvul())
				{
					Player pcTarget = target.getPlayer();
					if ((!skill.isIgnoreInvul() || ((pcTarget != null) && pcTarget.isGM())) && !target.isArtefact())
					{
						itr.remove();
						continue;
					}
				}
				// Filter by effect that ignores the skills.
				Effect ie = target.getEffectList().getEffectByType(EffectType.IgnoreSkill);
				if (ie != null)
				{
					if (ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId()))
					{
						itr.remove();
						continue;
					}
				}
				
				target.getListeners().onMagicHit(skill, this);
				
				if (player != null)
				{
					if (target.isNpc())
					{
						NpcInstance npc = (NpcInstance) target;
						List<QuestState> ql = player.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
						if (ql != null)
						{
							for (QuestState qs : ql)
							{
								qs.getQuest().notifySkillUse(npc, skill, qs);
							}
						}
					}
					
					// Check for pvp flag from traps and decoys.
					if (isNpc() && player.checkPvP(target, skill))
					{
						player.startPvPFlag(target);
					}
				}
				
				if (skill.getNegateSkill() > 0)
				{
					for (Effect e : target.getEffectList().getAllEffects())
					{
						Skill efs = e.getSkill();
						if ((efs.getId() == skill.getNegateSkill()) && e.isCancelable() && ((skill.getNegatePower() <= 0) || (efs.getPower() <= skill.getNegatePower())))
						{
							e.exit();
						}
					}
				}
				
				if ((skill.getCancelTarget() > 0) && Rnd.chance(skill.getCancelTarget()))
				{
					if (((target.getCastingSkill() == null) || !((target.getCastingSkill().getSkillType() == SkillType.TAKECASTLE) || (target.getCastingSkill().getSkillType() == SkillType.TAKEFORTRESS) || (target.getCastingSkill().getSkillType() == SkillType.TAKEFLAG))) && !target.isRaid())
					{
						target.abortAttack(true, true);
						target.abortCast(true, true);
						target.setTarget(null);
					}
				}
			}
			
			if (skill.isOffensive())
			{
				startAttackStanceTask();
			}
			
			// Apply self-effect on caster
			// Special condition for attacking aura skills (Vengeance 368):
			// else if not even one target is available, no self-effect.
			if (!(skill.isNotTargetAoE() && skill.isOffensive() && (targets.size() == 0)))
			{
				skill.getEffects(this, this, false, true);
			}
			
			// skill.useSkill(env, targets);
			skill.useSkill(this, targets);
			
		}
		catch (final Exception e)
		{
			_log.error("Error while Calling Skill!", e);
		}
	}
	
	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
		if (_triggers == null)
		{
			return;
		}
		
		Set<TriggerInfo> SkillsOnSkillAttack = _triggers.get(type);
		if (SkillsOnSkillAttack != null)
		{
			for (TriggerInfo t : SkillsOnSkillAttack)
			{
				if (t.getSkill() != ex)
				{
					useTriggerSkill(target == null ? getTarget() : target, null, t, owner, damage);
				}
			}
		}
	}
	
	public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		Skill skill = trigger.getSkill();
		if ((skill.getReuseDelay(this) > 0) && isSkillDisabled(skill))
		{
			return;
		}
		
		Creature aimTarget = skill.getAimingTarget(this, target);
		Creature realTarget = (target != null) && target.isCreature() ? (Creature) target : null;
		if (/*Rnd.chance(trigger.getChance())*/Rnd.get(140) < trigger.getChance() && trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, false, true, true))
		{
			if (targets == null)
			{
				targets = skill.getTargets(this, aimTarget, false);
			}
			
			int displayId = 0, displayLevel = 0;
			
			if (skill.hasEffects())
			{
				displayId = skill.getEffectTemplates()[0]._displayId;
				displayLevel = skill.getEffectTemplates()[0]._displayLevel;
			}
			
			if (displayId == 0)
			{
				displayId = skill.getDisplayId();
			}
			if (displayLevel == 0)
			{
				displayLevel = skill.getDisplayLevel();
			}
			
			if (trigger.getType() != TriggerType.SUPPORT_MAGICAL_SKILL_USE)
			{
				for (Creature cha : targets)
				{
					broadcastPacket(new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0));
				}
			}
			
			Formulas.calcSkillMastery(skill, this);
			callSkill(skill, targets, false);
			disableSkill(skill, skill.getReuseDelay(this));
		}
	}
	
	public boolean checkBlockedStat(Stats stat)
	{
		return (_blockedStats != null) && _blockedStats.contains(stat);
	}

	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if (!skill.isReflectable() || isInvul() || attacker.isInvul() || !skill.isOffensive())
			return false;

		if (skill.isMagic() && skill.getSkillType() != SkillType.MDAM)
			return false;

		if (!Rnd.chance(calcStat(skill.isMagic() ?
				Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0, attacker, skill)))
			return false;

		sendPacket(new SystemMessage2(SystemMsg.YOU_COUNTERED_C1S_ATTACK).addName(attacker));
		attacker.sendPacket(new SystemMessage2(SystemMsg.C1_DODGES_THE_ATTACK).addName(this));

		// LOG REFLECT EVENT
		try
		{
			CombatEventHook.onDamage(
					this,           // attacker (reflection source)
					attacker,       // target (damage receiver)
					(skill != null ? skill.getId() : 0),
					0.0,            // damage (reflection has no dmg)
					false,
					false
			);

		}
		catch (Exception e) {}

		return true;
	}

	public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
	{
		if (isDead())
			return;

		if (isDamageBlocked() || (attacker != null && attacker.isDamageBlocked()))
			return;

		if (skill == null || skill.hasEffects() || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
			return;

		if (!Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0.0D, attacker, skill)))
			return;

		double damage = (1189 * getPAtk(attacker)) / Math.max(attacker.getPDef(this), 1);

		attacker.sendPacket(new SystemMessage2(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));

		if (blow)
			damage *= 2;

		sendPacket(new SystemMessage2(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2)
				.addName(this).addName(attacker).addInteger((long) damage));

		// ----------------------------------------------------
		// DEBUG HOOK (SAFE)
		// ----------------------------------------------------
		try
		{
			CombatEventHook.onDamage(
					this,                               // attacker (counterattack source)
					attacker,                           // target
					(skill != null ? skill.getId() : 0),
					damage,                             // <<< REQUIRED
					false,                              // crit
					false                               // miss
			);
		}
		catch (Exception e)
		{
			System.out.println("[CombatEventHook] CounterAttack Error: " + e.getMessage());
		}
		// ----------------------------------------------------

		attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
	}


	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(Skill skill, long delay)
	{
		if (skill == null)
		{
			return;
		}
		
		_skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill Skill id * @param int Skill level
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(int skill, int skillLvL, long delay)
	{
		_skillReuses.put(skill, new TimeStamp(skill, skillLvL, delay));
	}
	
	public abstract boolean isAutoAttackable(Creature attacker);

	public void doAttack(Creature target)
	{
		if (target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isAlikeDead() || !isInRange(target, 2000) || isPlayer() && getPlayer().isInMountTransform())
			return;

		if (target.isInvisible() && getAI() instanceof DefaultAI)
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
			return;
		}

		getListeners().onAttack(target);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = Math.max(calculateAttackDelay(), 333);
		int ssGrade = 0;

		WeaponTemplate weaponItem = getActiveWeaponItem();
		if (weaponItem != null)
		{
			if (isPlayer() && (weaponItem.getAttackReuseDelay() > 0))
			{
				int reuse = (int) ((weaponItem.getAttackReuseDelay() * getReuseModifier(target) * 666 * calcStat(Stats.ATK_BASE, 0, target, null)) / 293. / getPAtkSpd());
				if (reuse > 0)
				{
					sendPacket(new SetupGauge(this, SetupGauge.RED, reuse));
					_attackReuseEndTime = (reuse + System.currentTimeMillis()) - 75;
					if (reuse > sAtk)
					{
						ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), reuse);
					}
				}
			}

			ssGrade = weaponItem.getCrystalType().externalOrdinal;
		}

		// DS: adjusted by 1/100 of a second as AI task is called with a small error
		// Especially on slower machines and disconnection occurs auto attacks on isAttackingNow () == true
		_attackEndTime = (sAtk + System.currentTimeMillis()) - 10;
		_isAttackAborted = false;

		Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);

		setHeading(PositionUtils.calculateHeadingFrom(this, target));

		// Select the type of attack to
		if (weaponItem == null)
		{
			doAttackHitSimple(attack, target, 1., !isPlayer(), sAtk, true);
		}
		else
		{
			switch (weaponItem.getItemType())
			{
				case BOW:
				case CROSSBOW:
					doAttackHitByBow(attack, target, sAtk);
					break;
				case POLE:
					doAttackHitByPole(attack, target, sAtk);
					break;
				case DUAL:
				case DUALFIST:
				case DUALDAGGER:
					doAttackHitByDual(attack, target, sAtk);
					break;
				default:
					doAttackHitSimple(attack, target, 1., true, sAtk, true);
			}
		}

		if (attack.hasHits())
			broadcastPacket(attack);
	}
	
	public int calculateTimeBetweenAttacks(Creature target, WeaponTemplate weapon)
	{
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					return (1500 * 345) / getPAtkSpd();
				case CROSSBOW:
					return (1200 * 345) / getPAtkSpd();
			default:
				break;
			}
		}
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}
	
	public int calculateReuseTime(Creature target, WeaponTemplate weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		
		int reuse = weapon.getAttackReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}
		
		reuse *= getReuseModifier(target);
		double atkSpd = getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
			case CROSSBOW:
				return (int) ((reuse * 345) / atkSpd);
			default:
				return (int) ((reuse * 312) / atkSpd);
		}
	}
	
	private void doAttackHitSimple(Attack attack, Creature target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		if (!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) (info.damage * multiplier);
			shld1 = info.shld;
			crit1 = info.crit;
		}
		
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}
	
	private void doAttackHitByBow(Attack attack, Creature target, int sAtk)
	{
		WeaponTemplate activeWeapon = getActiveWeaponItem();
		if (activeWeapon == null)
		{
			return;
		}
		
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		reduceArrowCount();
		
		if (!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
			
			if (isPlayable() && target.isPlayable())
			{
				damage1 *= Config.ALT_BOW_PVP_DAMAGE_MODIFIER;
			}
			else
			{
				damage1 *= Config.ALT_BOW_PVE_DAMAGE_MODIFIER;
			}
			
			int range = activeWeapon.getAttackRange();
			damage1 *= ((Math.min(range, getDistance(target)) / range) * .4) + 0.8;
		}
		
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}
	
	private void doAttackHitByDual(Attack attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		if (!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}
		
		if (!miss2)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage2 = (int) info.damage;
			shld2 = info.shld;
			crit2 = info.crit;
		}
		
		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false), sAtk / 2);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}
	
	private void doAttackHitByPole(Attack attack, Creature target, int sAtk)
	{
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 90.0D, target, null);
		int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), target, null);
		
		// Use as Math.round ordinary casting cuts to smaller
		// Double d = 2.95. int i = (int) d, which will i = 2
		// If 1% or 1 angle distance does not play a huge role, for
		// Number of targets is critical
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0.0D, target, null));
		
		if (isBoss())
		{
			attackcountmax += 27;
		}
		else if (isRaid())
		{
			attackcountmax += 12;
		}
		else if (isMonster() && (getLevel() > 0))
		{
			attackcountmax += getLevel() / 7.5D;
		}
		
		double mult = 1.;
		_poleAttackCount = 1;
		
		if (!isInZonePeace())
		{
			for (Creature t : getAroundCharacters(range, 200))
			{
				if (_poleAttackCount <= attackcountmax)
				{
					if ((t == target) || t.isDead() || !PositionUtils.isFacing(this, t, angle))
					{
						continue;
					}
					
					if (t.isAutoAttackable(this))
					{
						doAttackHitSimple(attack, t, mult, false, sAtk, false);
						mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
						_poleAttackCount++;
					}
				}
				else
				{
					break;
				}
			}
		}
		
		_poleAttackCount = 0;
		doAttackHitSimple(attack, target, 1., true, sAtk, true);
	}
	
	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}
	
	public boolean checkDoCastConditions(Skill skill, Creature target, boolean sendMessage)
	{
		if (skill == null)
		{
			return false;
		}
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			for (GlobalEvent e : getEvents())
			{
				if (!e.canUseSkill(this, target, skill))
				{
					if (sendMessage)
					{
						sendMessage("The skill you are trying to cast is not allowed in your current state.");
					}
					
					return false;
				}
			}
			
			if (!getPermissions().canUseSkill(target, skill, true))
			{
				return false;
			}
			
			int itemConsume[] = skill.getItemConsume();
			
			// Added so that phantoms dont require items.
			if (!getAI().isPhantomPlayerAI() && (itemConsume[0] > 0))
			{
				for (int i = 0; i < itemConsume.length; i++)
				{
					if (!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
					{
						if (sendMessage)
						{
							sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						}
						
						return false;
					}
				}
			}
			
			if (skill.getReferenceItemId() > 0)
			{
				if (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void doCast(Skill skill, Creature target, boolean forceUse)
	{
		if ((skill == null) || !checkDoCastConditions(skill, target, true))
		{
			return;
		}
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && getListeners().onMagicUse(skill, target, false))
		{
			return;
		}
		if (target == null)
			target = skill.getAimingTarget(this, getTarget());
		if (target == null)
			return;
		
		getListeners().onMagicUse(skill, target, false);
		
		if (this != target)
			setHeading(PositionUtils.calculateHeadingFrom(this, target));

		int magicId = skill.getId();
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		
		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime(this) : Formulas.calcMAtkSpd(this, skill, skill.getHitTime(this));
		int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;
		
		int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime(this));
		if (skillTime < minCastTime)
		{
			skillTime = minCastTime;
			skillInterruptTime = 0;
		}
		
		// fix wall hit if above 60% skill cast
		long skillTime60 = (long) (skillTime * (60.0 / 100.0));
		
		_animationEndTime40 = System.currentTimeMillis() + skillTime60;
		
		_animationEndTime = System.currentTimeMillis() + skillTime;
		
		if (skill.isMagic() && !skill.isSkillTimePermanent() && (getChargedSpiritShot() > 0))
		{
			skillTime = (int) (0.70 * skillTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
		}
		
		Formulas.calcSkillMastery(skill, this); // Calculate skill mastery for current cast
		long reuseDelay = Math.max(0, canOverrideCond(PcCondOverride.SKILL_REUSE_CONDITIONS) ? 0 : Formulas.calcSkillReuseDelay(this, skill));
		
		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));
		
		if (!skill.isHandler())
		{
			disableSkill(skill, reuseDelay);
		}
		
		if (isPlayer())
		{
			if (skill.getSkillType() == SkillType.PET_SUMMON)
			{
				sendPacket(SystemMsg.SUMMONING_YOUR_PET);
			}
			else if (!skill.isHandler())
			{
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
			}
			else
			{
				sendPacket(new SystemMessage2(SystemMsg.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));
			}
		}
		
		if (skill.getTargetType() == SkillTargetType.TARGET_HOLY)
		{
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);
		}

		
		if (skill.beforeUseSkill(this, target, forceUse))
		{
			return;
		}
		
		switch (skill.getFlyType())
		{
			case DUMMY:
			case CHARGE:
				Location flyLoc = getFlyLocation(target, skill);
				if (flyLoc != null)
				{
					_flyLoc = flyLoc;
				}
				else
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
				break;
			default:
				_flyLoc = null;
				break;
		}
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			double mpConsume1 = skill.getMpConsume1();
			if (skill.isUsingWhileCasting())
			{
				mpConsume1 += calcStat(skill.isMagic() ? Stats.MP_MAGIC_SKILL_CONSUME : Stats.MP_PHYSICAL_SKILL_CONSUME, skill.getMpConsume2(), target, skill);
			}
			
			if (mpConsume1 > 0)
			{
				if (_currentMp < mpConsume1)
				{
					sendPacket(SystemMsg.NOT_ENOUGH_MP);
					onCastEndTime();
					return;
				}
				reduceCurrentMp(mpConsume1, null);
			}
		}
		
		if (_flyLoc != null)
		{
			broadcastPacket(new FlyToLocation(this, _flyLoc, skill.getFlyType()));
		}
		
		_castingSkill = skill;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime;
		setCastingTarget(target);
		
		if (skill.isUsingWhileCasting())
		{
			callSkill(skill, skill.getTargets(this, target, forceUse), true);
		}
		
		if (isPlayer() && (skillTime > 200))
		{
			sendPacket(new SetupGauge(this, SetupGauge.BLUE, skillTime));
		}
		
		_scheduledCastCount = skill.getCastCount();
		_scheduledCastInterval = skill.getCastCount() > 0 ? skillTime / _scheduledCastCount : skillTime;
		
		// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
		_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), skillInterruptTime);
		_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), skill.getCastCount() > 0 ? skillTime / skill.getCastCount() : skillTime);
		
		_skillGeoCheckTask = null;
		if ((skill.getCastRange() < 32767) && (skill.getSkillType() != SkillType.TAKECASTLE) && (skill.getSkillType() != SkillType.TAKEFORTRESS) && (_scheduledCastInterval > 600))
		{
			_skillGeoCheckTask = ThreadPoolManager.getInstance().schedule(new MagicGeoCheckTask(this), (long) (_scheduledCastInterval * 0.5));
		}
	}
	
	private Location _flyLoc;
	
	private Location getFlyLocation(GameObject target, Skill skill)
	{
		if ((target != null) && (target != this))
		{
			Location loc;
			
			double radian = Location.convertHeadingToRadian(target.getHeading());
			if (skill.isFlyToBack())
			{
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40), target.getY() - (int) (Math.cos(radian) * 40), target.getZ());
			}
			else
			{
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 40), target.getY() + (int) (Math.cos(radian) * 40), target.getZ());
			}
			
			if (isFlying())
			{
				if (isPlayer() && ((Player) this).isInFlyingTransform() && ((loc.z <= 0) || (loc.z >= 6000)))
				{
					return null;
				}
				if (GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null)
				{
					return null;
				}
			}
			else
			{
				loc.correctGeoZ();
				
				if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc();
					
					if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
					{
						return null;
					}
				}
			}
			
			return loc;
		}
		
		double radian = Location.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * skill.getFlyRadius());
		
		if (isFlying())
		{
			return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getGeoIndex());
		}
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}
	
	public final void doDie(Creature killer)
	{
		// Synerge - If the oly is finishing, the player should not die, this is after the match ended. They should never die if in olympiad
		if (isPlayer() && (getPlayer().isPendingOlyEnd() || (getPlayer().isInOlympiadMode() && (getPlayer().getOlympiadGame().getType() != CompType.TEAM))))
		{
			return;
		}
		
		// killing is only possible one time
		if (!_isDead.compareAndSet(false, true))
		{
			return;
		}
		
		onDeath(killer);
	}
	
	protected void onDeath(Creature killer)
	{
		if (killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if (killerPlayer != null)
			{
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
			}
			
			killer.getListeners().onKill(this);
			
			if (isPlayer() && killer.isPlayable())
			{
				_currentCp = 0;
			}
		}
		
		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		
		_currentHp = 0;
		
		if (isPlayable())
		{
			boolean fightClubKeepBuffs = isPlayable() && getPlayer().isInFightClub() && (!getPlayer().getFightClubEvent().loseBuffsOnDeath(getPlayer()) || (isSummon() && !Config.FIGHT_CLUB_SUMMON_LOSE_BUFFS_ON_DEATH));
		
			// Stop all active skills effects in progress on the L2Character
			if (isBlessedByNoblesse() || isSalvation() || fightClubKeepBuffs)
			{
				if (isSalvation() && isPlayer() && !getPlayer().isInOlympiadMode() && getPlayer().getPermissions().canResurrect(this, false, true, false))
				{
					getPlayer().reviveRequest(getPlayer(), 100, false);
				}
				for (Effect e : getEffectList().getAllEffects())
				{
					// Noblesse Blessing Buff/debuff effects are retained after
					// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
					if ((e.getEffectType() == EffectType.BlessNoblesse) || (e.getSkill().getId() == Skill.SKILL_FORTUNE_OF_NOBLESSE) || (e.getSkill().getId() == Skill.SKILL_RAID_BLESSING))
					{
						e.exit();
					}
					else if ((e.getEffectType() == EffectType.AgathionResurrect) && !(isPlayable() && getPlayer().isInFightClub()))
					{
						if (isPlayer())
						{
							getPlayer().setAgathionRes(true);
						}
						e.exit();
					}
				}
			}
			else
			{
				for (Effect e : getEffectList().getAllEffects())
				{
					// Some effects persist at death
					if ((e.getEffectType() != EffectType.Transformation) && !e.getSkill().isPreservedOnDeath())
					{
						e.exit();
					}
				}
			}
		}
		
		// Fight Club
		// Killer is in Fight Club
		if ((killer != null) && killer.isPlayable() && killer.getPlayer().isInFightClub())
		{
			killer.getPlayer().getFightClubEvent().onKilled(killer, this);
		}
		else if (isPlayable() && getPlayer().isInFightClub())
		{
			getPlayer().getFightClubEvent().onKilled(killer, this);
		}
		
		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));
		getListeners().onDeath(killer);
		
		updateEffectIcons();
		updateStats();
		broadcastStatusUpdate();
	}
	
	protected void onRevive()
	{
		
	}
	
	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.hashCode());
	}
	
	public void enableSkill(int skill)
	{
		_skillReuses.remove(skill);
	}
	
	/**
	 * Return a map of 32 bits (0x00000000) containing all abnormal effects
	 */
	public int getAbnormalEffect()
	{
		return _abnormalEffects;
	}
	
	/**
	 * Return a map of 32 bits (0x00000000) containing all special effects
	 */
	public int getAbnormalEffect2()
	{
		return _abnormalEffects2;
	}
	
	/**
	 * Return a map of 32 bits (0x00000000) containing all event effects
	 */
	public int getAbnormalEffect3()
	{
		return _abnormalEffects3;
	}
	
	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
	}
	
	/**
	 * Returns skill collection for quick usage
	 */
	public Collection<Skill> getAllSkills()
	{
		return _skills.values();
	}
	
	/**
	 * Returns an array of skills for safe usage
	 */
	public final Skill[] getAllSkillsArray()
	{
		Collection<Skill> vals = _skills.values();
		return vals.toArray(new Skill[vals.size()]);
	}
	
	/**
	 * @return all skills that are not blocked by cooldown/disable/mute at this moment.
	 */
	public final Skill[] getAllAvailableSkillsArray()
	{
		Collection<Skill> skills = getAllSkills();
		List<Skill> vals = new ArrayList<>(skills);
		for (Skill skill : skills)
		{
			if (isUnActiveSkill(skill.getId()) || isSkillDisabled(skill) || isMuted(skill))
			{
				vals.remove(skill);
			}
		}
		return vals.toArray(new Skill[vals.size()]);
	}
	
	public final Skill[] getAllAvailableSkillsArray(SkillType... types)
	{
		List<Skill> vals = new ArrayList<>();
		for (Skill skill : getAllSkills())
		{
			if (isUnActiveSkill(skill.getId()) || isSkillDisabled(skill) || isMuted(skill))
			{
				continue;
			}
			for (SkillType type : types)
			{
				if (skill.getSkillType() == type)
				{
					vals.add(skill);
					break;
				}
			}
		}
		return vals.toArray(new Skill[vals.size()]);
	}
	
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}
	

	
	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}
	
	public Skill getCastingSkill()
	{
		return _castingSkill;
	}
	
	/**
	 * Return the current CP of the L2Character.
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}
	
	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.;
	}
	
	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}
	
	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}
	
	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.;
	}
	
	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}
	
	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}
	
	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.;
	}
	
	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}
	
	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}
	
	public Location getDestination()
	{
		return _destination;
	}
	
	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}
	
	public final Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	public double getMinDistance(GameObject obj)
	{
		double distance = getTemplate().getCollisionRadius();
		
		if ((obj != null) && obj.isCreature())
		{
			distance += ((Creature) obj).getTemplate().getCollisionRadius();
		}
		
		return distance;
	}
	
	@Override
	public double getMoveSpeed()
	{
		if (isRunning())
		{
			return getRunSpeed();
		}
		
		return getWalkSpeed();
	}
	
	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}
	
	/**
	 * @return Visible to the eye name.
	 */
	public String getVisibleName(Playable... eyes)
	{
		return getName();
	}
	
	/**
	 * @return visible to the eye title.
	 */
	public String getVisibleTitle(Playable... eyes)
	{
		return getTitle();
	}
	
	public Clan getVisibleClan(Playable... eyes)
	{
		return getClan();
	}
	
	public final int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
		{
			return 5 + (int) Math.sqrt(getLevel());
		}
		return weaponItem.getRandomDamage();
	}
	
	/**
	 * @return the maximum effective attack element.
	 */
	public Element getAttackElement()
	{
		return getAttackElement(null);
	}
	
	/**
	 * @param target whose element defense should we take. Or null to take the maximum attack element.
	 * @return the maximum effective element.
	 */
	public Element getAttackElement(Creature target)
	{
		double val, max = Double.MIN_VALUE;
		Element result = Element.NONE;
		for (Element e : Element.VALUES)
		{
			val = calcStat(e.getAttack(), 0., null, null);
			if (val <= 0.)
			{
				continue;
			}
			
			if (target != null)
			{
				val -= target.calcStat(e.getDefence(), 0., null, null);
			}
			
			if (val > max)
			{
				result = e;
				max = val;
			}
		}
		
		return result;
	}
	
	/**
	 * @param element
	 * @return the attack value of this element for this creature.
	 */
	public int getAttack(Element element)
	{
		if (element == Element.NONE)
		{
			return 0;
		}
		return (int) calcStat(element.getAttack(), 0., null, null);
	}
	
	/**
	 * @param element
	 * @return the defense value of this element for this creature.
	 */
	public int getDefence(Element element)
	{
		if (element == Element.NONE)
		{
			return 0;
		}
		return (int) calcStat(element.getDefence(), 0., null, null);
	}
	
	public double getReuseModifier(Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	public double getRunSpeed()
	{
		// return getSpeed(_template.getBaseRunSpd());
		return getSpeed(_template.getBaseRunSpd());
	}
	
	public final int getShldDef()
	{
		if (isPlayer())
		{
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
		}
		return (int) calcStat(Stats.SHIELD_DEFENCE, _template.getBaseShldDef(), null, null);
	}
	
	/**
	 * @return : The skill this character has learned or null if not found.
	 */
	public final Skill getSkill(Integer skillId)
	{
		return _skills.get(skillId);
	}
	
	/**
	 * @return : The skill this character has learned or null if not found.<br>
	 *         If the skill is in cooldown or muted, it will return null.
	 */
	public final Skill getAvailableSkill(Integer skillId)
	{
		Skill skill = _skills.get(skillId);
		if (skill == null)
		{
			return null;
		}
		if (isSkillDisabled(skill))
		{
			return null;
		}
		if (isMuted(skill))
		{
			return null;
		}
		
		return skill;
	}
	
	public final int getSkillDisplayLevel(Integer skillId)
	{
		Skill skill = _skills.get(skillId);
		if (skill == null)
		{
			return -1;
		}
		return skill.getDisplayLevel();
	}
	
	public final int getSkillLevel(Integer skillId)
	{
		Skill skill = _skills.get(skillId);
		if (skill == null)
		{
			return -1;
		}
		return skill.getLevel();
		// return getSkillLevel(skillId, -1);
	}
	
	public final int getSkillLevel(Integer skillId, int def)
	{
		Skill skill = _skills.get(skillId);
		if (skill == null)
		{
			return def;
		}
		return skill.getLevel();
	}
	
	public int getSkillMastery(Integer skillId)
	{
		if (_skillMastery == null)
		{
			return 0;
		}
		Integer val = _skillMastery.get(skillId);
		return val == null ? 0 : val.intValue();
	}
	
	public void removeSkillMastery(Integer skillId)
	{
		if (_skillMastery != null)
		{
			_skillMastery.remove(skillId);
		}
	}
	
	public double getSpeed(double baseSpeed)
	{
		if (isInWater())
		{
			return getSwimSpeed();
		}
		return (int) (calcStat(Stats.RUN_SPEED, baseSpeed, null, null) / getArmourExpertisePenalty());
	}
	
	public int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, _template.getBaseSTR(), null, null);
	}
	
	public int getSwimSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
	}
	
	public GameObject getTarget()
	{
		return _target.get();
	}
	
	/**
	 * @param classTypeOfTarget : Player.class, Creature.class, Playable.class, etc.
	 * @return if target corresponds to this class it will cast and return it, else null.
	 */
	@SuppressWarnings("unchecked")
	public <T extends GameObject> T getTarget(Class<T> classTypeOfTarget)
	{
		GameObject target = _target.get();
		if (target == null)
		{
			return null;
		}
		
		if (target.getClass() == classTypeOfTarget)
		{
			return (T) target;
		}
		if (classTypeOfTarget.isAssignableFrom(target.getClass()))
		{
			return (T) target;
		}
		
		return null;
	}
	
	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public void setTarget(GameObject object)
	{
		if ((object != null) && !object.isVisible())
		{
			object = null;
		}

		if (object == null)
		{
			_target = HardReferences.emptyRef();
		}
		else
		{
			_target = object.getRef();
		}
	}
	
	public CharTemplate getTemplate()
	{
		return _template;
	}
	
	public CharTemplate getBaseTemplate()
	{
		return _baseTemplate;
	}
	
	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}
	
	public void setTitle(String title)
	{
		_title = title;
		
	}
	
	public final double getWalkSpeed()
	{
		if (isInWater())
		{
			return getSwimSpeed();
		}
		return getSpeed(_template.getBaseWalkSpd());
	}
	
	public int getWIT()
	{
		return (byte) calcStat(Stats.STAT_WIT, _template.getBaseWIT(), null, null);
	}
	
	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}
	
	public boolean isAlikeDead()
	{
		return _fakeDeath || isDead();
	}
	
	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}
	
	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}
	
	public final boolean isSalvation()
	{
		return _isSalvation;
	}
	
	public boolean isEffectImmune()
	{
		return _effectImmunity.get();
	}
	
	public boolean isBuffImmune()
	{
		return _buffImmunity.get();
	}
	
	public boolean isDebuffImmune()
	{
		return _debuffImmunity.get();
	}
	
	public boolean isDead()
	{
		return (_currentHp < 0.5) || _isDead.get();
	}
	
	@Override
	public final boolean isFlying()
	{
		return _flying;
	}
	
	/**
	 * Is the character in battle pose
	 * @return true, if a character in a combat position, attacked or attacking
	 */
	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}
	
	/**
	 * @return currentTimeInMilis of the last time this character was attacked.
	 */
	public final long getLastAttackedTime()
	{
		return _lastAttackedTime;
	}
	
	/**
	 * @return if the character was attacked in the past timeInMilis miliseconds.
	 */
	public final boolean isLastAttackedIn(int timeInMilis)
	{
		return (System.currentTimeMillis() - _lastAttackedTime) <= timeInMilis;
	}
	
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public boolean isImmortal()
	{
		return _isImmortal;
	}
	
	public boolean isRealTargeted()
	{
		return _isRealTargeted;
	}
	
	public void addReceivedDebuff(int skillId, long period)
	{
		if (!Config.DEBUFF_PROTECTION_SYSTEM)
		{
			return;
		}
		
		Long[] debuffInfo = new Long[2];
		debuffInfo[0] = System.currentTimeMillis() + period;// Time of Debuff Effect End
		debuffInfo[1] = period;
		receivedDebuffs.put(skillId, debuffInfo);
	}
	
	public final boolean isRunning()
	{
		return _running;
	}
	
	public boolean isMoving()
	{
		return isMoving;
	}
	
	protected long _reuseDelay = 0L;
	
	public final void setReuseDelay(long newReuseDelay)
	{
		_reuseDelay = (newReuseDelay + System.currentTimeMillis());
	}
	
	public final long getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public boolean isSkillDisabled(Skill skill)
	{
		TimeStamp sts = _skillReuses.get(skill.hashCode());
		if (sts == null)
		{
			return false;
		}
		if (sts.hasNotPassed())
		{
			return true;
		}
		_skillReuses.remove(skill.hashCode());
		return false;
	}
	
	public final boolean isTeleporting()
	{
		return _isTeleporting.get();
	}
	
	/**
	 * ÃŽ Ã¢â‚¬â„¢ÃŽ ÃŽÅ½ÃŽ Ã‚Â·ÃŽ Ã‚Â²ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Â°ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽ Ã‚Â·ÃŽ ÃŽË†ÃŽÂ¡Ã¢â‚¬ ÃŽ ÃŽË†ÃŽÂ¡Ã¯Â¿Â½ ÃŽÂ¡Ã¢â‚¬ ÃŽ Ã‚ÂµÃŽ Ã‚Â»ÃŽ ÃŽË†, ÃŽ Ã‚Â² ÃŽ ÃŽÅ ÃŽ ÃŽÅ½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ ÃŽÅ½ÃŽÂ¡Ã¢â€šÂ¬ÃŽ ÃŽÅ½ÃŽ ÃŽâ€° ÃŽ ÃŽÅ½ÃŽ Ã‚Â½ÃŽ Ã‚Â° ÃŽ Ã‚Â±ÃŽÂ¡Ã†â€™ÃŽ ÃŽâ€žÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽÂ¡Ã¢â‚¬Â¡ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚ÂµÃŽ Ã‚Â· ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽ Ã‚Â» ÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚ÂµÃŽ ÃŽÅ ÃŽÂ¡Ã†â€™ÃŽ Ã‚Â½ÃŽ ÃŽâ€žÃŽÂ¡Ã¢â‚¬Â¹. Returns the position of the purpose for which it will be in
	 * half a second.
	 */
	public Location getIntersectionPoint(Creature target)
	{
		if (!Location.isFacing(this, target, 90))
		{
			return new Location(target.getX(), target.getY(), target.getZ());
		}
		double angle = Location.convertHeadingToDegree(target.getHeading());
		double radian = Math.toRadians(angle - 90);
		double range = target.getMoveSpeed() / 2; // distance for one second power of speed. We take half.
		return new Location((int) (target.getX() - (range * Math.sin(radian))), (int) (target.getY() + (range * Math.cos(radian))), target.getZ());
	}
	
	public Location applyOffset(Location point, int offset)
	{
		if (offset <= 0)
		{
			return point;
		}
		
		long dx = point.x - getX();
		long dy = point.y - getY();
		long dz = point.z - getZ();
		
		double distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		
		if (distance <= offset)
		{
			point.set(getX(), getY(), getZ());
			return point;
		}
		
		if (distance >= 1)
		{
			double cut = offset / distance;
			point.x -= (int) ((dx * cut) + 0.5);
			point.y -= (int) ((dy * cut) + 0.5);
			point.z -= (int) ((dz * cut) + 0.5);
			
			if (!isFlying() && !isInBoat() && !isInWater() && !isBoat())
			{
				point.correctGeoZ();
			}
		}
		
		return point;
	}
	
	public List<Location> applyOffset(List<Location> points, int offset)
	{
		offset = offset >> 4;
		if (offset <= 0)
		{
			return points;
		}
		
		long dx = points.get(points.size() - 1).x - points.get(0).x;
		long dy = points.get(points.size() - 1).y - points.get(0).y;
		long dz = points.get(points.size() - 1).z - points.get(0).z;
		
		double distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		if (distance <= offset)
		{
			Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}
		
		if (distance >= 1)
		{
			double cut = offset / distance;
			int num = (int) ((points.size() * cut) + 0.5);
			for (int i = 1; (i <= num) && (points.size() > 0); i++)
			{
				points.remove(points.size() - 1);
			}
		}
		
		return points;
	}
	
	private boolean setSimplePath(Location dest)
	{
		List<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if (moveList.isEmpty())
		{
			return false;
		}
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return true;
	}
	
	private boolean buildPathTo(int x, int y, int z, int offset, boolean pathFind)
	{
		return buildPathTo(x, y, z, offset, null, false, pathFind);
	}
	
	private boolean buildPathTo(int x, int y, int z, int offset, Creature follow, boolean forestalling, boolean pathFind)
	{
		int geoIndex = getGeoIndex();
		
		Location dest;
		
		if (forestalling && (follow != null) && follow.isMoving)
		{
			dest = getIntersectionPoint(follow);
		}
		else
		{
			dest = new Location(x, y, z);
		}
		
		if (isInBoat() || isBoat() || !Config.ALLOW_GEODATA)
		{
			applyOffset(dest, offset);
			return setSimplePath(dest);
		}
		
		if (isFlying() || isInWater())
		{
			applyOffset(dest, offset);
			
			Location nextloc;
			
			if (isFlying())
			{
				if (GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, true))
				{
					return setSimplePath(dest);
				}
				
				nextloc = GeoEngine.moveCheckInAir(getX(), getY(), getZ(), dest.x, dest.y, dest.z, getColRadius(), geoIndex);
				if ((nextloc != null) && !nextloc.equals(getX(), getY(), getZ()))
				{
					return setSimplePath(nextloc);
				}
			}
			else
			{
				int waterZ = getWaterZ();
				nextloc = GeoEngine.moveInWaterCheck(getX(), getY(), getZ(), dest.x, dest.y, dest.z, waterZ, geoIndex);
				if (nextloc == null)
				{
					return false;
				}
				
				List<Location> moveList = GeoMove.constructMoveList(getLoc(), nextloc.clone());
				_targetRecorder.clear();
				if (!moveList.isEmpty())
				{
					_targetRecorder.add(moveList);
				}
				
				int dz = dest.z - nextloc.z;
				
				if ((dz > 0) && (dz < 128))
				{
					moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
					if (moveList != null)
					{
						if (!moveList.isEmpty())
						{
							_targetRecorder.add(moveList);
						}
					}
				}
				
				return !_targetRecorder.isEmpty();
			}
			return false;
		}
		
		List<Location> moveList = null;
		try
		{
			moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, true); // onlyFullPath = true -
		}
		catch (Exception e)
		{
			if (_moveExceptionsThrown < 3)
			{
				_log.error("Building path has failed for character: " + this + " at location " + getLoc(), e);
			}
			
			_moveExceptionsThrown++;
		}
		if (moveList != null) // null -
		{
			if (moveList.isEmpty())
			{
				return false;
			}
			applyOffset(moveList, offset);
			if (moveList.isEmpty())
			{
				return false;
			}
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}
		
		if (pathFind)
		{
			List<List<Location>> targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.clone(), this, true, geoIndex);
			if (!targets.isEmpty())
			{
				moveList = targets.remove(targets.size() - 1);
				applyOffset(moveList, offset);
				if (!moveList.isEmpty())
				{
					targets.add(moveList);
				}
				if (!targets.isEmpty())
				{
					_targetRecorder.clear();
					_targetRecorder.addAll(targets);
					return true;
				}
			}
		}
		
		if (follow != null)
		{
			return false;
		}
		
		applyOffset(dest, offset);
		
		moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, false); // onlyFullPath = false
		if ((moveList != null) && !moveList.isEmpty()) // null
		{
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}
		
		return false;
	}
	
	public Creature getFollowTarget()
	{
		return _followTarget.get();
	}
	
	public void setFollowTarget(Creature target)
	{
		_followTarget = target == null ? HardReferences.emptyRef() : target.getRef();
	}
	
	public boolean followToCharacter(Creature target, int offset, boolean forestalling)
	{
		return followToCharacter(target.getLoc(), target, offset, forestalling);
	}
	
	public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
	{
		moveLock.lock();
		try
		{
			if (isMovementDisabled() || (target == null) || isInBoat() || isInWater() || (target.isInvisible() && !canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)))
			{
				return false;
			}
			
			offset = Math.max(offset, 10);
			if (isFollow && (target == getFollowTarget()) && (offset == _offset))
			{
				return true;
			}
			
			if ((Math.abs(getZ() - target.getZ()) > 1000) && !isFlying())
			{
				sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				return false;
			}
			
			getAI().clearNextAction();
			
			stopMove(false, false);
			
			if (buildPathTo(loc.x, loc.y, loc.z, offset, target, forestalling, !target.isDoor()))
			{
				_movingDestTempPos.set(loc.x, loc.y, loc.z);
			}
			else
			{
				return false;
			}
			
			isMoving = true;
			isFollow = true;
			_forestalling = forestalling;
			_offset = offset;
			setFollowTarget(target);
			
			moveNext(true);
			
			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}
	
	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
	{
		moveLock.lock();
		try
		{
			offset = Math.max(offset, 0);
			Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if (isMoving && !isFollow && _movingDestTempPos.equals(dst_geoloc))
			{
				sendActionFailed();
				return true;
			}
			
			if (isMovementDisabled())
			{
				getAI().setNextAction(nextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding, false);
				sendActionFailed();
				return false;
			}
			
			getAI().clearNextAction();
			
			if (isPlayer())
			{
				getAI().changeIntention(AI_INTENTION_ACTIVE, null, null);
			}
			
			stopMove(false, false);
			
			if (buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding))
			{
				_movingDestTempPos.set(dst_geoloc);
			}
			else
			{
				sendActionFailed();
				return false;
			}
			
			isMoving = true;
			
			moveNext(true);
			
			return true;
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	private void moveNext(boolean firstMove)
	{
		if (!isMoving || isMovementDisabled())
		{
			stopMove();
			return;
		}
		
		_previousSpeed = getMoveSpeed();
		if (_previousSpeed <= 0)
		{
			stopMove();
			return;
		}
		
		if (!firstMove)
		{
			Location dest = _destination;
			if (dest != null)
			{
				setLoc(dest, true);
			}
		}
		
		if (_targetRecorder.isEmpty())
		{
			CtrlEvent ctrlEvent = isFollow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
			stopMove(false, true);
			ThreadPoolManager.getInstance().execute(new NotifyAITask(this, ctrlEvent));
			return;
		}
		
		_moveList = _targetRecorder.remove(0);
		Location begin = _moveList.get(0).clone().geo2world();
		Location end = _moveList.get(_moveList.size() - 1).clone().geo2world();
		_destination = end;
		double distance = (isFlying() || isInWater()) ? begin.distance3D(end) : begin.distance(end);
		
		if (distance != 0)
		{
			setHeading(Location.calculateHeadingFrom(getX(), getY(), _destination.x, _destination.y));
		}
		
		broadcastMove();
		
		_startMoveTime = _followTimestamp = System.currentTimeMillis();
		if (_moveTaskRunnable == null)
		{
			_moveTaskRunnable = new MoveNextTask();
		}
		_moveTask = ThreadPoolManager.getInstance().schedule(_moveTaskRunnable.setDist(distance), getMoveTickInterval());
	}

	public class MoveNextTask extends RunnableImpl
	{
		private double alldist, donedist;
		
		public MoveNextTask setDist(double dist)
		{
			alldist = dist;
			donedist = 0.;
			return this;
		}
		
		@Override
		public void runImpl()
		{
			if (!isMoving)
			{
				return;
			}
			
			moveLock.lock();
			try
			{
				if (!isMoving)
				{
					return;
				}
				
				if (isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				Creature follow = null;
				final double speed = getMoveSpeed();
				if (speed <= 0)
				{
					stopMove();
					return;
				}
				long now = System.currentTimeMillis();
				
				if (isFollow)
				{
					follow = getFollowTarget();
					if ((follow == null) || (follow.isInvisible() && !canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)))
					{
						stopMove();
						return;
					}
					if (isInRangeZ(follow, _offset) && GeoEngine.canSeeTarget(Creature.this, follow, false))
					{
						stopMove();
						ThreadPoolManager.getInstance().execute(new NotifyAITask(Creature.this, CtrlEvent.EVT_ARRIVED_TARGET));
						return;
					}
				}
				
				if (alldist <= 0)
				{
					moveNext(false);
					return;
				}
				
				donedist += ((now - _startMoveTime) * _previousSpeed) / 1000.;
				double done = donedist / alldist;
				
				if (done < 0)
				{
					done = 0;
				}
				if (done >= 1)
				{
					moveNext(false);
					return;
				}
				
				if (isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				Location loc = null;
				
				int index = (int) (_moveList.size() * done);
				if (index >= _moveList.size())
				{
					index = _moveList.size() - 1;
				}
				if (index < 0)
				{
					index = 0;
				}
				
				loc = _moveList.get(index).clone().geo2world();
				
				if (!isFlying() && !isInBoat() && !isInWater() && !isBoat())
				{
					if ((loc.z - getZ()) > 256)
					{
						String bug_text = "geo bug 1 at: " + getLoc() + " => " + loc.x + "," + loc.y + "," + loc.z + "\tAll path: " + _moveList.get(0) + " => " + _moveList.get(_moveList.size() - 1);
						Log.add(bug_text, "geo");
						stopMove();
						return;
					}
				}
				
				if ((loc == null) || isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				setLoc(loc, true);
				
				if (isMovementDisabled())
				{
					stopMove();
					return;
				}
				
				if (isFollow && ((now - _followTimestamp) > (_forestalling ? 500 : 1000)) && (follow != null) && !follow.isInRange(_movingDestTempPos, Math.max(100, _offset)))
				{
					if ((Math.abs(getZ() - loc.z) > 1000) && !isFlying())
					{
						sendPacket(SystemMsg.CANNOT_SEE_TARGET);
						stopMove();
						return;
					}
					if (buildPathTo(follow.getX(), follow.getY(), follow.getZ(), _offset, follow, true, true))
					{
						_movingDestTempPos.set(follow.getX(), follow.getY(), follow.getZ());
					}
					else
					{
						stopMove();
						return;
					}
					moveNext(true);
					return;
				}
				
				_previousSpeed = speed;
				_startMoveTime = now;
				_moveTask = ThreadPoolManager.getInstance().schedule(this, getMoveTickInterval());
			}
			catch (Exception e)
			{
				_log.error("Error in Creature Moving! ", e);
			}
			finally
			{
				moveLock.unlock();
			}
		}
	}
	
	public int getMoveTickInterval()
	{
		return (isPlayer() ? 16000 : 32000) / ((int) Math.max(getMoveSpeed(), 1)); // TODO: Check the cast from getMoveSpeed
	}

	public boolean isPerfectBlock()
	{
		return _perfectBlock;
	}

	public void setPerfectBlock(boolean val)
	{
		_perfectBlock = val;
	}


	private void broadcastMove()
	{
		validateLocation(isPlayer() ? 2 : 1);
		broadcastPacket(movePacket());
	}
	
	/**
	 * ÃŽ Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ Ã‚Â²ÃŽ Ã‚Â»ÃŽ ÃŽË†ÃŽ Ã‚Â²ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽâ€žÃŽ Ã‚Â²ÃŽ ÃŽË†ÃŽ Ã‚Â¶ÃŽ Ã‚ÂµÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽ Ã‚Âµ ÃŽ ÃŽË† ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Â¹ÃŽ Ã‚Â»ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ StopMove, ValidateLocation
	 */
	public void stopMove()
	{
		stopMove(true, true);
	}
	
	/**
	 * ÃŽ Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ Ã‚Â²ÃŽ Ã‚Â»ÃŽ ÃŽË†ÃŽ Ã‚Â²ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽâ€žÃŽ Ã‚Â²ÃŽ ÃŽË†ÃŽ Ã‚Â¶ÃŽ Ã‚ÂµÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽ Ã‚Âµ ÃŽ ÃŽË† ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Â¹ÃŽ Ã‚Â»ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ StopMove
	 * @param validate - ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Â¹ÃŽ Ã‚Â»ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ ÃŽ Ã‚Â»ÃŽ ÃŽË† ValidateLocation
	 */
	public void stopMove(boolean validate)
	{
		stopMove(true, validate);
	}
	
	/**
	 * ÃŽ Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ Ã‚Â²ÃŽ Ã‚Â»ÃŽ ÃŽË†ÃŽ Ã‚Â²ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽâ€žÃŽ Ã‚Â²ÃŽ ÃŽË†ÃŽ Ã‚Â¶ÃŽ Ã‚ÂµÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽ Ã‚Âµ
	 * @param stop - ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Â¹ÃŽ Ã‚Â»ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ ÃŽ Ã‚Â»ÃŽ ÃŽË† StopMove
	 * @param validate - ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Â¹ÃŽ Ã‚Â»ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ ÃŽ Ã‚Â»ÃŽ ÃŽË† ValidateLocation
	 */
	public void stopMove(boolean stop, boolean validate)
	{
		if (!isMoving)
		{
			return;
		}
		
		moveLock.lock();
		try
		{
			if (!isMoving)
			{
				return;
			}
			
			isMoving = false;
			isFollow = false;
			
			if (_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}
			
			_destination = null;
			_moveList = null;
			
			_targetRecorder.clear();
			
			if (validate)
			{
				validateLocation(isPlayer() ? 2 : 1);
			}
			if (stop)
			{
				broadcastPacket(stopMovePacket());
			}
		}
		finally
		{
			moveLock.unlock();
		}
	}
	
	/** ÃŽ Ã¢â‚¬â„¢ÃŽ ÃŽÅ½ÃŽ Ã‚Â·ÃŽ Ã‚Â²ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Â°ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽ ÃŽÅ ÃŽ ÃŽÅ½ÃŽ ÃŽÅ½ÃŽÂ¡Ã¢â€šÂ¬ÃŽ ÃŽâ€žÃŽ ÃŽË†ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¢â‚¬Â¹ ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽ Ã‚Â²ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â€šÂ¬ÃŽÂ¡Ã¢â‚¬Â¦ÃŽ Ã‚Â½ÃŽ ÃŽÅ½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ ÃŽË† ÃŽ Ã‚Â²ÃŽ ÃŽÅ½ÃŽ ÃŽâ€žÃŽÂ¡Ã¢â‚¬Â¹, ÃŽ Ã‚ÂµÃŽÂ¡Ã¯Â¿Â½ÃŽ Ã‚Â»ÃŽ ÃŽË† ÃŽ ÃŽÅ’ÃŽÂ¡Ã¢â‚¬Â¹ ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Â¦ÃŽ ÃŽÅ½ÃŽ ÃŽâ€žÃŽ ÃŽË†ÃŽ ÃŽÅ’ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ ÃŽ Ã‚Â² ÃŽ Ã‚Â½ÃŽ Ã‚ÂµÃŽ ÃŽâ€°, ÃŽ ÃŽË†ÃŽ Ã‚Â»ÃŽ ÃŽË† ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ ÃŽâ€ž ÃŽ Ã‚Â½ÃŽ Ã‚ÂµÃŽ ÃŽâ€°. */
	public int getWaterZ()
	{
		if (!isInWater())
		{
			return Integer.MIN_VALUE;
		}
		
		int waterZ = Integer.MIN_VALUE;
		_zonesRead.lock();
		try
		{
			Zone zone;
			for (Zone _zone : _zones)
			{
				zone = _zone;
				if (zone.getType() == ZoneType.water)
				{
					if ((waterZ == Integer.MIN_VALUE) || (waterZ < zone.getTerritory().getZmax()))
					{
						waterZ = zone.getTerritory().getZmax();
					}
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		
		return waterZ;
	}
	
	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}
	
	public L2GameServerPacket movePacket()
	{
		return new CharMoveToLocation(this);
	}
	
	public void updateZones()
	{
		if (isInObserverMode())
		{
			return;
		}
		
		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;
		
		ArrayList<Zone> entering = null;
		ArrayList<Zone> leaving = null;
		
		Zone zone;
		
		_zonesWrite.lock();
		try
		{
			if (!_zones.isEmpty())
			{
				leaving = new ArrayList<>();
				for (Zone _zone : _zones)
				{
					zone = _zone;
					
					if (!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
					{
						leaving.add(zone);
					}
				}
				
				if (!leaving.isEmpty())
				{
					for (Zone element : leaving)
					{
						zone = element;
						_zones.remove(zone);
					}
				}
			}
			
			if (zones.length > 0)
			{
				entering = new ArrayList<>();
				for (Zone zone2 : zones)
				{
					zone = zone2;
					
					if (!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
					{
						entering.add(zone);
					}
				}
				
				if (!entering.isEmpty())
				{
					for (Zone element : entering)
					{
						zone = element;
						_zones.add(zone);
					}
				}
			}
		}
		finally
		{
			_zonesWrite.unlock();
		}
		
		onUpdateZones(leaving, entering);
		
	}
	
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		Zone zone;
		
		if ((leaving != null) && !leaving.isEmpty())
		{
			for (Zone element : leaving)
			{
				zone = element;
				zone.doLeave(this);
			}
		}
		
		if ((entering != null) && !entering.isEmpty())
		{
			for (Zone element : entering)
			{
				zone = element;
				zone.doEnter(this);
			}
		}
	}
	
	public boolean isInZonePeace()
	{
		
		// Synerge - Check if autoattackble on GM Events
		if (isPlayable() && GmEventManager.getInstance().isParticipating(getPlayer()))
		{
			switch (GmEventManager.getInstance().isAutoAttackable(getPlayer(), getPlayer()))
			{
				case GmEventManager.RETURN_TRUE:
					return false;
			}
		}
		
		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		
		if (Config.PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE && runnerEvent.isInProgress())
		{
			if ((getPlayer() != null) && getPlayer().isPlayer() && runnerEvent.isParticle(getPlayer()))
			{
				return false;
			}
		}
		
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle() && !isInActiveGlobalPvPZone();
	}
	
	public boolean isInZoneBattle()
	{
//		if ((getPlayer() != null) && (getPlayer().getTeamEvents() > 2))
//		{
//			return true;
//		}
//		
		// Synerge - Check if autoattackble on GM Events
		if (isPlayable() && GmEventManager.getInstance().isParticipating(getPlayer()))
		{
			switch (GmEventManager.getInstance().isAutoAttackable(getPlayer(), getPlayer()))
			{
				case GmEventManager.RETURN_TRUE:
					return true;
			}
		}

		return isInZone(ZoneType.battle_zone) || isInZone(ZoneType.world_pvp);
	}
	

	/**
	 * In this zone, players are not getting PvP Flags or Karma Neutral or Enemy players are Auto Attackable
	 * @return is Inside Valakas, Antharas or Baium zone
	 */
	public boolean isInZonePvP()
	{
		for (Zone zone : _zones)
			if (zone.getTemplate().isEpicPvP())
				return true;
		return false;
	}

	
	public boolean isInActiveGlobalPvPZone()
	{
		if (!GlobalPvPZoneTaskManager.getInstance().isGlobalPvpOn() || (isInZone(Zone.ZoneType.peace_zone) && isPlayable() && getPlayer().isInStoreMode()))
		{
			return false;
		}
		
		_zonesRead.lock();
		try
		{
			for (final Zone zone : _zones)
			{
				if ((zone.getType() == Zone.ZoneType.global_pvp_zone) && zone.isActive())
				{
					return true;
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		return false;
	}
	
	public boolean isInWater()
	{
		return isInZone(ZoneType.water) && !(isInBoat() || isBoat() || isFlying());
	}
	
	public boolean isInZone(ZoneType type)
	{
		_zonesRead.lock();
		try
		{
			Zone zone;
			for (Zone _zone : _zones)
			{
				zone = _zone;
				if (zone.getType() == type)
				{
					return true;
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		
		return false;
	}
	
	public boolean isInZone(String name)
	{
		_zonesRead.lock();
		try
		{
			Zone zone;
			for (Zone _zone : _zones)
			{
				zone = _zone;
				if (zone.getName().equals(name))
				{
					return true;
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		
		return false;
	}
	
	public boolean isInZone(Zone zone)
	{
		if (zone == null)
		{
			return false;
		}
		
		_zonesRead.lock();
		try
		{
			return _zones.contains(zone);
		}
		finally
		{
			_zonesRead.unlock();
		}
	}
	
	public Zone getZone(ZoneType type)
	{
		_zonesRead.lock();
		try
		{
			Zone zone;
			for (Zone _zone : _zones)
			{
				zone = _zone;
				if (zone.getType() == type)
				{
					return zone;
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		return null;
	}
	
	public List<Zone> getZones()
	{
		_zonesRead.lock();
		try
		{
			List<Zone> zones = new ArrayList<>(_zones.size());
			zones.addAll(_zones);
			return zones;
		}
		finally
		{
			_zonesRead.unlock();
		}
	}
	
	public Location getRestartPoint()
	{
		_zonesRead.lock();
		try
		{
			Zone zone;
			for (Zone _zone : _zones)
			{
				zone = _zone;
				if (zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if ((type == ZoneType.battle_zone) || (type == ZoneType.peace_zone) || (type == ZoneType.offshore) || (type == ZoneType.dummy))
					{
						return zone.getSpawn();
					}
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		
		return null;
	}
	
	public Location getPKRestartPoint()
	{
		_zonesRead.lock();
		try
		{
			Zone zone;
			for (Zone _zone : _zones)
			{
				zone = _zone;
				if (zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if ((type == ZoneType.battle_zone) || (type == ZoneType.peace_zone) || (type == ZoneType.offshore) || (type == ZoneType.dummy))
					{
						return zone.getPKSpawn();
					}
				}
			}
		}
		finally
		{
			_zonesRead.unlock();
		}
		
		return null;
	}
	
	public synchronized void setDynamicZone(ZoneTemplate template)
	{
		if (_dynamicZone != null)
		{
			World.removeZone(_dynamicZone);
		}
		
		if (template == null)
		{
			return;
		}
		
		_dynamicZone = new Zone(template);
		_dynamicZone.setName(String.valueOf(getObjectId() + ": Dynamic Zone " + _dynamicZone.getName()));
	}
	
	@Override
	public int getGeoZ(Location loc)
	{
		if (isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
		{
			return loc.z;
		}
		
		return super.getGeoZ(loc);
	}
	
	protected boolean needStatusUpdate()
	{
		if (!isVisible())
		{
			return false;
		}
		
		boolean result = false;
		
		int bar;
		bar = (int) ((getCurrentHp() * CLIENT_BAR_SIZE) / getMaxHp());
		if ((bar == 0) || (bar != _lastHpBarUpdate))
		{
			_lastHpBarUpdate = bar;
			result = true;
		}
		
		bar = (int) ((getCurrentMp() * CLIENT_BAR_SIZE) / getMaxMp());
		if ((bar == 0) || (bar != _lastMpBarUpdate))
		{
			_lastMpBarUpdate = bar;
			result = true;
		}
		
		if (isPlayer())
		{
			bar = (int) ((getCurrentCp() * CLIENT_BAR_SIZE) / getMaxCp());
			if ((bar == 0) || (bar != _lastCpBarUpdate))
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}
		
		return result;
	}
	
	@Override
	public void onForcedAttack(Player player, boolean shift)
	{
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		
		if (!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}
		
		player.getAI().Attack(this, true, shift);
	}
	
	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if (isAlikeDead())
		{
			sendActionFailed();
			return;
		}
		
		if (target.isNpc() && ((NpcInstance) target).isEventMob)
		{
			sendActionFailed();
			return;
		}
		
		if (target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}
		
		if (isPlayable() && target.isPlayable() && (isInZoneBattle() != target.isInZoneBattle()))
		{
			Player player = getPlayer();
			if (player != null)
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}
		
		target.getListeners().onAttackHit(this);
		
		// if hitted by a cursed weapon, Cp is reduced to 0, if a cursed weapon is hitted by a Hero, Cp is reduced to 0
		if (!miss && target.isPlayer() && (isCursedWeaponEquipped() || ((getActiveWeaponInstance() != null) && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped())))
		{
			target.setCurrentCp(0);
		}
		
		if (target.isStunned() && Formulas.calcStunBreak(crit))
			target.getEffectList().stopEffects(EffectType.Stun);
		
		
		displayGiveDamageMessage(target, damage, crit, miss, shld, false);
		
		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, damage));
		
		boolean checkPvP = checkPvP(target, null);
		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		if (!miss && (damage > 0))
		{
			target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true);
			
			// Skills cast by at physical attack
			if (!target.isDead())
			{
				if (crit)
				{
					useTriggers(target, TriggerType.CRIT, null, null, damage);
				}
				
				useTriggers(target, TriggerType.ATTACK, null, null, damage);
				
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				// if (Formulas.calcCastBreak(target, crit))
				if (Formulas.calcCastBreak(target, crit))
				{
					target.abortCast(false, true);
				}
			}
			
			if (soulshot && unchargeSS)
			{
				unChargeShots(false);
			}
		}
		
		if (miss)
		{
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
		}
		
		startAttackStanceTask();
		
		if (checkPvP)
		{
			startPvPFlag(target);
		}
	}
	
	public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
	{
		_castInterruptTime = 0;
		
		if (skill.isUsingWhileCasting())
		{
			aimingTarget.getEffectList().stopEffect(skill.getId());
			onCastEndTime();
			return;
		}
		
		if (!skill.isOffensive() && (getAggressionTarget() != null))
		{
			forceUse = true;
		}
		
		if (!skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if ((skill.getSkillType() == SkillType.PET_SUMMON) && isPlayer())
			{
				getPlayer().setPetControlItem(null);
			}
			onCastEndTime();
			return;
			
		}
		
		if ((_skillGeoCheckTask != null) && !GeoEngine.canSeeTarget(this, aimingTarget, isFlying()))
		{
			sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			broadcastPacket(new MagicSkillCancelled(getObjectId()));
			onCastEndTime();
			return;
		}
		
		List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			int vitalityConsume = skill.getVitConsume();
			if ((vitalityConsume > 0) && (isPlayer()))
			{
				Player p = (Player) this;
				p.setVitality(Math.max(0.0D, p.getVitality() - vitalityConsume));
			}
			
			int hpConsume = skill.getHpConsume();
			if (hpConsume > 0)
			{
				setCurrentHp(Math.max(0, _currentHp - hpConsume), false);
			}
			
			double mpConsume2 = skill.getMpConsume2();
			
			if (mpConsume2 > 0)
			{
				if (skill.isMusic())
				{
					double inc = mpConsume2 / 2;
					double add = 0;
					for (Effect e : getEffectList().getAllEffects())
					{
						if ((e.getSkill().getId() != skill.getId()) && e.getSkill().isMusic() && (e.getTimeLeft() > 30))
						{
							add += inc;
						}
					}
					mpConsume2 += add;
					mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
				}
				else if (skill.isMagic())
				{
					mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
				}
				else
				{
					mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
				}
				
				if ((_currentMp < mpConsume2) && isPlayable())
				{
					sendPacket(SystemMsg.NOT_ENOUGH_MP);
					onCastEndTime();
					return;
				}
				reduceCurrentMp(mpConsume2, null);
			}
		}
		
		callSkill(skill, targets, true);
		
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			if (skill.getNumCharges() > 0)
			{
				setIncreasedForce(getIncreasedForce() - skill.getNumCharges());
			}
			
			if (skill.isSoulBoost())
			{
				setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
			}
			else if (skill.getSoulsConsume() > 0)
			{
				setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
			}
		}
		
		switch (skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
				Location flyLoc;
				for (Creature target : targets)
				{
					// target.setHeading(this, false); //TODO [VISTALL] set heading of target ? Oo
					flyLoc = getFlyLocation(null, skill);
					target.setLoc(flyLoc);
					broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
				}
				break;
			default:
				break;
		}
		
		if (_scheduledCastCount > 0)
		{
			_scheduledCastCount--;
			_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), _scheduledCastInterval);
			_skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), _scheduledCastInterval);
			return;
		}
		
		// int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		// Synerge - Min cooltime is 100
		//int skillCoolTime = Math.max(100, Formulas.calcMAtkSpd(this, skill, skill.getCoolTime()));
		int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		if (skillCoolTime > 0)
		{
			ThreadPoolManager.getInstance().schedule(new CastEndTimeTask(this), skillCoolTime);
		}
		else
		{
			onCastEndTime();
		}
	}
	
	public void onCastEndTime()
	{
		finishFly();
		clearCastVars();
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, getCastingSkill(), getTarget());
	}
	
	public void clearCastVars()
	{
		_animationEndTime = 0;
		_animationEndTime40 = 0;
		_castInterruptTime = 0;
		_scheduledCastCount = 0;
		_castingSkill = null;
//		_skillTask = null;
//		_skillLaunchedTask = null;
//
//		_flyLoc = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_skillGeoCheckTask = null;
		_flyLoc = null;
		if (isPlayer())
		{
			((Player) this).setGroundSkillLoc(null);
		}
	}
	
	private void finishFly()
	{
		Location flyLoc = _flyLoc;
		_flyLoc = null;
		if (flyLoc != null)
		{
			setLoc(flyLoc);
			validateLocation(1);
		}
	}
	
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if ((attacker == null) || isDead() || (attacker.isDead() && !isDot))
		{
			return;
		}
		
		if (isDamageBlocked() && transferDamage)
		{
			return;
		}
		
		if (getEffectList().getEffectsBySkillId(922) != null)
		{
			return;
		}
		
		if (isDamageBlocked() && (attacker != this))
		{
			if (sendMessage)
			{
				attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			return;
		}
		
		if (canReflect)
		{
			if (attacker.absorbAndReflect(this, skill, damage))
				return;
			
			damage = absorbToEffector(attacker, damage);
			damage = absorbToMp(attacker, damage);
			damage = absorbToSummon(attacker, damage);
		}

		getListeners().onCurrentHpDamage(damage, attacker, skill);
		if (attacker != this)
		{
			if (sendMessage)
			//if (sendMessage && (damage > 0.0D || transferedDamage > 0.0D))
			{
				displayReceiveDamageMessage(attacker, (int) damage);
			}
			
			if (!isDot)
			{
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
			}
		}
		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	protected void onReduceCurrentHp(final double damage, Creature attacker, Skill skill,
									 boolean awake, boolean standUp, boolean directHp)
	{
		// Fight Club Support
		if (attacker != null && attacker.isPlayable() && attacker.getPlayer().isInFightClub())
		{
			attacker.getPlayer().getFightClubEvent().onDamage(attacker, this, damage);
		}

		// Wake-up from sleep
		if (awake && isSleeping())
		{
			getEffectList().stopEffects(EffectType.Sleep);
		}

		// Normal combat reaction
		if ((attacker != this) || (skill != null && skill.isOffensive()))
		{
			if (isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if (effect != null)
					getEffectList().stopEffect(effect.getSkill());
			}

			startAttackStanceTask();
			checkAndRemoveInvisible();
			_lastAttackedTime = System.currentTimeMillis();

			if (getCurrentHp() < 0.5)
				useTriggers(attacker, TriggerType.DIE, null, null, damage);
		}

		// APPLY FINAL DAMAGE – ONE TIME ONLY
		setCurrentHp(Math.max(getCurrentHp() - damage, 0), false);

		// ----------------------------------------------------
		// DEBUG HOOK (SAFE)
		// ----------------------------------------------------
		// DEBUG HOOK
		try
		{
			CombatEventHook.onDamage(
					attacker,                           // attacker
					this,                               // target
					(skill != null ? skill.getId() : 0),
					damage,                             // <<< REQUIRED
					false,                              // crit
					false                               // miss
			);
		}
		catch (Exception e)
		{
			System.out.println("[CombatEventHook] Error: " + e.getMessage());
		}
		// ----------------------------------------------------

		// Death check
		if (getCurrentHp() < 0.5)
		{
			if (!isPlayer() || !(getPlayer().isInOlympiadMode()
					&& getPlayer().getOlympiadGame().getType() != CompType.TEAM))
			{
				doDie(attacker);
			}
		}
	}

	
	public void reduceCurrentMp(double i, Creature attacker)
	{
		if ((attacker != null) && (attacker != this))
		{
			if (isSleeping())
			{
				getEffectList().stopEffects(EffectType.Sleep);
			}
			
			if (isMeditated())
			{
				Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if (effect != null)
				{
					getEffectList().stopEffect(effect.getSkill());
				}
			}
		}
		
		if (isDamageBlocked() && (attacker != null) && (attacker != this))
		{
			attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		
		// 5182 = Blessing of protection,
		if ((attacker != null) && attacker.isPlayer() && (Math.abs(attacker.getLevel() - getLevel()) > 10))
		{
			if ((attacker.getKarma() > 0) && (getEffectList().getEffectsBySkillId(5182) != null) && !isInZone(ZoneType.SIEGE))
			{
				return;
			}
			if ((getKarma() > 0) && (attacker.getEffectList().getEffectsBySkillId(5182) != null) && !attacker.isInZone(ZoneType.SIEGE))
			{
				return;
			}
		}
		
		i = _currentMp - i;
		
		if (i < 0)
		{
			i = 0;
		}
		
		setCurrentMp(i);
		
		if ((attacker != null) && (attacker != this))
		{
			startAttackStanceTask();
		}
	}
	
	public double relativeSpeed(GameObject target)
	{
		return getMoveSpeed() - (target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading())));
	}
	
	public void removeAllSkills()
	{
		for (Skill s : getAllSkillsArray())
		{
			removeSkill(s);
		}
	}
	
	public void removeBlockStats(List<Stats> stats)
	{
		if (_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if (_blockedStats.isEmpty())
			{
				_blockedStats = null;
			}
		}
	}
	
	public Skill removeSkill(Skill skill)
	{
		if (skill == null)
		{
			return null;
		}
		return removeSkillById(skill.getId());
	}
	
	public Skill removeSkillById(int id)
	{
		// Remove the skill from the L2Character _skills
		Skill oldSkill = _skills.remove(id);
		
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			removeTriggers(oldSkill);
			removeStatsOwner(oldSkill);
			if (Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler() || oldSkill.getName().startsWith("Item Skill")))
			{
				List<Effect> effects = getEffectList().getEffectsBySkill(oldSkill);
				if (effects != null)
				{
					for (Effect effect : effects)
					{
						effect.exit();
					}
				}
				
				Summon pet = getPet();
				if (pet != null)
				{
					effects = pet.getEffectList().getEffectsBySkill(oldSkill);
					if (effects != null)
					{
						for (Effect effect : effects)
						{
							effect.exit();
						}
					}
				}
			}
		}
		
		return oldSkill;
	}
	
	public void addTriggers(StatTemplate f)
	{
		if (f.getTriggerList().isEmpty())
		{
			return;
		}
		
		for (TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}
	
	public void addTrigger(TriggerInfo t)
	{
		if (_triggers == null)
		{
			_triggers = new ConcurrentHashMap<>();
		}
		
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if (hs == null)
		{
			hs = new CopyOnWriteArraySet<>();
			_triggers.put(t.getType(), hs);
		}
		
		hs.add(t);
		
		if (t.getType() == TriggerType.ADD)
		{
			useTriggerSkill(this, null, t, null, 0);
		}
	}
	
	public void removeTriggers(StatTemplate f)
	{
		if ((_triggers == null) || f.getTriggerList().isEmpty())
		{
			return;
		}
		
		for (TriggerInfo t : f.getTriggerList())
		{
			removeTrigger(t);
		}
	}
	
	public void removeTrigger(TriggerInfo t)
	{
		if (_triggers == null)
		{
			return;
		}
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if (hs == null)
		{
			return;
		}
		hs.remove(t);
	}
	
	public void sendActionFailed()
	{
		sendPacket(ActionFail.STATIC);
	}
	
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	@Override
	public CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new CharacterAI(this);
				}
			}
		}
		
		return _ai;
	}
	
	public void setAI(CharacterAI newAI)
	{
		if (newAI == null)
		{
			return;
		}
		
		CharacterAI oldAI = _ai;
		
		synchronized (this)
		{
			_ai = newAI;
		}
		
		if (oldAI != null)
		{
			if (oldAI.isActive())
			{
				oldAI.stopAITask();
				newAI.startAITask();
				newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
		}
	}
	
	public final void setCurrentHp(double newHp, boolean canRessurect, boolean sendInfo)
	{
		int maxHp = getMaxHp();
		
		newHp = Math.min(maxHp, Math.max(0, newHp));
		
		if (_currentHp == newHp)
		{
			return;
		}
		
		if ((newHp >= 0.5) && isDead() && !canRessurect)
		{
			return;
		}
		
		double hpStart = _currentHp;
		
		_currentHp = newHp;
		
		if (_isDead.compareAndSet(true, false))
		{
			onRevive();
		}
		
		checkHpMessages(hpStart, _currentHp);
		
		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		
		if (_currentHp < maxHp)
		{
			startRegeneration();
		}
	}
	
	public final void setCurrentHp(double newHp, boolean canRessurect)
	{
		setCurrentHp(newHp, canRessurect, true);
	}
	
	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();
		
		newMp = Math.min(maxMp, Math.max(0, newMp));
		
		if (_currentMp == newMp)
		{
			return;
		}
		
		if ((newMp >= 0.5) && isDead())
		{
			return;
		}
		
		_currentMp = newMp;
		
		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		
		if (_currentMp < maxMp)
		{
			startRegeneration();
		}
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if (!isPlayer())
		{
			return;
		}
		
		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0, newCp));
		
		if (_currentCp == newCp)
		{
			return;
		}
		
		if ((newCp >= 0.5) && isDead())
		{
			return;
		}
		
		_currentCp = newCp;
		
		if (sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		
		if (_currentCp < maxCp)
		{
			startRegeneration();
		}
	}
	
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		
		newHp = Math.min(maxHp, Math.max(0, newHp));
		newMp = Math.min(maxMp, Math.max(0, newMp));
		
		if ((_currentHp == newHp) && (_currentMp == newMp))
		{
			return;
		}
		
		if ((newHp >= 0.5) && isDead() && !canRessurect)
		{
			return;
		}
		
		double hpStart = _currentHp;
		
		_currentHp = newHp;
		_currentMp = newMp;
		
		if (_isDead.compareAndSet(true, false))
		{
			onRevive();
		}
		
		checkHpMessages(hpStart, _currentHp);
		
		broadcastStatusUpdate();
		sendChanges();
		
		if ((_currentHp < maxHp) || (_currentMp < maxMp))
		{
			startRegeneration();
		}
	}
	
	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}
	
	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}
	
	@Override
	public final int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public final void setHeading(Creature target, boolean toChar)
	{
		if ((target == null) || (target == this))
		{
			return;
		}
		setHeading(new Location(target.getX(), target.getY(), target.getZ()), toChar); // ÃŽ Ã‚Â½ÃŽ Ã‚Âµ ÃŽ ÃŽÅ’ÃŽ Ã‚ÂµÃŽ Ã‚Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ ÃŽ Ã‚Â½ÃŽ Ã‚Â° getLoc() ÃŽ ÃŽË†ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Â¡ÃŽ Ã‚Âµ ÃŽ Ã‚Â±ÃŽÂ¡Ã†â€™ÃŽ ÃŽâ€žÃŽ Ã‚ÂµÃŽÂ¡Ã¢â‚¬Å¡ ÃŽÂ¡Ã¢â‚¬ ÃŽ ÃŽË†ÃŽ ÃŽÅ ÃŽ Ã‚Â» ÃŽ ÃŽË†ÃŽ Ã‚Â· ÃŽ Ã‚Â·ÃŽ Ã‚Â° getHeading() ÃŽ Ã‚Â²ÃŽ Ã‚Â½ÃŽÂ¡Ã†â€™ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¢â€šÂ¬ÃŽ ÃŽË† getLoc()
	}
	
	public final void setHeading(Location target, boolean toChar)
	{
		setHeading((int) (Math.atan2(getY() - target.y, getX() - target.x) * HEADINGS_IN_PI) + (toChar ? 32768 : 0));
	}
	
	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting.compareAndSet(!value, value);
	}
	
	public final void setName(String name)
	{
		_name = name;
	}
	
	public Creature getCastingTarget()
	{
		return _castingTarget.get();
	}
	
	public void setCastingTarget(Creature target)
	{
		if (target == null)
		{
			_castingTarget = HardReferences.emptyRef();
		}
		else
		{
			_castingTarget = target.getRef();
		}
	}
	
	public void setSkillMastery(Integer skill, int mastery)
	{
		if (_skillMastery == null)
		{
			_skillMastery = new HashMap<>();
		}
		_skillMastery.put(skill, mastery);
	}
	
	public void setAggressionTarget(Creature target)
	{
		if (target == null)
		{
			_aggressionTarget = HardReferences.emptyRef();
		}
		else
		{
			_aggressionTarget = target.getRef();
		}
	}
	
	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}
	
	public void setWalking()
	{
		if (_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	public final void setRunning()
	{
		if (!_running)
		{
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	public void startAbnormalEffect(AbnormalEffect ae)
	{
		if (ae == AbnormalEffect.NULL)
		{
			_abnormalEffects = AbnormalEffect.NULL.getMask();
			_abnormalEffects2 = AbnormalEffect.NULL.getMask();
			_abnormalEffects3 = AbnormalEffect.NULL.getMask();
		}
		else if (ae.isSpecial())
		{
			_abnormalEffects2 |= ae.getMask();
		}
		else if (ae.isEvent())
		{
			_abnormalEffects3 |= ae.getMask();
		}
		else
		{
			_abnormalEffects |= ae.getMask();
		}
		sendChanges();
	}
	
	protected void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}
	
	public void startAttackStanceTask0()
	{
		
		if (isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}
		
		_stanceEndTime = System.currentTimeMillis() + 15000L;
		
		broadcastPacket(new AutoAttackStart(getObjectId()));
		
		final Future<?> task = _stanceTask;
		if (task != null)
		{
			task.cancel(false);
		}
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? _stanceTaskRunnable = new AttackStanceTask() : _stanceTaskRunnable, 1000L, 1000L);
	}
	
	/**
	 * ÃŽ Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ Ã‚Â°ÃŽ Ã‚Â²ÃŽ Ã‚Â»ÃŽ ÃŽË†ÃŽ Ã‚Â²ÃŽ Ã‚Â°ÃŽ Ã‚ÂµÃŽ ÃŽÅ’ ÃŽ Ã‚Â·ÃŽ Ã‚Â°ÃŽ ÃŽâ€žÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬Â¡ÃŽÂ¡Ã†â€™ ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ ÃŽË†ÃŽ ÃŽÅ’ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬ ÃŽ ÃŽË†ÃŽ ÃŽË† ÃŽ Ã‚Â±ÃŽ ÃŽÅ½ÃŽ Ã‚ÂµÃŽ Ã‚Â²ÃŽ ÃŽÅ½ÃŽ ÃŽâ€° ÃŽ ÃŽï¿½ÃŽ ÃŽÅ½ÃŽ Ã‚Â·ÃŽÂ¡Ã¢â‚¬Â¹.
	 */
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;
		
		final Future<?> task = _stanceTask;
		if (task != null)
		{
			task.cancel(false);
			_stanceTask = null;
			
			broadcastPacket(new AutoAttackStop(getObjectId()));
		}
	}
	
	private class AttackStanceTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if (!isInCombat())
			{
				stopAttackStanceTask();
			}
		}
	}
	
	/**
	 * ÃŽ Ã¯Â¿Â½ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ Ã‚Â°ÃŽ Ã‚Â½ÃŽ ÃŽÅ½ÃŽ Ã‚Â²ÃŽ ÃŽË†ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚ÂµÃŽ Ã‚Â³ÃŽ Ã‚ÂµÃŽ Ã‚Â½ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬ ÃŽ ÃŽË†ÃŽÂ¡Ã¯Â¿Â½
	 */
	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if (_isRegenerating)
			{
				_isRegenerating = false;
				
				if (_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}
	
	/**
	 * ÃŽ Ã¢â‚¬â€�ÃŽ Ã‚Â°ÃŽ ÃŽï¿½ÃŽÂ¡Ã†â€™ÃŽÂ¡Ã¯Â¿Â½ÃŽÂ¡Ã¢â‚¬Å¡ÃŽ ÃŽË†ÃŽÂ¡Ã¢â‚¬Å¡ÃŽÂ¡Ã¯Â¿Â½ ÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚ÂµÃŽ Ã‚Â³ÃŽ Ã‚ÂµÃŽ Ã‚Â½ÃŽ Ã‚ÂµÃŽÂ¡Ã¢â€šÂ¬ÃŽ Ã‚Â°ÃŽÂ¡Ã¢â‚¬ ÃŽ ÃŽË†ÃŽÂ¡Ã¯Â¿Â½
	 */
	protected void startRegeneration()
	{
		if (!isVisible() || isDead() || (getRegenTick() == 0L))
		{
			return;
		}
		
		if (_isRegenerating)
		{
			return;
		}
		
		regenLock.lock();
		try
		{
			if (!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? _regenTaskRunnable = new RegenTask() : _regenTaskRunnable, 0, getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}
	
	public long getRegenTick()
	{
		return 3000L;
	}
	
	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if (isAlikeDead() || (getRegenTick() == 0L))
			{
				return;
			}
			
			double hpStart = _currentHp;
			
			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;
			
			double addHp = 0.;
			double addMp = 0.;
			
			regenLock.lock();
			try
			{
				if (_currentHp < maxHp)
				{
					addHp += Formulas.calcHpRegen(Creature.this);
				}
				
				if (_currentMp < maxMp)
				{
					addMp += Formulas.calcMpRegen(Creature.this);
				}
				
				// Added regen bonus when character is sitting
				if (isPlayer() && Config.REGEN_SIT_WAIT)
				{
					Player pl = (Player) Creature.this;
					if (pl.isSitting())
					{
						pl.updateWaitSitTime();
						if (pl.getWaitSitTime() > 5)
						{
							addHp += pl.getWaitSitTime();
							addMp += pl.getWaitSitTime();
						}
					}
				}
				else if (isRaid())
				{
			          double mul_hp = NpcAndRaidMod.RaidRegenHP(Creature.this.getLevel());
			          double mul_mp = NpcAndRaidMod.RaidRegenMP(Creature.this.getLevel());
			          
					//addHp *= Config.RATE_RAID_REGEN;
					//addMp *= Config.RATE_RAID_REGEN;
			          addHp *= mul_hp;
			          addMp *= mul_mp;
				}
				
				_currentHp += Math.max(0, Math.min(addHp, ((calcStat(Stats.HP_LIMIT, null, null) * maxHp) / 100.) - _currentHp));
				_currentMp += Math.max(0, Math.min(addMp, ((calcStat(Stats.MP_LIMIT, null, null) * maxMp) / 100.) - _currentMp));
				
				_currentHp = Math.min(maxHp, _currentHp);
				_currentMp = Math.min(maxMp, _currentMp);
				
				
				if (isPlayer())
				{
					_currentCp += Math.max(0, Math.min(Formulas.calcCpRegen(Creature.this), ((calcStat(Stats.CP_LIMIT, null, null) * maxCp) / 100.) - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
				}
				
				if ((_currentHp == maxHp) && (_currentMp == maxMp) && (_currentCp == maxCp))
				{
					stopRegeneration();
				}
			}
			finally
			{
				regenLock.unlock();
			}
			
			broadcastStatusUpdate();
			sendChanges();
			
			checkHpMessages(hpStart, _currentHp);
		}
	}
	
	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		if (ae.isSpecial())
		{
			_abnormalEffects2 &= ~ae.getMask();
		}
		if (ae.isEvent())
		{
			_abnormalEffects3 &= ~ae.getMask();
		}
		else
		{
			_abnormalEffects &= ~ae.getMask();
		}
		sendChanges();
	}
	
	public void block()
	{
		_blocked = true;
	}
	
	public void unblock()
	{
		_blocked = false;
	}
	
	public boolean startConfused()
	{
		return _confused.getAndSet(true);
	}
	
	public boolean stopConfused()
	{
		return _confused.setAndGet(false);
	}
	
	public boolean startFear()
	{
		return _afraid.getAndSet(true);
	}
	
	public boolean stopFear()
	{
		return _afraid.setAndGet(false);
	}
	
	public boolean startMuted()
	{
		return _muted.getAndSet(true);
	}
	
	public boolean stopMuted()
	{
		return _muted.setAndGet(false);
	}
	
	public boolean startPMuted()
	{
		return _pmuted.getAndSet(true);
	}
	
	public boolean stopPMuted()
	{
		return _pmuted.setAndGet(false);
	}
	
	public boolean startAMuted()
	{
		return _amuted.getAndSet(true);
	}
	
	public boolean stopAMuted()
	{
		return _amuted.setAndGet(false);
	}
	
	public boolean startRooted()
	{
		return _rooted.getAndSet(true);
	}
	
	public boolean stopRooted()
	{
		return _rooted.setAndGet(false);
	}
	
	public boolean startSleeping()
	{
		return _sleeping.getAndSet(true);
	}
	
	public boolean stopSleeping()
	{
		return _sleeping.setAndGet(false);
	}
	
	public boolean startStunning()
	{
		return _stunned.getAndSet(true);
	}
	
	public boolean stopStunning()
	{
		return _stunned.setAndGet(false);
	}
	
	public boolean startParalyzed()
	{
		return _paralyzed.getAndSet(true);
	}
	
	public boolean stopParalyzed()
	{
		return _paralyzed.setAndGet(false);
	}
	
	public boolean startImmobilized()
	{
		return _immobilized.getAndSet(true);
	}
	
	public boolean stopImmobilized()
	{
		return _immobilized.setAndGet(false);
	}
	
	public boolean startHealBlocked()
	{
		return _healBlocked.getAndSet(true);
	}
	
	public boolean stopHealBlocked()
	{
		return _healBlocked.setAndGet(false);
	}
	
	public boolean startDamageBlocked()
	{
		return _damageBlocked.getAndSet(true);
	}
	
	public boolean stopDamageBlocked()
	{
		return _damageBlocked.setAndGet(false);
	}
	
	public boolean startBuffImmunity()
	{
		return _buffImmunity.getAndSet(true);
	}
	
	public boolean stopBuffImmunity()
	{
		return _buffImmunity.setAndGet(false);
	}
	
	public boolean startDebuffImmunity()
	{
		return _debuffImmunity.getAndSet(true);
	}
	
	public boolean stopDebuffImmunity()
	{
		return _debuffImmunity.setAndGet(false);
	}
	
	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}
	
	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}
	
	public boolean startWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(true);
	}
	
	public boolean stopWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(false);
	}
	
	public boolean startFrozen()
	{
		return _frozen.getAndSet(true);
	}
	
	public boolean stopFrozen()
	{
		return _frozen.setAndGet(false);
	}
	
	public void setFakeDeath(boolean value)
	{
		_fakeDeath = value;
	}
	
	public void breakFakeDeath()
	{
		getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
	}
	
	public void setMeditated(boolean value)
	{
		_meditated = value;
	}
	
	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}
	
	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}
	
	public void setIsInvul(boolean value)
	{
		_isInvul = value;
	}
	
	public void setIsImmortal(boolean value)
	{
		_isImmortal = value;
	}
	
	public void setIsRealTargeted(boolean value)
	{
		_isRealTargeted = value;
	}
	
	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}
	
	public boolean isConfused()
	{
		return _confused.get();
	}
	
	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}
	
	public boolean isAfraid()
	{
		return _afraid.get();
	}
	
	public boolean isBlocked()
	{
		return _blocked;
	}
	
	public boolean isMuted(Skill skill)
	{
		if ((skill == null) || skill.isNotAffectedByMute())
		{
			return false;
		}
		return (isMMuted() && skill.isMagic()) || (isPMuted() && !skill.isMagic());
	}
	
	public boolean isPMuted()
	{
		return _pmuted.get();
	}
	
	public boolean isMMuted()
	{
		return _muted.get();
	}
	
	public boolean isAMuted()
	{
		return _amuted.get();
	}
	
	public boolean isRooted()
	{
		return _rooted.get();
	}
	
	public boolean isSleeping()
	{
		return _sleeping.get();
	}
	
	public boolean isStunned()
	{
		return _stunned.get();
	}
	
	public boolean isMeditated()
	{
		return _meditated;
	}
	
	public boolean isWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.get();
	}
	
	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}
	
	public boolean isFrozen()
	{
		return _frozen.get();
	}
	
	public boolean isImmobilized()
	{
		return _immobilized.get() || (getRunSpeed() < 1);
	}
	
	public boolean isHealBlocked()
	{
		return isAlikeDead() || _healBlocked.get();
	}
	
	public boolean isDamageBlocked()
	{
		return isInvul() || _damageBlocked.get();
	}
	
	public boolean isCastingNow()
	{
		return _skillTask != null;
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}
	
	public boolean isMovementDisabled()
	{
		if (isCastingNow()) // Sometimes you can cast and move at the same time... depends on the skill.
		{
			final Skill skill = getCastingSkill();
			if ((skill != null) && skill.stopActor())
			{
				return true;
			}
		}
		
		return (isBlocked() && !isUsingWyvernTeleport()) || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isFrozen();
	}
	
	public boolean isActionsDisabled()
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
	}
	
	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}
	
	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}
	
	public void checkAndRemoveInvisible()
	{
		InvisibleType invisibleType = getInvisibleType();
		if (invisibleType == InvisibleType.EFFECT)
		{
			getEffectList().stopEffects(EffectType.Invisible);
		}
	}
	
	public void setInvisibleType(InvisibleType vis)
	{
		_invisibleType = vis;
	}
	
	@Override
	public InvisibleType getInvisibleType()
	{
		return _invisibleType;
	}
	
	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}
	
	public void teleToLocation(Location loc, int refId)
	{
		teleToLocation(loc.x, loc.y, loc.z, refId);
	}
	
	public void teleToLocation(Location loc, Reflection r)
	{
		teleToLocation(loc.x, loc.y, loc.z, r);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}
	
	public void teleToLocation(int x, int y, int z, int refId)
	{
		Reflection r = ReflectionManager.getInstance().get(refId);
		if (r == null)
		{
			return;
		}
		teleToLocation(x, y, z, r);
	}
	
	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if (isFakeDeath())
		{
			breakFakeDeath();
		}
		
		if (isTeleporting())
		{
			return;
		}
		
		abortCast(true, false);
		
		if (!isLockedTarget())
		{
			setTarget(null);
		}
		
		stopMove();
		
		if (!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
		{
			z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());
		}
		
		if (isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true))
		{
			Player player = (Player) this;
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
				x = newCoords.x;
				y = newCoords.y;
				z = newCoords.z;
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
		}
		
		if (isPlayer())
		{
			Player player = (Player) this;
			
			player.getListeners().onTeleport(x, y, z, r);
			
			// Synerge - Send a teleport event to the tutorial for the player
			Quest q = QuestManager.getQuest(255);
			if (q != null)
			{
				player.processQuestEvent(q.getName(), "CE42", null);
			}
			
			setIsTeleporting(true);
			
			decayMe();
			setXYZ(x, y, z);
			
			setReflection(r);
			
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);
			
			player.sendPacket(new TeleportToLocation(player, x, y, z));
			
			if (player.getPet() != null)
			{
				player.getPet().teleportToOwner();
			}
			
			if (player.isOnline() && (player.getClient() == null))
			{
				onTeleported();
			}
		}
		else
		{
			setXYZ(x, y, z);
			
			setReflection(r);
			
			broadcastPacket(new TeleportToLocation(this, x, y, z));
			onTeleported();
		}
	}
	
	public boolean onTeleported()
	{
		return _isTeleporting.compareAndSet(true, false);
	}
	
	public void sendMessage(CustomMessage message)
	{
		
	}
	
	public void sendCustomMessage(String address, Object... args)
	{
		
	}
	
	public void sendChatMessage(int objectId, int messageType, String charName, String text)
	{
		
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}
	
	@Override
	public double getColRadius()
	{
		return getTemplate().getCollisionRadius();
	}
	
	@Override
	public double getColHeight()
	{
		return getTemplate().getCollisionHeight();
	}
	
	public EffectList getEffectList()
	{
		if (_effectList == null)
		{
			synchronized (this)
			{
				if (_effectList == null)
				{
					_effectList = new EffectList(this);
				}
			}
		}
		
		return _effectList;
	}
	
	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 0xFFFF;
		
		// MonsterInstance leader;
		NpcInstance leader;
		// if (isRaid() || (isMinion() && ((leader = ((MinionInstance) this).getLeader()) != null) && leader.isRaid()))
		if (isRaid() || (isMinion() && ((leader = ((NpcInstance) this).getLeader()) != null) && leader.isRaid()))
		{
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		}
		else if (isNpc())
		{
			int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
			if (max_level_diff != -1000)
			{
				max_attacker_level = getLevel() + max_level_diff;
			}
		}
		
		if (attacker.getLevel() > max_attacker_level)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void onDelete()
	{
		GameObjectsStorage.remove(_storedId);
		
		getEffectList().stopAllEffects();
		
		super.onDelete();
		
		getListeners().onDeleted();
	}
	
	// ---------------------------- Not Implemented -------------------------------
	
	public void addExpAndSp(long exp, long sp)
	{
	}
	
	public void broadcastCharInfo()
	{
	}
	
	public void checkHpMessages(double currentHp, double newHp)
	{
	}
	
	public boolean checkPvP(Creature target, Skill skill)
	{
		return false;
	}
	
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		return true;
	}
	
	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}
	
	public boolean isFearImmune()
	{
		return false;
	}
	
	public boolean isLethalImmune()
	{
		if (ServerVariables.getString("GmHunter", "off").equals("on") && isPlayer() && getPlayer().isGM())
		{
			return true;
		}
		else if (Config.FORMULA_LETHAL_MAX_HP > 0)
		{
			return getMaxHp() >= Config.FORMULA_LETHAL_MAX_HP;
		}
		else
		{
			return false;
		}
	}
	
	public boolean getChargedSoulShot()
	{
		return false;
	}
	
	public int getChargedSpiritShot()
	{
		return 0;
	}
	
	public int getIncreasedForce()
	{
		return 0;
	}
	
	public int getConsumedSouls()
	{
		return 0;
	}
	
	public int getAgathionEnergy()
	{
		return 0;
	}
	
	public void setAgathionEnergy(int val)
	{
		//
	}
	
	public int getKarma()
	{
		return 0;
	}
	
	public double getLevelMod()
	{
		return 1;
	}
	
	public int getNpcId()
	{
		return 0;
	}
	
	public Summon getPet()
	{
		return null;
	}
	
	public int getPvpFlag()
	{
		return 0;
	}
	
	public int getTeamEvents()
	{
		return 0;
	}
	
	public int getPvPTeam()
	{
		return 0;
	}
	
	public void setTeam(TeamType t)
	{
		_team = t;
		sendChanges();
	}
	
	public TeamType getTeam()
	{
		return _team;
	}
	
	public boolean isUndead()
	{
		return false;
	}
	
	public boolean isParalyzeImmune()
	{
		return false;
	}
	
	public void reduceArrowCount()
	{
	}
	
	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}
	
	public void sendMessage(String message)
	{
	}
	
	public void sendMessage(Object message)
	{
		sendMessage(message.toString());
	}
	
	public void sendHtml(String file)
	{
	}
	
	public void sendHtml(NpcInstance npc, String file)
	{
	}
	
	public void sendHtmlMessage(String message)
	{
	}
	
	public void sendPacket(IStaticPacket mov)
	{
	}
	
	public void sendPacket(IStaticPacket... mov)
	{
	}
	
	public void sendPacket(List<? extends IStaticPacket> mov)
	{
	}
	
	public void setIncreasedForce(int i)
	{
	}
	
	public void setConsumedSouls(int i, NpcInstance monster)
	{
	}
	
	public void startPvPFlag(Creature target)
	{
	}
	
	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}
	
	public void updateEffectIcons()
	{
	}
	
	/*
	 * refreshHpMpCp getMaxHp getMaxHp getMaxHp
	 */
	protected void refreshHpMpCp()
	{
		final int maxHp = getMaxHp();
		final int maxMp = getMaxHp();
		final int maxCp = isPlayer() ? getMaxCp() : 0;
		
		if (_currentHp > maxHp)
		{
			setCurrentHp(maxHp, false);
		}
		if (_currentMp > maxMp)
		{
			setCurrentMp(maxMp, false);
		}
		if (_currentCp > maxCp)
		{
			setCurrentCp(maxCp, false);
		}
		
		if ((_currentHp < maxHp) || (_currentMp < maxMp) || (_currentCp < maxCp))
		{
			startRegeneration();
		}
	}
	
	public void updateStats()
	{
		// Support for propper stats update for skills like Servitor Share
		if (isPlayer() && (getPlayer().getPet() != null))
		{
			getPlayer().getPet().updateStats();
		}
		
		refreshHpMpCp();
		sendChanges();
	}
	
	public void setOverhitAttacker(Creature attacker)
	{
	}
	
	public void setOverhitDamage(double damage)
	{
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return false;
	}
	
	public boolean isHero()
	{
		return false;
	}
	
	public Clan getClan()
	{
		return null;
	}
	
	public double getRateAdena()
	{
		return getRateAdena(false);
	}
	
	public double getRateAdena(boolean excludeParty)
	{
		return 1.;
	}
	
	public double getRateItems()
	{
		return getRateItems(false);
	}
	
	public double getRateItems(boolean excludeParty)
	{
		return 1.;
	}
	
	public double getRateExp()
	{
		return getRateExp(false);
	}
	
	public double getRateExp(boolean excludeParty)
	{
		return 1.;
	}
	
	public double getRateSp()
	{
		return getRateSp(false);
	}
	
	public double getRateSp(boolean excludeParty)
	{
		return 1.;
	}
	
	public double getRateSpoil()
	{
		return getRateSpoil(false);
	}
	
	public double getRateSpoil(boolean excludeParty)
	{
		return 1.;
	}
	
	public double getRateSiege()
	{
		return 1.0;
	}
	
	public double getRaidRateItems()
	{
		return getRaidRateItems(false);
	}
	
	public double getRaidRateItems(boolean excludeParty)
	{
		return 1.;
	}
	
	public int getFormId()
	{
		return 0;
	}
	
	public boolean isNameAbove()
	{
		return true;
	}
	
	@Override
	public void setLoc(Location loc)
	{
		setXYZ(loc.x, loc.y, loc.z);
	}
	
	public void setLoc(Location loc, boolean MoveTask)
	{
		setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}
	
	@Override
	public void setXYZ(int x, int y, int z)
	{
		setXYZ(x, y, z, false);
	}
	
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if (!MoveTask)
		{
			stopMove();
		}
		
		moveLock.lock();
		try
		{
			super.setXYZ(x, y, z);
		}
		finally
		{
			moveLock.unlock();
		}
		
		updateZones();
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		
		updateStats();
		updateZones();
	}
	
	@Override
	public void spawnMe(Location loc)
	{
		if (loc.h > 0)
		{
			setHeading(loc.h);
		}
		super.spawnMe(loc);
	}
	
	@Override
	protected void onDespawn()
	{
		if (!isLockedTarget())
		{
			setTarget(null);
		}
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		
		updateZones();
		clearStatusListeners();
		
		super.onDespawn();
	}
	
	public final void doDecay()
	{
		if (!isDead())
		{
			return;
		}
		
		onDecay();
	}
	
	protected void onDecay()
	{
		decayMe();
	}
	
	public void validateLocation(int broadcast)
	{
		L2GameServerPacket sp = new ValidateLocation(this);
		if (broadcast == 0)
		{
			sendPacket(sp);
		}
		else if (broadcast == 1)
		{
			broadcastPacket(sp);
		}
		else
		{
			broadcastPacketToOthers(sp);
		}
	}
	
	// Function for deactivated skills of the character (if skill is inactive, it does not give stats and has a gray icon).
	private final TIntHashSet _unActiveSkills = new TIntHashSet();
	
	public void addUnActiveSkill(Skill skill)
	{
		if ((skill == null) || isUnActiveSkill(skill.getId()))
		{
			return;
		}
		
		removeStatsOwner(skill);
		removeTriggers(skill);
		
		_unActiveSkills.add(skill.getId());
	}
	
	public void removeUnActiveSkill(Skill skill)
	{
		if ((skill == null) || !isUnActiveSkill(skill.getId()))
		{
			return;
		}
		
		addStatFuncs(skill.getStatFuncs());
		addTriggers(skill);
		
		_unActiveSkills.remove(skill.getId());
	}
	
	public boolean isUnActiveSkill(int id)
	{
		return _unActiveSkills.contains(id);
	}
	
	public void removeInvisibleEffect()
	{
		if (getEffectList() == null)
		{
			return;
		}
		
		Effect effect = getEffectList().getEffectByType(EffectType.Invisible);
		if (effect != null)
		{
			effect.exit();
		}
	}
	
	public abstract int getLevel();
	
	public abstract ItemInstance getActiveWeaponInstance();
	
	public abstract WeaponTemplate getActiveWeaponItem();
	
	public abstract ItemInstance getSecondaryWeaponInstance();
	
	public abstract WeaponTemplate getSecondaryWeaponItem();
	
	public CharListenerList getListeners()
	{
		if (listeners == null)
		{
			synchronized (this)
			{
				if (listeners == null)
				{
					listeners = new CharListenerList(this);
				}
			}
		}
		return listeners;
	}
	
	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}
	
	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}
	
	public CharPermissionList getPermissions()
	{
		if (_permissions == null)
		{
			synchronized (this)
			{
				if (_permissions == null)
				{
					_permissions = new CharPermissionList(this);
				}
			}
		}
		return _permissions;
	}
	
	public <T extends Permission<Creature>> boolean addPermission(T permission)
	{
		return getPermissions().add(permission);
	}
	
	public <T extends Permission<Creature>> boolean removePermission(T permission)
	{
		return getPermissions().remove(permission);
	}
	
	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if (_statsRecorder == null)
		{
			synchronized (this)
			{
				if (_statsRecorder == null)
				{
					_statsRecorder = new CharStatsChangeRecorder<>(this);
				}
			}
		}
		
		return _statsRecorder;
	}
	
	@Override
	public boolean isCreature()
	{
		return true;
	}
	
	public void setOverrideCond(long mask)
	{
		_overrideConditions = mask;
	}
	
	public void addOverrideCond(PcCondOverride... excs)
	{
		addOverrideCond(0, excs);
	}
	
	public void addOverrideCond(int durationInSecs, PcCondOverride... excs)
	{
		for (PcCondOverride exc : excs)
		{
			_overrideConditions |= exc.getMask();
			
			if (isPlayer())
			{
				switch (exc)
				{
					case MAX_STATS_VALUE:
						updateStats();
						break;
					case SKILL_REUSE_CONDITIONS:
						getPlayer().resetReuse();
						sendPacket(new SkillCoolTime(getPlayer()));
						break;
					case SEE_ALL_PLAYERS:
						for (Player plr : World.getAroundPlayers(this))
						{
							if (plr.isInvisible())
							{
								sendPacket(getPlayer().addVisibleObject(plr));
							}
						}
						break;
					case HERO_AURA_CONDITIONS:
						broadcastCharInfo();
					default:
						break;
				}
			}
		}
		
		if (isPlayer())
		{
			getPlayer().setVar("OVRD_COND_" + getObjectId(), _overrideConditions, durationInSecs);
		}
	}
	
	public void removeOverridedCond(PcCondOverride... excs)
	{
		for (PcCondOverride exc : excs)
		{
			_overrideConditions &= ~exc.getMask();
			
			if (isPlayer())
			{
				switch (exc)
				{
					case MAX_STATS_VALUE:
						updateStats();
						break;
					case SEE_ALL_PLAYERS:
						for (Player plr : World.getAroundPlayers(this))
						{
							if (plr.isInvisible())
							{
								sendPacket(getPlayer().removeVisibleObject(plr, null));
							}
						}
						break;
					case HERO_AURA_CONDITIONS:
						broadcastCharInfo();
					default:
						break;
				}
			}
		}
		
		if (isPlayer())
		{
			getPlayer().setVar("OVRD_COND_" + getObjectId(), _overrideConditions, getPlayer().getVarTimeToExpire("OVRD_COND_" + getObjectId()));
		}
	}
	
	public boolean canOverrideCond(PcCondOverride excs)
	{
		if ((getPlayer() != null) && getPlayer().getVarB("OVRD_COND_" + getObjectId()) && !getPlayer().isGM())
		{
			getPlayer().unsetVar("OVRD_COND_" + getObjectId());
			return false;
		}
		
		return (_overrideConditions & excs.getMask()) == excs.getMask();
	}
	
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if (miss && target.isPlayer() && !target.isDamageBlocked())
		{
			target.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));
		}
		
		if (Config.ENABLE_DAM_ON_SCREEN)
		{
			if (isPlayer() && getPlayer().isDmgOnScreenEnable())
			{
				sendPacket(new DamageTextPacket(target.getObjectId(), damage, crit, miss, shld, magic, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_ATTACKER, ""));
			}
			if (target.isPlayer() && target.getPlayer().isDmgOnScreenEnable())
			{
				target.sendPacket(new DamageTextPacket(target.getObjectId(), damage, crit, miss, shld, magic, Config.DAM_ON_SCREEN_FONT, Config.DAM_ON_SCREEN_FONT_COLOR_TARGET, ""));
			}
		}
	}
	
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		//
	}
	
	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.values();
	}
	
	public TimeStamp getSkillReuse(Skill skill)
	{
		return _skillReuses.get(skill.hashCode());
	}
	
	int _color = 0x00;
	
	public int getColor()
	{
		return _color;
	}
	
	public void setColor(int _colorEffect2)
	{
		_color = _colorEffect2;
	}
	
	private ChampionTemplateTable.ChampionTemplate _championTemplate = null;
	
	public void setChampionTemplate(ChampionTemplateTable.ChampionTemplate championTemplate)
	{
		_championTemplate = championTemplate;
	}
	
	public ChampionTemplateTable.ChampionTemplate getChampionTemplate()
	{
		return _championTemplate;
	}
	
	private final boolean _isLeader = false;
	
	public boolean isLeader()
	{
		return _isLeader;
	}
	
	public boolean canShowLevelInTitle()
	{
		return !(getName().equals("Chest"));
	}
	
	private boolean _isUsingWyvernTeleport = false;
	
	public void setWyvernTeleport(boolean using)
	{
		_isUsingWyvernTeleport = using;
	}
	
	public boolean isUsingWyvernTeleport()
	{
		return _isUsingWyvernTeleport;
	}
	
	public void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
	}
	
	public void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
	}
	
	public boolean checkInteractionDistance(GameObject target)
	{
		return getRealDistance3D(target) < INTERACTION_DISTANCE;
	}
	
	public void setFlyLoc(Location loc)
	{
		_flyLoc = loc;
	}
	
	private boolean _isHide;
	
	public void setHide(boolean hide)
	{
		_isHide = hide;
	}
	
	public boolean isHide()
	{
		return _isHide;
	}
	
	private long _animationEndTime40;
	
	public long getAnimationEndTime60()
	{
		return _animationEndTime40;
	}
	
	public boolean isMageClass()
	{
		return getTemplate().getBaseMAtk() > 3.0D;
	}
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _template.getBaseHpMax(), null, null);
	}
	public final int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, _template.getBaseCpMax(), null, null);
	}
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _template.getBaseMpMax(), null, null);
	}
	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, _template.getBaseINT(), null, null);
	}
	public int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, _template.getBaseCON(), null, null);
	}
	public int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, _template.getBaseMEN(), null, null);
	}
	public int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, _template.getBaseDEX(), null, null);
	}
	
	public double getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1.0D / _template.getBaseRunSpd();
	}
	
	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * getPAtkSpd() / getTemplate().getBasePAtkSpd();
	}
	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, _template.getBasePAtkSpd(), null, null);
	}
	public int getMAtkSpd()
	{
		return (int) (calcStat(Stats.MAGIC_ATTACK_SPEED, _template.getBaseMAtkSpd(), null, null));
	}
	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), null, null);
	}
	public final int getMagicalAttackRange(Skill skill)
	{
		if (skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getTemplate().getBaseAtkRange();
	}
	public int getMDef(Creature target, Skill skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.getBaseMDef(), target, skill), 1);
	}
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _template.getBasePDef(), target, null);
	}
	/**
	 * Возвращает шанс физического крита (1000 == 100%)
	 * @param target
	 * @param skill
	 * @return
	 */
	public int getCriticalHit(Creature target, Skill skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, _template.getBaseCritRate(), target, skill);
	}
	public double getRunSpeedMultiplier()
	{
		return _template.getBaseRunSpd() > 1 ? getRunSpeed() * (1. / _template.getBaseRunSpd()) : 1;
	}
	
	public double getWalkSpeedMultiplier()
	{
		return _template.getBaseRunSpd() > 1 ? getWalkSpeed() * (1. / _template.getBaseWalkSpd()) : 1;
	}
	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0, target, null);
	}
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _template.getBasePAtk(), target, null);
	}
	public int getMAtk(Creature target, Skill skill)
	{
		if (skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		return (int) calcStat(Stats.MAGIC_ATTACK, _template.getBaseMAtk(), target, skill);
	}
	/**
	 * Возвращает шанс магического крита в процентах
	 * @param target
	 * @param skill
	 * @return
	 */
	public double getMagicCriticalRate(Creature target, Skill skill)
	{
		return calcStat(Stats.MCRITICAL_RATE, target, skill);
	}
	
	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}
	
	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}
	
	public List<Player> getAroundPlayers()
	{
		if(!isVisible())
			return Collections.emptyList();

		return World.getAroundPlayers(this);
	}

	public List<Player> getAroundPlayers(int radius, int height)
	{
		if (!isVisible())
		{
			return new LazyArrayList<>();
		}
		return World.getAroundPlayers(this, radius, height);
	}
	
}