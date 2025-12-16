package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RollingDps
{
    public static class Point
    {
        public long second;
        public double dpsOut;
        public double dpsIn;
    }

    public static List<Point> compute(FightSnapshot fight, String playerName, int windowSeconds)
    {
        // damage per second buckets
        Map<Long, double[]> sec = new TreeMap<>(); // [0]=out, [1]=in

        for (CombatLogEntry e : fight.logs)
        {
            long s = e.time / 1000L;
            double[] v = sec.computeIfAbsent(s, k -> new double[2]);

            if (!e.miss)
            {
                if (playerName.equalsIgnoreCase(e.attackerName))
                    v[0] += e.finalDamage;
                else if (playerName.equalsIgnoreCase(e.targetName))
                    v[1] += e.finalDamage;
            }
        }

        List<Long> keys = new ArrayList<>(sec.keySet());
        List<Point> out = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++)
        {
            double sumOut = 0, sumIn = 0;

            for (int w = 0; w < windowSeconds; w++)
            {
                int idx = i - w;
                if (idx < 0) break;

                double[] v = sec.get(keys.get(idx));
                sumOut += v[0];
                sumIn  += v[1];
            }

            Point p = new Point();
            p.second = keys.get(i);
            p.dpsOut = sumOut / windowSeconds;
            p.dpsIn  = sumIn  / windowSeconds;
            out.add(p);
        }

        return out;
    }
}
