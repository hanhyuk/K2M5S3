package server.life;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.MYSQL;

public class MapleMonsterProvider {
	private static final Logger logger = LoggerFactory.getLogger(MapleMonsterProvider.class);
	
	private static final MapleMonsterProvider instance = new MapleMonsterProvider();
	private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<Integer, List<MonsterDropEntry>>();
	private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<MonsterGlobalDropEntry>();

	public static final MapleMonsterProvider getInstance() {
		return instance;
	}

	public final List<MonsterGlobalDropEntry> getGlobalDrop() {
		return globaldrops;
	}

	/**
	 * 글로벌 드랍 정보를 캐싱한다.
	 */
	public final void loadGlobalDropInfo() {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			final Connection con = MYSQL.getConnection();
			ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
			rs = ps.executeQuery();

			while (rs.next()) {
				globaldrops.add(new MonsterGlobalDropEntry(
						rs.getInt("itemid"), 
						rs.getInt("chance"), 
						rs.getInt("continent"), 
						rs.getByte("dropType"), 
						rs.getInt("minimum_quantity"),
						rs.getInt("maximum_quantity"), 
						rs.getInt("questid")));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			logger.debug("Error retrieving drop", e);
		} finally {
			try {
				if (ps != null) {
					ps.close(); ps = null;
				}
				if (rs != null) {
					rs.close(); rs = null;
				}
			} catch (SQLException e) {
				logger.debug("", e);
			}
		}
	}

	/**
	 * 해당 몬스터의 드랍 정보를 반환한다.
	 * @param monsterId
	 * @return
	 */
	public final List<MonsterDropEntry> getDropInfo(final int monsterId) {
		if (drops.containsKey(monsterId)) {
			return drops.get(monsterId);
		}
		final List<MonsterDropEntry> ret = new LinkedList<MonsterDropEntry>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = MYSQL.getConnection().prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
			ps.setInt(1, monsterId);
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getInt("questid")));
			}
		} catch (SQLException e) {
			logger.debug("", e);
			return ret;
		} finally {
			try {
				if (ps != null) {
					ps.close(); ps = null;
				}
				if (rs != null) {
					rs.close(); rs = null;
				}
			} catch (SQLException e) {
				logger.debug("", e);
				return ret;
			}
		}
		drops.put(monsterId, ret);
		return ret;
	}

	public final void clearDrops() {
		drops.clear();
		globaldrops.clear();
		loadGlobalDropInfo();
	}
}
