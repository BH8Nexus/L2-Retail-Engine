package l2r.gameserver.DebugSystem.export;

import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.model.Player;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class CsvExporter
{
    public static void export(Player player, List<CombatLogEntry> logs)
    {
        File f = new File("log/debug/session_" + player.getName() + ".csv");

        try (PrintWriter pw = new PrintWriter(f))
        {
            pw.println("time,attacker,target,skill,damage,crit,miss");

            for (CombatLogEntry e : logs)
            {
                pw.println(
                        e.time + "," +
                                e.attackerName + "," +
                                e.targetName + "," +
                                e.skillId + "," +
                                (long)e.finalDamage + "," +
                                e.crit + "," +
                                e.miss
                );
            }
        }
        catch (Exception ignored) {}

        player.sendMessage("CSV exported: " + f.getName());
    }
}
