package com.magicbr.game.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class UIHelper {

    // 공통 UI 스타일들 (밝은 배경용 검은색 텍스트)
    public static Label.LabelStyle createTitleStyle() {
        FontManager.initialize();
        BitmapFont font = FontManager.getKoreanFontLarge();
        return new Label.LabelStyle(font, Color.BLACK);
    }

    public static Label.LabelStyle createNormalStyle() {
        FontManager.initialize();
        BitmapFont font = FontManager.getKoreanFont();
        return new Label.LabelStyle(font, Color.BLACK);
    }

    public static Label.LabelStyle createSubtitleStyle() {
        FontManager.initialize();
        BitmapFont font = FontManager.getKoreanFont();
        return new Label.LabelStyle(font, new Color(0.2f, 0.2f, 0.8f, 1f)); // 진한 파란색
    }

    public static TextButton.TextButtonStyle createButtonStyle() {
        FontManager.initialize();
        BitmapFont font = FontManager.getKoreanFont();
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.BLACK;
        style.overFontColor = new Color(0.8f, 0.4f, 0f, 1f); // 주황색 호버
        style.downFontColor = Color.DARK_GRAY;
        return style;
    }

    public static TextButton.TextButtonStyle createPrimaryButtonStyle() {
        FontManager.initialize();
        BitmapFont font = FontManager.getKoreanFont();
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.BLACK;
        style.overFontColor = new Color(0.2f, 0.2f, 0.8f, 1f); // 진한 파란색 호버
        style.downFontColor = Color.DARK_GRAY;
        return style;
    }

    // 버튼 배경 그리기 헬퍼 (개선된 디자인)
    public static void drawButtonBackground(ShapeRenderer renderer, float x, float y, float width, float height, boolean isPressed, boolean isHovered) {
        // 다층 그림자 (블러 효과)
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.15f);
        renderer.rect(x + 6, y - 6, width, height);
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.25f);
        renderer.rect(x + 4, y - 4, width, height);
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.35f);
        renderer.rect(x + 2, y - 2, width, height);

        // 버튼 베이스 (그라데이션)
        if (isPressed) {
            // 눌린 상태 - 어두운 그라데이션
            renderer.setColor(0.15f, 0.2f, 0.35f, 0.9f);
            renderer.rect(x, y, width, height);
            renderer.setColor(0.2f, 0.25f, 0.4f, 0.8f);
            renderer.rect(x + 2, y + 2, width - 4, height - 4);
        } else if (isHovered) {
            // 호버 상태 - 밝은 그라데이션
            renderer.setColor(0.25f, 0.35f, 0.55f, 0.9f);
            renderer.rect(x, y, width, height);
            renderer.setColor(0.35f, 0.45f, 0.65f, 0.8f);
            renderer.rect(x + 2, y + 2, width - 4, height - 4);
            renderer.setColor(0.45f, 0.55f, 0.75f, 0.6f);
            renderer.rect(x + 4, y + 4, width - 8, height - 8);
        } else {
            // 기본 상태 - 중간 톤 그라데이션
            renderer.setColor(0.2f, 0.25f, 0.4f, 0.9f);
            renderer.rect(x, y, width, height);
            renderer.setColor(0.3f, 0.35f, 0.5f, 0.8f);
            renderer.rect(x + 2, y + 2, width - 4, height - 4);
            renderer.setColor(0.4f, 0.45f, 0.6f, 0.6f);
            renderer.rect(x + 4, y + 4, width - 8, height - 8);
        }

        // 상단 하이라이트 (광택 효과)
        if (!isPressed) {
            renderer.setColor(0.6f, 0.7f, 0.9f, 0.6f);
            renderer.rect(x + 4, y + height * 0.7f, width - 8, height * 0.25f);
            renderer.setColor(0.8f, 0.9f, 1.0f, 0.4f);
            renderer.rect(x + 6, y + height * 0.8f, width - 12, height * 0.15f);
        }

        // 테두리
        renderer.end();
        renderer.begin(ShapeRenderer.ShapeType.Line);
        if (isPressed) {
            renderer.setColor(0.1f, 0.15f, 0.25f, 1.0f);
        } else if (isHovered) {
            renderer.setColor(0.5f, 0.7f, 0.9f, 1.0f);
        } else {
            renderer.setColor(0.4f, 0.5f, 0.7f, 1.0f);
        }
        renderer.rect(x, y, width, height);
        renderer.end();
        renderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    // 패널 배경 그리기 헬퍼
    public static void drawPanelBackground(ShapeRenderer renderer, float x, float y, float width, float height) {
        // 그림자
        renderer.setColor(0.0f, 0.0f, 0.0f, 0.4f);
        renderer.rect(x + 4, y - 4, width, height);

        // 배경
        renderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        renderer.rect(x, y, width, height);

        // 테두리 하이라이트
        renderer.setColor(0.3f, 0.3f, 0.4f, 0.7f);
        // 위쪽
        renderer.rect(x, y + height - 2, width, 2);
        // 왼쪽
        renderer.rect(x, y, 2, height);
    }
}