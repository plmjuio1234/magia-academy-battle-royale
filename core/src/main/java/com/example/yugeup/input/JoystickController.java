package com.example.yugeup.input;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.utils.Constants;

/**
 * 조이스틱 컨트롤러 클래스
 *
 * 모바일 가상 조이스틱을 구현합니다.
 * 터치 입력을 방향 벡터로 변환하여 플레이어 이동을 제어합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class JoystickController {

    // 조이스틱 중심 위치 (월드 좌표)
    private Vector2 basePosition;

    // 스틱 현재 위치 (월드 좌표)
    private Vector2 stickPosition;

    // 조이스틱 반경 (기본값, 카메라 줌에 따라 조정됨)
    private float radius;
    private float baseRadius;  // 원본 반경

    // 배경 반경 (터치 감지 영역)
    private float bgRadius;
    private float baseBgRadius;  // 원본 배경 반경

    // 스틱 반경
    private float stickRadius;
    private float baseStickRadius;  // 원본 스틱 반경

    // 데드존 (입력 인식 최소값)
    private float deadZone;

    // 입력 상태
    private boolean isDragging;

    // 터치 포인터 ID
    private int touchPointer;

    // 스틱의 상대 오프셋 (basePosition으로부터의 거리)
    private Vector2 stickOffset;

    // Viewport (좌표 변환용)
    private Viewport viewport;

    /**
     * JoystickController 생성자
     *
     * @param viewport 게임 뷰포트
     */
    public JoystickController(Viewport viewport) {
        this.viewport = viewport;

        // Constants에서 값 가져오기
        this.baseRadius = Constants.JOYSTICK_RADIUS;
        this.baseBgRadius = Constants.JOYSTICK_BG_RADIUS;
        this.baseStickRadius = Constants.JOYSTICK_STICK_RADIUS;
        this.radius = baseRadius;
        this.bgRadius = baseBgRadius;
        this.stickRadius = baseStickRadius;
        this.deadZone = Constants.JOYSTICK_DEADZONE;

        // 조이스틱 위치: 초기화 (updatePosition으로 설정됨)
        this.basePosition = new Vector2(0, 0);
        this.stickPosition = new Vector2(basePosition);
        this.stickOffset = new Vector2(0, 0);

        // 초기 상태
        this.isDragging = false;
        this.touchPointer = -1;
    }

    /**
     * 터치 다운 이벤트 처리
     *
     * @param screenX 터치 X 좌표 (스크린 좌표)
     * @param screenY 터치 Y 좌표 (스크린 좌표)
     * @param pointer 포인터 ID
     * @return 조이스틱 영역 내 터치인 경우 true
     */
    public boolean onTouchDown(int screenX, int screenY, int pointer) {
        // 스크린 좌표를 월드 좌표로 변환
        Vector2 touchPos = screenToWorld(screenX, screenY);

        // 조이스틱 범위 내 터치인가?
        float distance = touchPos.dst(basePosition);

        if (distance <= bgRadius * Constants.JOYSTICK_TOUCH_RANGE) {
            isDragging = true;
            touchPointer = pointer;
            updateStickPosition(touchPos);
            return true;
        }

        return false;
    }

    /**
     * 터치 드래그 이벤트 처리
     *
     * @param screenX 터치 X 좌표
     * @param screenY 터치 Y 좌표
     * @param pointer 포인터 ID
     */
    public void onTouchDragged(int screenX, int screenY, int pointer) {
        if (!isDragging || pointer != touchPointer) {
            return;
        }

        // 스크린 좌표를 월드 좌표로 변환
        Vector2 touchPos = screenToWorld(screenX, screenY);
        updateStickPosition(touchPos);
    }

    /**
     * 터치 업 이벤트 처리
     *
     * @param pointer 포인터 ID
     */
    public void onTouchUp(int pointer) {
        if (pointer == touchPointer) {
            isDragging = false;
            touchPointer = -1;
            stickOffset.set(0, 0);  // 오프셋 리셋
            stickPosition.set(basePosition);  // 중심으로 복귀
        }
    }

    /**
     * 스틱 위치 업데이트
     *
     * @param touchPos 터치 위치 (월드 좌표)
     */
    private void updateStickPosition(Vector2 touchPos) {
        // 중심에서 터치 위치까지의 벡터
        Vector2 delta = new Vector2(touchPos).sub(basePosition);

        // 반경을 초과하지 않도록 제한
        if (delta.len() > radius) {
            delta.nor().scl(radius);
        }

        // 상대 오프셋 저장 (basePosition으로부터의 거리)
        stickOffset.set(delta);

        // 스틱 위치 업데이트
        stickPosition.set(basePosition).add(stickOffset);
    }

    /**
     * 현재 입력 방향 벡터 반환 (정규화됨, 0~1 범위)
     *
     * @return 방향 벡터 (x, y 각각 -1~1)
     */
    public Vector2 getDirection() {
        if (!isDragging) {
            return new Vector2(0, 0);
        }

        // 스틱 위치에서 중심까지의 벡터
        Vector2 direction = new Vector2(stickPosition).sub(basePosition);

        // 정규화 (최대 거리로 나눔)
        if (direction.len() > 0.01f) {
            direction.scl(1f / radius);  // -1 ~ 1 범위로 변환
        }

        // 데드존 적용
        if (direction.len() < deadZone) {
            return new Vector2(0, 0);
        }

        // 방향 유지하되 크기를 1로 정규화
        return direction.nor();
    }

    /**
     * 조이스틱을 렌더링합니다 (ShapeRenderer 사용)
     * 외부 원: 반투명 (그라데이션으로 중심부터 투명도 증가)
     * 내부 원: 크기 축소 (50% 크기)
     *
     * @param shapeRenderer ShapeRenderer 인스턴스
     */
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // === 배경 (외부 원) - 반투명 처리 ===
        // 바깥쪽: 불투명 (alpha 0.6f)
        // 안쪽: 투명함 (alpha 0.1f)
        // 그라데이션 효과를 위해 여러 원을 겹쳐 그림
        int segments = 30;

        // 바깥쪽부터 안쪽으로 점진적으로 투명해지는 효과
        float outerAlpha = 0.6f;
        float innerAlpha = 0.05f;
        int layerCount = 5;  // 투명도 레이어 수

        for (int layer = 0; layer < layerCount; layer++) {
            // 현재 레이어의 반경 계산 (바깥쪽부터 안쪽으로)
            float layerRadius = bgRadius * (1f - (float)layer / layerCount);

            // 현재 레이어의 투명도 계산 (바깥쪽이 더 불투명, 안쪽이 투명)
            float alpha = outerAlpha - (outerAlpha - innerAlpha) * ((float)layer / layerCount);

            shapeRenderer.setColor(0.2f, 0.2f, 0.25f, alpha);
            shapeRenderer.circle(basePosition.x, basePosition.y, layerRadius, segments);
        }

        // === 스틱 (내부 원) - 크기 축소 ===
        // 원본 크기의 50%로 축소
        float reducedStickRadius = stickRadius * 0.5f;
        shapeRenderer.setColor(0.8f, 0.8f, 0.85f, 0.8f);  // 밝은 흰색, 불투명
        shapeRenderer.circle(stickPosition.x, stickPosition.y, reducedStickRadius, 20);

        shapeRenderer.end();
    }

    /**
     * 스크린 좌표를 월드 좌표로 변환
     *
     * Viewport의 unproject()를 사용하여 정확한 좌표 변환
     *
     * @param screenX 스크린 X 좌표
     * @param screenY 스크린 Y 좌표
     * @return 월드 좌표 Vector2
     */
    private Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 worldPos = new Vector3(screenX, screenY, 0);
        viewport.unproject(worldPos);
        return new Vector2(worldPos.x, worldPos.y);
    }

    /**
     * 조이스틱 위치 업데이트 (플레이어 위치 기준)
     *
     * @param playerX 플레이어 X 좌표
     * @param playerY 플레이어 Y 좌표
     * @param camera 게임 카메라 (줌 고려)
     */
    public void updatePosition(float playerX, float playerY, com.badlogic.gdx.graphics.OrthographicCamera camera) {
        // 카메라 줌을 고려한 실제 보이는 영역 크기
        float viewportWorldWidth = viewport.getWorldWidth() * camera.zoom;
        float viewportWorldHeight = viewport.getWorldHeight() * camera.zoom;

        // 조이스틱 크기를 카메라 줌에 맞춰 조정
        this.radius = baseRadius * camera.zoom;
        this.bgRadius = baseBgRadius * camera.zoom;
        this.stickRadius = baseStickRadius * camera.zoom;

        // 카메라 뷰포트의 좌하단 기준으로 오프셋 계산
        float offsetX = -(viewportWorldWidth * 0.5f) + Constants.JOYSTICK_X * camera.zoom;
        float offsetY = -(viewportWorldHeight * 0.5f) + Constants.JOYSTICK_Y * camera.zoom;

        // 플레이어 위치 기준으로 조이스틱 배치 (항상 업데이트)
        basePosition.set(playerX + offsetX, playerY + offsetY);

        // 드래그 중이면 저장된 오프셋을 적용하여 스틱 위치 유지
        if (isDragging) {
            stickPosition.set(basePosition).add(stickOffset);
        } else {
            // 드래그 중이 아니면 스틱도 중심으로
            stickPosition.set(basePosition);
        }
    }

    /**
     * 조이스틱 초기화
     */
    public void reset() {
        isDragging = false;
        touchPointer = -1;
        stickPosition.set(basePosition);
    }

    // ===== Getter & Setter =====

    public Vector2 getBasePosition() {
        return basePosition;
    }

    public Vector2 getStickPosition() {
        return stickPosition;
    }

    public float getRadius() {
        return radius;
    }

    public float getBgRadius() {
        return bgRadius;
    }

    public float getStickRadius() {
        return stickRadius;
    }

    public boolean isDragging() {
        return isDragging;
    }
}
