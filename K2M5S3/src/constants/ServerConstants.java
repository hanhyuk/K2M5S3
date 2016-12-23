/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package constants;

import client.MapleClient;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Properties;

public class ServerConstants {
    
    /* ���� ���� */
    public static String Host;
    public static int startMap;
    public static byte defaultFlag;
    public static int serverCount;
    public static int LoginPort; 
    public static int ChannelPort;
    public static int CashShopPort;
    public static int BuddyChatPort;
    
    public static boolean isLocal = false;
        
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
    public static int dbPort;
    public static String dbHost;
    public static String dbUser;
    public static String dbPassword;
    
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
    public static String serverCheckMessage = "���� " + serverName + " ���� ���� ���Դϴ�.\r\n �ڼ��� ������ Ȩ�������� �����Ͽ� �ֽʽÿ�.\r\n [���� : �ý��� ����ȭ]";
    
    /* ���� ���� */    
    public static boolean serverCheck;
    
    /* ��Ÿ ���� */
    public static boolean UnlockMaxDamage = true;
    public static boolean feverTime = true;
    public static boolean useMaxDrop;
    public static boolean useBossMaxDrop;
    public static boolean showPackets; 
    public static boolean realese = true; 
    public static String path = ""; 
    public static String windowsDumpPath = ""; 
    
    /* ���� ���� */
    public static short MAPLE_VERSION;
    public static byte subVersion;
    public static final byte check = 1;
    
    /* ��Ÿ ���� 2 */
    public static String hp_skillid_dummy = ""; 
    public static String hp_skillid_real[]; 
     
    static {
        try {
            FileInputStream setting = new FileInputStream("Settings/ServerSettings.properties");
            Properties setting_ = new Properties();
            setting_.load(setting);
            setting.close();
            defaultFlag = Byte.parseByte(setting_.getProperty(toUni("Flag")));
            Host = new String(setting_.getProperty(toUni("������")).getBytes("ISO-8859-1"), "euc-kr");
            serverCount = Integer.parseInt(setting_.getProperty(toUni("��������")));
            LoginPort = Integer.parseInt(setting_.getProperty(toUni("�α�����Ʈ")));
            ChannelPort = Integer.parseInt(setting_.getProperty(toUni("ä����Ʈ")));
            CashShopPort = Integer.parseInt(setting_.getProperty(toUni("ĳ�ü���Ʈ")));
            BuddyChatPort = Integer.parseInt(setting_.getProperty(toUni("ģ��������Ʈ")));
            
            defaultExpRate = Integer.parseInt(setting_.getProperty(toUni("����ġ����")));
            defaultDropRate = Integer.parseInt(setting_.getProperty(toUni("��ӹ���")));
            defaultMesoRate = Integer.parseInt(setting_.getProperty(toUni("�޼ҹ���")));
            defaultCashRate = Integer.parseInt(setting_.getProperty(toUni("ĳ�ù���")));
            defaultBossCashRate = Integer.parseInt(setting_.getProperty(toUni("����ĳ�ù���")));
            
            cshopNpc = Integer.parseInt(setting_.getProperty(toUni("ĳ�ü�NPC")));
            
            serverName = new String(setting_.getProperty(toUni("�����̸�")).getBytes("ISO-8859-1"), "euc-kr");
            serverMessage = new String(setting_.getProperty(toUni("�����޼���")).getBytes("ISO-8859-1"), "euc-kr");
            serverWelcome = new String(setting_.getProperty(toUni("����ȯ���޼���")).getBytes("ISO-8859-1"), "euc-kr");
            eventMessage = new String(setting_.getProperty(toUni("�̺�Ʈ�޼���")).getBytes("ISO-8859-1"), "euc-kr");
            beginner = new String(setting_.getProperty(toUni("ó�����۰���")).getBytes("ISO-8859-1"), "euc-kr");
            serverNotice = new String(setting_.getProperty(toUni("�����˸��޼���")).getBytes("ISO-8859-1"), "euc-kr"); 
            serverNotititle = new String(setting_.getProperty(toUni("������������")).getBytes("ISO-8859-1"), "euc-kr");
            serverNotification = new String(setting_.getProperty(toUni("������������")).getBytes("ISO-8859-1"), "euc-kr");
            recommendMessage = new String(setting_.getProperty(toUni("��õ�޼���")).getBytes("ISO-8859-1"), "euc-kr");
            serverHint = new String(setting_.getProperty(toUni("������Ʈ")).getBytes("ISO-8859-1"), "euc-kr");
            
            dbHost = new String(setting_.getProperty(toUni("Arc.dbHost")).getBytes("ISO-8859-1"), "euc-kr");
            dbPort = Integer.parseInt(setting_.getProperty(toUni("Arc.dbPort")));
            dbUser = new String(setting_.getProperty(toUni("Arc.dbUser")).getBytes("ISO-8859-1"), "euc-kr");
            dbPassword = new String(setting_.getProperty(toUni("Arc.dbPassword")).getBytes("ISO-8859-1"), "euc-kr");
            
            events = new String(setting_.getProperty(toUni("�̺�Ʈ")).getBytes("ISO-8859-1"), "euc-kr");
            
            startMap = Integer.parseInt(setting_.getProperty(toUni("���۸�")));
            serverHint = new String(setting_.getProperty(toUni("������Ʈ")).getBytes("ISO-8859-1"), "euc-kr");
            
            MAPLE_VERSION = Short.parseShort(setting_.getProperty(toUni("Ŭ���̾�Ʈ����")));
            subVersion = Byte.parseByte(setting_.getProperty(toUni("���̳ʹ���")));
            
            path = new String(setting_.getProperty(toUni("�ɼǰ������")).getBytes("ISO-8859-1"), "euc-kr");
            windowsDumpPath = new String(setting_.getProperty(toUni("�����������")).getBytes("ISO-8859-1"), "euc-kr");
            
            serverCheck = Boolean.parseBoolean(setting_.getProperty(toUni("��������")));
            showPackets = Boolean.parseBoolean(setting_.getProperty(toUni("��Ŷ���")));
            useMaxDrop = Boolean.parseBoolean(setting_.getProperty(toUni("�ִ������")));
            useBossMaxDrop = Boolean.parseBoolean(setting_.getProperty(toUni("�ִ뺸��������")));
                        
            bossMaxDrop = Integer.parseInt(setting_.getProperty(toUni("�ִ뺸����������۰���")));
            maxDrop = Integer.parseInt(setting_.getProperty(toUni("�ִ��������۰���")));
                        
        } catch (Exception e) {
            System.err.println("[����] ���� ���������� �ҷ����µ� �����Ͽ����ϴ�.");
            if (!realese) {
                e.printStackTrace();
            }
        }
    }
    
    public static int basePorts = (isLocal ? 100 : 0) + (ChannelPort); 

    protected static String toUni(String kor)
            throws UnsupportedEncodingException {
        return new String(kor.getBytes("KSC5601"), "8859_1");
    }
    
    public static String getServerHost(MapleClient ha) {
        try {
            return InetAddress.getByName(ServerConstants.Host).getHostAddress().replace("/", "");
        } catch (Exception e) {
            if (!ServerConstants.realese)
                e.printStackTrace();
        }
        return ServerConstants.Host;
    } 
}
