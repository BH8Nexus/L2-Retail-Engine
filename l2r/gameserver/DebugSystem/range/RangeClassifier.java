package l2r.gameserver.DebugSystem.range;

import l2r.gameserver.DebugSystem.testpvp.RangeType;
import l2r.gameserver.model.Player;

public final class RangeClassifier
{
    private RangeClassifier() {}

    public static RangeType classify(Player attacker, Player target)
    {
        if (attacker == null || target == null)
            return RangeType.MID;

        double dx = attacker.getX() - target.getX();
        double dy = attacker.getY() - target.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist <= 200)
            return RangeType.MELEE;

        if (dist <= 600)
            return RangeType.MID;

        return RangeType.ARCHER;
    }
}
