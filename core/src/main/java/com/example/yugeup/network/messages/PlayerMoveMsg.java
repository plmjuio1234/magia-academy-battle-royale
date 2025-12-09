package com.example.yugeup.network.messages;

/**
 * 플레이어 이동 메시지
 *
 * 플레이어의 위치 정보를 동기화합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class PlayerMoveMsg {
    public int playerId;
    public float x;
    public float y;

    public PlayerMoveMsg() {}
}
