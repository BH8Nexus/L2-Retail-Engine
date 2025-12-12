package l2r.gameserver.model.base;

public enum RestartType
{
	TO_VILLAGE,
	TO_CLANHALL,
	TO_CASTLE,
	TO_FORTRESS,
	TO_FLAG,
	FIXED,
	AGATHION,
	JAIL,
	SWEEPABLE,
	SPECIAL;

	public static final RestartType[] VALUES = values();
	
	private int _specialRestartType;
	
	public void setSpecialRestartType(int itemId)
	{
		_specialRestartType = itemId;
	}
	
	public int getSpecialRestartType()
	{
		return _specialRestartType;
	}
}
