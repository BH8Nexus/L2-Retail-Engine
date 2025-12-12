package l2r.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Regexp
{
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
}