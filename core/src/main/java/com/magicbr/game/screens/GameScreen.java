package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.entities.Player;
import com.magicbr.game.ui.VirtualJoystick;
import com.magicbr.game.utils.Constants;
import com.magicbr.game.utils.FontManager;

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

    public GameScreen(MagicBattleRoyale game, Constants.CharacterClass playerClass) {
        super(game);
        this.playerClass = playerClass;

        // 게임 카메라 초기화
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);

        // 플레이어 중앙에 생성
        player = new Player(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();

        // HUD 테이블 생성
        hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left();

        // 한글 폰트 사용
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

        // 임시 종료 버튼
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

        // 가상 조이스틱 추가 (모바일용)
        virtualJoystick = new VirtualJoystick(Constants.JOYSTICK_SIZE);
        virtualJoystick.setPosition(Constants.HUD_PADDING, Constants.HUD_PADDING);
        game.getUiStage().addActor(virtualJoystick);
    }

    @Override
    public void render(float delta) {
        gameTimer += delta;

        // 입력 처리
        handleInput(delta);

        // 플레이어 업데이트
        player.update(delta);

        // 카메라를 플레이어를 따라 이동
        gameCamera.position.x = player.getPosition().x;
        gameCamera.position.y = player.getPosition().y;
        gameCamera.update();

        // 게임 배경 (어두운 녹색으로 게임 느낌)
        ScreenUtils.clear(0.1f, 0.3f, 0.1f, 1f);

        // 게임 월드 렌더링
        renderGameWorld();

        // 가상 조이스틱 렌더링 (UI보다 먼저)
        renderVirtualJoystick();

        // HUD 업데이트
        updateHUD();

        // UI 렌더링
        game.getUiStage().act(delta);
        game.getUiStage().draw();
    }

    private void handleInput(float delta) {
        float moveX = 0, moveY = 0;

        // 키보드 입력 (데스크탑용)
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

        // 가상 조이스틱 입력 (모바일용)
        if (virtualJoystick.isTouched()) {
            moveX = virtualJoystick.getKnobPercentX();
            moveY = virtualJoystick.getKnobPercentY();
        }

        // 대각선 이동 시 속도 정규화 (키보드만)
        if (!virtualJoystick.isTouched() && moveX != 0 && moveY != 0) {
            moveX *= 0.707f;
            moveY *= 0.707f;
        }

        player.move(moveX, moveY);

        // 원소 선택 키 (1-5)
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

        // 맵 경계 그리기
        drawMapBounds();

        // 플레이어 렌더링
        player.render(shapeRenderer);

        shapeRenderer.end();
    }

    private void drawMapBounds() {
        // 맵 테두리
        shapeRenderer.setColor(Color.DARK_GRAY);

        // 경계선 두께
        float borderThickness = 50f;

        // 위쪽 경계
        shapeRenderer.rect(0, Constants.MAP_HEIGHT - borderThickness, Constants.MAP_WIDTH, borderThickness);
        // 아래쪽 경계
        shapeRenderer.rect(0, 0, Constants.MAP_WIDTH, borderThickness);
        // 왼쪽 경계
        shapeRenderer.rect(0, 0, borderThickness, Constants.MAP_HEIGHT);
        // 오른쪽 경계
        shapeRenderer.rect(Constants.MAP_WIDTH - borderThickness, 0, borderThickness, Constants.MAP_HEIGHT);

        // 맵 내부 바닥
        shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(borderThickness, borderThickness,
                          Constants.MAP_WIDTH - 2 * borderThickness,
                          Constants.MAP_HEIGHT - 2 * borderThickness);
    }

    private void renderVirtualJoystick() {
        // UI Stage 카메라로 조이스틱 렌더링
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