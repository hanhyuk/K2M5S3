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
	 * ����ڰ� ä���� �����ϰų� ĳ�ü� ����/���� �Ҷ�...
	 * ���������� ���� ������� ����(MapleCharacter ��ü)�� ����ȭ(ChracterTransfer Ŭ����) �Ѵ�.
	 * ����ȭ �� ������ Map �� add, get, remove ó�� �ȴ�.
	 *  
	 * �� PersistingTask �� ����� ����ȭ ������ �ֱ������� Ȯ���Ͽ� �����ð��� ���� ���� ��� �����Ѵ�.
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
	
	private final int REPEAT_TIME = 1000 * 60 * 15; //15��
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
	 * ���� �������� GM �����鿡�� �޼����� ������.
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
	 * ���� �������� ��� �������� �޼����� ������.
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
