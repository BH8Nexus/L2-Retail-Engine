package l2r.gameserver.DebugSystem;

import java.util.HashMap;
import java.util.Map;

public class SkillEfficiencyAnalyzer
{
    public static class SkillStat
    {
        public int hits;
        public int crits;
        public double totalDamage;

        public double avgDamage()
        {
            return hits == 0 ? 0 : totalDamage / hits;
        }

        public double critRate()
        {
            return hits == 0 ? 0 : (crits * 100.0) / hits;
        }

        public double efficiency()
        {
            return (avgDamage() * critRate()) / 100.0;
        }
    }

    public static Map<Integer, SkillStat> analyze(FightSnapshot fight, String playerName)
    {
        Map<Integer, SkillStat> map = new HashMap<>();

        for (CombatLogEntry e : fight.logs)
        {
            if (!playerName.equalsIgnoreCase(e.attackerName))
                continue;

            if (e.miss)
                continue;

            SkillStat st = map.computeIfAbsent(e.skillId, k -> new SkillStat());
            st.hits++;
            st.totalDamage += e.finalDamage;
            if (e.crit)
                st.crits++;
        }

        return map;
    }
}
