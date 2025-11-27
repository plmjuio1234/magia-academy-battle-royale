# PHASE_17_SKILL_LIGHTNING.md - 번개 속성 스킬

---

## 🎯 목표
번개 원소의 3가지 스킬 구현 (번개, 체인 라이트닝, 전자기장)

---

## 📋 구현 범위

### 번개 스킬 3종
- ✅ 스킬 A: 번개 (즉발 단일 대상 고데미지)
- ✅ 스킬 B: 체인 라이트닝 (연쇄 공격)
- ✅ 스킬 C: 전자기장 (감전 지속 데미지 + 슬로우)

---

## 📁 필요 파일

```
game/skill/lightning/
  ├─ LightningBolt.java
  ├─ ChainLightning.java
  └─ ElectricField.java

game/effect/
  └─ LightningEffect.java

game/buff/
  └─ ElectrocutedBuff.java
```

---

## 🔧 구현 가이드

### 1. LightningBolt 스킬 (번개)

```java
/**
 * 번개 스킬
 *
 * 즉시 목표 대상에게 강력한 번개를 떨어뜨립니다.
 * 발사체가 아닌 즉발 스킬입니다.
 */
public class LightningBolt extends ElementalSkill {
    private static final int BASE_DAMAGE = 70;
    private static final int MANA_COST = 25;
    private static final float BASE_COOLDOWN = 4.0f;

    private static final float MAX_RANGE = 700f;

    public LightningBolt() {
        super(401, "번개", ElementType.LIGHTNING);
        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (caster.getStats().getMana() < manaCost || currentCooldown > 0) {
            return;
        }

        // 범위 내 가장 가까운 몬스터 찾기
        Monster target = findNearestMonster(caster.getPosition(), MAX_RANGE * rangeMultiplier);

        if (target == null) {
            return;  // 대상 없음
        }

        caster.getStats().consumeMana(manaCost);

        // 즉시 데미지 적용
        CombatSystem.getInstance().dealDamage(caster, target, getDamage());

        // 번개 이펙트
        LightningEffect strikeEffect = new LightningEffect(LightningEffect.Type.STRIKE);
        strikeEffect.setPosition(target.getPosition());
        GameManager.getInstance().addEffect(strikeEffect);

        currentCooldown = getCooldown();
    }

    /**
     * 범위 내 가장 가까운 몬스터 찾기
     */
    private Monster findNearestMonster(Vector2 fromPos, float maxRange) {
        List<Monster> monsters = GameManager.getInstance().getMonsters();
        Monster nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Monster monster : monsters) {
            float distance = monster.getPosition().dst(fromPos);
            if (distance <= maxRange && distance < minDistance) {
                nearest = monster;
                minDistance = distance;
            }
        }

        return nearest;
    }
}
```

### 2. ChainLightning 스킬 (체인 라이트닝)

```java
/**
 * 체인 라이트닝 스킬
 *
 * 최초 대상에게 번개를 떨어뜨린 후 주변 적에게 연쇄 공격합니다.
 * 연쇄될 때마다 데미지가 감소합니다.
 */
public class ChainLightning extends ElementalSkill {
    private static final int BASE_DAMAGE = 50;
    private static final int MANA_COST = 35;
    private static final float BASE_COOLDOWN = 6.0f;

    private static final float MAX_RANGE = 600f;
    private static final int MAX_CHAIN_COUNT = 4;  // 최대 4번 연쇄
    private static final float CHAIN_RANGE = 200f;  // 연쇄 범위
    private static final float DAMAGE_REDUCTION = 0.7f;  // 연쇄마다 70%로 감소

    public ChainLightning() {
        super(402, "체인 라이트닝", ElementType.LIGHTNING);
        this.baseDamage = BASE_DAMAGE;
        this.manaCost = MANA_COST;
        this.baseCooldown = BASE_COOLDOWN;
    }

    @Override
    public void cast(Player caster, Vector2 targetPosition) {
        if (caster.getStats().getMana() < manaCost || currentCooldown > 0) {
            return;
        }

        Monster firstTarget = findNearestMonster(caster.getPosition(), MAX_RANGE * rangeMultiplier);

        if (firstTarget == null) {
            return;
        }

        caster.getStats().consumeMana(manaCost);

        // 연쇄 공격 시작
        performChainAttack(caster, firstTarget, getDamage(), MAX_CHAIN_COUNT, new HashSet<>());

        currentCooldown = getCooldown();
    }

    /**
     * 재귀적으로 연쇄 공격 수행
     */
    private void performChainAttack(Entity caster, Monster target, int damage,
                                    int remainingChains, Set<Integer> hitTargets) {
        if (target == null || remainingChains <= 0 || hitTargets.contains(target.getId())) {
            return;
        }

        // 데미지 적용
        CombatSystem.getInstance().dealDamage(caster, target, damage);
        hitTargets.add(target.getId());

        // 번개 이펙트
        LightningEffect chainEffect = new LightningEffect(LightningEffect.Type.CHAIN);
        chainEffect.setPosition(target.getPosition());
        GameManager.getInstance().addEffect(chainEffect);

        // 다음 대상 찾기
        Monster nextTarget = findNearestMonster(target.getPosition(), CHAIN_RANGE, hitTargets);

        if (nextTarget != null) {
            // 연결선 이펙트
            LightningEffect arcEffect = new LightningEffect(LightningEffect.Type.ARC);
            arcEffect.setLine(target.getPosition(), nextTarget.getPosition());
            GameManager.getInstance().addEffect(arcEffect);

            // 재귀 호출 (데미지 감소)
            int nextDamage = (int) (damage * DAMAGE_REDUCTION);
            performChainAttack(caster, nextTarget, nextDamage, remainingChains - 1, hitTargets);
        }
    }

    private Monster findNearestMonster(Vector2 fromPos, float maxRange) {
        return findNearestMonster(fromPos, maxRange, new HashSet<>());
    }

    private Monster findNearestMonster(Vector2 fromPos, float maxRange, Set<Integer> exclude) {
        List<Monster> monsters = GameManager.getInstance().getMonsters();
        Monster nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Monster monster : monsters) {
            if (exclude.contains(monster.getId())) continue;

            float distance = monster.getPosition().dst(fromPos);
            if (distance <= maxRange && distance < minDistance) {
                nearest = monster;
                minDistance = distance;
            }
        }

        return nearest;
    }
}
```

### 3. ElectricField 스킬 (전자기장)

```java
/**
 * 전자기장 스킬
 *
 * 지정 위치에 전기장을 생성하여 지속 데미지와 이동 속도 감소를 줍니다.
 */
public class ElectricField extends ElementalSkill {
    private static final int BASE_DAMAGE = 15;  // 초당 데미지
    private static final int MANA_COST = 40;
    private static final float BASE_COOLDOWN = 9.0f;

    private static final float FIELD_RADIUS = 180f;
    private static final float FIELD_DURATION = 5.0f;
    private static final float SLOW_AMOUNT = 0.5f;  // 50% 감속

    public ElectricField() {
        super(403, "전자기장", ElementType.LIGHTNING);
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

        ElectricFieldZone field = new ElectricFieldZone(
            targetPosition,
            FIELD_RADIUS * rangeMultiplier,
            getDamage(),
            FIELD_DURATION,
            SLOW_AMOUNT,
            caster
        );

        GameManager.getInstance().addSkillZone(field);
        currentCooldown = getCooldown();
    }
}

/**
 * 전자기장 구역
 */
class ElectricFieldZone {
    private Vector2 position;
    private float radius;
    private int damagePerSecond;
    private float duration;
    private float slowAmount;
    private Entity owner;

    private float elapsedTime = 0f;
    private float damageTimer = 0f;
    private static final float DAMAGE_INTERVAL = 0.5f;

    private Map<Integer, ElectrocutedBuff> affectedMonsters;
    private LightningEffect fieldEffect;

    public ElectricFieldZone(Vector2 position, float radius, int damagePerSecond,
                             float duration, float slowAmount, Entity owner) {
        this.position = position;
        this.radius = radius;
        this.damagePerSecond = damagePerSecond;
        this.duration = duration;
        this.slowAmount = slowAmount;
        this.owner = owner;

        this.affectedMonsters = new HashMap<>();
        this.fieldEffect = new LightningEffect(LightningEffect.Type.FIELD);
        this.fieldEffect.setPosition(position);
        this.fieldEffect.setScale(radius / 100f);
    }

    public void update(float delta) {
        elapsedTime += delta;
        damageTimer += delta;

        fieldEffect.update(delta);

        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Monster monster : monsters) {
            float distance = monster.getPosition().dst(position);

            if (distance <= radius) {
                // 전자기장 내부
                if (!affectedMonsters.containsKey(monster.getId())) {
                    // 감전 버프 추가
                    ElectrocutedBuff buff = new ElectrocutedBuff(slowAmount, 1.0f);
                    monster.addBuff(buff);
                    affectedMonsters.put(monster.getId(), buff);
                }

                // 데미지 적용
                if (damageTimer >= DAMAGE_INTERVAL) {
                    int damage = (int) (damagePerSecond * DAMAGE_INTERVAL);
                    CombatSystem.getInstance().dealDamage(owner, monster, damage);
                }
            } else {
                // 전자기장 밖으로 나감
                if (affectedMonsters.containsKey(monster.getId())) {
                    ElectrocutedBuff buff = affectedMonsters.remove(monster.getId());
                    monster.removeBuff(buff);
                }
            }
        }

        if (damageTimer >= DAMAGE_INTERVAL) {
            damageTimer = 0f;
        }
    }

    public void render(SpriteBatch batch) {
        fieldEffect.render(batch);
    }

    public boolean isAlive() {
        return elapsedTime < duration;
    }
}
```

### 4. ElectrocutedBuff 클래스

```java
/**
 * 감전 버프
 *
 * 이동 속도를 감소시킵니다.
 */
public class ElectrocutedBuff extends Buff {
    private float slowAmount;  // 감속 비율 (0.5 = 50% 감속)

    public ElectrocutedBuff(float slowAmount, float duration) {
        super(BuffType.ELECTROCUTED, duration);
        this.slowAmount = slowAmount;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (duration <= 0) {
            this.isActive = false;
        }
    }

    /**
     * 감속된 이동 속도 배율 반환
     */
    public float getSpeedMultiplier() {
        return 1.0f - slowAmount;
    }

    public float getSlowAmount() {
        return slowAmount;
    }
}
```

### 5. LightningEffect 클래스

```java
/**
 * 번개 이펙트
 */
public class LightningEffect {
    private Type type;
    private Vector2 position;
    private Vector2 lineEnd;  // ARC용
    private float scale = 1.0f;
    private float lifetime = 0f;
    private float maxLifetime;
    private boolean isAlive = true;

    public enum Type {
        STRIKE,     // 번개 타격
        CHAIN,      // 연쇄 번개
        ARC,        // 연결선
        FIELD       // 전기장
    }

    public LightningEffect(Type type) {
        this.type = type;
        this.position = new Vector2();
        this.lineEnd = new Vector2();

        switch (type) {
            case STRIKE:
                maxLifetime = 0.2f;
                break;
            case CHAIN:
                maxLifetime = 0.3f;
                break;
            case ARC:
                maxLifetime = 0.15f;
                break;
            case FIELD:
                maxLifetime = Float.MAX_VALUE;
                break;
        }
    }

    public void update(float delta) {
        lifetime += delta;

        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        float alpha = 1f - (lifetime / maxLifetime);

        switch (type) {
            case STRIKE:
                batch.setColor(1f, 1f, 0.3f, alpha);
                break;
            case CHAIN:
                batch.setColor(0.8f, 0.8f, 1f, alpha);
                break;
            case ARC:
                batch.setColor(0.9f, 0.9f, 1f, alpha * 0.7f);
                // 선 그리기 (ShapeRenderer 필요)
                break;
            case FIELD:
                batch.setColor(0.7f, 0.7f, 1f, 0.4f);
                break;
        }

        batch.setColor(1, 1, 1, 1);
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void setLine(Vector2 start, Vector2 end) {
        this.position.set(start);
        this.lineEnd.set(end);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isAlive() {
        return isAlive;
    }
}
```

---

## 🧪 테스트 계획

```java
public class TestLightningBolt {
    @Test
    public void 번개_즉발_데미지() {
        LightningBolt lightning = new LightningBolt();
        Player player = new Player(1);
        player.setPosition(100, 100);
        player.getStats().setMana(100);

        Monster monster = new Ghost();
        monster.setPosition(200, 100);
        int originalHP = monster.getHealth();

        GameManager.getInstance().addMonster(monster);

        lightning.cast(player, new Vector2(200, 100));

        assertTrue(monster.getHealth() < originalHP);
    }
}

public class TestChainLightning {
    @Test
    public void 체인_라이트닝_연쇄() {
        ChainLightning chainLightning = new ChainLightning();
        Player player = new Player(1);
        player.setPosition(100, 100);
        player.getStats().setMana(100);

        // 4마리 연쇄 가능 거리에 배치
        for (int i = 0; i < 4; i++) {
            Monster m = new Ghost();
            m.setPosition(200 + i * 150, 100);
            GameManager.getInstance().addMonster(m);
        }

        chainLightning.cast(player, new Vector2(200, 100));

        // 모든 몬스터가 데미지 받음
        List<Monster> monsters = GameManager.getInstance().getMonsters();
        for (Monster m : monsters) {
            assertTrue(m.getHealth() < m.getMaxHealth());
        }
    }
}

public class TestElectricField {
    @Test
    public void 전자기장_감속() {
        ElectricField field = new ElectricField();
        Player player = new Player(1);
        player.getStats().setMana(100);

        Monster monster = new Ghost();
        monster.setPosition(300, 300);
        float originalSpeed = monster.getStats().getSpeed();

        GameManager.getInstance().addMonster(monster);

        field.cast(player, new Vector2(300, 300));

        ElectricFieldZone zone = GameManager.getInstance().getSkillZones().get(0);
        zone.update(0.1f);

        // 감전 버프 확인
        assertTrue(monster.hasBuff(BuffType.ELECTROCUTED));

        // 이동 속도 감소 확인
        ElectrocutedBuff buff = (ElectrocutedBuff) monster.getBuff(BuffType.ELECTROCUTED);
        assertEquals(0.5f, buff.getSpeedMultiplier(), 0.01f);
    }
}
```

---

## ✅ 완료 조건

- [ ] LightningBolt 스킬 구현
- [ ] ChainLightning 스킬 구현
- [ ] ElectricField 스킬 구현
- [ ] ElectrocutedBuff 구현
- [ ] LightningEffect 이펙트 구현
- [ ] 연쇄 공격 로직 구현
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_18: 흙 속성 스킬**
- 바위 던지기 (Rock Throw)
- 지진 (Earthquake)
- 흙 갑옷 (Earth Armor)
