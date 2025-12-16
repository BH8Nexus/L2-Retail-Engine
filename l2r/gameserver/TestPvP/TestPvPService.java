package l2r.gameserver.TestPvP;

import l2r.gameserver.DebugSystem.testpvp.RangeType;
import l2r.gameserver.DebugSystem.testpvp.TestPvPSnapshotService;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.effects.EffectCubic;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.ItemFunctions;

public class TestPvPService
{
    // Visual-only dummy skills (must exist in skills XML)
    private static final int SKILL_MELEE_RADIUS  = 60001;
    private static final int SKILL_ARCHER_RADIUS = 60002;

    private static final int[] RANGE_CYCLE =
            {
                    120,   // melee
                    400,   // mid
                    900    // archer
            };

    private static final long RANGE_STEP_DELAY = 3500; // ms


    // ============================================================
    // ENTER TEST PVP
    // ============================================================

    public static void prepareBaseline(Player player)
    {
        // 0. Safety
        if (player == null)
            return;

        // 1. Unequip everything
        for (ItemInstance item : player.getInventory().getItems())
        {
            if (item != null && item.isEquipped())
                player.getInventory().unEquipItem(item);
        }

        // 2. Clear inventory (TEST ONLY)
        for (ItemInstance item : player.getInventory().getItems())
        {
            if (item != null)
                player.getInventory().destroyItem(item, "TestPvP");
        }

        // 3. Clear effects
        player.getEffectList().stopAllEffects();

        // 4. Clear cubics (L2R-safe)
        if (!player.getCubics().isEmpty())
        {
            player.getEffectList().stopAllSkillEffects(EffectType.Cubic);

            for (EffectCubic cubic : player.getCubics())
                player.removeCubic(cubic.getId());
        }

        // 5. Unsummon pet
        if (player.getPet() != null)
            player.getPet().unSummon();

        // 6. Give baseline gear & consumables
        giveBaselineGear(player);
        giveConsumables(player);

        // 7. Visual test ranges
        spawnTestRanges(player);

        // 8. Set TestPvP flag
        player.setVar("TestPvP", "1");

        // 9. Refresh client
        player.broadcastUserInfo(true);

        player.sendMessage("Test PvP mode enabled (baseline 0 enchant).");
    }

    // ============================================================
    // EXIT TEST PVP
    // ============================================================

    public static void exitTest(Player player)
    {
        if (player == null)
            return;

        if (!player.getVarB("TestPvP", false))
        {
            player.sendMessage("You are not in Test PvP mode.");
            return;
        }

        // 1. Remove visual ranges
        removeTestRanges(player);

        // 2. Unequip everything
        for (ItemInstance item : player.getInventory().getItems())
        {
            if (item != null && item.isEquipped())
                player.getInventory().unEquipItem(item);
        }

        // 3. Remove ONLY TestPvP items
        int[] TEST_PVP_ITEMS =
                {
                        // Weapons
                        6580, // Tallum Blade
                        7579, // Soul Bow
                        6608, // Arcana Mace

                        // Armor
                        2380, 2377, 2378, 2379, 2381,

                        // Consumables
                        1467, 1468, 1539, 1061, 728
                };

        ItemInstance[] items = player.getInventory().getItems();

        for (ItemInstance item : items)
        {
            if (item == null)
                continue;

            for (int id : TEST_PVP_ITEMS)
            {
                if (item.getItemId() == id)
                {
                    player.getInventory().destroyItem(item, "TestPvPExit");
                    break;
                }
            }
        }

        // 4. Clear effects
        player.getEffectList().stopAllEffects();

        // 5. Clear cubics safely
        if (!player.getCubics().isEmpty())
        {
            for (EffectCubic cubic : player.getCubics())
                player.removeCubic(cubic.getId());
        }

        // 6. Unsummon pet
        if (player.getPet() != null)
            player.getPet().unSummon();

        // 7. Clear flag
        player.unsetVar("TestPvP");

        // 8. Refresh client
        player.broadcastUserInfo(true);

        player.sendMessage("Test PvP mode exited.");
    }

    // ============================================================
    // GEAR / ITEMS
    // ============================================================

    private static void giveBaselineGear(Player player)
    {
        ClassId classId = player.getClassId();
        int weaponId;

        // 🔒 SAFETY GUARD (CRITICAL)
        if (classId == null || classId.getType2() == null)
        {
            weaponId = 6580; // fallback melee weapon
        }
        else
        {
            switch (classId.getType2())
            {
                case Warrior:
                case Knight:
                    weaponId = 6580; // Tallum Blade
                    break;

                case Rogue:
                    weaponId = 7579; // Soul Bow
                    break;

                case Wizard:
                case Summoner:
                case Healer:
                case Enchanter:
                    weaponId = 6608; // Arcana Mace
                    break;

                default:
                    weaponId = 6580;
                    break;
            }
        }

        giveItem(player, weaponId, 1);

        // Common armor set
        giveItem(player, 2380, 1);
        giveItem(player, 2377, 1);
        giveItem(player, 2378, 1);
        giveItem(player, 2379, 1);
        giveItem(player, 2381, 1);
    }


    private static void giveConsumables(Player player)
    {
        giveItem(player, 1467, 5000); // Soulshots
        giveItem(player, 1468, 2000); // Spiritshots
        giveItem(player, 1539, 200);  // CP Pot
        giveItem(player, 1061, 200);  // HP Pot
        giveItem(player, 728, 50);    // GHP
    }

    private static void giveItem(Player player, int itemId, long count)
    {
        ItemFunctions.addItem(player, itemId, count, true, "TestPvP");

        ItemInstance item = player.getInventory().getItemByItemId(itemId);
        if (item != null)
            item.setEnchantLevel(0); // baseline
    }

    // ============================================================
    // VISUAL RANGE SKILLS
    // ============================================================

    private static void spawnTestRanges(Player player)
    {
        Skill melee  = SkillTable.getInstance().getInfo(SKILL_MELEE_RADIUS, 1);
        Skill archer = SkillTable.getInstance().getInfo(SKILL_ARCHER_RADIUS, 1);

        try
        {
            if (melee != null)
                player.altUseSkill(melee, player);

            if (archer != null)
                player.altUseSkill(archer, player);
        }
        catch (Exception e)
        {
            player.sendMessage("TestPvP range visuals failed.");
        }
    }

    private static void removeTestRanges(Player player)
    {
        player.getEffectList().stopEffect(SKILL_MELEE_RADIUS);
        player.getEffectList().stopEffect(SKILL_ARCHER_RADIUS);
    }

    public static void applyEnchant(Player player, int enchant)
    {
        if (!player.getVarB("TestPvP", false))
        {
            player.sendMessage("You are not in Test PvP mode.");
            return;
        }

        for (ItemInstance item : player.getInventory().getItems())
        {
            if (item == null)
                continue;

            if (!item.isEquipped())
                continue;

            // weapon / armor / jewels only
            if (item.isWeapon() || item.isArmor() || item.isAccessory())
            {
                item.setEnchantLevel(enchant);
            }
        }

        player.broadcastUserInfo(true);
        player.sendMessage("Test PvP enchant set to +" + enchant);
    }
    public static void startRangeCycle(Player player, Player target)
    {
        if (!player.getVarB("TestPvP", false))
        {
            player.sendMessage("You are not in Test PvP mode.");
            return;
        }

        if (target == null)
        {
            player.sendMessage("Target not found.");
            return;
        }

        player.sendMessage("TestPvP range cycle started.");

        new Thread(() ->
        {
            int idx = 0;

            while (player.getVarB("TestPvP", false))
            {
                try
                {
                    int range = RANGE_CYCLE[idx % RANGE_CYCLE.length];
                    idx++;

                    teleportAtRange(player, target, range);

                    Thread.sleep(RANGE_STEP_DELAY);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }, "TestPvPRangeCycle-" + player.getObjectId()).start();
    }
    private static void teleportAtRange(Player player, Player target, int range)
    {
        // 🔹 SNAPSHOT BEFORE MOVE
        try
        {
            TestPvPSnapshotService.snapshot(
                    player,
                    target,
                    getRangeType(range)
            );
        }
        catch (Exception e)
        {
            // never break test cycle
        }

        double angle = Math.random() * Math.PI * 2;

        int x = (int) (target.getX() + range * Math.cos(angle));
        int y = (int) (target.getY() + range * Math.sin(angle));
        int z = target.getZ();

        player.teleToLocation(x, y, z);
    }

    private static RangeType getRangeType(int range)
    {
        if (range <= 200)
            return RangeType.MELEE;

        if (range <= 600)
            return RangeType.MID;

        return RangeType.ARCHER;
    }

}
