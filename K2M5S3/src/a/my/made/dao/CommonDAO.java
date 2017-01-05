package a.my.made.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.AccountStatusType;
import database.MYSQL;

public class CommonDAO extends BaseDAO {
	private static final Logger logger = LoggerFactory.getLogger(CommonDAO.class);
	
	/**
	 * 사용자 계정 상태를 모두 로그인 하지 않은 상태로 업데이트 한다.
	 */
	public static boolean updateAllUserLogout() {
		boolean result = false;
		
		final ParamMap params = new ParamMap();
		params.put("loggedin", AccountStatusType.NOT_LOGIN.getValue());
		result = AccountDAO.setAccountInfo(AccountDAO.DEFAULT_ACCOUNT_ID, params);
		
		return result;
	}
	
	/**
	 * 서버를 구동할때 DB에 남아 있는 불필요한 정보들을 모두 삭제한다.
	 */
	public static boolean deleteUnnecessaryDbInfoAtStartUp() {
		boolean result = false;

		//안드로이드
		result = deleteUnnecessaryDbInfo("uniqueid", "android", "uniqueid", "inventoryitems");
		//가방
		result = deleteUnnecessaryDbInfo("uniqueid", "extendedslots", "uniqueid", "inventoryitems");
		
		//고용상점 관련 
		result = deleteUnnecessaryDbInfo("characterid", "hiredmerch", "id", "characters");
		result = deleteUnnecessaryDbInfo("merchid", "hiredmerchantsaveitems", "id", "hiredmerchantsaves");
		
		//어빌리티
		result = deleteUnnecessaryDbInfo("player_id", "inner_ability_skills", "id", "characters");
		//예외케이스
		result = deleteUnnecessaryDbInfo("characterid", "inventoryitems", "id", "characters");
		
		//인벤토리 슬롯
		result = deleteUnnecessaryDbInfo("characterid", "inventoryslot", "id", "characters");
		//키맵
		result = deleteUnnecessaryDbInfo("characterid", "keymap", "id", "characters");
		//keyvalue
		result = deleteUnnecessaryDbInfo("cid", "keyvalue", "id", "characters");
		//keyvalue2
		result = deleteUnnecessaryDbInfo("cid", "keyvalue2", "id", "characters");
		//펫
		result = deleteUnnecessaryDbInfo("uniqueid", "pets", "uniqueid", "inventoryitems");
		
		//퀘스트 관련
		result = deleteUnnecessaryDbInfo("characterid", "questinfo", "id", "characters");
		result = deleteUnnecessaryDbInfo("characterid", "queststatus", "id", "characters");
		
		//퀵슬롯
		result = deleteUnnecessaryDbInfo("cid", "quickslot", "id", "characters");
		//보상
		result = deleteUnnecessaryDbInfo("cid", "rewardsaves", "id", "characters");
		
		//스킬매크로
		result = deleteUnnecessaryDbInfo("characterid", "skillmacros", "id", "characters");
		//스킬
		result = deleteUnnecessaryDbInfo("characterid", "skills", "id", "characters");
		//스킬쿨타임
		result = deleteUnnecessaryDbInfo("charid", "skills_cooldowns", "id", "characters");
		
		//고돌(캐시템)에서 저장한 맵 정보
		result = deleteUnnecessaryDbInfo("characterid", "trocklocations", "id", "characters");
		
		return result;
	}
	
	/**
	 * 특정 테이블에 있는 불필요한 DB 정보를 삭제한다.
	 * 두개의 테이블을 join 해서 교집합을 구하고, 교집합을 제외한 나머지는 모두 삭제한다.
	 * 
	 * 그리고 join 할때 1:1 의 관계가 아닐 경우 중복된 정보를 제거 하도록 쿼리를 작성하였다.
	 * 하지만 대용량 정보를 가지고 실제 수행 되는 시간을 측정하지는 않았으며, 만약 필요하다면
	 * 추가적인 쿼리 튜닝이 더 필요할 것으로 생각한다.
	 * 
	 * @param targetColumn 삭제 할 때 WHERE 조건 컬럼
	 * @param targetTable 삭제 할 정보가 있는 테이블
	 * @param joinColumn join 컬럼
	 * @param joinTable join 테이블
	 * @return
	 */
	private static boolean deleteUnnecessaryDbInfo(final String targetColumn, final String targetTable, final String joinColumn, final String joinTable) {
		boolean result = false;
		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		
		String selectQuery = "SELECT DISTINCT _ARG1_ FROM _ARG2_ WHERE _ARG1_ NOT IN (SELECT DISTINCT b._ARG3_ FROM _ARG2_ AS a, _ARG4_ AS b WHERE a._ARG1_ = b._ARG3_)";
		selectQuery = selectQuery.replaceAll("_ARG1_", targetColumn);
		selectQuery = selectQuery.replaceAll("_ARG2_", targetTable);
		selectQuery = selectQuery.replaceAll("_ARG3_", joinColumn);
		selectQuery = selectQuery.replaceAll("_ARG4_", joinTable);
		
		String deleteQuery = "DELETE FROM _ARG2_ WHERE _ARG1_ = ?";
		deleteQuery = deleteQuery.replaceAll("_ARG1_", targetColumn);
		deleteQuery = deleteQuery.replaceAll("_ARG2_", targetTable);
		
		try {
//			logger.debug("{}", selectQuery); logger.debug("{}", deleteQuery);
			
			//사용되지 않는 안드로이드 정보 삭제
    		con = MYSQL.getConnection();
            ps = con.prepareStatement(selectQuery);
            rs = ps.executeQuery();
            
            while (rs.next()) {
            	ps2 = con.prepareStatement(deleteQuery);
            	ps2.setInt(1, rs.getInt(targetColumn));
            	ps2.executeUpdate(); ps2.close(); ps2 = null;
            }
            
            result = true;
		} catch (Exception e) {
			logger.debug("", e);
		} finally {
			try {
				if( ps != null ) {
					ps.close(); ps = null;
				}
				if( ps2 != null ) {
					ps2.close(); ps2 = null;
				}
				if( rs != null ) {
					rs.close(); rs = null;
				}
			} catch(SQLException e) {
				logger.debug("", e);
			}
		}
		return result;
	}
}
