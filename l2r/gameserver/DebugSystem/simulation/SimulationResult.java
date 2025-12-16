package l2r.gameserver.DebugSystem.simulation;

public class SimulationResult
{
    public final double before;
    public final double after;
    public final int hits;

    public SimulationResult(double before, double after, int hits)
    {
        this.before = before;
        this.after  = after;
        this.hits   = hits;
    }

    public double deltaPercent()
    {
        if (before == 0) return 0;
        return ((after - before) / before) * 100.0;
    }
}
