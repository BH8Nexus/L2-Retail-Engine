package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
//Test code
import java.util.function.Function;
//Test code 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.cache.ImagesCache;
import l2r.gameserver.data.htm.bypasshandler.BypassType;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.scripts.Scripts.ScriptClassAndMethod;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.StringUtil;
import l2r.gameserver.utils.Strings;
import l2r.gameserver.utils.Util;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes VOLUMN UNKNOWN UL U TT TR TITLE TEXTCODE TEXTAREA TD TABLE SUP SUB STRIKE SPIN SELECT RIGHT PRE P OPTION OL MULTIEDIT LI LEFT INPUT IMG I HTML H7 H6 H5 H4 H3 H2 H1 FONT EXTEND EDIT COMMENT COMBOBOX CENTER
 * BUTTON BR BODY BAR ADDRESS A SEL LIST VAR FORE READONL ROWS VALIGN FIXWIDTH BORDERCOLORLI BORDERCOLORDA BORDERCOLOR BORDER BGCOLOR BACKGROUND ALIGN VALU READONLY MULTIPLE SELECTED TYP TYPE MAXLENGTH CHECKED SRC Y X QUERYDELAY NOSCROLLBAR IMGSRC B FG SIZE FACE COLOR DEFFON DEFFIXEDFONT WIDTH VALUE
 * TOOLTIP NAME MIN MAX HEIGHT DISABLED ALIGN MSG LINK HREF ACTION ClassId fstring
 */
public class NpcHtmlMessage extends L2GameServerPacket
{
	protected static final Logger _log = LoggerFactory.getLogger(NpcHtmlMessage.class);
	protected static final Pattern objectId = Pattern.compile("%objectId%");
	protected static final Pattern playername = Pattern.compile("%playername%");
	
	protected int _npcObjId;
	protected String _html;
	protected String _file = null;
	protected List<String> _replaces = new ArrayList<>();
	protected boolean have_appends = false;
	
	public NpcHtmlMessage(Player player, int npcId, String filename, int val)
	{
		List<ScriptClassAndMethod> appends = Scripts.dialogAppends.get(npcId);
		if ((appends != null) && (appends.size() > 0))
		{
			have_appends = true;
			if ((filename != null) && filename.equalsIgnoreCase("npcdefault.htm"))
			{
				setHtml(""); // ΠΊΠΎΠ½Ρ‚ΠµΠ½Ρ‚ Π·Π°Π΄Π°ΠµΡ‚Ρ�Ρ� Ρ�ΠΊΡ€ΠΈΠΏΡ‚Π°ΠΌΠΈ Ρ‡ΠµΡ€ΠµΠ· DialogAppend_
			}
			else
			{
				setFile(filename);
			}
			
			String replaces = "";
			
			// Π”ΠΎΠ±Π°Π²ΠΈΡ‚Ρ� Π² ΠΊΠΎΠ½ΠµΡ† Ρ�Ρ‚Ρ€Π°Π½ΠΈΡ‡ΠΊΠΈ Ρ‚ΠµΠΊΡ�Ρ‚, ΠΎΠΏΡ€ΠµΠ΄ΠµΠ»ΠµΠ½Π½Ρ‹ΠΉ Π² Ρ�ΠΊΡ€ΠΈΠΏΡ‚Π°Ρ….
			Object[] script_args = new Object[]
			{
				new Integer(val)
			};
			for (ScriptClassAndMethod append : appends)
			{
				Object obj = Scripts.getInstance().callScripts(player, append.className, append.methodName, script_args);
				if (obj != null)
				{
					replaces += obj;
				}
			}
			
			if (!replaces.equals(""))
			{
				replace("</body>", "\n" + Strings.bbParse(replaces) + "</body>");
			}
		}
		else
		{
			setFile(filename);
		}
	}
	
	public NpcHtmlMessage(Player player, NpcInstance npc, String filename, int val)
	{
		this(player, npc.getNpcId(), filename, val);
		
		_npcObjId = npc.getObjectId();
		
		player.setLastNpc(npc);
		
		replace("%npcId%", String.valueOf(npc.getNpcId()));
		replace("%npcname%", npc.getName());
		replace("%nick%", player.getName());
		replace("%class%", player.getClassId().getLevel());
		replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
		// replace("%serverName%", Config.SERVER_NAME);
	}
	
	public NpcHtmlMessage(Player player, NpcInstance npc)
	{
		if (npc == null)
		{
			_npcObjId = 5;
			player.setLastNpc(null);
		}
		else
		{
			_npcObjId = npc.getObjectId();
			player.setLastNpc(npc);
		}
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	public final NpcHtmlMessage setHtml(String text)
	{
		if (!text.contains("<html>"))
		{
			text = "<html><body>" + text + "</body></html>"; // <title>Message:</title> <br><br><br>
		}
		_html = text;
		return this;
	}
	
	public final NpcHtmlMessage setFile(String file)
	{
		_file = file;
		if (_file.startsWith("data/html/"))
		{
			_log.info("NpcHtmlMessage: need fix : " + file, new Exception());
			_file = _file.replace("data/html/", "");
		}
		return this;
	}
	
	public NpcHtmlMessage replace(String pattern, String value)
	{
		if ((pattern == null) || (value == null))
		{
			return this;
		}
		_replaces.add(pattern);
		_replaces.add(value);
		return this;
	}
	
	public NpcHtmlMessage replace(String pattern, Object value)
	{
		return replace(pattern, value.toString());
	}
	
	public NpcHtmlMessage replaceNpcString(String pattern, NpcString npcString, Object... arg)
	{
		if (pattern == null)
		{
			return this;
		}
		if (npcString.getSize() != arg.length)
		{
			throw new IllegalArgumentException("Not valid size of parameters: " + npcString);
		}
		
		_replaces.add(pattern);
		_replaces.add(HtmlUtils.htmlNpcString(npcString, arg));
		return this;
	}
	
	@Override
	protected void writeImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_file != null)
		{
			Functions.sendDebugMessage(player, "HTML: " + _file);
			String content = HtmCache.getInstance().getNotNull(_file, player);
			String content2 = HtmCache.getInstance().getNullable(_file, player);
			if (content2 == null)
			{
				setHtml(have_appends && _file.endsWith(".htm") ? "" : content);
			}
			else
			{
				setHtml(content);
			}
		}
		
		if (_html == null)
		{
			return;
		}
		
		for (int i = 0; i < _replaces.size(); i += 2)
		{
			_html = _html.replace(_replaces.get(i), _replaces.get(i + 1));
		}
		
		Matcher m = objectId.matcher(_html);
		if (m != null)
		{
			_html = m.replaceAll(String.valueOf(_npcObjId));
		}
		
		_html = playername.matcher(_html).replaceAll(player.getName());
		
		// Synerge - Replace and send all images and crests of this html
		_html = ImagesCache.getInstance().sendUsedImages(_html, player);
		if (_html.startsWith("CREST"))
		{
			_html = _html.substring(5);
		}
		
		player.cleanBypasses(BypassType.NPC);
		_html = player.encodeBypasses(_html, BypassType.NPC);
		
		writeC(0x19);
		writeD(_npcObjId);
		writeS(_html);
		writeD(0x00);
	}
	
	public String getText()
	{
		return _html;
	}
	
	public static String title(String title, String subTitle)
	{
		return "<html><title>" + title + "</title><body><center><br>" + "<b><font color=ffcc00>" + subTitle + "</font></b>" + "<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
	}
	
	public static String title2(String title, String subTitle)
	{
		return "<html><title>" + title + "</title><body><center><br>";
	}
	
	public static String footer(String name, String version)
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br></center></body></html>";
	}
	
	public static String footer(String footer)
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br><font color=\"303030\">" + footer + "</font></center></body></html>";
	}
	
	public static String footer()
	{
		return "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br></center></body></html>";
	}
	
	/**
	 * @param type : 0 - Normal, 1 - Yellow, 2 - Blue, 3 - Green, 4 - Red, 5 - Purple, 6 - Grey
	 * @param revert : revert ? fore : back
	 * @return return "[button value=value action="bypass -h Quest questName questEvent" width=width height=height back=type fore=type]";
	 */
	public static String questButton(String questName, String value, String questEvent, int width, int height, int type, boolean revert)
	{
		String back = getButtonType(type, !revert);
		String fore = getButtonType(type, revert);
		return "<button value=\"" + value + "\" action=\"bypass -h Quest " + questName + " " + questEvent + "\" " + "width=\"" + Integer.toString(width) + "\" height=\"" + Integer.toString(height) + "\" " + "back=\"" + back + "\" fore=\"" + fore + "\">";
	}
	
	/**
	 * @param type : 0 - Normal, 1 - Yellow, 2 - Blue, 3 - Green, 4 - Red, 5 - Purple, 6 - Grey
	 * @param revert : revert ? fore : back
	 * @return return "[button value=value action="bypass -h Quest questName questEvent" width=width height=height back=type fore=type]";
	 */
	public static String bypassButton(String bypass, String displayName, int width, int height, int type, boolean revert)
	{
		String back = getButtonType(type, !revert);
		String fore = getButtonType(type, revert);
		return "<button value=\"" + displayName + "\" action=\"bypass -h " + bypass + "\" " + "width=\"" + Integer.toString(width) + "\" height=\"" + Integer.toString(height) + "\" " + "back=\"" + back + "\" fore=\"" + fore + "\">";
	}
	
	private static String getButtonType(int type, boolean back)
	{
		switch (type)
		{
			case 0: // Normal
				if (back)
				{
					return "L2UI_ct1.button_df";
				}
				return "L2UI_ct1.button_df";
			case 1: // Yellow
				if (back)
				{
					return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_earth";
				}
				return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_earth_bg";
			case 2: // Blue
				if (back)
				{
					return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_Water";
				}
				return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_Water_bg";
			case 3: // Green
				if (back)
				{
					return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_wind";
				}
				return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_wind_bg";
			case 4: // Red
				if (back)
				{
					return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_fire";
				}
				return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_fire_bg";
			case 5: // Purple
				if (back)
				{
					return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_dark";
				}
				return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_dark_bg";
			case 6: // Grey
				if (back)
				{
					return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_divine";
				}
				return "L2UI_CT1.Button_DF.Gauge_DF_Attribute_divine_bg";
			default:
				if (back)
				{
					return "L2UI_ct1.button_df";
				}
				return "L2UI_ct1.button_df";
		}
	}
	
	/**
	 * @return [a action="bypass -h Quest questName event"][font color="color">value[/font][/a]
	 */
	public static String questLink(String questName, String value, String event, String color)
	{
		return "<a action=\"bypass -h Quest " + questName + " " + event + "\">" + "<font color=\"" + color + "\">" + value + "</font></a>";
	}
	
	/**
	 * @return [a action="bypass -h bypassValue"][font color="color">displayName[/font][/a]
	 */
	public static String bypass(String bypassValue, String displayName, String color)
	{
		return "<a action=\"bypass -h " + bypassValue + "\">" + "<font color=\"" + color + "\">" + displayName + "</font></a>";
	}
	
	/**
	 * @return [a action="bypass bypassValue"][font color="color">displayName[/font][/a]
	 */
	public static String bypassNoH(String bypass, String displayName, String color)
	{
		return "<a action=\"bypass " + bypass + "\">" + "<font color=\"" + color + "\">" + displayName + "</font></a>";
	}
	
	/**
	 * @return [table width="260" align="center"][tr][td width="260" align="center"] title [/td][/tr][/table][br]
	 */
	public static String topic(String title)
	{
		return "<table width=\"260\" align=\"center\"><tr><td width=\"260\" align=\"center\"> " + title + " </td></tr></table><br>";
	}
	
	/**
	 * ======== L2JServer code ========
	 */
	
	/*
	 * Copyright (C) 2004-2014 L2J Server This file is part of L2J Server. L2J Server is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
	 * later version. L2J Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
	 * General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
	 */
	
	/**
	 * @author UnAfraid
	 */
	public static class PageResult
	{
		private final int _pages;
		private final StringBuilder _pagerTemplate;
		private final StringBuilder _bodyTemplate;
		
		public PageResult(int pages, StringBuilder pagerTemplate, StringBuilder bodyTemplate)
		{
			_pages = pages;
			_pagerTemplate = pagerTemplate;
			_bodyTemplate = bodyTemplate;
		}
		
		public int getPages()
		{
			return _pages;
		}
		
		public StringBuilder getPagerTemplate()
		{
			return _pagerTemplate;
		}
		
		public StringBuilder getBodyTemplate()
		{
			return _bodyTemplate;
		}
	}
	
	/**
	 * A class containing useful methods for constructing HTML
	 * @author NosBit
	 */
	
	/**
	 * Gets the HTML representation of CP gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getCpGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_CP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_CP_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of HP gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getHpGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_HP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HP_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of HP Warn gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getHpWarnGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_HPWarn_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HPWarn_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of HP Fill gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getHpFillGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_HPFill_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HPFill_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of MP Warn gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getMpGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_MP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_MP_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of EXP Warn gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getExpGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_EXP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_EXP_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of Food gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getFoodGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_Food_Bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_Food_Center", 17, -13);
	}
	
	/**
	 * Gets the HTML representation of Weight gauge automatically changing level depending on current/max.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @return the HTML
	 */
	public static String getWeightGauge(int width, long current, long max, boolean displayAsPercentage)
	{
		return getWeightGauge(width, current, max, displayAsPercentage, Util.map(current, 0, max, 1, 5));
	}
	
	/**
	 * Gets the HTML representation of Weight gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @param level a number from 1 to 5 for the 5 different colors of weight gauge
	 * @return the HTML
	 */
	public static String getWeightGauge(int width, long current, long max, boolean displayAsPercentage, long level)
	{
		return getGauge(width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_Weight_bg_Center" + level, "L2UI_CT1.Gauges.Gauge_DF_Large_Weight_Center" + level, 17, -13);
	}
	
	/**
	 * Gets the HTML representation of a gauge.
	 * @param width the width
	 * @param current the current value
	 * @param max the max value
	 * @param displayAsPercentage if {@code true} the text in middle will be displayed as percent else it will be displayed as "current / max"
	 * @param backgroundImage the background image
	 * @param image the foreground image
	 * @param imageHeight the image height
	 * @param top the top adjustment
	 * @return the HTML
	 */
	private static String getGauge(int width, long current, long max, boolean displayAsPercentage, String backgroundImage, String image, long imageHeight, long top)
	{
		long currentDisplay = current;
		current = Math.min(current, max);
		final StringBuilder sb = new StringBuilder();
		StringUtil.append(sb, "<table width=", String.valueOf(width), " cellpadding=0 cellspacing=0><tr><td background=\"" + backgroundImage + "\">");
		StringUtil.append(sb, "<img src=\"" + image + "\" width=", String.valueOf((long) (((double) current / max) * width)), " height=", String.valueOf(imageHeight), ">");
		StringUtil.append(sb, "</td></tr><tr><td align=center><table cellpadding=0 cellspacing=", String.valueOf(top), "><tr><td>");
		if (displayAsPercentage)
		{
			StringUtil.append(sb, "<table cellpadding=0 cellspacing=2><tr><td>", String.format("%.2f%%", ((double) current / max) * 100), "</td></tr></table>");
		}
		else
		{
			final String tdWidth = String.valueOf((width - 10) / 2);
			StringUtil.append(sb, "<table cellpadding=0 cellspacing=0><tr><td width=" + tdWidth + " align=right>", String.valueOf(currentDisplay), "</td>");
			StringUtil.append(sb, "<td width=10 align=center>/</td><td width=" + tdWidth + ">", String.valueOf(max), "</td></tr></table>");
		}
		StringUtil.append(sb, "</td></tr></table></td></tr></table>");
		return sb.toString();
	}
	
	public static <T> PageResult createPage(Collection<T> elements, int page, int elementsPerPage, Function<Integer, String> pagerFunction, Function<T, String> bodyFunction)
	{
		return createPage(elements, elements.size(), page, elementsPerPage, pagerFunction, bodyFunction);
	}
	
	public static <T> PageResult createPage(T[] elements, int page, int elementsPerPage, Function<Integer, String> pagerFunction, Function<T, String> bodyFunction)
	{
		return createPage(Arrays.asList(elements), elements.length, page, elementsPerPage, pagerFunction, bodyFunction);
	}
	
	public static <T> PageResult createPage(Iterable<T> elements, int size, int page, int elementsPerPage, Function<Integer, String> pagerFunction, Function<T, String> bodyFunction)
	{
		int pages = size / elementsPerPage;
		if ((elementsPerPage * pages) < size)
		{
			pages++;
		}
		
		final StringBuilder pagerTemplate = new StringBuilder();
		if (pages > 1)
		{
			int breakit = 0;
			for (int i = 0; i < pages; i++)
			{
				pagerTemplate.append(pagerFunction.apply(i));
				breakit++;
				
				if (breakit > 5)
				{
					pagerTemplate.append("</tr><tr>");
					breakit = 0;
				}
			}
		}
		
		if (page >= pages)
		{
			page = pages - 1;
		}
		
		int start = 0;
		if (page > 0)
		{
			start = elementsPerPage * page;
		}
		
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (T element : elements)
		{
			if (i++ < start)
			{
				continue;
			}
			
			sb.append(bodyFunction.apply(element));
			
			if (i >= (elementsPerPage + start))
			{
				break;
			}
		}
		return new PageResult(pages, pagerTemplate, sb);
	}
}