package constants.programs;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import launch.world.WorldBroadcasting;
import packet.creators.MainPacketCreator;
import tools.CPUSampler;
import tools.DeadLockDetector;

public class Clean {
	private static final Logger logger = LoggerFactory.getLogger(Clean.class);
	
	private static transient ScheduledFuture<?> start;
	private static int timeo = 0;

	public static void main(String[] args) {
		if (start == null) {
			start = tools.Timer.WorldTimer.getInstance().register(new Runnable() {
				public void run() {
					if (timeo == 0) {
						CPUSampler.getInstance().start();
						logger.debug("CPUSampler Thread Start!!");
						DeadLockDetector clean = new DeadLockDetector(10, (byte) 1);
						clean.run();
						timeo++;
					} else if (timeo == -1) {
						timeo = 0;
					} else if (timeo != 0) {
						CPUSampler.getInstance().start();
						logger.debug("CPUSampler Thread Start!!");
						DeadLockDetector clean = new DeadLockDetector(10, (byte) 1);
						clean.run();
						WorldBroadcasting.broadcast(
								MainPacketCreator.serverNotice(1, "서버최적화가 진행중입니다. 다소 렉이 유발될수도 있으니 양해 부탁드립니다."));
						timeo++;
					}
				}
			}, 3600000 * 3);
		}
	}
}
