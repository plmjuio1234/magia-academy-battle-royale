package com.example.yugeup.game.skill;

/**
 * 스킬 이펙트 클래스
 *
 * 스킬 시각 효과를 관리합니다.
 * 파티클, 애니메이션 등을 표현합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillEffect {

    // 이펙트 위치
    private float x;
    private float y;

    // 이펙트 지속 시간
    private float duration;
    private float elapsed;

    // 활성화 상태
    private boolean active;

    /**
     * 이펙트를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // TODO: PHASE_27에서 구현
    }

    /**
     * 이펙트를 렌더링합니다.
     */
    public void render() {
        // TODO: PHASE_27에서 구현
    }

    // Getter & Setter
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getDuration() { return duration; }
    public void setDuration(float duration) { this.duration = duration; }

    public float getElapsed() { return elapsed; }
    public void setElapsed(float elapsed) { this.elapsed = elapsed; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
