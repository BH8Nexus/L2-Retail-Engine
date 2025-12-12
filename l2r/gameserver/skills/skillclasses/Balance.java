package l2r.gameserver.skills.skillclasses;

import java.util.List;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;

public class Balance extends Skill
{
	public Balance(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double summaryCurrentHp = 0;
		int summaryMaximumHp = 0;
		
		for (Creature target : targets)
		{
			if (target != null)
			{
				if (target.isAlikeDead())
				{
					continue;
				}
				summaryCurrentHp += target.getCurrentHp();
				summaryMaximumHp += target.getMaxHp();
			}
		}
		
		double percent = summaryCurrentHp / summaryMaximumHp;
		
		for (Creature target : targets)
		{
			if (target != null)
			{
				if (target.isAlikeDead())
				{
					continue;
				}
				
				double hp = target.getMaxHp() * percent;
				if (hp > target.getCurrentHp())
				{
					// HP increase, not above the limit
					double limit = (target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp()) / 100.;
					if (target.getCurrentHp() < limit)
					{
						target.setCurrentHp(Math.min(hp, limit), false);
					}
				}
				else
				{
					// decrease in HP, not less than 1.01 to prevent "false death" on Olympus / duel
					target.setCurrentHp(Math.max(1.01, hp), false);
				}
				
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		}
		
		if (isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}
