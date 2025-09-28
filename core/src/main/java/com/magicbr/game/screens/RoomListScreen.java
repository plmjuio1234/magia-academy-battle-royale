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
import com.magicbr.game.utils.FontManager;
import com.magicbr.game.utils.ScreenTransition;
import com.magicbr.game.utils.UIHelper;

public class RoomListScreen extends BaseScreen {
    private Table mainTable;
    private Table leftPanel;   // 방 목록
    private Table rightPanel;  // 캐릭터 미리보기
    private ShapeRenderer shapeRenderer;
    private ScreenTransition transition;

    public RoomListScreen(MagicBattleRoyale game) {
        super(game);
        shapeRenderer = new ShapeRenderer();
        transition = new ScreenTransition();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();
        Gdx.app.log("RoomListScreen", "방 목록 화면 생성 중");

        // OPENING 전환 효과 시작
        transition.startOpening();

        createMainLayout();
        createLeftPanel();
        createRightPanel();

        game.getUiStage().addActor(mainTable);
        Gdx.app.log("RoomListScreen", "방 목록 화면 생성 완료");
    }

    private void createMainLayout() {
        mainTable = new Table();
        mainTable.setFillParent(true);

        leftPanel = new Table();
        rightPanel = new Table();

        // 2:1 비율로 화면 분할
        mainTable.add(leftPanel).width(game.getUiStage().getWidth() * 0.65f).fillY().expandY();
        mainTable.add(rightPanel).width(game.getUiStage().getWidth() * 0.35f).fillY().expandY();
    }

    private void createLeftPanel() {
        FontManager.initialize();
        BitmapFont koreanFont = FontManager.getKoreanFont();
        BitmapFont koreanFontLarge = FontManager.getKoreanFontLarge();
        Label.LabelStyle titleStyle = new Label.LabelStyle(koreanFontLarge, new Color(0.8f, 0.6f, 0f, 1f)); // 진한 금색
        Label.LabelStyle normalStyle = new Label.LabelStyle(koreanFont, Color.BLACK);
        TextButton.TextButtonStyle primaryButtonStyle = UIHelper.createPrimaryButtonStyle();
        TextButton.TextButtonStyle buttonStyle = UIHelper.createButtonStyle();

        // 플랫폼별 스케일링
        boolean isAndroid = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;

        // 제목
        Label titleLabel = new Label("마도학원 방 목록", titleStyle);
        titleLabel.setFontScale(isAndroid ? 1.0f : 1.5f);
        leftPanel.add(titleLabel).padTop(40).padBottom(30).row();

        // 방 생성 버튼
        TextButton createRoomButton = new TextButton("🏛️ 새로운 방 생성", primaryButtonStyle);
        createRoomButton.getLabel().setFontScale(isAndroid ? 1.0f : 1.2f);
        createRoomButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RoomListScreen", "방 생성 클릭");
                game.setScreen(new CharacterSelectScreen(game));
            }
        });
        leftPanel.add(createRoomButton).width(isAndroid ? 600 : 400).height(isAndroid ? 100 : 70).padBottom(20).row();

        // 방 목록 스크롤 영역
        Table roomListTable = new Table();

        // 더미 방 목록 생성
        for (int i = 1; i <= 5; i++) {
            Table roomEntry = createRoomEntry(i, normalStyle, buttonStyle);
            roomListTable.add(roomEntry).width(isAndroid ? 750 : 500).height(isAndroid ? 120 : 80).padBottom(10).row();
        }

        ScrollPane scrollPane = new ScrollPane(roomListTable);
        scrollPane.setScrollingDisabled(true, false);
        leftPanel.add(scrollPane).width(isAndroid ? 780 : 520).height(isAndroid ? 600 : 400).padBottom(20).row();

        // 돌아가기 버튼
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

    private Table createRoomEntry(int roomNumber, Label.LabelStyle normalStyle, TextButton.TextButtonStyle buttonStyle) {
        Table roomEntry = new Table();
        boolean isAndroid = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;

        // 방 정보
        Label roomNameLabel = new Label("⚡ 초급자의 마도 수련장 #" + roomNumber, normalStyle);
        roomNameLabel.setFontScale(isAndroid ? 1.0f : 0.9f);

        Label roomInfoLabel = new Label("👥 " + (roomNumber % 4 + 1) + "/4명 | 🎯 마도사 입문", normalStyle);
        roomInfoLabel.setFontScale(isAndroid ? 0.9f : 0.8f);
        roomInfoLabel.setColor(new Color(0.2f, 0.2f, 0.8f, 1f)); // 진한 파란색

        // 참가 버튼
        TextButton joinButton = new TextButton("참가", buttonStyle);
        joinButton.getLabel().setFontScale(isAndroid ? 1.0f : 0.9f);
        final int currentRoom = roomNumber;
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("RoomListScreen", "방 " + currentRoom + " 참가");
                game.setScreen(new CharacterSelectScreen(game));
            }
        });

        // 레이아웃
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
        Label.LabelStyle titleStyle = new Label.LabelStyle(koreanFontLarge, new Color(0.8f, 0.6f, 0f, 1f)); // 진한 금색
        Label.LabelStyle normalStyle = new Label.LabelStyle(koreanFont, Color.BLACK);

        // 캐릭터 미리보기 제목
        Label previewTitle = new Label("내 마도사", titleStyle);
        previewTitle.setFontScale(1.2f);
        rightPanel.add(previewTitle).padTop(40).padBottom(30).row();

        // 캐릭터 정보
        Label characterName = new Label("⭐ 신입 마도사", normalStyle);
        characterName.setFontScale(1.1f);
        characterName.setColor(new Color(0.2f, 0.2f, 0.8f, 1f)); // 진한 파란색
        rightPanel.add(characterName).padBottom(20).row();

        // 원소 정보
        Label elementInfo = new Label("🔥 화염 원소 특화\n💧 빙결 원소 부특화", normalStyle);
        elementInfo.setFontScale(0.9f);
        elementInfo.setWrap(true);
        rightPanel.add(elementInfo).width(250).padBottom(30).row();

        // 스탯 정보
        Table statsTable = new Table();
        addStatRow(statsTable, "⚔️ 공격력", "★★☆☆☆", normalStyle);
        addStatRow(statsTable, "🛡️ 방어력", "★☆☆☆☆", normalStyle);
        addStatRow(statsTable, "⚡ 마력", "★★★☆☆", normalStyle);
        addStatRow(statsTable, "💨 속도", "★★☆☆☆", normalStyle);

        rightPanel.add(statsTable).width(280).padBottom(30).row();

        // 커스터마이징 버튼 (미래 기능)
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
        valueLabel.setColor(new Color(0.8f, 0.6f, 0f, 1f)); // 진한 금색

        table.add(nameLabel).left().width(120);
        table.add(valueLabel).left().row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.3f, 1f);

        // 전환 효과 업데이트
        transition.update(delta);

        // 전환 효과가 완료된 후에만 UI 렌더링
        if (transition.isOpeningComplete() || !transition.isActive()) {
            // 배경 패널 그리기
            drawBackground();

            // UI 렌더링
            game.getUiStage().act(delta);
            game.getUiStage().draw();
        }

        // 전환 효과가 진행 중일 때만 렌더링
        if (transition.isActive()) {
            shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
            transition.render(shapeRenderer, game.getUiStage().getWidth(), game.getUiStage().getHeight());
        }
    }

    private void drawBackground() {
        shapeRenderer.setProjectionMatrix(game.getUiStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 왼쪽 패널 배경
        UIHelper.drawPanelBackground(shapeRenderer, 20, 20,
            game.getUiStage().getWidth() * 0.65f - 30,
            game.getUiStage().getHeight() - 40);

        // 오른쪽 패널 배경
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