package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptEngine;

import org.apache.mina.core.session.IoSession;

import a.my.made.AccountStatusType;
import a.my.made.CommonType;
import a.my.made.LogUtils;
import a.my.made.UserType;
import client.stats.BuffStatsValueHolder;
import community.MapleGuildCharacter;
import community.MapleMultiChatCharacter;
import community.MaplePartyCharacter;
import community.MaplePartyOperation;
import community.MapleUserTrade;
import constants.ServerConstants;
import constants.programs.ControlUnit;
import database.MYSQL;
import database.MYSQLException;
import launch.CashShopServer;
import launch.ChannelServer;
import launch.LoginServer;
import launch.world.WorldBroadcasting;
import launch.world.WorldCommunity;
import packet.creators.MainPacketCreator;
import packet.crypto.MapleCrypto;
import packet.transfer.write.Packet;
import scripting.NPCScriptManager;
import server.shops.IMapleCharacterShop;

public class MapleClient {

	protected static final class CharNameAndId {

		public final String name;
		public final int id;

		public CharNameAndId(final String name, final int id) {
			super();
			this.name = name;
			this.id = id;
		}
	}
	public static final int DEFAULT_CHARSLOT = 6;
	public static final String CLIENT_KEY = "CLIENT";
	public static final int findAccIdForCharacterName(final String charName) {
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
			ps.setString(1, charName);
			ResultSet rs = ps.executeQuery();

			int ret = -1;
			if (rs.next()) {
				ret = rs.getInt("accountid");
			}
			rs.close();
			ps.close();

			return ret;
		} catch (final SQLException e) {
			System.err.println("findAccIdForCharacterName SQL error");
		}
		return -1;
	}
	public static int isValidAccount(String name) {
		Connection con = MYSQL.getConnection();
		try {
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT day FROM accounts WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				return 0;
			}
			int state = rs.getInt("day");
			rs.close();
			ps.close();
			return state;
		} catch (SQLException e) {
			throw new MYSQLException("���� ����. ��� ���� �ϼ��� �ҷ��� �� �����ϴ�.", e);
		}
	}
	private transient MapleCrypto send, receive;
	private transient IoSession session;
	private MapleCharacter player;
	private MapleCharacterStat playerstat;
	private int channel = 1;
	/**
	 * account ���̺��� id ��(AI PK)
	 */
	private int accId = 1;
	private int world;
	/**
	 * �α��� ���� ���� true : ����
	 * TODO �̰��� boolean ���� �ƴ϶� AccountStatusType ������ �����Ǿ�� ��.
	 */
	private boolean loggedIn = false;
	private boolean serverTransition = false;
	/**
	 * ���� ID
	 */
	private String accountName;
	private transient long lastPong;
	/**
	 * GM���� true : ����
	 */
	private boolean gm;
	/**
	 * ����
	 */
	private byte gender = -1;
	private int charslots = DEFAULT_CHARSLOT;
											/**
	 * �α��� �õ��� ���ʿ��ϰ� ���� �� ��츦 üũ�ϱ� ���� ī���� ����
	 */
	public transient short loginTryCount = 0;
	public boolean pinged = false, isCS = false;
	private transient List<Integer> allowedChar = new LinkedList<Integer>();
	private transient Set<String> macs = new HashSet<String>();
	private transient Map<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();

	private transient ScheduledFuture<?> idleTask = null;

	/**
	 * 2�����
	 */
	private transient String secondPassword;

	private transient String tempIP = "";// To be used only on

	// login
	/**
	 * 2����� ��뿩�� true:���
	 */
	private boolean usingSecondPassword = false;

	/**
	 * encoder ���� ���Ǵ� lock
	 */
	private final transient Lock encodeMutex = new ReentrantLock(true);

	/**
	 * decoder ���� ���Ǵ� lock
	 */
	private final transient Lock decodeMutex = new ReentrantLock(true);

	private long lastNpcClick = 0;

	private int chrslot;

	public MapleClient(MapleCrypto send, MapleCrypto receive, IoSession session) {
		this.send = send;
		this.receive = receive;
		this.session = session;
	}

	public void addChrSlot(int i, int id) {
		chrslot += i;
		setChrSlot(id);
	}

	/**
	 * �α��� �õ� Ƚ�� ���� 
	 */
	public void addLoginTryCount() {
		loginTryCount++;
	}

	public void banMacs() {
		Connection con = MYSQL.getConnection();
		try {
			loadMacsIfNescessary();
			List<String> filtered = new LinkedList<String>();
			PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				filtered.add(rs.getString("filter"));
			}
			rs.close();
			ps.close();

			ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
			for (String mac : macs) {
				boolean matched = false;
				for (String filter : filtered) {
					if (mac.matches(filter)) {
						matched = true;
						break;
					}
				}
				if (!matched) {
					ps.setString(1, mac);
					try {
						ps.executeUpdate();
					} catch (SQLException e) {
						// can fail because of UNIQUE key, we dont care
					}
				}
			}
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error banning MACs" + e);
		}
	}

	public boolean canClickNPC() {
		return lastNpcClick + 500 < System.currentTimeMillis();
	}

	public final boolean CheckIPAddress() {
		try {
			final PreparedStatement ps = MYSQL.getConnection().prepareStatement("SELECT SessionIP FROM accounts WHERE id = ?");
			ps.setInt(1, this.accId);
			final ResultSet rs = ps.executeQuery();

			boolean canlogin = false;

			if (rs.next()) {
				final String sessionIP = rs.getString("SessionIP");

				if (sessionIP != null) { // Probably a login proced skipper?
					canlogin = getSessionIPAddress().equals(sessionIP.split(":")[0]);
				}
			}
			rs.close();
			ps.close();

			return canlogin;
		} catch (final SQLException e) {
			System.out.println("Failed in checking IP address for client.");
		}
		return false;
	}

	/**
	 * ���� �α����� �� �� �ִ� �������� üũ �Ѵ�.
	 * 
	 * �α����� ������ ����(CommonType.LOGIN_POSSIBLE)�� ��� �Ϻ� ��� �������� ���� �����Ѵ�.
	 * 
	 * @param loginId ���� ���̵�
	 * @param loginPassword ���� ��й�ȣ
	 * @return CommonType.LOGIN_FAILD, CommonType.ACCOUNT_BAN, CommonType.LOGIN_ING, CommonType.LOGIN_SUCCESS
	 */

	public CommonType checkLoginAvailability(final String loginId, final String loginPassword) {
		CommonType result = CommonType.LOGIN_IMPOSSIBLE;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = MYSQL.getConnection().prepareStatement("SELECT * FROM accounts WHERE name = ? and password = ?");
			ps.setString(1, loginId);
			ps.setString(2, loginPassword);

			rs = ps.executeQuery();
			if (rs.next()) {
				//TODO "banned" ���� ���� �˻��� ���� ���� ���� �͵� �� �����ϰ�,
				//banned �÷��� ������� �ʵ��� ��ü �ҽ��� ��������.
				//�����δ� �� ó���� "loggedin" �÷��� ���� �̿��Ϸ��� �Ѵ�.
				if (rs.getInt("banned") > 0) {
					result = CommonType.ACCOUNT_BAN;
				} else if( rs.getByte("loggedin") != AccountStatusType.NOT_LOGIN.getValue() ) {
					result = CommonType.LOGIN_ING;
				} else {
					accountName = rs.getString("name");
					accId = rs.getInt("id");
					gender = rs.getByte("gender");
					gm = rs.getInt("gm") >= UserType.PUBLIC_GM.getValue();
					
					
					secondPassword = rs.getString("2ndpassword");
					chrslot = rs.getInt("chrslot");
					usingSecondPassword = rs.getByte("using2ndpassword") == 1;
					
					result = CommonType.LOGIN_POSSIBLE;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean CheckSecondPassword(String in) {
		boolean allow = false;

		if (in.equals(secondPassword)) {
			allow = true;
		}
		return allow;
	}

	/**
	 * �α��� �õ� Ƚ�� �ʱ�ȭ
	 */
	public void clearLoginTryCount() {
		loginTryCount = 0;
	}

	public void createdChar(final int id) {
		allowedChar.add(id);
	}

	public final boolean deleteCharacter(final int cid) {
		try {
			final Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement(
					"SELECT id, guildid, guildrank, name, alliancerank FROM characters WHERE id = ? AND accountid = ?");
			ps.setInt(1, cid);
			ps.setInt(2, accId);
			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				rs.close();
				ps.close();
				return false;
			}
			if (rs.getInt("guildid") > 0) { // is in a guild when deleted
				final MapleGuildCharacter mgc = new MapleGuildCharacter(cid, (short) 0, rs.getString("name"), (byte) -1,
						0, rs.getInt("guildrank"), rs.getInt("guildid"), false, rs.getInt("alliancerank"));
				LoginServer.getInstance().deleteGuildCharacter(mgc);
			}
			rs.close();
			ps.close();

			ps = con.prepareStatement("DELETE FROM characters WHERE id = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM skills WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM hiredmerch WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM mountdata WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM keyvalue WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM keyvalue2 WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM inventoryitems WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `inner_ability_skills` WHERE player_id = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `inventoryslot` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `keymap` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `questinfo` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `queststatus` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `quickslot` WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `skillmacros` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `skills_cooldowns` WHERE charid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `steelskills` WHERE cid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement("DELETE FROM `trocklocations` WHERE characterid = ?");
			ps.setInt(1, cid);
			ps.executeUpdate();
			ps.close();
			return true;
		} catch (final SQLException e) {
			System.err.println("DeleteChar error" + e);
		}
		return false;
	}

	public final void disconnect(final boolean removeInChannelServer, final boolean fromCS) {
		if (player != null && isLoggedIn()) {
			removalTask();
			player.saveToDB(true, fromCS);
			ControlUnit.��������(player.getName());
			ControlUnit.�����ڼ�.setText(String.valueOf((int) (Integer.parseInt(ControlUnit.�����ڼ�.getText()) - 1)));
			if (!fromCS) {
				final ChannelServer ch = ChannelServer.getInstance(channel);
				try {
					if (player.getMessenger() != null) {
						WorldCommunity.leaveMessenger(player.getMessenger().getId(),
								new MapleMultiChatCharacter(player));
						player.setMessenger(null);
					}
					if (player.getParty() != null) {
						final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
						chrp.setOnline(false);
						if (player.getParty().getExpedition() != null) {
							player.getParty().getExpedition().broadcastMessage(
									MainPacketCreator.updateExpedition(true, player.getParty().getExpedition()));
						}
						WorldCommunity.updateParty(player.getParty().getId(), MaplePartyOperation.LOG_ONOFF, chrp);
					}
					if (!serverTransition && isLoggedIn()) {
						WorldBroadcasting.loggedOff(player.getName(), player.getId(), channel,
								player.getBuddylist().getBuddyIds());
					} else { // Change channel
						WorldBroadcasting.loggedOn(player.getName(), player.getId(), channel,
								player.getBuddylist().getBuddyIds());
					}
					if (player.getGuildId() > 0) {
						ChannelServer.setGuildMemberOnline(player.getMGC(), false, -1);
					}
					for (BuffStatsValueHolder mbsvh : player.getAllBuffs_()) {
						if (mbsvh.schedule != null) {
							mbsvh.schedule.cancel(false);
							mbsvh.schedule = null;
						}
					}
				} catch (final Exception e) {
					if (player != null) {
						player.setMessenger(null);
					}
					if (!ServerConstants.realese)
						e.printStackTrace();
					System.err.println(LogUtils.getLogMessage(this, "ERROR") + e);
				} finally {
					if (removeInChannelServer && ch != null) {
						ch.removePlayer(player);
					}
					player = null;
				}
			} else {
				final CashShopServer cs = CashShopServer.getInstance();
				try {
					if (player.getParty() != null) {
						final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
						chrp.setOnline(false);
						WorldCommunity.updateParty(player.getParty().getId(), MaplePartyOperation.LOG_ONOFF, chrp);
					}
					if (!serverTransition && isLoggedIn()) {
						WorldBroadcasting.loggedOff(player.getName(), player.getId(), channel,
								player.getBuddylist().getBuddyIds());
					} else { // Change channel
						WorldBroadcasting.loggedOn(player.getName(), player.getId(), channel,
								player.getBuddylist().getBuddyIds());
					}
					if (player.getGuildId() > 0) {
						ChannelServer.setGuildMemberOnline(player.getMGC(), false, -1);
					}

				} catch (final Exception e) {
					player.setMessenger(null);
					if (!ServerConstants.realese)
						e.printStackTrace();
					System.err.println(LogUtils.getLogMessage(this, "ERROR") + e);
				} finally {
					if (removeInChannelServer && cs != null) {
						cs.getPlayerStorage().deregisterPlayer(player);
					}
					player = null;
				}
			}
		}
		
		//TODO ������ ����Ǵ� ������ ���°��� ���ؼ� ��α��� ���·� �����ϴ°� �ʿ� ���� ���δ�.
		//���� ���캸��.
		if (!serverTransition && isLoggedIn()) {
			updateLoginState(AccountStatusType.NOT_LOGIN.getValue(), null);
		}
	}

	public boolean gainCharacterSlot() {
		if (getCharacterSlots() >= 15) {
			return false;
		}
		charslots++;

		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con
					.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?");
			ps.setInt(1, charslots);
			ps.setInt(2, world);
			ps.setInt(3, accId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqlE) {
			sqlE.printStackTrace();
			return false;
		}
		return true;
	}

	public int getAccID() {
		return this.accId;
	}

	public final String getAccountName() {
		return accountName;
	}

	public final int getChannel() {
		return channel;
	}

	public final ChannelServer getChannelServer() {
		return ChannelServer.getInstance(channel);
	}

	public Map<Integer, Integer> getCharacterCard() {
		Map<Integer, Integer> chrcard = new HashMap<Integer, Integer>();
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM charactercard WHERE accountid = ?");
			ps.setInt(1, accId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				chrcard.put(rs.getInt("position"), rs.getInt("cardid"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			throw new MYSQLException("error getChrcard", e);
		}
		return chrcard;
	}

	public int getCharacterSlots() {
		if (isGm()) {
			return 15;
		}
		if (charslots != DEFAULT_CHARSLOT) {
			return charslots; // save a sql
		}
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?");
			ps.setInt(1, accId);
			ps.setInt(2, world);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				charslots = rs.getInt("charslots");
			} else {
				PreparedStatement psu = con
						.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)");
				psu.setInt(1, accId);
				psu.setInt(2, world);
				psu.setInt(3, charslots);
				psu.executeUpdate();
				psu.close();
			}
			rs.close();
			ps.close();
		} catch (SQLException sqlE) {
			sqlE.printStackTrace();
		}

		return charslots;
	}

	public int getChrSlot() {
		Connection con = MYSQL.getConnection();
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
			ps.setInt(1, accId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				chrslot = rs.getInt("chrslot");
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			if (!ServerConstants.realese) {
				ex.printStackTrace();
			}
		}
		return chrslot;
	}

	public final Lock getDecodeLock() {
		return decodeMutex;
	}

	public final Lock getEncodeLock() {
		return encodeMutex;
	}

	public final byte getGender() {
		return gender == -1 ? 0 : gender;
	}

	public final ScheduledFuture<?> getIdleTask() {
		return idleTask;
	}

	public final int getLastConnection() {
		final Connection connect = MYSQL.getConnection();
		PreparedStatement query = null;
		ResultSet result = null;
		try {
			query = connect.prepareStatement("SELECT lastconnect FROM accounts WHERE id = ?");
			query.setInt(1, accId);
			result = query.executeQuery();
			if (result.next()) {
				return Integer.parseInt(result.getString("lastconnect"));
			}
		} catch (SQLException e) {
			if (!ServerConstants.realese)
				e.printStackTrace();
		} finally {
			try {
				if (query != null) {
					query.close();
				}
				if (result != null) {
					result.close();
				}
			} catch (SQLException e) {
			}
		}
		return 2012010101;
	}

	public final long getLastPong() {
		return lastPong;
	}

	/**
	 * ���� ������ loggedin ���°��� ��ȸ�Ѵ�.
	 * 
	 * ���� �� �޼ҵ尡 ���Ǵ� ���� ������ ����.
	 * 1. ĳ�ü� ���� �Ҷ�
	 * 2. ingame(ä�� ����) �Ҷ�
	 * 3. 1�� �α��� �Ҷ�
	 * 4. ���� ������(controlUnit Ŭ����)���� ��ó�� �Ҷ�
	 * 
	 */
	public final int getLoginState() {
		int state = -1;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = MYSQL.getConnection().prepareStatement("SELECT loggedin, lastlogin FROM accounts WHERE id = ?");
			ps.setInt(1, getAccID());
			rs = ps.executeQuery();
			
			if( rs.next() ) {
				//TODO AccountStatusType Ŭ������ ����ϵ��� ���� �� ��� �κ� �����ؾ� ��. 
				state = rs.getByte("loggedin");

				//TODO "����"������ ������� �����ϱ� ���� ��ġ �� ������ ���δ�. 
				//������ 20�� �Ŀ� ��α��� ���·� DB �� ������Ʈ �ϸ�, �ߺ� �α����� ���� �� ������ ���δ�.
				//�� ����� ���� �Ϸ��� Ŭ���̾�Ʈ���� ����
				//������Ʈ�� ġ��, Ŭ���̾�Ʈ�� ������Ѿ� �Ѵ�.(������ �����������)
//				if (state == AccountStatusType.SERVER_TRANSITION.getValue() || state == AccountStatusType.CHANGE_CHANNEL.getValue()) {
//					if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) {
//						state = AccountStatusType.NOT_LOGIN.getValue();
//						updateLoginState(state, null);
//					}
//				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if( ps != null ) {
					ps.close(); ps = null;
				}
				if( rs != null ) {
					rs.close(); rs = null;
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return state;
	}

	/**
	 * �α��� �õ� Ƚ�� ��ȯ
	 */
	public short getLoginTryCount() {
		return loginTryCount;
	}

	public final Set<String> getMacs() {
		return Collections.unmodifiableSet(macs);
	}

	public String getPassword(String login) {
		String password = null;
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
			ps.setString(1, login);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				password = rs.getString("password");
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return password;
	}

	public MapleCharacter getPlayer() {
		return player;
	}

	public MapleCharacterStat getPlayerStat() {
		return playerstat;
	}

	public final MapleCrypto getReceiveCrypto() {
		return receive;
	}

	public final String getSecondPassword() {
		return secondPassword;
	}

	public final MapleCrypto getSendCrypto() {
		return send;
	}

	public final IoSession getSession() {
		return session;
	}

	public final String getSessionIPAddress() {
		return session.getRemoteAddress().toString().split(":")[0];
	}

	public String getTempIP() {
		return tempIP;
	}

	public final int getWorld() {
		return world;
	}

	public boolean hasBannedMac() {
		if (macs.isEmpty()) {
			return false;
		}
		boolean ret = false;
		int i = 0;
		try {
			Connection con = MYSQL.getConnection();
			StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
			for (i = 0; i < macs.size(); i++) {
				sql.append("?");
				if (i != macs.size() - 1) {
					sql.append(", ");
				}
			}
			sql.append(")");
			PreparedStatement ps = con.prepareStatement(sql.toString());
			i = 0;
			for (String mac : macs) {
				i++;
				ps.setString(i, mac);
			}
			ResultSet rs = ps.executeQuery();
			rs.next();
			if (rs.getInt(1) > 0) {
				ret = true;
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			System.err.println("Error checking mac bans" + ex);
		}
		return ret;
	}

	public final boolean isGm() {
		return gm;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean isUsing2ndPassword() {
		return usingSecondPassword;
	}

	public void loadAuthData() {
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con
					.prepareStatement("SELECT 2ndpassword, using2ndpassword FROM accounts WHERE id = ?");
			ps.setInt(1, this.accId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				secondPassword = rs.getString("2ndpassword");
				usingSecondPassword = rs.getByte("using2ndpassword") == 1;
			}
		} catch (Exception e) {
			if (!ServerConstants.realese)
				e.printStackTrace();
		}
	}

	public List<String> loadCharacterNames() {
		List<String> chars = new LinkedList<String>();
		for (CharNameAndId cni : loadCharactersInternal()) {
			chars.add(cni.name);
		}
		return chars;
	}

	public final List<MapleCharacter> loadCharacters() { // TODO make this less
															// costly zZz
		final List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
		for (final CharNameAndId cni : loadCharactersInternal()) {
			final MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
			chars.add(chr);
			allowedChar.add(chr.getId());
		}
		return chars;
	}

	private List<CharNameAndId> loadCharactersInternal() {
		long t = System.currentTimeMillis();
		List<CharNameAndId> chars = new LinkedList<CharNameAndId>();
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ?");
			ps.setInt(1, accId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.err.println("error loading characters internal" + e);
		}
		if (ServerConstants.isLocal) {
			System.out.println("Load Characters Internal time : " + (System.currentTimeMillis() - t) + "ms");
		}
		return chars;
	}

	private void loadMacsIfNescessary() throws SQLException {
		if (macs.isEmpty()) {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?");
			ps.setInt(1, accId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String[] macData = rs.getString("macs").split(", ");
				for (String mac : macData) {
					if (!mac.equals("")) {
						macs.add(mac);
					}
				}
			} else {
				throw new RuntimeException("No valid account associated with this client.");
			}
			rs.close();
			ps.close();
		}
	}

	public final boolean login_Auth(final int id) {
		return allowedChar.contains(id);
	}

	public final void pongReceived() {
		lastPong = System.currentTimeMillis();
	}

	public final void removalTask() {
		try {
			if (!player.getAllBuffs().isEmpty()) {
				player.cancelAllBuffs_();
			}
			if (!player.getAllDiseases().isEmpty()) {
				player.cancelAllDebuffs();
			}
			if (player.getTrade() != null) {
				MapleUserTrade.cancelTrade(player.getTrade());
			}
			NPCScriptManager.getInstance().dispose(this);

			final IMapleCharacterShop shop = player.getPlayerShop();
			if (shop != null) {
				shop.removeVisitor(player);
				if (shop.isOwner(player)) {
					shop.setOpen(true);
				}
			}
			if (player.getMap() != null) {
				player.getMap().removePlayer(player);
			}
			if (player.getEventInstance() != null) {
				player.getEventInstance().playerDisconnected(player);
			}
		} catch (final Throwable e) {
		}
	}

	public void removeClickedNPC() {
		lastNpcClick = 0;
	}

	public final void removeScriptEngine(final String name) {
		engines.remove(name);
	}

	public void send(Packet p) {
		getSession().write(p);
	}

	public void setAccID(int id) {
		this.accId = id;
	}

	public final void setAccountName(final String accountName) {
		this.accountName = accountName;
	}

	public boolean setBurningCharacter(int accid, int charid) {
		Connection con = MYSQL.getConnection();
		try {
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? AND id = ?");
			ps.setInt(1, accid);
			ps.setInt(2, charid);
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return false;
			}
			ps = con.prepareStatement("UPDATE characters SET burning = ? WHERE accountid = ? AND id = ?");
			ps.setByte(1, (byte) 1);
			ps.setInt(2, accid);
			ps.setInt(3, charid);
			ps.executeUpdate();
			rs.close();
			ps.close();
		} catch (SQLException e) {
			if (!ServerConstants.realese) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public final void setChannel(final int channel) {
		this.channel = channel;
	}

	public void setCharacterCard(Map<Integer, Integer> card) {
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM charactercard WHERE accountid = ?");
			ps.setInt(1, accId);
			ps.executeUpdate();
			ps.close();
			for (Entry<Integer, Integer> cardlist : card.entrySet()) {
				PreparedStatement psu = con
						.prepareStatement("INSERT INTO charactercard (accountid, cardid, position) VALUES (?, ?, ?)");
				psu.setInt(1, accId);
				psu.setInt(2, cardlist.getValue());
				psu.setInt(3, cardlist.getKey());
				psu.executeUpdate();
				psu.close();
			}
		} catch (SQLException e) {
			throw new MYSQLException("error setChrcard", e);
		}
	}

	public final void setChrSlot(int id) {
		try {
			PreparedStatement ps = MYSQL.getConnection()
					.prepareStatement("UPDATE accounts SET chrslot = ? WHERE id = ?");
			ps.setInt(1, (getChrSlot() + 1));
			ps.setInt(2, id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void setClickedNPC() {
		lastNpcClick = System.currentTimeMillis();
	}

	public void setGender(byte i) {
		final Connection con = MYSQL.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?");
			ps.setInt(1, i);
			ps.setInt(2, accId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public final void setIdleTask(final ScheduledFuture<?> idleTask) {
		this.idleTask = idleTask;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setPlayer(MapleCharacter player) {
		this.player = player;
	}

	public final void setScriptEngine(final String name, final ScriptEngine e) {
		engines.put(name, e);
	}

	public final void setSecondPassword(final String secondPassword) {
		this.secondPassword = secondPassword;
		this.usingSecondPassword = secondPassword != null;
		this.updateSecondPassword();
	}

	public final void setWorld(final int world) {
		this.world = world;
	}

	private void unban() {
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con
					.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
			ps.setInt(1, accId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error while unbanning" + e);
		}
	}

	public byte unban(String charname) {
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
			ps.setString(1, charname);

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				return -1;
			}
			final int accid = rs.getInt(1);
			rs.close();
			ps.close();

			ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
			ps.setInt(1, accid);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error while unbanning" + e);
			return -2;
		}
		return 0;
	}
	
	public void updateCharCard(Map<Integer, Integer> cid) {
		try {
			PreparedStatement ps = MYSQL.getConnection().prepareStatement("SELECT * FROM 'char_card'");
			ResultSet rs = ps.executeQuery();
		} catch (Exception e) {
			try {
				// ���̺� ����
				PreparedStatement ps = MYSQL.getConnection().prepareStatement(
						"create database char_card(accid int not null, worldid int not null default 0, charid int not null default 0, position int no null)");
				ps.executeQuery();
			} catch (SQLException ex) {
				// SKIP
			}
		}

		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("");
		} catch (Exception e) {

		}
	}
	
	/**
	 * @deprecated ���������� �α��� �� ��¥�� ������Ʈ �Ѵ�.
	 * �� ����� accounts ���̺� lastlogin �÷����� ����� ��� �ǹ̰� ���� �۾��̴�.
	 *  
	 */
	public final void updateLastConnection(String time) {
		try {
			Connection con = MYSQL.getConnection();

			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET lastconnect = ? WHERE id = ?");
			ps.setString(1, time);
			ps.setInt(2, accId);
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			if (!ServerConstants.realese)
				e.printStackTrace();
		}
	}
	
	/**
	 * ������� ���� ���� ���°��� �����Ѵ�.
	 * 
	 * @param state ������ ���°� 
	 * @param sessionIp 
	 * @return true(����)
	 */
	public final boolean updateLoginState(final int state, final String sessionIp) {
		boolean result = false;
		PreparedStatement ps = null;
		try {
			ps = MYSQL.getConnection().prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = ? WHERE id = ?");
			ps.setInt(1, state);
			ps.setString(2, sessionIp);
			ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			ps.setInt(4, getAccID());
			if( ps.executeUpdate() > 0 ) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if( ps != null ) {
					ps.close(); ps = null;
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		//FIXME �Ʒ� ������ �ʿ��� ���� �����ʿ�.
		if (state == AccountStatusType.NOT_LOGIN.getValue()) {
			loggedIn = false;
			serverTransition = false;
		} else {
			serverTransition = (state == AccountStatusType.SERVER_TRANSITION.getValue() || state == AccountStatusType.CHANGE_CHANNEL.getValue());
			loggedIn = !serverTransition;
		}
		
		return result;
	}

	public void updateMacs(String macData) {
		macs.addAll(Arrays.asList(macData.split(", ")));
		StringBuilder newMacData = new StringBuilder();
		Iterator<String> iter = macs.iterator();
		while (iter.hasNext()) {
			newMacData.append(iter.next());
			if (iter.hasNext()) {
				newMacData.append(", ");
			}
		}
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
			ps.setString(1, newMacData.toString());
			ps.setInt(2, accId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error saving MACs" + e);
		}
	}
	
	public final void updateSecondPassword() {
		try {
			final Connection con = MYSQL.getConnection();

			PreparedStatement ps = con
					.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `using2ndpassword` = ? WHERE id = ?");
			ps.setString(1, secondPassword);
			ps.setByte(2, (byte) (usingSecondPassword ? 1 : 0));
			ps.setInt(3, accId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.err.println("error updating login state" + e);
		}
	}
}