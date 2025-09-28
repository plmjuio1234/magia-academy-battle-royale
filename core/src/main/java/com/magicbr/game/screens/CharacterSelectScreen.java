package com.magicbr.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.magicbr.game.MagicBattleRoyale;
import com.magicbr.game.utils.Constants;
import com.magicbr.game.utils.FontManager;
import com.magicbr.game.utils.UIHelper;

public class CharacterSelectScreen extends BaseScreen {
    private Table table;
    private Constants.CharacterClass selectedClass = null;
    private Label descriptionLabel;
    private TextButton confirmButton;

    public CharacterSelectScreen(MagicBattleRoyale game) {
        super(game);
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();

        table = new Table();
        table.setFillParent(true);

        // UI 스타일 사용
        Label.LabelStyle titleStyle = UIHelper.createTitleStyle();
        Label.LabelStyle subtitleStyle = UIHelper.createSubtitleStyle();
        Label.LabelStyle normalStyle = UIHelper.createNormalStyle();
        TextButton.TextButtonStyle primaryButtonStyle = UIHelper.createPrimaryButtonStyle();
        TextButton.TextButtonStyle normalButtonStyle = UIHelper.createButtonStyle();

        Label titleLabel = new Label("🏛️ 마도 수련장 로비", titleStyle);
        titleLabel.setFontScale(1.8f);

        // 방 정보 표시
        Label roomInfoLabel = new Label("⚡ 초급자의 마도 수련장", subtitleStyle);
        roomInfoLabel.setFontScale(1.5f);

        descriptionLabel = new Label("🎯 게임 모드: 마도사 배틀로얄\n👥 플레이어: 1/4명 (싱글플레이 모드)\n🗺️ 맵: 마도학원 연습장\n\n⏰ 모든 플레이어가 준비되면 자동으로 게임이 시작됩니다!", normalStyle);
        descriptionLabel.setWrap(true);
        descriptionLabel.setFontScale(1.1f);

        // 플레이어 목록 추가
        Label playersTitle = new Label("👥 참가자 목록:", normalStyle);
        playersTitle.setFontScale(1.0f);
        playersTitle.setColor(new Color(0.8f, 0.6f, 0f, 1f)); // 진한 금색

        Label playersList = new Label("🧙‍♂️ 나 (준비됨)\n👻 AI 마도사 #1 (대기중)\n👻 AI 마도사 #2 (대기중)\n👻 AI 마도사 #3 (대기중)", normalStyle);
        playersList.setFontScale(0.9f);
        playersList.setColor(new Color(0.2f, 0.2f, 0.8f, 1f)); // 진한 파란색

        confirmButton = new TextButton("🚀 게임 시작!", primaryButtonStyle);
        confirmButton.getLabel().setFontScale(1.3f);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, Constants.CharacterClass.ELEMENTALIST));
            }
        });

        TextButton backButton = new TextButton("◀️ 방 목록으로", normalButtonStyle);
        backButton.getLabel().setFontScale(1.2f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new RoomListScreen(game));
            }
        });

        // 개선된 레이아웃
        table.center();
        table.add(titleLabel).padBottom(30).row();
        table.add(roomInfoLabel).padBottom(20).row();
        table.add(descriptionLabel).width(800).padBottom(30).row();
        table.add(playersTitle).padBottom(10).row();
        table.add(playersList).padBottom(40).row();
        table.add(confirmButton).width(320).height(75).padBottom(30).row();
        table.add(backButton).width(280).height(60);

        game.getUiStage().addActor(table);
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1f);

        game.getUiStage().act(delta);
        game.getUiStage().draw();
    }
}