package com.example.yugeup.network.messages;

/**
 * 방 업데이트 알림 메시지
 *
 * 서버가 방의 플레이어 목록 변경을 알릴 때 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RoomUpdateMsg {
    public PlayerInfo[] players;
    public int newHostId;

    public RoomUpdateMsg() {}
}
