package scripting;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import client.MapleClient;
import client.MaplePet;
import client.MapleProfession;
import client.MapleProfessionType;
import client.items.Equip;
import client.items.EquipWorthCalculator;
import client.items.IItem;
import client.items.Item;
import client.items.MapleInventory;
import client.items.MapleInventoryType;
import client.skills.ISkill;
import client.skills.InnerAbillity;
import client.skills.InnerSkillValueHolder;
import client.skills.SkillEntry;
import client.skills.SkillFactory;
import client.stats.DiseaseStats;
import client.stats.PlayerStat;
import community.MapleAlliance;
import community.MapleGuild;
import community.MapleGuildRanking;
import community.MapleParty;
import community.MaplePartyCharacter;
import community.MapleSquadLegacy;
import community.MapleUserTrade;
import constants.GameConstants;
import constants.ServerConstants;
import database.MYSQL;
import handler.channel.AuctionHandler;
import handler.channel.AuctionHandler.AuctionPacket;
import handler.channel.HiredMerchantHandler;
import handler.channel.InterServerHandler;
import handler.duey.DueyHandler;
import launch.ChannelServer;
import packet.creators.MainPacketCreator;
import packet.creators.UIPacket;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.items.InventoryManipulator;
import server.items.ItemInformation;
import server.life.MobSkillFactory;
import server.maps.AramiaFireWorks;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MaplePortal;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;
import server.maps.PotSystem;
import server.quest.MapleQuest;
import server.shops.MapleShopFactory;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.Timer.EtcTimer;

public class NPCConversationManager extends AbstractPlayerInteraction {
	private static final Logger logger = LoggerFactory.getLogger(NPCConversationManager.class);

	private MapleClient c;
	private int npc, questid, MAX_REBORNS = 3;
	private String getText;
	private byte type; // -1 = NPC, 0 = start quest, 1 = end quest
	public boolean pendingDisposal = false;

	public NPCConversationManager(MapleClient c, int npc, int questid, byte type) {
		super(c);
		this.c = c;
		this.npc = npc;
		this.questid = questid;
		this.type = type;
	}

	public String getServerName() {
		return ServerConstants.serverName;
	}

	public int getNpc() {
		return npc;
	}

	public int getReborns() {
		return getPlayer().getReborns();
	}

	public int getVPoints() {
		return getPlayer().getVPoints();
	}

	public void gainVPoints(int gainedpoints) {
		c.getPlayer().gainVPoints(gainedpoints);
	}

	public int getNX() {
		return getPlayer().getNX();
	}

	public int getQuest() {
		return questid;
	}

	public void sendNext(String text, int id) {
		if (text.contains("#L")) {
			sendSimple(text);
			return;
		}
		this.c.getSession().write(MainPacketCreator.getNPCTalk(id, (byte) 0, text, "00 01", (byte) 0));
	}

	public EquipWorthCalculator newEWC() {
		return EquipWorthCalculator.ewc();
	}

	public String getWeaponEquip() {
		StringBuilder string = new StringBuilder();
		for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP).list()) {
			if (GameConstants.isWeapon(item.getItemId()))
				string.append("#L" + item.getPosition() + "##i" + item.getItemId() + "##l\r\n");
		}
		return string.toString();
	}

	public String getAllEquip() {
		StringBuilder string = new StringBuilder();
		for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP).list()) {
			string.append("#L" + item.getPosition() + "##i" + item.getItemId() + "##l\r\n");
		}
		return string.toString();
	}

	public String getAllItem() {
		StringBuilder string = new StringBuilder();
		for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP).list()) {
			string.append("#L" + item.getUniqueId() + "##i " + item.getItemId() + "#\r\n");
		}
		return string.toString();
	}

	public Equip getEquip(byte slot) {
		return (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
	}

	public void giveBuff(int skill, int level) {
		SkillFactory.getSkill(skill).getEffect(level).applyTo(c.getPlayer());
	}

	public byte getType() {
		return type;
	}

	public void safeDispose() {
		pendingDisposal = true;
	}

	public void dispose() {
		NPCScriptManager.getInstance().dispose(c);
	}

	public void askMapSelection(final String sel) {
		c.getSession().write(MainPacketCreator.getMapSelection(npc, sel));
	}

	public void sendPlaces(String text) {
		getClient().getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0x10, text, "", (byte) 0));
	}

	public void sendNext(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
	}

	public void sendNextS(String text, byte type) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", type));
	}

	public void sendSimpleS(String text, byte type) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 5, text, "", (byte) type));
	}

	public void sendSimpleS(String text, byte type, int speaker) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 5, text, "", (byte) type, speaker));
	}

	public void sendNextS(String text, byte type, int speaker) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", type, speaker));
	}

	public void sendPrev(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
	}

	public void sendPrevS(String text, byte type) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", type));
	}

	public void sendPrevS(String text, byte type, int speaker) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", type, speaker));
	}

	public void sendNextPrev(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
	}

	public void sendNextPrevS(String text, byte type) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", type));
	}

	public void sendNextPrevS(String text, byte type, int speaker) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", type, speaker));
	}

	public void sendOk(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
	}

	public void sendOkS(String text, byte type) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", type));
	}

	public void sendOkS(String text, byte type, int speaker) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", type, speaker));
	}

	public void sendYesNo(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 2, text, "", (byte) 0));
	}

	public void sendYesNoS(String text, byte type) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 2, text, "", type));
	}

	public void sendYesNoS(String text, byte type, int speaker) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 2, text, "", type, speaker));
	}

	public void askAcceptDecline(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 15, text, "", (byte) 0));
	}

	public void askAcceptDeclineNoESC(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 16, text, "", (byte) 0));
	}

	public void askAvatar(String text, int... args) {
		c.getSession().write(MainPacketCreator.getNPCTalkStyle(npc, text, true, false, args));
	}

	public void askAvatarZero(String text, int... args) {
		c.getSession().write(MainPacketCreator.getNPCTalkStyle(npc, text, false, true, args));
	}

	public void askAvatarAndroid(String text, int... args) {
		c.getSession().write(MainPacketCreator.getNPCTalkStyle(npc, text, false, false, args));
	}

	public void sendSimple(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalk(npc, (byte) 5, text, "", (byte) 0));
	}

	public void sendGetNumber(String text, int def, int min, int max) {
		c.getSession().write(MainPacketCreator.getNPCTalkNum(npc, text, def, min, max));
	}

	public void sendGetText(String text) {
		c.getSession().write(MainPacketCreator.getNPCTalkText(npc, text));
	}

	public void setGetText(String text) {
		this.getText = text;
	}

	public String getText() {
		return getText;
	}

	public int setRandomAvatar(int ticket, int... args_all) {
		if (!haveItem(ticket)) {
			return -1;
		}
		gainItem(ticket, (short) -1);

		int args = args_all[Randomizer.nextInt(args_all.length)];
		if (args < 100) {
			c.getPlayer().setSkinColor((byte) args);
			c.getPlayer().updateSingleStat(PlayerStat.SKIN, args);
		} else if (args < 30000) {
			c.getPlayer().setFace(args);
			c.getPlayer().updateSingleStat(PlayerStat.FACE, args);
		} else {
			c.getPlayer().setHair(args);
			c.getPlayer().updateSingleStat(PlayerStat.HAIR, args);
		}
		c.getPlayer().equipChanged();
		return 1;
	}

	public int setAvatar(int ticket, int args) {
		if (ticket != 0 && ticket != 4000000) {
			if (!haveItem(ticket)) {
				return -1;
			}
			gainItem(ticket, (short) -1);
		}
		if (args < 100) {
			c.getPlayer().setSkinColor((byte) args);
			c.getPlayer().updateSingleStat(PlayerStat.SKIN, args);
		} else if (args < 30000) {
			c.getPlayer().setFace(args);
			c.getPlayer().updateSingleStat(PlayerStat.FACE, args);
		} else {
			c.getPlayer().setHair(args);
			c.getPlayer().updateSingleStat(PlayerStat.HAIR, args);
		}
		c.getPlayer().equipChanged();
		return 1;
	}

	public void setSkin(byte skinColor) {
		c.getPlayer().setSkinColor(skinColor);
		c.getPlayer().updateSingleStat(PlayerStat.SKIN, skinColor);
		c.getPlayer().equipChanged();
	}

	public void setFace(int faceId) {
		c.getPlayer().setFace(faceId);
		c.getPlayer().updateSingleStat(PlayerStat.FACE, faceId);
		c.getPlayer().equipChanged();
	}

	public void setHair(int hairId) {
		c.getPlayer().setHair(hairId);
		c.getPlayer().updateSingleStat(PlayerStat.HAIR, hairId);
		c.getPlayer().equipChanged();
	}

	public void setFaceAndroid(int faceId) {
		c.getPlayer().getAndroid().setFace(faceId);
		c.getPlayer().updateAndroid();
	}

	public void setHairAndroid(int hairId) {
		c.getPlayer().getAndroid().setHair(hairId);
		c.getPlayer().updateAndroid();
	}

	public void setSkinColorAndroid(int skinId) {
		c.getPlayer().getAndroid().setSkinColor(skinId);
		c.getPlayer().updateAndroid();
	}

	public int getAndroidGender() {
		int itemid = c.getPlayer().getAndroid().getItemId();
		return ItemInformation.getInstance().getAndroidBasicSettings(ItemInformation.getInstance().getAndroid(itemid)).getGender();
	}

	public void sendStorage() {
		if (getPlayer().getLevel() < 120) {
			getPlayer().dropMessage(1, "120레벨 이상부터 창고이용이 가능합니다.");
			return;
		}
		c.getPlayer().setConversation(4);
		c.getPlayer().getStorage().send2ndPWChecking(c, npc, false);
	}

	public void sendCompose() { // Celphis
		try {
			c.getPlayer().setConversation(4);
			c.getPlayer().getStorage().send2ndPWChecking(c, npc, true);
			c.getPlayer().dropMessage(1, "첫번째 장비템을 첫번째 슬롯으로 옮겨주세요");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void openShop(int id) {
		MapleShopFactory.getInstance().getShop(id).sendShop(c);
	}

	public void changeJob(int job) {
		c.getPlayer().changeJob(job);
	}

	public void startQuest(int id) {
		MapleQuest.getInstance(id).start(getPlayer(), npc);
	}

	public void completeQuest(int id) {
		MapleQuest.getInstance(id).complete(getPlayer(), npc);
	}

	public void forfeitQuest(int id) {
		MapleQuest.getInstance(id).forfeit(getPlayer());
	}

	public void forceStartQuest() {
		MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), null);
	}

	public void forceStartQuest(int id) {
		MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc(), null);
	}

	public void forceStartQuest(String customData) {
		MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), customData);
	}

	public void forceCompleteQuest() {
		MapleQuest.getInstance(questid).forceComplete(getPlayer(), getNpc());
	}

	public String getQuestCustomData() {
		return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).getCustomData();
	}

	public void setQuestCustomData(String customData) {
		getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(customData);
	}

	public String getQuestCustomData(int qid) {
		return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(qid)).getCustomData();
	}

	public void setQuestCustomData(int qid, String customData) {
		getPlayer().getQuestNAdd(MapleQuest.getInstance(qid)).setCustomData(customData);
	}

	public long getMeso() {
		return getPlayer().getMeso();
	}

	public void gainAp(final int amount) {
		c.getPlayer().gainAp(amount);
	}

	public void setAp(final int amount) {
		c.getPlayer().setAp(amount);
	}

	public void ApReset() {
		c.getPlayer().updateSingleStat(PlayerStat.STR, 4);
		c.getPlayer().updateSingleStat(PlayerStat.DEX, 4);
		c.getPlayer().updateSingleStat(PlayerStat.INT, 4);
		c.getPlayer().updateSingleStat(PlayerStat.LUK, 4);
	}

	public void gainSp(final int amount) {
		c.getPlayer().gainSP(amount);
	}

	public void gainMeso(int gain) {
		c.getPlayer().gainMeso(gain, true, false, true);
	}

	public void gainExp(int gain) {
		c.getPlayer().gainExp(gain, true, true, true);
	}

	public void getGMLevel() {
		c.getPlayer().getGMLevel();
	}

	public void expandInventory(byte type, int amt) {
		c.getPlayer().getInventory(MapleInventoryType.getByType(type)).addSlot((byte) 4);
		c.getPlayer().inventoryslot_changed = true;
	}

	public void unequipEverything() {
		MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
		MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
		List<Short> ids = new LinkedList<Short>();
		for (IItem item : equipped.list()) {
			ids.add(item.getPosition());
		}
		for (short id : ids) {
			InventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
		}
	}

	public final void clearSkills() {
		Map<ISkill, SkillEntry> skills = getPlayer().getSkills();
		for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
			getPlayer().changeSkillLevel(skill.getKey(), (byte) 0, (byte) 0);
		}
	}

	public final boolean isCash(final int itemid) {
		return ItemInformation.getInstance().isCash(itemid);
	}

	public boolean hasSkill(int skillid) {
		ISkill theSkill = SkillFactory.getSkill(skillid);
		if (theSkill != null) {
			return c.getPlayer().getSkillLevel(theSkill) > 0;
		}
		return false;
	}

	public MapleCharacter getChar() {
		return getPlayer();
	}

	public MapleClient getC() {
		return c;
	}

	public void showEffect(boolean broadcast, String effect) {
		if (broadcast) {
			c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showEffect(effect));
		} else {
			c.getSession().write(MainPacketCreator.showEffect(effect));
		}
	}

	public void playSound(boolean broadcast, String sound) {
		if (broadcast) {
			c.getPlayer().getMap().broadcastMessage(MainPacketCreator.playSound(sound));
		} else {
			c.getSession().write(MainPacketCreator.playSound(sound));
		}
	}

	public void environmentChange(boolean broadcast, String env) {
		if (broadcast) {
			c.getPlayer().getMap().broadcastMessage(MainPacketCreator.environmentChange(env, 2));
		} else {
			c.getSession().write(MainPacketCreator.environmentChange(env, 2));
		}
	}

	public void MapleStar(MapleCharacter chr) {
		int first_rank = 0;
		int second_rank = 0;
		int i = 0;
		try {
			ResultSet sql = MYSQL.getConnection().prepareStatement("SELECT * FROM characters WHERE gm = 0 ORDER BY fame DESC LIMIT 2").executeQuery();
			while (sql.next()) {
				i++;
				if (i == 1) {
					first_rank = sql.getInt("id");
				} else if (i == 2) {
					second_rank = sql.getInt("id");
				}
			}
			sql.close();
		} catch (SQLException ex) {
			logger.debug("{}", ex);
		}
		chr.send(UIPacket.getMapleStar((byte) 8, chr.getClient(), first_rank, second_rank));
	}

	public void ChangeHeadTitle(byte i) {
		if (i == 0) {
			getPlayer().setKeyValue2("HeadTitle", Randomizer.isSuccess(50) ? +Randomizer.rand(10000, 90000) + Randomizer.rand(1000, 9000)
					: Randomizer.isSuccess(50) ? Randomizer.rand(1000, 9000) : 0 + Randomizer.rand(100, 900) + Randomizer.rand(10, 90) + Randomizer.rand(1, 9));
			List<Integer> num_ = new ArrayList<Integer>();
			int num = getPlayer().getKeyValue2("HaedTitle");
			int aa = num / 10000;
			int bb = num / 1000 - aa * 10;
			int cc = num / 100 - (aa * 100 + bb * 10);
			int dd = num / 10 - (aa * 1000 + bb * 100 + cc * 10);
			int ee = num / 1 - (aa * 10000 + bb * 1000 + cc * 100 + dd * 10);
			num_.add(aa);
			num_.add(bb);
			num_.add(cc);
			num_.add(dd);
			num_.add(ee);
			getPlayer().send(MainPacketCreator.HeadTitle(num_));
			sendOk("유저님의 왕관칭호가 성공적으로 변경되었습니다.\\r\\n#r채널이동을 해야 정상적으로 변경이 완료됩니다.#k");
		} else {
			getPlayer().setKeyValue2("HeadTitle", 000000);
			List<Integer> num_ = new ArrayList<Integer>();
			num_.add(0);
			num_.add(0);
			num_.add(0);
			num_.add(0);
			num_.add(0);
			getPlayer().send(MainPacketCreator.HeadTitle(num_));
			sendOk("유저님의 왕관칭호가 성공적으로 제거되었습니다.\\r\\n#r채널이동을 해야 정상적으로 변경이 완료됩니다.#k");
		}
		getPlayer().getMap().broadcastMessage(MainPacketCreator.removePlayerFromMap(getPlayer().getId()));
		getPlayer().getMap().broadcastMessage(MainPacketCreator.spawnPlayerMapobject(getPlayer()));
	}

	public void sendRebornRank() {
		String chat = "#e환생포인트 랭킹#n\r\n";
		try {
			int index = 0;
			ResultSet rs = MYSQL.getConnection().prepareStatement("SELECT * FROM `characters` order by `reborns` desc limit 10").executeQuery();
			while (rs.next()) {
				index++;
				chat += "\r\n" + index + "위. " + rs.getString("name") + ", 환생 포인트 : " + rs.getInt("reborns");
			}
		} catch (Exception e) {
		}
		sendOk(chat);
	}

	public void updateBuddyCapacity(int capacity) {
		c.getPlayer().setBuddyCapacity(capacity);
	}

	public int getBuddyCapacity() {
		return c.getPlayer().getBuddyCapacity();
	}

	public int partyMembersInMap() {
		int inMap = 0;
		for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
			if (char2.getParty() == getPlayer().getParty()) {
				inMap++;
			}
		}
		return inMap;
	}

	public List<MapleCharacter> getPartyMembers() {
		if (getPlayer().getParty() == null) {
			return null;
		}
		List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates
																		// an
																		// empty
																		// array
																		// full
																		// of
																		// shit..
		for (ChannelServer channel : ChannelServer.getAllInstances()) {
			for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
				if (chr != null) { // double check <3
					chars.add(chr);
				}
			}
		}
		return chars;
	}

	public void warpPartyWithExp(int mapId, int exp) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
				curChar.changeMap(target, target.getPortal(0));
				curChar.gainExp(exp, true, false, true);
			}
		}
	}

	public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
				curChar.changeMap(target, target.getPortal(0));
				curChar.gainExp(exp, true, false, true);
				curChar.gainMeso(meso, true);
			}
		}
	}

	public int itemQuantity(int itemid) {
		return getPlayer().getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
	}

	public int getSkillLevel(int skillid) {
		return getPlayer().getSkillLevel(skillid);
	}

	public void resetReactors() {
		getPlayer().getMap().resetReactors(getClient());
	}

	public void genericGuildMessage(int code) {
		c.getSession().write(MainPacketCreator.genericGuildMessage((byte) code));
	}

	public void disbandGuild() {
		final int gid = c.getPlayer().getGuildId();
		if (gid <= 0 || c.getPlayer().getGuildRank() != 1) {
			return;
		}
		ChannelServer.disbandGuild(gid);
	}

	public void doReborn() {
		if (getPlayer().getReborns() < MAX_REBORNS) {
			getPlayer().setReborns(getPlayer().getReborns() + 1);
			List<Pair<PlayerStat, Long>> reborns = new ArrayList<Pair<PlayerStat, Long>>(4);
			getPlayer().setLevel(1);
			getPlayer().setExp(0);
			reborns.add(new Pair<PlayerStat, Long>(PlayerStat.LEVEL, Long.valueOf(1)));
			reborns.add(new Pair<PlayerStat, Long>(PlayerStat.EXP, Long.valueOf(0)));
		} else {
			getPlayer().getClient().getSession().write(MainPacketCreator.serverNotice(6, "You have reached the maximum amount of rebirths!"));
		}
	}

	public void increaseGuildCapacity() {
		if (c.getPlayer().getMeso() < 5000000) {
			c.getSession().write(MainPacketCreator.serverNotice(1, "500만 메소가 충분하지 않습니다."));
			return;
		}
		final int gid = c.getPlayer().getGuildId();
		if (gid <= 0) {
			return;
		}
		ChannelServer.increaseGuildCapacity(gid);
		c.getPlayer().gainMeso(-5000000, true, false, true);
	}

	public boolean createAlliance(String alliancename) {
		MapleParty pt = c.getPlayer().getParty();
		MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
		if (otherChar == null || otherChar.getId() == c.getPlayer().getId()) {
			return false;
		}
		try {
			return ChannelServer.createAlliance(alliancename, c.getPlayer().getId(), otherChar.getId(), c.getPlayer().getGuildId(), otherChar.getGuildId());
		} catch (Exception re) {
			re.printStackTrace();
			return false;
		}
	}

	public boolean addCapacityToAlliance() {
		try {
			final MapleGuild gs = ChannelServer.getGuild(c.getPlayer().getGuildId());
			if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
				if (ChannelServer.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && ChannelServer.changeAllianceCapacity(gs.getAllianceId())) {
					gainMeso(-MapleAlliance.CHANGE_CAPACITY_COST);
					return true;
				}
			}
		} catch (Exception re) {
			re.printStackTrace();
		}
		return false;
	}

	public boolean disbandAlliance() {
		try {
			final MapleGuild gs = ChannelServer.getGuild(c.getPlayer().getGuildId());
			if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
				if (ChannelServer.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && ChannelServer.disbandAlliance(gs.getAllianceId())) {
					return true;
				}
			}
		} catch (Exception re) {
			re.printStackTrace();
		}
		return false;
	}

	public void displayGuildRanks() {
		c.getSession().write(MainPacketCreator.showGuildRanks(npc, MapleGuildRanking.getInstance().getRank()));
	}

	public boolean removePlayerFromInstance() {
		if (c.getPlayer().getEventInstance() != null) {
			c.getPlayer().getEventInstance().removePlayer(c.getPlayer());
			return true;
		}
		return false;
	}

	public boolean isPlayerInstance() {
		if (c.getPlayer().getEventInstance() != null) {
			return true;
		}
		return false;
	}

	public void changeStat(byte slot, int type, short amount) {
		Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		switch (type) {
		case 0:
			sel.setStr(amount);
			break;
		case 1:
			sel.setDex(amount);
			break;
		case 2:
			sel.setInt(amount);
			break;
		case 3:
			sel.setLuk(amount);
			break;
		case 4:
			sel.setHp(amount);
			break;
		case 5:
			sel.setMp(amount);
			break;
		case 6:
			sel.setWatk(amount);
			break;
		case 7:
			sel.setMatk(amount);
			break;
		case 8:
			sel.setWdef(amount);
			break;
		case 9:
			sel.setMdef(amount);
			break;
		case 10:
			sel.setAcc(amount);
			break;
		case 11:
			sel.setAvoid(amount);
			break;
		case 12:
			sel.setHands(amount);
			break;
		case 13:
			sel.setSpeed(amount);
			break;
		case 14:
			sel.setJump(amount);
			break;
		case 15:
			sel.setUpgradeSlots((byte) amount);
			break;
		case 16:
			sel.setViciousHammer((byte) amount);
			break;
		case 17:
			sel.setLevel((byte) amount);
			break;
		default:
			break;
		}
		c.getPlayer().equipChanged();
	}

	public void giveMerchantMesos() {
		long mesos = 0;
		try {
			Connection con = (Connection) MYSQL.getConnection();
			PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
			ps.setInt(1, getPlayer().getId());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
			} else {
				mesos = rs.getLong("mesos");
			}
			rs.close();
			ps.close();

			ps = (PreparedStatement) con.prepareStatement("UPDATE hiredmerchants SET mesos = 0 WHERE merchantid = ?");
			ps.setInt(1, getPlayer().getId());
			ps.executeUpdate();
			ps.close();

		} catch (SQLException ex) {
			logger.debug("Error gaining mesos in hired merchant {}", ex);
		}
		c.getPlayer().gainMeso((int) mesos, true);
	}

	public long getMerchantMesos() {
		long mesos = 0;
		try {
			Connection con = (Connection) MYSQL.getConnection();
			PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
			ps.setInt(1, getPlayer().getId());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
			} else {
				mesos = rs.getLong("mesos");
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			logger.debug("Error gaining mesos in hired merchant {}", ex);
		}
		return mesos;
	}

	public void openMerchantItemStore() {
		this.c.getPlayer().setConversation(3);
		HiredMerchantHandler.displayMerch(this.c);
		this.c.getSession().write(MainPacketCreator.resetActions());
	}

	public final short getKegs() {
		return AramiaFireWorks.getInstance().getKegsPercentage();
	}

	public void giveKegs(final int kegs) {
		AramiaFireWorks.getInstance().giveKegs(c.getPlayer(), kegs);
	}

	public final MapleInventory getInventory(byte type) {
		return c.getPlayer().getInventory(MapleInventoryType.getByType(type));
	}

	public void resetStats(final int str, final int dex, final int int_, final int luk) {
		List<Pair<PlayerStat, Long>> stats = new ArrayList<Pair<PlayerStat, Long>>(2);
		final MapleCharacter chr = c.getPlayer();
		int total = chr.getStat().getStr() + chr.getStat().getDex() + chr.getStat().getLuk() + chr.getStat().getInt() + chr.getRemainingAp();

		total -= str;
		chr.getStat().setStr(str);

		total -= dex;
		chr.getStat().setDex(dex);

		total -= int_;
		chr.getStat().setInt(int_);

		total -= luk;
		chr.getStat().setLuk(luk);

		chr.setRemainingAp(total);
		stats.add(new Pair<PlayerStat, Long>(PlayerStat.STR, (long) str));
		stats.add(new Pair<PlayerStat, Long>(PlayerStat.DEX, (long) dex));
		stats.add(new Pair<PlayerStat, Long>(PlayerStat.INT, (long) int_));
		stats.add(new Pair<PlayerStat, Long>(PlayerStat.LUK, (long) luk));
		stats.add(new Pair<PlayerStat, Long>(PlayerStat.AVAILABLEAP, (long) total));
		c.getSession().write(MainPacketCreator.updatePlayerStats(stats, false, c.getPlayer().getJob()));
	}

	public final boolean dropItem(int slot, int invType, int quantity) {
		MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
		if (inv == null) {
			return false;
		}
		InventoryManipulator.drop(c, inv, (short) slot, (short) quantity);
		return true;
	}

	public void maxStats() {
		List<Pair<PlayerStat, Long>> statup = new ArrayList<Pair<PlayerStat, Long>>(2);

		c.getPlayer().setRemainingAp(0);
		c.getPlayer().setRemainingSp(0);
		c.getPlayer().getStat().setStr(32767);
		c.getPlayer().getStat().setDex(32767);
		c.getPlayer().getStat().setInt(32767);
		c.getPlayer().getStat().setLuk(32767);

		c.getPlayer().getStat().setHp(99999);
		c.getPlayer().getStat().setMp(99999);
		c.getPlayer().getStat().setMaxHp(99999);
		c.getPlayer().getStat().setMaxMp(99999);

		statup.add(new Pair(PlayerStat.STR, Long.valueOf(32767)));
		statup.add(new Pair(PlayerStat.DEX, Long.valueOf(32767)));
		statup.add(new Pair(PlayerStat.LUK, Long.valueOf(32767)));
		statup.add(new Pair(PlayerStat.INT, Long.valueOf(32767)));
		statup.add(new Pair(PlayerStat.HP, Long.valueOf(99999)));
		statup.add(new Pair(PlayerStat.MAXHP, Long.valueOf(99999)));
		statup.add(new Pair(PlayerStat.MP, Long.valueOf(99999)));
		statup.add(new Pair(PlayerStat.MAXMP, Long.valueOf(99999)));
		statup.add(new Pair(PlayerStat.AVAILABLEAP, Long.valueOf(0)));
		statup.add(new Pair(PlayerStat.AVAILABLESP, Long.valueOf(0)));

		c.getSession().write(MainPacketCreator.updatePlayerStats(statup, c.getPlayer().getJob()));
	}

	/// 원정대
	/// 이전버전/////////////////////////////////////////////////////////////////////////
	public MapleSquadLegacy getSquad(String type) {
		return c.getChannelServer().getMapleSquad(type);
	}

	public int getSquadAvailability(String type) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return -1;
		}
		return squad.getStatus();
	}

	public void registerSquad(String type, int minutes, String startText) {
		final MapleSquadLegacy squad = new MapleSquadLegacy(c.getChannel(), type, c.getPlayer(), minutes * 60 * 1000);
		final MapleMap map = c.getPlayer().getMap();

		map.broadcastMessage(MainPacketCreator.getClock(minutes * 60));
		map.broadcastMessage(MainPacketCreator.serverNotice(6, c.getPlayer().getName() + startText));
		c.getChannelServer().addMapleSquad(squad, type);
	}

	public boolean getSquadList(String type, byte type_) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return false;
		}
		if (type_ == 0) { // Normal viewing
			sendNext(squad.getSquadMemberString(type_));
		} else if (type_ == 1) { // Squad Leader banning, Check out banned
									// participant
			sendSimple(squad.getSquadMemberString(type_));
		} else if (type_ == 2) {
			if (squad.getBannedMemberSize() > 0) {
				sendSimple(squad.getSquadMemberString(type_));
			} else {
				sendNext(squad.getSquadMemberString(type_));
			}
		}
		return true;
	}

	public byte isSquadLeader(String type) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return -1;
		} else {
			if (squad.getLeader().getId() == c.getPlayer().getId()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public void banMember(String type, int pos) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.banMember(pos);
		}
	}

	public void acceptMember(String type, int pos) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.acceptMember(pos);
		}
	}

	public int addMember(String type, boolean join) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			return squad.addMember(c.getPlayer(), join);
		}
		return -1;
	}

	public byte isSquadMember(String type) {
		final MapleSquadLegacy squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return -1;
		} else {
			if (squad.getMembers().contains(c.getPlayer())) {
				return 1;
			} else if (squad.isBanned(c.getPlayer())) {
				return 2;
			} else {
				return 0;
			}
		}
	}

	public void setProfession(int index, int skill) {
		MapleProfession pro = c.getPlayer().getProfession();
		if (index == 1) {
			pro.setFirstProfession(MapleProfessionType.getProfessionById(skill));
			pro.setFirstProfessionExp(0);
			pro.setFirstProfessionLevel(1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(skill), (byte) 1, (byte) 10);
		} else if (index == 2) {
			pro.setSecondProfession(MapleProfessionType.getProfessionById(skill));
			pro.setSecondProfessionExp(0);
			pro.setSecondProfessionLevel(1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(skill), (byte) 1, (byte) 10);
		}
	}

	public void deleteProfession(int index) {
		MapleProfession pro = c.getPlayer().getProfession();
		if (index == 1) {
			pro.setFirstProfessionExp(0);
			pro.setFirstProfessionLevel(0);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(pro.getFirstProfessionSkill()), (byte) 0, (byte) 0);
			pro.setFirstProfession(MapleProfessionType.NONE);
		} else if (index == 2) {
			pro.setSecondProfessionExp(0);
			pro.setSecondProfessionLevel(0);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(pro.getSecondProfessionSkill()), (byte) 0, (byte) 0);
			pro.setSecondProfession(MapleProfessionType.NONE);
		}
	}

	public void levelUpProfession(int index) {
		MapleProfession pro = c.getPlayer().getProfession();
		if (index == 1) {
			pro.setFirstProfessionExp(0);
			pro.addFirstProfessionLevel(1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(pro.getFirstProfessionSkill()), (byte) 1, (byte) 10);
		} else if (index == 2) {
			pro.setSecondProfessionExp(0);
			pro.addSecondProfessionLevel(1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(pro.getSecondProfessionSkill()), (byte) 1, (byte) 10);
		}
	}

	public int getProfession(int index) {
		if (index == 1)
			return c.getPlayer().getProfession().getFirstProfessionSkill();
		else if (index == 2)
			return c.getPlayer().getProfession().getSecondProfessionSkill();
		return 0;
	}

	public void setExpEvent(int rate, int time) {
		final int origin = ChannelServer.getInstance(0).getExpRate();
		long period = time * 1000L;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.setExpRate(rate);
			cserv.broadcastPacket(MainPacketCreator.getGMText(7, "[이벤트] 경험치 배율 이벤트가 시작되었습니다!"));
		}
		EtcTimer t = EtcTimer.getInstance();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				for (ChannelServer cserv : ChannelServer.getAllInstances()) {
					cserv.setExpRate(origin);
					cserv.broadcastPacket(MainPacketCreator.getGMText(7, "[이벤트] 경험치 배율 이벤트가 종료되었습니다."));
				}
			}
		};
		t.schedule(r, period);
	}

	public void setDropEvent(int rate, int time) {
		final int origin = ChannelServer.getInstance(0).getDropRate();
		long period = time * 1000L;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.setDropRate(rate);
			cserv.broadcastPacket(MainPacketCreator.getGMText(7, "[이벤트] 드롭 배율 이벤트가 시작되었습니다!"));
		}
		EtcTimer t = EtcTimer.getInstance();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				for (ChannelServer cserv : ChannelServer.getAllInstances()) {
					cserv.setDropRate(origin);
					cserv.broadcastPacket(MainPacketCreator.getGMText(7, "[이벤트] 드롭 배율 이벤트가 종료되었습니다."));
				}
			}
		};
		t.schedule(r, period);
	}

	public void setMesoEvent(int rate, int time) {
		final byte origin = (byte) ChannelServer.getInstance(0).getMesoRate();
		long period = time * 1000L;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			cserv.setMesoRate((byte) rate);
			cserv.broadcastPacket(MainPacketCreator.getGMText(7, "[이벤트] 메소 배율 이벤트가 시작되었습니다!"));
		}
		EtcTimer t = EtcTimer.getInstance();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				for (ChannelServer cserv : ChannelServer.getAllInstances()) {
					cserv.setMesoRate(origin);
					cserv.broadcastPacket(MainPacketCreator.getGMText(7, "[이벤트] 메소 배율 이벤트가 종료되었습니다."));
				}
			}
		};
		t.schedule(r, period);
	}

	public void changeMap(int mapid) {
		ChannelServer cserv = c.getChannelServer();
		MapleMap target = null;
		if (c.getPlayer().getEventInstance() != null) {
			target = c.getPlayer().getEventInstance().getMapFactory().getMap(mapid);
		} else {
			target = cserv.getMapFactory().getMap(mapid);
		}

		MaplePortal targetPortal = null;
		if (mapid > 0) {
			try {
				targetPortal = target.getPortal(0);
			} catch (IndexOutOfBoundsException e) {
				// noop, assume the gm didn't know how many portals there are
				c.getPlayer().dropMessage(5, "Invalid portal selected.");
			} catch (NumberFormatException a) {
				// noop, assume that the gm is drunk
			}
		}
		if (targetPortal == null) {
			targetPortal = target.getPortal(0);
		}
		c.getPlayer().changeMap(target, targetPortal);
	}

	public void setName(String name) {
		String[] blocks = { "GM", "요크", "주니", "지훈", "정규" };
		for (String b : blocks)
			if (name.indexOf(b) != -1)
				return;
		for (char c : name.toCharArray()) {
			if (!(c > 'a' && c < 'z') && !(c > '가' && c < '하'))
				return;
		}
		if (!(name.length() > 1))
			return;
		Connection con = MYSQL.getConnection();
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("SELECT * FROM `characters` WHERE `name` = ?");
			ps.setString(1, name);
			if (!ps.executeQuery().next()) {
				getPlayer().setName(name);
				ps = con.prepareStatement("UPDATE `characters` SET `name` = ? WHERE `id` = ?");
				ps.setString(1, name);
				ps.setInt(2, getPlayer().getId());
				ps.executeUpdate();
			}
			ps.close();
			getPlayer().getClient().getSession().close();
		} catch (SQLException e) {
			logger.debug("{}", e);
		}
	}

	public void setInnerAbility(int level) {
		if (level >= 30) {
			InnerSkillValueHolder isvh = InnerAbillity.getInstance().renewSkill(0, -1);
			c.getPlayer().getInnerSkills().add(isvh);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
		} else if (level >= 60) {
			InnerSkillValueHolder isvh = InnerAbillity.getInstance().renewSkill(Randomizer.rand(0, 2), -1);
			c.getPlayer().getInnerSkills().add(isvh);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
		} else if (level >= 100) {
			InnerSkillValueHolder isvh = InnerAbillity.getInstance().renewSkill(Randomizer.rand(1, 3), -1);
			c.getPlayer().getInnerSkills().add(isvh);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
		}
	}

	public void setInnerStats() {
		InnerSkillValueHolder isvh = InnerAbillity.getInstance().renewSkill(0, -1);
		c.getPlayer().getInnerSkills().add(isvh);
		c.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
		c.getPlayer().send(MainPacketCreator.getPlayerInfo(c.getPlayer()));
		MapleMap currentMap = c.getPlayer().getMap();
		currentMap.removePlayer(c.getPlayer());
		currentMap.addPlayer(c.getPlayer());
	}

	public void invitedRPS() {
		if (c.getPlayer().getKeyValue2("RPS") != 0) {
			MapleCharacter ochr = c.getPlayer().getMap().getCharacterById_InMap(c.getPlayer().getKeyValue2("RPSOTHER"));
			if (ochr.getMeso() >= c.getPlayer().getKeyValue2("RPS")) {
				ochr.setKeyValue2("RPS", c.getPlayer().getKeyValue2("RPS"));
				ochr.setKeyValue2("RPSOTHER", c.getPlayer().getId());
				NPCScriptManager.getInstance().start(ochr.getClient(), 2100, "RPSACCEPT");
			} else {
				c.getPlayer().dropMessage(1, "상대방 메소가 부족합니다.");
			}
		} else {
			c.getPlayer().dropMessage(1, "메소가 설정되지 않았습니다.");
		}

	}

	public void acceptRPS() {
		MapleCharacter ochr = c.getPlayer().getMap().getCharacterById_InMap(c.getPlayer().getKeyValue2("RPSOTHER"));
		MapleUserTrade.inviteTrade(ochr, c.getPlayer(), false);
	}

	public void openCS() {
		InterServerHandler.EnterCS(c, c.getPlayer(), false);
	}

	public String MakeGuildPot() {
		try {
			boolean isExist = false;
			MapleReactor react = null;
			if (PotSystem.getPotId(getPlayer().getGuildId()) != 0) {
				return "이미 설치되있습니다.";
			}
			for (final MapleMapObject remo : getPlayer().getMap().getAllReactor()) {
				react = (MapleReactor) remo;
				if (react.getGuildid() == 0) {
					continue;
				} else if ((react.getPosition().getX() - 100 < getPlayer().getPosition().getX() && react.getPosition().getX() + 100 > getPlayer().getPosition().getX())
						&& (react.getPosition().getY() - 50 < getPlayer().getPosition().getY() && react.getPosition().getY() + 50 > getPlayer().getPosition().getY())) {
					isExist = true;
					return "타 길드 분재랑은 겹치지 못합니다.";
				}

			}

			if (!isExist) {
				int[] rids = { 100000, 100002 };
				int randrid = rids[Randomizer.rand(0, 1)];
				final MapleReactorStats stats = MapleReactorFactory.getReactor(randrid);
				final MapleReactor myReactor = new MapleReactor(stats, randrid, getPlayer().getGuildId());
				stats.setFacingDirection((byte) 0);
				myReactor.setPosition(c.getPlayer().getPosition());
				myReactor.setDelay(0);
				myReactor.setState((byte) 0);
				myReactor.setName(getPlayer().getGuild().getName());
				PotSystem.addPot(getPlayer().getGuildId(), myReactor.getReactorId(), 0);
				c.getPlayer().getMap().spawnReactor(myReactor);

				Connection con = MYSQL.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO pots (rid,`name`,x,y,gid,channel) VALUES ( ?, ?, ?, ?,?,?)");
				ps.setInt(1, myReactor.getReactorId());
				ps.setString(2, getPlayer().getGuild().getName());
				ps.setInt(3, myReactor.getPosition().x);
				ps.setInt(4, myReactor.getPosition().y);
				ps.setInt(5, getPlayer().getGuildId());
				ps.setInt(6, getClient().getChannel());
				ps.executeUpdate();
				return "성공적으로 설치되었습니다.";
			}
			return "설치에 실패하셨습니다.";
		} catch (Exception ex) {
			ex.printStackTrace();
			return "오류가 발생하여 설치하지 못했습니다.";
		}
	}

	public int getRC() {
		return getPlayer().getRC();
	}

	public void setRC(int rc) {
		getPlayer().gainRC(rc - getRC());
	}

	public void startCatch() {
		final int MaxCatchSize = (getMap().getCharactersSize() / 5) * 2;
		int CatchSize = 0;
		String CatchingName = "", CatchingName2 = "";

		MapleMap map = ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(109090300);
		map.stopCatch();
		List<MapleCharacter> players = new ArrayList<MapleCharacter>();
		players.addAll(getMap().getCharacters());
		Collections.addAll(players);
		Collections.shuffle(players);
		for (MapleCharacter chr : players) {
			chr.cancelAllBuffs();
			if (MaxCatchSize > CatchSize) {
				chr.isCatching = true;
				chr.isCatched = false;
				chr.changeMap(map, new Point(875, -453));
				CatchingName += chr.getName() + ",";
				CatchingName2 += chr.getName() + "\r\n";
				CatchSize++;
				chr.giveDebuff(DiseaseStats.STUN, MobSkillFactory.getMobSkill(123, 1));
			} else {
				chr.isCatched = true;
				chr.isCatching = false;
				chr.changeMap(map, new Point(-592, -451));
			}
		}

		map.broadcastMessage(MainPacketCreator.serverNotice(1, "[술래 목록]\r\n" + CatchingName2));
		map.broadcastMessage(MainPacketCreator.serverNotice(6, "[술래 목록] " + CatchingName));
		map.startCatch();
	}

	public void gainSponserItem(int item, final String name, short allstat, short damage, byte upgradeslot) {
		if (GameConstants.isEquip(item)) {
			Equip Item = (Equip) ItemInformation.getInstance().getEquipById(item);
			Item.setOwner(name);
			Item.setStr(allstat);
			Item.setDex(allstat);
			Item.setInt(allstat);
			Item.setLuk(allstat);
			Item.setWatk(damage);
			Item.setMatk(damage);
			Item.setUpgradeSlots(upgradeslot);
			InventoryManipulator.addFromDrop(c, Item, false);
		} else {
			gainItem(item, allstat, damage);
		}
	}

	public void gainPotentialItem(int item, int quantity, byte grade, byte thing, int potential1, int potential2, int potential3) {
		if (GameConstants.isEquip(item)) {
			Equip Item = (Equip) ItemInformation.getInstance().getEquipById(item);
			Item.setLines(grade);
			Item.setState(thing);
			Item.setPotential1(potential1);
			Item.setPotential2(potential2);
			Item.setPotential3(potential3);
			InventoryManipulator.addFromDrop(c, Item, false);
		}
	}

	public String getHyperSkills(byte type) {
		MapleData data = MapleDataProviderFactory.getDataProvider("Skill.wz").getData(StringUtil.getLeftPaddedStr("" + (getPlayer().getJob() == 2218 ? 2217 : getPlayer().getJob()), '0', 3) + ".img");
		int skillid = 0;
		String Lists = "";
		for (MapleData skill : data) {
			if (skill != null) {
				for (MapleData skillId : skill.getChildren()) {
					if (!skillId.getName().equals("icon")) {
						if (MapleDataTool.getIntConvert("hyper", skillId, 120) == type) {
							if ((MapleDataTool.getIntConvert("reqLev", skillId, 0) <= getPlayer().getLevel()) || (getPlayer().getReborns() > 50 && getPlayer().getLevel() >= 140)) {
								skillid = Integer.parseInt(skillId.getName());
								if (getPlayer().getSkillLevel(skillid) == 0) {
									Lists += "#L" + skillid + "##s" + skillid + "# #q" + skillid + "##l\r\n";
								}
							}
						}
					}
				}
			}
		}
		return Lists;
	}

	public String giveHyperSp() {
		boolean reborn = getPlayer().getReborns() > 50;
		if (getPlayer().getLevel() >= 140) {
			if ((!reborn && getPlayer().getKeyValue2("hyper140") != 1) || (reborn && getPlayer().getKeyValue2("hyper140") != 2)) {
				getPlayer().setKeyValue2("hyper140", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper1", getPlayer().getKeyValue2("hyper1") + 1);
				getPlayer().setKeyValue2("hyper2", getPlayer().getKeyValue2("hyper2") + 1);
			}
		}
		if (getPlayer().getLevel() >= 150) {
			if ((!reborn && getPlayer().getKeyValue2("hyper150") != 1) || (reborn && getPlayer().getKeyValue2("hyper150") != 2)) {
				getPlayer().setKeyValue2("hyper150", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper1", getPlayer().getKeyValue2("hyper1") + 1);
				getPlayer().setKeyValue2("hyper3", getPlayer().getKeyValue2("hyper3") + 1);
			}
		}
		if (getPlayer().getLevel() >= 160) {
			if ((!reborn && getPlayer().getKeyValue2("hyper160") != 1) || (reborn && getPlayer().getKeyValue2("hyper160") != 2)) {
				getPlayer().setKeyValue2("hyper160", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper1", getPlayer().getKeyValue2("hyper1") + 1);
				getPlayer().setKeyValue2("hyper2", getPlayer().getKeyValue2("hyper2") + 1);
			}
		}
		if (getPlayer().getLevel() >= 170) {
			if ((!reborn && getPlayer().getKeyValue2("hyper170") != 1) || (reborn && getPlayer().getKeyValue2("hyper170") != 2)) {
				getPlayer().setKeyValue2("hyper170", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper1", getPlayer().getKeyValue2("hyper1") + 1);
				getPlayer().setKeyValue2("hyper3", getPlayer().getKeyValue2("hyper3") + 1);
			}
		}
		if (getPlayer().getLevel() >= 180) {
			if ((!reborn && getPlayer().getKeyValue2("hyper180") != 1) || (reborn && getPlayer().getKeyValue2("hyper180") != 2)) {
				getPlayer().setKeyValue2("hyper180", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper2", getPlayer().getKeyValue2("hyper3") + 1);
			}
		}
		if (getPlayer().getLevel() >= 190) {
			if ((!reborn && getPlayer().getKeyValue2("hyper190") != 1) || (reborn && getPlayer().getKeyValue2("hyper190") != 2)) {
				getPlayer().setKeyValue2("hyper190", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper1", getPlayer().getKeyValue2("hyper1") + 1);
				getPlayer().setKeyValue2("hyper2", getPlayer().getKeyValue2("hyper2") + 1);
			}
		}
		if (getPlayer().getLevel() >= 200) {
			if ((!reborn && getPlayer().getKeyValue2("hyper200") != 1) || (reborn && getPlayer().getKeyValue2("hyper200") != 2)) {
				getPlayer().setKeyValue2("hyper200", reborn ? 2 : 1);
				getPlayer().setKeyValue2("hyper1", getPlayer().getKeyValue2("hyper1") + 1);
				getPlayer().setKeyValue2("hyper2", getPlayer().getKeyValue2("hyper2") + 1);
				getPlayer().setKeyValue2("hyper3", getPlayer().getKeyValue2("hyper3") + 1);
			}
		}
		getPlayer().setKeyValue2("hyper", 1);
		return "성공적으로 하이퍼 스킬 SP가 지급되었습니다.";
	}

	public void ItemName(MapleCharacter player, byte pos, final String name) {
		MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
		Equip eq = (Equip) equip.getItem(pos);
		eq.setOwner(name);
	}

	public String ItemList(MapleClient c, String error) {
		boolean a = false;
		StringBuilder str = new StringBuilder();
		MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
		List<String> stra = new LinkedList<String>();
		for (IItem item : equip.list()) {
			stra.add("#L" + item.getPosition() + "##v" + item.getItemId() + "##l\r\n");
			a = true;
		}
		if (!a) {
			stra.add(error);
			NPCScriptManager.getInstance().dispose(c);
		}
		for (String strb : stra) {
			str.append(strb);
		}
		return str.toString();
	}

	public String 마스터리북() {
		MapleData data = MapleDataProviderFactory.getDataProvider("Skill.wz").getData(StringUtil.getLeftPaddedStr("" + getJob(), '0', 3) + ".img");
		int a = 0;
		StringBuilder str = new StringBuilder();
		for (MapleData skill : data) {
			if (skill != null) {
				for (MapleData skillId : skill.getChildren()) {
					if (!skillId.getName().equals("icon")) {
						byte maxlevel = (byte) MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
						if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0 && !(MapleDataTool.getIntConvert("reqLev", skillId, 0) > 0) && Integer.parseInt(skillId.getName()) != 12110025
								&& Integer.parseInt(skillId.getName()) != 12101022) {
							try {
								if (getPlayer().getSkillLevel(Integer.parseInt(skillId.getName())) < maxlevel) {
									a++;
									str.append("#L" + Integer.parseInt(skillId.getName()) + "# #s" + Integer.parseInt(skillId.getName()) + "# #fn돋움##fs14##e#q" + Integer.parseInt(skillId.getName())
											+ "##n#fs##fn##l\r\n");
								}
							} catch (NumberFormatException e) {
								continue;
							}
						}
					}
				}
			}
		}
		if (a == 0) {
			str.append("#fn돋움##fs14##e더이상 마스터할 스킬이 없습니다.#n#fs##fn#\r\n");
		}
		return str.toString();
	}

	public String SoulItemList(MapleClient c, String b) {
		boolean a = false;
		String end;
		StringBuilder str = new StringBuilder();
		MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
		List<String> stra = new LinkedList<String>();
		for (IItem item : equip.list()) {
			Equip eq = (Equip) equip.getItem(item.getPosition());
			if (eq.getSoulEnchanter() != 0) {
				stra.add("#b#L" + item.getPosition() + "##i" + item.getItemId() + "##t" + item.getItemId() + "##l\r\n");
				a = true;
			}
		}
		if (a) {
			for (String strb : stra) {
				str.append(strb);
			}
			end = b + "\r\n\r\n#b" + str;
			return end;
		} else {
			StringBuilder str1 = new StringBuilder();
			str1.append("소울을 감정할 아이템이 존재하지 않습니다.");
			return str1.toString();
		}
	}

	public String SoulItem(byte pos, boolean a) {
		MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
		Equip eq = (Equip) equip.getItem(pos);
		if (a) {
			return "" + eq.getSoulSkill();
		} else {
			return "" + eq.getSoulEnchanter();
		}
	}

	public String getSoulSkillName(int i) {
		return SkillFactory.getSkillName(i);
	}

	public void showMessage(String txt) {
		getPlayer().send(MainPacketCreator.showQuestMessage(txt));
	}

	public void openDuey(boolean item) {
		if (getPlayer().getLevel() >= 120) {
			getPlayer().send(DueyHandler.sendDuey(item ? 10 : 9, getPlayer().getName()));
		} else {
			getPlayer().dropMessage(1, "레벨 120 이상부터 사용이 가능 합니다.");
			getPlayer().ea();
		}
	}

	public void BuyPET(int Petitem) {
		Item itemr = new Item(Petitem, (short) 1, (short) 1, (short) 0);
		itemr.setExpiration(2475606994921L);
		final MaplePet pet = MaplePet.createPet(Petitem, itemr.getExpiration());
		itemr.setPet(pet);
		itemr.setUniqueId(pet.getUniqueId());
		InventoryManipulator.addbyItem(c, itemr);
		InventoryManipulator.addFromDrop(getClient(), itemr, false);
	}

	public int AverageLevel(MapleCharacter chr) {
		int a = 0;
		for (final MapleCharacter partymem : chr.getClient().getChannelServer().getPartyMembers(chr.getParty())) {
			a += partymem.getLevel();
		}
		return (a / chr.getParty().getMembers().size());
	}

	public void startTime() {
		getPlayer().time = System.currentTimeMillis();
	}

	public int getTime() {
		return (int) ((System.currentTimeMillis() - getPlayer().time) / 1000);
	}

	public void openAuction() {
		c.getSession().write(UIPacket.OpenUI(0xA1));
		c.getSession().write(AuctionPacket.showItemList(AuctionHandler.WorldAuction.getItems(), false));
	}

	public void HyperSkillMax() {
		MapleData data = MapleDataProviderFactory.getDataProvider("Skill.wz").getData(StringUtil.getLeftPaddedStr("" + c.getPlayer().getJob(), '0', 3) + ".img");
		final ISkill skills = null;
		byte maxLevel = 0;
		for (MapleData skill : data) {
			if (skill != null) {
				for (MapleData skillId : skill.getChildren()) {
					if (!skillId.getName().equals("icon")) {
						maxLevel = (byte) MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
						if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0 && MapleDataTool.getIntConvert("reqLev", skillId, 0) > 0) { // 스킬창에
																																					// 안보이는
																																					// 스킬은
																																					// 올리지않음
							if (c.getPlayer().getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0)) {
								c.getPlayer().changeSkillLevel(SkillFactory.getSkill(Integer.parseInt(skillId.getName())), maxLevel,
										SkillFactory.getSkill(Integer.parseInt(skillId.getName())).isFourthJob() ? maxLevel : 0);
							}
						}
					}
				}
			}
		}
	}
}
