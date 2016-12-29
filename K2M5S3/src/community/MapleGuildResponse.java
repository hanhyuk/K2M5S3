package community;

import packet.creators.MainPacketCreator;
import packet.transfer.write.Packet;

public enum MapleGuildResponse {

	NOT_IN_CHANNEL(0x42), // 1.2.252+ (+18)
	ALREADY_IN_GUILD(0x40), // 1.2.252+ (+18)
	NOT_IN_GUILD(0x43); // 1.2.252+ (+18)
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
