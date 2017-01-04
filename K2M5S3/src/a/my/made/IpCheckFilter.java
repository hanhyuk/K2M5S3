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
 * 클라이언트에서 로그인 서버로 접근 할때, 접근주기가 너무 빠를 경우 접속차단하도록 처리.
 * 차단 된 아이피는 서버를 리붓하기 전에는 접속 할 수 없다.
 * 
 * @deprecated 현재 사용하지 않음
 */
public class IpCheckFilter extends IoFilterAdapter {
	private static final Logger logger = LoggerFactory.getLogger(IpCheckFilter.class);
	
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
			logger.warn("블록 처리된 [{}] 에서 로그인을 시도 하였습니다. 연결을 해제 합니다.", address);
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
				logger.warn("[{}] 아이피를 블록 처리 하였습니다. 연결을 해제 합니다.", address);
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
