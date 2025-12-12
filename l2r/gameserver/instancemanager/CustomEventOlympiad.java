package l2r.gameserver.instancemanager;

import l2r.gameserver.Announcements;
import l2r.gameserver.scripts.Functions;

public class CustomEventOlympiad extends Functions
{	
	private static final String spawngroup = "olympiad_manager";
	private  static CustomEventOlympiad	 _instance = new CustomEventOlympiad();
	private static boolean _spawnd = false;
	private static int Baium = 29020;
	//private static int olympiadManager = 31688;
	//private static ArrayList<SimpleSpawner> _spawnolympiad = new ArrayList<>(); 
	
	
	public static CustomEventOlympiad getInstance()
	{
		return _instance;
	}
	
	public void doSpawn(int bossIs)
	{
		if(bossIs == Baium)
		{
			Announcements.getInstance().announceToAll("Olympiad Manager Spawn");
		}
		
		
		if(_spawnd)
		{
			return;
		}
		
		_spawnd = true;
		//spawnOlympiadmanager();
		SpawnManager.getInstance().spawn(spawngroup);
	}
	
//	private void spawnOlympiadmanager()
//	{
//		final int OLYMPIAD[][] = {
//				{-85151,241476,-3730,25000 },
//				{-84816,151056,-3127,0}
//		};
//		SpawnNPCs(olympiadManager,OLYMPIAD,_spawnolympiad);
//	}
}
