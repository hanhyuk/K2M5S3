/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 배지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import client.MapleClient;
import constants.ServerConstants;
import launch.world.WorldConnected;
import java.util.Map;

public class ConnectedCommand implements Command {

    @Override
    public void execute(MapleClient c, String[] splittedLine) throws Exception, IllegalCommandSyntaxException {

	    Map<Integer, Integer> connected = WorldConnected.getConnected(c.getWorld());
	    StringBuilder conStr = new StringBuilder("현재 접속중인 인원: ");
	    boolean first = true;
	    for (int i : connected.keySet()) {
		if (!first) {
		    conStr.append(", ");
		} else {
		    first = false;
		}
		if (i == 0) {
		    conStr.append("총: ");
		    conStr.append(connected.get(i));
		} else {
		    conStr.append("채널");
		    conStr.append(i);
		    conStr.append(": ");
		    conStr.append(connected.get(i));
		}
	    }
	    c.getPlayer().dropMessage(6, conStr.toString());
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
		    new CommandDefinition("연결", "", "각 채널에 연결된 유저수를 출력합니다.", 1)
	};
    }
}
