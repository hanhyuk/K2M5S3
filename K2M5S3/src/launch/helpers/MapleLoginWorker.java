package launch.helpers;

import client.MapleClient;
import handler.login.CharLoginHandler;
import packet.creators.LoginPacket;

public class MapleLoginWorker {

	public static void registerClient(final MapleClient c) {
		if (c.finishLogin() == 0) {
			c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
			
			CharLoginHandler.getDisplayChannel(true, c);
			
			//TODO 로그인이 성공하고 나면 아래 로직의 의해 일정 주기 이후에 클라를 종료시킨다.1
			//알고 보니 로그인 이후 ingame 하려면 2차 비밀번호를 입력해야 하는데
			//해당 2차번호 인증이 성공할 경우 아래 종료처리 로직을 cancel 시킨다.
			//내가 궁금한건 굳이 그렇게 해야 하는가 인데... 소스 전체 검색해보니
			//setIdleTask 메소드를 사용하는 곳이 여기 한곳 밖에 없다. 그래서 일단 사용하지 않도록 주석처리한다.
//			c.setIdleTask(PingTimer.getInstance().schedule(new Runnable() {
//				public void run() {
//					c.getSession().closeNow();
//				}
//			}, 10 * 60 * 10000));
		} else {
			c.getSession().write(LoginPacket.getLoginFailed(7));
			return;
		}
	}
}
