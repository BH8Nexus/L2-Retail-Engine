package l2r.gameserver.DebugSystem.testpvp;

import l2r.gameserver.DebugSystem.*;
import l2r.gameserver.model.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TestPvPSnapshotService
{
    private static final Map<Integer, List<TestPvPSnapshot>> SNAPSHOTS =
            new ConcurrentHashMap<>();

    public static void snapshot(Player attacker, Player target, RangeType range)
    {
        if (attacker == null || target == null)
            return;

        List<CombatLogEntry> logs =
                CombatLogCollector.get(attacker.getObjectId());

        if (logs == null || logs.isEmpty())
            return;

        long dmg = 0;
        int hits = 0;
        int crits = 0;
        double distSum = 0;
        int distCount = 0;

        for (CombatLogEntry e : logs)
        {
            if (!e.targetName.equals(target.getName()))
                continue;

            dmg += e.finalDamage;
            hits++;
            if (e.crit)
                crits++;

        }

        if (hits == 0)
            return;

        TestPvPSnapshot snap = new TestPvPSnapshot(
                attacker.getName(),
                target.getName(),
                attacker.getClassId(),
                target.getClassId(),
                range,
                distSum / distCount,
                dmg,
                hits,
                crits
        );

        SNAPSHOTS
                .computeIfAbsent(attacker.getObjectId(), k -> new ArrayList<>())
                .add(snap);
    }

    public static List<TestPvPSnapshot> getSnapshots(Player player)
    {
        return SNAPSHOTS.getOrDefault(player.getObjectId(), Collections.emptyList());
    }

    public static void clear(Player player)
    {
        SNAPSHOTS.remove(player.getObjectId());
    }
}
