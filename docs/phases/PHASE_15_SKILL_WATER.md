# PHASE_15_SKILL_WATER.md - 물 속성 스킬

---

## 🎯 목표
물 원소의 3가지 스킬 구현 (아이스 샤드, 물 방어막, 파도)

---

## 📋 구현 범위

### 물 스킬 3종
- ✅ 스킬 A: 아이스 샤드 (다중 발사체, 관통)
- ✅ 스킬 B: 물 방어막 (데미지 흡수 보호막)
- ✅ 스킬 C: 파도 (전방 광역 밀어내기)

### 공통 기능
- ✅ 발사체 및 이펙트 렌더링
- ✅ 보호막 시스템
- ✅ 넉백 (밀어내기) 효과

---

## 📁 필요 파일

### 생성할 파일
```
game/skill/water/
  ├─ IceShard.java                (새로 생성)
  ├─ WaterShield.java             (새로 생성)
  └─ TidalWave.java               (새로 생성)

game/projectile/
  └─ IceShardProjectile.java      (새로 생성)

game/buff/
  └─ ShieldBuff.java              (새로 생성)

game/effect/
  └─ WaterEffect.java             (새로 생성)
```

---

## 🔧 구현 가이드

### 1. IceShard 스킬 (아이스 샤드)

```java
/**
 * 아이스 샤드 스킬
 *
 * 3개의 얼음 파편을 부채꼴로 발사합니다.
 * 각 파편은 최대 2마리의 적을 관통합니다.
 */
public class IceShard extends ElementalSkill {
    // 스킬 기본 스탯
    private static final int BASE_DAMAGE = 30;
    private static final int MANA_COST = 20;
    private static final float BASE_COOLDOWN = 3.0f;

    // 발사 설정
    private static final int SHARD_COUNT = 3;       // 파편 개수
    private static final float SPREAD_ANGLE = 30f;  // 확산 각도 (도)
    private static final float PROJECTILE_SPEED = 500f;
    private static final float PROJECTILE_RANGE = 600f;
    private static final int PIERCE_COUNT = 2;      // 관통 횟수

    /**
     * 아이스 샤드 생성자
     */
    public IceShard() {
        super(201, "아이스 샤드", ElementType.WATER);

        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    /**
     * 스킬 시전
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        // 마나 확인
        if (caster.getStats().getMana() < manaCost) {
            return;
        }

        // 쿨타임 확인
        if (currentCooldown > 0) {
            return;
        }

        // 마나 소비
        caster.getStats().consumeMana(manaCost);

        // 기본 방향 계산
        Vector2 baseDirection = new Vector2(targetPosition)
            .sub(caster.getPosition())
            .nor();

        // 3개의 파편 발사 (중앙, 좌, 우)
        for (int i = 0; i < SHARD_COUNT; i++) {
            // 각도 계산
            float angleOffset = (i - 1) * SPREAD_ANGLE;  // -30, 0, +30
            Vector2 direction = baseDirection.cpy().rotateDeg(angleOffset);

            // 발사체 생성
            IceShardProjectile projectile = new IceShardProjectile(
                caster,
                caster.getPosition().cpy(),
                direction,
                getDamage(),
                PROJECTILE_SPEED,
                PROJECTILE_RANGE * rangeMultiplier,
                PIERCE_COUNT
            );

            GameManager.getInstance().addProjectile(projectile);
        }

        // 쿨타임 시작
        currentCooldown = getCooldown();
    }
}
```

### 2. IceShardProjectile 클래스

```java
/**
 * 아이스 샤드 발사체
 *
 * 적을 관통할 수 있는 얼음 파편입니다.
 */
public class IceShardProjectile extends Projectile {
    private int pierceCount;        // 남은 관통 횟수
    private Set<Integer> hitTargets; // 이미 맞은 대상 (중복 방지)

    private WaterEffect trailEffect;

    /**
     * 아이스 샤드 발사체 생성자
     */
    public IceShardProjectile(Entity owner, Vector2 startPos, Vector2 direction,
                              int damage, float speed, float maxRange, int pierceCount) {
        super(owner, startPos, direction, damage, speed, maxRange);

        this.pierceCount = pierceCount;
        this.hitTargets = new HashSet<>();

        // 크기 설정
        this.setSize(24, 24);

        // 궤적 이펙트
        this.trailEffect = new WaterEffect(WaterEffect.Type.ICE_TRAIL);
    }

    /**
     * 매 프레임 업데이트
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 궤적 업데이트
        trailEffect.setPosition(position);
        trailEffect.update(delta);

        // 사거리 초과 시 제거
        if (distanceTraveled >= maxRange) {
            this.isAlive = false;
        }
    }

    /**
     * 충돌 처리
     */
    @Override
    public void onHit(Entity target) {
        // 몬스터만 대상
        if (!(target instanceof Monster)) {
            return;
        }

        Monster monster = (Monster) target;

        // 이미 맞은 대상은 무시
        if (hitTargets.contains(monster.getId())) {
            return;
        }

        // 데미지 적용
        CombatSystem.getInstance().dealDamage(owner, monster, damage);
        hitTargets.add(monster.getId());

        // 관통 횟수 감소
        pierceCount--;

        // 관통 횟수 소진 시 제거
        if (pierceCount <= 0) {
            this.isAlive = false;
        }

        // 얼어붙는 이펙트
        WaterEffect freezeEffect = new WaterEffect(WaterEffect.Type.FREEZE);
        freezeEffect.setPosition(monster.getPosition());
        GameManager.getInstance().addEffect(freezeEffect);
    }

    /**
     * 렌더링
     */
    @Override
    public void render(SpriteBatch batch) {
        trailEffect.render(batch);
        super.render(batch);
    }
}
```

### 3. WaterShield 스킬 (물 방어막)

```java
/**
 * 물 방어막 스킬
 *
 * 5초간 지속되는 보호막을 생성합니다.
 * 보호막은 일정량의 데미지를 흡수합니다.
 */
public class WaterShield extends ElementalSkill {
    // 스킬 기본 스탯
    private static final int BASE_SHIELD_AMOUNT = 100;  // 흡수량
    private static final int MANA_COST = 35;
    private static final float BASE_COOLDOWN = 12.0f;

    // 보호막 설정
    private static final float SHIELD_DURATION = 5.0f;

    /**
     * 물 방어막 생성자
     */
    public WaterShield() {
        super(202, "물 방어막", ElementType.WATER);

        this.baseDamage = BASE_SHIELD_AMOUNT;  // 흡수량으로 사용
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    /**
     * 스킬 시전
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        // 마나 확인
        if (caster.getStats().getMana() < manaCost) {
            return;
        }

        // 쿨타임 확인
        if (currentCooldown > 0) {
            return;
        }

        // 이미 보호막이 있으면 무시
        if (caster.hasBuff(BuffType.SHIELD)) {
            return;
        }

        // 마나 소비
        caster.getStats().consumeMana(manaCost);

        // 보호막 생성
        int shieldAmount = (int) (BASE_SHIELD_AMOUNT * damageMultiplier);
        ShieldBuff shield = new ShieldBuff(shieldAmount, SHIELD_DURATION);

        // 플레이어에게 버프 추가
        caster.addBuff(shield);

        // 보호막 이펙트
        WaterEffect shieldEffect = new WaterEffect(WaterEffect.Type.SHIELD);
        shieldEffect.setPosition(caster.getPosition());
        shieldEffect.attachTo(caster);  // 플레이어를 따라다님
        GameManager.getInstance().addEffect(shieldEffect);

        // 쿨타임 시작
        currentCooldown = getCooldown();
    }
}
```

### 4. ShieldBuff 클래스

```java
/**
 * 보호막 버프
 *
 * 플레이어가 받는 데미지를 흡수하는 보호막입니다.
 */
public class ShieldBuff extends Buff {
    private int shieldAmount;       // 남은 흡수량
    private int maxShieldAmount;    // 최대 흡수량

    /**
     * 보호막 버프 생성자
     *
     * @param shieldAmount 흡수량
     * @param duration 지속 시간
     */
    public ShieldBuff(int shieldAmount, float duration) {
        super(BuffType.SHIELD, duration);

        this.shieldAmount = shieldAmount;
        this.maxShieldAmount = shieldAmount;
    }

    /**
     * 데미지 흡수
     *
     * @param incomingDamage 들어오는 데미지
     * @return 흡수 후 남은 데미지
     */
    public int absorbDamage(int incomingDamage) {
        if (shieldAmount >= incomingDamage) {
            // 완전 흡수
            shieldAmount -= incomingDamage;
            return 0;
        } else {
            // 부분 흡수
            int remainingDamage = incomingDamage - shieldAmount;
            shieldAmount = 0;
            this.isActive = false;  // 보호막 파괴
            return remainingDamage;
        }
    }

    /**
     * 매 프레임 업데이트
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 지속 시간 종료 또는 흡수량 소진 시 제거
        if (duration <= 0 || shieldAmount <= 0) {
            this.isActive = false;
        }
    }

    public int getShieldAmount() {
        return shieldAmount;
    }

    public int getMaxShieldAmount() {
        return maxShieldAmount;
    }

    /**
     * 보호막 잔량 비율 (0.0 ~ 1.0)
     */
    public float getShieldRatio() {
        return (float) shieldAmount / maxShieldAmount;
    }
}
```

### 5. TidalWave 스킬 (파도)

```java
/**
 * 파도 스킬
 *
 * 전방에 거대한 파도를 일으켜 광역 데미지와 넉백을 줍니다.
 */
public class TidalWave extends ElementalSkill {
    // 스킬 기본 스탯
    private static final int BASE_DAMAGE = 80;
    private static final int MANA_COST = 45;
    private static final float BASE_COOLDOWN = 10.0f;

    // 파도 설정
    private static final float WAVE_WIDTH = 400f;   // 파도 너비
    private static final float WAVE_RANGE = 500f;   // 파도 사거리
    private static final float WAVE_DURATION = 1.0f; // 파도 지속 시간
    private static final float KNOCKBACK_FORCE = 300f; // 밀어내기 힘

    /**
     * 파도 생성자
     */
    public TidalWave() {
        super(203, "파도", ElementType.WATER);

        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    /**
     * 스킬 시전
     */
    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        // 마나 확인
        if (caster.getStats().getMana() < manaCost) {
            return;
        }

        // 쿨타임 확인
        if (currentCooldown > 0) {
            return;
        }

        // 마나 소비
        caster.getStats().consumeMana(manaCost);

        // 방향 계산
        Vector2 direction = new Vector2(targetPosition)
            .sub(caster.getPosition())
            .nor();

        // 파도 생성
        TidalWaveZone wave = new TidalWaveZone(
            caster.getPosition().cpy(),
            direction,
            WAVE_WIDTH * rangeMultiplier,
            WAVE_RANGE * rangeMultiplier,
            WAVE_DURATION,
            getDamage(),
            KNOCKBACK_FORCE,
            caster
        );

        GameManager.getInstance().addSkillZone(wave);

        // 쿨타임 시작
        currentCooldown = getCooldown();
    }
}

/**
 * 파도 구역
 *
 * 전방으로 이동하며 적에게 데미지와 넉백을 주는 파도입니다.
 */
class TidalWaveZone {
    private Vector2 position;       // 현재 위치
    private Vector2 direction;      // 이동 방향
    private float width;            // 파도 너비
    private float maxRange;         // 최대 사거리
    private float distanceTraveled; // 이동 거리
    private float duration;         // 지속 시간
    private float elapsedTime;

    private int damage;
    private float knockbackForce;
    private Entity owner;

    private Set<Integer> hitTargets;  // 중복 피해 방지
    private WaterEffect waveEffect;

    private static final float WAVE_SPEED = 400f;  // 파도 이동 속도

    public TidalWaveZone(Vector2 startPos, Vector2 direction, float width,
                         float maxRange, float duration, int damage,
                         float knockbackForce, Entity owner) {
        this.position = startPos;
        this.direction = direction;
        this.width = width;
        this.maxRange = maxRange;
        this.duration = duration;
        this.damage = damage;
        this.knockbackForce = knockbackForce;
        this.owner = owner;

        this.distanceTraveled = 0f;
        this.elapsedTime = 0f;
        this.hitTargets = new HashSet<>();

        // 파도 이펙트
        this.waveEffect = new WaterEffect(WaterEffect.Type.WAVE);
        this.waveEffect.setPosition(position);
        this.waveEffect.setDirection(direction);
        this.waveEffect.setScale(width / 200f);
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        elapsedTime += delta;

        // 파도 이동
        float moveDistance = WAVE_SPEED * delta;
        position.add(direction.cpy().scl(moveDistance));
        distanceTraveled += moveDistance;

        // 이펙트 위치 업데이트
        waveEffect.setPosition(position);
        waveEffect.update(delta);

        // 충돌 감지
        checkCollisions();
    }

    /**
     * 충돌 감지 및 데미지 적용
     */
    private void checkCollisions() {
        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Monster monster : monsters) {
            // 이미 맞은 대상은 무시
            if (hitTargets.contains(monster.getId())) {
                continue;
            }

            // 파도 범위 내 확인 (직사각형)
            if (isInWaveRange(monster.getPosition())) {
                // 데미지 적용
                CombatSystem.getInstance().dealDamage(owner, monster, damage);

                // 넉백 적용
                applyKnockback(monster);

                hitTargets.add(monster.getId());
            }
        }
    }

    /**
     * 파도 범위 내 확인
     */
    private boolean isInWaveRange(Vector2 targetPos) {
        // 파도 중심에서 목표까지의 벡터
        Vector2 toTarget = new Vector2(targetPos).sub(position);

        // 전방 거리 확인
        float forwardDistance = toTarget.dot(direction);
        if (forwardDistance < 0 || forwardDistance > 100f) {
            return false;  // 파도 앞뒤 범위 밖
        }

        // 좌우 거리 확인
        Vector2 perpendicular = new Vector2(-direction.y, direction.x);
        float sidewaysDistance = Math.abs(toTarget.dot(perpendicular));

        return sidewaysDistance <= width / 2;
    }

    /**
     * 넉백 적용
     */
    private void applyKnockback(Monster monster) {
        Vector2 knockbackVelocity = direction.cpy().scl(knockbackForce);
        monster.applyKnockback(knockbackVelocity, 0.5f);  // 0.5초 동안 밀림
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        waveEffect.render(batch);
    }

    /**
     * 파도가 아직 살아있는지 확인
     */
    public boolean isAlive() {
        return elapsedTime < duration && distanceTraveled < maxRange;
    }
}
```

### 6. WaterEffect 클래스

```java
/**
 * 물 이펙트
 *
 * 물 스킬에 사용되는 다양한 이펙트입니다.
 */
public class WaterEffect {
    private Type type;
    private Vector2 position;
    private Vector2 direction;
    private float scale = 1.0f;
    private float lifetime = 0f;
    private float maxLifetime;
    private boolean isAlive = true;

    private Entity attachedTo = null;  // 부착된 엔티티 (보호막용)

    /**
     * 이펙트 타입
     */
    public enum Type {
        ICE_TRAIL,      // 얼음 궤적
        FREEZE,         // 얼어붙음
        SHIELD,         // 보호막
        WAVE            // 파도
    }

    public WaterEffect(Type type) {
        this.type = type;
        this.position = new Vector2();
        this.direction = new Vector2(1, 0);

        // 타입별 수명 설정
        switch (type) {
            case ICE_TRAIL:
                maxLifetime = 0.3f;
                break;
            case FREEZE:
                maxLifetime = 0.5f;
                break;
            case SHIELD:
                maxLifetime = Float.MAX_VALUE;  // 버프와 함께 제거
                break;
            case WAVE:
                maxLifetime = Float.MAX_VALUE;
                break;
        }
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        lifetime += delta;

        // 부착된 엔티티 따라가기
        if (attachedTo != null) {
            position.set(attachedTo.getPosition());
        }

        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        // 타입별 렌더링 (임시: 색상 사각형)
        float alpha = 1f - (lifetime / maxLifetime);

        switch (type) {
            case ICE_TRAIL:
                batch.setColor(0.5f, 0.7f, 1f, alpha);
                break;
            case FREEZE:
                batch.setColor(0.3f, 0.5f, 1f, alpha);
                break;
            case SHIELD:
                batch.setColor(0.2f, 0.6f, 1f, 0.5f);
                break;
            case WAVE:
                batch.setColor(0.1f, 0.4f, 0.8f, 0.7f);
                break;
        }

        // batch.draw(texture, position.x, position.y, width, height);
        batch.setColor(1, 1, 1, 1);
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void setDirection(Vector2 direction) {
        this.direction.set(direction);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void attachTo(Entity entity) {
        this.attachedTo = entity;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }
}
```

---

## 🧪 테스트 계획

```java
/**
 * IceShard 테스트
 */
public class TestIceShard {
    private IceShard iceShard;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        iceShard = new IceShard();
        testPlayer = new Player(1);
        testPlayer.getStats().setMana(100);
    }

    @Test
    public void 3개의_파편_발사() {
        Vector2 target = new Vector2(500, 500);
        iceShard.cast(testPlayer, target);

        List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
        assertEquals(3, projectiles.size());
    }

    @Test
    public void 파편_관통_기능() {
        // 몬스터 2마리 일렬 배치
        Monster m1 = new Ghost();
        m1.setPosition(500, 500);
        Monster m2 = new Ghost();
        m2.setPosition(550, 500);

        GameManager.getInstance().addMonster(m1);
        GameManager.getInstance().addMonster(m2);

        // 아이스 샤드 발사
        iceShard.cast(testPlayer, new Vector2(600, 500));

        // 발사체 업데이트 (충돌 확인)
        IceShardProjectile proj = (IceShardProjectile)
            GameManager.getInstance().getProjectiles().get(1);

        int hp1 = m1.getHealth();
        int hp2 = m2.getHealth();

        proj.update(0.1f);  // 첫 번째 몬스터 충돌
        proj.update(0.1f);  // 두 번째 몬스터 충돌

        // 둘 다 데미지 받음
        assertTrue(m1.getHealth() < hp1);
        assertTrue(m2.getHealth() < hp2);

        // 관통 후 살아있음
        assertTrue(proj.isAlive());
    }
}

/**
 * WaterShield 테스트
 */
public class TestWaterShield {
    private WaterShield waterShield;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        waterShield = new WaterShield();
        testPlayer = new Player(1);
        testPlayer.getStats().setMana(100);
        testPlayer.getStats().setHealth(100);
    }

    @Test
    public void 보호막_생성() {
        waterShield.cast(testPlayer, testPlayer.getPosition());

        assertTrue(testPlayer.hasBuff(BuffType.SHIELD));
    }

    @Test
    public void 보호막_데미지_흡수() {
        waterShield.cast(testPlayer, testPlayer.getPosition());

        ShieldBuff shield = (ShieldBuff) testPlayer.getBuff(BuffType.SHIELD);
        int originalShield = shield.getShieldAmount();

        // 데미지 50 받기
        int remainingDamage = shield.absorbDamage(50);

        assertEquals(0, remainingDamage);  // 완전 흡수
        assertEquals(originalShield - 50, shield.getShieldAmount());
        assertEquals(100, testPlayer.getStats().getHealth());  // 체력 그대로
    }

    @Test
    public void 보호막_초과_데미지() {
        waterShield.cast(testPlayer, testPlayer.getPosition());

        ShieldBuff shield = (ShieldBuff) testPlayer.getBuff(BuffType.SHIELD);

        // 보호막보다 큰 데미지 (150)
        int remainingDamage = shield.absorbDamage(150);

        assertTrue(remainingDamage > 0);  // 일부 관통
        assertEquals(0, shield.getShieldAmount());  // 보호막 파괴
        assertFalse(shield.isActive());
    }
}

/**
 * TidalWave 테스트
 */
public class TestTidalWave {
    private TidalWave tidalWave;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        tidalWave = new TidalWave();
        testPlayer = new Player(1);
        testPlayer.setPosition(100, 100);
        testPlayer.getStats().setMana(100);
    }

    @Test
    public void 파도_생성() {
        Vector2 target = new Vector2(500, 100);
        tidalWave.cast(testPlayer, target);

        List<TidalWaveZone> zones = GameManager.getInstance().getSkillZones();
        assertEquals(1, zones.size());
    }

    @Test
    public void 파도_광역_데미지() {
        // 몬스터 3마리 배치 (파도 경로 상)
        Monster m1 = new Ghost();
        m1.setPosition(300, 100);
        Monster m2 = new Ghost();
        m2.setPosition(300, 150);
        Monster m3 = new Ghost();
        m3.setPosition(300, 300);  // 범위 밖

        GameManager.getInstance().addMonster(m1);
        GameManager.getInstance().addMonster(m2);
        GameManager.getInstance().addMonster(m3);

        // 파도 발사
        tidalWave.cast(testPlayer, new Vector2(500, 100));

        TidalWaveZone wave = GameManager.getInstance().getSkillZones().get(0);

        int hp1 = m1.getHealth();
        int hp2 = m2.getHealth();
        int hp3 = m3.getHealth();

        // 파도 이동
        wave.update(0.5f);

        // 범위 내 몬스터만 데미지
        assertTrue(m1.getHealth() < hp1);
        assertTrue(m2.getHealth() < hp2);
        assertEquals(hp3, m3.getHealth());  // 범위 밖
    }

    @Test
    public void 넉백_효과() {
        Monster monster = new Ghost();
        monster.setPosition(300, 100);
        Vector2 originalPos = monster.getPosition().cpy();

        GameManager.getInstance().addMonster(monster);

        // 파도 발사
        tidalWave.cast(testPlayer, new Vector2(500, 100));

        TidalWaveZone wave = GameManager.getInstance().getSkillZones().get(0);
        wave.update(0.5f);

        // 몬스터가 밀려남
        assertTrue(monster.getPosition().x > originalPos.x);
    }
}
```

---

## ✅ 완료 조건

- [ ] IceShard 스킬 구현
- [ ] WaterShield 스킬 구현
- [ ] TidalWave 스킬 구현
- [ ] IceShardProjectile 구현
- [ ] ShieldBuff 구현
- [ ] WaterEffect 이펙트 구현
- [ ] 넉백 시스템 구현
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_16: 바람 속성 스킬**
- 회오리 (Tornado)
- 바람 베기 (Wind Slash)
- 질주 (Dash)
