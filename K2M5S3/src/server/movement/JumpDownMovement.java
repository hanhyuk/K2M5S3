/*
 * ArcStory Project
 * √÷¡÷ø¯ sch2307@naver.com
 * ¿Ã¡ÿ junny_adm@naver.com
 * øÏ¡ˆ»∆ raccoonfox69@gmail.com
 * ∞≠¡§±‘ ku3135@nate.com
 * ±Ë¡¯»´ designer@inerve.kr
 */

package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;


public class JumpDownMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond;
    private Point offset;
    private int unk;
    private int fh;

    public JumpDownMovement(int type, Point position, int duration, int newstate) {
	super(type, position, duration, newstate);
    }

    public void setPixelsPerSecond(Point wobble) {
	this.pixelsPerSecond = wobble;
    }

    public void setOffset(Point wobble) {
	this.offset = wobble;
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
	packet.writePos(pixelsPerSecond);
	packet.writeShort(unk);
	packet.writeShort(fh);
	packet.writePos(offset);
	packet.write(getNewstate());
	packet.writeShort(getDuration());
        packet.write(0);
    }
}
