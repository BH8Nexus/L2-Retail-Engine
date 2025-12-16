package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DamageTimeline
{
    public static class Point
    {
        public long second;
        public double damage;
        public int hits;
        public int crits;
    }

    public static List<Point> build(List<CombatLogEntry> logs)
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
                p.damage += e.finalDamage;
                p.hits++;
                if (e.crit)
                    p.crits++;
            }
        }

        return new ArrayList<>(map.values());
    }
}
