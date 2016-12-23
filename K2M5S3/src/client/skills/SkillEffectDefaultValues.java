/*
 * ArcStory Project
 * ÃÖÁÖ¿ø sch2307@naver.com
 * ÀÌÁØ junny_adm@naver.com
 * ¿ìÁöÈÆ raccoonfox69@gmail.com
 * °­Á¤±Ô ku3135@nate.com
 * ±èÁøÈ« designer@inerve.kr
 */

package client.skills;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author T-Sun
 * 
 *   This file was written by T-Sun (doomgate17@naver.com)
 *
 *
 *
 */
public class SkillEffectDefaultValues {
    private final static Map<String, Integer> defvalues = new HashMap<String, Integer>();

    static {
        defvalues.put("time", -1);
        defvalues.put("damage", 100);
        defvalues.put("attackCount", 1);
        defvalues.put("bulletCount", 1);
        defvalues.put("moveTo", -1);
        defvalues.put("prop", 100);
        defvalues.put("mobCount", 1);
        defvalues.put("slotCount", 0);
        defvalues.put("type", 0);
        defvalues.put("onActive", -1); 
    }
    
    public static int getDef(String key) {
        if (defvalues.containsKey(key)) {
            return defvalues.get(key);
        }
        return 0;
    }
}
