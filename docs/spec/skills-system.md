# 스킬 시스템 구현 가이드

**참고**: @docs/prd/characters.md 에서 각 원소별 스킬 목록을 확인하세요.

---

## 스킬 클래스 설계

### Skill (기본 클래스)

```java
public abstract class Skill {
    protected int skillId;              // 1-5
    protected String skillName;         // 스킬명
    protected int baseDamage;           // 기본 피해량
    protected int manaCost;             // MP 소비량
    protected float cooldown;           // 쿨다운 (초)
    protected float currentCooldown;    // 현재 쿨다운 진행 시간
    protected float castTime;           // 시전 시간 (초)
    protected String elementType;       // "불", "물", "바람", "땅", "전기"

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }
    }

    public boolean canCast() {
        return currentCooldown <= 0;
    }

    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    public abstract void execute(Player caster, float targetX, float targetY);
}
```

---

## 원소별 스킬 구현

### 불 원소 스킬

#### FireSkill1: 화염구 (Fireball)
```java
public class FireSkill1 extends Skill {
    public FireSkill1() {
        skillId = 1;
        skillName = "화염구";
        baseDamage = 30;
        manaCost = 15;
        cooldown = 0.5f;
        castTime = 0.2f;
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 1. 벡터 계산
        Vector2 direction = new Vector2(targetX - caster.x, targetY - caster.y).nor();

        // 2. 투사체 생성
        Projectile proj = new Projectile(
            caster.x, caster.y,              // 시작 위치
            direction.x * 200, direction.y * 200,  // 속도 (픽셀/초)
            baseDamage,
            2.0f                             // 생존 시간
        );

        // 3. 서버에 전송 (CastSkillMsg)
        client.castSkill(skillId, targetX, targetY);
    }
}
```

#### FireSkill2: 화염 폭발 (Explosion)
```java
public class FireSkill2 extends Skill {
    public FireSkill2() {
        skillId = 2;
        skillName = "화염 폭발";
        baseDamage = 40;
        manaCost = 20;
        cooldown = 1.2f;
        castTime = 0.5f;
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 1. 광역 데미지 (반경 150px)
        float radius = 150;

        // 2. 파티클 이펙트
        ParticleEffect explosion = new ParticleEffect();
        explosion.setPosition(targetX, targetY);
        explosion.start();

        // 3. 서버에 전송
        client.castSkill(skillId, targetX, targetY);
    }
}
```

#### FireSkill3: 용암 분출 (Lava Burst)
```java
public class FireSkill3 extends Skill {
    public FireSkill3() {
        skillId = 3;
        skillName = "용암 분출";
        baseDamage = 35;
        manaCost = 25;
        cooldown = 2.0f;
        castTime = 0.8f;
    }

    @Override
    public void execute(Player caster, float targetX, float targetY) {
        // 1. DoT(지속 피해) 적용
        // 초당 5 피해, 3초 동안 지속

        // 2. 지형 이펙트
        // 시각적으로 지면에 불 표시

        // 3. 서버에 전송
        client.castSkill(skillId, targetX, targetY);
    }
}
```

---

## 물 원소 스킬 (요약)

| 스킬 | ID | 기본 피해 | MP | 쿨다운 | 효과 |
|------|-----|---------|-----|--------|------|
| 얼음창 | 1 | 25 | 12 | 0.4초 | 이동속도 30% 감소 (3초) |
| 물줄기 | 2 | 20 | 10 | 0.3초 | 연속 5발 |
| 치유의 물 | 3 | - | 15 | 2.0초 | 자가 회복 40HP |

---

## 바람 원소 스킬 (요약)

| 스킬 | ID | 기본 피해 | MP | 쿨다운 | 효과 |
|------|-----|---------|-----|--------|------|
| 회오리 | 1 | 20 | 15 | 1.0초 | DoT 3초 |
| 순간이동 | 2 | - | 20 | 3.0초 | 플레이어 위치 이동 |
| 바람 칼날 | 3 | 28 | 12 | 0.6초 | 발사 속도 빠름 |

---

## 스킬 관리자 (SkillManager)

```java
public class SkillManager {
    private Skill[] skills = new Skill[5];
    private Player player;

    public SkillManager(Player player, String elementType) {
        this.player = player;
        initializeSkills(elementType);
    }

    private void initializeSkills(String elementType) {
        if (elementType.equals("불")) {
            skills[0] = new FireSkill1();
            skills[1] = new FireSkill2();
            skills[2] = new FireSkill3();
            skills[3] = null;  // 예비
            skills[4] = null;  // 예비
        }
        // 다른 원소들도 유사하게 초기화
    }

    public void update(float delta) {
        for (Skill skill : skills) {
            if (skill != null) {
                skill.update(delta);
            }
        }
    }

    public void castSkill(int skillIndex, float targetX, float targetY) {
        if (skillIndex < 0 || skillIndex >= skills.length) return;

        Skill skill = skills[skillIndex];
        if (skill == null) return;

        // 1. MP 확인
        if (player.getMp() < skill.manaCost) {
            // 실패: MP 부족
            return;
        }

        // 2. 쿨다운 확인
        if (!skill.canCast()) {
            // 실패: 쿨다운 중
            return;
        }

        // 3. MP 차감
        player.setMp(player.getMp() - skill.manaCost);

        // 4. 스킬 실행
        skill.execute(player, targetX, targetY);

        // 5. 쿨다운 시작
        skill.resetCooldown();
    }

    public float getSkillCooldownPercent(int skillIndex) {
        if (skillIndex < 0 || skillIndex >= skills.length) return 0;
        if (skills[skillIndex] == null) return 0;

        Skill skill = skills[skillIndex];
        return skill.currentCooldown / skill.cooldown;  // 0.0 ~ 1.0
    }
}
```

---

## 투사체 클래스 (Projectile)

```java
public class Projectile {
    public float x, y;
    public float vx, vy;              // 속도
    public float damage;
    public float lifetime;            // 남은 생존 시간
    public float maxLifetime;
    public int casterPlayerId;
    public boolean active = true;

    public Projectile(float x, float y, float vx, float vy, float damage, float lifetime) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
    }

    public void update(float delta) {
        x += vx * delta;
        y += vy * delta;
        lifetime -= delta;

        if (lifetime <= 0) {
            active = false;
        }

        // 맵 경계 벗어나면 제거
        if (x < 0 || x > 1920 || y < 0 || y > 1920) {
            active = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(x, y, 5);  // 반지름 5px 원
    }

    public Circle getBounds() {
        return new Circle(x, y, 5);
    }
}
```

---

## 충돌 감지 및 피해 계산

### 단일 표적 (Point)
```java
public boolean checkCollisionWithPlayer(Projectile proj, Player target) {
    float distance = Vector2.dst(proj.x, proj.y, target.x, target.y);
    return distance <= (proj.size + target.size);
}
```

### 광역 (Circle)
```java
public List<Integer> checkCollisionWithRadius(float centerX, float centerY, float radius) {
    List<Integer> hitPlayers = new ArrayList<>();

    for (OtherPlayer other : otherPlayers.values()) {
        float distance = Vector2.dst(centerX, centerY, other.x, other.y);
        if (distance <= radius) {
            hitPlayers.add(other.id);
        }
    }

    return hitPlayers;
}
```

### 피해 계산
```java
public int calculateDamage(Skill skill, Player attacker, Player victim) {
    int baseDamage = skill.baseDamage;
    int attackerAttack = attacker.getAttackPower();
    int victimDefense = victim.getDefense();

    // 공식: damage = baseDamage × (1 + (공격력 - 방어력) / 100)
    int bonus = (int)(baseDamage * (attackerAttack - victimDefense) / 100.0f);
    int finalDamage = Math.max(1, baseDamage + bonus);

    return finalDamage;
}
```

---

## UI 통합

### 스킬 버튼 업데이트

```java
public void updateSkillButtons(SkillManager skillManager) {
    for (int i = 0; i < 4; i++) {
        // 쿨다운 진행률 (0.0 ~ 1.0)
        float cooldownPercent = skillManager.getSkillCooldownPercent(i);

        if (cooldownPercent > 0) {
            // 쿨다운 중: 버튼 비활성화 + 진행률 표시
            skillButtons[i].setDisabled(true);
            drawCooldownArc(i, cooldownPercent);
        } else {
            // 쿨다운 완료: 버튼 활성화
            skillButtons[i].setDisabled(false);
        }
    }
}
```

---

## 성능 최적화

### Object Pooling (투사체 재사용)

```java
public class ProjectilePool {
    private List<Projectile> available = new ArrayList<>();
    private List<Projectile> active = new ArrayList<>();
    private static final int POOL_SIZE = 100;

    public ProjectilePool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            available.add(new Projectile(0, 0, 0, 0, 0, 0));
        }
    }

    public Projectile obtain(float x, float y, float vx, float vy, float damage, float lifetime) {
        Projectile proj;
        if (available.isEmpty()) {
            proj = new Projectile(x, y, vx, vy, damage, lifetime);
        } else {
            proj = available.remove(0);
            proj.reset(x, y, vx, vy, damage, lifetime);
        }
        active.add(proj);
        return proj;
    }

    public void free(Projectile proj) {
        active.remove(proj);
        available.add(proj);
    }
}
```
