package l2r.gameserver.network.clientpackets;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.Config;
import l2r.gameserver.cache.ItemInfoCache;
import l2r.gameserver.dao.EmotionsTable;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.player.impl.GmAnswerListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.actor.Custom.ProtectionSystemChat.MessageProtection;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.olympiad.OlympiadGame;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.matching.MatchingRoom;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.TradesHandler;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.AutoHuntingPunish;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.MapUtils;
import l2r.gameserver.utils.Strings;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);
	
	private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\u001B(.[^\u001B]*)[^\b]");
	private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");
	
	private String _text;
	private ChatType _type;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		_type = l2r.commons.lang.ArrayUtils.valid(ChatType.VALUES, readD());
		_target = _type == ChatType.TELL ? readS(Config.CNAME_MAXLEN) : null;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isBeingPunished() && (activeChar.getBotPunishType() == AutoHuntingPunish.Punish.CHATBAN))
		{
			if (activeChar.getPlayerPunish().canTalk() && (activeChar.getBotPunishType() == AutoHuntingPunish.Punish.CHATBAN))
			{
				activeChar.endPunishment();
			}
			else if (activeChar.getBotPunishType() == AutoHuntingPunish.Punish.CHATBAN)
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_CHATTING_WILL_BE_BLOCKED_FOR_10_MINUTES);
				return;
			}
		}
		
		activeChar.isntAfk();
		
		if ((_type == null) || (_text == null) || (_text.length() == 0))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		// Players that are blocked cannot use the chat or commands
		if (activeChar.isBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		// _text = _text.replaceAll("\\\\n", "\n");
		//
		// if (_text.contains("\n"))
		// {
		// String[] lines = _text.split("\n");
		// _text = StringUtils.EMPTY;
		// for (int i = 0; i < lines.length; i++)
		// {
		// lines[i] = lines[i].trim();
		// if (lines[i].length() == 0)
		// {
		// continue;
		// }
		// if (_text.length() > 0)
		// {
		// _text += "\n >";
		// }
		// _text += lines[i];
		// }
		// }
		
		_text = _text.replaceAll("\\n", "").replaceAll("\n", "");
		
		if (_text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (_text.startsWith("_") && (activeChar.getVarB("vipticket", false) || activeChar.isGM()))
		{
			_type = ChatType.VIP_CHAT;
		}
		
		Player receiver = World.getPlayer(_target);
		if ((activeChar.getLevel() <= Config.PM_REQUIRED_LEVEL) && (activeChar.getSubClasses().size() <= 1) && ((_type == ChatType.TELL) && ((receiver == null) || !receiver.isGM())))
		{
			activeChar.sendMessage("This PM Chat is allowed only for characters with level higher than " + Config.PM_REQUIRED_LEVEL + " to avoid spam!");
			activeChar.sendActionFailed();
			return;
		}
		if ((activeChar.getLevel() <= Config.SHOUT_REQUIRED_LEVEL) && (activeChar.getSubClasses().size() <= 1) && (((_type == ChatType.SHOUT) && ((receiver == null) || !receiver.isGM())) || (_type == ChatType.TRADE)))
		{
			activeChar.sendMessage("This Shouting Chat is allowed only for characters with level higher than " + Config.SHOUT_REQUIRED_LEVEL + " to avoid spam!");
			activeChar.sendActionFailed();
			return;
		}
		if ((activeChar.getLevel() <= Config.CHATS_REQUIRED_LEVEL) && (activeChar.getSubClasses().size() <= 1) && (!_text.startsWith(".") || Config.NOT_USE_USER_VOICED) && ((_type == ChatType.ALL) && ((receiver == null) || !receiver.isGM())))
		{
			activeChar.sendMessage("This Chat is allowed only for characters with level higher than " + Config.CHATS_REQUIRED_LEVEL + " to avoid spam!");
			activeChar.sendActionFailed();
			return;
		}
		
		if (Functions.isEventStarted("events.Viktorina.Viktorina"))
		{
			if (activeChar.getVar("viktorina") == "on")
			{
				String answer = _text.trim();
				if (answer.length() > 0)
				{
					Object[] objects =
					{
						answer,
						activeChar
					};
					Functions.callScripts("events.Viktorina.Viktorina", "checkAnswer", objects);
				}
			}
			
		}
		
		// Handle shift-click item into .tradelist
		if (Config.ENABLE_TRADELIST_VOICE && (_text.toUpperCase().startsWith("WTS") || _text.toUpperCase().startsWith("WTB")))
		{
			TradesHandler.tryPublishTrade(activeChar, _text);
		}
		
		if (_text.startsWith(".") && _text.endsWith("findparty") && Config.PARTY_SEARCH_COMMANDS)
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();
			
			if (command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if (vch != null)
				{
					
					if (_text.indexOf(8) >= 0)
					{
						if (!checkActions(activeChar))
						{
							return;
						}
					}
					
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.command404", activeChar));
			return;
			
		}
		
		else if (_text.startsWith(".") && !Config.NOT_USE_USER_VOICED)
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();
			
			if ((command.length() > 0) && !fullcmd.startsWith("."))
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if (vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.command404", activeChar));
			}
		}
		
		else if (_text.startsWith(".") && _text.endsWith("offline") && Config.SERVICES_OFFLINE_TRADE_ALLOW && Config.NOT_USE_USER_VOICED)
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();
			
			if (command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if (vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.command404", activeChar));
			return;
		}
		
		if ((Config.CHATFILTER_MIN_LEVEL > 0) && ArrayUtils.contains(Config.CHATFILTER_CHANNELS, _type.ordinal()) && (activeChar.getLevel() < Config.CHATFILTER_MIN_LEVEL) && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
		{
			if (Config.CHATFILTER_WORK_TYPE == 1)
			{
				_type = ChatType.ALL;
			}
			else if (Config.CHATFILTER_WORK_TYPE == 2)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatNotHavePermission", activeChar).addNumber(Config.CHATFILTER_MIN_LEVEL));
				return;
			}
		}
		
		boolean globalchat = (_type != ChatType.ALLIANCE) && (_type != ChatType.CLAN) && (_type != ChatType.PARTY);
		
		if ((globalchat && (Config.TRADE_CHATS_REPLACE_FROM_ALL && (_type == ChatType.ALL))) || (Config.TRADE_CHATS_REPLACE_FROM_SHOUT && (_type == ChatType.SHOUT)) || (Config.TRADE_CHATS_REPLACE_FROM_TRADE && (_type == ChatType.TRADE)))
		{
			for (String s : Config.TRADE_WORDS)
			{
				if (_text.contains(s))
				{
					_type = ChatType.TRADE;
					break;
				}
			}
		}
		
		if ((globalchat || ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type.ordinal())) && (activeChar.getNoChannel() != 0))
		{
			if ((activeChar.getNoChannelRemained() > 0) || (activeChar.getNoChannel() < 0))
			{
				if (activeChar.getNoChannel() > 0)
				{
					int timeRemained = Math.round(activeChar.getNoChannelRemained() / 60000);
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatBanned", activeChar).addNumber(timeRemained));
				}
				else
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatBannedPermanently", activeChar));
				}
				
				activeChar.sendActionFailed();
				return;
			}
			activeChar.updateNoChannel(0);
		}
		
		if (globalchat && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
		{
			if (Config.ABUSEWORD_REPLACE && Config.containsAbuseWord(_text))
			{
				for (Pattern regex : Config.ABUSEWORD_LIST)
				{
					_text = regex.matcher(_text).replaceAll(Config.ABUSEWORD_REPLACE_STRING);
				}
				
				activeChar.sendActionFailed();
			}
			else if (Config.ABUSEWORD_BANCHAT && Config.containsAbuseWord(_text))
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ChatBanned", activeChar).addNumber(Config.ABUSEWORD_BANTIME * 60));
				Log.add(activeChar + ": " + _text, "abuse");
				activeChar.updateNoChannel(Config.ABUSEWORD_BANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}
		}
		
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
		ItemInstance item;
		int objectId;
		
		while (m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);
			
			if (item == null)
			{
				activeChar.sendActionFailed();
				break;
			}
			
			ItemInfoCache.getInstance().put(item);
		}
		
		String translit = activeChar.getVar("translit");
		if (translit != null)
		{
			m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while (m.find())
			{
				sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
				sb.append(_text.substring(end, end = m.end()));
			}
			
			_text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}
		
		// Synerge - Emotions system
		int emotion = EmotionsTable.containsEmotion(_text);
		if ((emotion != -1) && !World.getAroundPlayers(activeChar, 300, 300).isEmpty() && !activeChar.isInCombat() && !activeChar.isDead() && !activeChar.isCastingNow() && !activeChar.isSitting() && !activeChar.isNotShowEmotions())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), emotion));
		}
		
		// Log.LogChat(_type.name(), activeChar.getName(), _target, _text);
		Log.logChat(_type, activeChar.getName(), _target, _text);
		
		Say2 cs;
		if (activeChar.isInFightClub() && activeChar.getFightClubEvent().isHidePersonality())
		{
			cs = new Say2(0, _type, "Player", _text);
		}
		else
		{
			if (_type == ChatType.VIP_CHAT)
			{
				String txt = _text.substring(1);
				cs = new Say2(activeChar.getObjectId(), ChatType.COMMANDCHANNEL_COMMANDER, activeChar.getName(), txt);
			}
			else
			{
				cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);
			}
		}
		
		switch (_type)
		{
			case TELL:
				// Player receiver = World.getPlayer(_target);
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if ((receiver == null) && (activeChar.getPet() != null) && activeChar.getPet().getName().equals(_target))
				{
					receiver = activeChar;
				}
				
				if (receiver != null)
				{
					if (Config.GM_PM_COMMANDS)
					{
						int index = _text.indexOf("//");
						if (index == 0)
						{
							String[] wordList = _text.substring(index + 2).split(" ");
							if (wordList.length > 0)
							{
								
								// Build the bypass: //command targetname otherWords
								String bypass = "admin_" + wordList[0] + " " + _target;
								for (int i = 1; i < wordList.length; i++)
								{
									bypass += " " + wordList[i];
								}
								
								if (!AdminTable.getInstance().hasAccess(bypass, activeChar.getAccessLevel()))
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestBypassToServer.message1", activeChar));
									_log.warn("Character " + activeChar.getName() + " tried to use admin command " + bypass + ", without proper access level!");
									return;
								}
								
								if (AdminTable.getInstance().requireConfirm(bypass))
								{
									activeChar.ask(new l2r.gameserver.network.serverpackets.ConfirmDlg(SystemMsg.S1, 30000).addString("Are you sure you want execute command " + bypass), new GmAnswerListener(activeChar, bypass));
									return;
								}
								
								activeChar.setTarget(null); // Remove target!!! Many commands which are used with target wont be executed as it should.
								AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bypass);
							}
							break;
						}
					}
					
					if (receiver.isInOfflineMode())
					{
						// Original Message: {0} is in offline trade mode.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.targetInOfflineTrade", activeChar).addString(receiver));
						activeChar.sendActionFailed();
					}
					else if (!receiver.isInBlockList(activeChar) && (!receiver.isBlockAll() || receiver.canAcceptPM(activeChar.getObjectId())))
					{
						if (!receiver.getMessageRefusal() || receiver.canAcceptPM(activeChar.getObjectId()))
						{
							if (activeChar.isBlockAll() || activeChar.getMessageRefusal())
							{
								activeChar.acceptPM(receiver.getObjectId());
							}
							
							if (activeChar._antiFlood.canTell(receiver.getObjectId(), _text))
							{
								receiver.sendPacket(cs);
							}
							
							checkAutoRecall(activeChar, receiver);
							checkMessageCatcher(activeChar, receiver, _text);
							
							cs = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text);
							activeChar.sendPacket(cs);
							
							MessageProtection.getInstance().analizeMessage(activeChar, receiver, _text, "Whisp");
						}
						else if (receiver.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && receiver.getMessageRefusal() && !receiver.canAcceptPM(activeChar.getObjectId()))
						{
							activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), ActionFail.STATIC);
						}
						else
						{
							activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
						}
					}
					else
					{
						activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT, ActionFail.STATIC);
					}
				}
				else
				{
					activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_IS_NOT_CURRENTLY_LOGGED_IN).addString(_target), ActionFail.STATIC);
				}
				break;
			case VIP_CHAT:
				if (activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if (activeChar.isInObserverMode())
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
					return;
				}
				
				if (!activeChar.isGM() && !activeChar._antiFlood.canShout(_text))
				{
					activeChar.sendMessage("VIP chat is allowed once per 5 seconds.");
					return;
				}
				
				announce(activeChar, cs);
				activeChar.sendPacket(cs);
				break;
			case SHOUT:
				if (activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
					return;
				}
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					if (activeChar.isCursedWeaponEquipped())
					{
						activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
						return;
					}
					if (activeChar.isInObserverMode())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
						return;
					}
					
					if (!activeChar.isGM() && !activeChar._antiFlood.canShout(_text))
					{
						activeChar.sendMessage("Shout chat is allowed once per 5 seconds.");
						return;
					}
					
					if (Config.GLOBAL_SHOUT && activeChar.isInJail())
					{
						// Original Message: You may not use this chat while in jail.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat3", activeChar));
						return;
					}
					
					if (activeChar.getPvpKills() < Config.PVP_COUNT_SHOUT)
					{
						// Original Message: Shout chat is allowed only for characters with at least {0} PvP kills.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat2", activeChar).addNumber(Config.PVP_COUNT_SHOUT));
						return;
					}
					if (activeChar.getOnlineTime() < Config.ONLINE_TIME_SHOUT)
					{
						// Original Messsage: You character must have at least {0} hours online time to use shout chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat4", activeChar).addNumber(Config.ONLINE_TIME_SHOUT / 360));
						return;
					}
					
					if (activeChar.getLevel() < Config.LEVEL_FOR_SHOUT)
					{
						// Original Message: You character must be level {0} or above to use this chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat5", activeChar).addNumber(Config.LEVEL_FOR_SHOUT));
						return;
					}
				}
				
				if (Config.GLOBAL_SHOUT)
				{
					announce(activeChar, cs);
				}
				else
				{
					shout(activeChar, cs);
				}
				
				activeChar.sendPacket(cs);
				break;
			case TRADE:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					if (activeChar.isCursedWeaponEquipped())
					{
						activeChar.sendPacket(SystemMsg.SHOUT_AND_TRADE_CHATTING_CANNOT_BE_USED_WHILE_POSSESSING_A_CURSED_WEAPON);
						return;
					}
					if (activeChar.isInObserverMode())
					{
						activeChar.sendPacket(SystemMsg.YOU_CANNOT_CHAT_WHILE_IN_OBSERVATION_MODE);
						return;
					}
					
					if (!activeChar.isGM() && !activeChar._antiFlood.canTrade(_text, false))
					{
						activeChar.sendMessage("Trade chat is allowed once per 5 seconds.");
						return;
					}
					
					if (activeChar.getPvpKills() < Config.PVP_COUNT_TRADE)
					{
						// Original Message: Trade chat is allowed only for characters with at least {0} PvP kills.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat2", activeChar).addNumber(Config.PVP_COUNT_TRADE));
						return;
					}
					
					if (activeChar.getOnlineTime() < Config.ONLINE_TIME_TRADE)
					{
						// Original Message: You character must have at least {0} hours online time to use trade chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat3", activeChar).addNumber(Config.ONLINE_TIME_TRADE / 360));
						return;
					}
					
					if (activeChar.getLevel() < Config.LEVEL_FOR_TRADE)
					{
						// Original Messagee: You character must be level {0} or above to use Trade chat.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat4", activeChar).addNumber(Config.LEVEL_FOR_TRADE));
						return;
					}
					
					if (Config.GLOBAL_TRADE_CHAT && activeChar.isInJail())
					{
						// Original Message: You may not use this chat while in jail.
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat3", activeChar));
						return;
					}
					
					if (Config.USE_TRADE_WORDS_ON_GLOBAL_CHAT && Config.GLOBAL_TRADE_CHAT)
					{
						boolean allowed = false;
						for (String stw : Config.TRADE_WORDS)
						{
							if (_text.toLowerCase().startsWith(stw))
							{
								allowed = true;
								break;
							}
						}
						
						if (!allowed)
						{
							// Original Message: This chat is for trade/sell/buy, For Example: (WTB/WTT/WTS Valakas Necklace.)
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat5", activeChar));
							// Original Message: TradeWords: {0}.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.TradeChat6", activeChar).addString(Config.TRADE_WORDS));
							return;
						}
					}
				}
				
				if (Config.GLOBAL_TRADE_CHAT)
				{
					announce(activeChar, cs);
				}
				else
				{
					shout(activeChar, cs);
				}
				
				activeChar.sendPacket(cs);
				break;
			case ALL:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (activeChar.isCursedWeaponEquipped())
				{
					cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text);
				}
				
				List<Player> list = null;
				
				if (activeChar.isInObserverMode() && (activeChar.getObserverRegion() != null) && (activeChar.getOlympiadObserveGame() != null))
				{
					OlympiadGame game = activeChar.getOlympiadObserveGame();
					if (game != null)
					{
						list = game.getAllPlayers();
					}
				}
				else if (activeChar.isInOlympiadMode())
				{
					OlympiadGame game = activeChar.getOlympiadGame();
					if (game != null)
					{
						list = game.getAllPlayers();
					}
				}
				else
				{
					list = World.getAroundPlayers(activeChar);
				}
				
				if (list != null)
				{
					for (Player player : list)
					{
						if ((player == activeChar) || (player.getReflection() != activeChar.getReflection()) || player.isBlockAll() || player.isInBlockList(activeChar))
						{
							continue;
						}
						player.sendPacket(cs);
					}
				}
				
				activeChar.sendPacket(cs);
				break;
			case CLAN:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineMembers(cs);
				}
				break;
			case ALLIANCE:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if ((activeChar.getClan() != null) && (activeChar.getClan().getAlliance() != null))
				{
					activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
				}
				break;
			case PARTY:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (activeChar.isInParty())
				{
					activeChar.getParty().sendPacket(cs);
				}
				break;
			case PARTY_ROOM:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				MatchingRoom r = activeChar.getMatchingRoom();
				if ((r != null) && (r.getType() == MatchingRoom.PARTY_MATCHING))
				{
					r.sendPacket(cs);
				}
				break;
			case COMMANDCHANNEL_ALL:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					activeChar.getPlayerGroup().sendPacket(cs);
				}
				else
				{
					if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
					{
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
						return;
					}
					if (activeChar.getParty().getCommandChannel().getLeader() == activeChar)
					{
						activeChar.getParty().getCommandChannel().sendPacket(cs);
					}
					else
					{
						activeChar.sendPacket(SystemMsg.ONLY_THE_COMMAND_CHANNEL_CREATOR_CAN_USE_THE_RAID_LEADER_TEXT);
					}
				}
				break;
			case COMMANDCHANNEL_COMMANDER:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					activeChar.getPlayerGroup().sendPacket((Player p) -> ((p.getParty() != null) && p.getParty().isLeader(p)), cs);
				}
				else
				{
					if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
					{
						activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
						return;
					}
					if (activeChar.getParty().isLeader(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
					}
					else
					{
						activeChar.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
					}
				}
				break;
			case HERO_VOICE:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (activeChar.isHero() || activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
				{
					if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS))
					{
						if (activeChar.isInJail())
						{
							// Original Message: You may not use this chat while in jail.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.ShoutChat3", activeChar));
							return;
						}
						else if (!activeChar._antiFlood.canHero(_text))
						{
							// Original Message: Hero chat is allowed once per {0} seconds.
							activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.Say2C.HeroChat1", activeChar).addNumber(Config.HERO_CHAT_USE_DELAY));
							return;
						}
					}
				}
				
				for (Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if (!player.isInBlockList(activeChar) && !player.isBlockAll())
					{
						player.sendPacket(cs);
					}
				}
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT));
					return;
				}
				
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			case BATTLEFIELD:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && (activeChar.getBattlefieldChatId() == 0))
				{
					return;
				}
				
				for (Player player : GameObjectsStorage.getAllPlayersForIterate())
				{
					if (!player.isInBlockList(activeChar) && !player.isBlockAll() && (player.getBattlefieldChatId() == activeChar.getBattlefieldChatId()))
					{
						player.sendPacket(cs);
					}
				}
				break;
			case MPCC_ROOM:
				if (activeChar.getLevel() < Config.MIN_LEVEL_TO_USE_SHOUT)
				{
					activeChar.sendMessage(new StringBuilder().append("You cannot use this chat until you get ").append(Config.MIN_LEVEL_TO_USE_SHOUT).append(" level").toString());
					return;
				}
				MatchingRoom r2 = activeChar.getMatchingRoom();
				if ((r2 != null) && (r2.getType() == MatchingRoom.CC_MATCHING))
				{
					r2.sendPacket(cs);
				}
				break;
			default:
				_log.warn("Character " + activeChar.getName() + " used unknown chat type: " + _type.ordinal() + ".");
		}
	}
	
	private static void checkAutoRecall(Player sender, Player receiver)
	{
		if (receiver.isGM() && receiver.getQuickVarB("autoRecall", false))
		{
			if ((receiver.getDistance(sender) < 500) || sender.isTeleporting() || sender.isInOlympiadMode() || (sender.getReflection() != ReflectionManager.DEFAULT))
			{
				return;
			}
			
			sender.teleToLocation(Location.findAroundPosition(receiver, 100));
			receiver.sendMessage("Recalled " + sender.getName() + ". Use \"//autorecall false\" to disable it!");
		}
	}
	
	private static void checkMessageCatcher(Player sender, Player receiver, String text)
	{
		if (sender.isGM() || receiver.isGM())
		{
			return;
		}
		
		if (Say2C.EX_ITEM_LINK_PATTERN.matcher(text).find())
		{
			return;
		}
		
		final Say2 packet = new Say2(sender.getObjectId(), ChatType.TELL, sender.getName(), "-> " + receiver.getName() + ": " + text);
		for (Player gm : GameObjectsStorage.getAllPlayersForIterate())
		{
			final int minLength = gm.getVarInt("catchMessagesGM", -1);
			if ((minLength > 0) && (minLength <= text.length()) && !sender.equals(gm) && !receiver.equals(gm))
			{
				gm.sendPacket(packet);
			}
		}
	}
	
	private static void shout(Player activeChar, Say2 cs)
	{
		int rx = MapUtils.regionX(activeChar);
		int ry = MapUtils.regionY(activeChar);
		int offset = Config.SHOUT_OFFSET;
		
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if ((player == activeChar) || (activeChar.getReflection() != player.getReflection()) || player.isBlockAll() || player.isInBlockList(activeChar))
			{
				continue;
			}
			
			int tx = MapUtils.regionX(player);
			int ty = MapUtils.regionY(player);
			
			if (((tx >= (rx - offset)) && (tx <= (rx + offset)) && (ty >= (ry - offset)) && (ty <= (ry + offset))) || activeChar.isInRangeZ(player, Config.CHAT_RANGE))
			{
				player.sendPacket(cs);
			}
		}
	}
	
	private static void announce(Player activeChar, Say2 cs)
	{
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if ((player == activeChar) || (activeChar.getReflection() != player.getReflection()) || player.isBlockAll() || player.isInBlockList(activeChar))
			{
				continue;
			}
			
			player.sendPacket(cs);
		}
	}
	
	private boolean checkActions(Player owner)
	{
		int pos1 = -1;
		while ((pos1 = _text.indexOf(8, pos1)) > -1)
		{
			int pos = _text.indexOf("ID=", pos1);
			if (pos == -1)
			{
				return false;
			}
			StringBuilder result = new StringBuilder(9);
			pos += 3;
			while (Character.isDigit(_text.charAt(pos)))
			{
				result.append(_text.charAt(pos++));
			}
			int id = Integer.parseInt(result.toString());
			
			if ((id == 1000007) && _text.contains("Party looking for members"))
			{
				return true;
			}
			
			pos1 = _text.indexOf(8, pos) + 1;
			if (pos1 == 0) // missing ending tag
			{
				_log.info(getClient() + " sent invalid publish item msg! ID:" + id);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}