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
 * Ŭ���̾�Ʈ���� �α��� ������ ���� �Ҷ�, �����ֱⰡ �ʹ� ���� ��� ���������ϵ��� ó��.
 * ���� �� �����Ǵ� ������ �����ϱ� ������ ���� �� �� ����.
 */
public class IpCheckFilter implements IoFilter {

	/**
	 * ���� �õ� ���� 
	 */
	private long DELAY_VALUE = 2000L;
	/**
	 * �ִ� ���� �õ� Ƚ��
	 */
	private int CHECK_COUNT = 10;
	
	private final List<String> blockedIpList = new ArrayList<String>();
	private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();
	
	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		final String address = session.getRemoteAddress().toString().split(":")[0];
		
		if (blockedIpList.contains(address)) {
			System.out.println("��� ó���� [" + address + "] ���� �α����� �õ� �Ͽ����ϴ�. ������ ���� �մϴ�.");
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
			
			//���� �õ� ������ DELAY_VALUE ���� ���� ��� count ����
			if (difference < DELAY_VALUE) {
				count++;
			}
			
			//CHECK_COUNT �� �̻� �õ��� ��� ������ ��� ó�� 
			if (CHECK_COUNT <= count) {
				System.out.println("[" + address + "] �����Ǹ� ��� ó�� �Ͽ����ϴ�. ������ ���� �մϴ�.");
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
