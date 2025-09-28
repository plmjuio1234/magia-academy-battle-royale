package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;

public class TestMenuScreen implements Screen {
    private MagicBattleRoyale game;
    private BitmapFont font;

    public TestMenuScreen(MagicBattleRoyale game) {
        this.game = game;

        Gdx.app.log("TestMenuScreen", "Creating test menu screen");

        // 폰트 생성 및 확인
        font = new BitmapFont();
        Gdx.app.log("TestMenuScreen", "Font created - valid: " + (font != null));

        if (font.getData() != null) {
            Gdx.app.log("TestMenuScreen", "Font data exists");
        } else {
            Gdx.app.log("TestMenuScreen", "Font data is NULL!");
        }

        Gdx.app.log("TestMenuScreen", "TestMenuScreen constructor completed");
    }

    @Override
    public void show() {
        Gdx.app.log("TestMenuScreen", "Test menu screen shown");
        Gdx.app.log("TestMenuScreen", "Screen size: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
        Gdx.app.log("TestMenuScreen", "ViewPort size: " + game.getUiStage().getViewport().getWorldWidth() + "x" + game.getUiStage().getViewport().getWorldHeight());

        // 이제 Stage가 준비되었으니 Label 생성하고 추가
        Label.LabelStyle whiteStyle = new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE);
        Label titleLabel = new Label("TEST MENU SCREEN", whiteStyle);
        titleLabel.setFontScale(8f);
        titleLabel.setPosition(640 - titleLabel.getPrefWidth() / 2, 400);

        Label.LabelStyle cyanStyle = new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.CYAN);
        Label instructionLabel = new Label("If you see this, UI is working!", cyanStyle);
        instructionLabel.setFontScale(6f);
        instructionLabel.setPosition(640 - instructionLabel.getPrefWidth() / 2, 300);

        // Stage에 추가
        game.getUiStage().addActor(titleLabel);
        game.getUiStage().addActor(instructionLabel);

        Gdx.app.log("TestMenuScreen", "Labels added in show() method");
        Gdx.app.log("TestMenuScreen", "Title label: " + titleLabel.getWidth() + "x" + titleLabel.getHeight() + " at " + titleLabel.getX() + "," + titleLabel.getY());

        // Stage 업데이트 후 actors count 확인
        game.getUiStage().act(0.016f);
        Gdx.app.log("TestMenuScreen", "Stage updated - actors count: " + game.getUiStage().getActors().size);
    }

    @Override
    public void render(float delta) {
        // 배경색 설정
        ScreenUtils.clear(0.2f, 0.2f, 0.8f, 1f);

        // Stage 업데이트 및 그리기
        game.getUiStage().act(delta);
        game.getUiStage().draw();

        // 직접 SpriteBatch로도 텍스트 그려보기 - 매우 크고 눈에 띄게
        game.getBatch().begin();
        font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        font.getData().setScale(4f);
        font.draw(game.getBatch(), "DIRECT BATCH TEXT - BIG!", 50, 650);

        font.setColor(com.badlogic.gdx.graphics.Color.MAGENTA);
        font.getData().setScale(3f);
        font.draw(game.getBatch(), "Screen: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight(), 50, 600);

        font.setColor(com.badlogic.gdx.graphics.Color.ORANGE);
        font.getData().setScale(5f);
        font.draw(game.getBatch(), "ORANGE BIG TEXT", 50, 400);
        game.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        game.getUiStage().getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.app.log("TestMenuScreen", "Test menu screen hidden - clearing stage");
        game.getUiStage().clear();
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
    }
}