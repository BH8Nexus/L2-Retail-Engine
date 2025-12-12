/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.commons.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author UnAfraid
 */
public class ServerNetworkConfiguration
{
	private int _id;
	private final InetSocketAddress _listenAddress;
	private final InetAddress _externalAddress;
	
	private final List<String> _subnets = new ArrayList<>();
	private final List<String> _hosts = new ArrayList<>();
	
	private final List<GameServerAddress> _hostnames = new ArrayList<>();
	
	public ServerNetworkConfiguration(int id, InetSocketAddress listenAddress, InetAddress externalAddress)
	{
		_id = id;
		_listenAddress = listenAddress;
		_externalAddress = externalAddress;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public InetSocketAddress getListenAddress()
	{
		return _listenAddress;
	}
	
	public InetAddress getExternalAddress()
	{
		return _externalAddress;
	}
	
	public List<String> getHostnames()
	{
		return _hosts;
	}
	
	public List<String> getSubnets()
	{
		return _subnets;
	}
	
	public void rebuildHostnames()
	{
		_hostnames.clear();
		for (int i = 0; i < getHostnames().size(); i++)
		{
			try
			{
				_hostnames.add(new GameServerAddress(getSubnets().get(i), InetAddress.getByName(getHostnames().get(i))));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gets the server address.
	 * @param addr the addr
	 * @return the server address
	 */
	@SuppressWarnings("unlikely-arg-type")
	public InetAddress getServerAddress(String addr)
	{
		InetAddress address = null;
		try
		{
			address = InetAddress.getByName(addr);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		for (GameServerAddress a : _hostnames)
		{
			if (a.equals(address))
			{
				return a.getServerAddress();
			}
		}
		// Shouldn't happen!
		return null;
	}
	
	/**
	 * The Class GameServerAddress.
	 */
	private class GameServerAddress extends IPSubnet
	{
		private final InetAddress _address;
		
		/**
		 * Instantiates a new game server address.
		 * @param subnet the subnet
		 * @param address the address
		 * @throws UnknownHostException the unknown host exception
		 */
		public GameServerAddress(String subnet, InetAddress address) throws UnknownHostException
		{
			super(subnet);
			_address = address;
		}
		
		/**
		 * Gets the server address.
		 * @return the server address
		 */
		public InetAddress getServerAddress()
		{
			return _address;
		}
		
		@Override
		public String toString()
		{
			return _address + super.toString();
		}
	}
}