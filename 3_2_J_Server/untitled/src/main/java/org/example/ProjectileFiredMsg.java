package org.example;

/**
 * 발사체 발사 메시지
 */
public class ProjectileFiredMsg {
    public int playerId;           // 발사한 플레이어 ID
    public float startX;           // 시작 위치 X
    public float startY;           // 시작 위치 Y
    public int targetMonsterId;    // 타겟 몬스터 ID (-1이면 PVP 공격)
    public int targetPlayerId;     // 타겟 플레이어 ID (PVP용, -1이면 몬스터 공격)
    public String skillType;       // 스킬 타입

    public ProjectileFiredMsg() {
        this.targetMonsterId = -1;
        this.targetPlayerId = -1;
    }
}
