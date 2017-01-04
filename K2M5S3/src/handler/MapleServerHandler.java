package handler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.SessionFlag;
import client.MapleClient;
import constants.ServerConstants;
import constants.subclasses.ServerType;
import handler.cashshop.CashShopOperation;
import handler.channel.AllianceHandler;
import handler.channel.AngelicBusterHandler;
import handler.channel.AuctionHandler;
import handler.channel.BBSHandler;
import handler.channel.BuddyListHandler;
import handler.channel.ChatHandler;
import handler.channel.GuildHandler;
import handler.channel.HiredMerchantHandler;
import handler.channel.InterServerHandler;
import handler.channel.InventoryHandler;
import handler.channel.ItempotHandler;
import handler.channel.MobHandler;
import handler.channel.NPCHandler;
import handler.channel.PartyHandler;
import handler.channel.PetHandler;
import handler.channel.PhantomHandler;
import handler.channel.PlayerHandler;
import handler.channel.PlayerInteractionHandler;
import handler.channel.PlayersHandler;
import handler.channel.ProfessionHandler;
import handler.channel.StatsHandling;
import handler.channel.SummonHandler;
import handler.channel.ZeroHandler;
import handler.duey.DueyHandler;
import handler.login.CharLoginHandler;
import launch.ChannelServer;
import packet.creators.LoginPacket;
import packet.creators.MainPacketCreator;
import packet.creators.SoulWeaponPacket;
import packet.crypto.MapleCrypto;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import packet.skills.KinesisSkill;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import server.items.EnforceSystem;
import tools.Randomizer;
import tools.StringUtil;

/**
 * 소켓통신 할때 read, write 하는 등의 동작을 이곳에 구현한다.
 * 
 * TODO 로그인, 채널, 캐시샵 등 각각의 서버에서 받는 패킷들을 구분할수가 있다.
 * 하지만 현재 모든 서버에서 이 클래스를 공통으로 사용한다.
 * 이걸 분리하는것에 대해 검토하자.
 */
public class MapleServerHandler extends IoHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(MapleServerHandler.class);
	
	private int channel = 0;
	private ServerType type = null;
	private String clientKey = null;
	
	public MapleServerHandler(final ServerType type, final String clientKey) {
		this.type = type;
		this.clientKey = clientKey;
	}

	public MapleServerHandler(final ServerType type, final int channel, final String clientKey) {
		this.channel = channel;
		this.type = type;
		this.clientKey = clientKey;
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {}
	
	@Override
	public void sessionOpened(final IoSession session) throws Exception {
		final String address = session.getRemoteAddress().toString().split(":")[0];

		if (channel > 0) {
			if (ChannelServer.getInstance(channel).isShutdown()) {
				session.closeNow();
				return;
			}
		}

		final byte ivRecv[] = { (byte) 0x22, (byte) 0x3F, (byte) 0x37, (byte) Randomizer.nextInt(255) };
		final byte ivSend[] = { (byte) 0xC9, (byte) 0x3A, (byte) 0x27, (byte) Randomizer.nextInt(255) };

		final MapleClient client = new MapleClient(
				new MapleCrypto(ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION)),
				new MapleCrypto(ivRecv, ServerConstants.MAPLE_VERSION),
				session);
		client.setChannel(channel);

		if (type.equals(ServerType.LOGIN)) {
			session.write(LoginPacket.initializeConnection(ServerConstants.MAPLE_VERSION, ivSend, ivRecv, false));
		} else {
			session.write(LoginPacket.initializeConnection(ServerConstants.MAPLE_VERSION, ivSend, ivRecv, true));
		}

		session.setAttribute(clientKey, client);
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);

		switch (type) {
		case LOGIN:
			logger.info("[알림] {} 에서 로그인 서버로 연결을 시도했습니다.", address);
			break;
		case CHANNEL:
			logger.info("[알림] {} 에서 채널 서버로 연결을 시도했습니다.", address);
			break;
		case CASHSHOP:
			logger.info("[알림] {} 에서 캐시샵 서버로 연결을 시도했습니다.", address);
			break;
		default:
		}
	}

	@Override
	public void sessionClosed(final IoSession session) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(clientKey);
		if (client != null) {
			try {
				if (client.getIdleTask() != null) {
					client.getIdleTask().cancel(true);
					client.setIdleTask(null);
				}
				
				//값이 "N" 일 경우에는 사용자 정보를 저장하지 않도록 한다.
				//예를 들어 밴 처리 대상의 경우 현재 캐릭터 정보를 굳이 저장해 줄 필요는 없다.
				//TODO 해당 기능이 잘 되는지 테스트 필요.
				
				//TODO 사용자가 클라 종료시 세션이 끊어지는데... 로그인, 채널 서버 둘다 이 클래스를 사용한다.
				//그래서 접속이 끊어 질 경우 sessionClosed() 가 여러번 호출될수 있다. 채널서버와 연결이 종료 될때만 저장되도록 수정 필요.
				if( !"N".equals(session.getAttribute(SessionFlag.KEY_CHAR_SAVE)) ) {
					logger.debug("{} - 세션 종료시 캐릭터 정보 저장!", type);
					client.disconnect(true, type == ServerType.CASHSHOP ? true : false);
				}
			} finally {
				session.closeNow();
				session.removeAttribute(clientKey);
			}
		}
	}

	@Override
	public void messageReceived(final IoSession session, final Object message) throws Exception {
		final ReadingMaple rh = new ReadingMaple(new ByteStream((byte[]) message));
		final short headerValue = rh.readShort();
		final RecvPacketOpcode opcode = RecvPacketOpcode.getOpcode(headerValue);
		
		if (opcode != null) {
			final MapleClient client = (MapleClient) session.getAttribute(clientKey);
			try {
				handlePacket(opcode, rh, client, type);
			} catch (Exception e) {
				client.getSession().write(MainPacketCreator.resetActions());
				logger.error("recv handle error {}", e);
			}
		}
	}

	@Override
	public void messageSent(final IoSession session, final Object message) throws Exception {
		final Runnable r = ((Packet) message).getOnSend();
		if (r != null) {
			r.run();
		}
	}
	
	@Override
	public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {}

	@Override
	public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
		logger.debug("{}", cause);
	}

	public static final void handlePacket(final RecvPacketOpcode header, final ReadingMaple rh, final MapleClient client, final ServerType type) throws InterruptedException {
		
		switch (header) {
		case SERVER_MESSAGE_RESPONSE:
			//TODO 클라이언트에서 팝업창 띄울때 해당 패킷이 전달되는걸로 추측.
			//확인, 취소, 기타 등등의 버튼들을 눌렀을떄 발생하며. 패킷값들을 보면
			//각 팝업창에 해당하는 고유값이 존재하는것으로 보인다.
			//한번 조사해보면 쓸만한걸 건질수 있을지도... 급한건 아니다. 나중에...
			break;
		case CLIENT_HELLO:
			byte pLocale = rh.readByte();
			short pVersion = rh.readShort();
			short pString = rh.readShort();
			if (pLocale != ServerConstants.CHECK && pVersion != ServerConstants.MAPLE_VERSION && pString != ServerConstants.SUB_VERSION) {
				logger.debug("Client Checksum Failed: {}", client.getSessionIPAddress());
				SessionFlag.forceDisconnect(client.getSession());
			}
			break;
		case BUDDY_HELLO:
			InterServerHandler.getBuddyHello(rh, client);
			break;
		case BUDDY_PING:
			client.getSession().write(LoginPacket.BuddyPing());
			break;
		case BUDDY_CHAT:
			ChatHandler.BuddyChat(rh, client);
			break;
		case GUILD_CHAT:
			ChatHandler.GuildChat(rh, client);
			break;
		case CLIENT_CONNECTED_R:
			CharLoginHandler.getXignCodeResponse(rh.readByte() == 0, client);
			break;
		case SET_BURNING_CHAR_R:
			CharLoginHandler.setBurningCharacter(rh, client);
			break;
		case CLIENT_QUIT:
			//클라에서 하단에 시스템 메뉴 -> 게임종료 버튼 클릭시 호출.
			InterServerHandler.getGameQuitRequest(rh, client);
			break;
		case LOGIN_REQUEST:
			logger.warn("LOGIN_REQUEST 패킷 발견!");
			//CharLoginHandler.getLoginRequest(rh, c);
			break;
		case REDISPLAY_CHANNEL:
			//TODO RECV REDISPLAY_CHANNEL 패킷이 언제 호출되는지 확인 필요.
			logger.warn("REDISPLAY_CHANNEL 패킷 발견!");
			CharLoginHandler.getDisplayChannel(false, client);
			break;
		case ENTER_CREATE_CHAR: //캐릭터 생성을 완료 했을때.
			logger.warn("ENTER_CREATE_CHAR 패킷 발견!");
			CharLoginHandler.getIPRequest(rh, client);
			break;
		case SECONDPW_RESULT_R: //2차 비밀번호를 사용하는지 여부를 체크한다.
			CharLoginHandler.getSPCheck(rh, client);
			break;
		case SESSION_CHECK_R:
			CharLoginHandler.getSessionCheck(rh, client);
			break;
		case NEW_CONNECTION:
			logger.warn("NEW_CONNECTION 패킷 발견!");
			// CharLoginHandler.newConnection(c);
			break;
		case PONG:
			client.pongReceived();
			break;
		case LOGIN_PASSWORD:
			CharLoginHandler.login(rh, client);
			break;
		case CHARLIST_REQUEST:
			CharLoginHandler.CharlistRequest(rh, client);
			break;
		case CHARACTER_CARD:
			CharLoginHandler.updateCharCard(rh, client);
			break;
		case CHECK_CHAR_NAME:
			CharLoginHandler.CheckCharName(rh.readMapleAsciiString(), client);
			break;
		case CREATE_CHAR:
			CharLoginHandler.CreateChar(rh, client);
			break;
		case CLIENT_ERROR: // might not be correct
			if (rh.available() >= 6L) {
				rh.skip(6);
				short badPacketSize = rh.readShort();
				rh.skip(4);
				int pHeader = rh.readShort();
				String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
				pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
				String op = SendPacketOpcode.getOpcodeName(pHeader);
				String from = "";
				if (client.getPlayer() != null) {
					from = new StringBuilder().append("Chr: ").append(client.getPlayer().getName()).append(" LVL(")
							.append(client.getPlayer().getLevel()).append(") job: ").append(client.getPlayer().getJob())
							.append(" MapID: ").append(client.getPlayer().getMapId()).toString();
				}

				logger.debug("{}\n\nSendOP(-38):{} [{}] ({}){}\r\n\r\n", from, op, pHeaderStr, badPacketSize - 4, rh.toString(false));
			}
			break;
		case DELETE_CHAR:
			CharLoginHandler.DeleteChar(rh, client);
			break;
		case CHAR_SELECT:
			CharLoginHandler.Character_WithSecondPassword(rh, client);
			break;
		case AUTH_LOGIN_WITH_SPW:
			CharLoginHandler.checkSecondPassword(rh, client);
			break;
		case REG_SECOND_PASSWORD:
			CharLoginHandler.registerSecondPassword(rh, client);
			break;
		case ONLY_REG_SECOND_PASSWORD:
			CharLoginHandler.onlyRegisterSecondPassword(rh, client);
			break;
		case CHANGE_CHANNEL:
			InterServerHandler.ChangeChannel(rh, client, client.getPlayer());
			break;
		case PLAYER_LOGGEDIN:
			rh.skip(4);
			final int playerid = rh.readInt();
			if (type == ServerType.CHANNEL) {
				InterServerHandler.Loggedin(playerid, client);
			} else {
				logger.info("PLAYER_LOGGEDIN 패킷 EnterCS() 호출");
				CashShopOperation.EnterCS(playerid, client);
			}
			break;
		case ENTER_CASH_SHOP:
			logger.info("ENTER_CASH_SHOP 패킷 호출");
			InterServerHandler.EnterCS(client, client.getPlayer(), true);
			break;
		case ENTER_MTS:
			InterServerHandler.EnterMTS(client);
			break;
		case MOVE_PLAYER:
			PlayerHandler.MovePlayer(rh, client, client.getPlayer());
			break;
		case MOVE_ANDROID:
			PlayerHandler.MoveAndroid(rh, client, client.getPlayer());
			break;
		case CHAR_INFO_REQUEST:
			rh.skip(4);
			PlayerHandler.CharInfoRequest(rh.readInt(), client, client.getPlayer());
			break;
		case ROOM_CHANGE:
			PlayerHandler.RoomChange(rh, client, client.getPlayer());
			break;
		case DF_COMBO:
			PlayerHandler.absorbingDF(rh, client);
			break;
		case WILL_OF_SOWRD_COMBO:
			PlayerHandler.absorbingSword(rh, client.getPlayer());
			break;
		case PSYCHIC_GREP_R:
			KinesisSkill.PsychicGrep(rh, client);
			break;
		case PSYCHIC_ATTACK_R:
			KinesisSkill.PsychicAttack(rh, client);
			break;
		case PSYCHIC_ULTIMATE_R:
			KinesisSkill.PsychicUltimateDamager(rh, client);
			break;
		case PSYCHIC_DAMAGE_R:
			KinesisSkill.PsychicDamage(rh.readInt(), client);
			break;
		case CANCEL_PSYCHIC_GREP_R:
			rh.skip(8);
			KinesisSkill.CancelPsychicGrep(rh, client);
			break;
		case PASSIVE_ENERGY:
		case MAGNETIC_DAMAGE:
		case CLOSE_RANGE_ATTACK:
			PlayerHandler.closeRangeAttack(rh, client, client.getPlayer(), header != header.CLOSE_RANGE_ATTACK ? true : false);
			break;
		case RANGED_ATTACK:
			PlayerHandler.rangedAttack(rh, client, client.getPlayer());
			break;
		case MAGIC_ATTACK:
			PlayerHandler.MagicDamage(rh, client, client.getPlayer(), false);
			break;
		case SPECIAL_SKILL:
			PlayerHandler.SpecialSkill(rh, client, client.getPlayer());
			break;
		case FACE_EXPRESSION:
			PlayerHandler.ChangeEmotion(rh.readInt(), client.getPlayer());
			break;
		case ANDROID_FACE_EXPRESSION:
			PlayerHandler.ChangeEmotionAndroid(rh.readInt(), client.getPlayer());
			break;
		case TAKE_DAMAGE:
			PlayerHandler.TakeDamage(rh, client, client.getPlayer());
			break;
		case HEAL_OVER_TIME:
		case HEAL_OVER_TIME_FROM_POT:
			PlayerHandler.Heal(rh, client.getPlayer());
			break;
		case CANCEL_BUFF:
			PlayerHandler.CancelBuffHandler(rh.readInt(), client.getPlayer(), rh);
			break;
		case CANCEL_ITEM_EFFECT:
			PlayerHandler.CancelItemEffect(rh.readInt(), client.getPlayer());
			break;
		case USE_CHAIR:
			PlayerHandler.UseChair(rh.readInt(), client, client.getPlayer(), rh);
			break;
		case CANCEL_CHAIR:
			PlayerHandler.CancelChair(rh.readShort(), client, client.getPlayer());
			break;
		case USE_ITEMEFFECT:
			PlayerHandler.UseItemEffect(rh.readInt(), client, client.getPlayer());
			break;
		case MAKER_SKILL:
			PlayerHandler.makerSkill(rh, client);
			break;
		case SKILL_EFFECT:
			PlayerHandler.SkillEffect(rh, client.getPlayer());
			break;
		case MESO_DROP:
			rh.skip(4);
			PlayerHandler.DropMeso(rh.readInt(), client.getPlayer());
			break;
		case WHEEL_OF_FORTUNE:
			PlayerHandler.WheelOfFortuneEffect(rh.readInt(), client.getPlayer());
			break;
		case CHANGE_KEYMAP:
			PlayerHandler.ChangeKeymap(rh, client.getPlayer());
			break;
		case QUICK_SLOT:
			PlayerHandler.ChangeQuickSlot(rh, client.getPlayer());
			break;
		case CHANGE_MAP:
			if (type == ServerType.CHANNEL) {
				PlayerHandler.ChangeMap(rh, client, client.getPlayer());
			} else {
				CashShopOperation.LeaveCS(rh, client, client.getPlayer());
			}
			break;
		case CHANGE_MAP_SPECIAL:
			rh.skip(1);
			PlayerHandler.ChangeMapSpecial(rh.readMapleAsciiString(), client, client.getPlayer());
			break;
		case USE_INNER_PORTAL:
			rh.skip(1);
			PlayerHandler.InnerPortal(rh, client, client.getPlayer());
			break;
		case TROCK_ADD_MAP:
			PlayerHandler.TrockAddMap(rh, client, client.getPlayer());
			break;
		case ARAN_GAIN_COMBO:
			PlayerHandler.AranGainCombo(client, client.getPlayer());
			break;
		case ARAN_LOSE_COMBO:
			PlayerHandler.AranLoseCombo(client, client.getPlayer());
			break;
		case BLESS_OF_DARKNES:
			PlayerHandler.BlessOfDarkness(client.getPlayer());
			break;
		case SKILL_MACRO:
			PlayerHandler.ChangeSkillMacro(rh, client.getPlayer());
			break;
		case SUB_SUMMON_ACTION:
			PlayerHandler.subSummonAction(rh, client);
			break;
		case GIVE_FAME:
			PlayersHandler.GiveFame(rh, client, client.getPlayer());
			break;
		case TRANSFORM_PLAYER:
			PlayersHandler.TransformPlayer(rh, client, client.getPlayer());
			break;
		case NOTE_ACTION:
			PlayersHandler.Note(rh, client.getPlayer());
			break;
		case USE_DOOR:
			PlayersHandler.UseDoor(rh, client.getPlayer());
			break;
		case USE_MECH_DOOR:
			PlayersHandler.UseMechDoor(rh, client.getPlayer());
			break;
		case DAMAGE_REACTOR:
			PlayersHandler.HitReactor(rh, client);
			break;
		case CLOSE_CHALKBOARD:
			client.getPlayer().setChalkboard(null);
			break;
		case ITEM_SORT:
			InventoryHandler.ItemSort(rh, client);
			break;
		case DRESS_UP:
			AngelicBusterHandler.DressUpRequest(client.getPlayer(), rh);
			break;
		case ITEM_MOVE:
			InventoryHandler.ItemMove(rh, client);
			break;
		case ITEM_PICKUP:
			InventoryHandler.Pickup_Player(rh, client, client.getPlayer());
			break;
		case ITEM_GATHER:
			InventoryHandler.ItemGather(rh, client);
			break;
		case USE_CASH_ITEM:
			InventoryHandler.UseCashItem(rh, client);
			break;
		case RUNE_TOUCH:
			PlayersHandler.TouchRune(rh, client.getPlayer());
			break;
		case RUNE_USE:
			PlayersHandler.UseRune(rh, client.getPlayer());
			break;
		case USE_EDITIONAL_SCROLL:
			InventoryHandler.EditionalScroll(rh, client);
			break;
		case USE_PET_LOOT:
			InventoryHandler.UsePetLoot(rh, client);
			break;
		case USE_ITEM:
			InventoryHandler.UseItem(rh, client, client.getPlayer());
			break;
		case USE_SCRIPTED_NPC_ITEM:
			InventoryHandler.UseScriptedNPCItem(rh, client, client.getPlayer());
			break;
		case USE_RETURN_SCROLL:
			InventoryHandler.UseReturnScroll(rh, client, client.getPlayer());
			break;
		case USE_STAMP:
			InventoryHandler.UseStamp(rh, client);
			break;
		case USE_SOUL_ENCHANTER:
			InventoryHandler.UseSoulEnchanter(rh, client, client.getPlayer());
			break;
		case USE_SOUL_SCROLL:
			InventoryHandler.UseSoulScroll(rh, client, client.getPlayer());
			break;
		case SHOW_SOULEFFECT_R:
			client.getSession().write(SoulWeaponPacket.showSoulEffect(client.getPlayer(), rh.readByte()));
			break;
		case USE_EDITIONAL_STAMP:
			InventoryHandler.UseEditionalStamp(rh, client);
			break;
		case USE_MAGNIFY_GLASS:
			rh.skip(4);
			InventoryHandler.MagnifyingGlass(client, (byte) rh.readShort(), (byte) rh.readShort());
			break;
		case USE_UPGRADE_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), client, client.getPlayer());
			break;
		case USE_SPECIAL_SCROLL:
			rh.skip(4);
			InventoryHandler.UseSpecialScroll(rh, client.getPlayer());
			break;
		case USE_POTENTIAL_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), client, client.getPlayer());
			break;
		case USE_EQUIP_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), client, client.getPlayer());
			break;
		case USE_REBIRTH_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), client, client.getPlayer());
			break;
		case USE_MANYSET_CUBE:
			rh.skip(4);
			InventoryHandler.UseManySetCube(client, rh);
			break;
		case USE_SILVER_KARMA:
			InventoryHandler.UseKarma(rh, client);
			break;
		case USE_SUMMON_BAG:
			InventoryHandler.UseSummonBag(rh, client, client.getPlayer());
			break;
		case USE_SKILL_BOOK:
			InventoryHandler.UseSkillBook(rh, client, client.getPlayer());
			break;
		case USE_CATCH_ITEM:
			InventoryHandler.UseCatchItem(rh, client, client.getPlayer());
			break;
		case REWARD_ITEM:
			InventoryHandler.UseRewardItem(rh, client, client.getPlayer());
			break;
		case HYPNOTIZE_DMG:
			MobHandler.HypnotizeDmg(rh, client.getPlayer());
			break;
		case MOVE_LIFE:
			MobHandler.MoveMonster(rh, client, client.getPlayer());
			break;
		case AUTO_AGGRO:
			MobHandler.AutoAggro(rh.readInt(), client.getPlayer());
			break;
		case FRIENDLY_DAMAGE:
			MobHandler.FriendlyDamage(rh, client.getPlayer());
			break;
		case EQUIP_UPGRADE_SYSTEM:
			EnforceSystem.AddItemRecv(rh, client);
			break;
		case MONSTER_BOMB:
			MobHandler.MonsterBomb(rh.readInt(), client.getPlayer());
			break;
		case NPC_SHOP:
			NPCHandler.NPCShop(rh, client, client.getPlayer());
			break;
		case NPC_TALK:
			NPCHandler.NPCTalk(rh, client, client.getPlayer());
			break;
		case NPC_TALK_MORE:
			NPCHandler.NPCMoreTalk(rh, client);
			break;
		case NPC_ACTION:
			NPCHandler.NPCAnimation(rh, client);
			break;
		case QUEST_ACTION:
			NPCHandler.QuestAction(rh, client, client.getPlayer());
			break;
		case STORAGE:
			NPCHandler.Storage(rh, client, client.getPlayer());
			break;
		case GENERAL_CHAT:
			rh.skip(4);
			ChatHandler.GeneralChat(rh.readMapleAsciiString(), rh.readByte(), client, client.getPlayer());
			break;
		case PARTY_CHAT:
			ChatHandler.Others(rh, client, client.getPlayer());
			break;
		case WHISPER:
			ChatHandler.Whisper_Find(rh, client);
			break;
		case MESSENGER:
			ChatHandler.Messenger(rh, client);
			break;
		case AUTO_ASSIGN_AP:
			rh.skip(4);
			StatsHandling.AutoAssignAP(rh, client, client.getPlayer());
			break;
		case DISTRIBUTE_AP:
			rh.skip(4);
			StatsHandling.DistributeAP(rh, client, client.getPlayer());
			break;
		case DISTRIBUTE_HYPER_SP:
			rh.skip(4);
			StatsHandling.DistributeHyperSp(rh, rh.readInt(), client.getPlayer());
			break;
		case DISTRIBUTE_SP:
			rh.skip(4);
			StatsHandling.DistributeSP(rh, rh.readInt(), client, client.getPlayer());
			break;
		case PLAYER_INTERACTION:
			PlayerInteractionHandler.PlayerInteraction(rh, client, client.getPlayer());
			break;
		case GUILD_OPERATION:
			GuildHandler.GuildOpertion(rh, client);
			break;
		case DENY_GUILD_REQUEST:
			GuildHandler.DenyGuildRequest(rh, client);
			break;
		case ALLIANCE_OPERATION:
			AllianceHandler.AllianceOperatopn(rh, client, false);
			break;
		case DENY_ALLIANCE_REQUEST:
			AllianceHandler.AllianceOperatopn(rh, client, true);
			break;
		case BBS_OPERATION:
			BBSHandler.BBSOperatopn(rh, client);
			break;
		case PARTY_OPERATION:
			PartyHandler.PartyOperatopn(rh, client);
			break;
		case DENY_PARTY_REQUEST:
			PartyHandler.DenyPartyRequest(rh, client);
			break;
		case BUDDYLIST_MODIFY:
			BuddyListHandler.BuddyOperation(rh, client);
			break;
		case BUDDYLIST_UPDATE_R:
			BuddyListHandler.BuddyUpdate(rh, client);
			break;
		case BUY_CS_ITEM:
			CashShopOperation.BuyCashItem(rh, client, client.getPlayer());
			break;
		case COUPON_CODE:
			rh.skip(2); // 선물받을 대상. (MapleAsciiString)
			CashShopOperation.CouponCode(rh.readMapleAsciiString(), client);
			break;
		case CS_UPDATE:
			CashShopOperation.CSUpdate(rh, client, client.getPlayer());
			break;
		case DAMAGE_SUMMON:
			rh.skip(4);
			SummonHandler.DamageSummon(rh, client.getPlayer());
			break;
		case MOVE_SUMMON:
			SummonHandler.MoveSummon(rh, client.getPlayer());
			break;
		case SUMMON_ATTACK:
			SummonHandler.SummonAttack(rh, client, client.getPlayer());
			break;
		case SPAWN_PET:
			PetHandler.SpawnPet(rh, client, client.getPlayer());
			break;
		case REGISTER_PET_BUFF:
			PetHandler.RegisterPetBuff(rh, client.getPlayer());
			break;
		case MOVE_PET:
			PetHandler.MovePet(rh, client.getPlayer());
			break;
		case PET_CHAT:
			PetHandler.PetChat(rh, client.getPlayer());
			break;
		case PET_COMMAND:
			PetHandler.PetCommand(rh, client, client.getPlayer());
			break;
		case PET_FOOD:
			PetHandler.PetFood(rh, client, client.getPlayer());
			break;
		case PET_LOOT:
			InventoryHandler.Pickup_Pet(rh, client, client.getPlayer());
			break;
		case PET_AUTO_POT:
			PetHandler.Pet_AutoPotion(rh, client, client.getPlayer());
			break;
		case USE_HIRED_MERCHANT:
			HiredMerchantHandler.UseHiredMerchant(rh, client);
			break;
		case MERCH_ITEM_STORE:
			HiredMerchantHandler.MerchantItemStore(rh, client);
			break;
		case MOVE_DRAGON:
			SummonHandler.MoveDragon(rh, client.getPlayer());
			break;
		case USE_MAGNIFYING_GLASS:
			InventoryHandler.MagnifyingGlass(client, (byte) rh.readShort(), (byte) rh.readShort());
			break;
		case GOLDEN_HAMMER:
			InventoryHandler.UseGoldenHammer(rh, client);
			break;
		case HAMMER_EFFECT:
			InventoryHandler.HammerEffect(rh, client);
			break;
		case EQUIPPED_SKILL:
			PhantomHandler.equippedSkill(rh, client);
			break;
		case STEEL_SKILL_CHECK:
			PhantomHandler.steelSkillCheck(rh, client);
			break;
		case STEEL_SKILL:
			PhantomHandler.steelSkill(rh, client);
			break;
		case SUB_SUMMON:
			SummonHandler.subThingSummon(rh, client, client.getPlayer());
			break;
		case REMOVE_SUMMON:
			SummonHandler.removeSummon(rh, client);
			break;
		case HEAD_TITLE:
			InventoryHandler.headTitle(rh, client);
			break;
		case START_GATHER:
			ProfessionHandler.startGathering(rh, client);
			break;
		case END_GATHER:
			InventoryHandler.ItemGather(rh, client);
			break;
		case ITEMPOT_PUT:
			ItempotHandler.putItempot(rh, client);
			break;
		case ITEMPOT_REMOVE:
			ItempotHandler.removeItempot(rh, client);
			break;
		case ITEMPOT_FEED:
			ItempotHandler.feedItempot(rh, client);
			break;
		case ITEMPOT_CURE:
			ItempotHandler.cureItempot(rh, client);
			break;
		case PROFESSIONINFO_REQUEST:
			ProfessionHandler.getProfessionInfo(rh, client);
			break;
		case PROFESSION_MAKE_EFFECT:
			ProfessionHandler.professionMakeEffect(rh, client);
			break;
		case PROFESSION_MAKE_SOMETHING:
			ProfessionHandler.professionMakeTime(rh, client);
			break;
		case PROFESSION_MAKE:
			ProfessionHandler.professionMake(rh, client);
			break;
		case SPAWN_EXTRACTOR:
			ProfessionHandler.spawnExtractor(rh, client);
			break;
		case USE_RECIPE:
			ProfessionHandler.useRecipe(rh, client);
			break;
		case EXPEDITION_OPERATION:
			PartyHandler.processExpeditionRequest(rh, client);
			break;
		case AGI_BUFF:
			PlayerHandler.Agi_Buff(rh, client);
			break;
		case USE_BAG:
			ProfessionHandler.useBag(rh, client);
			break;
		case MOVE_BAG:
			ProfessionHandler.MoveBag(rh, client);
			break;
		case SWITCH_BAG:
			ProfessionHandler.SwitchBag(rh, client);
			break;
		case HYPER_RECV:
			//TODO [패킷] 실제 패킷의 길이와 헤더값이 일치 하지 않는다. buffer : 16 < packet : 17
			PlayerHandler.getHyperSkill(rh, client);
			break;
		case FOLLOW_REQUEST:
			PlayersHandler.FollowRequest(rh, client);
			break;
		case AUTO_FOLLOW_REPLY:
		case FOLLOW_REPLY:
			PlayersHandler.FollowReply(rh, client);
			break;
		case WARP_TO_STARPLANET:
			PlayerHandler.warpToStarplanet(rh.readByte(), rh, client.getPlayer());
			break;
		case MAPLE_GUIDE:
		case MAPLE_CONTENT_MAP:
			PlayerHandler.MapleGuide(rh, rh.readShort(), client.getPlayer());
			break;
		case RETRACE_MECH:
			PlayerHandler.CancelBuffHandler(rh.readInt(), client.getPlayer(), rh);
			break;
		case SET_FREE_JOB:
			PlayerHandler.SetFreeJob(rh, client.getPlayer());
			break;
		case MAPLE_CHAT:
			PlayerHandler.MapleChat(rh, client.getPlayer());
			break;
		case ORBITAL_FLAME:
			PlayerHandler.OrbitalFlame(rh, client);
			break;
		case DMG_FLAME:
			PlayerHandler.MagicDamage(rh, client, client.getPlayer(), true);
			break;
		case DUEY_HANDLER:
			DueyHandler.DueyHandler(rh, client.getPlayer());
			break;
		case ARROW_FLATTER_ACTION:
			PlayerHandler.ArrowFlatterAction(rh, client.getPlayer());
			break;
		case INNER_CHANGE:
			PlayerHandler.ChangeInner(rh, client);
			break;
		case STAR_PLANET_RANK:
			PlayerHandler.getStarPlanetRank(rh, client.getPlayer());
			break;
		case ZERO_WEAPONINFO:
			ZeroHandler.ZeroWeaponInfo(rh, client);
			break;
		case ZERO_UPGRADE:
			ZeroHandler.ZeroWeaponLevelUp(rh, client);
			break;
		case ZERO_SHOCKWAVE:
			ZeroHandler.ZeroShockWave(rh, client);
			break;
		case ZERO_CHAT:
			ZeroHandler.ZeroChat(rh, client, "녀석, 사실 너를 꽤나 좋아하는 것 같아. 어떻게 아느냐고 ? 원래 우리는 하나였으니까 말하지 않아도 다 알지.");
			break;
		case ZERO_TAG:
			ZeroHandler.ZeroTag(rh, client);
			break;
		case ZERO_OPEN:
			ZeroHandler.ZeroOpen(rh, client);
			break;
		case ZERO_CLOTHES:
			ZeroHandler.ZeroClothes(rh.readInt(), rh.readByte(), client);
			break;
		case ZERO_SCROLL:
			ZeroHandler.ZeroScroll(rh, client);
			break;
		case ZERO_SCROLL_START:
			ZeroHandler.ZeroScrollStart(rh, client.getPlayer(), client);
			break;
		case GAME_END: //클라이언트 강제(?) 종료 할 경우
			client.getPlayer().send(MainPacketCreator.GameEnd());
			break;
		case STARDUST:
			PlayerHandler.Stardust(rh, client);
			break;
		case HOLLY:
			rh.skip(5);
			PlayerHandler.Holly(rh, client.getPlayer());
			break;
		case AUCTION_R:
			AuctionHandler.Handle(rh, client, rh.readByte());
			break;
		case MIST_SKILL:
			PlayerHandler.mistSkill(rh, client.getPlayer());
			break;
		case COMBAT_ANALYZE:
			client.getSession().write(MainPacketCreator.getCombatAnalyze(rh.readByte()));
			break;
		default:
			logger.warn("[UNHANDLED] Recv [{}] founcd", header.toString());
			break;
		}
	}
}
