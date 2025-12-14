package l2r.gameserver.DebugSystem;

public class DamageBreakdown
{
    public double basePower;
    public double pAtk;
    public double mAtk;
    public double pDef;
    public double mDef;

    public double ssBoost;
    public double attributeBonus;
    public double pvpModifier;
    public double critMultiplier;

    // NEW
    public double lethalDamage;

    public double finalDamage;

    @Override
    public String toString()
    {
        return  "[DMG BREAKDOWN]\n" +
                "BasePower: " + basePower + "\n" +
                "pAtk: " + pAtk + "\n" +
                "mAtk: " + mAtk + "\n" +
                "pDef: " + pDef + "\n" +
                "mDef: " + mDef + "\n" +
                "SS Boost: " + ssBoost + "\n" +
                "Attribute: " + attributeBonus + "\n" +
                "PvP Mod: " + pvpModifier + "\n" +
                "Crit Mult: " + critMultiplier + "\n" +
                "Lethal Damage: " + lethalDamage + "\n" +
                "--------------------------------\n" +
                "FINAL DAMAGE: " + finalDamage;
    }
}
