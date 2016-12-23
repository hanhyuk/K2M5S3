/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package launch;

import constants.ServerConstants;
import constants.subclasses.ServerType;
import community.MapleGuildCharacter;
import database.MYSQL;
import handler.MapleServerHandler;
import launch.helpers.MapleLoginHelper;
import launch.helpers.MapleRankingWorker;
import launch.holder.WideObjectHolder;
import packet.crypto.EncryptionFactory;
import tools.Timer.WorldTimer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.buffer.CachedBufferAllocator;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class LoginServer {
    public static int PORT = ServerConstants.LoginPort;
    private IoAcceptor acceptor;
    private String serverName, eventMessage;
    private byte flag;
    private int userLimit;
    private static LoginServer instance = new LoginServer();
    public static boolean Running = false;
    public boolean isReboot = false;
    
    public static LoginServer getInstance() {
	return instance;
    }
    
    public void deleteGuildCharacter(MapleGuildCharacter mgc) {
	WideObjectHolder.getInstance().setGuildMemberOnline(mgc, false, -1);
	if (mgc.getGuildRank() > 1) { //not leader
	    WideObjectHolder.getInstance().leaveGuild(mgc);
	} else {
	    WideObjectHolder.getInstance().disbandGuild(mgc.getGuildId());
	}
    }
    

    public void run_startup_configurations() {
	try {
            userLimit = ServerConstants.defaultMaxChannelLoad;
            serverName = ServerConstants.serverName;
            eventMessage = ServerConstants.eventMessage;
            flag = ServerConstants.defaultFlag;
            try {
                PreparedStatement ps = MYSQL.getConnection().prepareStatement("UPDATE accounts SET loggedin = 0");
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                throw new RuntimeException("[오류] 모든 캐릭터를 접속종료 시키는데 실패했습니다. 데이터베이스 연결이 올바른지 확인해 주세요.");
            }
	} catch (Exception re) {
	    System.err.println("[오류] 로그인 서버 실행중 오류가 발생했습니다.");
            if (!ServerConstants.realese) re.printStackTrace();
	}

	IoBuffer.setUseDirectBuffer(false);
	IoBuffer.setAllocator(new CachedBufferAllocator());
        
	WorldTimer.getInstance().start();
	WorldTimer.getInstance().register(new MapleRankingWorker(), 1000 * 60 * 60);
	MapleLoginHelper.getInstance();
        
	try {
            /* 소켓 설정 시작 */
            acceptor = new NioSocketAcceptor();        
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory()));
            acceptor.setHandler(new MapleServerHandler(ServerType.LOGIN));
            acceptor.bind(new InetSocketAddress(PORT));
            /* 소켓 설정 종료 */
            System.out.println("[알림] 로그인서버가 " + PORT + " 포트를 성공적으로 개방하였습니다.");
	} catch (IOException e) {
	    System.err.println("[오류] 로그인서버가 " + PORT + " 포트를 개방하는데 실패했습니다.");
            if (!ServerConstants.realese) {
                e.printStackTrace();
            }
	}
    }

    public void shutdown() {
	System.out.println("[종료] 서버를 종료합니다..");
	WorldTimer.getInstance().stop();
        acceptor.unbind(new InetSocketAddress(PORT));
        Running = false;
    }

    public String getServerName() {
	return serverName;
    }

    public String getEventMessage() {
	return eventMessage;
    }

    public byte getFlag() {
	return flag;
    }

    public void setEventMessage(String newMessage) {
	this.eventMessage = newMessage;
    }

    public void setFlag(byte newflag) {
	flag = newflag;
    }

    public int getNumberOfSessions() {
	return acceptor.getManagedSessions().size();
    }

    public int getUserLimit() {
	return userLimit;
    }

    public void setUserLimit(int newLimit) {
	userLimit = newLimit;
    }
}