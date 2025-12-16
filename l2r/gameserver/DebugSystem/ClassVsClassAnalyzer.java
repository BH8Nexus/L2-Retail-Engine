package l2r.gameserver.DebugSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.ClassId;

public class ClassVsClassAnalyzer
{
    public static class Stat
    {
        public int fights;
        public int wins;
        public int losses;
        public double totalOut;
        public double totalIn;
        public long totalDurationMs;
    }

    public static Map<ClassId, Stat> analyze(
            List<FightSnapshot> fights,
            Player player)
    {
        Map<ClassId, Stat> map = new HashMap<>();
        String playerName = player.getName();

        for (FightSnapshot f : fights)
        {
            ClassId opponentClass = f.getOpponentClass(playerName);
            if (opponentClass == null)
                continue;

            Stat st = map.computeIfAbsent(opponentClass, k -> new Stat());
            st.fights++;
            st.totalOut += f.outgoingDamage;
            st.totalIn += f.incomingDamage;
            st.totalDurationMs += f.durationMs();

            if (KillAnalyzer.analyze(f, playerName).killed)
                st.wins++;
            if (DeathAnalyzer.analyze(f, playerName).died)
                st.losses++;
        }

        return map;
    }

    public static String balanceFlag(Stat st)
    {
        if (st.fights < 3)
            return "INSUFFICIENT";

        double wr = st.wins * 100.0 / st.fights;

        if (wr >= 65) return "ADVANTAGE";
        if (wr <= 35) return "DISADVANTAGE";
        return "BALANCED";
    }
}
