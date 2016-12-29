package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;

public class TunknownMovement extends AbstractLifeMovement {

	private Point offset;

	public TunknownMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	public void setOffset(Point wobble) {
		this.offset = wobble;
	}

	@Override
	public void serialize(WritingPacket packet) {
		packet.write(getType());
		packet.writePos(getPosition());
		packet.writePos(offset);
		packet.write(getNewstate());
		packet.writeShort(getDuration());
		packet.write(0);
	}
}
