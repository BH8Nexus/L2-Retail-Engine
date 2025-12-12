package l2r.gameserver.taskmanager;

import java.util.concurrent.Future;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.threading.SteppingRunnableQueueManager;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.premium.PremiumEnd;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();
	
	public static final LazyPrecisionTaskManager getInstance()
	{
		return _instance;
	}
	
	private LazyPrecisionTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);

		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				LazyPrecisionTaskManager.this.purge();
			}
			
		}, 60000L, 60000L);
	}
	
	public Future<?> addPCCafePointsTask(final Player player)
	{
		long delay = Config.ALT_PCBANG_POINTS_DELAY * 60000L;
		
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl()
			{
				if (player.isInOfflineMode() || (player.getLevel() < Config.ALT_PCBANG_POINTS_MIN_LVL) || player.isInJail())
				{
					return;
				}
				
				player.addPcBangPoints(Config.ALT_PCBANG_POINTS_BONUS, (Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0) && Rnd.chance(Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE));
			}
			
		}, delay, delay);
	}
	
	public Future<?> addVitalityRegenTask(final Player player)
	{
		long delay = 60000L;
		
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl()
			{
				if (player.isInOfflineMode() || !player.isInPeaceZone() || player.isInJail())
				{
					return;
				}
				
				player.setVitality(player.getVitality() + 1.0D);
			}
			
		}, delay, delay);
	}
	
	public Future<?> addNpcAnimationTask(final NpcInstance npc)
	{
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl()
			{
				if (npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving && !npc.isInCombat())
				{
					npc.onRandomAnimation();
				}
			}
			
		}, 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
	}
	
	public Future<?> startBonusExpirationTask(Player player)
	{
		long delay = (player.getBonus().getBonusExpire() * 1000L) - System.currentTimeMillis();
		
		return schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl()
			{
				PremiumEnd.getInstance().stopBonuses(player);
			}
			
		}, delay);
	}
}
