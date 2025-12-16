package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvpBalanceAnalyzer
{
    public static class Warning
    {
        public String type;
        public String message;

        public Warning(String type, String message)
        {
            this.type = type;
            this.message = message;
        }
    }

    // thresholds
    private static final double BURST_RATIO = 0.55;
    private static final double DPS_MULTIPLIER = 2.5;
    private static final double CRIT_RATE_LIMIT = 65.0;
    private static final double SKILL_DOMINANCE = 0.60;

    private static final long SHORT_FIGHT_MS = 2000;
    private static final long LONG_FIGHT_MS  = 90000;

    public static List<Warning> analyze(
            List<FightSnapshot> fights,
            SessionSummaryAnalyzer.Summary session,
            String playerName)
    {
        List<Warning> warnings = new ArrayList<>();

        int burstAbuseCount = 0;
        int critAbuseCount  = 0;
        Map<Integer, Integer> skillDominanceCount = new HashMap<>();

        for (int i = 0; i < fights.size(); i++)
        {
            FightSnapshot f = fights.get(i);
            long dur = f.durationMs();

            // ---- fight length anomalies
            if (dur < SHORT_FIGHT_MS)
                warnings.add(new Warning(
                        "Fight Anomaly",
                        "Fight " + i + " extremely short (" + (dur / 1000.0) + "s)"
                ));

            if (dur > LONG_FIGHT_MS)
                warnings.add(new Warning(
                        "Fight Anomaly",
                        "Fight " + i + " extremely long (" + (dur / 1000.0) + "s)"
                ));

            // ---- burst abuse
            KillAnalyzer.Result kr = KillAnalyzer.analyze(f, playerName);
            if (kr.killed && kr.outgoingTotal > 0)
            {
                double ratio = kr.outgoingWindow / kr.outgoingTotal;
                if (ratio >= BURST_RATIO)
                    burstAbuseCount++;
            }

            // ---- crit abuse (per fight)
            double fightCritRate =
                    f.hits == 0 ? 0 : (f.crits * 100.0 / f.hits);
            if (fightCritRate >= CRIT_RATE_LIMIT)
                critAbuseCount++;

            // ---- skill dominance
            Map<Integer, Double> skillDmg = new HashMap<>();
            for (CombatLogEntry e : f.logs)
            {
                if (!playerName.equalsIgnoreCase(e.attackerName))
                    continue;
                if (e.miss)
                    continue;

                skillDmg.merge(e.skillId, e.finalDamage, Double::sum);
            }

            for (Map.Entry<Integer, Double> e : skillDmg.entrySet())
            {
                if (f.outgoingDamage > 0 &&
                        (e.getValue() / f.outgoingDamage) >= SKILL_DOMINANCE)
                {
                    skillDominanceCount.merge(e.getKey(), 1, Integer::sum);
                }
            }
        }

        // ---- session-level evaluations

        if (fights.size() > 0 &&
                (burstAbuseCount * 1.0 / fights.size()) >= 0.30)
        {
            warnings.add(new Warning(
                    "Burst Abuse",
                    "High burst detected in " + burstAbuseCount +
                            "/" + fights.size() + " fights"
            ));
        }

        double avgDps = session.avgOutDps();
        if (session.maxDps >= avgDps * DPS_MULTIPLIER)
        {
            warnings.add(new Warning(
                    "DPS Outlier",
                    "Peak DPS (" + (int) session.maxDps +
                            ") is " + String.format("%.1f", session.maxDps / avgDps) +
                            "× higher than session average"
            ));
        }

        if (critAbuseCount >= 3)
        {
            warnings.add(new Warning(
                    "Crit Abuse",
                    "High crit rate in " + critAbuseCount + " fights"
            ));
        }

        for (Map.Entry<Integer, Integer> e : skillDominanceCount.entrySet())
        {
            if (e.getValue() >= 2)
            {
                warnings.add(new Warning(
                        "Skill Dominance",
                        "Skill " + e.getKey() +
                                " dominated " + e.getValue() + " fights"
                ));
            }
        }

        return warnings;
    }
}
