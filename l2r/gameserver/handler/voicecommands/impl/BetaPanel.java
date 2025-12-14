package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;

public class BetaPanel implements IVoicedCommandHandler
{
    private static final String[] COMMANDS = { "betapanel" };

    @Override
    public boolean useVoicedCommand(String command, Player player, String target)
    {
        if (command.equals("betapanel"))
        {
            player.sendPacket(new l2r.gameserver.network.serverpackets.NpcHtmlMessage(0)
                    .setFile("data/html/html-en/betapanel/main.htm"));
            return true;
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return COMMANDS;
    }
}
