package l2r.gameserver.DebugSystem;

import l2r.gameserver.DebugSystem.testpvp.RangeType;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.ClassId;

public class CombatLogEntry
{
    public long time;
    public ClassId attackerClassId;
    public ClassId targetClassId;
    public String attackerName;
    public String targetName;

    public int attackerId;
    public int targetId;

    public int skillId;
    public int skillLevel;

    public double basePower;
    public double pAtk;
    public double mAtk;
    public double pDef;
    public double mDef;

    public double finalDamage;

    public boolean crit;
    public boolean miss;
    public boolean reflect;
    public boolean counter;
    public RangeType rangeType;
    public String extraInfo;

    public CombatLogEntry()
    {
        this.time = System.currentTimeMillis();
    }

    public void setAttacker(Creature c)
    {
        if (c == null) return;

        attackerName = c.getName();
        attackerId = c.getObjectId();

        if (c instanceof Player)
            attackerClassId = ((Player) c).getClassId();

    }


    public void setTarget(Creature c)
    {
        if (c == null) return;

        targetName = c.getName();
        targetId = c.getObjectId();

        if (c instanceof Player)
            targetClassId = ((Player) c).getClassId();

    }


    public String toSimpleLine()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(new java.text.SimpleDateFormat("HH:mm:ss").format(time))
                .append("] ");

        sb.append(attackerName)
                .append(" → ")
                .append(targetName);

        sb.append(" | Skill=").append(skillId);
        sb.append(" | Dmg=").append((long) finalDamage);

        if (rangeType != null)
            sb.append(" | ").append(rangeType.name());

        if (crit) sb.append(" | CRIT");
        if (miss) sb.append(" | MISS");
        if (reflect) sb.append(" | REFLECT");
        if (counter) sb.append(" | COUNTER");

        return sb.toString();
    }

    public String toBreakdownText()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[BREAKDOWN]\n");
        sb.append("Time: ").append(new java.text.SimpleDateFormat("HH:mm:ss").format(time)).append("\n");
        sb.append("Attacker: ").append(attackerName).append("\n");
        sb.append("Target: ").append(targetName).append("\n");
        sb.append("Skill: ").append(skillId).append("\n");
        sb.append("--------------------------------\n");

        if (basePower > 0)
            sb.append("Base Power: ").append(basePower).append("\n");

        if (pAtk > 0)
            sb.append("P.Atk: ").append(pAtk).append("\n");

        if (mAtk > 0)
            sb.append("M.Atk: ").append(mAtk).append("\n");

        if (pDef > 0)
            sb.append("Target P.Def: ").append(pDef).append("\n");

        if (mDef > 0)
            sb.append("Target M.Def: ").append(mDef).append("\n");

        if (crit)
            sb.append("Critical: YES\n");

        if (miss)
            sb.append("Miss: YES\n");

        if (reflect)
            sb.append("Reflect: YES\n");

        if (counter)
            sb.append("Counterattack: YES\n");

        sb.append("--------------------------------\n");
        sb.append("Final Damage: ").append(finalDamage).append("\n");

        if (extraInfo != null && !extraInfo.isEmpty())
        {
            sb.append("--------------------------------\n");
            sb.append(extraInfo).append("\n");
        }

        return sb.toString();
    }

}
