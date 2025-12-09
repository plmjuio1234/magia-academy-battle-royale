# SPEC_MONSTER.md - 몬스터 AI & 스탯

> 서버 구현 기준 (C:/Users/plmju/AndroidStudioProjects/YuGeup/3_2_J_Server/untitled/src/main/java/org/example/ServerMonster.java)

---

## 👻 Ghost (고스트)

### 기본 스탯
```
HP: 60
ATK: 25
DEF: 1
SPD: 120 (픽셀/초)
경험치: 10 exp
```

### AI 행동
```
상태: IDLE → PURSUING → ATTACKING → DEAD

IDLE (대기)
  ├─ 플레이어 감지 (aggroRange 250) → PURSUING
  └─ 10% 확률 + 현재 상태 IDLE → 투명화

PURSUING (추적)
  ├─ 플레이어를 향해 이동
  ├─ 공격 범위(300) 진입 → ATTACKING
  └─ 플레이어 감지 범위 벗어남 → IDLE

ATTACKING (공격)
  ├─ 플레이어에게 공격 (데미지 25)
  ├─ 쿨타임: 2초
  └─ 공격 후 PURSUING으로
```

### 특수 능력: 투명화
```
투명화 상태: 클라이언트에서 보이지 않음
쿨타임: 5초
지속시간: 2초
발동 확률: 10% * delta (IDLE 중)
```

### 행동 패턴 (서버)
```
매 프레임 update(delta, activePlayers):
  1. 투명화 상태 업데이트
  2. 가장 가까운 플레이어 찾기
  3. AI 상태 업데이트
     - IDLE: 무작위 방향 이동 (또는 투명화)
     - PURSUING: 플레이어 방향으로 이동
     - ATTACKING: 공격 쿨타임 감소
  4. 위치 업데이트 (x += vx * delta)
  5. 맵 경계 체크
  6. 동기화 타이머 업데이트
```

---

## 💧 Slime (슬라임)

### 기본 스탯
```
HP: 40
ATK: 15
DEF: 2
SPD: 70 (픽셀/초)
경험치: 25 exp
```

### 특징
```
원소: FIRE, WATER, WIND, EARTH, ELECTRIC 중 랜덤
(원소별 약점 시스템은 추후 구현)
```

### AI 행동
```
상태: IDLE → PURSUING → ATTACKING → DEAD

IDLE (대기)
  ├─ 플레이어 감지 (aggroRange 200) → PURSUING
  └─ 대기 상태 유지

PURSUING (추적)
  ├─ 무작위 방향으로 느리게 이동
  ├─ 플레이어와 근접(30) → ATTACKING
  └─ 플레이어 감지 범위 벗어남 → IDLE

ATTACKING (공격)
  ├─ 플레이어에게 공격 (데미지 15)
  ├─ 쿨타임: 1.5초
  └─ 공격 후 PURSUING으로
```

### 행동 패턴 (서버)
```
동작: Ghost와 유사하나 더 느림
이동: 무작위 방향 선택하여 느리게 이동
공격 거리: 30픽셀 (Ghost: 300)
```

---

## 🗿 Golem (골렘)

### 기본 스탯
```
HP: 150 (가장 높음)
ATK: 50 (가장 높음)
DEF: 4 (가장 높음)
SPD: 50 (가장 느림, 픽셀/초)
경험치: 50 exp
```

### 특수 능력: 충전 공격
```
상태: 충전 중 → 공격 실행
충전 시간: 3초
공격력: ATK * 2 (100)
효과: 광범위 폭발
```

### AI 행동
```
상태: IDLE → PURSUING → ATTACKING(CHARGING) → DEAD

IDLE (대기)
  ├─ 플레이어 감지 (aggroRange 200) → PURSUING
  └─ 움직이지 않음

PURSUING (추적)
  ├─ 느리게 이동 (SPD 50)
  ├─ 플레이어와 근접(150) → ATTACKING
  └─ 플레이어 감지 범위 벗어남 → IDLE

ATTACKING (공격)
  ├─ 충전 시작: chargingTime = 0
  ├─ 시간 증가: chargingTime += delta
  ├─ 충전 완료: chargingTime >= 3.0
  │   ├─ 강화 공격 (데미지 100)
  │   ├─ 쿨타임 설정: 4초
  │   └─ PURSUING으로 돌아감
  └─ 충전 중: 움직이지 않음
```

### 방어력 특성
```
입은 데미지 = 기본 데미지 * 0.7  (30% 감소)

예시:
  플레이어 공격: 30 데미지
  Golem 입은 데미지: 30 * 0.7 = 21
```

### 행동 패턴 (서버)
```
매 프레임 update(delta, activePlayers):
  1. 플레이어 감지
  2. ATTACKING 상태일 경우:
     ├─ 충전 여부 확인
     ├─ 충전 중: chargingTime 증가
     ├─ 충전 완료: 공격 실행
     └─ 아니면: PURSUING으로 돌아감
  3. 나머지는 Ghost와 유사
```

---

## 🎯 몬스터 매니저

### 책임
```
public class MonsterManager {
    List<Monster> monsters;

    // 매 프레임 실행 (서버 20Hz = 50ms)
    void update(float delta, List<Player> players) {
        // 1. 스폰 타이머 업데이트
        // 2. 각 몬스터 AI 실행
        // 3. 각 몬스터 위치 업데이트
        // 4. 충돌 감지
        // 5. 사망한 몬스터 제거
        // 6. 100ms마다 동기화 메시지 전송
    }
}
```

### 스폰 규칙
```
시간대별 몬스터 타입:
  0~120초(2분):   Ghost 위주
  120~300초(5분): Ghost + Slime
  300~480초(8분): Slime 위주 + Golem 간헐적
  480~600초:      Golem 증가

스폰 빈도: 1초마다 1마리
최대 유지: 50마리/방
스폰 위치: 중앙(960, 960)에서 300픽셀 이상 거리
```

---

## 📡 클라이언트 동기화

### 동기화 메시지
```
MonsterSpawnMsg (1초마다)
  ├─ monsterId
  ├─ x, y (위치)
  ├─ monsterType
  └─ elementType (Slime만)

MonsterUpdateMsg (100ms마다)
  ├─ monsterId
  ├─ x, y, vx, vy (위치 & 속도)
  ├─ hp, maxHp
  └─ state ("IDLE", "PURSUING", "ATTACKING", "DEAD")

MonsterDeathMsg (즉시)
  ├─ monsterId
  ├─ dropX, dropY (드롭 위치)
  └─ (경험치는 클라이언트에서 계산)
```

---

**구현 참고**: 서버 코드는 3_2_J_Server/untitled/src/main/java/org/example/ 참조

