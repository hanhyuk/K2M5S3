/*
 * ArcStory Project
 * √÷¡÷ø¯ sch2307@naver.com
 * ¿Ã¡ÿ junny_adm@naver.com
 * øÏ¡ˆ»∆ raccoonfox69@gmail.com
 * ∞≠¡§±‘ ku3135@nate.com
 * ±Ë¡¯»´ designer@inerve.kr
 */

package packet.creators;

import client.MapleCharacter;
import client.stats.BuffStats;
import packet.opcode.SendPacketOpcode;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;

public class SoulWeaponPacket {
    
    public static Packet giveSoulGauge(int count, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.SOUL_WEAPON);
        packet.writeShort(count);
        packet.writeInt(skillid);
        packet.writeInt(0);
        packet.writeInt(1000);
        packet.writeInt(skillid);
        packet.write0(18);

        return packet.getPacket();
    }

    public static Packet cancelSoulGauge() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.SOUL_WEAPON);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static Packet giveSoulEffect(int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.SOUL_HEAD_EFFECT);
        packet.writeShort(0);
        packet.writeInt(skillid);
        packet.writeInt(640000);
        packet.write0(22);
        
        return packet.getPacket();
    }

    public static Packet showEnchanterEffect(int cid, byte result) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_ENCHANTER_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(result);

        return packet.getPacket();
    }

    public static Packet showSoulScrollEffect(int cid, byte result, boolean destroyed) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_SOULSCROLL_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(result);
        packet.write(destroyed ? 1 : 0);

        return packet.getPacket();
    }

    public static Packet giveForeignSoulEffect(int cid, int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, BuffStats.SOUL_HEAD_EFFECT);
        packet.writeInt(skillid);
        packet.writeLong(0x60000000000L);
        packet.write0(21);
        
        return packet.getPacket();
    }

    public static Packet cancelForeignSoulEffect(int cid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        PacketProvider.writeSingleMask(packet, BuffStats.SOUL_HEAD_EFFECT);
        packet.write(1);

        return packet.getPacket();
    }
    
    public static Packet showSoulEffect(MapleCharacter chr, byte use) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_SOULEFFECT_RESPONESE.getValue());
        packet.writeInt(chr.getId());
        packet.write(use);
        
        return packet.getPacket();
    }
}