package a.my.made;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;

public class LogUtils {
	
	public static final String getLogMessage(final MapleCharacter cfor, final String message) {
		return getLogMessage(cfor == null ? null : cfor.getClient(), message);
	}
	public static final String getLogMessage(final MapleCharacter cfor, final String message, final Object... parms) {
		return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
	}
	public static final String getLogMessage(final MapleClient cfor, final String message) {
		return getLogMessage(cfor, message, new Object[0]);
	}
	public static final String getLogMessage(final MapleClient cfor, final String message, final Object... parms) {
		final StringBuilder builder = new StringBuilder();
		if (cfor != null) {
			if (cfor.getPlayer() != null) {
				builder.append("<");
				builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
				builder.append(" (캐릭터식별코드: ");
				builder.append(cfor.getPlayer().getId());
				builder.append(")> ");
			}
			if (cfor.getAccountName() != null) {
				builder.append("(계정: ");
				builder.append(cfor.getAccountName());
				builder.append(") ");
			}
		}
		builder.append(message);
		int start;
		for (final Object parm : parms) {
			start = builder.indexOf("{}");
			builder.replace(start, start + 2, parm.toString());
		}
		return builder.toString();
	}
}
