package com.magicbr.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.magicbr.game.screens.SplashScreen;
import com.magicbr.game.utils.Assets;
import com.magicbr.game.utils.Client;
import com.magicbr.game.utils.FontManager;

public class MagicBattleRoyale extends Game {
    public static final int VIRTUAL_WIDTH = 1920;
    public static final int VIRTUAL_HEIGHT = 1080;

    private SpriteBatch batch;
    private AssetManager assetManager;
    private Assets assets;
    private Stage uiStage;
    private Client client;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        assets = new Assets(assetManager);
        uiStage = new Stage(new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

        Gdx.input.setInputProcessor(uiStage);

        assets.loadGameAssets();

        client = new Client();
        client.connect();

        setScreen(new SplashScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        if (client != null) client.disconnect();
        super.dispose();
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
        if (uiStage != null) uiStage.dispose();
        FontManager.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public Assets getAssets() {
        return assets;
    }

    public Stage getUiStage() {
        return uiStage;
    }

    public Client getClient() {
        return client;
    }
}
