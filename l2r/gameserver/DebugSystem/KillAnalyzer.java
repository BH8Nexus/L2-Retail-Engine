package l2r.gameserver.DebugSystem;

import java.util.HashMap;
import java.util.Map;

public class KillAnalyzer
{
    private static final long KILL_WINDOW_MS = 3000;
    private static final double BURST_RATIO = 0.45;
    private static final double SKILL_DOMINANCE_RATIO = 0.50;

    public static class Result
    {
        public boolean killed;
        public String reason;

        public double outgoingTotal;
        public double outgoingWindow;

        public int critsInWindow;
        public Map<Integer, Double> dmgPerSkill = new HashMap<>();
    }

    public static Result analyze(FightSnapshot fight, String playerName)
    {
        Result r = new Result();

        if (fight.logs.isEmpty())
            return r;

        long end = fight.endTime;
        long startWindow = end - KILL_WINDOW_MS;

        double totalOut = 0;
        double windowOut = 0;
        int crits = 0;

        for (CombatLogEntry e : fight.logs)
        {
            if (!playerName.equalsIgnoreCase(e.attackerName))
                continue;

            if (e.miss)
                continue;

            totalOut += e.finalDamage;

            if (e.time >= startWindow)
            {
                windowOut += e.finalDamage;
                if (e.crit)
                    crits++;

                r.dmgPerSkill.merge(e.skillId, e.finalDamage, Double::sum);
            }
        }

        r.outgoingTotal = totalOut;
        r.outgoingWindow = windowOut;
        r.critsInWindow = crits;

        // Αν δεν υπάρχει σοβαρό outgoing στο τέλος → δεν θεωρούμε kill
        if (windowOut <= 0)
        {
            r.killed = false;
            r.reason = "No lethal outgoing damage detected.";
            return r;
        }

        r.killed = true;

        // 1) Burst
        if (totalOut > 0 && (windowOut / totalOut) >= BURST_RATIO)
        {
            r.reason = "Burst kill (" + Math.round((windowOut / totalOut) * 100) + "% damage in last 3s)";
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
            if (windowOut > 0 && (e.getValue() / windowOut) >= SKILL_DOMINANCE_RATIO)
            {
                r.reason = "Dominated by skill ID " + e.getKey() +
                        " (" + Math.round((e.getValue() / windowOut) * 100) + "% of damage)";
                return r;
            }
        }

        // 4) Sustained pressure
        r.reason = "Sustained pressure kill";
        return r;
    }
}
