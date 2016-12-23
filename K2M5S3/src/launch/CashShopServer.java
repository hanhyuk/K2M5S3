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
import handler.MapleServerHandler;
import launch.holder.MapleCashShopPlayerHolder;
import packet.crypto.EncryptionFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import launch.helpers.ChracterTransfer;
import org.apache.mina.core.buffer.CachedBufferAllocator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class CashShopServer {

    private final int PORT = ServerConstants.CashShopPort;
    private IoAcceptor acceptor;
    private MapleCashShopPlayerHolder players;
    private static final CashShopServer instance = new CashShopServer();

    public static final CashShopServer getInstance() {
	return instance;
    }

    public final void run_startup_configurations() {
	IoBuffer.setUseDirectBuffer(false);
	IoBuffer.setAllocator(new CachedBufferAllocator());
	players = new MapleCashShopPlayerHolder();
	try {
            /* ���� ���� ���� */
            acceptor = new NioSocketAcceptor();        
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory()));
            acceptor.setHandler(new MapleServerHandler(ServerType.CASHSHOP));
            acceptor.bind(new InetSocketAddress(PORT));
            /* ���� ���� ���� */
            System.out.println("[�˸�] ĳ�ü������� " + PORT + " ��Ʈ�� ���������� �����Ͽ����ϴ�.");
	} catch (IOException e) {
	    System.err.println("[����] ĳ�ü������� " + PORT + " ��Ʈ�� �����ϴµ� �����߽��ϴ�.");
	}
	Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
    }
    
    public final MapleCashShopPlayerHolder getPlayerStorage() {
	return players;
    }

    public final void shutdown() {
	players.disconnectAll();
	acceptor.unbind(new InetSocketAddress(PORT));
    }

    private final class ShutDownListener implements Runnable {

	@Override
	public void run() {
	    System.out.println("Saving all connected clients...");
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
