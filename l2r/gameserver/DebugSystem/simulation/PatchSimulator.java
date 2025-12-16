package l2r.gameserver.DebugSystem.simulation;

import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.DebugSystem.simulation.PatchScenario;

import java.util.List;

public class PatchSimulator
{
    public static SimulationResult simulate(
            List<CombatLogEntry> logs,
            PatchScenario scenario,
            String playerName)
    {
        double totalBefore = 0;
        double totalAfter  = 0;
        int hits = 0;

        for (CombatLogEntry e : logs)
        {
            if (!playerName.equalsIgnoreCase(e.attackerName))
                continue;

            double dmg = e.finalDamage;
            totalBefore += dmg;

            double modified = dmg;

            if (scenario.skillId != null && e.skillId == scenario.skillId)
                modified *= scenario.damageMultiplier;

            if (scenario.classId != null &&
                    scenario.classId == e.attackerClassId)
                modified *= scenario.damageMultiplier;

            if (e.crit)
                modified *= scenario.critMultiplier;

            totalAfter += modified;
            hits++;
        }

        return new SimulationResult(
                totalBefore,
                totalAfter,
                hits
        );
    }
}
