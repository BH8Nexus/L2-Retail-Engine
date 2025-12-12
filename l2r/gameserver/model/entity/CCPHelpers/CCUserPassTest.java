/*
 * Copyright (C) 2004-2020 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.entity.CCPHelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;

/**
 * @author Flash
 */
public class CCUserPassTest
{
	
	public static void startUserPass(Player player, String text)
	{
		StringTokenizer st = new StringTokenizer(text, "|");
		String[] args = new String[st.countTokens()];
		for (int i = 0; i < args.length; i++)
		{
			args[i] = st.nextToken().trim();
		}
		
		String pageIndex = args[0].substring(args[0].length() - 1);
		if (pageIndex.equals("F"))
		{
			if (hasUserPass(player))
			{
				
			}
		}
	}
	
	public static boolean hasUserPass(Player player)
	{
		String user = getSecondaryUser(player);
		if ((user != null) && (user.length() > 0))
		{
			return true;
		}
		return false;
	}
	
	public static String getSecondaryUser(Player player)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT secondaryPassword FROM accounts WHERE login='" + player.getAccountName() + "'");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				return rset.getString("secondaryPassword");
			}
		}
		catch (SQLException e)
		{
			// TODO: handle exception
		}
		return null;
	}
	
}
