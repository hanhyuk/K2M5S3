package constants.programs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.MYSQL;

/**
 * TODO �����Ⱓ ���� �������� ���� ���� ������ �����ϴ� ���. �����ؼ� �����층 ó�� ���� ���� �ʿ�.
 */
public class OldUserDelete {
	
	private static final Logger logger = LoggerFactory.getLogger(OldUserDelete.class);
	
	public static void main(String[] args) {
		int deletedrows = 0;

		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts");
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int lastconnect = rs.getInt("lastconnect");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
				Date date = Calendar.getInstance().getTime();
				date.setDate(date.getDate() - 14);
				if (lastconnect < Integer.parseInt(sdf.format(date))) {
					deletedrows++;
					PreparedStatement ps3 = con.prepareStatement("DELETE FROM accounts WHERE id = ?");
					ps3.setInt(1, rs.getInt("id"));
					ps3.executeUpdate();
					ps3.close();

					PreparedStatement ps2 = con.prepareStatement("SELECT * FROM characters where accountid = ?");
					ps2.setInt(1, rs.getInt("id"));
					ResultSet rs2 = ps2.executeQuery();
					while (rs2.next()) {
						deleteCharacter(rs.getInt("id"), rs2.getInt("id"));
					}
					rs2.close();
					ps2.close();
				}

			}
			ps.close();
			rs.close();

		} catch (Throwable t) {
			t.printStackTrace();
		}

		logger.debug("{} ���� ������ �����Ǿ����ϴ�.", deletedrows);
	}

	public final static boolean deleteCharacter(final int accId, final int cid) {
		try {
			final Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement(
					"SELECT id, guildid, guildrank, name FROM characters WHERE id = ? AND accountid = ?");
			ps.setInt(1, cid);
			ps.setInt(2, accId);
			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				rs.close();
				ps.close();
				return false;
			}
			rs.close();
			ps.close();

			ps = con.prepareStatement("DELETE FROM characters WHERE id = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM skills WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM hiredmerch WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM mountdata WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM keyvalue WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM keyvalue2 WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM inventoryitems WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `inner_ability_skills` WHERE player_id = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `inventoryslot` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `keymap` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `questinfo` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `queststatus` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `quickslot` WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `skillmacros` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `skills_cooldowns` WHERE charid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `steelskills` WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `trocklocations` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();
			return true;
		} catch (final SQLException e) {
			logger.debug("DeleteChar error {}", e);
		}
		return false;
	}
}
