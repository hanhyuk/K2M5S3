package launch.world;

import launch.ChannelServer;
import packet.transfer.write.Packet;

/**
 * 모든 채널에 패킷을 방송하는 기능 제공. since 2012. 2. 24
 * 
 * @since Revision 25
 */
public class WorldBroadcasting {

	/**
	 * 모든 채널에 바이트 형식의 패킷을 전송함.
	 * 
	 * @param <byte[]>
	 *            바이트배열
	 * 
	 * @since Revision 25
	 */
	public static void broadcastMessage(byte[] data) {
		broadcast(data);
	}

	/**
	 * 모든 채널에 메이플패킷 형식의 패킷을 전송함.
	 * 
	 * @param <Packet>
	 *            메이플패킷
	 * 
	 * @since Revision 25
	 */
	public static void broadcastMessage(Packet data) {
		broadcast(data);
	}

	/**
	 * 모든 채널에 메이플패킷 형식의 패킷을 전송함.
	 * 
	 * @param <Packet>
	 *            메이플패킷
	 * 
	 * @since Revision 25
	 */
	public static void broadcast(Packet data) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.broadcastPacket(data);
		}
	}

	/**
	 * 모든 채널에 바이트 형식의 패킷을 전송함.
	 * 
	 * @param <byte[]>
	 *            바이트배열
	 * 
	 * @since Revision 25
	 */
	public static void broadcast(byte[] data) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.broadcastPacket(new Packet(data));
		}
	}

	/**
	 * 모든 채널에 확성기를 수신하는 유저만 볼 수 있는 패킷을 전송함.
	 * 
	 * @param <byte[]>
	 *            바이트배열
	 * 
	 * @since Revision 25
	 */
	public static void broadcastSmega(byte[] data) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.broadcastSmegaPacket(new Packet(data));
		}
	}

	/**
	 * 모든 채널에 GM만 받을 수 있는 패킷을 전송함.
	 * 
	 * @param <byte[]>
	 *            바이트배열
	 * 
	 * @since Revision 25
	 */
	public static void broadcastGM(byte[] data) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.broadcastGMPacket(new Packet(data));
		}
	}

	/**
	 * 모든 채널에 친구 로그오프 패킷을 전송함.
	 * 
	 * @param <String>
	 *            업데이트캐릭명
	 * @param <int>
	 *            업데이트캐릭ID
	 * @param <int>
	 *            채널
	 * @param <int[]>
	 *            패킷전송대상친구ID목록
	 * 
	 * @since Revision 25
	 */
	public static void loggedOff(String name, int characterId, int channel, int[] buddies) {
		for (ChannelServer cserv : ChannelServer.getAllInstances())
			cserv.updateBuddies(characterId, channel, buddies, true);
	}

	/**
	 * 모든 채널에 친구 로그온 패킷을 전송함.
	 * 
	 * @param <String>
	 *            업데이트캐릭명
	 * @param <int>
	 *            업데이트캐릭ID
	 * @param <int>
	 *            채널
	 * @param <int[]>
	 *            패킷전송대상친구ID목록
	 * 
	 * @since Revision 25
	 */
	public static void loggedOn(String name, int characterId, int channel, int buddies[]) {
		for (ChannelServer cserv : ChannelServer.getAllInstances())
			cserv.updateBuddies(characterId, channel, buddies, false);
	}
}
