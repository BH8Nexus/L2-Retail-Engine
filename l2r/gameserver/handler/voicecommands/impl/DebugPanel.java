package l2r.gameserver.handler.voicecommands.impl;



import l2r.gameserver.DebugSystem.CombatLogCollector;
import l2r.gameserver.DebugSystem.CombatLogEntry;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.List;
import java.util.stream.Collectors;

public class DebugPanel implements IVoicedCommandHandler
{
    private static final String[] COMMANDS =
            {
                    "debugpanel",
                    "debugpanel_logs",
                    "debugpanel_breakdowns",
                    "debugpanel_clear"
            };

    @Override
    public boolean useVoicedCommand(String command, Player player, String target)
    {
        switch (command)
        {
            case "debugpanel":
                showMainPanel(player);
                return true;

            case "debugpanel_logs":
                showDamageLogs(player, target);
                return true;

            case "debugpanel_breakdowns":
                showBreakdownLogs(player, target);
                return true;

            case "debugpanel_clear":
                CombatLogCollector.clear(player.getObjectId());
                player.sendMessage("✔ Your debug logs were cleared.");
                showMainPanel(player);
                return true;
        }
        return false;
    }

    private void showMainPanel(Player player)
    {
        NpcHtmlMessage msg = new NpcHtmlMessage(0);
        msg.setFile("data/html/html-en/debugpanel/main.htm");
        player.sendPacket(msg);
    }

    private void showDamageLogs(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        String filter = getFilter(target);

        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        // ---- Filtering ----
        switch (filter)
        {
            case "crit":
                list = list.stream().filter(e -> e.crit).collect(Collectors.toList())
                ;
                break;

            case "miss":
                list = list.stream().filter(e -> e.miss).collect(Collectors.toList())
            ;
                break;

            case "high":
                list = list.stream().filter(e -> e.finalDamage >= 1000).collect(Collectors.toList())
                ;
                break;

            default:
                break;
        }

        // ---- Render HTML ----
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<table width=320>");

        int count = 0;
        for (int i = list.size() - 1; i >= 0 && count < 100; i--, count++)
        {
            CombatLogEntry e = list.get(i);

            sb.append("<tr><td>");
            sb.append(e.toSimpleLine());
            sb.append("</td></tr>");
        }

        sb.append("</table>");
        sb.append("<br><button value=\"Back\" action=\"bypass -h debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }


    private void showBreakdownLogs(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());

        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        String filter = getFilter(target);

        // ---- FILTERING ----
        switch (filter)
        {
            case "crit":
                list = list.stream().filter(e -> e.crit).collect(Collectors.toList());
                break;

            case "miss":
                list = list.stream().filter(e -> e.miss).collect(Collectors.toList());
                break;

            case "high":
                list = list.stream().filter(e -> e.finalDamage >= 1000).collect(Collectors.toList());
                break;

            default:
                break;
        }

        // ---- RENDER HTML ----
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<table width=350>");

        int count = 0;
        for (int i = list.size() - 1; i >= 0 && count < 50; i--, count++)
        {
            CombatLogEntry e = list.get(i);

            if (e.extraInfo == null)
                continue;

            sb.append("<tr><td><pre>");
            sb.append(e.extraInfo);
            sb.append("</pre></td></tr>");
        }

        sb.append("</table>");
        sb.append("<br><button value=\"Back\" action=\"bypass -h debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    private static final int LOGS_PER_PAGE = 50;

    private int getPage(String target)
    {
        try
        {
            if (target == null) return 1;
            if (target.startsWith("page="))
                return Integer.parseInt(target.substring(5));
        }
        catch (Exception ignored) {}
        return 1;
    }
    private String getFilter(String target)
    {
        if (target == null) return "";
        if (target.startsWith("filter="))
            return target.substring(7);
        return "";
    }



    @Override
    public String[] getVoicedCommandList()
    {
        return COMMANDS;
    }

    // ============================================================
    // MAIN HTML PAGE
    // ============================================================
    private void showMainPage(Player player)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/html-en/DebugPanel/main.htm");
        player.sendPacket(html);
    }
}
