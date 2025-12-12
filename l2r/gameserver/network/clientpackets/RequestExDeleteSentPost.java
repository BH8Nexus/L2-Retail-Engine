package l2r.gameserver.network.clientpackets;

import l2r.gameserver.dao.MailDAO;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExShowSentPostList;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Ð—Ð°Ð¿Ñ€Ð¾Ñ� Ð½Ð° ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ðµ Ð¾Ñ‚Ð¿Ñ€Ð°Ð²Ð»ÐµÐ½Ð½Ñ‹Ñ… Ñ�Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ð¹. Ð£Ð´Ð°Ð»Ð¸Ñ‚ÑŒ Ð¼Ð¾Ð¶Ð½Ð¾ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¸Ñ�ÑŒÐ¼Ð¾ Ð±ÐµÐ· Ð²Ð»Ð¾Ð¶ÐµÐ½Ð¸Ñ�. ÐžÑ‚Ñ�Ñ‹Ð»Ð°ÐµÑ‚Ñ�Ñ� Ð¿Ñ€Ð¸ Ð½Ð°Ð¶Ð°Ñ‚Ð¸Ð¸ Ð½Ð° "delete" Ð² Ñ�Ð¿Ð¸Ñ�ÐºÐµ Ð¾Ñ‚Ð¿Ñ€Ð°Ð²Ð»ÐµÐ½Ð½Ñ‹Ñ… Ð¿Ð¸Ñ�ÐµÐ¼.
 * @see ExShowSentPostList
 * @see RequestExDeleteReceivedPost
 */
public class RequestExDeleteSentPost extends L2GameClientPacket
{
	private int _count;
	private int[] _list;

	/**
	 * format: dx[d]
	 */
	@Override
	protected void readImpl()
	{
		_count = readD(); // ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ñ�Ð»ÐµÐ¼ÐµÐ½Ñ‚Ð¾Ð² Ð´Ð»Ñ� ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ�
		if(_count * 4 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_list = new int[_count];
		for(int i = 0; i < _count; i++)
			_list[i] = readD(); // ÑƒÐ½Ð¸ÐºÐ°Ð»ÑŒÐ½Ñ‹Ð¹ Ð½Ð¾Ð¼ÐµÑ€ Ð¿Ð¸Ñ�ÑŒÐ¼Ð°
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;

		
		Collection<Mail> mails = MailDAO.getInstance().getSentMailByOwnerId(activeChar.getObjectId());
		if(!mails.isEmpty())
		{
			for(Mail mail : mails)
				if(ArrayUtils.contains(_list, mail.getMessageId()))
					if(mail.getAttachments().isEmpty())
					{
						//FIXME [G1ta0] ÐµÑ�Ð»Ð¸ Ð¿Ð¾Ñ‡Ñ‚Ð° Ð½Ðµ Ð¿Ñ€Ð¾Ñ‡Ð¸Ñ‚Ð°Ð½Ð° Ð¿Ð¾Ð»ÑƒÑ‡Ð°Ñ‚ÐµÐ»ÐµÐ¼, Ð²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾ Ð¸Ð¼ÐµÐµÑ‚ Ñ�Ð¼Ñ‹Ñ�Ð» ÑƒÐ´Ð°Ð»Ñ�Ñ‚ÑŒ ÐµÐµ Ñ�Ð¾Ð²Ñ�ÐµÐ¼, Ð½Ð° Ð¾Ñ„Ñ„. Ñ�ÐµÑ€Ð²ÐµÑ€Ðµ Ð½Ðµ ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ�.
						/*if(mail.isUnread())
							mail.delete();
						else*/
						MailDAO.getInstance().deleteSentMailByMailId(activeChar.getObjectId(), mail.getMessageId());
					}
		}

		activeChar.sendPacket(new ExShowSentPostList(activeChar));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}