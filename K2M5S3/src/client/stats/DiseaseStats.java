/*
 * ArcStory Project
 * √÷¡÷ø¯ sch2307@naver.com
 * ¿Ã¡ÿ junny_adm@naver.com
 * øÏ¡ˆ»∆ raccoonfox69@gmail.com
 * ∞≠¡§±‘ ku3135@nate.com
 * ±Ë¡¯»´ designer@inerve.kr
 */

package client.stats;

public enum DiseaseStats implements GlobalBuffStat {
    
    POISON      (0x0, 0), //1.2.239 Test.
    NULL        (0x0, 0), //1.2.241 Ok.
    SEAL        (0x1000000, 2), //1.2.252 Ok.
    DARKNESS    (0x800000, 2), //1.2.252 Test.
    WEAKEN      (0x4000, 2), //1.2.252 Test.
    CURSE       (0x2000, 2), //1.2.252 Ok.
    SLOW        (0x1000, 2), //1.2.252 Test.
    SEDUCE      (0x20, 2), //.2.252 Ok.
    POTION      (0x0, 0), //1.2.241 Ok.
    TRANSFORM   (0x0, 0), //1.2.241 Ok.
    BLIND       (0x0, 0), //1.2.241 Ok.
    TELEPORT    (0x0, 3), //1.2.241 Ok.
    STUN        (0x0, 0), //1.2.239 Test
    
    ZOMBIFY     (0x0, 4), //1.2.239 Test.
    REVERSE_DIRECTION       (0x0, 0), //1.2.239 Test.
    SHADOW      (0x0, 3), //1.2.239 Test.
    FREEZE      (0x0, 3), //1.2.239 Test
    DISABLE_POTENTIAL   (0x0, 3), //1.2.239 Test.
    TORNADO             (0x0, 3); //1.2.239 Test.
        
    private int i;
    private byte index;

    private DiseaseStats(int i) {
        this.i = i;
        this.index = 1;
    }

    private DiseaseStats(int i, int ii) {
        this.i = i;
        this.index = (byte) ii;
    }

    public boolean isFirst() {
        return index == 1;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getValue() {
        return i;
    }
}
