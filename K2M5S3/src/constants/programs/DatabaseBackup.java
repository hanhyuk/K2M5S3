package constants.programs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import constants.ServerConstants;
import launch.ChannelServer;
import tools.Timer.WorldTimer;

/**
 * @deprecated 자동 백업 기능 사용하지 않음.
 */
public class DatabaseBackup {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseBackup.class);
	private static int time = 0;
	public static DatabaseBackup instance = null;

	public static DatabaseBackup getInstance() {
		if (instance == null) {
			instance = new DatabaseBackup();
		}
		return instance;
	}

	public void startTasking() {
		WorldTimer tMan = WorldTimer.getInstance();
		Runnable r;
		r = new Runnable() {
			public void run() {
				if (time == 0) {
					logger.info("[알림] 현 시간부로 30분 주기로 데이터베이스 자동 백업 프로그램이 작동됩니다.");
					time++;
				} else if (time == -1) {
					time = 0;
				} else {
					try {
						for (ChannelServer cserv : ChannelServer.getAllInstances()) { // 데이터베이스
																						// 저장
																						// 전,
																						// 데이터베이스
																						// 저장.
							cserv.saveAllMerchant();
							for (MapleCharacter hp : cserv.getPlayerStorage().getAllCharacters().values()) {
								if (hp != null)
									hp.saveToDB(false, false);
							}
						}

						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
						String name = sdf.format(Calendar.getInstance().getTime());
						Process p = null;
						File toDel = null;

						p = Runtime.getRuntime()
								.exec("cmd /C \"" + ServerConstants.windowsDumpPath + "\\mysqldump -u"
										+ ServerConstants.dbUser + " -p" + ServerConstants.dbPassword
										+ " arcstory > DBBackup\\GameServer\\" + name + ".sql");
						p = Runtime.getRuntime().exec("cmd /C \"" + ServerConstants.path
								+ "\\gzip\" -9 DBBackup\\GameServer\\" + name + ".sql");
						p.getInputStream().read();

						try {
							p.waitFor();
						} finally {
							p.destroy();
						}

						toDel = new File("DBBackup\\GameServer\\" + name + ".sql");
						logger.info("[알림] '" + name + "' 날짜의 게임서버 데이터베이스 자동 백업이 완료되었습니다.");
						toDel.delete();

					} catch (IOException e) {
						logger.info("[알림] 데이터베이스 자동 백업이 실패하였습니다. {}", e);
					} catch (Exception e) {
						logger.info("[오류] 데이터베이스 자동 백업을 실행 중 알 수 없는 오류가 발생했습니다. {}", e);
					}
				}
			}
		};
		tMan.register(r, 1800000);
	}
}
