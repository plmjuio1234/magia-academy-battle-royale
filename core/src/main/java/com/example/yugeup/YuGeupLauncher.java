package com.example.yugeup;

import com.badlogic.gdx.Game;
import com.example.yugeup.screens.LoadingScreen;
import com.example.yugeup.utils.Logger;

/**
 * YuGeup 게임 메인 런처
 *
 * libGDX Game 클래스를 상속받아 화면 전환을 관리합니다.
 * 게임 시작 시 로딩 화면부터 시작됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class YuGeupLauncher extends Game {

    @Override
    public void create() {
        Logger.info("=== YuGeup 게임 시작 ===");
        Logger.info("화면 크기: " + com.example.yugeup.utils.Constants.SCREEN_WIDTH +
                    "x" + com.example.yugeup.utils.Constants.SCREEN_HEIGHT);

        // 로딩 화면으로 시작
        this.setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        Logger.info("=== YuGeup 게임 종료 ===");

        // 현재 화면 정리
        if (getScreen() != null) {
            getScreen().dispose();
        }

        // AssetManager 정리
        com.example.yugeup.utils.AssetManager.getInstance().dispose();

        super.dispose();
    }
}
