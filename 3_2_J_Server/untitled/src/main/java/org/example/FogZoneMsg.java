package org.example;

/**
 * Fog 구역 활성화 메시지
 *
 * 서버에서 클라이언트로 fog 레이어 활성화 정보를 전달합니다.
 */
public class FogZoneMsg {
    // 활성화할 구역 이름 (TMX 레이어명)
    public String zoneName;

    // 활성화 여부
    public boolean active;

    // 게임 경과 시간 (초)
    public float gameTime;

    public FogZoneMsg() {}

    public FogZoneMsg(String zoneName, boolean active, float gameTime) {
        this.zoneName = zoneName;
        this.active = active;
        this.gameTime = gameTime;
    }
}
