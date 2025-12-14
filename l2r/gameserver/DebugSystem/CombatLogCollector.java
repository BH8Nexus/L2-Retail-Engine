package l2r.gameserver.DebugSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatLogCollector
{
    private static final int MAX_LOGS = 200;

    private static final Map<Integer, Deque<CombatLogEntry>> playerLogs =
            new ConcurrentHashMap<>();

    // -----------------------------
    // ADD NEW LOG ENTRY
    // -----------------------------
    public static void push(int playerId, CombatLogEntry entry)
    {
        Deque<CombatLogEntry> list =
                playerLogs.computeIfAbsent(playerId, k -> new ArrayDeque<>());

        if (list.size() >= MAX_LOGS)
            list.removeFirst();

        list.addLast(entry);
    }

    // -----------------------------
    // GET LOG LIST
    // -----------------------------
    public static List<CombatLogEntry> get(int playerId)
    {
        return new ArrayList<>(
                playerLogs.getOrDefault(playerId, new ArrayDeque<>())
        );
    }

    // -----------------------------
    // CLEAR LOGS
    // -----------------------------
    public static void clear(int playerId)
    {
        Deque<CombatLogEntry> list = playerLogs.get(playerId);
        if (list != null)
            list.clear();
    }
}
