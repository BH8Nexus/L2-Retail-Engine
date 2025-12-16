package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.List;

public class FightTagAnalyzer
{
    public enum Tag
    {
        BURST,
        SUSTAIN,
        POKE,
        SPIKE,
        LOW_IMPACT
    }

    public static List<Tag> analyze(FightSnapshot fight, String playerName)
    {
        List<Tag> tags = new ArrayList<>();

        long durMs = fight.durationMs();
        double durSec = durMs / 1000.0;

        double totalOut = fight.outgoingDamage;
        double avgDps = durSec > 0 ? totalOut / durSec : 0;

        // ---- BURST
        KillAnalyzer.Result kr = KillAnalyzer.analyze(fight, playerName);
        if (kr.killed && kr.outgoingTotal > 0)
        {
            double ratio = kr.outgoingWindow / kr.outgoingTotal;
            if (ratio >= 0.50)
                tags.add(Tag.BURST);
        }

        // ---- SPIKE
        double critRate = fight.hits == 0 ? 0 : (fight.crits * 100.0 / fight.hits);
        if (fight.crits >= 3 && critRate >= 50)
            tags.add(Tag.SPIKE);

        // ---- SUSTAIN
        if (durSec >= 15)
        {
            double windowDps = kr.outgoingWindow / 3.0;
            if (avgDps > 0 && (windowDps / avgDps) >= 0.70)
                tags.add(Tag.SUSTAIN);
        }

        // ---- POKE
        if (fight.hits >= 8)
        {
            double avgHit = fight.hits == 0 ? 0 : (totalOut / fight.hits);
            double median = fight.medianHitDamage();
            if (median > 0 && avgHit <= median * 0.35)
                tags.add(Tag.POKE);
        }

        // ---- LOW IMPACT
        if (totalOut <= 300 ||
                (fight.incomingDamage > 0 && totalOut <= fight.incomingDamage * 0.05))
        {
            tags.add(Tag.LOW_IMPACT);
        }

        return tags;
    }
}
