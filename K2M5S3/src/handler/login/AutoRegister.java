package handler.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import a.my.made.CommonTypeCheck;
import client.MapleClient;
import constants.ServerConstants;
import database.MYSQL;

public class AutoRegister {

	/**
	 * 계정 생성이 가능한지 체크한다.
	 * 
	 * @param client
	 * @param loginId
	 * @param loginPassword
	 * @return
	 */
	public static CommonTypeCheck checkAccount(String loginId) {
		CommonTypeCheck accountType = CommonTypeCheck.ACCOUNT_YES;
		Connection connect = MYSQL.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//동일한 계정명이 존재하는지 체크
			ps = connect.prepareStatement("SELECT * FROM accounts WHERE name = ?");
			ps.setString(1, loginId);
			rs = ps.executeQuery();

			if( rs.next() ) {
				accountType = CommonTypeCheck.ACCOUNT_EXIST;
			}
			//FIXME 벤 처리 여부 체크
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close(); ps = null;
				}
				if (rs != null) {
					rs.close(); rs = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return accountType;
	}

	public static void registerAccount(MapleClient account, String name, String password) {
		Connection connect = MYSQL.getConnection();
		PreparedStatement query = null;
		ResultSet result = null;
		try {
			query = connect.prepareStatement("INSERT INTO accounts (name, password, ip) VALUES (?, ?, ?)",
					MYSQL.RETURN_GENERATED_KEYS);
			query.setString(1, name);
			query.setString(2, password);
			query.setString(3, account.getSessionIPAddress());
			query.executeUpdate();
		} catch (Exception error) {
			if (!ServerConstants.realese) {
				error.printStackTrace();
			}
		} finally {
			try {
				if (connect != null) {
					connect = null;
				}
				if (query != null) {
					query.close();
				}
				if (result != null) {
					result.close();
				}
			} catch (Exception error) {
				if (!ServerConstants.realese) {
					error.printStackTrace();
				}
			}
		}
	}
}
