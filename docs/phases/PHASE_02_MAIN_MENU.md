# PHASE_02_MAIN_MENU.md - 메인 메뉴 구현

---

## 🎯 목표
메인 메뉴 화면 구현 (시작/설정/종료 버튼)

---

## 📋 구현 범위

### 기능
- ✅ 메인 메뉴 UI (3개 버튼)
- ✅ 배경 이미지 (마법학교 테마)
- ✅ 게임 타이틀/로고
- ✅ 버튼 상호작용 (클릭 → 화면 전환)
- ✅ 설정 화면 기본 구성 (선택사항)

### 버튼 기능
| 버튼 | 동작 | 다음 화면 |
|------|------|---------|
| [시작] | 게임 시작 클릭 | PHASE_03: 네트워크 연결 |
| [설정] | 설정 화면 열기 | SettingsDialog |
| [종료] | 게임 종료 | System.exit(0) |

---

## 📁 필요 파일

### 생성할 파일
```
screens/
  ├─ MainMenuScreen.java (새로 생성)
  └─ SettingsDialog.java (선택사항)

ui/
  └─ ButtonComponent.java (재사용 가능)

assets/textures/
  └─ ui/main_menu_bg.png (배경)
```

---

## 🔧 구현 가이드

### MainMenuScreen 클래스
```java
public class MainMenuScreen implements IScreen {
    private Texture backgroundTexture;
    private List<Button> buttons = new ArrayList<>();
    private BitmapFont titleFont;

    @Override
    public void update(float delta) {
        // 버튼 입력 감지
        for (Button btn : buttons) {
            if (btn.isPressed()) {
                switch (btn.getId()) {
                    case "start":
                        // PHASE_03으로 (네트워크 연결)
                        screenManager.setScreen(new LobbyScreen());
                        break;
                    case "settings":
                        showSettingsDialog();
                        break;
                    case "exit":
                        Gdx.app.exit();
                        break;
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(backgroundTexture, 0, 0);

        // 타이틀 렌더링
        titleFont.draw(batch, "유급은 싫어", 540, 1500);

        // 버튼들 렌더링
        for (Button btn : buttons) {
            btn.render(batch);
        }
    }
}
```

---

## 🧪 테스트 계획

```
[ ] 메인 메뉴가 정상적으로 렌더링된다
    @Test
    public void 메인메뉴가_3개의_버튼을_표시한다() {
        MainMenuScreen screen = new MainMenuScreen();
        assertEquals(3, screen.getButtons().size());
    }

[ ] [시작] 버튼 클릭 시 LobbyScreen으로 전환
    @Test
    public void 시작_버튼_클릭_시_로비_화면으로_전환한다() {
        // 게임 실행 후 [시작] 클릭 확인
    }

[ ] [종료] 버튼 클릭 시 게임 종료
    @Test
    public void 종료_버튼_클릭_시_게임이_종료된다() {
        // 게임 실행 후 [종료] 클릭 확인
    }
```

---

## ✅ 완료 조건

- [ ] MainMenuScreen 클래스 구현
- [ ] 3개 버튼 구현 및 상호작용
- [ ] 배경 이미지 렌더링
- [ ] 각 버튼 기능 동작 확인
- [ ] 모든 테스트 통과

---

## 🔗 다음 Phase

**PHASE_03: 네트워크 기초 연결**

