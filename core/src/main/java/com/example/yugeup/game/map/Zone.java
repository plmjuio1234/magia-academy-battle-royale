package com.example.yugeup.game.map;

/**
 * 자기장 (안전 구역) 클래스
 *
 * 배틀로얄 방식의 맵 축소 시스템을 관리합니다.
 * 시간에 따라 안전 구역이 축소되며, 밖에 있으면 지속 데미지를 받습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class Zone {

    // 자기장 중심 좌표
    private float centerX;
    private float centerY;

    // 자기장 반경
    private float radius;

    // 축소 속도
    private float shrinkSpeed;

    /**
     * 자기장을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // TODO: PHASE_24에서 구현
    }

    /**
     * 자기장을 렌더링합니다.
     */
    public void render() {
        // TODO: PHASE_24에서 구현
    }

    /**
     * 특정 좌표가 안전 구역 내에 있는지 확인합니다.
     *
     * @param x X 좌표
     * @param y Y 좌표
     * @return 안전 구역 내 여부
     */
    public boolean isInSafeZone(float x, float y) {
        // TODO: PHASE_24에서 구현
        return false;
    }

    // Getter & Setter
    public float getCenterX() { return centerX; }
    public void setCenterX(float centerX) { this.centerX = centerX; }

    public float getCenterY() { return centerY; }
    public void setCenterY(float centerY) { this.centerY = centerY; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public float getShrinkSpeed() { return shrinkSpeed; }
    public void setShrinkSpeed(float shrinkSpeed) { this.shrinkSpeed = shrinkSpeed; }
}
