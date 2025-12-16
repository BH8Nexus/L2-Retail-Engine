package l2r.gameserver.DebugSystem.spectator;

import l2r.gameserver.DebugSystem.CombatLogCollector;
import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.List;

public class LiveSpectatorService
{
    public static void show(Player spectator)
    {
        SpectatorSession s =
                SpectatorRegistry.get(spectator.getObjectId());

        if (s == null)
        {
            spectator.sendMessage("Not spectating any fight.");
            return;
        }

        List<CombatLogEntry> logs =
                CombatLogCollector.get(s.watchedPlayerId);

        if (logs == null || logs.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>LIVE FIGHT SPECTATOR</h3>");

        int shown = 0;
        for (int i = logs.size() - 1; i >= 0 && shown < 8; i--)
        {
            sb.append(logs.get(i).toSimpleLine()).append("<br>");
            shown++;
        }

        sb.append("<br>");
        sb.append("<button value=\"Refresh\" action=\"bypass -h debugpanel_watch\" width=80 height=25>");
        sb.append("<button value=\"Stop\" action=\"bypass -h debugpanel_unwatch\" width=80 height=25>");
        sb.append("</center></body></html>");

        spectator.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }
}
