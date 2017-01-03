package a.my.made;

/**
 * MapleClient Ŭ������ �ִ� ���°����� �̰��� ������ �Ͽ���.
 * 
 * accounts ���̺� �ִ� loggedin(tinyint(1) 0 ~ 255 ����) �÷����� ����ϸ� �� ������ �������� �ֱ� ������ �и���.
 */
public enum AccountStatusType {

	/**
	 * �α��� ���� ���� ����
	 */
	NOT_LOGIN(0),
	/**
	 * Ŭ���̾�Ʈ���� ���̵�� ��й�ȣ�� ������ �Ϸ��� ���� 
	 */
	FIRST_LOGIN(1),
	/**
	 * FIRST_LOGIN ���¿��� 2�� ��й�ȣ ������ �Ϸ��� ����  
	 */
	SECOND_LOGIN(2),
	/**
	 * ���� ä�ο� �������� ����
	 */
	IN_CHANNEL(3),
	/**
	 * ĳ�ü��� �������� ���� 
	 */
	IN_CASHSHOP(4),
	/**
	 * ĳ�ü����� �������� ����
	 */
	OUT_CASHSHOP(5),
	
	//���� ���°�------------------------------------------------------------
	/**
	 * �α��� ���� ���� 2�� ��й�ȣ���� ������ �Ϸ� �� ����
	 */
	SERVER_TRANSITION(5),
	/**
	 * ä�� ���� �� ����(InterServerHandler.EnterCS()) TODO ĳ�ü�2 �̰� ȣ��Ǵ°��� ��� �ٸ��� Ȯ�� �ʿ�.
	 */
	CHANGE_CHANNEL(6);
	
	private final int value;
	private final int NOT_USE = 256;
	private final int MAX_VALUE = 255;
	
	private AccountStatusType(int value) {
		this.value = value;
	}
	
	public final int getValue() {
		try {
			if( value <= MAX_VALUE ) {
				return value;
			} else {
				throw new Exception("��� �� �� ���� ���°� �Դϴ�.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return NOT_USE;
	}
}
