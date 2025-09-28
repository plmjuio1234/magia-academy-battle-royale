package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.utils.GameTips;
import com.magicbr.game.utils.FontManager;

public class SplashScreen extends BaseScreen {
    private Table table;
    private float splashTimer = 0f;
    private static final float SPLASH_DURATION = 3f;
    private boolean transitioned = false;
    private ShapeRenderer shapeRenderer;
    private String currentTip;

    public SplashScreen(MagicBattleRoyale game) {
        super(game);
        shapeRenderer = new ShapeRenderer();
        currentTip = GameTips.getRandomTip();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();
        Gdx.app.log("SplashScreen", "스플래시 화면 생성 중");

        // FontManager 강제 리셋 (재시작 시 문제 해결)
        FontManager.reset();

        table = new Table();
        table.setFillParent(true);

        BitmapFont koreanFont = FontManager.getKoreanFont();
        BitmapFont koreanFontLarge = FontManager.getKoreanFontLarge();
        Label.LabelStyle labelStyle = new Label.LabelStyle(koreanFont, Color.WHITE);
        Label.LabelStyle logoStyle = new Label.LabelStyle(koreanFontLarge, Color.GOLD);

        // 화면을 3등분하여 레이아웃 구성
        Table topTable = new Table(); // 로고 영역
        Table middleTable = new Table(); // 로딩바 영역
        Table bottomTable = new Table(); // 팁 영역

        topTable.center();
        middleTable.center();
        bottomTable.center();

        // 로고 (상단 영역)
        Label titleLabel = new Label("마도학원 배틀로얄", logoStyle);
        titleLabel.setFontScale(1.3f);
        topTable.add(titleLabel).center();

        // 부제목 추가
        Label subtitleLabel = new Label("Magia Academy Battle Royale", labelStyle);
        subtitleLabel.setFontScale(0.8f);
        subtitleLabel.setColor(Color.CYAN);
        topTable.row();
        topTable.add(subtitleLabel).center().padTop(15);

        // 로딩바는 별도 렌더링이므로 여기서는 공간만 확보
        Label spacerLabel = new Label("", labelStyle);
        spacerLabel.setColor(Color.CLEAR);
        middleTable.add(spacerLabel).height(50);

        // 팁 (하단 영역)
        Label tipHeaderLabel = new Label("🔮 마법사의 조언", labelStyle);
        tipHeaderLabel.setFontScale(0.7f);
        tipHeaderLabel.setColor(Color.GOLD);
        bottomTable.add(tipHeaderLabel).center().row();

        Label tipLabel = new Label(currentTip, labelStyle);
        tipLabel.setWrap(true);
        tipLabel.setFontScale(0.8f);
        tipLabel.setAlignment(1); // 중앙 정렬
        bottomTable.add(tipLabel).width(600).center().padTop(10);

        // 전체 레이아웃: 상단-중단-하단 순서로 배치 (완전히 중앙 정렬)
        table.center();
        table.add(topTable).expandX().fillX().height(300).center().row();
        table.add(middleTable).expandX().fillX().height(120).center().row();
        table.add(bottomTable).expandX().fillX().height(180).center();

        game.getUiStage().addActor(table);

        Gdx.app.log("SplashScreen", "스플래시 화면 생성 완료");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1f);

        splashTimer += delta;

        // 로딩바 그리기
        drawLoadingBar(splashTimer / SPLASH_DURATION);

        // UI 렌더링
        game.getUiStage().act(delta);
        game.getUiStage().draw();

        // 3초 후 MenuScreen으로 전환
        if (splashTimer >= SPLASH_DURATION && !transitioned) {
            transitioned = true;
            Gdx.app.log("SplashScreen", "메인 메뉴로 전환");

            // 에셋 로딩 완료 처리
            game.getAssets().finishLoading();

            // MenuScreen으로 전환
            game.setScreen(new MenuScreen(game));
            return;
        }
    }

    private void drawLoadingBar(float progress) {
        if (progress > 1f) progress = 1f;

        shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);

        // 로딩바는 중간 섹션에 맞춰 배치 (화면을 3등분한 중간 부분)
        float barWidth = 500f;
        float barHeight = 25f;
        float barX = (game.getUiStage().getWidth() - barWidth) / 2f;

        // 화면을 3등분하여 중간 섹션에 명확히 위치
        float screenHeight = game.getUiStage().getHeight();

        // 화면을 3등분: 상단(로고) - 중간(로딩바) - 하단(팁)
        float sectionHeight = screenHeight / 3f;
        float middleSectionY = screenHeight - sectionHeight - (sectionHeight / 2f);

        // 로딩바를 중간 섹션 중앙보다 더 아래에 배치
        float barY = middleSectionY - (barHeight / 2f) - 130f;

        // 로딩바 그림자
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.4f);
        shapeRenderer.rect(barX + 3, barY - 3, barWidth, barHeight);

        // 로딩바 배경 (어두운 테두리)
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(barX - 3, barY - 3, barWidth + 6, barHeight + 6);

        // 로딩바 배경
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // 로딩바 진행 (그라데이션 효과)
        shapeRenderer.setColor(0.2f, 0.6f, 1.0f, 1f);
        shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);

        // 로딩바 하이라이트
        shapeRenderer.setColor(0.4f, 0.8f, 1.0f, 0.7f);
        shapeRenderer.rect(barX, barY + barHeight * 0.7f, barWidth * progress, barHeight * 0.3f);

        // 로딩바 광택 효과
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.3f);
        shapeRenderer.rect(barX + 5, barY + barHeight * 0.8f, (barWidth * progress) - 10, barHeight * 0.15f);

        shapeRenderer.end();

        // 로딩바 테두리
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.8f, 0.8f, 0.9f, 1.0f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        super.dispose();
    }
}