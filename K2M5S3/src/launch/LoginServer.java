/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
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
                throw new RuntimeException("[����] ��� ĳ���͸� �������� ��Ű�µ� �����߽��ϴ�. �����ͺ��̽� ������ �ùٸ��� Ȯ���� �ּ���.");
            }
	} catch (Exception re) {
	    System.err.println("[����] �α��� ���� ������ ������ �߻��߽��ϴ�.");
            if (!ServerConstants.realese) re.printStackTrace();
	}

	IoBuffer.setUseDirectBuffer(false);
	IoBuffer.setAllocator(new CachedBufferAllocator());
        
	WorldTimer.getInstance().start();
	WorldTimer.getInstance().register(new MapleRankingWorker(), 1000 * 60 * 60);
	MapleLoginHelper.getInstance();
        
	try {
            /* ���� ���� ���� */
            acceptor = new NioSocketAcceptor();        
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory()));
            acceptor.setHandler(new MapleServerHandler(ServerType.LOGIN));
            acceptor.bind(new InetSocketAddress(PORT));
            /* ���� ���� ���� */
            System.out.println("[�˸�] �α��μ����� " + PORT + " ��Ʈ�� ���������� �����Ͽ����ϴ�.");
	} catch (IOException e) {
	    System.err.println("[����] �α��μ����� " + PORT + " ��Ʈ�� �����ϴµ� �����߽��ϴ�.");
            if (!ServerConstants.realese) {
                e.printStackTrace();
            }
	}
    }

    public void shutdown() {
	System.out.println("[����] ������ �����մϴ�..");
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