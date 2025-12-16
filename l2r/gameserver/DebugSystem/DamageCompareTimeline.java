package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DamageCompareTimeline
{
    public static class Point
    {
        public long second;
        public double outgoing;
        public double incoming;
    }

    public static List<Point> build(List<CombatLogEntry> logs, String playerName)
    {
        Map<Long, Point> map = new TreeMap<>();

        for (CombatLogEntry e : logs)
        {
            long sec = e.time / 1000L;

            Point p = map.get(sec);
            if (p == null)
            {
                p = new Point();
                p.second = sec;
                map.put(sec, p);
            }

            if (!e.miss)
            {
                if (playerName.equalsIgnoreCase(e.attackerName))
                    p.outgoing += e.finalDamage;
                else if (playerName.equalsIgnoreCase(e.targetName))
                    p.incoming += e.finalDamage;
            }
        }

        return new ArrayList<>(map.values());
    }
}
