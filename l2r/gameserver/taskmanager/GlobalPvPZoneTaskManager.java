package l2r.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.cache.ImagesCache;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;

public class GlobalPvPZoneTaskManager extends RunnableImpl
{
	private static final long DELAY = 1000L;
	
	private ScheduledFuture<?> _feature;
	private boolean _globalPvPOn = false;
	
	public void startThread()
	{
		if ((_feature == null) || _feature.isCancelled())
		{
			_feature = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this, DELAY, DELAY);
		}
	}
	
	public void stopThread()
	{
		if ((_feature != null) && !_feature.isCancelled())
		{
			_feature.cancel(true);
		}
	}
	
	@Override
	public void runImpl()
	{
		if (isGlobalPvpOn())
		{
			givePvPFlags();
		}
	}
	
	private static void givePvPFlags()
	{
		for (Player player : GameObjectsStorage.getAllPlayersCopy())
		{
			if (player.isInActiveGlobalPvPZone())
			{
				player.startPvPFlag(null);
			}
		}
	}
	
	public void setGlobalPvpOn(boolean globalPvPOn)
	{
		_globalPvPOn = globalPvPOn;
	}
	
	public boolean isGlobalPvpOn()
	{
		return _globalPvPOn;
	}
	
	public void sendMainHtmlToPlayer(Player player)
	{
		String content = HtmCache.getInstance().getNotNull("events/globalPvpEvent/main.htm", player);
		content = content.replace("%serverName%", Config.SERVER_NAME);
		content = ImagesCache.getInstance().sendUsedImages(content, player);
		player.sendPacket(new TutorialShowHtml(content));
	}
	
	public static GlobalPvPZoneTaskManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static final GlobalPvPZoneTaskManager instance = new GlobalPvPZoneTaskManager();
	}
}
