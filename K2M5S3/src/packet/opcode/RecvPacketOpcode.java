/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package packet.opcode;

import tools.IniFileProcess;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public enum RecvPacketOpcode {
    //퐁, 클라이언트.
    PONG,
    BUDDY_PING,
    CLIENT_HELLO,
    BUDDY_HELLO,
    CLIENT_CONNECTED_R,
   
    // 로그인
    SESSION_CHECK_R,
    LOGIN_PASSWORD,
    LOGIN_REQUEST,
    SECONDPW_RESULT_R,
    SERVERLIST_REQUEST,
    CHARLIST_REQUEST,
    CHECK_CHAR_NAME,
    CREATE_CHAR,
    CLIENT_ERROR,
    DELETE_CHAR,
    CHAR_SELECT,
    AUTH_LOGIN_WITH_SPW,
    ONLY_REG_SECOND_PASSWORD,
    REG_SECOND_PASSWORD,
    NEW_CONNECTION,
    SET_BURNING_CHAR_R,
    CLIENT_QUIT,
    
    // 채널
    PLAYER_LOGGEDIN,
    REDISPLAY_CHANNEL,
    CHANGE_MAP,
    CHANGE_CHANNEL,
    SET_FREE_JOB,
    ENTER_CASH_SHOP,
    MOVE_PLAYER,
    CANCEL_CHAIR,
    USE_CHAIR,
    PSYCHIC_GREP_R,
    PSYCHIC_ATTACK_R,
    PSYCHIC_ULTIMATE_R,
    PSYCHIC_DAMAGE_R,
    CANCEL_PSYCHIC_GREP_R,
    CLOSE_RANGE_ATTACK,
    RANGED_ATTACK,
    MAGIC_ATTACK,
    PASSIVE_ENERGY,
    TAKE_DAMAGE,
    GENERAL_CHAT,
    CLOSE_CHALKBOARD,
    FACE_EXPRESSION,
    USE_ITEMEFFECT,
    WHEEL_OF_FORTUNE,
    MONSTER_BOOK_COVER,
    NPC_TALK,
    NPC_TALK_MORE,
    NPC_SHOP,
    STORAGE,
    USE_HIRED_MERCHANT,
    QUICK_SLOT,
    MERCH_ITEM_STORE,
    DUEY_ACTION,
    ITEM_SORT,
    ITEM_GATHER,
    ITEM_MOVE,
    USE_ITEM,
    CANCEL_ITEM_EFFECT,
    USE_SUMMON_BAG,
    MOVE_BAG,
    SWITCH_BAG,
    PET_FOOD,
    USE_MOUNT_FOOD,
    USE_SCRIPTED_NPC_ITEM,
    USE_CASH_ITEM,
    USE_EDITIONAL_SCROLL,
    USE_PET_LOOT,
    USE_CATCH_ITEM,
    USE_SKILL_BOOK,
    USE_RETURN_SCROLL,
    USE_MAGNIFY_GLASS,
    USE_UPGRADE_SCROLL,
    USE_STAMP,
    USE_EDITIONAL_STAMP,
    USE_BAG,
    USE_SOUL_SCROLL,
    USE_SOUL_ENCHANTER,
    DISTRIBUTE_AP,
    ROOM_CHANGE,
    DF_COMBO,
    HYPER_RECV,
    ZERO_TAG,
    ZERO_OPEN,
    WEAPON_ZERO,
    ZERO_SHOCKWAVE,
    WILL_OF_SOWRD_COMBO,
    AUTO_ASSIGN_AP,
    HEAL_OVER_TIME,
    HEAL_OVER_TIME_FROM_POT,
    DISTRIBUTE_HYPER_SP,
    DISTRIBUTE_SP,
    SPECIAL_SKILL,
    CANCEL_BUFF,
    SKILL_EFFECT,
    MESO_DROP,
    GIVE_FAME,
    CHARACTER_CARD,
    CHAR_INFO_REQUEST,
    SPAWN_PET,
    REGISTER_PET_BUFF,
    CHANGE_MAP_SPECIAL,
    USE_INNER_PORTAL,
    TROCK_ADD_MAP,
    QUEST_ACTION,
    SKILL_MACRO,
    SUB_SUMMON_ACTION,
    MYSTERY_BOOK,
    REWARD_ITEM,
    MAKER_SKILL,
    USE_TREASUER_CHEST,
    PARTY_CHAT,
    BUDDY_CHAT,
    GUILD_CHAT,
    WHISPER,
    MESSENGER,
    PLAYER_INTERACTION,
    PARTY_OPERATION,
    DENY_PARTY_REQUEST,
    GUILD_OPERATION,
    DENY_GUILD_REQUEST,
    BUDDYLIST_MODIFY,
    BUDDYLIST_UPDATE_R,
    NOTE_ACTION,
    USE_DOOR,
    USE_MECH_DOOR,
    CHANGE_KEYMAP,
    ENTER_MTS,
    ALLIANCE_OPERATION,
    DENY_ALLIANCE_REQUEST,
    ARAN_GAIN_COMBO,
    ARAN_LOSE_COMBO,
    BLESS_OF_DARKNES,
    BBS_OPERATION,
    TRANSFORM_PLAYER,
    MOVE_PET,
    PET_CHAT,
    PET_COMMAND,
    PET_LOOT,
    PET_AUTO_POT,
    MOVE_SUMMON,
    SUMMON_ATTACK,
    DAMAGE_SUMMON,
    SUB_SUMMON,
    REMOVE_SUMMON,
    MOVE_LIFE,
    AUTO_AGGRO,
    FRIENDLY_DAMAGE,
    MONSTER_BOMB,
    HYPNOTIZE_DMG,
    NPC_ACTION,
    ITEM_PICKUP,
    DAMAGE_REACTOR,
    CS_UPDATE,
    BUY_CS_ITEM,
    COUPON_CODE,
    MOVE_DRAGON,
    USE_SPECIAL_SCROLL,
    USE_EQUIP_SCROLL,
    USE_POTENTIAL_SCROLL,
    USE_MAGNIFYING_GLASS,
    USE_REBIRTH_SCROLL,
    USE_MANYSET_CUBE,
    USE_SILVER_KARMA,
    GOLDEN_HAMMER,
    HAMMER_EFFECT,
    EQUIPPED_SKILL,
    STEEL_SKILL,
    STEEL_SKILL_CHECK,
    HEAD_TITLE,
    START_GATHER,
    END_GATHER,
    ITEMPOT_PUT,
    ITEMPOT_REMOVE,
    ITEMPOT_FEED,
    ITEMPOT_CURE,
    PROFESSIONINFO_REQUEST,
    PROFESSION_MAKE,
    PROFESSION_MAKE_EFFECT,
    PROFESSION_MAKE_SOMETHING,
    SPAWN_EXTRACTOR,EXPEDITION_OPERATION,
    USE_RECIPE,
    NEW_CONNECT,
    INNER_CIRCULATOR,
    MOVE_ANDROID,
    ANDROID_FACE_EXPRESSION,
    AGI_BUFF,
    MAGNETIC_DAMAGE,
    FOLLOW_REQUEST,
    FOLLOW_REPLY,
    AUTO_FOLLOW_REPLY,
    WARP_TO_STARPLANET,
    RETRACE_MECH,
    SHOW_SOULEFFECT_R,
    ZERO_WEAPONINFO,
    ZERO_UPGRADE,
    ZERO_CHAT,
    ZERO_CLOTHES,
    ZERO_SCROLL,
    ZERO_SCROLL_START,
    MAPLE_GUIDE,
    MAPLE_CONTENT_MAP,
    ARROW_FLATTER_ACTION,
    ORBITAL_FLAME,
    MIST_SKILL,
    DMG_FLAME,
    STARDUST,
    DUEY_HANDLER,
    MAPLE_CHAT,
    FISHING,
    INNER_CHANGE,
    GAME_END,
    ENTER_CREATE_CHAR,
    HOLLY,
    DRESS_UP,
    AUCTION_R,
    RUNE_TOUCH,
    RUNE_USE,
    EQUIP_UPGRADE_SYSTEM,
    PROCESS_CHECK,
    STAR_PLANET_RANK,
    COMBAT_ANALYZE;

    private short value;
    private final static Map<Short, RecvPacketOpcode> RecvOpcodes = new HashMap<>();
    
    public static void initalized() {
        if (!RecvOpcodes.isEmpty()) {
            RecvOpcodes.clear();
        }
        for (RecvPacketOpcode recv : RecvPacketOpcode.values()) {
           RecvOpcodes.put(recv.getValue(), recv);
        }
    }
    
    public static Map<Short, RecvPacketOpcode> getRecvOpcodes() {
        return RecvOpcodes;
    }
    
    public static void loadOpcode() {
        try {
            IniFileProcess storage = new IniFileProcess(new File("Settings/Packet/RecvPacket.ini"));
            for (RecvPacketOpcode packet : RecvPacketOpcode.values()) {
                short value = -2;
                try {
                    if (storage.getString("Receive", packet.name()) != null) {
                        value = Short.parseShort(storage.getString("Receive", packet.name()));
                    }
                } catch (NumberFormatException error) {
                    error.printStackTrace();
                }
                packet.setValue(value);
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally { 
            initalized();
        }
    }

    public void setValue(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
    
    public static String getOpcodeName(int value) {

        for (RecvPacketOpcode opcode : values()) {
            if (opcode.getValue() == value) {
                return opcode.name();
            }
        }
        return "UNKNOWN";
    }
}