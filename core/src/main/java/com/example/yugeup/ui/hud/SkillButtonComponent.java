package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.example.yugeup.game.skill.ElementalSkill;

/**
 * 스킬 버튼 UI 컴포넌트 (원형)
 *
 * 게임 화면에서 스킬을 표시하고 관리하는 UI 컴포넌트입니다.
 * 원형 버튼, 스킬 아이콘, 쿨타임 오버레이, 터치 감지를 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillButtonComponent {
    // 스킬 정보
    private ElementalSkill skill;
    private TextureRegion skillIcon;
    private int skillId;
    private boolean isLearned;  // 배운 스킬인지 여부

    // 버튼 영역 (원형)
    private Circle buttonBounds;
    private float x;
    private float y;
    private float radius;  // 반경

    // 렌더링
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // 상태
    private boolean isPressed;
    private boolean isHovered;

    /**
     * 스킬 버튼 생성자
     *
     * @param skill 표시할 스킬
     * @param skillIcon 스킬 아이콘 텍스처
     * @param x 버튼 중심 X 좌표
     * @param y 버튼 중심 Y 좌표
     * @param radius 버튼 반경
     * @param isLearned 배운 스킬인지 여부
     * @param shapeRenderer 도형 렌더러
     * @param font 폰트
     */
    public SkillButtonComponent(ElementalSkill skill, TextureRegion skillIcon,
                                 float x, float y, float radius, boolean isLearned,
                                 ShapeRenderer shapeRenderer, BitmapFont font) {
        this.skill = skill;
        this.skillIcon = skillIcon;
        this.skillId = skill != null ? skill.getSkillId() : 0;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.isLearned = isLearned;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.buttonBounds = new Circle(x, y, radius);
        this.isPressed = false;
        this.isHovered = false;
    }

    /**
     * 스킬 버튼을 렌더링합니다. (원형)
     *
     * @param batch 스프라이트 배치
     */
    public void render(SpriteBatch batch) {
        if (skill == null) return;

        // 배경 색상 (배운 스킬: 투명도 70%, 미배운 스킬: 투명도 50%)
        float alpha = isLearned ? 0.7f : 0.5f;
        Color bgColor = isPressed ? new Color(0.3f, 0.3f, 0.3f, alpha) :
                        isHovered ? new Color(0.5f, 0.5f, 0.5f, alpha) :
                        new Color(0.2f, 0.2f, 0.2f, alpha);

        // 배운 스킬이 아니면 더 어둡게
        if (!isLearned) {
            bgColor = new Color(0.15f, 0.15f, 0.15f, alpha);
        }

        // 쿨타임 중이면 배경 투명도 감소
        if (!skill.isReady()) {
            bgColor.a = 0.4f;
        }

        // 원형 배경 그리기
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgColor);
        shapeRenderer.circle(x, y, radius);
        shapeRenderer.end();
        batch.begin();

        // 스킬 아이콘 그리기
        if (skillIcon != null) {
            // 쿨타임 중이거나 미배운 스킬은 어둡게 표시
            boolean shouldDarken = !isLearned || !skill.isReady();
            batch.setColor(shouldDarken ? new Color(0.4f, 0.4f, 0.4f, 1f) : Color.WHITE);
            batch.draw(skillIcon, x - radius + 5, y - radius + 5, radius * 2 - 10, radius * 2 - 10);
            batch.setColor(Color.WHITE);  // 색상 리셋
        }

        // 쿨타임 복구 오버레이 (아래에서부터 위로 색이 돌아옴, 배운 스킬만)
        if (!skill.isReady() && isLearned && skillIcon != null) {
            renderCooldownRecovery(batch);
        }

        // 원형 테두리 그리기 (더 얇게)
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        float lineWidth = 1.5f;  // 테두리 두께 조정
        shapeRenderer.setColor(isHovered && isLearned ? Color.YELLOW : Color.WHITE);
        // 여러 번 그려서 두께 조절
        for (float i = 0; i < lineWidth; i += 0.5f) {
            shapeRenderer.circle(x, y, radius - i);
        }
        shapeRenderer.end();
        batch.begin();
    }

    /**
     * 쿨타임 복구를 렌더링합니다
     * 현재는 아이콘 어둡게 + 남은 시간 텍스트로 표시
     *
     * @param batch 스프라이트 배치
     */
    private void renderCooldownRecovery(SpriteBatch batch) {
        if (skill == null || skill.isReady()) return;

        // 남은 쿨타임 시간 표시 (중앙)
        if (font != null) {
            float remainingTime = skill.getCurrentCooldown();
            if (remainingTime > 0.05f) {  // 0.05초 이하는 표시 안 함
                String timeText = String.format("%.1f", remainingTime);
                font.setColor(Color.WHITE);
                // 텍스트를 중앙에 표시
                float textWidth = timeText.length() * 8;
                font.draw(batch, timeText, x - textWidth / 2, y + 8);
            }
        }
    }

    /**
     * 터치 다운 처리 (원형 버튼)
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 버튼이 터치된 경우 true
     */
    public boolean handleTouchDown(float touchX, float touchY) {
        if (buttonBounds.contains(touchX, touchY)) {
            if (isLearned) {  // 배운 스킬일 때만 클릭 가능
                isPressed = true;
                return true;
            }
        }
        return false;
    }

    /**
     * 터치 업 처리
     *
     * @return 버튼이 눌려진 상태였던 경우 true
     */
    public boolean handleTouchUp() {
        if (isPressed) {
            isPressed = false;
            return true;
        }
        return false;
    }

    /**
     * 호버 상태 업데이트 (원형)
     *
     * @param mouseX 마우스 X 좌표
     * @param mouseY 마우스 Y 좌표
     */
    public void updateHover(float mouseX, float mouseY) {
        isHovered = buttonBounds.contains(mouseX, mouseY) && isLearned;
    }

    /**
     * 버튼 위치 업데이트
     *
     * @param x 새로운 중심 X 좌표
     * @param y 새로운 중심 Y 좌표
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.buttonBounds.setPosition(x, y);
    }

    // ===== Getter & Setter =====

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public ElementalSkill getSkill() { return skill; }
    public void setSkill(ElementalSkill skill) { this.skill = skill; }

    public TextureRegion getSkillIcon() { return skillIcon; }
    public void setSkillIcon(TextureRegion skillIcon) { this.skillIcon = skillIcon; }

    public Circle getButtonBounds() { return buttonBounds; }

    public boolean isPressed() { return isPressed; }
    public boolean isHovered() { return isHovered; }
    public boolean isLearned() { return isLearned; }
    public void setLearned(boolean learned) { this.isLearned = learned; }

    public boolean isSkillReady() {
        return skill != null && skill.isReady() && isLearned;
    }
}
