# PHASE_14_SKILL_FIRE.md - 불 속성 스킬

---

## 🎯 목표
불 원소의 3가지 스킬 구현 (파이어볼, 불 기둥, 운석)

---

## 📋 구현 범위

### 불 스킬 3종
- ✅ 스킬 A: 파이어볼 (단일 대상, 빠른 발사체)
- ✅ 스킬 B: 불 기둥 (범위 지속 데미지)
- ✅ 스킬 C: 운석 (광역 폭발)

### 공통 기능
- ✅ 발사체 렌더링
- ✅ 충돌 감지
- ✅ 데미지 계산
- ✅ 이펙트 표시

---

## 📁 필요 파일

### 생성할 파일
```
game/skill/fire/
  ├─ Fireball.java                (새로 생성)
  ├─ FlamePillar.java             (새로 생성)
  └─ Meteor.java                  (새로 생성)

game/projectile/
  ├─ FireballProjectile.java      (새로 생성)
  └─ MeteorProjectile.java        (새로 생성)

game/effect/
  └─ FlameEffect.java             (새로 생성)
```

### 기존 파일 수정
```
Constants.java                    (수정 - 불 스킬 상수 추가)
SkillFactory.java                 (수정 - 불 스킬 생성 로직)
```

---

## 🔧 구현 가이드

### 1. Fireball 스킬 (파이어볼)

```java
/**
 * 파이어볼 스킬
 *
 * 빠른 속도로 직선으로 날아가는 화염구를 발사합니다.
 * 적중 시 단일 대상에 데미지를 줍니다.
 */
public class Fireball extends ElementalSkill {
    // 스킬 기본 스탯
    private static final int BASE_DAMAGE = 40;
    private static final int MANA_COST = 15;
    private static final float BASE_COOLDOWN = 2.0f;

    // 발사체 설정
    private static final float PROJECTILE_SPEED = 600f;  // 픽셀/초
    private static final float PROJECTILE_RANGE = 800f;  // 최대 사거리

    /**
     * 파이어볼 생성자
     */
    public Fireball() {
        super(101, "파이어볼", ElementType.FIRE);

        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    /**
     * 스킬 시전
     *
     * @param caster 시전자
     * @param targetPosition 목표 위치
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

        // 발사 방향 계산
        Vector2 direction = new Vector2(targetPosition)
            .sub(caster.getPosition())
            .nor();  // 정규화

        // 발사체 생성
        FireballProjectile projectile = new FireballProjectile(
            caster,
            caster.getPosition().cpy(),
            direction,
            getDamage(),
            PROJECTILE_SPEED,
            PROJECTILE_RANGE * rangeMultiplier
        );

        // 게임에 발사체 추가
        GameManager.getInstance().addProjectile(projectile);

        // 쿨타임 시작
        currentCooldown = getCooldown();

        // 사운드 재생 (향후)
        // SoundManager.play("fireball_cast");
    }
}
```

### 2. FireballProjectile 클래스

```java
/**
 * 파이어볼 발사체
 *
 * 직선으로 날아가는 화염구입니다.
 */
public class FireballProjectile extends Projectile {
    private FlameEffect trailEffect;  // 궤적 이펙트

    /**
     * 파이어볼 발사체 생성자
     *
     * @param owner 발사자
     * @param startPos 시작 위치
     * @param direction 발사 방향
     * @param damage 데미지
     * @param speed 속도
     * @param maxRange 최대 사거리
     */
    public FireballProjectile(Entity owner, Vector2 startPos, Vector2 direction,
                              int damage, float speed, float maxRange) {
        super(owner, startPos, direction, damage, speed, maxRange);

        // 발사체 크기 설정
        this.setSize(32, 32);

        // 스프라이트 설정 (향후 텍스처 추가)
        // this.sprite = new Sprite(AssetManager.getTexture("fireball"));

        // 궤적 이펙트 초기화
        this.trailEffect = new FlameEffect(FlameEffect.Type.TRAIL);
    }

    /**
     * 매 프레임 업데이트
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 궤적 이펙트 위치 업데이트
        trailEffect.setPosition(position);
        trailEffect.update(delta);

        // 수명 확인
        if (distanceTraveled >= maxRange) {
            this.isAlive = false;
        }
    }

    /**
     * 충돌 처리
     */
    @Override
    public void onHit(Entity target) {
        // 데미지 적용
        if (target instanceof Monster) {
            Monster monster = (Monster) target;
            CombatSystem.getInstance().dealDamage(owner, monster, damage);

            // 적중 이펙트
            FlameEffect hitEffect = new FlameEffect(FlameEffect.Type.EXPLOSION);
            hitEffect.setPosition(position);
            GameManager.getInstance().addEffect(hitEffect);
        }

        // 발사체 제거
        this.isAlive = false;
    }

    /**
     * 렌더링
     */
    @Override
    public void render(SpriteBatch batch) {
        // 궤적 먼저 렌더링
        trailEffect.render(batch);

        // 발사체 렌더링
        super.render(batch);
    }
}
```

### 3. FlamePillar 스킬 (불 기둥)

```java
/**
 * 불 기둥 스킬
 *
 * 지정 위치에 3초간 지속되는 불 기둥을 생성합니다.
 * 범위 내 적에게 초당 데미지를 줍니다.
 */
public class FlamePillar extends ElementalSkill {
    // 스킬 기본 스탯
    private static final int BASE_DAMAGE = 25;  // 초당 데미지
    private static final int MANA_COST = 30;
    private static final float BASE_COOLDOWN = 8.0f;

    // 기둥 설정
    private static final float PILLAR_RADIUS = 120f;  // 범위
    private static final float PILLAR_DURATION = 3.0f;  // 지속 시간

    /**
     * 불 기둥 생성자
     */
    public FlamePillar() {
        super(102, "불 기둥", ElementType.FIRE);

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

        // 불 기둥 생성
        FlamePillarZone zone = new FlamePillarZone(
            targetPosition,
            PILLAR_RADIUS * rangeMultiplier,
            getDamage(),  // 초당 데미지
            PILLAR_DURATION,
            caster
        );

        // 게임에 추가
        GameManager.getInstance().addSkillZone(zone);

        // 쿨타임 시작
        currentCooldown = getCooldown();
    }
}

/**
 * 불 기둥 구역
 *
 * 지정 위치에 생성되어 지속 데미지를 주는 구역입니다.
 */
class FlamePillarZone {
    private Vector2 position;
    private float radius;
    private int damagePerSecond;
    private float duration;
    private float elapsedTime;
    private Entity owner;

    private FlameEffect pillarEffect;

    // 데미지 적용 타이머
    private float damageTimer = 0f;
    private static final float DAMAGE_INTERVAL = 0.5f;  // 0.5초마다 데미지

    public FlamePillarZone(Vector2 position, float radius, int damagePerSecond,
                           float duration, Entity owner) {
        this.position = position;
        this.radius = radius;
        this.damagePerSecond = damagePerSecond;
        this.duration = duration;
        this.owner = owner;
        this.elapsedTime = 0f;

        // 이펙트 생성
        this.pillarEffect = new FlameEffect(FlameEffect.Type.PILLAR);
        this.pillarEffect.setPosition(position);
        this.pillarEffect.setScale(radius / 100f);
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        elapsedTime += delta;
        damageTimer += delta;

        // 이펙트 업데이트
        pillarEffect.update(delta);

        // 데미지 적용
        if (damageTimer >= DAMAGE_INTERVAL) {
            applyDamage();
            damageTimer = 0f;
        }
    }

    /**
     * 범위 내 몬스터에게 데미지
     */
    private void applyDamage() {
        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Monster monster : monsters) {
            float distance = monster.getPosition().dst(position);

            if (distance <= radius) {
                // 0.5초당 데미지 = (초당 데미지 * 0.5)
                int damage = (int) (damagePerSecond * DAMAGE_INTERVAL);
                CombatSystem.getInstance().dealDamage(owner, monster, damage);
            }
        }
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        pillarEffect.render(batch);

        // 범위 표시 (디버그용)
        // ShapeRenderer.drawCircle(position, radius);
    }

    /**
     * 구역이 아직 살아있는지 확인
     */
    public boolean isAlive() {
        return elapsedTime < duration;
    }
}
```

### 4. Meteor 스킬 (운석)

```java
/**
 * 운석 스킬
 *
 * 하늘에서 거대한 운석이 떨어져 광역 폭발 데미지를 줍니다.
 * 가장 강력하지만 긴 쿨타임을 가집니다.
 */
public class Meteor extends ElementalSkill {
    // 스킬 기본 스탯
    private static final int BASE_DAMAGE = 150;
    private static final int MANA_COST = 50;
    private static final float BASE_COOLDOWN = 15.0f;

    // 운석 설정
    private static final float METEOR_RADIUS = 200f;  // 폭발 범위
    private static final float FALL_DURATION = 1.5f;  // 낙하 시간

    /**
     * 운석 생성자
     */
    public Meteor() {
        super(103, "운석", ElementType.FIRE);

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

        // 운석 생성
        MeteorProjectile meteor = new MeteorProjectile(
            caster,
            targetPosition,
            getDamage(),
            METEOR_RADIUS * rangeMultiplier,
            FALL_DURATION
        );

        // 게임에 추가
        GameManager.getInstance().addProjectile(meteor);

        // 쿨타임 시작
        currentCooldown = getCooldown();
    }
}
```

### 5. MeteorProjectile 클래스

```java
/**
 * 운석 발사체
 *
 * 하늘에서 떨어지는 운석입니다.
 * 낙하 후 폭발하여 광역 데미지를 줍니다.
 */
public class MeteorProjectile extends Projectile {
    private Vector2 targetPosition;  // 낙하 목표 위치
    private float explosionRadius;   // 폭발 반경
    private float fallDuration;      // 낙하 시간
    private float elapsedTime;       // 경과 시간

    private boolean hasExploded = false;

    // 이펙트
    private FlameEffect warningEffect;  // 경고 표시
    private FlameEffect meteorEffect;   // 운석 본체
    private FlameEffect explosionEffect; // 폭발

    /**
     * 운석 발사체 생성자
     */
    public MeteorProjectile(Entity owner, Vector2 targetPos, int damage,
                            float explosionRadius, float fallDuration) {
        super(owner, new Vector2(targetPos.x, targetPos.y + 1000), // 하늘 위에서 시작
              new Vector2(0, -1), damage, 0, 0);

        this.targetPosition = targetPos;
        this.explosionRadius = explosionRadius;
        this.fallDuration = fallDuration;
        this.elapsedTime = 0f;

        // 경고 이펙트 (땅에 빨간 원)
        this.warningEffect = new FlameEffect(FlameEffect.Type.WARNING_CIRCLE);
        this.warningEffect.setPosition(targetPos);
        this.warningEffect.setScale(explosionRadius / 100f);

        // 운석 이펙트
        this.meteorEffect = new FlameEffect(FlameEffect.Type.METEOR);
    }

    /**
     * 매 프레임 업데이트
     */
    @Override
    public void update(float delta) {
        elapsedTime += delta;

        if (!hasExploded) {
            // 낙하 중
            float progress = elapsedTime / fallDuration;

            if (progress >= 1.0f) {
                // 착탄
                explode();
            } else {
                // 운석 위치 업데이트 (하늘 → 땅)
                float startY = targetPosition.y + 1000;
                float currentY = startY - (progress * 1000);
                position.set(targetPosition.x, currentY);

                meteorEffect.setPosition(position);
            }

            // 경고 이펙트 업데이트
            warningEffect.update(delta);
            meteorEffect.update(delta);
        } else {
            // 폭발 이펙트 재생
            explosionEffect.update(delta);

            if (explosionEffect.isFinished()) {
                this.isAlive = false;
            }
        }
    }

    /**
     * 폭발 처리
     */
    private void explode() {
        hasExploded = true;

        // 폭발 이펙트 생성
        explosionEffect = new FlameEffect(FlameEffect.Type.EXPLOSION);
        explosionEffect.setPosition(targetPosition);
        explosionEffect.setScale(explosionRadius / 100f);

        // 범위 내 모든 몬스터에게 데미지
        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Monster monster : monsters) {
            float distance = monster.getPosition().dst(targetPosition);

            if (distance <= explosionRadius) {
                // 거리에 따라 데미지 감소 (중심부 100%, 가장자리 50%)
                float damageMultiplier = 1.0f - (distance / explosionRadius * 0.5f);
                int finalDamage = (int) (damage * damageMultiplier);

                CombatSystem.getInstance().dealDamage(owner, monster, finalDamage);
            }
        }

        // 경고 이펙트 제거
        warningEffect.setAlive(false);
        meteorEffect.setAlive(false);
    }

    /**
     * 렌더링
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!hasExploded) {
            warningEffect.render(batch);
            meteorEffect.render(batch);
        } else {
            explosionEffect.render(batch);
        }
    }

    @Override
    public void onHit(Entity target) {
        // 운석은 충돌 무시 (폭발만 데미지)
    }
}
```

### 6. FlameEffect 클래스

```java
/**
 * 화염 이펙트
 *
 * 불 스킬에 사용되는 다양한 이펙트입니다.
 */
public class FlameEffect {
    private Type type;
    private Vector2 position;
    private float scale = 1.0f;
    private float lifetime = 0f;
    private float maxLifetime;
    private boolean isAlive = true;

    // 파티클 시스템 (향후 구현)
    // private ParticleEffect particleEffect;

    /**
     * 이펙트 타입
     */
    public enum Type {
        TRAIL,           // 발사체 궤적
        EXPLOSION,       // 폭발
        PILLAR,          // 불 기둥
        METEOR,          // 운석
        WARNING_CIRCLE   // 경고 원
    }

    public FlameEffect(Type type) {
        this.type = type;
        this.position = new Vector2();

        // 타입별 수명 설정
        switch (type) {
            case TRAIL:
                maxLifetime = 0.3f;
                break;
            case EXPLOSION:
                maxLifetime = 0.5f;
                break;
            case PILLAR:
                maxLifetime = Float.MAX_VALUE;  // 외부에서 제어
                break;
            case METEOR:
                maxLifetime = Float.MAX_VALUE;
                break;
            case WARNING_CIRCLE:
                maxLifetime = Float.MAX_VALUE;
                break;
        }
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        lifetime += delta;

        if (lifetime >= maxLifetime) {
            isAlive = false;
        }

        // 파티클 업데이트 (향후)
        // particleEffect.update(delta);
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        // 파티클 렌더링 (향후)
        // particleEffect.draw(batch);

        // 임시: 색상 사각형
        batch.setColor(1f, 0.5f, 0f, 1f - (lifetime / maxLifetime));
        // batch.draw(whitepixel, position.x, position.y, 32 * scale, 32 * scale);
        batch.setColor(1, 1, 1, 1);
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isFinished() {
        return !isAlive;
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
 * Fireball 테스트
 */
public class TestFireball {
    private Fireball fireball;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        fireball = new Fireball();
        testPlayer = new Player(1);
        testPlayer.getStats().setMana(100);
    }

    @Test
    public void 파이어볼_시전_시_발사체_생성() {
        Vector2 target = new Vector2(500, 500);
        fireball.cast(testPlayer, target);

        List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
        assertEquals(1, projectiles.size());
        assertTrue(projectiles.get(0) instanceof FireballProjectile);
    }

    @Test
    public void 마나_부족_시_시전_불가() {
        testPlayer.getStats().setMana(5);  // 부족한 마나

        Vector2 target = new Vector2(500, 500);
        fireball.cast(testPlayer, target);

        List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
        assertEquals(0, projectiles.size());
    }

    @Test
    public void 쿨타임_중_시전_불가() {
        Vector2 target = new Vector2(500, 500);

        // 첫 시전
        fireball.cast(testPlayer, target);

        // 즉시 재시전 시도
        fireball.cast(testPlayer, target);

        // 발사체는 1개만
        assertEquals(1, GameManager.getInstance().getProjectiles().size());
    }
}

/**
 * FlamePillar 테스트
 */
public class TestFlamePillar {
    private FlamePillar flamePillar;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        flamePillar = new FlamePillar();
        testPlayer = new Player(1);
        testPlayer.getStats().setMana(100);
    }

    @Test
    public void 불_기둥_생성() {
        Vector2 target = new Vector2(500, 500);
        flamePillar.cast(testPlayer, target);

        List<FlamePillarZone> zones = GameManager.getInstance().getSkillZones();
        assertEquals(1, zones.size());
    }

    @Test
    public void 범위_내_몬스터_데미지() {
        Vector2 target = new Vector2(500, 500);
        flamePillar.cast(testPlayer, target);

        // 몬스터 생성 (범위 내)
        Monster monster = new Ghost();
        monster.setPosition(510, 510);  // 기둥 근처
        GameManager.getInstance().addMonster(monster);

        int originalHP = monster.getHealth();

        // 0.5초 경과 (데미지 1회)
        GameManager.getInstance().update(0.5f);

        assertTrue(monster.getHealth() < originalHP);
    }

    @Test
    public void 지속_시간_후_사라짐() {
        Vector2 target = new Vector2(500, 500);
        flamePillar.cast(testPlayer, target);

        FlamePillarZone zone = GameManager.getInstance().getSkillZones().get(0);

        // 3초 경과
        for (int i = 0; i < 6; i++) {
            zone.update(0.5f);
        }

        assertFalse(zone.isAlive());
    }
}

/**
 * Meteor 테스트
 */
public class TestMeteor {
    private Meteor meteor;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        meteor = new Meteor();
        testPlayer = new Player(1);
        testPlayer.getStats().setMana(100);
    }

    @Test
    public void 운석_시전() {
        Vector2 target = new Vector2(500, 500);
        meteor.cast(testPlayer, target);

        List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
        assertEquals(1, projectiles.size());
        assertTrue(projectiles.get(0) instanceof MeteorProjectile);
    }

    @Test
    public void 운석_낙하_및_폭발() {
        Vector2 target = new Vector2(500, 500);
        meteor.cast(testPlayer, target);

        MeteorProjectile meteorProj = (MeteorProjectile)
            GameManager.getInstance().getProjectiles().get(0);

        // 1.5초 경과 (낙하 완료)
        meteorProj.update(1.5f);

        assertTrue(meteorProj.hasExploded);
    }

    @Test
    public void 광역_데미지_적용() {
        Vector2 target = new Vector2(500, 500);
        meteor.cast(testPlayer, target);

        // 몬스터 3마리 생성 (폭발 범위 내, 경계, 범위 밖)
        Monster m1 = new Ghost();
        m1.setPosition(500, 500);  // 중심
        Monster m2 = new Ghost();
        m2.setPosition(650, 500);  // 경계
        Monster m3 = new Ghost();
        m3.setPosition(800, 500);  // 범위 밖

        GameManager.getInstance().addMonster(m1);
        GameManager.getInstance().addMonster(m2);
        GameManager.getInstance().addMonster(m3);

        int hp1 = m1.getHealth();
        int hp2 = m2.getHealth();
        int hp3 = m3.getHealth();

        // 운석 낙하
        MeteorProjectile meteorProj = (MeteorProjectile)
            GameManager.getInstance().getProjectiles().get(0);
        meteorProj.update(1.5f);

        // 중심 몬스터: 최대 데미지
        assertTrue(m1.getHealth() < hp1);

        // 경계 몬스터: 감소된 데미지
        assertTrue(m2.getHealth() < hp2);
        assertTrue(m2.getHealth() > m1.getHealth());

        // 범위 밖 몬스터: 데미지 없음
        assertEquals(hp3, m3.getHealth());
    }
}
```

---

## ✅ 완료 조건

- [ ] Fireball 스킬 구현
- [ ] FlamePillar 스킬 구현
- [ ] Meteor 스킬 구현
- [ ] FireballProjectile 구현
- [ ] MeteorProjectile 구현
- [ ] FlameEffect 이펙트 구현
- [ ] 데미지 적용 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_15: 물 속성 스킬**
- 아이스 샤드 (Ice Shard)
- 물 방어막 (Water Shield)
- 파도 (Tidal Wave)
