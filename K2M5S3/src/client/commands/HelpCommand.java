package client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;

public class HelpCommand implements Command {
	private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);
	
    @Override
    public void execute(MapleClient c, String[] splittedLine) throws Exception, IllegalCommandSyntaxException {
        try {
            CommandProcessor.getInstance().dropHelp(c.getPlayer(), CommandProcessor.getOptionalIntArg(splittedLine, 1, 1));
        } catch (Exception e) {
            logger.debug("{}", e);
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
            new CommandDefinition("GM��ɾ�", "[������ - �⺻�� : 1]", "��ɾ� ������ ǥ���մϴ�.", 1),
            new CommandDefinition("GM����", "[������ - �⺻�� : 1]", "��ɾ� ������ ǥ���մϴ�.", 1)
	};
    }
}
