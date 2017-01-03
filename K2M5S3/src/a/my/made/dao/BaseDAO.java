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
					//TODO ���̺� �÷� Ÿ���� tinyint(1) �� ��� true, false�� ��ȯ��.
					//���� �ܼ��� �ذ���� Ÿ���� int(1) �� �����ϸ� �ȴ�.
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
