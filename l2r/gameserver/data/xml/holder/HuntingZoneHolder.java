package l2r.gameserver.data.xml.holder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.Location;

/**
 * @author VISTALL
 * @date 22:21/09.03.2011
 */
public final class HuntingZoneHolder extends AbstractHolder
{
	private static final HuntingZoneHolder _instance = new HuntingZoneHolder();
	
	private final Map<Integer, HuntingZone> _huntingZones = new HashMap<>(290);
	
	public static HuntingZoneHolder getInstance()
	{
		return _instance;
	}
	
	public void addHuntingZone(HuntingZone zone)
	{
		_huntingZones.put(zone.getId(), zone);
	}
	
	public HuntingZone getHuntingZone(int id)
	{
		return _huntingZones.get(id);
	}
	
	public Collection<HuntingZone> getHuntingZones()
	{
		return _huntingZones.values();
	}
	
	@Override
	public int size()
	{
		return _huntingZones.size();
	}
	
	@Override
	public void clear()
	{
		_huntingZones.clear();
	}
	
	public static class HuntingZone
	{
		private final int _id;
		private final String _name;
		private final int _level;
		private final Location _loc;
		private final int _masterAreaId;
		
		public HuntingZone(StatsSet set)
		{
			_id = set.getInteger("id");
			_name = set.getString("name");
			_level = set.getInteger("level");
			_loc = new Location(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
			_masterAreaId = set.getInteger("affiliated_area_id");
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public Location getLoc()
		{
			return _loc;
		}
		
		/**
		 * @return The ID of the hunting zone this hunting zone is inside.
		 */
		public int getMasterAreaId()
		{
			return _masterAreaId;
		}
	}
}
