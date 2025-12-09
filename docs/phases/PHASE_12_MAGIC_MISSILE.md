# PHASE_12_MAGIC_MISSILE.md - 매직 미사일 (기본 공격)

---

## 🎯 목표
자동 타게팅 기본 공격 스킬 구현
(매직 미사일, 타게팅 시스템, ON/OFF 토글)

---

## 📋 구현 범위

### 매직 미사일 스킬
- ✅ MagicMissile 클래스 구현
- ✅ 자동으로 가장 가까운 몬스터 타게팅
- ✅ 발사 주기: 1초마다
- ✅ ON/OFF 토글 버튼

### 타게팅 시스템
- ✅ 범위 내 몬스터 탐지 (800 픽셀)
- ✅ 가장 가까운 적 우선 공격
- ✅ 타겟 사망 시 재탐색

### 발사체 시스템
- ✅ Projectile 클래스 구현
- ✅ 유도 미사일 (호밍)
- ✅ 충돌 감지 및 데미지

---

## 📁 필요 파일

### 생성할 파일
```
game/skill/
  ├─ MagicMissile.java           (새로 생성)
  ├─ Projectile.java             (새로 생성)
  ├─ TargetingSystem.java        (새로 생성)
  └─ Skill.java                  (기본 클래스, 새로 생성)

ui/hud/
  └─ MagicMissileButton.java     (새로 생성)
```

### 기존 파일 수정
```
Player.java                       (수정 - 스킬 추가)
GameManager.java                  (수정 - 발사체 관리)
```

---

## 🔧 구현 가이드

### 1. Skill 기본 클래스

```java
/**
 * 스킬 기본 클래스
 *
 * 모든 스킬의 공통 속성과 메서드를 정의합니다.
 */
public abstract class Skill {
    // 스킬 정보
    protected String name;
    protected String description;
    protected int manaCost;

    // 쿨타임
    protected float cooldown;
    protected float currentCooldown;

    // 활성화 상태
    protected boolean isEnabled = true;

    // 소유자
    protected Player owner;

    public Skill(String name, int manaCost, float cooldown, Player owner) {
        this.name = name;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.currentCooldown = 0f;
        this.owner = owner;
    }

    /**
     * 업데이트 (매 프레임)
     */
    public void update(float delta) {
        // 쿨타임 감소
        if (currentCooldown > 0) {
            currentCooldown -= delta;
        }

        // 활성화된 경우 사용
        if (isEnabled && isReady()) {
            tryUse();
        }
    }

    /**
     * 스킬 사용 시도
     */
    protected void tryUse() {
        // 마나 확인
        if (!owner.getStats().consumeMana(manaCost)) {
            return;
        }

        // 스킬 실행
        use();

        // 쿨타임 시작
        currentCooldown = cooldown;
    }

    /**
     * 스킬 실행 (서브클래스에서 구현)
     */
    protected abstract void use();

    /**
     * 스킬 준비 상태
     */
    public boolean isReady() {
        return currentCooldown <= 0;
    }

    /**
     * 쿨타임 비율 (0.0 ~ 1.0)
     */
    public float getCooldownRatio() {
        if (cooldown == 0) return 1.0f;
        return 1.0f - (currentCooldown / cooldown);
    }

    // ===== Getters & Setters =====

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getManaCost() { return manaCost; }
    public float getCooldown() { return cooldown; }
    public float getCurrentCooldown() { return currentCooldown; }
    public boolean isEnabled() { return isEnabled; }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void toggleEnabled() {
        this.isEnabled = !this.isEnabled;
    }
}
```

### 2. MagicMissile 클래스

```java
/**
 * 매직 미사일 스킬
 *
 * 자동으로 가장 가까운 몬스터를 타게팅하여 공격합니다.
 */
public class MagicMissile extends Skill {
    // 타게팅 시스템
    private TargetingSystem targetingSystem;

    // 스킬 설정
    private static final float TARGETING_RANGE = 800f;  // 타게팅 범위
    private static final int BASE_DAMAGE = 15;          // 기본 데미지
    private static final float FIRE_RATE = 1.0f;        // 발사 주기 (1초)

    public MagicMissile(Player owner, TargetingSystem targetingSystem) {
        super("Magic Missile", 5, FIRE_RATE, owner);
        this.targetingSystem = targetingSystem;
        this.description = "가장 가까운 적에게 자동으로 마법 미사일을 발사합니다.";
    }

    @Override
    protected void use() {
        // 타겟 찾기
        Monster target = targetingSystem.findNearestMonster(
            owner.getPosition(),
            TARGETING_RANGE
        );

        if (target == null) {
            return;  // 타겟 없음
        }

        // 발사체 생성
        createProjectile(target);
    }

    /**
     * 발사체 생성
     */
    private void createProjectile(Monster target) {
        // 데미지 계산 (공격력 기반)
        int damage = BASE_DAMAGE + owner.getStats().getAttackPower();

        // 발사체 생성
        Projectile projectile = new Projectile(
            owner.getPosition(),
            target,
            damage,
            400f  // 속도 (픽셀/초)
        );

        // GameManager에 발사체 추가
        GameManager.getInstance().addProjectile(projectile);
    }
}
```

### 3. TargetingSystem 클래스

```java
/**
 * 타게팅 시스템
 *
 * 범위 내 몬스터를 탐지하고 타겟을 선택합니다.
 */
public class TargetingSystem {
    private GameManager gameManager;

    public TargetingSystem() {
        this.gameManager = GameManager.getInstance();
    }

    /**
     * 가장 가까운 몬스터 찾기
     * @param origin 기준 위치
     * @param range 탐지 범위
     * @return 가장 가까운 몬스터 (없으면 null)
     */
    public Monster findNearestMonster(Vector2 origin, float range) {
        List<Monster> monsters = gameManager.getMonsters();
        Monster nearest = null;
        float minDistance = range;

        for (Monster monster : monsters) {
            // 사망한 몬스터 제외
            if (!monster.isAlive()) {
                continue;
            }

            // 거리 계산
            float distance = origin.dst(monster.getPosition());

            // 범위 내이고 더 가까우면 업데이트
            if (distance <= range && distance < minDistance) {
                nearest = monster;
                minDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * 범위 내 모든 몬스터 찾기
     */
    public List<Monster> findMonstersInRange(Vector2 origin, float range) {
        List<Monster> result = new ArrayList<>();
        List<Monster> monsters = gameManager.getMonsters();

        for (Monster monster : monsters) {
            if (!monster.isAlive()) {
                continue;
            }

            float distance = origin.dst(monster.getPosition());
            if (distance <= range) {
                result.add(monster);
            }
        }

        return result;
    }

    /**
     * 방향 내 몬스터 찾기 (원뿔 형태)
     */
    public List<Monster> findMonstersInCone(Vector2 origin, Vector2 direction, float range, float angle) {
        List<Monster> result = new ArrayList<>();
        List<Monster> monsters = gameManager.getMonsters();

        for (Monster monster : monsters) {
            if (!monster.isAlive()) {
                continue;
            }

            Vector2 toMonster = new Vector2(monster.getPosition()).sub(origin);
            float distance = toMonster.len();

            // 범위 확인
            if (distance > range) {
                continue;
            }

            // 각도 확인
            float angleBetween = direction.angleDeg(toMonster);
            if (Math.abs(angleBetween) <= angle / 2) {
                result.add(monster);
            }
        }

        return result;
    }
}
```

### 4. Projectile 클래스

```java
/**
 * 발사체 클래스
 *
 * 스킬의 발사체를 나타냅니다.
 */
public class Projectile {
    // 위치 및 이동
    private Vector2 position;
    private Vector2 velocity;
    private float speed;

    // 타겟
    private Monster target;
    private boolean isHoming;  // 유도 미사일 여부

    // 데미지
    private int damage;

    // 상태
    private boolean isAlive;
    private float lifetime;
    private float maxLifetime = 5.0f;  // 최대 수명 (5초)

    // 렌더링
    private Texture texture;
    private float size = 16f;

    public Projectile(Vector2 origin, Monster target, int damage, float speed) {
        this.position = new Vector2(origin);
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.isHoming = true;
        this.isAlive = true;
        this.lifetime = 0f;

        // 초기 방향 설정
        this.velocity = new Vector2();
        updateVelocity();

        // 텍스처 (임시)
        createTexture();
    }

    /**
     * 업데이트
     */
    public void update(float delta) {
        lifetime += delta;

        // 수명 종료
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return;
        }

        // 타겟 사망 시 직진
        if (target == null || !target.isAlive()) {
            isHoming = false;
        }

        // 유도 미사일
        if (isHoming) {
            updateVelocity();
        }

        // 위치 업데이트
        position.add(velocity.x * delta, velocity.y * delta);

        // 충돌 감지
        checkCollision();
    }

    /**
     * 속도 업데이트 (유도)
     */
    private void updateVelocity() {
        if (target == null || !target.isAlive()) {
            return;
        }

        // 타겟 방향 계산
        Vector2 direction = new Vector2(target.getPosition()).sub(position).nor();
        velocity.set(direction).scl(speed);
    }

    /**
     * 충돌 감지
     */
    private void checkCollision() {
        if (target == null || !target.isAlive()) {
            return;
        }

        // 거리 계산
        float distance = position.dst(target.getPosition());

        // 충돌 판정 (타겟 크기 고려)
        if (distance <= target.getCollisionRadius() + size / 2) {
            // 데미지 적용
            target.takeDamage(damage);

            // 발사체 소멸
            isAlive = false;
        }
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        batch.draw(texture,
            position.x - size / 2,
            position.y - size / 2,
            size, size);
    }

    /**
     * 텍스처 생성 (임시)
     */
    private void createTexture() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.5f, 0.5f, 1.0f, 1.0f);  // 파란색
        pixmap.fillCircle(8, 8, 6);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public boolean isAlive() {
        return isAlive;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        texture.dispose();
    }
}
```

### 5. MagicMissileButton UI

```java
/**
 * 매직 미사일 ON/OFF 버튼
 */
public class MagicMissileButton {
    private MagicMissile skill;

    // UI 위치 및 크기
    private float x, y, size;

    // 텍스처
    private Texture buttonOn;
    private Texture buttonOff;

    // 폰트
    private BitmapFont font;

    public MagicMissileButton(MagicMissile skill, float x, float y, float size) {
        this.skill = skill;
        this.x = x;
        this.y = y;
        this.size = size;

        // 텍스처 생성
        buttonOn = createTexture(0.3f, 0.8f, 0.3f, 1.0f);   // 녹색
        buttonOff = createTexture(0.8f, 0.3f, 0.3f, 1.0f);  // 빨간색

        font = new BitmapFont();
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        // 버튼 배경
        Texture texture = skill.isEnabled() ? buttonOn : buttonOff;
        batch.draw(texture, x, y, size, size);

        // 쿨타임 표시
        if (!skill.isReady()) {
            // 어두운 오버레이
            batch.setColor(0, 0, 0, 0.5f);
            float ratio = 1.0f - skill.getCooldownRatio();
            batch.draw(texture, x, y, size, size * ratio);
            batch.setColor(1, 1, 1, 1);

            // 남은 시간 표시
            String cooldownText = String.format("%.1f", skill.getCurrentCooldown());
            font.draw(batch, cooldownText, x + size / 3, y + size / 2);
        }

        // 스킬 이름
        font.draw(batch, "MM", x + size / 3, y - 5);
    }

    /**
     * 터치 감지
     */
    public boolean isTouched(float touchX, float touchY) {
        return touchX >= x && touchX <= x + size &&
               touchY >= y && touchY <= y + size;
    }

    /**
     * 버튼 클릭 처리
     */
    public void onClick() {
        skill.toggleEnabled();
    }

    private Texture createTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        buttonOn.dispose();
        buttonOff.dispose();
        font.dispose();
    }
}
```

---

## 🧪 테스트 계획

```java
/**
 * MagicMissile 테스트
 */
public class TestMagicMissile {
    private MagicMissile skill;
    private Player player;
    private TargetingSystem targetingSystem;

    @BeforeEach
    public void setUp() {
        player = new Player(1);
        targetingSystem = new TargetingSystem();
        skill = new MagicMissile(player, targetingSystem);
    }

    @Test
    public void 스킬_기본_정보() {
        assertEquals("Magic Missile", skill.getName());
        assertEquals(5, skill.getManaCost());
        assertEquals(1.0f, skill.getCooldown(), 0.01f);
    }

    @Test
    public void 마나_부족_시_사용_불가() {
        player.getStats().setCurrentMana(3);
        skill.tryUse();
        // 발사체 생성 안 됨
    }

    @Test
    public void 타겟_없으면_발사_안_함() {
        // 몬스터 없음
        skill.use();
        assertEquals(0, GameManager.getInstance().getProjectiles().size());
    }

    @Test
    public void 쿨타임_작동() {
        skill.tryUse();
        assertTrue(skill.getCurrentCooldown() > 0);
        assertFalse(skill.isReady());

        skill.update(1.1f);
        assertTrue(skill.isReady());
    }
}

/**
 * TargetingSystem 테스트
 */
public class TestTargetingSystem {
    private TargetingSystem targetingSystem;
    private Vector2 playerPos;

    @BeforeEach
    public void setUp() {
        targetingSystem = new TargetingSystem();
        playerPos = new Vector2(500, 500);
    }

    @Test
    public void 가장_가까운_몬스터_찾기() {
        Monster monster1 = new Monster(MonsterType.SLIME);
        monster1.setPosition(600, 500);  // 거리 100

        Monster monster2 = new Monster(MonsterType.GHOST);
        monster2.setPosition(800, 500);  // 거리 300

        GameManager.getInstance().addMonster(monster1);
        GameManager.getInstance().addMonster(monster2);

        Monster nearest = targetingSystem.findNearestMonster(playerPos, 1000);
        assertEquals(monster1, nearest);
    }

    @Test
    public void 범위_밖_몬스터_무시() {
        Monster monster = new Monster(MonsterType.SLIME);
        monster.setPosition(2000, 2000);  // 범위 밖

        GameManager.getInstance().addMonster(monster);

        Monster nearest = targetingSystem.findNearestMonster(playerPos, 800);
        assertNull(nearest);
    }

    @Test
    public void 사망한_몬스터_무시() {
        Monster monster = new Monster(MonsterType.SLIME);
        monster.setPosition(600, 500);
        monster.takeDamage(1000);  // 사망

        GameManager.getInstance().addMonster(monster);

        Monster nearest = targetingSystem.findNearestMonster(playerPos, 1000);
        assertNull(nearest);
    }
}

/**
 * Projectile 테스트
 */
public class TestProjectile {
    private Projectile projectile;
    private Monster target;

    @BeforeEach
    public void setUp() {
        target = new Monster(MonsterType.SLIME);
        target.setPosition(500, 500);

        projectile = new Projectile(
            new Vector2(100, 100),
            target,
            20,
            400f
        );
    }

    @Test
    public void 발사체_초기_상태() {
        assertTrue(projectile.isAlive());
    }

    @Test
    public void 발사체_이동() {
        Vector2 oldPos = new Vector2(projectile.getPosition());
        projectile.update(0.1f);
        assertNotEquals(oldPos, projectile.getPosition());
    }

    @Test
    public void 발사체_충돌_시_데미지() {
        int oldHealth = target.getHealth();

        // 타겟 위치로 이동
        projectile.position.set(target.getPosition());
        projectile.update(0.016f);

        assertTrue(target.getHealth() < oldHealth);
        assertFalse(projectile.isAlive());
    }

    @Test
    public void 타겟_사망_시_직진() {
        target.takeDamage(1000);
        projectile.update(0.1f);
        assertFalse(projectile.isHoming);
    }

    @Test
    public void 발사체_수명() {
        projectile.update(6.0f);
        assertFalse(projectile.isAlive());
    }
}
```

---

## ✅ 완료 조건

- [ ] Skill 기본 클래스 구현
- [ ] MagicMissile 클래스 구현
- [ ] TargetingSystem 클래스 구현
- [ ] Projectile 클래스 구현
- [ ] MagicMissileButton UI 구현
- [ ] 자동 타게팅 작동 확인
- [ ] 발사체 충돌 및 데미지 확인
- [ ] ON/OFF 토글 작동 확인
- [ ] 모든 단위 테스트 통과

---

## 🔗 다음 Phase 연결점

**PHASE_13: 원소 선택 시스템**
- 5개 원소 선택 UI
- 원소별 스킬 슬롯
- 스킬 잠금/해제 시스템
