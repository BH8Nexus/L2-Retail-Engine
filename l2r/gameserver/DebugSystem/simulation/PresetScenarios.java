package l2r.gameserver.DebugSystem.simulation;

import l2r.gameserver.DebugSystem.simulation.PatchScenario;
import l2r.gameserver.model.base.ClassId;

public class PresetScenarios
{
    public static PatchScenario skillNerf(int skillId, double percent)
    {
        PatchScenario p = new PatchScenario("Skill " + skillId + " -" + percent + "%");
        p.skillId = skillId;
        p.damageMultiplier = 1.0 - (percent / 100.0);
        return p;
    }

    public static PatchScenario classNerf(ClassId cid, double percent)
    {
        PatchScenario p = new PatchScenario(cid + " -" + percent + "%");
        p.classId = cid;
        p.damageMultiplier = 1.0 - (percent / 100.0);
        return p;
    }
}
