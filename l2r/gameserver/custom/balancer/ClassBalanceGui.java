/*
 * Copyright (C) 2004-2016 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.custom.balancer;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.custom.balancer.ClassBalanceHolder.AttackType;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.utils.Language;

/**
 * @author DevAtlas
 */
public class ClassBalanceGui implements ICommunityBoardHandler
{
	private static void showMainWindow(Player activeChar)
	{
		String html = "<html noscrollbar><title>Balance System by DevAtlas</title><body><center><br><br><br>";
		html += "<button value=\"Class Balance\" action=\"bypass _bbs_classbalance 1 false\" width=90 height=23 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">";
		html += "<button value=\"Oly Balance\" action=\"bypass _bbs_classbalance 1 true\" width=90 height=23 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">";
		html += "<br><br>";
		html += "<button value=\"Skill Balance\" action=\"bypass _bbs_skillbalance 1 false\" width=90 height=23 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">";
		html += "<button value=\"Oly Skill Balance\" action=\"bypass _bbs_skillbalance 1 true\" width=100 height=23 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">";
		html += "</center></body></html>";
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		
		if (!activeChar.isGM())
		{
			return;
		}
		
		if (command.equals("_bbs_balancer"))
		{
			showMainWindow(activeChar);
		}
		else if (command.startsWith("_bbs_classbalance"))
		{
			String info[] = command.substring(18).split(" ");
			int pageId = 1;
			if (info.length > 1)
			{
				pageId = Integer.parseInt(info[0]);
			}
			boolean isoly = Boolean.parseBoolean(info[1]);
			showMainHtml(activeChar, pageId, isoly);
		}
		else if (command.startsWith("_bbs_save_classbalance"))
		{
			String info[] = command.substring(23).split(" ");
			boolean isoly = Boolean.parseBoolean(info[1]);
			
			ClassBalanceManager.getInstance().rewriteToXml();
			showMainHtml(activeChar, Integer.parseInt(info[0]), isoly);
		}
		else if (command.startsWith("_bbs_remove_classbalance"))
		{
			String info[] = command.substring(25).split(" ");
			String key = info[0];
			int pageId = 1;
			if (info.length > 1)
			{
				pageId = Integer.parseInt(info[1]);
			}
			int type = Integer.valueOf(info[2]);
			boolean isoly = Boolean.parseBoolean(info[3]);
			ClassBalanceManager.getInstance().removeClassBalance(key, AttackType.VALUES[type], isoly);
			showMainHtml(activeChar, pageId, isoly);
		}
		else if (command.startsWith("_bbs_modify_classbalance"))
		{
			String st[] = command.split(";");
			
			int classId = Integer.valueOf(st[0].substring(25));
			int targetClassId = Integer.valueOf(st[1]);
			int attackType = Integer.valueOf(st[2]);
			double value = Double.parseDouble(st[3]);
			int pageId = Integer.parseInt(st[4]);
			boolean isSearch = Boolean.parseBoolean(st[5]);
			boolean isOly = Boolean.parseBoolean(st[6]);
			
			String key = classId + ";" + targetClassId;
			ClassBalanceHolder cbh = ClassBalanceManager.getInstance().getBalanceHolder(key);
			if (isOly)
			{
				cbh.addOlyBalance(AttackType.VALUES[attackType], value);
			}
			else
			{
				cbh.addNormalBalance(AttackType.VALUES[attackType], value);
			}
			ClassBalanceManager.getInstance().addClassBalance(key, cbh, true);
			if (isSearch)
			{
				showSearchHtml(activeChar, pageId, Integer.valueOf(classId), isOly);
			}
			else
			{
				showMainHtml(activeChar, pageId, isOly);
			}
		}
		else if (command.startsWith("_bbs_add_menu_classbalance"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(27), " ");
			int pageId = Integer.parseInt(st.nextToken());
			int race = Integer.parseInt(st.nextToken());
			int tRace = Integer.parseInt(st.nextToken());
			boolean isOly = Boolean.parseBoolean(st.nextToken());
			showAddHtml(activeChar, pageId, race, tRace, isOly);
		}
		else if (command.startsWith("_bbs_add_classbalance"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(22), " ");
			
			String className = st.nextToken().trim();
			String attackTypeSt = st.nextToken();
			String val = st.nextToken();
			String targetClassName = st.nextToken().trim();
			boolean isoly = Boolean.parseBoolean(st.nextToken());
			
			int classId = -1;
			if (!className.equals(""))
			{
				for (PlayerClass cId : PlayerClass.values())
				{
					if (cId.name().equalsIgnoreCase(className))
					{
						classId = cId.ordinal();
					}
				}
			}
			int targetClassId = -1;
			if (!targetClassName.equals(""))
			{
				for (PlayerClass cId : PlayerClass.values())
				{
					if (cId.name().equalsIgnoreCase(targetClassName))
					{
						targetClassId = cId.ordinal();
					}
				}
			}
			double value = Double.parseDouble(val);
			String key = classId + ";" + targetClassId;
			ClassBalanceHolder cbh = null;
			if (ClassBalanceManager.getInstance().getBalanceHolder(key) != null)
			{
				cbh = ClassBalanceManager.getInstance().getBalanceHolder(key);
			}
			else
			{
				cbh = new ClassBalanceHolder(classId, targetClassId);
			}
			if (attackTypeSt.equalsIgnoreCase("PSkillDamage"))
			{
				attackTypeSt = "PhysicalSkillDamage";
			}
			else if (attackTypeSt.equalsIgnoreCase("PSkillCritical"))
			{
				attackTypeSt = "PhysicalSkillCritical";
			}
			if (isoly)
			{
				cbh.addOlyBalance(AttackType.valueOf(attackTypeSt), value);
			}
			else
			{
				cbh.addNormalBalance(AttackType.valueOf(attackTypeSt), value);
			}
			ClassBalanceManager.getInstance().addClassBalance(key, cbh, false);
			showMainHtml(activeChar, 1, isoly);
		}
		else if (command.startsWith("_bbs_search_classbalance"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int classId = -1;
			try
			{
				classId = Integer.valueOf(st.nextToken());
			}
			catch (NumberFormatException ex)
			{
			}
			if (classId == -1)
			{
				return;
			}
			boolean isoly = Boolean.parseBoolean(st.nextToken());
			showSearchHtml(activeChar, 1, classId, isoly);
		}
		else if (command.startsWith("_bbs_search_nav_classbalance"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int classId = -1;
			int pageID = 1;
			try
			{
				classId = Integer.valueOf(st.nextToken());
				pageID = Integer.valueOf(st.nextToken());
			}
			catch (NumberFormatException ex)
			{
			}
			if (classId == -1)
			{
				return;
			}
			boolean isoly = Boolean.parseBoolean(st.nextToken());
			showSearchHtml(activeChar, pageID, classId, isoly);
		}
	}
	
	public void showMainHtml(Player activeChar, int pageId, boolean isolyinfo)
	{
		String html = HtmCache.getInstance().getNotNull("custom/classbalance/index.htm", activeChar);
		String info = getBalanceInfo(ClassBalanceManager.getInstance().getAllBalances().values(), pageId, false, isolyinfo);// "";
		
		int count = ClassBalanceManager.getInstance().getSize(isolyinfo), limitInPage = 7;
		html = html.replace("<?title?>", isolyinfo ? "Olympiad" : "");
		html = html.replace("<?isoly?>", String.valueOf(isolyinfo));
		html = html.replace("%pageID%", String.valueOf(pageId));
		int totalpages = 1, tmpcount = count;
		while ((tmpcount - 7) > 0)
		{
			totalpages++;
			tmpcount -= 7;
		}
		html = html.replace("%totalPages%", String.valueOf(totalpages));
		html = html.replace("%info%", info);
		html = html.replace("%previousPage%", String.valueOf(((pageId - 1) != 0) ? (pageId - 1) : 1));
		html = html.replace("%nextPage%", String.valueOf(((pageId * limitInPage) >= (count)) ? pageId : pageId + 1));
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	public void showAddHtml(Player activeChar, int pageId, int race, int tRace, boolean isoly)
	{
		String html = HtmCache.getInstance().getNotNull("custom/classbalance/" + (isoly ? "olyadd.htm" : "add.htm"), activeChar);
		String classes = "";
		for (PlayerClass _classId : PlayerClass.values())
		{
			if (_classId.getRace() == null)
			{
				continue;
			}
			if (isoly)
			{
				if ((_classId.getLevel().ordinal() == 3) && (_classId.getRace().ordinal() == race)) // (_classId.getLevel() == 4) &&
				{
					classes += _classId.name() + ";";
				}
			}
			else
			{
				if ((_classId.getLevel().ordinal() >= 2) && (_classId.getRace().ordinal() == race)) // (_classId.getLevel() == 4) &&
				{
					classes += _classId.name() + ";";
				}
			}
		}
		
		String tClasses = "";
		if (tRace != 6)
		{
			for (PlayerClass _classId : PlayerClass.values())
			{
				if (_classId.getRace() == null)
				{
					continue;
				}
				if (isoly)
				{
					if ((_classId.getLevel().ordinal() == 3) && (_classId.getRace().ordinal() == tRace)) // (_classId.getLevel() == 4) &&
					{
						tClasses += _classId.name() + ";";
					}
				}
				else
				{
					if ((_classId.getLevel().ordinal() >= 2) && (_classId.getRace().ordinal() == tRace)) // (_classId.getLevel() == 4) &&
					{
						tClasses += _classId.name() + ";";
					}
				}
			}
		}
		else
		{
			tClasses = "Monsters";
		}
		
		html = html.replace("<?pageId?>", String.valueOf(pageId));
		html = html.replace("<?tRace?>", String.valueOf(tRace));
		html = html.replace("<?race0Checked?>", (race == 0 ? "_checked" : ""));
		html = html.replace("<?race1Checked?>", (race == 1 ? "_checked" : ""));
		html = html.replace("<?race2Checked?>", (race == 2 ? "_checked" : ""));
		html = html.replace("<?race3Checked?>", (race == 3 ? "_checked" : ""));
		html = html.replace("<?race4Checked?>", (race == 4 ? "_checked" : ""));
		html = html.replace("<?race5Checked?>", (race == 5 ? "_checked" : ""));
		
		html = html.replace("<?classes?>", classes);
		html = html.replace("<?tClasses?>", tClasses);
		
		html = html.replace("<?race?>", String.valueOf(race));
		
		html = html.replace("<?trace0Checked?>", (tRace == 0 ? "_checked" : ""));
		html = html.replace("<?trace1Checked?>", (tRace == 1 ? "_checked" : ""));
		html = html.replace("<?trace2Checked?>", (tRace == 2 ? "_checked" : ""));
		html = html.replace("<?trace3Checked?>", (tRace == 3 ? "_checked" : ""));
		html = html.replace("<?trace4Checked?>", (tRace == 4 ? "_checked" : ""));
		html = html.replace("<?trace5Checked?>", (tRace == 5 ? "_checked" : ""));
		html = html.replace("<?trace6Checked?>", (tRace == 6 ? "_checked" : ""));
		html = html.replace("<?isoly?>", String.valueOf(isoly));
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	public void showSearchHtml(Player activeChar, int pageId, int sclassId, boolean isolysearch)
	{
		String html = HtmCache.getInstance().getNotNull("custom/classbalance/search.htm", activeChar);
		String info = getBalanceInfo(ClassBalanceManager.getInstance().getClassBalances(sclassId), pageId, true, isolysearch);// "";
		
		int count = ClassBalanceManager.getInstance().getClassBalanceSize(sclassId, isolysearch), limitInPage = 7;
		
		html = html.replace("%pageID%", String.valueOf(pageId));
		int totalpages = 1, tmpcount = count;
		while ((tmpcount - 7) > 0)
		{
			totalpages++;
			tmpcount -= 7;
		}
		html = html.replace("<?title?>", isolysearch ? "Olympiad" : "");
		html = html.replace("<?isoly?>", String.valueOf(isolysearch));
		html = html.replace("%totalPages%", String.valueOf(totalpages));
		html = html.replace("%info%", info);
		html = html.replace("%classID%", String.valueOf(sclassId));
		html = html.replace("%previousPage%", String.valueOf(((pageId - 1) != 0) ? (pageId - 1) : 1));
		html = html.replace("%nextPage%", String.valueOf(((pageId * limitInPage) >= (count)) ? pageId : pageId + 1));
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	private static String getBalanceInfo(Collection<ClassBalanceHolder> collection, int pageId, boolean search, boolean isOly)
	{
		if (collection == null)
		{
			return "";
		}
		String info = "";
		int count = 1, limitInPage = 7;
		for (ClassBalanceHolder balance : collection)
		{
			int classId = balance.getActiveClass();
			int targetClassId = balance.getTargetClass();
			String id = classId + ";" + targetClassId;
			
			if (PlayerClass.getClassById(classId).name().equals("") || PlayerClass.getClassById(targetClassId).name().equals(""))
			{
				if (PlayerClass.getClassById(classId).name().equals("") && (targetClassId != -1))
				{
					continue;
				}
				
			}
			Set<Entry<AttackType, Double>> localCollection = (isOly) ? balance.getOlyBalance().entrySet() : balance.getNormalBalance().entrySet();
			for (Entry<AttackType, Double> dt : localCollection)
			{
				if ((count > ((limitInPage * (pageId - 1)))) && (count <= (limitInPage * pageId)))
				{
					double val = dt.getValue();
					double percents = Math.round(val * 100) - 100;
					double addedValue = (double) Math.round((val + 0.1) * 10) / 10;
					double removedValue = (double) Math.round((val - 0.1) * 10) / 10;
					String attackTypeSt = dt.getKey().name();
					if (attackTypeSt.equalsIgnoreCase("PhysicalSkillDamage"))
					{
						attackTypeSt = "PSkillDamage";
					}
					else if (attackTypeSt.equalsIgnoreCase("PhysicalSkillCritical"))
					{
						attackTypeSt = "PSkillCritical";
					}
					String content = HtmCache.getInstance().getNotNull("custom/classbalance/info-template.htm", Language.ENGLISH);
					content = content.replace("<?pos?>", String.valueOf(count));
					content = content.replace("<?classId?>", String.valueOf(classId));
					content = content.replace("<?className?>", PlayerClass.getClassById(classId).name());
					content = content.replace("<?type?>", attackTypeSt);
					content = content.replace("<?key?>", id);
					content = content.replace("<?targetClassId?>", String.valueOf(targetClassId));
					content = content.replace("<?editedType?>", String.valueOf(dt.getKey().getId()));
					content = content.replace("<?removedValue?>", String.valueOf(removedValue));
					content = content.replace("<?search?>", String.valueOf(search));
					content = content.replace("<?isoly?>", String.valueOf(isOly));
					content = content.replace("<?addedValue?>", String.valueOf(addedValue));
					content = content.replace("<?pageId?>", String.valueOf(pageId));
					content = content.replace("<?targetClassName?>", (targetClassId == -1) ? "Monster" : PlayerClass.getClassById(targetClassId).name());
					content = content.replace("<?value?>", String.valueOf(val));
					content = content.replace("<?percents?>", (percents > 0 ? "+" : ""));
					content = content.replace("<?percentValue?>", String.valueOf(percents).substring(0, String.valueOf(percents).indexOf(".")));
					
					info += content;
				}
				count++;
			}
		}
		
		return info;
	}
	
	public static ClassBalanceGui getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassBalanceGui _instance = new ClassBalanceGui();
	}
	
	protected ClassBalanceGui()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		
		return new String[]
		{
			"_bbs_balancer",
			"_bbs_classbalance",
			"_bbs_add_menu_classbalance",
			"_bbs_add_classbalance",
			"_bbs_save_classbalance",
			"_bbs_remove_classbalance",
			"_bbs_modify_classbalance",
			"_bbs_search_classbalance",
			"_bbs_search_nav_classbalance"
		};
	}
	
	@Override
	public void onWriteCommand(Player arg0, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6)
	{
		// TODO Auto-generated method stub
		
	}
}
