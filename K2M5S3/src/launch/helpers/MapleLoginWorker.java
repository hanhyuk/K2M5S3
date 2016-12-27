package launch.helpers;

import client.MapleClient;
import handler.login.CharLoginHandler;
import packet.creators.LoginPacket;

public class MapleLoginWorker {

	public static void registerClient(final MapleClient c) {
		if (c.finishLogin() == 0) {
			c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
			
			CharLoginHandler.getDisplayChannel(true, c);
			
			//TODO �α����� �����ϰ� ���� �Ʒ� ������ ���� ���� �ֱ� ���Ŀ� Ŭ�� �����Ų��.1
			//�˰� ���� �α��� ���� ingame �Ϸ��� 2�� ��й�ȣ�� �Է��ؾ� �ϴµ�
			//�ش� 2����ȣ ������ ������ ��� �Ʒ� ����ó�� ������ cancel ��Ų��.
			//���� �ñ��Ѱ� ���� �׷��� �ؾ� �ϴ°� �ε�... �ҽ� ��ü �˻��غ���
			//setIdleTask �޼ҵ带 ����ϴ� ���� ���� �Ѱ� �ۿ� ����. �׷��� �ϴ� ������� �ʵ��� �ּ�ó���Ѵ�.
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
