/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package constants;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class ServerConstants {
	
	/*
     * ������ ���� ȯ������ ����
     */
	public static final boolean isLocal = true; //TODO log4j �������� �����ʿ�
	public static final boolean realese = false; //TODO log4j �������� �����ʿ�
    public static final String ROOT_PATH = "";
    public static final String LOCAL_ROOT_PATH = "D:/KMS_PROJECT/KMS_253_SERVER/";
	public static int createMaxAccount = 2;
	public static int loginTryMaxCount = 7;
	
    /* ���� ���� */
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
    
    /* DB ���� */ 
    public static int dbPort = 3306;
    public static String dbHost = "127.0.0.1";
    public static String dbUser = "root";
    public static String dbPassword = "root";
    
    /* Message ���� �� �̺�Ʈ ����*/
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
    public static String serverCheckMessage = "���� ���� ���� ���Դϴ�.";
    
    /* ���� ���� */    
    public static boolean serverCheck;
    
    /* ��Ÿ ���� */
    public static boolean UnlockMaxDamage = true;
    public static boolean feverTime = true;
    public static boolean useMaxDrop;
    public static boolean useBossMaxDrop;
    public static boolean showPackets; 
    public static String path = ""; 
    public static String windowsDumpPath = ""; 
    
    /* ���� ���� */
    public static short MAPLE_VERSION;
    public static byte subVersion;
    public static final byte check = 1;
    
    /* ��Ÿ ���� 2 */
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
     * ���� ������ ������Ƽ ������ �ε�ó�� 
     */
    public static void loadServerSetProp() {
        try {
        	
            FileInputStream fis = new FileInputStream(getRootPath() + "Settings/ServerSettings.properties");
            Properties prop = new Properties();
            prop.load(fis);
            fis.close(); fis = null;
            
            defaultFlag = Byte.parseByte(prop.getProperty(toUni("Flag")));
            Host = new String(prop.getProperty(toUni("������")).getBytes("ISO-8859-1"), "euc-kr");
            serverCount = Integer.parseInt(prop.getProperty(toUni("��������")));
            LoginPort = Integer.parseInt(prop.getProperty(toUni("�α�����Ʈ")));
            ChannelPort = Integer.parseInt(prop.getProperty(toUni("ä����Ʈ"))); basePorts = ChannelPort;
            CashShopPort = Integer.parseInt(prop.getProperty(toUni("ĳ�ü���Ʈ")));
            BuddyChatPort = Integer.parseInt(prop.getProperty(toUni("ģ��������Ʈ")));
            
            createMaxAccount = Integer.parseInt(prop.getProperty(toUni("�ִ������")));
            loginTryMaxCount = Integer.parseInt(prop.getProperty(toUni("�α��νõ�Ƚ��")));
            
            defaultExpRate = Integer.parseInt(prop.getProperty(toUni("����ġ����")));
            defaultDropRate = Integer.parseInt(prop.getProperty(toUni("��ӹ���")));
            defaultMesoRate = Integer.parseInt(prop.getProperty(toUni("�޼ҹ���")));
            defaultCashRate = Integer.parseInt(prop.getProperty(toUni("ĳ�ù���")));
            defaultBossCashRate = Integer.parseInt(prop.getProperty(toUni("����ĳ�ù���")));
            
            cshopNpc = Integer.parseInt(prop.getProperty(toUni("ĳ�ü�NPC")));
            
            serverName = new String(prop.getProperty(toUni("�����̸�")).getBytes("ISO-8859-1"), "euc-kr");
            serverMessage = new String(prop.getProperty(toUni("�����޼���")).getBytes("ISO-8859-1"), "euc-kr");
            serverWelcome = new String(prop.getProperty(toUni("����ȯ���޼���")).getBytes("ISO-8859-1"), "euc-kr");
            eventMessage = new String(prop.getProperty(toUni("�̺�Ʈ�޼���")).getBytes("ISO-8859-1"), "euc-kr");
            beginner = new String(prop.getProperty(toUni("ó�����۰���")).getBytes("ISO-8859-1"), "euc-kr");
            serverNotice = new String(prop.getProperty(toUni("�����˸��޼���")).getBytes("ISO-8859-1"), "euc-kr"); 
            serverNotititle = new String(prop.getProperty(toUni("������������")).getBytes("ISO-8859-1"), "euc-kr");
            serverNotification = new String(prop.getProperty(toUni("������������")).getBytes("ISO-8859-1"), "euc-kr");
            recommendMessage = new String(prop.getProperty(toUni("��õ�޼���")).getBytes("ISO-8859-1"), "euc-kr");
            serverHint = new String(prop.getProperty(toUni("������Ʈ")).getBytes("ISO-8859-1"), "euc-kr");
            
            dbHost = new String(prop.getProperty(toUni("Arc.dbHost")).getBytes("ISO-8859-1"), "euc-kr");
            dbPort = Integer.parseInt(prop.getProperty(toUni("Arc.dbPort")));
            dbUser = new String(prop.getProperty(toUni("Arc.dbUser")).getBytes("ISO-8859-1"), "euc-kr");
            dbPassword = new String(prop.getProperty(toUni("Arc.dbPassword")).getBytes("ISO-8859-1"), "euc-kr");
            
            events = new String(prop.getProperty(toUni("�̺�Ʈ")).getBytes("ISO-8859-1"), "euc-kr");
            
            startMap = Integer.parseInt(prop.getProperty(toUni("���۸�")));
            serverHint = new String(prop.getProperty(toUni("������Ʈ")).getBytes("ISO-8859-1"), "euc-kr");
            
            MAPLE_VERSION = Short.parseShort(prop.getProperty(toUni("Ŭ���̾�Ʈ����")));
            subVersion = Byte.parseByte(prop.getProperty(toUni("���̳ʹ���")));
            
            path = new String(prop.getProperty(toUni("�ɼǰ������")).getBytes("ISO-8859-1"), "euc-kr");
            windowsDumpPath = new String(prop.getProperty(toUni("�����������")).getBytes("ISO-8859-1"), "euc-kr");
            
            serverCheck = Boolean.parseBoolean(prop.getProperty(toUni("��������")));
            showPackets = Boolean.parseBoolean(prop.getProperty(toUni("��Ŷ���")));
            useMaxDrop = Boolean.parseBoolean(prop.getProperty(toUni("�ִ������")));
            useBossMaxDrop = Boolean.parseBoolean(prop.getProperty(toUni("�ִ뺸��������")));
                        
            bossMaxDrop = Integer.parseInt(prop.getProperty(toUni("�ִ뺸����������۰���")));
            maxDrop = Integer.parseInt(prop.getProperty(toUni("�ִ��������۰���")));
                        
        } catch (Exception e) {
            System.err.println("[����] ���� ���������� �ҷ����µ� �����Ͽ����ϴ�.");
            if (!realese) {
                e.printStackTrace();
            }
        }
    }

    protected static String toUni(String kor) throws UnsupportedEncodingException {
        return new String(kor.getBytes("KSC5601"), "8859_1");
    }
}
