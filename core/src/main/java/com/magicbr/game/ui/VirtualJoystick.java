package com.magicbr.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class VirtualJoystick extends Widget {
    private Vector2 knobPosition;
    private Vector2 centerPosition;
    private float outerRadius;
    private float innerRadius;
    private boolean touched;
    private ShapeRenderer shapeRenderer;

    // 조이스틱 값 (-1 ~ 1)
    private float knobPercentX = 0;
    private float knobPercentY = 0;

    public VirtualJoystick(float size) {
        this.outerRadius = size / 2f;
        this.innerRadius = size / 6f;
        this.knobPosition = new Vector2();
        this.centerPosition = new Vector2();
        this.shapeRenderer = new ShapeRenderer();
        this.touched = false;

        setSize(size, size);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touched = true;
                updateKnobPosition(x, y);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (touched) {
                    updateKnobPosition(x, y);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                touched = false;
                resetKnob();
            }
        });
    }

    private void updateKnobPosition(float x, float y) {
        // 위젯 중심점을 기준으로 계산
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // 터치 위치와 중심점 사이의 거리
        float deltaX = x - centerX;
        float deltaY = y - centerY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // 외부 반지름을 넘지 않도록 제한
        if (distance <= outerRadius) {
            knobPosition.set(deltaX, deltaY);
        } else {
            // 외부 반지름 경계에 맞춤
            float angle = (float) Math.atan2(deltaY, deltaX);
            knobPosition.set(
                (float) Math.cos(angle) * outerRadius,
                (float) Math.sin(angle) * outerRadius
            );
        }

        // 백분율 계산 (-1 ~ 1)
        knobPercentX = knobPosition.x / outerRadius;
        knobPercentY = knobPosition.y / outerRadius;
    }

    private void resetKnob() {
        knobPosition.set(0, 0);
        knobPercentX = 0;
        knobPercentY = 0;
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        centerPosition.set(getX() + getWidth() / 2f, getY() + getHeight() / 2f);
    }

    public void render(ShapeRenderer renderer) {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;

        // 외부 원 큰 그림자 (블러 효과)
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.2f);
        renderer.circle(centerX + 6, centerY - 6, outerRadius + 8);
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.3f);
        renderer.circle(centerX + 4, centerY - 4, outerRadius + 4);
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.4f);
        renderer.circle(centerX + 2, centerY - 2, outerRadius + 2);

        // 외부 원 배경 (다층 그라데이션)
        renderer.setColor(0.08f, 0.08f, 0.12f, 0.9f); // 가장 어두운 베이스
        renderer.circle(centerX, centerY, outerRadius);

        renderer.setColor(0.12f, 0.12f, 0.18f, 0.8f); // 중간층
        renderer.circle(centerX, centerY, outerRadius * 0.95f);

        renderer.setColor(0.18f, 0.18f, 0.25f, 0.7f); // 내부층
        renderer.circle(centerX, centerY, outerRadius * 0.85f);

        // 외부 원 내부 홈 (함몰 효과)
        renderer.setColor(0.05f, 0.05f, 0.08f, 0.9f);
        renderer.circle(centerX, centerY, outerRadius * 0.75f);

        // 내부 원 (노브) 위치
        float knobX = centerX + knobPosition.x;
        float knobY = centerY + knobPosition.y;

        // 노브 큰 그림자 (블러 효과)
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.2f);
        renderer.circle(knobX + 4, knobY - 4, innerRadius + 6);
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.35f);
        renderer.circle(knobX + 2, knobY - 2, innerRadius + 3);

        // 노브 배경 (그라데이션)
        if (touched) {
            // 터치 시 파란색 그라데이션
            renderer.setColor(0.15f, 0.45f, 0.8f, 0.95f); // 어두운 파란색 베이스
            renderer.circle(knobX, knobY, innerRadius);
            renderer.setColor(0.25f, 0.65f, 1.0f, 0.9f); // 밝은 파란색
            renderer.circle(knobX, knobY, innerRadius * 0.8f);
            renderer.setColor(0.4f, 0.8f, 1.0f, 0.7f); // 가장 밝은 중심
            renderer.circle(knobX, knobY, innerRadius * 0.5f);
        } else {
            // 기본 회색 그라데이션
            renderer.setColor(0.35f, 0.35f, 0.45f, 0.95f); // 어두운 베이스
            renderer.circle(knobX, knobY, innerRadius);
            renderer.setColor(0.55f, 0.55f, 0.65f, 0.9f); // 중간 톤
            renderer.circle(knobX, knobY, innerRadius * 0.8f);
            renderer.setColor(0.75f, 0.75f, 0.85f, 0.7f); // 밝은 중심
            renderer.circle(knobX, knobY, innerRadius * 0.5f);
        }

        // 노브 하이라이트 (광택 효과)
        if (touched) {
            renderer.setColor(0.7f, 0.95f, 1.0f, 0.8f);
        } else {
            renderer.setColor(0.9f, 0.9f, 0.95f, 0.8f);
        }
        renderer.circle(knobX - innerRadius * 0.4f, knobY + innerRadius * 0.4f, innerRadius * 0.3f);

        // 추가 광택 효과
        if (touched) {
            renderer.setColor(1.0f, 1.0f, 1.0f, 0.6f);
        } else {
            renderer.setColor(1.0f, 1.0f, 1.0f, 0.4f);
        }
        renderer.circle(knobX - innerRadius * 0.2f, knobY + innerRadius * 0.3f, innerRadius * 0.15f);

        // 테두리 그리기
        renderer.end();
        renderer.begin(ShapeRenderer.ShapeType.Line);

        // 외부 원 다중 테두리 (depth 효과)
        renderer.setColor(0.6f, 0.6f, 0.7f, 0.9f);
        renderer.circle(centerX, centerY, outerRadius);
        renderer.setColor(0.4f, 0.4f, 0.5f, 0.7f);
        renderer.circle(centerX, centerY, outerRadius * 0.75f);

        // 노브 테두리 (발광 효과)
        if (touched) {
            renderer.setColor(0.2f, 0.7f, 1.0f, 1.0f); // 파란색 발광
            renderer.circle(knobX, knobY, innerRadius + 1);
            renderer.setColor(0.1f, 0.5f, 0.8f, 1.0f);
            renderer.circle(knobX, knobY, innerRadius);
        } else {
            renderer.setColor(0.5f, 0.5f, 0.6f, 1.0f);
            renderer.circle(knobX, knobY, innerRadius);
        }

        renderer.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    public float getKnobPercentX() {
        return knobPercentX;
    }

    public float getKnobPercentY() {
        return knobPercentY;
    }

    public boolean isTouched() {
        return touched;
    }

    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}