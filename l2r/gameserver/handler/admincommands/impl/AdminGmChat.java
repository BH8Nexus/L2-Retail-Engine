package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.listener.actor.player.impl.GmAnswerListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.utils.MapUtils;

public class AdminGmChat implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_gmchat,
		admin_say,
		admin_snoop,
		admin_unsnoop
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch (command)
		{
			case admin_gmchat:
				try
				{
					String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
					Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
					AdminTable.broadcastToGMs(cs);
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
				break;
			case admin_say:
				try
				{
					Creature target = activeChar.getTarget() != null ? activeChar.getTarget().isCreature() ? (Creature) activeChar.getTarget() : null : null;
					if (target == null)
					{
						activeChar.sendMessage("Select a target.");
						return false;
					}
					
					Say2 cs = null;
					String text = fullString.replaceFirst(Commands.admin_say.name(), "").trim();
					switch (text.charAt(0))
					{
						case '!':
							cs = new Say2(0, ChatType.SHOUT, target.getName(), text.substring(1));
							int rx = MapUtils.regionX(target);
							int ry = MapUtils.regionY(target);
							int offset = Config.SHOUT_OFFSET;
							
							for (Player player : GameObjectsStorage.getAllPlayersForIterate())
							{
								if (activeChar.getReflection() != player.getReflection())
								{
									continue;
								}
								
								int tx = MapUtils.regionX(player);
								int ty = MapUtils.regionY(player);
								
								if (((tx >= (rx - offset)) && (tx <= (rx + offset)) && (ty >= (ry - offset)) && (ty <= (ry + offset))) || target.isInRangeZ(player, Config.CHAT_RANGE))
								{
									player.sendPacket(cs);
								}
							}
							break;
						case '@':
							if ((target.getPlayer() != null) && (target.getPlayer().getClan() != null))
							{
								target.getPlayer().getClan().broadcastToOnlineMembers(new Say2(0, ChatType.CLAN, target.getName(), text.substring(1)));
							}
							break;
						case '#':
							if ((target.getPlayer() != null) && target.getPlayer().isInParty())
							{
								target.getPlayer().getParty().sendPacket(new Say2(0, ChatType.PARTY, target.getName(), text.substring(1)));
							}
							break;
						case '$':
							if ((target.getPlayer() != null) && (target.getPlayer().getClan() != null) && (target.getPlayer().getClan().getAlliance() != null))
							{
								target.getPlayer().getClan().getAlliance().broadcastToOnlineMembers(new Say2(0, ChatType.ALLIANCE, target.getName(), text.substring(1)));
							}
							break;
						case '%':
							cs = new Say2(0, ChatType.HERO_VOICE, target.getName(), text.substring(1));
							for (Player player : GameObjectsStorage.getAllPlayersForIterate())
							{
								player.sendPacket(cs);
							}
							break;
						case '"':
							String name = text.split(" ")[0];
							text = text.replaceFirst(name, "").trim();
							name = name.substring(1);
							Player receiver = World.getPlayer(name);
							if (receiver != null)
							{
								if (Config.GM_PM_COMMANDS)
								{
									int index = text.indexOf("//");
									if (index == 0)
									{
										wordList = text.substring(index + 2).split(" ");
										if (wordList.length > 0)
										{
											
											// Build the bypass: //command targetname otherWords
											String bypass = "admin_" + wordList[0] + " " + name;
											for (int i = 1; i < wordList.length; i++)
											{
												bypass += " " + wordList[i];
											}
											
											if (!AdminTable.getInstance().hasAccess(bypass, activeChar.getAccessLevel()))
											{
												activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestBypassToServer.message1", activeChar));
												return false;
											}
											
											if (AdminTable.getInstance().requireConfirm(bypass))
											{
												activeChar.ask(new l2r.gameserver.network.serverpackets.ConfirmDlg(SystemMsg.S1, 30000).addString("Are you sure you want execute command " + bypass), new GmAnswerListener(activeChar, bypass));
												return false;
											}
											
											activeChar.setTarget(null); // Remove target!!! Many commands which are used with target wont be executed as it should.
											AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bypass);
										}
										break;
									}
								}
								
								if (!receiver.getMessageRefusal() || receiver.canAcceptPM(activeChar.getObjectId()))
								{
									if (activeChar.isBlockAll() || activeChar.getMessageRefusal())
									{
										activeChar.acceptPM(receiver.getObjectId());
									}
									
									if (activeChar._antiFlood.canTell(receiver.getObjectId(), text))
									{
										receiver.sendPacket(cs);
									}
									
									cs = new Say2(activeChar.getObjectId(), ChatType.TELL, "->" + receiver.getName(), text);
									activeChar.sendPacket(cs);
								}
								else
								{
									activeChar.sendPacket(SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
								}
							}
							break;
						case '^':
						default:
							cs = new Say2(0, ChatType.ALL, target.getName(), text);
							target.broadcastPacket(cs);
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
				break;
			case admin_snoop:
			{
				GameObject target = activeChar.getTarget();
				if (target == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message1", activeChar));
					return false;
				}
				if (!target.isPlayer())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message2", activeChar));
					return false;
				}
				
				Player player = (Player) target;
				player.addSnooper(activeChar);
				activeChar.addSnooped(player);
				break;
			}
			case admin_unsnoop:
			{
				GameObject target = activeChar.getTarget();
				if (target == null)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message3", activeChar));
					return false;
				}
				if (!target.isPlayer())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message4", activeChar));
					return false;
				}
				
				Player player = (Player) target;
				activeChar.removeSnooped(player);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admingmchat.message5", activeChar).addString(target.getName()));
				break;
			}
		}
		return true;
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}