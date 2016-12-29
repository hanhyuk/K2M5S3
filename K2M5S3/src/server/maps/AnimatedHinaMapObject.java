package server.maps;

public interface AnimatedHinaMapObject extends MapleMapObject {
    int getStance();
    void setStance(int stance);
    boolean isFacingLeft();
}
