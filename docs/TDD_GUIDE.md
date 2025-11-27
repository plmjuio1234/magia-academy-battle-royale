# TDD_GUIDE.md - 테스트 주도 개발 가이드

---

## 📋 목차

1. [TDD란?](#tdd란)
2. [Red-Green-Refactor 사이클](#red-green-refactor-사이클)
3. [테스트 작성 규칙](#테스트-작성-규칙)
4. [테스트 구조](#테스트-구조)
5. [예시 모음](#예시-모음)
6. [테스트 도구](#테스트-도구)

---

## TDD란?

### 정의

**Test-Driven Development (테스트 주도 개발)**는 다음 순서로 개발하는 방식입니다:

```
1. RED: 실패하는 테스트 작성
   └─ 구현 전에 먼저 테스트 코드를 작성

2. GREEN: 최소 구현으로 테스트 통과
   └─ 테스트를 통과하는 가장 간단한 코드 작성

3. REFACTOR: 코드 개선
   └─ 테스트를 유지하면서 코드를 정리
```

### 장점

```
✅ 버그 감소: 테스트로 미리 검증
✅ 리팩토링 자신감: 테스트가 보호
✅ 문서화: 테스트가 사용법 설명
✅ 설계 개선: 테스트하기 쉬운 설계로 유도
✅ 유지보수 용이: 회귀 테스트 자동화
```

---

## Red-Green-Refactor 사이클

### Step 1: RED (실패하는 테스트)

```
목표: 구현하고 싶은 기능의 테스트를 먼저 작성

예시:
  @Test
  public void 플레이어가_데미지를_받으면_체력이_감소한다() {
      Player player = new Player();
      player.setHealth(100);

      int actualDamage = player.takeDamage(30);

      assertEquals(70, player.getHealth());
      assertEquals(30, actualDamage);
  }

상태: 컴파일 에러 또는 테스트 실패 (RED) 🔴
```

### Step 2: GREEN (최소 구현)

```
목표: 테스트를 통과하는 최소한의 코드만 작성

예시:
  public class Player {
      private int health = 100;

      public void setHealth(int h) {
          this.health = h;
      }

      public int takeDamage(int damage) {
          this.health -= damage;
          return damage;
      }

      public int getHealth() {
          return this.health;
      }
  }

상태: 테스트 통과 (GREEN) 🟢
```

### Step 3: REFACTOR (개선)

```
목표: 코드를 더 읽기 쉽고 유지보수하기 좋게 개선

예시 - 개선 전:
  public int takeDamage(int damage) {
      this.health -= damage;
      return damage;
  }

예시 - 개선 후:
  public int takeDamage(int damage) {
      int actualDamage = Math.max(0, damage - (this.defense * 2));
      this.health -= actualDamage;

      // 사망 처리
      if (this.health <= 0) {
          this.health = 0;
          this.isDead = true;
      }

      return actualDamage;
  }

상태: 여전히 테스트 통과, 코드 개선됨 🟢
```

### 사이클 반복

```
다음 테스트 케이스로 이동:

RED 🔴
  ↓
  새로운 테스트 작성
  ↓
GREEN 🟢
  ↓
  최소 구현
  ↓
REFACTOR 🟢
  ↓
  코드 개선
  ↓
RED 🔴 (다시...)
```

---

## 테스트 작성 규칙

### 1. AAA 패턴 (Arrange-Act-Assert)

```java
@Test
public void 테스트명_명확한_행동() {
    // Arrange (준비): 테스트 환경 설정
    Player player = new Player();
    player.setHealth(100);
    player.setDefense(5);

    // Act (실행): 테스트할 동작 수행
    int actualDamage = player.takeDamage(30);

    // Assert (검증): 결과 확인
    assertEquals(20, actualDamage);  // 방어력 적용됨
    assertEquals(80, player.getHealth());  // 체력 감소
}
```

### 2. 테스트 메서드명 규칙

```java
// ✅ 좋은 예
@Test
public void 플레이어가_데미지를_받으면_체력이_감소한다() { }

@Test
public void 플레이어_체력이_0_이하_되면_사망한다() { }

@Test
public void 마나가_부족하면_스킬을_시전할_수_없다() { }

// ❌ 나쁜 예
@Test
public void test1() { }

@Test
public void testDamage() { }  // 한글 사용 X
```

### 3. 한 테스트는 한 가지만 검증

```java
// ❌ 나쁜 예 (여러 것 검증)
@Test
public void 플레이어_테스트() {
    Player p = new Player();
    assertEquals(100, p.getHealth());
    assertEquals(50, p.getMana());
    assertEquals(15, p.getAttack());
    assertTrue(p.isAlive());
}

// ✅ 좋은 예 (하나만 검증)
@Test
public void 플레이어_초기_체력은_100이다() {
    Player p = new Player();
    assertEquals(100, p.getHealth());
}

@Test
public void 플레이어_초기_마나는_50이다() {
    Player p = new Player();
    assertEquals(50, p.getMana());
}
```

### 4. 상수 사용

```java
// ❌ 나쁜 예 (매직 넘버)
@Test
public void 테스트() {
    assertEquals(70, player.getHealth());  // 70이 뭔가?
}

// ✅ 좋은 예 (상수 사용)
private static final int INITIAL_HEALTH = 100;
private static final int DAMAGE_AMOUNT = 30;
private static final int EXPECTED_HEALTH = 70;

@Test
public void 플레이어가_데미지를_받으면_체력이_감소한다() {
    Player player = new Player();
    assertEquals(INITIAL_HEALTH, player.getHealth());

    player.takeDamage(DAMAGE_AMOUNT);

    assertEquals(EXPECTED_HEALTH, player.getHealth());
}
```

---

## 테스트 구조

### 테스트 클래스 구조

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Player 클래스의 테스트
 *
 * 테스트 대상: Player.java
 * 테스트 범위: 데미지, 레벨업, 마나 시스템
 */
public class TestPlayer {
    // 테스트 고정값 (상수)
    private static final int INITIAL_HEALTH = 100;
    private static final int INITIAL_MANA = 50;
    private static final int LEVEL_UP_EXP = 100;

    // 각 테스트에서 사용할 객체
    private Player player;

    // 각 테스트 실행 전 실행 (초기화)
    @BeforeEach
    public void setUp() {
        player = new Player();
    }

    // ===== 체력 관련 테스트 =====
    @Test
    public void 플레이어_초기_체력은_100이다() {
        assertEquals(INITIAL_HEALTH, player.getHealth());
    }

    @Test
    public void 플레이어가_데미지를_받으면_체력이_감소한다() {
        player.takeDamage(30);
        assertEquals(70, player.getHealth());
    }

    @Test
    public void 플레이어_체력이_0_이하_되면_사망한다() {
        player.takeDamage(100);
        assertTrue(player.isDead());
    }

    // ===== 경험치 관련 테스트 =====
    @Test
    public void 플레이어가_경험치를_획득한다() {
        player.gainExperience(50);
        assertEquals(50, player.getExperience());
    }

    @Test
    public void 필요_경험치에_도달하면_레벨업한다() {
        player.gainExperience(LEVEL_UP_EXP);
        assertEquals(2, player.getLevel());
    }

    // ===== 마나 관련 테스트 =====
    @Test
    public void 마나가_부족하면_스킬을_시전할_수_없다() {
        player.setMana(10);
        assertFalse(player.canCastSkill(20));  // 마나 20 필요, 10만 있음
    }
}
```

### BeforeEach / AfterEach

```java
public class TestMonster {
    private Monster monster;
    private List<Monster> monsters;

    @BeforeEach
    public void setUp() {
        // 각 테스트 전에 실행
        monster = new Ghost();
        monsters = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        // 각 테스트 후에 실행 (정리)
        monster = null;
        monsters.clear();
    }

    @Test
    public void 테스트1() { }

    @Test
    public void 테스트2() { }
}
```

---

## 예시 모음

### 예시 1: Player 체력 시스템

```java
public class TestPlayerHealth {
    private Player player;

    @BeforeEach
    public void setUp() {
        player = new Player();
    }

    // 정상 데미지
    @Test
    public void 플레이어가_정상_데미지를_받는다() {
        int damage = player.takeDamage(30);
        assertEquals(30, damage);
        assertEquals(70, player.getHealth());
    }

    // 방어력 고려
    @Test
    public void 방어력이_데미지를_감소시킨다() {
        player.setDefense(5);  // 방어력 5 (10 감소)
        int damage = player.takeDamage(30);
        assertEquals(20, damage);  // 30 - 10 = 20
        assertEquals(80, player.getHealth());
    }

    // 사망 처리
    @Test
    public void 체력이_0_이하_되면_사망한다() {
        player.takeDamage(100);
        assertTrue(player.isDead());
        assertEquals(0, player.getHealth());
    }

    // 회복
    @Test
    public void 플레이어가_체력을_회복한다() {
        player.takeDamage(30);
        player.heal(20);
        assertEquals(90, player.getHealth());
    }

    // 과다 회복 방지
    @Test
    public void 회복시_최대_체력을_초과하지_않는다() {
        player.setHealth(95);
        player.heal(20);
        assertEquals(100, player.getHealth());
    }
}
```

### 예시 2: Monster AI

```java
public class TestMonsterAI {
    private Ghost ghost;
    private Player player;

    @BeforeEach
    public void setUp() {
        ghost = new Ghost();
        ghost.setPosition(0, 0);

        player = new Player();
        player.setPosition(100, 100);
    }

    // 어그로 범위
    @Test
    public void 플레이어가_어그로_범위_내면_추적한다() {
        List<Integer> players = new ArrayList<>();
        players.add(1);  // 플레이어 ID

        ghost.update(0.016f, players);  // 16ms

        assertEquals("PURSUING", ghost.getState());
    }

    @Test
    public void 플레이어가_어그로_범위_외면_유휴한다() {
        player.setPosition(500, 500);  // 너무 멀어짐

        List<Integer> players = new ArrayList<>();

        ghost.update(0.016f, players);

        assertEquals("IDLE", ghost.getState());
    }

    // 공격 쿨타임
    @Test
    public void 공격_쿨타임이_경과하면_다시_공격할_수_있다() {
        float attackCooldown = ghost.getAttackCooldown();
        ghost.setCurrentCooldown(0);

        assertTrue(ghost.canAttack());
    }

    @Test
    public void 공격_직후_쿨타임이_설정된다() {
        ghost.attack();
        assertTrue(ghost.getAttackCooldown() > 0);
    }
}
```

### 예시 3: Skill System

```java
public class TestSkillSystem {
    private Player player;
    private SkillManager skillManager;

    @BeforeEach
    public void setUp() {
        player = new Player();
        skillManager = player.getSkillManager();
    }

    // 마나 부족
    @Test
    public void 마나가_부족하면_스킬을_시전할_수_없다() {
        player.setMana(10);
        Skill skill = skillManager.getSkill(0);  // 마나 30 필요

        boolean result = skillManager.castSkill(0);

        assertFalse(result);
        assertEquals(10, player.getMana());
    }

    // 마나 충분
    @Test
    public void 마나가_충분하면_스킬을_시전한다() {
        player.setMana(50);

        boolean result = skillManager.castSkill(0);

        assertTrue(result);
        assertEquals(20, player.getMana());  // 30 소비
    }

    // 쿨타임 확인
    @Test
    public void 쿨타임이_남아있으면_스킬을_시전할_수_없다() {
        player.setMana(50);

        skillManager.castSkill(0);
        boolean result = skillManager.castSkill(0);  // 바로 다시

        assertFalse(result);  // 쿨타임 때문에 실패
    }

    // 쿨타임 감소
    @Test
    public void 시간이_경과하면_쿨타임이_감소한다() {
        skillManager.castSkill(0);
        float cooldown = skillManager.getCurrentCooldown(0);

        skillManager.update(1.0f);  // 1초 경과
        float newCooldown = skillManager.getCurrentCooldown(0);

        assertTrue(newCooldown < cooldown);
    }
}
```

### 예시 4: Combat System

```java
public class TestCombatSystem {
    private CombatSystem combat;
    private Player player;
    private Monster monster;

    @BeforeEach
    public void setUp() {
        combat = new CombatSystem();
        player = new Player();
        monster = new Ghost();
    }

    // 기본 데미지 계산
    @Test
    public void 데미지는_공격자_공격력으로_계산된다() {
        player.setAttack(20);
        int damage = combat.calculateDamage(player, monster);

        assertEquals(20, damage);
    }

    // 방어력 감소
    @Test
    public void 방어력이_데미지를_감소시킨다() {
        player.setAttack(30);
        monster.setDefense(5);

        int damage = combat.calculateDamage(player, monster);

        assertEquals(20, damage);  // 30 - 5 = 25... (근데 정확한 계산식은?)
    }

    // 최소 데미지 1
    @Test
    public void 데미지는_최소_1_이상이다() {
        player.setAttack(5);
        monster.setDefense(100);

        int damage = combat.calculateDamage(player, monster);

        assertTrue(damage >= 1);
    }

    // 몬스터 사망
    @Test
    public void 몬스터가_사망하면_경험치를_준다() {
        monster.setHealth(10);
        int expBefore = player.getExperience();

        combat.dealDamage(player, monster, 20);

        assertTrue(player.getExperience() > expBefore);
    }
}
```

---

## 테스트 도구

### JUnit 4 vs JUnit 5 (Jupiter)

```java
// JUnit 4 (구버전)
import org.junit.Test;
import org.junit.Before;

@Test
public void testName() { }

@Before
public void setUp() { }

// JUnit 5 (최신)
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

@Test
public void 테스트명() { }

@BeforeEach
public void setUp() { }
```

### Assertion 메서드들

```java
// 동등성 검증
assertEquals(expected, actual);      // 값이 같은가?
assertNotEquals(unexpected, actual); // 값이 다른가?

// 불린 검증
assertTrue(condition);               // true인가?
assertFalse(condition);              // false인가?

// Null 검증
assertNull(object);                  // null인가?
assertNotNull(object);               // null이 아닌가?

// 객체 검증
assertSame(expected, actual);        // 같은 객체인가?
assertNotSame(unexpected, actual);   // 다른 객체인가?

// 예외 검증
assertThrows(Exception.class, () -> {
    // 예외를 던지는 코드
});

// 배열 검증
assertArrayEquals(expected, actual);

// 컬렉션 검증
assertTrue(list.contains(element));
```

### 테스트 실행

```bash
# Android Studio에서:
# 1. 테스트 클래스 우클릭
# 2. "Run 'TestClassName'" 선택

# 또는 gradle 사용:
./gradlew test

# 특정 테스트만:
./gradlew test --tests TestPlayer
```

---

## Phase별 테스트 체크리스트

### 각 Phase 완료 전:

```
□ 모든 테스트 작성 (RED)
□ 모든 테스트 통과 (GREEN)
□ 코드 리팩토링 (REFACTOR)
□ 통합 테스트 실행
□ 엣지 케이스 테스트 추가
□ 문서 업데이트
```

### 테스트 커버리지 목표

```
- 유틸리티 함수: 100%
- 게임 로직 (Player, Monster, Skill): 90% 이상
- UI 컴포넌트: 70% 이상
- 네트워크: 80% 이상
```

---

**마지막 업데이트**: 2025-11-18
**버전**: 1.0
