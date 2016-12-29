package handler.channel;

import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import client.MapleClient;
import client.items.IItem;
import client.items.Item;
import client.items.MapleInventoryType;
import client.skills.SkillFactory;
import client.skills.SkillStatEffect;
import client.stats.PlayerStat;
import community.MaplePartyCharacter;
import packet.creators.MainPacketCreator;
import packet.creators.RunePacket;
import packet.transfer.read.ReadingMaple;
import scripting.EventInstanceManager;
import server.items.InventoryManipulator;
import server.items.ItemInformation;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleRune;
import tools.Randomizer;

public class PlayersHandler {
	private static final Logger logger = LoggerFactory.getLogger(PlayersHandler.class);

	public static final void Note(final ReadingMaple rh, final MapleCharacter chr) {
		final byte type = rh.readByte();
		switch (type) {
		case 0: { // Send to Note
			final String Sender = rh.readMapleAsciiString();
			final String SubStance = rh.readMapleAsciiString();
			if (Sender.equals(chr.getName())) {
				chr.getClient().getSession().write(MainPacketCreator.noteMessage(6));
				return;
			}
			chr.sendNote(Sender, SubStance);
			chr.getClient().getSession().write(MainPacketCreator.noteMessage(5));
			break;
		}
		case 1: { // Receive to Note
			final byte num = rh.readByte();
			rh.skip(2);
			for (int i = 0; i < num; i++) {
				final int id = rh.readInt();
				rh.skip(1);
				chr.deleteNote(id);
			}
			break;
		}
		default:
			logger.debug("Unhandled note action {} ", type);
		}
	}

	public static final void GiveFame(final ReadingMaple rh, final MapleClient c, final MapleCharacter chr) {
		final int who = rh.readInt();
		final int mode = rh.readByte();

		final int famechange = mode == 0 ? -1 : 1;
		final MapleCharacter target = (MapleCharacter) chr.getMap().getMapObject(who);

		switch (chr.canGiveFame(target)) {
		case OK:
			if (Math.abs(target.getFame() + famechange) <= 30000) {
				target.addFame(famechange);
				target.updateSingleStat(PlayerStat.FAME, target.getFame());
			}
			if (!chr.isGM()) {
				chr.hasGivenFame(target);
			}
			c.getSession().write(MainPacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
			target.getClient().getSession().write(MainPacketCreator.receiveFame(mode, chr.getName()));
			break;
		case NOT_TODAY:
			c.getSession().write(MainPacketCreator.giveFameErrorResponse(3));
			break;
		case NOT_THIS_MONTH:
			c.getSession().write(MainPacketCreator.giveFameErrorResponse(4));
			break;
		}
	}

	public static final void UseDoor(final ReadingMaple rh, final MapleCharacter chr) {
		final int cid = rh.readInt();
		final boolean mode = rh.readByte() == 0; // specifies if backwarp or
													// not, 1 town to target, 0
													// target to town
		for (MapleMapObject obj : chr.getMap().getAllDoor()) {
			final MapleDoor door = (MapleDoor) obj;
			if (door.getOwner().getId() == chr.getId() || door.getOwner().getParty().containsMembers(new MaplePartyCharacter(chr))) {
				door.warp(chr, mode);
				break;
			}
		}
		chr.send(MainPacketCreator.resetActions());
	}

	public static final void UseMechDoor(final ReadingMaple rh, final MapleCharacter chr) {
		final int oid = rh.readInt();
		final Point pos = rh.readPos();
		final int mode = rh.readByte();
		for (MapleMapObject obj : chr.getMap().getAllMechDoor()) {
			final MapleMechDoor door = (MapleMechDoor) obj;
			if (door.getOwnerId() == oid && door.getId() == mode) {
				chr.getMap().movePlayer(chr, pos);
				chr.checkFollow();
				break;
			}
		}
		chr.getClient().getSession().write(MainPacketCreator.resetActions());
	}

	public static final void TransformPlayer(final ReadingMaple rh, final MapleClient c, final MapleCharacter chr) {
		rh.skip(4); // Timestamp
		final byte slot = (byte) rh.readShort();
		final int itemId = rh.readInt();
		final String target = rh.readMapleAsciiString().toLowerCase();

		final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}
		switch (itemId) {
		case 2212000:
			for (final MapleCharacter search_chr : c.getPlayer().getMap().getCharacters()) {
				if (search_chr.getName().toLowerCase().equals(target)) {
					ItemInformation.getInstance().getItemEffect(2210023).applyTo(search_chr);
					search_chr.dropMessage(6, chr.getName() + " has played a prank on you!");
					InventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
				}
			}
			break;
		}
	}

	public static final void TouchRune(final ReadingMaple rh, final MapleCharacter chr) {
		rh.skip(4);
		int type = rh.readInt();
		MapleRune rune = chr.getMap().getAllRune().get(0);

		if (rune != null && rune.getRuneType() == type) {
			if (chr.getRuneTimeStamp() == 0) {
				chr.setTouchedRune(type);
				chr.send(RunePacket.RuneAction(5, 0));
			} else {
				chr.send(RunePacket.RuneAction(2, chr.getRuneTimeStamp()));
			}
		}
		chr.send(MainPacketCreator.resetActions());
	}

	public static final void UseRune(final ReadingMaple rh, final MapleCharacter chr) {
		final byte result = rh.readByte();
		final MapleRune rune = chr.getMap().getAllRune().get(0);
		SkillStatEffect effect;
		if (result == 1) {
			chr.getMap().broadcastMessage(RunePacket.showRuneEffect(chr.getTouchedRune()));
			chr.getMap().broadcastMessage(RunePacket.removeRune(rune, chr));
			chr.send(MainPacketCreator.resetActions());
			switch (chr.getTouchedRune()) {
			case 0: // ½Å¼ÓÀÇ ·é
				effect = SkillFactory.getSkill(80001427).getEffect(1);
				effect.applyTo(chr);
				break;
			case 1: // Àç»ýÀÇ ·é
				effect = SkillFactory.getSkill(80001428).getEffect(1);
				effect.applyTo(chr);
				break;
			case 2: // ºØ±«ÀÇ ·é
				effect = SkillFactory.getSkill(80001430).getEffect(1);
				effect.applyTo(chr);
				break;
			case 3: // ÆÄ¸êÀÇ ·é
				effect = SkillFactory.getSkill(80001432).getEffect(1);
				effect.applyTo(chr);
				break;
			case 4: // ÃµµÕÀÇ ·é
				for (int i = 0; i < 2; i++) { // °æÇèÄ¡ ¹öÇÁ, ÃµµÕ ¹öÇÁ.
					effect = SkillFactory.getSkill((i == 0) ? 80001752 : 80001762).getEffect(1);
					effect.applyTo(chr);
				}
				break;
			case 5: // ÁöÁøÀÇ ·é
				for (int i = 0; i < 2; i++) { // °æÇèÄ¡ ¹öÇÁ, °Å´ë ¹öÇÁ.
					effect = SkillFactory.getSkill((i == 0) ? 80001753 : 80001757).getEffect(1);
					effect.applyTo(chr);
				}
				break;
			case 6: // ¾îµÒÀÇ ·é
				effect = SkillFactory.getSkill(80001754).getEffect(1);
				effect.applyTo(chr);
				break;
			case 7: // º¸¹°ÀÇ ·é
				effect = SkillFactory.getSkill(80001755).getEffect(1);
				effect.applyTo(chr);
				break;
			}
			chr.getMap().removeMapObject(rune);
			chr.setRuneTimeStamp(false);
		} else {
			chr.setRuneTimeStamp(true);
		}
	}

	public static final void HitReactor(final ReadingMaple rh, final MapleClient c) {
		final int oid = rh.readInt();
		final int charPos = rh.readInt();
		final short stance = rh.readShort();
		final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);

		if (reactor == null || !reactor.isAlive()) {
			return;
		}
		if (c.getPlayer().getMapId() == 240050200) {
			EventInstanceManager eim = c.getPlayer().getEventInstance();
			if (reactor.getState() == 0) {
				eim.setProperty("choiceCave", "0");
			} else if (reactor.getState() == 2) {
				eim.setProperty("choiceCave", "1");
			} else {
				eim.setProperty("choiceCave", null);
			}
		} else if (c.getPlayer().getMapId() == 109090300) {
			int rand = Randomizer.rand(1, 10), itemid = 0;
			if (rand <= 2) {
				itemid = 2022163;
			} else if (rand > 2 && rand <= 4) {
				itemid = 2022165;
			} else if (rand > 4 && rand <= 6) {
				itemid = 2022166;
			}
			Item idrop = new Item(itemid, (byte) 0, (short) (1), (byte) 0);
			c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), idrop, reactor.getPosition(), true, true);
		}
		for (int i = 0; i < reactor.getStats().getStateEventSize(reactor.getState()); i++) {
			reactor.hitReactor(charPos, stance, c, (byte) i);
		}
	}

	public static void FollowRequest(final ReadingMaple rh, final MapleClient c) {
		MapleCharacter tt = c.getPlayer().getMap().getCharacterById_InMap(rh.readInt());
		if (rh.readByte() > 0) {
			tt = c.getPlayer().getMap().getCharacterById_InMap(c.getPlayer().getFollowId());
			if (tt != null && tt.getFollowId() == c.getPlayer().getId()) {
				tt.setFollowOn(true);
				c.getPlayer().setFollowOn(true);
			} else {
				c.getPlayer().checkFollow();
			}
			return;
		}
		if (rh.readByte() > 0) { // cancelling follow
			tt = c.getPlayer().getMap().getCharacterById_InMap(c.getPlayer().getFollowId());
			if (tt != null && tt.getFollowId() == c.getPlayer().getId() && c.getPlayer().isFollowOn()) {
				c.getPlayer().checkFollow();
			}
			return;
		}
		if (tt != null && tt.getPosition().distanceSq(c.getPlayer().getPosition()) < 10000 && tt.getFollowId() == 0 && c.getPlayer().getFollowId() == 0 && tt.getId() != c.getPlayer().getId()) { // estimate,
																																																	// should
																																																	// less
			tt.setFollowId(c.getPlayer().getId());
			tt.setFollowOn(false);
			tt.setFollowInitiator(false);
			c.getPlayer().setFollowOn(false);
			c.getPlayer().setFollowInitiator(false);
			tt.getClient().getSession().write(MainPacketCreator.followRequest(c.getPlayer().getId()));
		} else {
			c.getSession().write(MainPacketCreator.serverNotice(1, "³Ê¹« ¸Ö¸® ¶³¾îÁ® ÀÖ½À´Ï´Ù."));
		}
	}

	public static void FollowReply(final ReadingMaple rh, final MapleClient c) {
		if (c.getPlayer().getFollowId() > 0 && c.getPlayer().getFollowId() == rh.readInt()) {
			MapleCharacter tt = c.getPlayer().getMap().getCharacterById_InMap(c.getPlayer().getFollowId());
			if (tt != null && tt.getPosition().distanceSq(c.getPlayer().getPosition()) < 10000 && tt.getFollowId() == 0 && tt.getId() != c.getPlayer().getId()) { // estimate,
																																									// should
																																									// less
				boolean accepted = rh.readByte() > 0;
				if (accepted) {
					tt.setFollowId(c.getPlayer().getId());
					tt.setFollowOn(true);
					tt.setFollowInitiator(false);
					c.getPlayer().setFollowOn(true);
					c.getPlayer().setFollowInitiator(true);
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.followEffect(tt.getId(), c.getPlayer().getId(), null));
				} else {
					c.getPlayer().setFollowId(0);
					tt.setFollowId(0);
					tt.getClient().getSession().write(MainPacketCreator.getFollowMsg(5));
				}
			} else {
				if (tt != null) {
					tt.setFollowId(0);
					c.getPlayer().setFollowId(0);
				}
				c.getSession().write(MainPacketCreator.serverNotice(1, "³Ê¹« ¸Ö¸® ¶³¾îÁ® ÀÖ½À´Ï´Ù."));
			}
		} else {
			c.getPlayer().setFollowId(0);
		}
	}
}
