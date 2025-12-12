package l2r.gameserver.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.network.AccountData;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.AccountDataRequest;
import l2r.gameserver.utils.Util;

public class AccountsDAO
{
	private static final Logger _log = LoggerFactory.getLogger(AccountsDAO.class);
	private static Map<String, AccountData> _accountData = new ConcurrentHashMap<>();
	
	/*
	 * private static final AccountsDAO _instance = new AccountsDAO(); public static AccountsDAO getInstance() { return _instance; }
	 */
	
	/**
	 * Checks the server if it holds data for the given account, if not, it returns the default (AccountData.DUMMY). Only accounts who are logged in at least once in the server since the last restart are stored here.
	 */
	public static AccountData getAccountData(String accountName)
	{
		AccountData data = _accountData.get(accountName);
		if (data == null)
		{
			data = AccountData.DUMMY;
		}
		
		return data;
	}
	
	/**
	 * Checks the server if it holds data for the given account, if not, it waits the given time in miliseconds for the loginserver to answer with the account data packet. It usually happens in <10ms if the loginserver is local. Suggested usage for accounts who haven't logged in.
	 */
	public static AccountData getAccountData(String accountName, int timeoutInMilis)
	{
		AccountData data = _accountData.get(accountName);
		if (data == null)
		{
			AuthServerCommunication.getInstance().sendPacket(new AccountDataRequest(accountName));
			
			// Make at least 1 iteration happen and do not wait more than 5secs LOL!
			Util.constrain(timeoutInMilis, 1, 5000);
			
			// Wait until the request answer comes. The packet should automatically update the map via setAccountData.
			while ((timeoutInMilis > 0) && !_accountData.containsKey(accountName))
			{
				try
				{
					Thread.sleep(10);
				} // Sleep 10 milisec
				catch (InterruptedException e)
				{
					_log.error("AccountsDAO: ", e);
				}
				timeoutInMilis -= 10;
			}
			
			data = _accountData.get(accountName);
		}
		
		if (data == null)
		{
			data = AccountData.DUMMY;
		}
		
		return data;
	}
	
	public static void setAccountData(String accountName, AccountData data)
	{
		_accountData.put(accountName, data);
	}
	
	public static void deleteAccountData(String accountName)
	{
		_accountData.remove(accountName);
	}
}
