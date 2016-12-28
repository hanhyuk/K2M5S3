package packet.crypto;

import java.util.concurrent.locks.Lock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import packet.opcode.SendPacketOpcode;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import tools.HexTool;

public class MapleEncoder implements ProtocolEncoder {
	private static final Logger logger = LoggerFactory.getLogger(MapleEncoder.class);
	
	@Override
	public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

		if (logger.isDebugEnabled()) {
			final byte[] data = ((Packet) message).getBytes();
			final ReadingMaple rh = new ReadingMaple(new ByteStream(data));
			final short header_num = rh.readShort();
			logger.debug("SEND - [{}] {} \n {} ", SendPacketOpcode.getOpcodeName(header_num), HexTool.toString(data), HexTool.toStringFromAscii(data));
		}
		
		if (client != null) {
			final MapleCrypto send_crypto = client.getSendCrypto();
			final byte[] inputInitialPacket = ((Packet) message).getBytes();
			final byte[] unencrypted = new byte[inputInitialPacket.length];
			System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length);
			final byte[] ret = new byte[unencrypted.length + 4];
			
			final Lock mutex = client.getEncodeLock();
			try {
				mutex.lock();
				
				final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
				send_crypto.crypt(unencrypted);
				System.arraycopy(header, 0, ret, 0, 4);
				System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
				out.write(IoBuffer.wrap(ret));
			} finally {
				mutex.unlock();
			}
		} else {
			out.write(IoBuffer.wrap(((Packet) message).getBytes()));
		}
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		// nothing to do
	}
}
