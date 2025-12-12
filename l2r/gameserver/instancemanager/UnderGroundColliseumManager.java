package l2r.gameserver.instancemanager;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.gameserver.data.xml.holder.ZoneHolder;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.entity.Coliseum;

// TODO: zones for underground coliseum, restart points, spawn points etc.
// TODO: unregister and safe checks on register.
public class UnderGroundColliseumManager
{
	private static final Logger LOG = LoggerFactory.getLogger(UnderGroundColliseumManager.class);
	
	private static UnderGroundColliseumManager _instance;
	
	private HashMap<String, Coliseum> _coliseums;
	
	public static UnderGroundColliseumManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new UnderGroundColliseumManager();
		}
		return _instance;
	}
	
	public UnderGroundColliseumManager()
	{
		List<Zone> zones = ZoneHolder.getZonesByType(ZoneType.UnderGroundColiseum);
		if (zones.size() == 0)
		{
			LOG.warn("Not found zones for UnderGround Colliseum!!!");
		}
		else
		{
			for (Zone zone : zones)
			{
				getColiseums().put(zone.getName(), new Coliseum());
			}
		}
		
		LOG.warn("Loaded: " + getColiseums().size() + " UnderGround Colliseums.");
	}
	
	public HashMap<String, Coliseum> getColiseums()
	{
		if (_coliseums == null)
		{
			_coliseums = new HashMap<>();
		}
		return _coliseums;
	}
	
	public Coliseum getColiseumByLevelLimit(final int limit)
	{
		for (Coliseum coliseum : _coliseums.values())
		{
			if (coliseum.getMaxLevel() == limit)
			{
				return coliseum;
			}
		}
		return null;
	}
}