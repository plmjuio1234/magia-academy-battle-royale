# PHASE_01_LOADING_SCREEN.md - 로딩 화면 구현

---

## 🎯 목표
실제 게임 리소스를 로드하고, 진행도를 표시하는 로딩 화면 구현

---

## 📋 구현 범위

### 구현할 기능
- ✅ 게임 리소스 로드 (텍스처, 폰트, 맵 데이터)
- ✅ 로딩 프로그레스 바 표시
- ✅ 로딩 팁 텍스트 무작위 표시
- ✅ 상태 메시지 표시 ("폰트 로딩 25%", "맵 로딩 60%" 등)
- ✅ 로드 완료 시 메인 메뉴로 전환

### 구현하지 않을 기능
- ❌ 서버 연결 (PHASE_03에서)
- ❌ 실제 에셋 (테스트용 더미 파일 사용)

---

## 📁 필요 파일

### 생성할 파일
```
android/src/main/java/com/example/yugeup/
├── screens/
│   ├── LoadingScreen.java          (새로 생성)
│   └── IScreen.java                (인터페이스)
├── utils/
│   ├── AssetManager.java           (새로 생성)
│   └── Constants.java              (수정 - 로딩 팁 추가)
└── YuGeupLauncher.java             (수정 - 로딩 화면으로 시작)

assets/
├── textures/
│   ├── characters/
│   ├── monsters/
│   ├── skills/
│   ├── ui/
│   └── maps/
├── fonts/
│   ├── NotoSansCJK.ttf
│   └── NotoSansCJK-Bold.ttf
└── data/
    ├── skills.json
    └── monsters.json
```

---

## 🔧 구현 가이드

### 1. IScreen 인터페이스
```java
// 모든 화면이 구현해야 할 인터페이스
public interface IScreen {
    void show();      // 화면 표시
    void hide();      // 화면 숨김
    void update(float delta);  // 매 프레임 업데이트
    void render(SpriteBatch batch);  // 렌더링
}
```

### 2. LoadingScreen 클래스
```java
public class LoadingScreen implements IScreen {
    private float loadingProgress = 0f;  // 0f ~ 1f
    private String statusMessage = "";
    private String tipText = "";

    @Override
    public void update(float delta) {
        // 1. AssetManager에서 진행도 가져오기
        // 2. 팁 텍스트 로드 (완료 시 보여주기)
        // 3. 진행도 100% 도달 시 MainMenuScreen으로 전환
    }

    @Override
    public void render(SpriteBatch batch) {
        // 1. 로고 중앙 렌더링
        // 2. 프로그레스 바 렌더링 (25% 등)
        // 3. 상태 메시지 렌더링 ("폰트 로딩 25%")
        // 4. 팁 텍스트 렌더링
    }
}
```

### 3. AssetManager 클래스
```java
public class AssetManager {
    private float loadingProgress = 0f;
    private boolean isLoaded = false;

    public void loadAssets() {
        // 1. 폰트 로드 (0~25%)
        loadingProgress = 0.25f;
        loadFont("fonts/NotoSansCJK.ttf");

        // 2. 텍스처 로드 (25~60%)
        loadingProgress = 0.60f;
        loadTexture("textures/characters/");
        loadTexture("textures/monsters/");

        // 3. 맵 데이터 로드 (60~100%)
        loadingProgress = 1.0f;
        loadMapData("data/maps.json");

        isLoaded = true;
    }

    public float getProgress() {
        return loadingProgress;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}
```

---

## 🧪 테스트 계획

### 단위 테스트
```
[ ] AssetManager가 진행도를 올바르게 반환한다
    @Test
    public void 로딩_진행도가_0f_에서_1f_로_증가한다() {
        AssetManager manager = new AssetManager();
        assertEquals(0f, manager.getProgress());
        manager.loadAssets();
        assertEquals(1.0f, manager.getProgress());
    }

[ ] LoadingScreen이 완료 후 다음 화면으로 전환한다
    @Test
    public void 로딩_완료_후_메인메뉴로_전환한다() {
        LoadingScreen screen = new LoadingScreen();
        screen.update(10f);  // 충분히 오래 기다리기
        assertTrue(screen.isComplete());
    }

[ ] 팁 텍스트가 무작위로 선택된다
    @Test
    public void 로딩_팁이_Constants에서_무작위_선택된다() {
        String tip = Constants.getRandomTip();
        assertNotNull(tip);
        assertTrue(tip.length() > 0);
    }
```

### 통합 테스트
```
[ ] 게임 시작 시 로딩 화면이 첫 번째로 보인다
[ ] 로딩 완료 후 메인 메뉴로 자동 전환된다
[ ] 로딩 중 화면을 누르면 계속 로딩된다 (스킵 불가)
```

---

## ✅ 완료 조건

- [ ] LoadingScreen 클래스 구현
- [ ] AssetManager 클래스 구현
- [ ] 프로그레스 바 렌더링
- [ ] 상태 메시지 표시
- [ ] 팁 텍스트 표시
- [ ] 모든 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] 로딩 완료 → MainMenuScreen으로 자동 전환 확인

---

## 🔗 다음 Phase 연결점

**PHASE_02_MAIN_MENU.md**:
- LoadingScreen 완료 후 MainMenuScreen으로 전환
- MainMenuScreen의 [시작] 버튼 → PHASE_03 네트워크 연결로

---

**참고**:
- UI/UX 상세: SPEC_UI_SCREENS.md > LoadingScreen
- 상수값: Constants.java 참조
- 구현 예시: 총정리.md의 로딩 화면 섹션

