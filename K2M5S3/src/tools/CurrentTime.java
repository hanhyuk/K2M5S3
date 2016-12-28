package tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CurrentTime {

	public static String getCurrentTime() {
		Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
		SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
		String time = simpleTimeFormat.format(calz.getTime());
		return time;
	}

	public static String getAllCurrentTime() {
		Calendar calz = Calendar.getInstance(TimeZone.getTimeZone("KST"), Locale.KOREAN);
		SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = simpleTimeFormat.format(calz.getTime());
		return time;
	}

	public static int getLeftTimeFromMinute(int minute) {
		Calendar d = Calendar.getInstance(TimeZone.getTimeZone("KST"));
		int min = d.get(Calendar.MINUTE), sec = d.get(Calendar.SECOND);
		int secs = (min * 60) + sec;
		int leftsecs = (minute * 60) - (secs % (minute * 60));
		return leftsecs;
	}
}
