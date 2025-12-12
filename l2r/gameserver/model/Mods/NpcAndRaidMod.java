package l2r.gameserver.model.Mods;

import l2r.gameserver.Config;

public class NpcAndRaidMod {
  public static double NpcPAtkMul(int level) {
    double[][] PATK = new double[Config.NPC_PATK_MUL.length][3];
    double patk = 1.0D;
    int i = 0;
    if (Config.NPC_PATK_MUL.length != 0)
      for (String Patk : Config.NPC_PATK_MUL) {
        String[] splitReward = Patk.split(",");
        PATK[i][0] = Integer.parseInt(splitReward[0]);
        PATK[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] patk_data : PATK) {
      if (level <= patk_data[0]) {
        patk = patk_data[1];
        break;
      } 
    } 
    return patk;
  }
  
  public static double RaidPAtkMul(int level) {
    double[][] PATK = new double[Config.RAID_PATK_MUL.length][3];
    double patk = 1.0D;
    int i = 0;
    if (Config.RAID_PATK_MUL.length != 0)
      for (String Patk : Config.RAID_PATK_MUL) {
        String[] splitReward = Patk.split(",");
        PATK[i][0] = Integer.parseInt(splitReward[0]);
        PATK[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] patk_data : PATK) {
      if (level <= patk_data[0]) {
        patk = patk_data[1];
        break;
      } 
    } 
    return patk;
  }
  
  public static double NpcMAtkMul(int level) {
    double[][] MATK = new double[Config.NPC_MATK_MUL.length][3];
    double matk = 1.0D;
    int i = 0;
    if (Config.NPC_MATK_MUL.length != 0)
      for (String Matk : Config.NPC_MATK_MUL) {
        String[] splitReward = Matk.split(",");
        MATK[i][0] = Integer.parseInt(splitReward[0]);
        MATK[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] matk_data : MATK) {
      if (level <= matk_data[0]) {
        matk = matk_data[1];
        break;
      } 
    } 
    return matk;
  }
  
  public static double RaidMAtkMul(int level) {
    double[][] MATK = new double[Config.RAID_MATK_MUL.length][3];
    double matk = 1.0D;
    int i = 0;
    if (Config.RAID_MATK_MUL.length != 0)
      for (String Matk : Config.RAID_MATK_MUL) {
        String[] splitReward = Matk.split(",");
        MATK[i][0] = Integer.parseInt(splitReward[0]);
        MATK[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] matk_data : MATK) {
      if (level <= matk_data[0]) {
        matk = matk_data[1];
        break;
      } 
    } 
    return matk;
  }
  
  public static double NpcMaxHpMul(int level) {
    double[][] MAX_HP = new double[Config.NPC_MAX_HP_MUL.length][3];
    double maxhp = 1.0D;
    int i = 0;
    if (Config.NPC_MAX_HP_MUL.length != 0)
      for (String Maxhp : Config.NPC_MAX_HP_MUL) {
        String[] splitReward = Maxhp.split(",");
        MAX_HP[i][0] = Integer.parseInt(splitReward[0]);
        MAX_HP[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] maxhp_data : MAX_HP) {
      if (level <= maxhp_data[0]) {
        maxhp = maxhp_data[1];
        break;
      } 
    } 
    return maxhp;
  }
  
  public static double RaidMaxHpMul(int level) {
    double[][] MAX_HP = new double[Config.RAID_MAX_HP_MUL.length][3];
    double maxhp = 1.0D;
    int i = 0;
    if (Config.RAID_MAX_HP_MUL.length != 0)
      for (String Maxhp : Config.RAID_MAX_HP_MUL) {
        String[] splitReward = Maxhp.split(",");
        MAX_HP[i][0] = Integer.parseInt(splitReward[0]);
        MAX_HP[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] maxhp_data : MAX_HP) {
      if (level <= maxhp_data[0]) {
        maxhp = maxhp_data[1];
        break;
      } 
    } 
    return maxhp;
  }
  
  public static double NpcMaxMpMul(int level) {
    double[][] MAX_MP = new double[Config.NPC_MAX_MP_MUL.length][3];
    double maxmp = 1.0D;
    int i = 0;
    if (Config.NPC_MAX_MP_MUL.length != 0)
      for (String Maxmp : Config.NPC_MAX_MP_MUL) {
        String[] splitReward = Maxmp.split(",");
        MAX_MP[i][0] = Integer.parseInt(splitReward[0]);
        MAX_MP[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] maxmp_data : MAX_MP) {
      if (level <= maxmp_data[0]) {
        maxmp = maxmp_data[1];
        break;
      } 
    } 
    return maxmp;
  }
  
  public static double RaidMaxMpMul(int level) {
    double[][] MAX_MP = new double[Config.RAID_MAX_MP_MUL.length][3];
    double maxmp = 1.0D;
    int i = 0;
    if (Config.RAID_MAX_MP_MUL.length != 0)
      for (String Maxmp : Config.RAID_MAX_MP_MUL) {
        String[] splitReward = Maxmp.split(",");
        MAX_MP[i][0] = Integer.parseInt(splitReward[0]);
        MAX_MP[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] maxmp_data : MAX_MP) {
      if (level <= maxmp_data[0]) {
        maxmp = maxmp_data[1];
        break;
      } 
    } 
    return maxmp;
  }
  
  public static double NpcMDefMul(int level) {
    double[][] MDEF = new double[Config.NPC_MDEF_MUL.length][3];
    double mdef = 1.0D;
    int i = 0;
    if (Config.NPC_MDEF_MUL.length != 0)
      for (String Mdef : Config.NPC_MDEF_MUL) {
        String[] splitReward = Mdef.split(",");
        MDEF[i][0] = Integer.parseInt(splitReward[0]);
        MDEF[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] mdef_data : MDEF) {
      if (level <= mdef_data[0]) {
        mdef = mdef_data[1];
        break;
      } 
    } 
    return mdef;
  }
  
  public static double RaidMDefMul(int level) {
    double[][] MDEF = new double[Config.RAID_MDEF_MUL.length][3];
    double mdef = 1.0D;
    int i = 0;
    if (Config.RAID_MDEF_MUL.length != 0)
      for (String Mdef : Config.RAID_MDEF_MUL) {
        String[] splitReward = Mdef.split(",");
        MDEF[i][0] = Integer.parseInt(splitReward[0]);
        MDEF[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] mdef_data : MDEF) {
      if (level <= mdef_data[0]) {
        mdef = mdef_data[1];
        break;
      } 
    } 
    return mdef;
  }
  
  public static double NpcPDefMul(int level) {
    double[][] PDEF = new double[Config.NPC_PDEF_MUL.length][3];
    double pdef = 1.0D;
    int i = 0;
    if (Config.NPC_PDEF_MUL.length != 0)
      for (String Pdef : Config.NPC_PDEF_MUL) {
        String[] splitReward = Pdef.split(",");
        PDEF[i][0] = Integer.parseInt(splitReward[0]);
        PDEF[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] pdef_data : PDEF) {
      if (level <= pdef_data[0]) {
        pdef = pdef_data[1];
        break;
      } 
    } 
    return pdef;
  }
  
  public static double RaidPDefMul(int level) {
    double[][] PDEF = new double[Config.RAID_PDEF_MUL.length][3];
    double pdef = 1.0D;
    int i = 0;
    if (Config.RAID_PDEF_MUL.length != 0)
      for (String Pdef : Config.RAID_PDEF_MUL) {
        String[] splitReward = Pdef.split(",");
        PDEF[i][0] = Integer.parseInt(splitReward[0]);
        PDEF[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] pdef_data : PDEF) {
      if (level <= pdef_data[0]) {
        pdef = pdef_data[1];
        break;
      } 
    } 
    return pdef;
  }
  
  public static double RaidRegenHP(int level) {
    double[][] REGEN_HP = new double[Config.RAID_REGEN_HP.length][3];
    double Regen_HP = 1.0D;
    int i = 0;
    if (Config.RAID_REGEN_HP.length != 0)
      for (String regen_hp : Config.RAID_REGEN_HP) {
        String[] splitReward = regen_hp.split(",");
        REGEN_HP[i][0] = Integer.parseInt(splitReward[0]);
        REGEN_HP[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] regenhp_data : REGEN_HP) {
      if (level <= regenhp_data[0]) {
        Regen_HP = regenhp_data[1];
        break;
      } 
    } 
    return Regen_HP;
  }
  
  public static double RaidRegenMP(int level) {
    double[][] REEGEN_MP = new double[Config.RAID_REGEN_MP.length][3];
    double Regen_MP = 1.0D;
    int i = 0;
    if (Config.RAID_REGEN_MP.length != 0)
      for (String regenmp : Config.RAID_REGEN_MP) {
        String[] splitReward = regenmp.split(",");
        REEGEN_MP[i][0] = Integer.parseInt(splitReward[0]);
        REEGEN_MP[i][1] = Double.parseDouble(splitReward[1]);
        i++;
      }  
    for (double[] regen_mp_data : REEGEN_MP) {
      if (level <= regen_mp_data[0]) {
        Regen_MP = regen_mp_data[1];
        break;
      } 
    } 
    return Regen_MP;
  }
}
