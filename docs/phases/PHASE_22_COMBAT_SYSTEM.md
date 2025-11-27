# PHASE_22_COMBAT_SYSTEM.md - 전투 시스템

---

## 🎯 목표
플레이어-몬스터 전투 시스템 구현 (데미지 계산, 충돌 감지, 상태 이상)

---

## 📋 구현 범위

- ✅ 데미지 계산 시스템
- ✅ 충돌 감지 (발사체-몬스터, 몬스터-플레이어)
- ✅ 상태 이상 적용
- ✅ 전투 이벤트 처리

---

## 📁 필요 파일

```
game/combat/
  ├─ CombatSystem.java
  ├─ DamageCalculator.java
  └─ CollisionDetector.java
```

---

## 🔧 구현 가이드

### 1. CombatSystem 클래스

```java
/**
 * 전투 시스템
 */
public class CombatSystem {
    private static CombatSystem instance;
    private DamageCalculator damageCalculator;
    private CollisionDetector collisionDetector;

    public static CombatSystem getInstance() {
        if (instance == null) {
            instance = new CombatSystem();
        }
        return instance;
    }

    private CombatSystem() {
        this.damageCalculator = new DamageCalculator();
        this.collisionDetector = new CollisionDetector();
    }

    /**
     * 데미지 적용
     */
    public int dealDamage(Entity attacker, Entity defender, int baseDamage) {
        // 데미지 계산
        int finalDamage = damageCalculator.calculate(attacker, defender, baseDamage);

        // 방어 적용
        if (defender instanceof Monster) {
            finalDamage = applyDefense((Monster) defender, finalDamage);
        } else if (defender instanceof Player) {
            finalDamage = applyDefense((Player) defender, finalDamage);
        }

        // 체력 감소
        defender.takeDamage(finalDamage);

        // 사망 처리
        if (defender.getHealth() <= 0) {
            onEntityDeath(attacker, defender);
        }

        return finalDamage;
    }

    /**
     * 방어력 적용 (몬스터)
     */
    private int applyDefense(Monster defender, int damage) {
        // 몬스터는 방어력이 없으므로 그대로 반환
        return damage;
    }

    /**
     * 방어력 적용 (플레이어)
     */
    private int applyDefense(Player defender, int damage) {
        int defense = defender.getStats().getDefense();

        // 보호막 확인
        if (defender.hasBuff(BuffType.SHIELD)) {
            ShieldBuff shield = (ShieldBuff) defender.getBuff(BuffType.SHIELD);
            damage = shield.absorbDamage(damage);
        }

        // 방어력 계산: DEF * 2 = 감소 데미지
        damage = Math.max(1, damage - (defense * 2));

        return damage;
    }

    /**
     * 엔티티 사망 처리
     */
    private void onEntityDeath(Entity attacker, Entity defender) {
        if (defender instanceof Monster) {
            onMonsterDeath((Monster) defender, attacker);
        } else if (defender instanceof Player) {
            onPlayerDeath((Player) defender, attacker);
        }
    }

    /**
     * 몬스터 사망 처리
     */
    private void onMonsterDeath(Monster monster, Entity killer) {
        // 경험치 보상
        if (killer instanceof Player) {
            Player player = (Player) killer;
            player.gainExperience(monster.getExpReward());
        }

        // 서버 동기화
        if (killer instanceof Player) {
            MonsterSyncManager.getInstance().sendMonsterDeath(monster, (Player) killer);
        }

        // 게임에서 제거
        GameManager.getInstance().removeMonster(monster);
    }

    /**
     * 플레이어 사망 처리
     */
    private void onPlayerDeath(Player player, Entity killer) {
        player.setState(PlayerState.DEAD);

        // 서버 동기화
        // PlayerDeathMsg 전송

        // 게임 오버 처리
        if (player.isLocalPlayer()) {
            GameManager.getInstance().onLocalPlayerDeath();
        }
    }

    /**
     * 충돌 감지 업데이트
     */
    public void update(float delta) {
        collisionDetector.checkCollisions();
    }
}
```

### 2. DamageCalculator 클래스

```java
/**
 * 데미지 계산기
 */
public class DamageCalculator {
    /**
     * 최종 데미지 계산
     */
    public int calculate(Entity attacker, Entity defender, int baseDamage) {
        int finalDamage = baseDamage;

        // 공격자 공격력 추가
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            int attackPower = player.getStats().getAttack();
            finalDamage += attackPower;
        }

        // 크리티컬 확률
        if (isCritical()) {
            finalDamage = (int) (finalDamage * 1.5f);  // 50% 추가
        }

        // 원소 상성 (향후 추가)
        // finalDamage = applyElementalModifier(attacker, defender, finalDamage);

        return finalDamage;
    }

    /**
     * 크리티컬 판정
     */
    private boolean isCritical() {
        // 10% 확률
        return Math.random() < 0.1f;
    }

    /**
     * 원소 상성 적용 (향후)
     */
    private int applyElementalModifier(Entity attacker, Entity defender, int damage) {
        // 불 > 흙 > 물 > 불 등
        return damage;
    }
}
```

### 3. CollisionDetector 클래스

```java
/**
 * 충돌 감지기
 */
public class CollisionDetector {
    /**
     * 모든 충돌 확인
     */
    public void checkCollisions() {
        checkProjectileCollisions();
        checkMonsterPlayerCollisions();
    }

    /**
     * 발사체-몬스터 충돌
     */
    private void checkProjectileCollisions() {
        List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Projectile projectile : projectiles) {
            if (!projectile.isAlive()) continue;

            for (Monster monster : monsters) {
                if (isColliding(projectile, monster)) {
                    projectile.onHit(monster);
                }
            }
        }
    }

    /**
     * 몬스터-플레이어 충돌
     */
    private void checkMonsterPlayerCollisions() {
        List<Monster> monsters = GameManager.getInstance().getMonsters();
        List<Player> players = GameManager.getInstance().getAllPlayers();

        for (Monster monster : monsters) {
            for (Player player : players) {
                if (isColliding(monster, player)) {
                    onMonsterPlayerCollision(monster, player);
                }
            }
        }
    }

    /**
     * 충돌 판정 (원형)
     */
    private boolean isColliding(Entity a, Entity b) {
        float radiusA = Math.max(a.getWidth(), a.getHeight()) / 2;
        float radiusB = Math.max(b.getWidth(), b.getHeight()) / 2;

        float distance = a.getPosition().dst(b.getPosition());

        return distance < (radiusA + radiusB);
    }

    /**
     * 몬스터-플레이어 충돌 처리
     */
    private void onMonsterPlayerCollision(Monster monster, Player player) {
        // 스턴 상태면 공격 못 함
        if (monster.hasBuff(BuffType.STUNNED)) {
            return;
        }

        // 무적 상태면 데미지 안 받음
        if (player.hasBuff(BuffType.INVINCIBLE)) {
            return;
        }

        // 마지막 공격으로부터 1초 경과 확인
        if (System.currentTimeMillis() - monster.getLastAttackTime() < 1000) {
            return;
        }

        // 데미지 적용
        int damage = monster.getStats().getAttack();
        CombatSystem.getInstance().dealDamage(monster, player, damage);

        monster.setLastAttackTime(System.currentTimeMillis());

        // 몬스터 상태 변경
        monster.setState(MonsterState.ATTACKING);
    }
}
```

---

## 🧪 테스트 계획

```java
public class TestCombatSystem {
    private CombatSystem combatSystem;
    private Player player;
    private Monster monster;

    @BeforeEach
    public void setUp() {
        combatSystem = CombatSystem.getInstance();
        player = new Player(1);
        player.getStats().setHealth(100);
        player.getStats().setAttack(20);

        monster = new Ghost();
        monster.setHealth(60);
    }

    @Test
    public void 플레이어가_몬스터에게_데미지() {
        int baseDamage = 30;
        int finalDamage = combatSystem.dealDamage(player, monster, baseDamage);

        assertTrue(finalDamage >= baseDamage);  // 공격력 추가
        assertTrue(monster.getHealth() < 60);
    }

    @Test
    public void 몬스터_사망_처리() {
        combatSystem.dealDamage(player, monster, 1000);

        assertTrue(monster.getHealth() <= 0);
        // 몬스터 제거 확인
    }

    @Test
    public void 플레이어_방어력_적용() {
        player.getStats().setDefense(10);

        int damage = combatSystem.dealDamage(monster, player, 50);

        // 방어력 20 감소 (DEF * 2)
        assertEquals(30, damage);
    }

    @Test
    public void 보호막_데미지_흡수() {
        ShieldBuff shield = new ShieldBuff(50, 5.0f);
        player.addBuff(shield);

        combatSystem.dealDamage(monster, player, 30);

        // 보호막이 흡수
        assertEquals(100, player.getStats().getHealth());
        assertEquals(20, shield.getShieldAmount());
    }
}

public class TestDamageCalculator {
    private DamageCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new DamageCalculator();
    }

    @Test
    public void 데미지_계산() {
        Player player = new Player(1);
        player.getStats().setAttack(20);

        Monster monster = new Ghost();

        int damage = calculator.calculate(player, monster, 50);

        assertEquals(70, damage);  // 50 + 20
    }
}

public class TestCollisionDetector {
    private CollisionDetector detector;

    @BeforeEach
    public void setUp() {
        detector = new CollisionDetector();
    }

    @Test
    public void 발사체_몬스터_충돌() {
        Projectile projectile = new FireballProjectile(
            new Player(1),
            new Vector2(100, 100),
            new Vector2(1, 0),
            50, 500, 800
        );

        Monster monster = new Ghost();
        monster.setPosition(110, 100);

        GameManager.getInstance().addProjectile(projectile);
        GameManager.getInstance().addMonster(monster);

        detector.checkCollisions();

        // 충돌 확인
        assertFalse(projectile.isAlive());
    }

    @Test
    public void 몬스터_플레이어_충돌() {
        Player player = new Player(1);
        player.setPosition(100, 100);

        Monster monster = new Ghost();
        monster.setPosition(105, 100);

        GameManager.getInstance().addPlayer(player);
        GameManager.getInstance().addMonster(monster);

        int originalHP = player.getStats().getHealth();

        detector.checkCollisions();

        // 플레이어가 데미지 받음
        assertTrue(player.getStats().getHealth() < originalHP);
    }
}
```

---

## ✅ 완료 조건

- [ ] CombatSystem 구현
- [ ] DamageCalculator 구현
- [ ] CollisionDetector 구현
- [ ] 데미지 계산 확인
- [ ] 충돌 감지 확인
- [ ] 사망 처리 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_23: 플레이어 동기화**
- 원격 플레이어 렌더링
- 위치/상태 동기화
