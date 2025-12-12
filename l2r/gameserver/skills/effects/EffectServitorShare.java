package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.Func;

public class EffectServitorShare extends Effect
{
	public static final Stats[] STATS_USED =
	{
		Stats.POWER_ATTACK,
		Stats.POWER_DEFENCE,
		Stats.MAGIC_ATTACK,
		Stats.MAGIC_DEFENCE,
		Stats.MAX_HP,
		Stats.MAX_MP,
		Stats.CRITICAL_BASE,
		Stats.POWER_ATTACK_SPEED,
		Stats.MAGIC_ATTACK_SPEED
	};
	
	public class FuncShare extends Func
	{
		public FuncShare(Stats stat, int order, Object owner, double value)
		{
			super(stat, order, owner, value);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += env.character.getPlayer().calcStat(stat, stat.getInit()) * value;
		}
	}
	
	public EffectServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		onActionTime();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
	}
	
	public double getBonusStatValue(Stats stat)
	{
		switch (stat)
		{
			case POWER_ATTACK:
				return getEffected().getPAtk(null) * 0.5D;
			case POWER_DEFENCE:
				return getEffected().getPDef(null) * 0.5D;
			case MAGIC_ATTACK:
				return getEffected().getMAtk(null, null) * 0.25D;
			case MAGIC_DEFENCE:
				return getEffected().getMDef(null, null) * 0.25D;
			case MAX_HP:
				return getEffected().getMaxHp() * 0.1D;
			case MAX_MP:
				return getEffected().getMaxHp() * 0.1D;
			case CRITICAL_BASE:
				return getEffected().getCriticalHit(null, null) * 0.2D;
			case POWER_ATTACK_SPEED:
				return getEffected().getPAtkSpd() * 0.1D;
			case MAGIC_ATTACK_SPEED:
				return getEffected().getMAtkSpd() * 0.03D;
			default:
				return 0;
		}
	}
	
	@Override
	protected boolean onActionTime()
	{
		return false;
	}
}