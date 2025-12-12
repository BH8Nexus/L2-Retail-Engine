package l2r.gameserver.listener.game;

import l2r.gameserver.listener.GameListener;

public interface OnShutdownListener extends GameListener
{
	//void onShutdown(Shutdown.ShutdownMode p0);
	public void onShutdown();
}
