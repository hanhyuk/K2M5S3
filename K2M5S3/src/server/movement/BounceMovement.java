package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;

public class BounceMovement extends AbstractLifeMovement {

	private int fh, unk;

	public BounceMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	public void setUnk(int unk) {
		this.unk = unk;
	}

	public void setFH(int fh) {
		this.fh = fh;
	}

	@Override
	public void serialize(WritingPacket packet) {
		packet.write(getType());
		packet.writePos(getPosition());
		packet.writeShort(unk);
		packet.writeShort(fh);
		packet.write(getNewstate());
		packet.writeShort(getDuration());
		packet.write(0);
	}
}
