package org.example;

/**
 * Fog 데미지 메시지
 *
 * 서버에서 클라이언트로 fog 구역 내 플레이어 데미지 정보를 전달합니다.
 */
public class FogDamageMsg {
    // 데미지 받은 플레이어 ID
    public int playerId;

    // 데미지 양
    public int damage;

    // 데미지 후 남은 체력
    public int newHp;

    // 데미지를 준 구역 이름
    public String zoneName;

    public FogDamageMsg() {}

    public FogDamageMsg(int playerId, int damage, int newHp, String zoneName) {
        this.playerId = playerId;
        this.damage = damage;
        this.newHp = newHp;
        this.zoneName = zoneName;
    }
}
