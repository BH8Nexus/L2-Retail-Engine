package l2r.gameserver.DebugSystem;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class SpectatorFollowService
{
    private static final Map<Integer, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public static void start(Player gm, Player target)
    {
        stop(gm);

        ScheduledFuture<?> task = ThreadPoolManager.getInstance()
                .scheduleAtFixedRate(() ->
                        {
                            if (gm == null || target == null || !gm.isInObserverMode())
                            {
                                stop(gm);
                                return;
                            }

                            gm.teleToLocation(target.getLoc());
                        },
                        1000, 1000);

        tasks.put(gm.getObjectId(), task);
    }


    public static void stop(Player gm)
    {
        ScheduledFuture<?> t = tasks.remove(gm.getObjectId());
        if (t != null)
            t.cancel(false);
    }
}
