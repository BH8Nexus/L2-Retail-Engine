package l2r.gameserver.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import l2r.commons.annotations.Nullable;
import l2r.commons.math.TIntStringHashMap;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.custom.dailyquests.drops.Droplist;
import l2r.gameserver.custom.dailyquests.drops.DroplistGroup;
import l2r.gameserver.custom.dailyquests.drops.DroplistItem;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.entity.events.impl.AbstractFightClub;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.reward.RewardList;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.stats.Env;
import l2r.gameserver.templates.item.ItemTemplate;

public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;
	
	/**
	 * Π¤ΠΎΡ€ΠΌΠ°Ρ‚Ρ‚ΠµΡ€ Π΄Π»Ρ� Π°Π΄ΠµΠ½Ρ‹.<br>
	 * Locale.KOREA Π·Π°Ρ�Ρ‚Π°Π²Π»Ρ�ΠµΡ‚ ΠµΠ³ΠΎ Ρ„ΠΎΡ€Ρ‚ΠΌΠ°Ρ‚ΠΈΡ€ΠΎΠ²Π°Ρ‚Ρ� Ρ‡ΠµΡ€ΠµΠ· ",".<br>
	 * Locale.FRANCE Ρ„ΠΎΡ€ΠΌΠ°Ρ‚ΠΈΡ€ΡƒΠµΡ‚ Ρ‡ΠµΡ€ΠµΠ· " "<br>
	 * Π”Π»Ρ� Ρ„ΠΎΡ€ΠΌΠ°Ρ‚ΠΈΡ€ΠΎΠ²Π°Π½ΠΈΡ� Ρ‡ΠµΡ€ΠµΠ· "." ΡƒΠ±Ρ€Π°Ρ‚Ρ� Ρ� Π°Ρ€Π³ΡƒΠΌΠµΠ½Ρ‚ΠΎΠ² Locale.FRANCE
	 */
	private static NumberFormat adenaFormatter;
	
	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}
	
	/**
	 * Used in Enum to Integer conversion to support multiple enum variables.
	 * @param addTo the integer which the enum has to be added to.
	 * @return integer containing the enum.
	 */
	public static int add(int addTo, Enum<?> val)
	{
		return addTo | (1 << val.ordinal());
	}
	
	public static double cutOff(double num, int pow)
	{
		return (int) (num * Math.pow(10, pow)) / Math.pow(10, pow);
	}
	
	/**
	 * Used in Enum to Integer conversion to support multiple enum variables.
	 * @param removeFrom the integer which the enum has to be removed from.
	 * @return integer excluding the enum.
	 */
	public static int remove(int removeFrom, Enum<?> val)
	{
		return removeFrom & ~(1 << val.ordinal());
	}
	
	/**
	 * Checks if the given enum is inside the multienum integer.
	 * @param in the multienum integer.
	 * @return true if the enum is inside the multienum integer.
	 */
	public static boolean contains(int in, Enum<?> val)
	{
		return (in & (1 << val.ordinal())) != 0;
	}
	
	/**
	 * Π�Ρ€ΠΎΠ²ΠµΡ€Ρ�ΠµΡ‚ Ρ�Ρ‚Ρ€ΠΎΠΊΡƒ Π½Π° Ρ�ΠΎΠΎΡ‚Π²ΠµΡ‚Ρ�Π²ΠΈΠµ Ρ€ΠµΠ³ΡƒΠ»Ρ�Ρ€Π½ΠΎΠΌΡƒ Π²Ρ‹Ρ€Π°Π¶ΠµΠ½ΠΈΡ�
	 * @param text Π΅Ρ‚Ρ€ΠΎΠΊΠ°-ΠΈΡ�Ρ‚ΠΎΡ‡Π½ΠΈΠΊ
	 * @param template Π¨Π°Π±Π»ΠΎΠ½ Π΄Π»Ρ� ΠΏΠΎΠΈΡ�ΠΊΠ°
	 * @return true Π² Ρ�Π»ΡƒΡ‡Π°Πµ Ρ�ΠΎΠΎΡ‚Π²ΠµΡ‚Π²ΠΈΡ� Ρ�Ρ‚Ρ€ΠΎΠΊΠΈ Ρ�Π°Π±Π»ΠΎΠ½Ρƒ
	 */
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch (PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if (pattern == null)
		{
			return false;
		}
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}
	
	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if (Double.isNaN(x))
		{
			return nanString;
		}
		if (forceExponents)
		{
			return df.format(x);
		}
		if ((long) x == x)
		{
			return String.valueOf((long) x);
		}
		return String.valueOf(x);
	}
	
	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}
	
	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(int amount)
	{
		return adenaFormatter.format(amount);
	}
	
	public static String testAdena(int amount, int item)
	{
		return adenaFormatter.format(amount) + "" + ItemHolder.getInstance().getTemplate(item).getName();
	}
	
	/**
	 * @param time : time in <b>seconds</b>
	 * @param offset : the offset number. If the parsed time is 4d 13h 33m 12s with offset of 2, it will return only 4d and 14h. If set to <= 0, it will be disabled.
	 * @return a string representation of the given time on the bigges possible scale (s, m, h, d)
	 */
	public static String formatTime(int time, int offset)
	{
		if (time == 0)
		{
			return "now";
		}
		
		if (time == -1)
		{
			return "time ended";
		}
		
		time = Math.abs(time);
		String ret = "";
		
		long numMonths = time / 2592000;
		time -= numMonths * 2592000;
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		
		if (offset > 0)
		{
			if (numMonths > 0)
			{
				offset--;
			}
			if (numDays > 0)
			{
				if (offset > 0)
				{
					offset--;
				}
				else
				{
					// Round the months if there is no more offset.
					if ((numDays >= 15) && (offset == 0))
					{
						numMonths++;
					}
					numDays = 0;
				}
			}
			if (numHours > 0)
			{
				if (offset > 0)
				{
					offset--;
				}
				else
				{
					// Round the days if there is no more offset.
					if ((numHours >= 12) && (offset == 0))
					{
						numDays++;
					}
					numHours = 0;
				}
			}
			if (numMins > 0)
			{
				if (offset > 0)
				{
					offset--;
				}
				else
				{
					// Round the hours if there is no more offset.
					if ((numMins >= 30) && (offset == 0))
					{
						numHours++;
					}
					numMins = 0;
				}
			}
			if (numSeconds > 0)
			{
				if (offset > 0)
				{
					offset--;
				}
				else
				{
					// Round the minutes if there is no more offset.
					if ((numSeconds >= 30) && (offset == 0))
					{
						numMins++;
					}
					numSeconds = 0;
				}
			}
		}
		
		if (numMonths > 0)
		{
			ret += numMonths + "M ";
		}
		if (numDays > 0)
		{
			ret += Math.min(numDays, 30) + "d ";
		}
		if (numHours > 0)
		{
			ret += Math.min(numHours, 23) + "h ";
		}
		if (numMins > 0)
		{
			ret += Math.min(numMins, 59) + "m ";
		}
		if (numSeconds > 0)
		{
			ret += Math.min(numSeconds, 59) + "s";
		}
		
		return ret.trim();
	}
	
	/**
	 * @param time : time in <b>seconds</b>
	 * @return a string representation of the given time on the bigges possible scale (s, m, h, d)
	 */
	public static String formatTime(int time)
	{
		return formatTime(time, -1);
	}
	
	/**
	 * Π�Π½Ρ�Ρ‚Ρ€ΡƒΠΌΠµΠ½Ρ‚ Π΄Π»Ρ� ΠΏΠΎΠ΄Ρ�Ρ‡ΠµΡ‚Π° Π²Ρ‹ΠΏΠ°Π²Ρ�ΠΈΡ… Π²ΠµΡ‰ΠµΠΉ Ρ� ΡƒΡ‡ΠµΡ‚ΠΎΠΌ Ρ€ΠµΠΉΡ‚ΠΎΠ². Π’ΠΎΠ·Π²Ρ€Π°Ρ‰Π°ΠµΡ‚ 0 ΠµΡ�Π»ΠΈ Ρ�Π°Π½Ρ� Π½Πµ ΠΏΡ€ΠΎΡ�ΠµΠ», Π»ΠΈΠ±ΠΎ ΠΊΠΎΠ»ΠΈΡ‡ΠµΡ�Ρ‚Π²ΠΎ ΠµΡ�Π»ΠΈ ΠΏΡ€ΠΎΡ�ΠµΠ». Π�ΠΎΡ€Ρ€ΠµΠΊΡ‚Π½ΠΎ ΠΎΠ±Ρ€Π°Π±Π°Ρ‚Ρ‹Π²Π°ΠµΡ‚ Ρ�Π°Π½Ρ�Ρ‹ ΠΏΡ€ΠµΠ²Ρ‹Ρ�Π°Ρ�Ρ‰ΠΈΠµ
	 * 100%. Π¨Π°Π½Ρ� Π² 1:1000000 (L2Drop.MAX_CHANCE)
	 */
	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0))
		{
			return 0;
		}
		int dropmult = 1;
		if (rate)
		{
			calcChance *= Config.RATE_DROP_ITEMS;
		}
		if (calcChance > RewardList.MAX_CHANCE)
		{
			if ((calcChance % RewardList.MAX_CHANCE) == 0)
			{
				dropmult = (int) (calcChance / RewardList.MAX_CHANCE);
			}
			else
			{
				dropmult = (int) Math.ceil(calcChance / RewardList.MAX_CHANCE);
				calcChance = calcChance / dropmult;
			}
		}
		return Rnd.chance(calcChance / 10000.) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}
	
	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if (a.length > m)
		{
			throw new Exception("Overflow");
		}
		
		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for (int i = 0; i < m; i++)
		{
			result <<= bits;
			if (a.length > i)
			{
				next = a[i];
				if ((next >= mval) || (next < 0))
				{
					throw new Exception("Overload, value is out of range");
				}
			}
			else
			{
				next = 0;
			}
			result += next;
		}
		return result;
	}
	
	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if (a.length > m)
		{
			throw new Exception("Overflow");
		}
		
		long result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for (int i = 0; i < m; i++)
		{
			result <<= bits;
			if (a.length > i)
			{
				next = a[i];
				if ((next >= mval) || (next < 0))
				{
					throw new Exception("Overload, value is out of range");
				}
			}
			else
			{
				next = 0;
			}
			result += next;
		}
		return result;
	}
	
	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for (int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = next - (a * mval);
		}
		return result;
	}
	
	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for (int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
			result[i - 1] = (int) (next - (a * mval));
		}
		return result;
	}
	
	public static float[] parseCommaSeparatedFloatArray(String s)
	{
		if (s.isEmpty())
		{
			return new float[0];
		}
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		float[] val = new float[tmp.length];
		for (int i = 0; i < tmp.length; i++)
		{
			val[i] = Float.parseFloat(tmp[i]);
		}
		return val;
	}
	
	public static int[] parseCommaSeparatedIntegerArray(String s)
	{
		if (s.isEmpty())
		{
			return new int[0];
		}
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		int[] val = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++)
		{
			val[i] = Integer.parseInt(tmp[i]);
		}
		return val;
	}
	
	public static long[] parseCommaSeparatedLongArray(String s)
	{
		if (s.isEmpty())
		{
			return new long[0];
		}
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		long[] val = new long[tmp.length];
		for (int i = 0; i < tmp.length; i++)
		{
			val[i] = Long.parseLong(tmp[i]);
		}
		return val;
	}
	
	public static long[][] parseStringForDoubleArray(String s)
	{
		String[] temp = s.replaceAll("\\n", ";").split(";");
		long[][] val = new long[temp.length][];
		
		for (int i = 0; i < temp.length; i++)
		{
			val[i] = parseCommaSeparatedLongArray(temp[i]);
		}
		return val;
	}
	
	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}
	
	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}
	
	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return true;
	}
	
	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String val, type, result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		Object fldObj;
		List<Field> fields = new ArrayList<>();
		while (cls != null)
		{
			for (Field fld : cls.getDeclaredFields())
			{
				if (!fields.contains(fld))
				{
					if (ignoreStatics && Modifier.isStatic(fld.getModifiers()))
					{
						continue;
					}
					fields.add(fld);
				}
			}
			cls = cls.getSuperclass();
			if (!parentFields)
			{
				break;
			}
		}
		
		for (Field fld : fields)
		{
			fld.setAccessible(true);
			try
			{
				fldObj = fld.get(o);
				if (fldObj == null)
				{
					val = "NULL";
				}
				else
				{
					val = fldObj.toString();
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
			type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();
			
			result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}
		
		result += "]\n";
		return result;
	}
	
	private static Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", Pattern.DOTALL);
	
	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<>();
		while (m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}
		
		tpls.put(0, html);
		return tpls;
	}
	
	public static boolean isDigit(String text)
	{
		if (text == null)
		{
			return false;
		}
		return text.matches("[0-9]+");
	}
	
	/**
	 * @param number
	 * @return From 123123 returns 123,123
	 */
	public static String getNumberWithCommas(long number)
	{
		String text = String.valueOf(number);
		int size = text.length();
		for (int i = size; i > 0; i--)
		{
			if ((((size - i) % 3) == 0) && (i < size))
			{
				text = text.substring(0, i) + ',' + text.substring(i);
			}
		}
		return text;
	}
	
	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}
	
	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		for (int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}
		return number;
	}
	
	public static String printData(byte[] data, int len)
	{
		StringBuffer result = new StringBuffer();
		int counter = 0;
		for (int i = 0; i < len; i++)
		{
			if ((counter % 16) == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");
				int charpoint = i - 15;
				for (int a = 0; a < 16; a++)
				{
					int t1 = data[charpoint++];
					if ((t1 > 0x1f) && (t1 < 0x80))
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}
				result.append("\n");
				counter = 0;
			}
		}
		int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < (17 - rest); i++)
			{
				result.append("   ");
			}
			int charpoint = data.length - rest;
			for (int a = 0; a < rest; a++)
			{
				int t1 = data[charpoint++];
				if ((t1 > 0x1f) && (t1 < 0x80))
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Random rnd = new Random();
		for (int i = 0; i < size; i++)
		{
			array[i] = (byte) rnd.nextInt(256);
		}
		return array;
	}
	
	/**
	 * @param <T>
	 * @param array - the array to look into
	 * @param obj - the object to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise.
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param array - the array to look into
	 * @param obj - the integer to search for
	 * @return {@code true} if the {@code array} contains the {@code obj}, {@code false} otherwise
	 */
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static int getGearPoints(Player player)
	{
		int points = 0;
		ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		ItemInstance chest = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		ItemInstance legs = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		ItemInstance boots = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET);
		ItemInstance gloves = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		ItemInstance helmet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		ItemInstance ring1 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
		ItemInstance ring2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER);
		ItemInstance earring1 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR);
		ItemInstance earring2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR);
		ItemInstance necklace = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
		
		// ===== CALCULATE ITEM POINTS =====
		for (int n = 0; n < 11; n++)
		{
			double pointsPerEnch = 0;
			ItemInstance item = null;
			double tmpPts = 0;
			boolean isWeapon = false;
			
			switch (n)
			{
				case 0: // weapon
					item = weapon;
					isWeapon = true;
					break;
				case 1: // chest
					item = chest;
					break;
				case 2: // legs
					item = legs;
					break;
				case 3: // boots
					item = boots;
					break;
				case 4: // gloves
					item = gloves;
					break;
				case 5: // helmet
					item = helmet;
					break;
				case 6: // ring1
					item = ring1;
					break;
				case 7: // ring2
					item = ring2;
					break;
				case 8: // earring1
					item = earring1;
					break;
				case 9: // earring2
					item = earring2;
					break;
				case 10: // necklace
					item = necklace;
					break;
			}
			if (item == null)
			{
				continue;
			}
			
			switch (item.getTemplate().getItemGrade())
			{
				case D:
					tmpPts += 25;
					pointsPerEnch = 0.5;
					break;
				case A:
					tmpPts += 75;
					pointsPerEnch = 1.5;
					break;
				case S:
					tmpPts += 125;
					pointsPerEnch = 2.5;
					break;
				case S80:
					tmpPts += 200;
					pointsPerEnch = 4;
					break;
				case S84:
					tmpPts += 300;
					pointsPerEnch = 6;
					if (item.getName().contains("Elegia")) // Im too lazy to do it via IDs
					{
						tmpPts += 150;
						pointsPerEnch = 8;
					}
					else if (item.getName().contains("Vorpal")) // Im too lazy to do it via IDs
					{
						tmpPts += 50;
						pointsPerEnch = 7;
					}
					break;
				default:
					break;
			}
			
			// get the item enchantment points
			double tempEnchPts = 0;
			for (int i = 0; i < item.getEnchantLevel(); i++)
			{
				tempEnchPts += pointsPerEnch * i;
			}
			tmpPts += tempEnchPts;
			
			if (isWeapon)
			{
				tmpPts *= 2;
			}
			
			// now add the temporary calculated points
			points += tmpPts;
		}
		
		// ===== CALCULATE SKILL POINTS =====
		for (Skill skill : player.getAllSkills())
		{
			switch (skill.getId())
			{
				case 3561: // Ring of Baium
					points += 500;
					break;
				case 3562: // Ring of Queen Ant
					points += 300;
					break;
				case 3560: // Earring of Orfen
					points += 100;
					break;
				case 3558: // Earring of Antharas
					points += 700;
					break;
				case 3559: // Zaken's Earring
					points += 400;
					break;
				case 3557: // Necklace of Valakas
					points += 900;
				case 3604: // Frintezza's Necklace
					points += 600;
					break;
				case 3649: // Beleth's Ring
					points += 150;
					break;
				case 3650: // PvP Weapon - CP Drain
				case 3651: // PvP Weapon - Cancel
				case 3652: // PvP Weapon - Ignore Shield Defense
				case 3653: // PvP Weapon - Attack Chance
				case 3654: // PvP Weapon - Casting
				case 3655: // PvP Weapon - Rapid Fire
				case 3656: // PvP Weapon - Decrease Range
				case 3657: // PvP Weapon - Decrease Resist
				case 3658: // PvP Shield - Reflect Damage
					points += 500;
					break;
				case 3659: // PvP Armor - Damage Down
				case 3660: // PvP Armor - Critical Down
				case 3661: // PvP Armor - Heal
				case 3662: // PvP Armor - Speed Down
				case 3663: // PvP Armor - Mirage
					points += 200;
					break;
				case 641: // Knight Ability - Boost HP
				case 642: // Enchanter Ability - Boost Mana
				case 643: // Summoner Ability - Boost HP/MP
				case 644: // Rogue ability - Evasion
				case 645: // Rogue Ability - Long Shot
				case 646: // Wizard Ability - Mana Gain
				case 647: // Enchanter Ability - Mana Recovery
				case 648: // Healer Ability - Prayer
				case 650: // Warrior Ability - Resist Trait
				case 651: // Warrior Ability - Haste
				case 652: // Knight Ability - Defense
				case 653: // Rogue Ability - Critical Chance
				case 654: // Wizard Ability - Mana Stea'
				case 1489: // Summoner Ability - Resist Attribute
				case 1490: // Healer Ability - Heal
				case 1491: // Summoner Ability - Spirit
				case 5572: // Warrior Ability - Haste
				case 5573: // Knight Ability - Defense
				case 5574: // Log Ability - Critical Chance
				case 5575: // Wizard Ability - Mana Steel
				case 5576: // Enchanter Ability - Barrier
				case 5577: // Healer Ability - Heal
				case 5578: // Summoner Ability - Spirit
					points += 100;
					break;
			}
		}
		return points;
	}
	
	/**
	 * Usable to format size of ... something as memory, file size and etc.
	 * @param bytes
	 * @return B, KB, MB, GB, TB
	 */
	public static String toNumInUnits(long bytes)
	{
		int u = 0;
		for (; bytes > (1024 * 1024); bytes >>= 10)
		{
			u++;
		}
		if (bytes > 1024)
		{
			u++;
		}
		return String.format("%.1f %cB", bytes / 1024f, " KMGTPE".charAt(u));
	}
	
	public static String getCPU_BIOS_HWID(String hwid)
	{
		byte[] hwidBytes = asByteArray(hwid);
		return asHex(new byte[]
		{
			hwidBytes[0],
			hwidBytes[1],
			hwidBytes[2],
			hwidBytes[3],
			hwidBytes[4],
			hwidBytes[5]
		});
	}
	
	public static byte[] asByteArray(String hex)
	{
		byte[] buf = new byte[hex.length() / 2];
		
		for (int i = 0; i < hex.length(); i += 2)
		{
			int j = Integer.parseInt(hex.substring(i, i + 2), 16);
			buf[(i / 2)] = (byte) (j & 0xFF);
		}
		return buf;
	}
	
	public static final String asHex(byte[] raw, int offset, int size)
	{
		StringBuffer strbuf = new StringBuffer(raw.length * 2);
		
		for (int i = 0; i < size; i++)
		{
			if ((raw[(offset + i)] & 0xFF) < 16)
			{
				strbuf.append("0");
			}
			strbuf.append(Long.toString(raw[(offset + i)] & 0xFF, 16));
		}
		
		return strbuf.toString();
	}
	
	public static final String asHex(byte[] raw)
	{
		return asHex(raw, 0, raw.length);
	}
	
	public static int min(int value1, int value2, int... values)
	{
		int min = Math.min(value1, value2);
		for (int value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static int max(int value1, int value2, int... values)
	{
		int max = Math.max(value1, value2);
		for (int value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static long min(long value1, long value2, long... values)
	{
		long min = Math.min(value1, value2);
		for (long value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static long max(long value1, long value2, long... values)
	{
		long max = Math.max(value1, value2);
		for (long value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static float min(float value1, float value2, float... values)
	{
		float min = Math.min(value1, value2);
		for (float value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static float max(float value1, float value2, float... values)
	{
		float max = Math.max(value1, value2);
		for (float value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static double min(double value1, double value2, double... values)
	{
		double min = Math.min(value1, value2);
		for (double value : values)
		{
			if (min > value)
			{
				min = value;
			}
		}
		return min;
	}
	
	public static double max(double value1, double value2, double... values)
	{
		double max = Math.max(value1, value2);
		for (double value : values)
		{
			if (max < value)
			{
				max = value;
			}
		}
		return max;
	}
	
	public static int getIndexOfMaxValue(int... array)
	{
		int index = 0;
		for (int i = 1; i < array.length; i++)
		{
			if (array[i] > array[index])
			{
				index = i;
			}
		}
		return index;
	}
	
	public static int getIndexOfMinValue(int... array)
	{
		int index = 0;
		for (int i = 1; i < array.length; i++)
		{
			if (array[i] < array[index])
			{
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static int map(int input, int inputMin, int inputMax, int outputMin, int outputMax)
	{
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static long map(long input, long inputMin, long inputMax, long outputMin, long outputMax)
	{
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static double map(double input, double inputMin, double inputMax, double outputMin, double outputMax)
	{
		return (((input - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static int constrain(int input, int min, int max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static long constrain(long input, long min, long max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static double constrain(double input, double min, double max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/** @return Value at the given index or 0 if AIOOBE should be thrown. */
	public static <E> E safeGet(List<E> list, int index)
	{
		return ((index >= 0) && (list.size() > index)) ? list.get(index) : null;
	}
	
	/** @return Value at the given index or 0 if AIOOBE should be thrown. */
	public static int safeGet(int[] arr, int index)
	{
		return ((index >= 0) && (arr.length > index)) ? arr[index] : 0;
	}
	
	/** @return Value at the given index or 0 if AIOOBE should be thrown. */
	public static double safeGet(double[] arr, int index)
	{
		return ((index >= 0) && (arr.length > index)) ? arr[index] : 0;
	}
	
	/** @return Value at the given index or 0 if AIOOBE should be thrown. */
	public static float safeGet(float[] arr, int index)
	{
		return ((index >= 0) && (arr.length > index)) ? arr[index] : 0;
	}
	
	/** @return Value at the given index or 0 if AIOOBE should be thrown. */
	public static long safeGet(long[] arr, int index)
	{
		return ((index >= 0) && (arr.length > index)) ? arr[index] : 0;
	}
	
	/** @return Value at the given index or false if AIOOBE should be thrown. */
	public static boolean safeGet(boolean[] arr, int index)
	{
		return ((index >= 0) && (arr.length > index)) ? arr[index] : false;
	}
	
	/** @return Value at the given index or null if AIOOBE should be thrown. */
	public static <T> T safeGet(T[] arr, int index)
	{
		return ((index >= 0) && (arr.length > index)) ? arr[index] : null;
	}
	
	public static boolean isInRange(int value, int min, int max)
	{
		if (value < min)
		{
			return false;
		}
		if (value > max)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean isInRange(long value, long min, long max)
	{
		if (value < min)
		{
			return false;
		}
		if (value > max)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean isInRange(double value, double min, double max)
	{
		if (value < min)
		{
			return false;
		}
		if (value > max)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean isInRange(float value, float min, float max)
	{
		if (value < min)
		{
			return false;
		}
		if (value > max)
		{
			return false;
		}
		
		return true;
	}
	
	/** @return Hashcode representation of the collection and its elements. */
	public static <E> int hashCode(Collection<E> collection)
	{
		int hashCode = 1;
		Iterator<E> i = collection.iterator();
		while (i.hasNext())
		{
			E obj = i.next();
			hashCode = (31 * hashCode) + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	
	public static String formatDate(final Date date, final String format)
	{
		if (date == null)
		{
			return null;
		}
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	public static int parseNextInt(final StringTokenizer st, final int defaultVal)
	{
		try
		{
			final String value = st.nextToken().trim();
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			return defaultVal;
		}
	}
	
	public static String formatPay(Player player, long count, int item)
	{
		if (count > 0)
		{
			return formatAdena(count) + " " + getItemName(item);
		}
		return "Free";
	}
	
	public static String getItemIcon(int itemId)
	{
		return ItemHolder.getInstance().getTemplate(itemId).getIcon();
	}
	
	public static String getItemName(int itemId)
	{
		if (itemId == ItemTemplate.ITEM_ID_FAME)
		{
			return "Fame";
		}
		else if (itemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
		{
			return "PC Bang point";
		}
		else if (itemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
		{
			return "Clan reputation";
		}
		else if (itemId == ItemTemplate.FESTIVAL)
		{
			return "Festival Adena";
		}
		else
		{
			return ItemHolder.getInstance().getTemplate(itemId).getName();
		}
	}
	
	public static String getFullClassName(int classId)
	{
		switch (classId)
		{
			case 0:
				return "Human Fighter";
			case 1:
				return "Warrior";
			case 2:
				return "Gladiator";
			case 3:
				return "Warlord";
			case 4:
				return "Human Knight";
			case 5:
				return "Paladin";
			case 6:
				return "Dark Avenger";
			case 7:
				return "Rogue";
			case 8:
				return "Treasure Hunter";
			case 9:
				return "Hawkeye";
			case 10:
				return "Human Mystic";
			case 11:
				return "Human Wizard";
			case 12:
				return "Sorcerer";
			case 13:
				return "Necromancer";
			case 14:
				return "Warlock";
			case 15:
				return "Cleric";
			case 16:
				return "Bishop";
			case 17:
				return "Prophet";
			case 18:
				return "Elven Fighter";
			case 19:
				return "Elven Knight";
			case 20:
				return "Temple Knight";
			case 21:
				return "Sword Singer";
			case 22:
				return "Elven Scout";
			case 23:
				return "Plains Walker";
			case 24:
				return "Silver Ranger";
			case 25:
				return "Elven Mystic";
			case 26:
				return "Elven Wizard";
			case 27:
				return "Spellsinger";
			case 28:
				return "Elemental Summoner";
			case 29:
				return "Elven Oracle";
			case 30:
				return "Elven Elder";
			case 31:
				return "Dark Fighter";
			case 32:
				return "Palus Knight";
			case 33:
				return "Shillien Knight";
			case 34:
				return "Bladedancer";
			case 35:
				return "Assassin";
			case 36:
				return "Abyss Walker";
			case 37:
				return "Phantom Ranger";
			case 38:
				return "Dark Mystic";
			case 39:
				return "Dark Wizard";
			case 40:
				return "Spellhowler";
			case 41:
				return "Phantom Summoner";
			case 42:
				return "Shillien Oracle";
			case 43:
				return "Shillien Elder";
			case 44:
				return "Orc Fighter";
			case 45:
				return "Orc Raider";
			case 46:
				return "Destroyer";
			case 47:
				return "Monk";
			case 48:
				return "Tyrant";
			case 49:
				return "Orc Mystic";
			case 50:
				return "Orc Shaman";
			case 51:
				return "Overlord";
			case 52:
				return "Warcryer";
			case 53:
				return "Dwarven Fighter";
			case 54:
				return "Scavenger";
			case 55:
				return "Bounty Hunter";
			case 56:
				return "Artisan";
			case 57:
				return "Warsmith";
			case 88:
				return "Duelist";
			case 89:
				return "Dreadnought";
			case 90:
				return "Phoenix Knight";
			case 91:
				return "Hell Knight";
			case 92:
				return "Sagittarius";
			case 93:
				return "Adventurer";
			case 94:
				return "Archmage";
			case 95:
				return "Soultaker";
			case 96:
				return "Arcana Lord";
			case 97:
				return "Cardinal";
			case 98:
				return "Hierophant";
			case 99:
				return "Eva's Templar";
			case 100:
				return "Sword Muse";
			case 101:
				return "Wind Rider";
			case 102:
				return "Moonlight Sentinel";
			case 103:
				return "Mystic Muse";
			case 104:
				return "Elemental Master";
			case 105:
				return "Eva's Saint";
			case 106:
				return "Shillien Templar";
			case 107:
				return "Spectral Dancer";
			case 108:
				return "Ghost Hunter";
			case 109:
				return "Ghost Sentinel";
			case 110:
				return "Storm Screamer";
			case 111:
				return "Spectral Master";
			case 112:
				return "Shillien Saint";
			case 113:
				return "Titan";
			case 114:
				return "Grand Khavatari";
			case 115:
				return "Dominator";
			case 116:
				return "Doom Cryer";
			case 117:
				return "Fortune Seeker";
			case 118:
				return "Maestro";
			case 123:
				return "Kamael Soldier";
			case 124:
				return "Kamael Soldier";
			case 125:
				return "Trooper";
			case 126:
				return "Warder";
			case 127:
				return "Berserker";
			case 128:
				return "Soul Breaker";
			case 129:
				return "Soul Breaker";
			case 130:
				return "Arbalester";
			case 131:
				return "Doombringer";
			case 132:
				return "Soul Hound";
			case 133:
				return "Soul Hound";
			case 134:
				return "Trickster";
			case 135:
				return "Inspector";
			case 136:
				return "Judicator";
			default:
				return "None";
		}
	}
	
	public static void communityNextPage(Player player, String link)
	{
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(link);
		if (handler != null)
		{
			handler.onBypassCommand(player, link);
		}
	}
	
	/**
	 * @param event
	 * @return
	 * @From FFADeathMatchEvent @Making FFADeathMatch
	 */
	public static String getChangedEventName(AbstractFightClub event)
	{
		String eventName = event.getClass().getSimpleName();// For example FFADeathMatchEvent
		eventName = eventName.substring(0, eventName.length() - 5);// Making it FFADeathMatch
		return eventName;
	}
	
	public static String getFullClassName(ClassId classIndex)
	{
		switch (classIndex)
		{
			case phoenixKnight:
				return "Phoenix Knight";
			case hellKnight:
				return "Hell Knight";
			case arcanaLord:
				return "Arcana Lord";
			case evaTemplar:
				return "Eva's Templar";
			case swordMuse:
				return "Sword Muse";
			case windRider:
				return "Wind Rider";
			case moonlightSentinel:
				return "Moonlight Sentinel";
			case mysticMuse:
				return "Mystic Muse";
			case elementalMaster:
				return "Elemental Master";
			case evaSaint:
				return "Eva's Saint";
			case shillienTemplar:
				return "ShillenTemplar";
			case spectralDancer:
				return "Spectral Dancer";
			case ghostHunter:
				return "Ghost Hunter";
			case ghostSentinel:
				return "Ghost Sentinel";
			case stormScreamer:
				return "Storm Screamer";
			case spectralMaster:
				return "Spectral Master";
			case shillienSaint:
				return "Shillien Saint";
			case grandKhauatari:
				return "Grand Khauatari";
			case fortuneSeeker:
				return "Fortune Seeker";
			case abyssWalker:
				return "Abyss Walker";
			case adventurer:
				return "Adventurer";
			case arbalester:
				return "Arbalester";
			case archmage:
				return "Archmage";
			case artisan:
				return "Artisan";
			case assassin:
				return "Assassin";
			case berserker:
				return "Berserker";
			case bishop:
				return "Bishop";
			case bladedancer:
				return "Blade Dancer";
			case bountyHunter:
				return "Bounty Hunter";
			case cardinal:
				return "Cardinal";
			case cleric:
				return "Cleric";
			case darkAvenger:
				return "Dark Avanger";
			case darkFighter:
				return "Dark Fighter";
			case darkMage:
				return "Dark Mage";
			case darkWizard:
				return "Dark Wizard";
			case destroyer:
				return "Destroyer";
			case dominator:
				return "Dominator";
			case doombringer:
				return "Doombringer";
			case doomcryer:
				return "Doomcryer";
			case dreadnought:
				return "Dreadnought";
			case duelist:
				return "Duelist";
			case dummyEntry1:
				return "Dummy";
			case dummyEntry10:
				return "Dummy";
			case dummyEntry11:
				return "Dummy";
			case dummyEntry12:
				return "Dummy";
			case dummyEntry13:
				return "Dummy";
			case dummyEntry14:
				return "Dummy";
			case dummyEntry15:
				return "Dummy";
			case dummyEntry16:
				return "Dummy";
			case dummyEntry17:
				return "Dummy";
			case dummyEntry18:
				return "Dummy";
			case dummyEntry19:
				return "Dummy";
			case dummyEntry2:
				return "Dummy";
			case dummyEntry20:
				return "Dummy";
			case dummyEntry21:
				return "Dummy";
			case dummyEntry22:
				return "Dummy";
			case dummyEntry23:
				return "Dummy";
			case dummyEntry24:
				return "Dummy";
			case dummyEntry25:
				return "Dummy";
			case dummyEntry26:
				return "Dummy";
			case dummyEntry27:
				return "Dummy";
			case dummyEntry28:
				return "Dummy";
			case dummyEntry29:
				return "Dummy";
			case dummyEntry3:
				return "Dummy";
			case dummyEntry30:
				return "Dummy";
			case dummyEntry31:
				return "Dummy";
			case dummyEntry32:
				return "Dummy";
			case dummyEntry33:
				return "Dummy";
			case dummyEntry34:
				return "Dummy";
			case dummyEntry4:
				return "Dummy";
			case dummyEntry5:
				return "Dummy";
			case dummyEntry6:
				return "Dummy";
			case dummyEntry7:
				return "Dummy";
			case dummyEntry8:
				return "Dummy";
			case dummyEntry9:
				return "Dummy";
			case dwarvenFighter:
				return "Dwarven Fighter";
			case elder:
				return "Elder";
			case elementalSummoner:
				return "Elemental Summoner";
			case elvenFighter:
				return "Elven Fighter";
			case elvenKnight:
				return "Elven Knight";
			case elvenMage:
				return "Elven Mage";
			case elvenScout:
				return "Elven Scout";
			case elvenWizard:
				return "Elven Wizard";
			case femaleSoldier:
				return "Female Soldier";
			case femaleSoulbreaker:
				return "Female Soulbreaker";
			case femaleSoulhound:
				return "Female Soulhound";
			case fighter:
				return "Fighter";
			case gladiator:
				return "Gladiator";
			case hawkeye:
				return "Hawkeye";
			case hierophant:
				return "Hierophant";
			case inspector:
				return "Inspector";
			case judicator:
				return "Judicator";
			case knight:
				return "Knight";
			case maestro:
				return "Maestro";
			case mage:
				return "Mage";
			case maleSoldier:
				return "Male Soldier";
			case maleSoulbreaker:
				return "Male Soulbreaker";
			case maleSoulhound:
				return "Male Soulhound";
			case necromancer:
				return "Necromancer";
			case oracle:
				return "Oracle";
			case orcFighter:
				return "Orc Fighter";
			case orcMage:
				return "Orc Mage";
			case orcMonk:
				return "Orc Monk";
			case orcRaider:
				return "Orc Raider";
			case orcShaman:
				return "Orc Shaman";
			case overlord:
				return "Overlord";
			case paladin:
				return "Paladin";
			case palusKnight:
				return "Palus Knight";
			case phantomRanger:
				return "Phantom Ranger";
			case phantomSummoner:
				return "Phantom Summoner";
			case plainsWalker:
				return "Plains Walker";
			case prophet:
				return "Prophet";
			case rogue:
				return "Rogue";
			case sagittarius:
				return "Sagittarius";
			case scavenger:
				return "Scavenger";
			case shillienElder:
				return "Shillien Elder";
			case shillienKnight:
				return "Shillien Knight";
			case shillienOracle:
				return "Shillien Oracle";
			case silverRanger:
				return "Silver Ranger";
			case sorceror:
				return "Sorceror";
			case soultaker:
				return "Soultaker";
			case spellhowler:
				return "Spellhowler";
			case spellsinger:
				return "Spellsinger";
			case swordSinger:
				return "Swordsinger";
			case templeKnight:
				return "Temple Knight";
			case titan:
				return "Titan";
			case treasureHunter:
				return "Treasure Hunter";
			case trickster:
				return "Trickster";
			case trooper:
				return "Trooper";
			case tyrant:
				return "Tyrant";
			case warcryer:
				return "Warcryer";
			case warder:
				return "Warder";
			case warlock:
				return "Warlock";
			case warlord:
				return "Warlord";
			case warrior:
				return "Warrior";
			case warsmith:
				return "Warsmith";
			case wizard:
				return "Wizard";
			default:
				break;
		}
		return classIndex.name().substring(0, 1).toUpperCase() + classIndex.name().substring(1);
		
	}
	
	public static void handleIllegalPlayerAction(Player actor, String etc_str1, String etc_str2, int isBug)
	{
		ThreadPoolManager.getInstance().schedule(new IllegalPlayerAction(actor, etc_str1, etc_str2, isBug), 500);
	}
	
	public static boolean arrayContains(@Nullable Object[] array, @Nullable Object objectToLookFor)
	{
		if ((array == null) || (objectToLookFor == null))
		{
			return false;
		}
		for (final Object objectInArray : array)
		{
			if ((objectInArray != null) && objectInArray.equals(objectToLookFor))
			{
				return true;
			}
		}
		return false;
	}
	
	public static String declension(long count, DeclensionKey word)
	{
		String one = "";
		String two = "";
		String five = "";
		switch (word)
		{
			case DAYS:
				one = new String("Day");
				two = new String("Days");
				five = new String("Days");
				break;
			case HOUR:
				one = new String("Hour");
				two = new String("Hours");
				five = new String("Hours");
				break;
			case MINUTES:
				one = new String("Minute");
				two = new String("Minutes");
				five = new String("Minutes");
				break;
			case PIECE:
				one = new String("Piece");
				two = new String("Pieces");
				five = new String("Pieces");
				break;
			case POINT:
				one = new String("Point");
				two = new String("Points");
				five = new String("Points");
		}
		if (count > 100L)
		{
			count %= 100L;
		}
		if (count > 20L)
		{
			count %= 10L;
		}
		if (count == 1L)
		{
			return one.toString();
		}
		if ((count == 2L) || (count == 3L) || (count == 4L))
		{
			return two.toString();
		}
		return five.toString();
	}
	
	private static final char[] ALLOWED_CHARS =
	{
		'1',
		'2',
		'3',
		'4',
		'5',
		'6',
		'7',
		'8',
		'9',
		'0'
	};
	
	public static boolean isInteger(char c)
	{
		for (char possibility : ALLOWED_CHARS)
		{
			if (possibility == c)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param name
	 * @return Funcion que convierte el string en proper case, de cada palabra del string, la primera se hace mayuscula, y las demas todas minisculas
	 */
	public static String toProperCaseAll(String name)
	{
		StringTokenizer st = new StringTokenizer(name);
		String newString = "";
		
		newString = st.nextToken();
		name = newString.substring(0, 1).toUpperCase();
		if (newString.length() > 1)
		{
			name += newString.substring(1).toLowerCase();
		}
		
		while (st.hasMoreTokens())
		{
			newString = st.nextToken();
			
			if (newString.length() > 2)
			{
				name += " " + newString.substring(0, 1).toUpperCase();
				name += newString.substring(1).toLowerCase();
			}
			else
			{
				name += " " + newString;
			}
		}
		
		return name;
	}
	
	public static boolean getPay(Player player, int itemid, long count, boolean sendMessage)
	{
		if (count == 0)
		{
			return true;
		}
		boolean check = false;
		switch (itemid)
		{
			case -300:
				if (player.getFame() >= count)
				{
					player.setFame(player.getFame() - (int) count, "Disappeared: {0}.");
					check = true;
				}
				break;
			case -200:
				if ((player.getClan() != null) && (player.getClan().getLevel() >= 5) && (player.getClan().getLeader().isClanLeader()) && (player.getClan().getReputationScore() >= count))
				{
					player.getClan().incReputation((int) -count, false, "Disappeared: {0}.");
					check = true;
				}
				break;
			case -100:
				if (player.getPcBangPoints() >= count)
				{
					if (player.reducePcBangPoints((int) count))
					{
						check = true;
					}
				}
				break;
			default:
				if (player.getInventory().getCountOf(itemid) >= count)
				{
					if (player.getInventory().destroyItemByItemId(itemid, count, "deleted"))
					{
						check = true;
					}
				}
				break;
		}
		if (!check)
		{
			if (sendMessage)
			{
				sendNotEnoughItemsMsg(player, itemid, count);
			}
			return false;
		}
		if (sendMessage)
		{
			player.sendMessage("Disappeared: " + formatPay(player, count, itemid) + ".");
		}
		return true;
	}
	
	@Nullable
	public static ItemActionLog getPay(Player player, int itemId, long count, String action, boolean sendMessage)
	{
		if (count == 0L)
		{
			return null;
		}
		ItemActionLog log = null;
		boolean check = false;
		switch (itemId)
		{
			case -300:
			{
				if (player.getFame() >= count)
				{
					log = new ItemActionLog(ItemStateLog.EXCHANGE_LOSE, action, player, "Fame", count);
					player.setFame(player.getFame() - (int) count, null);
					check = true;
					break;
				}
				break;
			}
			case -200:
			{
				if ((player.getClan() != null) && (player.getClan().getLevel() >= 5) && player.getClan().getLeader().isClanLeader() && (player.getClan().getReputationScore() >= count))
				{
					log = new ItemActionLog(ItemStateLog.EXCHANGE_LOSE, action, player, "ClanFame", count);
					player.getClan().incReputation((int) -count, false, null);
					check = true;
					break;
				}
				break;
			}
			case -100:
			{
				if (player.getPcBangPoints() < count)
				{
					break;
				}
				log = new ItemActionLog(ItemStateLog.EXCHANGE_LOSE, action, player, "PcBangPoints", count);
				if (player.reducePcBangPoints((int) count))
				{
					check = true;
					break;
				}
				break;
			}
			default:
			{
				final ItemInstance item = player.getInventory().getItemByItemId(itemId);
				if ((item == null) || (item.getCount() < count))
				{
					break;
				}
				log = new ItemActionLog(ItemStateLog.EXCHANGE_LOSE, action, player, item, count);
				if (player.getInventory().destroyItem(item, count, null))
				{
					check = true;
					break;
				}
				break;
			}
		}
		if (!check)
		{
			if (sendMessage)
			{
				sendNotEnoughItemsMsg(player, itemId, count);
			}
			return null;
		}
		if (sendMessage)
		{
			player.sendMessage("Disappeared: " + formatPay(player, count, itemId));
		}
		return log;
	}
	
	private static void sendNotEnoughItemsMsg(Player player, int itemid, long count)
	{
		final String msg = "Yo do not have " + formatPay(player, count, itemid) + ".";
		player.sendPacket(new ExShowScreenMessage(msg, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, false));
		player.sendMessage(msg);
	}
	
	/**
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two objects
	 */
	public static double calculateDistance(Creature obj1, Creature obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return 1000000;
		}
		
		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	
	/**
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param includeZAxis - if true, includes also the Z axis in the calculation
	 * @return the distance between the two coordinates
	 */
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;
		
		if (includeZAxis)
		{
			final double dz = z1 - z2;
			return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	/**
	 * @param list
	 * @param joiner
	 * @return Une todos los elementos de un array con determinado caracter y devuelve el string final
	 */
	public static String joinArrayWithCharacter(Object[] list, String joiner)
	{
		if ((list == null) || (list.length < 1))
		{
			return "";
		}
		
		String result = "";
		for (Object val : list)
		{
			result += val + joiner;
		}
		
		return result.substring(0, result.length() - joiner.length());
	}
	
	public static String boolToString(boolean b)
	{
		return b ? "True" : "False";
	}
	
	/**
	 * @param drops
	 * @param env
	 * @return
	 */
	public static List<DroplistItem> calculateDroplistItems(Env env, Collection<Droplist> drops)
	{
		List<DroplistItem> itemsToDrop = null;
		for (Droplist drop : drops)
		{
			if (!drop.verifyConditions(env))
			{
				continue;
			}
			
			final List<DroplistItem> items = calculateDroplistGroups(drop.getGroups());
			if (!items.isEmpty())
			{
				if (itemsToDrop == null)
				{
					itemsToDrop = new ArrayList<>();
				}
				itemsToDrop.addAll(items);
			}
		}
		return itemsToDrop != null ? itemsToDrop : Collections.<DroplistItem> emptyList();
	}
	
	/**
	 * @param drops
	 * @param env
	 * @return
	 */
	public static List<DroplistItem> calculateDroplistItems(Env env, Droplist... drops)
	{
		List<DroplistItem> itemsToDrop = null;
		for (Droplist drop : drops)
		{
			if (!drop.verifyConditions(env))
			{
				continue;
			}
			
			final List<DroplistItem> items = calculateDroplistGroups(drop.getGroups());
			if (!items.isEmpty())
			{
				if (itemsToDrop == null)
				{
					itemsToDrop = new ArrayList<>();
				}
				itemsToDrop.addAll(items);
			}
		}
		return itemsToDrop != null ? itemsToDrop : Collections.<DroplistItem> emptyList();
	}
	
	/**
	 * @param groups
	 * @return
	 */
	public static List<DroplistItem> calculateDroplistGroups(List<DroplistGroup> groups)
	{
		List<DroplistItem> itemsToDrop = null;
		for (DroplistGroup group : groups)
		{
			final double groupRandom = 100 * Rnd.nextDouble();
			if (groupRandom < (group.getChance()))
			{
				final double itemRandom = 100 * Rnd.nextDouble();
				float cumulativeChance = 0;
				for (DroplistItem item : group.getItems())
				{
					if (itemRandom < (cumulativeChance += item.getChance()))
					{
						if (itemsToDrop == null)
						{
							itemsToDrop = new ArrayList<>();
						}
						itemsToDrop.add(item);
						break;
					}
				}
			}
		}
		return itemsToDrop != null ? itemsToDrop : Collections.<DroplistItem> emptyList();
	}
	
	/**
	 * @param range
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis
	 * @return {@code true} if the two objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (obj1.getReflectionId() != obj2.getReflectionId())
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}
		
		int rad = 0;
		if (obj1 instanceof Creature)
		{
			rad += ((Creature) obj1).getTemplate().getCollisionRadius();
		}
		if (obj2 instanceof Creature)
		{
			rad += ((Creature) obj2).getTemplate().getCollisionRadius();
		}
		
		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();
		double d = (dx * dx) + (dy * dy);
		
		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			d += (dz * dz);
		}
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
	
	/**
	 * Si pasa los 9999a pasan a ser 10k sin decimales, si pasa los 1000k pasa a ser 1kk, con 2 decimales si pasa los 1000kk pasa a ser 1kkk con 3 decimales
	 * @param price
	 * @return Esta funcion convierte el precio actual en un formato mas amigable
	 */
	public static String convertToLineagePriceFormat(double price)
	{
		if (price < 10000)
		{
			return Math.round(price) + "a";
		}
		else if (price < 1000000)
		{
			return Util.reduceDecimals(price / 1000, 1) + "k";
		}
		else if (price < 1000000000)
		{
			return Util.reduceDecimals(price / 1000 / 1000, 1) + "kk";
		}
		else
		{
			return Util.reduceDecimals(price / 1000 / 1000 / 1000, 1) + "kkk";
		}
	}
	
	public static String spaceBeforeUpper(String text)
	{
		final StringBuilder builder = new StringBuilder();
		for (char c : text.toCharArray())
		{
			if (Character.isUpperCase(c))
			{
				builder.append(' ');
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
	/**
	 * Funcion simple que devuelve el mismo numero solo que se asegura de que tenga maximo nDecim cantidad de decimales La idea esta en encontrar el . que separa el decimal y de ahi sumar tantos decimales como se quiera maximo 10.5912312, 2 = 10.59 Ademas si por ejemplo termina en .0 o .00 se los
	 * quita
	 * @param original
	 * @param nDecim
	 * @return Devuelve el mismo numero solo que se asegura de que tenga maximo nDecim cantidad de decimales
	 */
	public static String reduceDecimals(double original, int nDecim)
	{
		return reduceDecimals(original, nDecim, false);
	}
	
	public static String reduceDecimals(double original, int nDecim, boolean round)
	{
		String decimals = "#";
		if (nDecim > 0)
		{
			decimals += ".";
			for (int i = 0; i < nDecim; i++)
			{
				decimals += "#";
			}
		}
		
		final DecimalFormat df = new DecimalFormat(decimals);
		return df.format((round ? Math.round(original) : original)).replace(",", ".");
	}
	
	
	public static TIntStringHashMap parseTemplates(String html)
	{
		Matcher m = _pattern.matcher(html);
		
		TIntStringHashMap tpls = new TIntStringHashMap();
		
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}
		
		tpls.put(0, html);
		return tpls;
	}
	
}