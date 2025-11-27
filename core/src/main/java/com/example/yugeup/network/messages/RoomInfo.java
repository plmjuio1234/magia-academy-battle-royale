package com.example.yugeup.network.messages;

/**
 * 방 정보 클래스
 *
 * 방의 상세 정보를 담습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RoomInfo {
    // 방 고유 ID
    public int roomId;

    // 방 제목
    public String roomName;

    // 현재 플레이어 수
    public int currentPlayers;

    // 최대 플레이어 수
    public int maxPlayers;

    // 방장 이름
    public String hostName;

    // 게임 진행 중 여부
    public boolean isPlaying;

    /**
     * 기본 생성자 (KryoNet 직렬화용)
     */
    public RoomInfo() {}

    /**
     * 모든 필드를 초기화하는 생성자
     *
     * @param roomId 방 ID
     * @param roomName 방 제목
     * @param currentPlayers 현재 인원
     * @param maxPlayers 최대 인원
     * @param hostName 방장 이름
     * @param isPlaying 게임 진행 중 여부
     */
    public RoomInfo(int roomId, String roomName, int currentPlayers, int maxPlayers, String hostName, boolean isPlaying) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.hostName = hostName;
        this.isPlaying = isPlaying;
    }
}
