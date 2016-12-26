package a.my.made;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

import tools.Pair;

/**
 * Ŭ���̾�Ʈ���� �α��� ������ ���� �Ҷ�, �����ֱⰡ �ʹ� ���� ��� ���������ϵ��� ó��.
 * ���� �� �����Ǵ� ������ �����ϱ� ������ ���� �� �� ����.
 */
public class IpCheckFilter extends IoFilterAdapter {
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
		
		nextFilter.sessionOpened(session);
	}
}
