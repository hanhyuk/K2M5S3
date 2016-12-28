package launch;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ServerConstants;
import constants.programs.AutoReboot;
import constants.programs.ControlUnit;
import constants.programs.DatabaseBackup;
import constants.programs.DatabaseGarbageCollector;
import constants.programs.HighRanking;
import constants.programs.RewardScroll;
import constants.subclasses.QuickMove;
import constants.subclasses.setScriptableNPC;
import database.MYSQL;
import launch.helpers.MapleCacheData;
import launch.helpers.MapleLoginHelper;
import launch.world.WorldAuction;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import server.items.CashItemFactory;
import server.life.MapleMonsterProvider;

public final class Start {
	
	private static final Logger logger = LoggerFactory.getLogger(Start.class);
	
	public static void main(String args[]) throws IOException {
		final long START = System.currentTimeMillis();
		
		logger.info("[알림] 에뮬레이터 :: V1.2." + ServerConstants.MAPLE_VERSION + " 버전이 실행되었습니다.\n");

		ServerConstants.loadServerSetProp();
		
		MapleLoginHelper.getInstance().loadForbiddenNames();

		// 스케쥴링 처리에 필요한 스레드풀을 각각 분류해서 생성한다.
		tools.Timer.WorldTimer.getInstance().start();
		tools.Timer.EtcTimer.getInstance().start();
		tools.Timer.MapTimer.getInstance().start();
		tools.Timer.CloneTimer.getInstance().start();
		tools.Timer.EventTimer.getInstance().start();
		tools.Timer.BuffTimer.getInstance().start();
		tools.Timer.PingTimer.getInstance().start();
		tools.Timer.ShowTimer.getInstance().start();

		//랭킹 처리를 위한 쿼리를 주기적으로 실행. 현재 필요없기 떄문에 주석 처리
		//WorldTimer.getInstance().register(new MapleRankingWorker(), 1000 * 60 * 60);
		
		/* 소켓 설정 및 서버 가동 */
		LoginServer.getInstance().run_startup_configurations();
		ChannelServer.startServer();
		CashShopServer.getInstance().run_startup_configurations();
		BuddyChatServer.getInstance().run_startup_configurations();

		/* 옵코드 설정 */
		SendPacketOpcode.loadOpcode();
		RecvPacketOpcode.loadOpcode();

		/* 메모리 정리 및 캐싱쓰레드 시작 */
		CashItemFactory.getInstance();
		Start.clean();
		MapleCacheData mc = new MapleCacheData();
		mc.startCacheData();
		HighRanking.getInstance().startTasking();
		WorldAuction.load();
		QuickMove.doMain();
		setScriptableNPC.doMain();
		RewardScroll.getInstance();
		MapleMonsterProvider.getInstance().retrieveGlobal();

		/* 세부 쓰레드 시작 */
		DatabaseGarbageCollector.main(args);
		DatabaseBackup.getInstance().startTasking();
		ControlUnit.main(args);
		System.gc();

		/* 서버 오픈 완료 메세지 */
		long END = System.currentTimeMillis();
		System.out.println("[알림] 서버 오픈이 정상적으로 완료 되었으며, 소요된 시간은 : " + (END - START) / 1000.0 + "초 입니다.");
	}

	public static void clean() {
		try {
			int nu = 0;
			PreparedStatement ps;
			Calendar ocal = Calendar.getInstance();
			ps = MYSQL.getConnection().prepareStatement("SELECT * FROM acheck WHERE day = 1");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String key = rs.getString("keya");
				String day = ocal.get(ocal.YEAR) + "" + (ocal.get(ocal.MONTH) + 1) + "" + ocal.get(ocal.DAY_OF_MONTH);
				String da[] = key.split("_");
				if (!da[0].equals(day)) {
					ps = MYSQL.getConnection().prepareStatement("DELETE FROM acheck WHERE keya = ?");
					ps.setString(1, key);
					ps.executeUpdate();
					nu++;
				}
			}
			System.out.println("[알림] " + nu + "개의 1일 입장 기록을 지웠습니다.");
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}