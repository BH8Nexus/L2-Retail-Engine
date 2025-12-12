package l2r.gameserver.skills.skillclasses;

import java.util.List;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.templates.StatsSet;

public class PcBangPointsAddrnd extends Skill
{
	private final int _minCount;
	private final int _maxCount;
	
	public PcBangPointsAddrnd(StatsSet set)
	{
		super(set);
		_minCount = set.getInteger("PCMinCount");
		_maxCount = set.getInteger("PCMaxCount", _minCount);
	}
	
	@Override
	public void useSkill(Creature character, List<Creature> targets)
	{
		int points = Rnd.get(_minCount, _maxCount);
		
		for (Creature target : targets)
		{
			if (target.isPlayer())
			{
				Player player = target.getPlayer();
				player.addPcBangPoints(points, false);
			}
			getEffects(character, target, getActivateRate() > 0, false);
		}
		
		if (isSSPossible())
		{
			character.unChargeShots(isMagic());
		}
	}
}