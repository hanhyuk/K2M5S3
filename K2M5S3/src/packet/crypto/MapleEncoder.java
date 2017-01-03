package packet.crypto;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import constants.subclasses.ServerType;
import packet.opcode.SendPacketOpcode;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import tools.HexTool;

public class MapleEncoder implements ProtocolEncoder {
	private static final Logger logger = LoggerFactory.getLogger(MapleEncoder.class);
	
	private final Lock fairLock = new ReentrantLock(true);
	private String clientKey;
	private ServerType type;
	
	public MapleEncoder(final ServerType type, final String clientKey) {
		this.type = type;
		this.clientKey = clientKey;
	}
	
	@Override
	public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(clientKey);

		if (logger.isDebugEnabled()) {
			final byte[] data = ((Packet) message).getBytes();
			final ReadingMaple rh = new ReadingMaple(new ByteStream(data));
			final short header_num = rh.readShort();
			logger.debug("{} SEND - [{}] {} \n {} ", type, SendPacketOpcode.getOpcodeName(header_num), HexTool.toString(data), HexTool.toStringFromAscii(data));
		}
		
		if (client != null) {
			final MapleCrypto send_crypto = client.getSendCrypto();
			final byte[] inputInitialPacket = ((Packet) message).getBytes();
			final byte[] unencrypted = new byte[inputInitialPacket.length];
			System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length);
			final byte[] ret = new byte[unencrypted.length + 4];
			
			try {
				fairLock.lock();
				
				final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
				send_crypto.crypt(unencrypted);
				System.arraycopy(header, 0, ret, 0, 4);
				System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
				out.write(IoBuffer.wrap(ret));
			} finally {
				fairLock.unlock();
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
