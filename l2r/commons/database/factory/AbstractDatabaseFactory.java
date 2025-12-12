package l2r.commons.database.factory;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public abstract class AbstractDatabaseFactory
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private HikariDataSource connectionPool;
	
	
	public void doStart()
	{
		try
		{
			String configPath = getConfigPath();
			HikariConfig config = new HikariConfig(configPath);
			this.connectionPool = new HikariDataSource(config);
			this.logger.info("Database Connection working.");
//			this.logger.warn("Database Connection working.");
//			logger.debug("Database Connection working.");
		}
		catch(RuntimeException e)
		{
			this.logger.warn("Could not init database Connection");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void doStop()
	{
		this.connectionPool.shutdown();
	}
	
	public Connection getConnection()
	{
		try
		{
			return this.connectionPool.getConnection();
		}
		catch(SQLException e)
		{
			this.logger.warn("Can't get Connection from database",e);
			return null;
		}
	}
	
	public HikariDataSource getConnectionPool()
	{
		return this.connectionPool;
	}
	
	protected abstract String getConfigPath();
}
