# PHASE_21_MONSTER_SYNC.md - 몬스터 서버 동기화

---

## 🎯 목표
몬스터의 서버 동기화 시스템 구현 (스폰, 위치, 상태, 사망)

---

## 📋 구현 범위

- ✅ MonsterSpawnMsg (몬스터 생성)
- ✅ MonsterUpdateMsg (위치/상태 업데이트)
- ✅ MonsterDeathMsg (몬스터 사망)
- ✅ 클라이언트-서버 동기화

---

## 📁 필요 파일

```
network/messages/
  ├─ MonsterSpawnMsg.java
  ├─ MonsterUpdateMsg.java
  └─ MonsterDeathMsg.java

game/monster/
  └─ MonsterSyncManager.java

network/
  └─ MessageHandler.java (수정)
```

---

## 🔧 구현 가이드

### 1. MonsterSpawnMsg

```java
/**
 * 몬스터 스폰 메시지
 */
public class MonsterSpawnMsg {
    public int monsterId;       // 몬스터 고유 ID
    public int monsterType;     // 0=고스트, 1=슬라임, 2=골렘
    public float x;             // 스폰 위치 X
    public float y;             // 스폰 위치 Y
    public int health;          // 초기 체력
}
```

### 2. MonsterUpdateMsg

```java
/**
 * 몬스터 업데이트 메시지
 */
public class MonsterUpdateMsg {
    public int monsterId;
    public float x;
    public float y;
    public int state;           // 0=IDLE, 1=MOVING, 2=ATTACKING, etc.
    public int health;          // 현재 체력
}
```

### 3. MonsterDeathMsg

```java
/**
 * 몬스터 사망 메시지
 */
public class MonsterDeathMsg {
    public int monsterId;
    public int killerId;        // 처치한 플레이어 ID
    public int expReward;       // 경험치 보상
}
```

### 4. MonsterSyncManager

```java
/**
 * 몬스터 동기화 관리자
 */
public class MonsterSyncManager {
    private static MonsterSyncManager instance;
    private NetworkManager networkManager;

    // 동기화 타이머
    private float syncTimer = 0f;
    private static final float SYNC_INTERVAL = 0.05f;  // 20Hz (50ms)

    public static MonsterSyncManager getInstance() {
        if (instance == null) {
            instance = new MonsterSyncManager();
        }
        return instance;
    }

    private MonsterSyncManager() {
        this.networkManager = NetworkManager.getInstance();
    }

    /**
     * 몬스터 스폰 전송 (서버 → 클라이언트)
     */
    public void sendMonsterSpawn(Monster monster) {
        MonsterSpawnMsg msg = new MonsterSpawnMsg();
        msg.monsterId = monster.getId();
        msg.monsterType = monster.getType().ordinal();
        msg.x = monster.getPosition().x;
        msg.y = monster.getPosition().y;
        msg.health = monster.getHealth();

        networkManager.broadcastToAll(msg);
    }

    /**
     * 몬스터 업데이트 전송 (주기적)
     */
    public void update(float delta) {
        syncTimer += delta;

        if (syncTimer >= SYNC_INTERVAL) {
            sendMonsterUpdates();
            syncTimer = 0f;
        }
    }

    /**
     * 모든 몬스터 상태 전송
     */
    private void sendMonsterUpdates() {
        List<Monster> monsters = GameManager.getInstance().getMonsters();

        for (Monster monster : monsters) {
            MonsterUpdateMsg msg = new MonsterUpdateMsg();
            msg.monsterId = monster.getId();
            msg.x = monster.getPosition().x;
            msg.y = monster.getPosition().y;
            msg.state = monster.getState().ordinal();
            msg.health = monster.getHealth();

            networkManager.broadcastToAll(msg);
        }
    }

    /**
     * 몬스터 사망 전송
     */
    public void sendMonsterDeath(Monster monster, Player killer) {
        MonsterDeathMsg msg = new MonsterDeathMsg();
        msg.monsterId = monster.getId();
        msg.killerId = (killer != null) ? killer.getId() : -1;
        msg.expReward = monster.getExpReward();

        networkManager.broadcastToAll(msg);
    }

    /**
     * 몬스터 스폰 수신 처리
     */
    public void onMonsterSpawnReceived(MonsterSpawnMsg msg) {
        // 몬스터 타입 확인
        MonsterType type = MonsterType.values()[msg.monsterType];

        // 몬스터 생성
        Monster monster = MonsterFactory.createMonster(type);
        monster.setId(msg.monsterId);
        monster.setPosition(msg.x, msg.y);
        monster.setHealth(msg.health);

        // 게임에 추가
        GameManager.getInstance().addMonster(monster);
    }

    /**
     * 몬스터 업데이트 수신 처리
     */
    public void onMonsterUpdateReceived(MonsterUpdateMsg msg) {
        Monster monster = GameManager.getInstance().getMonsterById(msg.monsterId);

        if (monster == null) {
            return;  // 존재하지 않는 몬스터
        }

        // 위치 업데이트
        monster.setPosition(msg.x, msg.y);

        // 상태 업데이트
        MonsterState state = MonsterState.values()[msg.state];
        monster.setState(state);

        // 체력 업데이트
        monster.setHealth(msg.health);
    }

    /**
     * 몬스터 사망 수신 처리
     */
    public void onMonsterDeathReceived(MonsterDeathMsg msg) {
        Monster monster = GameManager.getInstance().getMonsterById(msg.monsterId);

        if (monster == null) {
            return;
        }

        // 몬스터 제거
        GameManager.getInstance().removeMonster(monster);

        // 경험치 보상 (처치한 플레이어에게만)
        Player localPlayer = GameManager.getInstance().getLocalPlayer();
        if (localPlayer != null && localPlayer.getId() == msg.killerId) {
            localPlayer.gainExperience(msg.expReward);
        }

        // 사망 이펙트
        createDeathEffect(monster.getPosition());
    }

    /**
     * 사망 이펙트 생성
     */
    private void createDeathEffect(Vector2 position) {
        // 임시: 간단한 파티클 이펙트
        // ParticleEffect deathEffect = new ParticleEffect();
        // deathEffect.setPosition(position);
        // GameManager.getInstance().addEffect(deathEffect);
    }
}
```

### 5. MessageHandler 수정

```java
/**
 * MessageHandler에 추가할 내용
 */
public class MessageHandler {
    // ... 기존 코드 ...

    /**
     * 메시지 핸들러 등록 (생성자에서 호출)
     */
    private void registerHandlers() {
        // ... 기존 핸들러들 ...

        // 몬스터 관련 핸들러
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof MonsterSpawnMsg) {
                    handleMonsterSpawn((MonsterSpawnMsg) object);
                } else if (object instanceof MonsterUpdateMsg) {
                    handleMonsterUpdate((MonsterUpdateMsg) object);
                } else if (object instanceof MonsterDeathMsg) {
                    handleMonsterDeath((MonsterDeathMsg) object);
                }
            }
        });
    }

    /**
     * 몬스터 스폰 처리
     */
    private void handleMonsterSpawn(MonsterSpawnMsg msg) {
        MonsterSyncManager.getInstance().onMonsterSpawnReceived(msg);
    }

    /**
     * 몬스터 업데이트 처리
     */
    private void handleMonsterUpdate(MonsterUpdateMsg msg) {
        MonsterSyncManager.getInstance().onMonsterUpdateReceived(msg);
    }

    /**
     * 몬스터 사망 처리
     */
    private void handleMonsterDeath(MonsterDeathMsg msg) {
        MonsterSyncManager.getInstance().onMonsterDeathReceived(msg);
    }
}
```

### 6. MonsterFactory 클래스

```java
/**
 * 몬스터 팩토리
 */
public class MonsterFactory {
    /**
     * 몬스터 타입에 따라 몬스터 생성
     */
    public static Monster createMonster(MonsterType type) {
        switch (type) {
            case GHOST:
                return new Ghost();
            case SLIME:
                return new Slime();
            case GOLEM:
                return new Golem();
            default:
                throw new IllegalArgumentException("Unknown monster type: " + type);
        }
    }
}
```

---

## 🧪 테스트 계획

```java
public class TestMonsterSync {
    private MonsterSyncManager syncManager;
    private NetworkManager mockNetwork;

    @BeforeEach
    public void setUp() {
        syncManager = MonsterSyncManager.getInstance();
        mockNetwork = NetworkManager.getInstance();
    }

    @Test
    public void 몬스터_스폰_메시지_전송() {
        Monster ghost = new Ghost();
        ghost.setId(1);
        ghost.setPosition(300, 300);

        syncManager.sendMonsterSpawn(ghost);

        // 메시지 전송 확인
        // verify(mockNetwork).broadcastToAll(any(MonsterSpawnMsg.class));
    }

    @Test
    public void 몬스터_스폰_메시지_수신() {
        MonsterSpawnMsg msg = new MonsterSpawnMsg();
        msg.monsterId = 1;
        msg.monsterType = MonsterType.GHOST.ordinal();
        msg.x = 300;
        msg.y = 300;
        msg.health = 60;

        syncManager.onMonsterSpawnReceived(msg);

        Monster monster = GameManager.getInstance().getMonsterById(1);
        assertNotNull(monster);
        assertEquals(MonsterType.GHOST, monster.getType());
        assertEquals(300f, monster.getPosition().x, 0.01f);
    }

    @Test
    public void 몬스터_업데이트_동기화() {
        Monster ghost = new Ghost();
        ghost.setId(1);
        ghost.setPosition(100, 100);
        GameManager.getInstance().addMonster(ghost);

        MonsterUpdateMsg msg = new MonsterUpdateMsg();
        msg.monsterId = 1;
        msg.x = 200;
        msg.y = 200;
        msg.state = MonsterState.MOVING.ordinal();
        msg.health = 50;

        syncManager.onMonsterUpdateReceived(msg);

        assertEquals(200f, ghost.getPosition().x, 0.01f);
        assertEquals(MonsterState.MOVING, ghost.getState());
        assertEquals(50, ghost.getHealth());
    }

    @Test
    public void 몬스터_사망_처리() {
        Monster ghost = new Ghost();
        ghost.setId(1);
        GameManager.getInstance().addMonster(ghost);

        Player killer = new Player(10);
        GameManager.getInstance().setLocalPlayer(killer);

        MonsterDeathMsg msg = new MonsterDeathMsg();
        msg.monsterId = 1;
        msg.killerId = 10;
        msg.expReward = 50;

        int originalExp = killer.getStats().getExperience();

        syncManager.onMonsterDeathReceived(msg);

        // 몬스터 제거 확인
        assertNull(GameManager.getInstance().getMonsterById(1));

        // 경험치 획득 확인
        assertEquals(originalExp + 50, killer.getStats().getExperience());
    }

    @Test
    public void 주기적_업데이트_전송() {
        Ghost ghost = new Ghost();
        ghost.setId(1);
        GameManager.getInstance().addMonster(ghost);

        // 0.05초 경과 (20Hz)
        syncManager.update(0.05f);

        // 업데이트 메시지 전송 확인
    }
}
```

---

## ✅ 완료 조건

- [ ] MonsterSpawnMsg 구현
- [ ] MonsterUpdateMsg 구현
- [ ] MonsterDeathMsg 구현
- [ ] MonsterSyncManager 구현
- [ ] MessageHandler에 핸들러 추가
- [ ] 동기화 주기 (20Hz) 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_22: 전투 시스템**
- 데미지 계산
- 충돌 감지
- 상태 이상 적용
