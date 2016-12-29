package server.movement;

import packet.transfer.write.WritingPacket;
import java.awt.Point;

public interface LifeMovementFragment {
	void serialize(WritingPacket packet);

	Point getPosition();
}
