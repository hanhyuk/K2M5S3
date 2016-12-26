package packet.crypto;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import client.MapleClient;

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
			final byte decryptedPacket[] = new byte[decoderState.packetlength];
			in.get(decryptedPacket, 0, decoderState.packetlength);
			decoderState.packetlength = -1;
			client.getReceiveCrypto().crypt(decryptedPacket);
			out.write(decryptedPacket);
			return true;
		}
		return false;
	}
}
