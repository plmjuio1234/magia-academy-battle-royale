package com.example.yugeup.network.messages;

/**
 * 발사체 발사 메시지
 *
 * 플레이어가 발사체를 발사할 때 모든 클라이언트에게 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ProjectileFiredMsg {
    public int playerId;           // 발사한 플레이어 ID
    public float startX;           // 시작 위치 X
    public float startY;           // 시작 위치 Y
    public int targetMonsterId;    // 타겟 몬스터 ID
    public String skillType;       // 스킬 타입 ("MagicMissile", "Fireball" 등)

    public ProjectileFiredMsg() {}
}
