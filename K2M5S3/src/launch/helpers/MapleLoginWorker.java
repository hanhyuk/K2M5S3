package launch.helpers;

import client.MapleClient;
import handler.login.CharLoginHandler;
import packet.creators.LoginPacket;
import tools.Timer.PingTimer;

public class MapleLoginWorker {

	public static void registerClient(final MapleClient c) {
		if (c.finishLogin() == 0) {
			c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
			
			CharLoginHandler.getDisplayChannel(true, c);
			
			c.setIdleTask(PingTimer.getInstance().schedule(new Runnable() {
				public void run() {
					c.getSession().closeNow();
				}
			}, 10 * 60 * 10000));
		} else {
			c.getSession().write(LoginPacket.getLoginFailed(7));
			return;
		}
	}
}
