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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastMap;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;

/**
 * @author Nik, Infern0
 */
public class CharacterEmails
{
	// private static Logger _log = Logger.getLogger(CharacterEmails.class.getName());
	
	private static final Logger LOG = LoggerFactory.getLogger(CharacterEmails.class);
	
	private static String EMAIL_VERIFICATION_HTML_PATH = "mods/EmailValidation.htm";
	
	public static void verifyEmail(Player activeChar)
	{
		if (!Config.ENABLE_EMAIL_VALIDATION)
		{
			return;
		}
		
		if (!hasEmail(activeChar))
		{
			sendEmailVerificationHtml(activeChar);
		}
	}
	
	public static void sendEmailVerificationHtml(Player activeChar)
	{
		String html = HtmCache.getInstance().getNotNull(EMAIL_VERIFICATION_HTML_PATH, activeChar);
		activeChar.sendPacket(new TutorialShowHtml(html));
	}
	
	public static void setEmail(Player activeChar, String email)
	{
		if (activeChar == null)
		{
			return;
		}
		
		setEmail(activeChar.getAccountName(), email);
	}
	
	public static void setEmail(String accountName, String email)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO account_data VALUES (?,?,?)");
			statement.setString(1, accountName);
			statement.setString(2, "email_addr");
			statement.setString(3, email);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOG.warn("Could not save email:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static boolean hasEmail(Player activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		return getEmail(activeChar.getAccountName()) != null;
	}
	
	public static String getEmail(Player activeChar)
	{
		if (activeChar == null)
		{
			return null;
		}
		
		return getEmail(activeChar.getAccountName());
	}
	
	public static String getEmail(String accountName)
	{
		String email = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT value FROM account_data WHERE account_name=? AND var='email_addr'");
			statement.setString(1, accountName);
			rset = statement.executeQuery();
			while (rset.next())
			{
				email = rset.getString(1);
			}
		}
		catch (Exception e)
		{
			LOG.warn("Could not get email:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return email;
	}
	
	public static void change(String command, Player player, String email, String email2, String friend)
	{
		
		if (player == null)
		{
			return;
		}
		
		if (!Config.ENABLE_EMAIL_VALIDATION)
		{
			// Original Message: Email Validation is disabled.
			player.sendMessage(new CustomMessage("l2r.gameserver.randoms.CharacterEmails.message1", player));
			return;
		}
		
		if (command.equalsIgnoreCase("emailvalidation"))
		{
			
			String mail = email;
			String mail2 = email2;
			String referral = friend;
			
			if (!referral.isEmpty())
			{
				if (player.getLevel() >= 20)
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Π’Ρ‹ Π΄ΠΎΠ»Π¶Π½Ρ‹ Π±Ρ‹Ρ‚Ρ� Π½Πµ Π½ΠΈΠ¶Πµ 20 ΡƒΡ€ΠΎΠ²Π½Ρ� ΠΈΡ�ΠΏΠΎΠ»Ρ�Π·ΠΎΠ²Π°Π½ΠΈΡ� Ρ�ΠΏΡ€Π°Π²ΠΎΡ‡Π½ΠΎΠΉ Ρ�ΠΈΡ�Ρ‚ΠµΠΌΡ‹. Π’Π°Ρ�Π° Π�Ρ€ΠΈΠ²ΠµΠ΄ΠΈ Π±ΡƒΠ΄ΡƒΡ‚ ΠΈΠ³Π½ΠΎΡ€ΠΈΡ€ΠΎΠ²Π°Ρ‚Ρ�Ρ�Ρ�." : "You must be below level 20 to use referral system. Your refer will be ignored."));
					return;
				}
				else if ((referral.length() > 16) || (CharacterDAO.getInstance().getObjectIdByName(referral) <= 0))
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Π�ΠΌΡ� ΠΏΠµΡ€Ρ�ΠΎΠ½Π°Π¶Π° Ρ€ΠµΡ„ΠµΡ€Π°Π»Π° Π½Πµ Ρ�ΡƒΡ‰ΠµΡ�Ρ‚Π²ΡƒΠµΡ‚. Π�Ρ�Ρ‚Π°Π²Ρ�Ρ‚Πµ ΠΏΠΎΠ»Πµ ΠΈΠΌΠµΠ½ΠΈ Ρ€ΠµΡ„ΠµΡ€Π°Π»Π° ΠΏΡƒΡ�Ρ‚Ρ‹ΠΌ, ΠµΡ�Π»ΠΈ Π²Ρ‹ Ρ…ΠΎΡ‚ΠΈΡ‚Πµ ΠΏΡ€ΠΎΠ΄ΠΎΠ»Π¶ΠΈΡ‚Ρ�, Π½Πµ ΡƒΡ�Ρ‚Π°Π½Π°Π²Π»ΠΈΠ²Π°Ρ� ΠµΠ³ΠΎ." : "The referral's character name doesn't exist. Leave the referral's name field blank if you wish to continue without setting it."));
					return;
				}
				else if (referral.equalsIgnoreCase(player.getName()))
				{
					player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Π’Ρ‹ Π½Πµ ΠΌΠΎΠ¶ΠµΡ‚Πµ ΠΈΡ�ΠΏΠΎΠ»Ρ�Π·ΠΎΠ²Π°Ρ‚Ρ� Ρ�ΠµΠ±Ρ� Π² ΠΊΠ°Ρ‡ΠµΡ�Ρ‚Π²Πµ Π½Π°ΠΏΡ€Π°Π²Π»ΠµΠ½ΠΈΡ�." : "You cannot use yourself as the referral."));
					return;
				}
				
				referralSystem(player, referral); // Blank "-" is accepted too.
			}
			
			if (!mail.isEmpty() && mail.contains("@") && mail.contains(".") && (mail.length() <= 50) && (mail.length() >= 5))
			{
				if (mail.equalsIgnoreCase(mail2))
				{
					CharacterEmails.setEmail(player, mail);
					// Original Message: Your account's e-mail address is set to: {0}
					player.sendMessage(new CustomMessage("l2r.gameserver.randoms.CharacterEmails.message2", player).addString(mail));
					player.sendPacket(TutorialCloseHtml.STATIC);
				}
				else
				{
					// Original Message: The e-mail addresses in the boxes do not match.
					player.sendMessage(new CustomMessage("l2r.gameserver.randoms.CharacterEmails.message3", player));
				}
			}
			else
			{
				// Original Message: The e-mail address is invalid.
				player.sendMessage(new CustomMessage("l2r.gameserver.randoms.CharacterEmails.message4", player));
			}
		}
	}
	
	private static void referralSystem(Player player, String referralName)
	{
		if (player.getHWID() == null)
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Π§Ρ‚ΠΎ-Ρ‚ΠΎ Π½Πµ Ρ‚Π°ΠΊ Ρ�Π»ΡƒΡ‡ΠΈΠ»ΠΎΡ�Ρ�, ΠΏΠΎΠ¶Π°Π»ΡƒΠΉΡ�Ρ‚Π°, Ρ�Π²Ρ�Π¶ΠΈΡ‚ΠµΡ�Ρ� Ρ� Π°Π΄ΠΌΠΈΠ½ΠΈΡ�Ρ‚Ρ€Π°Ρ‚ΠΎΡ€ΠΎΠΌ Ρ…Ρ€ΠΎΠΌΠΎΠΉ." : "Something wrong happend, please contact the lame admin."));
			LOG.warn("Referral System: Error while check character HWID!");
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM referral_system WHERE senderHWID=?");
			statement.setString(1, player.getHWID());
			rset = statement.executeQuery();
			if (rset.next())
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Π’Ρ‹ Π»ΠΈΠ±ΠΎ Π½Πµ Π½ΠΎΠ²Ρ‹ΠΉ ΠΈΠ³Ρ€ΠΎΠΊ ΠΈΠ»ΠΈ Π²Ρ‹ ΡƒΠ¶Πµ ΡƒΡ�Ρ‚Π°Π½ΠΎΠ²ΠΈΠ»ΠΈ Π½Π°ΠΏΡ€Π°Π²Π»ΠµΠ½ΠΈΠµ." : "You are either not a new player or you have already set a referral."));
				return;
			}
		}
		catch (Exception e)
		{
			LOG.warn("Could not get referral system settings:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO referral_system VALUES (?,?,?,?,?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setString(2, player.getHWID());
			statement.setString(3, player.getName());
			statement.setInt(4, 0); // senderReward
			statement.setString(5, referralName);
			statement.setDate(6, new Date(System.currentTimeMillis()));
			statement.setInt(7, 0);
			statement.executeUpdate();
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Referral", (player.isLangRus() ? "Π’Ρ‹ ΡƒΡ�ΠΏΠµΡ�Π½ΠΎ ΡƒΡ�Ρ‚Π°Π½ΠΎΠ²ΠΈΡ‚Ρ� Π½Π°ΠΏΡ€Π°Π²Π»ΠµΠ½ΠΈΠµ. Π�Π°ΠΊ Ρ‚ΠΎΠ»Ρ�ΠΊΠΎ Π²Ρ‹ Π΄ΠΎΡ�Ρ‚ΠΈΠ³Π°ΠµΡ‚Πµ 85 ΡƒΡ€ΠΎΠ²Π½Ρ�, Π²Π°Ρ�Πµ Π½Π°ΠΏΡ€Π°Π²Π»ΠµΠ½ΠΈΠµ Π±ΡƒΠ΄ΠµΡ‚ Π²ΠΎΠ·Π½Π°Π³Ρ€Π°Π¶Π΄ΠµΠ½." : "You have successfully set your referral. Once you reach Level 85, your referral will be rewarded."));
		}
		catch (Exception e)
		{
			LOG.warn("Could not save referral system settings:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public static void newcomersReward(Player activeChar, int amount, int levelPassed)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM referral_system WHERE senderHWID=? OR senderAccount=?");
			statement2 = con.prepareStatement("UPDATE `referral_system` SET `senderReward`=? WHERE (`senderHWID`=?) OR (`senderAccount`=?)");
			statement.setString(1, activeChar.getHWID());
			statement.setString(2, activeChar.getAccountName());
			rset = statement.executeQuery();
			while (rset.next())
			{
				int sndRwd = rset.getInt("senderReward");
				if (sndRwd >= 1000)
				{
					return;
				}
				
				if ((levelPassed == 20) && (sndRwd >= 250))
				{
					return;
				}
				else if ((levelPassed == 40) && (sndRwd >= 500))
				{
					return;
				}
				else if ((levelPassed == 60) && (sndRwd >= 750))
				{
					return;
				}
				else if ((levelPassed == 80) && (sndRwd >= 1000))
				{
					return;
				}
				
				statement2.setInt(1, amount + sndRwd);
				statement2.setString(2, activeChar.getHWID());
				statement2.setString(3, activeChar.getAccountName());
				int updated = statement2.executeUpdate();
				if (updated > 0)
				{
					Map<Integer, Long> item = new FastMap<>();
					item.put(13693, (long) amount);
					
					Functions.sendSystemMail(activeChar, "Newscomers' Gift", "Hey newcomer, I see that you are making a good progress. Here is a little reward to help you along your way :P", item);
					// _log.info("Referral System: Player " + activeChar.getName() + " has reached level " + levelPassed + " . And recived " + amount + " coins.");
				}
				
				return;
			}
			
		}
		catch (Exception e)
		{
			LOG.warn("Could not get referral system settings:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			DbUtils.closeQuietly(statement2);
		}
	}
	
	public static void referralReward(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		// PreparedStatement statement3 = null;
		ResultSet rset = null;
		// ResultSet rset3 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `referralName`, `rewarded` FROM `referral_system` WHERE `senderHWID` = ?");
			statement.setString(1, activeChar.getHWID());
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				String referralName = rset.getString("referralName");
				int rewarded = rset.getInt("rewarded");
				if (rewarded == 1)
				{
					return;
				}
				
				int updated = 0;
				statement2 = con.prepareStatement("UPDATE `referral_system` SET `rewarded` = '1' WHERE `senderHWID` = ?");
				statement2.setString(1, activeChar.getHWID());
				updated = statement2.executeUpdate();
				
				/*
				 * disabled till understand why does not take the count.. int rewardedCount = 0; statement3 = con.prepareStatement("SELECT COUNT(*) as rewarded FROM `referral_system` WHERE `referralName` = ? AND `rewarded` = '1' LIMIT 0, 30;"); statement3.setString(1, referralName); rset3 =
				 * statement.executeQuery(); if (rset3.next()) rewardedCount = rset3.getInt("rewarded");
				 */
				
				int referralOID = CharacterDAO.getInstance().getObjectIdByName(referralName);
				Player referralplayer = World.getPlayer(referralOID);
				if (referralOID > 0)
				{
					if (updated > 0)
					{
						int rewardAmount = 100;
						/*
						 * if (rewardedCount >= 30) rewardAmount = 150;
						 */
						
						Mail mail = new Mail();
						mail.setSenderId(1);
						mail.setSenderName("Admin");
						mail.setReceiverId(referralOID);
						mail.setReceiverName(referralName);
						mail.setTopic("Referral System");
						mail.setBody(referralName + " has reached level 85. You will be rewarded for inviting him to the server.");
						ItemInstance item = new ItemInstance(13693);
						item.setLocation(ItemInstance.ItemLocation.MAIL);
						item.setCount(rewardAmount);
						item.save();
						mail.addAttachment(item);
						mail.setType(Mail.SenderType.NEWS_INFORMER);
						mail.setUnread(true);
						mail.setExpireTime((720 * 3600) + (int) (System.currentTimeMillis() / 1000L));
						mail.save();
						
						if (referralplayer != null)
						{
							referralplayer.sendPacket(ExNoticePostArrived.STATIC_TRUE);
							referralplayer.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
						}
						
						// _log.info("Referral System: Player " + activeChar.getName() + " has reached max level with referral " + referralName + " . Reward sended.");
					}
				}
				return;
			}
		}
		catch (Exception e)
		{
			LOG.warn("Could not get referral system settings:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			DbUtils.closeQuietly(statement2);
		}
	}
	
	public static void showReferralHtml(Player activeChar)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM referral_system WHERE referralName=?");
			statement.setString(1, activeChar.getName());
			rset = statement.executeQuery();
			
			StringBuilder sb = new StringBuilder(500);
			sb.append("<html><title>Referral System</title><body> List of people which you have invited to the server:<br><table width=280>");
			sb.append("<tr><td width=100>");
			sb.append("<font color=LEVEL>Character</font>");
			sb.append("</td><td width=80>");
			sb.append("<font color=LEVEL>Date</font>");
			sb.append("</td><td width=80>");
			sb.append("<font color=LEVEL>Status</font>");
			sb.append("</td></tr>");
			while (rset.next())
			{
				String senderCharacter = rset.getString("senderCharacter");
				boolean rewarded = rset.getInt("rewarded") == 1;
				Date date = rset.getDate("date");
				String font = rewarded ? "<font color=00FF00>" : "<font color=FF0000>";
				sb.append("<tr><td>");
				sb.append(font + senderCharacter + "</font>");
				sb.append("</td><td>");
				sb.append(font + date.toString() + "</font>");
				sb.append("</td><td>");
				sb.append(font + (rewarded ? "aproved" : "unaproved") + "</font>");
				sb.append("</td></tr>");
			}
			
			sb.append("</table></body></html>");
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml(sb.toString());
			activeChar.sendPacket(html);
		}
		catch (Exception e)
		{
			LOG.warn("Could not get referral system settings:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
}
