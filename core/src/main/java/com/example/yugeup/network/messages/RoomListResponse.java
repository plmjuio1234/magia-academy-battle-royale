package com.example.yugeup.network.messages;

/**
 * 방 목록 응답 메시지
 *
 * 서버가 방 목록을 클라이언트에 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RoomListResponse {
    public RoomInfo[] rooms;

    public RoomListResponse() {}
}
