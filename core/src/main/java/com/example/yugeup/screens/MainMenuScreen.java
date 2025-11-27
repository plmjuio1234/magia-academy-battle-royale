package com.example.yugeup.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.utils.AssetManager;
import com.example.yugeup.utils.Constants;
import com.example.yugeup.utils.Logger;
import com.example.yugeup.utils.ScreenTransition;

/**
 * 메인 메뉴 화면
 *
 * 게임 시작, 설정, 종료 버튼을 표시합니다.
 * 로딩 완료 후 처음 보이는 화면입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MainMenuScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Viewport (화면 비율 유지)
    private Viewport viewport;
    private OrthographicCamera camera;

    // 배경 이미지
    private Texture backgroundTexture;
    private Texture logoTexture;

    // 폰트
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    // 버튼 아틀라스
    private com.badlogic.gdx.graphics.g2d.TextureAtlas buttonAtlas;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion startButtonDefault;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion startButtonHover;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion settingButtonDefault;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion settingButtonHover;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion exitButtonDefault;
    private com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion exitButtonHover;

    // 화면 전환
    private ScreenTransition transition;
    private boolean transitionStarted;

    // 버튼 영역
    private Rectangle startButton;
    private Rectangle settingsButton;
    private Rectangle exitButton;

    // 버튼 상태 (hover)
    private boolean startButtonHovered;
    private boolean settingsButtonHovered;
    private boolean exitButtonHovered;

    /**
     * MainMenuScreen을 생성합니다.
     *
     * @param game 게임 인스턴스
     */
    public MainMenuScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        // 카메라와 뷰포트 설정 (가상 해상도 사용)
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        this.camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);

        // 버튼 영역 초기화 (오른쪽으로 이동)
        float buttonX = (Constants.SCREEN_WIDTH - Constants.MENU_BUTTON_WIDTH) / 2f + 500f;

        this.startButton = new Rectangle(
            buttonX,
            Constants.MENU_FIRST_BUTTON_Y,
            Constants.MENU_BUTTON_WIDTH,
            Constants.MENU_BUTTON_HEIGHT
        );

        this.settingsButton = new Rectangle(
            buttonX,
            Constants.MENU_FIRST_BUTTON_Y - Constants.MENU_BUTTON_HEIGHT - Constants.MENU_BUTTON_SPACING,
            Constants.MENU_BUTTON_WIDTH,
            Constants.MENU_BUTTON_HEIGHT
        );

        this.exitButton = new Rectangle(
            buttonX,
            Constants.MENU_FIRST_BUTTON_Y - (Constants.MENU_BUTTON_HEIGHT + Constants.MENU_BUTTON_SPACING) * 2,
            Constants.MENU_BUTTON_WIDTH,
            Constants.MENU_BUTTON_HEIGHT
        );

        // 화면 전환 초기화 (페이드 인)
        this.transition = new ScreenTransition();
        this.transitionStarted = false;

        Logger.info("메인 메뉴 화면 생성됨");
    }

    @Override
    public void show() {
        Logger.info("메인 메뉴 화면 표시 시작");

        // 에셋 매니저에서 리소스 가져오기
        AssetManager assetManager = AssetManager.getInstance();

        this.backgroundTexture = assetManager.getTexture("main_bg");
        this.logoTexture = assetManager.getTexture("logo");
        this.titleFont = assetManager.getFont("font_title");
        this.buttonFont = assetManager.getFont("font_large");

        // 버튼 아틀라스 로드
        this.buttonAtlas = assetManager.getAtlas("button");
        if (buttonAtlas != null) {
            // atlas 파일의 이름과 정확히 일치해야 함
            this.startButtonDefault = buttonAtlas.findRegion("start-button-defualt");  // atlas 파일의 오타 그대로
            this.startButtonHover = buttonAtlas.findRegion("start-button-hover");
            this.settingButtonDefault = buttonAtlas.findRegion("setting-button-defualt");  // atlas 파일의 오타 그대로
            this.settingButtonHover = buttonAtlas.findRegion("setting-button-hover");
            this.exitButtonDefault = buttonAtlas.findRegion("exit-button-defualt");  // atlas 파일의 오타 그대로
            this.exitButtonHover = buttonAtlas.findRegion("exit-button-hover");
            Logger.info("버튼 아틀라스 로드 완료");
        } else {
            Logger.warn("버튼 아틀라스를 찾을 수 없습니다.");
        }

        if (backgroundTexture == null) {
            Logger.warn("메인 배경 텍스처를 찾을 수 없습니다.");
        }
        if (logoTexture == null) {
            Logger.warn("로고 텍스처를 찾을 수 없습니다.");
        }
        if (titleFont == null) {
            Logger.warn("타이틀 폰트를 찾을 수 없습니다.");
        }
        if (buttonFont == null) {
            Logger.warn("버튼 폰트를 찾을 수 없습니다.");
        }

        // 로딩 화면에서 전환되었을 때 원형 확장 애니메이션 시작
        if (!transitionStarted) {
            transitionStarted = true;
            transition.start(ScreenTransition.TransitionType.CIRCLE_EXPAND, 0.8f);
            Logger.info("메인 메뉴 전환 애니메이션 시작 (CIRCLE_EXPAND)");
        }
    }

    @Override
    public void render(float delta) {
        // 화면 클리어 (검은색)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 카메라 업데이트 및 적용
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 전환 애니메이션 업데이트
        transition.update(delta);

        // 마우스 입력 처리
        handleInput();

        // 배경 렌더링
        batch.begin();
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        }
        batch.end();

        // 로고 렌더링
        renderLogo();

        // 버튼 렌더링
        renderButtons();

        // 전환 애니메이션 렌더링 (로딩 화면에서 전환 시)
        transition.render(camera);
    }

    /**
     * 입력을 처리합니다.
     */
    private void handleInput() {
        // 마우스 위치를 게임 좌표로 변환
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // 화면 좌표를 월드 좌표로 변환
        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(
            new com.badlogic.gdx.math.Vector3(mouseX, mouseY, 0)
        );

        // 버튼 hover 상태 업데이트
        startButtonHovered = startButton.contains(worldCoords.x, worldCoords.y);
        settingsButtonHovered = settingsButton.contains(worldCoords.x, worldCoords.y);
        exitButtonHovered = exitButton.contains(worldCoords.x, worldCoords.y);

        // 클릭 처리
        if (Gdx.input.justTouched()) {
            if (startButtonHovered) {
                Logger.info("[시작] 버튼 클릭됨 - 로비 화면으로 전환");
                game.setScreen(new LobbyScreen(game));

            } else if (settingsButtonHovered) {
                Logger.info("[설정] 버튼 클릭됨");
                // TODO: 설정 화면 구현
                Logger.info("설정 화면이 아직 구현되지 않았습니다.");

            } else if (exitButtonHovered) {
                Logger.info("[종료] 버튼 클릭됨");
                Gdx.app.exit();
            }
        }
    }

    /**
     * 버튼을 렌더링합니다.
     */
    private void renderButtons() {
        batch.begin();

        // 버튼 이미지가 로드되었으면 이미지 사용, 아니면 ShapeRenderer 사용
        if (startButtonDefault != null && startButtonHover != null &&
            settingButtonDefault != null && settingButtonHover != null &&
            exitButtonDefault != null && exitButtonHover != null) {

            // 시작 버튼 (이미지를 버튼 영역 크기에 맞게 스케일)
            batch.draw(
                startButtonHovered ? startButtonHover : startButtonDefault,
                startButton.x, startButton.y,
                startButton.width, startButton.height
            );

            // 설정 버튼
            batch.draw(
                settingsButtonHovered ? settingButtonHover : settingButtonDefault,
                settingsButton.x, settingsButton.y,
                settingsButton.width, settingsButton.height
            );

            // 종료 버튼
            batch.draw(
                exitButtonHovered ? exitButtonHover : exitButtonDefault,
                exitButton.x, exitButton.y,
                exitButton.width, exitButton.height
            );
        }

        batch.end();
    }

    /**
     * 로고를 렌더링합니다.
     */
    private void renderLogo() {
        if (logoTexture != null) {
            batch.begin();

            // 로고를 오른쪽으로 이동
            float logoX = (Constants.SCREEN_WIDTH - Constants.MENU_LOGO_WIDTH) / 2f + 500f;
            batch.draw(logoTexture, logoX, Constants.MENU_LOGO_Y,
                      Constants.MENU_LOGO_WIDTH, Constants.MENU_LOGO_HEIGHT);

            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        // 뷰포트 업데이트 (화면 크기 변경 시 자동으로 비율 유지)
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // 일시정지 처리 (필요 시)
    }

    @Override
    public void resume() {
        // 재개 처리 (필요 시)
    }

    @Override
    public void hide() {
        Logger.info("메인 메뉴 화면 숨김");
    }

    @Override
    public void dispose() {
        Logger.info("메인 메뉴 화면 리소스 해제");

        if (batch != null) {
            batch.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (transition != null) {
            transition.dispose();
        }

        // 텍스처와 폰트는 AssetManager가 관리하므로 여기서 dispose하지 않음
    }
}
