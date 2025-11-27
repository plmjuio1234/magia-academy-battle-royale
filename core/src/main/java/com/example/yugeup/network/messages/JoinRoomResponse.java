package com.example.yugeup.network.messages;

/**
 * 방 참가 응답 메시지
 *
 * 서버가 방 참가 결과를 클라이언트에 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class JoinRoomResponse {
    public boolean success;
    public String message;
    public RoomInfo roomInfo;
    public PlayerInfo[] players;

    public JoinRoomResponse() {}
}
