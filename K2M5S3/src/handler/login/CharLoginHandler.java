package handler.login;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.AccountStatusType;
import a.my.made.CommonType;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleItempotMain;
import client.items.Equip;
import client.items.Item;
import client.items.MapleInventory;
import client.items.MapleInventoryType;
import client.skills.SkillFactory;
import constants.ServerConstants;
import launch.helpers.MapleNewCharJobType;
import launch.world.WorldConnected;
import packet.creators.LoginPacket;
import packet.creators.MainPacketCreator;
import packet.transfer.read.ReadingMaple;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Randomizer;

public class CharLoginHandler {
	private static final Logger logger = LoggerFactory.getLogger(CharLoginHandler.class);
	
	private static List<String> forbiddenNameList = new ArrayList<String>();
	
	/**
	 * 사용 불가능한 캐릭터명 리스트를 wz 파일에서 로딩한다.
	 */
	public static void cachingForbiddenNames() {
		final MapleDataProvider provider = MapleDataProviderFactory.getDataProvider("Etc.wz");
		final MapleData nameData = provider.getData("ForbiddenName.img");
		
		for( MapleData data : nameData.getChildren() ) {
			forbiddenNameList.add(MapleDataTool.getString(data));
		}
	}

	/**
	 * true : 사용불가 캐릭명 false : 사용가능 캐릭명
	 */
	public static boolean isForbiddenName(final String in) {
		for (final String name : forbiddenNameList) {
			if (in.contains(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * RECV LOGIN_PASSWORD 패킷 처리
	 */
	public static void login(ReadingMaple rh, MapleClient c) {
		if (ServerConstants.serverCheck && !c.isGm()) {
			c.send(MainPacketCreator.serverNotice(1, ServerConstants.serverCheckMessage));
			c.send(LoginPacket.getLoginFailed(20));
		} else {
			rh.skip(22);

			String login = rh.readMapleAsciiString(); // 로그인 ID
			String pwd = rh.readMapleAsciiString(); // 비밀번호

			CommonType checkType = AutoRegister.checkAccount(login);
			if (CommonType.ACCOUNT_CREATE_POSSIBLE == checkType) {
				AutoRegister.registerAccount(c, login, pwd);
				c.send(MainPacketCreator.serverNotice(1, "계정이 생성 되었습니다.\r\n다시 로그인 하세요."));
				c.send(LoginPacket.getLoginFailed(20));
			} else if( CommonType.ACCOUNT_EXISTS == checkType ) {
				CommonType commonType = c.checkLoginAvailability(login, pwd);

				if (CommonType.LOGIN_POSSIBLE == commonType) {
					boolean isChanged = c.updateLoginState(AccountStatusType.FIRST_LOGIN.getValue(), c.getSessionIPAddress());
					
					if( isChanged ) {
						c.send(LoginPacket.getAuthSuccessRequest(c));
						CharLoginHandler.getDisplayChannel(true, c);
					} else {
						logger.debug("updateLoginState() - 사용자의 계정(" + login + ") 접속 상태값 변경 실패. 원인 파악 필요.");
						c.send(LoginPacket.getLoginFailed(6));
					}
				} else if (CommonType.ACCOUNT_BAN == commonType) {
					c.send(LoginPacket.getLoginFailed(3));
				} else if (CommonType.LOGIN_ING == commonType) {
					c.send(LoginPacket.getLoginFailed(7));
				} else if (CommonType.LOGIN_IMPOSSIBLE == commonType) {
					logger.warn("계정 정보를 확인 할 수 없습니다. 이런 경우가 자주 발생한다면 시스템적으로 문제가 있는지 점검이 필요함.");
					c.send(LoginPacket.getLoginFailed(8));
				}
			}
		}
	}

	public static void CharlistRequest(ReadingMaple rh, MapleClient c) {
		final boolean isFirstLogin = rh.readByte() == 0;
		if (!isFirstLogin) { // 1.2.239+ 게임 종료 대응.
			rh.skip(1);
			final String account = rh.readMapleAsciiString();
			final String login = account.split(",")[0];
			final String pwd = account.split(",")[1];
			rh.skip(21);
			c.getSession().write(LoginPacket.getCharEndRequest(c, login, pwd, true));
			c.getSession().write(LoginPacket.getSelectedWorld());
		}
		int server = rh.readByte();
		int channel = rh.readByte();
		c.setWorld(server);
		c.setChannel(channel);
		logger.info("[알림] {} 에서 {} 계정으로 {} 채널로 연결을 시도 중입니다.", c.getSessionIPAddress().toString(), c.getAccountName(), (channel == 0 ? 1 : channel == 1 ? "20세이상" : channel));
		try {
			List<MapleCharacter> chars = c.loadCharacters();
			c.getSession().write(LoginPacket.charlist(c, c.isUsing2ndPassword(), chars));
			chars.clear();
			chars = null;
		} catch (Exception e) {
			logger.debug("{}", e);
		}
	}

	public static void onlyRegisterSecondPassword(ReadingMaple rh, MapleClient c) {
		String secondpw = rh.readMapleAsciiString();
		c.setSecondPassword(secondpw);
		c.updateSecondPassword();
		c.send(LoginPacket.getSecondPasswordResult(true));
	}

	public static void registerSecondPassword(ReadingMaple rh, MapleClient c) {
		String originalPassword = rh.readMapleAsciiString();
		String changePassword = rh.readMapleAsciiString();

		if (!originalPassword.equals(c.getSecondPassword())) {
			c.send(LoginPacket.getSecondPasswordResult(false));
		} else {
			c.setSecondPassword(changePassword);
			c.updateSecondPassword();
			c.send(LoginPacket.getSecondPasswordResult(true));
		}
	}

	/**
	 * RECV SECONDPW_RESULT_R 패킷 - 2차 비밀번호를 사용하는지 여부를 체크한다.(C -> S)
	 * @param rh
	 * @param c
	 */
	public static void getSPCheck(ReadingMaple rh, MapleClient c) {
		if (c.getSecondPassword() != null) {
			c.getSession().write(LoginPacket.getSecondPasswordCheck(true, false, true));
		} else {
			c.getSession().write(LoginPacket.getSecondPasswordCheck(false, false, false));
		}
	}

	/**
	 * @deprecated 실제 클라에서 쓰는게 아니라 접속기를 통한 로그인 처리 같아 보여서 일단 사용하지 않도록 함. 삭제는 추후에
	 *             정말 사용하지 않는지 확인 후에 제거.
	 */
	public static void getLoginRequest(ReadingMaple rh, MapleClient c) {
		// rh.skip(2);
		// final String account = rh.readMapleAsciiString();
		// final String login = account.split(",")[0];
		// final String pwd = account.split(",")[1];
		// int loginok = c.login(login, pwd);
		// if (loginok != 0) { // hack
		// c.getSession().closeNow();
		// return;
		// }
		// if (c.finishLogin() == 0) {
		// c.setAccountName(login);
		// c.getSession().write(LoginPacket.getRelogResponse());
		// c.getSession().write(LoginPacket.getCharEndRequest(c, login, pwd,
		// false));
		// } else {
		// c.getSession().write(LoginPacket.getLoginFailed(20));
		// }
	}

	public static void getXignCodeResponse(boolean response, MapleClient c) {
		if (response) {
			c.getSession().write(LoginPacket.getXignCodeResponse());
		}
	}

	/**
	 * (C -> S)클라에서 캐릭터 생성 완료후 ingame 할때 사용.
	 * 
	 * @param rh
	 * @param client
	 */
	public static void getIPRequest(ReadingMaple rh, MapleClient client) {
		//TODO 1,2차 로그인 이후 캐릭터 생성을 완료 할때 호출되는데
		//1,2차 로그인 상태인지 확인하는 처리가 필요하다.
		
		client.getSession().write(MainPacketCreator.getServerIP(client, ServerConstants.channelPort + client.getChannel(), ServerConstants.buddyChatPort, rh.readInt()));
	}

	/**
	 * 서버 선택하는 화면에 노출되는 서버 리스트를 클라이언트로 보낸다.
	 * @param firstLogin 사용자가 1차 로그인을 했을 경우 true
	 * @param c MapleClient
	 */
	public static void getDisplayChannel(final boolean firstLogin, final MapleClient c) {
		c.getSession().write(LoginPacket.getChannelBackImg(firstLogin, (byte)Randomizer.rand(0, 1)));
		
		/* 겉 멀티월드 시작 */
		int[] world = new int[] { 0, 1, 3, 4, 5, 10, 16, 29, 43, 44, 45 };
		for (int i = 0; i < world.length; i++) {
			c.getSession().write(LoginPacket.getServerList(world[i], WorldConnected.getConnected()));
		}
		/* 겉 멀티월드 종료 */
		
		c.getSession().write(LoginPacket.recommendWorld());
		c.getSession().write(LoginPacket.getEndOfServerList());
		c.getSession().write(LoginPacket.getLastWorld());
	}

	/**
	 * RECV SESSION_CHECK_R 패킷 처리
	 * 
	 * 클라이언트에서 세션 유지(?)를 위해 전송하는 패킷을 처리한다.
	 * 패킷이 전송되는 간격은 1분이다.
	 * 
	 * @param rh
	 * @param c
	 */
	public static void getSessionCheck(ReadingMaple rh, MapleClient c) {
		int pRequest = rh.readInt();
		int pResponse;
		pResponse = ((pRequest >> 5) << 5) + (((((pRequest & 0x1F) >> 3) ^ 2) << 3) + (7 - (pRequest & 7)));
		pResponse |= ((pRequest >> 7) << 7);
		c.getSession().write(LoginPacket.getSessionResponse(pResponse));
	}

	public static void setBurningCharacter(ReadingMaple rh, MapleClient c) {
		rh.skip(1);
		int accountId = rh.readInt();
		int charId = rh.readInt();
		if (!c.isLoggedIn() || c.getAccID() != accountId) { // hack
			return;
		}
		if (!c.setBurningCharacter(accountId, charId)) {
			c.getSession().write(MainPacketCreator.serverNotice(1, "잘못된 요청입니다."));
			return;
		}
		c.send(LoginPacket.setBurningEffect(charId));
	}

	public static void checkSecondPassword(ReadingMaple rh, MapleClient c) {
		String code = rh.readMapleAsciiString();
		if (!code.equals(c.getSecondPassword())) {
			c.send(LoginPacket.getSecondPasswordConfirm(false));
		} else {
			c.send(LoginPacket.getSecondPasswordConfirm(true));
		}
	}

	public static void CheckCharName(String name, MapleClient c) {
		c.getSession().write(LoginPacket.charNameResponse(name, !MapleCharacterUtil.canCreateChar(name) || isForbiddenName(name)));
	}

	public static void CreateChar(ReadingMaple rh, MapleClient c) {
		String name = rh.readMapleAsciiString();
		MapleCharacter newchar = MapleCharacter.getDefault(c);
		rh.skip(8);
		int JobType = rh.readInt(); // 1 = Adventurer, 0 = Cygnus, 2 = Aran
		short subCategory = rh.readShort();
		if (JobType == MapleNewCharJobType.제로.getValue()) {
			newchar.setSecondGender(rh.readByte());
		} else {
			newchar.setGender(rh.readByte());
		}
		newchar.setSkinColor(rh.readByte());
		rh.skip(1);
		newchar.setFace(rh.readInt());
		newchar.setHair(rh.readInt());
		if (JobType == MapleNewCharJobType.데몬슬레이어.getValue() || JobType == MapleNewCharJobType.제논.getValue()) {
			newchar.setSecondFace(rh.readInt());
		}
		if (JobType == MapleNewCharJobType.제로.getValue()) {
			newchar.setGender((byte) 1);
			newchar.setSecondSkinColor((byte) 0);
			newchar.setSecondFace(21290);
			newchar.setSecondHair(37623);
		}
		int top = rh.readInt();
		int bottom = 0;
		if (JobType != MapleNewCharJobType.레지스탕스.getValue() && JobType != MapleNewCharJobType.메르세데스.getValue()
				&& JobType != MapleNewCharJobType.데몬슬레이어.getValue() && JobType != MapleNewCharJobType.루미너스.getValue()
				&& JobType != MapleNewCharJobType.카이저.getValue() && JobType != MapleNewCharJobType.엔젤릭버스터.getValue()
				&& JobType != MapleNewCharJobType.제논.getValue() && JobType != MapleNewCharJobType.모험가.getValue()
				&& JobType != MapleNewCharJobType.캐논슈터.getValue() && JobType != MapleNewCharJobType.듀얼블레이더.getValue()
				&& JobType != MapleNewCharJobType.팬텀.getValue() && JobType != MapleNewCharJobType.제로.getValue() && JobType != MapleNewCharJobType.핑크빈.getValue()
				&& JobType != MapleNewCharJobType.키네시스.getValue()) {
			bottom = rh.readInt();
		}
		int cape = 0;
		if (JobType == MapleNewCharJobType.팬텀.getValue() || JobType == MapleNewCharJobType.루미너스.getValue() || JobType == MapleNewCharJobType.제로.getValue()
				|| JobType == MapleNewCharJobType.은월.getValue()) {
			cape = rh.readInt();
		}
		int shoes = rh.readInt();
		int weapon = rh.readInt();
		int shield = 0;
		if (JobType == MapleNewCharJobType.데몬슬레이어.getValue()) {
			shield = rh.readInt();
		}
		if (!MapleCharacterUtil.canCreateChar(name) || isForbiddenName(name)) { // 생성
																												// 도중
																												// 중복닉네임
																												// 발견시
			c.send(MainPacketCreator.serverNotice(1, "캐릭터 생성도중 오류가 발생했습니다!"));
			c.send(LoginPacket.getLoginFailed(30));
			return;
		}
		newchar.setSubcategory(subCategory);
		newchar.setName(name);
		if (c.isGm()) {
			newchar.setGMLevel((byte) 6);
		}

		if (JobType == MapleNewCharJobType.모험가.getValue() || JobType == MapleNewCharJobType.듀얼블레이더.getValue()
				|| JobType == MapleNewCharJobType.캐논슈터.getValue()) { // 모험가
			newchar.setJob((short) 0);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1, (byte) 0));
		} else if (JobType == MapleNewCharJobType.레지스탕스.getValue()) { // 레지스탕스
			newchar.setJob((short) 3000);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161054, (byte) 0, (short) 1, (byte) 0));
			newchar.changeSkillLevel(SkillFactory.getSkill(30001061), (byte) 1, (byte) 1);
		} else if (JobType == MapleNewCharJobType.시그너스.getValue()) { // 시그너스
			newchar.setJob((short) 1000);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1, (byte) 0));
			newchar.changeSkillLevel(SkillFactory.getSkill(10001003), (byte) 1, (byte) 1); // 장인의
																							// 혼
			newchar.changeSkillLevel(SkillFactory.getSkill(10001244), (byte) 1, (byte) 1); // 엘리멘탈
																							// 슬래시
			newchar.changeSkillLevel(SkillFactory.getSkill(10001245), (byte) 1, (byte) 1); // 져니
																							// 홈
			newchar.changeSkillLevel(SkillFactory.getSkill(10000246), (byte) 1, (byte) 1); // 엘리멘탈
																							// 하모니
			newchar.changeSkillLevel(SkillFactory.getSkill(10000252), (byte) 1, (byte) 1); // 엘리멘탈
																							// 쉬프트
		} else if (JobType == MapleNewCharJobType.아란.getValue()) { // 아란
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1, (byte) 0));
			newchar.setJob((short) 2000);
		} else if (JobType == MapleNewCharJobType.에반.getValue()) { // 에반
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161052, (byte) 0, (short) 1, (byte) 0));
			newchar.setJob((short) 2001);
		} else if (JobType == MapleNewCharJobType.메르세데스.getValue()) { // 메르세데스
			newchar.setJob((short) 2002);
			newchar.changeSkillLevel(SkillFactory.getSkill(20020109), (byte) 1, (byte) 1); // 엘프의
																							// 회복
			newchar.changeSkillLevel(SkillFactory.getSkill(20021110), (byte) 1, (byte) 1); // 엘프의
																							// 축복
			newchar.changeSkillLevel(SkillFactory.getSkill(20020111), (byte) 1, (byte) 1); // 스타일리쉬
																							// 무브
			newchar.changeSkillLevel(SkillFactory.getSkill(20020112), (byte) 1, (byte) 1); // 왕의
																							// 자격
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161079, (byte) 0, (short) 1, (byte) 0));
		} else if (JobType == MapleNewCharJobType.데몬슬레이어.getValue()) { // 데몬슬레이어
			newchar.setJob((short) 3001);
			newchar.changeSkillLevel(SkillFactory.getSkill(30011109), (byte) 1, (byte) 1); // 데빌
																							// 윙즈
			newchar.changeSkillLevel(SkillFactory.getSkill(30010110), (byte) 1, (byte) 1); // 데몬
																							// 점프
		} else if (JobType == MapleNewCharJobType.제논.getValue()) { // 제논
			newchar.setJob((short) 3002);
			newchar.changeSkillLevel(SkillFactory.getSkill(30020232), (byte) 1, (byte) 1); // 서플러스
																							// 서플라이
			newchar.changeSkillLevel(SkillFactory.getSkill(30020233), (byte) 1, (byte) 1); // 하이브리드
																							// 로직
			newchar.changeSkillLevel(SkillFactory.getSkill(30020234), (byte) 1, (byte) 1); // 멀티래터럴
																							// I
			newchar.changeSkillLevel(SkillFactory.getSkill(30021235), (byte) 1, (byte) 1); // 프로멧사
																							// 어썰트
			newchar.changeSkillLevel(SkillFactory.getSkill(30021236), (byte) 1, (byte) 1); // 멀티
																							// 모드
																							// 링커
			newchar.changeSkillLevel(SkillFactory.getSkill(30021237), (byte) 1, (byte) 1); // 에비에이션
																							// 리버티
			newchar.changeSkillLevel(SkillFactory.getSkill(30020240), (byte) 1, (byte) 1); // 카모플라쥬
		} else if (JobType == MapleNewCharJobType.팬텀.getValue()) { // 팬텀
			newchar.setJob((short) 2003);
			newchar.changeSkillLevel(SkillFactory.getSkill(20031203), (byte) 1, (byte) 1); // 리턴
																							// 오브
																							// 팬텀
			newchar.changeSkillLevel(SkillFactory.getSkill(20030204), (byte) 1, (byte) 1); // 데들리
																							// 인스팅트
			newchar.changeSkillLevel(SkillFactory.getSkill(20031205), (byte) 1, (byte) 1); // 팬텀
																							// 슈라우드
			newchar.changeSkillLevel(SkillFactory.getSkill(20030206), (byte) 1, (byte) 1); // 하이
																							// 덱스터러티
			newchar.changeSkillLevel(SkillFactory.getSkill(20031207), (byte) 1, (byte) 1); // 스틸
																							// 스킬
			newchar.changeSkillLevel(SkillFactory.getSkill(20031208), (byte) 1, (byte) 1); // 스킬
																							// 매니지먼트
			newchar.changeSkillLevel(SkillFactory.getSkill(20031209), (byte) 1, (byte) 1); // 저지먼트
			newchar.changeSkillLevel(SkillFactory.getSkill(20031260), (byte) 1, (byte) 1); // 저지먼트
																							// AUTO
																							// /
																							// MANUAL
		} else if (JobType == MapleNewCharJobType.미하일.getValue()) { // 미하일
			newchar.setJob((short) 5000);
			newchar.changeSkillLevel(SkillFactory.getSkill(50001214), (byte) 1, (byte) 1); // 빛의
																							// 수호
		} else if (JobType == MapleNewCharJobType.루미너스.getValue()) { // 루미너스
			newchar.setJob((short) 2004);
			newchar.changeSkillLevel(SkillFactory.getSkill(20040219), (byte) 1, (byte) 1); // 이퀄리브리엄
			newchar.changeSkillLevel(SkillFactory.getSkill(20040216), (byte) 1, (byte) 1); // 선파이어
			newchar.changeSkillLevel(SkillFactory.getSkill(20040217), (byte) 1, (byte) 1); // 이클립스
			newchar.changeSkillLevel(SkillFactory.getSkill(20040218), (byte) 1, (byte) 1); // 퍼미에이트
			newchar.changeSkillLevel(SkillFactory.getSkill(20040221), (byte) 1, (byte) 1); // 파워오브라이트
			newchar.changeSkillLevel(SkillFactory.getSkill(20041222), (byte) 1, (byte) 1); // 라이트
																							// 블링크
		} else if (JobType == MapleNewCharJobType.카이저.getValue()) { // 카이저
			newchar.setJob((short) 6000);
			newchar.changeSkillLevel(SkillFactory.getSkill(60001216), (byte) 1, (byte) 1); // 리셔플
																							// 스위치
																							// :
																							// 방어모드
			newchar.changeSkillLevel(SkillFactory.getSkill(60001217), (byte) 1, (byte) 1); // 리셔플
																							// 스위치
																							// :
																							// 공격모드
			newchar.changeSkillLevel(SkillFactory.getSkill(60001218), (byte) 1, (byte) 1); // 바티컬커넥트
			newchar.changeSkillLevel(SkillFactory.getSkill(60001219), (byte) 1, (byte) 1); // 아이언
																							// 윌
			newchar.changeSkillLevel(SkillFactory.getSkill(60001220), (byte) 1, (byte) 1); // 트랜스피규레이션
			newchar.changeSkillLevel(SkillFactory.getSkill(60001225), (byte) 1, (byte) 1); // 커맨드
		} else if (JobType == MapleNewCharJobType.엔젤릭버스터.getValue()) { // 카이저
			newchar.setJob((short) 6001);
			newchar.changeSkillLevel(SkillFactory.getSkill(60011216), (byte) 1, (byte) 1); // 석세서
			newchar.changeSkillLevel(SkillFactory.getSkill(60011218), (byte) 1, (byte) 1); // 매지컬
																							// 리프트
			newchar.changeSkillLevel(SkillFactory.getSkill(60011219), (byte) 1, (byte) 1); // 소울
																							// 컨트랙트
			newchar.changeSkillLevel(SkillFactory.getSkill(60011220), (byte) 1, (byte) 1); // 데이드림
			newchar.changeSkillLevel(SkillFactory.getSkill(60011221), (byte) 1, (byte) 1); // 코디네이트
			newchar.changeSkillLevel(SkillFactory.getSkill(60011222), (byte) 1, (byte) 1); // 드레스
																							// 업
		} else if (JobType == MapleNewCharJobType.제로.getValue()) {
			newchar.setJob((short) 10112);
			newchar.setLevel(100);
			newchar.changeSkillLevel(SkillFactory.getSkill(100001262), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100000282), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100001263), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100001264), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100001265), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100001266), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100001268), (byte) 1, (byte) 1);
			newchar.changeSkillLevel(SkillFactory.getSkill(100000279), (byte) 5, (byte) 5);
		} else if (JobType == MapleNewCharJobType.은월.getValue()) {
			newchar.setJob((short) 2005);
		} else if (JobType == MapleNewCharJobType.핑크빈.getValue()) {
			newchar.setJob((short) 13100);
		} else if (JobType == MapleNewCharJobType.키네시스.getValue()) {
			newchar.setJob((short) 14000);
		}
		newchar.setMap(ServerConstants.startMap);
		MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
		Equip eq_top = new Equip(top, (short) -5, (byte) 0);
		eq_top.setWdef((short) 3);
		eq_top.setUpgradeSlots((byte) 7);
		eq_top.setExpiration(-1);
		equip.addFromDB(eq_top.copy());
		if (JobType == MapleNewCharJobType.데몬슬레이어.getValue()) {
			Equip shielde = new Equip(shield, (short) -10, (byte) 0);
			shielde.setMp((short) 110);
			shielde.setHp((short) 200);
			shielde.setUpgradeSlots((byte) 7);
			shielde.setExpiration(-1);
			equip.addFromDB(shielde.copy());
		}
		if (JobType == MapleNewCharJobType.카이저.getValue() || JobType == MapleNewCharJobType.엔젤릭버스터.getValue()) {
			Equip js = new Equip(1352504, (short) -10, (byte) 0);
			if (JobType == MapleNewCharJobType.카이저.getValue()) {
				js = null;
				js = new Equip(1352504, (short) -10, (byte) 0);
			} else if (JobType == MapleNewCharJobType.엔젤릭버스터.getValue()) {
				js = null;
				js = new Equip(1352600, (short) -10, (byte) 0);
			}
			js.setWdef((short) 5);
			js.setMdef((short) 5);
			js.setUpgradeSlots((byte) 7);
			js.setExpiration(-1);
			equip.addFromDB(js.copy());
		}
		Equip shoese = new Equip(shoes, (short) -7, (byte) 0);
		shoese.setWdef((short) 2);
		shoese.setUpgradeSlots((byte) 7);
		shoese.setExpiration(-1);
		equip.addFromDB(shoese.copy());
		if (JobType != MapleNewCharJobType.레지스탕스.getValue() && JobType != MapleNewCharJobType.메르세데스.getValue()
				&& JobType != MapleNewCharJobType.데몬슬레이어.getValue() && JobType != MapleNewCharJobType.루미너스.getValue()
				&& JobType != MapleNewCharJobType.카이저.getValue() && JobType != MapleNewCharJobType.엔젤릭버스터.getValue()
				&& JobType != MapleNewCharJobType.제논.getValue() && JobType != MapleNewCharJobType.모험가.getValue()
				&& JobType != MapleNewCharJobType.캐논슈터.getValue() && JobType != MapleNewCharJobType.듀얼블레이더.getValue()
				&& JobType != MapleNewCharJobType.팬텀.getValue() && JobType != MapleNewCharJobType.제로.getValue() && JobType != MapleNewCharJobType.핑크빈.getValue()
				&& JobType != MapleNewCharJobType.키네시스.getValue()) { // 데몬슬레이어,
																		// 레지스탕스,
																		// 메르세데스,
																		// 루미너스,
																		// 카이저,
																		// 엔버,
																		// 제논,
																		// 키네시스는
																		// 한벌옷.
			Equip bottome = new Equip(bottom, (short) -6, (byte) 0);
			bottome.setWdef((short) 2);
			bottome.setUpgradeSlots((byte) 7);
			bottome.setExpiration(-1);
			equip.addFromDB(bottome.copy());
		}
		if (JobType == MapleNewCharJobType.팬텀.getValue() || JobType == MapleNewCharJobType.루미너스.getValue() || JobType == MapleNewCharJobType.제로.getValue()
				|| JobType == MapleNewCharJobType.은월.getValue()) {
			Equip capee = new Equip(cape, (short) -9, (byte) 0);
			capee.setWdef((short) 5);
			capee.setMdef((short) 5);
			capee.setUpgradeSlots((byte) 7);
			capee.setExpiration(-1);
			equip.addFromDB(capee.copy());
		}
		Equip weapone = new Equip(weapon, (short) -11, (byte) 0);
		if (JobType == MapleNewCharJobType.루미너스.getValue()) {
			weapone.setMatk((short) 17);
		} else {
			weapone.setWatk((short) 17);
		}
		weapone.setUpgradeSlots((byte) 7);
		weapone.setExpiration(-1);
		equip.addFromDB(weapone.copy());
		if (JobType == MapleNewCharJobType.제로.getValue()) {
			Equip js = new Equip(1562000, (short) -10, (byte) 0);
			weapone.setUpgradeSlots((byte) 7);
			weapone.setExpiration(-1);
			equip.addFromDB(js.copy());
		}
		if (MapleCharacterUtil.canCreateChar(name) && !isForbiddenName(name)) {
			MapleCharacter.saveNewCharToDB(newchar);
			MapleItempotMain.getInstance().newCharDB(newchar.getId());
			c.getSession().write(LoginPacket.addNewCharacterEntry(newchar, true));
			c.createdChar(newchar.getId());
		} else {
			c.getSession().write(LoginPacket.addNewCharacterEntry(newchar, false));
		}
		newchar = null;
	}

	public static void DeleteChar(ReadingMaple rh, MapleClient c) {
		String Secondpw_Client = rh.readMapleAsciiString();
		int Character_ID = rh.readInt();
		MapleCharacter chr = MapleCharacter.loadCharFromDB(Character_ID, c, false);
		if (!c.login_Auth(Character_ID)) {
			c.getSession().closeNow();
			return; // Attempting to delete other character
		}
		byte state = 0;
		//TODO 캐릭터 삭제 가능 조건 로직
//		if (chr.getMeso() < 5000000) {
//			c.getSession().write(MainPacketCreator.serverNotice(1, "캐릭터 삭제를 하기위해선 삭제하고자 하는 캐릭터에 5,000,000 메소를 소지하고 있어야 합니다."));
//			c.getSession().write(LoginPacket.getLoginFailed(20));
//			return;
//		}
		if (Secondpw_Client == null) { // Client's hacking
			c.getSession().closeNow();
			return;
		} else {
			if (!c.CheckSecondPassword(Secondpw_Client)) { // Wrong Password
				state = 0x14;
			}
		}
		if (state == 0) {
			if (!c.deleteCharacter(Character_ID)) {
				state = 1; // actually something else would be good o.o
			}
		}
		c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));
	}

	public static void Character_WithSecondPassword(ReadingMaple rh, MapleClient c) {
		String password = rh.readMapleAsciiString();
		int charId = rh.readInt();

		if (c.getSecondPassword() == null || !c.login_Auth(charId)) {
			c.getSession().closeNow();
			return;
		}
		if (c.CheckSecondPassword(password)) {
			c.updateLoginState(AccountStatusType.SECOND_LOGIN.getValue(), c.getSessionIPAddress());
			c.getSession().write(MainPacketCreator.getServerIP(c, ServerConstants.channelPort + c.getChannel(), ServerConstants.buddyChatPort, charId));
		} else {
			c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
		}
	}

	public static void updateCharCard(ReadingMaple rh, MapleClient c) {
		if (!c.isLoggedIn()) {
			c.getSession().closeNow();
			return;
		}
		Map<Integer, Integer> cid = new LinkedHashMap<Integer, Integer>();

		for (int i = 1; i <= 6; i++) {
			int charid = rh.readInt();
			cid.put(i, charid);
		}
		c.updateCharCard(cid);
	}
}
