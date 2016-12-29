package handler.channel;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.AccountStatusType;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.items.MapleInventoryType;
import client.skills.SkillFactory;
import community.BuddylistEntry;
import community.MapleGuild;
import community.MapleMultiChatCharacter;
import community.MapleUserTrade;
import constants.GameConstants;
import constants.ServerConstants;
import constants.programs.ControlUnit;
import handler.duey.DueyHandler;
import launch.BuddyChatServer;
import launch.ChannelServer;
import launch.helpers.ChracterTransfer;
import launch.helpers.MaplePlayerIdChannelPair;
import launch.holder.MapleBuffValueHolder;
import launch.holder.MaplePlayerHolder;
import launch.world.WorldBroadcasting;
import launch.world.WorldCommunity;
import packet.creators.CashPacket;
import packet.creators.LoginPacket;
import packet.creators.MainPacketCreator;
import packet.creators.SoulWeaponPacket;
import packet.creators.UIPacket;
import packet.skills.AngelicBusterSkill;
import packet.skills.ZeroSkill;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import scripting.NPCScriptManager;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.shops.IMapleCharacterShop;

public class InterServerHandler {
	private static final Logger logger = LoggerFactory.getLogger(InterServerHandler.class);

	public static final void EnterMTS(final MapleClient c) {
		final MapleMap map = c.getChannelServer().getMapFactory().getMap(910000000);
		c.getPlayer().changeMap(map, map.getPortal(0));
	}

	public static final void EnterCS(final MapleClient c, final MapleCharacter chr, final boolean ScriptEnter) {
		if (!chr.isAlive()) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}
		if (ScriptEnter && ServerConstants.cshopNpc != 0) {
			NPCScriptManager.getInstance().start(c, ServerConstants.cshopNpc);
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}
		final ChannelServer ch = ChannelServer.getInstance(c.getChannel());

		String ip = ServerConstants.Host;

		if (ip == null) { // Cash Shop not init yet
			c.getSession().write(MainPacketCreator.serverNotice(5, "캐시샵을 현재 사용할 수 없습니다."));
			return;
		}

		if (chr.getTrade() != null) {
			MapleUserTrade.cancelTrade(chr.getTrade());
		}

		final IMapleCharacterShop shop = chr.getPlayerShop();
		if (shop != null) {
			shop.removeVisitor(chr);
			if (shop.isOwner(chr)) {
				shop.setOpen(true);
			}
		}

		if (chr.getMessenger() != null) {
			MapleMultiChatCharacter messengerplayer = new MapleMultiChatCharacter(chr);
			WorldCommunity.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
		}
		ChannelServer.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
		ChannelServer.addCooldownsToStorage(chr.getId(), chr.getAllCooldowns());
		ChannelServer.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
		ChannelServer.ChannelChange_Data(new ChracterTransfer(chr), chr.getId(), -10);
		ch.removePlayer(chr);
		c.updateLoginState(AccountStatusType.CHANGE_CHANNEL.getValue(), c.getSessionIPAddress());

		c.getSession().write(MainPacketCreator.getChannelChange(c, ServerConstants.CashShopPort)); // default
																									// cashshop
																									// port
		chr.saveToDB(false, false);
		chr.getMap().removePlayer(chr);
		c.setPlayer(null);
	}

	/**
	 * 사용자의 인게임(채널 서버로 접속) 처리를 담당한다.
	 * 
	 * TODO PLAYER_LOGGEDIN 패킷을 전달받으면 호출이 되는 메소드이다.
	 * 1. 최초 로그인 할때 호출 되는지 확인이 필요
	 * 2. 채널 변경시에도 호출 되는지 확인 필요.
	 */
	public static void Loggedin(final int playerId, final MapleClient c) {
		final ChannelServer channelServer = c.getChannelServer();
		MapleCharacter player;
		
		final ChracterTransfer transfer = channelServer.getPlayerStorage().getPendingCharacter(playerId);
		if (transfer == null) {
			player = MapleCharacter.loadCharFromDB(playerId, c, true);
		} else {
			player = MapleCharacter.ReconstructChr(transfer, c, true);
		}
		if (player == null) {
			logger.error("필수적인 캐릭터 정보를 로딩 할 수 없습니다. playerId : {}", playerId);
			return;
		}
		c.setPlayer(player);
		c.setAccID(player.getAccountID());
		c.loadAuthData();
		c.getPlayer().setmorphGage(0);

		final int state = c.getLoginState();

		boolean allowLogin = false;

		if (state == AccountStatusType.SERVER_TRANSITION.getValue() || state == AccountStatusType.CHANGE_CHANNEL.getValue()) {
			if (!ChannelServer.isCharacterListConnected(c.loadCharacterNames(), true)) {
				allowLogin = true;
			}
		}
		if (!allowLogin) {
			c.setPlayer(null);
			c.getSession().closeNow();
			logger.debug("not allow login - {} from {} state : {}", c.getAccountName(), c.getSessionIPAddress(), state);
			return;
		}
		c.updateLoginState(AccountStatusType.IN_CHANNEL.getValue(), c.getSessionIPAddress());

		final ChannelServer cserv = ChannelServer.getInstance(c.getChannel());
		cserv.addPlayer(player);
		c.getPlayer().giveCoolDowns(ChannelServer.getCooldownsFromStorage(player.getId()));
		c.getSession().write(MainPacketCreator.HeadTitle(player.HeadTitle()));
		c.getSession().write(MainPacketCreator.getPlayerInfo(player));
		player.getMap().addPlayer(player);

		try {
			player.expirationTask();
		} catch (Exception e) {
			if (!ServerConstants.realese) {
				e.printStackTrace();
			}
		}

		if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -27) != null
				&& player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -28) != null) {
			if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -27).getAndroid() != null) {
				player.setAndroid(player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -27).getAndroid());
			}
		}

		try {
			// Start of expirationTask
			player.expirationTask();
			// Start of buffs
			final List<MapleBuffValueHolder> buffs = ChannelServer.getBuffsFromStorage(player.getId());
			if (buffs != null) {
				player.silentGiveBuffs(buffs);
			}
			c.getPlayer().giveSilentDebuff(ChannelServer.getDiseaseFromStorage(player.getId()));
			// Start of buddylist
			final int buddyIds[] = player.getBuddylist().getBuddyIds();
			WorldBroadcasting.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
			final MaplePlayerIdChannelPair[] onlineBuddies = WorldCommunity.multiBuddyFind(player.getId(), buddyIds);
			for (MaplePlayerIdChannelPair onlineBuddy : onlineBuddies) {
				final BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
				ble.setChannel(onlineBuddy.getChannel());
				player.getBuddylist().put(ble);
			}
			c.getSession().write(MainPacketCreator.updateBuddylist(player.getBuddylist().getBuddies(), 10, 0));
			if (player.getGuildId() > 0) {
				ChannelServer.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
				c.getSession().write(MainPacketCreator.showGuildInfo(player));
				final MapleGuild gs = ChannelServer.getGuild(player.getGuildId());
				if (gs != null) {
					final List<Packet> packetList = ChannelServer.getAllianceInfo(gs.getAllianceId(), true);
					if (packetList != null) {
						for (Packet pack : packetList) {
							if (pack != null) {
								c.getSession().write(pack);
							}
						}
					}
				} else { // guild not found, change guild id
					player.setGuildId(0);
					player.setGuildRank((byte) 5);
					player.setAllianceRank((byte) 5);
					player.saveGuildStatus();
				}
			}
		} catch (Exception e) {
			if (!ServerConstants.realese) {
				e.printStackTrace();
			}
		}
		player.showNote();
		player.updatePartyMemberHP();
		player.updateSkillPacket();
		c.getSession().write(MainPacketCreator.showMaplePoint(player));

		if (GameConstants.isPhantom(player.getJob())) {
			c.getSession().write(MainPacketCreator.cardAmount(c.getPlayer().getCardStack()));
		}

		for (MapleQuestStatus status : player.getStartedQuests()) {
			if (status.hasMobKills()) {
				c.getSession().write(MainPacketCreator.updateQuestMobKills(status));
			}
		}

		if (player.getJob() == 132) { // 다크나이트
			player.checkBerserk();
		}
		if (player.getQuickSlot().getQuickSlot().size() == 8) {
			c.send(MainPacketCreator.getQuickSlot(player.getQuickSlot().getQuickSlot()));
		}

		if (DueyHandler.DueyItemSize(player.getName()) > 0) {
			player.send(DueyHandler.DueyMessage(28));
		}

		/* 컨트롤 패널 접속자 수 추가 */
		if (!ControlUnit.현재접속자.contains(player.getName())) {
			ControlUnit.동접(player.getName());
			ControlUnit.접속자수.setText(String.valueOf((int) (Integer.parseInt(ControlUnit.접속자수.getText()) + 1)));
		}

		/* 제로 젠더 겹칠시 초기화 */
		if ((player.getGender() == 0) && (player.getSecondGender() == 0) && (GameConstants.isZero(player.getJob())
				|| (player.getGender() == 1) && (player.getSecondGender() == 1) && (GameConstants.isZero(player.getJob())))) {
			player.setGender((byte) 1);
			player.setSecondGender((byte) 0);
		}

		/* 제로가 아닐시 젠더 사용안함 처리 */
		if (!GameConstants.isZero(c.getPlayer().getJob())) {
			c.getPlayer().setSecondGender((byte) -1);
		} else {
			c.send(ZeroSkill.Clothes(player.getBetaClothes()));
		}

		/* 소울인챈터 */
		if ((player.isEquippedSoulWeapon()) && (transfer == null)) {
			player.setSoulCount(0);
			c.send(SoulWeaponPacket.giveSoulGauge(player.getSoulCount(), player.getEquippedSoulSkill()));
		}

		/* 최대데미지 해제 */
		if (ServerConstants.UnlockMaxDamage) {
			player.unlockMaxDamage();
		}

		/* 정령의 펜던트 */
		if (player.getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null) {
			player.equipPendantOfSpirit();
		}

		/* 오픈게이트 */
		c.getPlayer().setKeyValue("opengate", null);
		c.getPlayer().setKeyValue("count", null);

		/* 펜던트 슬롯 */
		if (!c.getPlayer().getStat().getJC()) {
			c.getPlayer().getStat().setJC(true);
		}

		/* 피버 타임 */
		if (ServerConstants.feverTime) {
			c.getSession().write(MainPacketCreator.feverTime());
		}

		c.getPlayer().send(UIPacket.detailShowInfo("우리는 한가족 ! " + ServerConstants.serverName + "에 오신걸 환영합니다.", 3));
		c.getSession().write(MainPacketCreator.serverMessage(ServerConstants.serverMessage));
		if (player.getMapId() == ServerConstants.startMap) {
			c.getSession().write(ZeroSkill.NPCTalk(ServerConstants.beginner));
		} else {
			c.getSession().write(ZeroSkill.NPCTalk(ServerConstants.serverNotice));
			c.getSession().write(MainPacketCreator.sendHint(ServerConstants.serverHint, 300, 15));
		}

		if (GameConstants.isXenon(player.getJob())) {
			player.startSurPlus();
		}

		if (GameConstants.isBlaster(player.getJob())) {
			// FIXME giveBulletGauge 메소드가 정의된 부분이 없어서 일단 주석으로 막음. 이후 구현이
			// 가능하다면 수정해야할듯.
			// player.giveBulletGauge(0, false);
		}

		if (GameConstants.isDemonAvenger(player.getJob())) {
			c.send(MainPacketCreator.giveDemonWatk(c.getPlayer().getStat().getHp()));
		}

		if (GameConstants.isAngelicBuster(player.getJob())) {
			for (int i = 0; i < 2; i++) {
				c.send(AngelicBusterSkill.AngelicBusterChangingWait((byte) 1, (i != 0)));
			}
		}

		c.getSession().write(CashPacket.pendantSlot(true));
		player.sendMacros();
		c.getSession().write(MainPacketCreator.getKeymap(player.getKeyLayout()));
		if (GameConstants.isKOC(player.getJob()) && player.getLevel() >= 100) {
			if (player.getSkillLevel(Integer.parseInt(String.valueOf(player.getJob() + "1000"))) <= 0) {
				player.teachSkill(Integer.parseInt(String.valueOf(player.getJob() + "1000")), (byte) 0,
						SkillFactory.getSkill(Integer.parseInt(String.valueOf(player.getJob() + "1000"))).getMaxLevel());
			}
		}
		if (c.getPlayer().getStat().ChDelay == 0) {
			c.getPlayer().getStat().ChDelay = 1;
		} else {
			c.getPlayer().getStat().ChDelay = System.currentTimeMillis();
		}
	}

	public static final void ChangeChannel(final ReadingMaple rh, final MapleClient c, final MapleCharacter chr) {
		if (!chr.isAlive()) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}
		final int channel = rh.readByte();
		if (c.getPlayer() != null) {
			if (c.getPlayer().getStat().ChDelay > 1) {
				if ((System.currentTimeMillis() - c.getPlayer().getStat().ChDelay) < 5500) {
					c.getPlayer().dropMessage(1, "5초뒤 채널을 이동 할 수 있습니다.");
					c.getPlayer().send(MainPacketCreator.resetActions());
					return;
				}
			}
		}
		final ChannelServer toch = ChannelServer.getInstance(channel);

		if (FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit()) || channel == c.getChannel()) {
			c.getSession().closeNow();
			return;
		} else if (toch == null || toch.isShutdown()) {
			c.getSession().write(MainPacketCreator.serverNotice(5, "현재 접근할 수 없습니다."));
			return;
		}
		if (chr.getTrade() != null) {
			MapleUserTrade.cancelTrade(chr.getTrade());
		}

		final IMapleCharacterShop shop = chr.getPlayerShop();
		if (shop != null) {
			shop.removeVisitor(chr);
			if (shop.isOwner(chr)) {
				shop.setOpen(true);
			}
		}

		final ChannelServer ch = ChannelServer.getInstance(c.getChannel());
		if (chr.getMessenger() != null) {
			WorldCommunity.silentLeaveMessenger(chr.getMessenger().getId(), new MapleMultiChatCharacter(chr));
		}
		ChannelServer.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
		ChannelServer.addCooldownsToStorage(chr.getId(), chr.getAllCooldowns());
		ChannelServer.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
		ChannelServer.ChannelChange_Data(new ChracterTransfer(chr), chr.getId(), channel);
		ch.removePlayer(chr);
		c.updateLoginState(AccountStatusType.CHANGE_CHANNEL.getValue(), c.getSessionIPAddress());
		c.getSession().write(MainPacketCreator.getChannelChange(c, ServerConstants.basePorts + (channel)));
		chr.saveToDB(false, false);
		chr.getMap().removePlayer(chr);
		c.setPlayer(null);
	}

	public static void getBuddyHello(ReadingMaple rh, MapleClient c) {
		final int clientid = rh.readInt();
		c.setAccID(clientid);
		rh.skip(13);
		final int playerids = rh.readInt();
		MapleCharacter player = null;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			final MaplePlayerHolder playerStorage = cserv.getPlayerStorage();
			final MapleCharacter chrs = playerStorage.getCharacterById(playerids);
			if (chrs != null) {
				player = chrs;
				break;
			}
		}
		c.setPlayer(player);
		if (BuddyChatServer.ChatClient.containsKey(clientid)) {
			BuddyChatServer.ChatClient.remove(clientid);
		}
		BuddyChatServer.ChatClient.put(clientid, c);
		c.getSession().write(MainPacketCreator.buddyHello(c));
	}

	public static void getGameQuitRequest(ReadingMaple rh, MapleClient c) {
		String account = rh.readMapleAsciiString();
		if (!c.isLoggedIn() && !c.getAccountName().equals(account)) { // hack
			c.getSession().closeNow();
			return;
		}
		c.getSession().write(MainPacketCreator.serverNotice(4, ""));
		c.getSession().write(LoginPacket.getKeyGuardResponse((account) + "," + (c.getPassword(account))));
	}
}
