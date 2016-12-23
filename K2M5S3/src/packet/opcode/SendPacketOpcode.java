/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package packet.opcode;

import constants.ServerConstants;
import tools.IniFileProcess;
import java.io.File;

public enum SendPacketOpcode {
    
    /* Test Opcode */
    TEST, 
    
    /* AntiHank */
    PROCESS_CHECK,
    
    /* 클라이언트 체크 */
    PING,
    BUDDY_PING,
    LOADING_XIGNCODE,
    SESSION_CHECK,

    /* 로그인 */
    LOGIN_STATUS,
    SECONDPW_ERROR,
    SECONDPW_RESULT,
    SERVERLIST,
    DETAIL_SHOW_INFO,
    SERVERSTATUS,
    SERVER_IP,
    CHARLIST,
    CHAR_NAME_RESPONSE,
    CHAR_END_REQUEST,
    RELOG_RESPONSE,
    ADD_NEW_CHAR_ENTRY,
    DELETE_CHAR_RESPONSE,
    CHANNEL_SELECTED,
    CHANNEL_BACK_IMG,
    RECOMMEND_WORLD,
    SELECTED_WORLD,
    SET_BURNING_CHAR,
    AUTH_STATUS,
    AUTH_STATUS_WITH_SPW,
    AUTH_STATUS_WITH_SPW_RESULT,
    LAST_WORLD,

    /* 채널 */
    CHANGE_CHANNEL,
    UPDATE_STATS,
    STEEL_SKILL,
    STEEL_SKILL_CHECK,
    FAME_RESPONSE,
    UPDATE_SKILLS,
    EQUIPPED_SKILL,
    AUCTION,
    AUCTION_BUY,
    DUMMY,
    WARP_TO_MAP,
    PENDANT_SLOT,
    SERVERMESSAGE,
    ELITE_BOSS_NOTICE,
    REBUTTON_FORM_ITEM_SKILL,
    AVATAR_MEGA,
    POPUP_MSG,
    ENFORCE_MSG,
    SPAWN_NPC,
    REMOVE_NPC,
    SPAWN_NPC_REQUEST_CONTROLLER,
    SPAWN_MONSTER,
    SPAWN_MONSTER_CONTROL,
    MOVE_MONSTER_RESPONSE,
    SHOW_POTENTIAL_EFFECT,
    SHOW_MAGNIFYING_EFFECT,
    SHOW_TEXT,
    CHATTEXT,
    SHOW_STATUS_INFO,
    EXP_BONUS,
    FEVER_TIME,
    SHOW_QUEST_COMPLETION,
    WHISPER,
    SPAWN_PLAYER,
    HEAD_TITLE_NEW,
    SPAWN_RUNE,
    REMOVE_RUNE,
    RESPAWN_RUNE,
    RUNE_ACTION,
    RUNE_EFFECT,
    DEATH_COUNT,
    SHOW_SCROLL_EFFECT,
    SHOW_ITEM_GAIN_INCHAT,
    QUICK_SLOT,
    CURRENT_MAP_WARP,
    KILL_MONSTER,
    KEYGUARD_CHECK,
    DROP_ITEM_FROM_MAPOBJECT,
    ABSORB_DF,
    FACIAL_EXPRESSION,
    MOVE_PLAYER,
    MOVE_MONSTER,
    CLOSE_RANGE_ATTACK,
    RANGED_ATTACK,
    MAGIC_ATTACK,
    OPEN_NPC_SHOP,
    CONFIRM_SHOP_TRANSACTION,
    OPEN_STORAGE,
    MODIFY_INVENTORY_ITEM,
    MASTERPIX,
    LINK_SKILL,
    REMOVE_PLAYER_FROM_MAP,
    REMOVE_ITEM_FROM_MAP,
    UPDATE_CHAR_LOOK,
    SHOW_FOREIGN_EFFECT,
    GIVE_FOREIGN_BUFF,
    CANCEL_FOREIGN_BUFF,
    DAMAGE_PLAYER,
    CHAR_INFO,
    GIVE_BUFF,
    INNER_INFO,
    CANCEL_BUFF,
    PLAYER_INTERACTION,
    UPDATE_CHAR_BOX,
    FIRE_WORK,
    STARDUST,
    STAR_PLANET_RANK,
    NPC_TALK,
    QUICK_MOVE,
    OPEN_WINDOW,
    DEADED_WINDOW,
    TIME_CAPSULE,
    SHOW_MAPLE_POINT,
    SPECIAL_MAP_EFFECT,
    SPECIAL_MAP_SOUND,
    KEYMAP,
    HAMMER_EFFECT,
    SHOW_MONSTER_HP,
    PARTY_OPERATION,
    UPDATE_PARTYMEMBER_HP,
    SHOW_EQUIP_EFFECT,
    BUDDY_CHAT,
    GUILD_CHAT,
    MULTI_CHAT,
    APPLY_MONSTER_STATUS,
    CANCEL_MONSTER_STATUS,
    CLOCK,
    SPAWN_PORTAL,
    PET_LOOT_STATUS,
    SPAWN_DOOR,
    REMOVE_DOOR,
    FLIP_THE_COIN,
    SPAWN_SUMMON,
    REMOVE_SUMMON,
    SPAWN_SUB_SUMMON,
    SUMMON_ATTACK,
    MOVE_SUMMON,
    SPAWN_MIST,
    REMOVE_MIST,
    DAMAGE_SUMMON,
    DAMAGE_MONSTER,
    BUDDYLIST,
    BUDDYLIST_UPDATE,
    SHOW_ITEM_EFFECT,
    SHOW_CHAIR,
    CANCEL_CHAIR,
    SKILL_EFFECT,
    CANCEL_SKILL_EFFECT,
    BOSS_ENV,
    REACTOR_SPAWN,
    REACTOR_HIT,
    REACTOR_DESTROY,
    MAP_EFFECT,
    MAP_EFFECT_TIME,
    MUSIC_BOX,
    GUILD_OPERATION,
    ALLIANCE_OPERATION,
    BBS_OPERATION,
    SHOW_MAGNET,
    MERCH_ITEM_MSG,
    MERCH_ITEM_STORE,
    MESSENGER,
    NPC_ACTION,
    SPAWN_PET,
    MOVE_PET,
    PET_CHAT,
    PET_COMMAND,
    PET_NAMECHANGE,
    COOLDOWN,
    PLAYER_HINT,
    DISABLE_UI,
    USE_SKILL_BOOK,
    LOCK_SKILL,
    UNLOCK_SKILL,
    SKILL_MACRO,
    CS_UPDATE,
    CS_OPERATION,
    CS_CODY,
    PLAYER_NPC,
    SHOW_NOTES,
    SUMMON_SKILL,
    CATCH_MONSTER,
    CHALKBOARD,
    TROCK_LOCATIONS,
    SPAWN_HIRED_MERCHANT,
    UPDATE_HIRED_MERCHANT,
    SEND_TITLE_BOX,
    DESTROY_HIRED_MERCHANT,
    UPDATE_INNER_ABILITY,
    UPDATE_INNER_EXP,
    UPDATE_MOUNT,
    FAIRY_PEND_MSG,
    DRAGON_MOVE,
    DRAGON_REMOVE,
    DRAGON_SPAWN,
    ARAN_COMBO, 
    RECHARGE_COMBO,
    TOP_MSG, 
    FINISH_SORT, 
    FINISH_GATHER, 
    PHANTOM_CARD,
    ACHIVEMENT_RATIO,
    NPC_TEMP_SUMMON,
    NPC_SCRIPTABLE, 
    HEAD_TITLE,
    ITEM_COOLDOWN,
    JAGUAR_UPDATE,
    ZERO_TAG,
    ZERO_MUITTAG,
    ZERO_UPDATE,
    ZERO_UPGRADE_SU,
    ZERO_WEAPON,
    ZERO_SHOCKWAVE,
    START_GATHER,
    SHOW_GATHER_EFFECT,
    PROFESSION_INFO,
    SHOW_MAGNETIC_EFFECT,
    SHOW_MAKE_EFFECT,
    SHOW_MAKE_RESULT, 
    REMOVE_EXTRACTOR, 
    SPAWN_EXTRACTOR, 
    EXPEDITION_OPERATION, 
    MEMORIAL_CUBE_WINDOW,
    ANDROID_SPAWN,
    ANDROID_MOVE,
    ANDROID_EMOTION,
    ANDROID_REMOVE,
    ANDROID_DEACTIVATED,
    AGI_BUFF, 
    OPEN_BAG, 
    PET_AUTO_HP,
    PET_AUTO_MP,
    GAME_END,
    LUMINOUS_MORPH,
    MECH_DOOR_SPAWN,
    MECH_DOOR_REMOVE,
    UPDATE_DRESS,
    ITEMPOT_ACTION,
    FOLLOW_EFFECT,
    FOLLOW_REQUEST,
    FOLLOW_MSG,
    FOLLOW_MOVE,
    HYPER,
    OWL_OF_MINERVA,
    OWL_RESULT,
    STING_EXPLOSION,
    GREEN_SHOW_INFO,
    MID_MSG,
    CLEAR_MID_MSG,
    ZERO_WEAPONINFO,
    ZERO_UPGRADE,
    SHOW_REDCUBE_WINDOW,
    SHOW_BLACKCUBE_EFFECT,
    SHOW_BLACKCUBE_WINDOW,
    ZERO_OPTION,
    ZERO_SCROLL,
    ZERO_SCROLL_START,
    SPAWN_ARROW_FLATTER,
    START_ARROW_FLATTER,
    CANCEL_ARROW_FLATTER,
    SHOW_SOULSCROLL_EFFECT,
    SHOW_ENCHANTER_EFFECT,
    SHOW_SOULEFFECT_RESPONESE,
    INTRO,
    SHOW_DAMAGE_SKIN,
    DUEY_HANDLER,
    EQUIP_UPGRADE_SYSTEM_SEND,
    MAPLE_CHAT,
    SHOW_STAGE_CLEAR,
    START_COMBAT_ANALYZE,
    PSYCHIC_GREP,
    PSYCHIC_ATTACK,
    PSYCHIC_DAMAGE,
    PSYCHIC_ULTIMATE,
    CANCEL_PSYCHIC_GREP;
    
    private short value = -2;
   
    public static String getOpcodeName(int value) {

        for (SendPacketOpcode opcode : values()) {
            if (opcode.getValue() == value) {
                System.out.println("DEBUG[SEND]: " + opcode.name());
                return opcode.name();
            }
        }
        return "UNKNOWN";
    }
    
    public static void loadOpcode() {
        try {
            IniFileProcess storage = new IniFileProcess(new File("Settings/Packet/SendPacket.ini"));
            for (SendPacketOpcode packet : SendPacketOpcode.values()) {
                short value = -2;
                try {
                    value = Short.parseShort(storage.getString("Send", packet.name()));
                } catch (NumberFormatException error) {
                    if (!ServerConstants.realese) {
                        System.out.println(storage.getString("Send", packet.name()) + " :: " + packet.name());
                    }
                }
                packet.setValue(value);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
    
    public void setValue(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}
