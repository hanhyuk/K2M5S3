package launch;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.CachedBufferAllocator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import constants.ServerConstants;
import constants.subclasses.ServerType;
import handler.MapleServerHandler;
import packet.crypto.EncryptionFactory;

public class BuddyChatServer {
	private static final Logger logger = LoggerFactory.getLogger(BuddyChatServer.class);

	private IoAcceptor acceptor;
	private static int PORT = ServerConstants.BuddyChatPort;
	private static BuddyChatServer Instance = new BuddyChatServer();
	private final String CLIENT_KEY = "BUDDY_SESSION_KEY";

	public final static Map<Integer, MapleClient> ChatClient = new HashMap<Integer, MapleClient>();

	public static BuddyChatServer getInstance() {
		return Instance;
	}

	public final void run_startup_configurations() {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new CachedBufferAllocator());
		try {
			/* ���� ���� ���� */
			acceptor = new NioSocketAcceptor();
			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory(ServerType.BUDDYCHAT, CLIENT_KEY)));
			acceptor.setHandler(new MapleServerHandler(ServerType.BUDDYCHAT, CLIENT_KEY));
			acceptor.bind(new InetSocketAddress(PORT));
			/* ���� ���� ���� */
			logger.info("[�˸�] ģ��ä�ü����� {} ��Ʈ�� ���������� �����Ͽ����ϴ�.", PORT);
		} catch (IOException e) {
			logger.warn("[����] ģ��ä�ü����� {} ��Ʈ�� �����ϴµ� �����߽��ϴ�. {}", PORT, e);
		}
	}

	public final void shutdown() {
		acceptor.unbind(new InetSocketAddress(PORT));
	}
}
