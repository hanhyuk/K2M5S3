package handler.channel;

import client.MapleCharacter;
import packet.creators.MainPacketCreator;
import packet.skills.AngelicBusterSkill;
import packet.transfer.read.ReadingMaple;

public class AngelicBusterHandler {
	public static void DressUpRequest(final MapleCharacter chr, ReadingMaple rh) {
		int code = rh.readInt();
		switch (code) {
		case 5010093:
			chr.getMap().broadcastMessage(MainPacketCreator.updateCharLook(chr, true));
			chr.getMap().broadcastMessage(AngelicBusterSkill.updateDress(code, chr));
			break;
		case 5010094:
			chr.getMap().broadcastMessage(MainPacketCreator.updateCharLook(chr, true));
			chr.getMap().broadcastMessage(AngelicBusterSkill.updateDress(code, chr));
			break;
		}
	}
}
