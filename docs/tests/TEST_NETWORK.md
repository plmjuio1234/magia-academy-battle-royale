# TEST_NETWORK.md - 네트워크 테스트

---

## 🎯 목표
네트워크 메시지 송수신 및 동기화 테스트

---

## 📋 테스트 항목

### 1. 연결 테스트
- [ ] 서버 연결 성공
- [ ] 서버 연결 실패 처리
- [ ] 재연결 기능
- [ ] 연결 끊김 감지

### 2. 메시지 송수신 테스트
- [ ] PlayerMoveMsg 송수신
- [ ] MonsterSpawnMsg 송수신
- [ ] MonsterUpdateMsg 송수신
- [ ] SkillCastMsg 송수신
- [ ] PlayerDeathMsg 송수신

### 3. 동기화 테스트
- [ ] 플레이어 위치 동기화
- [ ] 몬스터 위치 동기화
- [ ] 스킬 시전 동기화
- [ ] 게임 상태 동기화

---

## 🔧 테스트 코드 예시

### 연결 테스트

```java
@Test
public void 서버_연결_성공() {
    NetworkManager network = NetworkManager.getInstance();
    boolean connected = network.connect("localhost", 5000);

    assertTrue(connected);
    assertTrue(network.isConnected());
}

@Test
public void 서버_연결_실패_처리() {
    NetworkManager network = NetworkManager.getInstance();
    boolean connected = network.connect("invalid_host", 9999);

    assertFalse(connected);
    assertFalse(network.isConnected());
}

@Test
public void 재연결_기능() {
    NetworkManager network = NetworkManager.getInstance();
    network.connect("localhost", 5000);
    network.disconnect();

    // 재연결
    boolean reconnected = network.connect("localhost", 5000);
    assertTrue(reconnected);
}
```

### 메시지 송수신 테스트

```java
@Test
public void PlayerMoveMsg_송신() {
    NetworkManager network = NetworkManager.getInstance();
    network.connect("localhost", 5000);

    PlayerMoveMsg msg = new PlayerMoveMsg();
    msg.playerId = 1;
    msg.x = 100;
    msg.y = 200;

    network.sendMessage(msg);

    // 서버에서 수신 확인 (통합 테스트)
}

@Test
public void MonsterSpawnMsg_수신() {
    MessageHandler handler = MessageHandler.getInstance();

    MonsterSpawnMsg msg = new MonsterSpawnMsg();
    msg.monsterId = 1;
    msg.monsterType = MonsterType.GHOST.ordinal();
    msg.x = 300;
    msg.y = 300;
    msg.health = 60;

    handler.handleMonsterSpawn(msg);

    Monster monster = GameManager.getInstance().getMonsterById(1);
    assertNotNull(monster);
    assertEquals(MonsterType.GHOST, monster.getType());
}
```

### 동기화 테스트

```java
@Test
public void 플레이어_위치_동기화() {
    Player localPlayer = new Player(1);
    localPlayer.setPosition(100, 100);
    GameManager.getInstance().setLocalPlayer(localPlayer);

    Player remotePlayer = new Player(2);
    remotePlayer.setPosition(200, 200);
    GameManager.getInstance().addRemotePlayer(remotePlayer);

    // 원격 플레이어 위치 업데이트 메시지
    PlayerMoveMsg msg = new PlayerMoveMsg();
    msg.playerId = 2;
    msg.x = 250;
    msg.y = 250;

    MessageHandler.getInstance().handlePlayerMove(msg);

    // 위치 동기화 확인
    assertEquals(250f, remotePlayer.getPosition().x, 1f);
    assertEquals(250f, remotePlayer.getPosition().y, 1f);
}

@Test
public void 몬스터_동기화_주기_20Hz() {
    MonsterSyncManager syncManager = MonsterSyncManager.getInstance();

    Monster ghost = new Ghost();
    ghost.setId(1);
    GameManager.getInstance().addMonster(ghost);

    // 0.05초마다 업데이트 (20Hz)
    int updateCount = 0;

    for (int i = 0; i < 20; i++) {
        syncManager.update(0.05f);
        updateCount++;
    }

    // 1초 동안 20번 업데이트
    assertEquals(20, updateCount);
}
```

---

## ✅ 테스트 체크리스트

### 연결 관리
- [ ] 서버 연결
- [ ] 연결 해제
- [ ] 재연결
- [ ] 타임아웃 처리

### 메시지 전송
- [ ] 플레이어 이동
- [ ] 몬스터 스폰
- [ ] 스킬 시전
- [ ] 게임 이벤트

### 메시지 수신
- [ ] 올바른 핸들러 호출
- [ ] 메시지 파싱
- [ ] 게임 상태 반영

### 동기화
- [ ] 위치 동기화
- [ ] 상태 동기화
- [ ] 주기 확인 (20Hz)

---

## 📊 성능 테스트

```java
@Test
public void 네트워크_지연_측정() {
    long startTime = System.currentTimeMillis();

    // 메시지 전송
    PlayerMoveMsg msg = new PlayerMoveMsg();
    NetworkManager.getInstance().sendMessage(msg);

    // 응답 대기
    // ...

    long endTime = System.currentTimeMillis();
    long latency = endTime - startTime;

    // 100ms 이하 확인
    assertTrue(latency < 100);
}

@Test
public void 대량_메시지_처리() {
    // 100개 메시지 동시 전송
    for (int i = 0; i < 100; i++) {
        PlayerMoveMsg msg = new PlayerMoveMsg();
        msg.playerId = i;
        NetworkManager.getInstance().sendMessage(msg);
    }

    // 처리 확인
}
```

---

## 🔗 관련 문서
- PHASE_03_NETWORK_CORE.md
- PHASE_21_MONSTER_SYNC.md
- PHASE_23_PLAYER_SYNC.md
