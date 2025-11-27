# PHASE_20_MONSTER_RENDER.md - 몬스터 렌더링

---

## 🎯 목표
3가지 몬스터 타입 렌더링 및 애니메이션 시스템 구현

---

## 📋 구현 범위

- ✅ 몬스터 기본 클래스 및 렌더링
- ✅ 3가지 몬스터 타입 (고스트, 슬라임, 골렘)
- ✅ HP 바 표시
- ✅ 상태 애니메이션 (이동, 공격, 피격, 사망)

---

## 📁 필요 파일

```
game/monster/
  ├─ Monster.java         (수정 - 렌더링 추가)
  ├─ Ghost.java           (수정)
  ├─ Slime.java           (수정)
  └─ Golem.java           (수정)

game/animation/
  └─ MonsterAnimation.java (새로 생성)

ui/hud/
  └─ HPBar.java           (새로 생성)
```

---

## 🔧 구현 가이드

### 1. Monster 클래스 (렌더링 추가)

```java
/**
 * 몬스터 기본 클래스 (렌더링 기능 추가)
 */
public abstract class Monster extends Entity {
    // 기존 필드들...
    protected MonsterType type;
    protected MonsterStats stats;

    // 렌더링 관련
    protected Sprite sprite;
    protected MonsterAnimation animation;
    protected HPBar hpBar;

    // 상태
    protected MonsterState state = MonsterState.IDLE;

    /**
     * 생성자
     */
    public Monster(MonsterType type) {
        this.type = type;
        this.stats = createStats();
        this.hpBar = new HPBar(this);

        initializeSprite();
        initializeAnimation();
    }

    /**
     * 스프라이트 초기화 (각 몬스터별 오버라이드)
     */
    protected abstract void initializeSprite();

    /**
     * 애니메이션 초기화
     */
    protected void initializeAnimation() {
        this.animation = new MonsterAnimation(type);
    }

    /**
     * 매 프레임 업데이트
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 애니메이션 업데이트
        if (animation != null) {
            animation.update(delta);
        }

        // HP 바 위치 업데이트
        hpBar.setPosition(position.x, position.y + getHeight() + 5);
    }

    /**
     * 렌더링
     */
    @Override
    public void render(SpriteBatch batch) {
        if (sprite == null) return;

        // 현재 상태의 프레임 가져오기
        TextureRegion currentFrame = animation.getCurrentFrame(state);

        // 스프라이트 렌더링
        batch.draw(currentFrame, position.x, position.y, getWidth(), getHeight());

        // HP 바 렌더링
        hpBar.render(batch);

        // 버프 이펙트 렌더링
        renderBuffEffects(batch);
    }

    /**
     * 버프 이펙트 렌더링
     */
    private void renderBuffEffects(SpriteBatch batch) {
        if (hasBuff(BuffType.STUNNED)) {
            // 스턴 이펙트 (별 표시 등)
            renderStunEffect(batch);
        }

        if (hasBuff(BuffType.ELECTROCUTED)) {
            // 감전 이펙트
            renderElectrocutedEffect(batch);
        }
    }

    /**
     * 스턴 이펙트 렌더링
     */
    private void renderStunEffect(SpriteBatch batch) {
        // 임시: 노란색 원
        batch.setColor(1f, 1f, 0f, 0.5f);
        // batch.draw(stunIcon, position.x, position.y + getHeight(), 16, 16);
        batch.setColor(1, 1, 1, 1);
    }

    /**
     * 감전 이펙트 렌더링
     */
    private void renderElectrocutedEffect(SpriteBatch batch) {
        // 임시: 파란색 깜빡임
        float alpha = (float) Math.sin(System.currentTimeMillis() * 0.01f) * 0.5f + 0.5f;
        batch.setColor(0.5f, 0.5f, 1f, alpha);
        batch.setColor(1, 1, 1, 1);
    }

    /**
     * 상태 변경
     */
    public void setState(MonsterState state) {
        if (this.state != state) {
            this.state = state;
            animation.resetStateTime();
        }
    }

    public MonsterState getState() {
        return state;
    }
}

/**
 * 몬스터 상태
 */
enum MonsterState {
    IDLE,       // 대기
    MOVING,     // 이동
    ATTACKING,  // 공격
    HIT,        // 피격
    DEAD        // 사망
}
```

### 2. MonsterAnimation 클래스

```java
/**
 * 몬스터 애니메이션
 *
 * 몬스터의 상태별 애니메이션을 관리합니다.
 */
public class MonsterAnimation {
    private MonsterType type;

    // 상태별 애니메이션
    private Map<MonsterState, Animation<TextureRegion>> animations;

    // 현재 상태 경과 시간
    private float stateTime = 0f;

    /**
     * 생성자
     */
    public MonsterAnimation(MonsterType type) {
        this.type = type;
        this.animations = new HashMap<>();

        loadAnimations();
    }

    /**
     * 애니메이션 로드
     */
    private void loadAnimations() {
        // 각 몬스터 타입별 애니메이션 로드
        String typePrefix = type.name().toLowerCase();

        // 대기 애니메이션
        animations.put(MonsterState.IDLE,
            createAnimation(typePrefix + "_idle", 4, 0.2f));

        // 이동 애니메이션
        animations.put(MonsterState.MOVING,
            createAnimation(typePrefix + "_move", 6, 0.15f));

        // 공격 애니메이션
        animations.put(MonsterState.ATTACKING,
            createAnimation(typePrefix + "_attack", 4, 0.1f));

        // 피격 애니메이션
        animations.put(MonsterState.HIT,
            createAnimation(typePrefix + "_hit", 2, 0.1f));

        // 사망 애니메이션
        animations.put(MonsterState.DEAD,
            createAnimation(typePrefix + "_dead", 6, 0.15f));
    }

    /**
     * 애니메이션 생성
     *
     * @param animationName 애니메이션 이름
     * @param frameCount 프레임 수
     * @param frameDuration 프레임 지속 시간
     */
    private Animation<TextureRegion> createAnimation(String animationName,
                                                     int frameCount, float frameDuration) {
        // 텍스처 아틀라스에서 프레임 가져오기
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            // 실제로는 AssetManager에서 텍스처 가져옴
            // frames[i] = AssetManager.getTextureRegion(animationName + "_" + i);

            // 임시: 기본 텍스처 사용
            frames[i] = createDefaultFrame();
        }

        return new Animation<>(frameDuration, frames);
    }

    /**
     * 기본 프레임 생성 (임시)
     */
    private TextureRegion createDefaultFrame() {
        // 임시 구현
        return new TextureRegion();
    }

    /**
     * 현재 프레임 가져오기
     */
    public TextureRegion getCurrentFrame(MonsterState state) {
        Animation<TextureRegion> animation = animations.get(state);

        if (animation == null) {
            return createDefaultFrame();
        }

        // 루프 여부 (사망은 루프 안 함)
        boolean looping = (state != MonsterState.DEAD);

        return animation.getKeyFrame(stateTime, looping);
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        stateTime += delta;
    }

    /**
     * 상태 시간 리셋
     */
    public void resetStateTime() {
        stateTime = 0f;
    }
}
```

### 3. HPBar 클래스

```java
/**
 * HP 바
 *
 * 엔티티의 체력을 시각적으로 표시합니다.
 */
public class HPBar {
    private Entity owner;

    private Vector2 position;
    private float width = 50f;
    private float height = 5f;

    // 색상
    private Color bgColor = new Color(0.2f, 0.2f, 0.2f, 0.8f);
    private Color hpColor = new Color(0f, 1f, 0f, 1f);  // 녹색
    private Color lowHpColor = new Color(1f, 0f, 0f, 1f);  // 빨간색

    /**
     * 생성자
     */
    public HPBar(Entity owner) {
        this.owner = owner;
        this.position = new Vector2();
    }

    /**
     * 위치 설정
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch batch) {
        int currentHP = owner.getHealth();
        int maxHP = owner.getMaxHealth();

        if (currentHP <= 0) return;  // 사망 시 표시 안 함

        float hpRatio = (float) currentHP / maxHP;

        // 배경 (검은색)
        batch.setColor(bgColor);
        // batch.draw(whitepixel, position.x, position.y, width, height);

        // HP 바 (현재 체력)
        Color currentColor = (hpRatio > 0.3f) ? hpColor : lowHpColor;
        batch.setColor(currentColor);
        // batch.draw(whitepixel, position.x, position.y, width * hpRatio, height);

        // 원래 색상 복원
        batch.setColor(1, 1, 1, 1);
    }
}
```

### 4. Ghost 클래스 (렌더링 추가)

```java
/**
 * 고스트 몬스터
 */
public class Ghost extends Monster {
    private static final int BASE_HP = 60;
    private static final int ATTACK = 15;
    private static final float SPEED = 120f;

    public Ghost() {
        super(MonsterType.GHOST);
    }

    @Override
    protected MonsterStats createStats() {
        return new MonsterStats(BASE_HP, ATTACK, SPEED);
    }

    @Override
    protected void initializeSprite() {
        // 텍스처 로드
        // this.sprite = AssetManager.getSprite("ghost");

        // 크기 설정
        this.setSize(48, 48);
    }

    @Override
    public float getWidth() {
        return 48f;
    }

    @Override
    public float getHeight() {
        return 48f;
    }
}
```

### 5. Slime 클래스

```java
/**
 * 슬라임 몬스터
 */
public class Slime extends Monster {
    private static final int BASE_HP = 40;
    private static final int ATTACK = 10;
    private static final float SPEED = 80f;

    public Slime() {
        super(MonsterType.SLIME);
    }

    @Override
    protected MonsterStats createStats() {
        return new MonsterStats(BASE_HP, ATTACK, SPEED);
    }

    @Override
    protected void initializeSprite() {
        // 텍스처 로드
        // this.sprite = AssetManager.getSprite("slime");

        this.setSize(40, 32);
    }

    @Override
    public float getWidth() {
        return 40f;
    }

    @Override
    public float getHeight() {
        return 32f;
    }
}
```

### 6. Golem 클래스

```java
/**
 * 골렘 몬스터
 */
public class Golem extends Monster {
    private static final int BASE_HP = 150;
    private static final int ATTACK = 25;
    private static final float SPEED = 60f;

    public Golem() {
        super(MonsterType.GOLEM);
    }

    @Override
    protected MonsterStats createStats() {
        return new MonsterStats(BASE_HP, ATTACK, SPEED);
    }

    @Override
    protected void initializeSprite() {
        // 텍스처 로드
        // this.sprite = AssetManager.getSprite("golem");

        this.setSize(64, 64);
    }

    @Override
    public float getWidth() {
        return 64f;
    }

    @Override
    public float getHeight() {
        return 64f;
    }
}
```

---

## 🧪 테스트 계획

```java
public class TestMonsterRendering {
    private Ghost ghost;
    private SpriteBatch mockBatch;

    @BeforeEach
    public void setUp() {
        ghost = new Ghost();
        ghost.setPosition(100, 100);
        mockBatch = new SpriteBatch();
    }

    @Test
    public void 몬스터_렌더링() {
        assertNotNull(ghost.sprite);
        ghost.render(mockBatch);
    }

    @Test
    public void HP바_렌더링() {
        ghost.setHealth(30);  // 절반
        ghost.render(mockBatch);

        HPBar hpBar = ghost.hpBar;
        assertNotNull(hpBar);
    }

    @Test
    public void 상태_애니메이션_전환() {
        ghost.setState(MonsterState.MOVING);
        assertEquals(MonsterState.MOVING, ghost.getState());

        ghost.update(0.1f);
        // 애니메이션 프레임 변경 확인
    }
}

public class TestMonsterAnimation {
    private MonsterAnimation animation;

    @BeforeEach
    public void setUp() {
        animation = new MonsterAnimation(MonsterType.GHOST);
    }

    @Test
    public void 애니메이션_프레임_가져오기() {
        TextureRegion frame = animation.getCurrentFrame(MonsterState.IDLE);
        assertNotNull(frame);
    }

    @Test
    public void 상태_시간_리셋() {
        animation.update(1.0f);
        animation.resetStateTime();

        assertEquals(0f, animation.stateTime, 0.01f);
    }
}

public class TestHPBar {
    private HPBar hpBar;
    private Monster monster;

    @BeforeEach
    public void setUp() {
        monster = new Ghost();
        monster.setHealth(60);
        hpBar = new HPBar(monster);
    }

    @Test
    public void HP바_위치_설정() {
        hpBar.setPosition(100, 100);
        assertEquals(100f, hpBar.position.x, 0.01f);
        assertEquals(100f, hpBar.position.y, 0.01f);
    }

    @Test
    public void HP바_렌더링() {
        SpriteBatch mockBatch = new SpriteBatch();
        hpBar.render(mockBatch);
    }
}
```

---

## ✅ 완료 조건

- [ ] Monster 렌더링 기능 구현
- [ ] MonsterAnimation 시스템 구현
- [ ] HPBar 구현
- [ ] 3가지 몬스터 타입 렌더링
- [ ] 상태별 애니메이션 전환 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_21: 몬스터 서버 동기화**
- MonsterSpawnMsg
- MonsterUpdateMsg
- 몬스터 위치/상태 동기화
