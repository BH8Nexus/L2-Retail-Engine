package l2r.gameserver.network;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import l2r.gameserver.custom.FloodProtectorConfig;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.ChangeAccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FloodProtector
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FloodProtector.class);

	private final GameClient _client;
	private final FloodProtectorConfig _confing;
	private volatile long _nextTime = System.currentTimeMillis();
	private final AtomicInteger  _count = new AtomicInteger(0);
	private boolean _logged;
	private volatile boolean _punishmentInProgress;
	
	
	public FloodProtector(GameClient client, FloodProtectorConfig config)
	{
		_client = client;
		_confing = config;
	}
	
	public boolean tryPerformAction(String command)
	{
		long curTime = System.currentTimeMillis();

		if (_client.getActiveChar() != null) //&& _Client.getActiveChar().getplayer)
		{
			return true;
		}

		if (curTime < _nextTime || _punishmentInProgress)
		{
			if (_confing.LOG_FLOODING && !this._logged)
			{
				/*
				 *  Log
				 */
				log("called command ", command, " ~", String.valueOf(this._confing.FLOOD_PROTECTION_INTERVAL - this._nextTime - curTime), " ms after previous command");

				_logged = true;
			}

			_count.incrementAndGet();
			if (_punishmentInProgress && _confing.PUNISHMENT_LIMIT > 0 && _count.get() >= _confing.PUNISHMENT_LIMIT && _confing.PUNISHMENT_TYPE != null)
			{
				_punishmentInProgress = true;

				switch (_confing.PUNISHMENT_TYPE) {
					case "kick":
						/*
						 *   kickPlayer();
						 */
						kickPlayer();
						break;
					case "ban":
						/*
						 *  banAccount();
						 */
						banAccount();
						break;
					case "jail":
						/*
						 *  jailChar();
						 */
						JailChar();

						break;
				}
				_punishmentInProgress = false;
			}
			return false;
		}

		if (_count.get() > 0)
		{
			if (_confing.LOG_FLOODING)
			{
				/*
				 * Log
				 */
				log("issued ", String.valueOf(this._count), " extra requests within ~", String.valueOf(this._confing.FLOOD_PROTECTION_INTERVAL), " ms");
			}
		}
		_nextTime = curTime + _confing.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		_count.set(0);

		return true;
	}
	
	private void kickPlayer()
	{
		Player player = _client.getActiveChar();
		if (player != null)
		{
			player.kick();
			/*
			 * log
			 */
			log("kicked for flooding");
		}
	}

	private void banAccount()
	{
		int accessLevel = 0;
		int banExpire = 0;
		
		if (_confing.PUNISHMENT_TIME > 0L)
		{
			banExpire = (int)(System.currentTimeMillis() + _confing.PUNISHMENT_TIME / 1000L);
		}
		else {
			accessLevel = -100;
		}
		
		AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(_client.getLogin(), accessLevel, banExpire));
		Player player = _client.getActiveChar();
		
		if (player != null)
		{
			player.kick();
			
			/*
			 *  log 
			 */
			log("banned for flooding ", (this._confing.PUNISHMENT_TIME <= 0L) ? "forever" : ("for " + (this._confing.PUNISHMENT_TIME / 60000L) + " mins"));
		}
	}
	
	private void JailChar()
	{
		Player player = _client.getActiveChar();
		
		if (player != null)
		{
			//(player.getVar("") != null) && (_confing.PUNISHMENT_TIME / 60000L);
			log("jailed for flooding ", (this._confing.PUNISHMENT_TIME <= 0L) ? "forever" : ("for " + (this._confing.PUNISHMENT_TIME / 60000L) + " mins"));
		}
	}


	private void log(String... lines)
	{
		String address;
		StringBuilder output = new StringBuilder(100);
		output.append(_confing.FLOOD_PROTECTOR_TYPE);
		output.append(": ");

		switch (this._client.getState())
		{
			case IN_GAME:
				if (_client.getActiveChar() != null)
				{
					output.append(_client.getActiveChar().getName());
					output.append("(");
					output.append(_client.getActiveChar().getObjectId());
					output.append(")");
				}
				break;
			case AUTHED:
				if (_client.getLogin() != null)
				{
					output.append(_client.getLogin());
					output.append(" ");
				}
				break;
			case CONNECTED:
				address = _client.getIpAddr();
				if (address != null)
				{
					output.append(address);
					output.append(" ");
				}
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}
		Arrays.stream(lines).forEach(output::append);
		LOGGER.info(output.toString());
	}
}
