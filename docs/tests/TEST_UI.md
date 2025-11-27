# TEST_UI.md - UI 테스트

---

## 🎯 목표
화면 전환, UI 컴포넌트 상호작용 테스트

---

## 📋 테스트 항목

### 1. 화면 전환 테스트
- [ ] LoadingScreen → MainMenuScreen
- [ ] MainMenuScreen → LobbyScreen
- [ ] LobbyScreen → WaitingRoomScreen
- [ ] WaitingRoomScreen → GameScreen
- [ ] GameScreen → ResultScreen

### 2. UI 컴포넌트 테스트
- [ ] 버튼 클릭
- [ ] 다이얼로그 표시/숨김
- [ ] 입력 필드
- [ ] 리스트 스크롤

### 3. HUD 테스트
- [ ] HP 바 표시
- [ ] 스킬 버튼
- [ ] 조이스틱
- [ ] 킬 로그

---

## 🔧 테스트 코드 예시

### 화면 전환 테스트

```java
@Test
public void 로딩화면에서_메인메뉴로_전환() {
    LoadingScreen loadingScreen = new LoadingScreen();
    loadingScreen.show();

    // 리소스 로드 완료 시뮬레이션
    loadingScreen.onLoadingComplete();

    // 메인 메뉴로 전환 확인
    IScreen currentScreen = ScreenManager.getInstance().getCurrentScreen();
    assertTrue(currentScreen instanceof MainMenuScreen);
}

@Test
public void 게임화면에서_결과화면으로_전환() {
    Player player = new Player(1);
    GameScreen gameScreen = new GameScreen(player);
    gameScreen.show();

    // 게임 종료
    gameScreen.endGame();

    // 결과 화면 전환 확인
    IScreen currentScreen = ScreenManager.getInstance().getCurrentScreen();
    assertTrue(currentScreen instanceof ResultScreen);
}
```

### UI 컴포넌트 테스트

```java
@Test
public void 버튼_클릭_동작() {
    MainMenuScreen mainMenu = new MainMenuScreen();
    TextButton startButton = mainMenu.getStartButton();

    // 클릭 이벤트 시뮬레이션
    InputEvent event = new InputEvent();
    event.setType(InputEvent.Type.touchDown);
    startButton.fire(event);

    // 화면 전환 확인
    IScreen currentScreen = ScreenManager.getInstance().getCurrentScreen();
    assertTrue(currentScreen instanceof LobbyScreen);
}

@Test
public void 다이얼로그_표시() {
    Stage mockStage = new Stage();
    Skin mockSkin = new Skin();

    Dialog dialog = new Dialog("테스트", mockSkin);
    dialog.text("메시지");
    dialog.button("확인");

    dialog.show(mockStage);

    assertTrue(dialog.isVisible());
}

@Test
public void 원소_선택_다이얼로그() {
    Stage mockStage = new Stage();
    Skin mockSkin = new Skin();

    ElementSelectDialog dialog = new ElementSelectDialog(mockStage, mockSkin);
    dialog.show();

    // 원소 선택
    dialog.onElementSelected(ElementType.FIRE);
    dialog.onConfirmSelection();

    Player player = GameManager.getInstance().getLocalPlayer();
    assertEquals(ElementType.FIRE, player.getSelectedElement());
}
```

### HUD 테스트

```java
@Test
public void HP바_업데이트() {
    Player player = new Player(1);
    player.getStats().setHealth(100);

    HPBarComponent hpBar = new HPBarComponent(player);

    // 체력 감소
    player.takeDamage(30);
    hpBar.update(0.016f);

    // HP 바 표시 확인
    assertEquals(0.7f, hpBar.getHealthRatio(), 0.01f);
}

@Test
public void 스킬_버튼_쿨타임() {
    Player player = new Player(1);
    ElementalSkill skill = new Fireball();

    SkillButtonComponent button = new SkillButtonComponent(skill);

    // 스킬 시전
    skill.cast(player, new Vector2(100, 100));

    // 쿨타임 중 버튼 비활성화
    button.update(0.016f);
    assertFalse(button.isEnabled());

    // 쿨타임 경과 후 활성화
    button.update(skill.getCooldown());
    assertTrue(button.isEnabled());
}

@Test
public void 조이스틱_입력() {
    JoystickController joystick = new JoystickController(1080, 1920);

    // 터치 다운
    joystick.onTouchDown(100, 1900, 0);
    assertTrue(joystick.isDragging());

    // 드래그
    joystick.onTouchDragged(150, 1850, 0);

    // 방향 벡터 확인
    Vector2 direction = joystick.getDirection();
    assertTrue(direction.len() > 0);
}

@Test
public void 킬로그_메시지_추가() {
    KillLogComponent killLog = new KillLogComponent();

    killLog.addKill("플레이어1", "몬스터");
    killLog.addKill("플레이어2", "플레이어3");

    // 최대 5개까지 표시
    for (int i = 0; i < 10; i++) {
        killLog.addKill("Killer" + i, "Victim" + i);
    }

    assertEquals(5, killLog.getMessageCount());
}
```

---

## ✅ 테스트 체크리스트

### 화면 관리
- [ ] 모든 화면 생성
- [ ] 화면 전환
- [ ] 화면 리소스 해제

### 버튼 및 입력
- [ ] 버튼 클릭
- [ ] 롱 프레스
- [ ] 더블 클릭
- [ ] 입력 필드

### 다이얼로그
- [ ] 표시/숨김
- [ ] 확인/취소 버튼
- [ ] 모달 동작

### HUD 컴포넌트
- [ ] HP/MP 바
- [ ] 스킬 버튼
- [ ] 조이스틱
- [ ] 미니맵

---

## 📊 UI 성능 테스트

```java
@Test
public void UI_렌더링_성능() {
    Stage stage = new Stage();

    // 100개 UI 요소 추가
    for (int i = 0; i < 100; i++) {
        Label label = new Label("Label " + i, new Skin());
        stage.addActor(label);
    }

    long startTime = System.currentTimeMillis();

    // 렌더링
    stage.act(0.016f);
    stage.draw();

    long endTime = System.currentTimeMillis();
    long renderTime = endTime - startTime;

    // 16ms 이하 (60fps 유지)
    assertTrue(renderTime < 16);
}
```

---

## 🔗 관련 문서
- PHASE_02_MAIN_MENU.md
- PHASE_04_LOBBY_UI.md
- PHASE_13_ELEMENT_SELECT.md
