# TEST_PLAN.md - 전체 테스트 전략

---

## 🎯 목표
유급은 싫어 게임의 전체 테스트 계획 및 전략

---

## 📋 테스트 분류

### 1. 단위 테스트 (Unit Test)
- 개별 클래스 및 메서드 테스트
- JUnit 5 사용
- 각 Phase별 테스트 코드 포함

### 2. 통합 테스트 (Integration Test)
- 여러 컴포넌트 간 상호작용 테스트
- 네트워크 메시지 송수신
- 클라이언트-서버 통신

### 3. UI 테스트
- 화면 전환 테스트
- 버튼 클릭 동작
- 다이얼로그 표시

### 4. 게임플레이 테스트
- 스킬 시전 및 효과
- 전투 시스템
- 동기화

---

## 🔧 테스트 환경

### 필요 라이브러리
```gradle
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.9.0'
    testImplementation 'org.mockito:mockito-core:4.8.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:4.8.0'
}
```

### 테스트 디렉토리 구조
```
android/src/test/java/com/example/yugeup/
├── game/
│   ├── TestPlayer.java
│   ├── TestMonster.java
│   ├── TestSkill.java
│   └── TestCombat.java
├── network/
│   ├── TestNetworkManager.java
│   └── TestMessageHandler.java
├── ui/
│   └── TestScreens.java
└── utils/
    └── TestMathUtils.java
```

---

## 📊 테스트 커버리지 목표

- **전체 코드 커버리지**: 70% 이상
- **핵심 로직 커버리지**: 90% 이상
  - Player, Monster, Skill
  - CombatSystem
  - NetworkManager

---

## 🧪 테스트 케이스 우선순위

### P0 (최우선) - 게임 핵심 기능
- 플레이어 생성 및 이동
- 스킬 시전
- 몬스터 스폰 및 AI
- 전투 및 데미지 계산
- 네트워크 동기화

### P1 (높음) - 주요 기능
- 레벨업 시스템
- 스킬 업그레이드
- 맵 축소
- 게임 결과

### P2 (보통) - 부가 기능
- UI 애니메이션
- 이펙트
- 사운드

---

## ✅ 테스트 체크리스트

### Phase별 테스트
- [ ] PHASE_01 ~ PHASE_12 (기존 테스트)
- [ ] PHASE_13: 원소 선택
- [ ] PHASE_14~18: 5원소 스킬 (각 3개씩)
- [ ] PHASE_19: 스킬 업그레이드
- [ ] PHASE_20: 몬스터 렌더링
- [ ] PHASE_21: 몬스터 동기화
- [ ] PHASE_22: 전투 시스템
- [ ] PHASE_23: 플레이어 동기화
- [ ] PHASE_24: 맵 축소
- [ ] PHASE_25: PVP 전투
- [ ] PHASE_26: 게임 결과
- [ ] PHASE_27: 이펙트
- [ ] PHASE_28: 최적화

---

## 📝 테스트 리포트 형식

```
테스트 일자: 2025-XX-XX
테스트 대상: PHASE_XX
실행한 테스트: XX개
성공: XX개
실패: XX개
커버리지: XX%

주요 이슈:
1. ...
2. ...
```

---

## 🔗 다음 문서
- TEST_NETWORK.md - 네트워크 테스트
- TEST_UI.md - UI 테스트
- TEST_GAMEPLAY.md - 게임플레이 테스트
