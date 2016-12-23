/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package handler.channel;

import packet.transfer.read.ReadingMaple;
import server.maps.AnimatedHinaMapObject;
import server.movement.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class MovementParse {

    public static final List<LifeMovementFragment> parseMovement(final ReadingMaple rh) {
        final List<LifeMovementFragment> res = new ArrayList<LifeMovementFragment>();
        final byte numCommands = rh.readByte();
        for (byte i = 0; i < numCommands; i++) {
            final byte command = rh.readByte();
            short fh = 0;
            switch (command) {
                case 0: 
                case 8: 
                case 15:
                case 17:
                case 19:
                case 65:
                case 66:
                case 67: {
                    final short xpos = rh.readShort();
                    final short ypos = rh.readShort();
                    final short xwobble = rh.readShort();
                    final short ywobble = rh.readShort();
                    final short unk = rh.readShort();
                    if (command == 15 || command == 17) {
                        fh = rh.readShort();
                    }
                    final short xoffset = rh.readShort();
                    final short yoffset = rh.readShort();
                    final byte newstate = rh.readByte();
                    final short duration = rh.readShort();
                    rh.skip(1);
                    final AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setFh(fh);
                    alm.setUnk(unk);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    alm.setOffset(new Point(xoffset, yoffset));
                    res.add(alm);
                    break;
                }
                case 1: 
                case 2: 
                case 18:
                case 21:
                case 22:
                case 24:
                case 60:
                case 61:
                case 62:
                case 63: {
                    final short xmod = rh.readShort();
                    final short ymod = rh.readShort();
                    if (command == 21 || command == 22) {
                        fh = rh.readShort();
                    }
                    final byte newstate = rh.readByte();
                    final short duration = rh.readShort();
                    rh.skip(1);
                    final RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);
                    rlm.setFh(fh);
                    res.add(rlm);
                    break;
                }
                case 3:
                case 4:
                case 6:
                case 7:
                case 9:
                case 10:
                case 11:
                case 13:
                case 26:
                case 27:
                case 51: 
                case 52: 
                case 53: 
                case 74: 
                case 75: 
                case 76: 
                case 78: 
                case 80: {
                    final short xpos = rh.readShort();
                    final short ypos = rh.readShort();
                    final short unk = rh.readShort();
                    final byte newstate = rh.readByte();
                    final short duration = rh.readShort();
                    rh.skip(1);
                    final ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setUnk(unk);
                    res.add(cm);
                    break;
                }          
                case 55:
                case 64: {
                    final short xpos = rh.readShort();
                    final short ypos = rh.readShort();
                    final short xwobble = rh.readShort();
                    final short ywobble = rh.readShort();
                    final short unk = rh.readShort();
                    final byte newstate = rh.readByte();
                    final short duration = rh.readShort();
                    rh.skip(1);
                    final UnknownMovement um = new UnknownMovement(command, new Point(xpos, ypos), duration, newstate);
                    um.setUnk(unk);
                    um.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(um);
                    break;
                }
                case 14:
                case 16: {
                    final short xpos = rh.readShort();
                    final short ypos = rh.readShort();
                    final short unk = rh.readShort();
                    final byte newstate = rh.readByte();
                    final short duration = rh.readShort();
                    rh.skip(1);
                    final SunknownMovement sum = new SunknownMovement(command, new Point(xpos, ypos), duration, newstate);
                    sum.setUnk(unk);
                    res.add(sum);
                    break;
                }
                case 23: {
                    final short xpos = rh.readShort();
                    final short ypos = rh.readShort();
                    final short xoffset = rh.readShort();
                    final short yoffset = rh.readShort();
                    final byte newstate = rh.readByte();
                    final short duration = rh.readShort();
                    rh.skip(1);
                    final TunknownMovement tum = new TunknownMovement(command, new Point(xpos, ypos), duration, newstate);
                    tum.setOffset(new Point(xoffset, yoffset));
                    res.add(tum);
                    break;
                }
                case 12: {
                    res.add(new ChangeEquipSpecialAwesome(command, rh.readByte()));
                    break;
                }
                default: {
                    if (command != 71 && command != 73) { 
                        final byte newstate = rh.readByte();
                        final short duration = rh.readShort();
                        rh.skip(1);
                        final AranMovement am = new AranMovement(command, new Point(0, 0), duration, newstate);
                        res.add(am);
                        break;   
                    }
                }
            }
        }
        if (numCommands != res.size()) {
            return null; // Probably hack
        }
        return res;
    }

    public static final void updatePosition(final List<LifeMovementFragment> movement, final AnimatedHinaMapObject target, final int yoffset) {
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}