# PHASE_05_ROOM_SYSTEM.md - 방 관리 시스템 (생성, 참가, 목록)

---

## 🎯 목표
클라이언트의 방 관리 로직 구현 (생성, 참가, 목록 조회, 나가기)

---

## 📋 구현 범위

- ✅ GetRoomListMsg 전송 → RoomListResponse 수신
- ✅ 방 목록 파싱 및 캐싱
- ✅ JoinRoomMsg 전송 → JoinRoomResponse 수신
- ✅ LeaveRoomMsg 전송
- ✅ RoomUpdateMsg 수신 (플레이어 변경 감지)

---

## 📁 필요 파일

```
network/
  ├─ RoomManager.java (새로 생성)
  └─ messages/
      ├─ JoinRoomMsg.java
      ├─ JoinRoomResponse.java
      ├─ LeaveRoomMsg.java
      ├─ RoomUpdateMsg.java
      └─ ...

screens/
  └─ WaitingRoomScreen.java (기초 구현)
```

---

## 🔧 구현 가이드

### RoomManager 클래스

```java
public class RoomManager {
    private int currentRoomId = -1;
    private List<RoomInfo> roomList = new ArrayList<>();
    private List<Integer> playersInRoom = new ArrayList<>();

    // 방 목록 조회
    public void fetchRoomList() {
        NetworkManager.getInstance().sendMessage(new GetRoomListMsg());
    }

    // 방 목록 업데이트 (서버 응답)
    public void onRoomListReceived(RoomListResponse response) {
        roomList.clear();
        for (RoomInfo info : response.rooms) {
            roomList.add(info);
        }
    }

    // 방 참가
    public void joinRoom(int roomId) {
        NetworkManager.getInstance().sendMessage(
            new JoinRoomMsg(roomId));
    }

    // 방 참가 응답 처리
    public void onJoinRoomResponse(JoinRoomResponse response) {
        if (response.success) {
            currentRoomId = response.roomInfo.roomId;
            playersInRoom.clear();
            for (PlayerInfo p : response.players) {
                playersInRoom.add(p.playerId);
            }
            // WaitingRoomScreen으로 전환
        } else {
            Logger.warn("방 참가 실패: " + response.message);
        }
    }

    // 방 나가기
    public void leaveRoom() {
        NetworkManager.getInstance().sendMessage(new LeaveRoomMsg());
        currentRoomId = -1;
        playersInRoom.clear();
    }

    // 플레이어 변경 감지
    public void onRoomUpdated(RoomUpdateMsg msg) {
        playersInRoom.clear();
        for (PlayerInfo p : msg.players) {
            playersInRoom.add(p.playerId);
        }
    }

    public List<RoomInfo> getRoomList() {
        return roomList;
    }

    public int getCurrentRoomId() {
        return currentRoomId;
    }
}
```

### MessageHandler 메서드 추가

```java
public void onGetRoomListResponse(RoomListResponse msg) {
    RoomManager.getInstance().onRoomListReceived(msg);
}

public void onJoinRoomResponse(JoinRoomResponse msg) {
    RoomManager.getInstance().onJoinRoomResponse(msg);
}

public void onRoomUpdated(RoomUpdateMsg msg) {
    RoomManager.getInstance().onRoomUpdated(msg);
}
```

---

## 🧪 테스트 계획

```
[ ] GetRoomListMsg 전송 후 응답 수신
    @Test
    public void 방목록을_조회한다() {
        RoomManager mgr = RoomManager.getInstance();
        mgr.fetchRoomList();
        // 서버 응답 대기 후
        assertTrue(mgr.getRoomList().size() > 0);
    }

[ ] 방 참가 성공
    @Test
    public void 방에_참가한다() {
        mgr.joinRoom(1);
        // 응답 처리 후
        assertEquals(1, mgr.getCurrentRoomId());
    }

[ ] 플레이어 목록 업데이트
    @Test
    public void 방의_플레이어_목록이_업데이트된다() {
        // RoomUpdateMsg 수신 후
        assertTrue(mgr.getPlayersInRoom().size() > 0);
    }
```

---

## ✅ 완료 조건

- [ ] RoomManager 구현 완료
- [ ] 메서드별 테스트 통과
- [ ] 방 목록 조회/참가 동작 확인
- [ ] WaitingRoomScreen 기초 구현 (다음 Phase)

---

## 🔗 다음 Phase

**PHASE_06: 대기실 UI & 채팅**

