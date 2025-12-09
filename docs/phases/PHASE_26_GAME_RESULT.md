# PHASE_26_GAME_RESULT.md - 게임 종료 및 결과

---

## 🎯 목표
게임 종료 조건 및 결과 화면 구현

---

## 📋 구현 범위

- ✅ 게임 종료 조건 (10분 경과 또는 1명 생존)
- ✅ 순위 계산
- ✅ 결과 화면 UI
- ✅ 통계 표시

---

## 🔧 구현 가이드

### 1. GameResult 클래스

```java
public class GameResult {
    private int playerRank;             // 순위
    private int totalPlayers;           // 전체 플레이어 수
    private int kills;                  // 처치 수
    private int damageDealt;            // 준 데미지
    private int damageTaken;            // 받은 데미지
    private int survivalTime;           // 생존 시간(초)

    public GameResult(Player player) {
        this.playerRank = calculateRank(player);
        this.totalPlayers = GameManager.getInstance().getTotalPlayerCount();
        this.kills = player.getStats().getKills();
        this.damageDealt = player.getStats().getTotalDamageDealt();
        this.damageTaken = player.getStats().getTotalDamageTaken();
        this.survivalTime = (int) player.getSurvivalTime();
    }

    private int calculateRank(Player player) {
        List<Player> allPlayers = GameManager.getInstance().getAllPlayers();
        allPlayers.sort((a, b) -> Integer.compare(b.getStats().getScore(), a.getStats().getScore()));

        for (int i = 0; i < allPlayers.size(); i++) {
            if (allPlayers.get(i).getId() == player.getId()) {
                return i + 1;
            }
        }
        return allPlayers.size();
    }
}
```

### 2. ResultScreen 클래스

```java
public class ResultScreen implements IScreen {
    private GameResult result;
    private Stage stage;
    private Skin skin;

    public ResultScreen(GameResult result) {
        this.result = result;
        this.stage = new Stage();
        this.skin = new Skin();

        setupUI();
    }

    private void setupUI() {
        Table table = new Table();
        table.setFillParent(true);

        // 순위 표시
        Label rankLabel = new Label("순위: " + result.getPlayerRank() + " / " + result.getTotalPlayers(), skin);
        rankLabel.setFontScale(2.0f);
        table.add(rankLabel).pad(20).row();

        // 통계 표시
        table.add(new Label("처치: " + result.getKills(), skin)).pad(10).row();
        table.add(new Label("준 데미지: " + result.getDamageDealt(), skin)).pad(10).row();
        table.add(new Label("받은 데미지: " + result.getDamageTaken(), skin)).pad(10).row();
        table.add(new Label("생존 시간: " + formatTime(result.getSurvivalTime()), skin)).pad(10).row();

        // 확인 버튼
        TextButton confirmButton = new TextButton("확인", skin);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.getInstance().setScreen(new MainMenuScreen());
            }
        });
        table.add(confirmButton).size(200, 60).pad(20);

        stage.addActor(table);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    @Override
    public void render(SpriteBatch batch) {
        stage.act();
        stage.draw();
    }
}
```

---

## ✅ 완료 조건

- [ ] GameResult 클래스 구현
- [ ] ResultScreen UI 구현
- [ ] 순위 계산 확인
- [ ] 통계 표시 확인

---

## 🔗 다음 Phase

**PHASE_27: 이펙트 시스템**
