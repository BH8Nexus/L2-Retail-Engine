package l2r.gameserver.utils;

import org.apache.log4j.Logger;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.tables.AdminTable;

public final class IllegalPlayerAction implements Runnable
{
	//private static final org.apache.commons.logging.Log log = LogFactory.getLog("illegal-actions");
	private static final Logger log = Logger.getLogger(IllegalPlayerAction.class.getName());
	
	String etc_str1;
	String etc_str2;
	int isBug;
	Player actor;
	
	public static final int INFO = 0;
	public static final int WARNING = 1;
	public static final int CRITICAL = 2;
	
	public IllegalPlayerAction(Player actor, String etc_str1, String etc_str2, int isBug)
	{
		this.etc_str1 = etc_str1;
		this.etc_str2 = etc_str2;
		this.isBug = isBug;
		this.actor = actor;
	}
	
	@Override
	public void run()
	{
		StringBuilder msgb = new StringBuilder(160);
		int punishment = -1;
		msgb.append(actor.getName()).append(" ").append(isBug > 1 ? " use bug: " : " illegal action: " + etc_str1 + " " + etc_str2);
		
		switch (isBug)
		{
			case INFO:
				punishment = 0;
				break;
			case WARNING:
				punishment = Config.DEFAULT_PUNISH;
				break;
			case CRITICAL:
				punishment = Config.BUGUSER_PUNISH;
				break;
		}
		
		if (actor.isGM())
		{
			punishment = 0;
		}
		
		switch (punishment)
		{
			case 0:
				msgb.append(" punish: none");
				actor.sendMessage(new CustomMessage("l2r.gameserver.Util.IllegalAction.case0", actor));
				return;
			case 1:
				actor.sendMessage(new CustomMessage("l2r.gameserver.Util.IllegalAction.case1", actor));
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				actor.logout();
				msgb.append(" punish: kicked");
				break;
			case 2:
				actor.sendMessage(new CustomMessage("l2r.gameserver.Util.IllegalAction.case2", actor));
				actor.setAccessLevel(-100);
				//actor.setAccountAccesslevel(-100, "Autoban: " + etc_str2 + " in " + etc_str1, -1);
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				actor.logout();
				msgb.append(" punish: banned");
		}
		AdminTable.broadcastMessageToGMs(msgb.toString());
		log.info(msgb);
	}
}