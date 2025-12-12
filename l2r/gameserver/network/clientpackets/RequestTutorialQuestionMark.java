package l2r.gameserver.network.clientpackets;

import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2r.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	// format: cd
	int _number = 0;
	
	@Override
	protected void readImpl()
	{
		_number = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		player.isntAfk();
		if (player.isInFightClub())
		{
			FightClubEventManager.getInstance().sendEventPlayerMenu(player);
		}
		else
		{
			Quest q = QuestManager.getQuest(255);
			if (q != null)
			{
				player.processQuestEvent(q.getName(), "QM" + _number, null);
			}
			
			if (_number == player.getObjectId())
			{
				// Achievements.getInstance().usebypass(player, "_bbs_achievements", null);
				Achievements.getInstance().onBypass(player, "_bbs_achievements", null);
			}
		}
	}
}