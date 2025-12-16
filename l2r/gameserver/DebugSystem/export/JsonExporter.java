package l2r.gameserver.DebugSystem.export;

import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.model.Player;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class JsonExporter
{
    public static void export(Player player, List<CombatLogEntry> logs)
    {
        File f = new File("log/debug/session_" + player.getName() + ".json");

        try (PrintWriter pw = new PrintWriter(f))
        {
            pw.println("[");

            for (int i = 0; i < logs.size(); i++)
            {
                CombatLogEntry e = logs.get(i);

                pw.print("  {");
                pw.print("\"time\":" + e.time + ",");
                pw.print("\"attacker\":\"" + e.attackerName + "\",");
                pw.print("\"target\":\"" + e.targetName + "\",");
                pw.print("\"skill\":" + e.skillId + ",");
                pw.print("\"damage\":" + (long)e.finalDamage);
                pw.print("}");

                if (i < logs.size() - 1)
                    pw.println(",");
            }

            pw.println("\n]");
        }
        catch (Exception ignored) {}

        player.sendMessage("JSON exported: " + f.getName());
    }
}
