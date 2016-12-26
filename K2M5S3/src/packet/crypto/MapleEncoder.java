package packet.crypto;

import java.util.concurrent.locks.Lock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import client.MapleClient;
import constants.ServerConstants;
import packet.transfer.write.Packet;
import tools.HexTool;

public class MapleEncoder implements ProtocolEncoder {

	@Override
	public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

		if (client != null) {
			final MapleCrypto send_crypto = client.getSendCrypto();
			final byte[] inputInitialPacket = ((Packet) message).getBytes();
			final byte[] unencrypted = new byte[inputInitialPacket.length];
			System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length);
			final byte[] ret = new byte[unencrypted.length + 4];
			if (ServerConstants.showPackets) {
				final StringBuilder sb = new StringBuilder("S : ");
				sb.append(HexTool.toString(((Packet) message).getBytes())).append("\n")
						.append(HexTool.toStringFromAscii(((Packet) message).getBytes())).append("\n\n");
				System.out.println(sb.toString());
			}
			final Lock mutex = client.getLock();
			mutex.lock();
			try {
				final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
				send_crypto.crypt(unencrypted);
				System.arraycopy(header, 0, ret, 0, 4);
			} finally {
				mutex.unlock();
			}
			System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
			out.write(IoBuffer.wrap(ret));
		} else {
			out.write(IoBuffer.wrap(((Packet) message).getBytes()));
		}
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		// nothing to do
	}
}
