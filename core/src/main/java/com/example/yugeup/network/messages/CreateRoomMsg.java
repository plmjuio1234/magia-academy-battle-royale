package com.example.yugeup.network.messages;

/**
 * 방 생성 요청 메시지
 *
 * 클라이언트가 서버에 방 생성을 요청할 때 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class CreateRoomMsg {
    public String roomName;
    public int maxPlayers;

    public CreateRoomMsg() {}
}
