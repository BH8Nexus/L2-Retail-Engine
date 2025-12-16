package l2r.gameserver.DebugSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionSummaryAnalyzer
{
    public static class Summary
    {
        public int fights;
        public long totalDurationMs;

        public double totalOut;
        public double totalIn;

        public int hits;
        public int crits;
        public int misses;

        public int kills;
        public int deaths;

        public int longestFightIdx = -1;
        public long longestFightMs = 0;

        public int maxBurstFightIdx = -1;
        public double maxBurstValue = 0;

        public int maxDpsFightIdx = -1;
        public double maxDps = 0;

        public Map<Integer, Double> skillDamage = new HashMap<>();

        public double avgOutDps()
        {
            return totalDurationMs == 0 ? 0 : totalOut / (totalDurationMs / 1000.0);
        }

        public double avgInDps()
        {
            return totalDurationMs == 0 ? 0 : totalIn / (totalDurationMs / 1000.0);
        }

        public double critRate()
        {
            return hits == 0 ? 0 : (crits * 100.0) / hits;
        }

        public double missRate()
        {
            int total = hits + misses;
            return total == 0 ? 0 : (misses * 100.0) / total;
        }
    }

    public static Summary analyze(List<FightSnapshot> fights, String playerName)
    {
        Summary s = new Summary();
        s.fights = fights.size();

        for (int i = 0; i < fights.size(); i++)
        {
            FightSnapshot f = fights.get(i);
            long dur = f.durationMs();
            s.totalDurationMs += dur;
            s.totalOut += f.outgoingDamage;
            s.totalIn += f.incomingDamage;

            s.hits += f.hits;
            s.crits += f.crits;
            s.misses += f.misses;

            // longest fight
            if (dur > s.longestFightMs)
            {
                s.longestFightMs = dur;
                s.longestFightIdx = i;
            }

            // max DPS fight
            double dps = dur > 0 ? (f.outgoingDamage / (dur / 1000.0)) : 0;
            if (dps > s.maxDps)
            {
                s.maxDps = dps;
                s.maxDpsFightIdx = i;
            }

            // burst detection (reuse KillAnalyzer window logic)
            KillAnalyzer.Result kr = KillAnalyzer.analyze(f, playerName);
            if (kr.killed && kr.outgoingWindow > s.maxBurstValue)
            {
                s.maxBurstValue = kr.outgoingWindow;
                s.maxBurstFightIdx = i;
            }

            // kills / deaths
            if (KillAnalyzer.analyze(f, playerName).killed)
                s.kills++;
            if (DeathAnalyzer.analyze(f, playerName).died)
                s.deaths++;

            // top skills
            for (CombatLogEntry e : f.logs)
            {
                if (!playerName.equalsIgnoreCase(e.attackerName))
                    continue;
                if (e.miss)
                    continue;

                s.skillDamage.merge(e.skillId, e.finalDamage, Double::sum);
            }
        }

        return s;
    }
}
