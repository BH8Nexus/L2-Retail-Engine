package l2r.gameserver.DebugSystem;

import java.util.ArrayList;
import java.util.List;

public class FightAnalyzer
{
    private static final long FIGHT_GAP_MS = 5000; // 5 sec silence = new fight

    public static List<FightSnapshot> split(List<CombatLogEntry> logs, String playerName)
    {
        List<FightSnapshot> fights = new ArrayList<>();
        if (logs.isEmpty())
            return fights;

        FightSnapshot current = null;
        long lastTime = 0;

        for (CombatLogEntry e : logs)
        {
            if (current == null || (e.time - lastTime) > FIGHT_GAP_MS)
            {
                current = new FightSnapshot();
                current.startTime = e.time;
                fights.add(current);
            }

            current.endTime = e.time;
            current.add(e, playerName);

            lastTime = e.time;
        }

        return fights;
    }
}
