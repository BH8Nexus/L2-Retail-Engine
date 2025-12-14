package l2r.gameserver.model.beta;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExShowTrace;

public class BetaSupport implements IVoicedCommandHandler
{
    private static final String[] COMMANDS = { "beta" };

    @Override
    public boolean useVoicedCommand(String command, Player player, String target)
    {
        if (!command.startsWith("beta"))
            return false;

        // extract sub-command
        String[] parts = command.split(" ");
        int action = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

        switch (action)
        {
            case 1:
                resetGear(player);
                break;

            case 2:
                toggleDebug(player);
                break;

            case 3:
                showRangeCircle(player);
                break;

            default:
                showMenu(player);
                break;
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return COMMANDS;
    }

    // ---------------------------------------------------------------
    // MENU
    // ---------------------------------------------------------------
    private void showMenu(Player player)
    {
        String debug = player.getVar("betaDebug") == null ? "false" : player.getVar("betaDebug");

        player.sendMessage("=== Beta Panel ===");
        player.sendMessage("1. Reset Gear");
        player.sendMessage("2. Toggle Debug (Now: " + debug + ")");
        player.sendMessage("3. Show Range Circle");
        player.sendMessage("Use: .beta <number>");
    }

    // ---------------------------------------------------------------
    // RESET GEAR
    // ---------------------------------------------------------------
    private void resetGear(Player player)
    {
        Inventory inv = player.getInventory();

        int[] slots = {
                Inventory.PAPERDOLL_LHAND,
                Inventory.PAPERDOLL_GLOVES,
                Inventory.PAPERDOLL_CHEST,
                Inventory.PAPERDOLL_LEGS,
                Inventory.PAPERDOLL_FEET,
                Inventory.PAPERDOLL_HEAD,
                Inventory.PAPERDOLL_RHAND
        };

        for (int slot : slots)
        {
            ItemInstance item = inv.getPaperdollItem(slot);
            if (item != null)
                inv.unEquipItem(item);
        }

        player.broadcastUserInfo(true);
        player.sendMessage("Gear reset completed.");
    }

    // ---------------------------------------------------------------
    // TOGGLE DEBUG
    // ---------------------------------------------------------------
    private void toggleDebug(Player player)
    {
        boolean mode = "true".equals(player.getVar("betaDebug"));

        player.setVar("betaDebug", String.valueOf(!mode), -1);
        player.sendMessage("Debug Mode = " + (!mode));
    }

    // ---------------------------------------------------------------
    // RANGE CIRCLE
    // ---------------------------------------------------------------
    private void showRangeCircle(Player player)
    {
        int radius = 600;
        int points = 32;

        ExShowTrace trace = new ExShowTrace();

        double step = 2 * Math.PI / points;

        for (int i = 0; i < points; i++)
        {
            double angle = i * step;

            int x = (int)(player.getX() + radius * Math.cos(angle));
            int y = (int)(player.getY() + radius * Math.sin(angle));
            int z = player.getZ();

            trace.addTrace(x, y, z, 500);
        }

        player.sendPacket(trace);
        player.sendMessage("Range circle displayed.");
    }
}
