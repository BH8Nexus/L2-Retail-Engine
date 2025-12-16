package l2r.gameserver.DebugSystem.simulation;

import l2r.gameserver.model.base.ClassId;

public class PatchScenario
{
    public String name;

    public Integer skillId;        // null = όλα
    public ClassId classId;        // null = όλα

    public double damageMultiplier = 1.0;
    public double critMultiplier   = 1.0;

    public PatchScenario(String name)
    {
        this.name = name;
    }
}
