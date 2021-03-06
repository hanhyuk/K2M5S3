package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;

public class UnknownMovement extends AbstractLifeMovement {

	private Point pixelsPerSecond;
	private int unk, fh;

	public UnknownMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	public Point getPixelsPerSecond() {
		return pixelsPerSecond;
	}

	public void setPixelsPerSecond(Point wobble) {
		this.pixelsPerSecond = wobble;
	}

	public void setUnk(int unk) {
		this.unk = unk;
	}

	public void setFh(short fh) {
		this.fh = fh;
	}

	@Override
	public void serialize(WritingPacket packet) {
		packet.write(getType());
		packet.writePos(getPosition());
		packet.writePos(pixelsPerSecond);
		if (fh > 0) {
			packet.writeShort(fh);
		}
		packet.writeShort(unk);
		packet.write(getNewstate());
		packet.writeShort(getDuration());
		packet.write(0);
	}
}