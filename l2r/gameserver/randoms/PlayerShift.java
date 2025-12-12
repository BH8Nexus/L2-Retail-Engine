/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.randoms;

import java.util.List;
import java.util.Map.Entry;

import l2r.gameserver.achievements.Achievements;
import l2r.gameserver.achievements.iAchievement;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;

public class PlayerShift
{
	public static void getAchivmentLevels(Player target, Player requester)
	{
		if ((target == null) || (requester == null))
		{
			return;
		}
		
		String achPage = HtmCache.getInstance().getNotNull("actions/statsAch.htm", target);
		String full = "";
		
		int i = 0;
		
		for (Entry<Integer, Integer> ach : target.getAchievements().entrySet())
		{
			String achOne = HtmCache.getInstance().getNotNull("actions/statsAchOne.htm", target);
			iAchievement a = Achievements.getInstance().getAchievement(ach.getKey(), ach.getValue()); // TODO Test
			if (a == null)
			{
				continue;
			}
			
			i++;
			achOne = achOne.replaceAll("%bg%", (i % 2) == 0 ? "090908" : "0f100f");
			achOne = achOne.replaceAll("%name%", a.getName());
			achOne = achOne.replaceAll("%level%", "" + a.getLevel());
			full += achOne;
		}
		
		Functions.show(achPage.replaceFirst("%achievements%", full).replaceAll("%tgName%", target.getName()), requester, null);
	}
	
	public static void see(Player target, Player requester, String what)
	{
		if ((requester == null) || (target == null))
		{
			return;
		}
		
		if (what.equals("patak"))
		{
			warn(requester, target.getName() + " P.Attack is '" + target.getPAtk(null) + "' your is '" + requester.getPAtk(null) + "'.");
		}
		else if (what.equals("matak"))
		{
			warn(requester, target.getName() + " M.Attack is '" + target.getMAtk(null, null) + "' your is '" + requester.getMAtk(null, null) + "'.");
		}
		else if (what.equals("pdef"))
		{
			warn(requester, target.getName() + " P.Defence is '" + target.getPDef(null) + "' your is '" + requester.getPDef(null) + "'.");
		}
		else if (what.equals("mdef"))
		{
			warn(requester, target.getName() + " M.Defence is '" + target.getMDef(null, null) + "' your is '" + requester.getMDef(null, null) + "'.");
		}
		else if (what.equals("accuracy"))
		{
			warn(requester, target.getName() + " Accuracy is '" + target.getAccuracy() + "' your is '" + requester.getAccuracy() + "'.");
		}
		else if (what.equals("evasion"))
		{
			warn(requester, target.getName() + " Evasion is '" + target.getEvasionRate(null) + "' your is '" + requester.getEvasionRate(null) + "'.");
		}
		else if (what.equals("criticalHit"))
		{
			warn(requester, target.getName() + " Critical Rate is '" + target.getCriticalHit(null, null) + "' your is '" + requester.getCriticalHit(null, null) + "'.");
		}
		else if (what.equals("runSpeed"))
		{
			warn(requester, target.getName() + " Run Speed is '" + target.getRunSpeed() + "' your is '" + requester.getRunSpeed() + "'.");
		}
		else if (what.equals("attackSpeed"))
		{
			warn(requester, target.getName() + " Attack Speed is '" + target.getPAtkSpd() + "' your is '" + requester.getPAtkSpd() + "'.");
		}
		else if (what.equals("castSpeed"))
		{
			warn(requester, target.getName() + " Cast Speed is '" + target.getMAtkSpd() + "' your is '" + requester.getMAtkSpd() + "'.");
		}
	}
	
	public static void effects(Player player, Player target)
	{
		if ((player == null) || (target == null))
		{
			return;
		}
		
		StringBuilder append_message = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		append_message.append(target.getName()).append("<br></font></center><br>");
		
		List<Effect> list = target.getEffectList().getAllEffects();
		if ((list != null) && !list.isEmpty())
		{
			for (Effect e : list)
			{
				append_message.append(e.getSkill().getName()).append("<br1>");
			}
		}
		
		append_message.append("<br><center><button value=\"");
		append_message.append("" + new CustomMessage("l2r.gameserver.randoms.PlayerShift.Refresh", player));
		append_message.append("\" action=\"bypass _bbs_get_effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
		
		Functions.show(append_message.toString(), player, null);
	}
	
	private static void warn(Player player, String text)
	{
		player.sendPacket(new ConfirmDlg(SystemMsg.S1, 8000, 3).addString(text));
		return;
	}
}