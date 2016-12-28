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
								MainPacketCreator.serverNotice(1, "��������ȭ�� �������Դϴ�. �ټ� ���� ���ߵɼ��� ������ ���� ��Ź�帳�ϴ�."));
						timeo++;
					}
				}
			}, 3600000 * 3);
		}
	}
}
