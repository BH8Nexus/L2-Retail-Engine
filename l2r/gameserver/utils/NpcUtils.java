/*
 *  Test  code 
 */
package l2r.gameserver.utils;

import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.taskmanager.tasks.GameObjectTask.DeleteTask;
import l2r.gameserver.templates.npc.NpcTemplate;

public class NpcUtils
{
	public static NpcInstance spawnSingle(int npcId, int x, int y, int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0);
	}
	
	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, despawnTime);
	}
	
	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.DEFAULT, despawnTime);
	}
	
	public static NpcInstance spawnSingle(int npcId, Location loc)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0);
	}
	
	public static NpcInstance spawnSingle(int npcId, Location loc, long despawnTime)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime);
	}
	
	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection)
	{
		return spawnSingle(npcId, loc, reflection, 0);
	}
	
	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if (template == null)
		{
			throw new NullPointerException("Npc template id : " + npcId + " not found!");
		}
		
		final NpcInstance npc = template.getNewInstance();
		npc.setHeading(loc.h < 0 ? Rnd.get(0xFFFF) : loc.h);
		npc.setSpawnedLoc(loc);
		npc.setReflection(reflection);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
		
		npc.spawnMe(npc.getSpawnedLoc());
		if (despawnTime > 0)
		{
			ThreadPoolManager.getInstance().schedule(new DeleteTask(npc), despawnTime);
		}
		return npc;
	}
	
	public static NpcInstance createOnePrivateEx(final int npcId, final int x, final int y, final int z, final int i0, final int i1)
	{
		return createOnePrivateEx(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0, null, null, i0, i1);
	}
	
	public static NpcInstance createOnePrivateEx(final int npcId, final Location loc, final Reflection reflection, final Creature arg, final int i0, final int i1)
	{
		return createOnePrivateEx(npcId, loc, reflection, 0, null, arg, i0, i1);
	}
	
	public static NpcInstance createOnePrivateEx(final int npcId, final int x, final int y, final int z, final Creature arg, final int i0, final int i1)
	{
		return createOnePrivateEx(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0, null, arg, i0, i1);
	}
	
	public static NpcInstance createOnePrivateEx(final int npcId, final Location loc, final Reflection reflection, final long despawnTime, final String title, final Creature arg, final int i0, final int i1)
	{
		final NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if (template == null)
		{
			throw new NullPointerException("Npc template id : " + npcId + " not found!");
		}
		final NpcInstance npc = template.getNewInstance();
		npc.setHeading(loc.h < 0 ? Rnd.get(0xFFFF) : loc.h);
		npc.setSpawnedLoc(loc);
		npc.setReflection(reflection);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
		if (title != null)
		{
			npc.setTitle(title);
		}
		npc.spawnMe(npc.getSpawnedLoc());
		npc.setParam2(i0);
		npc.setParam3(i1);
		if (arg != null)
		{
			npc.setParam4(arg);
		}
		if (despawnTime > 0)
		{
			ThreadPoolManager.getInstance().schedule(new DeleteTask(npc), despawnTime);
		}
		return npc;
	}
	
}