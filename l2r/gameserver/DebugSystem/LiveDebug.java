package l2r.gameserver.DebugSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;

public class LiveDebug
{
    private static final Map<Integer, Boolean> activeDebuggers = new ConcurrentHashMap<>();

    public static final String GREEN  = "<font color=\"00FF00\">";
    public static final String RED    = "<font color=\"FF0000\">";
    public static final String BLUE   = "<font color=\"3399FF\">";
    public static final String YELLOW = "<font color=\"FFFF00\">";
    public static final String ORANGE = "<font color=\"FF9900\">";

    // ----------------------------------------
    // TOGGLE ACTIVE
    // ----------------------------------------
    public static void toggle(Player player)
    {
        int objId = player.getObjectId();

        if (activeDebuggers.containsKey(objId))
        {
            activeDebuggers.remove(objId);
            player.sendMessage("Live debug disabled.");
        }
        else
        {
            activeDebuggers.put(objId, true);
            player.sendMessage("Live debug ENABLED.");
        }
    }

    public static boolean isActive(Player player)
    {
        if (player == null)
            return false;

        return activeDebuggers.containsKey(player.getObjectId());
    }

    // ----------------------------------------
    // SAFE SEND (CREATURE)
    // ----------------------------------------
    public static void sendEvent(Creature creature, String msg)
    {
        if (creature == null || !creature.isPlayer())
            return;

        Player p = creature.getPlayer();
        if (isActive(p))
            p.sendMessage(msg);
    }

    // ----------------------------------------
    // SAFE SEND (PLAYER DIRECT)
    // ----------------------------------------
    public static void sendEvent(Player player, String msg)
    {
        if (isActive(player))
            player.sendMessage(msg);
    }

    // ----------------------------------------
    // COLORED MESSAGE
    // ----------------------------------------
    public static void sendColoredEvent(Creature creature, String msg, String color)
    {
        if (creature == null || !creature.isPlayer())
            return;

        Player p = creature.getPlayer();
        if (!isActive(p))
            return;

        p.sendMessage(color + msg + "</font>");
    }

    // ============================================================
    // SEND EVENT TO BOTH ATTACKER + TARGET
    // ============================================================
    public static void sendEventToBothSides(Creature attacker, Creature target, String msg)
    {
        sendEvent(attacker, msg);
        sendEvent(target, msg);
    }

    // ============================================================
    // COLORED VERSION FOR BOTH SIDES
    // ============================================================
    public static void sendColoredEventToBothSides(Creature attacker, Creature target, String msg, String color)
    {
        sendColoredEvent(attacker, msg, color);
        sendColoredEvent(target, msg, color);
    }

    public static void sendBreakdown(Creature creature, DamageBreakdown info)
    {
        if (creature == null || !creature.isPlayer())
            return;

        Player player = creature.getPlayer();
        if (!isActive(player))
            return;

        String[] lines = info.toString().split("\n");
        for (String line : lines)
            player.sendMessage(LiveDebug.BLUE + line + "</font>");
    }
}
