package client.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.SessionFlag;
import a.my.made.dao.AccountDAO;
import a.my.made.dao.ParamMap;
import client.MapleCharacter;
import client.MapleClient;
import database.MYSQL;
import launch.ChannelServer;

public class BanningCommands implements Command {
	private static final Logger logger = LoggerFactory.getLogger(BanningCommands.class);
	
	@Override
	public void execute(MapleClient client, String[] args) throws Exception {
		final String command = args[0];
		
		if (command.equals("!��")) {
			if (args.length < 1) {
				client.getPlayer().dropMessage(6, "!�� <ĳ�����̸�>");
			} else {
				final String charName = args[1];
				
				//��ü ä���� ������ ���� �������̶�� ������ ���������.
				for (ChannelServer server : ChannelServer.getAllInstances()) {
					MapleCharacter player = server.getPlayerStorage().getCharacterByName(charName);
					if( player != null ) {
						player.getClient().getSession().setAttribute(SessionFlag.KEY_CHAR_SAVE, "N");
						//TODO ä�� ���� �Ӹ� �ƴ϶� �α���, ����, ĳ�ü� �ʵ� ������ �����ؾ� �ϴ��� Ȯ���� �ʿ��ϴ�.
						player.getClient().getSession().closeNow();
					}
				}
				
				//DB update
				Connection con = null;
				PreparedStatement ps = null;
				ResultSet rs = null;
				try {
					con = MYSQL.getConnection();
					ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
					ps.setString(1, charName);
					rs = ps.executeQuery();
					
					int accountId = 0;
					if( rs.next() ) {
						accountId = rs.getInt("accountid");
					}
					
					if( accountId != 0 ) {
						final ParamMap params = new ParamMap();
						params.put("banned", 1);
						params.put("banreason", "");
						AccountDAO.setAccountInfo(accountId, params);
					}
					client.getPlayer().dropMessage(6, args[1] + " �� ����.");
				} catch (SQLException e) {
					logger.debug("{}", e);
				} finally {
					try {
						if( ps != null ) {
							ps.close(); ps = null;
						}
						if( rs != null ) {
							rs.close(); rs = null;
						}
					} catch(SQLException e) {
						logger.debug("{}", e);
					}
				}
			}
			
		} else if (command.equals("!���")) {
			if (args.length < 1) {
				client.getPlayer().dropMessage(6, "!��� <ĳ�����̸�>");
			} else {
				final boolean result = client.unBan(args[1]);
				if( result ) {
					client.getPlayer().dropMessage(6, "ĳ���Ͱ� ���������� ���� �����Ǿ����ϴ�.");
				}
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
				new CommandDefinition("�¶��ι�", "<ĳ�����̸�> <����>", "�¶��ι�", 3),
				new CommandDefinition("�������ι�", "<ĳ�����̸�>", "�������ι�", 3),
				new CommandDefinition("���", "<ĳ�����̸�>", "���", 3)
				};
	}
}
