//package l2r.gameserver.database;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import l2r.commons.dbcp.BasicDataSource;
//import l2r.gameserver.Config;
//
//public class DatabaseFactory extends BasicDataSource
//{
//	private static final DatabaseFactory _instance = new DatabaseFactory();
//	
//	public static final DatabaseFactory getInstance()
//	{
//		return _instance;
//	}
//	
//	public DatabaseFactory()
//	{
//		super(Config.DATABASE_DRIVER, Config.DATABASE_URL, Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_CONNECTIONS, Config.DATABASE_MAX_IDLE_TIMEOUT, Config.DATABASE_IDLE_TEST_PERIOD, false);
//	}
//	
//	@Override
//	public Connection getConnection() throws SQLException
//	{
//		return getConnection(null);
//	}
//}

package l2r.gameserver.database;

import l2r.commons.database.factory.AbstractDatabaseFactory;

public class DatabaseFactory extends AbstractDatabaseFactory
{
	
	private static final DatabaseFactory INSTANCE = new DatabaseFactory();

	public static DatabaseFactory getInstance()
	{
		return INSTANCE;
	}
	@Override
	protected String getConfigPath() {
		// TODO Auto-generated method stub
		return "config/database.properties";
	}
	
}