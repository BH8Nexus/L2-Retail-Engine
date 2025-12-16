package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.List;

public class FightSnapshot
{
    public long startTime;
    public long endTime;

    public double outgoingDamage;
    public double incomingDamage;

    public int hits;
    public int crits;
    public int misses;

    public List<CombatLogEntry> logs = new ArrayList<>();

    public long durationMs()
    {
        return endTime - startTime;
    }

    public void add(CombatLogEntry e, String playerName)
    {
        logs.add(e);

        if (e.miss)
        {
            misses++;
            return;
        }

        hits++;
        if (e.crit)
            crits++;

        if (playerName.equalsIgnoreCase(e.attackerName))
            outgoingDamage += e.finalDamage;
        else if (playerName.equalsIgnoreCase(e.targetName))
            incomingDamage += e.finalDamage;
    }
    public double medianHitDamage()
    {
        List<Double> hits = new java.util.ArrayList<>();
        for (CombatLogEntry e : logs)
        {
            if (!e.miss)
                hits.add(e.finalDamage);
        }

        if (hits.isEmpty())
            return 0;

        hits.sort(Double::compare);
        int mid = hits.size() / 2;
        return hits.size() % 2 == 0
                ? (hits.get(mid - 1) + hits.get(mid)) / 2.0
                : hits.get(mid);
    }
    public l2r.gameserver.model.base.ClassId getOpponentClass(String playerName)
    {
        for (CombatLogEntry e : logs)
        {
            if (playerName.equalsIgnoreCase(e.attackerName))
                return e.targetClassId;
            if (playerName.equalsIgnoreCase(e.targetName))
                return e.attackerClassId;
        }
        return null;
    }

}
