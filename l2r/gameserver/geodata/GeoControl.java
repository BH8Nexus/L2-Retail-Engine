package l2r.gameserver.geodata;

import java.util.HashMap;

import l2r.commons.geometry.Polygon;
import l2r.gameserver.model.entity.Reflection;

public interface GeoControl
{
	public abstract Polygon getGeoPos();
	
	public abstract HashMap<Long, Byte> getGeoAround();

	public abstract void setGeoAround(HashMap<Long, Byte> value);

	public abstract Reflection getReflection();

	public boolean isGeoCloser();
}