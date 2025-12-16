package l2r.gameserver.DebugSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombatAnalytics
{
    public static class SkillStats
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
    }

    public static class Summary
    {
        public int totalHits;
        public int crits;
        public int misses;
        public double totalDamage;

        public Map<Integer, SkillStats> perSkill = new HashMap<>();

        public double avgDamage()
        {
            return totalHits == 0 ? 0 : totalDamage / totalHits;
        }

        public double critRate()
        {
            return totalHits == 0 ? 0 : (crits * 100.0) / totalHits;
        }

        public double missRate()
        {
            return totalHits == 0 ? 0 : (misses * 100.0) / totalHits;
        }
    }

    public static Summary analyze(List<CombatLogEntry> logs)
    {
        Summary s = new Summary();

        for (CombatLogEntry e : logs)
        {
            s.totalHits++;

            if (e.miss)
            {
                s.misses++;
                continue;
            }

            s.totalDamage += e.finalDamage;

            if (e.crit)
                s.crits++;

            SkillStats sk = s.perSkill.get(e.skillId);
            if (sk == null)
            {
                sk = new SkillStats();
                s.perSkill.put(e.skillId, sk);
            }

            sk.hits++;
            sk.totalDamage += e.finalDamage;
            if (e.crit)
                sk.crits++;
        }

        return s;
    }
}
