package client.stats;

public enum MonsterStatus implements GlobalBuffStat {

	NEUTRALISE(0x0, 0), IMPRINT(0x0, 0), MONSTER_BOMB(0x0, 0), MAGIC_CRASH(0x0, 0),
	/* 1.2.239 테스트 라인 시작 */
	DARKNESS(0x0, 0), // 1.2.242 Ok.
	STUN(0x0, 0), // 1.2.242 Ok.
	TRIANGLE_FOMATION_S(0x0, 0), // 1.2.242 Ok.
	TRIANGLE_FOMATION(0x0, 0), // 1.2.242 Ok.
	POISON(0x100000, 2), // 1.2.252 Ok.
	SEAL(0x0, 0), // 1.2.242 Ok.
	SLOW(0x0, 0), // 1.2.242 Ok.
	FREEZE(0x40000000, 0), // 1.2.252 Ok.
	/* 테스트 라인 */
	STING_EXPLOSION(0x0, 0), WATK(0x0, 0), MDEF(0x800000, 0), // 1.2.252 Ok.
	WDEF(0x10000000, 0), // 1.2.252 Ok.
	MATK(0x0, 0), ACC(0x0, 0), AVOID(0x0, 0), SPEED(0x0, 0), SHOWDOWN(0x0, 0), WEAPON_ATTACK_UP(0x0, 0), WEAPON_DEFENSE_UP(0x0, 0), MAGIC_ATTACK_UP(0x0, 0), MAGIC_DEFENSE_UP(0x0, 0),
	/* 아래 부터는 미완성 */
	DOOM(0x0), SHADOW_WEB(0x0), WEAPON_IMMUNITY(0x0), MAGIC_IMMUNITY(0x0), NINJA_AMBUSH(0x0), VENOMOUS_WEAPON(0x0), HYPNOTIZE(0x0), WEAPON_DAMAGE_REFLECT(0x0), MAGIC_DAMAGE_REFLECT(0x0), SUMMON(0x0),
	/* 1.2.239 테스트 라인 종료 */
	;

	private final int i;
	private final byte index;

	private MonsterStatus(int i) {
		this.i = i;
		this.index = 0;
	}

	private MonsterStatus(int i, int index) {
		this.i = i;
		this.index = (byte) index;
	}

	public boolean isFirst() {
		return this.index == 0;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	public boolean isEmpty() {
		return this == SUMMON;
	}

	@Override
	public int getValue() {
		return this.i;
	}
}
