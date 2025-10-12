package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.entities.Player;
import com.magicbr.game.ui.VirtualJoystick;
import com.magicbr.game.utils.Client;
import com.magicbr.game.utils.Constants;
import com.magicbr.game.utils.FontManager;

import java.util.HashMap;
import java.util.Map;

public class GameScreen extends BaseScreen {
    private Constants.CharacterClass playerClass;
    private Table hudTable;
    private Label levelLabel;
    private Label elementLabel;
    private Label hpLabel;
    private Label mpLabel;
    private float gameTimer = 0f;

    private Player player;
    private OrthographicCamera gameCamera;
    private ShapeRenderer shapeRenderer;
    private VirtualJoystick virtualJoystick;

    private Client client;
    private float syncTimer = 0;
    private Map<Integer, OtherPlayer> otherPlayers = new HashMap<>();

    static class OtherPlayer {
        int id;
        float x, y;

        OtherPlayer(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        void render(ShapeRenderer shapeRenderer) {
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.circle(x, y, 30);

            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.circle(x, y, 20);
        }
    }

    public GameScreen(MagicBattleRoyale game, Constants.CharacterClass playerClass) {
        super(game);
        this.playerClass = playerClass;
        this.client = game.getClient();

        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);

        player = new Player(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();

        hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left();

        FontManager.initialize();
        BitmapFont koreanFont = FontManager.getKoreanFont();

        Label.LabelStyle labelStyle = new Label.LabelStyle(koreanFont, Color.WHITE);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = koreanFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.GRAY;

        levelLabel = new Label("레벨: " + player.getLevel(), labelStyle);
        elementLabel = new Label("원소: " + player.getSelectedElement(), labelStyle);
        hpLabel = new Label("HP: " + player.getHp() + "/" + player.getMaxHp(), labelStyle);
        mpLabel = new Label("MP: " + player.getMp() + "/" + player.getMaxMp(), labelStyle);

        TextButton exitButton = new TextButton("메뉴로 돌아가기", buttonStyle);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        hudTable.add(levelLabel).pad(10).row();
        hudTable.add(elementLabel).pad(10).row();
        hudTable.add(hpLabel).pad(10).row();
        hudTable.add(mpLabel).pad(10).row();
        hudTable.add(exitButton).pad(20);

        game.getUiStage().addActor(hudTable);

        virtualJoystick = new VirtualJoystick(Constants.JOYSTICK_SIZE);
        virtualJoystick.setPosition(Constants.HUD_PADDING, Constants.HUD_PADDING);
        game.getUiStage().addActor(virtualJoystick);
    }

    @Override
    public void render(float delta) {
        gameTimer += delta;

        handleInput(delta);

        player.update(delta);

        syncPlayerPosition(delta);

        updateOtherPlayers();

        gameCamera.position.x = player.getPosition().x;
        gameCamera.position.y = player.getPosition().y;
        gameCamera.update();

        ScreenUtils.clear(0.1f, 0.3f, 0.1f, 1f);

        renderGameWorld();

        renderVirtualJoystick();

        updateHUD();

        game.getUiStage().act(delta);
        game.getUiStage().draw();
    }

    private void syncPlayerPosition(float delta) {
        syncTimer += delta;
        if (syncTimer >= 0.05f) {
            Vector2 pos = player.getPosition();
            client.sendPlayerMove(pos.x, pos.y);
            syncTimer = 0;
        }
    }

    private void updateOtherPlayers() {
        Client.PlayerMoveMsg moveMsg = client.getLatestPlayerMove();
        if (moveMsg != null) {
            OtherPlayer other = otherPlayers.get(moveMsg.playerId);
            if (other == null) {
                other = new OtherPlayer(moveMsg.playerId, moveMsg.x, moveMsg.y);
                otherPlayers.put(moveMsg.playerId, other);
                Gdx.app.log("GameScreen", "새 플레이어 추가: " + moveMsg.playerId);
            } else {
                other.x = moveMsg.x;
                other.y = moveMsg.y;
            }
        }
    }

    private void handleInput(float delta) {
        float moveX = 0, moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = 1;
        }

        if (virtualJoystick.isTouched()) {
            moveX = virtualJoystick.getKnobPercentX();
            moveY = virtualJoystick.getKnobPercentY();
        }

        if (!virtualJoystick.isTouched() && moveX != 0 && moveY != 0) {
            moveX *= 0.707f;
            moveY *= 0.707f;
        }

        player.move(moveX, moveY);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            player.selectElement("불");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            player.selectElement("물");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            player.selectElement("땅");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            player.selectElement("전기");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            player.selectElement("바람");
        }
    }

    private void renderGameWorld() {
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawMapBounds();

        player.render(shapeRenderer);

        for (OtherPlayer other : otherPlayers.values()) {
            other.render(shapeRenderer);
        }

        shapeRenderer.end();
    }

    private void drawMapBounds() {
        shapeRenderer.setColor(Color.DARK_GRAY);

        float borderThickness = 50f;

        shapeRenderer.rect(0, Constants.MAP_HEIGHT - borderThickness, Constants.MAP_WIDTH, borderThickness);
        shapeRenderer.rect(0, 0, Constants.MAP_WIDTH, borderThickness);
        shapeRenderer.rect(0, 0, borderThickness, Constants.MAP_HEIGHT);
        shapeRenderer.rect(Constants.MAP_WIDTH - borderThickness, 0, borderThickness, Constants.MAP_HEIGHT);

        shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(borderThickness, borderThickness,
            Constants.MAP_WIDTH - 2 * borderThickness,
            Constants.MAP_HEIGHT - 2 * borderThickness);
    }

    private void renderVirtualJoystick() {
        shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        virtualJoystick.render(shapeRenderer);
        shapeRenderer.end();
    }

    private void updateHUD() {
        levelLabel.setText("레벨: " + player.getLevel());
        elementLabel.setText("원소: " + player.getSelectedElement() + " (1-5로 변경)");
        hpLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        mpLabel.setText("MP: " + player.getMp() + "/" + player.getMaxMp());
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (virtualJoystick != null) {
            virtualJoystick.dispose();
        }
        super.dispose();
    }
}
