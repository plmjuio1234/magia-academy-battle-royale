# PHASE_10_CHARACTER_STATS.md - 캐릭터 능력치 시스템

---

## 🎯 목표
플레이어 능력치(Stats) 시스템 구현
(HP, MP, 공격력, 방어력, 이동속도 관리)

---

## 📋 구현 범위

### 능력치 시스템
- ✅ PlayerStats 클래스 구현
- ✅ 기본 능력치: HP, MP, ATK, DEF, SPEED
- ✅ 최대값/현재값 관리
- ✅ 능력치 증감 메서드

### 능력치 계산
- ✅ 레벨에 따른 기본 능력치 공식
- ✅ 장비/버프 보너스 (향후 확장)
- ✅ 능력치 변화 이벤트

### UI 연동
- ✅ HP/MP 바 표시 (다음 Phase)
- ✅ 능력치 변화 알림

---

## 📁 필요 파일

### 생성할 파일
```
game/player/
  ├─ PlayerStats.java            (새로 생성)
  ├─ StatsCalculator.java        (새로 생성)
  └─ StatsChangeListener.java    (새로 생성)

game/player/
  └─ Player.java                 (수정 - Stats 연동)
```

### 기존 파일 수정
```
Constants.java                    (수정 - 능력치 상수 추가)
```

---

## 🔧 구현 가이드

### 1. PlayerStats 클래스

```java
/**
 * 플레이어 능력치 클래스
 *
 * 플레이어의 모든 능력치를 관리합니다.
 */
public class PlayerStats {
    // 체력 (Health Points)
    private int maxHealth;
    private int currentHealth;

    // 마나 (Mana Points)
    private int maxMana;
    private int currentMana;

    // 공격력 (Attack Power)
    private int attackPower;

    // 방어력 (Defense)
    private int defense;

    // 이동 속도 (Speed, 픽셀/초)
    private float speed;

    // 레벨
    private int level;

    // 능력치 변화 리스너
    private List<StatsChangeListener> listeners = new ArrayList<>();

    /**
     * 기본 생성자 (레벨 1 기준)
     */
    public PlayerStats() {
        this(1);
    }

    /**
     * 레벨 기반 생성자
     */
    public PlayerStats(int level) {
        this.level = level;
        calculateBaseStats();
    }

    /**
     * 레벨에 따른 기본 능력치 계산
     */
    private void calculateBaseStats() {
        // 기본 능력치 공식
        this.maxHealth = 100 + (level - 1) * 20;      // 100, 120, 140, ...
        this.maxMana = 50 + (level - 1) * 10;         // 50, 60, 70, ...
        this.attackPower = 10 + (level - 1) * 5;      // 10, 15, 20, ...
        this.defense = 5 + (level - 1) * 2;           // 5, 7, 9, ...
        this.speed = 300f + (level - 1) * 10f;        // 300, 310, 320, ...

        // 현재값 초기화 (최대값으로)
        this.currentHealth = maxHealth;
        this.currentMana = maxMana;
    }

    /**
     * 체력 감소
     * @param amount 감소량
     * @return 실제 감소된 체력
     */
    public int decreaseHealth(int amount) {
        int oldHealth = currentHealth;
        currentHealth = Math.max(0, currentHealth - amount);
        int actualDecrease = oldHealth - currentHealth;

        notifyHealthChanged(oldHealth, currentHealth);
        return actualDecrease;
    }

    /**
     * 체력 회복
     * @param amount 회복량
     * @return 실제 회복된 체력
     */
    public int increaseHealth(int amount) {
        int oldHealth = currentHealth;
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        int actualIncrease = currentHealth - oldHealth;

        notifyHealthChanged(oldHealth, currentHealth);
        return actualIncrease;
    }

    /**
     * 마나 소비
     * @param amount 소비량
     * @return 소비 성공 여부
     */
    public boolean consumeMana(int amount) {
        if (currentMana < amount) {
            return false;
        }

        int oldMana = currentMana;
        currentMana -= amount;
        notifyManaChanged(oldMana, currentMana);
        return true;
    }

    /**
     * 마나 회복
     * @param amount 회복량
     * @return 실제 회복된 마나
     */
    public int increaseMana(int amount) {
        int oldMana = currentMana;
        currentMana = Math.min(maxMana, currentMana + amount);
        int actualIncrease = currentMana - oldMana;

        notifyManaChanged(oldMana, currentMana);
        return actualIncrease;
    }

    /**
     * 데미지 계산 (방어력 고려)
     * @param rawDamage 기본 데미지
     * @return 실제 적용될 데미지
     */
    public int calculateDamageReceived(int rawDamage) {
        // 방어력 공식: 데미지 감소 = 방어력 * 2
        int damageReduction = defense * 2;
        int actualDamage = Math.max(1, rawDamage - damageReduction);  // 최소 1 데미지
        return actualDamage;
    }

    /**
     * 공격 데미지 계산
     * @return 공격력 기반 데미지
     */
    public int calculateAttackDamage() {
        // 기본 공격 데미지 = 공격력 * 1.0
        // 크리티컬, 스킬 보너스 등은 향후 추가
        return attackPower;
    }

    /**
     * 플레이어가 사망했는가?
     */
    public boolean isDead() {
        return currentHealth <= 0;
    }

    /**
     * 체력 비율 (0.0 ~ 1.0)
     */
    public float getHealthRatio() {
        return (float) currentHealth / maxHealth;
    }

    /**
     * 마나 비율 (0.0 ~ 1.0)
     */
    public float getManaRatio() {
        return (float) currentMana / maxMana;
    }

    /**
     * 레벨업
     */
    public void levelUp() {
        level++;
        calculateBaseStats();
        notifyLevelUp(level);
    }

    // ===== 리스너 관리 =====

    public void addListener(StatsChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StatsChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyHealthChanged(int oldValue, int newValue) {
        for (StatsChangeListener listener : listeners) {
            listener.onHealthChanged(oldValue, newValue, maxHealth);
        }
    }

    private void notifyManaChanged(int oldValue, int newValue) {
        for (StatsChangeListener listener : listeners) {
            listener.onManaChanged(oldValue, newValue, maxMana);
        }
    }

    private void notifyLevelUp(int newLevel) {
        for (StatsChangeListener listener : listeners) {
            listener.onLevelUp(newLevel);
        }
    }

    // ===== Getters & Setters =====

    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxMana() { return maxMana; }
    public int getCurrentMana() { return currentMana; }
    public int getAttackPower() { return attackPower; }
    public int getDefense() { return defense; }
    public float getSpeed() { return speed; }
    public int getLevel() { return level; }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = Math.min(currentHealth, maxHealth);
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        this.currentMana = Math.min(currentMana, maxMana);
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setCurrentHealth(int health) {
        int oldHealth = this.currentHealth;
        this.currentHealth = Math.max(0, Math.min(maxHealth, health));
        notifyHealthChanged(oldHealth, currentHealth);
    }

    public void setCurrentMana(int mana) {
        int oldMana = this.currentMana;
        this.currentMana = Math.max(0, Math.min(maxMana, mana));
        notifyManaChanged(oldMana, currentMana);
    }
}
```

### 2. StatsChangeListener 인터페이스

```java
/**
 * 능력치 변화 리스너
 *
 * 능력치가 변경될 때 UI 업데이트 등을 수행합니다.
 */
public interface StatsChangeListener {
    /**
     * 체력 변화
     * @param oldValue 이전 체력
     * @param newValue 새 체력
     * @param maxValue 최대 체력
     */
    void onHealthChanged(int oldValue, int newValue, int maxValue);

    /**
     * 마나 변화
     * @param oldValue 이전 마나
     * @param newValue 새 마나
     * @param maxValue 최대 마나
     */
    void onManaChanged(int oldValue, int newValue, int maxValue);

    /**
     * 레벨업
     * @param newLevel 새 레벨
     */
    void onLevelUp(int newLevel);
}
```

### 3. StatsCalculator 유틸 클래스

```java
/**
 * 능력치 계산 유틸리티
 *
 * 복잡한 능력치 계산을 담당합니다.
 */
public class StatsCalculator {
    /**
     * 레벨에 따른 최대 체력 계산
     */
    public static int calculateMaxHealth(int level) {
        return 100 + (level - 1) * 20;
    }

    /**
     * 레벨에 따른 최대 마나 계산
     */
    public static int calculateMaxMana(int level) {
        return 50 + (level - 1) * 10;
    }

    /**
     * 레벨에 따른 공격력 계산
     */
    public static int calculateAttackPower(int level) {
        return 10 + (level - 1) * 5;
    }

    /**
     * 레벨에 따른 방어력 계산
     */
    public static int calculateDefense(int level) {
        return 5 + (level - 1) * 2;
    }

    /**
     * 레벨에 따른 이동속도 계산
     */
    public static float calculateSpeed(int level) {
        return 300f + (level - 1) * 10f;
    }

    /**
     * 데미지 감소 계산
     * @param defense 방어력
     * @return 데미지 감소량
     */
    public static int calculateDamageReduction(int defense) {
        return defense * 2;
    }

    /**
     * 크리티컬 데미지 계산 (향후 구현)
     */
    public static int calculateCriticalDamage(int baseDamage, float critMultiplier) {
        return (int)(baseDamage * critMultiplier);
    }

    /**
     * 경험치로부터 레벨 계산 (PHASE_11에서 사용)
     */
    public static int calculateLevelFromExp(int exp) {
        // 레벨 = sqrt(exp / 100) + 1
        return (int)(Math.sqrt(exp / 100.0)) + 1;
    }

    /**
     * 레벨업에 필요한 경험치 계산
     */
    public static int calculateExpForLevel(int level) {
        // 레벨 2: 100 exp
        // 레벨 3: 400 exp
        // 레벨 4: 900 exp
        // 공식: (level - 1)^2 * 100
        return (level - 1) * (level - 1) * 100;
    }
}
```

### 4. Player 클래스 수정 (Stats 연동)

```java
/**
 * Player 클래스에 능력치 추가
 */
public class Player {
    private int id;
    private Vector2 position;
    private Vector2 velocity;
    private PlayerStats stats;  // 능력치 추가

    public Player(int id) {
        this.id = id;
        this.position = new Vector2(960, 960);  // 맵 중앙
        this.velocity = new Vector2(0, 0);
        this.stats = new PlayerStats(1);  // 레벨 1로 시작

        // 능력치 변화 리스너 등록
        stats.addListener(new StatsChangeListener() {
            @Override
            public void onHealthChanged(int oldValue, int newValue, int maxValue) {
                // HP 변화 처리 (UI 업데이트 등)
                if (newValue <= 0) {
                    onDeath();
                }
            }

            @Override
            public void onManaChanged(int oldValue, int newValue, int maxValue) {
                // MP 변화 처리
            }

            @Override
            public void onLevelUp(int newLevel) {
                // 레벨업 처리 (이펙트, 알림 등)
            }
        });
    }

    /**
     * 데미지 받기
     */
    public void takeDamage(int rawDamage) {
        int actualDamage = stats.calculateDamageReceived(rawDamage);
        stats.decreaseHealth(actualDamage);
    }

    /**
     * 체력 회복
     */
    public void heal(int amount) {
        stats.increaseHealth(amount);
    }

    /**
     * 마나 소비
     */
    public boolean useMana(int amount) {
        return stats.consumeMana(amount);
    }

    /**
     * 사망 처리
     */
    private void onDeath() {
        // 사망 애니메이션
        // 서버에 사망 알림
        // 리스폰 대기
    }

    public PlayerStats getStats() {
        return stats;
    }

    public boolean isAlive() {
        return !stats.isDead();
    }
}
```

---

## 🧪 테스트 계획

```java
/**
 * PlayerStats 테스트
 */
public class TestPlayerStats {
    private PlayerStats stats;

    @BeforeEach
    public void setUp() {
        stats = new PlayerStats(1);  // 레벨 1
    }

    @Test
    public void 레벨1_기본_능력치() {
        assertEquals(100, stats.getMaxHealth());
        assertEquals(50, stats.getMaxMana());
        assertEquals(10, stats.getAttackPower());
        assertEquals(5, stats.getDefense());
        assertEquals(300f, stats.getSpeed(), 0.01f);
    }

    @Test
    public void 체력_감소() {
        stats.decreaseHealth(30);
        assertEquals(70, stats.getCurrentHealth());
        assertFalse(stats.isDead());
    }

    @Test
    public void 체력_0_이하는_사망() {
        stats.decreaseHealth(150);
        assertEquals(0, stats.getCurrentHealth());
        assertTrue(stats.isDead());
    }

    @Test
    public void 체력_회복() {
        stats.decreaseHealth(50);
        stats.increaseHealth(30);
        assertEquals(80, stats.getCurrentHealth());
    }

    @Test
    public void 체력_회복은_최대값_초과_불가() {
        stats.increaseHealth(50);
        assertEquals(100, stats.getCurrentHealth());  // 최대 100
    }

    @Test
    public void 마나_소비() {
        boolean success = stats.consumeMana(20);
        assertTrue(success);
        assertEquals(30, stats.getCurrentMana());
    }

    @Test
    public void 마나_부족_시_소비_실패() {
        boolean success = stats.consumeMana(60);
        assertFalse(success);
        assertEquals(50, stats.getCurrentMana());  // 변화 없음
    }

    @Test
    public void 마나_회복() {
        stats.consumeMana(30);
        stats.increaseMana(20);
        assertEquals(40, stats.getCurrentMana());
    }

    @Test
    public void 데미지_계산_방어력_적용() {
        // 레벨 1: 방어력 5
        // 데미지 감소 = 5 * 2 = 10
        int actualDamage = stats.calculateDamageReceived(50);
        assertEquals(40, actualDamage);  // 50 - 10 = 40
    }

    @Test
    public void 최소_1_데미지() {
        int actualDamage = stats.calculateDamageReceived(5);
        assertEquals(1, actualDamage);  // 최소 1 데미지
    }

    @Test
    public void 공격_데미지_계산() {
        // 레벨 1: 공격력 10
        int damage = stats.calculateAttackDamage();
        assertEquals(10, damage);
    }

    @Test
    public void 체력_비율() {
        stats.decreaseHealth(50);
        assertEquals(0.5f, stats.getHealthRatio(), 0.01f);
    }

    @Test
    public void 레벨업_시_능력치_증가() {
        stats.levelUp();
        assertEquals(2, stats.getLevel());
        assertEquals(120, stats.getMaxHealth());
        assertEquals(60, stats.getMaxMana());
        assertEquals(15, stats.getAttackPower());
        assertEquals(7, stats.getDefense());
        assertEquals(310f, stats.getSpeed(), 0.01f);
    }

    @Test
    public void 레벨업_시_체력_마나_최대값으로() {
        stats.decreaseHealth(50);
        stats.consumeMana(30);

        stats.levelUp();

        assertEquals(120, stats.getCurrentHealth());  // 최대값으로
        assertEquals(60, stats.getCurrentMana());
    }
}

/**
 * StatsCalculator 테스트
 */
public class TestStatsCalculator {
    @Test
    public void 레벨별_최대_체력() {
        assertEquals(100, StatsCalculator.calculateMaxHealth(1));
        assertEquals(120, StatsCalculator.calculateMaxHealth(2));
        assertEquals(140, StatsCalculator.calculateMaxHealth(3));
    }

    @Test
    public void 레벨별_경험치() {
        assertEquals(0, StatsCalculator.calculateExpForLevel(1));
        assertEquals(100, StatsCalculator.calculateExpForLevel(2));
        assertEquals(400, StatsCalculator.calculateExpForLevel(3));
        assertEquals(900, StatsCalculator.calculateExpForLevel(4));
    }

    @Test
    public void 경험치로부터_레벨_계산() {
        assertEquals(1, StatsCalculator.calculateLevelFromExp(0));
        assertEquals(2, StatsCalculator.calculateLevelFromExp(100));
        assertEquals(3, StatsCalculator.calculateLevelFromExp(400));
    }
}

/**
 * StatsChangeListener 테스트
 */
public class TestStatsChangeListener {
    private PlayerStats stats;
    private boolean healthChangedCalled = false;
    private boolean manaChangedCalled = false;
    private boolean levelUpCalled = false;

    @BeforeEach
    public void setUp() {
        stats = new PlayerStats(1);
        stats.addListener(new StatsChangeListener() {
            @Override
            public void onHealthChanged(int oldValue, int newValue, int maxValue) {
                healthChangedCalled = true;
            }

            @Override
            public void onManaChanged(int oldValue, int newValue, int maxValue) {
                manaChangedCalled = true;
            }

            @Override
            public void onLevelUp(int newLevel) {
                levelUpCalled = true;
            }
        });
    }

    @Test
    public void 체력_변화_시_리스너_호출() {
        stats.decreaseHealth(10);
        assertTrue(healthChangedCalled);
    }

    @Test
    public void 마나_변화_시_리스너_호출() {
        stats.consumeMana(10);
        assertTrue(manaChangedCalled);
    }

    @Test
    public void 레벨업_시_리스너_호출() {
        stats.levelUp();
        assertTrue(levelUpCalled);
    }
}
```

---

## ✅ 완료 조건

- [ ] PlayerStats 클래스 구현
- [ ] StatsChangeListener 인터페이스 구현
- [ ] StatsCalculator 유틸 클래스 구현
- [ ] Player 클래스에 Stats 연동
- [ ] 레벨에 따른 능력치 계산 확인
- [ ] 데미지 계산 로직 확인
- [ ] 능력치 변화 리스너 작동 확인
- [ ] 모든 단위 테스트 통과

---

## 🔗 다음 Phase 연결점

**PHASE_11: 레벨 & 경험치 시스템**
- LevelSystem 클래스 구현
- 경험치 획득 및 레벨업 처리
- 경험치 바 UI 표시
