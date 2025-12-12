package l2r.gameserver.network.clientpackets;

import java.util.Map;

import l2r.gameserver.Config;
import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.handler.bypass.BypassHandler;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.events.fightclubmanager.FightClubEventManager;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.CharacterEmails;
import l2r.gameserver.randoms.CharacterIntro;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.utils.AccountEmail;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	// format: cS
	
	String _bypass = null;
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		// if ("close".equalsIgnoreCase(_bypass))
		// {
		// player.sendPacket(TutorialCloseHtml.STATIC);
		// return;
		// }
		
		if (player.isInFightClub())
		{
			FightClubEventManager.getInstance().requestEventPlayerMenuBypass(player, _bypass);
		}
		
		if (_bypass.startsWith("emailvalidation") && Config.ENABLE_EMAIL_VALIDATION)
		{
			String[] cm = _bypass.split(" ");
			
			if (cm.length < 3)
			{
				// Original Message: Please fill all required fields.
				player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestTutorialPassCmdToServer.message1", player));
				return;
			}
			
			String command = cm[0];
			String email1 = cm[1];
			String email2 = cm[2];
			String friend = "";
			if (cm.length > 3)
			{
				friend = cm[3];
			}
			
			CharacterEmails.change(command, player, email1, email2, friend);
		}
		// Synerge - Achievements system
		else if (Config.ENABLE_ACHIEVEMENTS && _bypass.startsWith("_bbs_achievements"))
		{
			String[] cm = _bypass.split(" ");
			if (_bypass.startsWith("_bbs_achievements_cat"))
			{
				int page = 0;
				if (cm.length < 1)
				{
					page = 1;
				}
				else
				{
					page = Integer.parseInt(cm[2]);
				}
				
				Achievements.getInstance().generatePage(player, Integer.parseInt(cm[1]), page);
			}
			else
			{
				Achievements.getInstance().onBypass(player, _bypass, cm);
			}
		}
		// else if (_bypass.startsWith("_bbs_achievements") && Config.ENABLE_ACHIEVEMENTS)
		// {
		// String[] cm = _bypass.split(" ");
		// if (_bypass.startsWith("_bbs_achievements_cat"))
		// {
		// int page = 0;
		// if (cm.length < 1)
		// {
		// page = 1;
		// }
		// else
		// {
		// page = Integer.parseInt(cm[2]);
		// }
		//
		// Achievements.getInstance().generatePage(player, Integer.parseInt(cm[1]), page);
		// }
		// else
		// {
		// Achievements.getInstance().usebypass(player, _bypass, cm);
		// }
		// }
		// else if (Config.ENABLE_ACHIEVEMENTS && _bypass.startsWith("_bbs_achievements_close"))
		// {
		// String[] cm = _bypass.split(" ");
		//
		// Achievements.getInstance().usebypass(player, _bypass, cm);
		// return;
		// }
		// Synerge - Support for handling bbs events on tutorial windows
		else if (_bypass.startsWith("_bbs"))
		{
			if (!Config.COMMUNITYBOARD_ENABLED)
			{
				player.sendPacket(new SystemMessage2(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
			}
			else
			{
				String[] cm = _bypass.split(" ");
				final ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(cm[0]);
				if (handler != null)
				{
					handler.onBypassCommand(player, _bypass);
				}
			}
		}
		// Synerge - Support for handling scripts events on tutorial windows
		else if (_bypass.startsWith("scripts_"))
		{
			String command = _bypass.substring(8).trim();
			String[] word = command.split("\\s+");
			String[] args = command.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if (path.length != 2)
			{
				return;
			}
			
			Map<String, Object> variables = null;
			
			if (word.length == 1)
			{
				Scripts.getInstance().callScripts(player, path[0], path[1], variables);
			}
			else
			{
				Scripts.getInstance().callScripts(player, path[0], path[1], new Object[]
				{
					args
				}, variables);
			}
		}
		else if (_bypass.startsWith("characterintro_"))
		{
			if (_bypass.startsWith("characterintro_emailvalidation"))
			{
				String[] cm = _bypass.split(" ");
				
				if (cm.length < 3)
				{
					// Original Message: Please fill all required fields.
					player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestTutorialPassCmdToServer.message1", player));
					return;
				}
				
				String email1 = cm[1];
				String email2 = cm[2];
				
				CharacterIntro.sendEmailValidation(player, email1, email2);
			}
			if (_bypass.startsWith("characterintro_referral"))
			{
				
				if (!Config.ENABLE_REFERRAL_SYSTEM)
				{
					player.sendMessageS("Sorry, but referral system is Disabled!", 5);
					return;
				}
				
				String[] cm = _bypass.split(" ");
				
				if (cm.length < 2)
				{
					// Original Message: Please fill all required fields.
					player.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestTutorialPassCmdToServer.message1", player));
					return;
				}
				
				String referral = cm[1];
				
				CharacterIntro.referralSystem(player, referral);
			}
			else
			{
				String command = _bypass.substring(15);
				CharacterIntro.bypassIntro(player, command);
			}
		}
		// Synerge - Support to use the bypasshandler on tutorial windows
		else if (BypassHandler.getInstance().useBypassCommandHandler(player, _bypass))
		{
			
		}
		else
		{
			Quest tutorial = QuestManager.getQuest(255);
			
			if (tutorial != null)
			{
				player.processQuestEvent(tutorial.getName(), _bypass, null);
			}
		}
		
		if (Config.ALLOW_MAIL_OPTION)
		{
			AccountEmail.onBypass(player, _bypass);
		}
	}
}