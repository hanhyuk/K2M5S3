package server.maps;

import java.awt.Point;

public abstract class AbstractHinaMapObject implements MapleMapObject {
	private Point position = new Point();
	private int objectId;

	@Override
	public abstract MapleMapObjectType getType();

	@Override
	public Point getPosition() {
		return new Point(position);
	}

	@Override
	public void setPosition(Point position) {
		this.position.x = position.x;
		this.position.y = position.y;
	}

	@Override
	public int getObjectId() {
		return objectId;
	}

	@Override
	public void setObjectId(int id) {
		this.objectId = id;
	}

	public Point getTruePosition() {
		return position;
	}
}
