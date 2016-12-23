/*
 * ArcStory Project
 * ÃÖÁÖ¿ø sch2307@naver.com
 * ÀÌÁØ junny_adm@naver.com
 * ¿ìÁöÈÆ raccoonfox69@gmail.com
 * °­Á¤±Ô ku3135@nate.com
 * ±èÁøÈ« designer@inerve.kr
 */

package packet.creators;

import client.MapleCharacter;
import client.MapleClient;
import packet.opcode.SendPacketOpcode;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;
import tools.HexTool;

public class UIPacket {
    
    public static Packet showInfo(String msg) {
	final WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.TOP_MSG.getValue());
	packet.writeMapleAsciiString(msg);
        
	return packet.getPacket();
    }
    
    public static Packet greenShowInfo(String msg) {
	final WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.GREEN_SHOW_INFO.getValue());
        packet.write(0);
	packet.writeMapleAsciiString(msg);
        packet.write(1); // 0 = Lock 1 = Clear
        
	return packet.getPacket();
    }
    
    public static Packet detailShowInfo(String msg, int color) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DETAIL_SHOW_INFO.getValue());
        packet.writeInt(color); //Color
        packet.writeInt(0x14); //Width
        packet.writeInt(0x04); //Height
        packet.writeInt(0); //Unknown
        packet.writeMapleAsciiString(msg);
        
        return packet.getPacket();
  }
    
    public static Packet getItemTopMsg(int itemid, String msg) {
        final WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MID_MSG.getValue());
        packet.writeInt(itemid);
        packet.writeMapleAsciiString(msg);
        
        return packet.getPacket();
    }
    
    public static Packet enforceMSG(String a, int id, int delay) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ENFORCE_MSG.getValue());
        packet.writeMapleAsciiString(a);
        packet.writeInt(id);
        packet.writeInt(delay);
        packet.write(1);
        
        return packet.getPacket();
    }
    
    public static Packet clearMidMsg() {
        WritingPacket mplew = new WritingPacket();
        mplew.writeShort(SendPacketOpcode.CLEAR_MID_MSG.getValue());

        return mplew.getPacket();
    }

    public static Packet getStatusMsg(int itemid) {
	WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
	packet.write(0x8); //1.2.250+ (+1)
	packet.writeInt(itemid);

	return packet.getPacket();
    }

   public static Packet getSPMsg(byte sp, short job) {
	WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
	packet.write(0x5); //1.2.250+ (+1)
	packet.writeShort(job);
	packet.write(sp);

	return packet.getPacket();
    }

    public static Packet getGPMsg(int itemid) {
	WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
	packet.write(0x8); //1.2.250+ (+1)
	packet.writeInt(itemid);

	return packet.getPacket();
    }
    
    public static final Packet MapNameDisplay(final int mapid) {
	final WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
	packet.write(0x4);
	packet.writeMapleAsciiString("maplemap/enter/" + mapid);

	return packet.getPacket();
    }

    public static final Packet showWZEffect(final String data, int value) {
	WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
	packet.write(value == 0 ? 0x16 : 0x19); //1.2.252+
	packet.writeMapleAsciiString(data);
	packet.writeInt(value);
        
	return packet.getPacket();
    }
    
    public static final Packet broadcastWZEffect(final int cid, final String data, int value) {
	WritingPacket packet = new WritingPacket();
	packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
	packet.write(value == 0 ? 0x16 : 0x19); //1.2.252+
	packet.writeMapleAsciiString(data);
	packet.writeInt(value);
        
	return packet.getPacket();
    }  
    
    public static final Packet showWZEffect(final String data) {
	return showWZEffect(data, 0);
    }

    public static Packet IntroDisableUI(boolean enable) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DISABLE_UI.getValue());
        packet.write(enable ? 1 : 0);
        if (enable) {
             packet.writeShort(1);
        }
        return packet.getPacket();
    }
    
    public static final Packet AchievementRatio(int value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ACHIVEMENT_RATIO.getValue());
        packet.writeInt(value);

        return packet.getPacket();
    }
        
    public static final Packet getMapleStar(byte type, MapleClient c, int star1, int star2) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MAPLE_CHAT.getValue());
        packet.write(type);
        MapleCharacter chr = null, s_chr = null;
        if (star1 > 0 || star2 > 0) {
            chr = MapleCharacter.loadCharFromDB(star1, c, true);
            if (star2 > 0) {
                s_chr = MapleCharacter.loadCharFromDB(star2, c, true);
            }
        }
        for (int i = 0; i < 2; i++) {
            if (i == 0 ? chr != null : s_chr != null) {
                packet.write(1);
                packet.writeInt(i == 0 ? chr.getId() : s_chr.getId());
                packet.writeInt(i == 0 ? chr.getLevel(): s_chr.getLevel()); 
                packet.writeLong(System.currentTimeMillis());
                packet.writeMapleAsciiString(i == 0 ? chr.getName() : s_chr.getName());
                PacketProvider.addPlayerLooks(packet, i == 0 ? chr : s_chr, true);
            } else { 
                packet.write(0);
            }
        }
        return packet.getPacket();
    }

    public static final Packet OpenUI(int value) {  
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.OPEN_WINDOW.getValue());
        packet.writeInt(value);
        packet.writeLong(0);
        
	return packet.getPacket();
    }
    
    public static final Packet DeadedUI(byte value) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DEADED_WINDOW.getValue());
        packet.write(value);
        packet.writeLong(0);
        
        return packet.getPacket();
    }
    
    public static final Packet showSpecialMapEffect(int type, int action, String music, String back) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPECIAL_MAP_EFFECT.getValue());
        packet.writeInt(type);
        packet.writeInt(action);
        packet.writeMapleAsciiString(music);
        if (back != null) {
            packet.writeMapleAsciiString(back);
        }
        return packet.getPacket();
    }

    public static final Packet cancelSpecialMapEffect() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPECIAL_MAP_EFFECT.getValue());
        packet.writeLong(0);
        
        return packet.getPacket();
   }

    public static final Packet playSpecialMapSound(String sound) {
      WritingPacket packet = new WritingPacket();
      packet.writeShort(SendPacketOpcode.SPECIAL_MAP_SOUND.getValue());
      packet.writeMapleAsciiString(sound);
      
      return packet.getPacket();
    }
    
    public static final Packet eliteBossNotice(int type, int mapid, int mobid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.ELITE_BOSS_NOTICE.getValue());
        packet.write(type);
        if (type == 1) {
            packet.writeInt(mapid);
        } else if (type == 2) {
            packet.writeInt(mapid);
            packet.writeInt(mobid);
            packet.write(HexTool.getByteArrayFromHexString("20 75 1A 00"));
        }
        return packet.getPacket();
    }
    
    public static Packet showPopupMessage(final String msg) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.POPUP_MSG.getValue());
        packet.writeMapleAsciiString(msg);
        packet.write(1);
        
        return packet.getPacket();
    }
}
