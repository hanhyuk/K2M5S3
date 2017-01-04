package a.my.made;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.Pair;

/**
 * Ŭ���̾�Ʈ���� �α��� ������ ���� �Ҷ�, �����ֱⰡ �ʹ� ���� ��� ���������ϵ��� ó��.
 * ���� �� �����Ǵ� ������ �����ϱ� ������ ���� �� �� ����.
 * 
 * @deprecated ���� ������� ����
 */
public class IpCheckFilter extends IoFilterAdapter {
	private static final Logger logger = LoggerFactory.getLogger(IpCheckFilter.class);
	
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
			logger.warn("��� ó���� [{}] ���� �α����� �õ� �Ͽ����ϴ�. ������ ���� �մϴ�.", address);
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
				logger.warn("[{}] �����Ǹ� ��� ó�� �Ͽ����ϴ�. ������ ���� �մϴ�.", address);
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
