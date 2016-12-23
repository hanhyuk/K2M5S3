/*
 * ArcStory Project
 * ÃÖÁÖ¿ø sch2307@naver.com
 * ÀÌÁØ junny_adm@naver.com
 * ¿ìÁöÈÆ raccoonfox69@gmail.com
 * °­Á¤±Ô ku3135@nate.com
 * ±èÁøÈ« designer@inerve.kr
 */

package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;


public class AbsoluteLifeMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond, offset;
    private int unk, fh;

    public AbsoluteLifeMovement(int type, Point position, int duration, int newstate) {
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
	packet.writePos(offset);
	packet.write(getNewstate());
	packet.writeShort(getDuration());
        packet.write(0);
    }
}
