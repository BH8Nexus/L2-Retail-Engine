package l2r.commons.net.nio.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MMOSocket
{
	private final Socket _socket;
	
	public static void getInstance()
	{
	}
	
	public MMOSocket(Socket socket, byte[] addr)
	{
		_socket = socket;
	}
	
	public InetAddress getLocalAddress()
	{
		return _socket.getLocalAddress();
	}
	
	public InetAddress getInetAddress()
	{
		return _socket.getInetAddress();
	}
	
	public void close() throws IOException
	{
		_socket.close();
	}
}
