package launch.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import launch.ChannelServer;

public class ShutdownServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownServer.class);
	
	private int channel;

	public ShutdownServer(int channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
		try {
			ChannelServer.getInstance(channel).shutdown();
		} catch (Exception e) {
			logger.debug("{}", e);
		}

		boolean error = true;
		while (error) {
			try {
				ChannelServer.getInstance(channel).unbind();
				error = false;
			} catch (Exception e) {
				error = true;
			}
		}

		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			while (!cserv.hasFinishedShutdown()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.debug("{}", e);
				}
			}
		}
		
		logger.info("[종료] 채널 {} 서버가 종료 되었습니다.", channel);
	}
}
