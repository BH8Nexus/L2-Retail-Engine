package l2r.loginserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.net.nio.impl.SelectorConfig;
import l2r.commons.net.nio.impl.SelectorStats;
import l2r.commons.net.nio.impl.SelectorThread;
import l2r.loginserver.database.L2DatabaseFactory;
import l2r.loginserver.gameservercon.GameServerCommunication;

public class AuthServer
{
	private static final Logger _log = LoggerFactory.getLogger(AuthServer.class);
	
	public static final int AUTH_SERVER_PROTOCOL = 3;
	
	private static AuthServer authServer;
	
	private final GameServerCommunication _gameServerListener;
	private final SelectorThread<L2LoginClient> _selectorThread;
	
	public static AuthServer getInstance()
	{
		return authServer;
	}
	
	public AuthServer() throws Throwable
	{
		Config.initCrypt();
		GameServerManager.getInstance();
		
		L2LoginPacketHandler lph = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig sc = new SelectorConfig();
		sc.AUTH_TIMEOUT = Config.LOGIN_TIMEOUT;
		SelectorStats sts = new SelectorStats();
		_selectorThread = new SelectorThread<>(sc, sts, lph, sh, sh, sh);
		
		_gameServerListener = GameServerCommunication.getInstance();
		_gameServerListener.openServerSocket(Config.GAME_SERVER_LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST), Config.GAME_SERVER_LOGIN_PORT);
		_gameServerListener.start();
		_log.info("Listening for gameservers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		
		_selectorThread.openServerSocket(Config.LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.LOGIN_HOST), Config.PORT_LOGIN);
		_selectorThread.start();
		_log.info("Listening for clients on " + Config.LOGIN_HOST + ":" + Config.PORT_LOGIN);
	}
	
	public GameServerCommunication getGameServerListener()
	{
		return _gameServerListener;
	}
	
	public static void checkFreePorts() throws Throwable
	{
		try (ServerSocket ss = (Config.LOGIN_HOST.equalsIgnoreCase("*") ? new ServerSocket(Config.PORT_LOGIN) : new ServerSocket(Config.PORT_LOGIN, 50, InetAddress.getByName(Config.LOGIN_HOST))))
		{
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Port [" + Config.PORT_LOGIN + "] is already in use!");
		}
	}
	
	public static void main(String[] args) throws Throwable
	{
		new File("./log/").mkdir();
		// Initialize config
		Config.load();
		// Check binding address
		checkFreePorts();
		Class.forName(Config.class.getName());
		// Initialize database
		//Class.forName(Config.DATABASE_DRIVER).newInstance();
		//L2DatabaseFactory.getInstance().getConnection().close();
		L2DatabaseFactory.getInstance().doStart();
		
		authServer = new AuthServer();
	}
}