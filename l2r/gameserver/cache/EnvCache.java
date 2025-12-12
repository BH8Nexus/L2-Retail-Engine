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
package l2r.gameserver.cache;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

import l2r.gameserver.stats.Env;

/**
 * @author UnAfraid
 */
public class EnvCache
{
	private final Deque<Env> _cache = new ConcurrentLinkedDeque<>();
	private final static EnvCache _instance = new EnvCache();
	
	public final static EnvCache getInstance()
	{
		return _instance;
	}
	
	protected EnvCache()
	{
	}
	
	public void recycle(Env env)
	{
		_cache.offer(env);
	}
	
	public Env pull()
	{
		return pullOrDefault(Env::new);
	}
	
	public Env pullOrDefault(Supplier<? extends Env> supplier)
	{
		final Env env = _cache.poll();
		return env != null ? env : supplier.get();
	}
}
