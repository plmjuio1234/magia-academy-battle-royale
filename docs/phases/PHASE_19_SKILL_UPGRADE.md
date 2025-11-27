# PHASE_19_SKILL_UPGRADE.md - 업그레이드 시스템 (재설계)

---

## 🎯 목표
레벨 5 이상에서 무작위 업그레이드를 선택하는 시스템 구현

---

## 📋 구현 범위

### ✅ 레벨별 시스템
- **레벨 1**: 매직 미사일만 사용
- **레벨 2, 3, 4**: 원소 스킬 A, B, C 순서대로 자동 학습
- **레벨 5+**: 무작위 3가지 업그레이드 중 선택

### ✅ 업그레이드 종류
**스킬 강화** (각 스킬별, 최대 레벨 5):
- 스킬 A 데미지 증가 (+5)
- 스킬 A 쿨타임 감소 (-10%)
- 스킬 B 데미지 증가 (+8)
- 스킬 B 쿨타임 감소 (-10%)
- 스킬 C 데미지 증가 (+12)
- 스킬 C 쿨타임 감소 (-10%)

**스탯 강화** (최대 레벨 5):
- 최대 체력 (+30 HP)
- 최대 마나 (+20 MP)
- 마나 재생 (+1 MP/초)
- 공격력 (+5)
- 이동 속도 (+5%)

---

## 📁 필요 파일

```
game/upgrade/                         (새로 생성)
  ├─ UpgradeOption.java              (업그레이드 열거형)
  └─ UpgradeManager.java              (통합 업그레이드 관리자)

game/skill/
  └─ ElementalSkill.java              (수정 - 업그레이드 메서드 추가)

ui/hud/
  └─ LevelUpUpgradePanel.java         (완전 재작성)

utils/
  └─ Constants.java                   (수정 - 새 상수 추가)
```

---

## 🔧 구현 내용

### 1. UpgradeOption (열거형)

```java
/**
 * 업그레이드 옵션 타입
 */
public enum UpgradeOption {
    // 스킬 강화
    SKILL_A_DAMAGE("스킬 A 데미지", "데미지 +5"),
    SKILL_A_COOLDOWN("스킬 A 쿨타임", "쿨타임 -10%"),
    SKILL_B_DAMAGE("스킬 B 데미지", "데미지 +8"),
    SKILL_B_COOLDOWN("스킬 B 쿨타임", "쿨타임 -10%"),
    SKILL_C_DAMAGE("스킬 C 데미지", "데미지 +12"),
    SKILL_C_COOLDOWN("스킬 C 쿨타임", "쿨타임 -10%"),

    // 스탯 강화
    STAT_MAX_HP("최대 체력", "+30 HP"),
    STAT_MAX_MP("최대 마나", "+20 MP"),
    STAT_MP_REGEN("마나 재생", "+1 MP/초"),
    STAT_ATTACK("공격력", "+5 공격력"),
    STAT_SPEED("이동 속도", "+5% 속도");
}
```

### 2. UpgradeManager (통합 관리자)

**핵심 기능**:
- 각 업그레이드의 현재 레벨 추적 (최대 5)
- 무작위 3개 선택지 생성 (만렙 제외)
- 스킬/스탯 업그레이드 적용

**주요 메서드**:
```java
// 업그레이드 적용
public boolean applyUpgrade(UpgradeOption option)

// 무작위 3개 생성
public List<UpgradeOption> generateRandomUpgrades()

// 현재 레벨 조회
public int getUpgradeLevel(UpgradeOption option)

// 미리보기 텍스트
public String getUpgradePreviewText(UpgradeOption option)
```

### 3. ElementalSkill (수정)

**추가된 필드**:
```java
protected int damageBonus;              // 데미지 누적 보너스
protected float cooldownReductionBonus; // 쿨타임 감소 누적
```

**추가된 메서드**:
```java
public void addDamageBonus(int bonus)
public void addCooldownReduction(float reduction)
```

**최종 데미지/쿨타임 계산**:
```java
public int getDamage() {
    return (int)(baseDamage * damageMultiplier) + damageBonus;
}

public float getCooldown() {
    return cooldown * (1 - cooldownReduction) * (1 - cooldownReductionBonus);
}
```

### 4. LevelUpUpgradePanel (UI)

**특징**:
- `create-room-frame.png` 이미지를 카드 배경으로 사용
- 게임 화면 위에 반투명 배경 (투명도 70%)
- 3개 카드를 중앙 정렬하여 세로로 배치
- 카드 클릭 시 즉시 적용

**렌더링 구조**:
```
┌─────────────────────────────┐
│   레벨 X 달성!              │
│   업그레이드를 선택하세요    │
├─────────────────────────────┤
│  ┌───┐  ┌───┐  ┌───┐       │
│  │ A │  │ B │  │ C │       │ ← 3개 카드
│  └───┘  └───┘  └───┘       │
└─────────────────────────────┘

각 카드 내용:
┌────────────┐
│ 옵션 이름  │ (크게, 흰색)
│ 효과 설명  │ (중간, 초록색)
│ Lv 0 → 1   │ (작게, 회색)
└────────────┘
```

### 5. Constants.java (새 상수)

```java
// 업그레이드 최대 레벨
public static final int MAX_UPGRADE_LEVEL = 5;

// 스킬 강화
public static final int SKILL_DAMAGE_UPGRADE_BONUS = 5;
public static final float SKILL_COOLDOWN_UPGRADE_REDUCTION = 0.1f;  // 10%

// 스탯 강화
public static final int STAT_HP_UPGRADE_BONUS = 30;
public static final int STAT_MP_UPGRADE_BONUS = 20;
public static final float STAT_MP_REGEN_UPGRADE_BONUS = 1.0f;
public static final int STAT_ATTACK_UPGRADE_BONUS = 5;
public static final float STAT_SPEED_UPGRADE_MULTIPLIER = 0.05f;  // 5%
```

---

## 🎮 사용 흐름

### 레벨업 프로세스
```
1. 플레이어가 경험치 획득
   ↓
2. LevelSystem에서 levelUp() 호출
   ↓
3. LevelUpListener.onLevelUp(newLevel) 트리거
   ↓
4. LevelUpUpgradePanel.onLevelUp(newLevel) 호출
   ↓
5-1. 레벨 < 5: 자동 스킬 학습 (원소 스킬 A, B, C)
5-2. 레벨 >= 5: 무작위 3가지 업그레이드 선택
   ↓
6. UpgradeManager.generateRandomUpgrades()
   - 최대 레벨(5)이 아닌 업그레이드만 수집
   - 무작위로 3개 선택
   ↓
7. UI 패널 표시
   - 반투명 배경 + 3개 카드
   - 각 카드: 이름, 효과, 레벨
   ↓
8. 플레이어가 카드 클릭
   ↓
9. UpgradeManager.applyUpgrade(option)
   - 스킬 강화: ElementalSkill.addDamageBonus() 또는 addCooldownReduction()
   - 스탯 강화: PlayerStats.setXxx()
   ↓
10. 패널 숨김 & 게임 재개
```

---

## ✅ 완료 조건

- [x] UpgradeOption 열거형 생성 (11가지)
- [x] UpgradeManager 통합 관리자 구현
- [x] ElementalSkill에 업그레이드 메서드 추가
- [x] LevelUpUpgradePanel UI 완전 재작성
- [x] Constants.java 새 상수 추가
- [x] 무작위 3개 선택 로직 구현
- [x] 최대 레벨 5 제한 적용
- [x] 만렙 업그레이드 제외 로직

---

## 🔗 다음 Phase

**PHASE_20: 몬스터 렌더링**
- 3가지 몬스터 타입 (고스트, 슬라임, 골렘)
- 애니메이션 및 상태 표시
- 체력 바 렌더링

---

## 📝 변경 이력

**v2.0 (2025-11-24)** - Phase 19 완전 재설계
- 기존 스킬 레벨 3 시스템 → 업그레이드 레벨 5 시스템
- RANGE 제거, 스킬/스탯 분리
- 무작위 선택 시스템 도입
- UI 개선 (카드 방식)

**v1.0 (이전)** - 초기 설계 (폐기)
- 스킬 레벨 3 (DAMAGE/RANGE/COOLDOWN)
- SkillUpgradeManager 분리
- 경험치 소비 방식

---

**마지막 업데이트**: 2025-11-24
**버전**: 2.0
