package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ServerConstants;

public class MYSQL {
	private static final Logger logger = LoggerFactory.getLogger(MYSQL.class);
	
	public static final int CLOSE_CURRENT_RESULT = 1;
	public static final int KEEP_CURRENT_RESULT = 2;
	public static final int CLOSE_ALL_RESULTS = 3;
	public static final int SUCCESS_NO_INFO = -2;
	public static final int EXECUTE_FAILED = -3;
	public static final int RETURN_GENERATED_KEYS = 1;
	public static final int NO_GENERATED_KEYS = 2;

	private static final ThreadLocal<Connection> con = new ThreadLocalConnection();

	public static Connection getConnection() {
		Connection c = con.get();
		try {
			c.getMetaData();
		} catch (SQLException e) {
			con.remove();
			c = con.get();
		}
		return c;
	}

	private static final class ThreadLocalConnection extends ThreadLocal<Connection> {

		@Override
		protected final Connection initialValue() {
			try {
				Class.forName("com.mysql.jdbc.Driver"); // touch the mysql
														// driver
			} catch (final ClassNotFoundException e) {
				logger.debug("[����] MYSQL Ŭ������ �߰��� �� �����ϴ�. {}", e);
			}

			try {
				return DriverManager.getConnection(
						"jdbc:mysql://" 
								+ ServerConstants.dbHost + ":" 
								+ ServerConstants.dbPort
								+ "/arcstory?autoReconnect=true&characterEncoding=euckr&useSSL=false&maxReconnects=5",
						ServerConstants.dbUser, 
						ServerConstants.dbPassword);
			} catch (SQLException e) {
				logger.debug("[����] �����ͺ��̽� ���ῡ ������ �߻��߽��ϴ�. {}", e);
				return null;
			}
		}
	}
}