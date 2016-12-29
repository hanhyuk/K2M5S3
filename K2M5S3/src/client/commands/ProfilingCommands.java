package client.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import tools.CPUSampler;

public class ProfilingCommands implements Command {
	private static final Logger logger = LoggerFactory.getLogger(ProfilingCommands.class);

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] { new CommandDefinition("감시시작", "", "프로파일러로 CPU 감시를 시작합니다.", 6), new CommandDefinition("감시종료", "<파일이름>", "프로파일러로 CPU 감시를 종료하고 해당 파일이름으로 결과를 저장합니다.", 6) };
	}

	@Override
	public void execute(MapleClient c, String[] splitted) {

		if (splitted[0].equals("!감시시작")) {
			CPUSampler sampler = CPUSampler.getInstance();
			sampler.addIncluded("Maplenewhandler");
			sampler.addIncluded("anticheat");
			sampler.addIncluded("client");
			sampler.addIncluded("commands");
			sampler.addIncluded("community");
			sampler.addIncluded("constants");
			sampler.addIncluded("database");
			sampler.addIncluded("handler");
			sampler.addIncluded("launch");
			sampler.addIncluded("packet");
			sampler.addIncluded("provier");
			sampler.addIncluded("scripting");
			sampler.addIncluded("server");
			sampler.addIncluded("tools");
			sampler.start();

		} else if (splitted[0].equals("!감시종료")) {
			CPUSampler sampler = CPUSampler.getInstance();
			try {
				String filename = "odinprofile.txt";
				if (splitted.length > 1) {
					filename = splitted[1];
				}
				File file = new File(filename);
				if (file.exists()) {
					c.getPlayer().dropMessage(6, "입력한 파일이 이미 존재합니다. 다른 이름을 사용해 주세요.");
					return;
				}
				sampler.stop();
				FileWriter fw = new FileWriter(file);
				sampler.save(fw, 1, 10);
				fw.close();
			} catch (IOException e) {
				logger.debug("Error saving profile {}", e);
			}
			sampler.reset();
		}
	}
}
