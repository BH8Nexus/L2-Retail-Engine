package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.DebugSystem.LiveDebug;

public class BetaLogVC implements IVoicedCommandHandler
{
    private static final String[] COMMANDS = { "betalog" };

    @Override
    public boolean useVoicedCommand(String command, Player player, String target)
    {
        LiveDebug.toggle(player);
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return COMMANDS;
    }
}
