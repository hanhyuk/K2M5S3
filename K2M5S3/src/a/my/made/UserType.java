package a.my.made;

/**
 * TODO ���� UserType �� ���Ǵ� �κ��� ����.
 * accounts ���̺� gm �÷�(tinyint(1) 0 ~ 255)�� ����� Ŭ���� �̴�. 
 */
public enum UserType {
	/**
	 * �Ϲ� ����
	 */
	USER(0),
    /**
     * �Ŀ� ���� 
     */
	SPONSOR(1),
    /**
     * �Ϲ� ������ 
     */
    PUBLIC_GM(10),
    /**
     * �Ѱ�����
     */
    SUPERGM(100);
	
	private final int value;
	private final int NOT_USE = 256;
	private final int MAX_VALUE = 255;
	
	private UserType(int value) {
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