# 네트워킹 설계 - Kryonet 기반

## 연결 구조

### TCP (신뢰성 필수)
- **포트**: 5000
- **용도**: 로그인, 매칭, 스킬 사용, 아이템 획득, 게임 상태 변화
- **특징**: 느리지만 100% 전달 보장
- **패킷 크기**: 최대 1KB

### UDP (속도 우선)
- **포트**: 5001
- **용도**: 플레이어 이동, 실시간 위치 동기화
- **특징**: 빠르지만 손실 가능 (보간으로 보완)
- **패킷 크기**: 최대 1KB
- **선택적**: 사용하지 않아도 TCP로 폴백 가능

---

## 메시지 프로토콜

### 기본 구조 (Kryo 직렬화)

```
Message Header (자동 처리 by Kryonet)
├── Message Type ID
├── Timestamp
└── Payload (각 메시지 클래스)
```

---

## 메시지 정의

### 1. 로그인 단계

#### CreateRoomMsg (Client → Server, TCP)
```java
{
  roomName: String,      // "방 이름"
  maxPlayers: int        // 4
}
```

#### CreateRoomResponse (Server → Client, TCP)
```java
{
  success: boolean,      // true/false
  roomId: int,          // 생성된 방 ID
  message: String       // "성공" / "실패 이유"
}
```

#### GetRoomListMsg (Client → Server, TCP)
```java
{
  // 아무 데이터 없음 (요청만)
}
```

#### RoomListResponse (Server → Client, TCP)
```java
{
  rooms: RoomInfo[]     // [{ roomId, roomName, currentPlayers, maxPlayers, hostName, isPlaying }, ...]
}
```

#### JoinRoomMsg (Client → Server, TCP)
```java
{
  roomId: int           // 입장할 방 ID
}
```

#### JoinRoomResponse (Server → Client, TCP)
```java
{
  success: boolean,
  message: String,
  roomInfo: RoomInfo,
  players: PlayerInfo[]
}
```

---

### 2. 방 관리

#### RoomUpdateMsg (Server → All Clients, TCP)
```java
{
  players: PlayerInfo[],   // 현재 방의 모든 플레이어
  newHostId: int          // 현재 방장 ID
}
```

#### LeaveRoomMsg (Client → Server, TCP)
```java
{
  // 요청만 (아무 데이터 없음)
}
```

---

### 3. 게임 시작

#### StartGameMsg (Client → Server, TCP)
```java
{
  // 요청만 (방장이 보냄)
}
```

#### GameStartNotification (Server → All Clients, TCP)
```java
{
  startTime: long         // 게임 시작 서버 타임스탐프 (밀리초)
}
```

---

### 4. 캐릭터 선택

#### SelectCharacterMsg (향후 구현, Client → Server, TCP)
```java
{
  playerId: int,
  selectedElement: String  // "불", "물", "바람", "땅", "전기"
}
```

#### CharacterSelectedNotification (Server → All Clients, TCP)
```java
{
  playerId: int,
  selectedElement: String
}
```

---

### 5. 게임플레이

#### PlayerMoveMsg (Client → Server, UDP)
```java
{
  playerId: int,
  x: float,              // 플레이어 X 좌표
  y: float               // 플레이어 Y 좌표
}
```

#### PlayerMoveNotification (Server → Other Clients, UDP)
```java
{
  playerId: int,
  x: float,
  y: float,
  velocityX: float,      // 부드러운 보간을 위한 속도
  velocityY: float
}
```

#### CastSkillMsg (Client → Server, TCP)
```java
{
  playerId: int,
  skillId: int,          // 1-5 (스킬 번호)
  targetX: float,        // 스킬 시전 위치/방향
  targetY: float
}
```

#### CastSkillNotification (Server → All Clients, TCP)
```java
{
  playerId: int,
  skillId: int,
  targetX: float,
  targetY: float,
  timestamp: long        // 서버 타임스탐프
}
```

#### DamageMsg (Server → All Clients, TCP)
```java
{
  victimId: int,         // 피해 입은 플레이어
  damageAmount: int,
  attackerId: int        // 공격자 (몬스터는 -1)
}
```

#### ItemPickupMsg (Client → Server, TCP)
```java
{
  itemId: int,
  playerId: int
}
```

#### ItemPickupNotification (Server → All Clients, TCP)
```java
{
  itemId: int,
  playerId: int,
  itemType: String       // "health", "mana", "skill", "essence"
}
```

---

### 6. 채팅

#### ChatMsg (Client → Server, TCP)
```java
{
  sender: String,
  text: String
}
```

#### ChatNotification (Server → All Clients, TCP)
```java
{
  sender: String,
  text: String,
  timestamp: long
}
```

---

## 동기화 전략

### 게임 상태 동기화 주기

| 항목 | 주기 | 방식 | 채널 |
|------|------|------|------|
| 플레이어 위치 | 100ms | UDP (또는 TCP) | UDP/TCP |
| 게임 상태 (HP/MP/레벨) | 50ms | 변경 시에만 | TCP |
| 스킬 사용 | 즉시 | 이벤트 기반 | TCP |
| 피해/치유 | 즉시 | 이벤트 기반 | TCP |
| 아이템 획득 | 즉시 | 이벤트 기반 | TCP |

### 클라이언트 예측 (Client-Side Prediction)

```
1. 플레이어 입력 감지 (조이스틱)
2. 로컬에서 위치 즉시 업데이트 (0ms 지연)
3. 서버로 위치 전송 (비동기)
4. 서버 응답 수신 → 검증
   - 정확한 경우: 그대로 진행
   - 오차 발생: 보간으로 복귀
```

---

## 서버 권한 모델

### 클라이언트는 다음을 신뢰하지 않음

1. **위치 정보**
   - 검증: 이전 위치 ± (PLAYER_SPEED × deltaTime)
   - 부정행위 탐지: 비정상 속도 → 서버 위치로 강제 복귀

2. **스킬 쿨다운**
   - 검증: 서버에서만 관리
   - 요청: 클라이언트가 스킬 사용 시 서버 확인

3. **충돌 판정**
   - 결정: 서버 (100% 신뢰)
   - 브로드캐스트: 피해 결과만 클라이언트에 전송

4. **피해 계산**
   - 계산: 서버
   - 공식: `damage = baseSkillDamage × (1 + (공격력 - 방어력) / 100)`

### 클라이언트는 다음을 즉시 처리

1. **입력 피드백**: 버튼 누른 즉시 애니메이션 실행
2. **위치 보간**: 서버 정보 기반 부드러운 이동
3. **UI 업데이트**: HP바, 타이머 등 로컬 렌더링

---

## 에러 처리

### 연결 끊김

#### 시나리오 1: 매칭 중 연결 끊김
```
클라이언트 → 자동으로 메인 메뉴로 복귀
서버 → 플레이어 제거, 방 상태 업데이트
나머지 클라이언트 → RoomUpdateMsg로 플레이어 제거 알림
```

#### 시나리오 2: 게임 중 연결 끊김
```
클라이언트 → 자동 재연결 시도 (3회)
재연결 실패 → 게임 종료, 결과 화면으로
서버 → 플레이어를 AI로 대체 (선택적)
```

### 네트워크 지연 (Latency)

- **0-50ms**: 정상 플레이 (보상 없음)
- **50-100ms**: 약간의 보간 오차 (용인)
- **100ms+**: 게임 품질 저하 (경고 표시)

---

## 보안

### 메시지 검증

1. **타입 검증**: 메시지 클래스 확인
2. **범위 검증**: 좌표, 데미지 등 범위 체크
3. **권한 검증**: 플레이어 ID 확인

### 부정행위 방지

- 스킬 쿨다운 서버 관리
- 스탯 서버 관리
- 위치 부정 탐지
- 과도한 데미지 필터링

---

## 성능 목표

- **메시지 처리량**: 1000 메시지/초 (서버당)
- **평균 지연시간**: < 50ms
- **패킷 손실률**: < 1% (UDP)
- **대역폭**: < 5KB/s per client
