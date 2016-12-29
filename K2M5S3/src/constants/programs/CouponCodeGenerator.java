package constants.programs;

import java.io.Console;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.MYSQL;
import tools.Randomizer;

public class CouponCodeGenerator {
	private static final Logger logger = LoggerFactory.getLogger(CouponCodeGenerator.class);

	public static void main(String[] args) {
		Connection con = MYSQL.getConnection();
		Console c = System.console();

		String temp = "Maple";
		do {
			logger.debug("쿠폰 타입 : 1 - 넥슨 캐쉬, 2 - 메이플 포인트, 3 - 메소");
			temp = c.readLine("쿠폰 타입? = ");
			if (!(temp.equals("1") || temp.equals("2") || temp.equals("3")))
				continue;
		} while (hasNumberFormatException(temp));
		final int type = Integer.parseInt(temp);

		temp = "Maple";
		do {
			temp = c.readLine("얼마 지급? = ");
		} while (hasNumberFormatException(temp));
		final int amount = Integer.parseInt(temp);

		temp = "Maple";
		do {
			temp = c.readLine("만들 갯수? = ");
		} while (hasNumberFormatException(temp));
		final int howMany = Integer.parseInt(temp);

		temp = "Maple";
		do {
			temp = c.readLine("중복 가능 횟수 = ");
		} while (hasNumberFormatException(temp));
		final int canValid = Integer.parseInt(temp);

		final String format = String.format("%d번 타입의 캐시를 %d번 지급하는 %d회 중복 가능한 쿠폰을 %d개 만듭니다. 맞습니까?", type, amount,
				canValid, howMany);
		if (!c.readLine(format + " (y/n) = ").equals("n")) {
			int number = howMany;
			PreparedStatement ps = null;
			while (number > 0) {
				String code = randCouponCode();
				try {
					ps = con.prepareStatement("INSERT INTO nxcode (`code`, valid, `type`, `item`) VALUES (?, ?, ?, ?)");
					ps.setString(1, code);
					ps.setInt(2, canValid);
					ps.setInt(3, type);
					ps.setInt(4, amount);
					ps.executeUpdate();
					printCouponInfo(code, amount, type, canValid, null);
					number--;
				} catch (SQLException ex) {
					logger.debug("[에러] {} {}", code, ex);
					continue;
				}
			}
		} else
			main(args);
	}

	private static String randCouponCode() {
		final String part = "12345QWERTYUIOPASDFGHJKLZXCVBNM67890";
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 30) {
			sb.append(Character.toString(part.charAt(Randomizer.nextInt(part.length()))));
		}
		return sb.toString();
	}

	public static String printCouponInfo(String code, int item, int type, int valid, String user) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(code).append(" | ");
			sb.append(item).append(" ").append(type == 1 ? "넥슨 캐쉬" : type == 2 ? "메이플 포인트" : "메소").append(" | ");
			sb.append(valid).append("회 사용 가능");
			sb.append("\r\n");
			FileOutputStream out = new FileOutputStream("쿠폰 코드.txt", true);
			out.write(sb.toString().getBytes());
			out.close();
			return sb.toString();
		} catch (Exception ex) {
		}
		return "정보가 존재하지 않음";
	}

	private static boolean hasNumberFormatException(String str) {
		try {
			Integer.parseInt(str);
			return false;
		} catch (NumberFormatException nfe) {
			return true;
		}
	}
}
