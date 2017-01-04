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
		
		//TODO !�������� ��ɾ� �׽�Ʈ �ʿ�.
		if (command.equals("!�˴ٿ�")) {
			
			//���� ��ϵ� ��� �����층�� �ߴ�
			Timer.stopAllTimer();
			
			//����ê ���� ����
			BuddyChatServer.getInstance().shutdown();
			
			//ĳ�ü� ���� ����
			CashShopServer.getInstance().shutdown();
			
			//�α��� ���� ����
			LoginServer.getInstance().shutdown();
			
			//TODO ��� ���� ���� - ���� ���� ���� �� �����ؾ� ��.
			//client.getChannelServer().closeAllMerchant();
			
			//ä�� ���� ���� ��� 1 
			//TODO ��� 1,2 �߿� ��� ������� ����� �ʿ���.
			//new ShutdownServer(client.getChannel()).run();
			
			//ä�� ���� ���� ��� 2
			for (ChannelServer server : ChannelServer.getAllInstances()) {
				new ShutdownServer(server.getChannel()).run();
			}
			
			client.getPlayer().dropMessage(6, "�˴ٿ� ���� �Ϸ�!");
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] { 
				new CommandDefinition("�˴ٿ�", "", "������ �籸���ϱ� ���� �� �����ؾ� �ϴ� ��ɾ�.", 6) 
				};
	}
}
