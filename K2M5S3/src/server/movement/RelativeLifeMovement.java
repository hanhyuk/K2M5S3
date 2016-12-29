package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;

public class RelativeLifeMovement extends AbstractLifeMovement {

	private short fh;

	public RelativeLifeMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	public void setFh(short fh) {
		this.fh = fh;
	}

	@Override
	public void serialize(WritingPacket packet) {
		packet.write(getType());
		packet.writePos(getPosition());
		if (fh > 0) {
			packet.writeShort(fh);
		}
		packet.write(getNewstate());
		packet.writeShort(getDuration());
		packet.write(0);
	}
}
