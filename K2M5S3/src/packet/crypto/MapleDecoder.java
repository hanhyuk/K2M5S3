package packet.crypto;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import packet.opcode.RecvPacketOpcode;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import tools.HexTool;

public class MapleDecoder extends CumulativeProtocolDecoder {
	private static final Logger logger = LoggerFactory.getLogger(MapleDecoder.class);
	
	final Lock fairLock = new ReentrantLock(true);
	private String clientKey;
	
	public MapleDecoder(String clientKey) {
		this.clientKey = clientKey;
	}
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(clientKey);

		int packetLength = -1;
		
		if (buffer.remaining() >= 4) {
			final int packetHeader = buffer.getInt();
			if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
				logger.warn("recv packet 헤더 검증 실패! 계정 : {} 캐릭명 : {}", client.getAccountName(), client.getPlayer().getName());
				session.closeNow();
				return false;
			}
			packetLength = MapleCrypto.getPacketLength(packetHeader);
		} else {
			return false;
		}
		
		if (buffer.remaining() >= packetLength) {
			try {
				fairLock.lock();
				
				final byte[] decryptedPacket = new byte[packetLength];
				buffer.get(decryptedPacket, 0, packetLength);
				
				client.getReceiveCrypto().crypt(decryptedPacket);
				out.write(decryptedPacket);
				
				if (logger.isDebugEnabled()) {
					final byte[] data = decryptedPacket;
					final ReadingMaple rh = new ReadingMaple(new ByteStream(data));
					final short header_num = rh.readShort();
					
					if (header_num != RecvPacketOpcode.MOVE_LIFE.getValue() && header_num != RecvPacketOpcode.MOVE_PLAYER.getValue()) {
						logger.debug("RECV - [{}] {} \n {} ", RecvPacketOpcode.getOpcodeName(header_num), HexTool.toString(data), HexTool.toStringFromAscii(data));
					}
				}
			} finally {
				fairLock.unlock();
			}
			return true;
		}
		return false;
	}
}
