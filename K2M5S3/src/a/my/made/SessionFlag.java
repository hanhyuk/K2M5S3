package a.my.made;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFlag {
	private static final Logger logger = LoggerFactory.getLogger(SessionFlag.class);
	
	/**
	 * 세션이 close 될때 캐릭터 정보를 DB에 저장 할지 여부 
	 * 
	 * N(저장안함), 그외 저장
	 */
	public static String KEY_CHAR_SAVE = "flagKeyCharSave";
	
	/**
	 * 사용자의 접속을 강제로 끊는다. 이때 캐릭터 정보는 저장하지 않는다.
	 * @param session
	 */
	public static void forceDisconnect(final IoSession session) {
		session.setAttribute(SessionFlag.KEY_CHAR_SAVE, "N");
		session.closeNow();
	}
}
