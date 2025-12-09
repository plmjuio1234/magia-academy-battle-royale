# PHASE_18_SKILL_EARTH.md - 흙 속성 스킬

---

## 🎯 목표
흙 원소의 3가지 스킬 구현 (바위 던지기, 지진, 흙 갑옷)

---

## 📋 구현 범위

### 흙 스킬 3종
- ✅ 스킬 A: 바위 던지기 (높은 데미지 발사체 + 스턴)
- ✅ 스킬 B: 지진 (광역 지속 데미지 + 슬로우)
- ✅ 스킬 C: 흙 갑옷 (방어력 증가 + HP 재생)

---

## 📁 필요 파일

```
game/skill/earth/
  ├─ RockThrow.java
  ├─ Earthquake.java
  └─ EarthArmor.java

game/projectile/
  └─ RockProjectile.java

game/buff/
  ├─ StunnedBuff.java
  └─ EarthArmorBuff.java

game/effect/
  └─ EarthEffect.java
```

---

## 🔧 구현 가이드

### 1. RockThrow 스킬 (바위 던지기)

```java
/**
 * 바위 던지기 스킬
 *
 * 무거운 바위를 던져 높은 데미지와 스턴을 줍니다.
 */
public class RockThrow extends ElementalSkill {
    private static final int BASE_DAMAGE = 60;
    private static final int MANA_COST = 25;
    private static final float BASE_COOLDOWN = 4.5f;

    private static final float PROJECTILE_SPEED = 400f;
    private static final float PROJECTILE_RANGE = 700f;
    private static final float STUN_DURATION = 1.0f;

    public RockThrow() {
        super(501, "바위 던지기", ElementType.EARTH);
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

        RockProjectile rock = new RockProjectile(
            caster,
            caster.getPosition().cpy(),
            direction,
            getDamage(),
            PROJECTILE_SPEED,
            PROJECTILE_RANGE * rangeMultiplier,
            STUN_DURATION
        );

        GameManager.getInstance().addProjectile(rock);
        currentCooldown = getCooldown();
    }
}

/**
 * 바위 발사체
 */
class RockProjectile extends Projectile {
    private float stunDuration;
    private EarthEffect rockEffect;

    public RockProjectile(Entity owner, Vector2 startPos, Vector2 direction,
                          int damage, float speed, float maxRange, float stunDuration) {
        super(owner, startPos, direction, damage, speed, maxRange);
        this.stunDuration = stunDuration;
        this.setSize(48, 48);

        this.rockEffect = new EarthEffect(EarthEffect.Type.ROCK);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        rockEffect.setPosition(position);
        rockEffect.setRotation(lifetime * 360f);  // 회전
        rockEffect.update(delta);

        if (distanceTraveled >= maxRange) {
            this.isAlive = false;
        }
    }

    @Override
    public void onHit(Entity target) {
        if (!(target instanceof Monster)) return;

        Monster monster = (Monster) target;

        // 데미지 적용
        CombatSystem.getInstance().dealDamage(owner, monster, damage);

        // 스턴 적용
        StunnedBuff stun = new StunnedBuff(stunDuration);
        monster.addBuff(stun);

        // 충돌 이펙트
        EarthEffect impactEffect = new EarthEffect(EarthEffect.Type.ROCK_IMPACT);
        impactEffect.setPosition(position);
        GameManager.getInstance().addEffect(impactEffect);

        this.isAlive = false;
    }

    @Override
    public void render(SpriteBatch batch) {
        rockEffect.render(batch);
        super.render(batch);
    }
}
```

### 2. StunnedBuff 클래스

```java
/**
 * 스턴 버프
 *
 * 대상의 이동과 공격을 막습니다.
 */
public class StunnedBuff extends Buff {
    public StunnedBuff(float duration) {
        super(BuffType.STUNNED, duration);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (duration <= 0) {
            this.isActive = false;
        }
    }

    /**
     * 스턴 상태인지 확인
     */
    public boolean isStunned() {
        return isActive;
    }
}
```

### 3. Earthquake 스킬 (지진)

```java
/**
 * 지진 스킬
 *
 * 플레이어 주변에 지진을 일으켜 광역 지속 데미지와 슬로우를 줍니다.
 */
public class Earthquake extends ElementalSkill {
    private static final int BASE_DAMAGE = 30;  // 초당 데미지
    private static final int MANA_COST = 40;
    private static final float BASE_COOLDOWN = 10.0f;

    private static final float QUAKE_RADIUS = 250f;
    private static final float QUAKE_DURATION = 4.0f;
    private static final float SLOW_AMOUNT = 0.4f;  // 40% 감속

    public Earthquake() {
        super(502, "지진", ElementType.EARTH);
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

        // 플레이어 위치 중심으로 지진 발생
        EarthquakeZone quake = new EarthquakeZone(
            caster.getPosition().cpy(),
            QUAKE_RADIUS * rangeMultiplier,
            getDamage(),
            QUAKE_DURATION,
            SLOW_AMOUNT,
            caster
        );

        GameManager.getInstance().addSkillZone(quake);
        currentCooldown = getCooldown();
    }
}

/**
 * 지진 구역
 */
class EarthquakeZone {
    private Vector2 position;
    private float radius;
    private int damagePerSecond;
    private float duration;
    private float slowAmount;
    private Entity owner;

    private float elapsedTime = 0f;
    private float damageTimer = 0f;
    private static final float DAMAGE_INTERVAL = 0.5f;

    private EarthEffect quakeEffect;

    public EarthquakeZone(Vector2 position, float radius, int damagePerSecond,
                          float duration, float slowAmount, Entity owner) {
        this.position = position;
        this.radius = radius;
        this.damagePerSecond = damagePerSecond;
        this.duration = duration;
        this.slowAmount = slowAmount;
        this.owner = owner;

        this.quakeEffect = new EarthEffect(EarthEffect.Type.EARTHQUAKE);
        this.quakeEffect.setPosition(position);
        this.quakeEffect.setScale(radius / 100f);
    }

    public void update(float delta) {
        elapsedTime += delta;
        damageTimer += delta;

        quakeEffect.update(delta);

        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Monster monster : monsters) {
            float distance = monster.getPosition().dst(position);

            if (distance <= radius) {
                // 데미지 적용
                if (damageTimer >= DAMAGE_INTERVAL) {
                    int damage = (int) (damagePerSecond * DAMAGE_INTERVAL);
                    CombatSystem.getInstance().dealDamage(owner, monster, damage);
                }

                // 슬로우 적용 (기존 감전과 유사)
                if (!monster.hasBuff(BuffType.SLOWED)) {
                    SlowedBuff slow = new SlowedBuff(slowAmount, 0.5f);
                    monster.addBuff(slow);
                }
            }
        }

        if (damageTimer >= DAMAGE_INTERVAL) {
            damageTimer = 0f;
        }
    }

    public void render(SpriteBatch batch) {
        quakeEffect.render(batch);
    }

    public boolean isAlive() {
        return elapsedTime < duration;
    }
}

/**
 * 슬로우 버프
 */
class SlowedBuff extends Buff {
    private float slowAmount;

    public SlowedBuff(float slowAmount, float duration) {
        super(BuffType.SLOWED, duration);
        this.slowAmount = slowAmount;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (duration <= 0) {
            this.isActive = false;
        }
    }

    public float getSpeedMultiplier() {
        return 1.0f - slowAmount;
    }
}
```

### 4. EarthArmor 스킬 (흙 갑옷)

```java
/**
 * 흙 갑옷 스킬
 *
 * 플레이어에게 흙 갑옷을 부여하여 방어력을 증가시키고 HP를 재생합니다.
 */
public class EarthArmor extends ElementalSkill {
    private static final int MANA_COST = 35;
    private static final float BASE_COOLDOWN = 12.0f;

    private static final float ARMOR_DURATION = 8.0f;
    private static final int DEFENSE_BONUS = 20;
    private static final int HP_REGEN_PER_SEC = 10;

    public EarthArmor() {
        super(503, "흙 갑옷", ElementType.EARTH);
        this.baseDamage = 0;  // 데미지 없음
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (caster.getStats().getMana() < manaCost || currentCooldown > 0) {
            return;
        }

        // 이미 갑옷 버프가 있으면 무시
        if (caster.hasBuff(BuffType.EARTH_ARMOR)) {
            return;
        }

        caster.getStats().consumeMana(manaCost);

        // 갑옷 버프 추가
        EarthArmorBuff armor = new EarthArmorBuff(
            ARMOR_DURATION,
            DEFENSE_BONUS,
            HP_REGEN_PER_SEC
        );
        caster.addBuff(armor);

        // 갑옷 이펙트
        EarthEffect armorEffect = new EarthEffect(EarthEffect.Type.ARMOR);
        armorEffect.setPosition(caster.getPosition());
        armorEffect.attachTo(caster);
        GameManager.getInstance().addEffect(armorEffect);

        currentCooldown = getCooldown();
    }
}
```

### 5. EarthArmorBuff 클래스

```java
/**
 * 흙 갑옷 버프
 *
 * 방어력 증가 + HP 재생
 */
public class EarthArmorBuff extends Buff {
    private int defenseBonus;
    private int hpRegenPerSec;
    private float regenTimer = 0f;

    public EarthArmorBuff(float duration, int defenseBonus, int hpRegenPerSec) {
        super(BuffType.EARTH_ARMOR, duration);
        this.defenseBonus = defenseBonus;
        this.hpRegenPerSec = hpRegenPerSec;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // HP 재생 (1초마다)
        regenTimer += delta;
        if (regenTimer >= 1.0f) {
            applyRegen();
            regenTimer = 0f;
        }

        if (duration <= 0) {
            this.isActive = false;
        }
    }

    /**
     * HP 재생 적용
     */
    private void applyRegen() {
        if (owner instanceof Player) {
            Player player = (Player) owner;
            int currentHP = player.getStats().getHealth();
            int maxHP = player.getStats().getMaxHealth();

            int newHP = Math.min(currentHP + hpRegenPerSec, maxHP);
            player.getStats().setHealth(newHP);
        }
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public int getHpRegenPerSec() {
        return hpRegenPerSec;
    }
}
```

### 6. EarthEffect 클래스

```java
/**
 * 흙 이펙트
 */
public class EarthEffect {
    private Type type;
    private Vector2 position;
    private float rotation = 0f;
    private float scale = 1.0f;
    private float lifetime = 0f;
    private float maxLifetime;
    private boolean isAlive = true;
    private Entity attachedTo = null;

    public enum Type {
        ROCK,           // 바위
        ROCK_IMPACT,    // 바위 충돌
        EARTHQUAKE,     // 지진
        ARMOR           // 갑옷
    }

    public EarthEffect(Type type) {
        this.type = type;
        this.position = new Vector2();

        switch (type) {
            case ROCK:
                maxLifetime = Float.MAX_VALUE;  // 발사체와 함께
                break;
            case ROCK_IMPACT:
                maxLifetime = 0.4f;
                break;
            case EARTHQUAKE:
                maxLifetime = Float.MAX_VALUE;
                break;
            case ARMOR:
                maxLifetime = Float.MAX_VALUE;  // 버프와 함께
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

        float alpha = 1f;
        if (maxLifetime != Float.MAX_VALUE) {
            alpha = 1f - (lifetime / maxLifetime);
        }

        switch (type) {
            case ROCK:
                batch.setColor(0.6f, 0.4f, 0.2f, 1f);
                break;
            case ROCK_IMPACT:
                batch.setColor(0.5f, 0.3f, 0.1f, alpha);
                break;
            case EARTHQUAKE:
                batch.setColor(0.4f, 0.3f, 0.2f, 0.6f);
                break;
            case ARMOR:
                batch.setColor(0.7f, 0.5f, 0.3f, 0.5f);
                break;
        }

        batch.setColor(1, 1, 1, 1);
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
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
public class TestRockThrow {
    @Test
    public void 바위_던지기_스턴() {
        RockThrow rockThrow = new RockThrow();
        Player player = new Player(1);
        player.setPosition(100, 100);
        player.getStats().setMana(100);

        Monster monster = new Ghost();
        monster.setPosition(300, 100);

        GameManager.getInstance().addMonster(monster);

        rockThrow.cast(player, new Vector2(300, 100));

        RockProjectile rock = (RockProjectile)
            GameManager.getInstance().getProjectiles().get(0);

        // 충돌 시뮬레이션
        rock.onHit(monster);

        // 스턴 확인
        assertTrue(monster.hasBuff(BuffType.STUNNED));
    }
}

public class TestEarthquake {
    @Test
    public void 지진_광역_데미지() {
        Earthquake earthquake = new Earthquake();
        Player player = new Player(1);
        player.setPosition(300, 300);
        player.getStats().setMana(100);

        // 범위 내 몬스터 배치
        Monster m1 = new Ghost();
        m1.setPosition(350, 300);
        Monster m2 = new Ghost();
        m2.setPosition(450, 300);  // 범위 밖

        GameManager.getInstance().addMonster(m1);
        GameManager.getInstance().addMonster(m2);

        earthquake.cast(player, player.getPosition());

        EarthquakeZone zone = GameManager.getInstance().getSkillZones().get(0);

        int hp1 = m1.getHealth();
        int hp2 = m2.getHealth();

        zone.update(0.5f);

        // 범위 내만 데미지
        assertTrue(m1.getHealth() < hp1);
        assertEquals(hp2, m2.getHealth());
    }
}

public class TestEarthArmor {
    @Test
    public void 흙_갑옷_방어력_증가() {
        EarthArmor armor = new EarthArmor();
        Player player = new Player(1);
        player.getStats().setMana(100);

        int originalDef = player.getStats().getDefense();

        armor.cast(player, player.getPosition());

        EarthArmorBuff buff = (EarthArmorBuff) player.getBuff(BuffType.EARTH_ARMOR);
        assertNotNull(buff);
        assertEquals(20, buff.getDefenseBonus());
    }

    @Test
    public void 흙_갑옷_HP_재생() {
        EarthArmor armor = new EarthArmor();
        Player player = new Player(1);
        player.getStats().setMana(100);
        player.getStats().setHealth(50);  // 절반

        armor.cast(player, player.getPosition());

        EarthArmorBuff buff = (EarthArmorBuff) player.getBuff(BuffType.EARTH_ARMOR);

        // 1초 경과
        buff.update(1.0f);

        // HP 증가
        assertTrue(player.getStats().getHealth() > 50);
    }
}
```

---

## ✅ 완료 조건

- [ ] RockThrow 스킬 구현
- [ ] Earthquake 스킬 구현
- [ ] EarthArmor 스킬 구현
- [ ] StunnedBuff 구현
- [ ] EarthArmorBuff 구현
- [ ] EarthEffect 이펙트 구현
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_19: 스킬 업그레이드 시스템**
- 경험치로 스킬 레벨업
- 3가지 업그레이드 옵션 (데미지/범위/쿨타임)
- 업그레이드 UI
