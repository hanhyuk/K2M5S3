/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.stats;

public enum BuffStats implements GlobalBuffStat {
    
    DISEASE_TOLERANCE   (0x1, 0), //1.2.251 Ok.
    STATUS_RESIST2      (0x2, 0), //1.2.251 Ok.
    MAX_DAMAGE          (0x4, 0), //1.2.251 Ok.
    //중간에 0x8, 0x10 비었음.
    PERCENT_DAMAGE_BUFF (0x20, 0), //1.2.251 Ok.
    MANA_REFLECTION     (0x40, 0), //1.2.251 Ok.
    BUFF_MASTERY        (0x2000, 0), //NONE OK.
    BOOSTER_R           (0x10000, 0), //1.2.251 Ok.
    RUNE_EXPRATE        (0x20000, 0), //1.2.251 Ok.
    ARIANT_COSS_IMU     (0x40000, 0), //1.2.252 Ok.
    STACK_ALLSTAT       (0x80000, 0), //1.2.251 Ok.
    STACK_SPEED         (0x100000, 0), //1.2.251 Ok.
    STACK_JUMP          (0x200000, 0), //1.2.251 Ok.
    STACK_AVOID         (0x400000, 0), //1.2.251 Ok.
    STACK_ACC           (0x800000, 0), //1.2.251 Ok.
    MAXMP_PHANTOM_R     (0x1000000, 0), //1.2.251 Ok.
    STACK_MP            (0x2000000, 0), //1.2.251 Ok.
    MAXHP_PHANTOM_R     (0x4000000, 0), //1.2.251 Ok.
    STACK_HP            (0x8000000, 0), //1.2.251 Ok. 
    STACK_MDEF          (0x10000000, 0), //1.2.251 Ok.
    STACK_WDEF          (0x20000000, 0), //1.2.251 Ok.
    STACK_MATK          (0x40000000, 0), //1.2.251 Ok.
    STACK_WATK          (0x80000000, 0), //1.2.251 Ok.

    SOUL_WATK           (0x8000000, 2), //1.2.251 Ok.
    MAGIC_GUARD_DAMAGER (0x10000000, 2), //1.2.251 Ok.
    MAXHP               (0x20000000, 2), //1.2.251 Ok.
    MAXMP               (0x40000000, 2), //1.2.251 Ok.
    ACC                 (0x80000000, 2), //1.2.251 Ok.
    BOOSTER             (0x1, 1), //1.2.251 Ok.
    DARKSIGHT           (0x2, 1), //1.2.251 Ok.
    MAGIC_GUARD         (0x4, 1), //1.2.251 Ok.    
    JUMP                (0x8, 1), //1.2.251 Ok.
    
    AVOID               (0x40, 1), //1.2.251 Ok.
    //0x20,0x80 비었음.
    SPEED               (0x10, 1), //1.2.251 Ok.
    WDEF                (0x400, 1), //1.2.251 Ok.
    MATK                (0x100, 1), //1.2.251 Ok.
    MDEF                (0x200, 1), //1.2.252 Ok.
    WATK                (0x800000, 1), //1.2.251 Ok.
    KINESIS_MATK        (0x400000, 1), //1.2.251 Ok.
    FIG_DAMAGE_BUFF     (0x20000, 1), //1.2.251 Ok.
    MOB_PDPR            (0x2000000, 1), //1.2.251 Ok.
    BOSS_DAMAGE         (0x10000000, 1), //1.2.251 Ok.
    //중간에 0x20000000 비었음.
    PARASHOCK_DAM_CR    (0x40000000, 1), //1.2.251 Ok.
    CR_PERCENT          (0x80000000, 1), //1.2.251 Ok.
    
    
    ECHO_OF_HERO        (0x20000000, 3), //1.2.251 Ok.
    //중간에 0x400000000  비었음.
    S_SOULARROW         (0x80000000, 3), //1.2.251 Ok.
    
    //중간에 0x1 비었음
    ILLUSION_STEP_EVA   (0x2, 2), //1.2.251 Ok.
    ADVANCED_BLESSING   (0x4, 2), //1.2.251 Ok.
    INFINITY            (0x8, 2), //1.2.251 Ok.
    SPIRIT_CLAW         (0x10, 2), //1.2.251 Ok.
    //중간에 0x40 비었음.
    SHARP_EYES          (0x80, 2), //1.2.251 Ok.
    STANCE              (0x100, 2), //1.2.251 Ok.
    MAPLE_WARRIOR       (0x200, 2), //1.2.251 Ok.
    HP_RECOVERY         (0x400, 2), //1.2.252 Ok.
    MORPH               (0x800, 2), //1.2.251 Ok.
    MESOGUARD           (0x10000, 2), //1.2.251 Ok.
    PICKPOCKET          (0x20000, 2), //1.2.251 Ok.
    SHADOW_PARTNER      (0x40000, 2), //1.2.251 Ok.
    HOLY_SYMBOL         (0x100000, 2), //1.2.251 Ok.
    WK_CHARGE           (0x200000, 2), //1.2.251 Ok.
    COMBO               (0x400000, 2), //1.2.251 Ok    
    SOUL_STONE          (0x40000000, 2), //1.2.252 Ok.
    MAGIC_RESISTANCE    (0x80000000, 2), //1.2.252 Ok.
    PARTY_DAMAGE        (0x2, 3), //1.2.242 Ok.
    MECHANIC_CAMOUFLAGE (0x4, 3), //1.2.252 Ok.
    ITEM_EXPRATE        (0x40, 3), //1.2.252 Ok.
    ITEM_EFFECT         (0x80, 3), //1.2.252 Ok.
    BODY_PRESSURE       (0x100, 3), //1.2.251 Ok.
    //중간에 0x200 비었음.
    AURA_RECOVERY       (0x400, 3), //1.2.251 Ok.
    ARAN_COMBO          (0x800, 3), //1.2.251 Ok.
    //중간에 0x1000, 0x2000 비었음
    ELEMENT_RESET       (0x4000, 3), //1.2.251 Ok.
        
    BEHOLDER            (0x20000000, 5), //1.2.251 Ok.
    ANSIENT_SPIRIT      (0x1, 4), //1.2.251 Ok.
    DAMAGE_R_PHANTOM    (0x100000, 4), //1.2.251 Ok.
    COMBAT_ORDERS       (0x40000000, 5), //1.2.251 Ok.
    TELEPORT_MASTERY    (0x80000000, 5), //1.2.251 Ok.
    BLESSING_ARMOR      (0x2, 4), //1.2.251 Ok.
    ROLL_OF_DICE        (0x4, 4), //1.2.251 Ok.
    RESISTANCE_HIDE     (0x20, 4), //1.2.251 Ok.
    HOWLING_DMG_DEC     (0x200, 4), //1.2.251 Ok.
    HOWLING_AVOID       (0x400, 4), //1.2.251 Ok.
    HOWLING_MAXMP       (0x800, 4), //1.2.251 Ok.
    HOWLING_CRITICAL    (0x1000, 4), //1.2.251 Ok.
    //중간에 0x2000 비었음.
    PERFECT_ARMOR       (0x4000, 4), //1.2.242 Ok.
    ENHANCED_MDEF       (0x8000, 4), //1.2.251 Ok.
    ENHANCED_WDEF       (0x10000, 4), //1.2.251 Ok.
    ENHANCED_MATK       (0x20000, 4), //1.2.251 Ok.
    ENHANCED_WATK       (0x40000, 4), //1.2.251 Ok.
    ENHANCED_MAXHP      (0x2000000, 4), //1.2.251 Ok.
    BISTFORM_DAMAGE     (0x400000, 4), //1.2.251 Ok.
    HOWLING_PARTY       (0x800000, 4), //1.2.251 Ok.
    FINAL_DAMAGE        (0x1000000, 4), //1.2.252 Ok.
    IGNORE_BUFFNON      (0x2000000, 4), //1.2.251 Ok.
    DRAW_BACK           (0x4000000, 4), //1.2.251 Ok.
    ENRAGE              (0x8000000, 4), //1.2.251 Ok.
    //중간에 0x10000000 비었음.
    SKILL_FULLCHARGE    (0x20000000, 6), //1.2.251 Ok.
    HOLY_SHELL          (0x40000000, 6), //1.2.242 Ok.
    //중간에 0x80000000 비었음.
    INFINITY_FORCE      (0x2, 5), //1.2.251 Ok.
    DEX_UP              (0x8000, 5), //1.2.251 Ok.
    BLESS               (0x1000000, 5), //1.2.251 Ok.
    //중간에 0x2000000, 0x4000000 비었음.
    INFLATION           (0x8000000, 5), //1.2.251 Ok.
    
    
    MASTER_MAGIC        (0x10, 2),  //1.2.252 Ok.
    DEFENCE_R           (0x200, 6), //1.2.251 Ok.
    SPIRIT_LINK         (0x400, 6), //1.2.251 Ok.
    SAINT_SAVER         (0x2000, 6), //1.2.252 Ok.
    CRIT_INC            (0x40000, 6), //1.2.251 Ok.
    DMG_INC             (0x200000, 6), //1.2.252 Ok.
    OAK_ROULETTE        (0x400000, 6), //1.2.251 Ok.
    DARK_SPECULATION    (0x800000, 6), //1.2.251 Ok.
    DAMAGE_RESIST       (0x1000000, 6), //1.2.251 Ok.
    ELEMENT_RESIST      (0x2000000, 6), //1.2.251 Ok.
    STATUS_RESIST       (0x4000000, 6), //1.2.251 Ok.
    ARCANE_AIM          (0x10000000, 6), //1.2.251 Ok.
    REVERSE_DAMAGE      (0x20000000, 8), //1.2.251 Ok.
    //중간에 0x40000000 비었음.
    S_SHARP_EYES        (0x80000000, 8), //1.2.251 Ok.
    S_DAMAGE_RESIST     (0x10, 8),
    WILL_OF_SWORD       (0x1, 7), //1.2.251 Ok.
    //중간에 0x2, 0x4 비었음.
    RESHUFFLE_MODE      (0x8, 7), //1.2.251 Ok.
    ROBURST_ARMOR       (0x10, 7), //1.2.251 Ok.
    MORPH_GAUGE         (0x20, 7), //1.2.251 Ok.
    STR_UP              (0x40, 7), //1.2.2521 Ok.
    LIFE_TIDAL          (0x80, 7), //1.2.251 Ok.
    TIME                (0x100, 7), //1.2.251 Ok.
    BLESS_OF_DARKNESS   (0x200, 7), //1.2.251 Ok.
    DARK_CRESSENDOR     (0x400, 7), //1.2.251 Ok.
    LUMINOUS_GAUGE      (0x800, 7), //1.2.251 Ok.
    VOYD_PRESSURE       (0x1000, 7), //1.2.251 Ok.
    BLESSING_ARMOR_WATK (0x2000, 7), //1.2.251 Ok.
    //중간에 0x4000, 0x8000, 0x10000, 0x20000, 0x40000, 0x80000 비었음.
    MIN_CRITICAL_DAMAGE (0x100000, 7), //1.2.251 Ok
    CRITICAL_RATE       (0x200000, 7), //1.2.251 Ok.
    PHANTOM_SHROUD      (0x400000, 7), //1.2.251 Ok
    LUCK_PHANTOM_THIEF  (0x800000, 7), //1.2.251 Ok.
    IGNORE_DEFENCE_R    (0x1000000, 7), //1.2.251 Ok.

    AFFINITY            (0x0, 1), //None Ok.
    SUCCESS             (0x0, 2), //None Ok.
    
    FINAL_ATTACK_BUFF   (0x1000000, 9), //1.2.251 Ok.
    BOSS_ATTACK         (0x10000000, 9), //1.2.251 Ok. 
    
    TIME_REWIND         (0x800000, 7), //1.2.251 Ok.
    SPIRIT              (0x1000000, 7), //1.2.251 Ok.
    //중간에 0x4000000 비었음.
    KILLING_POINT       (0x8000000, 7), //1.2.251 Ok.
    
    CHARGING_ENERGY     (0x1, 8), //1.2.251 Ok. 
    UNITI_OF_POWER      (0x2, 8), //1.2.251 Ok.
    FLIP_THE_COIN       (0x4, 8), //1.2.251 Ok. 
    ASURA               (0x8, 8), //1.2.252 Ok.
    ESP_BATTLE_ORDER    (0x10, 8), //1.2.251 Ok.
    BLEEDING_TOXIN      (0x20, 8), //1.2.252 Ok.
    //중간에 0x40 비었음.
    CONVERSION          (0x80, 3), //1.2.252 Ok.
    H_CRITICAL_RAGE     (0x100, 8), //1.2.251 Ok.
    HEAVENS_DOOR        (0x400, 8), //1.2.251 Ok.
    ANGELRAY_COUNT      (0x800, 8), //1.2.251 Ok.
    FIRE_AURA           (0x1000, 8), //1.2.251 Ok.
    PARTY_STANCE        (0x2000, 8), //1.2.252 Ok. 
    IGNORE_ATTACKNON    (0x40000, 8), //1.2.251 Ok.
    RECHARGE            (0x80000, 8), //1.2.252 Ok.
    SOUL_HEAD_EFFECT    (0x10000000, 10), //1.2.252 Ok.
    SOUL_WEAPON         (0x20000000, 10), //1.2.251 Ok.
    WIND_WALK           (0x80000000, 10), //1.2.252 Ok.
    
    HEAD_EFFECT         (0x1, 9), //1.2.251 Ok.
    ELEMENT_SOUL        (0x2, 9), //1.2.251 Ok.
    PERCENT_ACC         (0x4, 9), //1.2.251 Ok.
    DMG_DEC             (0x8, 9), //1.2.251 Ok.
    //0x10 비었음.
    ALBATROSS           (0x20, 9), //1.2.251 Ok.
    ADD_ACC             (0x40, 9), //1.2.251 Ok.
    ADD_AVOID           (0x80, 9), //1.2.251 Ok.
    
    STORM_BRINGER       (0x100, 9), //1.2.252 Ok.
    EXPRATE             (0x200, 9), //1.2.252 Ok.
    //중간에 0x400 비었음.
    NON_DEF             (0x800, 9), //1.2.252 Ok.
    ELEMENT_LIGHTNING           (0x1000, 9), //1.2.251 Ok.
    HYPER               (0x4000, 9), //1.2.252 Ok.
    SOARING             (0x8000, 9), //1.2.251 Ok.
    //중간에 0x10000 비었음.
    LIGHTNING_CHARGE    (0x20000, 9), //1.2.252 Ok.
    SURPLUS             (0x40000, 9), //1.2.251 Ok.
    //사이에 0x80000, 0x100000 비었음.
    ATTACK_COUNT        (0x200000, 9), //1.2.251 Ok. 
    DIABOLIC_RECOVERY   (0x2000000, 9), //1.2.251 Ok.
    ENHANCED_MAXMP      (0x4000000, 9), //1.2.251 Ok.    
   
    RUNE_RECOVERY       (0x20000000, 11), //1.2.251 Ok.
    DAMAGE_R_ZERO       (0x8, 10), //1.2.251 Ok.
    CRITICAL_R_ZERO     (0x10, 10), //1.2.251 Ok.
    CONCENTRATION       (0x20, 10), //1.2.251 Ok.
    QUICK_DRAW          (0x40, 10), //1.2.251 Ok.
    CRITICAL_GROWING    (0x80, 10), //1.2.251 Ok.
    DIVINE_SWIFT        (0x100, 10), //1.2.251 Ok.
    DIVINE_FORCE        (0x200, 10), //1.2.251 Ok.
    //중간에 0x400, 0x800, 0x1000 비었음.
    ADVANCED_QUIVER     (0x2000, 10),  //1.2.251 Ok.
    QUIVER_KARTRIGE     (0x4000, 10), //1.2.251 0k.
    IMMUNITY_BARRIER    (0x8000, 10), //1.2.251 Ok.
    EXTREAM_ARCH        (0x10000, 10), //1.2.251 Ok.
    BLESS_ANGSANGBLE    (0x40000, 10), //1.2.251 Ok.
    //중간에 0x80000 비었음.
    CHILLING_STEP       (0x100000, 10), //1.2.251 Ok. 
    PARASHOCK_DF_POWER  (0x200000, 10), //1.2.251 Ok.
    REIN_CANATION       (0x400000, 10), //1.2.251 Ok.
    //중간에 0x800000 비었음.
    CROSS_OVER_CHAIN    (0x1000000, 10), //1.2.251 Ok.
    LISTONATION         (0x2000000, 10), //1.2.251 Ok.
    DEATH_SUMMON        (0x40000000, 12), //1.2.251 Ok.
    DARK_LIGHTNING      (0x1, 11), //1.2.251 Ok.
    AURA                (0x2, 11), //1.2.251 Ok.
    //중간에 0x4, 0x8 비었음.
    DEVIL_CRY           (0x20, 4), //1.2.252 Ok.
    SUMMON_JAGUAR       (0x40, 11), ///1.2.251 Ok.
    //중간에 0x800 비었음.
    BARRIER             (0x1000, 11), //1.2.251 Ok.
    //중간에 0x2000, 0x4000 비었음.
    SHADOW_SERVANT      (0x8000, 11), //1.2.251 Ok.
    //중간에 0x10000 비었음.
    IGNITION            (0x20000, 11), //1.2.251 Ok.
    PHOENIX             (0x40000, 11), //1.2.251 Ok.
    ELEMENT_DARKNESS    (0x80000, 11), //1.2.251 Ok.
    ESCAPE              (0x8000000, 11), //1.2.251 Ok.
    SOHON               (0x10000000, 11), //1.2.251 Ok.
    
    COMBIATION_TRANING  (0x40, 13), //1.2.251 Ok.
    COOLTIME_FAUSE      (0x4000, 13), //1.2.251 Ok.
    IGNESS_RORE         (0x20000, 13), //1.2.251 Ok.
    KINESIS_PP          (0x10000000, 13), //1.2.251 Ok.
    KINESIS_INSTINCT    (0x1000000, 13), //1.2.251 Ok.
    SOUL_LINK_DAM       (0x1, 12), //1.2.251 Ok.
    ROYAL_GUARD         (0x2, 12), //1.2.252 Ok.
    THUNDER_RUNE        (0x100, 12), //1.2.252 Ok.
    IGNIGHT             (0x100000, 12), //1.2.251 Ok.
    SHADOW_BATT         (0x400000, 12), //1.2.251 Ok.
    FOX_ROYAL           (0x800000, 12), //1.2.251 Ok.
    COMBO_DRAIN         (0x400, 13), //1.2.251 Ok.
    SWING_RESEARCH      (0x800, 13), //1.2.251 Ok.
    EAZIS_SYSTEM        (0x2000000, 12), //1.2.251 Ok.
    KINESIS_ACC         (0x4000000, 13), //1.2.251 Ok.
    SPEED_INFUSION      (0x2000000, 14), //1.2.251 Ok.
    MONSTER_RIDING      (0x4000000, 14), //1.2.251 Ok.
    DASH_SPEED          (0x8000000, 14), //1.2.251 Ok.
    DASH_JUMP           (0x10000000, 14), //1.2.251 Ok.
    ENERGY_CHARGE       (0x20000000, 14), //1.2.252 Ok.
    //중간에 0x40000000 비었음.
    ;
    
    public final static int MAX_BUFFSTAT = 15;
    private final int buffstat;
    private final byte index;
        
    private BuffStats(int buffstat) {
	this.buffstat = buffstat;
	this.index = 0;
    }

    private BuffStats(int buffstat, int index) {
	this.buffstat = buffstat;
	this.index = (byte) index;
    }

    @Override
    public final int getIndex() {
	return index;
    }

    @Override
    public final int getValue() {
	return buffstat;
    }
}