package com.magicbr.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.utils.Client;
import com.magicbr.game.utils.Constants;
import com.magicbr.game.utils.UIHelper;

public class CharacterSelectScreen extends BaseScreen {
    private Table table;
    private Constants.CharacterClass selectedClass = null;
    private Label descriptionLabel;
    private Label playersListLabel;
    private TextButton confirmButton;
    private Client client;

    public CharacterSelectScreen(MagicBattleRoyale game) {
        super(game);
        client = game.getClient();
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();

        table = new Table();
        table.setFillParent(true);

        Label.LabelStyle titleStyle = UIHelper.createTitleStyle();
        Label.LabelStyle subtitleStyle = UIHelper.createSubtitleStyle();
        Label.LabelStyle normalStyle = UIHelper.createNormalStyle();
        TextButton.TextButtonStyle primaryButtonStyle = UIHelper.createPrimaryButtonStyle();
        TextButton.TextButtonStyle normalButtonStyle = UIHelper.createButtonStyle();

        Label titleLabel = new Label("🏛️ 마도 수련장 로비", titleStyle);
        titleLabel.setFontScale(1.8f);

        Label roomInfoLabel = new Label("⚡ 초급자의 마도 수련장", subtitleStyle);
        roomInfoLabel.setFontScale(1.5f);

        descriptionLabel = new Label("🎯 게임 모드: 마도사 배틀로얄\n👥 플레이어: 1/4명\n🗺️ 맵: 마도학원 연습장\n\n⏰ 모든 플레이어가 준비되면 자동으로 게임이 시작됩니다!", normalStyle);
        descriptionLabel.setWrap(true);
        descriptionLabel.setFontScale(1.1f);

        Label playersTitle = new Label("👥 참가자 목록:", normalStyle);
        playersTitle.setFontScale(1.0f);
        playersTitle.setColor(new Color(0.8f, 0.6f, 0f, 1f));

        playersListLabel = new Label("🔄 참가자 정보 로딩 중...", normalStyle);
        playersListLabel.setFontScale(0.9f);
        playersListLabel.setColor(new Color(0.2f, 0.2f, 0.8f, 1f));

        confirmButton = new TextButton("🚀 게임 시작!(방장만)", primaryButtonStyle);
        confirmButton.getLabel().setFontScale(1.3f);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                client.startGame();
                Gdx.app.log("CharacterSelectScreen", "게임 시작 요청");
            }
        });

        TextButton backButton = new TextButton("◀️ 방 나가기", normalButtonStyle);
        backButton.getLabel().setFontScale(1.2f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                client.leaveRoom();
                game.setScreen(new RoomListScreen(game));
            }
        });

        table.center();
        table.add(titleLabel).padBottom(30).row();
        table.add(roomInfoLabel).padBottom(20).row();
        table.add(descriptionLabel).width(800).padBottom(30).row();
        table.add(playersTitle).padBottom(10).row();
        table.add(playersListLabel).padBottom(40).row();
        table.add(confirmButton).width(320).height(75).padBottom(30).row();
        table.add(backButton).width(280).height(60);

        game.getUiStage().addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1f);

        checkServerResponses();

        game.getUiStage().act(delta);
        game.getUiStage().draw();
    }

    private void checkServerResponses() {
        Client.RoomUpdateMsg update = client.getLatestRoomUpdate();
        if (update != null) {
            updatePlayersList(update.players, update.newHostId);
        }

        Client.GameStartNotification notification = client.getLatestGameStart();
        if (notification != null) {
            Gdx.app.log("CharacterSelectScreen", "게임 시작!");
            game.setScreen(new GameScreen(game, Constants.CharacterClass.ELEMENTALIST));
        }
    }

    private void updatePlayersList(Client.PlayerInfo[] players, int hostId) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < players.length; i++) {
            Client.PlayerInfo player = players[i];

            String icon = player.isHost ? "👑" : "🧙‍♂️";
            String hostTag = player.isHost ? " (방장)" : "";

            sb.append(icon).append(" ").append(player.playerName).append(hostTag);

            if (i < players.length - 1) {
                sb.append("\n");
            }
        }

        playersListLabel.setText(sb.toString());

        descriptionLabel.setText(String.format(
            "🎯 게임 모드: 마도사 배틀로얄\n" +
                "👥 플레이어: %d/4명\n" +
                "🗺️ 맵: 마도학원 연습장\n\n" +
                "⏰ 모든 플레이어가 준비되면 자동으로 게임이 시작됩니다!",
            players.length
        ));

        Gdx.app.log("CharacterSelectScreen", "참가자 목록 업데이트: " + players.length + "명");
    }
}
