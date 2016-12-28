package a.my.made;

/**
 * ���� ���ο����� ����ϴ� flag �� ����
 */
public enum CommonType {

	/**
	 * ���� ���� ����
	 */
	ACCOUNT_CREATE_POSSIBLE(0),
	/**
	 * ������ ������ ����
	 */
	ACCOUNT_EXISTS(1),
	/**
	 * �α����� ������ ��� 
	 */
	LOGIN_POSSIBLE(2),
	/**
	 * ������ �� ������ ���
	 */
	ACCOUNT_BAN(3),
	/**
	 * �̹� �α��� ���� ���
	 */
	LOGIN_ING(5),
	/**
	 * �α����� �Ұ����� ���
	 */
	LOGIN_IMPOSSIBLE(6),
	/**
	 * ������� ����
	 */
	NONE(9999);

	private final int value;
	
	private CommonType(int i) {
		this.value = i;
	}

	public final int getValue() {
		return value;
	}
}
