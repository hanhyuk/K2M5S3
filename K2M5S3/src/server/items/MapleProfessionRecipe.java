/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package server.items;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 에반테이르
 */
public class MapleProfessionRecipe {
    public Map<Integer, MapleProfessionRecipeEntry> recipes = new HashMap<Integer, MapleProfessionRecipeEntry>();
    private static MapleProfessionRecipe instance = null;
    
    public static MapleProfessionRecipe getInstance() {
        if (instance == null) {
            instance = new MapleProfessionRecipe();
        }
        return instance;
    }
    
    public MapleProfessionRecipeEntry getRecipe(int skillid) {
        return recipes.get(skillid);
    }
}
