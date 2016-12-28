package packet.creators;

import constants.GameConstants;
import client.MapleClient;
import client.MapleCharacter;
import client.items.Equip;
import client.items.IItem;
import client.items.Item;
import client.items.MapleInventoryType;
import handler.channel.InventoryHandler;
import packet.transfer.write.Packet;
import packet.opcode.SendPacketOpcode;
import tools.HexTool;
import packet.transfer.write.WritingPacket;
import server.items.CashItemInfo;
import tools.Pair;
import java.util.List;
import server.shops.HiredMerchant;
import server.shops.MapleCharacterShopItem;

public class CashPacket {

	public static Packet warpCS(MapleClient c) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue() + 2);
		PacketProvider.addPlayerInfo(packet, c.getPlayer());
		packet.write(HexTool.getByteArrayFromHexString(
				"1F 00 8B 77 8E 06 04 00 00 00 00 00 00 00 0A 00 00 00 8C 77 8E 06 04 00 00 00 00 00 00 00 0A 00 00 00 C0 84 91 06 04 00 00 00 00 00 00 00 2C 10 00 00 C1 84 91 06 04 00 00 00 00 00 00 00 10 0E 00 00 C2 84 91 06 04 00 00 00 00 00 00 00 84 12 00 00 C3 84 91 06 04 00 00 00 00 00 00 00 34 17 00 00 C4 84 91 06 04 00 00 00 00 00 00 00 7C 0B 00 00 C5 84 91 06 04 00 00 00 00 00 00 00 24 09 00 00 C6 84 91 06 04 00 00 00 00 00 00 00 78 0F 00 00 36 0E 27 07 04 00 00 00 00 00 00 00 E0 10 00 00 37 0E 27 07 04 00 00 00 00 00 00 00 10 0E 00 00 BB 94 28 07 04 00 00 00 00 00 00 00 F8 16 00 00 BC 94 28 07 04 00 00 00 00 00 00 00 2C 10 00 00 27 A5 BF 07 04 00 00 00 00 00 00 00 2E 22 00 00 3E A5 BF 07 04 00 00 00 00 00 00 00 DA 2F 00 00 3F A5 BF 07 04 00 00 00 00 00 00 00 34 3A 00 00 40 A5 BF 07 04 00 00 00 00 00 00 00 08 52 00 00 50 A5 BF 07 04 00 00 00 00 00 00 00 DC 05 00 00 53 A5 BF 07 04 00 00 00 00 00 00 00 DC 05 00 00 54 A5 BF 07 04 00 00 00 00 00 00 00 10 0E 00 00 D0 C3 59 08 04 00 00 00 00 00 00 00 4E 0C 00 00 D1 C3 59 08 04 00 00 00 00 00 00 00 C0 08 00 00 D2 C3 59 08 04 00 00 00 00 00 00 00 06 09 00 00 D3 C3 59 08 04 00 00 00 00 00 00 00 92 09 00 00 3F D0 5C 08 04 00 00 00 00 00 00 00 34 08 00 00 40 D0 5C 08 04 00 00 00 00 00 00 00 34 08 00 00 41 D0 5C 08 04 00 00 00 00 00 00 00 34 08 00 00 42 D0 5C 08 04 00 00 00 00 00 00 00 06 09 00 00 43 D0 5C 08 04 00 00 00 00 00 00 00 AA 0A 00 00 89 56 5E 08 04 00 00 00 00 00 00 00 D6 06 00 00 EA DC 5F 08 04 00 00 00 00 00 00 00 32 05 00 00 00 00 1D 00 00 00 4C 6D 54 00 06 00 00 00 19 63 3D 01 02 63 3D 01 05 63 3D 01 06 63 3D 01 0A 63 3D 01 0C 63 3D 01 64 6D 54 00 05 00 00 00 D1 70 64 08 CD 70 64 08 CE 70 64 08 CF 70 64 08 D0 70 64 08 54 6D 54 00 04 00 00 00 78 F6 41 01 79 F6 41 01 7A F6 41 01 7B F6 41 01 55 6D 54 00 09 00 00 00 2F 2F 31 01 30 2F 31 01 31 2F 31 01 32 2F 31 01 33 2F 31 01 34 2F 31 01 35 FE FD 02 36 FE FD 02 37 FE FD 02 5D 6D 54 00 05 00 00 00 3D 63 3D 01 3E 63 3D 01 3F 63 3D 01 40 63 3D 01 41 63 3D 01 4E 6D 54 00 05 00 00 00 61 C3 59 08 5D C3 59 08 5E C3 59 08 03 C3 59 08 60 C3 59 08 66 6D 54 00 05 00 00 00 D2 70 64 08 D3 70 64 08 D4 70 64 08 D5 70 64 08 D6 70 64 08 56 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 5E 6D 54 00 03 00 00 00 91 F6 41 01 92 F6 41 01 93 F6 41 01 96 6D 54 00 06 00 00 00 56 C4 59 08 51 C4 59 08 52 C4 59 08 53 C4 59 08 54 C4 59 08 55 C4 59 08 4F 6D 54 00 05 00 00 00 18 2F 31 01 19 2F 31 01 1A 2F 31 01 1B 2F 31 01 1C 2F 31 01 87 6D 54 00 05 00 00 00 34 C4 59 08 02 49 5B 08 35 C4 59 08 36 C4 59 08 37 C4 59 08 6F 6D 54 00 08 00 00 00 12 C4 59 08 13 C4 59 08 14 C4 59 08 15 C4 59 08 16 C4 59 08 17 C4 59 08 18 C4 59 08 19 C4 59 08 5F 6D 54 00 05 00 00 00 71 2F 31 01 72 2F 31 01 73 2F 31 01 74 2F 31 01 75 2F 31 01 48 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 60 6D 54 00 04 00 00 00 B7 C3 59 08 B8 C3 59 08 B9 C3 59 08 BA C3 59 08 50 6D 54 00 03 00 00 00 75 F6 41 01 76 F6 41 01 77 F6 41 01 49 6D 54 00 06 00 00 00 03 63 3D 01 04 63 3D 01 07 63 3D 01 08 63 3D 01 09 63 3D 01 0B 63 3D 01 81 6D 54 00 05 00 00 00 E6 70 64 08 2E C4 59 08 2F C4 59 08 F5 F6 65 08 E5 70 64 08 51 6D 54 00 05 00 00 00 1F 2F 31 01 20 2F 31 01 21 2F 31 01 22 2F 31 01 23 2F 31 01 59 6D 54 00 04 00 00 00 4E A1 98 00 4F A1 98 00 50 A1 98 00 51 A1 98 00 71 6D 54 00 06 00 00 00 7A C3 59 08 1A C4 59 08 7B C3 59 08 7C C3 59 08 7D C3 59 08 7E C3 59 08 4A 6D 54 00 05 00 00 00 CC 3B 58 08 CD 3B 58 08 CE 3B 58 08 26 3C 58 08 CF 3B 58 08 62 6D 54 00 06 00 00 00 C3 C3 59 08 C4 C3 59 08 C5 C3 59 08 C6 C3 59 08 C7 C3 59 08 C8 C3 59 08 6A 6D 54 00 05 00 00 00 5D C3 59 08 5E C3 59 08 03 C3 59 08 60 C3 59 08 61 C3 59 08 5A 6D 54 00 03 00 00 00 81 F6 41 01 82 F6 41 01 83 F6 41 01 7A 6D 54 00 05 00 00 00 89 7D 67 08 8A 7D 67 08 8B 7D 67 08 8C 7D 67 08 8D 7D 67 08 4B 6D 54 00 07 00 00 00 E5 2E 31 01 E6 2E 31 01 E7 2E 31 01 E8 2E 31 01 E9 2E 31 01 EA 2E 31 01 EB 2E 31 01 53 6D 54 00 05 00 00 00 27 2F 31 01 28 2F 31 01 29 2F 31 01 2A 2F 31 01 2B 2F 31 01 27 00 00 00 02 2C 86 23 0A 02 2D 86 23 0A 03 56 0E 27 07 03 57 0E 27 07 03 57 C4 59 08 02 D0 3B 58 08 02 11 C4 59 08 04 F7 85 23 0A 04 F8 85 23 0A 01 75 A5 BF 07 00 30 86 23 0A 03 E0 DE F3 08 02 80 77 8E 06 04 98 77 8E 06 02 8B FE 21 0A 02 DF DE F3 08 02 8C FE 21 0A 02 89 FE 21 0A 00 6C 0C 25 0A 02 48 0E 27 07 02 4A 0E 27 07 04 20 58 F2 08 04 9B D1 F0 08 02 0C A5 BF 07 04 B7 85 23 0A 04 B8 85 23 0A 04 0A 86 23 0A 04 0B 86 23 0A 00 69 0C 25 0A 00 6A 0C 25 0A 00 6B 0C 25 0A 04 2E 2B C1 07 04 8C A4 BF 07 04 0D A5 BF 07 04 3E 2B C1 07 04 95 D1 F0 08 04 24 58 F2 08 04 AF D1 F0 08 04 2F 58 F2 08 04 00 00 00 04 85 FE 21 0A 04 86 FE 21 0A 04 87 FE 21 0A 04 88 FE 21 0A 03 00 00 00 04 00 B7 CE BE E2 42 00 B4 F5 BF ED B4 F5 20 BF B9 BB DA B0 ED 20 B8 DA C1 F8 20 C4 B3 B8 AF C5 CD B8 A6 20 BF F8 C7 D8 BF E4 3F 20 B1 D7 20 BA F1 B9 FD C0 CC 20 B1 C3 B1 DD C7 CF B8 E9 20 2D 2D 2D 2D 2D 3E 20 C5 AC B8 AF 0B 00 B8 C5 C1 F6 C4 C3 20 C7 CF C7 C1 53 00 C7 C7 B9 F6 C0 C7 20 C1 A4 BB F3 C0 BB 20 C7 E2 C7 D8 21 20 C7 C7 B9 F6 C0 C7 20 BC F8 B0 A3 BF A1 B4 C2 21 20 C1 C1 C0 BA 20 BE C6 C0 CC C5 DB C0 CC 20 C3 A3 BE C6 BF C3 20 B0 A1 B4 C9 BC BA C0 CC 20 B4 F5 BA ED B7 CE 20 55 50 B5 CB B4 CF B4 D9 2E 12 00 B8 DE C0 CC C7 C3 20 B7 CE BE E2 20 BD BA C5 B8 C0 CF 3E 00 B0 AD B7 C2 C7 D1 20 BF C9 BC C7 C0 CC 20 BA D9 BE EE C0 D6 B4 C2 20 B1 E2 B0 A3 C7 D1 C1 A4 20 C6 AF B1 DE 20 BE C6 C0 CC C5 DB B5 E9 C0 BB 20 B3 F5 C4 A1 C1 F6 20 B8 B6 BC BC BF E4 2E C0 D7 F2 08 0C 93 81 09 14 01 73 08 00 00 00 00 00 00 00 00 00 00 00 00 84 EA 74 08 A4 82 71 09 00 00 00 00 00 00 00 00 28 8C 46 09 D8 A3 61 09 00 00 00 00 44 D4 D7 09 D8 86 A0 08 48 17 73 08 30 16 82 09 00 00 00 00 F8 A4 80 09 00 00 00 00 00 00 00 00 E8 75 94 08 E8 10 AB 09 A4 E2 46 09 40 F3 C0 08 08 9F A0 08 50 AD 76 08 A8 CE 81 09 00 00 00 00 00 00 00 00 30 09 EB 08 84 E6 F4 09 00 00 00 00 00 00 00 00 E4 61 81 09 D4 85 99 08 20 DD 70 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7C 95 46 09 D4 05 C1 08 00 00 00 00 00 00 00 00 E4 B1 F2 08 2C 77 94 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8C C7 F2 08 00 00 00 00 8C 20 F3 08 00 00 00 00 9C D2 C0 08 00 00 00 00 00 00 00 00 80 CF 80 09 9C 24 8F 08 00 00 00 00 00 00 00 00 C4 D8 81 09 00 00 00 00 DC 70 8C 05 00 00 00 00 F8 C6 D6 09 BC C0 F4 09 00 00 00 00 00 00 00 00 00 00 00 00 1C FA F4 09 C0 A6 AA 09 00 00 00 00 00 00 00 00 00 00 00 00 CC C8 F4 09 00 00 00 00 58 82 80 09 BC 88 54 09 00 00 00 00 00 00 00 00 A8 5D 61 09 C8 78 80 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 B8 71 71 09 A0 A8 AB 08 00 00 00 00 00 00 00 00 A4 D4 D6 09 BC 83 AA 09 00 00 00 00 E4 27 7A 08 78 75 84 08 08 13 71 09 90 DE 98 05 00 00 00 00 00 00 00 00 EC 8D AA 09 58 4E 80 09 00 00 00 00 28 A8 C0 08 00 00 00 00 08 29 8F 08 EC CC F2 08 24 C8 66 08 00 00 00 00 90 90 AB 09 00 00 00 00 2C 18 F3 08 54 6C F2 08 5C 90 C0 08 74 2D 80 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 2B AC 09 00 00 00 00 18 9B D6 09 08 46 80 09 D0 8D 99 08 00 00 00 00 00 00 00 00 00 00 00 00 64 15 82 09 A8 6F 94 08 00 00 00 00 00 00 00 00 14 DB AA 09 34 BB 70 09 00 00 00 00 18 7F 61 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 7E 47 09 38 66 D6 09 5C 82 CB 08 00 00 00 00 40 73 CB 08 00 00 00 00 00 00 00 00 54 B3 B6 08 00 00 00 00 00 00 00 00 00 00 00 00 48 F6 54 09 00 00 00 00 00 00 00 00 0C EE D7 09 00 00 00 00 00 00 00 00 60 92 54 09 00 00 00 00 00 00 00 00 2C 76 71 08 00 00 00 00 00 00 00 00 00 00 00 00 5C C1 F4 09 00 00 00 00 14 AE FD 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 64 BF 81 09 00 00 00 00 00 00 00 00 70 BA B6 08 78 93 94 08 60 53 DE 08 0C AC F2 08 54 BD 46 09 68 C5 F2 08 64 B6 81 09 00 00 00 00 9C 53 71 09 94 78 81 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 88 AC B6 08 18 BD 9B 08 78 43 F5 09 00 00 00 00 40 A8 94 08 AC 70 81 09 2A 00 00 2A 98 93 D4 00 90 FE 44 18 A8 1F 83 0A 36 EC 93 65 00 00 00 80 D4 0F 00 00 69 00 63 00 6F 00 6E 00 00 00 00 00 35 EC 93 65 00 00 00 80 CE 0F 00 00 70 00 72 00 69 00 63 00 65 00 00 00 38 EC 93 65 00 00 00 80 BA 08 00 00 6E 00 70 00 63 00 00 00 00 00 00 00 3F EC 93 65 00 00 00 80 DD 0F 00 00 6E 00 70 00 63 00 00 00 00 00 00 00 22 EC 93 65 00 00 00 80 D1 0F 00 00 69 00 6E 00 66 00 6F 00 00 00 00 00 21 EC 93 65 00 00 00 80 FB 0F 00 00 73 00 70 00 65 00 63 00 00 00 00 00 24 EC 93 65 00 00 00 80 E6 0F 00 00 73 00 70 00 65 00 63 00 00 00 00 00 2B EC 93 65 00 00 00 80 F0 08 00 00 69 00 6E 00 66 00 6F 00 00 00 00 00 2E EC 93 65 00 00 00 80 F5 0F 00 00 69 00 63 00 6F 00 6E 00 00 00 00 00 2D EC 93 65 00 00 06 00 79 3D 4D 00 C9 84 91 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 96 00 00 00 96 00 00 00 0B 00 00 00 FB 78 33 01 FB 78 33 01 09 00 00 00 15 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3C 00 00 00 C0 35 4D 00 CD 84 91 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 96 00 00 00 96 00 00 00 0B 00 00 00 FC 78 33 01 FC 78 33 01 09 00 00 00 15 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3C 00 00 00 E2 62 54 00 CE 84 91 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 96 00 00 00 96 00 00 00 0B 00 00 00 FD 78 33 01 FD 78 33 01 09 00 00 00 15 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3C 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 FF FF FF FF FF FF FF FF 0A 00 00 00 3B 78 33 01 9D 78 33 01 0C 00 00 00 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5A 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 FF FF FF FF FF FF FF FF 0A 00 00 00 82 78 33 01 9D 78 33 01 10 00 00 00 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5A 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 FF FF FF FF FF FF FF FF 0A 00 00 00 8D 78 33 01 9A 78 33 01 10 00 00 00 15 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 78 00 00 00 00 00 01 00 00"));

		return packet.getPacket();
	}

	public static Packet useCharm(byte charmsleft, byte daysleft) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		packet.write(9);
		packet.write(1);
		packet.write(charmsleft);
		packet.write(daysleft);

		return packet.getPacket();
	}

	public static Packet itemExpired(int itemid) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		packet.write(3); // 1.2.250+ (+1)
		packet.writeInt(itemid);

		return packet.getPacket();
	}

	public static Packet GoldenHammer(boolean start, boolean success) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.HAMMER_EFFECT.getValue());
		if (start) {
			packet.write(0);
			packet.writeInt(1);
		} else {
			packet.write(2);
			packet.writeInt(success ? 0 : 1);
		}

		return packet.getPacket();
	}

	public static Packet changePetName(MapleCharacter chr, String newname, int slot) {
		WritingPacket mplew = new WritingPacket();
		mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());
		mplew.writeInt(chr.getId());
		mplew.writeInt(slot);
		mplew.writeMapleAsciiString(newname);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static Packet useChalkboard(final int charid, final String msg) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CHALKBOARD.getValue());
		packet.writeInt(charid);
		if (msg == null) {
			packet.write(0);
		} else {
			packet.write(1);
			packet.writeMapleAsciiString(msg);
		}
		return packet.getPacket();
	}

	public static Packet getTrockRefresh(MapleCharacter chr, byte vip, boolean delete) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
		packet.write(delete ? 2 : 3);
		packet.write(vip);
		chr.sendPacketTrock(packet, vip);

		return packet.getPacket();
	}

	public static Packet sendWishList(MapleCharacter chr, boolean update) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x0A); // 1.2.203+
		int[] list = chr.getWishlist();
		for (int i = 0; i < 12; i++) {
			packet.writeInt(list[i] != -1 ? list[i] : 0);
		}
		return packet.getPacket();
	}

	public static Packet showNXMapleTokens(MapleCharacter chr) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
		packet.writeInt(chr.getCSPoints(1)); // NXCash
		packet.writeInt(chr.getCSPoints(2)); // MPoint
		packet.writeInt(0); // MMileage

		return packet.getPacket();
	}

	public static Packet showBoughtCSItem(IItem item, int sn, int accid) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x0E); // 1.2.203+
		addCashItemInformation(packet, item, accid, sn);
		packet.write0(5); // 1.2.239+

		return packet.getPacket();
	}

	public static Packet showBoughtPendentSlot(int data) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x61); // 1.2.250+ (+2)
		packet.writeInt(data);
		packet.writeInt(0);

		return packet.getPacket();
	}

	public static Packet showBoughtCSPackages(List<Pair<Integer, CashItemInfo>> item, List<IItem> items, int accid) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x4A); // 1.2.250+ (+2)
		packet.write(item.size());
		int i = 0;
		for (Pair<Integer, CashItemInfo> p : item) {
			if (GameConstants.getInventoryType(p.getRight().getId()).equals(MapleInventoryType.EQUIP)) {
				Equip equip = (Equip) items.get(i);
				addCashItemInformation(packet, equip, accid, p.getLeft());
			} else {
				Item itemr = (Item) items.get(i);
				addCashItemInformation(packet, itemr, accid, p.getLeft());
			}
			i++;
		}
		packet.write0(7); // 1.2.239+

		return packet.getPacket();
	}

	public static Packet showCashInventory(MapleClient c) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x6);
		packet.write(0);
		packet.writeShort(c.getPlayer().getCashInventory().getInventory().size()); // CashInventory
																					// Size
		for (IItem item : c.getPlayer().getCashInventory().getInventory()) {
			addCashItemInformation(packet, item, c.getAccID(), item.getUniqueId());
		}
		if (c.getPlayer().getCashInventory().getInventory().size() > 0) {
			packet.writeInt(0);
		}
		packet.writeShort(c.getPlayer().getStorage().getSlots());
		packet.writeInt(c.getChrSlot()); // Character Slot.
		packet.writeShort(c.getChrSlot()); // Follwing Character Slot.

		return packet.getPacket();
	}

	public static Packet takeOutFromCashInventory(IItem item, short position) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x23); // 1.2.250 (+2)
		packet.write(1);
		packet.writeShort(position);
		PacketProvider.addItemInfo(packet, item, true, true, null);
		packet.write0(5); // 1.2.239+

		return packet.getPacket();
	}

	public static Packet putIntoCashInventory(IItem item, int accid) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x25); // 1.2.250+ (+2)
		addCashItemInformation(packet, item, accid, -1);

		return packet.getPacket();
	}

	public static void addCashItemInformation(WritingPacket packet, IItem item, int accid, int sn) {
		packet.writeLong(item.getUniqueId());
		packet.writeLong(accid);
		packet.writeInt(item.getItemId());
		packet.writeInt(sn);
		packet.writeShort(item.getQuantity());
		packet.writeAsciiString(item.getGiftFrom(), 13);
		packet.writeLong(PacketProvider.MAX_TIME);
		packet.write0(31); // 1.2.251+
		packet.writeLong(PacketProvider.getTime(-2));
		packet.write0(16);
	}

	public static Packet showBoughtCSQuestItem(short position, int itemid) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.writeInt(0x191); // 1.2.192+
		packet.write(0);
		packet.writeShort(1);
		packet.write(position);
		packet.write(0);
		packet.writeInt(itemid);

		return packet.getPacket();
	}

	public static Packet wrongCouponCode() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x54);
		packet.write(0xB9);

		return packet.getPacket();
	}

	public static Packet showCouponRedeemedItem(int itemid) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.writeShort(0x3E);
		packet.writeInt(0);
		packet.writeInt(1);
		packet.writeShort(1);
		packet.writeShort(0x1A);
		packet.writeInt(itemid);
		packet.writeInt(0);

		return packet.getPacket();
	}

	public static Packet readyToPurchase() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x5F);
		packet.writeInt(0);
		packet.writeInt(1);

		return packet.getPacket();
	}

	public static Packet sendPendont() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x1F);
		packet.writeShort(0);
		packet.writeShort(0x1E);

		return packet.getPacket();
	}

	public static Packet enableUse() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CHANNEL_SELECTED.getValue());
		packet.write(1);
		packet.writeInt(0);

		return packet.getPacket();
	}

	public static Packet showGifts() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x8);
		packet.writeShort(0);

		return packet.getPacket();
	}

	public static Packet enableUse3(MapleCharacter c) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x5);
		packet.writeInt(1);
		packet.write(0);

		return packet.getPacket();
	}

	public static Packet getCSCody() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_CODY.getValue());
		packet.writeInt(0);
		packet.writeInt(0);
		packet.writeInt(0);

		return packet.getPacket();
	}

	public static Packet payBackItem(int point, int id) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0x89); // 9A ¡æ 83
		packet.writeLong(id);
		packet.writeInt(point);
		packet.writeInt(0);

		return packet.getPacket();
	}

	public static Packet showRandomReward(int uniqueid, IItem item, short slot) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		packet.write(0xA7);
		packet.writeInt(uniqueid);
		packet.writeInt(0);
		packet.writeInt(0);
		PacketProvider.addItemInfo(packet, item, true, true, null);
		packet.writeShort(slot);
		packet.writeInt(0);

		return packet.getPacket();
	}

	public static Packet getOwlOpen() {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
		packet.write(9);
		packet.write(GameConstants.owlItems.length);
		for (int i : GameConstants.owlItems) {
			packet.writeInt(i); // these are the most searched items. too lazy
								// to actually make
		}
		return packet.getPacket();
	}

	public static Packet getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
		packet.write(8);
		packet.writeInt(0);
		packet.writeInt(itemSearch);
		int size = 0;

		for (HiredMerchant hm : hms) {
			size += hm.searchItem(itemSearch).size();
		}
		packet.writeInt(size);
		for (HiredMerchant hm : hms) {
			final List<MapleCharacterShopItem> items = hm.searchItem(itemSearch);
			for (MapleCharacterShopItem item : items) {
				packet.writeMapleAsciiString(hm.getOwnerName());
				packet.writeInt(hm.getMap().getId());
				packet.writeMapleAsciiString(hm.getDescription());
				packet.writeInt(item.item.getQuantity()); // I THINK.
				packet.writeInt(item.bundles); // I THINK.
				packet.writeInt(item.price);
				switch (InventoryHandler.OWL_ID) {
				case 0:
					packet.writeInt(hm.getOwnerId()); // store ID
					break;
				case 1:
					packet.writeInt(hm.getStoreId());
					break;
				default:
					packet.writeInt(hm.getObjectId());
					break;
				}
				packet.write(hm.getFreeSlot() == -1 ? 1 : 0);
				packet.write(GameConstants.getInventoryType(itemSearch).getType()); // position?
				if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
					PacketProvider.addItemInfo(packet, item.item, true, true, null);
				}
			}
		}
		return packet.getPacket();
	}

	public static Packet getOwlMessage(final int msg) {
		WritingPacket packet = new WritingPacket(3);
		packet.writeShort(SendPacketOpcode.OWL_RESULT.getValue());
		packet.write(msg);
		return packet.getPacket();
	}

	public static Packet pendantSlot(boolean p) {
		WritingPacket packet = new WritingPacket();
		packet.writeShort(SendPacketOpcode.PENDANT_SLOT.getValue());
		packet.write(p ? 1 : 0);
		return packet.getPacket();
	}
}