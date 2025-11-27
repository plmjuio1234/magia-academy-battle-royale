# TEST_GAMEPLAY.md - 게임플레이 테스트

---

## 🎯 목표
게임플레이 핵심 기능 테스트 (스킬, 전투, 동기화)

---

## 📋 테스트 항목

### 1. 플레이어 테스트
- [ ] 플레이어 생성
- [ ] 이동 (조이스틱)
- [ ] 능력치 관리
- [ ] 레벨업
- [ ] 사망

### 2. 스킬 테스트
- [ ] 5원소 × 3스킬 = 15개 스킬
- [ ] 매직 미사일 (기본 공격)
- [ ] 스킬 쿨타임
- [ ] 마나 소비
- [ ] 스킬 업그레이드

### 3. 전투 테스트
- [ ] 플레이어 → 몬스터 공격
- [ ] 몬스터 → 플레이어 공격
- [ ] 플레이어 → 플레이어 공격 (PVP)
- [ ] 데미지 계산
- [ ] 상태 이상 (스턴, 슬로우 등)

### 4. 몬스터 테스트
- [ ] 몬스터 스폰
- [ ] AI 동작
- [ ] 경험치 보상
- [ ] 사망 처리

---

## 🔧 테스트 코드 예시

### 플레이어 테스트

```java
@Test
public void 플레이어_생성() {
    Player player = new Player(1);
    assertNotNull(player);
    assertEquals(100, player.getStats().getHealth());
}

@Test
public void 플레이어_이동() {
    Player player = new Player(1);
    player.setPosition(100, 100);

    player.move(new Vector2(1, 0), 0.016f);  // 오른쪽 이동

    assertTrue(player.getPosition().x > 100);
}

@Test
public void 플레이어_레벨업() {
    Player player = new Player(1);
    int originalLevel = player.getStats().getLevel();
    int originalHealth = player.getStats().getMaxHealth();

    player.gainExperience(200);  // 충분한 경험치

    assertTrue(player.getStats().getLevel() > originalLevel);
    assertTrue(player.getStats().getMaxHealth() > originalHealth);
}
```

### 스킬 테스트

```java
@Test
public void 파이어볼_시전() {
    Player player = new Player(1);
    player.setElement(ElementType.FIRE);
    player.getStats().setMana(100);

    Fireball fireball = new Fireball();
    fireball.cast(player, new Vector2(500, 500));

    // 발사체 생성 확인
    List<Projectile> projectiles = GameManager.getInstance().getProjectiles();
    assertEquals(1, projectiles.size());

    // 마나 소비 확인
    assertTrue(player.getStats().getMana() < 100);
}

@Test
public void 아이스샤드_관통() {
    Player player = new Player(1);
    player.setElement(ElementType.WATER);
    player.getStats().setMana(100);

    // 몬스터 3마리 일렬 배치
    for (int i = 0; i < 3; i++) {
        Monster m = new Ghost();
        m.setPosition(200 + i * 50, 200);
        GameManager.getInstance().addMonster(m);
    }

    IceShard iceShard = new IceShard();
    iceShard.cast(player, new Vector2(400, 200));

    // 3개 발사체 생성 (부채꼴)
    assertEquals(3, GameManager.getInstance().getProjectiles().size());
}

@Test
public void 스킬_업그레이드() {
    Player player = new Player(1);
    player.setElement(ElementType.FIRE);
    player.getStats().setExperience(100);

    Fireball fireball = new Fireball();
    int originalDamage = fireball.getDamage();

    SkillUpgradeManager upgradeManager = new SkillUpgradeManager(player);
    upgradeManager.upgradeSkill(fireball, ElementalSkill.UpgradeType.DAMAGE);

    assertTrue(fireball.getDamage() > originalDamage);
    assertEquals(2, fireball.getSkillLevel());
}
```

### 전투 테스트

```java
@Test
public void 플레이어가_몬스터_공격() {
    Player player = new Player(1);
    Monster ghost = new Ghost();
    ghost.setHealth(60);

    int damage = CombatSystem.getInstance().dealDamage(player, ghost, 30);

    assertTrue(damage > 0);
    assertTrue(ghost.getHealth() < 60);
}

@Test
public void 몬스터가_플레이어_공격() {
    Player player = new Player(1);
    player.getStats().setHealth(100);

    Monster ghost = new Ghost();

    CombatSystem.getInstance().dealDamage(ghost, player, 20);

    assertTrue(player.getStats().getHealth() < 100);
}

@Test
public void PVP_전투() {
    Player attacker = new Player(1);
    Player defender = new Player(2);
    defender.getStats().setHealth(100);

    CombatSystem.getInstance().dealDamage(attacker, defender, 50);

    // PVP 데미지 감소 (70%)
    int expectedDamage = (int) (50 * 0.7f);
    assertEquals(100 - expectedDamage, defender.getStats().getHealth());
}

@Test
public void 스턴_상태이상() {
    Monster ghost = new Ghost();
    StunnedBuff stun = new StunnedBuff(2.0f);
    ghost.addBuff(stun);

    assertTrue(ghost.hasBuff(BuffType.STUNNED));

    // 2초 경과
    stun.update(2.0f);

    assertFalse(stun.isActive());
}
```

### 몬스터 테스트

```java
@Test
public void 몬스터_스폰() {
    MonsterSpawner spawner = new MonsterSpawner();
    Monster ghost = spawner.spawn(MonsterType.GHOST, new Vector2(300, 300));

    assertNotNull(ghost);
    assertEquals(MonsterType.GHOST, ghost.getType());
    assertEquals(60, ghost.getHealth());
}

@Test
public void 몬스터_AI_추적() {
    Monster ghost = new Ghost();
    ghost.setPosition(100, 100);

    Player player = new Player(1);
    player.setPosition(500, 100);

    // AI 업데이트
    ghost.updateAI(player, 0.016f);

    // 플레이어 방향으로 이동
    assertTrue(ghost.getPosition().x > 100);
}

@Test
public void 몬스터_사망_경험치보상() {
    Player player = new Player(1);
    int originalExp = player.getStats().getExperience();

    Monster ghost = new Ghost();
    CombatSystem.getInstance().dealDamage(player, ghost, 1000);

    // 경험치 획득
    assertTrue(player.getStats().getExperience() > originalExp);
}
```

### 게임 진행 테스트

```java
@Test
public void 맵_축소() {
    ZoneManager zoneManager = new ZoneManager();

    Zone initialZone = zoneManager.getCurrentZone();
    assertEquals(1920f, initialZone.getBounds().width);

    // 2분 경과
    zoneManager.update(120f);

    Zone newZone = zoneManager.getCurrentZone();
    assertTrue(newZone.getBounds().width < 1920f);
}

@Test
public void 구역_밖_데미지() {
    Player player = new Player(1);
    player.setPosition(2000, 2000);  // 맵 밖
    player.getStats().setHealth(100);

    ZoneManager zoneManager = new ZoneManager();

    // 1초 동안 업데이트 (20Hz)
    for (int i = 0; i < 20; i++) {
        zoneManager.update(0.05f);
    }

    // 데미지 받음
    assertTrue(player.getStats().getHealth() < 100);
}

@Test
public void 게임_종료_조건_10분() {
    GameManager gameManager = GameManager.getInstance();
    gameManager.startGame();

    // 10분 경과
    gameManager.update(600f);

    assertTrue(gameManager.isGameEnded());
}

@Test
public void 게임_종료_조건_1명_생존() {
    GameManager gameManager = GameManager.getInstance();
    gameManager.startGame();

    // 플레이어 8명 중 7명 사망
    List<Player> players = gameManager.getAllPlayers();
    for (int i = 1; i < 8; i++) {
        players.get(i).setHealth(0);
    }

    gameManager.checkGameEndCondition();

    assertTrue(gameManager.isGameEnded());
}
```

---

## ✅ 테스트 체크리스트

### 플레이어 기능
- [ ] 생성/초기화
- [ ] 이동 제어
- [ ] 능력치 관리
- [ ] 레벨 시스템

### 스킬 시스템
- [ ] 5원소 스킬 (15개)
- [ ] 매직 미사일
- [ ] 쿨타임 관리
- [ ] 업그레이드

### 전투 시스템
- [ ] PVE 전투
- [ ] PVP 전투
- [ ] 데미지 계산
- [ ] 상태 이상

### 몬스터 시스템
- [ ] 스폰/제거
- [ ] AI 동작
- [ ] 경험치 보상

### 게임 진행
- [ ] 맵 축소
- [ ] 게임 종료
- [ ] 결과 계산

---

## 📊 성능 테스트

```java
@Test
public void 다수_몬스터_렌더링_60fps() {
    // 100마리 몬스터 생성
    for (int i = 0; i < 100; i++) {
        Monster m = new Ghost();
        m.setPosition(i * 50, 100);
        GameManager.getInstance().addMonster(m);
    }

    long startTime = System.nanoTime();

    // 1프레임 업데이트 + 렌더링
    GameManager.getInstance().update(0.016f);
    GameManager.getInstance().render(new SpriteBatch());

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000000;  // ms

    // 16ms 이하 (60fps)
    assertTrue(duration < 16);
}
```

---

## 🔗 관련 문서
- PHASE_13 ~ PHASE_28 (모든 게임플레이 Phase)
- TEST_PLAN.md
- SDD.md
