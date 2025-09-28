package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.utils.FontManager;
import com.magicbr.game.utils.ScreenTransition;
import com.magicbr.game.utils.UIHelper;

public class MenuScreen extends BaseScreen {
    private Table table;
    private ScreenTransition transition;
    private ShapeRenderer shapeRenderer;
    private boolean transitionStarted = false;
    private Texture backgroundTexture;
    private SpriteBatch spriteBatch;

    public MenuScreen(MagicBattleRoyale game) {
        super(game);
        transition = new ScreenTransition();
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        // 배경 이미지 로드 (PNG만 지원)
        String[] imagePaths = {
            "images/backgrounds/main.png",
            "main.png",
            "main_converted.png"
        };

        boolean loaded = false;
        for (String path : imagePaths) {
            try {
                if (Gdx.files.internal(path).exists()) {
                    backgroundTexture = new Texture(Gdx.files.internal(path));
                    Gdx.app.log("MenuScreen", path + " 로드 성공!");
                    loaded = true;
                    break;
                }
            } catch (Exception e) {
                Gdx.app.log("MenuScreen", path + " 로드 실패: " + e.getMessage());
            }
        }

        // 로컬 파일도 확인 (main_converted.png)
        if (!loaded) {
            try {
                if (Gdx.files.local("main_converted.png").exists()) {
                    backgroundTexture = new Texture(Gdx.files.local("main_converted.png"));
                    Gdx.app.log("MenuScreen", "main_converted.png 로드 성공!");
                    loaded = true;
                }
            } catch (Exception e) {
                Gdx.app.log("MenuScreen", "main_converted.png 로드 실패: " + e.getMessage());
            }
        }

        if (!loaded) {
            loadBackupImage();
        }
    }

    private void loadBackupImage() {
        // 백업으로 libGDX 기본 이미지 사용
        try {
            backgroundTexture = new Texture(Gdx.files.internal("libgdx.png"));
            Gdx.app.log("MenuScreen", "백업 이미지(libgdx.png) 사용");
        } catch (Exception e4) {
            backgroundTexture = null;
            Gdx.app.log("MenuScreen", "백업 이미지 로드도 실패");
        }
    }

    @Override
    protected void create() {
        Gdx.app.log("MenuScreen", "MenuScreen constructor completed");
    }

    @Override
    public void show() {
        super.show();

        Gdx.app.log("MenuScreen", "Creating menu screen UI");

        table = new Table();
        table.setFillParent(true);

        // UI 스타일 사용
        Label.LabelStyle titleStyle = UIHelper.createTitleStyle();
        TextButton.TextButtonStyle buttonStyle = UIHelper.createPrimaryButtonStyle();

        // 플랫폼별 스케일링 계수
        boolean isAndroid = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;
        float titleScale = isAndroid ? 1.8f : 2.2f;
        float subtitleScale = isAndroid ? 1.0f : 1.2f;
        float buttonScale = isAndroid ? 1.0f : 1.4f;

        // 제목 (플랫폼별 크기)
        Label titleLabel = new Label("마도학원 배틀로얄", titleStyle);
        titleLabel.setFontScale(titleScale);

        // 부제목 (플랫폼별 크기)
        Label subtitleLabel = new Label("Magia Academy Battle Royale", UIHelper.createSubtitleStyle());
        subtitleLabel.setFontScale(subtitleScale);

        // 버튼들 (플랫폼별 크기)
        TextButton playButton = new TextButton("게임 시작", buttonStyle);
        playButton.getLabel().setFontScale(buttonScale);
        TextButton settingsButton = new TextButton("설정", buttonStyle);
        settingsButton.getLabel().setFontScale(buttonScale);
        TextButton exitButton = new TextButton("종료", buttonStyle);
        exitButton.getLabel().setFontScale(buttonScale);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!transitionStarted) {
                    transitionStarted = true;
                    transition.startClosing();
                    Gdx.app.log("MenuScreen", "화면 전환 시작");
                }
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MenuScreen", "설정 클릭됨");
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // 플랫폼별 버튼 크기 및 간격
        float buttonWidth = isAndroid ? 420 : 280;
        float buttonHeight = isAndroid ? 100 : 70;
        float padBottom = isAndroid ? 40 : 30;
        float titlePadBottom = isAndroid ? 35 : 25;
        float subtitlePadBottom = isAndroid ? 120 : 100;

        // 레이아웃 (중앙 정렬 및 간격 조정) - 플랫폼별 크기
        table.center();
        table.add(titleLabel).padBottom(titlePadBottom).row();
        table.add(subtitleLabel).padBottom(subtitlePadBottom).row();
        table.add(playButton).width(buttonWidth).height(buttonHeight).padBottom(padBottom).row();
        table.add(settingsButton).width(buttonWidth).height(buttonHeight).padBottom(padBottom).row();
        table.add(exitButton).width(buttonWidth).height(buttonHeight);

        game.getUiStage().addActor(table);

        Gdx.app.log("MenuScreen", "Menu screen UI created successfully - actors count: " + game.getUiStage().getActors().size);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.3f, 1f);

        // 배경 이미지 렌더링
        if (backgroundTexture != null) {
            spriteBatch.setProjectionMatrix(game.getUiStage().getCamera().combined);
            spriteBatch.begin();
            spriteBatch.draw(backgroundTexture, 0, 0, game.getUiStage().getWidth(), game.getUiStage().getHeight());
            spriteBatch.end();
        }

        // UI 렌더링
        game.getUiStage().act(delta);
        game.getUiStage().draw();

        // 전환 효과 업데이트 및 렌더링
        if (transitionStarted) {
            transition.update(delta);

            shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
            transition.render(shapeRenderer, game.getUiStage().getWidth(), game.getUiStage().getHeight());

            // 전환이 완료되면 새 화면으로 이동
            if (transition.isClosingComplete()) {
                game.setScreen(new RoomListScreen(game));
            }
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        super.dispose();
    }
}