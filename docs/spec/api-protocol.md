# API 프로토콜 상세 명세

**참고**: 이 문서는 @docs/design/networking.md 의 메시지 정의를 기술적으로 확장한 것입니다.

---

## 클래스 정의 및 직렬화

### 기본 클래스들

#### PlayerInfo
```java
public class PlayerInfo {
    public int playerId;           // 플레이어 고유 ID
    public String playerName;      // 플레이어 이름
    public boolean isHost;         // 방장 여부
}
```

**직렬화 순서**: playerId → playerName → isHost

#### RoomInfo
```java
public class RoomInfo {
    public int roomId;             // 방 고유 ID
    public String roomName;        // 방 이름
    public int currentPlayers;     // 현재 인원
    public int maxPlayers;         // 최대 인원
    public String hostName;        // 방장 이름
    public boolean isPlaying;      // 게임 진행 중 여부
}
```

---

## 메시지 타입별 상세 정의

### 메시지 등록 순서 (Kryo)

클라이언트와 서버 모두 같은 순서로 등록해야 함:

```java
kryo.register(Messages.class);                          // 0
kryo.register(CreateRoomMsg.class);                     // 1
kryo.register(CreateRoomResponse.class);                // 2
kryo.register(GetRoomListMsg.class);                    // 3
kryo.register(RoomInfo.class);                          // 4
kryo.register(RoomInfo[].class);                        // 5
kryo.register(RoomListResponse.class);                  // 6
kryo.register(JoinRoomMsg.class);                       // 7
kryo.register(JoinRoomResponse.class);                  // 8
kryo.register(PlayerInfo.class);                        // 9
kryo.register(PlayerInfo[].class);                      // 10
kryo.register(LeaveRoomMsg.class);                      // 11
kryo.register(RoomUpdateMsg.class);                     // 12
kryo.register(StartGameMsg.class);                      // 13
kryo.register(GameStartNotification.class);             // 14
kryo.register(ChatMsg.class);                           // 15
kryo.register(PlayerMoveMsg.class);                     // 16
```

---

## 로그인/방 생성 플로우

### 1. 방 생성

#### CreateRoomMsg (C→S, TCP)
```
필드:
  String roomName: 1-20자
  int maxPlayers: 2-4 범위

유효성 검사 (서버):
  - roomName이 null 또는 공백 → 거부
  - maxPlayers < 2 또는 > 4 → 거부

서버 처리:
  1. 새 GameRoom 객체 생성
  2. roomId = nextRoomId++ (1부터 시작)
  3. rooms.put(roomId, newRoom)
  4. CreateRoomResponse 반환
```

#### CreateRoomResponse (S→C, TCP)
```
필드:
  boolean success: 성공 여부
  int roomId: 생성된 방 ID (실패 시 -1)
  String message: "방이 생성되었습니다" / 에러메시지

응답 예시:
  성공: { success: true, roomId: 1, message: "방이 생성되었습니다" }
  실패: { success: false, roomId: -1, message: "올바르지 않은 방 이름" }
```

---

### 2. 방 목록 조회

#### GetRoomListMsg (C→S, TCP)
```
필드: (없음, 요청만)

서버 처리:
  1. rooms.values() 순회
  2. 각 GameRoom을 RoomInfo로 변환
  3. RoomListResponse 배열 생성
```

#### RoomListResponse (S→C, TCP)
```
필드:
  RoomInfo[] rooms: 활성 방 목록

응답 예시:
  {
    rooms: [
      { roomId: 1, roomName: "초급", currentPlayers: 2, maxPlayers: 4, hostName: "Player1", isPlaying: false },
      { roomId: 2, roomName: "고급", currentPlayers: 4, maxPlayers: 4, hostName: "Player2", isPlaying: true }
    ]
  }
```

---

### 3. 방 입장

#### JoinRoomMsg (C→S, TCP)
```
필드:
  int roomId: 입장할 방 ID

유효성 검사 (서버):
  - roomId가 존재하지 않음 → 거부
  - 방이 가득 찼음 (currentPlayers >= maxPlayers) → 거부
  - 게임 진행 중 (isPlaying == true) → 거부

서버 처리:
  1. PlayerData 객체 생성
  2. playerId = 연결의 ID 사용
  3. playerName = 클라이언트에서 전송 (향후)
  4. GameRoom.addPlayer() 호출
  5. JoinRoomResponse 반환
  6. 모든 플레이어에게 RoomUpdateMsg 브로드캐스트
```

#### JoinRoomResponse (S→C, TCP)
```
필드:
  boolean success: 성공 여부
  String message: 설명 메시지
  RoomInfo roomInfo: 방 정보
  PlayerInfo[] players: 현재 방의 모든 플레이어

응답 예시:
  {
    success: true,
    message: "방에 입장했습니다",
    roomInfo: { roomId: 1, roomName: "초급", ... },
    players: [
      { playerId: 1, playerName: "Player1", isHost: true },
      { playerId: 2, playerName: "Player2", isHost: false }
    ]
  }
```

---

## 방 관리

### RoomUpdateMsg (S→All, TCP)
```
필드:
  PlayerInfo[] players: 현재 모든 플레이어
  int newHostId: 현재 방장 ID

발송 시기:
  - 플레이어 입장
  - 플레이어 퇴장
  - 방장 변경

브로드캐스트 대상:
  - 방 내의 모든 클라이언트
  - 방의 호스트 재설정 가능
```

### LeaveRoomMsg (C→S, TCP)
```
필드: (없음, 요청만)

서버 처리:
  1. 현재 연결의 PlayerData 찾기
  2. GameRoom.removePlayer() 호출
  3. 방에 플레이어 없으면 방 삭제
  4. 방장이 퇴장하면 새 방장 지정 (첫 번째 플레이어)
  5. RoomUpdateMsg 브로드캐스트
```

---

## 게임 시작

### StartGameMsg (C→S, TCP)
```
필드: (없음, 방장만 보냄)

서버 처리:
  1. 방장 여부 검증
  2. 모든 플레이어 상태 확인
  3. GameRoom.isPlaying = true
  4. GameStartNotification 생성
  5. 모든 플레이어에게 전송
  6. 게임 루프 시작
```

### GameStartNotification (S→All, TCP)
```
필드:
  long startTime: 서버 타임스탐프 (System.currentTimeMillis())

클라이언트 처리:
  1. GameScreen으로 전환
  2. startTime 저장
  3. 경과 시간 = currentTime - startTime
  4. 게임 로직 시작
```

---

## 게임플레이

### PlayerMoveMsg (C→S, UDP 또는 TCP)
```
필드:
  int playerId: 플레이어 ID
  float x: 현재 X 좌표 (0 ~ 1920)
  float y: 현재 Y 좌표 (0 ~ 1920)

전송 주기: 100ms마다

서버 검증:
  1. playerId 확인
  2. 좌표 범위 확인 (0-1920)
  3. 이전 위치로부터의 거리 확인
     - 허용 거리 = PLAYER_SPEED × (100ms / 1000)
     - 초과 시 클라이언트 위치 강제 복귀
  4. 충돌 검사 (맵 경계)
  5. PlayerMoveNotification 브로드캐스트
```

### PlayerMoveNotification (S→Others, UDP)
```
필드:
  int playerId: 플레이어 ID
  float x: X 좌표
  float y: Y 좌표
  float velocityX: X 속도 (보간용)
  float velocityY: Y 속도 (보간용)

클라이언트 처리:
  1. OtherPlayer 객체 업데이트
  2. 현재 위치 → (x, y)로 부드러운 이동
  3. 보간 시간: 다음 메시지까지 (100ms)
```

---

## 스킬 시스템

### CastSkillMsg (C→S, TCP)
```
필드:
  int playerId: 플레이어 ID
  int skillId: 1-5 (스킬 번호)
  float targetX: 목표 X 좌표
  float targetY: 목표 Y 좌표

서버 검증:
  1. 스킬 ID 범위 확인 (1-5)
  2. 쿨다운 확인
  3. MP 확인 (스킬별로 다름)
  4. 위치 유효성 확인

서버 처리:
  1. MP 차감
  2. 쿨다운 설정 (스킬별 쿨타임)
  3. 충돌 계산
  4. CastSkillNotification 브로드캐스트
```

### CastSkillNotification (S→All, TCP)
```
필드:
  int playerId: 시전자 ID
  int skillId: 스킬 번호
  float targetX: 목표 좌표
  float targetY: 목표 좌표
  long timestamp: 서버 타임스탐프

클라이언트 처리:
  1. 스킬 애니메이션 실행
  2. 투사체 생성
  3. 충돌 감지 후 DamageMsg 수신 대기
```

### DamageMsg (S→All, TCP)
```
필드:
  int victimId: 피해 플레이어 ID
  int damageAmount: 피해량
  int attackerId: 공격자 ID (-1이면 환경)

서버 계산:
  damage = baseSkillDamage × (1 + (공격력 - 방어력) / 100)

클라이언트 처리:
  1. 플레이어 HP 감소
  2. 데미지 텍스트 표시
  3. HP = 0이면 플레이어 제거
```

---

## 데이터 검증 규칙

### 문자열 필드
- **비어있음 검사**: null 또는 길이 0 → 거부
- **길이 제한**: 방 이름 1-20자, 플레이어 이름 1-15자
- **문자 제한**: 영문, 한글, 숫자만 허용

### 숫자 필드
- **범위 검사**: 좌표 (0-1920), maxPlayers (2-4)
- **음수 검사**: ID, 시간값은 >= 0

### 부동수 필드
- **범위 검사**: 좌표, 속도 범위
- **NaN/Infinity 검사**: 유효한 수인지 확인

---

## 에러 코드

| 코드 | 의미 | 처리 |
|------|------|------|
| 0 | 성공 | 진행 |
| 1001 | 방이 없음 | 메뉴로 복귀 |
| 1002 | 방이 가득 찬 상태 | 다른 방 선택 |
| 1003 | 게임 진행 중 | 다른 방 선택 |
| 2001 | 연결 끊김 | 재연결 시도 |
| 2002 | 타임아웃 | 재연결 시도 |
| 3001 | 부정 행위 탐지 | 킹 및 강제 종료 |

