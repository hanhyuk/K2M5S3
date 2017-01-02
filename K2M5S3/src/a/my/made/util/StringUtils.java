package a.my.made.util;

public class StringUtils {

	/**
	 * 문자열이 null 또는 "" 또는 길이가 0 인경우 true, 그외 false
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
