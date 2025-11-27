# SPEC_NETWORK.md - 네트워크 프로토콜

> 자세한 메시지 정의는 총정리.md의 "🌐 네트워크 & 서버 아키텍처" 섹션 참조

---

## 📡 연결 설정

**서버**: 5000번 포트 (TCP) / 5001번 포트 (UDP, 미사용)
**클라이언트**: KryoNET Client로 연결
**직렬화**: KryoNET (자동 직렬화)

---

## 🔀 메시지 흐름

### 로비

```
GetRoomListMsg (클라이언트) → 서버
  ↓
RoomListResponse (서버) → 클라이언트

JoinRoomMsg (클라이언트) → 서버
  ↓
JoinRoomResponse (서버) → 클라이언트
RoomUpdateMsg (브로드캐스트) → 방 내 모든 클라이언트
```

### 게임 시작

```
StartGameMsg (호스트만) → 서버
  ↓
GameStartNotification (서버) → 모든 플레이어
  └─ startTime: long (ms)
```

### 게임 진행

**주기적 (매 프레임)**:
- PlayerMoveMsg (클라이언트 위치) → 서버 → 브로드캐스트

**이벤트 기반**:
- SkillCastMsg (스킬 시전) → 서버 → 브로드캐스트
- MonsterUpdateMsg (100ms마다) → 서버 → 브로드캐스트
- MonsterDeathMsg → 서버 → 브로드캐스트
- ChatMsg → 서버 → 브로드캐스트

---

## 📦 메시지 클래스들

### 로비 메시지

```java
// 방 목록 요청 (클라이언트)
public class GetRoomListMsg {}

// 방 목록 응답 (서버)
public class RoomListResponse {
    public RoomInfo[] rooms;  // roomId, name, players/max, host
}

// 방 참가 요청 (클라이언트)
public class JoinRoomMsg {
    public int roomId;
}

// 방 참가 응답 (서버)
public class JoinRoomResponse {
    public boolean success;
    public String message;
    public RoomInfo roomInfo;
    public PlayerInfo[] players;
}

// 방 플레이어 변경 (서버 브로드캐스트)
public class RoomUpdateMsg {
    public PlayerInfo[] players;
    public int newHostId;
}
```

### 게임 메시지

```java
// 게임 시작 (클라이언트 → 서버)
public class StartGameMsg {}

// 게임 시작 알림 (서버 → 클라이언트)
public class GameStartNotification {
    public long startTime;  // 시작 시간 (ms)
}

// 플레이어 이동 (클라이언트 → 서버 → 브로드캐스트)
public class PlayerMoveMsg {
    public int playerId;
    public float x, y;
}

// 스킬 시전 (클라이언트 → 서버 → 브로드캐스트)
public class SkillCastMsg {
    public int playerId;
    public int skillId;
    public float targetX, targetY;
    public String skillName;
    public int baseDamage;
}

// 몬스터 스폰 (서버 → 클라이언트)
public class MonsterSpawnMsg {
    public int monsterId;
    public float x, y;
    public String monsterType;  // "Ghost", "Slime", "Golem"
    public String elementType;  // Slime only
}

// 몬스터 상태 업데이트 (서버, 100ms마다)
public class MonsterUpdateMsg {
    public int monsterId;
    public float x, y, vx, vy;
    public int hp, maxHp;
    public String state;  // "IDLE", "PURSUING", "ATTACKING", "DEAD"
}

// 몬스터 사망 (서버 → 클라이언트)
public class MonsterDeathMsg {
    public int monsterId;
    public float dropX, dropY;
}

// 플레이어 공격 (클라이언트 → 서버)
public class PlayerAttackMonsterMsg {
    public int playerId;
    public int monsterId;
    public float attackerX, attackerY;  // 검증용
    public float skillDamage;
}

// 몬스터 피해 (서버 → 클라이언트)
public class MonsterDamageMsg {
    public int monsterId;
    public int newHp;
    public int damageAmount;
    public int attackerId;
}

// 채팅 (클라이언트 → 서버 → 브로드캐스트)
public class ChatMsg {
    public String sender;
    public String text;
}

// 게임 종료 (서버 → 클라이언트)
public class GameEndMsg {
    public int[] rankings;      // playerId 순서대로
    public int[] killCounts;    // 몬스터 처치 수
    public int[] playerKills;   // 플레이어 처치 수
}
```

---

## 🔄 동기화 주기

| 항목 | 주기 | 방향 |
|------|------|------|
| PlayerMove | 매 프레임 | 클라 → 서버 → 브로드 |
| MonsterSpawn | 1초마다 | 서버 → 클라이언트 |
| MonsterUpdate | 100ms마다 | 서버 → 클라이언트 |
| SkillCast | 즉시 | 클라 → 서버 → 브로드 |

---

## ⚠️ 에러 처리

```
연결 실패
  ├─ 재연결 시도 (최대 3회)
  └─ 실패 → 에러 다이얼로그

게임 중 연결 끊김
  ├─ 5초 내 재연결 시도
  ├─ 성공 → 게임 계속
  └─ 실패 → 게임 종료

메시지 수신 오류
  └─ 로그 기록 & 무시
```

---

**더 자세한 정보**: 총정리.md의 메시지 프로토콜 섹션 참조

