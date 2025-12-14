package l2r.gameserver.DebugSystem;

import l2r.gameserver.model.Creature;

public class CombatEventHook
{
    public static void onDamage(
            Creature attacker,
            Creature target,
            int skillId,
            double finalDamage,
            boolean crit,
            boolean miss)
    {
        try
        {
            if (attacker == null || target == null)
                return;

            CombatLogEntry log = new CombatLogEntry();

            log.setAttacker(attacker);
            log.setTarget(target);

            log.skillId = skillId;
            log.finalDamage = finalDamage;
            log.crit = crit;
            log.miss = miss;

            String msg = "[DMG] " + log.attackerName + " → " + log.targetName +
                    " | Skill=" + log.skillId +
                    " | Dmg=" + (long)log.finalDamage +
                    " | Crit=" + crit +
                    " | Miss=" + miss;

            // SAVE to debug collector
            CombatLogCollector.push(attacker.getObjectId(), log);
            CombatLogCollector.push(target.getObjectId(), log);

            // SEND live debug message
            LiveDebug.sendColoredEvent(attacker, msg,
                    miss ? LiveDebug.YELLOW :
                            crit ? LiveDebug.ORANGE :
                                    log.finalDamage > 1000 ? LiveDebug.RED : LiveDebug.GREEN
            );
        }
        catch (Exception e)
        {
            System.out.println("[CombatEventHook] Error: " + e.getMessage());
        }
    }
    public static void onDamageWithBreakdown(
            Creature attacker,
            Creature target,
            int skillId,
            DamageBreakdown info)
    {
        try
        {
            if (attacker == null || target == null)
                return;

            // 1. SAVE breakdown to collector
            CombatLogCollector.push(attacker.getObjectId(), new CombatLogEntry()
            {{
                setAttacker(attacker);
                setTarget(target);
                this.skillId = skillId;
                this.finalDamage = info.finalDamage;
                this.extraInfo = info.toString();
            }});

            // 2. SEND LIVE DEBUG (attacker only)
            LiveDebug.sendBreakdown(attacker, info);
        }
        catch (Exception e)
        {
            System.out.println("[CombatEventHook] Breakdown Error: " + e.getMessage());
        }
    }
}
