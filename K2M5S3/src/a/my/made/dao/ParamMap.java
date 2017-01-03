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
	 * key �� �ش��ϴ� value �� int ����ȯ �ؼ� ��ȯ�Ѵ�.
	 * key ���� ������ -1 �� ��ȯ�ϰ�, value ���� null �̸� 0 �� ��ȯ�Ѵ�.
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
	 * key �� �ش��ϴ� value �� String ����ȯ �ؼ� ��ȯ�Ѵ�.
	 * key ���� ������ null �� ��ȯ�Ѵ�.
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
	 * key �� �ش��ϴ� value �� Timestamp ����ȯ �ؼ� ��ȯ�Ѵ�.
	 * key ���� ������ null �� ��ȯ�Ѵ�.
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
	 * key ������ UPDATE ������ ���·� ������ ��ȯ�Ѵ�.
	 * 
	 * ���� ��� name = ?, age = ?, sex = ? �� ���� ���·� �����Ѵ�.
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
	 * key ������ INSERT ������ ���·� ������ ��ȯ�Ѵ�.
	 * 
	 * ���� ��� name, age, sex �� ���� ���·� �����Ѵ�.
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
	 * ������ ���� �Ҷ� �ʿ��� �Ķ���͸� �ڵ����� �����ϰ�, ������ index ������ ��ȯ�Ѵ�.
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
	        	throw new Exception("Ÿ�� ��ȯ ���� key : " + key);
	        }
	    }	
		
		return i;
	}
}
