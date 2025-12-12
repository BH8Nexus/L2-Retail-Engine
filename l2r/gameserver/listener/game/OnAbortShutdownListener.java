package l2r.gameserver.listener.game;

import l2r.gameserver.listener.GameListener;

public interface OnAbortShutdownListener extends GameListener
{
	//void onAbortShutdown(Shutdown.ShutdownMode p0, int p1);
	void onAbortShutdown();
}
