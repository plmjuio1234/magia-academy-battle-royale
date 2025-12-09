# PHASE_08_PLAYER_CONTROL.md - 플레이어 조작 (조이스틱)

---

## 🎯 목표
모바일 조이스틱 입력으로 플레이어 이동 구현

---

## 📋 구현 범위

- ✅ 모바일 조이스틱 UI (화면 좌측 하단)
- ✅ 터치 입력 감지
- ✅ 플레이어 이동 벡터 계산
- ✅ 서버로 위치 동기화 (PlayerMoveMsg)

---

## 📁 필요 파일

```
input/
  ├─ InputHandler.java
  ├─ JoystickController.java       (새로 생성)
  └─ TouchInputListener.java

game/player/
  └─ PlayerController.java         (새로 생성)
```

---

## 🔧 구현 가이드

### JoystickController 클래스

```java
/**
 * 모바일 조이스틱 컨트롤러
 *
 * 터치 입력으로 플레이어 이동 방향을 제어합니다.
 */
public class JoystickController {
    // 조이스틱 위치 및 크기
    private Vector2 stickBasePosition;   // 조이스틱 중심
    private Vector2 stickTouchPosition;  // 터치 위치
    private float stickRadius;          // 조이스틱 반경
    private float deadZone = 0.2f;      // 데드존 (입력 인식 최소값)

    // 입력 상태
    private boolean isDragging = false;
    private int touchPointer = -1;      // 터치 포인터 ID

    // 화면 설정
    private float screenWidth;
    private float screenHeight;

    public JoystickController(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // 조이스틱 위치: 화면 좌측 하단
        // 크기: 화면 너비의 18%
        this.stickRadius = screenWidth * 0.09f;
        this.stickBasePosition = new Vector2(
            stickRadius + 20,           // 왼쪽에서 20px
            stickRadius + 20            // 아래에서 20px
        );
        this.stickTouchPosition = new Vector2(stickBasePosition);
    }

    /**
     * 터치 다운 처리
     */
    public boolean onTouchDown(int screenX, int screenY, int pointer) {
        // 조이스틱 범위 내 터치인가?
        Vector2 touchPos = screenToWorld(screenX, screenY);
        float distance = touchPos.dst(stickBasePosition);

        if (distance <= stickRadius * 1.5f) {  // 터치 범위 여유
            isDragging = true;
            touchPointer = pointer;
            return true;
        }
        return false;
    }

    /**
     * 터치 드래그 처리
     */
    public void onTouchDragged(int screenX, int screenY, int pointer) {
        if (!isDragging || pointer != touchPointer) return;

        Vector2 touchPos = screenToWorld(screenX, screenY);

        // 중심에서 터치 위치까지의 거리 계산
        Vector2 delta = new Vector2(touchPos).sub(stickBasePosition);

        // 반경을 초과하지 않도록 제한
        if (delta.len() > stickRadius) {
            delta.nor().scl(stickRadius);
        }

        // 스틱 끝 위치 업데이트
        stickTouchPosition.set(stickBasePosition).add(delta);
    }

    /**
     * 터치 업 처리
     */
    public void onTouchUp(int pointer) {
        if (pointer == touchPointer) {
            isDragging = false;
            touchPointer = -1;
            stickTouchPosition.set(stickBasePosition);  // 중심으로 돌아감
        }
    }

    /**
     * 현재 입력 방향 벡터 반환 (0~1 범위)
     */
    public Vector2 getDirection() {
        if (!isDragging) {
            return new Vector2(0, 0);
        }

        // 스틱 위치에서 중심을 뺌
        Vector2 direction = new Vector2(stickTouchPosition)
            .sub(stickBasePosition)
            .nor();  // 정규화 (크기 1로)

        // 데드존 적용
        if (direction.len() < deadZone) {
            return new Vector2(0, 0);
        }

        return direction;
    }

    /**
     * 조이스틱 렌더링
     */
    public void render(SpriteBatch batch) {
        // 조이스틱 배경 (원형)
        batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
        drawCircle(batch, stickBasePosition, stickRadius * 1.2f);

        // 조이스틱 스틱 (원형)
        batch.setColor(0.7f, 0.7f, 0.7f, 0.7f);
        drawCircle(batch, stickTouchPosition, stickRadius * 0.5f);

        batch.setColor(1, 1, 1, 1);
    }

    /**
     * 스크린 좌표를 월드 좌표로 변환
     */
    private Vector2 screenToWorld(int screenX, int screenY) {
        // Android 화면 좌표는 좌상단 기준, libGDX는 좌하단 기준
        return new Vector2(screenX, screenHeight - screenY);
    }

    /**
     * 원형 그리기 (간단한 구현)
     */
    private void drawCircle(SpriteBatch batch, Vector2 center, float radius) {
        // 실제로는 Texture 또는 ShapeRenderer 사용
        // 임시: 사각형으로 대체
        batch.draw(whitepixel, center.x - radius, center.y - radius,
            radius * 2, radius * 2);
    }

    public Vector2 getStickBasePosition() {
        return stickBasePosition;
    }

    public Vector2 getStickTouchPosition() {
        return stickTouchPosition;
    }

    public float getStickRadius() {
        return stickRadius;
    }

    public boolean isDragging() {
        return isDragging;
    }
}
```

### PlayerController 클래스

```java
/**
 * 플레이어 컨트롤러
 *
 * 입력 처리 및 플레이어 이동을 담당합니다.
 */
public class PlayerController implements InputProcessor {
    private Player player;
    private JoystickController joystickController;
    private NetworkManager networkManager;

    // 이동 동기화 타이머
    private float syncTimer = 0f;
    private static final float SYNC_INTERVAL = 0.1f;  // 100ms마다 동기화

    public PlayerController(Player player, float screenWidth, float screenHeight) {
        this.player = player;
        this.joystickController = new JoystickController(screenWidth, screenHeight);
        this.networkManager = NetworkManager.getInstance();
    }

    /**
     * 매 프레임 업데이트
     */
    public void update(float delta) {
        // 조이스틱 입력으로 플레이어 방향 결정
        Vector2 direction = joystickController.getDirection();

        // 플레이어 이동
        if (direction.len() > 0) {
            // 이동 속도 = 능력치 * 방향
            float speed = player.getStats().getSpeed();
            player.setVelocity(
                direction.x * speed,
                direction.y * speed
            );
            player.setState(PlayerState.MOVING);
        } else {
            // 입력 없음
            player.setVelocity(0, 0);
            player.setState(PlayerState.IDLE);
        }

        // 위치 동기화 (주기적)
        syncTimer += delta;
        if (syncTimer >= SYNC_INTERVAL) {
            sendPlayerMove();
            syncTimer = 0;
        }
    }

    /**
     * 서버로 플레이어 위치 전송
     */
    private void sendPlayerMove() {
        PlayerMoveMsg msg = new PlayerMoveMsg();
        msg.playerId = player.getId();
        msg.x = player.getPosition().x;
        msg.y = player.getPosition().y;

        networkManager.sendMessage(msg);
    }

    /**
     * 조이스틱 렌더링
     */
    public void render(SpriteBatch batch) {
        joystickController.render(batch);
    }

    // ===== InputProcessor 구현 =====

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return joystickController.onTouchDown(screenX, screenY, pointer);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        joystickController.onTouchDragged(screenX, screenY, pointer);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        joystickController.onTouchUp(pointer);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) { return false; }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
}
```

---

## 🧪 테스트 계획

```java
public class TestJoystickController {
    private JoystickController joystick;

    @BeforeEach
    public void setUp() {
        joystick = new JoystickController(1080, 1920);
    }

    @Test
    public void 조이스틱_범위_내_터치_감지() {
        boolean result = joystick.onTouchDown(100, 1900, 0);
        assertTrue(result);
    }

    @Test
    public void 입력_없을_때_방향_벡터는_영벡터() {
        Vector2 direction = joystick.getDirection();
        assertEquals(0, direction.len(), 0.01f);
    }

    @Test
    public void 드래그_시_방향_벡터_생성() {
        joystick.onTouchDown(100, 1900, 0);
        joystick.onTouchDragged(150, 1850, 0);
        Vector2 direction = joystick.getDirection();
        assertTrue(direction.len() > 0);
    }

    @Test
    public void 반경을_초과하지_않음() {
        joystick.onTouchDown(100, 1900, 0);
        joystick.onTouchDragged(500, 500, 0);  // 멀리 드래그
        Vector2 pos = joystick.getStickTouchPosition();
        float distance = pos.dst(joystick.getStickBasePosition());
        assertTrue(distance <= joystick.getStickRadius());
    }
}

public class TestPlayerController {
    private PlayerController controller;
    private Player player;

    @BeforeEach
    public void setUp() {
        player = new Player(1);
        controller = new PlayerController(player, 1080, 1920);
    }

    @Test
    public void 플레이어_이동_속도_계산() {
        player.getStats().setSpeed(300);
        controller.joystickController.onTouchDown(100, 1900, 0);
        controller.joystickController.onTouchDragged(150, 1850, 0);

        controller.update(0.016f);

        assertTrue(Math.abs(player.getVelocity().len()) > 0);
    }

    @Test
    public void 위치_동기화_주기() {
        // 100ms 미만: 동기화 안 함
        controller.update(0.05f);
        // (확인 필요)

        // 100ms 초과: 동기화 함
        controller.update(0.06f);
        // (확인 필요)
    }
}
```

---

## ✅ 완료 조건

- [ ] JoystickController 구현
- [ ] PlayerController 구현
- [ ] 조이스틱 렌더링
- [ ] 터치 입력 감지
- [ ] 플레이어 이동 동작 확인
- [ ] 서버 동기화 작동 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_09: 카메라 & 맵 렌더링**

