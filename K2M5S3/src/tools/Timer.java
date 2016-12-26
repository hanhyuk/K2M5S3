package tools;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Timer {

	/**
	 * �̱�/��Ƽ ������ ��� ��밡��.
	 * Scheduled �̶�� �ܾ�� ���� Ư�� �ð��� �ֱ������� ���� �����Ҷ� ���. 
	 */
	private ScheduledThreadPoolExecutor scheduledExecutor;
	/**
	 * Timer Ŭ������ ��ӹ޾� ���� ����Ǵ� �������� ��ǥ�̸��� �����Ѵ�. 
	 */
	protected String name;
	/**
	 * Timer Ŭ������ ��ӹ��� ������� ������ ��ȣ�� �Ű�����. 
	 */
	private static final AtomicInteger threadNumber = new AtomicInteger(1);
	/**
	 * �����带 �����ϱ� ���� ������Ǯ�� �ʱⰪ�� �����Ѵ�.
	 */
	public void start() {
		if (scheduledExecutor != null && !scheduledExecutor.isShutdown() && !scheduledExecutor.isTerminated()) {
			return;
		}
		scheduledExecutor = new ScheduledThreadPoolExecutor(5, new RejectedThreadFactory());
		//setKeepAliveTime - �����尡 �����ϴ� ���� ��� �������� ������ �ð���ŭ �����带 �����Ѵ�. 
		scheduledExecutor.setKeepAliveTime(10, TimeUnit.MINUTES);
		//allowCoreThreadTimeOut - �����尡 ��� �۾��� �����ϰ� ������ �ð�(keepAliveTime)��ŭ ��� �ϰ� ������
		//���ο� �۾��� �����ؾ� �� ���, ����ϴ� �����带 timeOut ó�� ���� ���θ� �����Ѵ�.
		scheduledExecutor.allowCoreThreadTimeOut(true);
		scheduledExecutor.setMaximumPoolSize(8);
		//setContinueExistingPeriodicTasksAfterShutdownPolicy - scheduledExecutor �� ���� �Ǵ��� 
		//�������� �������� �۾��� ��� �����ϵ��� ������ ���� ���θ� �����Ѵ�.
		//�� ���� �⺻������ false�� �����Ǿ� �ִ�.
		//scheduledExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	}

	/**
	 * �����층 ó���� ���� ���Ǵ� ������� �� Ŭ������ ���� �����ȴ�.(factory) 
	 */
	private class RejectedThreadFactory implements ThreadFactory {

		private final AtomicInteger threadNumber2 = new AtomicInteger(1);
		private final String tname;

		public RejectedThreadFactory() {
			tname = name + Randomizer.nextInt();
		}

		@Override
		public Thread newThread(Runnable r) {
			final Thread t = new Thread(r);
			//������ �̸��� ��ǥ�̸� + �������� + ��ü Timer ��ü�� ���� + �� Timer �ȿ� �ִ� �������� �������� �����Ѵ�.
			t.setName(tname + "-W-" + threadNumber.getAndIncrement() + "-" + threadNumber2.getAndIncrement());
			return t;
		}
	}

	
	public ScheduledThreadPoolExecutor getScheduledExecutor() {
		return scheduledExecutor;
	}

	public void stop() {
		if (scheduledExecutor != null) {
			scheduledExecutor.shutdown();
		}
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
		if (scheduledExecutor == null) {
			return null;
		}
		return scheduledExecutor.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime) {
		if (scheduledExecutor == null) {
			return null;
		}
		return scheduledExecutor.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay) {
		if (scheduledExecutor == null) {
			return null;
		}
		return scheduledExecutor.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
	}

	//TODO EventManager Ŭ�������� ���Ǵµ� ���� Ȱ��Ǵ� �κ��� ������ Ȯ������.
	public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
		return schedule(r, timestamp - System.currentTimeMillis());
	}

	private static class LoggingSaveRunnable implements Runnable {

		Runnable r;
		
		public LoggingSaveRunnable(final Runnable r) {
			this.r = r;
		}

		@Override
		public void run() {
			try {
				r.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

		
	public static class WorldTimer extends Timer {
		private static WorldTimer instance = new WorldTimer();

		private WorldTimer() {
			name = "Worldtimer";
		}

		public static WorldTimer getInstance() {
			return instance;
		}
	}

	public static class LogoutTimer extends Timer {
		private static LogoutTimer instance = new LogoutTimer();

		private LogoutTimer() {
			name = "LogoutTimer";
		}

		public static LogoutTimer getInstance() {
			return instance;
		}
	}

	public static class MapTimer extends Timer {
		private static MapTimer instance = new MapTimer();

		private MapTimer() {
			name = "Maptimer";
		}

		public static MapTimer getInstance() {
			return instance;
		}
	}

	public static class BuffTimer extends Timer {
		private static BuffTimer instance = new BuffTimer();

		private BuffTimer() {
			name = "Bufftimer";
		}

		public static BuffTimer getInstance() {
			return instance;
		}
	}

	public static class EventTimer extends Timer {
		private static EventTimer instance = new EventTimer();

		private EventTimer() {
			name = "Eventtimer";
		}

		public static EventTimer getInstance() {
			return instance;
		}
	}

	public static class CloneTimer extends Timer {
		private static CloneTimer instance = new CloneTimer();

		private CloneTimer() {
			name = "Clonetimer";
		}

		public static CloneTimer getInstance() {
			return instance;
		}
	}

	public static class EtcTimer extends Timer {
		private static EtcTimer instance = new EtcTimer();

		private EtcTimer() {
			name = "Etctimer";
		}

		public static EtcTimer getInstance() {
			return instance;
		}
	}

	public static class CheatTimer extends Timer {
		private static CheatTimer instance = new CheatTimer();

		private CheatTimer() {
			name = "Cheattimer";
		}

		public static CheatTimer getInstance() {
			return instance;
		}
	}

	public static class ShowTimer extends Timer {
		private static ShowTimer instance = new ShowTimer();

		private ShowTimer() {
			name = "ShowTimer";
		}

		public static ShowTimer getInstance() {
			return instance;
		}
	}

	public static class PingTimer extends Timer {
		private static PingTimer instance = new PingTimer();

		private PingTimer() {
			name = "Pingtimer";
		}

		public static PingTimer getInstance() {
			return instance;
		}
	}

	

	
}