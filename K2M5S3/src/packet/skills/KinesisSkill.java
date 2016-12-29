package packet.skills;

import client.MapleClient;
import packet.opcode.SendPacketOpcode;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.WritingPacket;
import server.life.MapleMonster;

public class KinesisSkill {

    public static void PsychicUltimateDamager(ReadingMaple rh, final MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PSYCHIC_ULTIMATE.getValue());
        packet.writeInt(c.getPlayer().getId());
        packet.writeInt(rh.readInt());
        
        c.getSession().write(packet.getPacket());
    }
    
    public static void PsychicDamage(int mobcount, final MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PSYCHIC_DAMAGE.getValue());
        packet.writeInt(mobcount);
        packet.writeInt(1);
        
        c.getSession().write(packet.getPacket());
    }
    
    public static void PsychicAttack(ReadingMaple rh, final MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PSYCHIC_ATTACK.getValue());
        packet.writeInt(c.getPlayer().getId());
        packet.write(1);
        packet.writeInt(rh.readInt()); 
        packet.writeInt((int) rh.readLong());
        final int mobcount = rh.readInt();
        packet.writeInt(mobcount); 
        final int skillid = rh.readInt();
        packet.writeInt(skillid);
        packet.writeShort(rh.readShort());
        packet.writeInt((0xFFFFFFFF - mobcount) + 1);
        final int unknown_i = rh.readInt();
        packet.writeInt(unknown_i != 0xFFFFFFFF ? unknown_i + 4000 : unknown_i);
        packet.write(rh.readByte());
        final short unknown_si = rh.readShort();
        packet.writeShort(unknown_si != 0xFFFF ? unknown_si : 0);
        final short unknown_sii = rh.readShort();
        packet.writeShort(unknown_sii != 0xFFFF ? unknown_sii : 0);
        final short unknown_siii = rh.readShort();
        packet.writeShort(unknown_siii != 0xFFFF ? unknown_siii : 0);
        packet.writePos(rh.readPos());
        packet.writePos(rh.readPos());
        
        /* PPoint Check */
        c.getPlayer().givePPoint(skillid);
        
        c.getSession().write(packet.getPacket());
    }
    
    public static void CancelPsychicGrep(ReadingMaple rh, final MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_PSYCHIC_GREP.getValue());
        packet.writeInt(c.getPlayer().getId());
        packet.writeInt(rh.readInt());
        
        c.getSession().write(packet.getPacket());
    }
    
    public static void PsychicGrep(ReadingMaple rh, final MapleClient c) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.PSYCHIC_GREP.getValue());
        /* First AttackInfo Start */ 
        packet.writeInt(c.getPlayer().getId());
        packet.write(1);
        final int skillid = rh.readInt();
        packet.writeInt(skillid); 
        packet.writeShort(rh.readShort()); 
        packet.writeInt(rh.readInt()); 
        packet.writeInt(rh.readInt()); 
        /* First AttackInfo End */
        int i = 0;
        int point = 0;
        boolean end = false;
        MapleMonster target = null;
        while(true) {
            end = (rh.readByte() <= 0);
            packet.write(!end ? 1 : 0); 
            if (!end) {
                packet.write(!end ? 1 : 0);
                packet.writeInt(rh.readInt());
            } else {
                break;
            }
            rh.skip(4);
            packet.writeInt((i) + 1); 
            final int monsterid = rh.readInt();
            packet.writeInt(monsterid); //몬스터 아이디.
            packet.writeShort(rh.readShort());
            if (monsterid != 0) {
               target = c.getPlayer().getMap().getMonsterByOid(monsterid);
            }
            rh.skip(2);
            packet.writeInt(monsterid != 0 ? (int) target.getHp() : 100); 
            packet.writeInt(monsterid != 0 ? (int) target.getHp() : 100);
            packet.write(rh.readByte()); 
            packet.writePos(rh.readPos());
            packet.writePos(rh.readPos());
            packet.writePos(rh.readPos()); 
            packet.writePos(rh.readPos()); 
            i++;
        }
        /* PPoint Check */
        c.getPlayer().givePPoint(skillid);
        
        c.getSession().write(packet.getPacket());
    }
}
