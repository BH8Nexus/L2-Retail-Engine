package l2r.gameserver.network.loginservercon;

public enum ServerType
{
	NORMAL,
	RELAX,
	TEST,
	BROAD,
	RESTRICTED,
	EVENT,
	FREE,
	WORLD, // The accross server which holds dimensional siege.
	NEW,
	CLASSIC;
	
	private int _mask;
	
	ServerType()
	{
		_mask = 1 << ordinal();
	}
	
	public int getMask()
	{
		return _mask;
	}
}
