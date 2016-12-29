package launch.world;

import constants.ServerConstants;
import client.MapleCharacter;
import launch.CashShopServer;
import launch.ChannelServer;
import launch.holder.MapleWhereAreYou;
import java.util.HashMap;
import java.util.Map;

/**
 * 모든 채널을 스캔하여 유저가 접속해 있는지 등의 접속관련 정보 제공.
 *
 * since 2012. 2. 24
 * 
 * @since Revision 25
 */

public class WorldConnected {

	/**
	 * 모든 채널을 스캔하여 해당 계정이 고용상점을 갖고 있는지 체크.
	 * 
	 * @param <int>
	 *            계정번호
	 * 
	 * @return 고용상점소유여부
	 * 
	 * @since Revision 25
	 * 
	 */
	public static boolean hasMerchant(int accountId) {
		boolean ret = false;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			ret = cserv.constainsMerchant(accountId);
		}
		return ret;

	}

	/**
	 * 각 채널이 몇명의 유저를 수용중인지 정보 제공
	 * 
	 * @return 각채널이 가진 유저수
	 * 
	 * @since Revision 25
	 * 
	 */

	public static Map<Integer, Integer> getConnected(int serverId) {
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
		int total = 0;
		for (int i = 0; i < ServerConstants.serverCount; i++) {
			int curConnected = ChannelServer.getInstance(i).getPlayerStorage().getConnectedClients();
			ret.put(i, curConnected);
			total += curConnected;
		}
		ret.put(0, total);
		return ret;
	}

	/**
	 * 모든 채널을 스캔하여 해당 유저가 어느 채널에 접속해 있는지 확인.
	 * 
	 * @param <String>
	 *            유저명
	 * 
	 * @return 접속중 채널 (미접속시 -1 반환)
	 * 
	 * @since Revision 25
	 * 
	 */
	public static int find(String charName) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			if (cserv.getPlayerStorage().getCharacterByName(charName) != null) {
				return cserv.getChannel();
			}
		}
		return -1;
	}

	/**
	 * 모든 채널을 스캔하여 해당 유저가 어느 채널에 접속해 있는지 확인.
	 * 
	 * @param <int>
	 *            유저넘버
	 * 
	 * @return 접속중 채널 (미접속시 -1 반환)
	 * 
	 * @since Revision 25
	 * 
	 */
	public static int find(int characterId) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			if (cserv.getPlayerStorage().getCharacterById(characterId) != null) {
				return cserv.getChannel();
			}
		}
		return -1;
	}

	/**
	 * 캐시샵 채널을 스캔하여 해당 유저가 캐시샵에 접속해 있는지 확인.
	 * 
	 * @param <String>
	 *            유저명
	 * 
	 * @return 캐시샵접속여부
	 * 
	 * @since Revision 25
	 * 
	 */
	public static boolean isCharacterInCS(String name) {
		return CashShopServer.getInstance().getPlayerStorage().isCharacterConnected(name);
	}

	/**
	 * 모든 채널을 스캔하여 해당 유저가 어느 채널에든 접속해 있는지 확인.
	 * 
	 * @param <String>
	 *            유저명
	 * 
	 * @return 접속여부
	 * 
	 * @since Revision 25
	 * 
	 */
	public static boolean isConnected(String charName) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			if (cserv.getPlayerStorage().getCharacterByName(charName) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 모든 채널을 스캔하여 해당 유저가 어느 채널에든 접속해 있는지 확인.
	 * 
	 * @param <int>
	 *            유저넘버
	 * 
	 * @return 접속여부
	 * 
	 * @since Revision 25
	 * 
	 */
	public static boolean isConnected(int id) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			if (cserv.getPlayerStorage().getCharacterById(id) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 모든 채널을 스캔하여 해당 유저의 위치정보를 제공.
	 * 
	 * @param <String>
	 *            유저명
	 * 
	 * @return 해당유저의 위치정보
	 * 
	 * @since Revision 25
	 * 
	 */
	public static MapleWhereAreYou getLocation(String charName) {
		int channel = WorldConnected.find(charName);
		if (channel != -1) {
			MapleCharacter chr = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(charName);
			if (chr != null) {
				return new MapleWhereAreYou(chr.getMapId(), (byte) channel);
			}
		}
		return null;
	}

	public static Map<Integer, Integer> getConnected() {
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
		int total = 0;
		for (int i = 0; i < ServerConstants.serverCount; i++) {
			int curConnected = ChannelServer.getInstance(i).getPlayerStorage().getConnectedClients();
			ret.put(i, curConnected);
			total += curConnected;
		}
		ret.put(0, total);
		return ret;
	}
}
