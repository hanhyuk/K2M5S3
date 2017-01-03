package packet.crypto;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import constants.subclasses.ServerType;

public class EncryptionFactory implements ProtocolCodecFactory {

	private final ProtocolEncoder encoder;
	private final ProtocolDecoder decoder;

	public EncryptionFactory(final ServerType type, final String clientKey) {
		this.encoder = new MapleEncoder(type, clientKey); 
		this.decoder = new MapleDecoder(type, clientKey);
	}
	
	@Override
	public ProtocolEncoder getEncoder(IoSession is) throws Exception {
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession is) throws Exception {
		return decoder;
	}
}
