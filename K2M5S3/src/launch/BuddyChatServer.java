/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
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
            /* 소켓 설정 시작 */
            acceptor = new NioSocketAcceptor();        
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new EncryptionFactory()));
            acceptor.setHandler(new MapleServerHandler(ServerType.BUDDYCHAT));
            acceptor.bind(new InetSocketAddress(PORT));
            /* 소켓 설정 종료 */
            System.out.println("[알림] 친구채팅서버가 " + PORT + " 포트를 성공적으로 개방하였습니다.");
	} catch (IOException e) {
	    System.err.println("[오류] 친구채팅서버가 " + PORT + " 포트를 개방하는데 실패했습니다.");
            e.printStackTrace();
	}
    }

    public final void shutdown() {
	acceptor.unbind(new InetSocketAddress(PORT));
    }
}
