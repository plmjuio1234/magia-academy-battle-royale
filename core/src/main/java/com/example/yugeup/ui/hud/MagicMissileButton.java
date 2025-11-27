package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.example.yugeup.game.skill.MagicMissile;

/**
 * 매직 미사일 ON/OFF 버튼 (원형)
 *
 * 매직 미사일 스킬의 자동 공격을 제어하는 UI 버튼입니다.
 * ON 상태는 녹색, OFF 상태는 빨간색으로 표시됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MagicMissileButton {
    private MagicMissile skill;

    // UI 위치 및 크기 (원형)
    private float x, y, radius;
    private Circle buttonBounds;

    // 스킬 아이콘
    private TextureRegion skillIcon;

    // 렌더링
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    /**
     * MagicMissileButton 생성자
     *
     * @param skill 매직 미사일 스킬
     * @param x 버튼 중심 X 좌표
     * @param y 버튼 중심 Y 좌표
     * @param radius 버튼 반경
     * @param skillIcon 스킬 아이콘 (magicwand)
     * @param shapeRenderer 도형 렌더러
     */
    public MagicMissileButton(MagicMissile skill, float x, float y, float radius,
                              TextureRegion skillIcon, ShapeRenderer shapeRenderer) {
        this.skill = skill;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.skillIcon = skillIcon;
        this.shapeRenderer = shapeRenderer;
        this.buttonBounds = new Circle(x, y, radius);

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        glyphLayout = new GlyphLayout();
    }

    /**
     * 렌더링 (원형)
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        // 배경 색상 (ON: 녹색, OFF: 빨간색)
        float alpha = 0.7f;
        Color bgColor = skill.isEnabled()
            ? new Color(0.3f, 0.8f, 0.3f, alpha)  // 녹색
            : new Color(0.8f, 0.3f, 0.3f, alpha);  // 빨간색

        // 원형 배경 그리기
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgColor);
        shapeRenderer.circle(x, y, radius);
        shapeRenderer.end();
        batch.begin();

        // 스킬 아이콘 렌더링
        if (skillIcon != null) {
            boolean shouldDarken = !skill.isEnabled();
            if (shouldDarken) {
                batch.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
            }

            // 아이콘을 버튼 안쪽에 패딩 5px로 그리기
            batch.draw(skillIcon,
                x - radius + 5, y - radius + 5,
                radius * 2 - 10, radius * 2 - 10);

            if (shouldDarken) {
                batch.setColor(Color.WHITE);
            }
        }

        // 원형 테두리 그리기
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        float lineWidth = 1.5f;
        shapeRenderer.setColor(Color.WHITE);
        for (float i = 0; i < lineWidth; i += 0.5f) {
            shapeRenderer.circle(x, y, radius - i);
        }
        shapeRenderer.end();
        batch.begin();

        // "AUTO" 레이블 (버튼 중앙)
        String label = "AUTO";
        glyphLayout.setText(font, label);
        float labelX = x - glyphLayout.width / 2;
        float labelY = y + glyphLayout.height / 2;
        font.draw(batch, label, labelX, labelY);
    }

    /**
     * 터치 감지 (원형)
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 버튼이 터치되었는지 여부
     */
    public boolean isTouched(float touchX, float touchY) {
        return buttonBounds.contains(touchX, touchY);
    }

    /**
     * 버튼 클릭 처리
     */
    public void onClick() {
        skill.toggleEnabled();
    }

    /**
     * 리소스 해제
     */
    public void dispose() {
        font.dispose();
    }
}
