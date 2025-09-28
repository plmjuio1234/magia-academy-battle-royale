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

public class ResultScreen extends BaseScreen {
    private int playerRank;
    private String[] playerNames;
    private Table table;

    public ResultScreen(MagicBattleRoyale game, int playerRank, String[] playerNames) {
        super(game);
        this.playerRank = playerRank;
        this.playerNames = playerNames != null ? playerNames : new String[]{"Player1", "Player2", "Player3", "Player4"};
    }

    @Override
    protected void create() {

    }

    @Override
    public void show() {
        super.show();

        table = new Table();
        table.setFillParent(true);

        BitmapFont font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle goldStyle = new Label.LabelStyle(font, Color.GOLD);
        Label.LabelStyle silverStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        Label.LabelStyle bronzeStyle = new Label.LabelStyle(font, Color.BROWN);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;

        // 결과 제목
        String resultText;
        Label.LabelStyle resultStyle;
        switch (playerRank) {
            case 1:
                resultText = "승리!";
                resultStyle = goldStyle;
                break;
            case 2:
                resultText = "2등";
                resultStyle = silverStyle;
                break;
            case 3:
                resultText = "3등";
                resultStyle = bronzeStyle;
                break;
            default:
                resultText = "패배";
                resultStyle = labelStyle;
        }

        Label resultLabel = new Label(resultText, resultStyle);
        resultLabel.setFontScale(3f);

        Label rankLabel = new Label("당신의 순위: " + playerRank + "/4", labelStyle);
        rankLabel.setFontScale(1.5f);

        // 순위표
        Table rankingTable = new Table();
        Label rankingTitle = new Label("최종 순위:", labelStyle);
        rankingTitle.setFontScale(1.2f);
        rankingTable.add(rankingTitle).padBottom(20).row();

        for (int i = 0; i < Math.min(playerNames.length, 4); i++) {
            Label.LabelStyle rankStyle = i == 0 ? goldStyle : (i == 1 ? silverStyle : (i == 2 ? bronzeStyle : labelStyle));
            Label rankingLabel = new Label((i + 1) + ". " + playerNames[i], rankStyle);
            rankingTable.add(rankingLabel).padBottom(10).row();
        }

        // 버튼들
        TextButton playAgainButton = new TextButton("다시 플레이", buttonStyle);
        playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CharacterSelectScreen(game));
            }
        });

        TextButton menuButton = new TextButton("메인 메뉴", buttonStyle);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        // 레이아웃
        table.add(resultLabel).padBottom(30).row();
        table.add(rankLabel).padBottom(50).row();
        table.add(rankingTable).padBottom(50).row();
        table.add(playAgainButton).width(200).height(50).padBottom(20).row();
        table.add(menuButton).width(200).height(50);

        game.getUiStage().addActor(table);
    }

    @Override
    public void render(float delta) {
        // 순위에 따른 배경색
        if (playerRank == 1) {
            ScreenUtils.clear(0.2f, 0.4f, 0.1f, 1f); // 승리 - 녹색
        } else {
            ScreenUtils.clear(0.3f, 0.1f, 0.1f, 1f); // 패배 - 적색
        }

        game.getUiStage().act(delta);
        game.getUiStage().draw();
    }
}