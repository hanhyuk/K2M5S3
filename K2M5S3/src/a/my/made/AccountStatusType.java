package a.my.made;

/**
 * MapleClient 클래스에 있던 상태값들을 이곳에 재정의 하였다.
 * 
 * accounts 테이블에 있는 loggedin(tinyint(1) 0 ~ 255 범위) 컬럼에서 사용하며 이 값들은 연관성이 있기 때문에 분리함.
 */
public enum AccountStatusType {

	/**
	 * 로그인 하지 않은 상태
	 */
	NOT_LOGIN(0),
	/**
	 * 클라이언트에서 아이디와 비밀번호로 인증을 완료한 상태 
	 */
	FIRST_LOGIN(1),
	/**
	 * FIRST_LOGIN 상태에서 2차 비밀번호 인증을 완료한 상태  
	 */
	SECOND_LOGIN(2),
	/**
	 * 게임 채널에 접속중인 상태
	 */
	IN_CHANNEL(3),
	/**
	 * 캐시샵에 입장중인 상태 
	 */
	IN_CASHSHOP(4),
	/**
	 * 캐시샵에서 퇴장중인 상태
	 */
	OUT_CASHSHOP(5),
	
	//기존 상태값------------------------------------------------------------
	/**
	 * 로그인 성공 이후 2차 비밀번호까지 인증이 완료 된 상태
	 */
	SERVER_TRANSITION(5),
	/**
	 * 채널 변경 중 상태(InterServerHandler.EnterCS()) TODO 캐시샵2 이게 호출되는것이 어떻게 다른지 확인 필요.
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
				throw new Exception("사용 할 수 없는 상태값 입니다.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return NOT_USE;
	}
}
