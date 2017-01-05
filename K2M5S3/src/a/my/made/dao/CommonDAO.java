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
	 * ����� ���� ���¸� ��� �α��� ���� ���� ���·� ������Ʈ �Ѵ�.
	 */
	public static boolean updateAllUserLogout() {
		boolean result = false;
		
		final ParamMap params = new ParamMap();
		params.put("loggedin", AccountStatusType.NOT_LOGIN.getValue());
		result = AccountDAO.setAccountInfo(AccountDAO.DEFAULT_ACCOUNT_ID, params);
		
		return result;
	}
	
	/**
	 * ������ �����Ҷ� DB�� ���� �ִ� ���ʿ��� �������� ��� �����Ѵ�.
	 */
	public static boolean deleteUnnecessaryDbInfoAtStartUp() {
		boolean result = false;

		//�ȵ���̵�
		result = deleteUnnecessaryDbInfo("uniqueid", "android", "uniqueid", "inventoryitems");
		//����
		result = deleteUnnecessaryDbInfo("uniqueid", "extendedslots", "uniqueid", "inventoryitems");
		
		//������ ���� 
		result = deleteUnnecessaryDbInfo("characterid", "hiredmerch", "id", "characters");
		result = deleteUnnecessaryDbInfo("merchid", "hiredmerchantsaveitems", "id", "hiredmerchantsaves");
		
		//�����Ƽ
		result = deleteUnnecessaryDbInfo("player_id", "inner_ability_skills", "id", "characters");
		//�������̽�
		result = deleteUnnecessaryDbInfo("characterid", "inventoryitems", "id", "characters");
		
		//�κ��丮 ����
		result = deleteUnnecessaryDbInfo("characterid", "inventoryslot", "id", "characters");
		//Ű��
		result = deleteUnnecessaryDbInfo("characterid", "keymap", "id", "characters");
		//keyvalue
		result = deleteUnnecessaryDbInfo("cid", "keyvalue", "id", "characters");
		//keyvalue2
		result = deleteUnnecessaryDbInfo("cid", "keyvalue2", "id", "characters");
		//��
		result = deleteUnnecessaryDbInfo("uniqueid", "pets", "uniqueid", "inventoryitems");
		
		//����Ʈ ����
		result = deleteUnnecessaryDbInfo("characterid", "questinfo", "id", "characters");
		result = deleteUnnecessaryDbInfo("characterid", "queststatus", "id", "characters");
		
		//������
		result = deleteUnnecessaryDbInfo("cid", "quickslot", "id", "characters");
		//����
		result = deleteUnnecessaryDbInfo("cid", "rewardsaves", "id", "characters");
		
		//��ų��ũ��
		result = deleteUnnecessaryDbInfo("characterid", "skillmacros", "id", "characters");
		//��ų
		result = deleteUnnecessaryDbInfo("characterid", "skills", "id", "characters");
		//��ų��Ÿ��
		result = deleteUnnecessaryDbInfo("charid", "skills_cooldowns", "id", "characters");
		
		//��(ĳ����)���� ������ �� ����
		result = deleteUnnecessaryDbInfo("characterid", "trocklocations", "id", "characters");
		
		return result;
	}
	
	/**
	 * Ư�� ���̺� �ִ� ���ʿ��� DB ������ �����Ѵ�.
	 * �ΰ��� ���̺��� join �ؼ� �������� ���ϰ�, �������� ������ �������� ��� �����Ѵ�.
	 * 
	 * �׸��� join �Ҷ� 1:1 �� ���谡 �ƴ� ��� �ߺ��� ������ ���� �ϵ��� ������ �ۼ��Ͽ���.
	 * ������ ��뷮 ������ ������ ���� ���� �Ǵ� �ð��� ���������� �ʾ�����, ���� �ʿ��ϴٸ�
	 * �߰����� ���� Ʃ���� �� �ʿ��� ������ �����Ѵ�.
	 * 
	 * @param targetColumn ���� �� �� WHERE ���� �÷�
	 * @param targetTable ���� �� ������ �ִ� ���̺�
	 * @param joinColumn join �÷�
	 * @param joinTable join ���̺�
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
			
			//������ �ʴ� �ȵ���̵� ���� ����
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
