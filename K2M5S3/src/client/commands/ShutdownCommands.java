package client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import launch.BuddyChatServer;
import launch.CashShopServer;
import launch.ChannelServer;
import launch.LoginServer;
import launch.helpers.ShutdownServer;
import tools.Timer;

public class ShutdownCommands implements Command {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownCommands.class);
	
	@Override
	public void execute(MapleClient client, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		final String command = splitted[0];
		
		//TODO !서버종료 명령어 테스트 필요.
		if (command.equals("!셧다운")) {
			
			//현재 등록된 모든 스케쥴링을 중단
			Timer.stopAllTimer();
			
			//버디챗 서버 종료
			BuddyChatServer.getInstance().shutdown();
			
			//캐시샵 서버 종료
			CashShopServer.getInstance().shutdown();
			
			//로그인 서버 종료
			LoginServer.getInstance().shutdown();
			
			//TODO 고용 상점 종료 - 관련 로직 검토 후 적용해야 함.
			//client.getChannelServer().closeAllMerchant();
			
			//채널 서버 종료 방법 1 
			//TODO 방법 1,2 중에 어떤걸 사용할지 고민이 필요함.
			//new ShutdownServer(client.getChannel()).run();
			
			//채널 서버 종료 방법 2
			for (ChannelServer server : ChannelServer.getAllInstances()) {
				new ShutdownServer(server.getChannel()).run();
			}
			
			client.getPlayer().dropMessage(6, "셧다운 실행 완료!");
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] { 
				new CommandDefinition("셧다운", "", "서버를 재구동하기 전에 꼭 실행해야 하는 명령어.", 6) 
				};
	}
}
