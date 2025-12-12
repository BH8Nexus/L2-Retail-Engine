package l2r.gameserver.model.instances;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.*;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;

public class LuckPing extends DefaultAI
{

    private static final int MAX_RADIUS = 500;
    private static final Skill s_display_bug_of_fortune1 = SkillTable.getInstance().getInfo(6045, 1);
    //private static final Skill s_display_jackpot_firework = SkillTable.getInstance().getInfo(5778, 1);

    private long _nextEat;
    private int i_ai2, actor_lvl, prev_st;
    private boolean _firstSaid;
    // Npc id
    private static final int WINGLESS_LUCKY_PIG = 2502;
    private static final int GOLDEN_WINGLESS_LUCKY_PIG = 2503;

    // Chance drop list
    //private static final int WINGLESS_LUCKY_PIG_LEVEL_52_DROP_CHANCE = 90;
    //private static final int WINGLESS_LUCKY_PIG_LEVEL_70_DROP_CHANCE = 90;
    //private static final int WINGLESS_LUCKY_PIG_LEVEL_80_DROP_CHANCE = 90;

    // Golden Wingless Lucky Pig Drop Chance (Values 0 - 99)
    //private static final int GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_52_DROP_CHANCE = 99;
    //private static final int GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_70_DROP_CHANCE = 99;
    //private static final int GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_80_DROP_CHANCE = 99;

    public final static int Lucky_Pig = 2501;

    // Lucky Pig Spawn Chances %
    public final static float Lucky_Pig_Level_52_Spawn_Chance = 90.3f;
    public final static float Lucky_Pig_Level_70_Spawn_Chance = 90.3f;
    public final static float Lucky_Pig_Level_80_Spawn_Chance = 90.3f;
    public final static int despawnTime = 10; // in minutes

    //@formatter:off
    // Monsters IDs
    public final static int Lucky_Pig_Level_52[] =
            {
                    // Enchanted Valley
                    20589, 20590, 20591, 20592, 20593, 20594, 20595, 20596,
                    20597, 20598, 20599
            };

    public final static int Lucky_Pig_Level_70[] =
            {
                    // Forest of the Dead
                    18119, 21555, 21556, 21547, 21553, 21548, 21557, 21559,
                    21560, 21561, 21562, 21563, 21564, 21565, 21566, 21568,
                    21567, 21596, 21572, 21573, 21571, 21570, 21574, 21576,
                    21599, 21580, 21581, 21579, 21582, 21578, 21586, 21587,
                    21583, 21585, 21590, 21593, 21588,
                    // Valley of Saints
                    21520, 21521, 21524, 21523, 21526, 21529, 21541, 21531,
                    21530, 21533, 21532, 21536, 21535, 21537, 21539, 21544
            };

    public final static int Lucky_Pig_Level_80[] =
            {
                    // Beast Farm
                    18873, 18880, 18887, 18894, 18906, 18907, 18874, 18875,
                    18876, 18877, 18878, 18879, 18881, 18882, 18883, 18884,
                    18885, 18886, 18888, 18889, 18890, 18891, 18892, 18893,
                    18895, 18896, 18897, 18898, 18899, 18900,
                    // Plains of the Lizardmen
                    22768, 22769, 22773, 22772, 22771, 22770, 22774,
                    // Sel Mahum Training Grounds
                    18908, 22780, 22782, 22784, 22781, 22783, 22785, 22776,
                    22786, 22787, 22788, 22775, 22777, 22778,
                    // Fields of Silence & Fields of Whispers
                    22651, 22654, 22650, 22655, 22652, 22658, 22659,
                    // Crypts of Disgrace
                    22704, 22703, 22705,
                    // Den of Evil
                    22701, 22691, 22698, 22695, 22694, 22696, 22692, 22693,
                    22699, 22698, 22697, 18807, 22702,
                    // Primeval Island
                    22196, 22197, 22198, 22218, 22223, 22203, 22204, 22205,
                    22220, 22225, 22743, 22745, 22200, 22201, 22202, 22219,
                    22224, 22742, 22744, 22199, 22212, 22213, 22222, 22211,
                    22227, 22208, 22209, 22210, 22221, 22226, 22214,
                    // Dragon Valley
                    22815, 22822, 22823, 22824, 22862, 22818, 22819, 22860,
                    22829, 22858, 22830, 22828, 22827, 22826, 22861, 22825
            };

    // Drop list items
    //private static final int WINGLESS_LUCKY_PIG_LEVEL_52_DROP_ID = 8755; // Top-Grade Life Stone - Level 52

    private int[] Cristall = {9552, 9553, 9554, 9555, 9556, 9557};
    private int[] Cristall_Dush = {5577, 5578, 5579};


    public LuckPing(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected void onEvtSpawn()
    {
        super.onEvtSpawn();

        NpcInstance actor = getActor();

        addTimer(7778, 1000);

        if (getFirstSpawned(actor))
        {
            i_ai2 = 0;
            prev_st = 0;
        }
        else
        {
            i_ai2 = 3;
            prev_st = 3;
        }
        _firstSaid = false;

        actor_lvl = actor.getLevel();
    }

    @Override
    protected void onEvtArrived()
    {
        super.onEvtArrived();
        NpcInstance actor = getActor();
        if (actor == null)
        {
            return;
        }

        if (actor.getNpcId() == WINGLESS_LUCKY_PIG || actor.getNpcId() == GOLDEN_WINGLESS_LUCKY_PIG)
            return;


        if (i_ai2 > 9)
        {
            if (!_firstSaid)
            {
                Functions.npcSayInRange(actor, 600, NpcString.IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE);
                _firstSaid = true;
            }
            return;
        }
        ItemInstance closestItem = null;
        if (_nextEat < System.currentTimeMillis())
        {
            for (GameObject obj : World.getAroundObjects(actor, 20, 200))
            {
                //if (obj.isItem() && (((ItemInstance) obj).getItemId() == 57))
                if (obj.isItem() && ((ItemInstance) obj).isStackable() && ((ItemInstance) obj).isAdena())
                {
                    closestItem = (ItemInstance) obj;
                }
            }

            if ((closestItem != null) && (closestItem.getCount() < Config.MIN_ADENA_TO_EAT))
            {
                closestItem.deleteMe();
                actor.altUseSkill(s_display_bug_of_fortune1, actor);
                Functions.npcSayInRange(actor, 600, NpcString.YUMYUM_YUMYUM);
                _firstSaid = false;

                if ((i_ai2 == 2) && getFirstSpawned(actor))
                {
                    NpcInstance npc = NpcHolder.getInstance().getTemplate(getCurrActor(actor)).getNewInstance();
                    npc.setLevel(actor.getLevel());
                    npc.setSpawnedLoc(actor.getLoc());
                    npc.setReflection(actor.getReflection());
                    npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
                    npc.spawnMe(npc.getSpawnedLoc());
                    actor.doDie(actor);
                    actor.deleteMe();
                    addTimer(1500, 0, null, Config.TIME_IF_NOT_FEED * 60000);
                }

                i_ai2++;

                _nextEat = System.currentTimeMillis() + (Config.INTERVAL_EATING * 1000);
            }
            else if ((closestItem != null) && (closestItem.getCount() < Config.MIN_ADENA_TO_EAT) && !_firstSaid)
            {
                Functions.npcShout(actor, "Is this all? I want More!!! I won't eat below " + Config.MIN_ADENA_TO_EAT + " Adena!!!");
                _firstSaid = true;
            }
        }
    }

    private boolean getFirstSpawned(NpcInstance actor)
    {
        if ((actor.getNpcId() == GOLDEN_WINGLESS_LUCKY_PIG) || (actor.getNpcId() == WINGLESS_LUCKY_PIG))
        {
            return false;
        }
        return true;
    }

    private int getCurrActor(NpcInstance npc)
    {
        if (Rnd.chance(Config.CHANCE_GOLD_LAKFI))
        {
            return GOLDEN_WINGLESS_LUCKY_PIG;
        }
        return WINGLESS_LUCKY_PIG;
    }

    @Override
    protected boolean thinkActive()
    {
        NpcInstance actor = getActor();
        if ((actor == null) || actor.isDead())
        {
            return true;
        }

        if (actor.getNpcId() == WINGLESS_LUCKY_PIG || actor.getNpcId() == GOLDEN_WINGLESS_LUCKY_PIG)
            return true;

        if (!actor.isMoving && (_nextEat < System.currentTimeMillis()))
        {
            ItemInstance closestItem = null;
            for (GameObject obj : World.getAroundObjects(actor, MAX_RADIUS, 200))
            {
                //if (obj.isItem() && (((ItemInstance) obj).getItemId() == 57))
                if(obj.isItem() && ((ItemInstance) obj).isStackable() && ((ItemInstance) obj).isAdena())
                {
                    closestItem = (ItemInstance) obj;
                }
            }

            if (closestItem != null)
            {
                actor.moveToLocation(closestItem.getLoc(), 0, true);
            }
        }

        return false;
    }

    public int getChance(int stage)
    {
        switch (stage)
        {
            case 4:
                return 10;
            case 5:
                return 20;
            case 6:
                return 40;
            case 7:
                return 60;
            case 8:
                return 70;
            case 9:
                return 80;
            case 10:
                return 100;
            default:
                return 0;
        }
    }


    @Override
    protected void onEvtDead(Creature killer){
        super.onEvtDead(killer);
        NpcInstance actor = getActor();

        if (actor == null)
            return;
        int lvl = actor.getLevel();


        Player player = killer.getPlayer();
        if (player != null)
        {

            if (actor.getNpcId() == WINGLESS_LUCKY_PIG)
                switch (lvl){
                    case 52:
                        actor.dropItem(killer.getPlayer(), 8755, Rnd.get(1,2));
                        return;
                    case 70:
                        actor.dropItem(killer.getPlayer(), Cristall_Dush[Rnd.get(3)], Rnd.get(1,2));
                        return;
                    case 80:
                        actor.dropItem(killer.getPlayer(), Cristall[Rnd.get(6)], Rnd.get(1,2));
                        return;}
            if (actor.getNpcId() == GOLDEN_WINGLESS_LUCKY_PIG)
                switch (lvl) {
                    case 52:
                        actor.dropItem(killer.getPlayer(), 8755, Rnd.get(1, 2));
                        actor.dropItem(killer.getPlayer(), 14678, 1);
                        return;
                    case 70:
                        actor.dropItem(killer.getPlayer(), Cristall_Dush[Rnd.get(3)], Rnd.get(1, 2));
                        actor.dropItem(killer.getPlayer(), 14679, 1);
                        return;
                    case 80:
                        actor.dropItem(killer.getPlayer(), Cristall[Rnd.get(6)], Rnd.get(1, 2));
                        actor.dropItem(killer.getPlayer(), 14680, 1);
                        return;
                }
        }
        //getActor().endDecayTask();
    }

    @Override
    protected void onEvtTimer(int timerId, Object arg1, Object arg2)
    {
        NpcInstance actor = getActor();
        if (actor == null)
        {
            return;
        }

        if (timerId == 7778)
        {
            switch (i_ai2)
            {
                case 0:
                    Functions.npcSayInRange(actor, 600, NpcString.IF_YOU_HAVE_ITEMS_PLEASE_GIVE_THEM_TO_ME);
                    break;
                case 1:
                    Functions.npcSayInRange(actor, 600, NpcString.MY_STOMACH_IS_EMPTY);
                    break;
                case 2:
                    Functions.npcSayInRange(actor, 600, NpcString.IM_HUNGRY_IM_HUNGRY);
                    break;
                case 3:
                    Functions.npcSayInRange(actor, 600, NpcString.I_FEEL_A_LITTLE_WOOZY);
                    break;
                case 4:
                    Functions.npcSayInRange(actor, 600, NpcString.IM_STILL_NOT_FULL);
                    break;
                case 5:
                    Functions.npcSayInRange(actor, 600, NpcString.IM_STILL_HUNGRY);
                    break;
                case 6:
                    Functions.npcSayInRange(actor, 600, NpcString.NOW_ITS_TIME_TO_EAT);
                    break;
                case 7:
                    Functions.npcSayInRange(actor, 600, NpcString.GIVE_ME_SOMETHING_TO_EAT);
                    break;
                case 8:
                    Functions.npcSayInRange(actor, 600, NpcString.IM_STILL_HUNGRY_);
                    break;
                case 9:
                    Functions.npcSayInRange(actor, 600, NpcString.I_ALSO_NEED_A_DESSERT);
                    break;
                case 10:
                    Functions.npcSayInRange(actor, 600, NpcString.IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE);
                    break;
            }

            addTimer(7778, 10000 + (Rnd.get(10) * 1000));
        }

        if (timerId == 1500)
        {
            if ((prev_st == i_ai2) && (prev_st != 0) && (i_ai2 != 10))
            {
                actor.doDie(actor);
            }
            else
            {
                prev_st = i_ai2;
                addTimer(1500, Config.TIME_IF_NOT_FEED * 60000);
            }

        }
        else
        {
            super.onEvtTimer(timerId, arg1, arg2);
        }
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage)
    {

    }

    @Override
    protected void onEvtAggression(Creature target, int aggro)
    {
    }

    public static void addSpawn(int monsterId, Location loc, int heading, int despawnDelay, boolean isSummonSpawn)
    {
        try
        {
            SimpleSpawner spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(monsterId));
            spawn.setLoc(loc);
            spawn.setAmount(1);
            spawn.setHeading(heading);
            spawn.setReflection(ReflectionManager.DEFAULT);
            spawn.stopRespawn();

            NpcInstance npc = spawn.spawnOne();
            npc.scheduleDespawn(despawnDelay);
        }
        catch (Exception e)
        {
            _log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
