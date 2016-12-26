package packet.crypto;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class EncryptionFactory implements ProtocolCodecFactory {

	private final ProtocolEncoder encoder = new MapleEncoder();
	private final ProtocolDecoder decoder = new MapleDecoder();

	@Override
	public ProtocolEncoder getEncoder(IoSession is) throws Exception {
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession is) throws Exception {
		return decoder;
	}
}
