package l2r.gameserver.DebugSystem.export;

import l2r.gameserver.DebugSystem.CombatLogCollector;
import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.model.Player;

import java.util.List;

public class ExportService
{
    public static void exportSession(
            Player player,
            String format)
    {
        List<CombatLogEntry> logs =
                CombatLogCollector.get(player.getObjectId());

        if (logs == null || logs.isEmpty())
            return;

        switch (format)
        {
            case "csv":
                CsvExporter.export(player, logs);
                break;

            case "json":
                JsonExporter.export(player, logs);
                break;

            case "report":
                BalanceReportBuilder.export(player, logs);
                break;
        }
    }
}
