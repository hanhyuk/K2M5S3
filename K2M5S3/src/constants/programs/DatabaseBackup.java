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
 * @deprecated �ڵ� ��� ��� ������� ����.
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
					logger.info("[�˸�] �� �ð��η� 30�� �ֱ�� �����ͺ��̽� �ڵ� ��� ���α׷��� �۵��˴ϴ�.");
					time++;
				} else if (time == -1) {
					time = 0;
				} else {
					try {
						for (ChannelServer cserv : ChannelServer.getAllInstances()) { // �����ͺ��̽�
																						// ����
																						// ��,
																						// �����ͺ��̽�
																						// ����.
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
						logger.info("[�˸�] '" + name + "' ��¥�� ���Ӽ��� �����ͺ��̽� �ڵ� ����� �Ϸ�Ǿ����ϴ�.");
						toDel.delete();

					} catch (IOException e) {
						logger.info("[�˸�] �����ͺ��̽� �ڵ� ����� �����Ͽ����ϴ�. {}", e);
					} catch (Exception e) {
						logger.info("[����] �����ͺ��̽� �ڵ� ����� ���� �� �� �� ���� ������ �߻��߽��ϴ�. {}", e);
					}
				}
			}
		};
		tMan.register(r, 1800000);
	}
}
