package l2r.gameserver.handler.vote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;

public abstract class VoteBase
{
	public String getPlayerIp(Player player)
	{
		return player.getIP();
		// return player.getClient().getConnection().getInetAddress().getHostAddress();
	}
	
	public abstract void reward(Player player);
	
	public abstract void setVoted(Player player);
	
	public void updateDB(Player player, String columnName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(String.format("UPDATE accounts_vote set %s=? where vote_ip=?", columnName));
			statement.setLong(1, System.currentTimeMillis());
			statement.setString(2, getPlayerIp(player));
			statement.execute();
			// statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error in VoteBase::updateDB");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public boolean hasVoted(Player player)
	{
		try
		{
			String endpoint = getApiEndpoint(player);
			if (endpoint.startsWith("err"))
			{
				return false;
			}
			
			String voted = getApiResponse(endpoint);
			
			return tryParseBool(voted);
		}
		catch (Exception e)
		{
			player.sendMessage("Something went wrong. Please try again later.");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean tryParseBool(String bool)
	{
		if (bool.startsWith("1"))
		{
			return true;
		}
		else if (bool.startsWith("{\"apiver\":\"0.1c\",\"voted\":true"))
		{
			return true;
		}
		else if (bool.startsWith("{\"ok\":true,\"error_code\":0,\"description\":\"\",\"result\":{\"isVoted\":true"))
		{
			return true;
		}
		else if (bool.contains("<status>1</status>"))
		{
			return true;
		}
		
		return Boolean.parseBoolean(bool.trim());
	}
	
	public abstract String getApiEndpoint(Player player);
	
	public String getApiResponse(String endpoint)
	{
		StringBuilder stringBuilder = new StringBuilder();
		
		try
		{
			URL url = new URL(endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			connection.setReadTimeout(5 * 1000);
			connection.connect();
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					stringBuilder.append(line + "\n");
				}
			}
			catch (NullPointerException e)
			{
				System.out.println("Votebase.java: read error");
			}
			
			connection.disconnect();
			
			// System.out.println(stringBuilder.toString());
			
			return stringBuilder.toString();
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong in VoteBase::getApiResponse");
			e.printStackTrace();
			
			return "err";
		}
	}
}