package com.example.yugeup.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.network.MessageHandler;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.network.RoomManager;
import org.example.Main.*;

import com.example.yugeup.ui.dialog.NameInputDialog;
import com.example.yugeup.ui.lobby.CharacterPreview;
import com.example.yugeup.ui.lobby.RoomListPanel;
import com.example.yugeup.ui.dialog.CreateRoomDialog;
import com.example.yugeup.utils.Constants;
import com.example.yugeup.utils.UIDebugger;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 로비 화면
 *
 * 방 목록을 표시하고 방 생성/참가 기능을 제공합니다.
 * 서버로부터 방 목록을 받아와 실시간으로 업데이트합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LobbyScreen implements Screen {

    // 게임 인스턴스
    private Game game;

    // 렌더링 도구
    private SpriteBatch batch;

    // Viewport (화면 비율 유지)
    private Viewport viewport;
    private OrthographicCamera camera;

    // 배경
    private Texture backgroundTexture;

    // UI 패널
    private RoomListPanel roomListPanel;
    private CharacterPreview characterPreview;

    // 버튼 아틀라스
    private TextureAtlas buttonAtlas;

    // 다이얼로그
    private CreateRoomDialog createRoomDialog;
    private ShapeRenderer shapeRenderer;

    // 폰트
    private BitmapFont font;

    // 네트워크
    private NetworkManager networkManager;
    private MessageHandler messageHandler;
    private RoomManager roomManager;

    // 자동 새로고침 타이머
    private float refreshTimer;

    // 마우스 좌표 변환용
    private Vector3 worldCoords;

    private NameInputDialog nameInputDialog;
    private String currentPlayerName = "";  // 현재 플레이어 이름 저장

    /**
     * LobbyScreen 생성자
     *
     * @param game 게임 인스턴스
     */
    public LobbyScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();

        this.refreshTimer = 0f;

        // 카메라와 뷰포트 설정 (가상 해상도 사용)
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        this.camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);

        // 마우스 좌표 변환용
        this.worldCoords = new Vector3();

        // 네트워크 매니저 인스턴스 가져오기
        this.networkManager = NetworkManager.getInstance();
        this.messageHandler = MessageHandler.getInstance();
        this.roomManager = RoomManager.getInstance();

        // ShapeRenderer 초기화
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {

        // 에셋 매니저에서 리소스 가져오기
        com.example.yugeup.utils.AssetManager assetManager = com.example.yugeup.utils.AssetManager.getInstance();

        // 배경 텍스처 로드
        backgroundTexture = assetManager.getTexture("lobby_bg");

        // 폰트 로드
        this.font = assetManager.getFont("font_small");

        // 버튼 로드
        this.buttonAtlas = assetManager.getAtlas("button");
        if (buttonAtlas != null) {
            System.out.println("[LobbyScreen] 버튼 아틀라스 로드 성공");
        } else {
            System.err.println("[LobbyScreen] 버튼 아틀라스 로드 실패!");
        }

        // UI 패널 초기화 (UIDebugger 값 사용)
        roomListPanel = new RoomListPanel(
            com.example.yugeup.utils.UIDebugger.getLobbyRoomListX(),
            com.example.yugeup.utils.UIDebugger.getLobbyRoomListY(),
            com.example.yugeup.utils.UIDebugger.getLobbyRoomListWidth(),
            com.example.yugeup.utils.UIDebugger.getLobbyRoomListHeight(),
            font, buttonAtlas
        );

        characterPreview = new CharacterPreview(
            com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewX(),
            com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewY(),
            com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewWidth(),
            com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewHeight(),
            font, buttonAtlas
        );

        // 이름 입력 다이얼로그 초기화
        nameInputDialog = new NameInputDialog(font, assetManager.getFont("font_title"));

        // 방 생성 다이얼로그 초기화
        createRoomDialog = new CreateRoomDialog(font, assetManager.getFont("font_title"));

        // RoomManager에 Game 인스턴스 설정 (화면 전환용)
        roomManager.setGame(game);

        // 캐릭터 텍스처 설정 (아틀라스에서 정면 프레임 가져오기)
        TextureAtlas characterAtlas = assetManager.getAtlas("character");
        if (characterAtlas != null) {
            TextureAtlas.AtlasRegion characterRegion = characterAtlas.findRegion("character-front-0");
            if (characterRegion != null) {
                characterPreview.setCharacterRegion(characterRegion);
                System.out.println("[LobbyScreen] 캐릭터 정면 프레임 로드 완료");
            } else {
                System.err.println("[LobbyScreen] character-front-0 리전을 찾을 수 없음");
            }
        } else {
            System.err.println("[LobbyScreen] 캐릭터 아틀라스가 로드되지 않음");
        }

        // 서버 연결 확인
        if (!networkManager.isConnected()) {
            System.out.println("[LobbyScreen] 서버에 연결 중...");
            networkManager.connect(Constants.SERVER_HOST, Constants.SERVER_PORT);

            // 연결 완료 대기 (비동기 연결)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 방 목록 요청
        requestRoomList();

        System.out.println("[LobbyScreen] 로비 화면 표시");
    }

    @Override
    public void render(float delta) {
        // 화면 클리어
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 카메라 업데이트
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // 배경 렌더링
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.end();

        // UI 패널 렌더링
        batch.begin();
        roomListPanel.render(batch);
        characterPreview.render(batch);
        batch.end();

        // 방 생성 다이얼로그 렌더링 (UI 위에 표시)
        if (createRoomDialog.isVisible()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            createRoomDialog.render(batch, shapeRenderer);
        }

        // 이름 입력 다이얼로그 렌더링 (UI 위에 표시)
        if (nameInputDialog.isVisible()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            nameInputDialog.render(batch, shapeRenderer);
        }

        // UI 디버거 렌더링 (F1 키로 활성화)
        UIDebugger.render(batch, font);

        // 입력 처리
        UIDebugger.handleInput();
        handleInput();

        // 자동 새로고침 (5초마다)
        refreshTimer += delta;
        if (refreshTimer >= Constants.LOBBY_REFRESH_INTERVAL) {
            refreshTimer = 0f;
            requestRoomList();
        }

        // 네트워크 메시지 처리
        processNetworkMessages();
    }

    /**
     * 입력을 처리합니다.
     */
    private void handleInput() {
        // 이름 입력 다이얼로그가 표시 중이면 다이얼로그 입력만 처리
        if (nameInputDialog.isVisible()) {
            if (Gdx.input.justTouched()) {
                worldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(worldCoords, viewport.getScreenX(), viewport.getScreenY(),
                    viewport.getScreenWidth(), viewport.getScreenHeight());

                nameInputDialog.handleInput(worldCoords.x, worldCoords.y);
            } else {
                nameInputDialog.handleInput(0, 0);
            }
            return;
        }

        // 다이얼로그가 표시 중이면 다이얼로그 입력만 처리
        if (createRoomDialog.isVisible()) {
            if (Gdx.input.justTouched()) {
                // 화면 좌표를 월드 좌표로 변환
                worldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(worldCoords, viewport.getScreenX(), viewport.getScreenY(),
                    viewport.getScreenWidth(), viewport.getScreenHeight());

                createRoomDialog.handleInput(worldCoords.x, worldCoords.y);
            } else {
                // 키보드 입력만 처리 (터치 없을 때)
                createRoomDialog.handleInput(0, 0);
            }
            return;
        }

        if (Gdx.input.justTouched()) {
            // 화면 좌표를 월드 좌표로 변환
            worldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(worldCoords, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());

            float touchX = worldCoords.x;
            float touchY = worldCoords.y;

            // [새로고침] 버튼
            if (roomListPanel.isRefreshButtonClicked(touchX, touchY)) {
                System.out.println("[LobbyScreen] 새로고침 버튼 클릭");
                requestRoomList();
            }

            // [방 만들기] 버튼
            else if (roomListPanel.isCreateButtonClicked(touchX, touchY)) {
                System.out.println("[LobbyScreen] 방 만들기 버튼 클릭");

                // 이름이 설정되어 있으면 먼저 서버에 전송
                if (!currentPlayerName.isEmpty()) {
                    NetworkManager.SetPlayerNameMsg nameMsg = new NetworkManager.SetPlayerNameMsg();
                    nameMsg.playerName = currentPlayerName;
                    networkManager.sendTCP(nameMsg);

                    // 서버 처리 대기
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                createRoomDialog.show();
            }

            // [타이틀로] 버튼
            else if (roomListPanel.isBackButtonClicked(touchX, touchY)) {
                System.out.println("[LobbyScreen] 타이틀로 버튼 클릭");
                game.setScreen(new MainMenuScreen(game));
            }

            // [참가] 버튼
            else {
                int roomId = roomListPanel.getJoinedRoomId(touchX, touchY);
                if (roomId != -1) {
                    System.out.println("[LobbyScreen] 방 참가 요청: roomId=" + roomId);
                    roomManager.joinRoom(roomId);
                }
            }

            // [외형변경] 버튼
            if (characterPreview.isCustomizeButtonClicked(touchX, touchY)) {
                System.out.println("[LobbyScreen] 외형변경 버튼 클릭 - 현재 닉네임 설정 역할 진행중... 이름 설정 다이얼로그 표시");
                nameInputDialog.show(currentPlayerName, new NameInputDialog.NameInputCallback() {
                    @Override
                    public void onNameSet(String name) {
                        currentPlayerName = name;
                        characterPreview.setNickname(name);
                        System.out.println("[LobbyScreen] 플레이어 이름 설정: " + name);
                        // 서버에 이름 전송
                        NetworkManager.SetPlayerNameMsg msg = new NetworkManager.SetPlayerNameMsg();
                        msg.playerName = name;
                        networkManager.sendTCP(msg);
                    }
                });
            }
        }
    }

    /**
     * 서버에 방 목록을 요청합니다.
     */
    private void requestRoomList() {
        if (networkManager.isConnected()) {
            networkManager.sendTCP(new GetRoomListMsg());
            System.out.println("[LobbyScreen] 방 목록 요청 전송");
        } else {
            System.out.println("[LobbyScreen] 서버 연결 안됨 - 방 목록 요청 실패");
        }
    }

    /**
     * 네트워크 메시지를 처리합니다.
     */
    private void processNetworkMessages() {
        // RoomListResponse 처리
        RoomListResponse roomListResponse = messageHandler.pollRoomListResponse();
        if (roomListResponse != null) {
            onRoomListReceived(roomListResponse.rooms);
        }

        // CreateRoomResponse 처리
        CreateRoomResponse createRoomResponse = messageHandler.pollCreateRoomResponse();
        if (createRoomResponse != null) {
            roomManager.onCreateRoomResponse(createRoomResponse);
        }

        // JoinRoomResponse 처리
        JoinRoomResponse joinRoomResponse = messageHandler.pollJoinRoomResponse();
        if (joinRoomResponse != null) {
            roomManager.onJoinRoomResponse(joinRoomResponse);
        }
    }

    /**
     * 방 목록 수신 처리
     *
     * @param rooms 방 목록
     */
    private void onRoomListReceived(RoomInfo[] rooms) {
        roomListPanel.setRooms(rooms);
        System.out.println("[LobbyScreen] 방 목록 수신: " + (rooms != null ? rooms.length : 0) + "개");
    }

    @Override
    public void resize(int width, int height) {
        // 뷰포트 업데이트 (화면 비율 유지)
        viewport.update(width, height);
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
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
        // 화면 숨김 처리 (필요 시)
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (roomListPanel != null) {
            roomListPanel.dispose();
        }
        if (characterPreview != null) {
            characterPreview.dispose();
        }
        if (nameInputDialog != null) {
            // NameInputDialog는 dispose 메서드 없음 (폰트는 AssetManager에서 관리)
        }
        System.out.println("[LobbyScreen] 리소스 해제 완료");
    }
}
