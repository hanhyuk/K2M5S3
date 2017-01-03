package handler.login;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.CommonType;
import a.my.made.dao.AccountDAO;
import a.my.made.dao.ParamMap;
import a.my.made.dao.ResultMap;
import client.MapleClient;

/**
 * TODO 일단 클래스명부터 변경 할 필요가 있어 보인다. 그리고 이곳에 관련 기능들이 더 추가 될지 여부에 따라
 * 이 클래스를 계속 유지 할지... 삭제 할지 검토하자.
 *
 */
public class AutoRegister {
	private static final Logger logger = LoggerFactory.getLogger(AutoRegister.class);

	/**
	 * 계정 생성이 가능한지 체크한다.
	 */
	public static CommonType checkAccount(String loginId) {
		CommonType accountType = CommonType.ACCOUNT_CREATE_POSSIBLE;
		
		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(loginId);
		if( accountInfo.size() > 0 ) {
			accountType = CommonType.ACCOUNT_EXISTS;
		}
		
		return accountType;
	}

	/**
	 * 계정을 생성한다.
	 * 
	 * @param client
	 * @param loginId
	 * @param loginPassword
	 */
	public static void registerAccount(MapleClient client, String loginId, String loginPassword) {
		final ParamMap params = new ParamMap();
		params.put("name", loginId);
		params.put("password", loginPassword);
		params.put("ip", client.getSessionIPAddress());
		AccountDAO.addAccountInfo(params);
	}
}
