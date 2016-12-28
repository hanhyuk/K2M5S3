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
 * @deprecated 자동 리부팅 기능이 필요하지 않음.
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
					logger.info("[알림] 현 시간부로 4시간 주기로 서버를 자동으로 리부팅합니다.");
					time++;
				} else if (time == -1) {
					time = 0;
				} else {
					WorldBroadcasting.broadcastMessage(
							MainPacketCreator.serverNotice(1, "안정적인 서버운영을 위해, 약 1분간 서버리부팅이 진행됩니다.\r\n불편을 끼쳐드려 죄송합니다."));
					try {
						Thread.sleep(10000L);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					for (ChannelServer cserv : ChannelServer.getAllInstances()) { // 서버
																					// 종료전,
																					// 데이터베이스
																					// 저장.
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
