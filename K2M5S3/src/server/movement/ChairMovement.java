/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;

public class ChairMovement extends AbstractLifeMovement {

    private int unk;

    public ChairMovement(int type, Point position, int duration, int newstate) {
	super(type, position, duration, newstate);
    }

    public void setUnk(int unk) {
	this.unk = unk;
    }

    @Override
    public void serialize(WritingPacket packet) {
	packet.write(getType());
	packet.writePos(getPosition());
	packet.writeShort(unk);
	packet.write(getNewstate());
	packet.writeShort(getDuration());
        packet.write(0);
    }
}

