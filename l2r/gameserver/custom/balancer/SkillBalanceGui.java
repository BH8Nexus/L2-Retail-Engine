/**Copyright(C)2004-2016 L2J Server**This file is part of L2J Server.**L2J Server is free software:you can redistribute it and/or modify*it under the terms of the GNU General Public License as published by*the Free Software Foundation,either version 3 of the License,or*(at your option)any later version.**L2J Server is distributed in the hope that it will be useful,*but WITHOUT ANY WARRANTY;without even the implied warranty of*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU*General Public License for more details.**You should have received a copy of the GNU General Public License*along with this program.If not,see<http://www.gnu.org/licenses/>.
*/
package l2r.gameserver.custom.balancer;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.custom.balancer.SkillBalanceHolder.SkillChangeType;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Language;

/***
 * @author DevAtlas
 */
public class SkillBalanceGui implements ICommunityBoardHandler
{
	public static SkillBalanceGui getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected SkillBalanceGui()
	{
	}
	
	private static class SingletonHolder
	{
		protected static final SkillBalanceGui _instance = new SkillBalanceGui();
	}
	
	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		
		if (!activeChar.isGM())
		{
			return;
		}
		
		if (command.startsWith("_bbs_skillbalance"))
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
		else if (command.startsWith("_bbs_save_skillbalance"))
		{
			String info[] = command.substring(23).split(" ");
			boolean isoly = Boolean.parseBoolean(info[1]);
			
			SkillBalanceManager.getInstance().rewriteToXml();
			showMainHtml(activeChar, Integer.parseInt(info[0]), isoly);
		}
		else if (command.startsWith("_bbs_remove_skillbalance"))
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
			SkillBalanceManager.getInstance().removeSkillBalance(key, SkillChangeType.VALUES[type], isoly);
			showMainHtml(activeChar, pageId, isoly);
		}
		else if (command.startsWith("_bbs_modify_skillbalance"))
		{
			String st[] = command.split(";");
			
			int skillId = Integer.valueOf(st[0].substring(25));
			int target = Integer.valueOf(st[1]);
			int changeType = Integer.valueOf(st[2]);
			double value = Double.parseDouble(st[3]);
			int pageId = Integer.parseInt(st[4]);
			boolean isSearch = Boolean.parseBoolean(st[5]);
			boolean isOly = Boolean.parseBoolean(st[6]);
			
			String key = skillId + ";" + target;
			SkillBalanceHolder cbh = SkillBalanceManager.getInstance().getSkillHolder(key);
			if (isOly)
			{
				cbh.addOlySkillBalance(SkillChangeType.VALUES[changeType], value);
			}
			else
			{
				cbh.addSkillBalance(SkillChangeType.VALUES[changeType], value);
			}
			SkillBalanceManager.getInstance().addSkillBalance(key, cbh, true);
			if (isSearch)
			{
				showSearchHtml(activeChar, pageId, skillId, isOly);
			}
			else
			{
				showMainHtml(activeChar, pageId, isOly);
			}
		}
		else if (command.startsWith("_bbs_add_menu_skillbalance"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(27), " ");
			int pageId = Integer.parseInt(st.nextToken());
			int tRace = Integer.parseInt(st.nextToken());
			boolean isOly = Boolean.parseBoolean(st.nextToken());
			showAddHtml(activeChar, pageId, tRace, isOly);
		}
		else if (command.startsWith("_bbs_add_skillbalance"))
		{
			String st[] = command.substring(22).split(";");
			StringTokenizer st2 = new StringTokenizer(command.substring(22), ";");
			if ((st2.countTokens() != 5) || st[0].isEmpty() || st[1].isEmpty() || st[2].isEmpty() || st[3].isEmpty() || st[4].isEmpty())
			{
				activeChar.sendMessage("Incorrect input count.");
				return;
			}
			int skillId = Integer.valueOf(st[0].trim());
			String attackTypeSt = st[1].trim();
			String val = st[2].trim();
			String targetClassName = st[3].trim();
			boolean isoly = Boolean.parseBoolean(st[4].trim());
			
			double value = Double.parseDouble(val);
			if (SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)) == null)
			{
				activeChar.sendMessage("Skill with id: " + skillId + " not found!");
				return;
			}
			int targetClassId = targetClassName.equals("All") ? -2 : -1;
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
			targetClassId = SkillChangeType.valueOf(attackTypeSt).isOnlyVsAll() ? -2 : targetClassId;
			
			String key = skillId + ";" + targetClassId;
			SkillBalanceHolder cbh = null;
			if (SkillBalanceManager.getInstance().getSkillHolder(key) != null)
			{
				cbh = SkillBalanceManager.getInstance().getSkillHolder(key);
			}
			else
			{
				cbh = new SkillBalanceHolder(skillId, targetClassId);
			}
			
			if (isoly)
			{
				cbh.addOlySkillBalance(SkillChangeType.valueOf(attackTypeSt), value);
			}
			else
			{
				cbh.addSkillBalance(SkillChangeType.valueOf(attackTypeSt), value);
			}
			SkillBalanceManager.getInstance().addSkillBalance(key, cbh, isoly);
			showMainHtml(activeChar, 1, isoly);
		}
		else if (command.startsWith("_bbs_search_skillbalance"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int skillId = -1;
			try
			{
				String str = st.nextToken();
				skillId = Integer.valueOf(str);
			}
			catch (NumberFormatException ex)
			{
			}
			if (skillId == -1)
			{
				return;
			}
			boolean isoly = Boolean.parseBoolean(st.nextToken());
			showSearchHtml(activeChar, 1, skillId, isoly);
		}
		else if (command.startsWith("_bbs_search_nav_skillbalance"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int skillId = -1;
			int pageID = 1;
			try
			{
				skillId = Integer.valueOf(st.nextToken());
				pageID = Integer.valueOf(st.nextToken());
			}
			catch (NumberFormatException ex)
			{
			}
			if (skillId == -1)
			{
				return;
			}
			boolean isoly = Boolean.parseBoolean(st.nextToken());
			showSearchHtml(activeChar, pageID, skillId, isoly);
		}
		else if (command.startsWith("_bbs_get_skillbalance"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int skillId = -1;
			try
			{
				skillId = Integer.valueOf(st.nextToken());
			}
			catch (NumberFormatException ex)
			{
			}
			if (skillId == -1)
			{
				return;
			}
			Skill sk = SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId));
			if (sk != null)
			{
				activeChar.addSkill(sk);
				activeChar.sendMessage("You have learned: " + sk.getName());
				// activeChar.sendSkillList();
			}
		}
		
	}
	
	public void showMainHtml(Player activeChar, int pageId, boolean isolyinfo)
	{
		String html = HtmCache.getInstance().getNotNull("custom/skillbalance/index.htm", activeChar);
		String info = getSkillBalanceInfo(SkillBalanceManager.getInstance().getAllBalances().values(), pageId, false, isolyinfo);// "";
		
		int count = SkillBalanceManager.getInstance().getSize(isolyinfo), limitInPage = 6;
		html = html.replace("<?title?>", isolyinfo ? "Olympiad" : "");
		html = html.replace("<?isoly?>", String.valueOf(isolyinfo));
		html = html.replace("%pageID%", String.valueOf(pageId));
		int totalpages = 1, tmpcount = count;
		while ((tmpcount - 6) > 0)
		{
			totalpages++;
			tmpcount -= 6;
		}
		html = html.replace("%totalPages%", String.valueOf(totalpages));
		html = html.replace("%info%", info);
		html = html.replace("%previousPage%", String.valueOf(((pageId - 1) != 0) ? (pageId - 1) : 1));
		html = html.replace("%nextPage%", String.valueOf(((pageId * limitInPage) >= (count)) ? pageId : pageId + 1));
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	public void showSearchHtml(Player activeChar, int pageId, int skillId, boolean isolysearch)
	{
		String html = HtmCache.getInstance().getNotNull("custom/skillbalance/search.htm", activeChar);
		String info = getSkillBalanceInfo(SkillBalanceManager.getInstance().getSkillBalances(skillId), pageId, true, isolysearch);// "";
		
		int count = SkillBalanceManager.getInstance().getSkillBalanceSize(skillId, isolysearch), limitInPage = 6;
		
		html = html.replace("%pageID%", String.valueOf(pageId));
		int totalpages = 1, tmpcount = count;
		while ((tmpcount - 6) > 0)
		{
			totalpages++;
			tmpcount -= 6;
		}
		html = html.replace("<?title?>", isolysearch ? "Olympiad" : "");
		html = html.replace("<?isoly?>", String.valueOf(isolysearch));
		html = html.replace("%totalPages%", String.valueOf(totalpages));
		html = html.replace("%info%", info);
		html = html.replace("%skillId%", String.valueOf(skillId));
		html = html.replace("%previousPage%", String.valueOf(((pageId - 1) != 0) ? (pageId - 1) : 1));
		html = html.replace("%nextPage%", String.valueOf(((pageId * limitInPage) >= (count)) ? pageId : pageId + 1));
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	public void showAddHtml(Player activeChar, int pageId, int tRace, boolean isoly)
	{
		String html = HtmCache.getInstance().getNotNull("custom/skillbalance/" + (isoly ? "olyadd.htm" : "add.htm"), activeChar);
		String tClasses = "";
		if (tRace < 6)
		{
			for (PlayerClass _classId : PlayerClass.values())
			{
				if (_classId.getRace() == null)
				{
					continue;
				}
				if ((_classId.getLevel().ordinal() == 3) && (_classId.getRace().ordinal() == tRace))
				{
					tClasses += _classId.name() + ";";
				}
			}
		}
		else
		{
			tClasses = tRace == 6 ? "Monsters" : "All";
		}
		html = html.replace("<?pageId?>", String.valueOf(pageId));
		html = html.replace("<?isoly?>", String.valueOf(isoly));
		html = html.replace("<?tClasses?>", tClasses);
		
		html = html.replace("<?trace0Checked?>", (tRace == 0 ? "_checked" : ""));
		html = html.replace("<?trace1Checked?>", (tRace == 1 ? "_checked" : ""));
		html = html.replace("<?trace2Checked?>", (tRace == 2 ? "_checked" : ""));
		html = html.replace("<?trace3Checked?>", (tRace == 3 ? "_checked" : ""));
		html = html.replace("<?trace4Checked?>", (tRace == 4 ? "_checked" : ""));
		html = html.replace("<?trace5Checked?>", (tRace == 5 ? "_checked" : ""));
		html = html.replace("<?trace6Checked?>", (tRace == 6 ? "_checked" : ""));
		html = html.replace("<?trace7Checked?>", (tRace == 7 ? "_checked" : ""));
		
		ShowBoard.separateAndSend(html, activeChar);
	}
	
	private static String getSkillBalanceInfo(Collection<SkillBalanceHolder> collection, int pageId, boolean search, boolean isOly)
	{
		if (collection == null)
		{
			return "";
		}
		String info = "";
		int count = 1, limitInPage = 6;
		for (SkillBalanceHolder balance : collection)
		{
			int targetClassId = balance.getTarget();
			
			if (PlayerClass.getClassById(targetClassId).name().equals(""))
			{
				if ((targetClassId > -1))
				{
					continue;
				}
				
			}
			Set<Entry<SkillChangeType, Double>> localCollection = (isOly) ? balance.getOlyBalance().entrySet() : balance.getNormalBalance().entrySet();
			for (Entry<SkillChangeType, Double> dt : localCollection)
			{
				if ((count > ((limitInPage * (pageId - 1)))) && (count <= (limitInPage * pageId)))
				{
					double val = dt.getValue();
					double percents = Math.round(val * 100) - 100;
					double addedValue = (double) Math.round((val + 0.1) * 10) / 10;
					double removedValue = (double) Math.round((val - 0.1) * 10) / 10;
					
					String content = HtmCache.getInstance().getNotNull("custom/skillbalance/info-template.htm", Language.ENGLISH);
					content = content.replace("<?pos?>", String.valueOf(count));
					content = content.replace("<?key?>", balance.getSkillId() + ";" + balance.getTarget());
					content = content.replace("<?skillId?>", String.valueOf(balance.getSkillId()));
					content = content.replace("<?skillName?>", SkillTable.getInstance().getInfo(balance.getSkillId(), SkillTable.getInstance().getMaxLevel(balance.getSkillId())).getName());
					content = content.replace("<?type?>", dt.getKey().name());
					content = content.replace("<?editedType?>", String.valueOf(dt.getKey().getId()));
					content = content.replace("<?removedValue?>", String.valueOf(removedValue));
					content = content.replace("<?search?>", String.valueOf(search));
					content = content.replace("<?isoly?>", String.valueOf(isOly));
					content = content.replace("<?addedValue?>", String.valueOf(addedValue));
					content = content.replace("<?pageId?>", String.valueOf(pageId));
					content = content.replace("<?value?>", String.valueOf(val));
					content = content.replace("<?targetClassName?>", (targetClassId <= -1) ? ((targetClassId == -1) ? "Monster" : "All") : PlayerClass.getClassById(targetClassId).name());
					content = content.replace("<?percents?>", (percents > 0 ? "+" : ""));
					content = content.replace("<?percentValue?>", String.valueOf(percents).substring(0, String.valueOf(percents).indexOf(".")));
					content = content.replace("<?targetId?>", String.valueOf(targetClassId));
					content = content.replace("<?skillIcon?>", SkillTable.getInstance().getInfo(balance.getSkillId(), SkillTable.getInstance().getMaxLevel(balance.getSkillId())).getIcon());
					info += content;
				}
				count++;
			}
		}
		
		return info;
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_bbs_skillbalance",
			"_bbs_add_menu_skillbalance",
			"_bbs_add_skillbalance",
			"_bbs_save_skillbalance",
			"_bbs_remove_skillbalance",
			"_bbs_modify_skillbalance",
			"_bbs_search_skillbalance",
			"_bbs_search_nav_skillbalance",
			"_bbs_get_skillbalance"
		};
	}
	
	@Override
	public void onWriteCommand(Player arg0, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6)
	{
		// TODO Auto-generated method stub
		
	}
	
}
