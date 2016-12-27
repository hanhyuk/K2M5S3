package a.my.made;

public enum CommonTypeCheck {

	ACCOUNT_YES(0), 	//���� ���� ����
	ACCOUNT_EXIST(1), 	//������ ������ ����
	ACCOUNT_OVER(2),	//���� ���� Ƚ�� �ʰ�
	ACCOUNT_BAN(3),		//������ �� ������ ���
	LOGIN_SUCCESS(4),	//�α��� ������
	LOGIN_ING(5),		//�̹� �α��� ���� ���
	NONE(9999);			//������� ����

	private final int i;

	private CommonTypeCheck(int i) {
		this.i = i;
	}

	public final int getValue() {
		return i;
	}
}
