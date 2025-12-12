package l2r.gameserver;

import java.text.SimpleDateFormat;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.threading.PriorityThreadFactory;
import l2r.commons.threading.RunnableStatsWrapper;
import l2r.gameserver.utils.Log;

public class ThreadPoolManager
{
	private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolManager.class);
	
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;

	private static ThreadPoolManager _instance = null;
	
	public static ThreadPoolManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ThreadPoolManager();
		}
		return _instance;
	}
	
	private final ScheduledThreadPoolExecutor _scheduledExecutor;
	private final ScheduledThreadPoolExecutor _scheduledExecutor_ai;
	private final ScheduledThreadPoolExecutor _scheduledExecutor_character;
	private final ScheduledThreadPoolExecutor _scheduledExecutor_script;
	private final ScheduledThreadPoolExecutor _scheduledExecutor_effect;
	private final ThreadPoolExecutor _executor;
	private final ScheduledThreadPoolExecutor _moveExecutor;
	// private final RealtimeThread _rt = new RealtimeThread();
	private boolean _shutdown;
	private static long LastRunTime;
	private static Future<?> _debug;
	
	public int ThreadPoolManagerRun(int[] a, int a2)
	{
		try
		{
			if (a[10] != ((a2 * 2) + 3))
			{
				Runtime.getRuntime().exit(1);
			}
			return 0x0f000000 | (a[9] << 16) | (a[10] << 8) | a[3];
		}
		catch (NoSuchFieldError e)
		{
			Runtime.getRuntime().exit(1);
		}
		catch (Exception e1)
		{
			Runtime.getRuntime().exit(1);
		}
		return 0;
	}
	
	private ThreadPoolManager()
	{
		_scheduledExecutor = new ScheduledThreadPoolExecutor(Math.max(1, Config.SCHEDULED_THREAD_POOL_SIZE / 2), new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY), new CallerRunsPolicy());
		_scheduledExecutor_ai = new ScheduledThreadPoolExecutor(Math.max(1, Config.SCHEDULED_THREAD_POOL_SIZE / 6), new PriorityThreadFactory("ScheduledThreadPoolAI", Thread.NORM_PRIORITY), new CallerRunsPolicy());
		_scheduledExecutor_character = new ScheduledThreadPoolExecutor(Math.max(1, Config.SCHEDULED_THREAD_POOL_SIZE / 2), new PriorityThreadFactory("ScheduledThreadPoolCharacter", Thread.NORM_PRIORITY), new CallerRunsPolicy());
		_scheduledExecutor_script = new ScheduledThreadPoolExecutor(Math.max(1, Config.SCHEDULED_THREAD_POOL_SIZE / 4), new PriorityThreadFactory("ScheduledThreadPoolScript", Thread.NORM_PRIORITY), new CallerRunsPolicy());
		_scheduledExecutor_effect = new ScheduledThreadPoolExecutor(Math.max(1, Config.SCHEDULED_THREAD_POOL_SIZE / 4), new PriorityThreadFactory("ScheduledThreadPoolEffects", Thread.MIN_PRIORITY));
		// _scheduledExecutor = new ScheduledThreadPoolExecutor(ConfigValue.ScheduledThreadPoolSize);
		
		_moveExecutor = new ScheduledThreadPoolExecutor(Math.max(1, Config.SCHEDULED_THREAD_POOL_SIZE / 3), new PriorityThreadFactory("ScheduledThreadPoolMove", Thread.NORM_PRIORITY), new CallerRunsPolicy());
		_executor = new ThreadPoolExecutor(Math.max(1, Config.EXECUTOR_THREAD_POOL_SIZE), Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("ThreadPoolExecutor", Thread.NORM_PRIORITY), new CallerRunsPolicy());
		

		scheduleAtFixedRate(() ->
		{
			_scheduledExecutor.purge();
			_scheduledExecutor_ai.purge();
			_scheduledExecutor_character.purge();
			_scheduledExecutor_script.purge();
			_executor.purge();
			_moveExecutor.purge();
			_scheduledExecutor_effect.purge();
		}, 300000L, 300000L);
		
		if (Config.ThreadPoolManagerDebug)
		{
			LastRunTime = System.currentTimeMillis();
			_debug = scheduleAtFixedRate(new DebugThreadPool(), Config.ThreadPoolManagerDebugInterval, Config.ThreadPoolManagerDebugInterval);
		}
		//long _time = 300000L; // 300000L
	}
	
	public void purge()
	{
		_scheduledExecutor.purge();
		_scheduledExecutor_ai.purge();
		_scheduledExecutor_character.purge();
		_scheduledExecutor_script.purge();
		_executor.purge();
		_moveExecutor.purge();
		_scheduledExecutor_effect.purge();
	}
	
	public void getPurge()
	{
		_scheduledExecutor.purge();
		_scheduledExecutor_ai.purge();
		_scheduledExecutor_character.purge();
		_scheduledExecutor_script.purge();
		_scheduledExecutor_effect.purge();
		_executor.purge();
		LOG.info(getStats().toString());
	}
	
	public boolean isShutdown()
	{
		return _shutdown;
	}
	
	/*
	 *  return validate 
	 *  TEST to see if is better
	 */
	private long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	public ScheduledFuture<?> scheduleMV(Runnable r, long delay)
	{
		return _moveExecutor.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return _scheduledExecutor.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAI(Runnable r, long delay)
	{
		return _scheduledExecutor_ai.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> schedule(Runnable r, long delay, boolean bool)
	{
		return _scheduledExecutor_character.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
	{
		return _scheduledExecutor_effect.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> schedule_scripts(Runnable r, long delay)
	{
		return _scheduledExecutor_script.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return _scheduledExecutor.scheduleAtFixedRate(wrap(r), validate(initial), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay)
	{
		return _scheduledExecutor.scheduleWithFixedDelay(wrap(r), validate(initial), validate(delay), TimeUnit.MILLISECONDS);
	}
	
	public void execute(Runnable r)
	{
		_executor.execute(r);
	}
	
	public void shutdown() throws InterruptedException
	{
		_shutdown = true;
		try
		{
			_scheduledExecutor.shutdown();
			_scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
			
			_scheduledExecutor_ai.shutdown();
			_scheduledExecutor_ai.awaitTermination(10, TimeUnit.SECONDS);
			
			_scheduledExecutor_script.shutdown();
			_scheduledExecutor_script.awaitTermination(10, TimeUnit.SECONDS);
			
			_scheduledExecutor_character.shutdown();
			_scheduledExecutor_character.awaitTermination(10, TimeUnit.SECONDS);
			
			_scheduledExecutor_effect.shutdown();
			_scheduledExecutor_effect.awaitTermination(10, TimeUnit.SECONDS);
		}
		finally
		{
			_executor.shutdown();
			_executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}
	
	public Runnable wrap(Runnable r)
	{
		return Config.ENABLE_RUNNABLE_STATS ? RunnableStatsWrapper.wrap(r) : r;
	}
	
	public static String data = new SimpleDateFormat("MM.dd.HH").format(System.currentTimeMillis());
	
	class DebugThreadPool implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				long runTime = System.currentTimeMillis();
				if (((runTime - LastRunTime - Config.ThreadPoolManagerDebugDeflect) > Config.ThreadPoolManagerDebugInterval) || ((runTime - LastRunTime) < 0))
				{
					long delay_time = runTime - LastRunTime - Config.ThreadPoolManagerDebugInterval;
					if (Config.ThreadPoolManagerDebugLogConsol)
					{
						LOG.info("Delay: " + delay_time + ", Queued: " + _scheduledExecutor.getQueue().size() + ", Task count: " + _scheduledExecutor.getTaskCount());
					}
					else if ((Config.ThreadPoolManagerDebugLogConsolDelay > 0) && (delay_time >= Config.ThreadPoolManagerDebugLogConsolDelay))
					{
						LOG.info("Delay: " + delay_time + ", Queued: " + _scheduledExecutor.getQueue().size() + ", Task count: " + _scheduledExecutor.getTaskCount());
					}
					if (Config.ThreadPoolManagerDebugLogFile)
					{
						Log.add("Delay: " + delay_time + ", Queued: " + _scheduledExecutor.getQueue().size() + ", Task count: " + _scheduledExecutor.getTaskCount(), "./../thread_pool/thread_pool_debug_" + data);
					}
				}
				LastRunTime = runTime;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public CharSequence getStats2()
	{
		StringBuilder list = new StringBuilder();
		
		list.append("ScheduledThreadPool\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor.getTaskCount()).append("\n");
		
		list.append("ScheduledThreadPoolAI\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_ai.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor_ai.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor_ai.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor_ai.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor_ai.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor_ai.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor_ai.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor_ai.getTaskCount()).append("\n");
		
		list.append("ScheduledThreadPoolCharacter\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_character.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor_character.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor_character.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor_character.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor_character.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor_character.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor_character.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor_character.getTaskCount()).append("\n");
		
		list.append("ScheduledThreadPoolEffect\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_effect.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor_effect.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor_effect.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor_effect.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor_effect.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor_effect.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor_effect.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor_effect.getTaskCount()).append("\n");
		
		list.append("ScheduledThreadPoolScript\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_script.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor_script.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor_script.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor_script.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor_script.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor_script.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor_script.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor_script.getTaskCount()).append("\n");
		
		list.append("ThreadPoolExecutor\n");
		list.append("ScheduledThreadPoolMove\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_moveExecutor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_moveExecutor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_moveExecutor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_moveExecutor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_moveExecutor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_moveExecutor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_moveExecutor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_moveExecutor.getTaskCount()).append("\n");
		
		list.append("ThreadPoolExecutor\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_executor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_executor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_executor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_executor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_executor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_executor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_executor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_executor.getTaskCount()).append("\n");
		
		return list;
	}
	
	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();
		
		list.append("ScheduledThreadPool\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor.getActiveCount() + " ").append("\n");
		list.append("\tgetCorePoolSize: ...... ").append(_scheduledExecutor.getCorePoolSize() + " ").append("\n");
		list.append("\tgetPoolSize: ...... ").append(_scheduledExecutor.getPoolSize() + " ").append("\n");
		list.append("\tgetLargestPoolSize: ...... ").append(_scheduledExecutor.getLargestPoolSize() + " ").append("\n");
		list.append("\tgetMaximumPoolSize: ...... ").append(_scheduledExecutor.getMaximumPoolSize() + " ").append("\n");
		list.append("\tgetCompletedTaskCount: ...... ").append(_scheduledExecutor.getCompletedTaskCount() + " ").append("\n");
		list.append("\tgetQueuedTaskCount: ...... ").append(_scheduledExecutor.getQueue().size() + " ").append("\n");
		list.append("\tgetTaskCount: ...... ").append(_scheduledExecutor.getTaskCount() + " ").append("\n");
		
		list.append("ScheduledThreadPoolAI\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_ai.getActiveCount() + " ").append("\n");
		list.append("\tgetCorePoolSize: ...... ").append(_scheduledExecutor_ai.getCorePoolSize() + " ").append("\n");
		list.append("\tgetPoolSize: ...... ").append(_scheduledExecutor_ai.getPoolSize() + " ").append("\n");
		list.append("\tgetLargestPoolSize: ...... ").append(_scheduledExecutor_ai.getLargestPoolSize() + " ").append("\n");
		list.append("\tgetMaximumPoolSize: ...... ").append(_scheduledExecutor_ai.getMaximumPoolSize() + " ").append("\n");
		list.append("\tgetCompletedTaskCount: ...... ").append(_scheduledExecutor_ai.getCompletedTaskCount() + " ").append("\n");
		list.append("\tgetQueuedTaskCount: ...... ").append(_scheduledExecutor_ai.getQueue().size() + " ").append("\n");
		list.append("\tgetTaskCount: ...... ").append(_scheduledExecutor_ai.getTaskCount() + " ").append("\n");
		
		list.append("ScheduledThreadPoolCharacter\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_character.getActiveCount() + " ").append("\n");
		list.append("\tgetCorePoolSize: ...... ").append(_scheduledExecutor_character.getCorePoolSize() + " ").append("\n");
		list.append("\tgetPoolSize: ...... ").append(_scheduledExecutor_character.getPoolSize() + " ").append("\n");
		list.append("\tgetLargestPoolSize: ...... ").append(_scheduledExecutor_character.getLargestPoolSize() + " ").append("\n");
		list.append("\tgetMaximumPoolSize: ...... ").append(_scheduledExecutor_character.getMaximumPoolSize() + " ").append("\n");
		list.append("\tgetCompletedTaskCount: ...... ").append(_scheduledExecutor_character.getCompletedTaskCount() + " ").append("\n");
		list.append("\tgetQueuedTaskCount: ...... ").append(_scheduledExecutor_character.getQueue().size() + " ").append("\n");
		list.append("\tgetTaskCount: ...... ").append(_scheduledExecutor_character.getTaskCount() + " ").append("\n");
		
		list.append("ScheduledThreadPoolEffect\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_effect.getActiveCount() + " ").append("\n");
		list.append("\tgetCorePoolSize: ...... ").append(_scheduledExecutor_effect.getCorePoolSize() + " ").append("\n");
		list.append("\tgetPoolSize: ...... ").append(_scheduledExecutor_effect.getPoolSize() + " ").append("\n");
		list.append("\tgetLargestPoolSize: ...... ").append(_scheduledExecutor_effect.getLargestPoolSize() + " ").append("\n");
		list.append("\tgetMaximumPoolSize: ...... ").append(_scheduledExecutor_effect.getMaximumPoolSize() + " ").append("\n");
		list.append("\tgetCompletedTaskCount: ...... ").append(_scheduledExecutor_effect.getCompletedTaskCount() + " ").append("\n");
		list.append("\tgetQueuedTaskCount: ...... ").append(_scheduledExecutor_effect.getQueue().size() + " ").append("\n");
		list.append("\tgetTaskCount: ...... ").append(_scheduledExecutor_effect.getTaskCount() + " ").append("\n");
		
		list.append("ScheduledThreadPoolScript\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor_script.getActiveCount() + " ").append("\n");
		list.append("\tgetCorePoolSize: ...... ").append(_scheduledExecutor_script.getCorePoolSize() + " ").append("\n");
		list.append("\tgetPoolSize: ...... ").append(_scheduledExecutor_script.getPoolSize() + " ").append("\n");
		list.append("\tgetLargestPoolSize: ...... ").append(_scheduledExecutor_script.getLargestPoolSize() + " ").append("\n");
		list.append("\tgetMaximumPoolSize: ...... ").append(_scheduledExecutor_script.getMaximumPoolSize() + " ").append("\n");
		list.append("\tgetCompletedTaskCount: ...... ").append(_scheduledExecutor_script.getCompletedTaskCount() + " ").append("\n");
		list.append("\tgetQueuedTaskCount: ...... ").append(_scheduledExecutor_script.getQueue().size() + " ").append("\n");
		list.append("\tgetTaskCount: ...... ").append(_scheduledExecutor_script.getTaskCount() + " ").append("\n");
		
		list.append("ScheduledThreadPoolMove\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_moveExecutor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_moveExecutor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_moveExecutor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_moveExecutor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_moveExecutor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_moveExecutor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_moveExecutor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_moveExecutor.getTaskCount()).append("\n");
		
		list.append("ThreadPoolExecutor\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(_executor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(_executor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(_executor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(_executor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(_executor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(_executor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(_executor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(_executor.getTaskCount()).append("\n");
		
		return list;
	}
	
	public ScheduledThreadPoolExecutor getSheduled()
	{
		return _scheduledExecutor;
	}
	
	public ThreadPoolExecutor getExecutor()
	{
		return _executor;
	}
	
	public ScheduledThreadPoolExecutor getSheduledMove()
	{
		return _moveExecutor;
	}
	
	public void startDebug()
	{
		if (Config.ThreadPoolManagerDebug)
		{
			if (_debug != null)
			{
				_debug.cancel(true);
			}
			LastRunTime = System.currentTimeMillis();
			_debug = scheduleAtFixedRate(new DebugThreadPool(), Config.ThreadPoolManagerDebugInterval, Config.ThreadPoolManagerDebugInterval);
		}
	}
	
	public void stopDebug()
	{
		if (Config.ThreadPoolManagerDebug && (_debug != null))
		{
			_debug.cancel(true);
		}
	}
	
	public static void cancel(Future<?> _task, boolean break_task)
	{
		try
		{
			if (_task != null)
			{
				_task.cancel(break_task);
			}
		}
		catch (Exception e)
		{
		}
	}
}
