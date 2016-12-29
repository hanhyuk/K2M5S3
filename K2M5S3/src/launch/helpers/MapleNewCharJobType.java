package launch.helpers;

public enum MapleNewCharJobType {
    레지스탕스((byte) 0),
    모험가((byte) 1),
    시그너스((byte) 2),
    아란((byte) 3),
    에반((byte) 4),
    메르세데스((byte) 5),
    데몬슬레이어((byte) 6),
    팬텀((byte) 7),
    듀얼블레이더((byte) 8),
    미하일((byte) 9),
    루미너스((byte) 10),
    카이저((byte) 11),
    엔젤릭버스터((byte) 12),
    캐논슈터((byte) 13),
    제논((byte) 14),
    제로((byte) 15),
    은월((byte) 16),
    핑크빈((byte) 17),
    키네시스((byte) 18);
    private byte jobCode = -1;
    
    private MapleNewCharJobType(byte type) {
        this.jobCode = type;
    }
    
    public byte getValue() {
        return jobCode;
    }
    
    
}
