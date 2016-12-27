package packet.crypto;

import java.util.concurrent.locks.Lock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import client.MapleClient;
import constants.ServerConstants;
import packet.opcode.RecvPacketOpcode;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import tools.HexTool;

public class MapleDecoder extends CumulativeProtocolDecoder {

	public static final String DECODER_STATE_KEY = MapleDecoder.class.getName() + ".STATE";

	public static class DecoderState {
		public int packetlength = -1;
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		final DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
		
		if (decoderState.packetlength == -1) {
			if (in.remaining() >= 4) {
				final int packetHeader = in.getInt();
				if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
					session.closeNow();
					return false;
				}
				decoderState.packetlength = MapleCrypto.getPacketLength(packetHeader);
			} else {
				return false;
			}
		}
		if (in.remaining() >= decoderState.packetlength) {
			final Lock mutex = client.getDecodeLock();
			try {
				mutex.lock();
				
				final byte decryptedPacket[] = new byte[decoderState.packetlength];
				in.get(decryptedPacket, 0, decoderState.packetlength);
				decoderState.packetlength = -1;
				client.getReceiveCrypto().crypt(decryptedPacket);
				out.write(decryptedPacket);
				
				if (ServerConstants.showPackets) {
					final byte[] data = decryptedPacket;
					final ReadingMaple rh = new ReadingMaple(new ByteStream(data));
					final short header_num = rh.readShort();
					
					if (header_num != RecvPacketOpcode.MOVE_LIFE.getValue() && header_num != RecvPacketOpcode.MOVE_PLAYER.getValue()) {
						final StringBuilder sb = new StringBuilder("RECV - [" + RecvPacketOpcode.getOpcodeName(header_num) + "] : ");
						sb.append(HexTool.toString(data)).append("\n")
								.append(HexTool.toStringFromAscii(data)).append("\n\n");
						System.out.println(sb.toString());
					}
				}
				
				
			} finally {
				mutex.unlock();
			}
			return true;
		}
		return false;
	}
}
