package com.example.yugeup.network.messages;

/**
 * 방 생성 응답 메시지
 *
 * 서버가 방 생성 결과를 클라이언트에 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class CreateRoomResponse {
    public boolean success;
    public int roomId;
    public String message;

    public CreateRoomResponse() {}
}
