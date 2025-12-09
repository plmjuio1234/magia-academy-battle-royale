# PHASE_03_NETWORK_CORE.md - 네트워크 기초 연결

---

## 🎯 목표
KryoNET 기반 서버 연결 및 기본 메시지 송수신 구현

---

## 📋 구현 범위

- ✅ NetworkManager 싱글톤 구현
- ✅ 서버 연결 (localhost:5000)
- ✅ 기본 메시지 클래스 정의
- ✅ 메시지 등록 (Kryo)
- ✅ 연결 상태 처리

---

## 📁 필요 파일

```
network/
  ├─ NetworkManager.java (새로 생성)
  ├─ MessageHandler.java (새로 생성)
  └─ messages/
      ├─ BaseMessage.java
      ├─ GetRoomListMsg.java
      ├─ RoomListResponse.java
      └─ ... (기타)

utils/
  └─ Constants.java (수정 - 네트워크 설정 추가)
```

---

## 🔧 구현 가이드

### NetworkManager 싱글톤

```java
public class NetworkManager {
    private static NetworkManager instance;
    private KryoNetClient client;
    private boolean isConnected = false;
    private int playerId = -1;

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void connect(String host, int port) {
        client = new KryoNetClient();
        client.getKryo().register(GetRoomListMsg.class);
        client.getKryo().register(RoomListResponse.class);
        // ... 기타 메시지 등록

        client.addListener(new Listener() {
            public void received(Connection conn, Object obj) {
                MessageHandler.getInstance().handle(obj);
            }
        });

        try {
            client.connect(5000, host, Constants.NETWORK_TCP_PORT,
                Constants.NETWORK_UDP_PORT);
            isConnected = true;
        } catch (Exception e) {
            Logger.error("연결 실패", e);
            isConnected = false;
        }
    }

    public void sendMessage(Object msg) {
        if (isConnected && client != null) {
            client.sendTCP(msg);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
```

### 메시지 클래스

```java
public class GetRoomListMsg {}

public class RoomListResponse {
    public RoomInfo[] rooms;
}

public class RoomInfo {
    public int roomId;
    public String roomName;
    public int currentPlayers;
    public int maxPlayers;
}
```

---

## 🧪 테스트 계획

```
[ ] 서버 연결 성공 시 isConnected = true
[ ] 메시지 송신 성공
[ ] 메시지 수신 성공
[ ] 연결 실패 시 예외 처리
```

---

## ✅ 완료 조건

- [ ] NetworkManager 구현 및 싱글톤 확인
- [ ] KryoNET 메시지 등록
- [ ] 서버 연결/해제 동작
- [ ] 기본 메시지 송수신 테스트 통과

---

**주의**: 서버가 실행 중이어야 함 (3_2_J_Server)

