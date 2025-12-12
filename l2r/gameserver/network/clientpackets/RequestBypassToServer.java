package l2r.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.Config;
import l2r.gameserver.custom.OfflineBufferManager;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.forum.ForumBBSManager;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.bypass.BypassHandler;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.impl.Vote;
import l2r.gameserver.handler.vote.VoteHopzone;
import l2r.gameserver.handler.vote.VoteNetwork;
import l2r.gameserver.handler.vote.VoteTopzone;
import l2r.gameserver.instancemanager.BypassManager.DecodedBypass;
import l2r.gameserver.instancemanager.OlympiadHistoryManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.listener.actor.player.impl.GmAnswerListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.OlympiadManagerInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.clientpackets.security.AbstractBypassPacket;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.tables.AdminTable;

public class RequestBypassToServer extends /* L2GameClientPacket */AbstractBypassPacket
{
	private static String[] ScriptBypassesFilterWords =
	{
		"util",
		"events",
		"services",
		"handler",
		"zones",
		"actions"
	};
	// Format: cS
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);
	private DecodedBypass bp = null;
	
	@Override
	protected void readImpl()
	{
		String bypass = readS();
		if (!bypass.isEmpty())
		{
			bp = getClient().getActiveChar().decodeBypass(bypass);
		}
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if ((activeChar == null) || (bp == null) || (activeChar.isBlocked() && !activeChar.isInObserverMode() && ((bp.bypass == null) || !(bp.bypass.contains("secondaryPassS") || bp.bypass.contains("secondaryPassF") || bp.bypass.contains("ProposePass") || bp.bypass.contains("CheckPass") || bp.bypass.contains("RecoverPage") || bp.bypass.startsWith("doRecoverAcc") || bp.bypass.contains("TryPass") || bp.bypass.contains("user_report")))))
		{
			return;
		}
		
		// Flood Protection
		if (!checkReuseTime(activeChar, bp.bypass))
		{
			return;
		}

//		if (!activeChar.checkFloodProtection(getFloodProtectorType(),bp.bypass))
//		{
//			return;
//		}
		
		if ((bp.handler == null) && !Config.ALLOW_TALK_TO_NPCS)
		{
			return;
		}
		if ((bp.handler != null) && !Config.COMMUNITYBOARD_ENABLED)
		{
			return;
		}
		if ((bp.handler != null) && activeChar.isCursedWeaponEquipped())
		{
			return;
		}

		
		// Synerge - Bypass debug
		if (activeChar.canOverrideCond(PcCondOverride.DEBUG_CONDITIONS) && activeChar.isDebug())
		{
			activeChar.sendMessage("Bypass: " + bp.bypass + " - Handler: " + (bp.handler != null));
		}
		
		try
		{
			NpcInstance npc = activeChar.getLastNpc();
			GameObject target = activeChar.getTarget();
			if ((npc == null) && (target != null) && target.isNpc())
			{
				npc = (NpcInstance) target;
			}
			
			if (bp.bypass.startsWith("admin_"))
			{
				if (!AdminTable.getInstance().hasAccess(bp.bypass, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestBypassToServer.message1", activeChar));
					_log.warn("Character " + activeChar.getName() + " tried to use admin command " + bp.bypass + ", without proper access level!");
					return;
				}
				
				if (AdminTable.getInstance().requireConfirm(bp.bypass.split(" ")[0]))
				{
					activeChar.ask(new l2r.gameserver.network.serverpackets.ConfirmDlg(SystemMsg.S1, 30000).addString("Are you sure you want execute command " + bp.bypass), new GmAnswerListener(activeChar, bp.bypass));
					return;
				}
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
			}
			else if (bp.bypass.startsWith("openURL "))
			{
				openURL(activeChar, bp.bypass.substring("openURL ".length()));
			}
			else if (bp.bypass.equals("come_here") && activeChar.isGM())
			{
				comeHere(getClient());
			}
			else if (bp.bypass.startsWith("player_help "))
			{
				playerHelp(activeChar, bp.bypass.substring(12));
			}
			else if (bp.bypass.startsWith("scripts_"))
			{
				String command = bp.bypass.substring(8).trim();
				if (!StringUtils.startsWithAny(command.toLowerCase(), ScriptBypassesFilterWords))
				{
					_log.warn("Player " + activeChar.getName() + " tried to use bypass " + bp.bypass);
					return;
				}
				String[] word = command.split("\\s+");
				String[] args = command.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if (path.length != 2)
				{
					_log.warn("Bad Script bypass!");
					return;
				}
				
				Map<String, Object> variables = null;
				if (npc != null)
				{
					variables = new HashMap<>(1);
					variables.put("npc", npc.getRef());
				}
				
				if (word.length == 1)
				{
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
				}
				else
				{
					Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[]
					{
						args
					}, variables);
				}
			}
			else if (bp.bypass.startsWith("user_"))
			{
				String command = bp.bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);
				
				if (vch != null)
				{
					vch.useVoicedCommand(word, activeChar, args);
				}
				else
				{
					_log.warn("Unknow voiced command '" + word + "'");
				}
			}
			else if (bp.bypass.startsWith("vote "))
			{
				Vote.restoreVotedData(activeChar, activeChar.getIP());
				
				String voteSiteName = bp.bypass.substring(5).trim();
				switch (voteSiteName)
				{
					case "hopzone":
						if (activeChar.eligibleToVoteHop())
						{
							VoteHopzone voteHop = new VoteHopzone();
							if (voteHop.hasVoted(activeChar))
							{
								voteHop.updateDB(activeChar, "last_hop_vote");
								voteHop.setVoted(activeChar);
								voteHop.reward(activeChar);
							}
							else
							{
								// activeChar.sendMessage("You didn't vote yet.");
								activeChar.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Hopzone", "You didn't vote yet."));
								Vote.showHtm(activeChar);
							}
						}
						else
						{
							Vote.showHtm(activeChar);
						}
						break;
					case "topzone":
						if (activeChar.eligibleToVoteTop())
						{
							VoteTopzone voteTop = new VoteTopzone();
							if (voteTop.hasVoted(activeChar))
							{
								voteTop.updateDB(activeChar, "last_top_vote");
								voteTop.setVoted(activeChar);
								voteTop.reward(activeChar);
							}
							else
							{
								// activeChar.sendMessage("You didn't vote yet.");
								activeChar.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Topzone", "You didn't vote yet."));
								Vote.showHtm(activeChar);
							}
						}
						else
						{
							Vote.showHtm(activeChar);
						}
						break;
					case "network":
						if (activeChar.eligibleToVoteBra())
						{
							VoteNetwork voteBra = new VoteNetwork();
							if (voteBra.hasVoted(activeChar))
							{
								voteBra.updateDB(activeChar, "last_net_vote");
								voteBra.setVoted(activeChar);
								voteBra.reward(activeChar);
							}
							else
							{
								// activeChar.sendMessage("You didn't vote yet.");
								activeChar.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Network", "You didn't vote yet."));
								Vote.showHtm(activeChar);
							}
						}
						else
						{
							Vote.showHtm(activeChar);
						}
						break;
				}
			}
			else if (bp.bypass.startsWith("npc_"))
			{
				int endOfId = bp.bypass.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = bp.bypass.substring(4, endOfId);
				}
				else
				{
					id = bp.bypass.substring(4);
				}
				GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if ((object != null) && object.isNpc() && (endOfId > 0) && activeChar.isInRange(object.getLoc(), Creature.INTERACTION_DISTANCE))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
				}
				else if ((object == null) && (endOfId > 0)) // Npc not seen by the player
				{
					object = GameObjectsStorage.findObject(Integer.parseInt(id));
					if ((object != null) && object.isNpc() && ((NpcInstance) object).getTemplate().noInterractionDistance)
					{
						((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
					}
				}
			}
			else if (bp.bypass.startsWith("_olympiad?"))
			{
				String[] ar = bp.bypass.replace("_olympiad?", "").split("&");
				String firstVal = ar[0].split("=")[1];
				String secondVal = ar[1].split("=")[1];
				
				if (firstVal.equalsIgnoreCase("move_op_field"))
				{
					if (!Config.ENABLE_OLYMPIAD_SPECTATING)
					{
						return;
					}
					
					// Transition in view of Olympiad is allowed only from the manager or from the arena.
					if (((activeChar.getLastNpc() instanceof OlympiadManagerInstance) && activeChar.getLastNpc().isInRange(activeChar, Creature.INTERACTION_DISTANCE)) || (activeChar.getOlympiadObserveGame() != null))
					{
						Olympiad.addSpectator(Integer.parseInt(secondVal) - 1, activeChar);
					}
				}
			}
			else if (bp.bypass.startsWith("_diary"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (bp.bypass.startsWith("_match"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				
				OlympiadHistoryManager.getInstance().showHistory(activeChar, heroclass, heropage);
			}
			else if (bp.bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				GameObject object = activeChar.getTarget();
				if ((object != null) && object.isNpc())
				{
					((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass);
				}
			}
			else if(bp.bypass.startsWith("menu_select?"))
			{
				//if (activeChar.isInRangeZ((GameObject)npc, activeChar.getInteractDistance((GameObject)npc)))
				if(activeChar.isInRangeZ(npc, 150))
				{
					String paramString = bp.bypass.substring(bp.bypass.indexOf('?') + 1);
					StringTokenizer st = new StringTokenizer(paramString, "&");
					int ask = Integer.parseInt(st.nextToken().split("=")[1]);
					int reply = Integer.parseInt(st.nextToken().split("=")[1]);
					if(npc != null)
					{
						npc.onMenuSelect(activeChar, ask, reply);
					}
				}
			}
			else if (bp.bypass.startsWith("partyMatchingInvite"))
			{
				try
				{
					String targetName = bp.bypass.substring(20);
					Player receiver = World.getPlayer(targetName);
					SystemMessage sm;
					
					if (receiver == null)
					{
						activeChar.sendMessage("First select a user to invite to your party.");
						return;
					}
					
					if ((receiver.isOnline() == false))
					{
						activeChar.sendMessage("Player is in offline mode.");
						return;
					}
					
					if ((!activeChar.isGM() && receiver.isInvisible()) || (!activeChar.isInvisible() && receiver.isInvisible() && !activeChar.isGM()))
					{
						activeChar.sendMessage("Incorrect target.");
						return;
					}
					
					if (receiver.isInParty())
					{
						activeChar.sendMessage("Player " + receiver.getName() + " is already in a party.");
						return;
					}
					
					if (activeChar.getBlockList().contains(receiver))
					{
						activeChar.sendMessage("Player " + receiver.getName() + " is in your ignore list.");
						return;
					}
					
					if (receiver == activeChar)
					{
						activeChar.sendMessage("Wrong target.");
						return;
					}
					
					if (receiver.isCursedWeaponEquipped() || activeChar.isCursedWeaponEquipped())
					{
						receiver.sendMessage("You cannot invite this person to join in your party right now.");
						return;
					}
					
					if (receiver.isInJail() || activeChar.isInJail())
					{
						activeChar.sendMessage("You cannot invite a player while is in Jail.");
						return;
					}
					
					if (receiver.isInOlympiadMode() || activeChar.isInOlympiadMode())
					{
						if ((receiver.isInOlympiadMode() != activeChar.isInOlympiadMode()) || (receiver.getOlympiadGame().getId() != activeChar.getOlympiadGame().getId()) || (receiver.getOlympiadSide() != activeChar.getOlympiadSide()))
						{
							activeChar.sendMessage("You cannot invite this player to join your party right now.");
							return;
						}
					}
					
					activeChar.sendMessage("You invited " + receiver.getName() + " to join your party.");
					
					if (!activeChar.isInParty())
					{
						Party newparty = new Party(activeChar, Party.ITEM_LOOTER);
						activeChar.setParty(newparty);
						receiver.joinParty(newparty);
					}
					else
					{
						if (activeChar.getParty().isInDimensionalRift())
						{
							activeChar.sendMessage("You cannot invite a player when you are in the Dimensional Rift.");
						}
						else
						{
							Party plparty = activeChar.getParty();
							receiver.joinParty(plparty);
						}
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					e.printStackTrace();
				}
			}
			else if (bp.bypass.startsWith("multisell "))
			{
				MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0);
			}
			else if (bp.bypass.startsWith("Quest "))
			{
				String p = bp.bypass.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
				{
					activeChar.processQuestEvent(p, "", npc);
				}
				else
				{
					activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
				}
			}
			else if ((Config.COMMUNITYBOARD_ENABLED && (bp.bypass.startsWith(ForumBBSManager.FORUM_BBS_CMD) || Config.FORUM_IN_WHOLE_COMMUNITY_BOARD)) || bp.bypass.startsWith(Config.FORUM_TAB))
			{
				if ((Config.FORUM_IN_WHOLE_COMMUNITY_BOARD && !bp.bypass.startsWith(ForumBBSManager.FORUM_BBS_CMD)) || bp.bypass.startsWith(Config.FORUM_TAB))
				{
					bp.bypass = bp.bypass.replace(Config.FORUM_TAB, ForumBBSManager.FORUM_BBS_CMD);
				}
				
				ForumBBSManager.getInstance().onBypassCommand(activeChar, bp.bypass);
			}
			// Synerge - Bypass for Buff Store
			else if (bp.bypass.startsWith("BuffStore"))
			{
				try
				{
					OfflineBufferManager.getInstance().processBypass(activeChar, bp.bypass);
				}
				catch (Exception ex)
				{
				}
			}
			else if (bp.bypass.startsWith("_bbsnpcs")) // Community close custom design
			{
				sendFileToPlayer("bbs_npcs.html", true);
				return;
			}
			else if (bp.handler != null)
			{
				if (!Config.COMMUNITYBOARD_ENABLED)
				{
					activeChar.sendPacket(new SystemMessage2(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
				}
				else
				{
					bp.handler.onBypassCommand(activeChar, bp.bypass);
				}
			}
			// Synerge - Support for secondary password on cb
			else if (bp.bypass.startsWith("ProposePass") || bp.bypass.startsWith("CheckPass") || bp.bypass.startsWith("TryPass") || bp.bypass.startsWith("RecoverPage") || bp.bypass.startsWith("doRecoverAcc"))
			{
				Quest tutorial = QuestManager.getQuest(255);
				if (tutorial != null)
				{
					activeChar.processQuestEvent(tutorial.getName(), bp.bypass, null);
				}
			}
			// Synerge - Bypass handler
			else
			{
				BypassHandler.getInstance().useBypassCommandHandler(activeChar, bp.bypass);
			}
		}
		catch (Exception e)
		{
			String st = "Char '" + activeChar.getName() + "' sent Bad RequestBypassToServer: " + bp.bypass;
			GameObject target = activeChar.getTarget();
			if ((target != null) && target.isNpc())
			{
				st = st + " via NPC #" + ((NpcInstance) target).getNpcId();
			}
			_log.error(st, e);
		}
	}
	
	/**
	 * @param string
	 * @param b
	 */
	private void sendFileToPlayer(String string, boolean b)
	{
		// TODO Auto-generated method stub
		
	}
	
	private static void comeHere(GameClient client)
	{
		GameObject obj = client.getActiveChar().getTarget();
		if ((obj != null) && obj.isNpc())
		{
			NpcInstance temp = (NpcInstance) obj;
			Player activeChar = client.getActiveChar();
			temp.setTarget(activeChar);
			temp.moveToLocation(activeChar.getLoc(), 0, true);
		}
	}
	
	private static void openURL(Player player, String url)
	{
		// TODO: Give support with smartguard
		// player.sendPacket(new OpenURLPacket(url));
	}
	
	private static void playerHelp(Player activeChar, String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(path);
		activeChar.sendPacket(html);
	}
	
	// // Synerge - This packet can be used while the character is blocked
	// @Override
	// public boolean canBeUsedWhileBlocked()
	// {
	// return true;
	// }
}
