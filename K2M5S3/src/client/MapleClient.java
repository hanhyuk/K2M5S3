package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javax.script.ScriptEngine;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.AccountStatusType;
import a.my.made.CommonType;
import a.my.made.LogUtils;
import a.my.made.UserType;
import a.my.made.dao.AccountDAO;
import a.my.made.dao.ParamMap;
import a.my.made.dao.ResultMap;
import a.my.made.util.StringUtils;
import client.stats.BuffStatsValueHolder;
import community.MapleGuildCharacter;
import community.MapleMultiChatCharacter;
import community.MaplePartyCharacter;
import community.MaplePartyOperation;
import community.MapleUserTrade;
import constants.programs.ControlUnit;
import database.MYSQL;
import database.MYSQLException;
import launch.CashShopServer;
import launch.ChannelServer;
import launch.holder.WideObjectHolder;
import launch.world.WorldBroadcasting;
import launch.world.WorldCommunity;
import packet.creators.MainPacketCreator;
import packet.crypto.MapleCrypto;
import packet.transfer.write.Packet;
import scripting.NPCScriptManager;
import server.shops.IMapleCharacterShop;

public class MapleClient {
	private static final Logger logger = LoggerFactory.getLogger(MapleClient.class);

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
	private transient MapleCrypto send, receive;
	private transient IoSession session;
	private MapleCharacter player;
	private MapleCharacterStat playerstat;
	private int channel = 1;
	/**
	 * account 테이블의 id 값(AI PK)
	 */
	private int accountId = 1;
	private int world;
	/**
	 * 로그인 성공 여부 true : 성공 TODO 이값은 boolean 값이 아니라 AccountStatusType 값으로 수정되어야
	 * 함.
	 */
	private boolean loggedIn = false;
	private boolean serverTransition = false;
	/**
	 * 계정 ID
	 */
	private String accountName;
	private transient long lastPong;
	/**
	 * GM여부 true : 지엠
	 */
	private boolean gm;
	/**
	 * 성별
	 */
	private byte gender = -1;
	private int charslots = DEFAULT_CHARSLOT;
	public boolean pinged = false, isCS = false;
	private transient List<Integer> allowedChar = new LinkedList<Integer>();
	private transient Set<String> macs = new HashSet<String>();
	private transient Map<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();

	private transient ScheduledFuture<?> idleTask = null;

	/**
	 * 2차비번
	 */
	private transient String secondPassword;

	private transient String tempIP = "";// To be used only on

	// login
	/**
	 * 2차비번 사용여부 true:사용
	 */
	private boolean usingSecondPassword = false;

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

	public boolean canClickNPC() {
		return lastNpcClick + 500 < System.currentTimeMillis();
	}

	/**
	 * 현재 로그인을 할 수 있는 상태인지 체크 한다.
	 * 
	 * 로그인이 가능한 상태(CommonType.LOGIN_POSSIBLE)일 경우 일부 멤버 변수들의 값을 변경한다.
	 * 
	 * @param loginId
	 *            계정 아이디
	 * @param loginPassword
	 *            계정 비밀번호
	 * @return CommonType.LOGIN_FAILD, CommonType.ACCOUNT_BAN,
	 *         CommonType.LOGIN_ING, CommonType.LOGIN_SUCCESS
	 */

	public CommonType checkLoginAvailability(final String loginId, final String loginPassword) {
		CommonType result = CommonType.LOGIN_IMPOSSIBLE;

		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(loginId, loginPassword);

		for (ResultMap rm : accountInfo) {
			if (rm.getInt("banned") > 0) {
				// TODO "banned" 관련 파일 검색한 다음 쓸모 없는 것들 다 삭제하고,
				// banned 컬럼을 사용하지 않도록 전체 소스를 수정하자.
				// 앞으로는 밴 처리도 "loggedin" 컬럼의 값을 이용하려고 한다.
				result = CommonType.ACCOUNT_BAN;
			} else if (rm.getInt("loggedin") != AccountStatusType.NOT_LOGIN.getValue()) {
				result = CommonType.LOGIN_ING;
			} else {
				accountName = rm.getString("name");
				accountId = rm.getInt("id");
				gender = (byte) rm.getInt("gender");
				gm = rm.getInt("gm") >= UserType.PUBLIC_GM.getValue();

				secondPassword = rm.getString("2ndpassword");
				chrslot = rm.getInt("chrslot");
				usingSecondPassword = rm.getInt("using2ndpassword") == 1;

				result = CommonType.LOGIN_POSSIBLE;
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

	public void createdChar(final int id) {
		allowedChar.add(id);
	}

	public void deleteGuildCharacter(MapleGuildCharacter mgc) {
		WideObjectHolder.getInstance().setGuildMemberOnline(mgc, false, -1);
		if (mgc.getGuildRank() > 1) { // not leader
			WideObjectHolder.getInstance().leaveGuild(mgc);
		} else {
			WideObjectHolder.getInstance().disbandGuild(mgc.getGuildId());
		}
	}
	
	public final boolean deleteCharacter(final int cid) {
		try {
			final Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name, alliancerank FROM characters WHERE id = ? AND accountid = ?");
			ps.setInt(1, cid);
			ps.setInt(2, accountId);
			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				rs.close();
				ps.close();
				return false;
			}
			if (rs.getInt("guildid") > 0) { // is in a guild when deleted
				final MapleGuildCharacter mgc = new MapleGuildCharacter(cid, (short) 0, rs.getString("name"), (byte) -1, 0, rs.getInt("guildrank"), rs.getInt("guildid"), false, rs.getInt("alliancerank"));
				deleteGuildCharacter(mgc);
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
			logger.debug("DeleteChar error {}", e);
		}
		return false;
	}

	/**
	 * 서버에 접속중인 연결(세션)을 종료 시킨다.
	 * 
	 * 1. 캐시샵서버 2. 채널서버 3. 포털이 없을때 4. 세션이 종료 될때
	 * 
	 */
	public final void disconnect(final boolean removeInChannelServer, final boolean fromCS) {
		if (player != null && isLoggedIn()) {
			removalTask();
			player.saveToDB(true, fromCS);
			ControlUnit.동접제거(player.getName());
			ControlUnit.접속자수.setText(String.valueOf((int) (Integer.parseInt(ControlUnit.접속자수.getText()) - 1)));
			if (!fromCS) {
				final ChannelServer channelServer = ChannelServer.getInstance(channel);
				try {
					if (player.getMessenger() != null) {
						WorldCommunity.leaveMessenger(player.getMessenger().getId(), new MapleMultiChatCharacter(player));
						player.setMessenger(null);
					}
					if (player.getParty() != null) {
						final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
						chrp.setOnline(false);
						if (player.getParty().getExpedition() != null) {
							player.getParty().getExpedition().broadcastMessage(MainPacketCreator.updateExpedition(true, player.getParty().getExpedition()));
						}
						WorldCommunity.updateParty(player.getParty().getId(), MaplePartyOperation.LOG_ONOFF, chrp);
					}
					if (!serverTransition && isLoggedIn()) {
						WorldBroadcasting.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
					} else { // Change channel
						WorldBroadcasting.loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
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
					logger.debug("{} / {} ", LogUtils.getLogMessage(this, "ERROR"), e);
				} finally {
					if (removeInChannelServer && channelServer != null) {
						channelServer.removePlayer(player);
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
						WorldBroadcasting.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
					} else { // Change channel
						WorldBroadcasting.loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
					}
					if (player.getGuildId() > 0) {
						ChannelServer.setGuildMemberOnline(player.getMGC(), false, -1);
					}

				} catch (final Exception e) {
					player.setMessenger(null);
					logger.debug("{} / {}", LogUtils.getLogMessage(this, "ERROR"), e);
				} finally {
					if (removeInChannelServer && cs != null) {
						cs.getPlayerStorage().deregisterPlayer(player);
					}
					player = null;
				}
			}
		}

		// TODO 연결이 종료되는 시점에 상태값을 비교해서 비로그인 상태로 설정하는건 필요 없어 보인다.
		// 좀더 살펴보자.
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
			PreparedStatement ps = con.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?");
			ps.setInt(1, charslots);
			ps.setInt(2, world);
			ps.setInt(3, accountId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqlE) {
			sqlE.printStackTrace();
			return false;
		}
		return true;
	}

	public int getAccID() {
		return this.accountId;
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
			ps.setInt(1, accountId);
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?");
			ps.setInt(1, accountId);
			ps.setInt(2, world);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				charslots = rs.getInt("charslots");
			} else {
				PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)");
				psu.setInt(1, accountId);
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
		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(this.accountId);

		for (ResultMap rm : accountInfo) {
			chrslot = rm.getInt("chrslot");
		}

		return chrslot;
	}

	public final byte getGender() {
		return gender == -1 ? 0 : gender;
	}

	public final ScheduledFuture<?> getIdleTask() {
		return idleTask;
	}

	public final int getLastConnection() {
		int result = 1999123100;

		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(getAccID());
		for (ResultMap rm : accountInfo) {
			result = Integer.parseInt(rm.getString("lastconnect"));
		}

		return result;
	}

	public final long getLastPong() {
		return lastPong;
	}

	/**
	 * 현재 계정의 loggedin 상태값을 조회한다.
	 * 
	 * @see AccountStatusType
	 * 
	 */
	public final int getLoginState() {
		int state = -1;

		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(getAccID());
		for (ResultMap rm : accountInfo) {
			state = rm.getInt("loggedin");
		}

		return state;
	}

	public final Set<String> getMacs() {
		return Collections.unmodifiableSet(macs);
	}

	/**
	 * 사용자의 계정 비밀번호를 반환한다.
	 * 
	 * @param loginId
	 * @return
	 */
	public String getPassword(String loginId) {
		String result = null;

		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(loginId);
		for (ResultMap rm : accountInfo) {
			result = rm.getString("password");
		}

		return result;
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
			logger.debug("Error checking mac bans {}", ex);
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

	public void setAuthData() {
		final List<ResultMap> accountInfo = AccountDAO.getAccountInfo(getAccID());
		for (ResultMap rm : accountInfo) {
			secondPassword = rm.getString("2ndpassword");
			usingSecondPassword = rm.getInt("using2ndpassword") == 1;
		}
	}

	public List<String> loadCharacterNames() {
		List<String> chars = new LinkedList<String>();
		for (CharNameAndId cni : loadCharactersInternal()) {
			chars.add(cni.name);
		}
		return chars;
	}

	public final List<MapleCharacter> loadCharacters() {
		final List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
		for (final CharNameAndId cni : loadCharactersInternal()) {
			final MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
			chars.add(chr);
			allowedChar.add(chr.getId());
		}
		return chars;
	}

	private List<CharNameAndId> loadCharactersInternal() {
		List<CharNameAndId> chars = new LinkedList<CharNameAndId>();
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ?");
			ps.setInt(1, accountId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			logger.debug("error loading characters internal {}", e);
		}
		return chars;
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
		this.accountId = id;
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
			logger.debug("{}", e);
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
			ps.setInt(1, accountId);
			ps.executeUpdate();
			ps.close();
			for (Entry<Integer, Integer> cardlist : card.entrySet()) {
				PreparedStatement psu = con.prepareStatement("INSERT INTO charactercard (accountid, cardid, position) VALUES (?, ?, ?)");
				psu.setInt(1, accountId);
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
		final ParamMap params = new ParamMap();
		params.put("chrslot", (getChrSlot() + 1));
		AccountDAO.setAccountInfo(id, params);
	}

	public void setClickedNPC() {
		lastNpcClick = System.currentTimeMillis();
	}

	public void setGender(int i) {
		final ParamMap params = new ParamMap();
		params.put("gender", i);
		AccountDAO.setAccountInfo(accountId, params);
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

	public final boolean ban(String reason) {
		boolean result = false;

		final ParamMap params = new ParamMap();
		params.put("banned", 1);
		params.put("banreason", reason);
		result = AccountDAO.setAccountInfo(this.accountId, params);

		return result;
	}

	/**
	 * 캐릭터명으로 계정을 찾아서 언밴 처리.
	 * 
	 * @param charname
	 * @return true(성공) false(실패)
	 */
	public boolean unBan(final String charname) {
		boolean result = false;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = MYSQL.getConnection();
			ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
			ps.setString(1, charname);
			rs = ps.executeQuery();

			int accountId = 0;
			if (rs.next()) {
				accountId = rs.getInt("accountid");
			}
			ps.close();
			ps = null;
			rs.close();
			rs = null;

			if (accountId != 0) {
				final ParamMap params = new ParamMap();
				params.put("banned", 0);
				params.put("banreason", "");
				result = AccountDAO.setAccountInfo(accountId, params);
			}
		} catch (Exception e) {
			logger.debug("{}", e);
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
				logger.debug("{}", e);
			}
		}
		return result;
	}

	public void updateCharCard(Map<Integer, Integer> cid) {
		try {
			PreparedStatement ps = MYSQL.getConnection().prepareStatement("SELECT * FROM 'char_card'");
			ResultSet rs = ps.executeQuery();
		} catch (Exception e) {
			try {
				// 테이블 없음
				PreparedStatement ps = MYSQL.getConnection().prepareStatement("create database char_card(accid int not null, worldid int not null default 0, charid int not null default 0, position int no null)");
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
	 * 사용자의 계정 접속 상태값을 변경한다.
	 * 
	 * @param state
	 *            변경할 상태값
	 * @param sessionIp
	 * @return true(성공)
	 */
	public final boolean updateLoginState(final int state, final String sessionIp) {
		boolean result = false;

		final ParamMap params = new ParamMap();
		params.put("loggedin", state);
		params.put("SessionIP", StringUtils.isEmpty(sessionIp) ? "" : sessionIp);
		params.put("lastlogin", new Timestamp(System.currentTimeMillis()));
		result = AccountDAO.setAccountInfo(accountId, params);

		// FIXME 아래 로직이 필요한 건지 검토필요.
		// if (state == AccountStatusType.NOT_LOGIN.getValue()) {
		// loggedIn = false;
		// serverTransition = false;
		// } else {
		// serverTransition = (state ==
		// AccountStatusType.SERVER_TRANSITION.getValue() || state ==
		// AccountStatusType.CHANGE_CHANNEL.getValue());
		// loggedIn = !serverTransition;
		// }

		return result;
	}

	public final boolean updateSecondPassword() {
		boolean result = false;

		final ParamMap params = new ParamMap();
		params.put("2ndpassword", secondPassword);
		params.put("using2ndpassword", (usingSecondPassword ? 1 : 0));
		params.put("lastlogin", new Timestamp(System.currentTimeMillis()));
		result = AccountDAO.setAccountInfo(accountId, params);

		return result;
	}
}