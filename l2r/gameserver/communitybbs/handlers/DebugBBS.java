package l2r.gameserver.communitybbs.handlers;

import l2r.gameserver.DebugSystem.export.ExportService;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.handler.voicecommands.impl.DebugPanel;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

public class DebugBBS implements ICommunityBoardHandler
{
    private static final String[] COMMANDS =
            {
                    "_bbsdebug",
                    "_bbsdebug_logs",
                    "_bbsdebug_breakdowns",
                    "_bbsdebug_analytics",
                    "_bbsdebug_timeline",
                    "_bbsdebug_compare",
                    "_bbsdebug_dps",
                    "_bbsdebug_death",
                    "_bbsdebug_export_fight_csv",
                    "_bbsdebug_export_fight_json",
                    "_bbsdebug_fight_select",
                    "_bbsdebug_kill",
                    "_bbsdebug_skills",
                    "_bbsdebug_session",
                    "_bbsdebug_pvp_report",
                    "_bbsdebug_tuning",
                    "_bbsdebug_tags",
                    "_bbsdebug_class_matrix",
                    "_bbsdebug_export",
                    "_bbsdebug_watchlist"


            };

    @Override
    public void onBypassCommand(Player player, String bypass)
    {
        if (player == null)
            return;

        if (!player.isGM())
        {
            player.sendMessage("Access denied.");
            return;
        }


        DebugPanel panel = new DebugPanel();

        if (bypass.equals("_bbsdebug"))
        {
            showMain(player);
            return;
        }

        if (bypass.startsWith("_bbsdebug_logs"))
        {
            String params = bypass.replace("_bbsdebug_logs", "").trim();
            panel.showDamageLogs(player, params);
            return;
        }

        if (bypass.startsWith("_bbsdebug_breakdowns"))
        {
            String params = bypass.replace("_bbsdebug_breakdowns", "").trim();
            panel.showBreakdownLogs(player, params);
        }

        if (bypass.startsWith("_bbsdebug_analytics"))
        {
            String params = bypass.replace("_bbsdebug_analytics", "").trim();
            panel.showAnalytics(player, params);
        }
        if (bypass.startsWith("_bbsdebug_timeline"))
        {
            String params = bypass.replace("_bbsdebug_timeline", "").trim();
            panel.showTimeline(player, params);
        }

        if (bypass.startsWith("_bbsdebug_compare"))
        {
            String params = bypass.replace("_bbsdebug_compare", "").trim();
            panel.showCompareTimeline(player, params);
        }
        if (bypass.startsWith("_bbsdebug_dps"))
        {
            String params = bypass.replace("_bbsdebug_dps", "").trim();
            panel.showRollingDps(player, params);
        }
        if (bypass.startsWith("_bbsdebug_death"))
        {
            String params = bypass.replace("_bbsdebug_death", "").trim();
            panel.showDeathReason(player, params);
        }
        if (bypass.equals("_bbsdebug_export_fight_csv"))
        {
            panel.exportFight(player, "", true);
        }
        if (bypass.equals("_bbsdebug_export_fight_json"))
        {
            panel.exportFight(player, "", false);
        }
        if (bypass.startsWith("_bbsdebug_fight_select"))
        {
            String params = bypass.replace("_bbsdebug_fight_select", "").trim();
            panel.showFightSelector(player, params);
        }

        if (bypass.startsWith("_bbsdebug_kill"))
        {
            String params = bypass.replace("_bbsdebug_kill", "").trim();
            panel.showKillRecap(player, params);
        }
        if (bypass.startsWith("_bbsdebug_skills"))
        {
            String params = bypass.replace("_bbsdebug_skills", "").trim();
            panel.showSkillEfficiency(player, params);
        }
        if (bypass.equals("_bbsdebug_session"))
        {
            panel.showSessionSummary(player);
        }
        if (bypass.equals("_bbsdebug_pvp_report"))
        {
            panel.showPvpBalanceReport(player);
        }
        if (bypass.equals("_bbsdebug_tuning"))
        {
            panel.showSkillTuningHints(player);
        }
        if (bypass.startsWith("_bbsdebug_tags"))
        {
            String params = bypass.replace("_bbsdebug_tags", "").trim();
            panel.showFightTags(player, params);
        }
        if (bypass.equals("_bbsdebug_class_matrix"))
        {
            panel.showClassMatrix(player);
        }

        if (bypass.startsWith("_bbsdebug_export"))
        {
            if (player.getAccessLevel().getLevel() < 100)
            {
                player.sendMessage("Admin only.");
                return;
            }

            String format = bypass.replace("_bbsdebug_export", "").trim();
            ExportService.exportSession(player, format);
            return;
        }
        if (bypass.equals("_bbsdebug_watchlist"))
        {
            showWatchList(player);
            return;
        }

        if (bypass.startsWith("_bbsdebug_watch "))
        {
            if (!player.isGM())
            {
                player.sendMessage("Access denied.");
                return;
            }

            int targetId = Integer.parseInt(bypass.split(" ")[1]);
            DebugPanel.startSpectator(player, targetId);
            return;
        }

    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
    {
        // Δεν χρησιμοποιείται (read-only debug panel)
    }

    private void showWatchList(Player player)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/html-en/debugpanel/watch_list.htm");

        StringBuilder sb = new StringBuilder();
        sb.append("<table width=300>");

        for (Player p : GameObjectsStorage.getAllPlayersForIterate())
        {
            if (p == null)
                continue;

            if (p.isGM())
                continue;

            if (!p.isInCombat())
                continue;

            sb.append("<tr>");
            sb.append("<td>").append(p.getName()).append("</td>");
            sb.append("<td>");
            sb.append("<button value=\"Watch\" ");
            sb.append("action=\"bypass -h _bbsdebug_watch ").append(p.getObjectId()).append("\" ");
            sb.append("width=60 height=20>");
            sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");

        html.replace("%players%", sb.toString());
        player.sendPacket(html);
    }



    private void showMain(Player player)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/html-en/debugpanel/main.htm");
        player.sendPacket(html);
    }

    @Override
    public String[] getBypassCommands()
    {
        return COMMANDS;
    }
}
