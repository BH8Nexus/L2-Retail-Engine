//package l2r.loginserver.database;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import l2r.commons.dbcp.BasicDataSource;
//import l2r.loginserver.Config;
//
//public class L2DatabaseFactory extends BasicDataSource
//{
//	private static final L2DatabaseFactory _instance = new L2DatabaseFactory();
//	
//	public static final L2DatabaseFactory getInstance()
//	{
//		return _instance;
//	}
//	
//	public L2DatabaseFactory()
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
package l2r.loginserver.database;

import l2r.commons.database.factory.AbstractDatabaseFactory;

public  class L2DatabaseFactory extends AbstractDatabaseFactory
{
	
	private static final L2DatabaseFactory INSTANCE = new L2DatabaseFactory();

	
	public static final L2DatabaseFactory getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	protected String getConfigPath() {
		// TODO Auto-generated method stub
		return "config/database.properties";
	}
	
}