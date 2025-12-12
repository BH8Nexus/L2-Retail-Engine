package l2r.gameserver.templates;

public class CharTemplate
{
	public final static int[] EMPTY_ATTRIBUTES = new int[6];
	
	private final int baseSTR;
	private final int baseCON;
	private final int baseDEX;
	private final int baseINT;
	private final int baseWIT;
	private final int baseMEN;
	private final double baseHpMax;
	private final double baseCpMax;
	private final double baseMpMax;
	
	/** HP Regen base */
	private final double baseHpReg;
	
	/** MP Regen base */
	private final double baseMpReg;
	
	/** CP Regen base */
	private final double baseCpReg;
	
	private final double basePAtk;
	private final double baseMAtk;
	private final double basePDef;
	private final double baseMDef;
	private final int basePAtkSpd;
	private final int baseMAtkSpd;
	private final double baseShldDef;
	private final int baseAtkRange;
	private final int baseShldRate;
	private final int baseCritRate;
	private final double baseRunSpd;
	private final double baseWalkSpd;
	
	private final int[] baseAttributeAttack;
	private final int[] baseAttributeDefence;
	
	//private final WeaponType baseAttackType;
	
	private final double collisionRadius;
	private final double collisionHeight;
	
	
	
	public CharTemplate(StatsSet set)
	{
		baseSTR = set.getInteger("baseSTR");
		baseCON = set.getInteger("baseCON");
		baseDEX = set.getInteger("baseDEX");
		baseINT = set.getInteger("baseINT");
		baseWIT = set.getInteger("baseWIT");
		baseMEN = set.getInteger("baseMEN");
		baseHpMax = set.getDouble("baseHpMax", 0);
		baseCpMax = set.getDouble("baseCpMax", 0);
		baseMpMax = set.getDouble("baseMpMax", 0);
		baseHpReg = set.getDouble("baseHpReg", 0);
		baseCpReg = set.getDouble("baseCpReg", 0);
		baseMpReg = set.getDouble("baseMpReg", 0);
		basePAtk = set.getDouble("basePAtk");
		baseMAtk = set.getDouble("baseMAtk");
		basePDef = set.getDouble("basePDef");
		baseMDef = set.getDouble("baseMDef");
		basePAtkSpd = set.getInteger("basePAtkSpd", 300);
		baseMAtkSpd = set.getInteger("baseMAtkSpd", 333);
		
		baseShldDef = set.getDouble("baseShldDef", 0);
		
		baseAtkRange = set.getInteger("baseAtkRange");
		
		baseShldRate = set.getInteger("baseShldRate", 0);
		baseCritRate = set.getInteger("baseCritRate");
		baseRunSpd = set.getDouble("baseRunSpd");
		baseWalkSpd = set.getDouble("baseWalkSpd");
		baseAttributeAttack = set.getIntegerArray("baseAttributeAttack", EMPTY_ATTRIBUTES);
		baseAttributeDefence = set.getIntegerArray("baseAttributeDefence", EMPTY_ATTRIBUTES);
		
		//baseAttackType = WeaponType.valueOf(set.getString("baseAttackType", "NONE").toUpperCase());
		
		// Geometry
		collisionRadius = set.getDouble("collision_radius", 5);
		collisionHeight = set.getDouble("collision_height", 5);
	}
	
	public int getNpcId()
	{
		return 0;
	}
	
	public static StatsSet getEmptyStatsSet()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseHpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseCpReg", 0);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseCritRate", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("baseWalkSpd", 0);
		//npcDat.set("baseAttackType", "NONE");
		return npcDat;
	}
	
	public final int getBaseSTR()
	{
		return baseSTR;
	}
	
	public final int getBaseCON()
	{
		return baseCON;
	}
	
	public final int getBaseDEX()
	{
		return baseDEX;
	}
	
	public final int getBaseINT()
	{
		return baseINT;
	}
	
	public final int getBaseWIT()
	{
		return baseWIT;
	}
	
	public final int getBaseMEN()
	{
		return baseMEN;
	}
	
	public double getBaseCpMax()
	{
		return baseCpMax;
	}
	
	public double getBaseCpMax(int level)
	{
		return baseCpMax;
	}
	
	public double getBaseHpMax()
	{
		return baseHpMax;
	}
	
	public double getBaseHpMax(int level)
	{
		return baseHpMax;
	}
	
	public double getBaseMpMax()
	{
		return baseMpMax;
	}
	
	public double getBaseMpMax(int level)
	{
		return baseMpMax;
	}
	
	public double getBaseCpReg()
	{
		return baseCpReg;
	}
	
	public double getBaseCpReg(int level)
	{
		return baseCpReg;
	}
	
	public double getBaseHpReg()
	{
		return baseHpReg;
	}
	
	public double getBaseMpReg()
	{
		return baseMpReg;
	}
	
	public double getBaseHpReg(int level)
	{
		return baseHpReg;
	}
	
	public double getBaseMpReg(int level)
	{
		return baseMpReg;
	}
	
	public final int getBasePAtk()
	{
		return (int) basePAtk;
	}
	
	public final int getBaseMAtk()
	{
		return (int) baseMAtk;
	}
	
	public final int getBaseMAtkSpd()
	{
		return baseMAtkSpd;
	}
	
	public final int getBaseAtkRange()
	{
		return baseAtkRange;
	}
	
	public final int getBaseShldRate()
	{
		return baseShldRate;
	}
	
	public final int getBaseShldDef()
	{
		return (int) baseShldDef;
	}
	
	public final int getBasePDef()
	{
		return (int) basePDef;
	}
	
	public final int getBaseMDef()
	{
		return (int) baseMDef;
	}
	
	public final int getBasePAtkSpd()
	{
		return basePAtkSpd;
	}
	
	public final int getBaseCritRate()
	{
		return baseCritRate;
	}
	
	public final int getBaseRunSpd()
	{
		return (int) baseRunSpd;
	}
	
	public final int getBaseWalkSpd()
	{
		return (int) baseWalkSpd;
	}
	
	public final int[] getBaseAttributeAttack()
	{
		return baseAttributeAttack;
	}
	
	public final int[] getBaseAttributeDefence()
	{
		return baseAttributeDefence;
	}
	
	public final double getCollisionRadius()
	{
		return collisionRadius;
	}
	
	public final double getCollisionHeight()
	{
		return collisionHeight;
	}
}