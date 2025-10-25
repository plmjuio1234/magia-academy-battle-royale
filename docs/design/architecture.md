# 시스템 아키텍처

## 전체 구조도

```
┌─────────────────────────────────────────────────────────────┐
│                      Android / Desktop                       │
│                  (libGDX Client Application)                 │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  Screen System (Scene2D UI)                             ││
│  │  ┌──────────┬──────────┬──────────┬──────────────────┐ ││
│  │  │SplashScr │MenuScr   │LobbyScr  │CharSelectScreen │ ││
│  │  │   een    │  een     │  een     │                  │ ││
│  │  └──────────┴──────────┴──────────┴──────────────────┘ ││
│  │  ┌─────────────────────────────────────────────────────┐││
│  │  │         GameScreen (Main Gameplay)                 │││
│  │  │  ┌────────────┐  ┌──────────────┐  ┌────────────┐ │││
│  │  │  │Player      │  │GameCamera    │  │HUD Layer   │ │││
│  │  │  │Movement    │  │(Orthographic)│  │(HP/MP/UI)  │ │││
│  │  │  └────────────┘  └──────────────┘  └────────────┘ │││
│  │  └─────────────────────────────────────────────────────┘││
│  │  ┌──────────┐                                            ││
│  │  │ResultScr │                                            ││
│  │  │ een      │                                            ││
│  │  └──────────┘                                            ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │         Client Manager (Kryonet)                        ││
│  │  TCP:5000 (연결, 스킬, 아이템)                          ││
│  │  UDP:5001 (플레이어 위치)                               ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │         Entity Management System                        ││
│  │  ┌──────────┬──────────┬──────────┐                     ││
│  │  │Player    │Monsters  │Projectiles                   │││
│  │  │entities  │entities  │entities  │                    ││
│  │  └──────────┴──────────┴──────────┘                     ││
│  │  ┌─────────────────────────────────┐                    ││
│  │  │  Physics/Collision Detection    │                    ││
│  │  └─────────────────────────────────┘                    ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                            ↕ (Kryonet)
┌─────────────────────────────────────────────────────────────┐
│                    Kryonet Server (Java)                    │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐│
│  │         Room Management                                 ││
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐        ││
│  │  │GameRoom    │  │PlayerData  │  │RoomUpdate  │        ││
│  │  │Management  │  │Tracking    │  │Broadcast   │        ││
│  │  └────────────┘  └────────────┘  └────────────┘        ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │         Game State Management                           ││
│  │  ┌────────────────────────────────────────────────────┐ ││
│  │  │ Server-side game logic                             │ ││
│  │  │ - Collision detection                              │ ││
│  │  │ - Damage calculation                               │ ││
│  │  │ - Game phase transitions                           │ ││
│  │  └────────────────────────────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

---

## 클라이언트 레이어 구조

### 1. Presentation Layer (화면)
- **BaseScreen**: 모든 화면의 기본 클래스
- **MenuScreen**: 메인 메뉴
- **RoomListScreen**: 방 목록 + 방 생성
- **LobbyScreen**: 매칭 대기실
- **CharacterSelectScreen**: 캐릭터/원소 선택
- **GameScreen**: 실제 게임 플레이
- **ResultScreen**: 게임 결과

### 2. Game Logic Layer
- **Player**: 로컬 플레이어 엔티티
  - 위치, 속도, HP/MP, 레벨
  - 이동, 스킬 사용, 피해 수신

- **OtherPlayer**: 다른 플레이어 (네트워크 동기화)
  - 네트워크에서 받은 위치 정보
  - 렌더링용 위치 보간

- **Monster**: 몬스터 엔티티 (향후)
  - AI 로직
  - 공격 패턴
  - 드롭 아이템

- **Projectile**: 투사체 (스킬, 마법)
  - 위치, 속도, 방향
  - 충돌 감지

### 3. Rendering Layer
- **SpriteBatch**: 2D 렌더링
- **ShapeRenderer**: 디버깅용 기하 도형 렌더링
- **OrthographicCamera**: 카메라 관리
- **ParticleEffect**: 스킬 이펙트 (향후)

### 4. Networking Layer
- **Client** (Kryonet 기반)
  - TCP: 매칭, 스킬, 아이템
  - UDP: 플레이어 위치
  - 수신 리스너 관리

### 5. UI Layer (Scene2D)
- **VirtualJoystick**: 이동 입력
- **SkillButtons**: 스킬 버튼 (4개)
- **HUD**: HP/MP 바, 타이머, 레벨 표시
- **Inventory**: 인벤토리 패널

### 6. Utility Layer
- **Assets**: 리소스 로딩 관리
- **FontManager**: 한글 폰트 관리
- **Constants**: 게임 상수 (해상도, 속도 등)
- **ScreenTransition**: 화면 전환 효과

---

## 서버 레이어 구조

### 1. Connection Management
- **Server** (Kryonet 기반)
  - TCP 포트: 5000
  - UDP 포트: 5001
  - 최대 연결: 4명 (방당)

### 2. Room Management
- **GameRoom**: 게임 방 정보
  - roomId, roomName, maxPlayers
  - players List
  - host, isPlaying 플래그

### 3. Player Management
- **PlayerData**: 플레이어 데이터
  - id, name, connection
  - currentRoom 참조
  - 게임 상태 (위치, HP/MP)

### 4. Game Logic
- 위치 검증 (이동 속도 체크)
- 스킬 쿨다운 관리
- 충돌 판정 계산
- 피해 계산

---

## 데이터 흐름

### 클라이언트 → 서버

```
Player Input (이동, 스킬)
    ↓
Client.sendTCP/UDP()
    ↓
Server 수신
    ↓
Game Logic 검증
    ↓
상태 업데이트
    ↓
Broadcast to All Players
```

### 서버 → 클라이언트

```
Game Event (위치 동기화, 피해, 아이템)
    ↓
Server.broadcast()
    ↓
Network Listener
    ↓
Client 상태 업데이트
    ↓
화면 렌더링
```

---

## 기술 스택

| 계층 | 기술 | 버전 |
|------|------|------|
| 게임 엔진 | libGDX | 1.12+ |
| 네트워킹 | Kryonet | 2.22.0-RC1 |
| 직렬화 | Kryo | 5.5.0 |
| 빌드 도구 | Maven/Gradle | - |
| 타겟 플랫폼 | Android / Desktop | API 21+ |

---

## 성능 최적화 전략

### 렌더링 최적화
- SpriteBatch 배치 드로우
- TextureAtlas 활용
- Viewport 스케일링

### 네트워크 최적화
- 패킷 압축 (Kryo 자동)
- 대역폭 제한: 5KB/s per client
- 메시지 병합

### 메모리 최적화
- Object Pooling (투사체, 이펙트, 몬스터)
- 동적 스크립트 언로드
- 텍스처 최적화 (크기 축소)
