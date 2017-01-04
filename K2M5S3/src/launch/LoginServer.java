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
	 * 서버와 클라이언트는 소켓통신을 한다. 그리고 클라이언트가 실행되면 로그인 서버로 접속을 시도 하고, 이 연결을 위한 기본 설정을
	 * 이곳에서 한다.
	 */
	public void start() {
		try {
			/*
			 * 아래 2라인에 관한 자세한 설명은 아래 링크를 참고 http://civan.tistory.com/160,
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

			logger.info("로그인서버가 {} 포트를 성공적으로 개방하였습니다.", LOGIN_PORT);
		} catch (Exception e) {
			logger.error("로그인서버가 {} 포트를 개방하는데 실패했습니다. {}", LOGIN_PORT, e);
		}
	}

	public void shutdown() {
		logger.info("로그인 서버를 종료 합니다.");
		WorldTimer.getInstance().stop();
		acceptor.unbind(new InetSocketAddress(LOGIN_PORT));
	}
	
	public int getManagedSessionCount() {
		return acceptor.getManagedSessionCount();
	}
}