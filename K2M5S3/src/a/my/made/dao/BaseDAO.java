package a.my.made.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDAO {
	private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
	
	public static List<ResultMap> convertResultSetToList(ResultSet rs) {
		List<ResultMap> result = new ArrayList<ResultMap>();
		
		try {
			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();
			
			while (rs.next()) {
				ResultMap row = new ResultMap();
				for (int i = 1; i <= columns; ++i) {
					//TODO 테이블 컬럼 타입이 tinyint(1) 일 경우 true, false로 변환됨.
					//가장 단순한 해결법은 타입을 int(1) 로 변경하면 된다.
					row.put(md.getColumnName(i), rs.getObject(i));
				}
				result.add(row);
			}
	
		} catch(Exception e) {
			logger.debug("{}", e);
		}
		
		return result;
	}
}
