package tools;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Timer {

	/**
	 * 싱글/멀티 스레드 모두 사용가능.
	 * Scheduled 이라는 단어와 같이 특정 시간에 주기적으로 뭔가 실행할때 사용. 
	 */
	private ScheduledThreadPoolExecutor scheduledExecutor;
	/**
	 * Timer 클래스를 상속받아 실제 실행되는 스레드의 대표이름을 지정한다. 
	 */
	protected String name;
	/**
	 * Timer 클래스를 상속받은 순서대로 쓰레드 번호가 매겨진다. 
	 */
	private static final AtomicInteger threadNumber = new AtomicInteger(1);
	/**
	 * 스레드를 관리하기 위한 스레드풀의 초기값을 설정한다.
	 */
	public void start() {
		if (scheduledExecutor != null && !scheduledExecutor.isShutdown() && !scheduledExecutor.isTerminated()) {
			return;
		}
		scheduledExecutor = new ScheduledThreadPoolExecutor(5, new RejectedThreadFactory());
		//setKeepAliveTime - 스레드가 수행하는 일이 모두 끝나더라도 지정한 시간만큼 스레드를 유지한다. 
		scheduledExecutor.setKeepAliveTime(10, TimeUnit.MINUTES);
		//allowCoreThreadTimeOut - 스레드가 모든 작업을 수행하고 지정된 시간(keepAliveTime)만큼 대기 하고 있을때
		//새로운 작업을 수행해야 할 경우, 대기하는 스레드를 timeOut 처리 할지 여부를 지정한다.
		scheduledExecutor.allowCoreThreadTimeOut(true);
		scheduledExecutor.setMaximumPoolSize(8);
		//setContinueExistingPeriodicTasksAfterShutdownPolicy - scheduledExecutor 가 종료 되더라도 
		//수행중인 스레드의 작업을 계속 수행하도록 할지에 대한 여부를 결정한다.
		//이 값은 기본적으로 false로 설정되어 있다.
		//scheduledExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	}

	/**
	 * 스케쥴링 처리를 위해 사용되는 스레드는 이 클래스를 통해 생성된다.(factory) 
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
			//스레드 이름은 대표이름 + 랜덤숫자 + 전체 Timer 객체의 순번 + 각 Timer 안에 있는 스레드의 순번으로 설정한다.
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

	//TODO EventManager 클래스에서 사용되는데 현재 활용되는 부분이 없는지 확인하자.
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