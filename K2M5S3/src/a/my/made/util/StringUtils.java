package a.my.made.util;

public class StringUtils {

	/**
	 * ���ڿ��� null �Ǵ� "" �Ǵ� ���̰� 0 �ΰ�� true, �׿� false
	 * 
	 */
	public static boolean isEmpty(String s) {
		boolean result = false;
		
		if( s == null || "".equals(s) || s.length() == 0 ) {
			return true;
		}
		
		return result;
	}
	
	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}
}
