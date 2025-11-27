package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.example.yugeup.game.monster.Monster;

/**
 * HP 바 컴포넌트
 *
 * 몬스터나 플레이어의 체력을 시각적으로 표시합니다.
 * 배경(검은색)과 현재 체력(녹색/빨간색)을 렌더링합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class HPBar {
    // HP 바를 표시할 엔티티
    private Monster owner;

    // HP 바 위치
    private Vector2 position;

    // HP 바 크기
    private float width = 32f;
    private float height = 4f;

    // 색상
    private Color bgColor = new Color(0.2f, 0.2f, 0.2f, 0.8f);  // 배경 (어두운 회색)
    private Color hpColor = new Color(0f, 1f, 0f, 1f);  // 체력 (녹색)
    private Color lowHpColor = new Color(1f, 0f, 0f, 1f);  // 낮은 체력 (빨간색)

    // ShapeRenderer (static으로 공유)
    private static ShapeRenderer shapeRenderer;

    /**
     * HP 바 생성자
     *
     * @param owner HP 바를 표시할 몬스터
     */
    public HPBar(Monster owner) {
        this.owner = owner;
        this.position = new Vector2();

        // ShapeRenderer 초기화 (한 번만)
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer();
        }
    }

    /**
     * HP 바 위치 설정
     *
     * @param x X 좌표
     * @param y Y 좌표
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    /**
     * HP 바 렌더링
     *
     * @param batch 스프라이트 배치
     */
    public void render(SpriteBatch batch) {
        int currentHP = owner.getCurrentHealth();
        int maxHP = owner.getMaxHealth();

        if (currentHP <= 0) {
            return;  // 사망 시 표시 안 함
        }

        // 체력 비율 계산
        float hpRatio = (float) currentHP / maxHP;

        // 배치 종료 후 ShapeRenderer로 그리기
        batch.end();

        // 카메라 프로젝션 매트릭스 설정 (batch와 동일한 카메라 사용)
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 배경 렌더링 (어두운 회색)
        shapeRenderer.setColor(bgColor);
        shapeRenderer.rect(position.x, position.y, width, height);

        // HP 바 렌더링 (현재 체력)
        Color currentColor = (hpRatio > 0.3f) ? hpColor : lowHpColor;
        shapeRenderer.setColor(currentColor);
        shapeRenderer.rect(position.x, position.y, width * hpRatio, height);

        shapeRenderer.end();

        // 배치 재시작
        batch.begin();
    }

    /**
     * HP 바 너비 설정
     *
     * @param width 너비
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * HP 바 높이 설정
     *
     * @param height 높이
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * HP 바 색상 설정
     *
     * @param hpColor 체력 색상
     * @param lowHpColor 낮은 체력 색상
     */
    public void setColors(Color hpColor, Color lowHpColor) {
        this.hpColor = hpColor;
        this.lowHpColor = lowHpColor;
    }

    /**
     * ShapeRenderer 리소스 해제
     */
    public static void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
    }
}
