package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.example.yugeup.game.level.LevelSystem;

/**
 * 경험치 바 UI 컴포넌트
 *
 * HUD에 표시되는 경험치 바입니다.
 * 현재 레벨, 경험치, 진행도를 시각적으로 표시합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ExpBarComponent {
    private LevelSystem levelSystem;

    // UI 위치 및 크기
    private float x, y, width, height;

    // 텍스처
    private Texture barBackground;
    private Texture barFill;

    // 폰트
    private BitmapFont font;

    /**
     * 생성자
     *
     * @param levelSystem 레벨 시스템
     * @param x X 좌표
     * @param y Y 좌표
     * @param width 너비
     * @param height 높이
     */
    public ExpBarComponent(LevelSystem levelSystem, float x, float y, float width, float height) {
        this.levelSystem = levelSystem;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // 텍스처 생성
        barBackground = createTexture(0.2f, 0.2f, 0.2f, 0.8f);
        barFill = createTexture(0.3f, 0.7f, 1.0f, 1.0f);  // 파란색

        // 폰트
        font = new BitmapFont();
        font.getData().setScale(1.2f);
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        // 배경
        batch.draw(barBackground, x, y, width, height);

        // 경험치 바 (진행도)
        float fillWidth = width * levelSystem.getExpRatio();
        batch.draw(barFill, x, y, fillWidth, height);

        // 텍스트 (레벨 & 경험치)
        String text = String.format("Lv.%d  %d / %d (%d%%)",
            levelSystem.getCurrentLevel(),
            levelSystem.getCurrentExp(),
            levelSystem.getExpForNextLevel(),
            levelSystem.getExpPercentage());

        font.draw(batch, text, x + 10, y + height - 5);
    }

    /**
     * 텍스처 생성 (단색)
     *
     * @param r Red (0.0 ~ 1.0)
     * @param g Green (0.0 ~ 1.0)
     * @param b Blue (0.0 ~ 1.0)
     * @param a Alpha (0.0 ~ 1.0)
     * @return 생성된 텍스처
     */
    private Texture createTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        barBackground.dispose();
        barFill.dispose();
        font.dispose();
    }
}
