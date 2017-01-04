package launch;

import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.CachedBufferAllocator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ServerConstants;
import constants.subclasses.ServerType;
import handler.MapleServerHandler;
import packet.crypto.EncryptionFactory;
import tools.Timer.WorldTimer;

public class LoginServer {
	private static final Logger logger = LoggerFactory.getLogger(LoginServer.class);

	private static LoginServer instance = new LoginServer();
	
	private final String CLIENT_KEY = "LOGIN_SESSION_KEY";
	private final int LOGIN_PORT = ServerConstants.loginPort;
	private IoAcceptor acceptor;
	
	public static LoginServer getInstance() {
		return instance;
	}

	/**
	 * ������ Ŭ���̾�Ʈ�� ��������� �Ѵ�. �׸��� Ŭ���̾�Ʈ�� ����Ǹ� �α��� ������ ������ �õ� �ϰ�, �� ������ ���� �⺻ ������
	 * �̰����� �Ѵ�.
	 */
	public void start() {
		try {
			/*
			 * �Ʒ� 2���ο� ���� �ڼ��� ������ �Ʒ� ��ũ�� ���� http://civan.tistory.com/160,
			 * https://mina.apache.org/mina-project/userguide/ch8-iobuffer/ch8-
			 * iobuffer.html
			 */
			IoBuffer.setUseDirectBuffer(false);
			IoBuffer.setAllocator(new CachedBufferAllocator());
//			IoBuffer.setAllocator(new SimpleBufferAllocator());

			acceptor = new NioSocketAcceptor();
			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			// acceptor.getFilterChain().addFirst("ipCheck", new IpCheckFilter());
			acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory(ServerType.LOGIN, CLIENT_KEY)));
			acceptor.setHandler(new MapleServerHandler(ServerType.LOGIN, CLIENT_KEY));
			acceptor.bind(new InetSocketAddress(LOGIN_PORT));

			logger.info("�α��μ����� {} ��Ʈ�� ���������� �����Ͽ����ϴ�.", LOGIN_PORT);
		} catch (Exception e) {
			logger.error("�α��μ����� {} ��Ʈ�� �����ϴµ� �����߽��ϴ�. {}", LOGIN_PORT, e);
		}
	}

	public void shutdown() {
		logger.info("�α��� ������ ���� �մϴ�.");
		WorldTimer.getInstance().stop();
		acceptor.unbind(new InetSocketAddress(LOGIN_PORT));
	}
	
	public int getManagedSessionCount() {
		return acceptor.getManagedSessionCount();
	}
}