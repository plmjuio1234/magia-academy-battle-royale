package com.example.yugeup.game.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * 임시 이펙트 클래스 (Phase 14+용)
 *
 * 스킬 이펙트를 임시로 표시하기 위해 ShapeRenderer를 사용합니다.
 * Phase 27에서 파티클 시스템으로 대체될 예정입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public abstract class TempEffect {

    // 이펙트 위치
    protected Vector2 position;

    // 이펙트 지속시간 (초)
    protected float maxDuration;

    // 남은 지속시간 (초)
    protected float remainingDuration;

    // 이펙트가 활성화되어 있는지
    protected boolean isActive;

    // 이펙트 색상
    protected Color color;

    /**
     * 임시 이펙트 생성자
     *
     * @param x 이펙트 X 좌표
     * @param y 이펙트 Y 좌표
     * @param duration 지속시간 (초)
     * @param color 이펙트 색상
     */
    public TempEffect(float x, float y, float duration, Color color) {
        this.position = new Vector2(x, y);
        this.maxDuration = duration;
        this.remainingDuration = duration;
        this.isActive = true;
        this.color = color;
    }

    /**
     * 이펙트를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (!isActive) return;

        remainingDuration -= delta;
        if (remainingDuration <= 0) {
            remainingDuration = 0;
            isActive = false;
        }
    }

    /**
     * 이펙트를 렌더링합니다.
     *
     * @param shapeRenderer ShapeRenderer (이미 begin() 상태여야 함)
     */
    public abstract void render(ShapeRenderer shapeRenderer);

    /**
     * 이펙트가 활성화되어 있는지 확인합니다.
     *
     * @return 활성화 상태
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 남은 지속시간의 비율을 반환합니다 (0.0 ~ 1.0).
     *
     * UI나 이펙트의 진행도를 계산할 때 사용합니다.
     *
     * @return 진행률
     */
    public float getProgress() {
        if (maxDuration == 0) return 0f;
        return remainingDuration / maxDuration;
    }

    /**
     * 이펙트 위치를 반환합니다.
     *
     * @return 위치
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * 투명도를 계산합니다 (시간에 따라 점차 투명해짐).
     *
     * @return 투명도 (0.0 ~ 1.0)
     */
    public float getAlpha() {
        return getProgress();  // 진행률이 높을수록 불투명
    }
}
