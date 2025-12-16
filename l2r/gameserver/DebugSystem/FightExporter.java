package l2r.gameserver.DebugSystem;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class FightExporter
{
    public static File exportCsv(FightSnapshot fight, int index, String playerName) throws Exception
    {
        File dir = new File("log/debug/fights/");
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, "fight_" + index + "_" + playerName + ".csv");
        FileWriter fw = new FileWriter(file);

        // ---- header
        fw.write("time,attacker,target,skillId,damage,crit,miss\n");

        for (CombatLogEntry e : fight.logs)
        {
            fw.write(
                    e.time + "," +
                            e.attackerName + "," +
                            e.targetName + "," +
                            e.skillId + "," +
                            e.finalDamage + "," +
                            e.crit + "," +
                            e.miss + "\n"
            );
        }

        fw.close();
        return file;
    }

    public static File exportJson(FightSnapshot fight, int index, String playerName) throws Exception
    {
        File dir = new File("log/debug/fights/");
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, "fight_" + index + "_" + playerName + ".json");
        FileWriter fw = new FileWriter(file);

        fw.write("{\n");
        fw.write("\"index\": " + index + ",\n");
        fw.write("\"player\": \"" + playerName + "\",\n");
        fw.write("\"start\": " + fight.startTime + ",\n");
        fw.write("\"end\": " + fight.endTime + ",\n");
        fw.write("\"outgoingDamage\": " + fight.outgoingDamage + ",\n");
        fw.write("\"incomingDamage\": " + fight.incomingDamage + ",\n");
        fw.write("\"hits\": " + fight.hits + ",\n");
        fw.write("\"crits\": " + fight.crits + ",\n");
        fw.write("\"misses\": " + fight.misses + ",\n");
        fw.write("\"logs\": [\n");

        for (int i = 0; i < fight.logs.size(); i++)
        {
            CombatLogEntry e = fight.logs.get(i);
            fw.write("  {\n");
            fw.write("    \"time\": " + e.time + ",\n");
            fw.write("    \"attacker\": \"" + e.attackerName + "\",\n");
            fw.write("    \"target\": \"" + e.targetName + "\",\n");
            fw.write("    \"skillId\": " + e.skillId + ",\n");
            fw.write("    \"damage\": " + e.finalDamage + ",\n");
            fw.write("    \"crit\": " + e.crit + ",\n");
            fw.write("    \"miss\": " + e.miss + "\n");
            fw.write("  }" + (i + 1 < fight.logs.size() ? "," : "") + "\n");
        }

        fw.write("]\n}");
        fw.close();

        return file;
    }
}
