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

public class LoggerChatting {
	private static final Logger logger = LoggerFactory.getLogger(LoggerChatting.class);
	
	public static String chatLog = "ChatLog.txt";

	public static void writeLog(String log, String text) {
		try {
			Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
			File file = new File(ServerConstants.getRootPath() + "Settings/Logs/" + log);

			FileOutputStream fos = new FileOutputStream(file, true);

			fos.write((currentTime.getTime().toLocaleString() + " " + text + "" + System.getProperty("line.separator")).getBytes());
			fos.close();
		} catch (Exception e) {
			logger.debug("{}", e);
		}
	}

	public static String getChatLogType(String type, MapleCharacter chr, String chattext) {
		return "[" + type + "] " + chr.getName() + " : " + chattext + " ÇöÀç¸Ê : " + chr.getMap().getStreetName() + "-" + chr.getMap().getMapName() + " (" + chr.getMap().getId() + ")";
	}
}
