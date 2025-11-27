package com.example.yugeup.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * 화면 전환 애니메이션 유틸리티
 *
 * 원형 확장 효과로 화면 전환을 부드럽게 만듭니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ScreenTransition {

    private ShapeRenderer shapeRenderer;
    private float transitionTime;
    private float currentTime;
    private boolean isTransitioning;
    private TransitionType type;

    /** 전환 애니메이션 타입 */
    public enum TransitionType {
        FADE_IN,   // 페이드 인 (검은색 → 투명)
        FADE_OUT,  // 페이드 아웃 (투명 → 검은색)
        CIRCLE_EXPAND,  // 원형 확장 (중앙에서 확장)
        CIRCLE_SHRINK   // 원형 축소 (중앙으로 축소)
    }

    /**
     * ScreenTransition을 생성합니다.
     */
    public ScreenTransition() {
        this.shapeRenderer = new ShapeRenderer();
        this.transitionTime = 0f;
        this.currentTime = 0f;
        this.isTransitioning = false;
        this.type = TransitionType.CIRCLE_EXPAND;
    }

    /**
     * 전환 애니메이션을 시작합니다.
     *
     * @param type 전환 타입
     * @param duration 전환 시간 (초)
     */
    public void start(TransitionType type, float duration) {
        this.type = type;
        this.transitionTime = duration;
        this.currentTime = 0f;
        this.isTransitioning = true;
    }

    /**
     * 전환 애니메이션을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간
     */
    public void update(float delta) {
        if (isTransitioning) {
            currentTime += delta;
            if (currentTime >= transitionTime) {
                currentTime = transitionTime;
                isTransitioning = false;
            }
        }
    }

    /**
     * 전환 애니메이션을 렌더링합니다.
     *
     * @param camera 렌더링에 사용할 카메라
     */
    public void render(com.badlogic.gdx.graphics.OrthographicCamera camera) {
        if (!isTransitioning && currentTime >= transitionTime) {
            return;  // 전환 완료됨
        }

        // ShapeRenderer에 카메라의 projection matrix 적용
        shapeRenderer.setProjectionMatrix(camera.combined);

        float progress = MathUtils.clamp(currentTime / transitionTime, 0f, 1f);

        switch (type) {
            case FADE_IN:
                renderFadeIn(progress);
                break;
            case FADE_OUT:
                renderFadeOut(progress);
                break;
            case CIRCLE_EXPAND:
                renderCircleExpand(progress);
                break;
            case CIRCLE_SHRINK:
                renderCircleShrink(progress);
                break;
        }
    }

    /**
     * 페이드 인 효과를 렌더링합니다.
     *
     * @param progress 진행률 (0.0 ~ 1.0)
     */
    private void renderFadeIn(float progress) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 1f - progress);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();
    }

    /**
     * 페이드 아웃 효과를 렌더링합니다.
     *
     * @param progress 진행률 (0.0 ~ 1.0)
     */
    private void renderFadeOut(float progress) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, progress);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();
    }

    /**
     * 원형 확장 효과를 렌더링합니다.
     * 중앙에서 원이 확장되면서 검은색이 사라집니다.
     *
     * @param progress 진행률 (0.0 ~ 1.0)
     */
    private void renderCircleExpand(float progress) {
        if (progress >= 1.0f) {
            return; // 완료됨, 아무것도 그리지 않음
        }

        float centerX = Constants.SCREEN_WIDTH / 2f;
        float centerY = Constants.SCREEN_HEIGHT / 2f;

        // 화면 대각선 길이 계산 (원이 화면 전체를 덮을 수 있도록)
        float maxRadius = (float) Math.sqrt(centerX * centerX + centerY * centerY);
        float currentRadius = maxRadius * progress;

        // 블렌딩 활성화
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 1f);

        // 원 외부를 검은색으로 채우기 위해 여러 개의 사각형으로 나누어 그리기
        int segments = 64; // 원을 둘러싼 세그먼트 수
        float angleStep = 360f / segments;

        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            // 삼각형 4개로 화면 외곽 채우기
            float rad1 = (float) Math.toRadians(angle1);
            float rad2 = (float) Math.toRadians(angle2);

            float x1 = centerX + currentRadius * MathUtils.cos(rad1);
            float y1 = centerY + currentRadius * MathUtils.sin(rad1);
            float x2 = centerX + currentRadius * MathUtils.cos(rad2);
            float y2 = centerY + currentRadius * MathUtils.sin(rad2);

            // 화면 경계까지 확장
            float edgeX1 = centerX + maxRadius * 2 * MathUtils.cos(rad1);
            float edgeY1 = centerY + maxRadius * 2 * MathUtils.sin(rad1);
            float edgeX2 = centerX + maxRadius * 2 * MathUtils.cos(rad2);
            float edgeY2 = centerY + maxRadius * 2 * MathUtils.sin(rad2);

            // 사각형 그리기 (원 외부 영역)
            shapeRenderer.triangle(x1, y1, x2, y2, edgeX1, edgeY1);
            shapeRenderer.triangle(x2, y2, edgeX2, edgeY2, edgeX1, edgeY1);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * 원형 축소 효과를 렌더링합니다.
     *
     * @param progress 진행률 (0.0 ~ 1.0)
     */
    private void renderCircleShrink(float progress) {
        float centerX = Constants.SCREEN_WIDTH / 2f;
        float centerY = Constants.SCREEN_HEIGHT / 2f;

        float maxRadius = (float) Math.sqrt(centerX * centerX + centerY * centerY);
        float currentRadius = maxRadius * (1f - progress);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, progress);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();
    }

    /**
     * 전환이 진행 중인지 확인합니다.
     *
     * @return 전환 진행 여부
     */
    public boolean isTransitioning() {
        return isTransitioning;
    }

    /**
     * 전환이 완료되었는지 확인합니다.
     *
     * @return 전환 완료 여부
     */
    public boolean isComplete() {
        return !isTransitioning && currentTime >= transitionTime;
    }

    /**
     * 리소스를 해제합니다.
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
