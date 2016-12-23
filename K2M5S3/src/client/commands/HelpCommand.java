/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 배지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import constants.ServerConstants;
import client.MapleClient;

public class HelpCommand implements Command {

    @Override
    public void execute(MapleClient c, String[] splittedLine) throws Exception, IllegalCommandSyntaxException {
        try {
            CommandProcessor.getInstance().dropHelp(c.getPlayer(), CommandProcessor.getOptionalIntArg(splittedLine, 1, 1));
        } catch (Exception e) {
            if (!ServerConstants.realese) e.printStackTrace();
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
            new CommandDefinition("GM명령어", "[페이지 - 기본값 : 1]", "명령어 도움말을 표시합니다.", 1),
            new CommandDefinition("GM도움말", "[페이지 - 기본값 : 1]", "명령어 도움말을 표시합니다.", 1)
	};
    }
}
