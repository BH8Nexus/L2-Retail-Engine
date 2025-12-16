package l2r.gameserver.DebugSystem.spectator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpectatorRegistry
{
    private static final Map<Integer, SpectatorSession> SESSIONS = new ConcurrentHashMap<>();

    public static void start(int spectatorId, int targetId)
    {
        SESSIONS.put(spectatorId, new SpectatorSession(spectatorId, targetId));
    }

    public static void stop(int spectatorId)
    {
        SESSIONS.remove(spectatorId);
    }

    public static SpectatorSession get(int spectatorId)
    {
        return SESSIONS.get(spectatorId);
    }
}
