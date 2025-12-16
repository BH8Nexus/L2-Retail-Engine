package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.TestPvP.TestPvPService;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;

public class TestPvPCommand implements IVoicedCommandHandler
{
    private static final String[] COMMANDS = { "testpvp" };

    @Override
    public boolean useVoicedCommand(String command, Player player, String args)
    {
        // TODO: add config guard later

        if (player.isInOlympiadMode() || player.isInObserverMode())
        {
            player.sendMessage("You cannot use test PvP right now.");
            return true;
        }

        TestPvPService.prepareBaseline(player);
        player.sendMessage("Test PvP baseline applied (0 enchant).");
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return COMMANDS;
    }
}
