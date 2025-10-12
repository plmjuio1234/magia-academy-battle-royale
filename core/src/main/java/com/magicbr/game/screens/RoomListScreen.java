package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.utils.Client;
import com.magicbr.game.utils.FontManager;
import com.magicbr.game.utils.ScreenTransition;
import com.magicbr.game.utils.UIHelper;

public class RoomListScreen extends BaseScreen {
    private Table mainTable;
    private Table leftPanel;
    private Table rightPanel;
    private ShapeRenderer shapeRenderer;
    private ScreenTransition transition;
    private Client client;

    private Table roomListTable;
    private ScrollPane scrollPane;
    private Label errorLabel;
    private float errorTimer = 0;

    public RoomListScreen(MagicBattleRoyale game) {
        super(game);
        shapeRenderer = new ShapeRenderer();
        transition = new ScreenTransition();
        client = game.getClient();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();
        Gdx.app.log("RoomListScreen", "방 목록 화면 생성 중");

        transition.startOpening();

        createMainLayout();
        createLeftPanel();
        createRightPanel();

        game.getUiStage().addActor(mainTable);

        // 서버에 방 목록 요청
        requestRoomList();

        Gdx.app.log("RoomListScreen", "방 목록 화면 생성 완료");
    }

    private void createMainLayout() {
        mainTable = new Table();
        mainTable.setFillParent(true);

        leftPanel = new Table();
        rightPanel = new Table();

        mainTable.add(leftPanel).width(game.getUiStage().getWidth() * 0.65f).fillY().expandY();
        mainTable.add(rightPanel).width(game.getUiStage().getWidth() * 0.35f).fillY().expandY();
    }

    private void createLeftPanel() {
        FontManager.initialize();
        BitmapFont koreanFont = FontManager.getKoreanFont();
        BitmapFont koreanFontLarge = FontManager.getKoreanFontLarge();
        Label.LabelStyle titleStyle = new Label.LabelStyle(koreanFontLarge, new Color(0.8f, 0.6f, 0f, 1f));
        Label.LabelStyle normalStyle = new Label.LabelStyle(koreanFont, Color.BLACK);
        TextButton.TextButtonStyle primaryButtonStyle = UIHelper.createPrimaryButtonStyle();
        TextButton.TextButtonStyle buttonStyle = UIHelper.createButtonStyle();

        boolean isAndroid = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;

        Label titleLabel = new Label("마도학원 방 목록", titleStyle);
        titleLabel.setFontScale(isAndroid ? 1.0f : 1.5f);
        leftPanel.add(titleLabel).padTop(40).padBottom(30).row();

        TextButton createRoomButton = new TextButton("🏛️ 새로운 방 생성", primaryButtonStyle);
        createRoomButton.getLabel().setFontScale(isAndroid ? 1.0f : 1.2f);
        createRoomButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RoomListScreen", "방 생성 클릭");

                // 서버에 방 생성 요청
                client.createRoom("초급자의 마도 수련장", 4);

                // 잠시 후 캐릭터 선택 화면으로 (실제로는 응답 받은 후 이동)
                game.setScreen(new CharacterSelectScreen(game));
            }
        });
        leftPanel.add(createRoomButton).width(isAndroid ? 600 : 400).height(isAndroid ? 100 : 70).padBottom(20).row();

        // 방 목록 스크롤 영역 (초기에는 빈 테이블)
        roomListTable = new Table();

        // 로딩 메시지
        Label loadingLabel = new Label("방 목록을 불러오는 중...", normalStyle);
        loadingLabel.setFontScale(isAndroid ? 1.0f : 0.9f);
        roomListTable.add(loadingLabel).pad(20);

        scrollPane = new ScrollPane(roomListTable);
        scrollPane.setScrollingDisabled(true, false);
        leftPanel.add(scrollPane).width(isAndroid ? 780 : 520).height(isAndroid ? 600 : 400).padBottom(20).row();

        // 에러 메시지 라벨 추가
        errorLabel = new Label("", normalStyle);
        errorLabel.setFontScale(isAndroid ? 1.0f : 0.9f);
        errorLabel.setColor(Color.RED);
        leftPanel.add(errorLabel).padBottom(10).row();

        // 새로고침 버튼 추가
        TextButton refreshButton = new TextButton("🔄 새로고침", buttonStyle);
        refreshButton.getLabel().setFontScale(isAndroid ? 1.0f : 1.0f);
        refreshButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RoomListScreen", "방 목록 새로고침");
                requestRoomList();
            }
        });
        leftPanel.add(refreshButton).width(isAndroid ? 300 : 200).height(isAndroid ? 90 : 60).padBottom(10).row();

        TextButton backButton = new TextButton("◀️ 메인 메뉴", buttonStyle);
        backButton.getLabel().setFontScale(isAndroid ? 1.0f : 1.1f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        leftPanel.add(backButton).width(isAndroid ? 300 : 200).height(isAndroid ? 90 : 60);
    }

    private void requestRoomList() {
        // 서버에 방 목록 요청
        client.getRoomList();
    }

    private Table createRoomEntry(int roomId, String roomName, int currentPlayers, int maxPlayers,
                                  String hostName, Label.LabelStyle normalStyle,
                                  TextButton.TextButtonStyle buttonStyle) {
        Table roomEntry = new Table();
        boolean isAndroid = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;

        Label roomNameLabel = new Label("⚡ " + roomName, normalStyle);
        roomNameLabel.setFontScale(isAndroid ? 1.0f : 0.9f);

        Label roomInfoLabel = new Label("👥 " + currentPlayers + "/" + maxPlayers + "명 | 방장: " + hostName, normalStyle);
        roomInfoLabel.setFontScale(isAndroid ? 0.9f : 0.8f);
        roomInfoLabel.setColor(new Color(0.2f, 0.2f, 0.8f, 1f));

        TextButton joinButton = new TextButton("참가", buttonStyle);
        joinButton.getLabel().setFontScale(isAndroid ? 1.0f : 0.9f);
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RoomListScreen", "방 " + roomId + " 참가");

                // 서버에 방 입장 요청
                client.joinRoom(roomId);

                // 에러 메시지 초기화
                errorLabel.setText("");
                errorTimer = 0;
            }
        });

        Table infoTable = new Table();
        infoTable.add(roomNameLabel).left().row();
        infoTable.add(roomInfoLabel).left().padTop(5);

        roomEntry.add(infoTable).expandX().fillX().left();
        roomEntry.add(joinButton).width(isAndroid ? 120 : 80).height(isAndroid ? 90 : 60).right();

        return roomEntry;
    }

    private void createRightPanel() {
        FontManager.initialize();
        BitmapFont koreanFont = FontManager.getKoreanFont();
        BitmapFont koreanFontLarge = FontManager.getKoreanFontLarge();
        Label.LabelStyle titleStyle = new Label.LabelStyle(koreanFontLarge, new Color(0.8f, 0.6f, 0f, 1f));
        Label.LabelStyle normalStyle = new Label.LabelStyle(koreanFont, Color.BLACK);

        Label previewTitle = new Label("내 마도사", titleStyle);
        previewTitle.setFontScale(1.2f);
        rightPanel.add(previewTitle).padTop(40).padBottom(30).row();

        Label characterName = new Label("⭐ 신입 마도사", normalStyle);
        characterName.setFontScale(1.1f);
        characterName.setColor(new Color(0.2f, 0.2f, 0.8f, 1f));
        rightPanel.add(characterName).padBottom(20).row();

        Label elementInfo = new Label("🔥 화염 원소 특화\n💧 빙결 원소 부특화", normalStyle);
        elementInfo.setFontScale(0.9f);
        elementInfo.setWrap(true);
        rightPanel.add(elementInfo).width(250).padBottom(30).row();

        Table statsTable = new Table();
        addStatRow(statsTable, "⚔️ 공격력", "★★☆☆☆", normalStyle);
        addStatRow(statsTable, "🛡️ 방어력", "★☆☆☆☆", normalStyle);
        addStatRow(statsTable, "⚡ 마력", "★★★☆☆", normalStyle);
        addStatRow(statsTable, "💨 속도", "★★☆☆☆", normalStyle);

        rightPanel.add(statsTable).width(280).padBottom(30).row();

        TextButton customizeButton = new TextButton("🎨 외형 변경", UIHelper.createButtonStyle());
        customizeButton.getLabel().setFontScale(0.9f);
        customizeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RoomListScreen", "커스터마이징 기능 (준비중)");
            }
        });
        rightPanel.add(customizeButton).width(180).height(50);
    }

    private void addStatRow(Table table, String statName, String statValue, Label.LabelStyle style) {
        Label nameLabel = new Label(statName, style);
        nameLabel.setFontScale(0.8f);
        Label valueLabel = new Label(statValue, style);
        valueLabel.setFontScale(0.8f);
        valueLabel.setColor(new Color(0.8f, 0.6f, 0f, 1f));

        table.add(nameLabel).left().width(120);
        table.add(valueLabel).left().row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.3f, 1f);

        // 서버 응답 체크 및 UI 업데이트
        checkServerResponses();

        // 에러 메시지 타이머 업데이트
        if (errorTimer > 0) {
            errorTimer -= delta;
            if (errorTimer <= 0) {
                errorLabel.setText("");
            }
        }

        transition.update(delta);

        if (transition.isOpeningComplete() || !transition.isActive()) {
            drawBackground();

            game.getUiStage().act(delta);
            game.getUiStage().draw();
        }

        if (transition.isActive()) {
            shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
            transition.render(shapeRenderer, game.getUiStage().getWidth(), game.getUiStage().getHeight());
        }
    }

    private void checkServerResponses() {
        // 방 목록 응답 체크
        Client.RoomInfo[] rooms = client.getLatestRoomList();
        if (rooms != null) {
            updateRoomListUI(rooms);
        }

        // 방 입장 응답 체크
        Client.JoinRoomResponse joinResponse = client.getLatestJoinRoomResponse();
        if (joinResponse != null) {
            if (joinResponse.success) {
                // 성공 시 캐릭터 선택 화면으로
                game.setScreen(new CharacterSelectScreen(game));
            } else {
                // 실패 시 에러 표시
                showError(joinResponse.message);
            }
        }
    }

    private void showError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorTimer = 3.0f; // 3초 후 사라짐
        Gdx.app.log("RoomListScreen", "에러 표시: " + message);
    }

    private void updateRoomListUI(Client.RoomInfo[] rooms) {
        FontManager.initialize();
        BitmapFont koreanFont = FontManager.getKoreanFont();
        Label.LabelStyle normalStyle = new Label.LabelStyle(koreanFont, Color.BLACK);
        TextButton.TextButtonStyle buttonStyle = UIHelper.createButtonStyle();
        boolean isAndroid = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;

        // 기존 방 목록 삭제
        roomListTable.clear();

        if (rooms.length == 0) {
            Label emptyLabel = new Label("생성된 방이 없습니다", normalStyle);
            emptyLabel.setFontScale(isAndroid ? 1.0f : 0.9f);
            roomListTable.add(emptyLabel).pad(20);
        } else {
            // 새 방 목록 추가
            for (Client.RoomInfo room : rooms) {
                Table roomEntry = createRoomEntry(room.roomId, room.roomName,
                    room.currentPlayers, room.maxPlayers,
                    room.hostName, normalStyle, buttonStyle);
                roomListTable.add(roomEntry).width(isAndroid ? 750 : 500)
                    .height(isAndroid ? 120 : 80)
                    .padBottom(10).row();
            }
        }

        Gdx.app.log("RoomListScreen", "방 목록 UI 업데이트 완료: " + rooms.length + "개");
    }

    private void drawBackground() {
        shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        UIHelper.drawPanelBackground(shapeRenderer, 20, 20,
            game.getUiStage().getWidth() * 0.65f - 30,
            game.getUiStage().getHeight() - 40);

        UIHelper.drawPanelBackground(shapeRenderer,
            game.getUiStage().getWidth() * 0.65f + 10, 20,
            game.getUiStage().getWidth() * 0.35f - 30,
            game.getUiStage().getHeight() - 40);

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
