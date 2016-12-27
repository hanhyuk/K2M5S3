/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package constants;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class ServerConstants {
	
	/*
     * 서버가 로컬 환경인지 여부
     */
	public static final boolean isLocal = true; //TODO log4j 설정으로 변경필요
	public static final boolean realese = false; //TODO log4j 설정으로 변경필요
    public static final String ROOT_PATH = "";
    public static final String LOCAL_ROOT_PATH = "D:/KMS_PROJECT/KMS_253_SERVER/";
	public static int createMaxAccount = 2;
	public static int loginTryMaxCount = 7;
	
    /* 서버 설정 */
    public static String Host;
    public static int startMap;
    public static byte defaultFlag;
    public static int serverCount;
    public static int LoginPort; 
    public static int ChannelPort;
    public static int CashShopPort;
    public static int BuddyChatPort;
    
    
    
        
    public static int defaultExpRate;
    public static int defaultMesoRate;
    public static int defaultDropRate;
    public static int defaultCashRate;
    public static int defaultBossCashRate;
    public static int maxDrop;
    public static int bossMaxDrop;
    
    public static int defaultMaxChannelLoad = 50;
    
    public static int cshopNpc = 0; 
    public static int chatlimit = 0;
    
    /* DB 설정 */ 
    public static int dbPort = 3306;
    public static String dbHost = "127.0.0.1";
    public static String dbUser = "root";
    public static String dbPassword = "root";
    
    /* Message 설정 및 이벤트 설정*/
    public static String recommendMessage = "";
    public static String serverName = "";
    public static String serverMessage = "";
    public static String serverWelcome = "";
    public static String eventMessage = "";
    public static String serverHint = "";
    public static String beginner = "";
    public static String serverNotice = "";
    public static String serverNotititle = "";
    public static String serverNotification = "";
    public static String events = "";
    public static String serverCheckMessage = "현재 서버 점검 중입니다.";
    
    /* 개발 설정 */    
    public static boolean serverCheck;
    
    /* 기타 설정 */
    public static boolean UnlockMaxDamage = true;
    public static boolean feverTime = true;
    public static boolean useMaxDrop;
    public static boolean useBossMaxDrop;
    public static boolean showPackets; 
    public static String path = ""; 
    public static String windowsDumpPath = ""; 
    
    /* 버전 설정 */
    public static short MAPLE_VERSION;
    public static byte subVersion;
    public static final byte check = 1;
    
    /* 기타 설정 2 */
    public static String hp_skillid_dummy = ""; 
    public static String hp_skillid_real[]; 
    
    public static int basePorts;
    
    public static String getRootPath() {
    	if( isLocal ) {
    		return LOCAL_ROOT_PATH;
    	} else {
    		return ROOT_PATH;
    	}
    }
    
    /**
     * 서버 관리용 프로퍼티 설정값 로딩처리 
     */
    public static void loadServerSetProp() {
        try {
        	
            FileInputStream fis = new FileInputStream(getRootPath() + "Settings/ServerSettings.properties");
            Properties prop = new Properties();
            prop.load(fis);
            fis.close(); fis = null;
            
            defaultFlag = Byte.parseByte(prop.getProperty(toUni("Flag")));
            Host = new String(prop.getProperty(toUni("아이피")).getBytes("ISO-8859-1"), "euc-kr");
            serverCount = Integer.parseInt(prop.getProperty(toUni("서버개수")));
            LoginPort = Integer.parseInt(prop.getProperty(toUni("로그인포트")));
            ChannelPort = Integer.parseInt(prop.getProperty(toUni("채널포트"))); basePorts = ChannelPort;
            CashShopPort = Integer.parseInt(prop.getProperty(toUni("캐시샵포트")));
            BuddyChatPort = Integer.parseInt(prop.getProperty(toUni("친구서버포트")));
            
            createMaxAccount = Integer.parseInt(prop.getProperty(toUni("최대계정수")));
            loginTryMaxCount = Integer.parseInt(prop.getProperty(toUni("로그인시도횟수")));
            
            defaultExpRate = Integer.parseInt(prop.getProperty(toUni("경험치배율")));
            defaultDropRate = Integer.parseInt(prop.getProperty(toUni("드롭배율")));
            defaultMesoRate = Integer.parseInt(prop.getProperty(toUni("메소배율")));
            defaultCashRate = Integer.parseInt(prop.getProperty(toUni("캐시배율")));
            defaultBossCashRate = Integer.parseInt(prop.getProperty(toUni("보스캐시배율")));
            
            cshopNpc = Integer.parseInt(prop.getProperty(toUni("캐시샵NPC")));
            
            serverName = new String(prop.getProperty(toUni("서버이름")).getBytes("ISO-8859-1"), "euc-kr");
            serverMessage = new String(prop.getProperty(toUni("서버메세지")).getBytes("ISO-8859-1"), "euc-kr");
            serverWelcome = new String(prop.getProperty(toUni("서버환영메세지")).getBytes("ISO-8859-1"), "euc-kr");
            eventMessage = new String(prop.getProperty(toUni("이벤트메세지")).getBytes("ISO-8859-1"), "euc-kr");
            beginner = new String(prop.getProperty(toUni("처음시작공지")).getBytes("ISO-8859-1"), "euc-kr");
            serverNotice = new String(prop.getProperty(toUni("서버알림메세지")).getBytes("ISO-8859-1"), "euc-kr"); 
            serverNotititle = new String(prop.getProperty(toUni("서버공지제목")).getBytes("ISO-8859-1"), "euc-kr");
            serverNotification = new String(prop.getProperty(toUni("서버공지내용")).getBytes("ISO-8859-1"), "euc-kr");
            recommendMessage = new String(prop.getProperty(toUni("추천메세지")).getBytes("ISO-8859-1"), "euc-kr");
            serverHint = new String(prop.getProperty(toUni("서버힌트")).getBytes("ISO-8859-1"), "euc-kr");
            
            dbHost = new String(prop.getProperty(toUni("Arc.dbHost")).getBytes("ISO-8859-1"), "euc-kr");
            dbPort = Integer.parseInt(prop.getProperty(toUni("Arc.dbPort")));
            dbUser = new String(prop.getProperty(toUni("Arc.dbUser")).getBytes("ISO-8859-1"), "euc-kr");
            dbPassword = new String(prop.getProperty(toUni("Arc.dbPassword")).getBytes("ISO-8859-1"), "euc-kr");
            
            events = new String(prop.getProperty(toUni("이벤트")).getBytes("ISO-8859-1"), "euc-kr");
            
            startMap = Integer.parseInt(prop.getProperty(toUni("시작맵")));
            serverHint = new String(prop.getProperty(toUni("서버힌트")).getBytes("ISO-8859-1"), "euc-kr");
            
            MAPLE_VERSION = Short.parseShort(prop.getProperty(toUni("클라이언트버전")));
            subVersion = Byte.parseByte(prop.getProperty(toUni("마이너버전")));
            
            path = new String(prop.getProperty(toUni("옵션경로지정")).getBytes("ISO-8859-1"), "euc-kr");
            windowsDumpPath = new String(prop.getProperty(toUni("덤프경로지정")).getBytes("ISO-8859-1"), "euc-kr");
            
            serverCheck = Boolean.parseBoolean(prop.getProperty(toUni("서버점검")));
            showPackets = Boolean.parseBoolean(prop.getProperty(toUni("패킷출력")));
            useMaxDrop = Boolean.parseBoolean(prop.getProperty(toUni("최대드랍사용")));
            useBossMaxDrop = Boolean.parseBoolean(prop.getProperty(toUni("최대보스드랍사용")));
                        
            bossMaxDrop = Integer.parseInt(prop.getProperty(toUni("최대보스드랍아이템개수")));
            maxDrop = Integer.parseInt(prop.getProperty(toUni("최대드랍아이템개수")));
                        
        } catch (Exception e) {
            System.err.println("[오류] 서버 세팅파일을 불러오는데 실패하였습니다.");
            if (!realese) {
                e.printStackTrace();
            }
        }
    }

    protected static String toUni(String kor) throws UnsupportedEncodingException {
        return new String(kor.getBytes("KSC5601"), "8859_1");
    }
}
