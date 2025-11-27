# PHASE_11_LEVEL_EXP_SYSTEM.md - 레벨 & 경험치 시스템

---

## 🎯 목표
경험치 획득 및 레벨업 시스템 구현
(몬스터 처치 시 경험치, 레벨업 효과, 경험치 바 UI)

---

## 📋 구현 범위

### 경험치 시스템
- ✅ LevelSystem 클래스 구현
- ✅ 경험치 획득 로직
- ✅ 레벨업 판정 및 처리
- ✅ 경험치 바 UI

### 몬스터 경험치
- ✅ 몬스터 종류별 경험치 설정
- ✅ 처치 시 경험치 획득
- ✅ 파티 경험치 분배 (향후)

### 레벨업 효과
- ✅ 레벨업 애니메이션
- ✅ 능력치 자동 상승
- ✅ 체력/마나 완전 회복

---

## 📁 필요 파일

### 생성할 파일
```
game/level/
  ├─ LevelSystem.java            (새로 생성)
  ├─ ExperienceManager.java      (새로 생성)
  └─ LevelUpEffect.java          (새로 생성)

ui/hud/
  └─ ExpBarComponent.java        (새로 생성)
```

### 기존 파일 수정
```
Player.java                       (수정 - 레벨 시스템 연동)
Monster.java                      (수정 - 경험치 추가)
PlayerStats.java                  (수정 - 레벨업 처리)
```

---

## 🔧 구현 가이드

### 1. LevelSystem 클래스

```java
/**
 * 레벨 시스템
 *
 * 플레이어의 레벨과 경험치를 관리합니다.
 */
public class LevelSystem {
    // 현재 레벨
    private int currentLevel;

    // 경험치
    private int currentExp;
    private int expForNextLevel;

    // 플레이어 Stats 참조
    private PlayerStats playerStats;

    // 레벨업 리스너
    private List<LevelUpListener> listeners = new ArrayList<>();

    /**
     * 생성자
     */
    public LevelSystem(PlayerStats playerStats) {
        this.playerStats = playerStats;
        this.currentLevel = 1;
        this.currentExp = 0;
        this.expForNextLevel = calculateExpForLevel(2);
    }

    /**
     * 경험치 획득
     * @param amount 경험치량
     */
    public void gainExperience(int amount) {
        currentExp += amount;

        // 레벨업 체크
        while (currentExp >= expForNextLevel) {
            levelUp();
        }

        notifyExpGained(amount);
    }

    /**
     * 레벨업 처리
     */
    private void levelUp() {
        // 남은 경험치 계산
        int remainingExp = currentExp - expForNextLevel;

        // 레벨 증가
        currentLevel++;
        currentExp = remainingExp;

        // 다음 레벨 경험치 계산
        expForNextLevel = calculateExpForLevel(currentLevel + 1);

        // 능력치 증가
        playerStats.levelUp();

        // 레벨업 효과
        notifyLevelUp(currentLevel);
    }

    /**
     * 레벨에 필요한 경험치 계산
     * @param level 목표 레벨
     * @return 필요한 경험치
     */
    private int calculateExpForLevel(int level) {
        // 레벨 2: 100 exp
        // 레벨 3: 400 exp (누적)
        // 레벨 4: 900 exp (누적)
        // 공식: (level - 1)^2 * 100
        return (level - 1) * (level - 1) * 100;
    }

    /**
     * 경험치 비율 (0.0 ~ 1.0)
     */
    public float getExpRatio() {
        if (expForNextLevel == 0) return 1.0f;
        return (float) currentExp / expForNextLevel;
    }

    /**
     * 현재 레벨의 경험치 진행도 (백분율)
     */
    public int getExpPercentage() {
        return (int)(getExpRatio() * 100);
    }

    // ===== 리스너 관리 =====

    public void addListener(LevelUpListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LevelUpListener listener) {
        listeners.remove(listener);
    }

    private void notifyExpGained(int amount) {
        for (LevelUpListener listener : listeners) {
            listener.onExpGained(amount, currentExp, expForNextLevel);
        }
    }

    private void notifyLevelUp(int newLevel) {
        for (LevelUpListener listener : listeners) {
            listener.onLevelUp(newLevel);
        }
    }

    // ===== Getters =====

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public int getExpForNextLevel() {
        return expForNextLevel;
    }

    public int getRemainingExp() {
        return expForNextLevel - currentExp;
    }
}

/**
 * 레벨업 리스너
 */
interface LevelUpListener {
    void onExpGained(int amount, int currentExp, int maxExp);
    void onLevelUp(int newLevel);
}
```

### 2. ExperienceManager 클래스

```java
/**
 * 경험치 관리자
 *
 * 몬스터 처치 시 경험치 분배를 담당합니다.
 */
public class ExperienceManager {
    /**
     * 몬스터 처치 경험치 계산
     * @param monsterType 몬스터 종류
     * @return 경험치량
     */
    public static int getExpForMonster(MonsterType monsterType) {
        switch (monsterType) {
            case GHOST:
                return 30;   // 고스트: 30 exp
            case SLIME:
                return 20;   // 슬라임: 20 exp
            case GOLEM:
                return 50;   // 골렘: 50 exp
            default:
                return 10;
        }
    }

    /**
     * 레벨 차이에 따른 경험치 보정
     * @param baseExp 기본 경험치
     * @param playerLevel 플레이어 레벨
     * @param monsterLevel 몬스터 레벨
     * @return 보정된 경험치
     */
    public static int adjustExpByLevelDifference(int baseExp, int playerLevel, int monsterLevel) {
        int levelDiff = monsterLevel - playerLevel;

        if (levelDiff >= 5) {
            // 5레벨 이상 높으면 150%
            return (int)(baseExp * 1.5f);
        } else if (levelDiff >= 2) {
            // 2~4레벨 높으면 120%
            return (int)(baseExp * 1.2f);
        } else if (levelDiff <= -5) {
            // 5레벨 이상 낮으면 50%
            return (int)(baseExp * 0.5f);
        } else if (levelDiff <= -2) {
            // 2~4레벨 낮으면 80%
            return (int)(baseExp * 0.8f);
        }

        // 레벨 차이 -1 ~ 1: 100%
        return baseExp;
    }

    /**
     * 파티 경험치 분배 (향후 구현)
     */
    public static int calculatePartyExp(int totalExp, int partySize) {
        // 파티원 수에 따라 경험치 분배
        // 예: 2명 = 60% 씩, 3명 = 50% 씩
        if (partySize <= 1) {
            return totalExp;
        } else if (partySize == 2) {
            return (int)(totalExp * 0.6f);
        } else if (partySize == 3) {
            return (int)(totalExp * 0.5f);
        } else {
            return (int)(totalExp * 0.4f);
        }
    }
}
```

### 3. LevelUpEffect 클래스

```java
/**
 * 레벨업 이펙트
 *
 * 레벨업 시 재생되는 시각적 효과입니다.
 */
public class LevelUpEffect {
    private Vector2 position;
    private float lifetime;
    private float maxLifetime = 2.0f;  // 2초
    private boolean isAlive;

    // 파티클 효과
    private List<Particle> particles = new ArrayList<>();
    private Color color = new Color(1f, 1f, 0f, 1f);  // 금색

    public LevelUpEffect(Vector2 position) {
        this.position = new Vector2(position);
        this.lifetime = 0f;
        this.isAlive = true;

        // 파티클 생성
        createParticles();
    }

    /**
     * 파티클 생성
     */
    private void createParticles() {
        for (int i = 0; i < 20; i++) {
            float angle = (float)Math.random() * 360f;
            float speed = 100f + (float)Math.random() * 100f;
            particles.add(new Particle(position, angle, speed));
        }
    }

    /**
     * 업데이트
     */
    public void update(float delta) {
        lifetime += delta;

        // 파티클 업데이트
        for (Particle particle : particles) {
            particle.update(delta);
        }

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        // 알파값 감소 (페이드 아웃)
        float alpha = 1.0f - (lifetime / maxLifetime);
        color.a = alpha;

        batch.setColor(color);

        // 파티클 렌더링
        for (Particle particle : particles) {
            particle.render(batch);
        }

        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() {
        return isAlive;
    }

    /**
     * 파티클 클래스
     */
    private static class Particle {
        Vector2 position;
        Vector2 velocity;
        float lifetime;

        public Particle(Vector2 origin, float angle, float speed) {
            this.position = new Vector2(origin);
            this.velocity = new Vector2(
                (float)Math.cos(Math.toRadians(angle)) * speed,
                (float)Math.sin(Math.toRadians(angle)) * speed
            );
            this.lifetime = 0f;
        }

        public void update(float delta) {
            position.add(velocity.x * delta, velocity.y * delta);
            lifetime += delta;

            // 중력 효과
            velocity.y -= 500f * delta;
        }

        public void render(SpriteBatch batch) {
            // 작은 사각형으로 렌더링 (임시)
            batch.draw(whitePixel, position.x, position.y, 4, 4);
        }
    }
}
```

### 4. ExpBarComponent UI

```java
/**
 * 경험치 바 UI 컴포넌트
 *
 * HUD에 표시되는 경험치 바입니다.
 */
public class ExpBarComponent {
    private LevelSystem levelSystem;

    // UI 위치 및 크기
    private float x, y, width, height;

    // 텍스처
    private Texture barBackground;
    private Texture barFill;

    // 폰트
    private BitmapFont font;

    public ExpBarComponent(LevelSystem levelSystem, float x, float y, float width, float height) {
        this.levelSystem = levelSystem;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // 텍스처 생성 (임시)
        barBackground = createTexture(0.2f, 0.2f, 0.2f, 0.8f);
        barFill = createTexture(0.3f, 0.7f, 1.0f, 1.0f);  // 파란색

        // 폰트
        font = new BitmapFont();
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        // 배경
        batch.draw(barBackground, x, y, width, height);

        // 경험치 바 (진행도)
        float fillWidth = width * levelSystem.getExpRatio();
        batch.draw(barFill, x, y, fillWidth, height);

        // 텍스트 (레벨 & 경험치)
        String text = String.format("Lv.%d  %d / %d (%d%%)",
            levelSystem.getCurrentLevel(),
            levelSystem.getCurrentExp(),
            levelSystem.getExpForNextLevel(),
            levelSystem.getExpPercentage());

        font.draw(batch, text, x + 10, y + height - 5);
    }

    /**
     * 텍스처 생성 (임시)
     */
    private Texture createTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        barBackground.dispose();
        barFill.dispose();
        font.dispose();
    }
}
```

### 5. Player 클래스 수정

```java
/**
 * Player 클래스에 레벨 시스템 추가
 */
public class Player {
    private int id;
    private Vector2 position;
    private PlayerStats stats;
    private LevelSystem levelSystem;  // 레벨 시스템 추가

    public Player(int id) {
        this.id = id;
        this.stats = new PlayerStats(1);
        this.levelSystem = new LevelSystem(stats);

        // 레벨업 리스너 등록
        levelSystem.addListener(new LevelUpListener() {
            @Override
            public void onExpGained(int amount, int currentExp, int maxExp) {
                // 경험치 획득 알림 (UI)
            }

            @Override
            public void onLevelUp(int newLevel) {
                // 레벨업 효과 재생
                playLevelUpEffect();
            }
        });
    }

    /**
     * 몬스터 처치 시 경험치 획득
     */
    public void onMonsterKilled(Monster monster) {
        int baseExp = ExperienceManager.getExpForMonster(monster.getType());
        int adjustedExp = ExperienceManager.adjustExpByLevelDifference(
            baseExp,
            levelSystem.getCurrentLevel(),
            monster.getLevel()
        );

        levelSystem.gainExperience(adjustedExp);
    }

    /**
     * 레벨업 이펙트 재생
     */
    private void playLevelUpEffect() {
        LevelUpEffect effect = new LevelUpEffect(position);
        // GameManager에 이펙트 추가
    }

    public LevelSystem getLevelSystem() {
        return levelSystem;
    }
}
```

---

## 🧪 테스트 계획

```java
/**
 * LevelSystem 테스트
 */
public class TestLevelSystem {
    private LevelSystem levelSystem;
    private PlayerStats stats;

    @BeforeEach
    public void setUp() {
        stats = new PlayerStats(1);
        levelSystem = new LevelSystem(stats);
    }

    @Test
    public void 초기_레벨은_1() {
        assertEquals(1, levelSystem.getCurrentLevel());
        assertEquals(0, levelSystem.getCurrentExp());
    }

    @Test
    public void 레벨2_필요_경험치는_100() {
        assertEquals(100, levelSystem.getExpForNextLevel());
    }

    @Test
    public void 경험치_획득() {
        levelSystem.gainExperience(50);
        assertEquals(50, levelSystem.getCurrentExp());
    }

    @Test
    public void 레벨업_발생() {
        levelSystem.gainExperience(100);
        assertEquals(2, levelSystem.getCurrentLevel());
        assertEquals(0, levelSystem.getCurrentExp());
    }

    @Test
    public void 남은_경험치_이월() {
        levelSystem.gainExperience(120);  // 레벨업 + 20 남음
        assertEquals(2, levelSystem.getCurrentLevel());
        assertEquals(20, levelSystem.getCurrentExp());
    }

    @Test
    public void 여러_레벨_동시_상승() {
        levelSystem.gainExperience(500);  // 레벨 1 -> 3
        assertEquals(3, levelSystem.getCurrentLevel());
    }

    @Test
    public void 경험치_비율() {
        levelSystem.gainExperience(50);  // 50 / 100 = 0.5
        assertEquals(0.5f, levelSystem.getExpRatio(), 0.01f);
    }

    @Test
    public void 경험치_백분율() {
        levelSystem.gainExperience(75);  // 75%
        assertEquals(75, levelSystem.getExpPercentage());
    }

    @Test
    public void 레벨업_시_능력치_증가() {
        int oldMaxHealth = stats.getMaxHealth();
        levelSystem.gainExperience(100);
        assertTrue(stats.getMaxHealth() > oldMaxHealth);
    }
}

/**
 * ExperienceManager 테스트
 */
public class TestExperienceManager {
    @Test
    public void 몬스터별_경험치() {
        assertEquals(20, ExperienceManager.getExpForMonster(MonsterType.SLIME));
        assertEquals(30, ExperienceManager.getExpForMonster(MonsterType.GHOST));
        assertEquals(50, ExperienceManager.getExpForMonster(MonsterType.GOLEM));
    }

    @Test
    public void 레벨_차이_경험치_보정() {
        int baseExp = 100;

        // 5레벨 높은 몬스터: 150%
        assertEquals(150, ExperienceManager.adjustExpByLevelDifference(baseExp, 1, 6));

        // 2레벨 높은 몬스터: 120%
        assertEquals(120, ExperienceManager.adjustExpByLevelDifference(baseExp, 1, 3));

        // 같은 레벨: 100%
        assertEquals(100, ExperienceManager.adjustExpByLevelDifference(baseExp, 5, 5));

        // 5레벨 낮은 몬스터: 50%
        assertEquals(50, ExperienceManager.adjustExpByLevelDifference(baseExp, 10, 5));
    }

    @Test
    public void 파티_경험치_분배() {
        int totalExp = 100;

        // 솔로: 100%
        assertEquals(100, ExperienceManager.calculatePartyExp(totalExp, 1));

        // 2인: 60%
        assertEquals(60, ExperienceManager.calculatePartyExp(totalExp, 2));

        // 3인: 50%
        assertEquals(50, ExperienceManager.calculatePartyExp(totalExp, 3));
    }
}

/**
 * LevelUpEffect 테스트
 */
public class TestLevelUpEffect {
    private LevelUpEffect effect;

    @BeforeEach
    public void setUp() {
        effect = new LevelUpEffect(new Vector2(100, 100));
    }

    @Test
    public void 이펙트_초기_상태() {
        assertTrue(effect.isAlive());
    }

    @Test
    public void 이펙트_수명_2초() {
        effect.update(2.1f);
        assertFalse(effect.isAlive());
    }

    @Test
    public void 파티클_생성() {
        // 20개의 파티클이 생성되는지 확인
        assertEquals(20, effect.particles.size());
    }
}
```

---

## ✅ 완료 조건

- [ ] LevelSystem 클래스 구현
- [ ] ExperienceManager 클래스 구현
- [ ] LevelUpEffect 클래스 구현
- [ ] ExpBarComponent UI 구현
- [ ] Player 클래스에 레벨 시스템 연동
- [ ] 몬스터 처치 시 경험치 획득 확인
- [ ] 레벨업 시 능력치 자동 증가 확인
- [ ] 레벨업 이펙트 재생 확인
- [ ] 모든 단위 테스트 통과

---

## 🔗 다음 Phase 연결점

**PHASE_12: 매직 미사일**
- 기본 공격 스킬 구현
- 자동 타게팅 시스템
- ON/OFF 토글 버튼
