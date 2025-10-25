# 스킬 획득 및 강화 시스템 (로그라이크 + 배틀로얄)

---

## 시스템 개요

**컨셉**: 로그라이크 스타일의 스킬 선택 시스템 + 배틀로얄의 빠른 진행

**진행 방식**:
1. **1레벨**: 기본공격만 사용 가능
2. **2레벨**: 원소 선택 (불/물/바람/땅/전기) + 첫 번째 스킬 획득
3. **3-20레벨**: 레벨업할 때마다 새로운 스킬 선택 가능 (3개 중 1개 선택)
4. **스킬 강화**: 같은 스킬 2개 이상 보유 시 강화 가능

---

## 1. 기본공격 (BasicAttack)

```java
public class BasicAttack {
    private static final int DAMAGE = 15;
    private static final int MANA_COST = 5;
    private static final float COOLDOWN = 0.3f;
    private static final float PROJECTILE_SPEED = 350f;

    // 모든 플레이어가 1레벨부터 사용 가능
    // 원소에 따라 색상은 다르지만 기능은 동일
}
```

**특징**:
- MP 소비 적음 (5)
- 빠른 쿨다운 (0.3초)
- 모든 플레이어가 소유

---

## 2. 스킬 획득 시스템 (SkillAcquisition)

### 레벨별 스킬 획득

```
Level 1:  기본공격만 사용

Level 2:  원소 선택 + 첫 번째 스킬
          선택지: 각 원소의 스킬 1개

Level 3:  두 번째 스킬 선택
          선택지: 보유하지 않은 스킬 3개 (원소 무관)

Level 4+: 세 번째 스킬 선택
          선택지: 보유하지 않은 스킬 3개
```

### 스킬 선택 UI

```java
public class SkillSelectionUI {
    // 레벨업 시 나타나는 UI
    // 선택지 1, 선택지 2, 선택지 3 표시
    // 각각의 스킬 정보 표시 (피해, MP, 쿨다운)
    // 사용자가 선택할 때까지 게임 일시 정지
}
```

---

## 3. 플레이어 보유 스킬 관리

```java
public class PlayerSkillSet {
    // 보유 스킬은 최대 4개까지
    private List<Skill> skills = new ArrayList<>();  // 크기: 1-4

    // 1번 슬롯: 기본공격 (고정)
    // 2-4번 슬롯: 획득한 스킬들

    public void addSkill(Skill skill) {
        if (skills.size() < 4) {
            skills.add(skill);
        }
    }

    public Skill getSkillAtSlot(int slot) {
        // slot 0: 기본공격
        // slot 1-3: 획득한 스킬
        if (slot == 0) return basicAttack;
        return skills.size() > slot - 1 ? skills.get(slot - 1) : null;
    }
}
```

---

## 4. 스킬 강화 시스템 (Upgrade)

### 강화 메커니즘

```
예: 화염구를 2번 획득했을 때

화염구 Level 1 (기본)
  → 피해: 30
  → MP: 15
  → 쿨다운: 0.5s

화염구 Level 2 (1회 강화)
  → 피해: 30 + 8 = 38
  → MP: 15 (변화 없음)
  → 쿨다운: 0.5s (변화 없음)

화염구 Level 3 (2회 강화)
  → 피해: 38 + 8 = 46
  → MP: 15 (변화 없음)
  → 쿨다운: 0.5s (변화 없음)
```

### 강화 공식

```
강화 효과 = 기본값 × 강화 배수

불 원소:
  - 강화당 피해 +30% (매 강화마다 기본 피해의 30% 추가)
  - 예: 30 → 39 → 50 → 65

물 원소:
  - 강화당 피해 +25%
  - 추가: MP 비용 5% 감소 (매 강화마다)

바람 원소:
  - 강화당 피해 +35%
  - 추가: 쿨다운 10% 감소

기타:
  - 각 원소별 특성 강화
```

### 구현

```java
public class SkillLevel {
    private int baseLevel = 1;  // 몇 번 강화되었는가

    public int getUpgradedDamage() {
        float multiplier = 1.0f + (baseLevel - 1) * 0.30f;  // 30% per level
        return (int)(baseDamage * multiplier);
    }
}
```

---

## 5. 스킬 선택 알고리즘

### 획득 스킬 선택지 생성

```java
public class SkillSelector {
    /**
     * 플레이어가 이미 보유하지 않은 스킬 3개를 랜덤으로 선택
     * 가중치:
     *   - 높은 레벨에서는 강한 스킬 확률 증가
     *   - 플레이어 선택 원소의 스킬 확률 증가 (50%)
     *   - 기타 원소의 스킬도 나올 수 있음 (50%)
     */
    public List<Skill> selectRandomSkills(Player player, int count) {
        List<Skill> available = getAllSkillsExcept(player.getOwnedSkills());

        // 가중치 적용
        List<Skill> weighted = applyWeights(available, player);

        // 무작위 선택
        return getRandomSkills(weighted, count);
    }
}
```

---

## 6. 레벨업 이벤트 핸들링

```java
public class PlayerLevelUp {
    public void onLevelUp(Player player, int newLevel) {
        if (newLevel == 2) {
            // 원소 선택 + 첫 번째 스킬 선택
            showElementSelection();
            showSkillSelection(3);  // 3개 스킬 중 1개 선택

        } else if (newLevel >= 3) {
            // 스킬 선택
            showSkillSelection(3);  // 3개 스킬 중 1개 선택

            // 같은 스킬이 있으면 강화 옵션 제공
            if (hasMultipleSameSkill(player)) {
                showUpgradeOption();
            }
        }
    }
}
```

---

## 7. UI 흐름

### 레벨 2에서의 선택

```
┌─────────────────────────────────────┐
│    원소를 선택하세요 (1/2 선택)      │
│                                     │
│  [불🔥] [물💧] [바람💨]             │
│  [땅🪨] [전기⚡]                     │
└─────────────────────────────────────┘
         ↓ (불 선택)
┌─────────────────────────────────────┐
│  첫 번째 스킬을 선택하세요            │
│                                     │
│  스킬1: 화염구                       │
│  - 피해: 30                         │
│  - MP: 15 | 쿨다운: 0.5초          │
│  [선택]                             │
│                                     │
│  스킬2: 얼음창                       │
│  - 피해: 25                         │
│  - MP: 12 | 쿨다운: 0.6초          │
│  [선택]                             │
│                                     │
│  스킬3: 회오리                       │
│  - 피해: 28                         │
│  - MP: 16 | 쿨다운: 1.0초          │
│  [선택]                             │
└─────────────────────────────────────┘
```

### 레벨 3+ 에서의 선택

```
┌─────────────────────────────────────┐
│  다음 스킬을 선택하세요               │
│  (현재 보유: 화염구, ...)            │
│                                     │
│  스킬1: 번개                         │
│  - 피해: 35                         │
│  [선택]                             │
│                                     │
│  스킬2: 물줄기 (이미 보유)           │
│  [강화]                             │
│                                     │
│  스킬3: 치유의 물                    │
│  - 회복: 25HP                       │
│  [선택]                             │
└─────────────────────────────────────┘
```

---

## 8. 네트워크 동기화

### 서버에 전송할 메시지

```java
public class SkillAcquisitionMsg {
    public int playerId;
    public int skillId;
    public int skillLevel;  // 강화 횟수
}
```

---

## 9. 데이터 구조 정의

### Skill 클래스 확장

```java
public abstract class Skill {
    protected int skillId;
    protected String skillName;
    protected int baseDamage;
    protected int manaCost;
    protected float cooldown;
    protected String elementType;

    // 추가
    protected int rarity;        // 1(일반) ~ 4(전설)
    protected int requiredLevel; // 획득 가능 레벨
    protected int upgradeTier;   // 강화 횟수 (기본값 1)
}
```

---

## 10. 라운드 진행 시간과 스킬 수

### 시간 vs 레벨 vs 보유 스킬

```
0-2분:   레벨 1-3   →  스킬 1-2개
2-4분:   레벨 3-5   →  스킬 2-3개
4-6분:   레벨 5-8   →  스킬 3-4개 (최대)
6-8분:   레벨 8-12  →  스킬 강화 집중
8-10분:  레벨 12+   →  최종 전투
```

---

## 요약

| 항목 | 상세 내용 |
|------|---------|
| **기본공격** | 모든 플레이어가 시작 시 보유 |
| **2레벨** | 원소 선택 + 첫 스킬 획득 |
| **3레벨+** | 레벨업마다 스킬 선택 (최대 4개) |
| **강화** | 같은 스킬 중복 시 강화 가능 |
| **UI** | 레벨업 시 자동 일시정지 후 선택 화면 |
| **네트워크** | 스킬 획득/강화를 서버에 동기화 |

