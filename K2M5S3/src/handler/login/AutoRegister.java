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
 * TODO �ϴ� Ŭ��������� ���� �� �ʿ䰡 �־� ���δ�. �׸��� �̰��� ���� ��ɵ��� �� �߰� ���� ���ο� ����
 * �� Ŭ������ ��� ���� ����... ���� ���� ��������.
 *
 */
public class AutoRegister {
	private static final Logger logger = LoggerFactory.getLogger(AutoRegister.class);

	/**
	 * ���� ������ �������� üũ�Ѵ�.
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
	 * ������ �����Ѵ�.
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
