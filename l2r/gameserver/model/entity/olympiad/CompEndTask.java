package l2r.gameserver.model.entity.olympiad;


import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompEndTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(CompEndTask.class);

	@Override
	public void runImpl()
	{
		if (Olympiad.isOlympiadEnd())
			return;

		Olympiad._inCompPeriod = false;

		try
		{
			OlympiadManager manager = Olympiad._manager;

			// Ð•Ñ�Ð»Ð¸ Ð¾Ñ�Ñ‚Ð°Ð»Ð¸Ñ�ÑŒ Ð¸Ð³Ñ€Ñ‹, Ð¶Ð´ÐµÐ¼ Ð¸Ñ… Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ� ÐµÑ‰Ðµ Ð¾Ð´Ð½Ñƒ Ð¼Ð¸Ð½ÑƒÑ‚Ñƒ
			if (manager != null && !manager.getOlympiadGames().isEmpty())
			{
				ThreadPoolManager.getInstance().schedule(new CompEndTask(), 60000);
				return;
			}

			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_ENDED));
			_log.info("Olympiad System: Olympiad Game Ended");
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.warn("Olympiad System: Failed to save Olympiad configuration", e);
		}
		Olympiad.init();
	}
}