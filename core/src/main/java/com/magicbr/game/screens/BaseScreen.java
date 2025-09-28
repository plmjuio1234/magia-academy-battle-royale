package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.utils.Constants;

public abstract class BaseScreen implements Screen {
    protected final MagicBattleRoyale game;
    protected OrthographicCamera camera;
    protected Viewport viewport;

    public BaseScreen(MagicBattleRoyale game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);

        create();
    }

    protected abstract void create();

    @Override
    public void show() {
        // 입력 프로세서를 이 화면의 Stage로 설정
        if (game.getUiStage() != null) {
            Gdx.input.setInputProcessor(game.getUiStage());
        }
    }

    @Override
    public void render(float delta) {
        // 기본 렌더링은 각 화면에서 구현
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        if (game.getUiStage() != null) {
            game.getUiStage().getViewport().update(width, height, true);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        // 화면이 숨겨질 때 Stage의 모든 액터 제거
        if (game.getUiStage() != null) {
            game.getUiStage().clear();
        }
    }

    @Override
    public void dispose() {

    }
}