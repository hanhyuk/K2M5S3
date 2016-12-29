package packet.skills;

import client.stats.BuffStats;
import packet.creators.PacketProvider;
import packet.opcode.SendPacketOpcode;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;

public class AdventurerSkill {
    
    public static Packet giveBeholderDominant(int buffid1, int buffid2, int buffid3) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.BEHOLDER);
        packet.writeShort(1);
        packet.writeInt(buffid1); 
        packet.writeInt(344117);
        packet.writeInt(0);
        packet.write(4);
        packet.writeInt(buffid2); 
        if (buffid3 == 1311014) {
            packet.writeInt(1311014);
            packet.writeInt(0);
        }
        packet.write0(12);
        packet.writeShort(1);
        packet.write0(3);

        return packet.getPacket();
    }
    
    public static Packet CancelHeholderBuff() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketProvider.writeSingleMask(packet, BuffStats.BEHOLDER);
        
        return packet.getPacket();
    }
}