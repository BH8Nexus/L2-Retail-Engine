package l2r.gameserver.DebugSystem.spectator;

public class SpectatorSession
{
    public final int spectatorId;
    public final int watchedPlayerId;

    public long lastUpdate;

    public SpectatorSession(int spectatorId, int watchedPlayerId)
    {
        this.spectatorId = spectatorId;
        this.watchedPlayerId = watchedPlayerId;
        this.lastUpdate = System.currentTimeMillis();
    }
}
