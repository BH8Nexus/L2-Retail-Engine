package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.DebugSystem.*;
import l2r.gameserver.DebugSystem.export.ExportService;
import l2r.gameserver.DebugSystem.spectator.LiveSpectatorService;
import l2r.gameserver.DebugSystem.spectator.SpectatorRegistry;
import l2r.gameserver.DebugSystem.testpvp.RangeType;
import l2r.gameserver.TestPvP.TestPvPService;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.model.Skill;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DebugPanel implements IVoicedCommandHandler
{
    private boolean requireGM(Player player)
    {
        if (!player.isGM())
        {
            player.sendMessage("Access denied.");
            return false;
        }
        return true;
    }

    private boolean requireAdmin(Player player)
    {
        if (!player.isGM() || player.getAccessLevel().getLevel() < 100)
        {
            player.sendMessage("Admin only.");
            return false;
        }
        return true;
    }


    private static final String[] COMMANDS =
            {
                    "debugpanel",
                    "debugpanel_logs",
                    "debugpanel_breakdowns",
                    "debugpanel_clear",
                    "debugpanel_export_csv",
                    "debugpanel_export_txt",
                    "debugpanel_analytics",
                    "debugpanel_timeline",
                    "debugpanel_compare",
                    "debugpanel_fights",
                    "debugpanel_dps",
                    "debugpanel_death",
                    "debugpanel_export_fight_csv",
                    "debugpanel_export_fight_json",
                    "debugpanel_fight_select",
                    "debugpanel_kill",
                    "debugpanel_skills",
                    "debugpanel_session",
                    "debugpanel_pvp_report",
                    "debugpanel_tuning",
                    "debugpanel_tags",
                    "debugpanel_class_matrix",
                    "debugpanel_export",
                    "debugpanel_watch",
                    "debugpanel_unwatch",
                    "debugpanel_testpvp",
                    "debugpanel_testpvp_exit",
                    "debugpanel_testpvp_enchant0",
                    "debugpanel_testpvp_enchant5",
                    "debugpanel_testpvp_enchant15",
                    "debugpanel_testpvp_cycle"


            };

    private static final int PAGE_SIZE = 20;
    private static final int MAX_PAGES = 50;

    @Override
    public boolean useVoicedCommand(String command, Player player, String target)
    {
        switch (command)
        {
            case "debugpanel":
            {
                if (!requireGM(player))
                    return true;

                showMainPanel(player);
                return true;
            }

            case "debugpanel_logs":
            case "debugpanel_breakdowns":
            {
                if (!requireGM(player))
                    return true;

                showDamageLogs(player, target);
                return true;
            }

            case "debugpanel_export_csv":
                exportLogs(player, target, true);
                return true;

            case "debugpanel_export_txt":
                exportLogs(player, target, false);
                return true;

            case "debugpanel_analytics":
                showAnalytics(player, target);
                return true;

            case "debugpanel_timeline":
                showTimeline(player, target);
                return true;

            case "debugpanel_compare":
                showCompareTimeline(player, target);
                return true;

            case "debugpanel_fights":
                showFightList(player, target);
                return true;

            case "debugpanel_dps":
                showRollingDps(player, target);
                return true;

            case "debugpanel_death":
                showDeathReason(player, target);
                return true;

            case "debugpanel_export_fight_csv":
                exportFight(player, target, true);
                return true;

            case "debugpanel_export_fight_json":
                exportFight(player, target, false);
                return true;

            case "debugpanel_fight_select":
                showFightSelector(player, target);
                return true;

            case "debugpanel_kill":
                showKillRecap(player, target);
                return true;

            case "debugpanel_skills":
                showSkillEfficiency(player, target);
                return true;

            case "debugpanel_session":
                showSessionSummary(player);
                return true;

            case "debugpanel_pvp_report":
                showPvpBalanceReport(player);
                return true;

            case "debugpanel_tuning":
                showSkillTuningHints(player);
                return true;

            case "debugpanel_tags":
                showFightTags(player, target);
                return true;

            case "debugpanel_class_matrix":
                showClassMatrix(player);
                return true;

            case "debugpanel_export":
            {
                if (!requireAdmin(player))
                    return true;

                ExportService.exportSession(player, target);
                return true;
            }

            case "debugpanel_watch":
            {
                if (!requireAdmin(player))
                    return true;

                int targetId = Integer.parseInt(target);
                SpectatorRegistry.start(player.getObjectId(), targetId);
                LiveSpectatorService.show(player);
                return true;
            }

            case "debugpanel_unwatch":
            {
                if (!requireAdmin(player))
                    return true;

                SpectatorRegistry.stop(player.getObjectId());
                player.sendMessage("Spectator mode stopped.");
                return true;
            }

            case "debugpanel_clear":
                CombatLogCollector.clear(player.getObjectId());
                player.sendMessage("✔ Your debug logs were cleared.");
                showMainPanel(player);
                return true;

            case "debugpanel_testpvp":
            {

                TestPvPService.prepareBaseline(player);
                player.sendMessage("✔ TestPvP baseline applied (0 enchant).");
                return true;
            }
            case "debugpanel_testpvp_exit":
            {

                TestPvPService.exitTest(player);
                return true;
            }
            case "debugpanel_testpvp_enchant0":
            {
                if (!player.isGM())
                    return true;

                TestPvPService.applyEnchant(player, 0);
                return true;
            }

            case "debugpanel_testpvp_enchant5":
            {
                if (!player.isGM())
                    return true;

                TestPvPService.applyEnchant(player, 5);
                return true;
            }

            case "debugpanel_testpvp_enchant15":
            {
                if (!player.isGM())
                    return true;

                TestPvPService.applyEnchant(player, 15);
                return true;
            }
            case "debugpanel_testpvp_cycle":
            {
                if (!player.isGM())
                    return true;

                if (!(player.getTarget() instanceof Player))
                {
                    player.sendMessage("Target a player first.");
                    return true;
                }


                Player targetPlayer =
                        player.getTarget() instanceof Player
                                ? (Player) player.getTarget()
                                : null;

                TestPvPService.startRangeCycle(player, targetPlayer);
                return true;
            }



        }
        return false;
    }

    private void showMainPanel(Player player)
    {
        NpcHtmlMessage msg = new NpcHtmlMessage(0);
        msg.setFile("data/html/html-en/debugpanel/main.htm");
        player.sendPacket(msg);
    }

    // ========================= DAMAGE LOGS =========================
    public void showDamageLogs(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        String filter = getFilter(target);
        int page = extractPage(target);

        List<CombatLogEntry> filtered =
                list.stream().filter(e -> matchFilter(e, filter)).collect(Collectors.toList());

        int total = filtered.size();
        int totalPages = Math.min(MAX_PAGES, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (totalPages == 0)
            totalPages = 1;

        page = Math.min(page, totalPages - 1);

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");

        appendNavigation(sb, "debugpanel_logs", filter, page, totalPages);

        sb.append("<table width=320>");
        for (int i = to - 1; i >= from; i--)
        {
            CombatLogEntry e = filtered.get(i);

            sb.append("<tr>");
            sb.append("<td width=40 align=center>");
            sb.append(getSkillIconHtml(e.skillId));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(e.toSimpleLine());
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table><br>");

        appendNavigation(sb, "debugpanel_logs", filter, page, totalPages);

        sb.append("<br>");
        sb.append("<button value=\"Export CSV\" action=\"bypass -h voice .debugpanel_export_csv ")
                .append(filter).append("\" width=120 height=25>");
        sb.append("<button value=\"Export TXT\" action=\"bypass -h voice .debugpanel_export_txt ")
                .append(filter).append("\" width=120 height=25><br><br>");

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    // ========================= BREAKDOWNS =========================
    public void showBreakdownLogs(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        String filter = getFilter(target);
        int page = extractPage(target);

        List<CombatLogEntry> filtered =
                list.stream()
                        .filter(e -> e.extraInfo != null)
                        .filter(e -> matchFilter(e, filter))
                        .collect(Collectors.toList());

        int total = filtered.size();
        int totalPages = Math.min(MAX_PAGES, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (totalPages == 0)
            totalPages = 1;

        page = Math.min(page, totalPages - 1);

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");

        appendNavigation(sb, "debugpanel_breakdowns", filter, page, totalPages);

        sb.append("<table width=350>");
        for (int i = to - 1; i >= from; i--)
        {
            sb.append("<tr><td><pre>");
            sb.append(filtered.get(i).extraInfo);
            sb.append("</pre></td></tr>");
        }
        sb.append("</table><br>");

        appendNavigation(sb, "debugpanel_breakdowns", filter, page, totalPages);

        sb.append("<br><button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    // ========================= EXPORT =========================
    private void exportLogs(Player player, String target, boolean csv)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs to export.");
            return;
        }

        String filter = getFilter(target);

        List<CombatLogEntry> filtered =
                list.stream().filter(e -> matchFilter(e, filter)).collect(Collectors.toList());

        try
        {
            File dir = new File("log/debug/");
            if (!dir.exists())
                dir.mkdirs();

            String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String name = "debug_" + player.getName() + "_" + time + (csv ? ".csv" : ".txt");
            File file = new File(dir, name);

            FileWriter fw = new FileWriter(file);

            if (csv)
                fw.write("time,attacker,target,skillId,damage,crit,miss\n");

            for (CombatLogEntry e : filtered)
            {
                if (csv)
                {
                    fw.write(e.time + "," + e.attackerName + "," + e.targetName + "," +
                            e.skillId + "," + e.finalDamage + "," +
                            e.crit + "," + e.miss + "\n");
                }
                else
                {
                    fw.write(e.toSimpleLine() + "\n");
                }
            }
            fw.close();

            player.sendMessage("✔ Exported: log/debug/" + name);
        }
        catch (Exception e)
        {
            player.sendMessage("Export failed. Check server logs.");
            e.printStackTrace();
        }
    }

    // ========================= HELPERS =========================
    private void appendNavigation(StringBuilder sb, String cmd, String filter, int page, int totalPages)
    {
        sb.append("<table width=320><tr>");

        if (page > 0)
            sb.append("<td align=left><button value=\"<< Prev\" action=\"bypass -h voice .")
                    .append(cmd).append(" ").append(filter)
                    .append(" page:").append(page - 1)
                    .append("\" width=60 height=20></td>");
        else sb.append("<td></td>");

        sb.append("<td align=center>Page ").append(page + 1).append(" / ").append(totalPages).append("</td>");

        if (page + 1 < totalPages)
            sb.append("<td align=right><button value=\"Next >>\" action=\"bypass -h voice .")
                    .append(cmd).append(" ").append(filter)
                    .append(" page:").append(page + 1)
                    .append("\" width=60 height=20></td>");
        else sb.append("<td></td>");

        sb.append("</tr></table><br>");
    }

    private int extractPage(String params)
    {
        if (params == null)
            return 0;

        for (String p : params.split(" "))
        {
            if (p.startsWith("page:"))
            {
                try { return Math.max(0, Integer.parseInt(p.substring(5))); }
                catch (Exception ignored) {}
            }
        }
        return 0;
    }

    private String getFilter(String target)
    {
        if (target == null)
            return "";
        return target.replaceAll("page:\\d+", "").trim();
    }

    private boolean matchFilter(CombatLogEntry e, String filter)
    {
        if (filter == null || filter.isEmpty())
            return true;

        String[] parts = filter.toLowerCase().split(" ");
        for (String f : parts)
        {
            if (f.startsWith("skill:"))
            {
                int id = Integer.parseInt(f.substring(6));
                if (e.skillId != id)
                    return false;
            }
            else if (f.equals("crit") && !e.crit)
                return false;
            else if (f.equals("miss") && !e.miss)
                return false;
            else if (f.startsWith("dmg>"))
            {
                double min = Double.parseDouble(f.substring(4));
                if (e.finalDamage < min)
                    return false;
            }
            else if (f.startsWith("range:"))
            {
                String r = f.substring(6).toUpperCase();
                try
                {
                    if (e.rangeType != RangeType.valueOf(r))
                        return false;
                }
                catch (Exception ex)
                {
                    return false;
                }
            }

        }
        return true;
    }

    public void showAnalytics(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        String filter = getFilter(target);

        List<CombatLogEntry> filtered =
                list.stream()
                        .filter(e -> matchFilter(e, filter))
                        .collect(Collectors.toList());

        CombatAnalytics.Summary s = CombatAnalytics.analyze(filtered);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");

        sb.append("<table width=320>");
        sb.append("<tr><td>Total Hits</td><td>").append(s.totalHits).append("</td></tr>");
        sb.append("<tr><td>Average Damage</td><td>").append(String.format("%.1f", s.avgDamage())).append("</td></tr>");
        sb.append("<tr><td>Crit Rate</td><td>").append(String.format("%.1f%%", s.critRate())).append("</td></tr>");
        sb.append("<tr><td>Miss Rate</td><td>").append(String.format("%.1f%%", s.missRate())).append("</td></tr>");
        sb.append("</table><br>");

        sb.append("<table width=350>");
        sb.append("<tr><th>Skill</th><th>Hits</th><th>Avg Dmg</th><th>Crit%</th></tr>");

        s.perSkill.forEach((skillId, st) ->
        {
            sb.append("<tr>");
            sb.append("<td>").append(skillId).append("</td>");
            sb.append("<td>").append(st.hits).append("</td>");
            sb.append("<td>").append(String.format("%.1f", st.avgDamage())).append("</td>");
            sb.append("<td>").append(String.format("%.1f%%", st.critRate())).append("</td>");
            sb.append("</tr>");
        });

        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }


    // ========================= C6 SKILL TOOLTIP =========================
    private String getSkillIconHtml(int skillId)
    {
        Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

        String title;
        if (skill != null)
            title = skill.getName() + " (Lv " + skill.getLevel() + ")";
        else
            title = "Skill ID: " + skillId;

        String icon = (skillId > 0)
                ? "icon.skill" + String.format("%04d", skillId)
                : "icon.etc_question_mark_i00";

        return "<img src=\"" + icon + "\" width=32 height=32 title=\"" + title + "\">";
    }


    public void showTimeline(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        String filter = getFilter(target);

        List<CombatLogEntry> filtered =
                list.stream()
                        .filter(e -> matchFilter(e, filter))
                        .collect(Collectors.toList());

        List<DamageTimeline.Point> points = DamageTimeline.build(filtered);

        double max = 0;
        for (DamageTimeline.Point p : points)
            if (p.damage > max)
                max = p.damage;

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<table width=380>");

        sb.append("<tr><th>Time</th><th>Damage</th><th>Graph</th></tr>");

        for (DamageTimeline.Point p : points)
        {
            int bar = max > 0 ? (int) ((p.damage / max) * 150) : 0;

            sb.append("<tr>");
            sb.append("<td>").append(p.second).append("</td>");
            sb.append("<td>").append((int) p.damage).append("</td>");
            sb.append("<td>");
            sb.append("<div title=\"Hits: ").append(p.hits)
                    .append(" | Crits: ").append(p.crits)
                    .append("\" style=\"background:#FF5555;height:10px;width:")
                    .append(bar)
                    .append("px\"></div>");
            sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showCompareTimeline(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        String filter = getFilter(target);

        List<CombatLogEntry> filtered =
                list.stream()
                        .filter(e -> matchFilter(e, filter))
                        .collect(Collectors.toList());

        List<DamageCompareTimeline.Point> points =
                DamageCompareTimeline.build(filtered, player.getName());

        double max = 0;
        for (DamageCompareTimeline.Point p : points)
        {
            max = Math.max(max, Math.max(p.outgoing, p.incoming));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<table width=420>");
        sb.append("<tr><th>Time</th><th>OUT</th><th>IN</th><th>Compare</th></tr>");

        for (DamageCompareTimeline.Point p : points)
        {
            int outBar = max > 0 ? (int) ((p.outgoing / max) * 120) : 0;
            int inBar  = max > 0 ? (int) ((p.incoming / max) * 120) : 0;

            sb.append("<tr>");
            sb.append("<td>").append(p.second).append("</td>");
            sb.append("<td>").append((int) p.outgoing).append("</td>");
            sb.append("<td>").append((int) p.incoming).append("</td>");
            sb.append("<td>");
            sb.append("<div style=\"display:flex\">");
            sb.append("<div title=\"Outgoing\" style=\"background:#55FF55;height:10px;width:")
                    .append(outBar).append("px\"></div>");
            sb.append("<div title=\"Incoming\" style=\"background:#FF5555;height:10px;width:")
                    .append(inBar).append("px\"></div>");
            sb.append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showFightList(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights =
                FightAnalyzer.split(list, player.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<table width=420>");
        sb.append("<tr><th>#</th><th>Duration</th><th>OUT</th><th>IN</th><th>Crit%</th></tr>");

        int i = 1;
        for (FightSnapshot f : fights)
        {
            double critRate = f.hits == 0 ? 0 : (f.crits * 100.0 / f.hits);

            sb.append("<tr>");
            sb.append("<td>").append(i).append("</td>");
            sb.append("<td>").append(f.durationMs() / 1000.0).append("s</td>");
            sb.append("<td>").append((int) f.outgoingDamage).append("</td>");
            sb.append("<td>").append((int) f.incomingDamage).append("</td>");
            sb.append("<td>").append(String.format("%.1f%%", critRate)).append("</td>");
            sb.append("</tr>");
            i++;
        }

        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showRollingDps(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        // πάρε fights
        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        // δείχνουμε το ΤΕΛΕΥΤΑΙΟ fight (safe default)
        int idx = extractFightIndex(target, fights.size() - 1);
        FightSnapshot fight = fights.get(idx);

        List<RollingDps.Point> dps3 =
                RollingDps.compute(fight, player.getName(), 3);
        List<RollingDps.Point> dps5 =
                RollingDps.compute(fight, player.getName(), 5);

        double max = 0;
        for (RollingDps.Point p : dps3)
            max = Math.max(max, Math.max(p.dpsOut, p.dpsIn));
        for (RollingDps.Point p : dps5)
            max = Math.max(max, Math.max(p.dpsOut, p.dpsIn));

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Rolling DPS (Last Fight)</h3>");

        // ---- 3s window
        sb.append("<table width=420>");
        sb.append("<tr><th colspan=4>3s Window</th></tr>");
        sb.append("<tr><th>Time</th><th>OUT</th><th>IN</th><th>Graph</th></tr>");

        for (RollingDps.Point p : dps3)
        {
            int outBar = max > 0 ? (int) ((p.dpsOut / max) * 120) : 0;
            int inBar  = max > 0 ? (int) ((p.dpsIn  / max) * 120) : 0;

            sb.append("<tr>");
            sb.append("<td>").append(p.second).append("</td>");
            sb.append("<td>").append((int) p.dpsOut).append("</td>");
            sb.append("<td>").append((int) p.dpsIn).append("</td>");
            sb.append("<td><div style=\"display:flex\">");
            sb.append("<div style=\"background:#55FF55;height:10px;width:")
                    .append(outBar).append("px\"></div>");
            sb.append("<div style=\"background:#FF5555;height:10px;width:")
                    .append(inBar).append("px\"></div>");
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        sb.append("</table><br>");

        // ---- 5s window
        sb.append("<table width=420>");
        sb.append("<tr><th colspan=4>5s Window</th></tr>");
        sb.append("<tr><th>Time</th><th>OUT</th><th>IN</th><th>Graph</th></tr>");

        for (RollingDps.Point p : dps5)
        {
            int outBar = max > 0 ? (int) ((p.dpsOut / max) * 120) : 0;
            int inBar  = max > 0 ? (int) ((p.dpsIn  / max) * 120) : 0;

            sb.append("<tr>");
            sb.append("<td>").append(p.second).append("</td>");
            sb.append("<td>").append((int) p.dpsOut).append("</td>");
            sb.append("<td>").append((int) p.dpsIn).append("</td>");
            sb.append("<td><div style=\"display:flex\">");
            sb.append("<div style=\"background:#55FF55;height:10px;width:")
                    .append(outBar).append("px\"></div>");
            sb.append("<div style=\"background:#FF5555;height:10px;width:")
                    .append(inBar).append("px\"></div>");
            sb.append("</div></td>");
            sb.append("</tr>");
        }
        sb.append("</table><br>");

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showDeathReason(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        // Παίρνουμε το τελευταίο fight
        int idx = extractFightIndex(target, fights.size() - 1);
        FightSnapshot fight = fights.get(idx);

        DeathAnalyzer.Result r =
                DeathAnalyzer.analyze(fight, player.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Death Analysis (Last Fight)</h3>");

        if (!r.died)
        {
            sb.append("<br><font color=LEVEL>Not a death scenario.</font><br>");
        }
        else
        {
            sb.append("<table width=360>");
            sb.append("<tr><td><b>Reason</b></td><td>").append(r.reason).append("</td></tr>");
            sb.append("<tr><td>Total Incoming</td><td>").append((int) r.incomingTotal).append("</td></tr>");
            sb.append("<tr><td>Incoming (last 3s)</td><td>").append((int) r.incomingWindow).append("</td></tr>");
            sb.append("<tr><td>Crits (last 3s)</td><td>").append(r.critsInWindow).append("</td></tr>");
            sb.append("</table><br>");

            if (!r.dmgPerSkill.isEmpty())
            {
                sb.append("<table width=360>");
                sb.append("<tr><th>Skill</th><th>Damage</th></tr>");
                r.dmgPerSkill.forEach((skillId, dmg) ->
                {
                    sb.append("<tr>");
                    sb.append("<td>").append(skillId).append("</td>");
                    sb.append("<td>").append(dmg.intValue()).append("</td>");
                    sb.append("</tr>");
                });

                sb.append("</table><br>");
            }
        }

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void exportFight(Player player, String target, boolean csv)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        // fight index (default: last)
        int index = fights.size() - 1;

        try
        {
            File file = csv
                    ? FightExporter.exportCsv(fights.get(index), index, player.getName())
                    : FightExporter.exportJson(fights.get(index), index, player.getName());

            player.sendMessage("✔ Fight exported: " + file.getPath());
        }
        catch (Exception e)
        {
            player.sendMessage("Export failed. Check server logs.");
            e.printStackTrace();
        }
    }

    public void showFightSelector(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        int maxIndex = fights.size() - 1;
        int fightIdx = extractFightIndex(target, maxIndex);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Select Fight</h3>");

        sb.append("<table width=420>");
        sb.append("<tr><th>#</th><th>Duration</th><th>OUT</th><th>IN</th><th>Action</th></tr>");

        for (int i = 0; i < fights.size(); i++)
        {
            FightSnapshot f = fights.get(i);
            sb.append("<tr>");
            sb.append("<td>").append(i).append("</td>");
            sb.append("<td>").append(f.durationMs() / 1000.0).append("s</td>");
            sb.append("<td>").append((int) f.outgoingDamage).append("</td>");
            sb.append("<td>").append((int) f.incomingDamage).append("</td>");
            sb.append("<td>");
            sb.append("<button value=\"View\" action=\"bypass -h voice .debugpanel_analytics fight:")
                    .append(i)
                    .append("\" width=60 height=20>");
            sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table><br>");

        // Prev / Next
        sb.append("<table width=420><tr>");
        if (fightIdx > 0)
        {
            sb.append("<td align=left>")
                    .append("<button value=\"<< Prev\" action=\"bypass -h voice .debugpanel_fight_select fight:")
                    .append(fightIdx - 1)
                    .append("\" width=80 height=20></td>");
        }
        else sb.append("<td></td>");

        sb.append("<td align=center>Selected: ").append(fightIdx).append(" / ").append(maxIndex).append("</td>");

        if (fightIdx < maxIndex)
        {
            sb.append("<td align=right>")
                    .append("<button value=\"Next >>\" action=\"bypass -h voice .debugpanel_fight_select fight:")
                    .append(fightIdx + 1)
                    .append("\" width=80 height=20></td>");
        }
        else sb.append("<td></td>");

        sb.append("</tr></table><br>");

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }


    private int extractFightIndex(String params, int max)
    {
        if (params == null)
            return max;

        for (String p : params.split(" "))
        {
            if (p.startsWith("fight:"))
            {
                try
                {
                    int idx = Integer.parseInt(p.substring(6));
                    return Math.max(0, Math.min(idx, max));
                }
                catch (Exception ignored) {}
            }
        }
        return max;
    }

    public void showKillRecap(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        int idx = extractFightIndex(target, fights.size() - 1);
        FightSnapshot fight = fights.get(idx);

        KillAnalyzer.Result r =
                KillAnalyzer.analyze(fight, player.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Kill Recap (Fight ").append(idx).append(")</h3>");

        if (!r.killed)
        {
            sb.append("<br><font color=LEVEL>No kill detected.</font><br>");
        }
        else
        {
            sb.append("<table width=360>");
            sb.append("<tr><td><b>Reason</b></td><td>").append(r.reason).append("</td></tr>");
            sb.append("<tr><td>Total Outgoing</td><td>").append((int) r.outgoingTotal).append("</td></tr>");
            sb.append("<tr><td>Outgoing (last 3s)</td><td>").append((int) r.outgoingWindow).append("</td></tr>");
            sb.append("<tr><td>Crits (last 3s)</td><td>").append(r.critsInWindow).append("</td></tr>");
            sb.append("</table><br>");

            if (!r.dmgPerSkill.isEmpty())
            {
                sb.append("<table width=360>");
                sb.append("<tr><th>Skill</th><th>Damage</th></tr>");
                r.dmgPerSkill.forEach((skillId, dmg) ->
                {
                    sb.append("<tr>");
                    sb.append("<td>").append(skillId).append("</td>");
                    sb.append("<td>").append(dmg.intValue()).append("</td>");
                    sb.append("</tr>");
                });
                sb.append("</table><br>");
            }
        }

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showSkillEfficiency(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        int idx = extractFightIndex(target, fights.size() - 1);
        FightSnapshot fight = fights.get(idx);

        Map<Integer, SkillEfficiencyAnalyzer.SkillStat> stats =
                SkillEfficiencyAnalyzer.analyze(fight, player.getName());

        if (stats.isEmpty())
        {
            player.sendMessage("No skill usage detected.");
            return;
        }

        double totalOut = fight.outgoingDamage;

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Skill Efficiency (Fight ").append(idx).append(")</h3>");

        sb.append("<table width=520>");
        sb.append("<tr>");
        sb.append("<th>Skill</th>");
        sb.append("<th>Hits</th>");
        sb.append("<th>Crit%</th>");
        sb.append("<th>Total</th>");
        sb.append("<th>Avg</th>");
        sb.append("<th>Share%</th>");
        sb.append("<th>Eff</th>");
        sb.append("</tr>");

        stats.forEach((skillId, st) ->
        {
            double share = totalOut > 0 ? (st.totalDamage * 100.0 / totalOut) : 0;

            sb.append("<tr>");
            sb.append("<td>").append(skillId).append("</td>");
            sb.append("<td>").append(st.hits).append("</td>");
            sb.append("<td>").append(String.format("%.1f", st.critRate())).append("</td>");
            sb.append("<td>").append((int) st.totalDamage).append("</td>");
            sb.append("<td>").append((int) st.avgDamage()).append("</td>");
            sb.append("<td>").append(String.format("%.1f", share)).append("</td>");
            sb.append("<td>").append((int) st.efficiency()).append("</td>");
            sb.append("</tr>");
        });


        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showSessionSummary(Player player)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        SessionSummaryAnalyzer.Summary s =
                SessionSummaryAnalyzer.analyze(fights, player.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Session Summary</h3>");

        sb.append("<table width=420>");
        sb.append("<tr><td>Fights</td><td>").append(s.fights).append("</td></tr>");
        sb.append("<tr><td>Total Duration</td><td>")
                .append(String.format("%.1f", s.totalDurationMs / 1000.0)).append("s</td></tr>");
        sb.append("<tr><td>Total OUT</td><td>").append((int) s.totalOut).append("</td></tr>");
        sb.append("<tr><td>Total IN</td><td>").append((int) s.totalIn).append("</td></tr>");
        sb.append("<tr><td>Avg OUT DPS</td><td>").append((int) s.avgOutDps()).append("</td></tr>");
        sb.append("<tr><td>Avg IN DPS</td><td>").append((int) s.avgInDps()).append("</td></tr>");
        sb.append("<tr><td>Crit Rate</td><td>")
                .append(String.format("%.1f%%", s.critRate())).append("</td></tr>");
        sb.append("<tr><td>Miss Rate</td><td>")
                .append(String.format("%.1f%%", s.missRate())).append("</td></tr>");
        sb.append("<tr><td>Kills</td><td>").append(s.kills).append("</td></tr>");
        sb.append("<tr><td>Deaths</td><td>").append(s.deaths).append("</td></tr>");
        sb.append("</table><br>");

        sb.append("<table width=420>");
        sb.append("<tr><th>Highlights</th><th>Fight #</th></tr>");
        sb.append("<tr><td>Longest Fight</td><td>").append(s.longestFightIdx).append("</td></tr>");
        sb.append("<tr><td>Highest DPS</td><td>").append(s.maxDpsFightIdx).append("</td></tr>");
        sb.append("<tr><td>Biggest Burst</td><td>").append(s.maxBurstFightIdx).append("</td></tr>");
        sb.append("</table><br>");

        // Top 5 skills
        sb.append("<table width=420>");
        sb.append("<tr><th colspan=3>Top Skills</th></tr>");
        sb.append("<tr><th>Skill</th><th>Damage</th><th>Share%</th></tr>");

        s.skillDamage.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(e ->
                {
                    double share = s.totalOut > 0 ? (e.getValue() * 100.0 / s.totalOut) : 0;

                    sb.append("<tr>");
                    sb.append("<td>").append(e.getKey()).append("</td>");
                    sb.append("<td>").append(e.getValue().intValue()).append("</td>");
                    sb.append("<td>").append(String.format("%.1f", share)).append("</td>");
                    sb.append("</tr>");
                });

        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }


    public void showPvpBalanceReport(Player player)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        SessionSummaryAnalyzer.Summary session =
                SessionSummaryAnalyzer.analyze(fights, player.getName());

        List<PvpBalanceAnalyzer.Warning> warnings =
                PvpBalanceAnalyzer.analyze(fights, session, player.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>PvP Balance Report</h3>");

        if (warnings.isEmpty())
        {
            sb.append("<br><font color=LEVEL>No balance issues detected.</font><br>");
        }
        else
        {
            sb.append("<table width=500>");
            sb.append("<tr><th>Type</th><th>Warning</th></tr>");

            for (PvpBalanceAnalyzer.Warning w : warnings)
            {
                sb.append("<tr>");
                sb.append("<td>").append(w.type).append("</td>");
                sb.append("<td>").append(w.message).append("</td>");
                sb.append("</tr>");
            }

            sb.append("</table><br>");
        }

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showSkillTuningHints(Player player)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        // session summary
        SessionSummaryAnalyzer.Summary session =
                SessionSummaryAnalyzer.analyze(fights, player.getName());

        // session skill stats (aggregate from fights)
        Map<Integer, SkillEfficiencyAnalyzer.SkillStat> skillStats = new java.util.HashMap<>();
        for (FightSnapshot f : fights)
        {
            Map<Integer, SkillEfficiencyAnalyzer.SkillStat> m =
                    SkillEfficiencyAnalyzer.analyze(f, player.getName());
            m.forEach((id, st) ->
            {
                SkillEfficiencyAnalyzer.SkillStat agg =
                        skillStats.computeIfAbsent(id, k -> new SkillEfficiencyAnalyzer.SkillStat());
                agg.hits += st.hits;
                agg.crits += st.crits;
                agg.totalDamage += st.totalDamage;
            });
        }

        // PvP warnings
        List<PvpBalanceAnalyzer.Warning> warnings =
                PvpBalanceAnalyzer.analyze(fights, session, player.getName());

        List<SkillTuningAdvisor.Hint> hints =
                SkillTuningAdvisor.analyze(skillStats, session, warnings);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Skill Tuning Hints</h3>");

        if (hints.isEmpty())
        {
            sb.append("<br><font color=LEVEL>No tuning suggestions.</font><br>");
        }
        else
        {
            sb.append("<table width=520>");
            sb.append("<tr><th>Skill</th><th>Type</th><th>Reason</th></tr>");

            for (SkillTuningAdvisor.Hint h : hints)
            {
                String color = "NERF".equals(h.type) ? "FF5555" : "55FF55";
                sb.append("<tr>");
                sb.append("<td>").append(h.skillId).append("</td>");
                sb.append("<td><font color=").append(color).append(">")
                        .append(h.type).append("</font></td>");
                sb.append("<td>").append(h.reason).append("</td>");
                sb.append("</tr>");
            }

            sb.append("</table><br>");
        }

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }
    public void showFightTags(Player player, String target)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        int idx = extractFightIndex(target, fights.size() - 1);
        FightSnapshot fight = fights.get(idx);

        List<FightTagAnalyzer.Tag> tags =
                FightTagAnalyzer.analyze(fight, player.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Fight Tags (Fight ").append(idx).append(")</h3>");

        if (tags.isEmpty())
        {
            sb.append("<br><font color=LEVEL>No tags detected.</font><br>");
        }
        else
        {
            sb.append("<table width=320>");
            sb.append("<tr><th>Tag</th><th>Description</th></tr>");

            for (FightTagAnalyzer.Tag t : tags)
            {
                sb.append("<tr>");
                sb.append("<td>").append(t.name()).append("</td>");
                sb.append("<td>").append(describeTag(t)).append("</td>");
                sb.append("</tr>");
            }

            sb.append("</table><br>");
        }

        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public void showClassMatrix(Player player)
    {
        List<CombatLogEntry> list = CombatLogCollector.get(player.getObjectId());
        if (list == null || list.isEmpty())
        {
            player.sendMessage("No logs found.");
            return;
        }

        List<FightSnapshot> fights = FightAnalyzer.split(list, player.getName());
        if (fights.isEmpty())
        {
            player.sendMessage("No fights detected.");
            return;
        }

        Map<ClassId, ClassVsClassAnalyzer.Stat> stats =
                ClassVsClassAnalyzer.analyze(fights, player);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<h3>Class vs Class Balance</h3>");

        sb.append("<table width=520>");
        sb.append("<tr>");
        sb.append("<th>Opponent</th>");
        sb.append("<th>Fights</th>");
        sb.append("<th>Win%</th>");
        sb.append("<th>Avg OUT</th>");
        sb.append("<th>Avg IN</th>");
        sb.append("<th>Avg Time</th>");
        sb.append("<th>Status</th>");
        sb.append("</tr>");

        stats.forEach((cls, st) ->
        {
            double wr = st.fights == 0 ? 0 : (st.wins * 100.0 / st.fights);
            String flag = ClassVsClassAnalyzer.balanceFlag(st);

            sb.append("<tr>");
            sb.append("<td>").append(cls.toPrettyString()).append("</td>");
            sb.append("<td>").append(st.fights).append("</td>");
            sb.append("<td>").append(String.format("%.1f", wr)).append("</td>");
            sb.append("<td>").append((int) (st.totalOut / st.fights)).append("</td>");
            sb.append("<td>").append((int) (st.totalIn / st.fights)).append("</td>");
            sb.append("<td>").append(String.format("%.1f",
                    st.totalDurationMs / 1000.0 / st.fights)).append("s</td>");

            String color =
                    "ADVANTAGE".equals(flag) ? "55FF55" :
                            "DISADVANTAGE".equals(flag) ? "FF5555" : "LEVEL";

            sb.append("<td><font color=").append(color).append(">")
                    .append(flag).append("</font></td>");
            sb.append("</tr>");
        });

        sb.append("</table><br>");
        sb.append("<button value=\"Back\" action=\"bypass -h voice .debugpanel\" width=100 height=25>");
        sb.append("</center></body></html>");

        player.sendPacket(new NpcHtmlMessage(0).setHtml(sb.toString()));
    }

    public static void startSpectator(Player gm, int targetId)
    {
        Player target = World.getPlayer(targetId);
        if (target == null)
        {
            gm.sendMessage("Target not found.");
            return;
        }

        Location loc = target.getLoc();
        gm.enterObserverMode(loc);

        gm.sendMessage("Now spectating: " + target.getName());
    }

    private String describeTag(FightTagAnalyzer.Tag t)
    {
        switch (t)
        {
            case BURST: return "High damage concentrated in short time window";
            case SUSTAIN: return "Consistent damage over long duration";
            case POKE: return "Many low-damage hits";
            case SPIKE: return "Critical hit spike";
            case LOW_IMPACT: return "Minimal combat impact";
            default: return "";
        }
    }


    @Override
    public String[] getVoicedCommandList()
    {
        return COMMANDS;
    }
}
