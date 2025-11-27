package com.example.yugeup.game.map;

/**
 * 자기장 관리 클래스
 *
 * 자기장의 생성, 축소, 데미지 처리를 관리합니다.
 * 서버로부터 자기장 정보를 받아 동기화합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ZoneManager {

    // 현재 자기장
    private Zone currentZone;

    // 자기장 시작 시간
    private float zoneStartTime;

    /**
     * 자기장을 초기화합니다.
     */
    public void initialize() {
        // TODO: PHASE_24에서 구현
    }

    /**
     * 자기장을 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        // TODO: PHASE_24에서 구현
    }

    /**
     * 자기장 외부에 있는 플레이어에게 데미지를 적용합니다.
     */
    public void applyZoneDamage() {
        // TODO: PHASE_24에서 구현
    }

    // Getter & Setter
    public Zone getCurrentZone() { return currentZone; }
    public void setCurrentZone(Zone currentZone) { this.currentZone = currentZone; }

    public float getZoneStartTime() { return zoneStartTime; }
    public void setZoneStartTime(float zoneStartTime) { this.zoneStartTime = zoneStartTime; }
}
