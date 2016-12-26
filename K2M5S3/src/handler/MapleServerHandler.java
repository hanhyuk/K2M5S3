package handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import client.MapleClient;
import constants.ServerConstants;
import constants.subclasses.ServerType;
import handler.cashshop.CashShopOperation;
import handler.channel.AllianceHandler;
import handler.channel.AngelicBusterHandler;
import handler.channel.AntiHackHandler;
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
import packet.crypto.MapleDecoder;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import packet.skills.KinesisSkill;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import server.items.EnforceSystem;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;

/**
 * 소켓통신 할때 read, write 하는 등의 동작을 이곳에 구현한다.
 */
public class MapleServerHandler extends IoHandlerAdapter {

	private int channel = 0;
	private ServerType type = null;
	
	private final List<String> blockedIpList = new ArrayList<String>();
	private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();

	public MapleServerHandler(final ServerType type) {
		this.type = type;
	}

	public MapleServerHandler(final ServerType type, final int channel) {
		this.channel = channel;
		this.type = type;
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("MapleServerHandler");
	}
	
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

		MapleDecoder.DecoderState decoderState = new MapleDecoder.DecoderState();
		session.setAttribute(MapleDecoder.DECODER_STATE_KEY, decoderState);

		if (type.equals(ServerType.LOGIN)) {
			session.write(LoginPacket.initializeConnection(ServerConstants.MAPLE_VERSION, ivSend, ivRecv, false));
		} else {
			session.write(LoginPacket.initializeConnection(ServerConstants.MAPLE_VERSION, ivSend, ivRecv, true));
		}

		session.setAttribute(MapleClient.CLIENT_KEY, client);
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);

		switch (type) {
		case LOGIN:
			System.out.println("[알림] " + address + " 에서 로그인 서버로 연결을 시도했습니다.");
			break;
		case CHANNEL:
			System.out.println("[알림] " + address + " 에서 채널 서버로 연결을 시도했습니다.");
			break;
		case CASHSHOP:
			System.out.println("[알림] " + address + " 에서 캐시샵 서버로 연결을 시도했습니다.");
			break;
		default:
		}
	}

	@Override
	public void sessionClosed(final IoSession session) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
		if (client != null) {
			try {
				if (client.getIdleTask() != null) {
					client.getIdleTask().cancel(true);
					client.setIdleTask(null);
				}
				client.disconnect(true, type == ServerType.CASHSHOP ? true : false);
			} finally {
				session.closeNow();
				session.removeAttribute(MapleClient.CLIENT_KEY);
			}
		}
	}

	@Override
	public void messageReceived(final IoSession session, final Object message) throws Exception {
		final ReadingMaple rh = new ReadingMaple(new ByteStream((byte[]) message));
		final short header_num = rh.readShort();
		
		if (header_num != RecvPacketOpcode.MOVE_LIFE.getValue() && header_num != RecvPacketOpcode.MOVE_PLAYER.getValue()) {
			if (ServerConstants.showPackets) {
				final StringBuilder sb = new StringBuilder("[" + RecvPacketOpcode.getOpcodeName(header_num) + "] : ");
				sb.append(HexTool.toString((byte[]) message)).append("\n")
						.append(HexTool.toStringFromAscii((byte[]) message)).append("\n\n");
				System.out.println(sb.toString());
			}
		}
		RecvPacketOpcode recv = RecvPacketOpcode.getRecvOpcodes().get(header_num);
		if (recv != null) {
			final MapleClient c = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
			try {
				handlePacket(recv, rh, c, type);
			} catch (Exception e) {
				if (!ServerConstants.realese) {
					e.printStackTrace();
				}
				c.getSession().write(MainPacketCreator.resetActions());
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
		if (!ServerConstants.realese) {
			cause.printStackTrace();
		}
	}

	public static final void handlePacket(final RecvPacketOpcode header, final ReadingMaple rh, final MapleClient c,
			final ServerType type) throws InterruptedException {
		switch (header) {
		case CLIENT_HELLO:
			byte pLocale = rh.readByte();
			short pVersion = rh.readShort();
			short pString = rh.readShort();
			if (pLocale != ServerConstants.check && pVersion != ServerConstants.MAPLE_VERSION
					&& pString != ServerConstants.subVersion) {
				System.err.println("Client Checksum Failed: " + c.getSessionIPAddress());
				c.getSession().close(true);
			}
			break;
		case BUDDY_HELLO:
			InterServerHandler.getBuddyHello(rh, c);
			break;
		case BUDDY_PING:
			c.getSession().write(LoginPacket.BuddyPing());
			break;
		case BUDDY_CHAT:
			ChatHandler.BuddyChat(rh, c);
			break;
		case GUILD_CHAT:
			ChatHandler.GuildChat(rh, c);
			break;
		case CLIENT_CONNECTED_R:
			CharLoginHandler.getXignCodeResponse(rh.readByte() == 0, c);
			break;
		case SET_BURNING_CHAR_R:
			CharLoginHandler.setBurningCharacter(rh, c);
			break;
		case CLIENT_QUIT:
			InterServerHandler.getGameQuitRequest(rh, c);
			break;
		case LOGIN_REQUEST:
			CharLoginHandler.getLoginRequest(rh, c);
			break;
		case REDISPLAY_CHANNEL:
			CharLoginHandler.getDisplayChannel(false, c);
			break;
		case ENTER_CREATE_CHAR:
			CharLoginHandler.getIPRequest(rh, c);
			break;
		case SECONDPW_RESULT_R:
			CharLoginHandler.getSPCheck(rh, c);
			break;
		case SESSION_CHECK_R:
			CharLoginHandler.getSessionCheck(rh, c);
			break;
		case NEW_CONNECTION:
			System.out.println("----------- 경고 ---------");
			System.out.println("!NEW_CONNECTION 패킷 발견!");
			System.out.println("--------------------------");
			// CharLoginHandler.newConnection(c);
			break;
		case PONG:
			c.pongReceived();
			break;
		case LOGIN_PASSWORD:
			CharLoginHandler.login(rh, c);
			break;
		case CHARLIST_REQUEST:
			CharLoginHandler.CharlistRequest(rh, c);
			break;
		case CHARACTER_CARD:
			CharLoginHandler.updateCharCard(rh, c);
			break;
		case CHECK_CHAR_NAME:
			CharLoginHandler.CheckCharName(rh.readMapleAsciiString(), c);
			break;
		case CREATE_CHAR:
			CharLoginHandler.CreateChar(rh, c);
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
				if (c.getPlayer() != null) {
					from = new StringBuilder().append("Chr: ").append(c.getPlayer().getName()).append(" LVL(")
							.append(c.getPlayer().getLevel()).append(") job: ").append(c.getPlayer().getJob())
							.append(" MapID: ").append(c.getPlayer().getMapId()).toString();
				}

				String Recv = new StringBuilder().append(from).append("\r\n").append("SendOP(-38): ").append(op)
						.append(" [").append(pHeaderStr).append("] (").append(badPacketSize - 4).append(")")
						.append(rh.toString(false)).append("\r\n\r\n").toString();
				System.out.println(Recv);
			}
			break;
		case DELETE_CHAR:
			CharLoginHandler.DeleteChar(rh, c);
			break;
		case CHAR_SELECT:
			CharLoginHandler.Character_WithSecondPassword(rh, c);
			break;
		case AUTH_LOGIN_WITH_SPW:
			CharLoginHandler.checkSecondPassword(rh, c);
			break;
		case REG_SECOND_PASSWORD:
			CharLoginHandler.registerSecondPassword(rh, c);
			break;
		case ONLY_REG_SECOND_PASSWORD:
			CharLoginHandler.onlyRegisterSecondPassword(rh, c);
			break;
		case CHANGE_CHANNEL:
			InterServerHandler.ChangeChannel(rh, c, c.getPlayer());
			break;
		case PLAYER_LOGGEDIN:
			rh.skip(4);
			final int playerid = rh.readInt();
			if (type == ServerType.CHANNEL) {
				InterServerHandler.Loggedin(playerid, c);
			} else {
				CashShopOperation.EnterCS(playerid, c);
			}
			break;
		case ENTER_CASH_SHOP:
			InterServerHandler.EnterCS(c, c.getPlayer(), true);
			break;
		case ENTER_MTS:
			InterServerHandler.EnterMTS(c);
			break;
		case MOVE_PLAYER:
			PlayerHandler.MovePlayer(rh, c, c.getPlayer());
			break;
		case MOVE_ANDROID:
			PlayerHandler.MoveAndroid(rh, c, c.getPlayer());
			break;
		case CHAR_INFO_REQUEST:
			rh.skip(4);
			PlayerHandler.CharInfoRequest(rh.readInt(), c, c.getPlayer());
			break;
		case ROOM_CHANGE:
			PlayerHandler.RoomChange(rh, c, c.getPlayer());
			break;
		case DF_COMBO:
			PlayerHandler.absorbingDF(rh, c);
			break;
		case WILL_OF_SOWRD_COMBO:
			PlayerHandler.absorbingSword(rh, c.getPlayer());
			break;
		case PSYCHIC_GREP_R:
			KinesisSkill.PsychicGrep(rh, c);
			break;
		case PSYCHIC_ATTACK_R:
			KinesisSkill.PsychicAttack(rh, c);
			break;
		case PSYCHIC_ULTIMATE_R:
			KinesisSkill.PsychicUltimateDamager(rh, c);
			break;
		case PSYCHIC_DAMAGE_R:
			KinesisSkill.PsychicDamage(rh.readInt(), c);
			break;
		case CANCEL_PSYCHIC_GREP_R:
			rh.skip(8);
			KinesisSkill.CancelPsychicGrep(rh, c);
			break;
		case PASSIVE_ENERGY:
		case MAGNETIC_DAMAGE:
		case CLOSE_RANGE_ATTACK:
			PlayerHandler.closeRangeAttack(rh, c, c.getPlayer(), header != header.CLOSE_RANGE_ATTACK ? true : false);
			break;
		case RANGED_ATTACK:
			PlayerHandler.rangedAttack(rh, c, c.getPlayer());
			break;
		case MAGIC_ATTACK:
			PlayerHandler.MagicDamage(rh, c, c.getPlayer(), false);
			break;
		case SPECIAL_SKILL:
			PlayerHandler.SpecialSkill(rh, c, c.getPlayer());
			break;
		case FACE_EXPRESSION:
			PlayerHandler.ChangeEmotion(rh.readInt(), c.getPlayer());
			break;
		case ANDROID_FACE_EXPRESSION:
			PlayerHandler.ChangeEmotionAndroid(rh.readInt(), c.getPlayer());
			break;
		case TAKE_DAMAGE:
			PlayerHandler.TakeDamage(rh, c, c.getPlayer());
			break;
		case HEAL_OVER_TIME:
		case HEAL_OVER_TIME_FROM_POT:
			PlayerHandler.Heal(rh, c.getPlayer());
			break;
		case CANCEL_BUFF:
			PlayerHandler.CancelBuffHandler(rh.readInt(), c.getPlayer(), rh);
			break;
		case CANCEL_ITEM_EFFECT:
			PlayerHandler.CancelItemEffect(rh.readInt(), c.getPlayer());
			break;
		case USE_CHAIR:
			PlayerHandler.UseChair(rh.readInt(), c, c.getPlayer(), rh);
			break;
		case CANCEL_CHAIR:
			PlayerHandler.CancelChair(rh.readShort(), c, c.getPlayer());
			break;
		case USE_ITEMEFFECT:
			PlayerHandler.UseItemEffect(rh.readInt(), c, c.getPlayer());
			break;
		case MAKER_SKILL:
			PlayerHandler.makerSkill(rh, c);
			break;
		case SKILL_EFFECT:
			PlayerHandler.SkillEffect(rh, c.getPlayer());
			break;
		case MESO_DROP:
			rh.skip(4);
			PlayerHandler.DropMeso(rh.readInt(), c.getPlayer());
			break;
		case WHEEL_OF_FORTUNE:
			PlayerHandler.WheelOfFortuneEffect(rh.readInt(), c.getPlayer());
			break;
		case CHANGE_KEYMAP:
			PlayerHandler.ChangeKeymap(rh, c.getPlayer());
			break;
		case QUICK_SLOT:
			PlayerHandler.ChangeQuickSlot(rh, c.getPlayer());
			break;
		case CHANGE_MAP:
			if (type == ServerType.CHANNEL) {
				PlayerHandler.ChangeMap(rh, c, c.getPlayer());
			} else {
				CashShopOperation.LeaveCS(rh, c, c.getPlayer());
			}
			break;
		case CHANGE_MAP_SPECIAL:
			rh.skip(1);
			PlayerHandler.ChangeMapSpecial(rh.readMapleAsciiString(), c, c.getPlayer());
			break;
		case USE_INNER_PORTAL:
			rh.skip(1);
			PlayerHandler.InnerPortal(rh, c, c.getPlayer());
			break;
		case TROCK_ADD_MAP:
			PlayerHandler.TrockAddMap(rh, c, c.getPlayer());
			break;
		case ARAN_GAIN_COMBO:
			PlayerHandler.AranGainCombo(c, c.getPlayer());
			break;
		case ARAN_LOSE_COMBO:
			PlayerHandler.AranLoseCombo(c, c.getPlayer());
			break;
		case BLESS_OF_DARKNES:
			PlayerHandler.BlessOfDarkness(c.getPlayer());
			break;
		case SKILL_MACRO:
			PlayerHandler.ChangeSkillMacro(rh, c.getPlayer());
			break;
		case SUB_SUMMON_ACTION:
			PlayerHandler.subSummonAction(rh, c);
			break;
		case GIVE_FAME:
			PlayersHandler.GiveFame(rh, c, c.getPlayer());
			break;
		case TRANSFORM_PLAYER:
			PlayersHandler.TransformPlayer(rh, c, c.getPlayer());
			break;
		case NOTE_ACTION:
			PlayersHandler.Note(rh, c.getPlayer());
			break;
		case USE_DOOR:
			PlayersHandler.UseDoor(rh, c.getPlayer());
			break;
		case USE_MECH_DOOR:
			PlayersHandler.UseMechDoor(rh, c.getPlayer());
			break;
		case DAMAGE_REACTOR:
			PlayersHandler.HitReactor(rh, c);
			break;
		case CLOSE_CHALKBOARD:
			c.getPlayer().setChalkboard(null);
			break;
		case ITEM_SORT:
			InventoryHandler.ItemSort(rh, c);
			break;
		case DRESS_UP:
			AngelicBusterHandler.DressUpRequest(c.getPlayer(), rh);
			break;
		case ITEM_MOVE:
			InventoryHandler.ItemMove(rh, c);
			break;
		case ITEM_PICKUP:
			InventoryHandler.Pickup_Player(rh, c, c.getPlayer());
			break;
		case ITEM_GATHER:
			InventoryHandler.ItemGather(rh, c);
			break;
		case USE_CASH_ITEM:
			InventoryHandler.UseCashItem(rh, c);
			break;
		case RUNE_TOUCH:
			PlayersHandler.TouchRune(rh, c.getPlayer());
			break;
		case RUNE_USE:
			PlayersHandler.UseRune(rh, c.getPlayer());
			break;
		case USE_EDITIONAL_SCROLL:
			InventoryHandler.EditionalScroll(rh, c);
			break;
		case USE_PET_LOOT:
			InventoryHandler.UsePetLoot(rh, c);
			break;
		case USE_ITEM:
			InventoryHandler.UseItem(rh, c, c.getPlayer());
			break;
		case USE_SCRIPTED_NPC_ITEM:
			InventoryHandler.UseScriptedNPCItem(rh, c, c.getPlayer());
			break;
		case USE_RETURN_SCROLL:
			InventoryHandler.UseReturnScroll(rh, c, c.getPlayer());
			break;
		case USE_STAMP:
			InventoryHandler.UseStamp(rh, c);
			break;
		case USE_SOUL_ENCHANTER:
			InventoryHandler.UseSoulEnchanter(rh, c, c.getPlayer());
			break;
		case USE_SOUL_SCROLL:
			InventoryHandler.UseSoulScroll(rh, c, c.getPlayer());
			break;
		case SHOW_SOULEFFECT_R:
			c.getSession().write(SoulWeaponPacket.showSoulEffect(c.getPlayer(), rh.readByte()));
			break;
		case USE_EDITIONAL_STAMP:
			InventoryHandler.UseEditionalStamp(rh, c);
			break;
		case USE_MAGNIFY_GLASS:
			rh.skip(4);
			InventoryHandler.MagnifyingGlass(c, (byte) rh.readShort(), (byte) rh.readShort());
			break;
		case USE_UPGRADE_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), c, c.getPlayer());
			break;
		case USE_SPECIAL_SCROLL:
			rh.skip(4);
			InventoryHandler.UseSpecialScroll(rh, c.getPlayer());
			break;
		case USE_POTENTIAL_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), c, c.getPlayer());
			break;
		case USE_EQUIP_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), c, c.getPlayer());
			break;
		case USE_REBIRTH_SCROLL:
			rh.skip(4);
			InventoryHandler.UseUpgradeScroll((byte) rh.readShort(), (byte) rh.readShort(), c, c.getPlayer());
			break;
		case USE_MANYSET_CUBE:
			rh.skip(4);
			InventoryHandler.UseManySetCube(c, rh);
			break;
		case USE_SILVER_KARMA:
			InventoryHandler.UseKarma(rh, c);
			break;
		case USE_SUMMON_BAG:
			InventoryHandler.UseSummonBag(rh, c, c.getPlayer());
			break;
		case USE_SKILL_BOOK:
			InventoryHandler.UseSkillBook(rh, c, c.getPlayer());
			break;
		case USE_CATCH_ITEM:
			InventoryHandler.UseCatchItem(rh, c, c.getPlayer());
			break;
		case REWARD_ITEM:
			InventoryHandler.UseRewardItem(rh, c, c.getPlayer());
			break;
		case HYPNOTIZE_DMG:
			MobHandler.HypnotizeDmg(rh, c.getPlayer());
			break;
		case MOVE_LIFE:
			MobHandler.MoveMonster(rh, c, c.getPlayer());
			break;
		case AUTO_AGGRO:
			MobHandler.AutoAggro(rh.readInt(), c.getPlayer());
			break;
		case FRIENDLY_DAMAGE:
			MobHandler.FriendlyDamage(rh, c.getPlayer());
			break;
		case EQUIP_UPGRADE_SYSTEM:
			EnforceSystem.AddItemRecv(rh, c);
			break;
		case MONSTER_BOMB:
			MobHandler.MonsterBomb(rh.readInt(), c.getPlayer());
			break;
		case NPC_SHOP:
			NPCHandler.NPCShop(rh, c, c.getPlayer());
			break;
		case NPC_TALK:
			NPCHandler.NPCTalk(rh, c, c.getPlayer());
			break;
		case NPC_TALK_MORE:
			NPCHandler.NPCMoreTalk(rh, c);
			break;
		case NPC_ACTION:
			NPCHandler.NPCAnimation(rh, c);
			break;
		case QUEST_ACTION:
			NPCHandler.QuestAction(rh, c, c.getPlayer());
			break;
		case STORAGE:
			NPCHandler.Storage(rh, c, c.getPlayer());
			break;
		case GENERAL_CHAT:
			rh.skip(4);
			ChatHandler.GeneralChat(rh.readMapleAsciiString(), rh.readByte(), c, c.getPlayer());
			break;
		case PARTY_CHAT:
			ChatHandler.Others(rh, c, c.getPlayer());
			break;
		case WHISPER:
			ChatHandler.Whisper_Find(rh, c);
			break;
		case MESSENGER:
			ChatHandler.Messenger(rh, c);
			break;
		case AUTO_ASSIGN_AP:
			rh.skip(4);
			StatsHandling.AutoAssignAP(rh, c, c.getPlayer());
			break;
		case DISTRIBUTE_AP:
			rh.skip(4);
			StatsHandling.DistributeAP(rh, c, c.getPlayer());
			break;
		case DISTRIBUTE_HYPER_SP:
			rh.skip(4);
			StatsHandling.DistributeHyperSp(rh, rh.readInt(), c.getPlayer());
			break;
		case DISTRIBUTE_SP:
			rh.skip(4);
			StatsHandling.DistributeSP(rh, rh.readInt(), c, c.getPlayer());
			break;
		case PLAYER_INTERACTION:
			PlayerInteractionHandler.PlayerInteraction(rh, c, c.getPlayer());
			break;
		case GUILD_OPERATION:
			GuildHandler.GuildOpertion(rh, c);
			break;
		case DENY_GUILD_REQUEST:
			GuildHandler.DenyGuildRequest(rh, c);
			break;
		case ALLIANCE_OPERATION:
			AllianceHandler.AllianceOperatopn(rh, c, false);
			break;
		case DENY_ALLIANCE_REQUEST:
			AllianceHandler.AllianceOperatopn(rh, c, true);
			break;
		case BBS_OPERATION:
			BBSHandler.BBSOperatopn(rh, c);
			break;
		case PARTY_OPERATION:
			PartyHandler.PartyOperatopn(rh, c);
			break;
		case DENY_PARTY_REQUEST:
			PartyHandler.DenyPartyRequest(rh, c);
			break;
		case BUDDYLIST_MODIFY:
			BuddyListHandler.BuddyOperation(rh, c);
			break;
		case BUDDYLIST_UPDATE_R:
			BuddyListHandler.BuddyUpdate(rh, c);
			break;
		case BUY_CS_ITEM:
			CashShopOperation.BuyCashItem(rh, c, c.getPlayer());
			break;
		case COUPON_CODE:
			rh.skip(2); // 선물받을 대상. (MapleAsciiString)
			CashShopOperation.CouponCode(rh.readMapleAsciiString(), c);
			break;
		case CS_UPDATE:
			CashShopOperation.CSUpdate(rh, c, c.getPlayer());
			break;
		case DAMAGE_SUMMON:
			rh.skip(4);
			SummonHandler.DamageSummon(rh, c.getPlayer());
			break;
		case MOVE_SUMMON:
			SummonHandler.MoveSummon(rh, c.getPlayer());
			break;
		case SUMMON_ATTACK:
			SummonHandler.SummonAttack(rh, c, c.getPlayer());
			break;
		case SPAWN_PET:
			PetHandler.SpawnPet(rh, c, c.getPlayer());
			break;
		case REGISTER_PET_BUFF:
			PetHandler.RegisterPetBuff(rh, c.getPlayer());
			break;
		case MOVE_PET:
			PetHandler.MovePet(rh, c.getPlayer());
			break;
		case PET_CHAT:
			PetHandler.PetChat(rh, c.getPlayer());
			break;
		case PET_COMMAND:
			PetHandler.PetCommand(rh, c, c.getPlayer());
			break;
		case PET_FOOD:
			PetHandler.PetFood(rh, c, c.getPlayer());
			break;
		case PET_LOOT:
			InventoryHandler.Pickup_Pet(rh, c, c.getPlayer());
			break;
		case PET_AUTO_POT:
			PetHandler.Pet_AutoPotion(rh, c, c.getPlayer());
			break;
		case USE_HIRED_MERCHANT:
			HiredMerchantHandler.UseHiredMerchant(rh, c);
			break;
		case MERCH_ITEM_STORE:
			HiredMerchantHandler.MerchantItemStore(rh, c);
			break;
		case MOVE_DRAGON:
			SummonHandler.MoveDragon(rh, c.getPlayer());
			break;
		case USE_MAGNIFYING_GLASS:
			InventoryHandler.MagnifyingGlass(c, (byte) rh.readShort(), (byte) rh.readShort());
			break;
		case GOLDEN_HAMMER:
			InventoryHandler.UseGoldenHammer(rh, c);
			break;
		case HAMMER_EFFECT:
			InventoryHandler.HammerEffect(rh, c);
			break;
		case EQUIPPED_SKILL:
			PhantomHandler.equippedSkill(rh, c);
			break;
		case STEEL_SKILL_CHECK:
			PhantomHandler.steelSkillCheck(rh, c);
			break;
		case STEEL_SKILL:
			PhantomHandler.steelSkill(rh, c);
			break;
		case SUB_SUMMON:
			SummonHandler.subThingSummon(rh, c, c.getPlayer());
			break;
		case REMOVE_SUMMON:
			SummonHandler.removeSummon(rh, c);
			break;
		case HEAD_TITLE:
			InventoryHandler.headTitle(rh, c);
			break;
		case START_GATHER:
			ProfessionHandler.startGathering(rh, c);
			break;
		case END_GATHER:
			InventoryHandler.ItemGather(rh, c);
			break;
		case ITEMPOT_PUT:
			ItempotHandler.putItempot(rh, c);
			break;
		case ITEMPOT_REMOVE:
			ItempotHandler.removeItempot(rh, c);
			break;
		case ITEMPOT_FEED:
			ItempotHandler.feedItempot(rh, c);
			break;
		case ITEMPOT_CURE:
			ItempotHandler.cureItempot(rh, c);
			break;
		case PROFESSIONINFO_REQUEST:
			ProfessionHandler.getProfessionInfo(rh, c);
			break;
		case PROFESSION_MAKE_EFFECT:
			ProfessionHandler.professionMakeEffect(rh, c);
			break;
		case PROFESSION_MAKE_SOMETHING:
			ProfessionHandler.professionMakeTime(rh, c);
			break;
		case PROFESSION_MAKE:
			ProfessionHandler.professionMake(rh, c);
			break;
		case SPAWN_EXTRACTOR:
			ProfessionHandler.spawnExtractor(rh, c);
			break;
		case USE_RECIPE:
			ProfessionHandler.useRecipe(rh, c);
			break;
		case EXPEDITION_OPERATION:
			PartyHandler.processExpeditionRequest(rh, c);
			break;
		case AGI_BUFF:
			PlayerHandler.Agi_Buff(rh, c);
			break;
		case USE_BAG:
			ProfessionHandler.useBag(rh, c);
			break;
		case MOVE_BAG:
			ProfessionHandler.MoveBag(rh, c);
			break;
		case SWITCH_BAG:
			ProfessionHandler.SwitchBag(rh, c);
			break;
		case HYPER_RECV:
			PlayerHandler.getHyperSkill(rh, c);
			break;
		case FOLLOW_REQUEST:
			PlayersHandler.FollowRequest(rh, c);
			break;
		case AUTO_FOLLOW_REPLY:
		case FOLLOW_REPLY:
			PlayersHandler.FollowReply(rh, c);
			break;
		case WARP_TO_STARPLANET:
			PlayerHandler.warpToStarplanet(rh.readByte(), rh, c.getPlayer());
			break;
		case MAPLE_GUIDE:
		case MAPLE_CONTENT_MAP:
			PlayerHandler.MapleGuide(rh, rh.readShort(), c.getPlayer());
			break;
		case RETRACE_MECH:
			PlayerHandler.CancelBuffHandler(rh.readInt(), c.getPlayer(), rh);
			break;
		case SET_FREE_JOB:
			PlayerHandler.SetFreeJob(rh, c.getPlayer());
			break;
		case MAPLE_CHAT:
			PlayerHandler.MapleChat(rh, c.getPlayer());
			break;
		case ORBITAL_FLAME:
			PlayerHandler.OrbitalFlame(rh, c);
			break;
		case DMG_FLAME:
			PlayerHandler.MagicDamage(rh, c, c.getPlayer(), true);
			break;
		case DUEY_HANDLER:
			DueyHandler.DueyHandler(rh, c.getPlayer());
			break;
		case ARROW_FLATTER_ACTION:
			PlayerHandler.ArrowFlatterAction(rh, c.getPlayer());
			break;
		case INNER_CHANGE:
			PlayerHandler.ChangeInner(rh, c);
			break;
		case STAR_PLANET_RANK:
			PlayerHandler.getStarPlanetRank(rh, c.getPlayer());
			break;
		case ZERO_WEAPONINFO:
			ZeroHandler.ZeroWeaponInfo(rh, c);
			break;
		case ZERO_UPGRADE:
			ZeroHandler.ZeroWeaponLevelUp(rh, c);
			break;
		case ZERO_SHOCKWAVE:
			ZeroHandler.ZeroShockWave(rh, c);
			break;
		case ZERO_CHAT:
			ZeroHandler.ZeroChat(rh, c, "녀석, 사실 너를 꽤나 좋아하는 것 같아. 어떻게 아느냐고 ? 원래 우리는 하나였으니까 말하지 않아도 다 알지.");
			break;
		case ZERO_TAG:
			ZeroHandler.ZeroTag(rh, c);
			break;
		case ZERO_OPEN:
			ZeroHandler.ZeroOpen(rh, c);
			break;
		case ZERO_CLOTHES:
			ZeroHandler.ZeroClothes(rh.readInt(), rh.readByte(), c);
			break;
		case ZERO_SCROLL:
			ZeroHandler.ZeroScroll(rh, c);
			break;
		case ZERO_SCROLL_START:
			ZeroHandler.ZeroScrollStart(rh, c.getPlayer(), c);
			break;
		case GAME_END:
			c.getPlayer().send(MainPacketCreator.GameEnd());
			break;
		case STARDUST:
			PlayerHandler.Stardust(rh, c);
			break;
		case HOLLY:
			rh.skip(5);
			PlayerHandler.Holly(rh, c.getPlayer());
			break;
		case AUCTION_R:
			AuctionHandler.Handle(rh, c, rh.readByte());
			break;
		case MIST_SKILL:
			PlayerHandler.mistSkill(rh, c.getPlayer());
			break;
		case COMBAT_ANALYZE:
			c.getSession().write(MainPacketCreator.getCombatAnalyze(rh.readByte()));
			break;
		case PROCESS_CHECK:
			AntiHackHandler.ProcessCheck(rh, c);
			break;
		default:
			System.out.println("[UNHANDLED] Recv [" + header.toString() + "] found");
			break;
		}
	}
}
