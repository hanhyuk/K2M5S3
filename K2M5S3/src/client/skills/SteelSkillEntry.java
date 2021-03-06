package client.skills;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteelSkillEntry implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(SteelSkillEntry.class);
	
    private int skillid, slot, skilllevel;
    private boolean equipped = false;

	public SteelSkillEntry(int skillid, int skilllevel) {
        this.skillid = skillid;
        this.skilllevel = skilllevel;
    }
    
    public void setSlot(int slot) {
        if (slot < 1 && slot > 5) {
            throw new RuntimeException("[오류] 잘못된 슬롯이 설정되었습니다.");
        }
        this.slot = slot;
    }
    
    public void setEquipped ( boolean a) {
        this.equipped = a;
    }
    
    public boolean isEquipped() {
        return equipped;
    }

    public int getSkillId() {
        return skillid;
    }

    public int getSlot() {
        return slot;
    }
    
    public int getSlot(int index) {
        return slot;
    }
    
    public int getSkillLevel() {
        return skilllevel;
    }
    
    public static int getJobIndex(int skillid) {
        int jobid = skillid / 10000;
        if (jobid % 100 == 0) {
            return 1;
        } else if (jobid % 10 == 2) {
            return 4;
        } else if (jobid % 10 == 3) {
            return 5;
        } else if (jobid % 10 == 1) {
            return 3;
        } else if (jobid % 10 == 0) {
            return 2;
        }
        logger.debug("[오류] 스틸 스킬 정보 직업계산 실패. 스킬아이디 : {}", skillid);
        return 0;
    }
    
    public static int getJobIndexB(int baseSkillId) {
        switch (baseSkillId) {
            case 24001001:
                return 1;
            case 24101001:
                return 2;
            case 24111001:
                return 3;
            case 24121001:
                return 4;
            case 24121054:
                return 5;
        }
        logger.debug("[오류] 스틸 스킬 정보 직업계산 실패. 베이스 스킬아이디 : {}", baseSkillId);
        return 0;
    }
}