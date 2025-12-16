package l2r.gameserver.DebugSystem.export;

import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.model.Player;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BalanceReportBuilder
{
    public static void export(Player player, List<CombatLogEntry> logs)
    {
        File f = new File("log/debug/report_" + player.getName() + ".txt");

        try (PrintWriter pw = new PrintWriter(f))
        {
            pw.println("=== SESSION BALANCE REPORT ===");
            pw.println("Player: " + player.getName());
            pw.println("Entries: " + logs.size());
            pw.println("");

            // συνολικό damage
            double total = logs.stream()
                    .mapToDouble(e -> e.finalDamage)
                    .sum();

            pw.println("Total Damage: " + (long)total);

            // crit rate
            long crits = logs.stream().filter(e -> e.crit).count();
            pw.println("Crit Rate: " + (crits * 100 / logs.size()) + "%");

            // top skill
            logs.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.skillId,
                            Collectors.summingDouble(e -> e.finalDamage)))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(e ->
                            pw.println("Top Skill: " + e.getKey() +
                                    " (" + e.getValue().longValue() + " dmg)")
                    );
        }
        catch (Exception ignored) {}

        player.sendMessage("Balance report exported.");
    }
}
