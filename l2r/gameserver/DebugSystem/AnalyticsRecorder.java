package l2r.gameserver.DebugSystem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnalyticsRecorder
{
    private static final String LOG_PATH = "log/analytics/combat_events.csv";

    private static final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Records combat events into CSV file.
     */
    public static synchronized void recordCombatEvent(
            long timestamp,
            String attackerName,
            String targetName,
            int attackerId,
            int targetId,
            int skillId,
            double damage,
            boolean crit,
            boolean miss)
    {
        try
        {
            // Ensure folder exists
            java.io.File folder = new java.io.File("log/analytics/");
            if (!folder.exists())
                folder.mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_PATH, true)))
            {
                writer.printf(
                        "%s,%s,%s,%d,%d,%d,%.2f,%b,%b%n",
                        sdf.format(new Date(timestamp)),
                        safe(attackerName),
                        safe(targetName),
                        attackerId,
                        targetId,
                        skillId,
                        damage,
                        crit,
                        miss
                );
            }
        }
        catch (IOException e)
        {
            System.out.println("[AnalyticsRecorder] Error: " + e.getMessage());
        }
    }

    private static String safe(String s)
    {
        return (s == null ? "null" : s.replace(",", ";"));
    }
}
