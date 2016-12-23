/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package community;

import packet.creators.MainPacketCreator;
import packet.transfer.write.Packet;

public enum MapleGuildResponse {

    NOT_IN_CHANNEL(0x42), //1.2.252+ (+18)
    ALREADY_IN_GUILD(0x40), //1.2.252+ (+18)
    NOT_IN_GUILD(0x43); //1.2.252+ (+18)
    private int value;

    private MapleGuildResponse(int val) {
	value = val;
    }

    public int getValue() {
	return value;
    }

    public Packet getPacket() {
	return MainPacketCreator.genericGuildMessage((byte) value);
    }
}
