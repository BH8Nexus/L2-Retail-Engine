package l2r.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.dao.JdbcDAO;
import l2r.commons.dao.JdbcEntityState;
import l2r.commons.dao.JdbcEntityStats;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;

public class MailDAO implements JdbcDAO<Integer, Mail>
{
	private static final Logger _log = LoggerFactory.getLogger(MailDAO.class);
	
	private final static String RESTORE_MAIL = "SELECT sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread FROM mail WHERE message_id = ?";
	private final static String STORE_MAIL = "INSERT INTO mail(sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private final static String UPDATE_MAIL = "UPDATE mail SET sender_id = ?, sender_name = ?, receiver_id = ?, receiver_name = ?, expire_time = ?, topic = ?, body = ?, price = ?, type = ?, unread = ? WHERE message_id = ?";
	private final static String REMOVE_MAIL = "DELETE FROM mail WHERE message_id = ?";
	
	private final static String RESTORE_EXPIRED_MAIL = "SELECT message_id FROM mail WHERE expire_time <= ?";
	
	private final static String RESTORE_OWN_MAIL = "SELECT message_id FROM character_mail WHERE char_id = ? AND is_sender = ?";
	private final static String STORE_OWN_MAIL = "INSERT INTO character_mail(char_id, message_id, is_sender) VALUES (?,?,?)";
	private final static String REMOVE_OWN_MAIL = "DELETE FROM character_mail WHERE char_id = ? AND message_id = ? AND is_sender = ?";
	
	private final static String RESTORE_MAIL_ATTACHMENTS = "SELECT item_id FROM mail_attachments WHERE message_id = ?";
	private final static String STORE_MAIL_ATTACHMENT = "INSERT INTO mail_attachments(message_id, item_id) VALUES (?,?)";
	private final static String REMOVE_MAIL_ATTACHMENTS = "DELETE FROM mail_attachments WHERE message_id = ?";
	
	private final static MailDAO instance = new MailDAO();
	
	public static MailDAO getInstance()
	{
		return instance;
	}
	
	private final AtomicLong load = new AtomicLong();
	private final AtomicLong insert = new AtomicLong();
	private final AtomicLong update = new AtomicLong();
	private final AtomicLong delete = new AtomicLong();
	
	// private final Cache cache;
	private static Map<Integer, Mail> _cache;
	private final JdbcEntityStats stats = new JdbcEntityStats()
	{
		@Override
		public long getLoadCount()
		{
			return load.get();
		}
		
		@Override
		public long getInsertCount()
		{
			return insert.get();
		}
		
		@Override
		public long getUpdateCount()
		{
			return update.get();
		}
		
		@Override
		public long getDeleteCount()
		{
			return delete.get();
		}
	};
	
	private MailDAO()
	{
		_cache = new ConcurrentHashMap<>();
	}
	
	public Map<Integer, Mail> getCache()
	{
		return _cache;
	}
	
	@Override
	public JdbcEntityStats getStats()
	{
		return stats;
	}
	
	private List<Mail> getMailByOwnerId(int ownerId, boolean sent)
	{
		List<Integer> messageIds = Collections.emptyList();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_OWN_MAIL);
			statement.setInt(1, ownerId);
			statement.setBoolean(2, sent);
			rset = statement.executeQuery();
			messageIds = new ArrayList<>();
			while (rset.next())
			{
				messageIds.add(rset.getInt(1));
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while restore mail of owner : " + ownerId, e);
			messageIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return load(messageIds);
	}
	
	private boolean deleteMailByOwnerIdAndMailId(int ownerId, int messageId, boolean sent)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_OWN_MAIL);
			statement.setInt(1, ownerId);
			statement.setInt(2, messageId);
			statement.setBoolean(3, sent);
			return statement.execute();
		}
		catch (SQLException e)
		{
			_log.error("Error while deleting mail of owner : " + ownerId, e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public List<Mail> getReceivedMailByOwnerId(int receiverId)
	{
		return getMailByOwnerId(receiverId, false);
	}
	
	public List<Mail> getSentMailByOwnerId(int senderId)
	{
		return getMailByOwnerId(senderId, true);
	}
	
	public Mail getReceivedMailByMailId(int receiverId, int messageId)
	{
		List<Mail> list = getMailByOwnerId(receiverId, false);
		
		for (Mail mail : list)
		{
			if (mail.getMessageId() == messageId)
			{
				return mail;
			}
		}
		
		return null;
	}
	
	public Mail getSentMailByMailId(int senderId, int messageId)
	{
		List<Mail> list = getMailByOwnerId(senderId, true);
		
		for (Mail mail : list)
		{
			if (mail.getMessageId() == messageId)
			{
				return mail;
			}
		}
		
		return null;
	}
	
	public boolean deleteReceivedMailByMailId(int receiverId, int messageId)
	{
		return deleteMailByOwnerIdAndMailId(receiverId, messageId, false);
	}
	
	public boolean deleteSentMailByMailId(int senderId, int messageId)
	{
		return deleteMailByOwnerIdAndMailId(senderId, messageId, true);
	}
	
	public List<Mail> getExpiredMail(int expireTime)
	{
		List<Integer> messageIds = Collections.emptyList();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_EXPIRED_MAIL);
			statement.setInt(1, expireTime);
			rset = statement.executeQuery();
			messageIds = new ArrayList<>();
			while (rset.next())
			{
				messageIds.add(rset.getInt(1));
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while restore expired mail!", e);
			messageIds.clear();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return load(messageIds);
	}
	
	@Override
	public Mail load(Integer id)
	{
		Mail mail = _cache.get(id);
		if (mail != null)
		{
			return mail;
		}
		
		try
		{
			// mail = null;
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(RESTORE_MAIL);
				statement.setInt(1, id);
				rset = statement.executeQuery();
				if (rset.next())
				{
					mail = new Mail();
					mail.setMessageId(id);
					mail.setSenderId(rset.getInt(1));
					mail.setSenderName(rset.getString(2));
					mail.setReceiverId(rset.getInt(3));
					mail.setReceiverName(rset.getString(4));
					mail.setExpireTime(rset.getInt(5));
					mail.setTopic(rset.getString(6));
					mail.setBody(rset.getString(7));
					mail.setPrice(rset.getLong(8));
					mail.setType(Mail.SenderType.VALUES[rset.getInt(9)]);
					mail.setUnread(rset.getBoolean(10));
					
					DbUtils.close(statement, rset);
					
					statement = con.prepareStatement(RESTORE_MAIL_ATTACHMENTS);
					statement.setInt(1, id);
					rset = statement.executeQuery();
					
					ItemInstance item;
					int objectId;
					while (rset.next())
					{
						objectId = rset.getInt(1);
						item = ItemsDAO.getInstance().load(objectId);
						if (item != null)
						{
							mail.addAttachment(item);
						}
					}
				}
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			
			load.incrementAndGet();
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch (SQLException e)
		{
			_log.error("Error while restoring mail : " + id, e);
			return null;
		}
		
		// cache.put(new Element(mail.getMessageId(), mail));
		_cache.put(mail.getMessageId(), mail);
		return mail;
	}
	
	public List<Mail> load(Collection<Integer> messageIds)
	{
		if (messageIds.isEmpty())
		{
			return Collections.emptyList();
		}
		
		List<Mail> list = new ArrayList<>(messageIds.size());
		
		Mail mail;
		for (Integer messageId : messageIds)
		{
			mail = load(messageId);
			if (mail != null)
			{
				list.add(mail);
			}
		}
		
		return list;
	}
	
	@Override
	public void save(Mail mail)
	{
		if (!mail.getJdbcState().isSavable())
		{
			return;
		}
		
		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(STORE_MAIL, Statement.RETURN_GENERATED_KEYS);
				statement.setInt(1, mail.getSenderId());
				statement.setString(2, mail.getSenderName());
				statement.setInt(3, mail.getReceiverId());
				statement.setString(4, mail.getReceiverName());
				statement.setInt(5, mail.getExpireTime());
				statement.setString(6, mail.getTopic());
				statement.setString(7, mail.getBody());
				statement.setLong(8, mail.getPrice());
				statement.setInt(9, mail.getType().ordinal());
				statement.setBoolean(10, mail.isUnread());
				statement.execute();
				
				rset = statement.getGeneratedKeys();
				rset.next();
				
				mail.setMessageId(rset.getInt(1));
				
				if (!mail.getAttachments().isEmpty())
				{
					DbUtils.close(statement);
					
					statement = con.prepareStatement(STORE_MAIL_ATTACHMENT);
					
					for (ItemInstance item : mail.getAttachments())
					{
						statement.setInt(1, mail.getMessageId());
						statement.setInt(2, item.getObjectId());
						statement.addBatch();
					}
					
					statement.executeBatch();
				}
				
				DbUtils.close(statement);
				
				if (mail.getType() == Mail.SenderType.NORMAL)
				{
					statement = con.prepareStatement(STORE_OWN_MAIL);
					statement.setInt(1, mail.getSenderId());
					statement.setInt(2, mail.getMessageId());
					statement.setBoolean(3, true);
					statement.execute();
				}
				
				DbUtils.close(statement);
				
				statement = con.prepareStatement(STORE_OWN_MAIL);
				statement.setInt(1, mail.getReceiverId());
				statement.setInt(2, mail.getMessageId());
				statement.setBoolean(3, false);
				statement.execute();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			
			insert.incrementAndGet();
			
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch (SQLException e)
		{
			_log.error("Error while saving mail!", e);
			return;
		}
		
		// cache.put(new Element(mail.getMessageId(), mail));
		_cache.put(mail.getMessageId(), mail);
	}
	
	@Override
	public void update(Mail mail)
	{
		if (!mail.getJdbcState().isUpdatable())
		{
			return;
		}
		
		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(UPDATE_MAIL);
				statement.setInt(1, mail.getSenderId());
				statement.setString(2, mail.getSenderName());
				statement.setInt(3, mail.getReceiverId());
				statement.setString(4, mail.getReceiverName());
				statement.setInt(5, mail.getExpireTime());
				statement.setString(6, mail.getTopic());
				statement.setString(7, mail.getBody());
				statement.setLong(8, mail.getPrice());
				statement.setInt(9, mail.getType().ordinal());
				statement.setBoolean(10, mail.isUnread());
				statement.setInt(11, mail.getMessageId());
				statement.execute();
				
				if (mail.getAttachments().isEmpty())
				{
					DbUtils.close(statement);
					
					statement = con.prepareStatement(REMOVE_MAIL_ATTACHMENTS);
					statement.setInt(1, mail.getMessageId());
					statement.execute();
				}
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			
			update.incrementAndGet();
			mail.setJdbcState(JdbcEntityState.STORED);
		}
		catch (SQLException e)
		{
			_log.error("Error while updating mail : " + mail.getMessageId(), e);
			return;
		}
		
		// cache.putIfAbsent(new Element(mail.getMessageId(), mail));
		_cache.putIfAbsent(mail.getMessageId(), mail);
	}
	
	@Override
	public void saveOrUpdate(Mail mail)
	{
		if (mail.getJdbcState().isSavable())
		{
			save(mail);
		}
		else if (mail.getJdbcState().isUpdatable())
		{
			update(mail);
		}
	}
	
	public static boolean hasUnreadMail(int receiverId, String mailTopic, String mailBody)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(message_id) as count from mail WHERE receiver_id=? AND unread=1 AND topic LIKE ? AND body LIKE ?"))
		{
			statement.setInt(1, receiverId);
			statement.setString(2, mailTopic);
			statement.setString(3, mailBody);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					return rset.getInt("count") > 0;
				}
			}
		}
		catch (SQLException e)
		{
			_log.error("Error while checking if " + receiverId + " has unread Mails with Topic: " + mailTopic, e);
		}
		return false;
	}
	
	@Override
	public void delete(Mail mail)
	{
		if (!mail.getJdbcState().isDeletable())
		{
			return;
		}
		
		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(REMOVE_MAIL);
				statement.setInt(1, mail.getMessageId());
				statement.execute();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
			
			delete.incrementAndGet();
			mail.setJdbcState(JdbcEntityState.DELETED);
		}
		catch (SQLException e)
		{
			_log.error("Error while deleting mail : " + mail.getMessageId(), e);
			return;
		}
		
		// cache.remove(mail.getExpireTime());
		_cache.remove(mail.getExpireTime());
	}
}
