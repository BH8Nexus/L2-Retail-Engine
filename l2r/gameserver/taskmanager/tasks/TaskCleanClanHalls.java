package l2r.gameserver.taskmanager.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2r.gameserver.taskmanager.TaskTypes;
import l2r.gameserver.utils.Log;

public class TaskCleanClanHalls extends Task
{
	private static final Logger _log = LoggerFactory.getLogger(TaskCleanClanHalls.class);
	private static final String NAME = "TaskCleanClanHalls";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_log.info("Clean Clan Halls Global Task: launched.");
		int unactivMembers = 0;
		for (ClanHall ch : ResidenceHolder.getInstance().getResidenceList(ClanHall.class))
		{
			if ((ch != null) && (ch.getOwner() != null))
			{
				for (UnitMember cm : ch.getOwner())
				{
					if ((cm != null) && ((System.currentTimeMillis() - (CharacterDAO.getInstance().getLastAccessTime(cm.getName()) * 1000)) > (Config.DAYS_TO_CHECK_FOR_CH_DELETE * 24 * 60 * 60000)))
					{
						unactivMembers++;
					}
				}
				
				if (Config.MIN_PLAYERS_IN_CLAN_TO_KEEP_CH > (ch.getOwner().getAllMembers().size() - unactivMembers))
				{
					for (Player member : ch.getOwner().getOnlineMembers(0))
					{
						if (member != null)
						{
							member.sendMessage("Clan Hall has been removed from your clan due unactive players!");
							member.updatePledgeClass();
							member.broadcastUserInfo(true);
						}
					}
					
					Log.add("Remove " + ch.getName() + "  clanhall from clan " + ch.getOwner().getName() + "(id:" + ch.getOwner().getClanId() + "), coz system found " + unactivMembers + " innactive members, from total: " + ch.getOwner().getAllSize() + "", "residence");
					ch.changeOwner(null);
				}
			}
		}
		
		_log.info("Clean Clan Halls EndGlobal Task: completed.");
	}
	
	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "7", "06:30:00", "");
	}
}