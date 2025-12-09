package com.example.yugeup.network;

import org.example.Main.*;
import com.badlogic.gdx.Game;
import com.example.yugeup.screens.WaitingRoomScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * 방 관리 시스템
 *
 * 방 생성, 참가, 나가기 기능을 담당합니다.
 * 싱글톤 패턴으로 구현되어 게임 전역에서 접근 가능합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RoomManager {
    private static RoomManager instance;

    // 현재 참가 중인 방 ID (-1이면 방에 참가하지 않음)
    private int currentRoomId = -1;

    // 현재 방 정보
    private RoomInfo currentRoom;

    // 현재 방의 플레이어 목록
    private List<PlayerInfo> playersInRoom;

    // 방 목록 캐시
    private List<RoomInfo> roomList;

    // 게임 인스턴스 (화면 전환용)
    private Game game;

    /**
     * Private 생성자 - 싱글톤 패턴
     */
    private RoomManager() {
        this.playersInRoom = new ArrayList<>();
        this.roomList = new ArrayList<>();
    }

    /**
     * RoomManager 인스턴스를 반환합니다.
     *
     * @return RoomManager 싱글톤 인스턴스
     */
    public static synchronized RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    /**
     * Game 인스턴스를 설정합니다.
     * 화면 전환을 위해 필요합니다.
     *
     * @param game Game 인스턴스
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * 방 목록을 조회합니다.
     * NetworkManager를 통해 GetRoomListMsg를 전송합니다.
     */
    public void fetchRoomList() {
        NetworkManager.getInstance().sendTCP(new GetRoomListMsg());
    }

    /**
     * 방 목록 응답을 처리합니다.
     * MessageHandler로부터 호출됩니다.
     *
     * @param response 서버로부터 받은 방 목록 응답
     */
    public void onRoomListReceived(RoomListResponse response) {
        roomList.clear();
        if (response.rooms != null) {
            for (RoomInfo info : response.rooms) {
                roomList.add(info);
            }
        }
    }

    /**
     * 방을 생성합니다.
     *
     * @param roomName 방 제목 (최대 20자)
     * @param maxPlayers 최대 인원 (2~8명)
     */
    public void createRoom(String roomName, int maxPlayers) {
        CreateRoomMsg msg = new CreateRoomMsg();
        msg.roomName = roomName;
        msg.maxPlayers = maxPlayers;
        NetworkManager.getInstance().sendTCP(msg);
    }

    /**
     * 방 생성 응답을 처리합니다.
     * 성공 시 바로 WaitingRoomScreen으로 전환합니다.
     *
     * @param response 서버로부터 받은 방 생성 응답
     */
    public void onCreateRoomResponse(CreateRoomResponse response) {
        if (response.success) {
            // 방 생성 성공 - 서버에서 이미 방에 추가되었음
            currentRoomId = response.roomInfo.roomId;
            currentRoom = response.roomInfo;
            playersInRoom.clear();

            if (response.players != null) {
                for (PlayerInfo player : response.players) {
                    playersInRoom.add(player);
                }
            }

            System.out.println("[RoomManager] 방 생성 성공: " + currentRoom.roomName);
            System.out.println("[RoomManager] 플레이어 수: " + playersInRoom.size() + "/" + currentRoom.maxPlayers);

            // WaitingRoomScreen으로 전환
            if (game != null) {
                game.setScreen(new WaitingRoomScreen(game, currentRoom, playersInRoom));
            }
        } else {
            System.err.println("[RoomManager] 방 생성 실패: " + response.message);
        }
    }

    /**
     * 방에 참가합니다.
     *
     * @param roomId 참가할 방 ID
     */
    public void joinRoom(int roomId) {
        JoinRoomMsg msg = new JoinRoomMsg();
        msg.roomId = roomId;
        NetworkManager.getInstance().sendTCP(msg);
    }

    /**
     * 방 참가 응답을 처리합니다.
     * 성공 시 WaitingRoomScreen으로 전환합니다.
     *
     * @param response 서버로부터 받은 방 참가 응답
     */
    public void onJoinRoomResponse(JoinRoomResponse response) {
        if (response.success) {
            currentRoomId = response.roomInfo.roomId;
            currentRoom = response.roomInfo;
            playersInRoom.clear();

            if (response.players != null) {
                for (PlayerInfo player : response.players) {
                    playersInRoom.add(player);
                }
            }

            System.out.println("[RoomManager] 방 참가 성공: " + currentRoom.roomName);
            System.out.println("[RoomManager] 플레이어 수: " + playersInRoom.size() + "/" + currentRoom.maxPlayers);

            // WaitingRoomScreen으로 전환
            if (game != null) {
                game.setScreen(new WaitingRoomScreen(game, currentRoom, playersInRoom));
            }
        } else {
            System.err.println("[RoomManager] 방 참가 실패: " + response.message);
        }
    }

    /**
     * 현재 방을 나갑니다.
     */
    public void leaveRoom() {
        if (currentRoomId != -1) {
            NetworkManager.getInstance().sendTCP(new LeaveRoomMsg());
            currentRoomId = -1;
            currentRoom = null;
            playersInRoom.clear();
            System.out.println("[RoomManager] 방 나가기");
        }
    }

    /**
     * 방 업데이트 메시지를 처리합니다.
     * 플레이어 목록 변경 시 호출됩니다.
     *
     * @param msg 서버로부터 받은 방 업데이트 메시지
     */
    public void onRoomUpdate(RoomUpdateMsg msg) {
        playersInRoom.clear();

        if (msg.players != null) {
            for (PlayerInfo player : msg.players) {
                playersInRoom.add(player);
            }
        }

        System.out.println("[RoomManager] 방 업데이트: 플레이어 수 = " + playersInRoom.size());

        // 호스트 변경 처리
        if (msg.newHostId != -1) {
            System.out.println("[RoomManager] 새 호스트 ID: " + msg.newHostId);
        }
    }

    /**
     * 현재 방 목록을 반환합니다.
     *
     * @return 방 목록 리스트
     */
    public List<RoomInfo> getRoomList() {
        return roomList;
    }

    /**
     * 현재 참가 중인 방 ID를 반환합니다.
     *
     * @return 방 ID (-1이면 참가하지 않음)
     */
    public int getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * 현재 방 정보를 반환합니다.
     *
     * @return 현재 방 정보
     */
    public RoomInfo getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 현재 방의 플레이어 목록을 반환합니다.
     *
     * @return 플레이어 목록
     */
    public List<PlayerInfo> getPlayersInRoom() {
        return playersInRoom;
    }

    /**
     * 게임 시작을 요청합니다.
     * (PHASE_06에서 구현 예정)
     */
    public void startGame() {
        // TODO: PHASE_06에서 구현
    }
}
