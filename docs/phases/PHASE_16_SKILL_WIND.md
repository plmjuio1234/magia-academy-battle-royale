# PHASE_16_SKILL_WIND.md - 바람 속성 스킬

---

## 🎯 목표
바람 원소의 3가지 스킬 구현 (회오리, 바람 베기, 질주)

---

## 📋 구현 범위

### 바람 스킬 3종
- ✅ 스킬 A: 회오리 (적을 끌어당기는 지속 범위)
- ✅ 스킬 B: 바람 베기 (빠른 직선 관통 공격)
- ✅ 스킬 C: 질주 (순간이동 + 무적)

### 공통 기능
- ✅ 끌어당기기 (Pull) 효과
- ✅ 고속 발사체
- ✅ 이동 기술 (Dash)

---

## 📁 필요 파일

```
game/skill/wind/
  ├─ Tornado.java
  ├─ WindSlash.java
  └─ Dash.java

game/projectile/
  └─ WindSlashProjectile.java

game/effect/
  └─ WindEffect.java
```

---

## 🔧 구현 가이드

### 1. Tornado 스킬 (회오리)

```java
/**
 * 회오리 스킬
 *
 * 지정 위치에 회오리를 생성하여 주변 적을 끌어당기며 지속 데미지를 줍니다.
 */
public class Tornado extends ElementalSkill {
    private static final int BASE_DAMAGE = 20;      // 초당 데미지
    private static final int MANA_COST = 30;
    private static final float BASE_COOLDOWN = 7.0f;

    private static final float TORNADO_RADIUS = 150f;
    private static final float TORNADO_DURATION = 4.0f;
    private static final float PULL_FORCE = 200f;  // 끌어당기는 힘

    public Tornado() {
        super(301, "회오리", ElementType.WIND);
        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (caster.getStats().getMana() < manaCost || currentCooldown > 0) {
            return;
        }

        caster.getStats().consumeMana(manaCost);

        TornadoZone tornado = new TornadoZone(
            targetPosition,
            TORNADO_RADIUS * rangeMultiplier,
            getDamage(),
            TORNADO_DURATION,
            PULL_FORCE,
            caster
        );

        GameManager.getInstance().addSkillZone(tornado);
        currentCooldown = getCooldown();
    }
}

/**
 * 회오리 구역
 */
class TornadoZone {
    private Vector2 position;
    private float radius;
    private int damagePerSecond;
    private float duration;
    private float pullForce;
    private Entity owner;

    private float elapsedTime = 0f;
    private float damageTimer = 0f;
    private static final float DAMAGE_INTERVAL = 0.5f;

    private WindEffect tornadoEffect;

    public TornadoZone(Vector2 position, float radius, int damagePerSecond,
                       float duration, float pullForce, Entity owner) {
        this.position = position;
        this.radius = radius;
        this.damagePerSecond = damagePerSecond;
        this.duration = duration;
        this.pullForce = pullForce;
        this.owner = owner;

        this.tornadoEffect = new WindEffect(WindEffect.Type.TORNADO);
        this.tornadoEffect.setPosition(position);
        this.tornadoEffect.setScale(radius / 100f);
    }

    public void update(float delta) {
        elapsedTime += delta;
        damageTimer += delta;

        tornadoEffect.update(delta);

        // 몬스터 끌어당기기 + 데미지
        List<Monster> monsters = GameManager.getInstance().getMonsters();
        for (Monster monster : monsters) {
            float distance = monster.getPosition().dst(position);

            if (distance <= radius) {
                // 끌어당기기
                Vector2 pullDirection = new Vector2(position).sub(monster.getPosition()).nor();
                Vector2 pullVelocity = pullDirection.scl(pullForce * delta);
                monster.applyForce(pullVelocity);

                // 데미지 (주기적)
                if (damageTimer >= DAMAGE_INTERVAL) {
                    int damage = (int) (damagePerSecond * DAMAGE_INTERVAL);
                    CombatSystem.getInstance().dealDamage(owner, monster, damage);
                }
            }
        }

        if (damageTimer >= DAMAGE_INTERVAL) {
            damageTimer = 0f;
        }
    }

    public void render(SpriteBatch batch) {
        tornadoEffect.render(batch);
    }

    public boolean isAlive() {
        return elapsedTime < duration;
    }
}
```

### 2. WindSlash 스킬 (바람 베기)

```java
/**
 * 바람 베기 스킬
 *
 * 매우 빠른 속도로 직선을 관통하는 바람 칼날을 발사합니다.
 */
public class WindSlash extends ElementalSkill {
    private static final int BASE_DAMAGE = 45;
    private static final int MANA_COST = 20;
    private static final float BASE_COOLDOWN = 3.5f;

    private static final float PROJECTILE_SPEED = 1000f;  // 매우 빠름
    private static final float PROJECTILE_RANGE = 900f;
    private static final int PIERCE_COUNT = 5;  // 5마리 관통

    public WindSlash() {
        super(302, "바람 베기", ElementType.WIND);
        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (caster.getStats().getMana() < manaCost || currentCooldown > 0) {
            return;
        }

        caster.getStats().consumeMana(manaCost);

        Vector2 direction = new Vector2(targetPosition).sub(caster.getPosition()).nor();

        WindSlashProjectile projectile = new WindSlashProjectile(
            caster,
            caster.getPosition().cpy(),
            direction,
            getDamage(),
            PROJECTILE_SPEED,
            PROJECTILE_RANGE * rangeMultiplier,
            PIERCE_COUNT
        );

        GameManager.getInstance().addProjectile(projectile);
        currentCooldown = getCooldown();
    }
}

/**
 * 바람 베기 발사체
 */
class WindSlashProjectile extends Projectile {
    private int pierceCount;
    private Set<Integer> hitTargets;
    private WindEffect slashEffect;

    public WindSlashProjectile(Entity owner, Vector2 startPos, Vector2 direction,
                               int damage, float speed, float maxRange, int pierceCount) {
        super(owner, startPos, direction, damage, speed, maxRange);
        this.pierceCount = pierceCount;
        this.hitTargets = new HashSet<>();

        this.setSize(64, 16);  // 길쭉한 형태

        this.slashEffect = new WindEffect(WindEffect.Type.SLASH_TRAIL);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        slashEffect.setPosition(position);
        slashEffect.update(delta);

        if (distanceTraveled >= maxRange) {
            this.isAlive = false;
        }
    }

    @Override
    public void onHit(Entity target) {
        if (!(target instanceof Monster)) return;

        Monster monster = (Monster) target;
        if (hitTargets.contains(monster.getId())) return;

        CombatSystem.getInstance().dealDamage(owner, monster, damage);
        hitTargets.add(monster.getId());

        pierceCount--;
        if (pierceCount <= 0) {
            this.isAlive = false;
        }

        // 베임 이펙트
        WindEffect cutEffect = new WindEffect(WindEffect.Type.CUT);
        cutEffect.setPosition(monster.getPosition());
        GameManager.getInstance().addEffect(cutEffect);
    }

    @Override
    public void render(SpriteBatch batch) {
        slashEffect.render(batch);
        super.render(batch);
    }
}
```

### 3. Dash 스킬 (질주)

```java
/**
 * 질주 스킬
 *
 * 목표 방향으로 빠르게 이동하며 짧은 시간 무적 상태가 됩니다.
 * 유틸리티 스킬로 데미지는 없습니다.
 */
public class Dash extends ElementalSkill {
    private static final int MANA_COST = 25;
    private static final float BASE_COOLDOWN = 5.0f;

    private static final float DASH_DISTANCE = 300f;
    private static final float DASH_DURATION = 0.3f;
    private static final float INVINCIBLE_DURATION = 0.5f;

    public Dash() {
        super(303, "질주", ElementType.WIND);
        this.baseDamage = 0;  // 데미지 없음
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (caster.getStats().getMana() < manaCost || currentCooldown > 0) {
            return;
        }

        caster.getStats().consumeMana(manaCost);

        // 이동 방향 계산
        Vector2 direction = new Vector2(targetPosition).sub(caster.getPosition()).nor();
        Vector2 dashTarget = caster.getPosition().cpy().add(
            direction.scl(DASH_DISTANCE * rangeMultiplier)
        );

        // 맵 경계 체크
        dashTarget.x = Math.max(0, Math.min(dashTarget.x, Constants.MAP_WIDTH));
        dashTarget.y = Math.max(0, Math.min(dashTarget.y, Constants.MAP_HEIGHT));

        // 대시 실행
        caster.performDash(dashTarget, DASH_DURATION);

        // 무적 버프 추가
        InvincibleBuff invincible = new InvincibleBuff(INVINCIBLE_DURATION);
        caster.addBuff(invincible);

        // 잔상 이펙트
        WindEffect dashEffect = new WindEffect(WindEffect.Type.DASH_TRAIL);
        dashEffect.setPosition(caster.getPosition());
        dashEffect.attachTo(caster);
        GameManager.getInstance().addEffect(dashEffect);

        currentCooldown = getCooldown();
    }
}

/**
 * 무적 버프
 */
class InvincibleBuff extends Buff {
    public InvincibleBuff(float duration) {
        super(BuffType.INVINCIBLE, duration);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (duration <= 0) {
            this.isActive = false;
        }
    }
}
```

### 4. WindEffect 클래스

```java
/**
 * 바람 이펙트
 */
public class WindEffect {
    private Type type;
    private Vector2 position;
    private float scale = 1.0f;
    private float lifetime = 0f;
    private float maxLifetime;
    private boolean isAlive = true;
    private Entity attachedTo = null;

    public enum Type {
        TORNADO,        // 회오리
        SLASH_TRAIL,    // 베기 궤적
        CUT,            // 베임
        DASH_TRAIL      // 질주 잔상
    }

    public WindEffect(Type type) {
        this.type = type;
        this.position = new Vector2();

        switch (type) {
            case TORNADO:
                maxLifetime = Float.MAX_VALUE;
                break;
            case SLASH_TRAIL:
                maxLifetime = 0.2f;
                break;
            case CUT:
                maxLifetime = 0.3f;
                break;
            case DASH_TRAIL:
                maxLifetime = 0.5f;
                break;
        }
    }

    public void update(float delta) {
        lifetime += delta;

        if (attachedTo != null) {
            position.set(attachedTo.getPosition());
        }

        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        float alpha = 1f - (lifetime / maxLifetime);

        switch (type) {
            case TORNADO:
                batch.setColor(0.7f, 1f, 0.7f, 0.6f);
                break;
            case SLASH_TRAIL:
                batch.setColor(0.9f, 1f, 0.9f, alpha);
                break;
            case CUT:
                batch.setColor(0.8f, 1f, 0.8f, alpha);
                break;
            case DASH_TRAIL:
                batch.setColor(0.6f, 1f, 0.6f, alpha * 0.5f);
                break;
        }

        batch.setColor(1, 1, 1, 1);
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
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
}
```

---

## 🧪 테스트 계획

```java
public class TestTornado {
    @Test
    public void 회오리_생성_및_끌어당기기() {
        Tornado tornado = new Tornado();
        Player player = new Player(1);
        player.getStats().setMana(100);

        Monster monster = new Ghost();
        monster.setPosition(300, 300);
        Vector2 originalPos = monster.getPosition().cpy();

        GameManager.getInstance().addMonster(monster);

        // 회오리 시전 (200, 200)
        tornado.cast(player, new Vector2(200, 200));

        TornadoZone zone = GameManager.getInstance().getSkillZones().get(0);
        zone.update(0.1f);

        // 몬스터가 회오리 중심으로 끌려감
        float newDistance = monster.getPosition().dst(200, 200);
        float oldDistance = originalPos.dst(200, 200);
        assertTrue(newDistance < oldDistance);
    }
}

public class TestWindSlash {
    @Test
    public void 바람_베기_관통() {
        WindSlash windSlash = new WindSlash();
        Player player = new Player(1);
        player.getStats().setMana(100);

        // 5마리 일렬 배치
        for (int i = 0; i < 5; i++) {
            Monster m = new Ghost();
            m.setPosition(200 + i * 50, 200);
            GameManager.getInstance().addMonster(m);
        }

        windSlash.cast(player, new Vector2(500, 200));

        WindSlashProjectile proj = (WindSlashProjectile)
            GameManager.getInstance().getProjectiles().get(0);

        // 모든 몬스터 통과
        for (int i = 0; i < 10; i++) {
            proj.update(0.05f);
        }

        assertEquals(5, proj.hitTargets.size());
    }
}

public class TestDash {
    @Test
    public void 질주_이동() {
        Dash dash = new Dash();
        Player player = new Player(1);
        player.setPosition(100, 100);
        player.getStats().setMana(100);

        Vector2 originalPos = player.getPosition().cpy();

        dash.cast(player, new Vector2(500, 100));

        // 플레이어가 이동함
        assertNotEquals(originalPos, player.getPosition());
    }

    @Test
    public void 질주_무적() {
        Dash dash = new Dash();
        Player player = new Player(1);
        player.getStats().setMana(100);

        dash.cast(player, new Vector2(500, 100));

        assertTrue(player.hasBuff(BuffType.INVINCIBLE));
    }
}
```

---

## ✅ 완료 조건

- [ ] Tornado 스킬 구현
- [ ] WindSlash 스킬 구현
- [ ] Dash 스킬 구현
- [ ] 끌어당기기 효과 구현
- [ ] 무적 버프 구현
- [ ] WindEffect 이펙트 구현
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_17: 번개 속성 스킬**
- 번개 (Lightning Bolt)
- 체인 라이트닝 (Chain Lightning)
- 전자기장 (Electric Field)
