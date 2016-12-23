/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package launch;

import client.MapleClient;
import constants.ServerConstants;
import constants.subclasses.ServerType;
import handler.MapleServerHandler;
import packet.crypto.EncryptionFactory;
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

public class BuddyChatServer {

    private IoAcceptor acceptor;
    private static int PORT = ServerConstants.BuddyChatPort;
    private static BuddyChatServer Instance = new BuddyChatServer();
    
    public final static Map<Integer,MapleClient> ChatClient = new HashMap<Integer,MapleClient>();
    
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
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory()));
            acceptor.setHandler(new MapleServerHandler(ServerType.BUDDYCHAT));
            acceptor.bind(new InetSocketAddress(PORT));
            /* ���� ���� ���� */
            System.out.println("[�˸�] ģ��ä�ü����� " + PORT + " ��Ʈ�� ���������� �����Ͽ����ϴ�.");
	} catch (IOException e) {
	    System.err.println("[����] ģ��ä�ü����� " + PORT + " ��Ʈ�� �����ϴµ� �����߽��ϴ�.");
            e.printStackTrace();
	}
    }

    public final void shutdown() {
	acceptor.unbind(new InetSocketAddress(PORT));
    }
}
