package a.my.made.dao;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamMap extends HashMap<String, Object> {
	private static final Logger logger = LoggerFactory.getLogger(ParamMap.class);
	
	private static final long serialVersionUID = -5333606417292366565L;

	/**
	 * key 에 해당하는 value 를 int 형변환 해서 반환한다.
	 * key 값이 없으면 -1 을 반환하고, value 값이 null 이면 0 을 반환한다.
	 * 
	 * @param key
	 * @return 
	 */
	private int getInt(String key) throws Exception {
		int result = -1;
		
		if( this.containsKey(key) ) {
			if( this.get(key) == null ) {
				result = 0;
			} else {
				result = Integer.parseInt(this.get(key).toString());
			}
		} else {
			throw new Exception("not found value");
		}	
		
		return result;
	}
	
	/**
	 * key 에 해당하는 value 를 String 형변환 해서 반환한다.
	 * key 값이 없으면 null 을 반환한다.
	 *  
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private String getString(String key) throws Exception {
		String result = null;
		
		if( this.containsKey(key) ) {
			if( this.get(key) != null ) {
				result = String.valueOf(this.get(key));
			}
		} else {
			throw new Exception("not found value");
		}
		
		return result;
	}
	
	/**
	 * key 에 해당하는 value 를 Timestamp 형변환 해서 반환한다.
	 * key 값이 없으면 null 을 반환한다.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private Timestamp getTimestamp(String key) throws Exception {
		Timestamp result = null;
		
		if( this.containsKey(key) ) {
			if( this.get(key) != null ) {
				result = (Timestamp)this.get(key);
			}
		} else {
			throw new Exception("not found value");
		}
		
		return result;
	}
	/**
	 * key 값들을 UPDATE 쿼리문 형태로 변경후 반환한다.
	 * 
	 * 예를 들어 name = ?, age = ?, sex = ? 와 같은 형태로 변경한다.
	 * @return
	 * @throws Exception
	 */
	public String getParamsQuery() throws Exception {
		String result = "";
		
		Iterator<String> iterator = this.keySet().iterator();
	    while (iterator.hasNext()) {
	        String key = (String) iterator.next();
	        result += String.format(",%s = ?", key);
	    }	
	    
	    return result.substring(1);
	}
	
	/**
	 * key 값들을 INSERT 쿼리문 형태로 변경후 반환한다.
	 * 
	 * 예를 들어 name, age, sex 와 같은 형태로 변경한다.
	 * @return
	 * @throws Exception
	 */
	public String[] getParamsColsQuery() throws Exception {
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";
		
		Iterator<String> iterator = this.keySet().iterator();
	    while (iterator.hasNext()) {
	        String key = (String) iterator.next();
	        result[0] += String.format(",%s", key);
	        result[1] += ",?";
	    }	
	    
	    result[0] = result[0].substring(1);
	    result[1] = result[1].substring(1);
	    
	    return result;
	}
	
	/**
	 * 쿼리를 수행 할때 필요한 파라메터를 자동으로 설정하고, 마지막 index 정보를 반환한다.
	 * @param ps
	 * @return
	 * @throws Exception
	 */
	public int setParamsQuery(final PreparedStatement ps) throws Exception {
		int i = 1;

		Iterator<String> iterator = this.keySet().iterator();
		
	    while (iterator.hasNext()) {
	        String key = (String) iterator.next();
	        Object value = this.get(key);
	        
	        if( value instanceof Integer ) {
	        	ps.setInt(i++, getInt(key));
	        } else if( value instanceof String ) {
	        	ps.setString(i++, getString(key));
	        } else if( value instanceof Timestamp ) {
	        	ps.setTimestamp(i++, getTimestamp(key));
	        } else {
	        	i = -1;
	        	throw new Exception("타입 변환 예외 key : " + key);
	        }
	    }	
		
		return i;
	}
}
