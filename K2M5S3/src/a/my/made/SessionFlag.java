package a.my.made;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFlag {
	private static final Logger logger = LoggerFactory.getLogger(SessionFlag.class);
	
	/**
	 * ������ close �ɶ� ĳ���� ������ DB�� ���� ���� ���� 
	 * 
	 * N(�������), �׿� ����
	 */
	public static String KEY_CHAR_SAVE = "flagKeyCharSave";
	
	/**
	 * ������� ������ ������ ���´�. �̶� ĳ���� ������ �������� �ʴ´�.
	 * @param session
	 */
	public static void forceDisconnect(final IoSession session) {
		session.setAttribute(SessionFlag.KEY_CHAR_SAVE, "N");
		session.closeNow();
	}
}
