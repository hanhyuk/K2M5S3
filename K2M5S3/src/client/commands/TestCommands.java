/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 배지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import static client.commands.CommandProcessor.getOptionalIntArg;
import client.MapleClient;
import packet.creators.MainPacketCreator;
import tools.StringUtil;

public class TestCommands implements Command {

    @Override
    public void execute(final MapleClient c, final String[] splitted) throws Exception, IllegalCommandSyntaxException {
        if (splitted[0].equals("!시계")) {
	    c.getPlayer().getMap().broadcastMessage(MainPacketCreator.getClock(getOptionalIntArg(splitted, 1, 60)));
	} else if (splitted[0].equals("!패킷")) {
	    if (splitted.length > 1) {
		c.getSession().write(MainPacketCreator.getPacketFromHexString(StringUtil.joinStringFrom(splitted, 1)));
	    } else {
		c.getPlayer().dropMessage(6, "패킷데이터를 입력해주세요.");
	    }
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
	    new CommandDefinition("시계", "<시간>", "현재 맵에 시간을 띄웁니다.", 2),
	    new CommandDefinition("패킷", "<헥스데이터>", "입력한 16진수 데이터의 패킷을 현재 클라이언트에 보냅니다.", 6),
	};
      }
}
