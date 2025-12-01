package com.example.yugeup.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.network.MessageHandler;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.network.RoomManager;
import com.example.yugeup.utils.AssetManager;
import com.example.yugeup.utils.Constants;
import com.example.yugeup.utils.UIDebugger;
import org.example.Main.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 대기실 화면
 *
 * 방에 참가한 플레이어 목록을 표시하고 게임 시작을 기다립니다.
 * 방장은 게임 시작 버튼을 사용할 수 있습니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class WaitingRoomScreen implements Screen {

    private Game game;
    private RoomInfo roomInfo;
    private List<PlayerInfo> players;

    // 렌더링
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Viewport viewport;

    // 폰트
    private BitmapFont font;
    private BitmapFont titleFont;
    private com.badlogic.gdx.graphics.g2d.GlyphLayout glyphLayout;

    // 배경
    private Texture backgroundTexture;

    // 캐릭터 아틀라스
    private TextureAtlas characterAtlas;
    private TextureRegion characterFront;

    // 버튼 아틀라스
    private TextureAtlas buttonAtlas;
    private TextureRegion gameStartButtonDefault;
    private TextureRegion gameStartButtonHover;
    private TextureRegion readyButtonDefault;
    private TextureRegion readyButtonHover;
    private TextureRegion exitButtonDefault;
    private TextureRegion exitButtonHover;

    // 채팅 관련
    private List<String> chatMessages = new ArrayList<>();  // 최근 10개 메시지
    private String chatInput = "";  // 현재 입력 중인 텍스트
    private static final int MAX_CHAT_MESSAGES = 10;
    private Rectangle sendButtonBounds;  // 전송 버튼 영역
    private Rectangle chatInputBounds;   // 입력창 영역
    private boolean chatInputFocused = false;  // 입력창 포커스 여부

    /**
     * WaitingRoomScreen 생성자
     *
     * @param game Game 인스턴스
     * @param roomInfo 현재 방 정보
     * @param players 현재 방의 플레이어 목록
     */
    public WaitingRoomScreen(Game game, RoomInfo roomInfo, List<PlayerInfo> players) {
        this.game = game;
        this.roomInfo = roomInfo;
        this.players = players;
    }

    @Override
    public void show() {
        // 렌더링 초기화
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        glyphLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        // 에셋 로드
        AssetManager assetManager = AssetManager.getInstance();
        font = assetManager.getFont("font_medium");  // 채팅창용: font_small (36px) 대신 font_medium (48px) 사용으로 스케일 최소화
        titleFont = assetManager.getFont("font_title");
        backgroundTexture = assetManager.getTexture("waitroom_bg");

        // 배경이 없으면 로비 배경 사용
        if (backgroundTexture == null) {
            backgroundTexture = assetManager.getTexture("lobby_bg");
        }

        characterAtlas = assetManager.getAtlas("character");
        characterFront = characterAtlas.findRegion("character-front-0");

        // 버튼 아틀라스 로드
        buttonAtlas = assetManager.getAtlas("button");
        gameStartButtonDefault = buttonAtlas.findRegion("game-start-button-defualt");
        gameStartButtonHover = buttonAtlas.findRegion("game-start-button-hover");
        readyButtonDefault = buttonAtlas.findRegion("ready-button-defualt");
        readyButtonHover = buttonAtlas.findRegion("ready-button-hover");
        // 버튼 이름 수정: room-exit-button 사용 (atlas 오타: defualt)
        exitButtonDefault = buttonAtlas.findRegion("room-exit-button-defualt");
        exitButtonHover = buttonAtlas.findRegion("room-exit-button-hover");

        // 채팅 UI 영역 초기화
        float BG_SCALE = Constants.SCREEN_WIDTH / 500f;
        float chatX = Constants.WAITROOM_CHAT_OFFSET_X * BG_SCALE;
        float chatY = Constants.WAITROOM_CHAT_OFFSET_Y * BG_SCALE;
        float chatWidth = Constants.WAITROOM_CHAT_WIDTH_PX * BG_SCALE;
        float chatHeight = Constants.WAITROOM_CHAT_HEIGHT_PX * BG_SCALE;

        // 전송 버튼 영역 (우측 하단) - 크기 줄임, 높이 감소
        float sendButtonWidth = 40f * BG_SCALE;
        float sendButtonHeight = 18f * BG_SCALE;  // 25f → 18f로 더 감소
        sendButtonBounds = new Rectangle(
            chatX + chatWidth - sendButtonWidth - 5f * BG_SCALE,
            chatY + 5f * BG_SCALE,
            sendButtonWidth,
            sendButtonHeight
        );

        // 채팅 입력창 영역 (전송 버튼 왼쪽) - 높이 감소
        chatInputBounds = new Rectangle(
            chatX + 5f * BG_SCALE,
            chatY + 5f * BG_SCALE,
            chatWidth - sendButtonWidth - 15f * BG_SCALE,
            sendButtonHeight  // 동일한 높이
        );

        // InputProcessor 등록 (문자 입력 처리)
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!chatInputFocused) return false;
                if (chatInput.length() < 50) {
                    chatInput += character;
                }
                return true;
            }
        });

        System.out.println("[WaitingRoomScreen] 대기실 화면 초기화 완료");
        System.out.println("[WaitingRoomScreen] 방: " + roomInfo.roomName + " (" + players.size() + "/" + roomInfo.maxPlayers + ")");
    }

    @Override
    public void render(float delta) {
        // 배경 클리어
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // RoomUpdateMsg 처리
        handleRoomUpdate();

        // ChatMsg 처리
        handleChatMessage();

        // GameStartNotification 처리 (렌더링 전에 체크하여 화면 전환 시 즉시 반환)
        if (checkGameStart()) {
            return;  // 게임 시작 시 즉시 반환하여 렌더링 스킵
        }

        // 뷰포트 적용
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        // 배경 렌더링
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.end();

        // 타이틀 렌더링 제거 (배경 이미지에 포함되어 있음)

        // 플레이어 슬롯 렌더링 (상단 4칸)
        renderPlayerSlots();

        // 채팅 영역 렌더링 (PHASE_06에서 구현 예정)
        renderChatArea();

        // 버튼 렌더링 (우측 하단)
        renderButtons();

        // UI 디버거 렌더링 (F1 키로 활성화)
        UIDebugger.render(batch, font);

        // 입력 처리
        UIDebugger.handleInput();
        handleInput();
    }

    /**
     * RoomUpdateMsg를 처리하여 플레이어 목록을 업데이트합니다.
     */
    private void handleRoomUpdate() {
        RoomUpdateMsg msg = MessageHandler.getInstance().pollRoomUpdateMsg();
        if (msg != null) {
            players.clear();
            if (msg.players != null) {
                for (PlayerInfo player : msg.players) {
                    players.add(player);
                }
            }
            System.out.println("[WaitingRoomScreen] 플레이어 목록 업데이트: " + players.size() + "명");
        }
    }

    /**
     * ChatMsg를 처리하여 채팅 메시지를 업데이트합니다.
     */
    private void handleChatMessage() {
        ChatMsg msg = MessageHandler.getInstance().pollChatMsg();
        if (msg != null) {
            // 메시지 추가
            String fullMessage = msg.sender + ": " + msg.text;
            chatMessages.add(fullMessage);

            // 최대 10개까지만 유지
            if (chatMessages.size() > MAX_CHAT_MESSAGES) {
                chatMessages.remove(0);
            }

            System.out.println("[WaitingRoomScreen] 채팅 메시지 수신: " + fullMessage);
        }
    }

    /**
     * GameStartNotification을 체크하여 게임 화면으로 전환합니다.
     *
     * @return 게임 시작 시 true, 아니면 false
     */
    private boolean checkGameStart() {
        GameStartNotification notification = MessageHandler.getInstance().pollGameStartNotification();
        if (notification != null) {
            System.out.println("[WaitingRoomScreen] 게임 시작 알림 수신! startTime: " + notification.startTime);
            System.out.println("[WaitingRoomScreen] GameScreen으로 전환합니다.");

            // notification.players 사용 (스폰 위치 포함)
            java.util.List<PlayerInfo> playersWithSpawn = null;
            if (notification.players != null) {
                playersWithSpawn = java.util.Arrays.asList(notification.players);
                System.out.println("[WaitingRoomScreen] 서버에서 받은 플레이어 스폰 정보:");
                for (PlayerInfo p : playersWithSpawn) {
                    System.out.println("  - " + p.playerName + " → (" + p.spawnX + ", " + p.spawnY + ")");
                }
            } else {
                playersWithSpawn = players;  // fallback
            }

            // GameScreen으로 전환 (스폰 위치 포함된 players 사용)
            game.setScreen(new GameScreen(game, roomInfo, playersWithSpawn));
            this.dispose();

            System.out.println("[WaitingRoomScreen] GameScreen 전환 완료!");
            return true;
        }
        return false;
    }

    /**
     * 플레이어 슬롯을 상단 4칸으로 렌더링합니다.
     */
    private void renderPlayerSlots() {
        // UI 디버거 활성화 시 디버거 값 사용, 아니면 Constants 값 사용
        float slotOffsetX = UIDebugger.getWaitroomSlotOffsetX();
        float slotOffsetY = UIDebugger.getWaitroomSlotOffsetY();
        float slotSpacing = UIDebugger.getWaitroomSlotSpacing();
        float BG_SCALE = Constants.SCREEN_WIDTH / 500f;

        float slotWidth = UIDebugger.getWaitroomSlotWidth() * BG_SCALE;
        float slotHeight = UIDebugger.getWaitroomSlotHeight() * BG_SCALE;
        float slotStartX = slotOffsetX * BG_SCALE;
        float slotY = Constants.SCREEN_HEIGHT - (slotOffsetY * BG_SCALE) - slotHeight;

        for (int i = 0; i < 4; i++) {
            float x = slotStartX + i * (slotWidth + (slotSpacing * BG_SCALE));
            float y = slotY;

            // 슬롯 배경 (UI 디버거 활성화 시 와이어프레임 표시)
            if (UIDebugger.isEnabled()) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(1f, 0f, 0f, 0.5f);  // 빨간 테두리
                shapeRenderer.rect(x, y, slotWidth, slotHeight);
                shapeRenderer.end();
            }

            // 플레이어 정보 렌더링
            batch.begin();
            if (i < players.size()) {
                PlayerInfo player = players.get(i);

                // 캐릭터 스프라이트 (이 부분은 변경 없음)
                float charX = x + (slotWidth - Constants.WAITROOM_CHARACTER_SIZE) / 2f;
                float charY = y + 50f;
                batch.draw(characterFront, charX, charY, Constants.WAITROOM_CHARACTER_SIZE, Constants.WAITROOM_CHARACTER_SIZE);

                // 플레이어 이름 (상단 중앙)
                font.setColor(Color.BLACK);
                String playerName = player.playerName;
                if (player.isHost) {
                    playerName += " (방장)";
                }

                // [수정] GlyphLayout을 사용하여 정확한 중앙 정렬
                glyphLayout.setText(font, playerName);
                float textX = x + (slotWidth - glyphLayout.width) / 2f;
                font.draw(batch, playerName, textX, y + slotHeight - 50f);

            } else {
                // 빈 자리
                font.setColor(Color.GRAY);
                String emptyText = "빈 자리";

                // [수정] GlyphLayout을 사용하여 가로 및 세로 중앙 정렬
                glyphLayout.setText(font, emptyText);
                float textX = x + (slotWidth - glyphLayout.width) / 2f; // 가로 중앙
                float textY = y + (slotHeight + glyphLayout.height) / 2f; // 세로 중앙
                font.draw(batch, emptyText, textX, textY);
            }
            batch.end();
        }
    }

    /**
     * 채팅 영역을 렌더링합니다.
     */
    private void renderChatArea() {
        // UI 디버거 활성화 시 디버거 값 사용
        float chatOffsetX = UIDebugger.getWaitroomChatOffsetX();
        float chatOffsetY = UIDebugger.getWaitroomChatOffsetY();
        float BG_SCALE = Constants.SCREEN_WIDTH / 500f;

        float chatX = chatOffsetX * BG_SCALE;
        float chatY = chatOffsetY * BG_SCALE;
        float chatWidth = UIDebugger.getWaitroomChatWidth() * BG_SCALE;
        float chatHeight = UIDebugger.getWaitroomChatHeight() * BG_SCALE;

        // 채팅 배경 (UI 디버거 활성화 시 와이어프레임 표시)
        if (UIDebugger.isEnabled()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0f, 1f, 0f, 0.5f);  // 녹색 테두리
            shapeRenderer.rect(chatX, chatY, chatWidth, chatHeight);
            shapeRenderer.end();
        }

        batch.begin();

        // 원본 폰트 스케일 저장
        float originalFontScale = font.getScaleX();

        // 채팅 메시지 영역 (상단) - 폰트 스케일 조정
        float messageAreaHeight = chatHeight - 50f * BG_SCALE;
        float messageStartY = chatY + chatHeight - 10f * BG_SCALE;
        float lineHeight = 12f * BG_SCALE;  // 메시지 간격 축소 (20f → 12f)

        // 채팅 메시지 표시 (최신 메시지가 아래쪽) - font_medium (48px) 사용으로 스케일 조정
        font.getData().setScale(0.5f);  // 48px * 0.5f = 24px (합리적 크기)
        font.setColor(Color.BLACK);  // 흰색 → 검은색으로 변경 (배경에 보이도록)
        int displayCount = Math.min(chatMessages.size(), (int)(messageAreaHeight / lineHeight));
        int startIndex = Math.max(0, chatMessages.size() - displayCount);

        for (int i = startIndex; i < chatMessages.size(); i++) {
            int displayIndex = i - startIndex;
            String message = chatMessages.get(i);
            font.draw(batch, message, chatX + 8f * BG_SCALE,
                     messageStartY - (displayIndex * lineHeight));
        }

        batch.end();

        // 채팅 입력창 배경 (하단) - 밝은 회색, 높은 투명도, 둥근 모서리
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.4f, 0.4f, 0.45f, 0.45f);  // 연한 회색, 높은 투명도 (0.45 = 55% 투명)
        // 둥근 모서리 입력창 (반경 8px)
        renderRoundedRect(shapeRenderer, chatInputBounds.x, chatInputBounds.y,
                         chatInputBounds.width, chatInputBounds.height, 8f);
        shapeRenderer.end();

        batch.begin();

        // 입력 텍스트 - font_medium (48px) 사용으로 스케일 조정
        font.getData().setScale(0.55f);  // 48px * 0.55f = 26px (입력창용 적절한 크기)
        font.setColor(chatInputFocused ? Color.WHITE : Color.LIGHT_GRAY);
        String displayText = chatInput.isEmpty() && !chatInputFocused ? "메시지를 입력하세요..." : chatInput;
        font.draw(batch, displayText, chatInputBounds.x + 8f * BG_SCALE,
                 chatInputBounds.y + chatInputBounds.height * 0.55f);

        batch.end();

        // 마우스 호버 체크
        com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(
            Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        boolean sendButtonHovered = sendButtonBounds.contains(mousePos.x, mousePos.y);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (sendButtonHovered) {
            shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 1f);  // 호버 시 밝은 파란색
        } else {
            shapeRenderer.setColor(0.2f, 0.4f, 0.7f, 1f);  // 기본 파란색
        }
        // 둥근 모서리 전송 버튼 (반경 6px)
        renderRoundedRect(shapeRenderer, sendButtonBounds.x, sendButtonBounds.y,
                         sendButtonBounds.width, sendButtonBounds.height, 6f);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(0.5f);  // 전송 버튼 텍스트 (48px * 0.5f = 24px)
        font.setColor(Color.WHITE);
        String sendButtonText = "전송";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, sendButtonText);
        font.draw(batch, sendButtonText,
                 sendButtonBounds.x + (sendButtonBounds.width - layout.width) / 2,
                 sendButtonBounds.y + sendButtonBounds.height * 0.55f);

        // 원본 폰트 스케일로 복원
        font.getData().setScale(originalFontScale);
        batch.end();
    }

    /**
     * 우측 하단 버튼들을 렌더링합니다.
     * 마우스 위치에 따라 호버 효과를 적용합니다.
     */
    private void renderButtons() {
        // UI 디버거 활성화 시 디버거 값 사용
        float buttonOffsetX = UIDebugger.getWaitroomButtonOffsetX();
        float buttonOffsetY = UIDebugger.getWaitroomButtonOffsetY();
        float BG_SCALE = Constants.SCREEN_WIDTH / 500f;

        float x = buttonOffsetX * BG_SCALE;
        float y = buttonOffsetY * BG_SCALE;

        // 마우스 좌표 계산 (뷰포트 좌표계로 변환)
        // unproject()를 사용하여 화면 좌표를 게임 월드 좌표로 변환
        com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        float mouseX = mousePos.x;
        float mouseY = mousePos.y;

        batch.begin();

        // [방 나가기] 버튼 (항상 표시, 하단) - room-exit-button은 92x32이므로 일반 버튼 크기로 렌더링
        boolean exitHovered = isButtonHovered(mouseX, mouseY, x, y,
                                              Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT);
        TextureRegion exitTexture = exitHovered ? exitButtonHover : exitButtonDefault;
        if (exitTexture != null) {
            batch.draw(exitTexture, x, y,
                      Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT);
        }

        // [게임 시작] 또는 [준비] 버튼 (방 나가기 버튼 위)
        float topButtonY = y + Constants.WAITROOM_BUTTON_HEIGHT + Constants.WAITROOM_BUTTON_SPACING;
        boolean topButtonHovered = isButtonHovered(mouseX, mouseY, x, topButtonY,
                                                   Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT);

        boolean isHost = isCurrentPlayerHost();
        if (isHost) {
            // 방장: [게임 시작] 버튼
            TextureRegion gameStartTexture = topButtonHovered ? gameStartButtonHover : gameStartButtonDefault;
            if (gameStartTexture != null) {
                batch.draw(gameStartTexture, x, topButtonY,
                          Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT);
            }
        } else {
            // 일반 플레이어: [준비] 버튼
            TextureRegion readyTexture = topButtonHovered ? readyButtonHover : readyButtonDefault;
            if (readyTexture != null) {
                batch.draw(readyTexture, x, topButtonY,
                          Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT);
            }
        }

        batch.end();
    }

    /**
     * 현재 플레이어가 방장인지 확인합니다.
     *
     * @return 현재 플레이어가 방장이면 true, 아니면 false
     */
    private boolean isCurrentPlayerHost() {
        int myId = NetworkManager.getInstance().getCurrentPlayerId();
        if (myId == -1) {
            return false;  // 아직 플레이어 ID가 할당되지 않음
        }

        // 현재 플레이어 ID와 방장 ID를 비교
        for (PlayerInfo player : players) {
            if (player.isHost && player.playerId == myId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 키보드 입력을 처리합니다. (한글 지원)
     *
     * 폰트에서 지원하는 모든 문자 입력을 처리합니다.
     */
    private void handleKeyboardInput() {
        if (!chatInputFocused) return;

        // 백스페이스 처리
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (chatInput.length() > 0) {
                chatInput = chatInput.substring(0, chatInput.length() - 1);
            }
        }

        // 엔터 키 (전송)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            sendChatMessage();
        }

        // ESC (포커스 해제)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            chatInputFocused = false;
        }

        // 최대 길이 제한 (50자)
        if (chatInput.length() > 50) {
            chatInput = chatInput.substring(0, 50);
        }
    }

    /**
     * 한글을 포함한 문자 입력 처리
     * render() 메서드에서 호출되어 모든 입력 문자를 받습니다.
     */
    private void handleTextInput() {
        if (!chatInputFocused) return;

        // 입력 상자가 활성화되면 한글 입력기 띄우기 (Android, iOS 대응)
        // PC에서는 일반 키보드 입력으로 처리됨
    }

    /**
     * 채팅 메시지를 전송합니다.
     */
    private void sendChatMessage() {
        if (chatInput.trim().isEmpty()) {
            System.out.println("[WaitingRoomScreen] 빈 메시지는 전송하지 않습니다.");
            return;
        }

        // 현재 플레이어 이름 가져오기
        int myId = NetworkManager.getInstance().getCurrentPlayerId();
        String senderName = "Player" + myId;

        // 플레이어 목록에서 이름 찾기
        for (PlayerInfo player : players) {
            if (player.playerId == myId) {
                senderName = player.playerName;
                break;
            }
        }

        // ChatMsg 생성 및 전송
        ChatMsg msg = new ChatMsg();
        msg.sender = senderName;
        msg.text = chatInput.trim();

        NetworkManager.getInstance().sendTCP(msg);
        System.out.println("[WaitingRoomScreen] 채팅 메시지 전송: " + msg.sender + ": " + msg.text);

        // 입력창 초기화
        chatInput = "";
    }

    /**
     * 입력 처리 (버튼 클릭 + 키보드 입력)
     */
    private void handleInput() {
        // 키보드 입력 처리
        handleKeyboardInput();

        if (Gdx.input.justTouched()) {
            // 터치 좌표를 뷰포트 좌표로 변환
            com.badlogic.gdx.math.Vector3 touchPos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPos);
            float touchX = touchPos.x;
            float touchY = touchPos.y;

            // 채팅 입력창 클릭 (포커스)
            if (chatInputBounds.contains(touchX, touchY)) {
                chatInputFocused = true;
                System.out.println("[WaitingRoomScreen] 채팅 입력창 포커스 - 키보드로 입력하세요");
                return;
            }

            // 전송 버튼 클릭
            if (sendButtonBounds.contains(touchX, touchY)) {
                sendChatMessage();
                return;
            }

            // 다른 영역 클릭 시 포커스 해제
            chatInputFocused = false;

            // [방 나가기] 버튼 클릭 - room-exit-button은 92x32이므로 일반 버튼 크기로 처리
            if (isButtonClicked(touchX, touchY, Constants.WAITROOM_BUTTON_X, Constants.WAITROOM_BUTTON_Y,
                               Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT)) {
                System.out.println("[WaitingRoomScreen] 방 나가기 버튼 클릭");
                RoomManager.getInstance().leaveRoom();
                game.setScreen(new LobbyScreen(game));
                this.dispose();
            }

            // [게임 시작] 또는 [준비] 버튼 클릭
            float topButtonY = Constants.WAITROOM_BUTTON_Y + Constants.WAITROOM_BUTTON_HEIGHT + Constants.WAITROOM_BUTTON_SPACING;
            if (isButtonClicked(touchX, touchY, Constants.WAITROOM_BUTTON_X, topButtonY,
                               Constants.WAITROOM_BUTTON_WIDTH, Constants.WAITROOM_BUTTON_HEIGHT)) {
                boolean isHost = isCurrentPlayerHost();
                System.out.println("[WaitingRoomScreen] 상단 버튼 클릭! isHost: " + isHost);

                if (isHost) {
                    System.out.println("[WaitingRoomScreen] 게임 시작 버튼 클릭 - 서버에 StartGameMsg 전송");
                    System.out.println("[WaitingRoomScreen] 현재 플레이어 ID: " + NetworkManager.getInstance().getCurrentPlayerId());

                    // 서버에 게임 시작 메시지 전송
                    StartGameMsg startMsg = new StartGameMsg();
                    NetworkManager.getInstance().sendTCP(startMsg);

                    System.out.println("[WaitingRoomScreen] StartGameMsg 전송 완료");
                } else {
                    System.out.println("[WaitingRoomScreen] 준비 버튼 클릭");
                    // TODO: 준비 상태 토글 (서버에 ReadyToggleMsg 구현 필요)
                }
            }
        }
    }

    /**
     * 버튼 위에 마우스가 있는지 확인합니다.
     *
     * @param mouseX 마우스 X 좌표
     * @param mouseY 마우스 Y 좌표
     * @param buttonX 버튼 X 좌표
     * @param buttonY 버튼 Y 좌표
     * @param buttonWidth 버튼 너비
     * @param buttonHeight 버튼 높이
     * @return 마우스가 버튼 위에 있으면 true
     */
    private boolean isButtonHovered(float mouseX, float mouseY, float buttonX, float buttonY,
                                    float buttonWidth, float buttonHeight) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
               mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }

    /**
     * 버튼 클릭 여부를 확인합니다.
     */
    private boolean isButtonClicked(float touchX, float touchY, float buttonX, float buttonY,
                                    float buttonWidth, float buttonHeight) {
        return touchX >= buttonX && touchX <= buttonX + buttonWidth &&
               touchY >= buttonY && touchY <= buttonY + buttonHeight;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /**
     * 둥근 모서리 사각형을 그립니다.
     */
    private void renderRoundedRect(ShapeRenderer sr, float x, float y, float width, float height, float radius) {
        // 모서리가 너무 크면 조정
        float r = Math.min(radius, Math.min(width / 2, height / 2));

        // 중앙 사각형 (가로)
        sr.rect(x + r, y, width - r * 2, height);

        // 좌우 사각형 (세로)
        sr.rect(x, y + r, width, height - r * 2);

        // 4개의 모서리 원
        sr.circle(x + r, y + r, r, 10);
        sr.circle(x + width - r, y + r, r, 10);
        sr.circle(x + r, y + height - r, r, 10);
        sr.circle(x + width - r, y + height - r, r, 10);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
