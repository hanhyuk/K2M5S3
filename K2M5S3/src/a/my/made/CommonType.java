package a.my.made;

/**
 * 서버 내부에서만 사용하는 flag 값 정의
 */
public enum CommonType {

	/**
	 * 계정 생성 가능
	 */
	ACCOUNT_CREATE_POSSIBLE(0),
	/**
	 * 동일한 계정이 존재
	 */
	ACCOUNT_EXISTS(1),
	/**
	 * 로그인이 가능한 경우 
	 */
	LOGIN_POSSIBLE(2),
	/**
	 * 계정이 벤 상태인 경우
	 */
	ACCOUNT_BAN(3),
	/**
	 * 이미 로그인 중인 경우
	 */
	LOGIN_ING(5),
	/**
	 * 로그인이 불가능한 경우
	 */
	LOGIN_IMPOSSIBLE(6),
	/**
	 * 사용하지 않음
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
