package a.my.made.dao;

import java.sql.Connection;
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
	
	//---- select----
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
		} else if( StringUtils.isNotEmpty(loginId) && StringUtils.isNotEmpty(loginPassword) ) {
			query = query.replaceAll("__ARGS__", "name = ? AND password = ?");
		} else if( StringUtils.isNotEmpty(loginId) ) {
			query = query.replaceAll("__ARGS__", "name = ?");
		}
		
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
	
	
	//---- update ----
	public static boolean setAccountInfo(final int accountId, final ParamMap params) {
		return updateAccountTable(accountId, params);
	}
	
	private static boolean updateAccountTable(final int accountId, final ParamMap params) {
		boolean result = false;
		Connection con = MYSQL.getConnection();
		PreparedStatement ps = null;
		
		try {
			String where = "";
			if( DEFAULT_ACCOUNT_ID != accountId ) {
				where = "WHERE id = ?";
			}
			ps = con.prepareStatement(String.format("UPDATE accounts SET %s %s", params.getParamsQuery(), where));
			
			int index = params.setParamsQuery(ps);
			if( index != -1 ) {
				if( DEFAULT_ACCOUNT_ID != accountId ) {
					ps.setInt(index++, accountId);
				}
				ps.executeUpdate();
				result = true;
			}
		} catch (Exception e) {
			logger.debug("{}", e);
		} finally {
			try {
				if( ps != null ) {
					ps.close(); ps = null;
				}
			} catch(SQLException e) {
				logger.debug("{}", e);
			}
		}
		return result;
	}
	
	//---- insert ----
	public static boolean addAccountInfo(final ParamMap params) {
		return insertAccountTable(params);
	}
	
	private static boolean insertAccountTable(final ParamMap params) {
		boolean result = false;
		Connection con = MYSQL.getConnection();
		PreparedStatement ps = null;
		
		try {
			ps = con.prepareStatement(String.format("INSERT INTO accounts (%s) VALUES (%s)", params.getParamsColsQuery()[0], params.getParamsColsQuery()[1]));
			logger.debug("{}", ps);
			int index = params.setParamsQuery(ps);
			if( index != -1 ) {
				ps.executeUpdate();
				result = true;
			}
		} catch (Exception e) {
			logger.debug("{}", e);
		} finally {
			try {
				if( ps != null ) {
					ps.close(); ps = null;
				}
			} catch(SQLException e) {
				logger.debug("{}", e);
			}
		}
		return result;
	}
}
