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
		
		if (command.equals("!밴")) {
			if (args.length < 1) {
				client.getPlayer().dropMessage(6, "!밴 <캐릭터이름>");
			} else {
				final String charName = args[1];
				
				//전체 채널을 뒤져서 현재 접속중이라면 연결을 끊어버린다.
				for (ChannelServer server : ChannelServer.getAllInstances()) {
					MapleCharacter player = server.getPlayerStorage().getCharacterByName(charName);
					if( player != null ) {
						player.getClient().getSession().setAttribute(SessionFlag.KEY_CHAR_SAVE, "N");
						//TODO 채널 서버 뿐만 아니라 로그인, 버디, 캐시샵 쪽도 연결을 종료해야 하는지 확인이 필요하다.
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
					client.getPlayer().dropMessage(6, args[1] + " 밴 성공.");
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
			
		} else if (command.equals("!언밴")) {
			if (args.length < 1) {
				client.getPlayer().dropMessage(6, "!언밴 <캐릭터이름>");
			} else {
				final boolean result = client.unBan(args[1]);
				if( result ) {
					client.getPlayer().dropMessage(6, "캐릭터가 성공적으로 밴이 해제되었습니다.");
				}
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
				new CommandDefinition("온라인밴", "<캐릭터이름> <이유>", "온라인밴", 3),
				new CommandDefinition("오프라인밴", "<캐릭터이름>", "오프라인밴", 3),
				new CommandDefinition("언밴", "<캐릭터이름>", "언밴", 3)
				};
	}
}
