package com.example.yugeup.game.map;

/**
 * Fog 구역 클래스
 *
 * TMX 맵의 fog 그룹 내 각 레이어를 나타냅니다.
 * 활성화되면 해당 구역에 안개가 표시되고 플레이어에게 데미지를 줍니다.
 *
 * @author YuGeup Development Team
 * @version 2.0
 */
public class Zone {

    // 구역 이름 (TMX 레이어명과 동일)
    private String name;

    // 활성화 여부 (fog가 켜졌는지)
    private boolean active;

    // 활성화된 시간 (초)
    private float activeTime;

    /**
     * Zone 생성자
     *
     * @param name 구역 이름 (TMX 레이어명)
     */
    public Zone(String name) {
        this.name = name;
        this.active = false;
        this.activeTime = 0f;
    }

    /**
     * 구역을 활성화합니다. (fog ON)
     */
    public void activate() {
        this.active = true;
        this.activeTime = 0f;
        System.out.println("[Zone] fog 활성화: " + name);
    }

    /**
     * 구역을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (active) {
            activeTime += delta;
        }
    }

    // ===== Getter & Setter =====

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public float getActiveTime() {
        return activeTime;
    }
}
