package com.example.yugeup.game.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 원형 임시 이펙트 클래스
 *
 * 스킬 존, 폭발 등을 표현하기 위해 원형 이펙트를 렌더링합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class CircleEffect extends TempEffect {

    // 원의 반경
    private float radius;

    // 외곽선 굵기
    private float lineWidth;

    /**
     * 원형 이펙트 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param radius 반경
     * @param duration 지속시간 (초)
     * @param color 색상
     */
    public CircleEffect(float x, float y, float radius, float duration, Color color) {
        super(x, y, duration, color);
        this.radius = radius;
        this.lineWidth = 2f;
    }

    /**
     * 원형 이펙트를 렌더링합니다.
     *
     * @param shapeRenderer ShapeRenderer (이미 begin() 상태여야 함)
     */
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (!isActive) return;

        // 시간 경과에 따라 투명도 변경
        Color renderColor = new Color(color);
        renderColor.a = getAlpha();

        shapeRenderer.setColor(renderColor);

        // 원 그리기 (채우지 않음 - 외곽선만)
        shapeRenderer.circle(position.x, position.y, radius, 32);
    }

    /**
     * 반경을 설정합니다.
     *
     * @param radius 반경
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * 반경을 반환합니다.
     *
     * @return 반경
     */
    public float getRadius() {
        return radius;
    }
}
