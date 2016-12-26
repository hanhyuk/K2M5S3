package launch;

import java.net.InetSocketAddress;
import java.sql.PreparedStatement;

import org.apache.mina.core.buffer.CachedBufferAllocator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import a.my.made.IpCheckFilter;
import community.MapleGuildCharacter;
import constants.ServerConstants;
import constants.subclasses.ServerType;
import database.MYSQL;
import handler.MapleServerHandler;
import launch.holder.WideObjectHolder;
import packet.crypto.EncryptionFactory;
import tools.Timer.WorldTimer;

public class LoginServer {
	private final int LOGIN_PORT = ServerConstants.LoginPort;
	private static LoginServer instance = new LoginServer();
	private IoAcceptor acceptor;
	private String serverName, eventMessage;
	private byte flag;
	private int userLimit;

	public static LoginServer getInstance() {
		return instance;
	}

	public void deleteGuildCharacter(MapleGuildCharacter mgc) {
		WideObjectHolder.getInstance().setGuildMemberOnline(mgc, false, -1);
		if (mgc.getGuildRank() > 1) { // not leader
			WideObjectHolder.getInstance().leaveGuild(mgc);
		} else {
			WideObjectHolder.getInstance().disbandGuild(mgc.getGuildId());
		}
	}

	/**
	 * ������ Ŭ���̾�Ʈ�� ��������� �Ѵ�. �׸��� Ŭ���̾�Ʈ�� ����Ǹ� �α��� ������ ������ �õ� �ϰ�,
	 * �� ������ ���� �⺻ ������ �̰����� �Ѵ�. 
	 */
	public void run_startup_configurations() {
		try {
			userLimit = ServerConstants.defaultMaxChannelLoad;
			serverName = ServerConstants.serverName;
			eventMessage = ServerConstants.eventMessage;
			flag = ServerConstants.defaultFlag;
			
			PreparedStatement ps = MYSQL.getConnection().prepareStatement("UPDATE accounts SET loggedin = 0");
			ps.executeUpdate();
			ps.close(); ps = null;
			
			
			/*
			 * �Ʒ� 2���ο� ���� �ڼ��� ������ �Ʒ� ��ũ�� ���� 
			 * http://civan.tistory.com/160,
			 * https://mina.apache.org/mina-project/userguide/ch8-iobuffer/ch8-iobuffer.html
			 */
			IoBuffer.setUseDirectBuffer(false);
			IoBuffer.setAllocator(new CachedBufferAllocator());

			
			acceptor = new NioSocketAcceptor();
			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			acceptor.getFilterChain().addFirst("ipCheck", new IpCheckFilter());
			acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory()));
			acceptor.setHandler(new MapleServerHandler(ServerType.LOGIN));
			acceptor.bind(new InetSocketAddress(LOGIN_PORT));
			
			System.out.println("[�˸�] �α��μ����� " + LOGIN_PORT + " ��Ʈ�� ���������� �����Ͽ����ϴ�.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		System.out.println("[����] ������ �����մϴ�..");
		WorldTimer.getInstance().stop();
		acceptor.unbind(new InetSocketAddress(LOGIN_PORT));
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