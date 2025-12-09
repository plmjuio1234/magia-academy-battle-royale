# SDD (Software Design Document) - 유급은 싫어

---

## 📑 목차

1. [개요](#개요)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [주요 모듈 설계](#주요-모듈-설계)
4. [데이터 흐름](#데이터-흐름)
5. [클래스 다이어그램](#클래스-다이어그램)
6. [설계 패턴](#설계-패턴)
7. [상태 관리](#상태-관리)
8. [에러 처리](#에러-처리)

---

## 개요

### 프로젝트 정보
- **게임명**: 유급은 싫어 (배틀로얄 + 로그라이크 하이브리드)
- **플랫폼**: Android (libGDX)
- **네트워크**: KryoNET 기반 멀티플레이
- **게임 기간**: 10분
- **플레이어**: 2~8명

### 설계 목표
1. **확장성**: 스킬/몬스터 추가 용이
2. **성능**: 60fps 유지 (특히 모바일)
3. **동기화**: 네트워크 지연에 강건한 설계
4. **유지보수**: 명확한 책임 분리 (SRP)
5. **테스트성**: TDD 기반 개발 용이

---

## 시스템 아키텍처

### 계층 구조 (Layered Architecture)

```
┌─────────────────────────────────────────┐
│  Presentation Layer (UI 렌더링)          │
│  ├─ LoadingScreen                       │
│  ├─ MainMenuScreen                      │
│  ├─ LobbyScreen / WaitingRoomScreen     │
│  └─ GameScreen (HUD 컴포넌트)           │
├─────────────────────────────────────────┤
│  Game Logic Layer (게임 로직)            │
│  ├─ PlayerController (입력 처리)        │
│  ├─ GameManager (게임 진행)             │
│  ├─ CombatSystem (전투)                 │
│  ├─ LevelSystem (레벨업)                │
│  └─ MapManager (맵/자기장)              │
├─────────────────────────────────────────┤
│  Entity Layer (엔티티)                   │
│  ├─ Player                              │
│  ├─ Monster                             │
│  ├─ Skill / Projectile                  │
│  └─ Zone                                │
├─────────────────────────────────────────┤
│  Network Layer (네트워킹)                │
│  ├─ NetworkManager                      │
│  ├─ MessageHandler                      │
│  ├─ RoomManager                         │
│  └─ KryoNET 메시지들                    │
├─────────────────────────────────────────┤
│  Data Layer (저장소)                     │
│  ├─ Constants                           │
│  ├─ GameState (현재 게임 상태)          │
│  └─ AssetManager                        │
└─────────────────────────────────────────┘
```

### 모듈 간 통신

```
UserInput
    ↓
[PlayerController]
    ↓
[GameManager] ←→ [CombatSystem]
    ↓              ↓
[NetworkManager]  [LevelSystem]
    ↓              ↓
[Server]     [MonsterManager]
```

---

## 주요 모듈 설계

### 1. Screen Management (화면 관리)

```
IScreen (인터페이스)
  ├─ show()      : 화면 표시
  ├─ hide()      : 화면 숨김
  ├─ update()    : 매 프레임 업데이트
  └─ render()    : 렌더링

↓ 구현

LoadingScreen
  ├─ 게임 리소스 로드
  ├─ 진행도 표시
  └─ 로드 완료 시 메인메뉴로 이동

MainMenuScreen
  ├─ 메인 메뉴 UI
  └─ 시작/설정/종료 버튼

GameScreen
  ├─ 게임 월드 렌더링
  ├─ HUD 컴포넌트
  ├─ 입력 처리
  └─ 게임 오버 처리
```

### 2. Player System (플레이어 시스템)

```
Player (엔티티)
  ├─ position: Vector2          (위치)
  ├─ playerStats: PlayerStats   (능력치)
  ├─ skills: SkillManager       (스킬)
  ├─ inventory: Inventory       (인벤토리)
  └─ state: PlayerState         (상태)

PlayerStats (능력치)
  ├─ health: int                (체력)
  ├─ mana: int                  (마나)
  ├─ attack: int                (공격력)
  ├─ defense: int               (방어력)
  ├─ speed: float               (이동속도)
  ├─ level: int                 (레벨)
  └─ experience: int            (경험치)

PlayerController (입력 처리)
  ├─ handleJoystickInput()      (조이스틱)
  ├─ handleSkillInput()         (스킬 버튼)
  └─ updatePosition()           (위치 동기화)
```

### 3. Monster System (몬스터 시스템)

```
Monster (기본 클래스)
  ├─ position: Vector2          (위치)
  ├─ stats: MonsterStats        (스탯)
  ├─ aiState: AIState           (AI 상태)
  ├─ target: Player             (타겟)
  └─ update(delta)              (AI 업데이트)

MonsterStats
  ├─ health: int
  ├─ attack: int
  ├─ speed: float
  └─ aggroRange: float

MonsterSpawner
  ├─ spawnGhost()
  ├─ spawnSlime()
  ├─ spawnGolem()
  └─ getMonsterByType()

MonsterManager
  ├─ monsters: List<Monster>
  ├─ update(delta, players)
  ├─ addMonster(monster)
  ├─ removeMonster(monsterId)
  └─ getMonsterById(id)
```

### 4. Skill System (스킬 시스템)

```
Skill (기본 클래스)
  ├─ skillId: int
  ├─ name: String              (스킬명)
  ├─ damage: int               (데미지)
  ├─ manaCost: int             (마나 소비)
  ├─ cooldown: float           (쿨타임)
  ├─ cast()                    (시전)
  └─ update(delta)             (쿨타임 감소)

MagicMissile extends Skill
  ├─ autoTarget: boolean
  ├─ projectileSpeed: float
  ├─ findNearestMonster()
  └─ fireProjectile()

ElementalSkill extends Skill
  ├─ element: ElementType
  ├─ level: int
  ├─ upgrades[3]             (3가지 강화 옵션)
  └─ applyUpgrade()

SkillManager
  ├─ activeSkills: Map<SlotId, Skill>
  ├─ castSkill(slotId)
  ├─ upgradeSkill(slotId)
  └─ updateCooldowns(delta)
```

### 5. Combat System (전투 시스템)

```
CombatSystem
  ├─ dealDamage(attacker, target, damage)
  ├─ calculateDamage(base, stats)
  ├─ applyEffect(target, effect)
  ├─ onMonsterDeath(monster, killer)
  └─ onPlayerDeath(player, killer)

DamageCalculator
  ├─ baseDamage: int
  ├─ attacker: Entity
  ├─ defender: Entity
  ├─ calculate(): int
  │   ├─ 공격자 공격력 적용
  │   ├─ 방어자 방어력 감소
  │   ├─ 타입별 보정 (불, 물, 바람 등)
  │   └─ 최종 데미지 반환
  └─ getModifier(attacker, defender)

CollisionDetector
  ├─ checkMonsterCollision(player, monsters)
  ├─ checkProjectileCollision(projectile, targets)
  └─ checkPlayerCollision(players)
```

### 6. Level System (레벨 시스템)

```
LevelSystem
  ├─ currentLevel: int
  ├─ currentExp: int
  ├─ maxExp: int
  ├─ gainExperience(amount)
  ├─ levelUp()
  ├─ getStatsBonus(level)
  └─ isMaxLevel(): boolean

ExperienceTable
  ├─ expRequirement[15]        (각 레벨 필요 경험치)
  ├─ getExpForLevel(level)
  └─ getTotalExp(level)
```

### 7. Map System (맵 시스템)

```
GameMap
  ├─ width: int = 1920
  ├─ height: int = 1920
  ├─ zones: List<Zone>
  ├─ currentZone: Zone
  ├─ getZoneAtPosition(x, y)
  └─ isInMapBounds(x, y)

Zone
  ├─ zoneId: int
  ├─ bounds: Rectangle
  ├─ status: ZoneStatus (OPEN/CLOSING/CLOSED)
  ├─ closureTimer: float
  ├─ damagePerSecond: int
  └─ isPlayerInZone(player): boolean

ZoneManager
  ├─ zones: List<Zone>
  ├─ currentTime: float
  ├─ gameEndTime: float = 600f  (10분)
  ├─ update(delta)              (자기장 진행)
  ├─ closeZone(zoneId)
  └─ getDamage(player)
```

### 8. Network System (네트워크 시스템)

```
NetworkManager
  ├─ server: KryoNetServer
  ├─ client: KryoNetClient
  ├─ gameState: GameState
  ├─ sendMessage(msg)
  ├─ onMessageReceived(msg)
  └─ disconnect()

MessageHandler
  ├─ handlers: Map<MsgType, Handler>
  ├─ handle(msg)
  ├─ registerHandler(type, handler)
  └─ onPlayerMove(msg)
     ├─ onMonsterSpawn(msg)
     ├─ onMonsterUpdate(msg)
     ├─ onSkillCast(msg)
     └─ onGameEnd(msg)

RoomManager
  ├─ rooms: Map<RoomId, Room>
  ├─ currentRoom: Room
  ├─ createRoom(name, maxPlayers)
  ├─ joinRoom(roomId)
  ├─ leaveRoom()
  └─ startGame()

Room
  ├─ roomId: int
  ├─ players: List<Player>
  ├─ maxPlayers: int
  ├─ isPlaying: boolean
  ├─ hostId: int
  ├─ addPlayer(player)
  ├─ removePlayer(playerId)
  └─ broadcast(message)
```

---

## 데이터 흐름

### 게임 시작 ~ 플레이어 공격까지

```
1. 플레이어 터치
   ↓
2. [PlayerController] 입력 감지
   ├─ 스킬 버튼 누름
   └─ SkillManager에 시전 요청
   ↓
3. [SkillManager] 스킬 실행
   ├─ 마나 확인
   ├─ 쿨타임 확인
   ├─ 스킬 시전
   └─ [CombatSystem]에 공격 요청
   ↓
4. [CombatSystem] 데미지 계산
   ├─ DamageCalculator 사용
   ├─ 몬스터에 데미지 적용
   ├─ 경험치 계산
   └─ [LevelSystem]에 경험치 추가
   ↓
5. [LevelSystem] 경험치 처리
   ├─ 경험치 합산
   ├─ 레벨업 확인
   └─ 능력치 증가
   ↓
6. [NetworkManager] 서버 동기화
   ├─ PlayerAttackMsg 전송
   └─ MonsterDamageMsg 수신
   ↓
7. [UI/HUD] 렌더링 업데이트
   ├─ 체력 바 변경
   ├─ 경험치 바 변경
   ├─ 레벨 표시
   └─ 킬 로그 표시
```

### 몬스터 스폰 ~ 플레이어 피해까지

```
1. [MonsterSpawner] 몬스터 생성
   ├─ 시간대별 종류 결정
   ├─ 몬스터 객체 생성
   └─ MonsterManager에 추가
   ↓
2. [MonsterManager] 게임에 추가
   ├─ monsters 리스트에 추가
   └─ [NetworkManager]에 스폰 메시지 전송
   ↓
3. [NetworkManager] 브로드캐스트
   ├─ MonsterSpawnMsg 전송
   └─ 모든 클라이언트에 몬스터 추가
   ↓
4. 매 프레임 [MonsterManager.update()]
   ├─ 각 몬스터 AI 실행
   ├─ 위치/상태 변경
   ├─ CollisionDetector에 충돌 확인
   └─ [NetworkManager]에 상태 동기화
   ↓
5. [CollisionDetector] 충돌 감지
   ├─ 몬스터 ↔ 플레이어 충돌
   ├─ CombatSystem에 피해 신청
   └─ 플레이어 HP 감소
   ↓
6. [UI/HUD] 플레이어 HP 바 감소
```

---

## 클래스 다이어그램

### 엔티티 상속 관계

```
Entity (추상 클래스)
├─ position: Vector2
├─ sprite: Sprite
├─ health: int
├─ update(delta): void
└─ render(batch): void
    │
    ├─ Player extends Entity
    │   ├─ stats: PlayerStats
    │   ├─ skills: SkillManager
    │   ├─ inventory: Inventory
    │   └─ takeDamage(damage): void
    │
    ├─ Monster extends Entity
    │   ├─ monsterType: MonsterType
    │   ├─ ai: AIController
    │   ├─ target: Player
    │   └─ attack(target): void
    │
    └─ Projectile extends Entity
        ├─ owner: Entity
        ├─ damage: int
        ├─ velocity: Vector2
        └─ lifetime: float
```

### 스킬 상속 관계

```
Skill (추상 클래스)
├─ skillId: int
├─ name: String
├─ manaCost: int
├─ damage: int
├─ cooldown: float
├─ cast(caster): void
└─ update(delta): void
    │
    ├─ MagicMissile extends Skill
    │   ├─ autoTarget: boolean
    │   └─ findNearestMonster(): Monster
    │
    └─ ElementalSkill extends Skill
        ├─ element: ElementType
        ├─ level: int
        ├─ currentCooldown: float
        └─ upgrade(upgradeType): void
            │
            ├─ SkillFire extends ElementalSkill
            │   ├─ skillA: Fireball
            │   ├─ skillB: FlameWave
            │   └─ skillC: Inferno
            │
            ├─ SkillWater extends ElementalSkill
            ├─ SkillWind extends ElementalSkill
            ├─ SkillEarth extends ElementalSkill
            └─ SkillLightning extends ElementalSkill
```

---

## 설계 패턴

### 1. Singleton Pattern (단일 패턴)

관리 클래스들은 게임당 하나만 필요:

```java
public class GameManager {
    private static GameManager instance;

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
}

// 사용
GameManager.getInstance().startGame();
```

**적용 대상**:
- GameManager
- NetworkManager
- SkillManager
- LevelSystem
- MapManager

### 2. Observer Pattern (옵저버 패턴)

이벤트 발생 시 여러 리스너에 알림:

```java
public interface GameEventListener {
    void onMonsterDeath(Monster monster);
    void onPlayerLevelUp(Player player);
    void onGameEnd(GameResult result);
}

public class GameManager {
    private List<GameEventListener> listeners = new ArrayList<>();

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    private void notifyMonsterDeath(Monster monster) {
        for (GameEventListener listener : listeners) {
            listener.onMonsterDeath(monster);
        }
    }
}
```

**적용 대상**:
- 게임 이벤트 (monster death, level up, game end)
- 네트워크 메시지 수신
- UI 업데이트

### 3. Strategy Pattern (전략 패턴)

몬스터 AI의 다양한 행동 전략:

```java
public interface AIStrategy {
    void execute(Monster monster, List<Player> players, float delta);
}

public class ChasingStrategy implements AIStrategy {
    @Override
    public void execute(Monster monster, List<Player> players, float delta) {
        // 추적 로직
    }
}

public class Monster {
    private AIStrategy strategy;

    public void setAIStrategy(AIStrategy strategy) {
        this.strategy = strategy;
    }

    public void updateAI(float delta) {
        strategy.execute(this, players, delta);
    }
}
```

### 4. Factory Pattern (팩토리 패턴)

몬스터 생성:

```java
public class MonsterFactory {
    public static Monster createMonster(MonsterType type) {
        switch (type) {
            case GHOST:
                return new Ghost();
            case SLIME:
                return new Slime();
            case GOLEM:
                return new Golem();
            default:
                throw new IllegalArgumentException();
        }
    }
}
```

### 5. Object Pool Pattern (객체 풀 패턴)

빈번히 생성/삭제되는 발사체:

```java
public class ProjectilePool {
    private List<Projectile> available = new ArrayList<>();
    private List<Projectile> inUse = new ArrayList<>();

    public Projectile obtain() {
        if (available.isEmpty()) {
            return new Projectile();
        }
        Projectile p = available.remove(0);
        inUse.add(p);
        return p;
    }

    public void release(Projectile p) {
        inUse.remove(p);
        p.reset();
        available.add(p);
    }
}
```

---

## 상태 관리

### GameState (게임 상태 머신)

```
┌──────────────┐
│   LOADING    │ (초기 상태)
└──────┬───────┘
       │ 리소스 로드 완료
       ↓
┌──────────────┐
│ MAIN_MENU    │
└──────┬───────┘
       │ 게임 시작 클릭
       ↓
┌──────────────┐
│   LOBBY      │
└──────┬───────┘
       │ 방 참가/방 생성
       ↓
┌──────────────┐
│ WAITING_ROOM │
└──────┬───────┘
       │ 게임 시작
       ↓
┌──────────────┐
│    PLAYING   │
└──────┬───────┘
       │ 10분 경과 또는 플레이어 1명
       ↓
┌──────────────┐
│   FINISHED   │ → MAIN_MENU로 돌아감
└──────────────┘
```

### PlayerState (플레이어 상태)

```
IDLE         (기본 상태)
   ├─ 입력 감지 → MOVING
   └─ 스킬 시전 → CASTING

MOVING       (이동 중)
   ├─ 입력 해제 → IDLE
   ├─ 스킬 시전 → CASTING
   └─ 피해 → DAMAGED

CASTING      (스킬 시전 중)
   └─ 시전 완료 → IDLE (또는 MOVING)

DAMAGED      (피해 중)
   └─ 피해 애니메이션 끝 → IDLE

DEAD         (사망)
   └─ (상태 변경 없음)
```

### AIState (몬스터 AI 상태)

```
IDLE         (대기)
   ├─ 플레이어 감지 (aggroRange) → PURSUING
   └─ 타이머 만료 → PATROLLING

PURSUING     (추적)
   ├─ 공격 범위 진입 → ATTACKING
   ├─ 플레이어 시야 벗어남 → IDLE
   └─ HP 0 → DEAD

ATTACKING    (공격)
   ├─ 공격 끝남 → PURSUING
   ├─ 플레이어 거리 멀어짐 → PURSUING
   └─ HP 0 → DEAD

PATROLLING   (순찰)
   ├─ 플레이어 감지 → PURSUING
   └─ 경로 끝 → IDLE

DEAD         (사망)
   └─ (상태 변경 없음)
```

---

## 에러 처리

### 예외 처리 전략

```java
public class GameException extends Exception {
    public GameException(String message) {
        super(message);
    }
}

public class NetworkException extends GameException {
    public NetworkException(String message) {
        super("네트워크 에러: " + message);
    }
}

public class GameLogicException extends GameException {
    public GameLogicException(String message) {
        super("게임 로직 에러: " + message);
    }
}
```

### 에러 로깅

```java
public class Logger {
    public static void error(String msg, Exception e) {
        System.err.println("[ERROR] " + msg);
        e.printStackTrace();
    }

    public static void warn(String msg) {
        System.out.println("[WARN] " + msg);
    }

    public static void info(String msg) {
        System.out.println("[INFO] " + msg);
    }
}
```

### 네트워크 에러 처리

```
네트워크 연결 실패
  ├─ 재연결 시도 (최대 3회)
  ├─ 실패 → 에러 다이얼로그 표시
  └─ 게임 로비로 돌아감

게임 중 연결 끊김
  ├─ 게임 일시 정지
  ├─ 재연결 시도
  ├─ 5초 이내 재연결 성공 → 게임 계속
  └─ 실패 → 게임 끝내고 결과 화면 표시
```

---

## 성능 최적화

### 렌더링 최적화

```
- Batch 렌더링: 텍스처 바꿈 최소화
- Object Culling: 화면 밖 객체 렌더링 X
- Texture Atlas: 여러 이미지를 하나로
- Level of Detail: 원거리 객체 간소 렌더링
```

### 메모리 최적화

```
- Object Pool: 발사체, 이펙트 재사용
- Sprite Cache: 자주 사용하는 스프라이트 메모리 유지
- Lazy Loading: 필요할 때만 리소스 로드
- Garbage Collection 최소화
```

### 네트워크 최적화

```
- 메시지 집약: 여러 정보를 한 메시지로
- 동기화 주기 조정:
  - 플레이어 위치: 매번 전송
  - 몬스터 위치: 100ms마다
  - 능력치: 변경될 때만
```

---

**마지막 업데이트**: 2025-11-18
**버전**: 1.0
