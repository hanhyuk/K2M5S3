package handler.channel;

import static community.BuddyList.BuddyOperation.ADDED;
import static community.BuddyList.BuddyOperation.DELETED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import client.MapleClient;
import client.MaplePlayerIdNamePair;
import community.BuddyList;
import community.BuddyList.BuddyAddResult;
import community.BuddyList.BuddyOperation;
import community.BuddylistEntry;
import database.MYSQL;
import launch.ChannelServer;
import launch.world.WorldCommunity;
import launch.world.WorldConnected;
import packet.creators.MainPacketCreator;
import packet.transfer.read.ReadingMaple;

public class BuddyListHandler {
	private static final Logger logger = LoggerFactory.getLogger(BuddyListHandler.class);
	
	private static final class CharacterIdNameBuddyCapacity extends MaplePlayerIdNamePair {

		private int buddyCapacity;

		public CharacterIdNameBuddyCapacity(int id, String name, int level, int job, int buddyCapacity) {
			super(id, name, level, job);
			this.buddyCapacity = buddyCapacity;
		}

		public int getBuddyCapacity() {
			return buddyCapacity;
		}
	}

	private static final void nextPendingRequest(final MapleClient c) {
		MaplePlayerIdNamePair pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
		if (pendingBuddyRequest != null) {
			c.getSession()
					.write(MainPacketCreator.requestBuddylistAdd(c, pendingBuddyRequest.getId(), pendingBuddyRequest.getName(), pendingBuddyRequest.getLevel(), pendingBuddyRequest.getJob(), false));
		}
	}

	private static final CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(final String name) throws SQLException {
		Connection con = MYSQL.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name LIKE ?");
		ps.setString(1, name);
		ResultSet rs = ps.executeQuery();
		CharacterIdNameBuddyCapacity ret = null;
		if (rs.next()) {
			ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), rs.getInt("level"), rs.getInt("job"), rs.getInt("buddyCapacity"));
		}
		rs.close();
		ps.close();
		return ret;
	}

	public static final void BuddyUpdate(final ReadingMaple rh, final MapleClient c) {
		c.getSession().write(MainPacketCreator.buddylistUpdate(0));
	}

	public static final void BuddyOperation(final ReadingMaple rh, final MapleClient c) {
		final int mode = rh.readByte();
		final BuddyList buddylist = c.getPlayer().getBuddylist();
		if (mode == 1) { // 친구 추가.
			final String addName = rh.readMapleAsciiString();
			final String groupName = rh.readMapleAsciiString();
			final BuddylistEntry ble = buddylist.get(addName);
			if (addName.length() > 13 || groupName.length() > 16) {
				return;
			}
			if (ble != null && !ble.isVisible()) { // 이미 친구로 등록되어 있습니다.
				c.getSession().write(MainPacketCreator.serverNotice(1, "이미 친구로 등록되어 있습니다."));
				return;
			} else if (buddylist.isFull()) { // 친구리스트가 꽉 찼습니다.
				c.getSession().write(MainPacketCreator.buddylistMessage((byte) 0x0B, null));
				return;
			} else {
				try {
					CharacterIdNameBuddyCapacity charWithId = null;
					int channel;
					final MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterByName(addName);
					if (otherChar != null) {
						channel = c.getChannel();
						if (!otherChar.isGM() || c.getPlayer().isGM()) {
							charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), otherChar.getLevel(), otherChar.getJob(), otherChar.getBuddylist().getCapacity());
						}
					} else {
						channel = WorldConnected.find(addName);
						charWithId = getCharacterIdAndNameFromDatabase(addName);
					}
					if (charWithId != null) {
						BuddyAddResult buddyAddResult = null;
						if (channel != -1) {
							buddyAddResult = WorldCommunity.requestBuddyAdd(addName, c.getChannel(), c.getPlayer().getId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob(),
									groupName);
						} else {
							Connection con = MYSQL.getConnection();
							PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
							ps.setInt(1, charWithId.getId());
							ResultSet rs = ps.executeQuery();
							if (!rs.next()) {
								ps.close();
								rs.close();
								throw new RuntimeException("Result set expected");
							} else {
								int count = rs.getInt("buddyCount");
								if (count >= charWithId.getBuddyCapacity()) {
									buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
								}
							}
							rs.close();
							ps.close();

							ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
							ps.setInt(1, charWithId.getId());
							ps.setInt(2, c.getPlayer().getId());
							rs = ps.executeQuery();
							if (rs.next()) {
								buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
							}
							rs.close();
							ps.close();
						}
						if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) { // 상대
																				// 친구추가창이
																				// 꽉
																				// 찼습니다.
							c.getSession().write(MainPacketCreator.serverNotice(1, "상대 친구목록이 꽉 찼습니다."));
							return;
						} else {
							int displayChannel = -1;
							if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1) {
								c.send(MainPacketCreator.serverNotice(1, "이미 대상의 친구목록에 캐릭터가 있습니다."));
								return;
							} else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
								Connection con = MYSQL.getConnection();
								PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (`characterid`, `buddyid`, `groupname`, `pending`) VALUES (?, ?, ?, 1)");
								ps.setInt(1, charWithId.getId());
								ps.setInt(2, c.getPlayer().getId());
								ps.setString(3, groupName);
								ps.executeUpdate();
								ps.close();
							}
							buddylist.put(new BuddylistEntry(charWithId.getName(), charWithId.getId(), groupName, displayChannel, true, charWithId.getLevel(), charWithId.getJob()));
							c.getSession().write(MainPacketCreator.updateBuddylist(buddylist.getBuddies(), 10, 0));
							c.getSession().write(MainPacketCreator.buddylistMessage((byte) 0x30, null));
							c.getSession().write(MainPacketCreator.buddylistMessage((byte) 0x1C, charWithId.getName()));
						}
					} else { // 캐릭터를 발견하지 못했습니다.
						c.getSession().write(MainPacketCreator.buddylistMessage((byte) 0x0F, null));
					}
				} catch (SQLException e) {
					logger.debug("SQL THROW {}", e);
				}
			}
		} else if (mode == 2 || mode == 3) { // 친구 수락
			int otherCid = rh.readInt();
			if (!buddylist.isFull()) {
				try {
					final int channel = WorldConnected.find(otherCid);
					String otherName = null;
					int otherLevel = 0, otherJob = 0;
					MapleCharacter otherChar = null;
					for (ChannelServer cserv : ChannelServer.getAllInstances()) {
						otherChar = cserv.getPlayerStorage().getCharacterById(otherCid);
						if (otherChar != null) {
							break;
						}
					}
					if (otherChar == null) {
						Connection con = MYSQL.getConnection();
						PreparedStatement ps = con.prepareStatement("SELECT name, level, job FROM characters WHERE id = ?");
						ps.setInt(1, otherCid);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							otherName = rs.getString("name");
							otherLevel = rs.getInt("level");
							otherJob = rs.getInt("job");
						}
						rs.close();
						ps.close();
					} else {
						otherName = otherChar.getName();
					}
					if (otherName != null) {
						buddylist.put(new BuddylistEntry(otherName, otherCid, "그룹 미지정", channel, true, otherLevel, otherJob));
						c.getSession().write(MainPacketCreator.requestBuddylistAdd(c, otherCid, otherName, otherChar.getLevel(), otherChar.getJob(), true));
						c.getSession().write(MainPacketCreator.updateBuddylist(buddylist.getBuddies(), 10, 0));
						notifyRemoteChannel(c, channel, otherCid, ADDED);
					}
				} catch (SQLException e) {
					logger.debug("SQL THROW {}", e);
				}
			} else {
				c.getSession().write(MainPacketCreator.buddylistMessage((byte) 0x0B, null));
			}
		} else if (mode == 5) { // 친구 삭제
			final int otherCid = rh.readInt();
			if (buddylist.containsVisible(otherCid)) {
				notifyRemoteChannel(c, WorldConnected.find(otherCid), otherCid, DELETED);
			}
			buddylist.remove(otherCid);
			c.getSession().write(MainPacketCreator.updateBuddylist(c.getPlayer().getBuddylist().getBuddies(), 18, otherCid));
		} else if (mode == 10) { // 친구 늘리기
			if (c.getPlayer().getMeso() >= 50000) {
				c.getPlayer().setBuddyCapacity(c.getPlayer().getBuddyCapacity() + 5);
				c.getPlayer().gainMeso(-50000, false);
				c.getSession().write(MainPacketCreator.updateBuddyCapacity(c.getPlayer().getBuddyCapacity()));
			}
		}
		nextPendingRequest(c);
	}

	private static void notifyRemoteChannel(final MapleClient c, final int remoteChannel, final int otherCid, final BuddyOperation operation) {
		final MapleCharacter player = c.getPlayer();
		if (remoteChannel != -1) {
			ChannelServer.getInstance(remoteChannel).buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation, player.getLevel(), player.getJob());
		}
	}
}