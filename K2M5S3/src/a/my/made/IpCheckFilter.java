package a.my.made;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

import tools.Pair;

/**
 * 클라이언트에서 로그인 서버로 접근 할때, 접근주기가 너무 빠를 경우 접속차단하도록 처리.
 * 차단 된 아이피는 서버를 리붓하기 전에는 접속 할 수 없다.
 */
public class IpCheckFilter implements IoFilter {

	/**
	 * 접속 시도 간격 
	 */
	private long DELAY_VALUE = 2000L;
	/**
	 * 최대 접속 시도 횟수
	 */
	private int CHECK_COUNT = 10;
	
	private final List<String> blockedIpList = new ArrayList<String>();
	private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();
	
	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		final String address = session.getRemoteAddress().toString().split(":")[0];
		
		if (blockedIpList.contains(address)) {
			System.out.println("블록 처리된 [" + address + "] 에서 로그인을 시도 하였습니다. 연결을 해제 합니다.");
			session.closeNow();
			return;
		}
		
		final Pair<Long, Byte> track = tracker.get(address);

		byte count;
		if (track == null) {
			count = 1;
		} else {
			count = track.right;

			final long difference = System.currentTimeMillis() - track.left;
			
			//접속 시도 간격이 DELAY_VALUE 보다 빠를 경우 count 증가
			if (difference < DELAY_VALUE) {
				count++;
			}
			
			//CHECK_COUNT 값 이상 시도한 경우 아이피 블록 처리 
			if (CHECK_COUNT <= count) {
				System.out.println("[" + address + "] 아이피를 블록 처리 하였습니다. 연결을 해제 합니다.");
				blockedIpList.add(address);
				tracker.remove(address);
				session.closeNow();
				return;
			}
		}
		
		tracker.put(address, new Pair<Long, Byte>(System.currentTimeMillis(), count));
	}
	
	@Override
	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		nextFilter.sessionClosed(session);
	}
	
	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		System.out.println("IpCheckFilter");
	}
	
	@Override
	public void init() throws Exception {
	}

	@Override
	public void destroy() throws Exception {
	}

	@Override
	public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
	}

	@Override
	public void onPostAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		
		
	}

	@Override
	public void onPreRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		
		
	}

	@Override
	public void onPostRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		
		
	}

	@Override
	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		
		
	}

	@Override
	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		
		
	}

	@Override
	public void inputClosed(NextFilter nextFilter, IoSession session) throws Exception {
		
		
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		
		
	}

	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		
		
	}

	@Override
	public void filterClose(NextFilter nextFilter, IoSession session) throws Exception {
		
		
	}

	@Override
	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		
		
	}

}
