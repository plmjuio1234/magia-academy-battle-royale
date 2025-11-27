# PHASE_07_GAME_SCREEN.md - 게임 화면 기본 구성

---

## 🎯 목표
게임 플레이 화면의 기본 구조 구현
(렌더링 루프, 카메라 시스템, 플레이어 렌더링)

---

## 📋 구현 범위

### 게임 루프
- ✅ 60fps 게임 루프
- ✅ update(delta) → render() 주기
- ✅ delta time 계산 (프레임 독립적 움직임)

### 렌더링 시스템
- ✅ SpriteBatch 기본 설정
- ✅ 카메라 설정 (플레이어 중심)
- ✅ 뷰포트 관리

### 플레이어 표시
- ✅ 로컬 플레이어 렌더링 (화면 중앙)
- ✅ 플레이어 위치 좌표 시스템

### UI 기본
- ✅ HUD 레이어 (나중에 추가)
- ✅ 디버그 정보 표시 (선택사항)

---

## 📁 필요 파일

### 생성할 파일
```
screens/
  └─ GameScreen.java              (새로 생성)

game/
  ├─ GameManager.java             (새로 생성)
  └─ GameState.java               (새로 생성)

camera/
  └─ CameraController.java        (새로 생성)
```

### 기존 파일 수정
```
YuGeupLauncher.java              (수정 - GameScreen으로 시작)
Constants.java                    (수정 - 게임 상수 추가)
```

---

## 🔧 구현 가이드

### 1. GameScreen 클래스

```java
/**
 * 게임 화면
 *
 * 게임의 주요 루프가 실행되는 화면입니다.
 * 플레이어, 몬스터, 스킬 등 모든 게임 요소를 관리합니다.
 */
public class GameScreen implements IScreen {
    private GameManager gameManager;
    private CameraController cameraController;
    private SpriteBatch batch;
    private OrthogonalCamera camera;

    // 게임 상태
    private float gameTimer = 0f;
    private static final float GAME_END_TIME = 600f;  // 10분

    // 플레이어 정보
    private Player localPlayer;
    private Map<Integer, Player> remotePlayers = new HashMap<>();

    public GameScreen(Player player) {
        this.localPlayer = player;
        this.gameManager = GameManager.getInstance();
        this.batch = new SpriteBatch();
        this.camera = new OrthogonalCamera();
        this.cameraController = new CameraController(camera);
    }

    @Override
    public void show() {
        // 게임 시작 신호
        gameManager.startGame();
        gameTimer = 0f;

        // 네트워크 메시지 리스너 등록
        MessageHandler.getInstance().addPlayerMoveListener(this);
        MessageHandler.getInstance().addGameEventListener(this);
    }

    @Override
    public void hide() {
        // 게임 종료 신호
        gameManager.stopGame();

        // 메시지 리스너 제거
        MessageHandler.getInstance().removePlayerMoveListener(this);
        MessageHandler.getInstance().removeGameEventListener(this);
    }

    @Override
    public void update(float delta) {
        // 게임 시간 증가
        gameTimer += delta;

        // 게임 종료 확인
        if (gameTimer >= GAME_END_TIME) {
            endGame();
            return;
        }

        // 게임 매니저 업데이트
        gameManager.update(delta);

        // 로컬 플레이어 업데이트
        if (localPlayer != null) {
            localPlayer.update(delta);
        }

        // 원격 플레이어 업데이트
        for (Player player : remotePlayers.values()) {
            player.update(delta);
        }

        // 카메라 업데이트 (플레이어 따라가기)
        if (localPlayer != null) {
            cameraController.update(localPlayer.getPosition());
        }

        // 입력 처리 (다음 Phase에서)
        // handleInput();
    }

    @Override
    public void render(SpriteBatch batch) {
        // 화면 클리어
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 카메라 적용
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // 게임 월드 렌더링
        renderGameWorld(batch);

        batch.end();

        // HUD 렌더링 (화면 고정)
        renderHUD(batch);
    }

    /**
     * 게임 월드 렌더링 (카메라 적용)
     */
    private void renderGameWorld(SpriteBatch batch) {
        // 맵 렌더링
        gameManager.getGameMap().render(batch);

        // 몬스터 렌더링
        for (Monster monster : gameManager.getMonsters()) {
            monster.render(batch);
        }

        // 원격 플레이어 렌더링
        for (Player player : remotePlayers.values()) {
            player.render(batch);
        }

        // 로컬 플레이어 렌더링 (중앙)
        if (localPlayer != null) {
            localPlayer.render(batch);
        }

        // 스킬 이펙트 렌더링
        gameManager.getSkillEffects().forEach(effect -> effect.render(batch));
    }

    /**
     * HUD 렌더링 (화면 고정, 카메라 미적용)
     */
    private void renderHUD(SpriteBatch batch) {
        // 카메라 미적용 (화면 고정)
        OrthogonalCamera hudCamera = new OrthogonalCamera();
        hudCamera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.setProjectionMatrix(hudCamera.combined);

        batch.begin();

        // 나중에 HUD 컴포넌트 렌더링
        // (PHASE_08에서 구현)

        batch.end();
    }

    /**
     * 게임 종료
     */
    private void endGame() {
        // 결과 계산
        GameResult result = gameManager.calculateResult();

        // 결과 화면으로 전환
        ScreenManager.getInstance().setScreen(new ResultScreen(result));
    }

    /**
     * 원격 플레이어 위치 업데이트 (서버로부터 수신)
     */
    public void onPlayerMoveReceived(PlayerMoveMsg msg) {
        if (msg.playerId == localPlayer.getId()) {
            return;  // 로컬 플레이어는 무시
        }

        Player remotePlayer = remotePlayers.get(msg.playerId);
        if (remotePlayer == null) {
            // 처음 보는 플레이어 - 생성
            remotePlayer = new Player(msg.playerId);
            remotePlayers.put(msg.playerId, remotePlayer);
        }

        // 위치 업데이트
        remotePlayer.setPosition(msg.x, msg.y);
    }

    /**
     * 새 플레이어 입장
     */
    public void onPlayerJoined(int playerId) {
        if (playerId != localPlayer.getId()) {
            Player newPlayer = new Player(playerId);
            remotePlayers.put(playerId, newPlayer);
        }
    }

    /**
     * 플레이어 퇴장
     */
    public void onPlayerLeft(int playerId) {
        remotePlayers.remove(playerId);
    }

    /**
     * 플레이어 사망
     */
    public void onPlayerDeath(int playerId) {
        if (playerId == localPlayer.getId()) {
            // 로컬 플레이어 사망
            endGame();
        } else {
            // 원격 플레이어 사망 - 맵에서 제거
            remotePlayers.remove(playerId);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameManager.dispose();
    }
}
```

### 2. GameManager 싱글톤

```java
/**
 * 게임 관리자
 *
 * 게임의 전반적인 상태와 진행을 관리합니다.
 */
public class GameManager {
    private static GameManager instance;

    private boolean isGameRunning = false;
    private GameMap gameMap;
    private MonsterManager monsterManager;
    private List<Sprite> skillEffects = new ArrayList<>();

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * 게임 시작
     */
    public void startGame() {
        isGameRunning = true;
        gameMap = new GameMap();
        monsterManager = new MonsterManager();
    }

    /**
     * 게임 중단
     */
    public void stopGame() {
        isGameRunning = false;
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        if (!isGameRunning) return;

        // 맵 업데이트 (자기장 진행)
        gameMap.update(delta);

        // 몬스터 업데이트
        monsterManager.update(delta);

        // 이펙트 업데이트 및 제거
        skillEffects.removeIf(effect -> {
            effect.update(delta);  // 수명 감소
            return !effect.isAlive();
        });
    }

    /**
     * 게임 결과 계산
     */
    public GameResult calculateResult() {
        // 미구현 (PHASE_26에서)
        return new GameResult();
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public MonsterManager getMonsterManager() {
        return monsterManager;
    }

    public List<Monster> getMonsters() {
        return monsterManager.getMonsters();
    }

    public List<Sprite> getSkillEffects() {
        return skillEffects;
    }

    public void dispose() {
        if (gameMap != null) gameMap.dispose();
        if (monsterManager != null) monsterManager.dispose();
    }
}
```

### 3. CameraController 클래스

```java
/**
 * 카메라 컨트롤러
 *
 * 플레이어를 따라다니는 카메라를 관리합니다.
 */
public class CameraController {
    private OrthogonalCamera camera;
    private Vector2 targetPosition;
    private float smoothSpeed = 5f;  // 카메라 부드러움

    public CameraController(OrthogonalCamera camera) {
        this.camera = camera;
        this.targetPosition = new Vector2();

        // 카메라 초기 설정
        camera.setToOrtho(false,
            Constants.SCREEN_WIDTH,
            Constants.SCREEN_HEIGHT);
    }

    /**
     * 카메라 업데이트
     */
    public void update(Vector2 playerPosition) {
        // 목표 위치 설정
        targetPosition.set(playerPosition);

        // 부드러운 이동 (Lerp)
        camera.position.x += (targetPosition.x - camera.position.x) * smoothSpeed * Gdx.graphics.getDeltaTime();
        camera.position.y += (targetPosition.y - camera.position.y) * smoothSpeed * Gdx.graphics.getDeltaTime();

        // 맵 경계 제한
        clampCameraToMapBounds();

        // 카메라 업데이트
        camera.update();
    }

    /**
     * 맵 경계 내로 카메라 제한
     */
    private void clampCameraToMapBounds() {
        float mapWidth = 1920f;
        float mapHeight = 1920f;
        float screenWidth = Constants.SCREEN_WIDTH;
        float screenHeight = Constants.SCREEN_HEIGHT;

        // 최소 경계
        camera.position.x = Math.max(screenWidth / 2, camera.position.x);
        camera.position.y = Math.max(screenHeight / 2, camera.position.y);

        // 최대 경계
        camera.position.x = Math.min(mapWidth - screenWidth / 2, camera.position.x);
        camera.position.y = Math.min(mapHeight - screenHeight / 2, camera.position.y);
    }

    public OrthogonalCamera getCamera() {
        return camera;
    }
}
```

---

## 🧪 테스트 계획

```java
/**
 * GameScreen 테스트
 */
public class TestGameScreen {
    private GameScreen gameScreen;
    private Player testPlayer;

    @BeforeEach
    public void setUp() {
        testPlayer = new Player(1);
        testPlayer.setPosition(100, 100);
        gameScreen = new GameScreen(testPlayer);
    }

    @Test
    public void 게임_화면이_생성된다() {
        assertNotNull(gameScreen);
    }

    @Test
    public void 게임이_시작되면_게임_타이머가_증가한다() {
        gameScreen.show();
        gameScreen.update(1.0f);
        assertTrue(gameScreen.gameTimer > 0);
    }

    @Test
    public void 10분_경과_후_게임이_종료된다() {
        gameScreen.show();
        gameScreen.update(601f);  // 601초 (10분 초과)
        // 게임 종료 확인
    }

    @Test
    public void 원격_플레이어가_위치_업데이트를_받는다() {
        PlayerMoveMsg msg = new PlayerMoveMsg();
        msg.playerId = 2;
        msg.x = 500;
        msg.y = 500;

        gameScreen.onPlayerMoveReceived(msg);

        Player remotePlayer = gameScreen.remotePlayers.get(2);
        assertNotNull(remotePlayer);
        assertEquals(500, remotePlayer.x);
        assertEquals(500, remotePlayer.y);
    }

    @Test
    public void 새_플레이어_입장_처리() {
        gameScreen.onPlayerJoined(3);
        assertTrue(gameScreen.remotePlayers.containsKey(3));
    }

    @Test
    public void 플레이어_퇴장_처리() {
        gameScreen.onPlayerJoined(3);
        gameScreen.onPlayerLeft(3);
        assertFalse(gameScreen.remotePlayers.containsKey(3));
    }
}

/**
 * GameManager 테스트
 */
public class TestGameManager {
    private GameManager gameManager;

    @BeforeEach
    public void setUp() {
        gameManager = GameManager.getInstance();
    }

    @Test
    public void 게임_시작() {
        gameManager.startGame();
        assertTrue(gameManager.isGameRunning());
    }

    @Test
    public void 게임_중단() {
        gameManager.startGame();
        gameManager.stopGame();
        assertFalse(gameManager.isGameRunning());
    }

    @Test
    public void 맵이_초기화된다() {
        gameManager.startGame();
        assertNotNull(gameManager.getGameMap());
    }

    @Test
    public void 몬스터_관리자가_초기화된다() {
        gameManager.startGame();
        assertNotNull(gameManager.getMonsterManager());
    }
}

/**
 * CameraController 테스트
 */
public class TestCameraController {
    private CameraController cameraController;
    private OrthogonalCamera camera;

    @BeforeEach
    public void setUp() {
        camera = new OrthogonalCamera();
        cameraController = new CameraController(camera);
    }

    @Test
    public void 카메라가_플레이어를_따라간다() {
        Vector2 playerPos = new Vector2(500, 500);
        cameraController.update(playerPos);

        // 카메라가 플레이어 방향으로 이동 (완전 같지는 않음, smooth)
        assertTrue(Math.abs(camera.position.x - 500) < 100);
    }

    @Test
    public void 카메라가_맵_경계를_벗어나지_않는다() {
        Vector2 playerPos = new Vector2(0, 0);  // 맵 왼쪽 끝
        cameraController.update(playerPos);

        assertTrue(camera.position.x >= Constants.SCREEN_WIDTH / 2);
    }
}
```

---

## ✅ 완료 조건

- [ ] GameScreen 클래스 구현
- [ ] GameManager 싱글톤 구현
- [ ] CameraController 구현
- [ ] 게임 루프 60fps 동작 확인
- [ ] 플레이어 렌더링 확인
- [ ] 카메라 플레이어 추적 확인
- [ ] 모든 단위 테스트 통과

---

## 🔗 다음 Phase 연결점

**PHASE_08: 플레이어 조작 (조이스틱)**
- PlayerController 구현
- 조이스틱 입력 처리

