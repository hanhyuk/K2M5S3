package a.my.made.dao;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultMap extends HashMap<String, Object> {
	private static final Logger logger = LoggerFactory.getLogger(ResultMap.class);
	
	private static final long serialVersionUID = -4349021475139034528L;
	
	/**
	 * key �� �ش��ϴ� value �� int ����ȯ �ؼ� ��ȯ�Ѵ�.
	 * key ���� ������ -1 �� ��ȯ�ϰ�, value ���� null �̸� 0 �� ��ȯ�Ѵ�.
	 * 
	 * @param key
	 * @return 
	 */
	public int getInt(String key) {
		int result = -1;
		try {
			if( this.containsKey(key) ) {
				if( this.get(key) == null ) {
					result = 0;
				} else {
					result = Integer.parseInt(this.get(key).toString());
				}
			} else {
				throw new Exception("not found value");
			}	
		} catch(Exception e) {
			logger.debug("key : {}", key, e);
		}
		
		return result;
	}
	
	public String getString(String key) {
		String result = null;
		try {
			if( this.containsKey(key) ) {
				if( this.get(key) != null ) {
					result = String.valueOf(this.get(key));
				}
			} else {
				throw new Exception("not found value");
			}	
		} catch(Exception e) {
			logger.debug("{}", e);
		}
		
		return result;
	}
} 
