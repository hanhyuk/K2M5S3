package launch;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.dao.CommonDAO;
import client.skills.SkillFactory;
import constants.ServerConstants;
import constants.programs.ControlUnit;
import constants.programs.RewardScroll;
import constants.subclasses.QuickMove;
import handler.login.CharLoginHandler;
import launch.world.WorldAuction;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import server.items.CashItemFactory;
import server.life.MapleMonsterProvider;
import tools.Timer;

public final class Start {
	private static final Logger logger = LoggerFactory.getLogger(Start.class);
	
	public static void main(String args[]) throws IOException {
		logger.info("[알림] 에뮬레이터 :: V1.2." + ServerConstants.MAPLE_VERSION + " 버전이 실행되었습니다.\n");

		//서버 구동에 필요한 상수값 로딩
		ServerConstants.init();
		
		//불필요한 DB 정보 삭제
		CommonDAO.deleteUnnecessaryDbInfoAtStartUp();
		//모든 사용자 로그아웃
		CommonDAO.updateAllUserLogout();
		
		//패킷 정보 로딩
		SendPacketOpcode.loadOpcode();
		RecvPacketOpcode.loadOpcode();
		
		//캐시템 정보 로딩
		CashItemFactory.getInstance();
		//보상 아이템 정보를 로딩.
		RewardScroll.getInstance();
		//TODO 경매장 정보 로딩. 분석 필요.
		WorldAuction.load();
		//사용할수 없는 캐릭명 로딩
		CharLoginHandler.loadForbiddenNames();
		//TODO 빠른 이동 정보 로딩. 어디서 사용되는지 확인 필요.
		QuickMove.doMain();
		//글로벌 드랍 정보 로딩
		MapleMonsterProvider.getInstance().loadGlobalDropInfo();
		
		//스케쥴러 등록
		Timer.startAllTimer();
		
		//로그인, 채널, 캐시샵, 버디챗 서버 활성화
		LoginServer.getInstance().start();
		ChannelServer.start(ServerConstants.openChannelCount);
		CashShopServer.getInstance().start();
		BuddyChatServer.getInstance().start();

		//hh
		//MapleCacheData mc = new MapleCacheData(); mc.startCacheData();
		
		SkillFactory.cacheSkillData();
		
		
		
		
		
		ControlUnit.main(args);
		
		System.gc();

		logger.info("[알림] 서버 오픈이 정상적으로 완료 되습니다.");
	}
}