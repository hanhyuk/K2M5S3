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
import constants.subclasses.ServerType;
import packet.opcode.RecvPacketOpcode;
import packet.transfer.read.ByteStream;
import packet.transfer.read.ReadingMaple;
import tools.HexTool;

public class MapleDecoder extends CumulativeProtocolDecoder {
	private static final Logger logger = LoggerFactory.getLogger(MapleDecoder.class);
	
	final Lock fairLock = new ReentrantLock(true);
	private String clientKey;
	private ServerType type;
	
	public MapleDecoder(final ServerType type, final String clientKey) {
		this.type = type;
		this.clientKey = clientKey;
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out) throws Exception {
//		logger.debug("{} - doDecode START", type);
//		logger.debug("버퍼 패킷 길이 : {} , 버퍼 덤프 : {}", buffer.remaining(), buffer.getHexDump());

		final MapleClient client = (MapleClient) session.getAttribute(clientKey);
		int packetLength = -1;
		
		if (buffer.remaining() >= 4) {
//			logger.debug("헤더값 검증 시작.");
			
			buffer.mark();
			
			final int packetHeader = buffer.getInt();
			
			if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
				logger.error("RECV 패킷 헤더값 오류. 세션 종료!");
				session.closeNow();
				return false;
			}
			
			packetLength = MapleCrypto.getPacketLength(packetHeader);
//			logger.debug("헤더값 검증 종료.");
		} else {
			return false;
		}
		
//		logger.debug("초과 여부 : {}, 실제 패킷 길이 : {} , 버퍼 패킷 길이 : {}", packetLength > buffer.remaining(), packetLength, buffer.remaining());
		
		if (buffer.remaining() >= packetLength) {
//			logger.debug("패킷 복호화 시작");
			try {
				fairLock.lock();
				
				//decryptPacket(true, buffer, packetLength, out, client);
				final byte[] decryptedPacket = new byte[packetLength];
				buffer.get(decryptedPacket, 0, packetLength);
				
				client.getReceiveCrypto().crypt(decryptedPacket);
				out.write(decryptedPacket);
				
				if (logger.isDebugEnabled()) {
					final byte[] data = decryptedPacket;
					final ReadingMaple rh = new ReadingMaple(new ByteStream(data));
					final short header_num = rh.readShort();
					
					if (header_num != RecvPacketOpcode.MOVE_LIFE.getValue() && header_num != RecvPacketOpcode.MOVE_PLAYER.getValue()) {
						//logger.debug("RECV - [{}] {} \n {} ", RecvPacketOpcode.getOpcodeName(header_num), HexTool.toString(data), HexTool.toStringFromAscii(data));
						logger.debug("{} RECV - [{}] {}", type, RecvPacketOpcode.getOpcodeName(header_num), HexTool.toString(data));
					}
				}
			} finally {
				fairLock.unlock();
			}
//			logger.debug("패킷 복호화 종료");
			
			return true;
		} else {
//			logger.warn("client packet error start");
			buffer.reset();
//			logger.warn("client packet error end");
			return false;
		}
	}
}