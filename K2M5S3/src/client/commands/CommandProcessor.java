package client.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import client.MapleClient;
import tools.Pair;
import tools.StringUtil;

public class CommandProcessor {
	private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

	private final List<Pair<String, String>> gmlog = new LinkedList<Pair<String, String>>();
	private final Map<String, DefinitionCommandPair> commands = new LinkedHashMap<String, DefinitionCommandPair>();
	private static CommandProcessor instance = new CommandProcessor();
	private final Lock rl = new ReentrantLock();

	public static CommandProcessor getInstance() {
		return instance;
	}

	private CommandProcessor() {
		instance = this;
		registerCommand(new HelpCommand()); 
		registerCommand(new BanningCommands());
		registerCommand(new CharCommands());
		registerCommand(new CharInfoCommand());
		registerCommand(new ConnectedCommand());
		registerCommand(new DebugCommands());
		registerCommand(new MapleoCommands());
		registerCommand(new MonsterInfoCommands());
		registerCommand(new NPCSpawningCommands());
		registerCommand(new NoticeCommand());
		registerCommand(new PlayerCommand());
		registerCommand(new ProfilingCommands());
		registerCommand(new ReloadingCommands());
		registerCommand(new SearchCommands());
		registerCommand(new ServerMessageCommand());
		registerCommand(new ShutdownCommands());
		registerCommand(new SpawnMonsterCommand());
		registerCommand(new TestCommands());
		registerCommand(new WarpCommands());
		registerCommand(new ProfilingCommands());
		registerCommand(new SpawnObjectCommands());
	}

	private void registerCommand(Command command) {
		for (CommandDefinition def : command.getDefinition()) {
			commands.put(def.getCommand(), new DefinitionCommandPair(command, def));
		}
	}

	public static String joinAfterString(String splitted[], String str) {
		for (int i = 1; i < splitted.length; i++) {
			if (splitted[i].equalsIgnoreCase(str) && i + 1 < splitted.length) {
				return StringUtil.joinStringFrom(splitted, i + 1);
			}
		}
		return null;
	}

	public static int getOptionalIntArg(String splitted[], int position, int def) {
		if (splitted.length > position) {
			try {
				return Integer.parseInt(splitted[position]);
			} catch (NumberFormatException nfe) {
				return def;
			}
		}
		return def;
	}

	public static String getNamedArg(String splitted[], int startpos, String name) {
		for (int i = startpos; i < splitted.length; i++) {
			if (splitted[i].equalsIgnoreCase(name) && i + 1 < splitted.length) {
				return splitted[i + 1];
			}
		}
		return null;
	}

	public static Integer getNamedIntArg(String splitted[], int startpos, String name) {
		String arg = getNamedArg(splitted, startpos, name);
		if (arg != null) {
			try {
				return Integer.parseInt(arg);
			} catch (NumberFormatException nfe) {
				// swallow - we don't really care
			}
		}
		return null;
	}

	public static int getNamedIntArg(String splitted[], int startpos, String name, int def) {
		Integer ret = getNamedIntArg(splitted, startpos, name);
		if (ret == null) {
			return def;
		}
		return ret.intValue();
	}

	public static Double getNamedDoubleArg(String splitted[], int startpos, String name) {
		String arg = getNamedArg(splitted, startpos, name);
		if (arg != null) {
			try {
				return Double.parseDouble(arg);
			} catch (NumberFormatException nfe) {
				// swallow - we don't really care
			}
		}
		return null;
	}

	public boolean processCommand(MapleClient c, String line) {
		return instance.processCommandInternal(c, line);
	}

	public void dropHelp(MapleCharacter chr, int page) {
		List<DefinitionCommandPair> allCommands = new ArrayList<DefinitionCommandPair>(commands.values());
		int startEntry = (page - 1) * 20;
		chr.dropMessage(6, "명령 도움말: --------" + page + "---------");
		for (int i = startEntry; i < startEntry + 20 && i < allCommands.size(); i++) {
			CommandDefinition commandDefinition = allCommands.get(i).getDefinition();
			if (chr.hasGmLevel(commandDefinition.getRequiredLevel())) {
				chr.dropMessage(6, commandDefinition.getCommand() + " " + commandDefinition.getParameterDescription() + ": " + commandDefinition.getHelp());
			}
		}
	}

	private boolean processCommandInternal(MapleClient c, String line) {
		if (line.charAt(0) == '!' || line.charAt(0) == '@') {
			String[] splitted = line.split(" ");
			if (splitted.length > 0 && splitted[0].length() > 1) {
				DefinitionCommandPair definitionCommandPair = commands.get(splitted[0].substring(1));
				if (definitionCommandPair != null && c.getPlayer().getGMLevel() >= definitionCommandPair.getDefinition().getRequiredLevel()) {
					try {
						definitionCommandPair.getCommand().execute(c, splitted);
					} catch (IllegalCommandSyntaxException e) {
						logger.debug("{}", e);
						return true;
					} catch (Exception e) {
						logger.debug("{}", e);
						return true;
					}
					if (c != null)
						if (c.getPlayer() != null)
							if (c.getPlayer().getGMLevel() > 0) {
								rl.lock();
								try {
									gmlog.add(new Pair<String, String>(c.getPlayer().getName(), line));
								} finally {
									rl.unlock();
								}
							}
					return true;
				} else {
					c.getPlayer().dropMessage(6, "명령어 " + splitted[0] + " 은(는) 존재하지 않거나 실행권한이 없습니다.");
					return true;
				}
			}
		}
		return false;
	}
}

class DefinitionCommandPair {

	private Command command;
	private CommandDefinition definition;

	public DefinitionCommandPair(Command command, CommandDefinition definition) {
		super();
		this.command = command;
		this.definition = definition;
	}

	public Command getCommand() {
		return command;
	}

	public CommandDefinition getDefinition() {
		return definition;
	}
}
