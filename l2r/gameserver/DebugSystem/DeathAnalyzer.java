package l2r.gameserver.DebugSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeathAnalyzer
{
    private static final long DEATH_WINDOW_MS = 3000;
    private static final double BURST_RATIO = 0.45;
    private static final double SKILL_DOMINANCE_RATIO = 0.50;

    public static class Result
    {
        public boolean died;
        public String reason;

        public double incomingTotal;
        public double incomingWindow;

        public int critsInWindow;
        public Map<Integer, Double> dmgPerSkill = new HashMap<>();
    }

    public static Result analyze(FightSnapshot fight, String playerName)
    {
        Result r = new Result();

        if (fight.logs.isEmpty())
            return r;

        long end = fight.endTime;
        long startWindow = end - DEATH_WINDOW_MS;

        double totalIn = 0;
        double windowIn = 0;
        int crits = 0;

        for (CombatLogEntry e : fight.logs)
        {
            if (!playerName.equalsIgnoreCase(e.targetName))
                continue;

            if (e.miss)
                continue;

            totalIn += e.finalDamage;

            if (e.time >= startWindow)
            {
                windowIn += e.finalDamage;
                if (e.crit)
                    crits++;

                r.dmgPerSkill.merge(e.skillId, e.finalDamage, Double::sum);
            }
        }

        r.incomingTotal = totalIn;
        r.incomingWindow = windowIn;
        r.critsInWindow = crits;

        // Αν δεν υπάρχει σοβαρό incoming στο τέλος, δεν θεωρούμε death
        if (windowIn <= 0)
        {
            r.died = false;
            r.reason = "No lethal incoming damage detected.";
            return r;
        }

        r.died = true;

        // 1) Burst
        if (totalIn > 0 && (windowIn / totalIn) >= BURST_RATIO)
        {
            r.reason = "Burst damage (" + Math.round((windowIn / totalIn) * 100) + "% in last 3s)";
            return r;
        }

        // 2) Crit spike
        if (crits >= 2)
        {
            r.reason = "Critical spike (" + crits + " crits in last 3s)";
            return r;
        }

        // 3) Skill dominance
        for (Map.Entry<Integer, Double> e : r.dmgPerSkill.entrySet())
        {
            if (windowIn > 0 && (e.getValue() / windowIn) >= SKILL_DOMINANCE_RATIO)
            {
                r.reason = "Dominated by skill ID " + e.getKey() +
                        " (" + Math.round((e.getValue() / windowIn) * 100) + "% of damage)";
                return r;
            }
        }

        // 4) Sustained pressure
        r.reason = "Sustained pressure (no burst, steady DPS)";
        return r;
    }
}
