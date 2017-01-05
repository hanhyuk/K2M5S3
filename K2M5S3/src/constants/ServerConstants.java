package constants;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO 서버셋팅 할때 ServerSettings.properties 파일 사용하는데...
 * 운영만 하는 사람의 입장에서 필요한 것들만 남기고 모두 다 이 클래스 안에 하드코딩 하는걸로 수정하자.
 */
public class ServerConstants {
	private static final Logger logger = LoggerFactory.getLogger(ServerConstants.class);
	
	/**
     * 서버를 구동 할때 로컬 환경 여부 true(로컬)
     */
	private static final boolean IS_LOCAL = true;
    /**
     * jar 파일로 서버 구동 할때 최상위 디렉토리 경로
     */
    public static final String ROOT_PATH = "";
    /**
     * jar 가 아닌 로컬환경에서 서버 구동 할때 최상위 디렉토리 경로
     */
    public static final String LOCAL_ROOT_PATH = "D:/KMS_PROJECT/KMS_253_SERVER/";
    public static final String CONFIG_RECV_PACKET_INI_PATH = "config/recvPacket.ini";
    public static final String CONFIG_SEND_PACKET_INI_PATH = "config/sendPacket.ini";
    public static final String CONFIG_SERVER_SETTING_PROP_PATH = "config/serverSettings.properties";
    public static final String CONFIG_REWARD_SCROLL_PROP_PATH = "config/rewardScroll.properties";
    public static final String CONFIG_LOG_FILE_PATH = "logs/log";
    
    /**
     * TODO 어떤 용도인지? 
     */
    public static final byte DEFAULT_FLAG = 2;
    /**
     *  이 값이 true 이면 최대 데미지를 21억으로 상향하기 위해 플레이어에게 륀느의 가호 버프를 건다.
     */
    public static final boolean IS_UNLOCK_MAX_DAMAGE = true;
    /**
     * 강화 확률을 상향 적용 한다.
     * true(상향), 그외 미적용
     * TODO 어느 부분에 구체적인 영향을 주는지 검토 필요.
     */
    public static final boolean IS_FEVER_TIME = false;    
    /**
     * KMS 메이저 버전 
     */
    public static final short MAPLE_VERSION = 253;
    /**
     * KMS 마이너 버전
     */
    public static final byte SUB_VERSION = 1;
    /**
     * ?
     */
    public static final byte CHECK = 1;
    
    
    /**
     * 서버가 사용하는 IP 
     */
    public static String host;
    /**
     * 시작맵
     */
    public static int startMap;
    /**
     * 오픈 할 채널 서버의 최대수
     */
    public static int openChannelCount;
    /**
     * 로그인서버 포트
     */
    public static int loginPort; 
    /**
     * 채널서버 포트
     */
    public static int channelPort;
    /**
     * 캐시샵서버 포트
     */
    public static int cashShopPort;
    /**
     * 버디챗서버 포트
     */
    public static int buddyChatPort;
    /**
     * 경험치 배율 
     */
    public static int defaultExpRate;
    /**
     * 메소 배율 
     */
    public static int defaultMesoRate;
    /**
     * 드랍 배율
     */
    public static int defaultDropRate;
    /**
     * 캐시 배율
     */
    public static int defaultCashRate;
    /**
     * 보스 캐시 배율
     */
    public static int defaultBossCashRate;
    /**
     * 캐시샵 NPC
     */
    public static int cshopNpc; 
    /**
     * 현재 ControlUnit 에서 채팅 로그를 보여주는데 채팅 건수를 누적하는데 이 변수를 사용한다.
     * 그리고 500 건 이상이 되면 채팅 로그를 초기화 한다.
     * TODO 채팅 로그의 경우 파일로 떨구게 만들고, 관리기에서는 보이지 않도록 하는 방향 검통 필요.
     */
    public static int chatlimit = 0;
    /**
     * DB 연결 포트 
     */
    public static int dbPort = 3306;
    /**
     * DB 연결 IP
     */
    public static String dbHost = "127.0.0.1";
    /**
     * DB 연결 ID
     */
    public static String dbUser = "root";
    /**
     * DB 연결 비밀번호
     */
    public static String dbPassword = "root";
    /**
     * @deprecated 추천 메세지. 
     * TODO 실제 클라에서 언제 보여지는 건지 확인이 안됨. 
     */
    public static String recommendMessage = "";
    /**
     * 서버 이름
     */
    public static String serverName;
    /**
     * ingame 했을때 화면 상단에 좌로 흘러가는 노란색 메세지 
     */
    public static String serverMessage;
    /**
     * 1차 로그인 이후 월드 선택 하면 채널 선택하는 화면 위쪽에 말풍선 형태로 보여지는 메세지  
     */
    public static String eventMessage;
    /**
     * ingame 했을때 시작맵(startMap)에 있을 경우 라즐리(제로무기 태도)가 보여주는 메세지 
     */
    public static String startMapMessage;
    /**
     * ingame 했을때 라즐리(제로무기 태도)가 보여주는 메세지 
     */
    public static String serverNotice;
    /**
     * ingame 했을때 캐릭터 머리위에 말풍선 형태로 보여지는 메세지
     * 일정시간 후 자동으로 사라짐. 
     */
    public static String serverHint;
    /**
     * ingame 했을때 투명 배경의 팝업창 형태로 보여지는 메세지 제목
     * 일정시간 후 자동으로 사라짐.
     */
    public static String serverNotititle;
    /**
     * ingame 했을때 투명 배경의 팝업창 형태로 보여지는 메세지
     * 일정시간 후 자동으로 사라짐.
     */
    public static String serverNotification;
    /**
     * 이벤트로 등록할 스크립트 파일 명(파일이 여러개일 경우 콤마(,)로 구분)
     */
    public static String events;
    /**
     * 서버 패치 이후 테스트의 목적으로 서버 구동 후에 GM 권한을 가진 계정만 로그인 할수 있도록 설정 할때 사용
     */
    public static boolean serverCheck = false;
    /**
     * serverCheck 값이 true 일때 사용자가 로그인 시도 할때 보여주는 메세지
     */
    public static String serverCheckMessage = " 점검중.";
    /**
     * maxDrop 값 적용 여부
     */
    public static boolean useMaxDrop;
    /**
     * bossMaxDrop 값 적용 여부
     */
    public static boolean useBossMaxDrop;
    /**
     * 최대 드랍 아이템 수
     */
    public static int maxDrop;
    /**
     * 최대 보스 드랍 아이템 수
     */
    public static int bossMaxDrop;

    private static Properties properties = null;
    private static final String DEFAULT_CHARSET = Charset.defaultCharset().name();
    
    public static String getRootPath() {
    	if( IS_LOCAL ) {
    		return LOCAL_ROOT_PATH;
    	} else {
    		return ROOT_PATH;
    	}
    }
    
    /**
     * 프로퍼티 설정값 로딩 
     */
    public static void init() {
    	FileInputStream fis = null;
        try {
            fis = new FileInputStream(getRootPath() + CONFIG_SERVER_SETTING_PROP_PATH);
            properties = new Properties();
            properties.load(fis);
            
            bossMaxDrop = getInt("최대보스드랍아이템개수");
            maxDrop = getInt("최대드랍아이템개수");
            openChannelCount = getInt("오픈채널수");
            loginPort = getInt("로그인포트");
            channelPort = getInt("채널포트");
            cashShopPort = getInt("캐시샵포트");
            buddyChatPort = getInt("친구서버포트");
            defaultExpRate = getInt("경험치배율");
            defaultDropRate = getInt("드롭배율");
            defaultMesoRate = getInt("메소배율");
            defaultCashRate = getInt("캐시배율");
            defaultBossCashRate = getInt("보스캐시배율");
            cshopNpc = getInt("캐시샵NPC");
            startMap = getInt("시작맵");
            
            host = getString("아이피");
            serverName = getString("서버이름");
            serverMessage = getString("서버메세지");
            eventMessage = getString("이벤트메세지");
            startMapMessage = getString("처음시작공지");
            serverNotice = getString("서버알림메세지"); 
            serverNotititle = getString("서버공지제목");
            serverNotification = getString("서버공지내용");
            serverHint = getString("서버힌트");
            dbHost = getString("디비아이피");
            dbPort = getInt("디비포트");
            dbUser = getString("디비아이디");
            dbPassword = getString("디비비밀번호");
            events = getString("이벤트");
            serverHint = getString("서버힌트");
            serverCheckMessage = getString("서버점검메세지");

            serverCheck = getBoolean("서버점검");
            useMaxDrop = getBoolean("최대드랍사용");
            useBossMaxDrop = getBoolean("최대보스드랍사용");
                        
        } catch (Exception e) {
            logger.error("[오류] 서버 세팅파일을 불러오는데 실패하였습니다.", e);
        } finally {
        	try {
				if( fis != null ) {
					fis.close(); fis = null;
				}
			} catch(Exception e) {
				logger.debug("{}", e);
			}
        }
    }
    
    public static int getInt(final String key) {
    	return (Integer)getValue(key, PropertiesValueType.INT);
    }
    
    public static String getString(final String key) {
    	return (String)getValue(key, PropertiesValueType.STRING);
    }
    
    public static boolean getBoolean(final String key) {
    	return (Boolean)getValue(key, PropertiesValueType.BOOLEAN);
    }
    
    private enum PropertiesValueType {
    	INT, STRING, BOOLEAN
    }
    
    private static Object getValue(final String key, final PropertiesValueType type) {
    	Object result = null;
    	try {
    		if( PropertiesValueType.STRING == type ) {
        		result = new String(properties.getProperty(convert(key)).getBytes("ISO-8859-1"), "euc-kr");
        	} else if( PropertiesValueType.INT == type ) {
        		result =  new Integer(properties.getProperty(convert(key)));
        	} else if( PropertiesValueType.BOOLEAN == type ) {
        		result =  new Boolean(properties.getProperty(convert(key)));
        	} else {
        		result =  null;
        	}	
    	} catch(Exception e) {
    		logger.debug("key : {}", key, e);
    	}
    	return result;
    }
    
    private static String convert(final String kor) throws UnsupportedEncodingException {
    	return new String(kor.getBytes(DEFAULT_CHARSET), "ISO-8859-1");
    }
}
