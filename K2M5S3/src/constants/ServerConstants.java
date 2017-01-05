package constants;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO �������� �Ҷ� ServerSettings.properties ���� ����ϴµ�...
 * ��� �ϴ� ����� ���忡�� �ʿ��� �͵鸸 ����� ��� �� �� Ŭ���� �ȿ� �ϵ��ڵ� �ϴ°ɷ� ��������.
 */
public class ServerConstants {
	private static final Logger logger = LoggerFactory.getLogger(ServerConstants.class);
	
	/**
     * ������ ���� �Ҷ� ���� ȯ�� ���� true(����)
     */
	private static final boolean IS_LOCAL = true;
    /**
     * jar ���Ϸ� ���� ���� �Ҷ� �ֻ��� ���丮 ���
     */
    public static final String ROOT_PATH = "";
    /**
     * jar �� �ƴ� ����ȯ�濡�� ���� ���� �Ҷ� �ֻ��� ���丮 ���
     */
    public static final String LOCAL_ROOT_PATH = "D:/KMS_PROJECT/KMS_253_SERVER/";
    public static final String CONFIG_RECV_PACKET_INI_PATH = "config/recvPacket.ini";
    public static final String CONFIG_SEND_PACKET_INI_PATH = "config/sendPacket.ini";
    public static final String CONFIG_SERVER_SETTING_PROP_PATH = "config/serverSettings.properties";
    public static final String CONFIG_REWARD_SCROLL_PROP_PATH = "config/rewardScroll.properties";
    public static final String CONFIG_LOG_FILE_PATH = "logs/log";
    
    /**
     * TODO � �뵵����? 
     */
    public static final byte DEFAULT_FLAG = 2;
    /**
     *  �� ���� true �̸� �ִ� �������� 21������ �����ϱ� ���� �÷��̾�� ������ ��ȣ ������ �Ǵ�.
     */
    public static final boolean IS_UNLOCK_MAX_DAMAGE = true;
    /**
     * ��ȭ Ȯ���� ���� ���� �Ѵ�.
     * true(����), �׿� ������
     * TODO ��� �κп� ��ü���� ������ �ִ��� ���� �ʿ�.
     */
    public static final boolean IS_FEVER_TIME = false;    
    /**
     * KMS ������ ���� 
     */
    public static final short MAPLE_VERSION = 253;
    /**
     * KMS ���̳� ����
     */
    public static final byte SUB_VERSION = 1;
    /**
     * ?
     */
    public static final byte CHECK = 1;
    
    
    /**
     * ������ ����ϴ� IP 
     */
    public static String host;
    /**
     * ���۸�
     */
    public static int startMap;
    /**
     * ���� �� ä�� ������ �ִ��
     */
    public static int openChannelCount;
    /**
     * �α��μ��� ��Ʈ
     */
    public static int loginPort; 
    /**
     * ä�μ��� ��Ʈ
     */
    public static int channelPort;
    /**
     * ĳ�ü����� ��Ʈ
     */
    public static int cashShopPort;
    /**
     * ����ê���� ��Ʈ
     */
    public static int buddyChatPort;
    /**
     * ����ġ ���� 
     */
    public static int defaultExpRate;
    /**
     * �޼� ���� 
     */
    public static int defaultMesoRate;
    /**
     * ��� ����
     */
    public static int defaultDropRate;
    /**
     * ĳ�� ����
     */
    public static int defaultCashRate;
    /**
     * ���� ĳ�� ����
     */
    public static int defaultBossCashRate;
    /**
     * ĳ�ü� NPC
     */
    public static int cshopNpc; 
    /**
     * ���� ControlUnit ���� ä�� �α׸� �����ִµ� ä�� �Ǽ��� �����ϴµ� �� ������ ����Ѵ�.
     * �׸��� 500 �� �̻��� �Ǹ� ä�� �α׸� �ʱ�ȭ �Ѵ�.
     * TODO ä�� �α��� ��� ���Ϸ� ������ �����, �����⿡���� ������ �ʵ��� �ϴ� ���� ���� �ʿ�.
     */
    public static int chatlimit = 0;
    /**
     * DB ���� ��Ʈ 
     */
    public static int dbPort = 3306;
    /**
     * DB ���� IP
     */
    public static String dbHost = "127.0.0.1";
    /**
     * DB ���� ID
     */
    public static String dbUser = "root";
    /**
     * DB ���� ��й�ȣ
     */
    public static String dbPassword = "root";
    /**
     * @deprecated ��õ �޼���. 
     * TODO ���� Ŭ�󿡼� ���� �������� ���� Ȯ���� �ȵ�. 
     */
    public static String recommendMessage = "";
    /**
     * ���� �̸�
     */
    public static String serverName;
    /**
     * ingame ������ ȭ�� ��ܿ� �·� �귯���� ����� �޼��� 
     */
    public static String serverMessage;
    /**
     * 1�� �α��� ���� ���� ���� �ϸ� ä�� �����ϴ� ȭ�� ���ʿ� ��ǳ�� ���·� �������� �޼���  
     */
    public static String eventMessage;
    /**
     * ingame ������ ���۸�(startMap)�� ���� ��� ����(���ι��� �µ�)�� �����ִ� �޼��� 
     */
    public static String startMapMessage;
    /**
     * ingame ������ ����(���ι��� �µ�)�� �����ִ� �޼��� 
     */
    public static String serverNotice;
    /**
     * ingame ������ ĳ���� �Ӹ����� ��ǳ�� ���·� �������� �޼���
     * �����ð� �� �ڵ����� �����. 
     */
    public static String serverHint;
    /**
     * ingame ������ ���� ����� �˾�â ���·� �������� �޼��� ����
     * �����ð� �� �ڵ����� �����.
     */
    public static String serverNotititle;
    /**
     * ingame ������ ���� ����� �˾�â ���·� �������� �޼���
     * �����ð� �� �ڵ����� �����.
     */
    public static String serverNotification;
    /**
     * �̺�Ʈ�� ����� ��ũ��Ʈ ���� ��(������ �������� ��� �޸�(,)�� ����)
     */
    public static String events;
    /**
     * ���� ��ġ ���� �׽�Ʈ�� �������� ���� ���� �Ŀ� GM ������ ���� ������ �α��� �Ҽ� �ֵ��� ���� �Ҷ� ���
     */
    public static boolean serverCheck = false;
    /**
     * serverCheck ���� true �϶� ����ڰ� �α��� �õ� �Ҷ� �����ִ� �޼���
     */
    public static String serverCheckMessage = " ������.";
    /**
     * maxDrop �� ���� ����
     */
    public static boolean useMaxDrop;
    /**
     * bossMaxDrop �� ���� ����
     */
    public static boolean useBossMaxDrop;
    /**
     * �ִ� ��� ������ ��
     */
    public static int maxDrop;
    /**
     * �ִ� ���� ��� ������ ��
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
     * ������Ƽ ������ �ε� 
     */
    public static void init() {
    	FileInputStream fis = null;
        try {
            fis = new FileInputStream(getRootPath() + CONFIG_SERVER_SETTING_PROP_PATH);
            properties = new Properties();
            properties.load(fis);
            
            bossMaxDrop = getInt("�ִ뺸����������۰���");
            maxDrop = getInt("�ִ��������۰���");
            openChannelCount = getInt("����ä�μ�");
            loginPort = getInt("�α�����Ʈ");
            channelPort = getInt("ä����Ʈ");
            cashShopPort = getInt("ĳ�ü���Ʈ");
            buddyChatPort = getInt("ģ��������Ʈ");
            defaultExpRate = getInt("����ġ����");
            defaultDropRate = getInt("��ӹ���");
            defaultMesoRate = getInt("�޼ҹ���");
            defaultCashRate = getInt("ĳ�ù���");
            defaultBossCashRate = getInt("����ĳ�ù���");
            cshopNpc = getInt("ĳ�ü�NPC");
            startMap = getInt("���۸�");
            
            host = getString("������");
            serverName = getString("�����̸�");
            serverMessage = getString("�����޼���");
            eventMessage = getString("�̺�Ʈ�޼���");
            startMapMessage = getString("ó�����۰���");
            serverNotice = getString("�����˸��޼���"); 
            serverNotititle = getString("������������");
            serverNotification = getString("������������");
            serverHint = getString("������Ʈ");
            dbHost = getString("��������");
            dbPort = getInt("�����Ʈ");
            dbUser = getString("�����̵�");
            dbPassword = getString("����й�ȣ");
            events = getString("�̺�Ʈ");
            serverHint = getString("������Ʈ");
            serverCheckMessage = getString("�������˸޼���");

            serverCheck = getBoolean("��������");
            useMaxDrop = getBoolean("�ִ������");
            useBossMaxDrop = getBoolean("�ִ뺸��������");
                        
        } catch (Exception e) {
            logger.error("[����] ���� ���������� �ҷ����µ� �����Ͽ����ϴ�.", e);
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
