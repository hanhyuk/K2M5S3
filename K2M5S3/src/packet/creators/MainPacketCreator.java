/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package packet.creators;

import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleItempot;
import client.MapleKeyLayout;
import client.MaplePet;
import client.MapleQuestStatus;
import client.SkillEffectEntry;
import client.items.Equip;
import client.items.IEquip.ScrollResult;
import client.items.IItem;
import client.items.MapleInventoryType;
import client.skills.ISkill;
import client.skills.InnerSkillValueHolder;
import client.skills.SkillEntry;
import client.skills.SkillMacro;
import client.skills.SkillStatEffect;
import client.skills.StackedSkillEntry;
import client.stats.BuffStats;
import client.stats.DiseaseStats;
import client.stats.GlobalBuffStat;
import client.stats.PlayerStat;
import community.BuddylistEntry;
import community.GuildRankingInfo;
import community.GuildSkills;
import community.MapleAlliance;
import community.MapleExpedition;
import community.MapleExpeditionType;
import community.MapleGuild;
import community.MapleGuildCharacter;
import community.MapleGuildContents;
import community.MapleParty;
import community.MaplePartyCharacter;
import community.MaplePartyOperation;
import community.MapleUserTrade;
import constants.GameConstants;
import constants.ServerConstants;
import constants.subclasses.QuickMoveEntry;
import handler.channel.PlayerInteractionHandler;
import launch.ChannelServer;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;
import server.items.ItemInformation;
import server.items.MapleRing;
import server.life.MapleNPC;
import server.life.MaplePlayerNPC;
import server.life.MobSkill;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleSubSummon;
import server.maps.MapleSummon;
import server.maps.MapleWorldMapItem;
import server.maps.MoveSubSummon;
import server.movement.LifeMovementFragment;
import server.shops.MapleShop;
import server.shops.MapleShopItem;
import tools.AttackPair;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.Triple;

public class MainPacketCreator {
    
    public final static byte[] Host = new byte[]{(byte) 175, (byte) 207, (byte) 0, (byte) 33}; //�� ������ ���� IPA.
    public final static List<Pair<PlayerStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

    public static Packet getServerIP(final MapleClient c, final int port, final int buddyport, final int clientId) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        packet.writeShort(0);
        if (c.getTempIP().length() > 0) {
            for (String s : c.getTempIP().split(",")) {
                packet.write(Integer.parseInt(s));
            }
        } else {
            packet.write(Host); 
        }
        packet.writeShort(port); 
        if (c.getTempIP().length() > 0) {
            for (String s : c.getTempIP().split(",")) {
                packet.write(Integer.parseInt(s));
            }
        } else {
            packet.write(Host); 
        }
        packet.writeShort(buddyport);
        packet.writeInt(clientId);
        packet.writeLong(1);
        packet.write0(7);

        return packet.getPacket();
    }
 
    public static Packet getChannelChange(final MapleClient c, final int port) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        packet.write(1);
        if (c.getTempIP().length() > 0) {
            for (String s : c.getTempIP().split(",")) {
                packet.write(Integer.parseInt(s));
            }
        } else {
            packet.write(Host);
        }
        packet.writeShort(port);

        return packet.getPacket();
    }

    public static Packet getPlayerInfo(final MapleCharacter chr) {
        final WritingPacket packet = new WritingPacket();
        final boolean NotifiCheck = ServerConstants.serverNotititle.length() > 0;
        packet.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        packet.writeLong(chr.getClient().getChannel());
        packet.write(0);
        packet.write(1);
        for (int i = 0; i < 6; i++) {
            packet.writeShort(0); //1.2.239, ���� ��.
        } 
        packet.write(1);
        packet.writeShort(NotifiCheck ? 1 : 0);
        if (NotifiCheck) {
            packet.writeMapleAsciiString(ServerConstants.serverNotititle);
            packet.writeMapleAsciiString(ServerConstants.serverNotification);
        }
        chr.CRand().connectData(packet);
        PacketProvider.addPlayerInfo(packet, chr);
        PacketProvider.addCoreInfo(packet, chr);
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        packet.writeInt(100);
        packet.writeShort(0);
        packet.write(GameConstants.isPhantom(chr.getJob()) ? 0 : 1);
        packet.writeLong(0);
        packet.writeLong(0);
        if (chr.getMap().getFieldType().equals("63")) {
            packet.write(0);
        }
        return packet.getPacket();
    }

    public static Packet resetActions() {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(1);
        packet.writeInt(0);
        packet.write(0xFF); //1.2.220+
        packet.writeInt(0); //1.2.220+ 
        
        return packet.getPacket();
    }

    public static Packet updatePlayerStats(final List<Pair<PlayerStat, Long>> stats, final int evan) {
        return updatePlayerStats(stats, false, evan);
    }

    public static Packet updatePlayerStats(final List<Pair<PlayerStat, Long>> stats, final boolean itemReaction, final int evan) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (final Pair<PlayerStat, Long> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<PlayerStat, Long>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<PlayerStat, Long>>() {
                @Override
                public int compare(final Pair<PlayerStat, Long> o1, final Pair<PlayerStat, Long> o2) {
                    long val1 = o1.getLeft().getValue();
                    long val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        packet.writeInt(updateMask);
        Integer value;
        for (final Pair<PlayerStat, Long> statupdate : mystats) {
            value = statupdate.getLeft().getValue();
            if (value >= 1) {
                if (value == PlayerStat.SKIN.getValue()) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else if (value <= PlayerStat.HAIR.getValue()) {
                    packet.writeInt(statupdate.getRight());
                } else if (value < PlayerStat.JOB.getValue()) {
                    packet.write(statupdate.getRight().byteValue());
                } else if (value == PlayerStat.JOB.getValue()) {
                    packet.writeInt(statupdate.getRight().intValue());
                } else if (value == PlayerStat.HP.getValue() && value == PlayerStat.MP.getValue()) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else if (value >= PlayerStat.HP.getValue() && value <= PlayerStat.MAXMP.getValue()) {
                    packet.writeInt(statupdate.getRight().intValue());
                } else if (value == PlayerStat.AVAILABLESP.getValue()) { 
                    if (!GameConstants.isPinkBean(evan)) { //��ũ�� ���� ������� Extended SP.
                        packet.write(0);
                    } else {
                        packet.writeShort(statupdate.getRight().shortValue());
                    }
                } else if (value == PlayerStat.AVAILABLE_HYPERSP.getValue()) {
                    packet.writeInt(statupdate.getRight().intValue());
                    packet.writeInt(0);
                } else if (value == PlayerStat.EXP.getValue() || value == PlayerStat.MESO.getValue()) {
                    packet.writeLong(statupdate.getRight().longValue());
                } else if (value < 0xFFFF) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else {
                    packet.writeInt(statupdate.getRight().intValue()); 
                }
            }
        }
        packet.write(0xFF); //1.2.220+
        packet.writeInt(0); //1.2.220+
        
        return packet.getPacket();
    }

    public static Packet updateHyperSp(int mode, int remainSp) {
        return updateHyperSp("hyper", 0x1C, mode, remainSp);
    }
 
    public static Packet updateHyperSp(String value, int array, int mode, int gainCount) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.HYPER.getValue());
        packet.writeMapleAsciiString(value); // �̰͵� � ���� �ִ°Ű���.
        packet.writeInt(array); // ���� 16������ 1�� �þ.
        packet.writeInt(mode); // 0: ó��, 1: �ι�°, 2: ����°
        packet.write(gainCount);
        packet.writeInt(0);
        
        return packet.getPacket();
    }
    
    public static Packet getPacketFromHexString(String hex) {
        return new Packet(HexTool.getByteArrayFromHexString(hex));
    }
    
    public static Packet GameEnd() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GAME_END.getValue());
        
        return packet.getPacket();
    }
    
    public static Packet HeadTitle(List<Integer> num) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.HEAD_TITLE_NEW.getValue());
        for (Integer num_ : num) {
            packet.writeMapleAsciiString("");
            packet.write(num_ == 0 ? -1 : num_); //�����ϸ� num_ 
        }
        return packet.getPacket();
    }
    
    public static Packet FireWork(MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FIRE_WORK.getValue());
        packet.writeInt(chr.getId());
        
        return packet.getPacket();
    }

    public static Packet updateSp(MapleCharacter chr, final boolean itemReaction) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(itemReaction ? 1 : 0);
        packet.writeInt(PlayerStat.AVAILABLESP.getValue());
        if (!GameConstants.isPinkBean(chr.getJob())) { //��ũ�� ���� ������� Extended SP.
            packet.write(chr.getRemainingSpSize());
            for (int i = 0; i < chr.getRemainingSps().length; i++) {
                if (chr.getRemainingSp(i) > 0) {
                    packet.write(i + 1);
                    packet.writeInt(chr.getRemainingSp(i));
                }
            } 
            packet.write(0xFF); //1.2.220+ 
            packet.writeInt(0); //1.2.220+
        } else {
            packet.writeShort(chr.getRemainingSp());
            packet.write(0xFF); //1.2.220+ 
            packet.write0(6); //1.2.220+
        }
        return packet.getPacket();
    }

    public static Packet saintSaver(MapleCharacter chr) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(0);
        packet.writeInt(PlayerStat.SAINT_SABER.getValue()); 
        packet.writeInt(chr.getStat().getSaintSaver());
        packet.write(0xFF); //1.2.220+ 
        packet.writeInt(0); //1.2.220+ 
        
        return packet.getPacket();
    }

    public static Packet updateTodayTrait(MapleCharacter chr) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(0);
        packet.writeInt(PlayerStat.TRAIT_LIMIT.getValue());
        packet.writeShort(chr.getTodayCharisma());     //������ ī������
        packet.writeShort(chr.getTodayInsight());     //������ ������
        packet.writeShort(chr.getTodayWillPower());     //������ ����
        packet.writeShort(chr.getTodayDiligence());     //������ ������
        packet.writeShort(chr.getTodayEmpathy());     //������ ����
        packet.writeShort(chr.getTodayCharm());     //������ �ŷ�
        packet.writeShort(0);
        packet.writeLong(PacketProvider.getTime(-2));
        packet.write(0xFF); //1.2.220+ 
        packet.writeInt(0); //1.2.220+ 
        
        return packet.getPacket();
    }

    public static Packet getWarpToMap(final MapleMap to, final int spawnPoint, final MapleCharacter chr) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        packet.writeLong(chr.getClient().getChannel());
        packet.write(0);
        packet.write(2); //�� �̵�Ƚ��(ó�� ���ӵ� ����)
        for (int i = 0; i < 4; i++) {
            packet.writeInt(0);
        }
        packet.writeInt(to.getId());
        packet.write(spawnPoint);
        packet.writeInt(chr.getStat().getHp());
        packet.writeShort(0);
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        packet.writeInt(100);
        packet.writeShort(0);
        packet.write(GameConstants.isPhantom(chr.getJob()) ? 0 : 1);
        packet.writeLong(0);
        packet.writeLong(0);
        if (to.getFieldType().equals("63")) {
            packet.write(0);
        }
        return packet.getPacket();
    }

    public static Packet instantMapWarp(final byte portal) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        packet.writeShort(0); //1.2.220+
        packet.writeInt(portal); 

        return packet.getPacket();
    }

    public static Packet spawnPortal(final int townId, final int targetId, int skillid, final Point pos) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        packet.writeInt(townId);
        packet.writeInt(targetId);
        if (townId != 999999999 && targetId != 999999999) {
            packet.writeInt(skillid);
            packet.writePos(pos);
        }
        return packet.getPacket();
    }
    
    public static Packet spawnDoor(final int cid, final Point pos, final boolean animated) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        packet.write(!animated ? 1 : 0);
        packet.writeInt(cid);
        packet.writePos(pos);

        return packet.getPacket();
    }

    public static Packet removeDoor(final int cid, final boolean animatied) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
        packet.write(!animatied ? 1 : 0);
        packet.writeInt(cid);
        
        return packet.getPacket();
    }

    public static Packet spawnSummon(MapleSummon summon, int skillLevel, int duration, boolean animated) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
        packet.writeInt(summon.getOwner().getId());
        packet.writeInt(summon.getObjectId());
        packet.writeInt(summon.getSkill());
        packet.write(summon.getOwnerChr().getLevel()); 
        packet.write(skillLevel);
        packet.writePos(summon.getPosition());
        packet.write(summon.getSkill() == 33001010 || summon.getSkill() == 33001011 || summon.getSkill() == 5321004 || summon.getSkill() == 5320011 || summon.getSkill() == 61101002 || summon.getSkill() == 101100100 || summon.getSkill() == 14000027 || summon.getSkill() == 22171081 ? 5 : 4);
        if (GameConstants.isAngel(summon.getSkill())) {
            packet.writeShort(0x3D);
        } else if (summon.getOwner().getMap() != null) {
            packet.writeShort(summon.getOwner().getMap().getFootholds().findMaple(summon.getPosition()).getId());
        } else {
            packet.writeShort(0);
        }
        packet.write(summon.getMovementType().getValue());
        packet.write(summon.getSummonType());
        packet.write(animated ? 1 : 0); //1.2.182+
        packet.writeInt(summon.getMaelstromId());
        packet.write(0); //1.2.215+
        packet.write(1); //1.2.215+
        packet.writeInt(0); //��Ʈ Ŀ�´Ͽ�
        packet.writeInt(0); //1.2.215+ 
        MapleCharacter player = summon.getOwnerChr();
        packet.write((summon.getSkill() == 4341006 || summon.getSkill() == 14111024 || summon.getSkill() == 14121054 || summon.getSkill() == 14121055 || summon.getSkill() == 14121056) && player != null ? 1 : 0); //�̷� �̹�¡, ������ ����Ʈ
        if ((summon.getSkill() == 4341006 || summon.getSkill() == 14111024 || summon.getSkill() == 14121054 || summon.getSkill() == 14121055 || summon.getSkill() == 14121056) && player != null) {
            PacketProvider.addPlayerLooks(packet, player, true);
        }
        if (summon.getSkill() == 35111002 || summon.getSkill() == 12120007) {
            packet.write(0);
        }
        packet.write(0); //1.2.252+
        packet.writeInt(duration); //1.2.252+
        packet.write(1); //1.2.252+
        
        return packet.getPacket();
    }

    public static Packet spawnSubSummon(MapleCharacter chr, MapleSubSummon subSummon) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_SUB_SUMMON.getValue());
        packet.writeShort(subSummon.getCheck());
        packet.writeInt(subSummon.getCid());
        packet.writeInt(chr.getMapId());
        packet.writeShort(1);
        packet.writeInt(subSummon.getValue());
        packet.writeShort(subSummon.getValue2());
        packet.writePos(subSummon.getStrange());
        if (subSummon.getValue2() != 4) {
            packet.writePos(subSummon.getStrange2());
        }
        packet.writeShort(subSummon.getSecondValue());
        packet.writeInt(subSummon.getTime());
        packet.writeShort(subSummon.getStrange3());
        packet.writeInt(subSummon.getSkillId());
        packet.write(subSummon.getSkillLevel());
        packet.writeShort(0);
        
        return packet.getPacket();
    }

    public static Packet moveSubSummon(MapleCharacter chr, MoveSubSummon moveSummons) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_SUB_SUMMON.getValue());
        packet.writeShort(moveSummons.getCheck());
        packet.writeInt(moveSummons.getCid());
        packet.writeInt(chr.getMapId());
        packet.writeInt(moveSummons.getCid());
        packet.writeInt(moveSummons.getSkillid());
        packet.writeInt(moveSummons.getPoint().x);
        packet.writeInt(moveSummons.getPoint().y);
        
        return packet.getPacket();
    }

    public static Packet removeSummon(MapleSummon summon, boolean animated) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
        packet.writeInt(summon.getOwner().getId());
        packet.writeInt(summon.getObjectId());
        if (animated) {
            switch (summon.getSkill()) {
                case 35121003:
                case 14000027:
                case 14111024: //������ ����Ʈ
                case 14121054: //������ �Ϸ���
                    packet.write(10);
                    break;
                case 35111001:
                case 35111010:
                case 35111009:
                case 35111002:
                case 35111005:
                case 35111011:
                case 35121009:
                case 35121010:
                case 35121011:
                case 33101008:
                    packet.write(5);
                    break;
                case 101100100:
                case 101100101:
                case 14121003: //��ũ�Ͻ� ����
                case 36121002: //Ȧ�α׷� �׷���Ƽ : ����
                case 36121013: //Ȧ�α׷� �׷���Ƽ : ����
                case 36121014: //Ȧ�α׷� �׷���Ƽ : ����
                case 5321052:  //�Ѹ� ĳ�� ���κ���
                    packet.write(0);
                    break;
                default:
                    packet.write(4);
                    break;
            } 
        } else if (summon.getSkill() == 14000027 || summon.getSkill() == 14100027 || summon.getSkill() == 14110029 || summon.getSkill() == 14120008) {
            packet.write(16);
        } else {
            packet.write(1);
        }
        return packet.getPacket();
    }

    /**
     * Possible values for <code>type</code>:<br>
     * 1: You cannot move that channel. Please try again later.<br>
     * 2: You cannot go into the cash shop. Please try again later.<br>
     * 3: The Item-Trading shop is currently unavailable, please try again
     * later.<br>
     * 4: You cannot go into the trade shop, due to the limitation of user
     * count.<br>
     * 5: You do not meet the minimum level requirement to access the Trade
     * Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static Packet serverMessage(String message) {
        return serverMessage(4, 0, message, true, false, 0);
    }

    public static Packet serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false, false, 0);
    }

    public static Packet serverNotice(int type, String message, int itemid) {
        return serverMessage(type, 0, message, false, false, type == 6 && itemid != 0 ? itemid : 0);
    }

    public static Packet serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, false, 0);
    }

    public static Packet serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, false, smegaEar, 0);
    }

    private static Packet serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar, int itemid) {
        WritingPacket packet = new WritingPacket();

        /* 0: [Notice]<br>
         * 1: Popup<br>
         * 2: Megaphone<br>
         * 3: Super Megaphone<br>
         * 4: Scrolling message at top<br>
         * 5: Pink Text<br>
         * 6: Lightblue Text
         * 8: Item megaphone
         * 9: Heart megaphone
         * 10: Skull Super megaphone
         * 11: Green megaphone message?
         * 12: Three line of megaphone text
         * 13: End of file =.="
         * 14: Green Gachapon box
         * 15: Red Gachapon box*/
        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(type);
        if (servermessage) {
            packet.write(1);
        }
        packet.writeMapleAsciiString(message);

        switch (type) {
            case 3:
            case 9:
            case 10:
                packet.write(channel - 1); // channel
                packet.write(megaEar ? 1 : 0);
                break;
            case 6:
            case 18:
                packet.writeInt(itemid);
                break;
        }
        return packet.getPacket();
    }

    public static Packet tripleSmega(List<String> message, boolean ear, int channel) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(10);

        if (message.get(0) != null) {
            packet.writeMapleAsciiString(message.get(0));
        }
        packet.write(message.size());
        for (int i = 1; i < message.size(); i++) {
            if (message.get(i) != null) {
                packet.writeMapleAsciiString(message.get(i));
            }
        }
        packet.write(channel);
        packet.write(ear ? 1 : 0);

        return packet.getPacket();
    }

    public static Packet getAvatarMega(MapleCharacter chr, int channel, int itemId, List<String> text, boolean ear) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        packet.writeInt(itemId);
        packet.writeMapleAsciiString(chr.getName());
        for (String i : text) {
            packet.writeMapleAsciiString(i);
        }
        packet.writeInt(channel);
        packet.write(ear ? 1 : 0);
        PacketProvider.addPlayerLooks(packet, chr, true);

        return packet.getPacket();
    }

    public static Packet itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(8);
        packet.writeMapleAsciiString(msg);
        packet.write(channel);
        packet.write(whisper ? 1 : 0);
        if (item == null) {
            packet.write(0);
        } else {
            PacketProvider.addItemInfo(packet, item, false, false, true, null);
        }
        return packet.getPacket();
    }

    public static Packet spawnNPC(MapleNPC life, boolean show) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        packet.writeInt(life.getObjectId());
        packet.writeInt(life.getId());
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(0); //1.2.252+, isTown ?
        packet.write(life.getF() == 1 ? 0 : 1);
        packet.writeShort(life.getFh());
        packet.writeShort(life.getRx0());
        packet.writeShort(life.getRx1());
        packet.write(show ? 1 : 0);
        packet.write0(5);
        packet.writeInt(-1);
        packet.write0(11); //1.2.250+

        return packet.getPacket();
    }

    public static Packet removeNPC(final int objectid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
        packet.writeInt(objectid);

        return packet.getPacket();
    }

    public static Packet removeNPCController(final int objectid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        packet.write(0);
        packet.writeInt(objectid);

        return packet.getPacket();
    }

    public static Packet spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        packet.write(1);
        packet.writeInt(life.getObjectId());
        packet.writeInt(life.getId());
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(0); //1.2.252+, isTown ?
        packet.write(life.getF() == 1 ? 0 : 1);
        packet.writeShort(life.getFh());
        packet.writeShort(life.getRx0());
        packet.writeShort(life.getRx1());
        packet.write(MiniMap ? 1 : 0);
        packet.write0(5);
        packet.writeInt(-1);
        packet.write0(11); //1.2.252+

        return packet.getPacket();
    }

    public static Packet spawnPlayerNPC(MaplePlayerNPC npc, int id) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        packet.write(npc.getDirection());
        packet.writeInt(id);
        packet.writeMapleAsciiString(npc.getName());
        packet.write(1);//173change
        packet.write(npc.getSkin());
        packet.writeInt(npc.getFace());
        packet.writeInt(0);
        packet.write(0);
        packet.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (byte position : equip.keySet()) {
            byte pos = (byte) (position * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, equip.get(position));
            } else if ((pos > 100 || pos == -128) && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, equip.get(position));
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, equip.get(position));
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            packet.write(entry.getKey());
            packet.writeInt(entry.getValue());
        }
        packet.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            packet.write(entry.getKey());
            packet.writeInt(entry.getValue());
        }
        packet.write(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        Integer weapon = equip.get((byte) -11);
        packet.writeInt(cWeapon != null ? cWeapon : 0);
        packet.writeInt(weapon != null ? weapon : 0);
        packet.write0(17);

        return packet.getPacket();
    }

    public static Packet getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        packet.writeInt(cidfrom);
        packet.write(whiteBG ? 1 : 0);
        packet.writeMapleAsciiString(text);
        packet.write(show);
        packet.writeShort(0);
        packet.write(0xFF); //1.2.201+

        return packet.getPacket();
    }

    public static Packet AranCombo(int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        packet.writeInt(value);

        return packet.getPacket();
    }

    public static Packet RechargeCombo(int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.RECHARGE_COMBO.getValue());
        packet.writeInt(value);

        return packet.getPacket();
    }

    public static Packet moveFollow(Point otherStart, Point myStart, Point otherEnd, List< LifeMovementFragment> moves) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
        packet.writeInt(0); //v192
        packet.writePos(otherStart);
        packet.writePos(myStart);
        PacketProvider.serializeMovementList(packet, moves);
        packet.write(4);
        for (int i = 0; i < 5; i++) {
            packet.write(0);
        }
        packet.write(0);
        packet.writePos(otherEnd);
        packet.writePos(otherStart);

        return packet.getPacket();
    }

    public static Packet GainEXP_Trait(final int gain, final int type, boolean limited) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(limited ? 0x14 : 0x13); //1.2.250+ (+2)
        /* 0 : ī������
         * 1 : ������
         * 2 : ����
         * 3 : ������
         * 4 : ����
         * 5 : �ŷ�
         */
        switch (type) {
            case 0:
                packet.writeInt(PlayerStat.CHARISMA.getValue());
                break;
            case 1:
                packet.writeInt(PlayerStat.INSIGHT.getValue());
                break;
            case 2:
                packet.writeInt(PlayerStat.WILLPOWER.getValue());
                break;
            case 3:
                packet.writeInt(PlayerStat.CRAFT.getValue());
                break;
            case 4:
                packet.writeInt(PlayerStat.SENSE.getValue());
                break;
            case 5:
                packet.writeInt(PlayerStat.CHARM.getValue());
                break;
        }
        packet.writeInt(gain);
        return packet.getPacket();
    }
    
    public static Packet GainEXP_Others(final long gain, final boolean inChat, final boolean white) {
        return GainEXP_Monster(gain, inChat, white, 0, 0, 0, 0, 0);
    }

    public static Packet GainEXP_Monster(final long gain, final boolean inChat, final boolean white, final int partyinc, final int Class_Bonus_EXP, final int elfBlessing, final int buff, final int event) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0x04); // 0x04 = exp, 0x05 = fame, 0x06 = mesos, 0x07 = guildpoints
        packet.write(white ? 1 : 0);
        packet.writeInt(gain);
        packet.write(inChat ? 1 : 0); // Not in chat
	long flag = event > 0 ? 0x00000001 : 0;
        packet.writeLong(flag);
	if((flag & 1) != 0) {
            packet.writeInt(event);
        }
	if((flag & 4) != 0) {
            packet.write(1); // unused
	}
	if(inChat) {
            byte wat = 1;
            packet.write(wat);
            if(wat > 0) {
            	packet.write(1);
            }
	}
        if((flag & 0x20) != 0) {
            packet.writeInt(2);
	}
	if((flag & 0x10) != 0) {
            packet.writeInt(3);
	}
	if((flag & 0x40) != 0) {
            packet.writeInt(4);
	}
	if((flag & 0x80) != 0) {
            packet.writeInt(5);
	}
	if((flag & 0x100) != 0) {
            packet.writeInt(6);
	}
	if((flag & 0x200) != 0) {
            packet.writeInt(7);
	}
	if((flag & 0x400) != 0) {
            packet.writeInt(8);
	}
	if((flag & 0x800) != 0) {
            packet.writeInt(9);
	}
	if((flag & 0x1000) != 0) {
            packet.writeInt(10);
	}
	if((flag & 0x2000) != 0) {
            packet.writeInt(11);
	}
	if((flag & 0x4000) != 0) {
            packet.writeInt(12);
	}
	if((flag & 0x8000) != 0) {
            packet.writeInt(13);
	}
	if((flag & 0x10000) != 0) {
            packet.writeInt(14);
	}
	if((flag & 0x20000) != 0) {
            packet.writeInt(15);
	}
	if((flag & 0x40000) != 0) {
            packet.writeInt(16);
	}
	if((flag & 0x80000) != 0) {
            packet.writeInt(17);
	}
	if((flag & 0x100000) != 0) {
            packet.writeInt(18);
	}
        if((flag & 0x200000) != 0) {
            packet.writeInt(19);
        }
        if((flag & 0x400000) != 0) {
            packet.writeInt(20);
        }
        if((flag & 0x800000) != 0) {
            packet.writeInt(21);
        }
        if((flag & 0x1000000) != 0) {
            packet.writeInt(22);
        }
        if((flag & 0x2000000) != 0) {
            packet.writeInt(23);
        }
        if((flag & 0x4000000) != 0) {
            packet.writeInt(24);
        }
        return packet.getPacket();
    }

    public static Packet fairyPendantMessage(final int incExpR) { // bonusExp, <= 0x3D
        final WritingPacket mplew = new WritingPacket(14);
        mplew.writeShort(SendPacketOpcode.EXP_BONUS.getValue());
        mplew.writeInt(0x11); // 0x11 = pendant, 0x31 = evan medal
        mplew.writeInt(0/*termStart*/); // hour = 0 upon equipping
        mplew.writeInt(incExpR);

        return mplew.getPacket();
    }

    public static Packet getShowFameGain(final int gain) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(6); //1.2.250+ (+1)
        packet.writeInt(gain);

        return packet.getPacket();
    }

    public static Packet showMesoGain(final long gain, final boolean inChat) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            packet.write(0);
            packet.write(1);
            packet.write(0);
            packet.writeLong(gain); //1.2.203+
        } else {
            packet.write(7); //1.2.250+ (+1)
            packet.writeInt(gain);
            packet.writeInt(-1);
        } 
        return packet.getPacket();
    }

    public static Packet getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    public static Packet getShowItemGain(int itemId, short quantity, boolean inChat) {
        WritingPacket packet = new WritingPacket();
        if (inChat) {
            packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            packet.write(7); //1.2.251+
            packet.write(1); //item count
            packet.writeInt(itemId);
            packet.writeInt(quantity);
        } else {
            packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            packet.write(0);
            packet.write(0);
            packet.writeInt(itemId);
            packet.writeInt(quantity);
        }
        return packet.getPacket();
    }
    
    public static Packet getShowItemGainSimple(int itemId, boolean full) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0);
        packet.write(full ? 3 : 2);
        packet.writeInt(itemId);
        
        return packet.getPacket();
    }

    public static Packet showRewardItemAnimation(int itemId, String effect) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(0x14); //1.2.252+ (+3)
        packet.writeInt(itemId);
        packet.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            packet.writeMapleAsciiString(effect);
        }
        return packet.getPacket();
    }

    public static Packet showRewardItemAnimation(int itemId, String effect, int from_playerid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(from_playerid);
        packet.write(0x14); //1.2.252+ (+3)
        packet.writeInt(itemId);
        packet.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            packet.writeMapleAsciiString(effect);
        }
        return packet.getPacket();
    }

    public static Packet dropItemFromMapObject(MapleWorldMapItem drop, Point dropfrom, Point dropto, byte mod) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        packet.write(0);
        packet.write(mod);
        packet.writeInt(drop.getObjectId());
        packet.write(drop.getMeso() > 0 ? 1 : 0);
        packet.writeInt(drop.isFly() ? 1 : 0);
        packet.writeInt(drop.getGradiant());
        packet.writeInt(drop.getSpeed());
        packet.writeInt(drop.getItemId());
        packet.writeInt(drop.getOwner());
        packet.write(drop.getMeso() > 0 ? 2 : 0);
        packet.writePos(dropto);
        packet.writeInt(0);
        if (mod != 2) {
            packet.writePos(drop.isFly() ? new Point(dropfrom.x, dropfrom.y - 71) : dropfrom);
            packet.writeShort(0);
            packet.write0(3);
        }
        if (drop.getMeso() == 0) {
            packet.write(0);
            packet.writeLong(PacketProvider.getTime(drop.getItem().getExpiration()));
        }
        packet.writeShort(drop.isPlayerDrop() ? 0 : 1);
        if (mod == 2) {
            if (drop.getMeso() > 0) {
                packet.write0(3);
            } else {
                packet.write(0);
            }
        }
        packet.write(0);
        packet.writeInt(drop.isTouch() ? 1 : 0);
        if ((drop.getItemId() / 1000000 == 1) && (drop.getMeso() == 0) && (drop.getEquip() != null)) {
            Equip item = (Equip) drop.getItem();
            switch (item.getState()) {
                case 17: //����
                    packet.write(1);
                    break;
                case 18: //����
                    packet.write(2);
                    break;
                case 19: //����ũ
                    packet.write(3);
                    break;
                case 20: //�����帮
                    packet.write(4);
                    break;
                default:
                    packet.write(0);
                    break;
            }
        } else {
          packet.write(0);
        }
        packet.writeInt(0); //1.2.250+
        
        return packet.getPacket();
    }

    public static Packet spawnPlayerMapobject(MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        packet.writeInt(chr.getId());
        packet.write(chr.getLevel());
        packet.writeMapleAsciiString(chr.getName());
        packet.writeMapleAsciiString("");
        MapleGuildContents gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
        if (gs != null) {
            packet.writeMapleAsciiString(gs.getName());
            packet.writeShort(gs.getLogoBG());
            packet.write(gs.getLogoBGColor());
            packet.writeShort(gs.getLogo());
            packet.write(gs.getLogoColor());
        } else {
            packet.writeLong(0);
        }
        packet.write(chr.getGender());
        packet.write(chr.getSubcategory());
        packet.writeInt(chr.getFame());
        packet.writeInt(0);
        packet.writeInt(Randomizer.rand(1, 7)); //7: ���
        final List<Pair<Integer, Integer>> buffvalue = new ArrayList<Pair<Integer, Integer>>();
        int[] mask = new int[BuffStats.MAX_BUFFSTAT];
        mask[7] |= 0x18000000; //Default BuffMask, 1.2.252 Ok.
        mask[10] |= 0x300; //Default BuffMask, 1.2.252 Ok.
        mask[11] |= 0x2; //Default BuffMask, 1.2.252 Ok.
        mask[12] |= 0x28400; //Defalut BuffMask, 1.2.252 Ok.
        mask[13] |= 0x1010; //Default BuffMask, 1.2.252 Ok.
        mask[14] |= 0x3FC00000; //Default BuffMask, 1.2.252 Ok.
        //----------------------------------------------------------------------
        if (chr.getBuffedValue(BuffStats.DARKSIGHT) != null || chr.isHidden()) {
            mask[mask.length - 14] |= BuffStats.DARKSIGHT.getValue();
        }
        //----------------------------------------------------------------------
        if (chr.getBuffedValue(BuffStats.COMBO) != null) {
            mask[mask.length - 13] |= BuffStats.COMBO.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(BuffStats.COMBO).intValue()), 1));
        }
        //----------------------------------------------------------------------
        /*if (chr.getBuffedValue(BuffStats.AURA) != null) {
            mask[mask.length - 4] |= BuffStats.AURA.getValue();
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(BuffStats.AURA).intValue()), 2));
            buffvalue.add(new Pair<Integer, Integer>(Integer.valueOf(chr.getTrueBuffSource(BuffStats.AURA)), 3));
        }*/
        //----------------------------------------------------------------------*
        if (chr.getBuffedValue(BuffStats.WIND_WALK) != null) {
            mask[mask.length - 5] |= BuffStats.WIND_WALK.getValue();
        }
        //----------------------------------------------------------------------
        if ((chr.getEquippedSoulSkill() > 0) && (chr.getSoulCount() >= chr.getSoulSkillMpCon())) {
            mask[mask.length - 5] |= BuffStats.SOUL_HEAD_EFFECT.getValue();
        }
        //----------------------------------------------------------------------
        for (int i = 0; i < mask.length; i++) {
            packet.writeInt(mask[i]);
        }
        for (Pair<Integer, Integer> i : buffvalue) {
            if (i.right == 3) {
                packet.writeInt(i.left.intValue());
            } else if (i.right == 2) {
                packet.writeShort(i.left.shortValue());
            } else if (i.right == 1) {
                packet.write(i.left.byteValue());
            }
        }
        /* ��������ũ ���� */
        packet.writeInt(-1);
        /* ������ ���� ��Ŷ */
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeShort(0);
        if ((chr.getEquippedSoulSkill() > 0) && (chr.getSoulCount() >= chr.getSoulSkillMpCon())) {
            packet.writeInt(0);
            packet.writeInt(chr.getEquippedSoulSkill());
        }
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        
        packet.writeInt(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        
        packet.writeInt(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        
        packet.writeInt(0); //1.2.250+
        packet.writeInt(0); //1.2.250+
        packet.write(0); //1.2.250+
        
        int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.write0(9);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.write0(11);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        if (chr.getKeyValue2("mountid") != 0 && chr.getKeyValue2("mountid") != -1) {
            packet.write0(3);
            packet.writeInt(chr.getKeyValue2("mountid"));
            packet.writeInt(chr.getKeyValue2("mountskillid"));
        } else {
            packet.write0(11);
        }
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.write0(9);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.write(0);
        packet.write((!GameConstants.isZero(chr.getJob())) || (!GameConstants.isEvan(chr.getJob())) ? 0 : 1);
        packet.writeInt(Randomizer.nextInt());
        packet.writeShort(0);
        packet.writeShort(0);
        packet.writeShort(0);
        packet.writeShort(0);
        packet.writeShort(0);
        for (int i = 1; i <= 3; i++) { 
            packet.writeInt(CHAR_MAGIC_SPAWN);
            packet.write(0);
            packet.writeShort(0);
            packet.writeShort(i != 3 ? 0 : chr.getJob());
            packet.writeShort(0);
            packet.writeShort(0);
            packet.writeShort(0);
            if (i == 1) {
                packet.writeShort(0);
                packet.writeShort(0);
                packet.writeShort(0);
            }
        }
        PacketProvider.addPlayerLooks(packet, chr, false);
        if (GameConstants.isZero(chr.getJob())) {
            PacketProvider.addPlayerLooksZero(packet, chr, false);
        }
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(chr.getItemEffect());
        packet.writeInt(0);
        packet.writeInt(chr.getHeadTitle());
        packet.writeInt(chr.getDamageSkin());
        packet.writeInt(chr.getDamageSkin()); 
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0); //1.2.239+
        packet.writeShort(-1);
        packet.writeInt(chr.getChair());
        packet.writeInt(chr.getChairText() != null ? 1 : 0);
        if ((chr.getChairText() != null) && (chr.getChairText().length() > 0)) {
            packet.writeMapleAsciiString(chr.getChairText());
        }
        packet.writeInt(0); //1.2.250+
        packet.writePos(chr.getPosition());
        packet.write(chr.getStance()); 
        packet.writeShort(chr.getFH());
        packet.writeShort(0); //1.2.250+
        
        packet.writeInt(chr.getMount().getLevel());
        packet.writeInt(chr.getMount().getExp());
        packet.writeInt(chr.getMount().getFatigue());
        
        PlayerShopPacket.addAnnounceBox(packet, chr);
        packet.write(chr.getChalkboard() != null ? 1 : 0);
        if ((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0)) {
            packet.writeMapleAsciiString(chr.getChalkboard());
        }
        
        Triple rings = chr.getRings(false);
        addRingInfo(packet, (List) rings.getFirst());
        addRingInfo(packet, (List) rings.getSecond());
        addMRingInfo(packet, (List) rings.getThird(), chr);
        
        packet.write(0);
        packet.writeInt(0);  
        packet.writeInt(0); 
        if (GameConstants.isKaiser(chr.getJob())) {
            packet.writeShort(0);
            packet.write(0);
            packet.writeInt(1);
            packet.writeShort(0);
        }
        PacketProvider.addMonsterLife(packet, chr);
        packet.writeInt(1);
        List<Integer> num_ = chr.HeadTitle();
        for (Integer num_1 : num_) {
            packet.write(num_1 == 0 ? -1 : num_1);
        }
        packet.writeInt(0); 
        packet.writeLong(1); //1.2.239+ 
        packet.writeLong(0); //1.2.239+
        packet.writeInt(0); //1.2.239+
        packet.write(0); //1.2.240+
        packet.write(1); //1.2.251+
        packet.writeInt(0); //1.2.252+
        if (chr.getMapId() == 109090300) {
            packet.write(chr.isCatching ? 1 : 0);
        }
        return packet.getPacket();
    } 

    public static Packet finishedSort(int type) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
        packet.write(1);
        packet.write(type);
        
        return packet.getPacket();
    }

    public static Packet finishedGather(int type) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FINISH_GATHER.getValue());
        packet.write(1);
        packet.write(type);
        
        return packet.getPacket();
    }

    public static Packet removePlayerFromMap(int cid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        packet.writeInt(cid);

        return packet.getPacket();
    }
    
    public static Packet getMilliClock(int time) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CLOCK.getValue());
        packet.write(6);
        packet.writeInt(time);
        return packet.getPacket();
    }

    public static Packet facialExpression(MapleCharacter from, int expression) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        packet.writeInt(from.getId());
        packet.writeInt(expression);
        packet.writeInt(-1);
        packet.write(0);

        return packet.getPacket();
    }

    public static Packet movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        packet.writeInt(cid);
        packet.writeInt(0); //1.2.192+
        packet.writePos(startPos);
        packet.writeInt(0);
        PacketProvider.serializeMovementList(packet, moves);
        
        return packet.getPacket();
    }

    public static Packet moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        WritingPacket packet = new WritingPacket();  
        packet.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        packet.writeInt(cid);
        packet.writeInt(oid);
        packet.writeInt(0);
        packet.writePos(startPos);
        packet.writeInt(0);
        PacketProvider.serializeMovementList(packet, moves);
        
        return packet.getPacket();
    }
    
    public static Packet summonAttack(final int cid, final int summonSkillId, final byte animation, final byte tbyte, final List<AttackPair> allDamage, final int level, boolean isDarkFlare, final int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        packet.writeInt(cid);
        packet.writeInt(summonSkillId); 
        packet.write(level);
        packet.write(animation);
        packet.write(tbyte);
        for (AttackPair oned : allDamage) {
            if (oned.attack != null) {
                packet.writeInt(oned.objectid);
                if (oned.objectid == 0) {
                    continue;
                }
                packet.write(7);
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    packet.writeInt(eachd.left.intValue());
                }
            }
        }
        packet.writeShort(!isDarkFlare ? 0 : 1); //1.2.252+
        packet.writeInt(!isDarkFlare ? value : 0); //1.2.252+
        
        return packet.getPacket();
    }

    public static Packet attack(RecvPacketOpcode op, MapleCharacter player, int cid, byte tbyte, int skill, int level, short display, byte speed, List<AttackPair> damage, short value, final Point pos, final byte mastery, final int charge, final int lvl, final int itemid) {
        WritingPacket packet = new WritingPacket();
        if (op == RecvPacketOpcode.CLOSE_RANGE_ATTACK) {
            packet.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        } else if (op == RecvPacketOpcode.RANGED_ATTACK) {
            packet.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        } else {
            packet.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        }

        packet.writeInt(cid);
        packet.write(0); //1.2.201+
        packet.write(tbyte);
        
        if (skill != 0) {
            packet.write(lvl);
        } else {
            packet.write(0);
        }

        if (skill > 0) {
            packet.write(level);
            packet.writeInt(skill);
        } else {
            packet.write(0);
        }
        
        packet.write0(10); //1.2.251+
        packet.writeShort(display);
        
        packet.write(0xFF); //1.2.251+
        packet.write0(6); //1.2.251+
        
        packet.write(speed);
        packet.write(mastery);
        packet.writeInt(itemid > 0 ? itemid : charge > 0 ? charge : 0);
       
        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                packet.writeInt(oned.objectid);
                if (oned.objectid == 0) {
                    continue;
                }
                packet.write(7);
                packet.write(0);
                packet.write(0);
                packet.writeShort(value); //1.2.251+
                for (Pair<Integer, Boolean> eachd : oned.attack) {
                    if (eachd.right) {
                        packet.writeInt(eachd.left.intValue() | 0x80000000); 
                    } else {
                        packet.writeInt(eachd.left.intValue());
                    }
                }
            }
        }
        if (charge > 0) {
            packet.writeInt(charge); //������ ��ų�� �������ٸ� ���� �о���.
        }
        packet.writeLong(0);
        
        return packet.getPacket();
    }
    
    public static void addShopInfo(WritingPacket packet, MapleClient c, int sid, List<MapleShopItem> items) {
        final ItemInformation ii = ItemInformation.getInstance();
        packet.writeInt(0);
        packet.writeInt(sid); //1.2.240+
        packet.writeLong(0); //1.2.251+
        packet.writeShort(items.size() + c.getPlayer().getRebuyList().size());
        for (MapleShopItem item : items) {
            PacketProvider.addShopItemInfo(packet, item, c, null, sid);
        }
        for (IItem item : c.getPlayer().getRebuyList()) {
            PacketProvider.addShopItemInfo(packet, new MapleShopItem((short) 1000, item.getItemId(), (int) ii.getPrice(item.getItemId()), 0, (byte) 0, (short) item.getQuantity(), 0), c, item, -1);
        }
    }

    public static Packet getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        packet.write(0);
        addShopInfo(packet, c, sid, items);
        
        return packet.getPacket();
    }
    
    public static Packet confirmShopTransaction(byte code, int boughtIndex) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        packet.write(code); // 8 = sell, 0 = buy, 0x20 = due to an error
        packet.write(boughtIndex < 0 ? 0 : 1);
        packet.writeInt(boughtIndex < 0 ? 0 : boughtIndex);
        if (boughtIndex < 0) {
            packet.write(0); //1.2.251+
        }
        return packet.getPacket();
    }

    public static Packet confirmShopTransactionAdditional(int sid, List<MapleShopItem> items, MapleShop shop, final MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        packet.write(8); // 8 = sell, 0 = buy, 0x20 = due to an error <- 160�� (4) 168�� (5)
        addShopInfo(packet, c, sid, items);
        
        return packet.getPacket();
    }

    public static Packet addInventorySlot(MapleInventoryType type, IItem item) {
        return addInventorySlot(type, item, false);
    }

    public static Packet addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(fromDrop ? 1 : 0);
        packet.writeShort(1); // add mode
        packet.write(item.getPosition() > 100 && type == MapleInventoryType.ETC ? 9 : 0);
        packet.write(type.getType()); // iv type
        packet.write(item.getPosition()); // slot id
        PacketProvider.addItemInfo(packet, item, true, false, null);

        return packet.getPacket();
    }

    public static Packet updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(fromDrop ? 1 : 0);
        packet.write(1);
        packet.write(0);
        packet.write(item.getPosition() > 100 && type == MapleInventoryType.ETC ? 6 : 1);
        packet.write(type.getType()); // iv type
        packet.writeShort(item.getPosition()); // slot id
        packet.writeShort(item.getQuantity());
        packet.write(0);

        return packet.getPacket();
    }

    public static Packet moveInventoryItem(MapleInventoryType type, short src, short dst, boolean bag, boolean bothBag) {
        return moveInventoryItem(type, src, dst, (byte) -1, bag, bothBag);
    }

    public static Packet moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator, boolean bag, boolean bothBag) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(1);
        packet.write(0);
        packet.write(bag ? (bothBag ? 8 : 5) : 2);
        packet.write(type.getType());
        packet.writeShort(src);
        packet.writeShort(dst);
        if (equipIndicator != -1) {
            packet.writeShort(equipIndicator);
        }
        return packet.getPacket();
    }

    public static Packet moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total, boolean bag, boolean switchSrcDst, boolean bothBag) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(2);
        packet.write(0);
        packet.write(bag && (switchSrcDst || bothBag) ? 7 : 3);
        packet.write(type.getType());
        packet.writeShort(src);
        packet.write(bag && (!switchSrcDst || bothBag) ? 6 : 1); // merge mode?
        packet.write(type.getType());
        packet.writeShort(dst);
        packet.writeShort(total);

        return packet.getPacket();
    }

    public static Packet moveAndMergeWithRestInventoryItem(MapleInventoryType type, short src, short dst, short srcQ, short dstQ, boolean bag, boolean switchSrcDst, boolean bothBag) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(2);
        packet.write(0);
        packet.write(bag && (switchSrcDst || bothBag) ? 6 : 1);
        packet.write(type.getType());
        packet.writeShort(src);
        packet.writeShort(srcQ);
        packet.write(bag && (!switchSrcDst || bothBag) ? 6 : 1);
        packet.write(type.getType());
        packet.writeShort(dst);
        packet.writeShort(dstQ);

        return packet.getPacket();
    }

    public static Packet clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(fromDrop ? 1 : 0);
        packet.write(1);
        packet.write(0);
        packet.write(slot > 100 && type == MapleInventoryType.ETC ? 7 : 3);
        packet.write(type.getType());
        packet.writeShort(slot);

        return packet.getPacket();
    }

    public static Packet removeInventoryItem(MapleInventoryType type, short slot) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(0);
        packet.write(1);
        packet.write(0);
        packet.write(slot > 100 && type == MapleInventoryType.ETC ? 7 : 3);
        packet.write(type.getType());
        packet.writeShort(slot);

        return packet.getPacket();
    }

    public static Packet updateSpecialItemUse(IItem item, byte invType) {
        return updateSpecialItemUse(item, invType, false, null);
    }

    public static Packet updateSpecialItemUse(IItem item, byte invType, boolean theShort, MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(0); // could be from drop
        packet.write(2); // always 2
        packet.write(0);
        packet.write(invType == MapleInventoryType.ETC.getType() && item.getPosition() > 100 ? 7 : 3); // quantity > 0 (?)
        packet.write(invType); // Inventory type
        packet.writeShort(item.getPosition()); // item slot
        packet.write(0);
        packet.write(invType);
        if (item.getType() == 1 || theShort) {
            packet.writeShort(item.getPosition()); // wtf repeat
        } else {
            packet.write(item.getPosition());
        }
        PacketProvider.addItemInfo(packet, item, true, true, false, false, chr);
        if (item.getPosition() < 0) {
            packet.write(2); //?
        }

        return packet.getPacket();
    }

    public static Packet updateSpecialItemUseT(IItem item, byte invType, MapleCharacter chr, int type) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(0); // could be from drop
        packet.write(2); // always 2
        packet.write(0);
        packet.write(3); // quantity > 0 (?)
        packet.write(invType); // Inventory type
        packet.writeShort(item.getPosition()); // item slot
        packet.write(0);
        packet.write(invType);
        if (item.getType() == 1) {
            packet.writeShort(item.getPosition()); // wtf repeat
        } else {
            packet.write(item.getPosition());
        }
        PacketProvider.addItemInfo(packet, item, true, true, false, false, chr);
        packet.write(type);

        return packet.getPacket();
    }

    public static Packet scrolledItem(IItem scroll, IItem item, boolean destroyed, boolean potential) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1); // fromdrop always true
        packet.write(destroyed ? 2 : 3);
        packet.write(0);
        packet.write(scroll.getQuantity() > 0 ? 1 : 3);
        packet.write(GameConstants.getInventoryType(scroll.getItemId()).getType());
        packet.writeShort(scroll.getPosition());
        if (scroll.getQuantity() > 0) {
            packet.writeShort(scroll.getQuantity());
        }
        packet.write(3);
        if (!destroyed) {
            packet.write(MapleInventoryType.EQUIP.getType());
            packet.writeShort(item.getPosition());
            packet.write(0);
        }
        packet.write(MapleInventoryType.EQUIP.getType());
        packet.writeShort(item.getPosition());
        if (!destroyed) {
            PacketProvider.addItemInfo(packet, item, true, true, null);
        }
        if (!potential) {
            packet.write(7);
        } else {
            packet.write(11);
        }

        return packet.getPacket();
    }
    
    public static Packet getScrollEffect(int chr, ScrollResult scrollSuccess) {
        return getScrollEffect(chr, scrollSuccess, 0, 0);
    }
    
    public static Packet getScrollEffect(int chr, int scrollid, int victimid) {
        return getScrollEffect(chr, ScrollResult.SUCCESS, scrollid, victimid);
    }

    public static Packet getScrollEffect(int chr, ScrollResult scrollSuccess, int scrollid, int victimid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        packet.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS:
                packet.write(1);
                break;
            case FAIL:
                packet.write(0);
                break;
            case CURSE:
                packet.write(2);
                break;
        }
        packet.write(0);
        packet.writeInt(scrollid);
        packet.writeInt(victimid);

        return packet.getPacket();
    }

    public static Packet explodeDrop(int oid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        packet.write(4); // 4 = Explode
        packet.writeInt(oid);
        packet.writeShort(655);

        return packet.getPacket();
    }

    public static Packet removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, false, 0);
    }

    public static Packet removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        packet.write(animation); // 0 = Expire, 1 = without animation, 2 = pickup, 4 = explode
        packet.writeInt(oid);
        if (animation >= 2) {
            packet.writeInt(cid);
            if (pet) { // allow pet pickup?
                packet.writeInt(slot);
            }
        }
        return packet.getPacket();
    }

    public static Packet updateCharLook(MapleCharacter chr, boolean Angelic) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        packet.writeInt(chr.getId());
        packet.write(1);
        PacketProvider.addPlayerLooks(packet, chr, false);
        if(GameConstants.isZero(chr.getJob())) {
            PacketProvider.addPlayerLooksZero(packet, chr, false);
        }
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(packet, rings.getFirst());
        addRingInfo(packet, rings.getSecond());
        addMRingInfo(packet, rings.getThird(), chr);
        packet.writeInt(0);
        packet.writeInt(0); //1.2.220+, 0x68
        if (Angelic) {
            packet.writeShort(0);
            packet.writeInt(34);
        }
        return packet.getPacket();
    }
    
    public static Packet dropInventoryItem(MapleInventoryType type, short src) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(1);
        packet.write(0);
        packet.write(3);
        packet.write(type.getType());
        packet.writeShort(src);
        if (src < 0) {
            packet.write(1);
        }

        return packet.getPacket();
    }

    public static Packet dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(1);
        packet.write(0);
        packet.write(1);
        packet.write(type.getType());
        packet.writeShort(item.getPosition());
        packet.writeShort(item.getQuantity());

        return packet.getPacket();
    }

    public static Packet damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, byte direction, int reflect, boolean is_pg, int oid, int pos_x, int pos_y) throws InterruptedException {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        packet.writeInt(cid);
        packet.write(skill);
        packet.writeInt(damage < 0 ? -damage : damage);
        packet.writeShort(0);
        packet.writeInt(monsteridfrom);
        packet.write(direction);
        
        packet.writeInt(oid); //1.2.220+
        packet.writeInt(0);
        packet.writeInt(0);
        
        if (reflect > 0) {
            packet.write(reflect);
            packet.write(is_pg ? 1 : 0);
            packet.writeInt(oid);
            packet.write(6);
            packet.writeShort(pos_x);
            packet.writeShort(pos_y);
            packet.write(0);
        } else {
            packet.writeShort(0);
        }
        packet.writeInt(damage < 0 ? -damage : damage);
        if (fake > 0) {
            packet.writeInt(fake);
        }
        return packet.getPacket();
    }

    public static Packet startQuest(final MapleCharacter c, final int quest, final String data) {
        final WritingPacket packet = new WritingPacket(); 
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeInt(quest); //1.2.250+
        packet.write(1);
        packet.writeMapleAsciiString(data != null ? data : "");

        return packet.getPacket();
    }

    public static Packet forfeitQuest(MapleCharacter c, int quest) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeInt(quest); //1.2.250+
        packet.write0(23);
        
        return packet.getPacket();
    }

    public static Packet completeQuest(final int quest) { 
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeInt(quest); //1.2.250+
        packet.write(2);
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        
        return packet.getPacket();
    }

    public static Packet updateInfoQuest(final int quest, final String data) { //sub_17C6DA0
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0x0D); //1.2.250+ (+1)
        packet.writeInt(quest);
        packet.writeMapleAsciiString(data);

        return packet.getPacket();
    }
    
    public static Packet getCharInfo(final MapleCharacter hp, final boolean isSelf) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        packet.writeInt(hp.getId());
        packet.write(0); //1.2.220+
        packet.write(hp.getLevel());
        packet.writeShort(hp.getJob());
        packet.write0(3); //��Ʋ��ũ 10 ����.
        packet.writeInt(hp.getFame()); //�α⵵
        packet.write(0); //��ȥ
        List<Integer> professions = new ArrayList<Integer>(2);
        packet.write(professions.size()); //�����������
        for (Integer i : professions) {
            packet.writeShort(i);
        }
        if (hp.getGuildId() <= 0) {
            packet.writeMapleAsciiString("-");
            packet.writeMapleAsciiString(GameConstants.��ũ(hp.getRankPoint()));
        } else {
            final MapleGuildContents gs = hp.getClient().getChannelServer().getGuildSummary(hp.getGuildId());
            if (gs != null) {
                packet.writeMapleAsciiString(gs.getName());
                if (gs.getAllianceId() > 0) {
                    final MapleAlliance alliance = ChannelServer.getAlliance(gs.getAllianceId());
                    if (alliance != null) {
                        packet.writeMapleAsciiString(GameConstants.��ũ(hp.getRankPoint()));
                    } else {
                        packet.writeMapleAsciiString(GameConstants.��ũ(hp.getRankPoint()));
                    }
                } else {
                    packet.writeMapleAsciiString(GameConstants.��ũ(hp.getRankPoint()));
                }
            } else {
                packet.writeMapleAsciiString("-");
                packet.writeMapleAsciiString(GameConstants.��ũ(hp.getRankPoint()));
            }
        }
        packet.write(0xFF);
        packet.write(0);
        IItem inv = null;
        int peteqid = 0;
        boolean petpacket = false;
        int petindex = 0;
        int position = 114;
        for (final MaplePet pet : hp.getPets()) {
            if (pet != null) {
                if (petindex >= 1) {
                    position = 123 + petindex;
                }
                inv = hp.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -position);
                peteqid = inv != null ? inv.getItemId() : 0;
                if (!petpacket) {
                    packet.write(1); //�� ��ȯ ó�� üũ
                }
                packet.write(1); //�� �����
                packet.writeInt(petindex++);
                packet.writeInt(pet.getPetItemId()); // petid
                packet.writeMapleAsciiString(pet.getName());
                packet.write(pet.getLevel()); // pet level
                packet.writeShort(pet.getCloseness()); // pet closeness
                packet.write(pet.getFullness()); // pet fullness
                packet.writeShort(pet.getSkillValue());
                packet.writeInt(peteqid);
                packet.writeInt(-1); //v171
                petpacket = true;
            }
        }
        if (!petpacket) {
            packet.write(0);
        }
        packet.write(0);
        packet.write(0);
        IItem medal = hp.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
        if (medal != null) {
            packet.writeInt(medal.getItemId());
        } else {
            packet.writeInt(0);
        }
        ArrayList<Integer> medalQuests = new ArrayList<>();
        ArrayList<Long> medalQuestsTime = new ArrayList<>();
        for (MapleQuestStatus q : hp.getCompletedQuests()) {
            if (q.getQuest().getMedalItem() != 0) {
                medalQuests.add((Integer) q.getQuest().getId());
                medalQuestsTime.add(q.getCompletionTime());
            }
        }
        Collections.sort(medalQuests);
        packet.writeShort(medalQuests.size());
        int iz = 0;
        for (Integer i : medalQuests) {
            packet.writeInt(i); //1.2.251+
            packet.writeLong(PacketProvider.getTime(medalQuestsTime.get(iz)));
            ++iz;
        }
        /* 1.2.251 �߰�, �߰� �ڵ� ���. */
        packet.write(1); //��������Ų ������.
        packet.writeInt(0); 
        for (int i = 0; i < 1; i++) { //������ ��ŭ ���ư�.
            packet.writeInt(2431965); 
            packet.write(0);
            packet.writeMapleAsciiString("�⺻ ������ ��Ų�̴�.\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n");
        }
        packet.writeInt(-1);
        packet.writeInt(0);
        packet.write(1);
        packet.write0(6);
        /* 1.2.251 �߰�, �߰� �ڵ� ���. */      
        packet.write(GameConstants.getTraitLevel(hp.getStat().getAmbition())); // ambition
        packet.write(GameConstants.getTraitLevel(hp.getStat().getInsight())); // insight
        packet.write(GameConstants.getTraitLevel(hp.getStat().getWillPower())); // willpower
        packet.write(GameConstants.getTraitLevel(hp.getStat().getDiligence())); // diligence
        packet.write(GameConstants.getTraitLevel(hp.getStat().getEmpathy())); // empathy
        packet.write(GameConstants.getTraitLevel(hp.getStat().getCharm())); // charm
        packet.writeInt(hp.getClient().getAccID());
        packet.writeMapleAsciiString(ServerConstants.serverName);
        packet.write0(29);
        packet.writeInt(hp.getGender());
        
        return packet.getPacket();
    }

    public static Packet giveJaguarRiding(int buffid, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.MONSTER_RIDING);
        packet.write0(9);
        packet.writeInt(buffid);
        packet.writeInt(skillid);
        packet.write(1);
        packet.write(1);
        packet.write0(7);
        packet.write(1);
        packet.write(6);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static Packet cancelJaguarRiding() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.MONSTER_RIDING);
        packet.write(7); //1,2,239+
        packet.write(1); //1.2.239+
        
        return packet.getPacket();
    }

    public static Packet givePirate(List<Triple<BuffStats, Integer, Boolean>> statups, int duration, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005 || skillid == 15121005;
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        packet.write0(9); //�뽬�� ���� �ν��� ����, 1.2.239+
        for (Triple<BuffStats, Integer, Boolean> stat : statups) {
            if (!stat.getThird()) {
                packet.writeInt(stat.getSecond());
                packet.writeInt(skillid);
                packet.write(!infusion ? 1 : 0);
                packet.writeInt(!infusion ? 1 : 0);
                if (infusion) {
                    packet.write0(5);
                }
                packet.writeShort(duration);
            }
        }
        packet.writeInt(infusion ? 600 : 0);
        if (!infusion) {
            packet.write(1); 
        }
       packet.write(infusion ? 0 : 0x08);
       packet.writeInt(0); //v192
        
        return packet.getPacket();
    }

    public static Packet giveForeignPirate(List<Triple<BuffStats, Integer, Boolean>> statups, int duration, int cid, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005 || skillid == 15121005;
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeBuffMask(packet, statups);
        packet.write0(23); 
        for (Triple<BuffStats, Integer, Boolean> stat : statups) {
            packet.writeInt(stat.getSecond().intValue());
            packet.writeInt(skillid);
            packet.writeInt(0);
            packet.write0(infusion ? 7 : 1);
            packet.writeShort(duration);
        }
        packet.writeShort(infusion ? 600 : 0);

        return packet.getPacket();
    }

    public static Packet giveEnergyCharge(int bar, int maxbar, int skillid, int charge, boolean Maximum, boolean Cooling) {
        WritingPacket packet = new WritingPacket();
        boolean active = ((Maximum) || (Cooling));
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.ENERGY_CHARGE);
        packet.write0(active ? 5 : 9);
        if (active) {
            packet.writeInt(skillid);
        }
        packet.writeShort(active ? charge : Math.min(bar, maxbar)); // 0 = no bar, 10000 = full bar
        packet.writeLong(Cooling ? 0x101000000000000L : 0);
        packet.writeLong(0);
        packet.write(0x1F);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    public static Packet giveForeignEnergyCharge(int cid, int bar) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, BuffStats.ENERGY_CHARGE);
        packet.write0(19);
        packet.writeInt(bar == 10000 ? 5120018 : 0);
        packet.writeShort(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        packet.write0(13);
        
        return packet.getPacket();
    }

    public final static Packet giveArcane(int skillid, int x, Map<Integer, Integer> statups, int duration) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.ARCANE_AIM);
        packet.writeShort(x);
        packet.writeInt(skillid);
        packet.writeInt(duration);
        packet.write0(18);
        
        return packet.getPacket();
    }

    public static final Packet giveAffinity(int skillid, int value, int bufflength) {
        WritingPacket packet = new WritingPacket();
        final List<Triple<BuffStats, Integer, Boolean>> statups = Collections.singletonList(new Triple<>(BuffStats.AFFINITY, 0, false));
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        packet.writeShort(value);
        packet.writeInt(skillid);
        packet.writeInt(bufflength);
        packet.write0(13);
      
      return packet.getPacket();
    }

    public static Packet giveDice(int buffid, int skillid, int duration, List<Triple<BuffStats, Integer, Boolean>> statups) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        packet.writeShort(buffid);
        packet.writeInt(skillid);
        packet.writeInt(duration);
        packet.write0(5);
        /*
         * 2 : 30
         * 3 : 20, 20
         * 4 : 15
         * 5 : 20
         * 6 : 30
         *
         */
        packet.writeInt(buffid == 3 ? 30 : 0); //MAX HP
        packet.writeInt(buffid == 3 ? 30 : 0); //MAX MP
        packet.writeInt(buffid == 4 ? 15 : 0); //CRITICAL RATE
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(buffid == 2 ? 30 : 0); //Physical Defense
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(buffid == 5 ? 20 : 0); //Increase Damage
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(buffid == 6 ? 30 : 0); //Increase EXP Rate
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0); //1.2.214+
        packet.writeInt(0);
        packet.write(1);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static Packet giveDoubleDice(int buffid, int skillid, int duration, List<Triple<BuffStats, Integer, Boolean>> statups) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        packet.writeShort(buffid);
        packet.writeInt(skillid);
        packet.writeInt(duration);
        packet.write0(5);
        int dice2 = 0, dice3 = 0, dice4 = 0, dice5 = 0, dice6 = 0;
        switch (buffid & 0xF) {
            case 2:
                dice2 += 30;
                break;
            case 3:
                dice3 += 30;
                break;
            case 4:
                dice4 += 15;
                break;
            case 5:
                dice5 += 20;
                break;
            case 6:
                dice6 += 30;
                break;
        }
        if ((buffid & 0xF) == 0x1 || ((buffid & 0xF) == ((buffid & 0xF0) / 10))) {
            switch ((buffid & 0xF0) / 10) {
                case 2:
                    dice2 += 30;
                    break;
                case 3:
                    dice3 += 30;
                    break;
                case 4:
                    dice4 += 15;
                    break;
                case 5:
                    dice5 += 20;
                    break;
                case 6:
                    dice6 += 30;
                    break;
            }
        }
        packet.writeInt(dice3); //MAX HP
        packet.writeInt(dice3); //MAX MP
        packet.writeInt(dice4); //CRITICAL RATE
        packet.writeInt(0);
        
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        
        packet.writeInt(dice2); //Physical Defense
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        
        packet.writeInt(dice5); //Increase Damage
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        
        packet.writeInt(0);
        packet.writeInt(dice6); //Increase EXP Rate
        packet.writeInt(0);
        packet.writeInt(0);
        
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0); //1.2.214+
        packet.writeInt(0);
        
        packet.write(1);
        packet.writeInt(0);

        return packet.getPacket();
    }
    
    public static Packet noteMessage(int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        packet.write(value);
        if (value == 6) {
            packet.write(5);
        }
        return packet.getPacket();
    }
    
    public static Packet showNotes(ResultSet notes, int count) throws SQLException {
	WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
	packet.write(4); //1.2.251+ (+1)
	packet.write(count);
	for (int i = 0; i < count; i++) {
	    packet.writeInt(notes.getInt("id"));
	    packet.writeMapleAsciiString(notes.getString("from"));
	    packet.writeMapleAsciiString(notes.getString("message"));
	    packet.writeLong(PacketProvider.getKoreanTimestamp(notes.getLong("timestamp")));
	    packet.write(0);
	    notes.next();
	}
	return packet.getPacket();
    }

    public static Packet givePhantomJudgement(int buffid, int bufflength, List<Triple<BuffStats, Integer, Boolean>> statups, int type) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        for (Triple<BuffStats, Integer, Boolean> statup : statups) {
            if (!statup.getThird()) {
                packet.writeShort(type);
                packet.writeInt(buffid);
                packet.writeInt(bufflength);
            }
        }
        packet.write0(5);
        packet.writeInt(type == 1 ? 10 : 20);
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(0);
        
        return packet.getPacket();
    }
    
        public static Packet showAngelicBlessBuffEffect(int cid, int ringid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, BuffStats.ITEM_EFFECT);
        packet.writeShort(1);
        packet.writeInt(-ringid);
        packet.writeShort(0); 
        packet.write(0x18);
        packet.write0(22);
        
        return packet.getPacket();
    }


    public static Packet giveBuff(int buffid, int bufflength, List<Triple<BuffStats, Integer, Boolean>> statups, SkillStatEffect effect, Map<BuffStats, List<StackedSkillEntry>> stacks, int animationTime) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        for (Triple<BuffStats, Integer, Boolean> statup : statups) {
            if (!statup.getThird()) {
                if (buffid == 23101003 || buffid == 65101002 || buffid == 4111002 || buffid == 4211008
                        || buffid == 14111000 || buffid == 4331002 || buffid == 36111006 || buffid == 15121004
                        || buffid == 31121054 || buffid == 65121004 || buffid == 80001428 || buffid == 3210013
                        || buffid == 13110026 || buffid == 101120109) {
                    packet.writeInt(statup.getSecond().intValue());
                } else {
                    packet.writeShort(statup.getSecond().shortValue());
                }
                packet.writeInt(buffid);
                packet.writeInt(bufflength);
            }
        }
        
        if (buffid == 1301013) {
            packet.writeInt(0);
            packet.write(5);
        }
        
        packet.writeInt(0);
        
        if (buffid == 80001757) {
            packet.write(0x0A);
        } else {
            packet.write(0);
        }
        
        if (buffid == 2321054 || buffid == 13120008 || buffid == 32001016 || buffid == 32101009 || buffid == 32111012 || buffid == 32121017 || buffid == 32121018 || buffid == 51111008) {
            packet.writeInt(buffid == 51111008 ? 1 : 0);
        }

        if (buffid == 1211014 || buffid == 32001016 || buffid == 32101009 || buffid == 32111012 || buffid == 32121017 
            || buffid == 32121018 || buffid == 2221054 || buffid == 36121003 || buffid == 11101022 || buffid == 51111008 
            || buffid == 11111022 || buffid == 33101006 || buffid == 100001263 || buffid == 100001264) {
                packet.write(buffid == 3111011 || buffid == 3211012 ? 0 : buffid == 33101006 ? 0x5A : 1);
        }
       
        /* Stack Buff ó�� ����, ����*/
        if (buffid == 2321005 || buffid == 3111011 || buffid == 3211012 || buffid == 36111003 || buffid == 25121209 || buffid == 27121005) { 
            packet.writeInt(buffid == 36111003 || buffid == 2321005 ? 10 - effect.getX() : effect.getX());
            if (buffid == 36111003) {
                packet.write(0);
            } else if (buffid == 25121209 || buffid == 2321005) {
                packet.writeInt(0);
            } else if (buffid == 3111011 || buffid == 3211012) {
                packet.writeLong(0);
            }
        } else { 
            packet.writeInt(buffid == 80001428 ? 0x78 : buffid == 1301013 ? buffid : buffid == 1311015 || buffid == 51111008 ? 1 : buffid == 5121055 ? 5120018 : 0);
            if (buffid == 2111011 || buffid == 2211012 || buffid == 2311012 || buffid == 3211011 || buffid == 27111004
                    || buffid == 30021237 || buffid == 32121054) {
                packet.write(buffid == 32121054 ? 1 : 0);
            } else if (buffid == 1301013 || buffid == 1311015 || buffid == 1321015 || buffid == 2321054 || buffid == 3121002
                    || buffid == 3221002 || buffid == 27121006 || buffid == 32111005 || buffid == 33121004 || buffid == 36121004
                    || buffid == 80001428 || buffid == 13121005 || buffid == 12120013 || buffid == 12120014 || buffid == 3221054
                    || buffid == 11111023 || buffid == 24121004) {
                packet.writeInt(0);
            } else if (buffid == 51111008) {
                packet.writeLong(0);
            }
        }
        
        /* Stack Buff ó�� ����, ���� */
        
        for (Triple<BuffStats, Integer, Boolean> statup : statups) {
            if (statup.getThird()) {
                packet.writeInt(stacks.get(statup.getFirst()).size());
                for (StackedSkillEntry sse : stacks.get(statup.getFirst())) {
                    packet.writeInt(sse.getSkillId());
                    packet.writeInt(sse.getValue());
                    for (int i = 0; i < 2; i++) {
                        packet.writeInt(sse.getTime()); //1.2.252+
                    }
                    packet.writeLong(sse.getBuffLength()); //1.2.251+
                }
            }
        }

        /* ��ų ������ ���� */
        if ((animationTime > 0) && (buffid != 101120109)) {
            packet.writeShort(animationTime * 10);
        } else if (buffid == 101120109) {
            for (Triple<BuffStats, Integer, Boolean> statup : statups) {
                if (statup.getFirst() == BuffStats.IMMUNITY_BARRIER) {
                    packet.writeInt(statup.getSecond().intValue());
                    break;
                }
            }
            packet.writeShort(0);
        } else {
            packet.writeShort(0);
        }
        /* ��ų ������ ���� */
        packet.writeShort(0); 
        /* SkillType ���� */
        packet.write(1);
        /* SkillType ���� */
        packet.writeLong(0); //1.2.251+, ��� ���� �ñ� ����.
        
        return packet.getPacket();
    }

    public static Packet giveSpiritLink(int duration, int skill1, int skill2) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.SPIRIT_LINK);
        packet.writeInt(skill1);
        packet.writeInt(skill2);
        packet.writeInt(duration);
        packet.write0(9);
        packet.writeInt(0); //v192
        
        return packet.getPacket();
    }

    public static Packet giveSurPlus(int surplus) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.SURPLUS);
        packet.writeShort(surplus);
        packet.writeInt(30020232);
        packet.write0(22);
        
        return packet.getPacket();
    }
    
    public static Packet givePsychicPoint(int skillid, int point) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.KINESIS_PP);
        packet.writeShort(point);
        packet.writeInt(skillid);
        packet.write0(22);
        
        return packet.getPacket();
    }

    public static Packet giveDebuff(final DiseaseStats stats, int x, final MobSkill skill) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, stats);
        if (stats.equals(DiseaseStats.TELEPORT)) {
            packet.writeShort(skill.getY());
        }
        if (stats.equals(DiseaseStats.ZOMBIFY)) {
            packet.write0(9);
        }
        packet.writeShort(x);
        packet.writeShort(skill.getSkillId());
        packet.writeShort(skill.getSkillLevel());
        if (!stats.equals(DiseaseStats.ZOMBIFY)) {
            packet.writeInt(skill.getDuration());
            packet.write0(9);
        }
        if (stats.STUN != null) {
            packet.writeShort(0);
        } else if (stats.SEAL != null || stats.CURSE != null || stats.BLIND != null) {
            packet.writeShort(900);
        } else if (stats.SEDUCE != null || stats.TRANSFORM != null) {
            packet.writeShort(2400); //1.2.239+
        } else if (stats.TELEPORT != null || stats.WEAKEN != null) {
            packet.writeShort(2350); //�Ӹ����� �߱������ �ð�
        } else if (stats.ZOMBIFY != null) {
            packet.writeShort(60);
        } else {
            packet.write(0);
        }
        if (stats.SLOW != null) {
            packet.writeInt(1500);
        }
        if (stats.SLOW != null) {
            packet.write(4);
        }
        packet.write0(14); //1.2.251+, ������ �� ����.
        
        return packet.getPacket();
    }

    public static Packet giveForeignDebuff(int cid, final DiseaseStats statups, MobSkill skill) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, statups);
        if (skill.getSkillId() == 125) {
            packet.writeShort(0);
        }
        packet.writeShort(skill.getX());
        packet.writeShort(skill.getSkillId());
        packet.writeShort(skill.getSkillLevel());
        packet.writeLong(0);
        packet.writeLong(0);
        packet.write(0);
        packet.writeInt(0);
        packet.writeShort(0);
        packet.writeShort(900); //Delay
        
        return packet.getPacket();
    }

    public static Packet cancelForeignDebuff(int cid, DiseaseStats stats) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, stats);

        return packet.getPacket();
    }

     public static Packet giveMount(int buffid, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.MONSTER_RIDING);
        packet.write0(9);
        packet.writeInt(buffid);
        packet.writeInt(skillid);
        packet.write0(9); 
        packet.write(1);
        packet.write(9);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    public static Packet showMonsterRiding(int cid, int itemId, int skillId) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, BuffStats.MONSTER_RIDING);
        packet.write0(23);
        packet.writeInt(itemId); 
        packet.writeInt(skillId); 
        packet.write0(7);
        
        return packet.getPacket();
    }

    public static Packet giveForeignBuff(int cid, List<Triple<BuffStats, Integer, Boolean>> statups, SkillStatEffect effect) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeBuffMask(packet, statups);
        for (Triple<BuffStats, Integer, Boolean> statup : statups) {
            switch (effect.getSourceId()) {
                case 36111006:
                case 30021237:
                    packet.writeShort(1394);
                    break;
                case 65101002:
                    packet.writeShort(1000);
                    break;
                case 61111008:
                case 61120008:
                case 61121053:
                    packet.write(0x0A);
                    packet.writeShort(1201);
                    break;
                default:
                    packet.writeShort(statup.getSecond().shortValue());
                    break;
            }
            if (statup.getFirst() == BuffStats.SHADOW_PARTNER
                    || statup.getFirst() == BuffStats.WK_CHARGE
                    || statup.getFirst() == BuffStats.MORPH
                    || statup.getFirst() == BuffStats.ITEM_EFFECT
                    || statup.getFirst() == BuffStats.SAINT_SAVER
                    || statup.getFirst() == BuffStats.AURA
                    || statup.getFirst() == BuffStats.REVERSE_DAMAGE
                    || statup.getFirst() == BuffStats.INFLATION
                    || statup.getFirst() == BuffStats.SPIRIT_LINK
                    || statup.getFirst() == BuffStats.DAMAGE_RESIST && effect.getSourceId() == 23111005
                    || statup.getFirst() == BuffStats.DMG_DEC
                    || statup.getFirst() == BuffStats.HEAD_EFFECT
                    || effect.getSourceId() == 61111008
                    || effect.getSourceId() == 61120008
                    || effect.getSourceId() == 61121053
                    || effect.getSourceId() == 65121004
                    || effect.getSourceId() == 65121054
                    || (effect.getSourceId() == 30021237)) {
                packet.writeInt(effect.isSkill() ? effect.getSourceId() : -effect.getSourceId());
            }
        }
        packet.writeShort(effect.getSourceId() == 11101022 || effect.getSourceId() == 11111022 ? 1 : 0); 
        packet.write(effect.getSourceId() == 11121005 ? 0x10 : 0);
        packet.writeLong(effect.getSourceId() == 11101022 || effect.getSourceId() == 11111022 ? 0x10 : 0); 
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0); //��Ƽ��ų �� ������.

        return packet.getPacket();
    }

    public static final <T extends GlobalBuffStat> Packet cancelForeignBuff(int cid, int skillid, List<T> statups) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeBuffMaskFromList(packet, statups);
        if (GameConstants.SoulMoonSkill(skillid)) {
            packet.writeShort(0x101); //1.2.251+
        }
        return packet.getPacket();
    }

    public static final <T extends GlobalBuffStat> Packet cancelForeignRiding(int cid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, BuffStats.MONSTER_RIDING);
        packet.write(1);
        
        return packet.getPacket();
    }

    public static final <T extends GlobalBuffStat> Packet cancelBuff(List<T> statups, boolean mount, boolean saint, Map<BuffStats, List<StackedSkillEntry>> stacks) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        if (statups != null) {
            PacketProvider.writeBuffMaskFromList(packet, statups);
            for (T buffs : statups) {
                if (stacks.containsKey((BuffStats) buffs)) {
                    packet.writeInt(stacks.get((BuffStats) buffs).size());
                    for (StackedSkillEntry sse : stacks.get((BuffStats) buffs)) {
                        packet.writeInt(sse.getSkillId());
                        packet.writeInt(sse.getValue());
                        for (int i = 0; i < 2; i++) {
                            packet.writeInt(sse.getTime()); //1.2.252+
                        }
                        packet.writeLong(sse.getBuffLength()); //1.2.252+
                    }
                }
            }
            packet.write0(statups.size() * 4);
            packet.write(statups.size());
            packet.writeInt(0);
        } else {
            PacketProvider.writeSingleMask(packet, BuffStats.MONSTER_RIDING);
            packet.write(0x0C);
            packet.write(1);
        }
        return packet.getPacket();
    }

    public static Packet cancelDebuff(DiseaseStats stats) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, stats);
        packet.write(3);
        packet.write(1);

        return packet.getPacket();
    }

    public static Packet cancelExeed() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.ENHANCED_MAXMP);
        
        return packet.getPacket();
    }

    public static Packet updateMount(MapleCharacter chr, boolean levelup) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        packet.writeInt(chr.getId());
        packet.writeInt(chr.getMount().getLevel());
        packet.writeInt(chr.getMount().getExp());
        packet.writeInt(chr.getMount().getFatigue());
        packet.write(levelup ? 1 : 0);

        return packet.getPacket();
    }

    public static Packet mountInfo(MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        packet.writeInt(chr.getId());
        packet.write(1);
        packet.writeInt(chr.getMount().getLevel());
        packet.writeInt(chr.getMount().getExp());
        packet.writeInt(chr.getMount().getFatigue());

        return packet.getPacket();
    }

    public static Packet getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x18);
        packet.write(0x19); //v192 +9
        packet.write(owner ? 0 : 1);
        packet.writeMapleAsciiString(c.getName() + " : " + chat);
        return packet.getPacket();
    }

    public static Packet getTradePartnerAdd(MapleCharacter c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(PlayerInteractionHandler.VISIT);
        packet.write(1);
        PacketProvider.addPlayerLooks(packet, c, false);
        packet.writeMapleAsciiString(c.getName());
        packet.writeShort(c.getJob());
        
        return packet.getPacket();
    }

    public static Packet getTradeInvite(MapleCharacter c, boolean isCashTrade) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(PlayerInteractionHandler.INVITE_TRADE);
        packet.write(isCashTrade ? 7 : 4);
        packet.writeMapleAsciiString(c.getName());
        packet.writeInt(0); //TradeID
        
        return packet.getPacket();
    }

    public static Packet getTradeMesoSet(byte number, long meso) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(4);
        packet.write(number);
        packet.writeLong(meso);
        
        return packet.getPacket();
    }

    public static Packet getTradeItemAdd(byte number, IItem item) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0);
        packet.write(number);
        PacketProvider.addItemInfo(packet, item, false, false, true, null);
        
        return packet.getPacket();
    }

    public static Packet getTradeStart(MapleClient c, MapleUserTrade trade, byte number, boolean isCashTrade) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x14);
        packet.write(isCashTrade ? 7 : 4);
        packet.write(2);
        packet.write(number);
        if (number == 1) {
            packet.write(0);
            PacketProvider.addPlayerLooks(packet, trade.getPartner().getChr(), false);
            packet.writeMapleAsciiString(trade.getPartner().getChr().getName());
            packet.writeShort(trade.getPartner().getChr().getJob());
        }
        packet.write(number);
        PacketProvider.addPlayerLooks(packet, c.getPlayer(), false);
        packet.writeMapleAsciiString(c.getPlayer().getName());
        packet.writeShort(c.getPlayer().getJob());
        packet.write(0xFF);

        return packet.getPacket();
    }

    public static Packet getTradeConfirmation() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(8); //v192 +6

        return packet.getPacket();
    }

    public static Packet StartRPS() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x60);

        return packet.getPacket();
    }

    public static Packet FinishRPS(byte result, byte rps) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x71);
        packet.write(result);
        packet.write(rps);

        return packet.getPacket();
    }

    public static Packet ExitRPS() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x1C);
        packet.write(0);
        packet.write(0x03);

        return packet.getPacket();
    }

    public static Packet TradeMessage(final byte UserSlot, final byte message) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x1C); //v192 +6
        packet.write(UserSlot);
        packet.write(message);
        //0x07 = success [tax is automated]
        //0x08 = unsuccessful
        //0x09 = "You cannot make the trade because there are some items which you cannot carry more than one."
        //0x10 = "You cannot make the trade because the other person's on a different map."

        return packet.getPacket();
    }

    public static Packet getTradeCancel(final byte UserSlot) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x1C); //v192 +6
        packet.write(UserSlot);
        packet.write(2);

        return packet.getPacket();
    }

    public static Packet getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
        return getNPCTalk(npc, msgType, talk, endBytes, type, 0);
    }

    public static Packet getNPCTalk(int npcId, byte msgType, String talk, String endBytes, byte type, int speaker) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npcId);
        packet.write(0); //Not mean. Add. v192 
        packet.write(msgType);
        packet.writeShort(type); // 1 = No ESC, 3 = show character + no esc
        if (speaker != 0) {
            packet.writeInt(speaker);
        }
        if (msgType == 0x10) {
            packet.writeInt(0);
            packet.writeInt(5);
        }
        packet.writeMapleAsciiString(talk);
        packet.write(HexTool.getByteArrayFromHexString(endBytes));
        packet.writeInt(0); 
        
        return packet.getPacket();
    }

    public static Packet getQuickMove(List<QuickMoveEntry> quickmoves) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.QUICK_MOVE.getValue());
        packet.write(quickmoves.size());
        for (QuickMoveEntry q : quickmoves) {
            packet.writeShort(0); //1.2.193+
            packet.writeMapleAsciiString(q.getName());
            packet.writeInt(q.getNpcId());
            packet.writeInt(q.getIcon());
            packet.writeInt(q.getLevelLimit());
            packet.writeMapleAsciiString(q.getDesc());
            packet.writeLong(PacketProvider.getTime(-2));
            packet.writeLong(PacketProvider.MAX_TIME);
        }
        return packet.getPacket();
    }

    public static Packet getMapSelection(final int npcid, final String sel) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npcid);
        packet.write(0);
        packet.writeShort(0x11);
        packet.write0(9);
        packet.writeMapleAsciiString(sel);
        
        return packet.getPacket();
    }
    
    public static Packet getNPCTalkStyle(int npcId, String talk, boolean adventure, boolean isZero, int... args) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npcId);
        packet.write(0); //Not mean, 192+
        if (!adventure) {
            packet.writeShort(isZero ? 0x20 : 0x0A);
        } else {
            packet.writeInt(0x09);
        }
        packet.write(0);
        packet.writeMapleAsciiString(talk);
        if (isZero) {
            packet.writeInt(0);
        }
        packet.write(args.length);
        for (int i = 0; i < args.length; i++) {
            packet.writeInt(args[i]);
        }
        if (isZero) { //�ӽ�ó��, ���� �����ٶ�.
            packet.write(args.length);
            for (int i = 0; i < args.length; i++) {
                packet.writeInt(args[i]);
            }
        }
        return packet.getPacket();
    }

    public static Packet getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.write(0); //Not mean. Add. v192 
        packet.writeShort(4);
        packet.write(0);
        packet.writeMapleAsciiString(talk);
        packet.writeInt(def);
        packet.writeInt(min);
        packet.writeInt(max);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    public static Packet getNPCTalkText(int npc, String talk) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.write(0); //Not mean. Add. v192 
        packet.writeShort(3);
        packet.write(0);
        packet.writeMapleAsciiString(talk);
        packet.writeInt(0);
        packet.writeInt(0);
        
        return packet.getPacket();
    }
    
    public static Packet showOwnBuffEffect(int skillid, int effectid) {
        return showBuffeffect(-1, 20, skillid, effectid, 0, (byte) 3); //�÷��̾� ������ ��ų������ �̿�.
    }
    
    public static Packet showOwnBerserk(int playerLevel, int skillLevel, boolean Berserk) {
        return showBuffeffect(-1,  skillLevel, 1320006, 1, playerLevel,  Berserk ? (byte) 1 : (byte) 0);
    }
    
    public static Packet showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel) {
        return showBuffeffect(-1, skillLevel, skillid, effectid, playerLevel, (byte) 3);
    }

    public static Packet showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel) {
        return showBuffeffect(cid, skillLevel, skillid, effectid, playerLevel, (byte) 3);
    }
    
    public static Packet showBerserk(int cid, int playerLevel, int skillLevel, boolean Berserk) {
        return showBuffeffect(cid, skillLevel, 1320006, 1, playerLevel, Berserk ? (byte) 1 : (byte) 0);
    }
   
    public static Packet showBuffeffect(int cid, int level, int skillid, int effectid, int playerLevel, byte direction) {
        WritingPacket packet = new WritingPacket();
        if (cid == -1) {
            packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        } else {
            packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
            packet.writeInt(cid);
        }
        packet.write(effectid);
        packet.writeInt(skillid);
        if (effectid == 2 && skillid == 31111003) {
            packet.writeInt(0);
        }
        if (playerLevel > 0) {
            packet.write(playerLevel);
        }
        packet.write(level);
        if (skillid == 30001061 || skillid == 142121008) {
            packet.write(1);
        }
        if (direction != (byte) 3) {
            packet.write(direction);
        }
        return packet.getPacket();
    }
    
    public static Packet showNovaLift(int cid, int skillid, byte[] available) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(1);
        packet.writeInt(skillid);
        packet.writeShort(0x166);
        packet.write(available);
        packet.write(1);

        return packet.getPacket();
    }

    public static Packet captureMob(boolean success) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(1);
        packet.writeInt(30001061);
        packet.write(0x47); //1.2.239+
        packet.write(1);
        packet.write(!success ? 1 : 0);
        
        return packet.getPacket();
    }

    /**
     * ��� ä�� ���� �ڵ�
     *
     * <code>���� �ڵ�</code>:<br>
     * 0 : �Ϲ� ä��(���) 1 : �ӼӸ� ä��(�ʷϻ�) 2 : ��Ƽ ä��(��ȫ��) 3 : ģ�� ä��(��Ȳ��) 4 : ���
     * ä��(�����) 5 : (���� �ʷϻ�) 6 : (�ణ ũ�� ���� ��ȫ��) 7 : (ȸ��) 8 : (�����) 9 : (���� �����)
     * 10 : (�Ķ���) 11 : ��� ä��(��������� ������) 12 : (����) 13 : (�����Ķ������� �Ķ���) 15 :
     * (���������� ������) 17 : (�������) 18 : (�����Ķ��������� ��ȫ��) 19 : (���������� ������) 20 : (����������
     * ���) 21 : (����������� ������) 22 : (�ʷϻ������� ���) 23 : (�ʷϻ������� ����[W:-1]) 25 : (�����)
     * 26 : (�ϴû�) 27 : (���� �۾�ü)
     */
    public static Packet getGMText(int type, String text) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_TEXT.getValue());
        packet.writeShort(type);
        packet.writeMapleAsciiString(text);
        return packet.getPacket();
    }

    /*
     * �߰��� �Ű������� �ʿ����� ���� ����Ʈ *
     * 
     * 0x01 : ������
     * 0x0A : RESIST
     * 0x0C : ��Ż����
     * 0x0D : ��������
     * 0x12 : ���ͺ�
     * 0x14 : ������� (�Ǵ� �볭��) ������
     * 
     */
    public static Packet showSpecialEffect(int effect) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(effect);

        return packet.getPacket();
    }

    public static Packet showSpecialEffect(int cid, int effect) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(effect);

        return packet.getPacket();
    }

    public static Packet updateSkill(MapleCharacter chr, final Map<ISkill, SkillEntry> update) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        packet.write(1);
        packet.writeShort(0);
        packet.writeShort(update.size());
        for (final Entry<ISkill, SkillEntry> skills : update.entrySet()) {
            packet.writeInt(skills.getKey().getId());
            packet.writeInt(skills.getValue().skillevel);
            packet.writeInt(skills.getValue().masterlevel);
            packet.writeLong(PacketProvider.getTime(skills.getValue().expiration));
        }
        packet.write(4); //1.2.251+
        
        return packet.getPacket();
    }

    public static Packet updateProfessionSkill(int exp, int skillid, int level, int master) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        packet.write(0);
        packet.write(0);
        packet.write(0); // v203
        packet.writeShort(1);
        packet.writeInt(skillid);
        if (level == 0 && exp == 0) {
            packet.writeInt(-1); //Delete Skill
            packet.writeInt(0);
            packet.writeLong(PacketProvider.getTime(-2)); //zero Time
        } else if (exp != -1 && level != -1) {
            packet.writeShort(exp);
            packet.write(0);
            packet.write(level);
            packet.writeInt(master);
            packet.writeLong(PacketProvider.getTime(-1)); //max Time
        } else if (exp == 0 && level == 0) {
            packet.writeShort(exp);
            packet.write(0);
            packet.write(level);
            packet.writeInt(master);
            packet.writeLong(PacketProvider.getTime(-1)); //max Time
        } else {
            packet.writeInt(2147483647);
            packet.writeInt(0);
            packet.writeLong(PacketProvider.getTime(-1)); //max Time
        }
        packet.write(4);

        return packet.getPacket();
    }

    public static Packet updateQuestMobKills(final MapleQuestStatus status) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeInt(status.getQuest().getId()); //1.2.251+
        packet.write(1);
        final StringBuilder sb = new StringBuilder();
        for (final int kills : status.getMobKills().values()) {
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
        }
        packet.writeMapleAsciiString(sb.toString());
        packet.write0(8);

        return packet.getPacket();
    }

    public static Packet getShowQuestCompletion(int id) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        packet.writeInt(id); //1.2.251+

        return packet.getPacket();
    }

    public static Packet getKeymap(MapleKeyLayout layout) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.KEYMAP.getValue());
        layout.writeData(packet);

        return packet.getPacket();
    }

    public static Packet getPetAutoHP(int itemId) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
        packet.writeInt(itemId);
        return packet.getPacket();
    }

    public static Packet getPetAutoMP(int itemId) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
        packet.writeInt(itemId);
        return packet.getPacket();
    }
    
    public static void addRingInfo(WritingPacket packet, List<MapleRing> rings) {
        packet.write(rings.size());
        for (MapleRing ring : rings) {
            packet.writeInt(1);
            packet.writeLong(ring.getRingId());
            packet.writeLong(ring.getPartnerRingId());
            packet.writeInt(ring.getItemId());
        }
    }

    public static void addMRingInfo(WritingPacket packet, List<MapleRing> rings, MapleCharacter chr) {
        packet.write(rings.size());
        for (MapleRing ring : rings) {
            packet.writeInt(1);
            packet.writeInt(chr.getId());
            packet.writeInt(ring.getPartnerChrId());
            packet.writeInt(ring.getItemId());
        }
    }

    public static Packet getWhisper(String sender, int channel, String text) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(0x12);
        packet.writeMapleAsciiString(sender);
        packet.writeShort(channel);
        packet.writeMapleAsciiString(text);

        return packet.getPacket();
    }

    public static Packet getWhisperReply(String target, byte reply) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(0x0A);
        packet.writeMapleAsciiString(target);
        packet.write(reply);//  0x0 = cannot find char, 0x1 = success

        return packet.getPacket();
    }

    public static Packet getFindReply(String target, boolean friend, int channel) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(friend ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(3);
        packet.writeInt(channel);

        return packet.getPacket();
    }

    public static Packet getFindReplyWithCS(String target, boolean friend) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(friend ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(2);
        packet.writeInt(-1);

        return packet.getPacket();
    }

    public static Packet getFindReplyWithMap(String target, boolean friend, int mapid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(friend ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(1);
        packet.writeInt(mapid);
        packet.write0(8); // ?? official doesn't send zeros here but whatever

        return packet.getPacket();
    }

    public static Packet getInventoryFull() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(0);
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet getShowInventoryFull() {
        return getShowInventoryStatus(0xFF);
    }

    public static Packet showItemUnavailable() {
        return getShowInventoryStatus(0xFE);
    }

    public static Packet getShowInventoryStatus(int mode) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0);
        packet.write(mode);
        packet.writeInt(0);
        packet.writeInt(0);

        return packet.getPacket();
    }
    
    /* â�� ��Ŷ ���� */

    public static Packet getStorage(int npcId, byte slots, Collection<IItem> items, long meso) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x16);
        packet.writeInt(npcId);
        packet.write(slots);
        packet.writeLong(0x7E);
        packet.writeLong(meso);
        packet.writeShort(0);
        packet.write((byte) items.size());
        for (IItem item : items) {
            PacketProvider.addItemInfo(packet, item, true, true, null);
        }
        packet.writeShort(0);
        packet.write(0);

        return packet.getPacket();
    }

    public static Packet getStorageFull() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x11);

        return packet.getPacket();
    }

    public static Packet mesoStorage(byte slots, long meso) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x13);
        packet.write(slots);
        packet.writeShort(2);
        packet.writeShort(0);
        packet.writeInt(0);
        packet.writeLong(meso);

        return packet.getPacket();
    }

    public static Packet storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x0D);
        packet.write(slots);
        packet.writeShort(type.getBitfieldEncoding());
        packet.writeShort(0);
        packet.writeInt(0);
        packet.write(items.size());
        for (IItem item : items) {
            PacketProvider.addItemInfo(packet, item, true, true, null);
        }
        return packet.getPacket();
    }
    
    
   public static Packet arrangeStorage(byte slots, Collection<IItem> items, boolean changed) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x0F);
        packet.write(slots);
        packet.write(124);
        packet.write0(10);
        packet.write(items.size());
        for (IItem item : items) {
            PacketProvider.addItemInfo(packet, item, true, true, null);
        }
        packet.write(0);
        return packet.getPacket();
    }

    public static Packet takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x9);
        packet.write(slots);
        packet.writeShort(type.getBitfieldEncoding());
        packet.writeShort(0);
        packet.writeInt(0);
        packet.write(items.size());
        for (IItem item : items) {
            PacketProvider.addItemInfo(packet, item, true, true, null);
        }
        return packet.getPacket();
    }
    
    public static Packet get2ndPWChecking(boolean incorrect) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x15);
        packet.write(!incorrect ? 1 : 0);
        
        return packet.getPacket();
    }
    
    /* â�� ��Ŷ ���� */

    public static Packet fairyPendantMessage(int type, int percent) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FAIRY_PEND_MSG.getValue());
        packet.writeShort(21); // 0x15
        packet.writeInt(0); // idk
        packet.writeShort(0); // idk
        packet.writeShort(percent); // percent
        packet.writeShort(0); // idk

        return packet.getPacket();
    }

    public static Packet giveFameResponse(int mode, String charname, int newfame) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        packet.write(0);
        packet.writeMapleAsciiString(charname);
        packet.write(mode);
        packet.writeInt(newfame); //Change Short -> Int

        return packet.getPacket();
    }

    public static Packet giveFameErrorResponse(int status) {
        WritingPacket packet = new WritingPacket();

        /*	* 0: ok, use giveFameResponse<br>
         * 1: the username is incorrectly entered<br>
         * 2: users under level 15 are unable to toggle with fame.<br>
         * 3: can't raise or drop fame anymore today.<br>
         * 4: can't raise or drop fame for this character for this month anymore.<br>
         * 5: received fame, use receiveFame()<br>
         * 6: level of fame neither has been raised nor dropped due to an unexpected error*/
        packet.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        packet.write(status);

        return packet.getPacket();
    }

    public static Packet receiveFame(int mode, String charnameFrom) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        packet.write(5);
        packet.writeMapleAsciiString(charnameFrom);
        packet.write(mode);

        return packet.getPacket();
    }

    public static Packet partyCreated(MapleParty party) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        packet.write(0x10); //1.2.220+ (+1)
        packet.writeInt(party.getId());
        packet.writeInt(party.getLeader().getDoorTown());
        packet.writeInt(party.getLeader().getDoorTarget());
        packet.write(0); //Unk
        packet.writeInt(party.getLeader().getDoorPosition().x); 
        packet.writeInt(party.getLeader().getDoorPosition().y);
        packet.write(party.getVisible());
        packet.writeMapleAsciiString(party.getPatryTitle());

        return packet.getPacket();
    }
    
    public static Packet partyInvite(MapleCharacter from) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        packet.write(0x4);
        packet.writeInt(from.getParty().getId());
        packet.writeMapleAsciiString(from.getName());
        packet.writeInt(from.getLevel());
        packet.writeInt(from.getJob());
        packet.writeInt(0);
        packet.write(0);
        packet.write(0); //1.2.220+

        return packet.getPacket();
    }

    public static Packet partyStatusMessage(int message) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        packet.write(message);

        return packet.getPacket();
    }

    public static Packet partyStatusMessage(int message, String charname) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        packet.write(message); // 23: 'Char' have denied request to the party.
        packet.writeMapleAsciiString(charname);

        return packet.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, WritingPacket packet, boolean leaving, boolean exped) {
        List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
        if (party == null) {
            partymembers = new ArrayList<>();
        } else {
            partymembers = new ArrayList<>(party.getMembers());
        }
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            packet.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            packet.writeAsciiString(StringUtil.getRightPaddedStr(partychar.getName(), '\0', 13));
        }
        for (MaplePartyCharacter partychar : partymembers) {
            packet.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                packet.writeInt(1);
            } else {
                packet.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            packet.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                packet.writeInt(partychar.getChannel());
            } else {
                packet.writeInt(-2);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            packet.writeInt(0);
        }
        packet.writeInt(party.getLeader().getId());
        if (exped) {
            return;
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                packet.writeInt(partychar.getMapid());
            } else {
                packet.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                packet.writeInt(partychar.getDoorTown());
                packet.writeInt(partychar.getDoorTarget());
                packet.writeInt(0);
                packet.writeInt(partychar.getDoorPosition().x);
                packet.writeInt(partychar.getDoorPosition().y);
            } else {
                packet.writeInt(leaving ? 999999999 : 0);
                packet.writeLong(leaving ? 999999999 : 0);
                packet.writeLong(leaving ? -1 : 0);
            }
        }
        packet.write(party.getVisible());
        packet.writeMapleAsciiString(party.getPatryTitle());
    }

    public static Packet updateParty(int forChannel, MapleParty party, MaplePartyOperation op, MaplePartyCharacter target) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                packet.write(0x15); //1.2.220+ (+1)
                packet.writeInt(party.getId());
                packet.writeInt(target.getId());
                if (op == MaplePartyOperation.DISBAND) {
                    packet.write(0);
                    packet.writeInt(target.getId());
                } else {
                    packet.write(1);
                    if (op == MaplePartyOperation.EXPEL) {
                        packet.write(1);
                    } else {
                        packet.write(0);
                    }
                    packet.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, packet, op == MaplePartyOperation.LEAVE, false);
                }
                break;
            case JOIN:
                packet.write(0x18); //1.2.220+ (+1)
                packet.writeInt(party.getId());
                packet.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, packet, false, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                packet.write(0x0F); //1.2.220+ (+1)
                packet.writeInt(party.getId());
                addPartyStatus(forChannel, party, packet, op == MaplePartyOperation.LOG_ONOFF, false);
                break;
            case CHANGE_LEADER:
                packet.write(0x30); //1.2.220+ (+1)
                packet.writeInt(target.getId());
                packet.write(0);
                break;
            case CHANGE_PARTY_TITLE:
                packet.write(0x4D); //1.2.220 New+
                packet.write(party.getVisible()); 
                packet.writeMapleAsciiString(party.getPatryTitle());
                packet.writeInt(target.getId());
                packet.writeInt(party.getId());
                packet.writeInt(1); 
                packet.writeInt(1);
                packet.write(1); 
                break;
        }
        return packet.getPacket();
    }
   
    public static Packet partyPortal(int townId, int targetId, int skillId, Point position, boolean animation) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        packet.write(0x4E); //1.2.220 (+1)
        packet.write(animation ? 0 : 1);
        packet.writeInt(townId);
        packet.writeInt(targetId);
        packet.writeInt(skillId);
        packet.writePos(position);

        return packet.getPacket();
    }

    public static Packet updatePartyMemberHP(int cid, long curhp, int maxhp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        packet.writeInt(cid);
        packet.writeInt(curhp);
        packet.writeInt(maxhp);

        return packet.getPacket();
    }
    
    public static Packet buddyHello(MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        packet.write(0);
        
        return packet.getPacket();
    }
        
    public static Packet buddyChat(MapleCharacter chr, String text) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDY_CHAT.getValue());
        for (int i = 0; i < 2; i++) {
            packet.writeInt(chr.getAccountID());
        }
        packet.writeInt(chr.getId());
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        packet.writeMapleAsciiString(text);
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet GuildChat(MapleCharacter chr, final int guildId, String text) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_CHAT.getValue());
        packet.writeInt(chr.getAccountID());
        packet.writeInt(guildId);
        packet.writeInt(chr.getAccountID());
        packet.writeInt(chr.getId());
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        packet.writeMapleAsciiString(text);
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet multiChat(String name, String chattext, int mode) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MULTI_CHAT.getValue());
        packet.write(mode); 
        packet.writeMapleAsciiString(name);
        packet.writeMapleAsciiString(chattext);

        return packet.getPacket();
    }

    public static Packet getClock(int time) { // time in seconds
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CLOCK.getValue());
        packet.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        packet.writeInt(time);

        return packet.getPacket();
    }

    public static Packet getClockTime(int hour, int min, int sec) { // Current Time
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.CLOCK.getValue());
        packet.write(1); //Clock-Type
        packet.write(hour);
        packet.write(min);
        packet.write(sec);

        return packet.getPacket();
    }
    
    public static Packet spawnClockMist(MapleMist clock) {
      WritingPacket packet = new WritingPacket();
      packet.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
      packet.writeInt(clock.getObjectId());
      packet.write(1);
      packet.writeInt(clock.getMobOwner().getObjectId());
      packet.writeInt(clock.getMobSkill().getSkillId());
      packet.write(clock.getClockType());
      packet.writeShort(7);
      packet.writeInt(clock.getBox().x);
      packet.writeInt(clock.getBox().y);
      packet.writeInt(clock.getBox().x + clock.getBox().width);
      packet.writeInt(clock.getBox().y + clock.getBox().height);
      packet.writeInt(0);
      packet.writePos(clock.getMobOwner().getPosition());
      packet.writeInt(0);
      packet.writeInt(clock.getClockType() == 2 ? -15 : clock.getClockType() == 1 ? 15 : 0);
      packet.writeInt(120);

      return packet.getPacket();
    }

    public static Packet spawnMist(final MapleMist mist) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        packet.writeInt(mist.getObjectId());
        packet.write(0);
        if (mist.getOwner() != null) {
            packet.writeInt(mist.getOwner().getId());
            packet.writeInt(mist.getSourceSkill().getId());
            packet.write(mist.getSkillLevel());
        } else {
            packet.writeInt(mist.getMobOwner().getId());
            packet.writeInt(mist.getMobSkill().getSkillId());
            packet.write(mist.getMobSkill().getSkillLevel());
        }
        packet.writeShort(mist.getSkillDelay());
        packet.writeInt(mist.getBox().x);
        packet.writeInt(mist.getBox().y);
        packet.writeInt(mist.getBox().x + mist.getBox().width);
        packet.writeInt(mist.getBox().y + mist.getBox().height);
        packet.writeInt(mist.isShelter() ? 2 : (mist.isPoisonMist() || mist.isBurningRegion() || mist.isTimeCapsule() || mist.getSourceSkill().getId() == 80001431 || mist.getSourceSkill().getId() == 33111013 || mist.getSourceSkill().getId() == 35121052) ? 0 : 1);
        if (mist.getOwner() != null) {
            packet.writePos(mist.getOwner().getPosition());
        } else {
            packet.writeInt(0);
        }
        packet.write0(17); //1.2.251+
        if (mist.getSourceSkill().getId() == 4121015 || mist.getSourceSkill().getId() == 33111013 || mist.getSourceSkill().getId() == 35121052) {
            packet.write(mist.getOwner().getPosition().x < mist.getBox().x ? 1 : 0); 
        }
        return packet.getPacket();
    }

    public static Packet removeMist(final int oid, final boolean eruption) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        packet.writeInt(oid);
        packet.write(eruption ? 1 : 0); //1.2.181+
        
        return packet.getPacket();
    }

    public static Packet damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        packet.writeInt(cid);
        packet.writeInt(summonSkillId);
        packet.write(unkByte);
        packet.writeInt(damage);
        packet.writeInt(monsterIdFrom);
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet buddylistMessage(byte message, String name) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(message);
        if (message == 0x1C) {
            packet.writeMapleAsciiString(name);
        }
        return packet.getPacket();
    }

    public static Packet updateBuddylist(Collection<BuddylistEntry> buddylist, int deleted, int OtherCid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        if (deleted == 18) {
            packet.writeShort(0x2A); //1.2.239+ (+5)
            packet.writeInt(OtherCid);
        } else {
            packet.write(0x18); //1.2.239+ (+4)
            packet.writeInt(buddylist.size());
            for (BuddylistEntry buddy : buddylist) {
                packet.writeInt(buddy.getCharacterId());
                packet.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13)); 
                packet.write(0x7); //Unknown Packets.
                packet.writeInt(buddy.getChannel());
                packet.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getGroup(), '\0', 18)); 
                packet.writeInt(buddy.getCharacterId()); //AccID Packets
                packet.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13)); //NICKNAME
                packet.write0(260); //�޸� + �׷�
            }
        }
        return packet.getPacket();
    }

    public static Packet requestBuddylistAdd(MapleClient c, int cidFrom, String nameFrom, int levelFrom, int jobFrom, boolean add) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        if (!add) {
            packet.write(0x1B); //1.2.239+ (+5)
            packet.write(1); 
            packet.writeInt(cidFrom); 
            packet.writeInt(cidFrom); //AccID Packets
            packet.writeMapleAsciiString(nameFrom); 
            packet.writeInt(levelFrom); 
            packet.writeInt(jobFrom); 
            packet.writeInt(0);
            packet.writeInt(cidFrom);
            packet.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13));
            packet.write(0x6); //Unknown Packets.
            packet.writeInt(c.getChannel());
            packet.writeAsciiString("�׷� ������", 18);
            packet.writeInt(cidFrom); //AccID Packets
            packet.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13)); //NICKNAME
            packet.write0(261); //�޸�.
        } else {
            packet.write(0x28); //1.2.239+ (+5)
            packet.writeInt(cidFrom); 
            packet.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13));
            packet.write(0x5); //Unknown Packets.
            packet.writeInt(c.getChannel());
            packet.writeAsciiString("�׷� ������", 18);
            packet.writeInt(cidFrom); //AccID Packets
            packet.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13)); //NICKNAME
            packet.write0(260); //OtherCid + �޸� + �׷�.
        }
        return packet.getPacket();
    }
    
    public static Packet buddylistUpdate(int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDYLIST_UPDATE.getValue());
        packet.writeInt(value);
        
        return packet.getPacket();
    }

    public static Packet updateBuddyChannel(int characterid, int channel, String nameFrom, boolean isVisible) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(0x2C); //1.2.239+ (+5)
        packet.writeInt(characterid);
        packet.write0(5); 
        packet.writeInt(channel); 
        packet.write(isVisible ? 1 : 0); 
        packet.write(1); 
        packet.writeMapleAsciiString(nameFrom); //1.2.220+

        return packet.getPacket();
    }

    public static Packet itemEffect(int characterid, int itemid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        packet.writeInt(characterid);
        packet.writeInt(itemid);

        return packet.getPacket();
    }

    public static Packet updateBuddyCapacity(int capacity) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(0x2E); //1.2.239+ (+5)
        packet.write(capacity);

        return packet.getPacket();
    }

    public static Packet showChair(int characterid, int itemid, String chairtext) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        packet.writeInt(characterid);
        packet.writeInt(itemid);
        packet.writeInt(chairtext != null ? 1 : 0);
        if (chairtext != null) {
            packet.writeMapleAsciiString(chairtext);
        }
        packet.writeInt(0); //1.2.252+, Extended Chair Size.
        packet.writeInt(0); //1.2.252+, Unknown
        
        return packet.getPacket();
    }
  
    public static Packet cancelChair(MapleCharacter chr, int id) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        packet.writeInt(chr.getId()); //1.2.220+
        if (id == -1) {
            packet.write(0);
        } else {
            packet.write(1);
            packet.writeShort(id);
        }
        return packet.getPacket();
    }

    public static Packet spawnReactor(MapleReactor reactor) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        packet.writeInt(reactor.getObjectId());
        packet.writeInt(reactor.getReactorId());
        packet.write(reactor.getState());
        packet.writePos(reactor.getPosition());
        packet.write(reactor.getFacingDirection());
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static Packet triggerReactor(MapleReactor reactor, int cid, int stance) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        packet.writeInt(reactor.getObjectId());
        packet.write(reactor.getState());
        packet.writePos(reactor.getPosition());
        packet.writeShort(stance);
        packet.write(0);
        packet.write(5);
        packet.writeInt(cid); //1.2.241+

        return packet.getPacket();
    }

    public static Packet destroyReactor(MapleReactor reactor) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        packet.writeInt(reactor.getObjectId());
        packet.write(reactor.getState());
        packet.writePos(reactor.getPosition());

        return packet.getPacket();
    }

    public static Packet musicChange(String song) {
        return environmentChange(song, 7); // +1 [v181]
    }

    public static Packet showEffect(String effect) {
        return environmentChange(effect, 4); // +1 [v181]
    }

    public static Packet playSound(String sound) {
        return environmentChange(sound, 5); // +1 [v181]
    }

    public static Packet environmentChange(String env, int mode) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        packet.write(mode);
        packet.writeMapleAsciiString(env);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    public static Packet startMapEffect(String msg, int itemid, boolean active) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        packet.writeInt(itemid);
        packet.writeMapleAsciiString(msg);
        packet.write(active ? 0 : 1); //1.2.252+
         
        return packet.getPacket();
    }

    public static Packet removeMapEffect() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        packet.writeInt(0);

        return packet.getPacket();
    }
    
    public static Packet startMapEffectTime(String msg, int id, int time) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MAP_EFFECT_TIME.getValue());
        packet.writeMapleAsciiString(msg);
        packet.writeInt(id);
        packet.writeInt(time);
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet createGuildInfo(MapleCharacter c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x38); 

        MapleGuildCharacter initiator = c.getMGC();
        MapleGuild g = c.getClient().getChannelServer().getGuild(initiator);
        if (g == null) { //failed to read from DB - don't show a guild
            packet.write(0);
            return packet.getPacket();
        } else {
            //MapleGuild holds the absolute correct value of guild rank after it is initiated
            MapleGuildCharacter mgc = g.getMGC(c.getId());
            c.setGuildRank(mgc.getGuildRank());
        }
        packet.writeInt(c.getGuildId()); //not entirely sure about this one
        packet.writeMapleAsciiString(g.getName());
        for (int i = 1; i <= 5; i++) {
            packet.writeMapleAsciiString(g.getRankTitle(i));
        }
        g.addMemberData(packet);
        packet.writeInt(g.getCapacity());
        packet.writeShort(g.getLogoBG());
        packet.write(g.getLogoBGColor());
        packet.writeShort(g.getLogo());
        packet.write(g.getLogoColor());
        packet.writeMapleAsciiString(g.getNotice());
        packet.writeInt(500); //��ġ
        packet.writeInt(500); //��ġ
        packet.writeInt(g.getAllianceId());
        packet.write(1); // GuildLevel
        packet.writeShort(1); // GuildRank
        packet.writeShort(g.getGP()); // GuildPoint
        packet.write0(5); // 1.2.203+

        return packet.getPacket();
    }

    public static Packet showGuildInfo(MapleCharacter c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x31); 
        packet.write(0);
        
        if (c == null) { //failed to read from DB - don't show a guild
            packet.write(0);
            return packet.getPacket();
        } else {
            packet.write(1);
            MapleGuildCharacter initiator = c.getMGC();
            MapleGuild g = c.getClient().getChannelServer().getGuild(initiator);

            MapleGuildCharacter mgc = g.getMGC(c.getId());
            c.setGuildRank(mgc.getGuildRank());

            packet.writeInt(c.getGuildId());//not entirely sure about this one
            packet.writeMapleAsciiString(g.getName());
            for (int i = 1; i <= 5; i++) {
                packet.writeMapleAsciiString(g.getRankTitle(i));
            }
            g.addMemberData(packet);
            packet.writeInt(g.getCapacity());
            packet.writeShort(g.getLogoBG());
            packet.write(g.getLogoBGColor());
            packet.writeShort(g.getLogo());
            packet.write(g.getLogoColor());
            packet.writeMapleAsciiString(g.getNotice());
            packet.writeInt(500); // ��ġ
            packet.writeInt(500); // ��ġ
            packet.writeInt(g.getAllianceId());
            packet.write(1); // GuildLevel
            packet.writeShort(1); // GuildRank
            packet.writeShort(g.getGP()); // GuildPoint
            packet.write0(132); //1.2.239+
        }
        return packet.getPacket();
    }

    public static Packet guildLeaderChanged(int gid, int oldLeader, int newLeader, int allianceId) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x79); //1.2.251+ (+1)
        packet.writeInt(gid);
        packet.writeInt(oldLeader);
        packet.writeInt(newLeader);
        packet.write(0);

        return packet.getPacket();
    }

    public static Packet guildSkillPurchased(int gid, int sid, int level, long expiration, String purchase, String activate) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x71); //1.2.251+ (+1)
        packet.writeInt(gid);
        packet.writeInt(sid);
        packet.writeShort(level);
        packet.writeLong(PacketProvider.getTime(expiration));
        packet.writeMapleAsciiString(purchase);
        packet.writeMapleAsciiString(activate);

        return packet.getPacket();
    }

    public static Packet guildMemberOnline(int gid, int cid, boolean bOnline) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x5F); //1.2.251+ (+1)  
        packet.writeInt(gid);
        packet.writeInt(cid);
        packet.writeShort(bOnline ? 0x101 : 0); //ON : 01 01, OFF : 00 00

        return packet.getPacket();
    }

    public static Packet guildInvite(int gid, String charName, int levelFrom, int jobFrom, int cidFrom) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x07);
        packet.writeInt(gid);
        packet.writeMapleAsciiString(charName);
        packet.writeInt(levelFrom);
        packet.writeInt(jobFrom);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    public static Packet guildLevel(int gid, int exp, int level) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x68);
        packet.writeInt(gid);
        packet.writeInt(exp);
        packet.writeInt(level);

        return packet.getPacket();
    }

    public static Packet denyGuildInvitation(String charname) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x3E);
        packet.writeMapleAsciiString(charname);

        return packet.getPacket();
    }

    public static Packet genericGuildMessage(byte code) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(code);

        return packet.getPacket();
    }

    public static Packet newGuildMember(MapleGuildCharacter mgc) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x45);
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
        packet.writeInt(mgc.getJobId());
        packet.writeInt(mgc.getLevel());
        packet.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        packet.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        packet.writeInt(3);
        packet.writeInt(0); //v192
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeLong(PacketProvider.getTime(-2));

        return packet.getPacket();
    }

    public static Packet memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(bExpelled ? 0x4E : 0x4B); 
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeMapleAsciiString(mgc.getName());

        return packet.getPacket();
    }

    public static Packet changeRank(MapleGuildCharacter mgc) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x60); //1.2.251+ (+1)
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.write(mgc.getGuildRank());

        return packet.getPacket();
    }

    public static Packet guildNotice(int gid, String notice) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x64); //1.2.251+ (+1) 
        packet.writeInt(gid);
        packet.writeMapleAsciiString(notice);

        return packet.getPacket();
    }

    public static Packet guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x5E); //1.2.251+ (+1)
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeInt(mgc.getLevel());
        packet.writeInt(mgc.getJobId());

        return packet.getPacket();
    }

    public static Packet rankTitleChange(int gid, String[] ranks) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x60); //1.2.251+ (+1) 
        packet.writeInt(gid);
        for (String r : ranks) {
            packet.writeMapleAsciiString(r);
        }
        return packet.getPacket();
    }

    public static Packet guildDisband(int gid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x52); //1.2.251+ (+1)
        packet.writeInt(gid);

        return packet.getPacket();
    }

    public static Packet guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x65); //1.2.251+ (+1)
        packet.writeInt(gid);
        packet.writeShort(bg);
        packet.write(bgcolor);
        packet.writeShort(logo);
        packet.write(logocolor);
        
        return packet.getPacket();
    }

    public static Packet guildCapacityChange(int gid, int capacity) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x5C); //1.2.251+ (+1) Tests.
        packet.writeInt(gid);
        packet.write(capacity);

        return packet.getPacket();
    }

    public static Packet removeGuildFromAlliance(MapleAlliance alliance, MapleGuild expelledGuild, boolean expelled) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x10);
        addAllianceInfo(packet, alliance);
        packet.writeInt(expelledGuild.getId());
        packet.writeMapleAsciiString(expelledGuild.getName());
        for (int a = 1; a <= 5; a++) {
            packet.writeMapleAsciiString(expelledGuild.getRankTitle(a));
        }
        expelledGuild.addMemberData(packet);

        packet.writeInt(expelledGuild.getCapacity());
        packet.writeShort(expelledGuild.getLogoBG());
        packet.write(expelledGuild.getLogoBGColor());
        packet.writeShort(expelledGuild.getLogo());
        packet.write(expelledGuild.getLogoColor());
        packet.writeMapleAsciiString(expelledGuild.getNotice());
        packet.writeInt(expelledGuild.getGP());
        packet.writeInt(expelledGuild.getGP());
        packet.writeInt(expelledGuild.getAllianceId());
        packet.write(6); //��巹��
        packet.writeShort(0); // guild rank 
        packet.writeShort(expelledGuild.getSkills().size());
        for (GuildSkills sk : expelledGuild.getSkills()) {
            packet.writeInt(sk.skillID);
            packet.writeShort(sk.level);
            packet.writeLong(PacketProvider.getTime(sk.timestamp));
            packet.writeMapleAsciiString(sk.purchaser);
            packet.writeMapleAsciiString(sk.activator);
        }
        packet.write(expelled ? 1 : 0); //1 = expelled, 0 = left
        return packet.getPacket();
    }

    public static Packet disbandAlliance(int alliance) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x1D);
        packet.writeInt(alliance);

        return packet.getPacket();
    }

    public static Packet addGuildToAlliance(MapleAlliance alliance, MapleGuild newGuild) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x12);
        addAllianceInfo(packet, alliance);
        packet.writeInt(newGuild.getId()); //???
        packet.writeInt(newGuild.getId());
        packet.writeMapleAsciiString(newGuild.getName());
        for (int a = 1; a <= 5; a++) {
            packet.writeMapleAsciiString(newGuild.getRankTitle(a));
        }
        newGuild.addMemberData(packet);

        packet.writeInt(newGuild.getCapacity());
        packet.writeShort(newGuild.getLogoBG());
        packet.write(newGuild.getLogoBGColor());
        packet.writeShort(newGuild.getLogo());
        packet.write(newGuild.getLogoColor());
        packet.writeMapleAsciiString(newGuild.getNotice());
        packet.writeInt(newGuild.getGP());
        packet.writeInt(newGuild.getGP());
        packet.writeInt(newGuild.getAllianceId());
        packet.write(6); //��巹��
        packet.writeShort(0); // guild rank 
        packet.writeShort(newGuild.getSkills().size());
        for (GuildSkills sk : newGuild.getSkills()) {
            packet.writeInt(sk.skillID);
            packet.writeShort(sk.level);
            packet.writeLong(PacketProvider.getTime(sk.timestamp));
            packet.writeMapleAsciiString(sk.purchaser);
            packet.writeMapleAsciiString(sk.activator);
        }
        packet.write(0); //???
        return packet.getPacket();
    }

    private static void addAllianceInfo(WritingPacket packet, MapleAlliance alliance) {
        packet.writeInt(alliance.getId());
        packet.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            packet.writeMapleAsciiString(alliance.getRank(i));
        }

        packet.write(alliance.getNoGuilds());
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            packet.writeInt(alliance.getGuildId(i));
        }
        packet.writeInt(alliance.getCapacity()); // ????
        packet.writeMapleAsciiString(alliance.getNotice());
    }

    public static Packet getAllianceInfo(MapleAlliance alliance) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0C);
        packet.write(alliance == null ? 0 : 1); //in an alliance
        if (alliance != null) {
            addAllianceInfo(packet, alliance);
        }
        return packet.getPacket();
    }

    public static Packet changeAlliance(MapleAlliance alliance, final boolean in) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x01);
        packet.write(in ? 1 : 0);
        packet.writeInt(in ? alliance.getId() : 0);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < noGuilds; i++) {
            g[i] = ChannelServer.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return resetActions();
            }
        }
        packet.write(noGuilds);
        for (int i = 0; i < noGuilds; i++) {
            packet.writeInt(g[i].getId());
            //must be world
            Collection<MapleGuildCharacter> members = g[i].getMembers();
            packet.writeInt(members.size());
            for (MapleGuildCharacter mgc : members) {
                packet.writeInt(mgc.getId());
                packet.write(in ? mgc.getAllianceRank() : 0);
            }
        }
        return packet.getPacket();
    }

    public static Packet changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x02);
        packet.writeInt(allianceid);
        packet.writeInt(oldLeader);
        packet.writeInt(newLeader);
        return packet.getPacket();
    }

    public static Packet allianceMemberOnline(int alliance, int gid, int id, boolean online) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0E);
        packet.writeInt(alliance);
        packet.writeInt(gid);
        packet.writeInt(id);
        packet.write(online ? 1 : 0);

        return packet.getPacket();
    }

    public static Packet updateAlliance(MapleGuildCharacter mgc, int allianceid) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x18);
        packet.writeInt(allianceid);
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeInt(mgc.getLevel());
        packet.writeInt(mgc.getJobId());

        return packet.getPacket();
    }

    public static Packet updateAllianceRank(int allianceid, MapleGuildCharacter mgc) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x1B);
        packet.writeInt(allianceid);
        packet.writeInt(mgc.getId());
        packet.writeInt(mgc.getAllianceRank());

        return packet.getPacket();
    }

    public static Packet updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x19);
        packet.writeInt(allianceid);
        packet.writeInt(oldLeader);
        packet.writeInt(newLeader);
        return packet.getPacket();
    }

    public static Packet sendAllianceInvite(String allianceName, MapleCharacter inviter) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x03);
        packet.writeInt(inviter.getGuildId());
        packet.writeMapleAsciiString(inviter.getName());
        //alliance invite did NOT change
        packet.writeMapleAsciiString(allianceName);
        return packet.getPacket();
    }

    public static Packet changeGuildInAlliance(MapleAlliance alliance, MapleGuild guild, final boolean add) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x04);
        packet.writeInt(add ? alliance.getId() : 0);
        packet.writeInt(guild.getId());
        Collection<MapleGuildCharacter> members = guild.getMembers();
        packet.writeInt(members.size());
        for (MapleGuildCharacter mgc : members) {
            packet.writeInt(mgc.getId());
            packet.write(add ? mgc.getAllianceRank() : 0);
        }
        return packet.getPacket();
    }

    public static Packet changeAllianceRank(int allianceid, MapleGuildCharacter player) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x05);
        packet.writeInt(allianceid);
        packet.writeInt(player.getId());
        packet.writeInt(player.getAllianceRank());
        return packet.getPacket();
    }

    public static Packet createGuildAlliance(MapleAlliance alliance) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0F);
        addAllianceInfo(packet, alliance);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = ChannelServer.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return resetActions();
            }
        }
        for (MapleGuild gg : g) {
            if (gg != null) {
                packet.writeInt(gg.getId());
                packet.writeMapleAsciiString(gg.getName());
                for (int a = 1; a <= 5; a++) {
                    packet.writeMapleAsciiString(gg.getRankTitle(a));
                }
                gg.addMemberData(packet);

                packet.writeInt(gg.getCapacity());
                packet.writeShort(gg.getLogoBG());
                packet.write(gg.getLogoBGColor());
                packet.writeShort(gg.getLogo());
                packet.write(gg.getLogoColor());
                packet.writeMapleAsciiString(gg.getNotice());
                packet.writeInt(gg.getGP());
                packet.writeInt(gg.getGP());
                packet.writeInt(gg.getAllianceId());
                packet.write(6); //��巹��
                packet.writeShort(0); // guild rank 
                packet.writeShort(gg.getSkills().size());
                for (GuildSkills sk : gg.getSkills()) {
                    packet.writeInt(sk.skillID);
                    packet.writeShort(sk.level);
                    packet.writeLong(PacketProvider.getTime(sk.timestamp));
                    packet.writeMapleAsciiString(sk.purchaser);
                    packet.writeMapleAsciiString(sk.activator);
                }
            }
        }
        return packet.getPacket();
    }

    public static Packet getAllianceUpdate(MapleAlliance alliance) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x17);
        addAllianceInfo(packet, alliance);
        return packet.getPacket();
    }

    public static Packet getGuildAlliance(MapleAlliance alliance) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0D);

        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = ChannelServer.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return resetActions();
            }
        }
        packet.writeInt(noGuilds);//ammount of guilds joined
        for (MapleGuild gg : g) {
            if (gg != null) {
                packet.writeInt(gg.getId());
                packet.writeMapleAsciiString(gg.getName());
                for (int a = 1; a <= 5; a++) {
                    packet.writeMapleAsciiString(gg.getRankTitle(a));
                }
                gg.addMemberData(packet);

                packet.writeInt(gg.getCapacity());
                packet.writeShort(gg.getLogoBG());
                packet.write(gg.getLogoBGColor());
                packet.writeShort(gg.getLogo());
                packet.write(gg.getLogoColor());
                packet.writeMapleAsciiString(gg.getNotice());
                packet.writeInt(gg.getGP());
                packet.writeInt(gg.getGP());
                packet.writeInt(gg.getAllianceId());
                packet.write(6); //��巹��
                packet.writeShort(0); // guild rank 
                packet.writeShort(gg.getSkills().size());
                for (GuildSkills sk : gg.getSkills()) {
                    packet.writeInt(sk.skillID);
                    packet.writeShort(sk.level);
                    packet.writeLong(PacketProvider.getTime(sk.timestamp));
                    packet.writeMapleAsciiString(sk.purchaser);
                    packet.writeMapleAsciiString(sk.activator);
                }
            }
        }
        return packet.getPacket();
    }

    public static Packet BBSThreadList(ResultSet rs, int start) throws SQLException {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        packet.write(6);

        int threadCount = rs.getRow();

        if (!rs.last()) {
            packet.write(0);
        } else if (rs.getInt("localthreadid") == 0) { //has a notice
            packet.write(1);
            addThread(packet, rs);
            threadCount--; //one thread didn't count (because it's a notice)
        } else {
            packet.write(0);
        }
        if (!rs.absolute(start + 1)) { //seek to the thread before where we start
            rs.first(); //uh, we're trying to start at a place past possible
            start = 0;
        }
        packet.writeInt(threadCount);
        packet.writeInt(Math.min(10, threadCount - start));

        for (int i = 0; i < Math.min(10, threadCount - start); i++) {
            addThread(packet, rs);
            rs.next();
        }
        return packet.getPacket();
    }

    private static void addThread(WritingPacket packet, ResultSet rs) throws SQLException {
        packet.writeInt(rs.getInt("localthreadid"));
        packet.writeInt(rs.getInt("postercid"));
        packet.writeMapleAsciiString(rs.getString("name"));
        packet.writeLong(PacketProvider.getKoreanTimestamp(rs.getLong("timestamp")));
        packet.writeInt(rs.getInt("icon"));
        packet.writeInt(rs.getInt("replycount"));
    }

    public static Packet showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        packet.write(7);

        packet.writeInt(localthreadid);
        packet.writeInt(threadRS.getInt("postercid"));
        packet.writeLong(PacketProvider.getKoreanTimestamp(threadRS.getLong("timestamp")));
        packet.writeMapleAsciiString(threadRS.getString("name"));
        packet.writeMapleAsciiString(threadRS.getString("startpost"));
        packet.writeInt(threadRS.getInt("icon"));

        if (repliesRS != null) {
            int replyCount = threadRS.getInt("replycount");
            packet.writeInt(replyCount);

            int i;
            for (i = 0; i < replyCount && repliesRS.next(); i++) {
                packet.writeInt(repliesRS.getInt("replyid"));
                packet.writeInt(repliesRS.getInt("postercid"));
                packet.writeLong(PacketProvider.getKoreanTimestamp(repliesRS.getLong("timestamp")));
                packet.writeMapleAsciiString(repliesRS.getString("content"));
            }
            if (i != replyCount || repliesRS.next()) {
                //in the unlikely event that we lost count of replyid
                throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
                //we need to fix the database and stop the packet sending
                //or else it'll probably error 38 whoever tries to read it
                //there is ONE case not checked, and that's when the thread
                //has a replycount of 0 and there is one or more replies to the
                //thread in bbs_replies
            }
        } else {
            packet.writeInt(0); //0 replies
        }
        return packet.getPacket();
    }

    public static Packet showGuildRanks(int npcid, List<GuildRankingInfo> all) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x49);
        packet.writeInt(npcid);
        packet.writeInt(all.size());

        for (GuildRankingInfo info : all) {
            packet.writeMapleAsciiString(info.getName());
            packet.writeInt(info.getGP());
            packet.writeInt(info.getLogo());
            packet.writeInt(info.getLogoBg());
            packet.writeInt(info.getLogoBgColor());
            packet.writeInt(info.getLogoColor());
        }

        return packet.getPacket();
    }

    public static Packet updateGP(int gid, int GP) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x48);
        packet.writeInt(gid);
        packet.writeInt(GP);

        return packet.getPacket();
    }

    public static Packet skillEffect(MapleCharacter from, SkillEffectEntry entry, final Point pos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        packet.writeInt(from.getId());
        packet.writeInt(entry.getSkillId());
        packet.write(entry.getLevel());
        packet.write(entry.getFlags());
        packet.write(entry.getSpeed());
        packet.write(entry.getUnk()); // Direction ??
        if (entry.getSkillId() == 13111020) {
            packet.writePos(pos); // Position
        }
        return packet.getPacket();
    }

    public static Packet skillCancel(MapleCharacter from, int skillId) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        packet.writeInt(from.getId());
        packet.writeInt(skillId);

        return packet.getPacket();
    }

    public static Packet showMagnet(int mobid, byte success) { // Monster Magnet
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        packet.writeInt(mobid);
        packet.write(success);

        return packet.getPacket();
    }

    public static Packet sendHint(String hint, int width, int height) {
        WritingPacket packet = new WritingPacket();
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        packet.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        packet.writeMapleAsciiString(hint);
        packet.writeShort(width);
        packet.writeShort(height);
        packet.write(1);

        return packet.getPacket();
    }

    public static Packet messengerInvite(String from, int messengerid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x03);
        packet.writeMapleAsciiString(from);
        packet.write(0x10);
        packet.writeInt(messengerid);
        packet.write(0x00);

        return packet.getPacket();
    }

    public static Packet addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0);
        packet.write(position);
        PacketProvider.addPlayerLooks(packet, chr, true);
        packet.writeMapleAsciiString(from);
        packet.write(channel);
        packet.write(position);
        packet.writeShort(chr.getJob());
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static Packet removeMessengerPlayer(int position) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x02);
        packet.write(position);

        return packet.getPacket();
    }

    public static Packet updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x08);
        packet.write(1);
        packet.write(position);
        PacketProvider.addPlayerLooks(packet, chr, true);
        packet.writeMapleAsciiString(from);
        packet.write(channel);
        packet.write(0x00);

        return packet.getPacket();
    }

    public static Packet joinMessenger(int position) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x01);
        packet.write(position);

        return packet.getPacket();
    }

    public static Packet messengerChat(String from, String text) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x06);
        packet.writeMapleAsciiString(from);
        packet.writeMapleAsciiString(text);
        
        return packet.getPacket();
    }

    public static Packet messengerNote(String text, int mode, int mode2) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(mode);
        packet.writeMapleAsciiString(text);
        packet.write(mode2);

        return packet.getPacket();
    }

    public static Packet showEquipEffect(byte team) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        packet.writeShort(team);
        
        return packet.getPacket();
    }

    public static Packet summonSkill(int cid, int summonSkillId, int newStance) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        packet.writeInt(cid);
        packet.writeInt(summonSkillId);
        packet.write(newStance);

        return packet.getPacket();
    }

    public static Packet skillCooldown(int sid, int time) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        packet.writeInt(1); //1.2.239+, Activated.
        packet.writeInt(sid);
        packet.writeInt(time);
        
        return packet.getPacket();
    }

    public static Packet useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        WritingPacket packet = new WritingPacket();

        packet.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        packet.writeInt(chr.getId());
        packet.write(1);
        packet.writeInt(skillid);
        packet.writeInt(maxlevel);
        packet.write(canuse ? 1 : 0);
        packet.write(success ? 1 : 0);

        return packet.getPacket();
    }

    public static Packet getMacros(SkillMacro[] macros) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        packet.write(count); // number of macros
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                packet.writeMapleAsciiString(macro.getName());
                packet.write(macro.getShout());
                packet.writeInt(macro.getSkill1());
                packet.writeInt(macro.getSkill2());
                packet.writeInt(macro.getSkill3());
            }
        }
        return packet.getPacket();
    }

    public static Packet catchMonster(int mobid, byte success) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
        packet.writeInt(mobid);
        packet.write(success);
        packet.write(success);
        
        return packet.getPacket();
    }

    public static Packet giveSunfireBuff(List<Triple<BuffStats, Integer, Boolean>> statups, int gauge, int bufflength) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        for (Triple<BuffStats, Integer, Boolean> statup : statups) {
            if (!statup.getThird()) {
                packet.writeShort(statup.getSecond().shortValue());
                packet.writeInt(20040216);
                packet.writeInt(bufflength);
            }
        }
        addSunfireGaugeInfo(packet, gauge, bufflength);
        return packet.getPacket();
    }

    public static Packet giveEclipseBuff(List<Triple<BuffStats, Integer, Boolean>> statups, int gauge, int bufflength) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeBuffMask(packet, statups);
        for (Triple<BuffStats, Integer, Boolean> statup : statups) {
            if (!statup.getThird()) {
                packet.writeShort(statup.getSecond().shortValue());
                packet.writeInt(20040217);
                packet.writeInt(bufflength);
            }
        }
        addEclipseGaugeInfo(packet, gauge, bufflength);
        return packet.getPacket();
    }

    public static Packet giveEquilibriumBuff(int equilibrium, int skill_1, int skill_2) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.LUMINOUS_GAUGE);
        addEquilibriumGaugeInfo(packet, equilibrium, skill_1, skill_2);
        
        return packet.getPacket();
    }

    private static void addSunfireGaugeInfo(WritingPacket packet, int gauge, int bufflength) {
        packet.write0(5);
        packet.writeInt(20040216); 
        packet.writeInt(bufflength);
        packet.writeLong(0);
        packet.writeInt(Math.min(gauge, 10000));
        packet.writeInt(-1);
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(0);
    }

    private static void addEclipseGaugeInfo(WritingPacket packet, int gauge, int bufflength) {
        packet.write0(5);
        packet.writeInt(20040217);
        packet.writeInt(bufflength);
        packet.writeLong(0);
        packet.writeInt(Math.max(gauge, -1));
        packet.writeInt(10000);
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(0);
    }

    private static void addEquilibriumGaugeInfo(final WritingPacket packet, int equilibrium, int skill_1, int skill_2) {
        packet.writeShort(2);
        packet.writeInt(equilibrium);
        packet.writeInt(10000);
        packet.write0(5);
        packet.writeInt(skill_1);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(skill_2);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(1); //1.2.251+
        packet.writeInt(-1);
        packet.writeInt(0);
        packet.writeInt(600); 
        packet.write(1);
        packet.writeInt(0);
    }

    public static Packet checkSunfireSkill(int gauge) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.LUMINOUS_MORPH.getValue());
        packet.writeInt(Math.min(gauge, 9999));
        packet.write(gauge <= 1 ? 1 : 2);
        
        return packet.getPacket();
    }

    public static Packet checkEclipseSkill(int gauge) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.LUMINOUS_MORPH.getValue());
        packet.writeInt(Math.min(gauge, 9999));
        packet.write(gauge >= 9999 ? 2 : 1);
        
        return packet.getPacket();
    }

    public static Packet spawnDragon(MapleDragon d) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
        packet.writeInt(d.getOwner());
        packet.writeInt(d.getPosition().x);
        packet.writeInt(d.getPosition().y);
        packet.write(d.getStance());
        packet.writeShort(0);
        packet.writeShort(d.getJobId());
        
        return packet.getPacket();
    }

    public static Packet removeDragon(int chrid) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
        packet.writeInt(chrid);
        
        return packet.getPacket();
    }

    public static Packet moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue()); //not sure
        packet.writeInt(d.getOwner());
        packet.writeInt(0); //192+
        packet.writePos(startPos);
        packet.writeInt(0);
        PacketProvider.serializeMovementList(packet, moves);
        
        return packet.getPacket();
    }

    public static Packet updateEquipSlot(IItem item) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(2);
        packet.write(0);
        packet.write(3);
        packet.write(1);
        packet.writeShort(item.getPosition());
        packet.write(0);
        packet.write(item.getType());
        packet.writeShort(item.getPosition());
        PacketProvider.addItemInfo(packet, item, true, true, null);
        if (item.getPosition() < 0) {
            packet.write(2);
        }
        return packet.getPacket();
    }
    
    public static Packet updateStarForceEquipSlot(IItem item) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(1);
        packet.write(2);
        packet.write(0);
        packet.write(3);
        packet.write(1);
        packet.writeShort(item.getPosition());
        packet.write(0);
        packet.write(item.getType());
        packet.writeShort(item.getPosition());
        PacketProvider.addItemInfo(packet, item, true, true, null);
        packet.write(4);

        return packet.getPacket();
    }

    public static Packet showMagnifyingEffect(int id, int slot) {
        WritingPacket packet = new WritingPacket(8);
        packet.writeShort(SendPacketOpcode.SHOW_MAGNIFYING_EFFECT.getValue());
        packet.writeInt(id);
        packet.writeShort(slot);
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet showCubeEffect(int id, int cubeid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_POTENTIAL_EFFECT.getValue());
        packet.writeInt(id);
        packet.write(1);
        packet.writeInt(cubeid);
        
        return packet.getPacket();
    }

    public static Packet showStampEffect(int id, int stampid, boolean success) {
        WritingPacket mppacket = new WritingPacket();
        mppacket.writeShort(SendPacketOpcode.SHOW_POTENTIAL_EFFECT.getValue());
        mppacket.writeInt(id);
        mppacket.write(success ? 1 : 0);
        mppacket.writeInt(stampid);
        
        return mppacket.getPacket();
    }

    public static Packet getQuickSlot(List<Pair<Integer, Integer>> quickslots) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
        packet.write(1);
        Collections.sort(quickslots, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
                int val1index = p1.getLeft();
                int val2index = p2.getLeft();
                if (val1index > val2index) {
                    return 1;
                } else if (val1index == val2index) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        for (Pair<Integer, Integer> p : quickslots) {
            packet.writeInt(p.getRight());
        }
        return packet.getPacket();
    }

    public static Packet absorbingDF(int oid, int count, int color, boolean isZeroWP, MapleCharacter chr, Point pos) { 
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(1);
        packet.writeInt(chr.getId());
        packet.writeInt(oid);
        packet.writeInt(isZeroWP ? 0x09 : 0);
        packet.write(1);
        packet.writeInt(count);
        packet.writeInt(color);
        packet.writeInt(isZeroWP ? 0x2B : 0x2E);
        packet.writeInt(0x06);
        packet.writeInt(isZeroWP ? 0x2D : 0x1F);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0);
        packet.write(0);
        if (isZeroWP) {
            packet.writeInt((int) pos.getX());
            packet.writeInt((int) pos.getY());
            packet.writeInt((int) pos.getX());
            packet.writeInt((int) pos.getY());
        }
        return packet.getPacket();
    }

    public static Packet absorbingCardStack(int cid, int room, int skillid, boolean judgement, int x) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(1);
        packet.writeInt(room);
        packet.write(0);
        packet.writeInt(skillid);
        for (int i = 0; i < x; i++) {
            packet.write(1);
            packet.writeInt(Randomizer.rand(1, 10));
            packet.writeInt(Randomizer.rand(1, 3));
            packet.writeInt(Randomizer.rand(1, 28));
            packet.writeInt(Randomizer.rand(1, 10));
            packet.writeInt(Randomizer.rand(0, 8));
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(Randomizer.nextInt()); 
            packet.writeInt(0);
            packet.writeInt(0); //1.2.252+
        }
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet cardAmount(int amount) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PHANTOM_CARD.getValue());
        packet.write(amount);
        
        return packet.getPacket();
    }

    public static Packet showRandBuffEffect(int cid, int skillid, int effectid, int level, boolean broadcast, boolean doubledice) {
        WritingPacket packet = new WritingPacket();
        if (broadcast) {
            packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
            packet.writeInt(cid);
        } else {
            packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        }
        packet.write(5); //1.2.239+, (+1)
        packet.writeInt(effectid);
        packet.writeInt(-1);
        packet.writeInt(skillid);
        packet.write(level);
        packet.write(doubledice ? 1 : 0);

        return packet.getPacket();
    }

    public static Packet spawnNPCTemp(int value1, int value2) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_TEMP_SUMMON.getValue());
        packet.writeShort(value1);
        packet.writeShort(value2);
        packet.writeMapleAsciiString("summon");
        packet.write0(5);
        
        return packet.getPacket();
    }

    /**
     * Makes any NPC in the game scriptable.
     *
     * @param npcId - The NPC's ID, found in WZ files/MCDBdjdd
     * @param description - If the NPC has quests, this will be the text of the
     * menu item
     * @return
     */
    
    public static Packet setNPCScriptable(List<Pair<Integer, String>> npcs) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
        packet.write(npcs.size());
        for (Pair<Integer, String> s : npcs) {
            packet.writeInt(s.left);
            packet.writeMapleAsciiString(s.right);
            packet.writeInt(0); // start time
            packet.writeInt(Integer.MAX_VALUE); // end time
        }
        return packet.getPacket();
    }

    public static Packet showItemLevelupEffect() {
        return showSpecialEffect(0x16); //1.2.251+, (+1)
    }

    public static Packet showForeignItemLevelupEffect(int cid) {
        return showSpecialEffect(cid, 0x16); //1.2.251+, (+1)
    }

    public static Packet itemCooldown(int itemid, int uniqueid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ITEM_COOLDOWN.getValue());
        packet.write(0);
        packet.writeInt(itemid);
        packet.writeLong(uniqueid);
        
        return packet.getPacket();
    }

    public static Packet showAngelicBlessEffect(int cid, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(3);
        packet.writeInt(skillid);
        packet.write(1);
        
        return packet.getPacket();
    }

    public static Packet showHeadTitle(int cid, int title) {  
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.HEAD_TITLE.getValue());
        packet.writeInt(cid);
        packet.writeInt(title);
        
        return packet.getPacket();
    }

    public static Packet updateJaguar(MapleCharacter hp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.JAGUAR_UPDATE.getValue());
        PacketProvider.addWildHunterInfo(packet, hp);

        return packet.getPacket();
    }

    public static Packet startGathering(int oid, int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.START_GATHER.getValue());
        packet.writeInt(oid);
        packet.writeInt(value);
        
        return packet.getPacket();
    }

    public static Packet showGathering(int cid, int tool) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REACTOR_HIT.getValue()); 
        packet.writeInt(cid);
        if (tool > 0) {
            packet.write(1);
            packet.writeInt(tool);
            packet.writeInt(0);
        } else {
            packet.write(0);
        }
        return packet.getPacket();
    }

    public static Packet showGatherComplete(int cid, boolean success) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_GATHER_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(success ? 1 : 0);
        
        return packet.getPacket();
    }

    public static Packet getProfessionInfo(String skillid, int skill1, int skill2, int rate) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PROFESSION_INFO.getValue());
        packet.writeMapleAsciiString(skillid);
        packet.writeInt(skill1);
        packet.writeInt(skill2);
        packet.write(1);
        packet.writeInt(rate);
        
        return packet.getPacket();
    }

    public static Packet showProfessionMakeEffect(int cid, String effect, int time, int value) {
        WritingPacket packet = new WritingPacket();
        if (cid != -1) {
            packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
            packet.writeInt(cid);
        } else { 
            packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        }
        packet.write(0x24); //1.2.251+ (+1)
        packet.writeMapleAsciiString(effect);
        packet.write(1);
        packet.writeInt(time);
        packet.writeInt(value);
        
        return packet.getPacket();
    }

    public static Packet showProfessionMakeSomething(int cid, int thing, int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_MAKE_EFFECT.getValue());
        packet.writeInt(cid);
        packet.writeInt(thing);
        packet.writeInt(value);
        
        return packet.getPacket();
    }

    public static Packet showProfessionMakeResult(int cid, int skillid, int grade, int itemid, int quantity, int exp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_MAKE_RESULT.getValue());
        packet.writeInt(cid);
        packet.writeInt(skillid);
        packet.writeInt(grade);
        packet.writeInt(itemid);
        packet.writeInt(quantity);
        packet.writeInt(exp);
        
        return packet.getPacket();
    }

    public static Packet makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
        packet.writeInt(cid);
        packet.writeMapleAsciiString(cname);
        packet.writeInt(pos.x);
        packet.writeInt(pos.y);
        packet.writeShort(timeLeft); //fh or time left, dunno
        packet.writeInt(itemId); //3049000, 3049001...
        packet.writeInt(fee);
        
        return packet.getPacket();
    }

    public static Packet removeExtractor(int cid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
        packet.writeInt(cid);
        packet.writeInt(1); //probably 1 = animation, 2 = make something?
        
        return packet.getPacket();
    }

    public static Packet showOwnRecoverHP(long hp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(0x23); //1.2.251+, (+1)
        packet.writeInt(hp);
        
        return packet.getPacket();
    }

    public static Packet inviteExpedition(MapleExpeditionType exptype, MapleCharacter chr) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x57); //v192
        wh.writeInt(chr.getLevel());
        wh.writeInt(chr.getJob());
        wh.writeInt(0);
        wh.writeMapleAsciiString(chr.getName());
        wh.writeInt(exptype.code);
        
        return wh.getPacket();
    }

    public static Packet updateExpedition(boolean silent, MapleExpedition exp) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(silent ? 0x4A : 0x4C); //v192
        wh.writeInt(exp.getType().code);
        wh.writeInt(0);
        for (int i = 0; i <= 4; i++) {
            if (exp.isContained(i)) {
                addPartyStatus(0, exp.getParty(i), wh, false, true);
            } else {
                wh.write0(226);
            }
        }
        return wh.getPacket();
    }

    public static Packet partymoveExpedition(MapleParty party, int to, int channel) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x55);
        wh.writeInt(0);
        wh.writeInt(to);
        addPartyStatus(channel, party, wh, false, true);
        return wh.getPacket();
    }

    public static Packet changeLeaderExpedition(int index) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x54);
        wh.writeInt(index);
        return wh.getPacket();
    }

    public static Packet expelExpedition(String name) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x51);
        wh.writeMapleAsciiString(name);
        return wh.getPacket();
    }

    public static Packet expeledExpedition() {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x52);
        return wh.getPacket();
    }

    public static Packet disbandExpedition() {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x53);
        return wh.getPacket();
    }

    public static Packet leavedExpedition() {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x50);
        return wh.getPacket();
    }

    public static Packet leaveExpedition(String name) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
        wh.write(0x4F);
        wh.writeMapleAsciiString(name);
        
        return wh.getPacket();
    }

    public static Packet openBag(int index, int itemId, boolean firstTime) {
        WritingPacket wh = new WritingPacket();
        wh.writeShort(SendPacketOpcode.OPEN_BAG.getValue());
        wh.writeInt(index);
        wh.writeInt(itemId);
        wh.writeShort(firstTime ? 1 : 0); //this might actually be 2 bytes
        
        return wh.getPacket();
    }

    public static Packet addMoruItem(IItem item) { 
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(0); // could be from drop
        packet.write(2); // always 2
        packet.write(0);
        packet.write(3); // quantity > 0 (?)
        packet.write(1); // Inventory type
        packet.writeShort(item.getPosition()); // item slot
        packet.write(0);
        packet.write(1);
        packet.writeShort(item.getPosition()); // wtf repeat
        PacketProvider.addItemInfo(packet, item, true, true, null);
        
        return packet.getPacket();
    }

    public static Packet showMagneticConnect(int cid, List<MapleMapObject> objs) {
        WritingPacket w = new WritingPacket();
        w.writeShort(SendPacketOpcode.SHOW_MAGNETIC_EFFECT.getValue());
        w.writeInt(cid);
        for (MapleMapObject o : objs) {
            w.writeInt(o.getObjectId());
        }
        return w.getPacket();
    }

    public static Packet showItempotAction(MapleItempot mip, boolean show) {
        WritingPacket w = new WritingPacket();
        w.writeShort(SendPacketOpcode.ITEMPOT_ACTION.getValue());
        w.write(show ? 0 : 1);
        w.writeInt(mip.getSlot());
        w.writeInt(show ? 2 : 1);
        if (show) {
            w.writeInt(mip.getLifeId());
            w.write(mip.getLevel());
            w.write(mip.getStatus());
            w.writeInt(mip.getFullness());
            w.writeInt(mip.getCloseness());
            w.writeInt(mip.getIncCloseLeft());
            w.writeInt(10);
            w.writeInt(1);
            w.write(0);
            w.writeLong(PacketProvider.getKoreanTimestamp(mip.getLastFeedTime()));
            w.writeLong(PacketProvider.getKoreanTimestamp(mip.getSleepTime()));
            w.writeLong(PacketProvider.getKoreanTimestamp(mip.getStartTime()));
            w.writeLong(PacketProvider.getKoreanTimestamp(System.currentTimeMillis()));
            w.writeInt(mip.getMaxClose());
            w.writeInt(mip.getMaxFull());
            w.writeInt(mip.getIncClose() + 1);
            w.writeInt(mip.getFeedInterval());
            w.write(10);
            w.writeInt(0);
        }
        return w.getPacket();
    }

    public static Packet giveLifeTidal(boolean isHpBetter, int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.LIFE_TIDAL);
        packet.writeShort(isHpBetter ? 2 : 1);
        packet.writeInt(27110007);
        packet.writeInt(2100000000);
        packet.write0(5);
        packet.writeInt(value);
        packet.write0(14); 
        
        return packet.getPacket();
    }

    public static Packet giveDemonWatk(long hp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.LIFE_TIDAL);
        packet.writeShort(3);
        packet.writeInt(0);
        packet.writeInt(2100000000);
        packet.write0(5);
        packet.writeInt(hp);
        packet.write0(13); 
        
        return packet.getPacket();
    }
    
    public static Packet FireBlink(int cid, Point pos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        packet.write(0); 
        packet.write(3); 
        packet.writeInt(cid);
        packet.writePos(pos);
        
        return packet.getPacket();
    }

    public static Packet OrbitalFlame(int cid, int skillid, int effect, int direction, int range) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        /* ����� */
        packet.writeInt(0x11);
        /* ��ų�� ���� Ÿ�� ���� */   
        packet.write(1);
        packet.writeInt(0);
        packet.writeInt(skillid);
        packet.write(1);
        packet.writeInt(2);
        packet.writeInt(effect); //effect
        packet.writeInt(17);
        packet.writeInt(17);
        packet.writeInt(90);
        packet.write0(12);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(8);
        packet.writeInt(0); //1.2.252+
        packet.write(0);
        packet.writeInt(direction);
        packet.writeInt(range); 
        
        return packet.getPacket();
    }
    
    public static Packet giveMagicArrow(MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(chr.getId());
        packet.writeInt(0x0A);
        packet.write(1);
        packet.writeInt(0);
        packet.writeInt(3100010);
        packet.write(1);
        packet.writeInt(10 - chr.quivercount[2]);
        packet.writeInt(0);
        packet.writeInt(Randomizer.rand(0x0A, 0x14));
        packet.writeInt(Randomizer.rand(0x05, 0x0A));
        packet.writeInt(Randomizer.rand(0x04, 0x12D));
        packet.writeInt(Randomizer.rand(0x14, 0x30));
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0); //1.2.252+
        packet.write(0);
        
        return packet.getPacket();
    } 

    public static Packet giveMesoExplosion(int cid, List<Integer> mobs, List<Pair<Integer, Point>> mesos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(0x0C);
        packet.write(1);
        packet.writeInt(mobs.size());
        for (Integer mob : mobs) {
            packet.writeInt(mob);
        }
        packet.writeInt(4210014);
        for (int i = 0; i < mesos.size(); i++) {
            packet.write(1);
            packet.writeInt(i + 2);
            packet.writeInt(1);
            packet.writeInt(Randomizer.rand(0x2A, 0x2F));
            packet.writeInt(Randomizer.rand(0x03, 0x04));
            packet.writeInt(Randomizer.rand(0x04, 0x132));
            packet.writeInt(700);
            packet.writeInt(mesos.get(i).right.x);
            packet.writeInt(mesos.get(i).right.y);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
            packet.writeInt(0); //1.2.252+
        }
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet giveShadowBat(int cid, int oid, Point pos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(15);
        packet.write(1);
        packet.writeInt(oid);
        packet.writeInt(14000028);
        packet.write(1);
        packet.writeInt(2);
        packet.writeInt(1);
        packet.writeInt(1);
        packet.writeInt(5);
        packet.writeInt(46);
        packet.writeInt(500);
        packet.writeLong(0);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0); //1.2.252+
        packet.write(0);
        /* Position Values */
        packet.writeInt((int) pos.getX());
        packet.writeInt((int) pos.getY());
        packet.writeInt((int) pos.getX());
        packet.writeInt((int) pos.getY());

        return packet.getPacket();
    }
    
    
    public static Packet giveShadowBatBounce(int cid, int oid, Point pos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(1); 
        packet.writeInt(cid);
        packet.writeInt(oid);
        packet.writeInt(0x10);
        packet.write(1);
        packet.write(HexTool.getByteArrayFromHexString("D2 39 C5 00"));
        packet.writeInt(14000029); 
        packet.write(1);
        packet.writeInt(3);
        packet.writeInt(1);
        packet.writeInt(5);
        packet.writeInt(5);
        packet.writeInt(0x25);
        packet.writeInt(3);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0);
        packet.write(0);
        /* Position Values */
        packet.writeInt((int) pos.getX());
        packet.writeInt((int) pos.getY());
        packet.writeInt((int) pos.getX());
        packet.writeInt((int) pos.getY());
        
        return packet.getPacket();
    }

    public static Packet cancelEquilbriam() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.LUMINOUS_GAUGE);
        return packet.getPacket();
    }

    public static Packet pendantSlot(boolean p) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PENDANT_SLOT.getValue());
        packet.write(p ? 1 : 0);
        return packet.getPacket();
    }

    public static Packet CongratulationMusicBox(String name, int itemid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MUSIC_BOX.getValue());
        packet.writeInt(itemid);
        packet.writeMapleAsciiString(name);
        return packet.getPacket();
    }

    public static Packet followRequest(int chrid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_REQUEST.getValue());
        packet.writeInt(chrid);

        return packet.getPacket();
    }

    public static Packet getFollowMsg(int opcode) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
        packet.writeLong(opcode);

        return packet.getPacket();
    }

    public static Packet followEffect(int initiator, int replier, Point toMap) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
        packet.writeInt(initiator);
        packet.writeInt(replier);
        if (replier == 0) {
            packet.write(toMap == null ? 0 : 1);
            if (toMap != null) {
                packet.writeInt(toMap.x);
                packet.writeInt(toMap.y);
            }
        }
        return packet.getPacket();
    }

    public static Packet MegidoFlameRe(int cid, int oid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(3);
        packet.write(1);
        packet.writeInt(1);
        packet.writeInt(oid);
        packet.writeInt(2121055);
        /* ���� ���� ó���� */
        packet.write(1);
        packet.writeInt(2);
        packet.writeInt(2);
        packet.writeInt(Randomizer.rand(10, 17));
        packet.writeInt(Randomizer.rand(10, 16));
        packet.writeInt(Randomizer.rand(40, 52));
        packet.writeInt(20);
        packet.writeInt(0);
        packet.writeInt(0);
        /* ���� ���� ó���� */
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0);
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet ShieldChacingRe(int cid, int unkwoun, int oid, int unkwoun2, int unkwoun3) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(1);
        packet.writeInt(cid);
        packet.writeInt(unkwoun);
        packet.writeInt(4);
        packet.write(1);
        packet.writeInt(oid);
        packet.writeInt(31221014);
        /* ���� ���� ó���� */
        packet.write(1);
        packet.writeInt(unkwoun2 + 1);
        packet.writeInt(3);
        packet.writeInt(unkwoun3);
        packet.writeInt(3);
        packet.writeInt(Randomizer.rand(36, 205));
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        /* ���� ���� ó���� */
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0); //1.2.252+
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet ShieldChacing(int cid, List<Integer> moblist, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(3);
        packet.write(1);
        packet.writeInt(moblist.size());
        for (int i = 0; i < moblist.size(); i++) {
            packet.writeInt((moblist.get(i)).intValue());
        }
        packet.writeInt(skillid);
        for (int i = 1; i <= moblist.size(); i++) {
            packet.write(1);
            packet.writeInt(1 + i);
            packet.writeInt(3);
            packet.writeInt(Randomizer.rand(1, 20));
            packet.writeInt(Randomizer.rand(20, 50));
            packet.writeInt(Randomizer.rand(50, 200));
            packet.writeInt(skillid == 2121055 ? 720 : 660);
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
            packet.writeInt(0); //1.2.252+
        }
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet HomingMisile(int cid, List<Integer> moblist, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(20);
        packet.write(1);
        packet.writeInt(moblist.size());
        for (int i = 0; i < moblist.size(); i++) {
            packet.writeInt(moblist.get(i).intValue());
        }
        packet.writeInt(skillid);
        for (int i = 0; i < moblist.size(); i++) {
            packet.write(1);
            packet.writeInt(i + 2);
            packet.writeInt(skillid == 35101002 ? 1 : 2);
            packet.writeInt(Randomizer.rand(40, 50));
            packet.writeInt(Randomizer.rand(10, 15));
            packet.writeInt(Randomizer.rand(19, 50));
            packet.writeInt(500);
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
            packet.writeInt(0);
        }
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet EazisSystem(int cid, int oid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(5);
        packet.write(1);
        packet.writeInt(oid);
        packet.writeInt(36110004);
        for (int i = 0; i < 3; i++) {
            packet.write(1);
            packet.writeInt(i + 2);
            packet.writeInt(0);
            packet.writeInt(35);
            packet.writeInt(5);
            packet.writeInt(Randomizer.rand(80, 100));
            packet.writeInt(Randomizer.rand(200, 300));
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
            packet.writeInt(0); //1.2.252+
        }
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet getDeathCount(int count) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DEATH_COUNT.getValue());
        packet.writeInt(count);
        
        return packet.getPacket();
    }

    public static Packet combokill(int combo, int monster) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0x25); //1.2.250+ (+2)
        packet.write(1);
        packet.writeInt(combo);
        packet.writeInt(monster);
        
        return packet.getPacket();
    }
    public static Packet multikill(int combo, long exp) { 
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0x25); //1.2.250+ (+2)
        packet.write(0);
        packet.writeLong(exp);
        packet.writeInt(combo);
        
        return packet.getPacket();
    }

    public static Packet PinPointRocket(int cid, List<Integer> moblist) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(6);
        packet.write(1);
        packet.writeInt(moblist.size());
        for (int i = 0; i < moblist.size(); i++) {
            packet.writeInt((moblist.get(i)).intValue());
        }
        packet.writeInt(36001005);
        for (int i = 1; i <= moblist.size(); i++) {
            packet.write(1);
            packet.writeInt(i + 7);
            packet.writeInt(0);
            packet.writeInt(Randomizer.rand(10, 20));
            packet.writeInt(Randomizer.rand(20, 40));
            packet.writeInt(Randomizer.rand(40, 200));
            packet.writeInt(Randomizer.rand(500, 2000));
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
            packet.writeInt(0); //1.2.252+
        }
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet OnOffFlipTheCoin(boolean on) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FLIP_THE_COIN.getValue());
        packet.write(on ? 1 : 0);
        
        return packet.getPacket();
    }

    public static Packet TrifleWorm(int cid, int skillid, int ga, int oid, int gu) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(7);
        packet.write(1);
        packet.writeInt(gu);
        packet.writeInt(oid);
        packet.writeInt(skillid);
        for (int i = 1; i < ga; i++) {
            packet.write(1);
            packet.writeInt(2 + i);
            packet.writeInt(1);
            packet.writeInt(Randomizer.rand(42, 47));
            packet.writeInt(7 + i);
            packet.writeInt(Randomizer.rand(5, 171));
            packet.writeInt(Randomizer.rand(0, 55));
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
            packet.writeInt(0); //1.2.252+
        }
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet CriticalGrowing(int critical) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.CRITICAL_GROWING);
        packet.writeShort(critical);
        packet.writeInt(4220015);
        packet.write0(22);

        return packet.getPacket();
    }

    public static Packet KillingPoint(int count) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.KILLING_POINT);
        packet.write0(5);
        packet.writeInt(count);
        packet.write0(10);
                
        return packet.getPacket();
    }

    public static Packet QuiverKartrige(int count, int mode) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.QUIVER_KARTRIGE);
        packet.writeInt(count);
        packet.writeInt(3101009);
        packet.write0(9);
        packet.writeInt(mode);
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    public static Packet ZeroWP(int wp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0x23); //1.2.250+ (+2)
        packet.writeInt(wp);
        
        return packet.getPacket();
    }

    public static Packet ZeroUpdate(MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ZERO_UPDATE.getValue());
        packet.writeInt(chr.getWP());
        
        return packet.getPacket();
    }

    public static Packet RedCubeStart(MapleCharacter chr, IItem item, int cubeId) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_REDCUBE_WINDOW.getValue());
        packet.writeInt(chr.getId());
        packet.write(0);
        packet.writeInt(cubeId);
        packet.writeInt(item.getPosition());
        PacketProvider.addItemInfo(packet, item, true, true, false, false, chr);
        
        return packet.getPacket();
    }
    
    public static Packet BlackCubeStart(MapleCharacter chr, IItem item, int cubeId, int equipline) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_BLACKCUBE_WINDOW.getValue());
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        packet.write(1);
        PacketProvider.addItemInfo(packet, item, true, true, false, false, chr);
        packet.writeInt(cubeId);
        packet.writeInt(item.getPosition());
        packet.writeInt(equipline); //1.2.252+
        
        return packet.getPacket();
    }

    public static Packet BlackCubeEffect(int cid, int cubeId) { 
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_BLACKCUBE_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(1);
        packet.writeInt(cubeId);
        
        return packet.getPacket();
    }
   
    public static Packet MemorialCubeWindow(MapleCharacter chr, IItem item, int cubeId) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MEMORIAL_CUBE_WINDOW.getValue());
        packet.writeLong(PacketProvider.getTime(System.currentTimeMillis()));
        packet.write(1);
        PacketProvider.addItemInfo(packet, item, true, true, chr);
        packet.writeInt(cubeId);
        packet.writeInt(item.getPosition());
        
        return packet.getPacket();
    }

    public static Packet showKartaEffect() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(0x21); //1.2.251+ (+1)
        packet.writeMapleAsciiString("Effect/BasicEff.img/JobChangedElf");
        packet.write(1);
        packet.writeInt(0);
        packet.writeInt(2);
        packet.writeInt(5155000);
        
        return packet.getPacket();
    }

    public static Packet absorbingFG(int cid, int skillid, int sn) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(13);
        packet.writeInt(257);
        packet.write(0);
        packet.writeInt(sn);
        packet.writeInt(skillid);
        packet.write(1);
        packet.writeInt(Randomizer.rand(1, 10));
        packet.writeInt(skillid == 25100010 ? 1 : 2);
        packet.writeInt(Randomizer.rand(10, 20));
        packet.writeInt(Randomizer.rand(20, 40));
        packet.writeInt(Randomizer.rand(40, 50));
        packet.writeInt(630);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0); //1.2.252+
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet absorbingRFG(int cid, int skillid, int sn) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ABSORB_DF.getValue());
        packet.write(1);
        packet.writeInt(cid);
        packet.writeInt(sn);
        packet.writeInt(4);
        packet.write(1);
        packet.writeInt(sn);
        packet.writeInt(skillid);
        packet.write(1);
        packet.writeInt(Randomizer.rand(1, 10));
        packet.writeInt(skillid == 25100010 ? 4 : 5);
        packet.writeInt(Randomizer.rand(40, 43));
        packet.writeInt(Randomizer.rand(3, 4));
        packet.writeInt(Randomizer.rand(250, 294));
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(Randomizer.nextInt());
        packet.writeInt(0);
        packet.writeInt(0); //1.2.252+
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet spawnArrowFlatter(MapleCharacter chr, int state, Point pos) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_ARROW_FLATTER.getValue());
        packet.writeInt(1);
        packet.writeInt(1);
        packet.writeInt(chr.getId());
        packet.writeInt(state); //ȭ�� On/Off
        packet.writeInt(pos.x);
        packet.writeInt(pos.y);
        packet.write(0);
        
        return packet.getPacket();
    }

    public static Packet spawnArrowFlatter(int state) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.START_ARROW_FLATTER.getValue());
        packet.writeInt(1);
        packet.writeInt(state); //ȭ�� On/Off
        
        return packet.getPacket();
    }

    public static Packet cancelArrowFlatter(int state) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_ARROW_FLATTER.getValue());
        packet.writeInt(1);
        packet.writeInt(state); //ȭ�� On/Off
        
        return packet.getPacket();
    }

    public static Packet showIntro(boolean a) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.INTRO.getValue());
        if (!a) {
            packet.write(0);
        } else {
            packet.write(1);
            packet.writeShort(1);
        }
        return packet.getPacket();
    }

    public static Packet showForeignDamageSkin(MapleCharacter chr, int skinid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_DAMAGE_SKIN.getValue());
        packet.writeInt(chr.getId());
        packet.writeInt(skinid);
                
        return packet.getPacket();
    }
    
    public static Packet showQuestMessage(String text) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0x0C); //1.2.250+ (+1)
        packet.writeMapleAsciiString(text);
        
        return packet.getPacket();
    }

    public static Packet updateInnerExp(int exp) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_INNER_EXP.getValue());
        packet.writeInt(exp);
        
        return packet.getPacket();
    }

    public static Packet updateInnerAbility(final InnerSkillValueHolder skill, int index, boolean last) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_INNER_ABILITY.getValue());
        packet.write(last ? 1 : 0);
        packet.write(1);
        packet.writeShort(index);
        packet.writeInt(skill.getSkillId());
        packet.writeShort(skill.getSkillLevel());
        packet.writeShort(skill.getRank());
        packet.write(last ? 1 : 0);
        
        return packet.getPacket();
    }

    public static Packet sendItemMessage(int itemid, String a) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(0x16);
        packet.writeMapleAsciiString(a);
        PacketProvider.addItemInfo(packet, ItemInformation.getInstance().getEquipById(itemid), false, false, null);
        packet.write(0);
        
        return packet.getPacket();
    }
    
    public static Packet showStardust(int cid, byte[] unk1, byte[] unk2, int skillid, int direction) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.STARDUST.getValue());
        packet.writeInt(cid);
        packet.writeInt(2);
        packet.write(unk1);
        packet.write(unk2);
        packet.writeInt(skillid);
        packet.writeInt(0);
        packet.writeInt(20);
        packet.write(direction);
        packet.writeInt(4);

        return packet.getPacket();
    }
    
    public static Packet DummyBlueStar(int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DUMMY.getValue());
        packet.writeLong(0);
        packet.write(value);
        
        return packet.getPacket();
    }

    public static Packet feverTime() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.FEVER_TIME.getValue());
        packet.writeInt(2);
    
        return packet.getPacket();
    }

    public static Packet TimeCapsule() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.TIME_CAPSULE.getValue());
        
        return packet.getPacket();
   }
    
    public static final Packet showMaplePoint(MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_MAPLE_POINT.getValue());
        packet.writeInt(chr.getCSPoints(2));
        
        return packet.getPacket();
    }
    
    public static Packet ElementalCharge(int count, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.CROSS_OVER_CHAIN);
        packet.writeShort(5);
        packet.writeInt(skillid);
        packet.writeLong(0);
        packet.write(0x6);
        packet.write(count);
        packet.writeShort(0x0C);
        packet.writeShort(514); //02 02, �ǹ̰� ���� ��. 
        packet.write0(13);
        
        return packet.getPacket();
    }

    public static Packet updateMasterPix(MapleCharacter chr, IItem baseitem, IItem basicitem, IItem masterpix) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        packet.write(0);
        packet.write(3);
        packet.write(0);
        packet.write(3);
        packet.write(baseitem.getType());
        packet.writeShort(baseitem.getPosition());
        packet.write(3);
        packet.write(basicitem.getType());
        packet.writeShort(basicitem.getPosition());
        packet.write(0);
        packet.write(masterpix.getType());
        packet.writeShort(masterpix.getPosition());
        PacketProvider.addItemInfo(packet, masterpix, true, true, false, false, chr);
        
        return packet.getPacket();
    }

    public static Packet MasterPix(IItem item, int ml) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MASTERPIX.getValue());
        packet.writeInt(0x79);
        packet.write(ml);
        packet.write(1);
        packet.writeShort(item.getPosition());
        packet.writeShort(0);
        
        return packet.getPacket();
    }
    
    public static Packet getLinkedSkill(MapleCharacter chr, int skillid, int cid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.LINK_SKILL.getValue());
        packet.writeInt(0);
        packet.writeInt(skillid);
        packet.writeInt(cid);
        packet.writeMapleAsciiString(chr.getName());

        return packet.getPacket();
    }

    public static Packet getStarPlanetRank(List<String> name, List<Integer> point) {
        WritingPacket packet = new WritingPacket(); 
        packet.writeShort(SendPacketOpcode.STAR_PLANET_RANK.getValue());
        packet.write(6);
        packet.writeLong(PacketProvider.getKoreanTimestamp(System.currentTimeMillis()));
        packet.write(name.size() != 0 ? 1 : 0); 
        packet.writeInt(name.size());
        for (int i = 0;  i < name.size(); i++) {
            packet.writeInt(i + 1);
            packet.writeInt((int) point.get(i).intValue());
            packet.writeInt((int) point.get(i).intValue());
            packet.writeMapleAsciiString((String) name.get(i));
        } 
       return packet.getPacket();
    }
    
    public static Packet getCombatAnalyze(byte on) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.START_COMBAT_ANALYZE.getValue());
        packet.write(on);
        
        return packet.getPacket();
    }
}
