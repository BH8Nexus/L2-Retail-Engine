package l2r.commons.math;

import gnu.trove.map.hash.TIntObjectHashMap;

public class TIntStringHashMap extends TIntObjectHashMap<String>
{
    public String getNotNull(int key)
    {
        String value = get(key);
        return value == null ? "" : value;
    }
}
