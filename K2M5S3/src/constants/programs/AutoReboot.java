package constants.programs;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import constants.ServerConstants;
import launch.ChannelServer;
import launch.world.WorldBroadcasting;
import packet.creators.MainPacketCreator;
import tools.Timer.WorldTimer;

/**
 * @deprecated �ڵ� ������ ����� �ʿ����� ����.
 */
public class AutoReboot {
	private static final Logger logger = LoggerFactory.getLogger(AutoReboot.class);
	
	private static String path = "";
	private static int time = 0;

	public static void main(final String args[]) {
		WorldTimer tMan = WorldTimer.getInstance();
		Runnable r = new Runnable() {
			public void run() {
				if (time == 0) {
					logger.info("[�˸�] �� �ð��η� 4�ð� �ֱ�� ������ �ڵ����� �������մϴ�.");
					time++;
				} else if (time == -1) {
					time = 0;
				} else {
					WorldBroadcasting.broadcastMessage(
							MainPacketCreator.serverNotice(1, "�������� ������� ����, �� 1�а� ������������ ����˴ϴ�.\r\n������ ���ĵ�� �˼��մϴ�."));
					try {
						Thread.sleep(10000L);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					for (ChannelServer cserv : ChannelServer.getAllInstances()) { // ����
																					// ������,
																					// �����ͺ��̽�
																					// ����.
						cserv.saveAllMerchant();
						for (MapleCharacter hp : cserv.getPlayerStorage().getAllCharacters().values()) {
							if (hp != null)
								hp.saveToDB(false, false);
						}
					}
					for (ChannelServer cserv : ChannelServer.getAllInstances()) {
						cserv.getPlayerStorage().disconnectAll();
					}
					runFile();
					System.exit(1);
				}
			}
		};
		tMan.register(r, 14400000);
	}

	public static void runFile() {
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec(ServerConstants.path + "\\AutoReboot.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
