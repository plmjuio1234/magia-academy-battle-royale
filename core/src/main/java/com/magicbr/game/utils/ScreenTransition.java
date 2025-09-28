package com.magicbr.game.utils;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class ScreenTransition {
    public enum TransitionType {
        CLOSING,    // 밖에서 안으로 닫히는 효과
        OPENING     // 안에서 밖으로 열리는 효과
    }

    private TransitionType type;
    public float duration;
    public float timer;
    private boolean isActive;
    private boolean isComplete;

    public ScreenTransition() {
        this.duration = 0.8f; // 0.8초 동안 애니메이션
        reset();
    }

    public void startClosing() {
        this.type = TransitionType.CLOSING;
        this.timer = 0f;
        this.isActive = true;
        this.isComplete = false;
    }

    public void startOpening() {
        this.type = TransitionType.OPENING;
        this.timer = 0f;
        this.isActive = true;
        this.isComplete = false;
    }

    public void update(float delta) {
        if (!isActive) return;

        timer += delta;
        if (timer >= duration) {
            timer = duration;
            isComplete = true;
            isActive = false;
        }
    }

    public void render(ShapeRenderer renderer, float screenWidth, float screenHeight) {
        if (!isActive && timer == 0f) return;

        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;
        float maxRadius = Math.max(screenWidth, screenHeight) * 0.8f;

        float progress = timer / duration;
        // 부드러운 easing 효과 (더 자연스러운 곡선)
        progress = MathUtils.sin(progress * MathUtils.PI / 2f);

        renderer.begin(ShapeRenderer.ShapeType.Filled);

        if (type == TransitionType.CLOSING) {
            // 밖에서 안으로 닫히는 효과 - 화면 전체를 검게 하고 중앙에서 점점 작아지는 원형 구멍
            renderer.setColor(0f, 0f, 0f, 1f);
            renderer.rect(0, 0, screenWidth, screenHeight);

            // 현재 진행에 따라 구멍이 점점 작아짐 (1.0에서 0.0으로)
            float currentRadius = maxRadius * (1f - progress);
            if (currentRadius > 0) {
                // 구멍 효과를 위해 스텐실 버퍼 대신 원을 여러 개 겹쳐 그리기
                renderer.setColor(0.2f, 0.2f, 0.3f, 1f); // 배경색과 동일하게
                renderer.circle(centerX, centerY, currentRadius);
            }

        } else if (type == TransitionType.OPENING) {
            // 안에서 밖으로 열리는 효과 - 검은 화면에서 중앙부터 점점 커지는 원형으로 밝아짐
            renderer.setColor(0f, 0f, 0f, 1f);
            renderer.rect(0, 0, screenWidth, screenHeight);

            // 현재 진행에 따라 밝은 원이 점점 커짐
            float currentRadius = maxRadius * progress;
            if (currentRadius > 0) {
                renderer.setColor(0.2f, 0.2f, 0.3f, 1f); // 배경색과 동일하게
                renderer.circle(centerX, centerY, currentRadius);
            }
        }

        renderer.end();
    }


    public boolean isClosingComplete() {
        return type == TransitionType.CLOSING && isComplete;
    }

    public boolean isOpeningComplete() {
        return type == TransitionType.OPENING && isComplete;
    }

    public boolean isActive() {
        return isActive;
    }

    public void reset() {
        this.timer = 0f;
        this.isActive = false;
        this.isComplete = false;
        this.type = null;
    }
}