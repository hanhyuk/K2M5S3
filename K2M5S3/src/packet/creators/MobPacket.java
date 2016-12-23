/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package packet.creators;

import client.stats.MonsterStatus;
import client.stats.MonsterStatusEffect;
import packet.opcode.SendPacketOpcode;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;
import server.life.MapleMonster;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import java.awt.Point;
import java.util.List;
import java.util.Map;

public class MobPacket {

    public static Packet damageMonster(final int oid, final long damage) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(0);
        packet.writeInt(damage);

        return packet.getPacket();
    }

    public static Packet damageFriendlyMob(final MapleMonster mob, final long damage) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(mob.getObjectId());
        packet.write(1);
        packet.writeInt(damage);
        packet.writeInt(mob.getHp());
        packet.writeInt(mob.getMobMaxHp());

        return packet.getPacket();
    }

    public static Packet killMonster(final int oid, final int animation) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(animation); // 0 = dissapear, 1 = fade out, 2+ = special

        return packet.getPacket();
    }

    public static Packet healMonster(final int oid, final int heal) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(0);
        packet.writeInt(-heal);

        return packet.getPacket();
    }

    public static Packet showMonsterHP(int oid, int remhppercentage) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
        packet.writeInt(oid);
        packet.write(remhppercentage);

        return packet.getPacket();
    }

    public static Packet showBossHP(final MapleMonster mob) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        packet.write(6);
        packet.writeInt(mob.getId());
        packet.writeInt(mob.getHp());
        packet.writeInt(mob.getMobMaxHp());
        packet.write(mob.getStats().getTagColor());
        packet.write(mob.getStats().getTagBgColor());

        return packet.getPacket();
    }
    
        public static Packet showFinalBossHP(final MapleMonster mob) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        packet.write(6);
        packet.writeInt(mob.getId());
        packet.writeInt(mob.getFinalMaxHP());
        packet.writeInt(mob.getMobFinalMaxHP());
        packet.write(mob.getStats().getTagColor());
        packet.write(mob.getStats().getTagBgColor());
        
        return packet.getPacket();
    }
    
    public static Packet moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, Point startPos, List<LifeMovementFragment> moves) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(useskill ? 1 : 0);
        packet.write(skill);
        packet.write(skill1);
        packet.write(skill2);
        packet.write(skill3);
        packet.write(skill4);
        packet.write(0);
        packet.write(0);
        packet.writeInt(0); //1.2.192+
        packet.writePos(startPos);
        packet.writeInt(0);
        serializeMovementList(packet, moves);
        packet.write(0); //1.2.252+
        
        return packet.getPacket();
    }

    private static void serializeMovementList(WritingPacket packet, List<LifeMovementFragment> moves) {
        packet.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(packet);
        }
    }

    public static void addMobSkillInfo(WritingPacket packet, MapleMonster life) {
        if (life.isStatChanged()) {
            packet.write(1);
            packet.writeInt(life.getHp());
            packet.writeInt(life.getMp());
            packet.writeInt(life.getStats().getPad());
            packet.writeInt(life.getStats().getPhysicalDefense());
            packet.writeInt(life.getStats().getMad());
            packet.writeInt(life.getStats().getMagicDefense());
            packet.writeInt(life.getStats().getSpeed());
            packet.writeInt(life.getStats().getAcc());
            packet.writeInt(life.getStats().getEva());
            packet.writeInt(2100000000);
            packet.writeInt(life.getStats().getPushed());
            packet.writeInt(life.getStats().getLevel());
        } else {
            packet.write(0);
        }
        packet.writeShort(0);
        packet.writeShort(0);
        packet.writeShort(0);
        packet.writeShort(0x6000);
        packet.writeShort(0xFE00);
        packet.writeShort(0x29F);
        for (int i = 0; i < 4; i++) {
            packet.writeLong(0);
            packet.write(HexTool.getByteArrayFromHexString("3B 11"));
        }
        packet.write0(119);
    }
    
     public static Packet spawnMonster(MapleMonster life, int spawnType, int effect, int link, boolean EliteMonster) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        packet.write(0);
        packet.writeInt(life.getObjectId());
        packet.write(1); 
        packet.writeInt(life.getId());
        addMobSkillInfo(packet, life);
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(life.getStance());
        if ((life.getId() == 8910000) || (life.getId() == 8910100)) {
            packet.write(0);
        }
        packet.writeShort(life.getFh());
        packet.writeShort(life.getFh() + 1);
        if ((effect != 0) || (link != 0)) {
            packet.write(effect != 0 ? effect : -3);
            packet.writeInt(link);
        } else {
            if (spawnType == 0) {
                packet.write(effect);
                packet.write(0);
                packet.writeShort(0);
            }
            packet.write(spawnType);
        }
        packet.write(0xFF); //Monster Carnival.
        packet.writeInt(life.getHp());
        packet.write0(17);
        packet.writeLong(-1);
        packet.writeInt(0); 
        packet.write(0);
        packet.writeInt(100);
        packet.writeInt(EliteMonster ? life.getEliteType() : -1);
        if ((life.getEliteType() != -1) && (EliteMonster)) {
            packet.writeInt(1);
            packet.writeInt(life.getEliteType());
            packet.writeInt(1);
        }
        packet.writeInt(0); //1.2.252+
        packet.write(0); //1.2.252+
        packet.write(0); //1.2.252+

        return packet.getPacket();
    }    

    public static Packet controlMonster(MapleMonster life, boolean newSpawn, boolean aggro, boolean EliteMonster) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        packet.write(aggro ? 2 : 1);
        packet.writeInt(life.getObjectId());
        packet.write(1);
        packet.writeInt(life.getId());
        addMobSkillInfo(packet, life);
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(life.getStance());
        packet.writeShort(life.getFh());
        packet.writeShort(life.getFh() + 1);
        packet.write(newSpawn ? 0xFE : life.isFake() ? 0xFC : 0xFF);
        packet.write(0xFF); //Monster Carnival.
        packet.writeInt(life.getHp());
        packet.write0(17);
        packet.writeLong(-1);
        packet.writeInt(0); 
        packet.write(0);
        packet.writeInt(100);
        packet.writeInt(EliteMonster ? life.getEliteType() : -1);
        if ((life.getEliteType() != -1) && (EliteMonster)) {
            packet.writeInt(1);
            packet.writeInt(life.getEliteType());
            packet.writeInt(1);
        }
        packet.writeInt(0); //1.2.252+
        packet.write(0); //1.2.252+
        packet.write(0); //1.2.252+
        
        return packet.getPacket();
    }
    
    public static Packet spawnFakeMonster(MapleMonster life, int link, boolean isZakum, int effetId, String effectString) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        packet.write(0);
        packet.writeInt(life.getObjectId());
        packet.write(1);
        packet.writeInt(life.getId());
        addMobSkillInfo(packet, life);
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(life.getStance());
        packet.writeShort(life.getFh());
        packet.writeShort(life.getFh());
        packet.write(0xFE);
        packet.write(0xFF);
        packet.writeInt(life.getHp()); 
        packet.write0(17);
        packet.writeLong(-1);
        packet.writeInt(effetId);
        packet.write(0);
        packet.writeInt(100);
        packet.writeInt(-1);
        packet.writeInt(0);
        packet.write(0);
        packet.write(isZakum ? 1 : 0); 
        if (isZakum) {
            packet.writeMapleAsciiString(effectString); 
            packet.writeInt(0);
            packet.write(0);
        }
        return packet.getPacket();
    }

    public static Packet stopControllingMonster(int oid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        packet.write(0);
        packet.writeInt(oid);

        return packet.getPacket();
    }

    public static Packet makeMonsterInvisible(MapleMonster life) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        packet.write(0);
        packet.writeInt(life.getObjectId());

        return packet.getPacket();
    }
    
    public static Packet moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
        packet.writeInt(objectid);
        packet.writeShort(moveid);
        packet.write(useSkills ? 1 : 0);
        packet.writeInt(currentMp);
        packet.write(skillId);
        packet.write(skillLevel);
        packet.write0(7); //1.2.250+
        
        return packet.getPacket();
    }
    
    public static void writeMonsterIntMask(WritingPacket packet, Map<MonsterStatus, Integer> stats) {
        int[] mask = new int[3];
        for (MonsterStatus stat : stats.keySet()) {
            mask[(stat.getIndex())] |= stat.getValue();
        }
        for (int i = 0; i < mask.length; i++) {
            packet.writeInt(mask[i]);
        }
    }
    
    public static Packet applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
        return applyMonsterStatus(oid, mse, null);
    }

    public static Packet applyMonsterStatus(final int oid, final MonsterStatusEffect mse, final List<Integer> reflection) {
        if (mse.getStati().containsKey(MonsterStatus.POISON)) {
            return applyPoison(oid, mse, 20);
        }
        int triangle = 0;
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        writeMonsterIntMask(packet, mse.getStati());
        for (Map.Entry<MonsterStatus, Integer> stat : mse.getStati().entrySet()) {
            if (stat.getKey().equals(MonsterStatus.TRIANGLE_FOMATION)) {
                triangle = stat.getValue();
            }
            packet.writeInt(stat.getValue());
            if (mse.isMonsterSkill()) {
                packet.writeShort(mse.getMobSkill().getSkillId());
                packet.writeShort(mse.getMobSkill().getSkillLevel());
            } else {
                packet.writeInt(mse.getSkill().getId());
            }
            if (mse.getStati().containsKey(MonsterStatus.NEUTRALISE)) {
                packet.writeInt(10);
                packet.write(1);
                return packet.getPacket();
            }
            packet.writeShort(0); //skilldelay
        }
        if (reflection != null) {
            for (Integer ref : reflection) {
                packet.writeInt(ref);
            }
        }
        if (!mse.isMonsterSkill()) {
            if (mse.getSkill().getId() == 25111206) {
                packet.writeInt(25111206);
                packet.write(-1);
            }
        }
        packet.writeLong(0); //duration
        packet.writeInt(0); //1.2.252+
        packet.writeShort(1600);
        int size = mse.getStati().size();
        if ((reflection != null) && (reflection.size() > 0)) {
            size /= 2; 
        }
        packet.write(size); 
        packet.write(1 + triangle);
        
        return packet.getPacket();
    }
    
    public static Packet applyPoison(int oid, final MonsterStatusEffect mse, int skilllevel) {
      WritingPacket packet = new WritingPacket();
      packet.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
      packet.writeInt(oid);
      writeMonsterIntMask(packet, mse.getStati());
      /* 도입부 시작 */
      packet.write(1);
      packet.writeInt(mse.getOwnerId());
      packet.writeInt(mse.getSkill().getId());
      packet.writeInt(mse.getPoisonDamage());
      packet.writeInt(1000);     
      packet.write(HexTool.getByteArrayFromHexString("04 C7 7B 37")); //지속시간.
      packet.writeInt(13000);
      /* 도입부 끝 */
      packet.write(skilllevel);
      packet.write0(21);
      packet.write(7);

      return packet.getPacket();
    }
    
    public static Packet cancelMonsterStatus(int cid, int oid, Map<MonsterStatus, Integer> stats) {
        WritingPacket packet = new WritingPacket();
        if (stats.containsKey(MonsterStatus.POISON)) {
            return cancelPoison(oid, cid, stats);
        }
        packet.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        writeMonsterIntMask(packet, stats);
        packet.write(3); 
        packet.write(2);

        return packet.getPacket();
    }

    public static Packet cancelPoison(int oid, int cid, Map<MonsterStatus, Integer> stats) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        writeMonsterIntMask(packet, stats);
        packet.writeInt(0);
        packet.writeInt(1);
        packet.writeInt(cid);
        packet.write(HexTool.getByteArrayFromHexString("B0 61 03 02")); //지속시간.
        packet.write(6);

        return packet.getPacket();
    }

    public static Packet swallowMonster(final int oid, final int cid) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(4);
        packet.writeInt(cid);
        
        return packet.getPacket();
    }
}
