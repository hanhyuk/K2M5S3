package a.my.made;

public enum CommonTypeCheck {

	ACCOUNT_YES(0), 	//계정 생성 가능
	ACCOUNT_EXIST(1), 	//동일한 계정이 존재
	ACCOUNT_OVER(2),	//계정 생성 횟수 초과
	ACCOUNT_BAN(3),		//계정이 벤 상태인 경우
	LOGIN_SUCCESS(4),	//로그인 성공시
	LOGIN_ING(5),		//이미 로그인 중인 경우
	NONE(9999);			//사용하지 않음

	private final int i;

	private CommonTypeCheck(int i) {
		this.i = i;
	}

	public final int getValue() {
		return i;
	}
}
