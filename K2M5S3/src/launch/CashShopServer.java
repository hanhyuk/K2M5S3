package launch;

import java.io.IOException;
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
import launch.helpers.ChracterTransfer;
import launch.holder.MapleCashShopPlayerHolder;
import packet.crypto.EncryptionFactory;

public class CashShopServer {
	private static final Logger logger = LoggerFactory.getLogger(CashShopServer.class);

	private final int PORT = ServerConstants.cashShopPort;
	private IoAcceptor acceptor;
	private MapleCashShopPlayerHolder players;
	private static final CashShopServer instance = new CashShopServer();
	private final String CLIENT_KEY = "CASH_SHOP_SESSION_KEY";

	public static final CashShopServer getInstance() {
		return instance;
	}

	public void start() {
		IoBuffer.setUseDirectBuffer(false);
		IoBuffer.setAllocator(new CachedBufferAllocator());
		players = new MapleCashShopPlayerHolder();
		try {
			/* 소켓 설정 시작 */
			acceptor = new NioSocketAcceptor();
			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory(ServerType.CASHSHOP, CLIENT_KEY)));
			acceptor.setHandler(new MapleServerHandler(ServerType.CASHSHOP, CLIENT_KEY));
			acceptor.bind(new InetSocketAddress(PORT));
			/* 소켓 설정 종료 */
			logger.info("[알림] 캐시샵서버가 {} 포트를 성공적으로 개방하였습니다.", PORT);
		} catch (IOException e) {
			logger.info("[알림] 캐시샵서버가 {} 포트를 개방하는데 실패했습니다. {}", PORT, e);
		}
		//TODO 서버가 정상적으로 종료 또는 강제 종료와 같은 비정상 종료 처리 할 경우
		//아래 로직이 수행된다. 일단 서버 셧다운 처리와 관련된 로직이 정리되지 않았으므로
		//사용하지 않도록 주석 처리하고, 이후 정리가 되면 다시 활성화 시킬 예정이다.
		//Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
	}

	public final MapleCashShopPlayerHolder getPlayerStorage() {
		return players;
	}

	public final void shutdown() {
		players.disconnectAll();
		acceptor.unbind(new InetSocketAddress(PORT));
	}

	public int getManagedSessionCount() {
		return acceptor.getManagedSessionCount();
	}
	
	private final class ShutDownListener implements Runnable {

		@Override
		public void run() {
			players.disconnectAll();
			acceptor.unbind(new InetSocketAddress(PORT));
		}
	}

	public void ChannelChange_Data(ChracterTransfer transfer, int characterid) {
		getPlayerStorage().registerPendingPlayer(transfer, characterid);
	}

	public final boolean isCharacterInCS(String name) {
		return getPlayerStorage().isCharacterConnected(name);
	}
}
