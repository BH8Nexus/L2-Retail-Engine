package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillTuningAdvisor
{
    public static class Hint
    {
        public int skillId;
        public String type; // NERF / BUFF
        public String reason;

        public Hint(int skillId, String type, String reason)
        {
            this.skillId = skillId;
            this.type = type;
            this.reason = reason;
        }
    }

    public static List<Hint> analyze(
            Map<Integer, SkillEfficiencyAnalyzer.SkillStat> sessionSkillStats,
            SessionSummaryAnalyzer.Summary session,
            List<PvpBalanceAnalyzer.Warning> warnings)
    {
        List<Hint> hints = new ArrayList<>();

        if (sessionSkillStats.isEmpty())
            return hints;

        // median avg damage
        List<Double> avgs = sessionSkillStats.values().stream()
                .map(SkillEfficiencyAnalyzer.SkillStat::avgDamage)
                .sorted()
                .collect(Collectors.toList());

        double medianAvg =
                avgs.size() % 2 == 0
                        ? (avgs.get(avgs.size()/2 - 1) + avgs.get(avgs.size()/2)) / 2.0
                        : avgs.get(avgs.size()/2);

        // skill dominance from warnings
        List<Integer> dominantSkills = warnings.stream()
                .filter(w -> "Skill Dominance".equals(w.type))
                .map(w -> extractSkillId(w.message))
                .filter(id -> id > 0)
                .collect(Collectors.toList());

        for (Map.Entry<Integer, SkillEfficiencyAnalyzer.SkillStat> e : sessionSkillStats.entrySet())
        {
            int skillId = e.getKey();
            SkillEfficiencyAnalyzer.SkillStat st = e.getValue();

            double share = session.totalOut > 0
                    ? (st.totalDamage * 100.0 / session.totalOut)
                    : 0;

            int nerfScore = 0;
            int buffScore = 0;

            if (share >= 45) nerfScore++;
            if (st.avgDamage() >= medianAvg * 1.8) nerfScore++;
            if (st.critRate() >= 60) nerfScore++;
            if (dominantSkills.contains(skillId)) nerfScore++;

            if (share <= 8) buffScore++;
            if (st.avgDamage() <= medianAvg * 0.6) buffScore++;
            if (st.critRate() <= 10) buffScore++;
            if (st.hits >= 5) buffScore++;

            if (nerfScore >= 2)
            {
                hints.add(new Hint(
                        skillId,
                        "NERF",
                        buildReason(st, share, medianAvg)
                ));
            }
            else if (buffScore >= 2)
            {
                hints.add(new Hint(
                        skillId,
                        "BUFF",
                        buildReason(st, share, medianAvg)
                ));
            }
        }

        return hints;
    }

    private static String buildReason(
            SkillEfficiencyAnalyzer.SkillStat st,
            double share,
            double medianAvg)
    {
        return "Share " + String.format("%.1f", share) + "%, " +
                "Avg " + (int) st.avgDamage() + " vs median " + (int) medianAvg + ", " +
                "Crit " + String.format("%.1f", st.critRate()) + "%";
    }

    private static int extractSkillId(String msg)
    {
        // expects: "Skill 1234 dominated ..."
        try
        {
            String[] p = msg.split(" ");
            return Integer.parseInt(p[1]);
        }
        catch (Exception e)
        {
            return -1;
        }
    }
}
