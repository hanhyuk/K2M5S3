package a.my.made.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.util.StringUtils;
import database.MYSQL;

/**
 * accounts 테이블과 관련된 DB 처리는 모두 이곳에 정의한다.
 * 
 */
public class AccountDAO extends BaseDAO {
	private static final Logger logger = LoggerFactory.getLogger(AccountDAO.class);
	
	public static final int DEFAULT_ACCOUNT_ID = -1;
	
	public static List<ResultMap> getAccountInfo(final int accountId) {
		return selectAccountTable(accountId, null, null);
	}
	
	public static List<ResultMap> getAccountInfo(final String loginId) {
		return selectAccountTable(DEFAULT_ACCOUNT_ID, loginId, null);
	}
	
	public static List<ResultMap> getAccountInfo(final String loginId, final String loginPassword) {
		return selectAccountTable(DEFAULT_ACCOUNT_ID, loginId, loginPassword);
	}
	
	private static List<ResultMap> selectAccountTable(final int accountId, final String loginId, final String loginPassword) {
		List<ResultMap> result = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String query = "SELECT * FROM accounts WHERE __ARGS__";
		
		if( DEFAULT_ACCOUNT_ID != accountId ) {
			query = query.replaceAll("__ARGS__", "id = ?");
		} else if( StringUtils.isNotEmpty(loginId) ) {
			query = query.replaceAll("__ARGS__", "name = ?");
			
			if( StringUtils.isNotEmpty(loginPassword) ) {
				query = query.replaceAll("__ARGS__", "name = ? AND password = ?");
			}
		}
		logger.debug("query : {}", query);
		try {
			ps = MYSQL.getConnection().prepareStatement(query);
			
			if( DEFAULT_ACCOUNT_ID != accountId ) {
				ps.setInt(1, accountId);
			} else if( StringUtils.isNotEmpty(loginId) ) {
				ps.setString(1, loginId);
				
				if( StringUtils.isNotEmpty(loginPassword) ) {
					ps.setString(2, loginPassword);
				}
			}
			logger.debug("query : {}", ps);
			rs = ps.executeQuery();
			result = convertResultSetToList(rs);
		} catch(Exception e) {
			logger.debug("{}", e);
		} try {
			if( ps != null ) {
				ps.close(); ps = null;
			}
			if( rs != null ) {
				rs.close(); rs = null;
			}
		} catch(SQLException e) {
			logger.debug("{}", e);
		}
		
		return result;
	}
}
