package handler.channel;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import client.MapleCharacterStat;
import client.MapleClient;
import client.MapleKeyBinding;
import client.SkillEffectEntry;
import client.items.Equip;
import client.items.IEquip;
import client.items.IItem;
import client.items.MapleInventoryType;
import client.items.MapleWeaponType;
import client.skills.CancelCooldownAction;
import client.skills.ISkill;
import client.skills.InnerAbillity;
import client.skills.InnerSkillValueHolder;
import client.skills.SkillFactory;
import client.skills.SkillMacro;
import client.skills.SkillStatEffect;
import client.stats.BuffStats;
import client.stats.MonsterStatus;
import client.stats.MonsterStatusEffect;
import client.stats.PlayerStat;
import community.MaplePartyCharacter;
import constants.GameConstants;
import constants.ServerConstants;
import database.MYSQL;
import launch.ChannelServer;
import packet.creators.AndroidPacket;
import packet.creators.CashPacket;
import packet.creators.MainPacketCreator;
import packet.creators.MobPacket;
import packet.creators.UIPacket;
import packet.opcode.RecvPacketOpcode;
import packet.skills.AdventurerSkill;
import packet.skills.AngelicBusterSkill;
import packet.skills.KaiserSkill;
import packet.skills.MechanicSkill;
import packet.transfer.read.ReadingMaple;
import scripting.NPCScriptManager;
import server.items.InventoryManipulator;
import server.items.ItemInformation;
import server.items.MakerItemFactory;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MaplePortal;
import server.maps.MapleSubSummon;
import server.maps.MapleSummon;
import server.maps.MapleWorldMapItem;
import server.maps.MoveSubSummon;
import server.maps.SummonMovementType;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import tools.AttackPair;
import tools.CaltechEval;
import tools.Pair;
import tools.Randomizer;
import tools.Timer;
import tools.Timer.BuffTimer;
import tools.Timer.EtcTimer;
import tools.Timer.MapTimer;
import tools.Triple;

public class PlayerHandler {
	private static final Logger logger = LoggerFactory.getLogger(PlayerHandler.class);

	private static ItemInformation ii = ItemInformation.getInstance();
	private static int 여우령 = 0, Rank = 0;

	private static int isFinisher(int skillid) {
		switch (skillid) {
		case 1111003:
			return 2;
		case 1121015:
			return 4;
		case 1101012:
			return 1;
		}
		return 0;
	}

	public static void ChangeSkillMacro(ReadingMaple rh, MapleCharacter chr) {
		int num = rh.readByte();
		String name;
		int shout, skill1, skill2, skill3;
		SkillMacro macro;
		for (int i = 0; i < num; i++) {
			name = rh.readMapleAsciiString();
			shout = rh.readByte();
			skill1 = rh.readInt();
			skill2 = rh.readInt();
			skill3 = rh.readInt();
			macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
			chr.updateMacros(i, macro);
		}
	}

	public static void ChangeKeymap(ReadingMaple rh, MapleCharacter chr) {
		if ((rh.available() != 8) && (chr != null)) { // else = pet auto pot
			rh.skip(4);
			int numChanges = rh.readInt();
			for (int i = 0; i < numChanges; i++) {
				int key = rh.readInt();
				byte type = rh.readByte();
				int action = rh.readInt();
				if ((type == 1) && (action >= 1000)) {
					ISkill skill = SkillFactory.getSkill(action);
					if ((skill != null) && (((!skill.isFourthJob()) && (!skill.isBeginnerSkill()) && (skill.isInvisible()) && (chr.getSkillLevel(skill) <= 0))
							|| (GameConstants.isLinkedAttackSkill(action)) || (action % 10000 < 1000))) {
						continue;
					}
				}
				chr.changeKeybinding(key, new MapleKeyBinding(type, action));
			}
		} else {
			int mode = rh.readInt(); // 1 : HP, 2 : MP
			int itemId = rh.readInt();
			if (mode == 1) {
				chr.setPetAutoHP(itemId);
			} else {
				chr.setPetAutoMP(itemId);
			}
		}
	}

	public static void ChangeQuickSlot(ReadingMaple rh, MapleCharacter chr) {
		final StringBuilder ret = new StringBuilder();
		for (int i = 0; i < 8; i++) { // really hacky way of doing it
			ret.append(rh.readAsciiString(1));
			rh.skip(3);
		}
		chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.QUICK_SLOT)).setCustomData(ret.toString());
	}

	public static void UseChair(int itemId, MapleClient c, MapleCharacter chr, ReadingMaple rh) {
		if (itemId == 3014000 || itemId == 3014001) {
			final String Special = rh.readMapleAsciiString();
			chr.setChairText(Special);
		}
		if (itemId == 3010000 && chr.getMapId() == 3000500) {
			NPCScriptManager.getInstance().start(c, 2003);
		}
		chr.setChair(itemId);
		chr.getMap().broadcastMessage(chr, MainPacketCreator.showChair(chr.getId(), itemId, chr.getChairText()), false);
		c.getSession().write(MainPacketCreator.resetActions());
	}

	public static void CancelChair(short id, MapleClient c, MapleCharacter chr) {
		if (id == -1) { // Cancel Chair
			if (chr.getChair() == 3010587) {
				for (final MapleMapObject mmo : chr.getMap().getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MIST))) {
					final MapleMist capsule = (MapleMist) mmo;
					if (chr.getId() == capsule.getOwner().getId() && capsule.isTimeCapsule()) {
						chr.getMap().removeMapObject(mmo);
						chr.getMap().broadcastMessage(MainPacketCreator.removeMist(capsule.getObjectId(), false));
						break;
					}
				}
			}
			chr.setChair(0);
			chr.setChairText(null);
			c.getSession().write(MainPacketCreator.cancelChair(chr, -1));
			chr.getMap().broadcastMessage(chr, MainPacketCreator.showChair(chr.getId(), 0, chr.getChairText()), true);
		} else { // Use In-Map Chair
			chr.setChair(id);
			c.getSession().write(MainPacketCreator.cancelChair(chr, id));
		}
	}

	public static void TrockAddMap(ReadingMaple rh, MapleClient c, MapleCharacter chr) {
		byte addrem = rh.readByte();
		byte vip = rh.readByte();
		if (addrem == 0) {
			chr.deleteFromTrockMaps(vip, rh.readInt());
		} else if (addrem == 1) {
			if (chr.getMap().getForcedReturnId() == 999999999) {
				chr.addTrockMap(vip, chr.getMapId());
			}
		}
		c.getSession().write(CashPacket.getTrockRefresh(chr, vip, addrem == 3));
	}

	public static void CharInfoRequest(int objectid, MapleClient c, MapleCharacter chr) {
		MapleCharacter player = (MapleCharacter) c.getPlayer().getMap().getMapObject(objectid);
		if (player != null) {
			if (!player.isGM() || (c.getPlayer().isGM() && player.isGM())) {
				c.getSession().write(MainPacketCreator.getCharInfo(player, c.getPlayer().equals(player)));
			} else {
				c.getSession().write(MainPacketCreator.resetActions());
			}
		}
	}

	public static void absorbingDF(ReadingMaple rh, MapleClient c) {
		int size = rh.readInt();
		int skillid = 0;
		if (GameConstants.isEunWol(c.getPlayer().getJob()) || GameConstants.isNightWalker(c.getPlayer().getJob()) || GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
			skillid = rh.readInt();
		}
		/* 시커 테스트 */
		int room = 0;
		byte unk = 0;
		int sn = 0;
		for (int i = 0; i < size; i++) {
			room = GameConstants.isDemonAvenger(c.getPlayer().getJob()) || c.getPlayer().getJob() == 212 ? 0 : rh.readInt();
			unk = rh.readByte();
			sn = rh.readInt();
			if (GameConstants.isDemonSlayer(c.getPlayer().getJob())) {
				c.getPlayer().addMP(c.getPlayer().getStat().getForce(room));
			}
			if (rh.available() > 0 && !GameConstants.isNightWalker(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) { // 제발
																																						// 예외처리좀
																																						// 하자
				unk = rh.readByte();
				sn = rh.readInt();
			}
			if (GameConstants.isEunWol(c.getPlayer().getJob()) && 여우령 != 0) {
				c.getPlayer().getMap().broadcastMessage(MainPacketCreator.absorbingRFG(c.getPlayer().getId(), skillid, sn));
				여우령 = 여우령 - 50;
			}
			if (GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
				boolean rand = Randomizer.isSuccess(80);
				if (rand) {
					c.getPlayer().getMap().broadcastMessage(AngelicBusterSkill.SoulSeekerRegen(c.getPlayer(), sn));
				}
			}
			if (GameConstants.isNightWalker(c.getPlayer().getJob())) {
				if (sn > 0) {
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.giveShadowBatBounce(c.getPlayer().getId(), sn, c.getPlayer().getPosition()));
					for (MapleSummon summon : c.getPlayer().getSummons().values()) {
						if (summon.getSkill() == 14000027) {
							c.getPlayer().getMap().removeMapObject(summon);
							summon.sendDestroyData(c);
							c.getPlayer().setBatCount(c.getPlayer().getBatCount() - 1);
						}
					}
					c.getPlayer().send(MainPacketCreator.resetActions());
				}
			}
			if ((GameConstants.isDemonAvenger(c.getPlayer().getJob())) && rh.available() >= 8) {
				c.getPlayer().getMap().broadcastMessage(MainPacketCreator.ShieldChacingRe(c.getPlayer().getId(), rh.readInt(), rh.readInt(), unk, c.getPlayer().getKeyValue2("schacing")));
				break;
			}
		}
	}

	public static void ArrowFlatterAction(ReadingMaple rh, final MapleCharacter chr) {
		final int state = rh.readByte();
		final Point pos = rh.readIntPos();
		chr.getMap().broadcastMessage(MainPacketCreator.spawnArrowFlatter(chr, state, pos));
		chr.getMap().broadcastMessage(MainPacketCreator.spawnArrowFlatter(state));
		EtcTimer tMan = EtcTimer.getInstance();
		tMan.schedule(new Runnable() {
			@Override
			public void run() {
				chr.getMap().broadcastMessage(MainPacketCreator.cancelArrowFlatter(state));
			}
		}, 30000);
	}

	public static void absorbingSword(ReadingMaple rh, MapleCharacter chr) {
		if (!chr.isAlive()) {
			chr.getClient().getSession().write(MainPacketCreator.resetActions());
			return;
		}
		final int mobcount = rh.readInt();
		final List<Integer> oids = new ArrayList<>();
		for (int i = 0; i < mobcount; i++) {
			oids.add(rh.readInt());
		}
		int skillid = chr.getBuffedSkillEffect(BuffStats.WILL_OF_SWORD).getSourceId();
		chr.getMap().broadcastMessage(KaiserSkill.absorbingSwordCount(chr.getId(), oids, skillid));
		chr.getClient().send(KaiserSkill.cancelWillofSword());
	}

	public static void TakeDamage(ReadingMaple rh, MapleClient c, MapleCharacter chr) throws InterruptedException {
		rh.skip(4); // Ticks
		rh.skip(4);
		byte type = rh.readByte();
		rh.skip(1); // Element - 0x00 = elementless, 0x01 = ice, 0x02 = fire,
					// 0x03 = lightning
		int damage = rh.readInt();

		int oid = 0;
		int monsteridfrom = 0;
		int reflect = 0;
		byte direction = 0;
		int pos_x = 0;
		int pos_y = 0;
		int fake = 0;
		int mpattack = 0;
		boolean is_pg = false;
		boolean isDeadlyAttack = false;
		boolean guardianSpiritActivated = false;
		MapleMonster attacker = null;
		MapleCharacter attacker2 = null;
		MapleMapObject attacke = null;
		MapleCharacterStat stats = chr.getStat();
		rh.skip(2);
		if (type != -2 && type != -3 && type != -4) { // Not map damage
			monsteridfrom = rh.readInt();
			oid = rh.readInt();
			attacker = (MapleMonster) chr.getMap().getMonsterByOid(oid);
			direction = rh.readByte();

			if (chr.getSkillLevel(36110004) > 0) {
				chr.getStat().setHp(chr.getStat().getHp() + -damage);
				final List<Pair<PlayerStat, Long>> statupz = new ArrayList<Pair<PlayerStat, Long>>(8);
				statupz.add(new Pair<PlayerStat, Long>(PlayerStat.HP, (long) stats.hp));
				c.getSession().write(MainPacketCreator.updatePlayerStats(statupz, chr.getJob()));
				chr.send(MainPacketCreator.EazisSystem(chr.getId(), oid));
				AttackInfo attack = DamageParse.parseDmgM(chr, rh, false);
				chr.getMap().broadcastMessage(chr, MainPacketCreator.attack(RecvPacketOpcode.CLOSE_RANGE_ATTACK, chr, chr.getId(), attack.tbyte, attack.skill, chr.getSkillLevel(36110004),
						attack.display, attack.speed, attack.allDamage, attack.value, attack.position, (byte) 0, 0, chr.getLevel(), 0), chr.getPosition());
			}

			if (chr.getSkillLevel(12000024) > 0) {
				int minermp = (int) (damage * 0.8);
				damage -= minermp;
				chr.addMP(-minermp);
			}

			SkillStatEffect eff = chr.getBuffedSkillEffect(BuffStats.DARK_CRESSENDOR, 36111003);
			if (eff != null && damage == -1) {
				if (eff.makeChanceResult()) {
					chr.dualBrid++;
					if (chr.dualBrid >= 10) {
						chr.cancelEffectFromBuffStat(BuffStats.DARK_CRESSENDOR, 36111003);
					} else {
						eff.applyTo(chr);
					}
				}

			}

			if (chr.getJob() >= 433 && chr.getJob() <= 434 && damage != -1) {
				if (chr.getSkillLevel(4330009) > 0) {
					if (Randomizer.rand(1, 100) <= SkillFactory.getSkill(4330009).getEffect(chr.getSkillLevel(4330009)).getER()) {
						damage = -1;
						SkillFactory.getSkill(4330009).getEffect(chr.getSkillLevel(4330009)).applyTo(chr);
					}
				}
			}

			if (damage != -1) {
				if ((chr.getJob() >= 411 && chr.getJob() <= 412) || (chr.getJob() >= 421 && chr.getJob() <= 422)) {
					MapleSummon summon = chr.getSummons().get(chr.getJob() >= 411 && chr.getJob() <= 412 ? 4111007 : 4211007);
					if (summon != null) {
						final List<AttackPair> allDamage = new ArrayList<AttackPair>();
						final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
						allDamageNumbers.add(new Pair<Integer, Boolean>(damage * 13, false));
						allDamage.add(new AttackPair(oid, allDamageNumbers));
						chr.getMap().broadcastMessage(null, MainPacketCreator.summonAttack(chr.getId(), summon.getObjectId(), (byte) 0x78, (byte) 0x11, allDamage, chr.getLevel(), true, 0),
								summon.getPosition());
						attacker.damage(chr, damage * 13, true); // 데미지 제한
					}
				}
			}

			if (type != -1) { // Bump damage
				MobAttackInfo attackInfo = MobAttackInfoFactory.getInstance().getMobAttackInfo(attacker, type);
				if (attackInfo.isDeadlyAttack()) {
					isDeadlyAttack = true;
					mpattack = stats.getMp() - 1;
				} else {
					mpattack += attackInfo.getMpBurn();
				}
				MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
				if (skill != null && (damage == -1 || damage > 0)) {
					skill.applyEffect(chr, attacker, false);
				}
				attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
			}
		}

		/* 홀리 매직쉘 가드 카운트 차감 */
		if (chr.getBuffedValue(BuffStats.HOLY_SHELL) != null) {
			if (chr.getKeyValue2("HolyMagicShellLifeCount") != 0 && chr.getKeyValue2("HolyMagicShellLifeCount") != -1) {
				int life = chr.getKeyValue2("HolyMagicShellLifeCount");
				if (life > 0) {
					life--;
				}
				chr.setKeyValue2("HolyMagicShellLifeCount", life);
				if (life == 0) {
					chr.cancelEffectFromBuffStat(BuffStats.HOLY_SHELL, -1);
				}
			}
		}

		if (GameConstants.isXenon(chr.getJob())) {
			if (Randomizer.isSuccess(60)) {
				chr.giveSurPlus(1);
			}
		}

		if (chr.getBuffedValue(BuffStats.BLESSING_ARMOR, 1210016) != null) {
			int guardCount = chr.getBuffedValue(BuffStats.BLESSING_ARMOR, 1210016).intValue();
			if (guardCount == 1) {
				chr.cancelEffectFromBuffStat(BuffStats.BLESSING_ARMOR, 1210016);
			} else {
				chr.setBuffedValue(BuffStats.BLESSING_ARMOR, 1210016, guardCount - 1);
			}
		}

		if (damage == -1) {
			if (chr.getJob() / 100 == 4) {
				fake = 4020002 + ((chr.getJob() / 10 - 40) * 100000);
			} else if (chr.getJob() == 122) { // 가디언 이리스
				fake = 1220006;
				guardianSpiritActivated = true;
			} else if (GameConstants.isMercedes(chr.getJob())) {
				fake = 23000001;
			} else if (chr.getJob() == 512) { // 가드 크러쉬
				fake = 5120014;
			}
		}
		if (damage == 0) { // 가드
			if (chr.getSkillLevel(31110008) > 0) {
				SkillStatEffect effs = SkillFactory.getSkill(31110008).getEffect(chr.getSkillLevel(31110008));
				int recHP = (int) (chr.getStat().getCurrentMaxHp() * (effs.getY() / 100.0D));
				int recMP = effs.getZ();
				chr.addHP(recHP);
				chr.addMP(recMP);
				chr.handleForceGain(oid, 31110008, chr.getStat().addForce(effs.getZ()));
			}
		}

		if (chr.getJob() == 2711 || chr.getJob() == 2712) {
			if (chr.getSkillLevel(27110007) > 0) { // 라이프 타이달
				ISkill skill = SkillFactory.getSkill(27110007);
				int critical = chr.getSkillLevel(skill);
				if ((chr.getStat().getHp() / chr.getStat().getCurrentMaxHp()) * 100 < (chr.getStat().getMp() / chr.getStat().getCurrentMaxMp()) * 100) {
					c.send(MainPacketCreator.giveLifeTidal(false, skill.getEffect(critical).getX()));
				} else if ((chr.getStat().getHp() / chr.getStat().getCurrentMaxHp()) * 100 > (chr.getStat().getMp() / chr.getStat().getCurrentMaxMp()) * 100) {
					if (critical > 0) {
						chr.getStat().passive_sharpeye_rate += skill.getEffect(critical).getProb();
						chr.getStat().passive_sharpeye_min_percent += skill.getEffect(critical).getCriticalMin();
						c.send(MainPacketCreator.giveLifeTidal(true, skill.getEffect(critical).getProb()));
					}
				}
			}
		}
		if (damage > 0) {
			if (chr.getBuffedValue(BuffStats.MORPH) != null) {
				chr.cancelMorphs();
			}

			if (type == -1) {
				if (chr.getBuffedValue(BuffStats.ACC) != null) {
					attacker = (MapleMonster) chr.getMap().getMapObject(oid);
					if (attacker != null) {
						long bouncedamage = (int) (damage * (chr.getBuffedValue(BuffStats.ACC).doubleValue() / 100));
						bouncedamage = (int) Math.min(bouncedamage, attacker.getMobMaxHp() / 2);
						attacker.damage(chr, bouncedamage, true);
						chr.checkMonsterAggro(attacker);
						damage -= bouncedamage;
						chr.getMap().broadcastMessage(chr, MobPacket.damageMonster(oid, bouncedamage), chr.getPosition());
						if (GameConstants.isDemonSlayer(chr.getJob())) { // 다크
																			// 리벤지
																			// 효과
							if (chr.getSkillLevel(31101003) > 0) {
								SkillStatEffect skills = SkillFactory.getSkill(31101003).getEffect(chr.getSkillLevel(31101003));
								if (skills.makeChanceResult()) {
									attacker.applyStatus(chr, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, 1), SkillFactory.getSkill(31101003), null, false), false,
											skills.getValue("subTime"), false);
								}
							}
						}
						is_pg = true;
					}
				} else if (chr.getBuffedValue(BuffStats.BLESS_OF_DARKNESS) != null) {
					attacker = (MapleMonster) chr.getMap().getMapObject(oid);
					if (attacker != null) {
						int reducedamage = (int) (damage * (chr.getBuffedValue(BuffStats.BLESS_OF_DARKNESS).doubleValue() / 100));
						damage = reducedamage;
						chr.setBlessOfDark((byte) (chr.getBlessOfDark() - 1));
						if (chr.getBlessOfDark() == 0) {
							chr.cancelEffect(SkillFactory.getSkill(27100003).getEffect(1), false, -1);
						} else {
							SkillFactory.getSkill(27100003).getEffect(chr.getSkillLevel(27100003)).applyTo(chr);
						}
					}
				} else if (chr.getSkillLevel(4221006) > 0) {
					for (final MapleMist mist : chr.getMap().getAllMistsThreadsafe()) {
						if (mist.getSourceSkill() != null) {
							if (mist.getSourceSkill().getId() == 4221006) {
								for (final MapleMapObject mo : chr.getMap().getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
									if (((MapleCharacter) mo).getId() == chr.getId()) {
										damage = 0;
									}
								}
							}
						}
					}
				}
			} else if (type != -2 && type != -3 && type != -4) { // -2, -3, -4 :
																	// Map
																	// Damage
				switch (chr.getJob()) {
				case 112: {
					ISkill skill = SkillFactory.getSkill(1120004);
					if (chr.getSkillLevel(1120004) > 0) {
						damage = (int) ((skill.getEffect(chr.getSkillLevel(skill)).getX() / 1000.0) * damage);
					}
					break;
				}
				case 122: {
					ISkill skill = SkillFactory.getSkill(1220005);
					if (chr.getSkillLevel(1220005) > 0) {
						damage = (int) ((skill.getEffect(chr.getSkillLevel(skill)).getX() / 1000.0) * damage);
					}
					break;
				}
				case 132: {
					ISkill skill = SkillFactory.getSkill(1320005);
					if (chr.getSkillLevel(1320005) > 0) {
						damage = (int) ((skill.getEffect(chr.getSkillLevel(skill)).getX() / 1000.0) * damage);
					}
					break;
				}
				}
			}
			long hploss = 0;
			int mploss = 0;
			if (chr.getBuffedValue(BuffStats.PARTY_DAMAGE) != null) {
				damage -= (int) Math.ceil(damage / 10);
				if (chr.getParty() != null) {
					for (MaplePartyCharacter pPlayer : chr.getParty().getMembers()) {
						final MapleCharacter player = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(pPlayer.getId());
						player.addHP((int) Math.ceil(damage / 10));
					}
				}
			}
			if (chr.getBuffedValue(BuffStats.MAGIC_GUARD) != null || chr.getSkillLevel(27000003) > 0) {
				if (isDeadlyAttack) {
					if (stats.getHp() > 1) {
						hploss = stats.getHp() - 1;
					}
					if (stats.getMp() > 1) {
						mploss = stats.getMp() - 1;
					}
				} else {
					if (chr.getSkillLevel(27000003) > 0) {
						ISkill skill = SkillFactory.getSkill(27000003);
						SkillStatEffect eff = skill.getEffect(chr.getSkillLevel(skill));
						mploss = (int) (damage * (eff.getX() / 100.0));
					} else {
						mploss = (int) (damage * (chr.getBuffedValue(BuffStats.MAGIC_GUARD).doubleValue() / 100.0));
					}
					hploss = damage - mploss;
					mpattack += mploss;

					if (mploss > stats.getMp()) {
						hploss += mploss - stats.getMp();
						mpattack = stats.getMp();
					}
				}
			}
			if (chr.getBuffedValue(BuffStats.MESOGUARD) != null) {
				damage = (damage % 2 == 0) ? damage / 2 : (damage / 2) + 1;

				int mesoloss = (int) (damage * (chr.getBuffedValue(BuffStats.MESOGUARD).doubleValue() / 100.0));
				if (chr.getMeso() < mesoloss) {
					chr.gainMeso(-chr.getMeso(), false);
					chr.cancelEffectFromBuffStat(BuffStats.MESOGUARD, -1);
				} else {
					chr.gainMeso(-mesoloss, false);
				}
				if (isDeadlyAttack && stats.getMp() > 1) {
					mpattack = stats.getMp() - 1;
				}
			}
			if (chr.getBuffedValue(BuffStats.STANCE, 22181004) != null) { // 오닉스의
																			// 의지
				int level = chr.getSkillLevel(22181004);
				int lessDaMper = (int) (new CaltechEval("5+d(" + level + "/2)").evaluate());
				if (hploss > 0) {
					int lessDaM = (int) (hploss * (lessDaMper / 100.0D));
					hploss -= lessDaM;
				} else {
					int lessDaM = (int) (damage * (lessDaMper / 100.0D));
					damage -= lessDaM;
				}
			}
			List<Integer> attack = attacke instanceof MapleMonster || attacke == null ? null : (new ArrayList<Integer>());
			if ((chr.getJob() == 531 || chr.getJob() == 532) && attacke != null) {
				final ISkill divine = SkillFactory.getSkill(5310009);
				if (chr.getSkillLevel(divine) > 0) {
					final SkillStatEffect divineShield = divine.getEffect(chr.getSkillLevel(divine));
					if (divineShield.makeChanceResult()) {
						if (attacke instanceof MapleMonster) {
							attacker = (MapleMonster) attacke;
							final int theDmg = (int) (divineShield.getDamage());
							attacker.damage(chr, theDmg, true);
							chr.getMap().broadcastMessage(MobPacket.damageMonster(attacker.getObjectId(), theDmg));
						} else {
							attacker2 = (MapleCharacter) attacke;
							attacker2.addHP(-divineShield.getDamage());
							attack.add((int) divineShield.getDamage());
						}
					}
				}
			}
			if (chr.getSkillLevel(1210016) > 0 && chr.getJob() == 122) { // 블래싱
																			// 아머
				SkillStatEffect effect = SkillFactory.getSkill(1210016).getEffect(chr.getSkillLevel(1210016));
				if (!chr.skillisCooling(1210016)) {
					if (effect.makeChanceResult()) {
						effect.applyTo(chr);
						ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 1210016), effect.getCooldown());
						chr.addCooldown(1210016, System.currentTimeMillis(), effect.getCooldown(), timer);
					}
				}
			}
			if ((chr.getJob() == 512 || chr.getJob() == 522) && chr.getBuffedValue(BuffStats.PERCENT_DAMAGE_BUFF) == null && chr.getSkillLevel(chr.getJob() == 512 ? 5120011 : 5220012) > 0) {
				final ISkill divine = SkillFactory.getSkill(chr.getJob() == 512 ? 5120011 : 5220012);
				SkillStatEffect effect = divine.getEffect(chr.getSkillLevel(chr.getJob() == 512 ? 5120011 : 5220012));
				if (chr.getSkillLevel(divine) > 0 && !chr.skillisCooling(divine.getId())) {
					final SkillStatEffect divineShield = divine.getEffect(chr.getSkillLevel(divine));
					if (divineShield.makeChanceResult()) {
						divineShield.applyTo(chr);
						ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, chr.getJob() == 512 ? 5120011 : 5220012), effect.getCooldown());
						chr.addCooldown(chr.getJob() == 512 ? 5120011 : 5220012, System.currentTimeMillis(), effect.getCooldown(), timer);
					}
				}
			}

			if (attacker != null && chr.getBuffedValue(BuffStats.BEHOLDER, 1301013) != null && chr.getSkillLevel(1320011) > 0 && chr.getSummons().get(1301013) != null) {
				SkillStatEffect revenge = SkillFactory.getSkill(1320011).getEffect(chr.getSkillLevel(1320011));
				if (!chr.skillisCooling(1320011) && revenge.makeChanceResult()) {
					MapleSummon hs = chr.getSummons().get(1301013);
					final List<AttackPair> allDamage = new ArrayList<AttackPair>();
					final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
					if (hs != null) {
						long toDamage = (int) Math.round(chr.getStat().getMaxAttack() * (revenge.getDamage() / 100.0D));
						if (!attacker.getStats().isBoss()) {
							if (Randomizer.isSuccess(revenge.getZ())) {
								toDamage = (int) attacker.getHp();
							}
						}
						long recoverHP = (int) Math.round(toDamage * (revenge.getX() / 100.0D));
						recoverHP = Math.min(recoverHP, chr.getStat().getHp() / 20);
						chr.addHP(recoverHP);
						chr.send(MainPacketCreator.showOwnRecoverHP(recoverHP));
						allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf((int) toDamage), false));
						allDamage.add(new AttackPair(oid, allDamageNumbers));
						attacker.damage(chr, toDamage, true);
						chr.getMap().broadcastMessage(MainPacketCreator.summonAttack(hs.getOwner().getId(), hs.getObjectId(), (byte) 0x78, (byte) 0x11, allDamage, chr.getLevel(), true, 0),
								hs.getPosition());
						chr.checkMonsterAggro(attacker);
						ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 1320011), revenge.getCooldown());
						chr.addCooldown(1320011, System.currentTimeMillis(), revenge.getCooldown(), timer);
					} else {
						logger.debug("BEHOLDER Revenge activated. but summon is null");
					}
				}
			}

			if (isDeadlyAttack) {
				chr.addMPHP(stats.getHp() > 1 ? -(stats.getHp() - 1) : 0, stats.getMp() > 1 ? -(stats.getMp() - 1) : 0);
			} else {
				chr.addMPHP(hploss > 0 ? -hploss : -damage, -mpattack);
			}

		}

		if (guardianSpiritActivated) {
			rh.skip(11);
			int gsOid = rh.readInt();
			MapleMonster gsMob = (MapleMonster) chr.getMap().getMonsterByOid(gsOid);
			if (!gsMob.getStats().isBoss()) {
				gsMob.applyStatus(chr, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.STUN, 1), SkillFactory.getSkill(1220006), null, false), false,
						SkillFactory.getSkill(1220006).getEffect(chr.getSkillLevel(1220006)).getDuration(), false);
			}
		}
		if (!chr.isHidden()) {
			chr.getMap().broadcastMessage(chr, MainPacketCreator.damagePlayer(type, monsteridfrom, chr.getId(), damage, fake, direction, reflect, is_pg, oid, pos_x, pos_y), false);
		}
	}

	public static void AranGainCombo(MapleClient c, MapleCharacter chr) {
		if (GameConstants.isAran(chr.getJob())) {
			short combo = chr.getCombo();
			long curr = System.currentTimeMillis();
			combo++;
			chr.updateCombo(combo, curr);
		}
	}

	public static void AranLoseCombo(MapleClient c, MapleCharacter chr) {
		if (GameConstants.isAran(chr.getJob())) {
			final short losecombo = (short) ((chr.getCombo() / 100) + 1);
			chr.updateCombo((short) (chr.getCombo() - losecombo), System.currentTimeMillis());
		}
	}

	public static void BlessOfDarkness(MapleCharacter chr) {
		if (chr.getJob() >= 2710 && chr.getJob() <= 2712) {
			if (chr.getBlessOfDark() < 3) {
				chr.setBlessOfDark((byte) (chr.getBlessOfDark() + 1));
				SkillFactory.getSkill(27100003).getEffect(chr.getSkillLevel(27100003)).applyTo(chr);
			}
		}
	}

	public static void UseItemEffect(int itemId, MapleClient c, MapleCharacter chr) {
		IItem toUse = chr.getInventory(MapleInventoryType.CASH).findById(itemId);
		if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}
		chr.setItemEffect(itemId);
		chr.getMap().broadcastMessage(chr, MainPacketCreator.itemEffect(chr.getId(), itemId), false);
	}

	public static void CancelItemEffect(int id, MapleCharacter chr) {
		if (GameConstants.isAngelicBlessBuffEffectItem(id)) {
			return;
		}
		if (GameConstants.isAngelicBlessBuffEffectItem(-id)) {
			return;
		}
		chr.cancelEffect(ItemInformation.getInstance().getItemEffect(-id), false, -1);
	}

	public static void CancelBuffHandler(int sourceid, MapleCharacter chr, ReadingMaple rh) {
		if (GameConstants.isAngelicBlessBuffEffectItem(sourceid)) {
			return;
		}
		if (GameConstants.isAngelicBlessBuffEffectItem(-sourceid)) {
			return;
		}
		if (sourceid / 10000 == 202) {
			sourceid = -sourceid;
		}
		switch (sourceid) {
		case 35001002: { // 메탈아머 : 휴면
			chr.send(MechanicSkill.cancelHuman());
			chr.getMap().broadcastMessage(chr, MechanicSkill.cancelHuman(), false);
			break;
		}
		case 35111003: { // 메탈 아머 : 탱크
			chr.send(MechanicSkill.cancelTank());
			chr.getMap().broadcastMessage(chr, MechanicSkill.cancelTank(), false);
			break;
		}
		case 33001001: { // 재규어 라이딩
			chr.send(MainPacketCreator.cancelJaguarRiding());
			break;
		}
		case 31011000:
		case 31201000:
		case 31211000:
		case 31221000: { // 익시드
			chr.send(MainPacketCreator.cancelExeed());
			chr.exeedCount = 0;
			chr.exeedAttackCount = 0;
			break;
		}
		case 1311013: { // 비홀더 도미넌트
			chr.send(AdventurerSkill.CancelHeholderBuff());
			break;
		}
		case 60001218:
		case 60011218: { // 매지컬 리프트
			rh.skip(2);
			byte[] available = rh.read((int) rh.available());
			chr.getMap().broadcastMessage(chr, MainPacketCreator.showNovaLift(chr.getId(), sourceid, available), false);
			break;
		}
		case 9001004: { // 운영자 숨기
			chr.setHide(true);
		}
		default: {
			ISkill skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(sourceid));
			if (skill == null) {
				chr.dropMessage(6, "스킬 오류 발생! SkillID : " + sourceid + " 오류게시판에 오류를 작성해주세요!");
				return;
			}
			if (skill.isChargeSkill()) {
				if (sourceid == 3121004 ? skill.getEffect(20).getCooldown() > 0
						: sourceid == 3121009 ? skill.getEffect(20).getCooldown() > 0 : skill.getEffect(chr.getSkillLevel(skill)).getCooldown() > 0) {
					if (!chr.isEquilibrium() && !GameConstants.isDarkSkills(skill.getId())) {
						SkillStatEffect effect = skill.getEffect(chr.getSkillLevel(skill));
						chr.send(MainPacketCreator.skillCooldown(skill.getId(), effect.getCooldown()));
						chr.addCooldown(skill.getId(), System.currentTimeMillis(), effect.getCooldown(),
								BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, skill.getId()), effect.getCooldown()));
					}
				}
				chr.setKeyDownSkill_Time(0);
				chr.getMap().broadcastMessage(chr, MainPacketCreator.skillCancel(chr, skill.getId()), false);
				chr.setSkillEffect(null);
			} else {
				if (chr.getBuffedValue(BuffStats.WK_CHARGE) != null && chr.getBuffedValue(BuffStats.LIGHTNING_CHARGE) != null && skill.getId() != 1211008) {
					chr.cancelEffectFromBuffStat(BuffStats.WK_CHARGE, -1);
					chr.cancelEffectFromBuffStat(BuffStats.LIGHTNING_CHARGE, -1);
					SkillFactory.getSkill(1211008).getEffect(chr.getSkillLevel(1211008)).applyTo(chr);
				} else {
					if (sourceid != -1) {
						chr.cancelEffect(SkillFactory.getSkill(sourceid).getEffect(chr.getSkillLevel(sourceid)), false, -1);
					}
					if (sourceid == 11101022 || sourceid == 11111022) {
						chr.cancelEffectFromBuffStat(BuffStats.HEAD_EFFECT, -1);
					}
				}
			}
			break;
		}
		}
	}

	public static void SkillEffect(ReadingMaple rh, MapleCharacter chr) {
		SkillEffectEntry entry = new SkillEffectEntry(rh.readInt(), rh.readByte(), rh.readByte(), rh.readByte(), rh.readByte());
		ISkill skill = SkillFactory.getSkill(entry.getSkillId());
		int skilllevel_serv = chr.getSkillLevel(skill);
		if (skilllevel_serv > 0 && skilllevel_serv == entry.getLevel() && skill.isChargeSkill() && entry.getLevel() > 0 || skill.getId() == 35101009) {
			chr.setKeyDownSkill_Time(System.currentTimeMillis());
			chr.setSkillEffect(entry);
			if (skill.getId() == 27101202 || skill.getId() == 35001001 || skill.getId() == 35101009 || skill.getId() == 2221011) {
				skill.getEffect(entry.getLevel()).applyTo(chr);
				chr.getMap().broadcastMessage(chr, MainPacketCreator.skillEffect(chr, entry, chr.getPosition()), false);
			} else {
				chr.getMap().broadcastMessage(chr, MainPacketCreator.skillEffect(chr, entry, chr.getPosition()), false);
			}
		}
	}

	public static void SpecialSkill(ReadingMaple rh, final MapleClient c, final MapleCharacter chr) {

		if (c.getPlayer() == null || chr == null || !chr.isAlive()) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}

		rh.skip(4); // Old X and Y
		int skillid = rh.readInt();
		if (GameConstants.isZero(chr.getJob())) {
			rh.skip(1);
		}
		byte skillLevel = rh.readByte();
		if (GameConstants.isAran(chr.getJob())) {
			chr.useComboSkill(skillid);
		}

		ISkill skill = SkillFactory.getSkill(GameConstants.getLinkedBuffSkill(skillid));
		if (chr.getSkillLevel(skill) != skillLevel && chr.getSummonLinkSkillLevel(skill) != skillLevel) {
			chr.changeSkillLevel(skill, skillLevel, (byte) skill.getMasterLevel());
		}
		SkillStatEffect effect = SkillFactory.getSkill(GameConstants.getLinkedBuffSkill(skillid)).getEffect(skillLevel);

		if ((skillid == 142121008) || (effect.getCooldown() > 0 && !chr.isEquilibrium())) {
			if (chr.skillisCooling(skillid)) {
				return;
			}
			boolean hasNoCoolTime = false;
			for (InnerSkillValueHolder isvh : chr.getInnerSkills()) {
				if (isvh.getSkillId() == 70000045) {
					hasNoCoolTime = Randomizer.isSuccess(SkillFactory.getSkill(70000045).getEffect(chr.getSkillLevel(70000045)).getSkillStats().getStats("nocoolProp"));
					if (hasNoCoolTime) {
						break;
					}
				}
			}
			if (!hasNoCoolTime && skillid != 35111002 && skillid != 61101002) {
				if (!chr.isEquilibrium() && !GameConstants.isDarkSkills(skillid)) {
					c.getSession().write(MainPacketCreator.skillCooldown(skillid, (skillid == 142121008) ? 45 * 1000 : effect.getCooldown()));
					ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, skillid), (skillid == 142121008) ? 45 * 1000 : effect.getCooldown());
					chr.addCooldown(skillid, System.currentTimeMillis(), (skillid == 142121008) ? 45 * 1000 : effect.getCooldown(), timer);
				}
			}
		} else if (skillid == 23111009 || skillid == 23111010) {
			if (skillid == 23111009 || skillid == 23111010) { // 엘리멘탈 나이트 - 쿨타임
																// 공유
				if (chr.skillisCooling(23111008)) {
					return;
				}
				c.getSession().write(MainPacketCreator.skillCooldown(23111008, effect.getCooldown()));
				ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 23111008), effect.getCooldown());
				chr.addCooldown(23111008, System.currentTimeMillis(), effect.getCooldown(), timer);
			}
		} else if (skillid == 5211015 || skillid == 5211016) {
			if (skillid == 5211015 || skillid == 5211016) { // 어셈블 크루 - 쿨타임 공유
				if (chr.skillisCooling(5211011)) {
					return;
				}
				c.getSession().write(MainPacketCreator.skillCooldown(5211011, effect.getCooldown()));
				ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 5211011), effect.getCooldown());
				chr.addCooldown(5211011, System.currentTimeMillis(), effect.getCooldown(), timer);
			}
		}

		switch (skillid) {
		case 36121002: // 홀로그램 그래피티 : 관통
		case 36121013: // 홀로그램 그래피티 : 역장
		case 36121014: { // 홀로그램 그래피티 : 지원
			Point pos = rh.readPos();
			MapleSummon summon = new MapleSummon(chr, skillid, pos, SummonMovementType.STATIONARY);
			chr.getMap().spawnSummon(summon, true, 20000);
			break;
		}
		case 101100100:
		case 101100101: { // 스로잉 웨폰
			Point pos = rh.readPos();
			MapleSummon summon = new MapleSummon(chr, 101100100, pos, SummonMovementType.ZEROWEAPON);
			chr.getMap().spawnSummon(summon, true, 5000);
			c.getSession().write(MainPacketCreator.resetActions());
			break;
		}
		case 23111008: { // 엘리멘탈 나이트
			skillid += Randomizer.nextInt(2);
			break;
		}
		case 25100009: { // 여우령
			rh.skip(1);
			int sn = rh.readInt();
			c.getPlayer().send(MainPacketCreator.absorbingFG(c.getPlayer().getId(), 25100010, sn));
			여우령 = 50;
			break;
		}
		case 25120110: // 불여우령
			rh.skip(1);
			int fsn = rh.readInt();
			c.getPlayer().send(MainPacketCreator.absorbingFG(c.getPlayer().getId(), 25120115, fsn));
			여우령 = 100;
			break;
		case 12001027:
		case 12001028: { // 파이어 워크
			c.getPlayer().getMap().broadcastMessage(MainPacketCreator.FireWork(chr));
			break;
		}
		case 12101025: { // 파이어 블링크
			Point position = rh.readPos();
			c.getPlayer().send(MainPacketCreator.FireBlink(chr.getId(), position));
			break;
		}
		case 12120013:
		case 12120014: { // 스피릿 오브 플레임
			rh.skip(7);
			if (chr.getSkillLevel(skillid) <= 0) {
				chr.teachSkill(skillid, (byte) 30, (byte) 30);
			}
			MapleSummon summon = chr.getSummons().get(12120013);
			MapleSummon summons = chr.getSummons().get(12120014);
			if (summon != null) {
				chr.getMap().broadcastMessage(MainPacketCreator.removeSummon(summon, true));
				chr.getMap().removeMapObject(summon);
				chr.removeVisibleMapObject(summon);
				chr.getSummons().remove(12120013);
			} else if (summons != null) {
				chr.getMap().broadcastMessage(MainPacketCreator.removeSummon(summons, true));
				chr.getMap().removeMapObject(summons);
				chr.removeVisibleMapObject(summons);
				chr.getSummons().remove(12120014);
			}
			MapleSummon tosummons = new MapleSummon(chr, skillid, chr.getPosition(), SummonMovementType.FLAME_SUMMON);
			tosummons.setPosition(chr.getPosition());
			chr.getMap().spawnSummon(tosummons, true, SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid)).getDuration());
			chr.getSummons().put(skillid, tosummons);
			tosummons.addHP(Integer.MAX_VALUE);
			final List<Triple<BuffStats, Integer, Boolean>> stat2 = Collections
					.singletonList(new Triple<BuffStats, Integer, Boolean>(BuffStats.IGNORE_DEFENCE_R, SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid)).getX(), false));
			chr.getClient().getSession().write(MainPacketCreator.giveBuff(skillid, SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid)).getDuration(), stat2,
					SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid)), null, SkillFactory.getSkill(skillid).getAnimationTime()));
			break;
		}
		case 1311013: { // 비홀더 도미넌트
			c.getSession().write(AdventurerSkill.giveBeholderDominant(1301013, 1311013, 0));
			c.getSession().write(MainPacketCreator.resetActions());
			break;
		}
		case 1311014: { // 비홀더 부가 스킬.
			if (skillid == 1311013) {
				c.getSession().write(AdventurerSkill.giveBeholderDominant(1301013, 1311013, 1311014));
			} else {
				c.getSession().write(AdventurerSkill.giveBeholderDominant(1301013, 1301013, 1311014));
			}
			c.getSession().write(MainPacketCreator.resetActions());
			break;
		}
		case 65111100: { // 소울시커
			rh.skip(4);
			int soulnum = rh.readByte();
			int scheck = 0;
			int scheck2 = 0;
			if (soulnum == 1) {
				scheck = rh.readInt();
			} else if (soulnum == 4) {
				scheck = rh.readInt();
				scheck2 = rh.readInt();
			}
			rh.skip(3);
			c.send(AngelicBusterSkill.SoulSeeker(chr, skillid, soulnum, scheck, scheck2));
			c.send(AngelicBusterSkill.unlockSkill());
			c.send(AngelicBusterSkill.showRechargeEffect());
			break;
		}
		case 2121052: // 메기도 플레임
		case 31221001: // 쉴드 체이싱
		case 35101002: // 호밍 미사일
		case 35110017: // 어드밴스트 호밍 미사일
		case 36001005: { // 핀포인트 로켓
			List<Integer> moblist = new ArrayList<Integer>();
			byte count = rh.readByte();
			for (int i = 0; i < count; i++) {
				moblist.add(i, rh.readInt());
			}
			if (skillid == 31221001) {
				c.send(MainPacketCreator.ShieldChacing(chr.getId(), moblist, 31221014));
			} else if (skillid == 36001005) {
				c.send(MainPacketCreator.PinPointRocket(chr.getId(), moblist));
			} else if (skillid == 2121052) {
				c.send(MainPacketCreator.MegidoFlameRe(chr.getId(), moblist.get(0)));
			} else if (skillid == 35101002 || skillid == 35110017) {
				c.send(MainPacketCreator.HomingMisile(chr.getId(), moblist, skillid));
			}
			break;
		}
		case 33001011: { // 서먼 재규어
			Point mpos = rh.readPos();
			MapleSummon summon = new MapleSummon(chr, skillid, mpos, SummonMovementType.SUMMON_JAGUAR);
			summon.setPosition(mpos);
			chr.getMap().spawnSummon(summon, true, Integer.MAX_VALUE);
			SkillStatEffect buffeffect = SkillFactory.getSkill(33001007).getEffect(skillLevel);
			buffeffect.applyTo(chr, null);
			break;
		}
		case 12111022: { // 마엘스트룸
			Point mpos = rh.readPos();
			rh.skip(3);
			int mobid = rh.readInt();
			MapleSummon summon = new MapleSummon(chr, skillid, mpos, SummonMovementType.STATIONARY);
			summon.setPosition(mpos);
			summon.setMaelstromId(chr.getMap().getMonsterByOid(mobid).getId());
			chr.getMap().spawnSummon(summon, true, effect.getDuration());
			break;
		}
		case 12101022: { // 번 앤 레스트
			int a = SkillFactory.getSkill(12101022).getEffect(chr.getSkillLevel(skillid)).getX();
			int mp = c.getPlayerStat().getMp();
			int Mmp = c.getPlayerStat().getMaxMp();
			c.getPlayerStat().setMp(mp + mp * (a / 100) < Mmp ? mp + mp * (a / 100) : Mmp);
			break;
		}
		case 4211006: { // 메소 익스플로젼
			rh.skip(3);
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MainPacketCreator.showBuffeffect(c.getPlayer().getId(), skillid, 1, c.getPlayer().getLevel(), skillLevel), false);
			List<MapleMapObject> drops = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 320000, Arrays.asList(MapleMapObjectType.ITEM));
			final List<MapleWorldMapItem> allmesos = new ArrayList<>();
			for (int i = 0; i < drops.size(); i++) { // 범위 내에 있는 1메소이며, 소유권이
														// 자신에게 있는 메소
				MapleWorldMapItem drop = (MapleWorldMapItem) drops.get(i);
				if (drop.getMeso() == 1 && drop.getOwner() == c.getPlayer().getId()) {
					allmesos.add(drop);
				}
			}
			final int maxmeso_count = Randomizer.rand(allmesos.isEmpty() ? 0 : 1, allmesos.size() > effect.getBulletCount() ? effect.getBulletCount() : allmesos.size());
			final List<Pair<Integer, Point>> mesos = new ArrayList<>();
			for (int i = 0; i < maxmeso_count; i++) {
				final int randmeso_remove = Randomizer.rand(0, allmesos.size() - 1);
				mesos.add(new Pair(allmesos.get(randmeso_remove).getObjectId(), allmesos.get(randmeso_remove).getPosition()));
				allmesos.remove(randmeso_remove);
			}
			final List<MapleMapObject> mobjects = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 320000, Arrays.asList(MapleMapObjectType.MONSTER));
			final List<Integer> moids = new ArrayList<>();
			final int randmob_count = Randomizer.rand(mobjects.isEmpty() ? 0 : 1, mobjects.size() > 10 ? 10 : mobjects.size());
			for (int i = 0; i < randmob_count; i++) {
				final int randmob_remove = Randomizer.rand(0, mobjects.size() - 1);
				moids.add(mobjects.get(randmob_remove).getObjectId());
				mobjects.remove(randmob_remove);
			}
			if (mesos.isEmpty() || moids.isEmpty()) {
				c.getSession().write(MainPacketCreator.resetActions());
				return;
			}
			MapleWorldMapItem remove;
			for (int i = 0; i < mesos.size(); i++) {
				remove = (MapleWorldMapItem) c.getPlayer().getMap().getMapObject(mesos.get(i).left);
				c.getPlayer().getMap().removeMapObject(remove);
				c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeItemFromMap(remove.getObjectId(), 0, c.getPlayer().getId()));
			}
			c.getPlayer().getMap().broadcastMessage(MainPacketCreator.giveMesoExplosion(c.getPlayer().getId(), moids, mesos));
			c.getSession().write(MainPacketCreator.resetActions());
			break;
		}
		}
		if (chr.lastUsedSkill + 100 > System.currentTimeMillis()) {
			chr.usedSkillFast++;
		} else {
			chr.usedSkillFast = 0;
		}
		chr.lastUsedSkill = System.currentTimeMillis();

		if (skillid == 5211011) { // 어셈블 크루
			switch (Randomizer.nextInt(2)) {
			case 0:
				skillid = 5211011;
				break;
			case 1:
				skillid = 5211015;
				break;
			case 2:
				skillid = 5211016;
				break;
			}
		}
		switch (skillid) {
		case 36111008:
			chr.giveSurPlus(10);
			break;
		case 36111003:
			chr.dualBrid = 0;
			break;
		case 36121054:
			chr.giveSurPlus(20);
			break;
		case 4221054:
			chr.send(MainPacketCreator.OnOffFlipTheCoin(false));
			chr.dualBrid = 0;
			break;
		case 21121058:
			chr.updateCombo((short) 500, System.currentTimeMillis());
			break;
		}

		if (skill != null) {
			if (skillid == 27121054) {
				effect.applyequilibriumBuff(chr, Randomizer.nextBoolean());
			} else if (skillid == 31211004) {
				chr.startDiabolicRecovery(effect);
			} else if (effect.getPowerEnergy() > 0) {
				if (chr.getBuffedValue(BuffStats.SURPLUS) != null) {
					chr.giveSurPlus(-effect.getPowerEnergy());
				}
			} else if (GameConstants.isKinesis(chr.getJob())) {
				chr.givePPoint(skillid);
			}
			switch (skillid) {
			case 1121001:
			case 1221001:
			case 1321001:
			case 9001020: // GM magnet
				byte number_of_mobs = rh.readByte();
				rh.skip(3);
				for (int i = 0; i < number_of_mobs; i++) {
					int mobId = rh.readInt();
					MapleMonster mob = chr.getMap().getMonsterByOid(mobId);
					if (mob != null) {
						mob.switchController(chr, mob.isControllerHasAggro());
					}
				}
				chr.getMap().broadcastMessage(chr, MainPacketCreator.showBuffeffect(chr.getId(), skillid, 1, rh.readByte(), skillLevel), chr.getPosition());
				break;
			case 30001061: { // 포획
				int mobid = rh.readInt();
				MapleMonster mob = chr.getMap().getMonsterByOid(mobid);
				if (mob.getHp() > mob.getMobMaxHp() / 2) {
					chr.send(MainPacketCreator.captureMob(false));
					chr.ea();
					return;
				}
				if (!Randomizer.isSuccess(95)) {
					chr.send(MainPacketCreator.captureMob(false));
					chr.ea();
					return;
				}
				chr.send(MainPacketCreator.updateJaguar(chr));
				chr.send(MainPacketCreator.captureMob(true));
				chr.send(MainPacketCreator.catchMonster(mob.getId(), (byte) 1));
				chr.setKeyValue2("CapturedJaguar", mob.getId());
				chr.getMap().killMonster(mob, chr, false, false, (byte) 1);
				chr.ea();
				break;
			}
			case 35111002: { // 마그네틱 필드
				byte entry = rh.readByte(); // 0,1,2 (first:0 second:1 third:2)
				if (entry == 2) {
					rh.skip(8);
				}
				Point pos = rh.readPos();
				MapleSummon summon = new MapleSummon(chr, 35111002, pos, SummonMovementType.STATIONARY);
				chr.getMap().spawnSummon(summon, true, 0);
				if (chr.getMap().countSummonSkill(chr, 35111002) == 3) {
					c.getPlayer().send(MainPacketCreator.resetActions());
					c.getSession().write(MainPacketCreator.skillCooldown(skillid, effect.getCooldown()));
					ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, skillid), effect.getCooldown());
					chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown(), timer);
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showMagneticConnect(c.getPlayer().getId(), chr.getMap().getSummonObjects(chr, 35111002)));
					c.getPlayer().send(MainPacketCreator.showMagneticConnect(c.getPlayer().getId(), chr.getMap().getSummonObjects(chr, 35111002)));
					MapTimer.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							for (MapleMapObject o : chr.getMap().getSummonObjects(chr, 35111002)) {
								MapleSummon summon2 = (MapleSummon) o;
								chr.getMap().broadcastMessage(MainPacketCreator.removeSummon(summon2, true));
								chr.getMap().removeMapObject(summon2);
								summon2.getOwner().removeVisibleMapObject(summon2);
								if (summon2.getOwner().getSummons().get(summon2.getSkill()) != null) {
									summon2.getOwner().getSummons().remove(summon2.getSkill());
								}
							}
						}
					}, 60000);
				}
				c.getSession().write(MainPacketCreator.resetActions());
				effect.applyTo(c.getPlayer(), pos);
				break;
			}
			default:
				Point pos = null;
				if (effect.isMagicDoor() || rh.available() == 7 || rh.available() == 8) {
					pos = rh.readPos();
				}
				if (effect.isMagicDoor()) { // Mystic Door
					if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
						effect.applyTo(c.getPlayer(), pos);
					} else {
						chr.dropMessage(5, "현재 위치에선 미스틱 도어를 소환할 수 없습니다.");
						chr.send(MainPacketCreator.resetActions());
					}
				} else {
					if (skillid >= 30010183 && skillid <= 30010186) { // 데몬점프
						chr.getMap().broadcastMessage(chr, MainPacketCreator.showBuffeffect(chr.getId(), skillid, 1, chr.getLevel(), skillLevel), chr.getPosition());
					}
					if (effect.parseMountInfo(c.getPlayer(), skill.getId()) != 0 && c.getPlayer().getBuffedValue(BuffStats.MONSTER_RIDING) == null && c.getPlayer().getDragon() != null) {
						c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeDragon(c.getPlayer().getId()));
						c.getPlayer().getMap().removeMapObject(c.getPlayer().getDragon());
						c.getPlayer().setDragon(null);
					}
					effect.applyTo(c.getPlayer(), pos);
				}
				break;
			}
		}
	}

	public static void closeRangeAttack(ReadingMaple rh, final MapleClient c, final MapleCharacter chr, boolean touchAttack) {

		if (c.getPlayer() == null || !chr.isAlive()) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}

		AttackInfo attack = DamageParse.parseDmgM(chr, rh, touchAttack);
		boolean mirror = chr.getBuffedValue(BuffStats.SHADOW_PARTNER) != null;
		int attackCount = ((chr.getJob() >= 430 && chr.getJob() <= 434) ? 2
				: (attack.skill == 61101002 || attack.skill == 61110211) ? 3 : (attack.skill == 61120007 || attack.skill == 61121217) ? 5 : 1);
		int skillLevel = attack.skillLevel != 0 ? attack.skillLevel : 1;

		ISkill skill = null;
		SkillStatEffect effect = null;
		if (GameConstants.SurfaceDamageSkillLink(attack.skill)) {
			for (int i = 0; i < attack.allDamage.size(); ++i) { // 마리수
				for (int x = 0; x < attack.allDamage.get(i).attack.size(); ++x) { // 뎀지수
					MapleMonster Target = chr.getMap().getMonsterByOid(attack.allDamage.get(i).objectid);
					Target.damage(chr, attack.allDamage.get(i).attack.get(x).left, false);
					if (Target.getHp() <= 0) {
						break;
					}
				}
			}
		}

		if (attack.skill != 0) {
			skill = SkillFactory.getSkill(attack.skill);
			effect = attack.getAttackEffect(chr, skillLevel, skill);
			if ((skill.getId() == 31011000 || skill.getId() == 31201000 || skill.getId() == 31211000 || skill.getId() == 31221000) && attack.targets > 0) {
				if (c.getPlayer().exeedCount < 20) {
					c.getPlayer().exeedCount++;
				}
				c.getPlayer().exeedAttackCount++;
				final List<Triple<BuffStats, Integer, Boolean>> stat = Collections.singletonList(
						new Triple<BuffStats, Integer, Boolean>(BuffStats.ENHANCED_MAXMP, Integer.valueOf(c.getPlayer().exeedAttackCount > 4 ? 4 : c.getPlayer().exeedAttackCount), false));
				c.getSession().write(MainPacketCreator.giveBuff(skill.getId(), 15000, stat, effect, null, skill.getAnimationTime()));
				final List<Triple<BuffStats, Integer, Boolean>> stat2 = Collections.singletonList(new Triple<BuffStats, Integer, Boolean>(BuffStats.LIFE_TIDAL, c.getPlayer().exeedCount, false));
				c.getSession().write(MainPacketCreator.giveBuff(30010230, 2100000000, stat2, effect, null, skill.getAnimationTime()));
				DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
				return;
			} else if (attack.skill == 61101002 || attack.skill == 61110211) { // 윌
																				// 오브
																				// 소드
				DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
				c.send(KaiserSkill.cancelWillofSword());
				SkillStatEffect realEffect = SkillFactory.getSkill(61101002).getEffect(c.getPlayer().getSkillLevel(61101002));
				c.getSession().write(MainPacketCreator.skillCooldown(61101002, realEffect.getCooldown()));
				ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 61101002), realEffect.getCooldown());
				chr.addCooldown(61101002, System.currentTimeMillis(), realEffect.getCooldown(), timer);
				chr.cancelEffectFromBuffStat(BuffStats.WILL_OF_SWORD, -1);
				return;
			} else if (attack.skill == 61120007 || attack.skill == 61121217) { // 어드밴스드
																				// 윌
																				// 오브
																				// 소드
																				// (트랜스피규레이션)
				effect = attack.getAttackEffect(chr, chr.getSkillLevel(attack.skill), skill);
				DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
				c.send(KaiserSkill.cancelWillofSword());
				SkillStatEffect realEffect = SkillFactory.getSkill(61101002).getEffect(c.getPlayer().getSkillLevel(61101002));
				c.getSession().write(MainPacketCreator.skillCooldown(61101002, realEffect.getCooldown()));
				ScheduledFuture<?> timer = BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 61101002), realEffect.getCooldown());
				chr.addCooldown(61101002, System.currentTimeMillis(), realEffect.getCooldown(), timer);
				chr.cancelEffectFromBuffStat(BuffStats.WILL_OF_SWORD, -1);
				return;
			}

			if ((skill != null) && (skill.getId() != 2100010)) { // 쿨타임 설정.
				effect = attack.getAttackEffect(chr, skillLevel, skill);
				attackCount = effect.getAttackCount();
				if (attack.skill == 11121055) {
					c.getSession().write(MainPacketCreator.skillCooldown(11121052, 90));
					chr.addCooldown(11121052, System.currentTimeMillis(), 90 * 1000, BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 11121052), effect.getCooldown()));
				} else if (attack.skill == 12121001) {
					c.getSession().write(MainPacketCreator.skillCooldown(attack.skill, 5));
					chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(),
							BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, attack.skill), effect.getCooldown()));
				} else if (SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown() > 0 && !touchAttack) {
					if (attack.skill == 1321013) {
						if (chr.getBuffedValue(BuffStats.IGNORE_DEFENCE_R) == null && chr.getBuffedValue(BuffStats.REIN_CANATION) == null) {
							c.getSession().write(MainPacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
							chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(),
									BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, attack.skill), effect.getCooldown()));
						}
					} else if (attack.skill == 65121052) {
						BuffTimer.getInstance().schedule(new Runnable() {
							@Override
							public void run() {
								SkillStatEffect sneffect = SkillFactory.getSkill(65121052).getEffect(c.getPlayer().getSkillLevel(65121052));
								c.getSession().write(MainPacketCreator.skillCooldown(65121052, sneffect.getCooldown()));
								chr.addCooldown(65121052, System.currentTimeMillis(), sneffect.getCooldown(),
										BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 65121052), sneffect.getCooldown()));
							}
						}, 12000);
					} else if (attack.skill == 5121013) {
						BuffTimer.getInstance().schedule(new Runnable() {
							@Override
							public void run() {
								SkillStatEffect sneffect = SkillFactory.getSkill(5121013).getEffect(c.getPlayer().getSkillLevel(5121013));
								c.getSession().write(MainPacketCreator.skillCooldown(5121013, sneffect.getCooldown()));
								chr.addCooldown(5121013, System.currentTimeMillis(), sneffect.getCooldown(),
										BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 5121013), sneffect.getCooldown()));
							}
						}, 12000);
					} else if (attack.skill == 4221052) {
						BuffTimer.getInstance().schedule(new Runnable() {
							@Override
							public void run() {
								SkillStatEffect sneffect = SkillFactory.getSkill(4221052).getEffect(c.getPlayer().getSkillLevel(4221052));
								c.getSession().write(MainPacketCreator.skillCooldown(4221052, sneffect.getCooldown()));
								chr.addCooldown(4221052, System.currentTimeMillis(), sneffect.getCooldown(),
										BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, 4221052), sneffect.getCooldown()));
							}
						}, 12000);
					} else {
						if (!chr.isEquilibrium() && !GameConstants.isDarkSkills(attack.skill) && skill.getId() != 31221001) {
							c.getSession().write(MainPacketCreator.skillCooldown(attack.skill, SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown()));
							chr.addCooldown(attack.skill, System.currentTimeMillis(), SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown(),
									BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, attack.skill), SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown()));
						}
					}
				}

				if (attack.skill == 31111003) { // 블러디 레이븐 피회복
					int recover = (int) (chr.getStat().getCurrentMaxHp() * (effect.getX() / 100.0D));
					chr.addHP(recover);
				}

				if (GameConstants.isAngelicBuster(chr.getJob())) {
					int Recharge = effect.getOnActive();
					if (Recharge > -1) {
						if (Randomizer.isSuccess(Recharge)) {
							c.send(AngelicBusterSkill.unlockSkill());
							c.send(AngelicBusterSkill.showRechargeEffect());
						}
					}
				}

				if (chr.getBuffedValue(BuffStats.HEAD_EFFECT) != null) {
					int stateid = 0;
					try {
						/* 솔루나 타임 딜레이 시작 */
						Robot robot = new Robot();
						robot.delay(180);
						/* 솔루나 타임 딜레이 종료 */
						if (chr.getBuffedValue(BuffStats.DMG_DEC, 11101022) != null) {
							chr.cancelEffectFromBuffStat(BuffStats.DMG_DEC, 11101022);
							stateid = 11111022;
						} else if (chr.getBuffedValue(BuffStats.DMG_DEC, 11111022) != null) {
							chr.cancelEffectFromBuffStat(BuffStats.DMG_DEC, 11111022);
							stateid = 11101022;
						}
						SkillStatEffect stateeffect = SkillFactory.getSkill(stateid).getEffect(chr.getSkillLevel(stateid));
						stateeffect.applyTo(chr);
					} catch (AWTException ex) {
						if (ServerConstants.realese) {
							ex.printStackTrace();
						}
					}
				}
			}
		}

		attack = DamageParse.Modify_AttackCrit(attack, chr, 1, effect);
		attackCount *= mirror ? 2 : 1;

		int numFinisherOrbs = 0;
		Integer comboBuff = chr.getBuffedValue(BuffStats.COMBO);

		if (isFinisher(attack.skill) > 0) {
			if (comboBuff != null) {
				numFinisherOrbs = comboBuff.intValue() - 1;
			}
			chr.handleOrbconsume(isFinisher(attack.skill));
		} else if ((attack.targets > 0) && (comboBuff != null)) {
			switch (chr.getJob()) {
			case 110:
			case 111:
			case 112:
			case 2411:
			case 2412:
				if (attack.skill == 1111008)
					break;
				chr.handleOrbgain();
			}
		}
		if ((isFinisher(attack.skill) > 0) && (numFinisherOrbs == 0)) {
			return;
		}

		chr.checkFollow();
		if (attack.skill == 21101003) {
			for (AttackPair ap : attack.allDamage) {
				chr.getMap().broadcastMessage(chr, MobPacket.damageMonster(ap.objectid, ap.attack.get(0).getLeft()), false);
			}
		} else {
			chr.getMap().broadcastMessage(chr, MainPacketCreator.attack(RecvPacketOpcode.CLOSE_RANGE_ATTACK, chr, chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed,
					attack.allDamage, attack.value, attack.position, (byte) 0, 0, chr.getLevel(), 0), chr.getPosition());
		}
		DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, ((GameConstants.isKaiser(chr.getJob()) || touchAttack) ? null : effect),
				mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
	}

	public static void rangedAttack(ReadingMaple rh, MapleClient c, MapleCharacter chr) {
		if (c.getPlayer() == null || chr == null || !chr.isAlive()) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}

		AttackInfo attack = DamageParse.parseDmgR(c.getPlayer(), rh);
		int bulletCount = 1;
		int skillLevel = attack.skillLevel;

		ISkill skill = SkillFactory.getSkill(attack.skill);
		SkillStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
		MapleMap map = chr.getMap();
		if (GameConstants.SurfaceDamageSkillLink(attack.skill)) {
			for (int i = 0; i < attack.allDamage.size(); ++i) { // 마리수
				for (int x = 0; x < attack.allDamage.get(i).attack.size(); ++x) { // 뎀지수
					MapleMonster Target = chr.getMap().getMonsterByOid(attack.allDamage.get(i).objectid);
					Target.damage(chr, attack.allDamage.get(i).attack.get(x).left, false);
					if (Target.getHp() <= 0) {
						break;
					}
				}
			}
		}

		if (GameConstants.isAngelicBuster(chr.getJob())) {
			int Recharge = effect.getOnActive();
			if (Recharge > -1) {
				if (Randomizer.isSuccess(Recharge)) {
					c.send(AngelicBusterSkill.unlockSkill());
					c.send(AngelicBusterSkill.showRechargeEffect());
				}
			}
		}

		attack = DamageParse.Modify_AttackCrit(attack, chr, 2, effect);
		Integer ShadowPartner = chr.getBuffedValue(BuffStats.SHADOW_PARTNER);
		if (ShadowPartner != null) {
			bulletCount *= 2;
		}
		int projectile = 0, visProjectile = 0;

		if (chr.getBuffedValue(BuffStats.SOUL_WATK) == null && attack.skill != 4111004 && !GameConstants.isMercedes(chr.getJob()) && (GameConstants.isUsingArrowForBowJob(chr.getJob())
				|| GameConstants.isUsingArrowForCrossBowJob(chr.getJob()) || GameConstants.isUsingStarJob(chr.getJob()) || GameConstants.isUsingBulletJob(chr.getJob()))) {

			if (attack.slot == 0) {
				for (IItem item : chr.getInventory(MapleInventoryType.USE).list()) {
					if (GameConstants.isUsingBulletJob(chr.getJob()) && GameConstants.isBullet(item.getItemId())) {
						projectile = item.getItemId();
						break;
					} else if (GameConstants.isUsingStarJob(chr.getJob()) && GameConstants.isThrowingStar(item.getItemId())) {
						projectile = item.getItemId();
						break;
					} else if (GameConstants.isUsingArrowForBowJob(chr.getJob()) && GameConstants.isArrowForBow(item.getItemId())) {
						projectile = item.getItemId();
						break;
					} else if (GameConstants.isUsingArrowForCrossBowJob(chr.getJob()) && GameConstants.isArrowForCrossBow(item.getItemId())) {
						projectile = item.getItemId();
						break;
					}
				}
			} else {
				projectile = chr.getInventory(MapleInventoryType.USE).getItem(attack.slot).getItemId();
			}
			boolean termed = false;
			if (projectile == 0) {
				if (chr.getJob() >= 3500 && chr.getJob() <= 3512) {
					projectile = 2330000;
				}
				if (chr.getJob() == 501 || (chr.getJob() >= 530 && chr.getJob() <= 533)) {
					projectile = 2330000;
				}
				if (projectile == 0) {
					projectile = 0;
				}
				termed = true;
			}

			if (attack.csstar > 0) {
				visProjectile = chr.getInventory(MapleInventoryType.CASH).getItem((short) attack.csstar).getItemId();
			} else {
				visProjectile = projectile;
			}
			if (chr.getBuffedValue(BuffStats.SPIRIT_CLAW) == null && !termed) {
				IItem ipp = chr.getInventory(MapleInventoryType.USE).getItem(attack.slot);
				int bulletConsume = bulletCount;
				if (effect != null && effect.getBulletConsume() != 0) {
					bulletConsume = effect.getBulletConsume() * (ShadowPartner != null ? 2 : 1);
				}
				if ((chr.getJob() == 411 || chr.getJob() == 412) && bulletConsume > 0 && ipp.getQuantity() < ItemInformation.getInstance().getSlotMax(projectile)
						&& chr.getBuffedValue(BuffStats.SPIRIT_CLAW) == null) {
					final ISkill expert = SkillFactory.getSkill(4110012);
					if (chr.getSkillLevel(expert) > 0) {
						final SkillStatEffect eff = expert.getEffect(chr.getSkillLevel(expert));
						if (eff.makeChanceResult()) {
							ipp.setQuantity((short) (ipp.getQuantity() + 1));
							c.getSession().write(MainPacketCreator.updateInventorySlot(MapleInventoryType.USE, ipp, false));
							bulletConsume = 0;
						}
					}
				}
				if ((bulletConsume > 0) && (!GameConstants.isMechanic(chr.getJob()))) {
					InventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true);
				}
			}
		}
		if (attack.skill != 0 && SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown() > 0) {
			c.getSession().write(MainPacketCreator.skillCooldown(attack.skill, SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown()));
			chr.addCooldown(attack.skill, System.currentTimeMillis(), SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown(),
					BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, attack.skill), SkillFactory.getSkill(attack.skill).getEffect(skillLevel).getCooldown()));
		}
		chr.getMap().broadcastMessage(chr, MainPacketCreator.attack(RecvPacketOpcode.RANGED_ATTACK, chr, chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed,
				attack.allDamage, attack.value, attack.position, (byte) 0, 0, chr.getLevel(), visProjectile), chr.getPosition());
		DamageParse.applyAttack(attack, skill, chr, bulletCount, null, ShadowPartner != null ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED);
	}

	public static void MagicDamage(ReadingMaple rh, MapleClient c, MapleCharacter chr, boolean isFlame) {
		if (c.getPlayer() == null || chr == null || !chr.isAlive()) {
			c.getSession().write(MainPacketCreator.resetActions());
			return;
		}

		AttackInfo attack = DamageParse.parseDmgMa(rh, isFlame);
		int bulletCount = 1;
		int skillLevel = attack.skillLevel;

		ISkill skill = SkillFactory.getSkill(attack.skill);
		SkillStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
		MapleMap map = chr.getMap();
		if (GameConstants.SurfaceDamageSkillLink(attack.skill)) {
			for (int i = 0; i < attack.allDamage.size(); ++i) { // 마리수
				for (int x = 0; x < attack.allDamage.get(i).attack.size(); ++x) { // 뎀지수
					MapleMonster Target = chr.getMap().getMonsterByOid(attack.allDamage.get(i).objectid);
					Target.damage(chr, attack.allDamage.get(i).attack.get(x).left, false);
					if (Target.getHp() <= 0) {
						break;
					}
				}
			}
		}

		if ((attack.skill != 0) && (GameConstants.isKinesis(chr.getJob()))) {
			c.getPlayer().givePPoint(attack.skill);
		}

		chr.checkFollow();
		attack = DamageParse.Modify_AttackCrit(attack, chr, 3, effect);

		if (attack.skill != 0 && effect.getCooldown() > 0) {
			if (!chr.isEquilibrium() && !GameConstants.isDarkSkills(attack.skill)) {
				c.getSession().write(MainPacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
				chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(), BuffTimer.getInstance().schedule(new CancelCooldownAction(chr, attack.skill), effect.getCooldown()));
			}
		}

		chr.getMap().broadcastMessage(chr, MainPacketCreator.attack(RecvPacketOpcode.MAGIC_ATTACK, chr, chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed,
				attack.allDamage, attack.value, attack.position, (byte) 0, attack.charge, chr.getLevel(), 0), chr.getPosition());

		switch (attack.skill) {
		case 27101100: // 실피드 랜서
		case 27101202: // 보이드 프레셔
		case 27111100: // 스펙트럴 라이트
		case 27111202: // 녹스피어
		case 27121100: // 라이트 리플렉션
		case 27121202: // 아포칼립스
		case 2121006:
		case 2221003:
		case 2221006:
		case 2221007:
		case 2221012:
		case 2321007: // 엔젤 레이
		case 2111003: // 포이즌 미스트
		case 2121003: // 미스트 이럽션
		case 22181002: // 다크포그
			bulletCount = effect.getAttackCount();
			DamageParse.applyAttack(attack, skill, chr, bulletCount, effect, AttackType.RANGED);
			break;
		default:
			DamageParse.applyAttackMagic(attack, skill, c.getPlayer(), effect);
			break;
		}
	}

	public static void WheelOfFortuneEffect(int itemId, MapleCharacter chr) {
		switch (itemId) {
		case 5510000: {
			if (!chr.isAlive()) {
				chr.getMap().broadcastMessage(chr, MainPacketCreator.showSpecialEffect(chr.getId(), itemId), false);
			}
			break;
		}
		}
	}

	public static void DropMeso(int meso, MapleCharacter chr) {
		if (!chr.isAlive() || (meso < 10 || meso > 50000) || (meso > chr.getMeso())) {
			chr.getClient().getSession().write(MainPacketCreator.resetActions());
			return;
		}
		chr.gainMeso(-meso, false, true);
		chr.getMap().spawnMesoDrop(meso, chr.getPosition(), chr, chr, true, (byte) 0);
	}

	public static void ChangeEmotion(int emote, MapleCharacter chr) {
		if (emote > 0) {
			chr.getMap().broadcastMessage(chr, MainPacketCreator.facialExpression(chr, emote), false);
		}
	}

	public static void ChangeEmotionAndroid(int emote, MapleCharacter chr) {
		if (emote > 0) {
			chr.getMap().broadcastMessage(chr, AndroidPacket.showAndroidEmotion(chr.getId(), emote), false);
		}
	}

	public static void Heal(ReadingMaple rh, MapleCharacter chr) {
		rh.skip(8);
		int healHP = rh.readShort();
		int healMP = rh.readShort();
		MapleCharacterStat stats = chr.getStat();
		if (stats.getHp() <= 0) {
			return;
		}
		if (healHP != 0) {
			chr.addHP(healHP);
		}
		if (healMP != 0) {
			chr.addMP(healMP);
		}
	}

	public static final void MoveAndroid(final ReadingMaple rh, final MapleClient ha, final MapleCharacter hp) {
		rh.skip(12); // v192 +4byte.
		final List<LifeMovementFragment> res = MovementParse.parseMovement(rh);
		if (res != null && hp != null && res.size() != 0 && hp.getMap() != null && hp.getAndroid() != null) { // map
																												// crash
																												// hack
			final Point pos = new Point(hp.getAndroid().getPosition());
			hp.getAndroid().updatePosition(res);
			hp.getMap().broadcastMessage(hp, AndroidPacket.moveAndroid(hp.getId(), pos, res), false);
		}
	}

	public static void MovePlayer(ReadingMaple rh, MapleClient c, MapleCharacter chr) {
		Point Original_Pos = chr.getPosition(); // 4 bytes Added on v.80 MSEA
		rh.skip(22); // v192 Unknown +4byte.
		List<LifeMovementFragment> res = null;
		try {
			res = MovementParse.parseMovement(rh);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			if (!ServerConstants.realese) {
				aioobe.printStackTrace();
			}
			logger.debug("Movement Parse Error : {}", rh.toString());
		}
		if (res != null) { // TODO more validation of input data
			MapleMap map = c.getPlayer().getMap();
			if (chr.isHidden()) {
				chr.setLastRes(res);
			} else {
				map.broadcastMessage(chr, MainPacketCreator.movePlayer(chr.getId(), res, Original_Pos), false);
			}
			MovementParse.updatePosition(res, chr, 0);
			Point pos = chr.getTruePosition();
			map.movePlayer(chr, chr.getPosition());

			if ((chr.getFollowId() > 0) && (chr.isFollowOn()) && (chr.isFollowInitiator())) {
				MapleCharacter fol = map.getCharacterById_InMap(chr.getFollowId());
				if (fol != null) {
					Point original_pos = fol.getPosition();
					fol.getClient().getSession().write(MainPacketCreator.moveFollow(Original_Pos, original_pos, pos, res));
					MovementParse.updatePosition(res, fol, 0);
					map.movePlayer(fol, pos);
					map.broadcastMessage(fol, MainPacketCreator.movePlayer(fol.getId(), res, Original_Pos), false);
				} else {
					chr.checkFollow();
				}

			}
		} else {
			chr.ea();
		}
	}

	public static void ChangeMapSpecial(String portal_name, MapleClient c, MapleCharacter chr) {
		MaplePortal portal = chr.getMap().getPortal(portal_name);
		if (portal != null) {
			portal.enterPortal(c);
		}
	}

	public static void ChangeMap(ReadingMaple rh, MapleClient c, MapleCharacter chr) {
		if (rh.available() != 0) {
			rh.skip(7); // 1 = from dying 2 = regular portals
			int targetid = rh.readInt();
			MaplePortal portal = chr.getMap().getPortal(rh.readMapleAsciiString());
			if (rh.available() >= 7) {
				rh.skip(4);
			}
			if (chr.getMapId() == 109090300) {
				chr.dropMessage(1, "술래잡기가 다 끝날 때까지 기다려주세요! 보상있습니다.");
				c.getSession().write(MainPacketCreator.resetActions());
				return;
			}
			boolean wheel = rh.readShort() > 0;
			if (targetid != -1 && !chr.isAlive()) {
				if (chr.getEventInstance() != null) {
					chr.getEventInstance().revivePlayer(chr);
				}
				chr.setStance(0);
				if (wheel && chr.getEventInstance() == null) {
					if (chr.haveItem(5510000, 1, false, true)) { // Wheel of
																	// Fortune
						chr.getStat().setHp((chr.getStat().getMaxHp() / 100) * 40);
						InventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
						MapleMap to = chr.getMap();
						chr.changeMap(to, to.getPortal(0));
					} else {
						chr.getStat().setHp(50);
						MapleMap to = chr.getMap().getReturnMap();
						chr.changeMap(to, to.getPortal(0));
						if (chr.getParty() != null) {
							if (chr.getParty().getExpedition() != null && chr.getParty().getExpedition().getLastBossMap() != -1) {
								chr.getParty().getExpedition().addDeadChar(chr.getId());
							}
						}
					}
				} else {
					chr.getStat().setHp(50);
					MapleMap to = chr.getMap().getReturnMap();
					chr.changeMap(to, to.getPortal(0));
					if (chr.getParty() != null) {
						if (chr.getParty().getExpedition() != null && chr.getParty().getExpedition().getLastBossMap() != -1) {
							chr.getParty().getExpedition().addDeadChar(chr.getId());
						}
					}
				}
			} else if (targetid != -1 && chr.isGM()) {
				if (chr.getEventInstance() == null) {
					MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
					chr.changeMap(to, to.getPortal(0));
				} else {
					MapleMap to = chr.getEventInstance().getMapFactory().getMap(targetid);
					chr.changeMap(to, to.getPortal(0));
				}
			} else {
				if (portal != null) {
					portal.enterPortal(c);
				} else {
					c.getSession().write(MainPacketCreator.resetActions());
				}
			}
		}
	}

	public static void InnerPortal(ReadingMaple rh, MapleClient c, MapleCharacter chr) {
		MaplePortal portal = c.getPlayer().getMap().getPortal(rh.readMapleAsciiString());
		int toX = rh.readShort();
		int toY = rh.readShort();

		if (portal == null) {
			c.disconnect(true, false);
			return;
		}
		chr.getMap().movePlayer(chr, new Point(toX, toY));
		chr.checkFollow();
	}

	public static void Agi_Buff(ReadingMaple rh, MapleClient c) {
		int skill = rh.readInt();
		if (c.getPlayer().getSkillLevel(skill) > 0) {
			SkillStatEffect eff = SkillFactory.getSkill(skill).getEffect(c.getPlayer().getSkillLevel(skill));
			if (eff.makeChanceResult()) {
				eff.applyTo(c.getPlayer());
			}
		}
	}

	public static void RoomChange(ReadingMaple rh, MapleClient c, MapleCharacter player) {
		if (player.getMapId() < 910000000 || player.getMapId() > 910000022) {
			c.getPlayer().ea();
			return;
		}
		byte channel = rh.readByte();
		int targetMap = rh.readInt();
		MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetMap);

		if (c.getChannel() != channel) {
			if (c.getPlayer().getLastCC() + 10000 > System.currentTimeMillis()) {
				c.getPlayer().message(5, "채널 이동은 10초마다 가능합니다.");
				c.getSession().write(MainPacketCreator.resetActions());
				return;
			}
			c.getPlayer().crossChannelWarp(c, targetMap, channel);
		} else {
			player.changeMap(to, to.getPortal("sp"));
		}
	}

	public static void makerSkill(ReadingMaple rh, MapleClient c) {
		ItemInformation ii = ItemInformation.getInstance();
		int type = rh.readInt();// type: 1 = make something, 4 = dissassemble, 3
								// = make monster crystals
		int toCreate = rh.readInt();
		switch (type) {
		case 1:
			MakerItemFactory.MakerItemCreateEntry recipe = MakerItemFactory.getItemCreateEntry(toCreate);
			if (!canCreate(c, recipe) || c.getPlayer().getInventory(ii.getInventoryType(toCreate)).isFull()) {
				c.getPlayer().dropMessage(1, "해당 아이템이 없거나 인벤토리가 꽉 찼습니다.");
				return;
			}
			c.getPlayer().gainMeso(-recipe.getCost(), false);
			for (Pair<Integer, Integer> p : recipe.getReqItems()) {
				int toRemove = p.getLeft();
				InventoryManipulator.removeById(c, ii.getInventoryType(toRemove), toRemove, p.getRight(), false, false);
			}
			if (ii.getInventoryType(toCreate) == MapleInventoryType.EQUIP) {
				boolean prodStim = rh.readByte() == 1; // 1 = production manual
														// used, 0 = not
				int gemz = rh.readShort(); // amount of gems used
				rh.readShort(); // O_o
				Equip item = (Equip) ii.getEquipById(toCreate);
				if (prodStim) {
					int prodId = recipe.getcatalyst();
					if (prodId == -1) {
						c.getPlayer().dropMessage(1, "Something went wrong, please notify a GM about this issue.");
						return;
					}
					if (!c.getPlayer().haveItem(prodId)) { // meaning he tried
															// to packet edit,
															// feel free to
															// autoban here
						return;
					}
					if (new Random().nextInt(9) < 1) { // 10% fail rate when u
														// use a stimulator
						item = null;
					} else {
						item = ii.randomizeStats(item);
					}
					InventoryManipulator.removeById(c, MapleInventoryType.ETC, prodId, 1, false, false);
				}
				for (int i = 0; i < gemz; i++) {
					int gem = rh.readInt();
					if (c.getPlayer().haveItem(gem)) {
						InventoryManipulator.removeById(c, MapleInventoryType.ETC, gem, 1, false, false);
						ii.addCrystalEffect(item, gem);
					} else { // he/she tried to packet edit
						return;
					}
				}
				if (item != null) {
					InventoryManipulator.addFromDrop(c, item, true);
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.getScrollEffect(c.getPlayer().getId(), IEquip.ScrollResult.SUCCESS));
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showSpecialEffect(0x11)); // 1.2.251+,
																										// (+1)
					c.getPlayer().dropMessage(1, "제작 성공하였습니다.");
				} else {
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.getScrollEffect(c.getPlayer().getId(), IEquip.ScrollResult.FAIL));
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showSpecialEffect(0x11)); // 1.2.251+,
																										// (+1)
					c.getPlayer().dropMessage(1, "Failed attempt to create your item.");
				}
			} else { // strengthening crystals O:
				Pair<Integer, Short> reward = recipe.getRandomReward();
				InventoryManipulator.addById(c, reward.getLeft(), reward.getRight());
				c.getSession().write(MainPacketCreator.getShowItemGain(reward.getLeft(), reward.getRight(), true));
				c.getPlayer().getMap()
						.broadcastMessage(MainPacketCreator.getScrollEffect(c.getPlayer().getId(), reward.getLeft() >= toCreate ? IEquip.ScrollResult.SUCCESS : IEquip.ScrollResult.FAIL));
				c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showSpecialEffect(0x11)); // 1.2.251+,
																									// (+1)
				c.getPlayer().dropMessage(1, reward.getLeft() >= toCreate ? "Congratulations! You've succeeded in the making of the item! You've made " : "Failed attempt to create your item.");
			}
			break;
		case 3:
			// monster crystal making
			if (c.getPlayer().getItemQuantity(toCreate, false) >= 100) {
				int lvl = ii.getETCMonsLvl(toCreate);
				if (lvl != -1) {
					InventoryManipulator.removeById(c, MapleInventoryType.ETC, toCreate, 100, false, false);
					InventoryManipulator.addById(c, Math.min(Math.max(5, (int) Math.ceil(lvl / 10.0)) - 5 + 4260000, 4260008), (short) 1);
					c.getPlayer().dropMessage(1, "Congratulations. You've made 1 Monster Crystals!");
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showSpecialEffect(0x11)); // 1.2.251+,
																										// (+1)
					c.getPlayer().getMap().broadcastMessage(MainPacketCreator.getScrollEffect(c.getPlayer().getId(), IEquip.ScrollResult.SUCCESS));
				} else {
					c.getPlayer().dropMessage(1, "You cannot use these items to make a monster crystal!");
				}
			}
			break;
		case 4:
			// dissassemble
			rh.readInt(); // No idea what this is, it's always 1 so maybe the
							// amount? O:
			short slot = rh.readShort();
			if (c.getPlayer().haveItem(toCreate)) {
				InventoryManipulator.removeFromSlot(c, slot >= 0 ? MapleInventoryType.EQUIP
						: MapleInventoryType.EQUIPPED/*
														 * I'm not sure u can
														 * put one of ur
														 * equipped items but
														 * w.e not taking risks
														 * :D
														 */, (byte) slot, (short) 1, false);
				int itemToGain = ((ii.getReqLevel(toCreate) - 50) / 10) + 4260000;
				int amount = ii.getWeaponType(toCreate) == MapleWeaponType.NOT_A_WEAPON ? new Random().nextInt(15) + 6 : new Random().nextInt(15) + 20; // I
																																						// never
																																						// checked
																																						// the
																																						// real
																																						// amounts
																																						// but
																																						// that's
																																						// about
																																						// what
																																						// i
																																						// Maple
																																						// in
																																						// GMS
																																						// O_o
				InventoryManipulator.addById(c, itemToGain, (short) amount);
				c.getPlayer().dropMessage(1, "Congratulations. You've made " + amount + " Monster Crystals!");
				c.getPlayer().getMap().broadcastMessage(MainPacketCreator.getScrollEffect(c.getPlayer().getId(), IEquip.ScrollResult.SUCCESS));
				c.getPlayer().getMap().broadcastMessage(MainPacketCreator.showSpecialEffect(0x11)); // 1.2.251+,
																									// (+1)
				c.getSession().write(MainPacketCreator.getShowItemGain(itemToGain, (short) amount, true));
			}
			break;
		default:
			break;
		}
	}

	public static boolean canCreate(MapleClient c, MakerItemFactory.MakerItemCreateEntry recipe) {
		return hasItems(c, recipe) && c.getPlayer().getMeso() >= recipe.getCost() && c.getPlayer().getLevel() >= recipe.getReqLevel()
				&& c.getPlayer().getSkillLevel(c.getPlayer().getJob() / 1000 * 1000 + 1007) >= recipe.getReqSkillLevel();
	}

	public static boolean hasItems(MapleClient c, MakerItemFactory.MakerItemCreateEntry recipe) {
		for (Pair<Integer, Integer> p : recipe.getReqItems()) {
			int itemId = p.getLeft();
			if (c.getPlayer().getInventory(ii.getInventoryType(itemId)).countById(itemId) < p.getRight()) {
				return false;
			}
		}
		return true;
	}

	public static void subSummonAction(ReadingMaple rh, MapleClient c) {
		short check = rh.readShort();
		if (check == 3) {
			int cid = rh.readInt();
			rh.skip(4);
			int skillid = rh.readInt();
			rh.skip(4);
			Point point = rh.readIntPos();
			MoveSubSummon moveSummons = new MoveSubSummon(c.getPlayer(), cid, check, skillid, point);
			c.getPlayer().send(MainPacketCreator.moveSubSummon(c.getPlayer(), moveSummons));
		} else if (check != 4) {
			int cid = rh.readInt();
			int value = rh.readInt();
			byte value2 = rh.readByte();
			Point point_f = rh.readPos();
			Point point_s = null;
			if (value2 != 4) {
				point_s = rh.readPos();
			}
			short secondValue = rh.readShort();
			int skillid = rh.readInt();
			int skillLevel = rh.readInt();
			int time = rh.readInt();
			short strange3 = rh.readShort();
			MapleSubSummon subSummon = new MapleSubSummon(c.getPlayer(), check, cid, value, (byte) value2, point_f, point_s, secondValue, skillid, skillLevel, strange3, time);
			c.getPlayer().setSubSummon(subSummon);
			c.getPlayer().send(MainPacketCreator.spawnSubSummon(c.getPlayer(), subSummon));
		}
	}

	public static void getHyperSkill(ReadingMaple rh, MapleClient c) {
		String value = rh.readMapleAsciiString();
		if (value.equals("honorLeveling")) {
			return;
		}
		int array = rh.readInt();
		int mode = rh.readInt();
		c.send(MainPacketCreator.updateHyperSp(value, array, mode, 1));
	}

	public static void SetFreeJob(final ReadingMaple rh, final MapleCharacter chr) {
		int jobid = rh.readInt();
		int jobcoin = 0;
		long meso = 0;
		rh.skip(1);
		if (chr.getLevel() >= 206) {
			jobcoin = 50;
			meso = 1096700000;
		} else if (chr.getLevel() >= 205) {
			jobcoin = 45;
			meso = 967600000;
		} else if (chr.getLevel() >= 203) {
			jobcoin = 40;
			meso = 886500000;
		} else if (chr.getLevel() >= 200) {
			jobcoin = 35;
			meso = 795700000;
		} else if (chr.getLevel() >= 188) {
			jobcoin = 30;
			meso = 687500000;
		} else if (chr.getLevel() >= 167) {
			jobcoin = 25;
			meso = 567300000;
		} else if (chr.getLevel() >= 160) {
			jobcoin = 20;
			meso = 437500000;
		} else if (chr.getLevel() >= 142) {
			jobcoin = 15;
			meso = 326800000;
		} else if (chr.getLevel() >= 138) {
			jobcoin = 10;
			meso = 215400000;
		} else if (chr.getLevel() >= 131) {
			jobcoin = 8;
			meso = 10108000;
		} else if (chr.getLevel() >= 130) {
			jobcoin = 6;
			meso = 8750000;
		} else if (chr.getLevel() >= 126) {
			jobcoin = 4;
			meso = 5270000;
		} else if (chr.getLevel() >= 112) {
			jobcoin = 2;
			meso = 3580000;
		} else {
			jobcoin = 1;
			meso = 1830000;
		}
		if (chr.haveItem(4310086, jobcoin, false, false)) {
			chr.gainItem(4310086, (short) -jobcoin, false, 0, null);
			chr.zerooskill(chr.getJob());
			chr.zerooskill(chr.getJob() - 1);
			chr.zerooskill(chr.getJob() - 2);
			chr.changeJob(jobid);
			chr.maxskill(chr.getJob());
			chr.maxskill(chr.getJob() - 1);
			chr.maxskill(chr.getJob() - 2);
			chr.getClient().getSession().write(MainPacketCreator.getNPCTalk(9010000, (byte) 0, "당신은 이제부터 새로운 직업을 갖게 되었습니다.", "00 00", (byte) 0));
			chr.getClient().getSession().write(MainPacketCreator.resetActions());
		} else {
			if (chr.getMeso() >= meso) {
				chr.gainMeso(-meso, false);
				chr.zerooskill(chr.getJob());
				chr.zerooskill(chr.getJob() - 1);
				chr.zerooskill(chr.getJob() - 2);
				chr.changeJob(jobid);
				chr.maxskill(chr.getJob());
				chr.maxskill(chr.getJob() - 1);
				chr.maxskill(chr.getJob() - 2);
				chr.getClient().getSession().write(MainPacketCreator.getNPCTalk(9010000, (byte) 0, "당신은 이제부터 새로운 직업을 갖게 되었습니다.", "00 00", (byte) 0));
				chr.getClient().getSession().write(MainPacketCreator.resetActions());
			} else {
				chr.getClient().getSession().write(MainPacketCreator.getNPCTalk(9010000, (byte) 0, "새로운 직업을 선택하기 위한 메소가 부족합니다.", "00 00", (byte) 0));
				chr.getClient().getSession().write(MainPacketCreator.resetActions());
			}
		}
	}

	public static void getStarPlanetRank(final ReadingMaple rh, final MapleCharacter chr) {
		List name = new LinkedList();
		List level = new LinkedList();
		try {
			Connection con = MYSQL.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm = 0 ORDER BY level DESC LIMIT 100");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				name.add(rs.getString("name"));
				level.add(Integer.valueOf(rs.getInt("level")));
			}
			rs.close();
			ps.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		chr.send(MainPacketCreator.getStarPlanetRank(name, level));
	}

	public static void warpToStarplanet(final byte action, final ReadingMaple rh, final MapleCharacter chr) {
		if (action == 2) {
			rh.skip(1); // 알 수 없음.
			int mapcode = rh.readInt(); // 이전에 있는 맵의 코드.
			int direction = rh.readByte(); // 현재 스타플래닛 맵이면 1, 아니면 0.

			if (direction == 1) {
				chr.dropMessage(5, "[알림] " + ServerConstants.serverName + "의 광장인 스타플래닛맵으로 이동합니다.");
				MapleMap map = chr.getClient().getChannelServer().getMapFactory().getMap(340000100);
				chr.changeMap(map, map.getPortal(0));
			} else {
				chr.dropMessage(5, "[알림] 광장으로 이동하기 이전의 맵으로 다시 이동합니다.");
				MapleMap map = chr.getClient().getChannelServer().getMapFactory().getMap(chr.getKeyValue2("Return_to_Starplanet"));
				chr.changeMap(map, map.getPortal(0));
			}
			chr.setKeyValue2("Return_to_Starplanet", mapcode);
		}
	}

	public static void MapleGuide(final ReadingMaple rh, final short action, final MapleCharacter chr) {
		if (action == 0) {
			final int mapid = rh.readInt();
			MapleMap map = chr.getClient().getChannelServer().getMapFactory().getMap(mapid);
			chr.changeMap(map, map.getPortal(0));
		}
	}

	public static void MapleChat(ReadingMaple rh, MapleCharacter chr) {
		byte type = rh.readByte();
		int first_rank = 0;
		int second_rank = 0;
		int i = 0;
		try {
			ResultSet sql = MYSQL.getConnection().prepareStatement("SELECT * FROM characters WHERE gm = 0 ORDER BY level DESC LIMIT 2").executeQuery();
			while (sql.next()) {
				i++;
				if (i == 1) {
					first_rank = sql.getInt("id");
				} else if (i == 2) {
					second_rank = sql.getInt("id");
				}
			}
			sql.close();
		} catch (SQLException e) {
			logger.debug("{}", e);
		}
		chr.send(UIPacket.getMapleStar(type, chr.getClient(), first_rank, second_rank));
	}

	public static void OrbitalFlame(final ReadingMaple rh, final MapleClient c) {
		MapleCharacter chr = c.getPlayer();

		int tempskill = rh.readInt();
		byte unk = rh.readByte();
		int direction = rh.readShort();
		int skillid = 0;
		int elementid = 0;
		int effect = 0;
		switch (tempskill) {
		case 12001020:
			skillid = 12000026;
			elementid = 12000022;
			effect = 1;
			break;
		case 12100020:
			skillid = 12100028;
			elementid = 12100026;
			effect = 2;
			break;
		case 12110020:
			skillid = 12110028;
			elementid = 12110024;
			effect = 3;
			break;
		case 12120006:
			skillid = 12120010;
			elementid = 12120007;
			effect = 4;
			break;
		}
		SkillStatEffect flame = SkillFactory.getSkill(tempskill).getEffect(chr.getSkillLevel(tempskill));
		if (flame != null && chr.getSkillLevel(elementid) > 0) {
			if (!chr.getSummons().keySet().contains(elementid)) {
				SkillStatEffect element = SkillFactory.getSkill(elementid).getEffect(chr.getSkillLevel(elementid));
				MapleSummon summon = new MapleSummon(chr, element, chr.getPosition(), SummonMovementType.FOLLOW);
				chr.getSummons().put(elementid, summon);
				chr.getMap().spawnSummon(summon, true, element.getDuration());
				element.applyTo(chr);
			}
		}
		chr.getMap().broadcastMessage(MainPacketCreator.OrbitalFlame(chr.getId(), skillid, effect, direction, flame.getRange()));
	}

	public static final void ChangeInner(ReadingMaple rh, MapleClient ha) {
		int rank = rh.readInt(); // 고정한 등급
		int count = rh.readInt(); // 고정한 어빌리티 갯수
		int consume = 100 + (rank == 1 ? 400 : rank == 2 ? 5000 : rank == 3 ? 10000 : 0) + (count == 1 ? 3000 : count == 2 ? 8000 : 0);
		ha.getPlayer().setInnerExp(ha.getPlayer().getInnerExp() - consume);
		ha.getPlayer().getClient().getSession().write(MainPacketCreator.updateInnerExp(ha.getPlayer().getInnerExp()));
		List<InnerSkillValueHolder> newValues = new LinkedList<InnerSkillValueHolder>();
		for (InnerSkillValueHolder isvh : ha.getPlayer().getInnerSkills()) {
			if (rank != 0) {

			}
			newValues.add(InnerAbillity.getInstance().renewSkill(isvh.getRank(), 0, false));
			ha.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), (byte) 0, (byte) 0);
		}
		ha.getPlayer().getInnerSkills().clear();
		for (InnerSkillValueHolder isvh : newValues) {
			ha.getPlayer().getInnerSkills().add(isvh);
			ha.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
			ha.getPlayer().getClient().getSession().write(MainPacketCreator.updateInnerAbility(isvh, ha.getPlayer().getInnerSkills().size(), ha.getPlayer().getInnerSkills().size() == 3));
		}
		ha.getPlayer().getClient().getSession().write(UIPacket.showPopupMessage("어빌리티 재설정에 성공 하였습니다."));
	}

	public static int getRank() {
		return Rank;
	}

	public static void Holly(ReadingMaple rh, MapleCharacter chr) {
		if (chr == null || !chr.isAlive()) {
			return;
		}
		List<MapleMist> mistsInMap = chr.getMap().getAllMistsThreadsafe();
		MapleMist fountain = null;
		for (MapleMist mist : mistsInMap) {
			if (mist.getSource().getSourceId() == 2311011) {
				fountain = mist;
				break;
			}
		}
		rh.readByte();
		int timesUsed = rh.readInt();
		int sourceid = rh.readInt();
		Point pos = rh.readPos();

		chr.addHP(chr.getStat().getMaxHp() / 100 * fountain.getSource().getX());
		chr.getClient().getSession().write(MainPacketCreator.showBuffeffect(-1, sourceid, 2, chr.getLevel(), fountain.getSource().getLevel()));
	}

	public static void Stardust(ReadingMaple rh, MapleClient c) {
		byte[] unk1 = rh.read(8);
		rh.skip(4);
		byte[] unk2 = rh.read(4);
		int skillid = rh.readInt();
		rh.skip(4);
		byte direction = rh.readByte();
		rh.skip(8);
		if (skillid == 12121001) {
			c.getSession().write(MainPacketCreator.skillCooldown(skillid, 5 * 1000));
			c.getPlayer().addCooldown(skillid, System.currentTimeMillis(), 5 * 1000, Timer.BuffTimer.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), skillid), 5 * 1000));
		}
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MainPacketCreator.showStardust(c.getPlayer().getId(), unk1, unk2, skillid, direction), false);
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MainPacketCreator.showBuffeffect(c.getPlayer().getId(), skillid, 3, c.getPlayer().getLevel(), 1), false);
	}

	public static void mistSkill(ReadingMaple rh, MapleCharacter chr) {
		int duration = 0;
		final int skillId = rh.readInt(); // 칠링 스텝, 이그나이트
		if (skillId == 2100010) {
			duration = rh.readInt(); // 300 or 350.
		}
		final int size = rh.readShort();
		SkillStatEffect effect = SkillFactory.getSkill(skillId).getEffect(1);
		for (int i = 0; i < (size * 2); i++) {
			Point pos = rh.readIntPos();
			chr.getMap().spawnMist(new MapleMist(effect.calculateBoundingBox(pos, chr.isFacingLeft()), chr, effect), (skillId == 2100010) ? (duration * 20) : (size * 2000), false, false, false, false,
					false);
		}
	}

	public static void CharacterCard(ReadingMaple rh, MapleClient c) {
		Map<Integer, Integer> card = new HashMap<Integer, Integer>();
		for (int i = 0; i < 9; i++) {
			card.put(i, rh.readInt());
		}
		c.setCharacterCard(card);
	}
}
