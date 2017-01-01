package packet.skills;

import client.stats.BuffStats;
import handler.channel.MapleMechDoor;
import packet.creators.PacketProvider;
import packet.opcode.SendPacketOpcode;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;
import tools.HexTool;

public class MechanicSkill {

    
     /**
     * 메탈아머 : 휴먼
     *
     * @param skillId - 패킷값 구분.
     * @return - 패킷값을 보냄.
     */
    public static Packet giveHuman(int skillid, int bufflength, int mountid) {
        WritingPacket packet = new WritingPacket();
        int statup = 0;
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        packet.writeInt(0x4110000);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0x1D8010);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(BuffStats.MONSTER_RIDING.getValue());
        for (int i = 0; i < 6; i++) {
            if ((i == 0) || (i == 1)) {
                statup = 2100;
            } else if ((i == 2)) {
                statup = 0x37;
            } else if (i == 5) {
                statup = 0x1E;
            } else {
                statup = 600;
            }
            packet.writeInt(statup);
            packet.writeInt(skillid);
            packet.writeInt((i == 5) ? 0 : bufflength);
        }
        packet.write0(9);
        packet.writeInt(mountid);
        packet.writeInt(skillid);
        packet.write0(5);
        for (int i = 0; i < 3; i++) {
            packet.writeInt(1);
            packet.writeInt(skillid);
            packet.writeInt((i == 2) ? -1 : 30);
            packet.write(HexTool.getByteArrayFromHexString("8E 49 F5 43 8E 49 F5 43"));
            packet.writeLong(0);
        }
        packet.writeInt(0);
        packet.write(1);
        packet.write(0x5);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

     /**
     * 메탈아머 : 휴먼 캔슬모션
     *
     * @param skillId - 패킷값 구분.
     * @return - 패킷값을 보냄.
     */
    public static Packet cancelHuman() { 
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        packet.writeInt(0x4110000);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0x1D8010);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(BuffStats.MONSTER_RIDING.getValue());
        packet.write0(20);
        packet.write(0x7);
        packet.write(0x1);
        
        return packet.getPacket();
    }
    
     /**
     * 메탈아머 : 탱크 
     *
     * @param skillId - 패킷값 구분.
     * @return - 패킷값을 보냄.
     */
    
    public static Packet giveTank(int skillid, int bufflength, int mountid, int smountid) {
        WritingPacket packet = new WritingPacket();
        int statup = 0;
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        packet.writeInt(0x4000000);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0x1D8010);
        packet.writeInt(0);
        packet.writeInt(0x400000);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(BuffStats.MONSTER_RIDING.getValue());
        for (int i = 0; i < 7; i++) {
            if ((i == 0) || (i == 1)) {
                statup = 2100;
            } else if ((i == 2)) {
                statup = 0x37;
            } else if (i == 5) {
                statup = 0x01;
            } else if (i == 6) {
                statup = 0x32;
            } else {
                statup = 600;
            }
            packet.writeInt(statup);
            packet.writeInt(skillid);
            packet.writeInt((i == 5) ? 0 : bufflength);
        }
        packet.write0(9);
        packet.writeInt(mountid);
        packet.writeInt(skillid);
        packet.writeInt(smountid);
        packet.write(0);
        packet.writeInt(1);
        packet.writeInt(skillid);
        packet.writeInt(30);
        packet.write(HexTool.getByteArrayFromHexString("EF 41 C6 37 EF 41 C6 37"));
        packet.write0(12);
        packet.write(1);
        packet.write(0x9);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

     /**
     * 메탈아머 : 탱크 캔슬모션
     *
     * @param skillId - 패킷값 구분.
     * @return - 패킷값을 보냄.
     */
    public static Packet cancelTank() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        packet.writeInt(0x5000020);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0x4);
        packet.writeInt(0x1D8010);
        packet.writeInt(0);
        packet.writeInt(0x40000);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(BuffStats.MONSTER_RIDING.getValue());
        packet.write0(12);
        packet.write(0x25);
        packet.write(0x01);
        
        return packet.getPacket();
    }
    
     /**
     * 메탈아머 : 휴먼, 탱크 공통 스탯
     *
     * @param skillId - 패킷값 구분.
     * @return - 패킷값을 보냄.
     */
    public static Packet giveMetalStats(int skillid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        packet.writeInt(0x5000020);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0x4);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.writeShort(0); //Color
        packet.writeInt(30000227);
        packet.write0(13);
        for (int i = 0; i < 4; i++) {
            packet.writeInt((i == 0) ? 2 : (i == 1) ? 0 : 1);
            packet.writeInt((i == 1) ? skillid : 30000227);
            packet.writeInt((i == 1) ? 30 : 10);
            packet.write(HexTool.getByteArrayFromHexString("38 3B D1 37 38 3B D1 37"));
            packet.writeInt((i == 1) && (skillid == 35111003) ? 1 : 0);
            if ((i != 0)) {
                packet.writeInt(0);
            }
        }
        packet.writeInt(0);
        packet.write(0x1);
        packet.writeInt(0);
        
        return packet.getPacket();
    }

    /**
     * 메카닉 오픈게이트 소환 패킷
     *
     * @param door - 메카닉의 오픈게이트 클래스를 불러옴.
     * @param active - 작동하는지 안하는지 여부를 물음.
     * @return - 패킷값을 보냄.
     */
    public static Packet mechDoorSpawn(MapleMechDoor door, boolean active) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
        packet.write(active ? 0 : 1);
        packet.writeInt(door.getOwnerId());
        packet.writePos(door.getTruePosition());
        packet.write(door.getId());
        if (door.getPartyId() > 0) {
            packet.writeInt(door.getPartyId()); //1.2.252+
        }        
        return packet.getPacket();
    }

    /**
     * 메카닉 오픈게이트 취소 패킷
     *
     * @param door - 메카닉의 오픈게이트 클래스를 불러옴.
     * @param active - 작동하는지 안하는지 여부를 물음.
     * @return - 패킷값을 보냄.
     */
    public static Packet mechDoorRemove(MapleMechDoor door, boolean active) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
        packet.write(active ? 0 : 1);
        packet.writeInt(door.getOwnerId());
        packet.write(door.getId());
        
        return packet.getPacket();
    }
    
    /**
     * 메카닉 위장색
     *
     */
    public static Packet MechanicMetalArmorCamouflage(int id, int time) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.MECHANIC_CAMOUFLAGE);
        packet.writeShort(id);
        packet.writeInt(30000227);
        packet.writeInt(time);
        packet.write0(18);
        
        return packet.getPacket();
    }
}