package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import constants.ServerConstants;

/**
 * TODO ���� ä�� ����� ���Ϸ� �״µ�... �� �κ��� logback ���̺귯���� ���� ���ں� ���Ϸ� �������� ���� �ʿ�.
 */
public class LoggerChatting {
	private static final Logger logger = LoggerFactory.getLogger(LoggerChatting.class);
	
	public static void writeLog(String text) {
		try {
			Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
			File file = new File(ServerConstants.getRootPath() + ServerConstants.CONFIG_LOG_FILE_PATH);

			FileOutputStream fos = new FileOutputStream(file, true);

			fos.write((currentTime.getTime().toLocaleString() + " " + text + "" + System.getProperty("line.separator")).getBytes());
			fos.close();
		} catch (Exception e) {
			logger.debug("{}", e);
		}
	}

	public static String getChatLogType(String type, MapleCharacter chr, String chattext) {
		return "[" + type + "] " + chr.getName() + " : " + chattext + " ����� : " + chr.getMap().getStreetName() + "-" + chr.getMap().getMapName() + " (" + chr.getMap().getId() + ")";
	}
}
