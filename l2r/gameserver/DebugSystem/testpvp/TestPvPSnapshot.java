package l2r.gameserver.DebugSystem.testpvp;

import l2r.gameserver.model.base.ClassId;

public class TestPvPSnapshot
{
    public final long time;
    public final String attacker;
    public final String target;

    public final ClassId attackerClass;
    public final ClassId targetClass;

    public final RangeType range;

    public final double avgDistance;
    public final long totalDamage;
    public final int hits;
    public final int crits;

    public TestPvPSnapshot(
            String attacker,
            String target,
            ClassId attackerClass,
            ClassId targetClass,
            RangeType range,
            double avgDistance,
            long totalDamage,
            int hits,
            int crits)
    {
        this.time = System.currentTimeMillis();
        this.attacker = attacker;
        this.target = target;
        this.attackerClass = attackerClass;
        this.targetClass = targetClass;
        this.range = range;
        this.avgDistance = avgDistance;
        this.totalDamage = totalDamage;
        this.hits = hits;
        this.crits = crits;
    }
}
