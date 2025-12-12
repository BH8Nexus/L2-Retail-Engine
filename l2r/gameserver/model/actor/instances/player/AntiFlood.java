package l2r.gameserver.model.actor.instances.player;

import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

public class AntiFlood
{
	private final TIntLongMap _recentReceivers;
	private final TIntLongMap _recentInviteAcademy;
	private final Player _owner;
	private long _lastSent;
	private String _lastText;
	private long _allChatUseTime;
	private long _shoutChatUseTime;
	private long _tradeChatUseTime;
	private long _heroChatUseTime;
	private long _privateChatUseTime;
	private long _mailUseTime;
	private long _lastLfcTime;
	private long _lastPingTime;
	private long _lastFindPartyTime;
	private long _lastRequestedCaptcha;
	private long _lastAcademyRegTime;
	
	public AntiFlood(final Player owner)
	{
		_recentReceivers = new TIntLongHashMap();
		_recentInviteAcademy = new TIntLongHashMap();
		_lastSent = 0L;
		_lastText = "";
		_owner = owner;
	}
	
	public boolean canAll(final String text)
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.ALL_CHAT_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.all.level", _owner, new Object[0]).add(Config.ALL_CHAT_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.ALL_CHAT_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_allChatUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.all.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_allChatUseTime = currentMillis + (Config.ALL_CHAT_USE_DELAY * 1000L);
		}
		return true;
	}
	
	public boolean canShout(String text)
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.SHOUT_CHAT_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.shout.level", _owner, new Object[0]).add(Config.SHOUT_CHAT_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.SHOUT_CHAT_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_shoutChatUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.shout.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_shoutChatUseTime = currentMillis + (Config.SHOUT_CHAT_USE_DELAY * 1000L);
		}
		return true;
	}
	
	public boolean canShout()
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.SHOUT_CHAT_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.shout.level", _owner, new Object[0]).add(Config.SHOUT_CHAT_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.SHOUT_CHAT_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_shoutChatUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.shout.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_shoutChatUseTime = currentMillis + (Config.SHOUT_CHAT_USE_DELAY * 1000L);
		}
		return true;
	}
	
	public boolean canTrade(final String text, boolean Premium)
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.TRADE_CHAT_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.trade.level", _owner, new Object[0]).add(Config.TRADE_CHAT_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.TRADE_CHAT_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_tradeChatUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.trade.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_tradeChatUseTime = currentMillis + (Config.TRADE_CHAT_USE_DELAY * 1000L);
		}
		return true;
	}
	
	public boolean canHero(final String text)
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.HERO_CHAT_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.hero.level", _owner, new Object[0]).add(Config.HERO_CHAT_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.HERO_CHAT_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_heroChatUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.hero.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_heroChatUseTime = currentMillis + (Config.HERO_CHAT_USE_DELAY * 1000L);
		}
		return true;
	}
	
	public boolean canMail()
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.MAIL_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_mail.level", _owner, new Object[0]).add(Config.MAIL_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.MAIL_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_mailUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_mail.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_mailUseTime = currentMillis + (Config.MAIL_USE_DELAY * 1000L);
		}
		return true;
	}
	
	public boolean canRegisterForAcademy()
	{
		long currentMillis = System.currentTimeMillis();
		
		if ((currentMillis - _lastAcademyRegTime) < (5 * 1000))
		{
			return false;
		}
		
		_lastAcademyRegTime = currentMillis;
		return true;
	}
	
	public boolean canInviteInAcademy(int charId)
	{
		long currentMillis = System.currentTimeMillis();
		long lastSent;
		int lastChar;
		
		TIntLongIterator itr = _recentInviteAcademy.iterator();
		
		while (itr.hasNext())
		{
			itr.advance();
			lastChar = itr.key();
			lastSent = itr.value();
			
			if ((lastChar == charId) && ((currentMillis - lastSent) < (Config.ACADEMY_INVITE_DELAY * 60 * 1000)))
			{
				return false;
			}
		}
		
		lastSent = _recentInviteAcademy.put(charId, currentMillis);
		return true;
	}
	
	public boolean canTell(final int receiverId, final String text)
	{
		if (_owner.isGM())
		{
			return true;
		}
		if (Config.PRIVATE_CHAT_USE_MIN_LEVEL > _owner.getLevel())
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.private.level", _owner, new Object[0]).add(Config.PRIVATE_CHAT_USE_MIN_LEVEL).toString());
			return false;
		}
		if (Config.PRIVATE_CHAT_USE_DELAY > 0)
		{
			final long currentMillis = System.currentTimeMillis();
			final int delay = (int) ((_privateChatUseTime - currentMillis) / 1000L);
			if (delay > 0)
			{
				_owner.sendMessage(new CustomMessage("antispam.no_chat.private.delay", _owner, new Object[0]).add(delay).toString());
				return false;
			}
			_privateChatUseTime = currentMillis + (Config.PRIVATE_CHAT_USE_DELAY * 1000L);
		}
		final long currentMillis = System.currentTimeMillis();
		final TIntLongIterator itr = _recentReceivers.iterator();
		int recent = 0;
		while (itr.hasNext())
		{
			itr.advance();
			final long lastSent = itr.value();
			if ((currentMillis - lastSent) < (text.equalsIgnoreCase(_lastText) ? 600000L : 60000L))
			{
				++recent;
			}
			else
			{
				itr.remove();
			}
		}
		long lastSent = _recentReceivers.put(receiverId, currentMillis);
		long delay2 = 333L;
		if (recent > 3)
		{
			lastSent = _lastSent;
			delay2 = (recent - 3) * 3333L;
		}
		_lastText = text;
		_lastSent = currentMillis;
		final int remainingDelay = (int) ((delay2 - (currentMillis - lastSent)) / 1000L);
		if (remainingDelay > 0)
		{
			_owner.sendMessage(new CustomMessage("antispam.no_chat.private.delay", _owner, new Object[0]).add(remainingDelay).toString());
			return false;
		}
		return true;
	}
	
	public boolean canLfcChoose()
	{
		final long currentMillis = System.currentTimeMillis();
		if ((currentMillis - _lastLfcTime) < 180000L)
		{
			return false;
		}
		_lastLfcTime = currentMillis;
		return true;
	}
	
	public boolean canFindParty()
	{
		long currentMillis = System.currentTimeMillis();
		
		if ((currentMillis - _lastFindPartyTime) < (Config.PARTY__DELAY_TIME * 1000))
		{
			return false;
		}
		
		_lastFindPartyTime = currentMillis;
		return true;
	}
	
	public boolean canPing()
	{
		long currentMillis = System.currentTimeMillis();
		
		if ((currentMillis - _lastPingTime) < 10000L)
		{
			return false;
		}
		
		_lastPingTime = currentMillis;
		return true;
	}
	
	public boolean canRequestCaptcha()
	{
		long currentMillis = System.currentTimeMillis();
		
		if ((currentMillis - _lastRequestedCaptcha) < (5 * 1000))
		{
			return false;
		}
		
		_lastRequestedCaptcha = currentMillis;
		return true;
	}
}
