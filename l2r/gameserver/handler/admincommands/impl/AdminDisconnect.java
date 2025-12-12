package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminDisconnect implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_disconnect,
		admin_kick
	}
	
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		
		switch (command)
		{
			case admin_disconnect:
			case admin_kick:
				final Player player;
				if (wordList.length == 1)
				{
					// Π�Π±Ρ€Π°Π±ΠΎΡ‚ΠΊΠ° ΠΏΠΎ Ρ‚Π°Ρ€Π³ΠµΡ‚Ρƒ
					GameObject target = activeChar.getTarget();
					if (target == null)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admindisconnect.message1", activeChar));
						break;
					}
					if (!target.isPlayer())
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						break;
					}
					player = (Player) target;
				}
				else
				{
					// Π�Π±Ρ€Π°Π±ΠΎΡ‚ΠΊΠ° ΠΏΠΎ Π½ΠΈΠΊΡƒ
					player = World.getPlayer(wordList[1]);
					if (player == null)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admindisconnect.message2", activeChar).addString(wordList[1]));
						break;
					}
				}
				
				if (player.getObjectId() == activeChar.getObjectId())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admindisconnect.message3", activeChar));
					break;
				}
				
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admindisconnect.message4", activeChar).addString(player.getName()));
				
				if (player.isInOfflineMode())
				{
					player.setOfflineMode(false);
					player.kick();
					return true;
				}
				
				player.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.AdminDisconnect.YoureKickedByGM", player));
				player.sendPacket(SystemMsg.YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_);
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					@Override
					public void runImpl()
					{
						player.kick();
					}
				}, 500);
				break;
		}
		return true;
	}
	
	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}