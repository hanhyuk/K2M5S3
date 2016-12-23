/*
 * ArcStory Project
 * ÃÖÁÖ¿ø sch2307@naver.com
 * ÀÌÁØ junny_adm@naver.com
 * ¿ìÁöÈÆ raccoonfox69@gmail.com
 * °­Á¤±Ô ku3135@nate.com
 * ±èÁøÈ« designer@inerve.kr
 */

package server.movement;

import java.awt.Point;

public abstract class AbstractLifeMovement implements LifeMovement {

    private final Point position;
    private final int duration;
    private final int newstate;
    private final int type;

    public AbstractLifeMovement(int type, Point position, int duration, int newstate) {
	super();
	this.type = type;
	this.position = position;
	this.duration = duration;
	this.newstate = newstate;
    }

    @Override
    public int getType() {
	return this.type;
    }

    @Override
    public int getDuration() {
	return duration;
    }

    @Override
    public int getNewstate() {
	return newstate;
    }

    @Override
    public Point getPosition() {
	return position;
    }
}
