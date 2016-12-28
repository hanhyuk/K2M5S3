package a.my.made;

/**
 * TODO 현재 UserType 을 사용되는 부분이 없음.
 * accounts 테이블에 gm 컬럼(tinyint(1) 0 ~ 255)과 연계된 클래스 이다. 
 */
public enum UserType {
	/**
	 * 일반 유저
	 */
	USER(0),
    /**
     * 후원 유저 
     */
	SPONSOR(1),
    /**
     * 일반 관리자 
     */
    PUBLIC_GM(10),
    /**
     * 총관리자
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
				throw new Exception("사용 할 수 없는 상태값 입니다.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return NOT_USE;
	}
}