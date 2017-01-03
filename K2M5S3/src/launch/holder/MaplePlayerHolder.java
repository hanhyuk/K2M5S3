package launch.holder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import a.my.made.UserType;
import client.MapleCharacter;
import launch.helpers.ChracterTransfer;
import packet.transfer.write.Packet;
import tools.Timer.WorldTimer;

public class MaplePlayerHolder {

	/**
	 * 사용자가 채널을 변경하거나 캐시샵 입장/퇴장 할때...
	 * 서버에서는 현재 사용자의 정보(MapleCharacter 객체)를 직렬화(ChracterTransfer 클래스) 한다.
	 * 직렬화 된 정보는 Map 에 add, get, remove 처리 된다.
	 *  
	 * 이 PersistingTask 의 기능은 직렬화 정보를 주기적으로 확인하여 일정시간이 지난 것의 경우 제거한다.
	 */
	public class PersistingTask implements Runnable {
		@Override
		public void run() {
			relatedToPendingMutex.lock();
			try {
				final long REMOVE_OVER_TIME = 1000 * 40;
				final long currenttime = System.currentTimeMillis();
				final Iterator<Map.Entry<Integer, ChracterTransfer>> itr = pendingCharacterMap.entrySet().iterator();

				while (itr.hasNext()) {
					if (currenttime - itr.next().getValue().tranferTime > REMOVE_OVER_TIME) {
						itr.remove();
					}
				}
			} finally {
				relatedToPendingMutex.unlock();
			}
		}
	}
	
	private final int REPEAT_TIME = 1000 * 60 * 15; //15분
	private final Lock mutex = new ReentrantLock();
	private final Lock relatedToPendingMutex = new ReentrantLock();
	private final Map<String, MapleCharacter> nameToCharMap = new HashMap<String, MapleCharacter>();
	private final Map<Integer, MapleCharacter> idToCharMap = new HashMap<Integer, MapleCharacter>();
	private final Map<Integer, Object> effectsMap = new HashMap<Integer, Object>();
	private final Map<Integer, ChracterTransfer> pendingCharacterMap = new HashMap<Integer, ChracterTransfer>();

	public MaplePlayerHolder() {
		WorldTimer.getInstance().register(new PersistingTask(), REPEAT_TIME);
	}

	/**
	 * 현재 접속중인 GM 유저들에게 메세지를 보낸다.
	 */
	public final void broadcastGMPacket(final Packet data) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToCharMap.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
				chr = itr.next();

				if (chr.getClient().isLoggedIn() && chr.hasGmLevel(UserType.PUBLIC_GM.getValue())) {
					chr.getClient().getSession().write(data);
				}
			}
		} finally {
			mutex.unlock();
		}
	}
	
	/**
	 * 현재 접속중인 모든 유저에게 메세지를 보낸다.
	 */
	public final void broadcastPacket(final Packet data) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToCharMap.values().iterator();
			while (itr.hasNext()) {
				itr.next().getClient().getSession().write(data);
			}
		} finally {
			mutex.unlock();
		}
	}

	

	public final void broadcastSmegaPacket(final Packet data) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToCharMap.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
				chr = itr.next();

				if (chr.getClient().isLoggedIn() && chr.getSmega()) {
					chr.getClient().getSession().write(data);
				}
			}
		} finally {
			mutex.unlock();
		}
	}

	public final void deregisterPendingPlayer(final int charid) {
		relatedToPendingMutex.lock();
		try {
			if (pendingCharacterMap.get(charid) != null)
				pendingCharacterMap.remove(charid);
		} finally {
			relatedToPendingMutex.unlock();
		}
	}

	public final void deregisterPlayer(final MapleCharacter chr) {
		mutex.lock();
		try {
			nameToCharMap.remove(chr.getName().toLowerCase());
			idToCharMap.remove(chr.getId());
		} finally {
			mutex.unlock();
		}
	}

	public final void disconnectAll() {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToCharMap.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
				chr = itr.next();
				chr.getClient().disconnect(false, false);
				chr.getClient().getSession().closeNow();
				itr.remove();
			}
		} finally {
			mutex.unlock();
		}
	}

	public final Map<String, MapleCharacter> getAllCharacters() {
		return nameToCharMap;
	}

	public final MapleCharacter getCharacterById(final int id) {
		return idToCharMap.get(id);
	}

	public final MapleCharacter getCharacterByName(final String name) {
		return nameToCharMap.get(name.toLowerCase());
	}

	public final int getConnectedClients() {
		return idToCharMap.size();
	}

	public final Object getEffect(final int id) {
		return effectsMap.get(id);
	}

	public final int getNotGmConnectedClients() {
		int people = 0;
		mutex.lock();
		try {
			for (MapleCharacter hp : nameToCharMap.values()) {
				if (!hp.isGM()) {
					people++;
				}
			}
		} finally {
			mutex.unlock();
		}
		return people;
	}

	public final String getOnlinePlayers(final boolean byGM) {
		final StringBuilder sb = new StringBuilder();
		if (byGM) {
			mutex.lock();
			try {
				final Iterator<MapleCharacter> itr = nameToCharMap.values().iterator();
				while (itr.hasNext()) {
					sb.append(", ");
				}
			} finally {
				mutex.unlock();
			}
		} else {
			mutex.lock();
			try {
				final Iterator<MapleCharacter> itr = nameToCharMap.values().iterator();
				MapleCharacter chr;
				while (itr.hasNext()) {
					chr = itr.next();

					if (!chr.isGM()) {
						sb.append(", ");
					}
				}
			} finally {
				mutex.unlock();
			}
		}
		return sb.toString();
	}

	public final ChracterTransfer getPendingCharacter(final int charid) {
		final ChracterTransfer toreturn = pendingCharacterMap.get(charid);// .right;
		if (toreturn != null) {
			deregisterPendingPlayer(charid);
		}
		return toreturn;
	}

	public final void registerPendingPlayer(final ChracterTransfer chr, final int playerid) {
		relatedToPendingMutex.lock();
		try {
			pendingCharacterMap.put(playerid, chr);
		} finally {
			relatedToPendingMutex.unlock();
		}
	}

	public final void registerPlayer(final MapleCharacter chr) {
		mutex.lock();
		try {
			nameToCharMap.put(chr.getName().toLowerCase(), chr);
			idToCharMap.put(chr.getId(), chr);
			if (effectsMap.get(chr.getId()) == null) {
				effectsMap.put(chr.getId(), new Object());
			}
				
		} finally {
			mutex.unlock();
		}
	}

	public final void saveAll() {
		mutex.lock();
		try {
			for (MapleCharacter hp : nameToCharMap.values()) {
				hp.saveToDB(false, false);
			}
		} finally {
			mutex.unlock();
		}
	}
}
