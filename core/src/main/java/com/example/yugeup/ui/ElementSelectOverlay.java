package com.example.yugeup.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.utils.Constants;

/**
 * 원소 선택 오버레이
 *
 * GameScreen 위에 렌더링되는 원소 선택 UI입니다.
 * Scene2D를 사용하지 않고 SpriteBatch + ShapeRenderer로 구현됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ElementSelectOverlay {
    private Player player;
    private BitmapFont font;
    private TextureAtlas elementalsAtlas;
    private Viewport viewport;

    // 원소 아이콘
    private TextureRegion fireIcon, waterIcon, windIcon, thunderIcon, rackIcon;

    // 버튼 영역
    private Rectangle[] elementButtons;  // 5개 원소 버튼
    private Rectangle confirmButton;     // 확정 버튼

    // 선택 상태
    private ElementType selectedElement = null;
    private ElementType hoveredElement = null;
    private boolean isConfirmed = false;

    // 애니메이션 스케일 (각 원소별)
    private float[] elementScales = new float[5];
    private float[] targetScales = new float[5];

    // 동적 레이아웃 계산
    private float skillPreviewHeight = 0f;  // 스킬 설명 영역 높이
    private float confirmButtonY = 0f;       // 동적 Y 좌표
    private float targetConfirmButtonY = 0f; // 목표 Y 좌표
    private static final float CONFIRM_BUTTON_ANIMATION_SPEED = 0.15f;  // 애니메이션 속도

    // 리스너
    private ElementSelectListener listener;

    /**
     * 원소 선택 오버레이 생성자
     *
     * @param player 플레이어
     * @param font 폰트
     * @param elementalsAtlas 원소 아틀라스
     * @param viewport ViewPort (마우스 좌표 변환용)
     */
    public ElementSelectOverlay(Player player, BitmapFont font, TextureAtlas elementalsAtlas, Viewport viewport) {
        this.player = player;
        this.font = font;
        this.elementalsAtlas = elementalsAtlas;
        this.viewport = viewport;

        // 원소 아이콘 로드
        loadIcons();

        // 버튼 영역 초기화
        initializeButtons();

        // 애니메이션 스케일 초기화
        for (int i = 0; i < 5; i++) {
            elementScales[i] = 1.0f;
            targetScales[i] = 1.0f;
        }
    }

    /**
     * 원소 아이콘 로드
     */
    private void loadIcons() {
        fireIcon = elementalsAtlas.findRegion("fire");
        waterIcon = elementalsAtlas.findRegion("water");
        windIcon = elementalsAtlas.findRegion("wind");
        thunderIcon = elementalsAtlas.findRegion("thunder");
        rackIcon = elementalsAtlas.findRegion("rack");  // 흙
    }

    /**
     * 버튼 영역 초기화
     */
    private void initializeButtons() {
        elementButtons = new Rectangle[5];

        float buttonSize = Constants.ELEMENT_BUTTON_SIZE;
        float spacing = Constants.ELEMENT_BUTTON_SPACING;
        float totalWidth = 5 * buttonSize + 4 * spacing;
        float startX = (Constants.SCREEN_WIDTH - totalWidth) / 2;
        float startY = Constants.SCREEN_HEIGHT / 2 + 50;  // 150 -> 50으로 변경 (아래로 이동)

        for (int i = 0; i < 5; i++) {
            float x = startX + i * (buttonSize + spacing);
            elementButtons[i] = new Rectangle(x, startY, buttonSize, buttonSize);
        }

        // 확정 버튼 - 초기 위치는 스킬 설명 없을 때 기본값
        // 스킬 설명이 생기면 동적으로 아래로 이동
        float initialConfirmY = Constants.SCREEN_HEIGHT / 2 - 200;
        confirmButton = new Rectangle(
            Constants.SCREEN_WIDTH / 2 - 150,
            initialConfirmY,
            300,
            80
        );

        // 동적 Y 좌표 초기화
        this.confirmButtonY = initialConfirmY;
        this.targetConfirmButtonY = initialConfirmY;
    }

    /**
     * 입력 처리
     */
    public void handleInput() {
        // 화면 좌표를 월드 좌표로 변환
        Vector3 screenCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 worldCoords = viewport.unproject(screenCoords);

        float mouseX = worldCoords.x;
        float mouseY = worldCoords.y;

        // 호버 감지
        hoveredElement = null;
        for (int i = 0; i < elementButtons.length; i++) {
            if (elementButtons[i].contains(mouseX, mouseY)) {
                hoveredElement = ElementType.values()[i];
                break;
            }
        }

        // 타겟 스케일 업데이트
        updateTargetScales();

        // 클릭 처리
        if (Gdx.input.justTouched()) {
            // 원소 버튼 클릭 확인
            for (int i = 0; i < elementButtons.length; i++) {
                if (elementButtons[i].contains(mouseX, mouseY)) {
                    ElementType clickedElement = ElementType.values()[i];

                    // 같은 원소를 다시 클릭하면 선택 해제
                    if (selectedElement == clickedElement) {
                        selectedElement = null;
                        System.out.println("[ElementSelectOverlay] 원소 선택 해제");
                    } else {
                        selectedElement = clickedElement;
                        System.out.println("[ElementSelectOverlay] 원소 선택: " + selectedElement.getDisplayName());
                    }
                    return;
                }
            }

            // 확정 버튼 클릭 확인
            if (confirmButton.contains(mouseX, mouseY)) {
                if (selectedElement != null) {
                    player.setElement(selectedElement);
                    isConfirmed = true;

                    if (listener != null) {
                        listener.onElementConfirmed(selectedElement);
                    }

                    System.out.println("[ElementSelectOverlay] 원소 확정: " + selectedElement.getDisplayName());
                } else {
                    System.out.println("[ElementSelectOverlay] 원소를 먼저 선택하세요!");
                }
            }
        }
    }

    /**
     * 타겟 스케일 업데이트
     */
    private void updateTargetScales() {
        for (int i = 0; i < 5; i++) {
            ElementType element = ElementType.values()[i];

            // 선택된 원소 또는 호버된 원소는 확대
            if (element == selectedElement || element == hoveredElement) {
                targetScales[i] = 1.3f;  // 30% 확대
            } else {
                targetScales[i] = 1.0f;  // 원래 크기
            }
        }
    }

    /**
     * 렌더링
     *
     * @param batch SpriteBatch
     * @param shapeRenderer ShapeRenderer
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // 애니메이션 스케일 업데이트 (부드러운 전환)
        for (int i = 0; i < 5; i++) {
            float diff = targetScales[i] - elementScales[i];
            elementScales[i] += diff * 0.15f;  // 부드러운 보간
        }

        // 어두운 오버레이 배경
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);  // 검은색 80% 투명도
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        // 배치 시작
        batch.begin();

        // 제목
        font.getData().setScale(2.5f);
        font.setColor(Color.YELLOW);
        String title = "당신의 원소를 선택하세요";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, title);
        font.draw(batch, title, (Constants.SCREEN_WIDTH - layout.width) / 2, Constants.SCREEN_HEIGHT / 2 + 350);

        // 원소 버튼 렌더링
        renderElementButtons(batch, shapeRenderer);

        // 스킬 미리보기 (선택된 원소가 있으면 렌더링)
        // 스킬 설명 시작 Y 좌표 통일 (힌트 텍스트와 같은 위치에서 시작)
        float skillDescriptionStartY = Constants.SCREEN_HEIGHT / 2 - 50;

        if (selectedElement != null) {
            // 스킬 설명 높이 계산 후 확정 버튼 Y 위치 업데이트
            calculateSkillPreviewHeight(batch);
            renderSkillPreview(batch, skillDescriptionStartY);

            // 확정 버튼 Y 좌표: 스킬 설명 끝 아래 80px
            targetConfirmButtonY = skillDescriptionStartY - skillPreviewHeight - 80f;
        } else {
            // 스킬 설명이 없으면 확정 버튼을 기본 위치로
            font.getData().setScale(1.2f);
            font.setColor(Color.LIGHT_GRAY);
            String hint = "원소를 선택하면 3가지 스킬을 확인할 수 있습니다";
            layout.setText(font, hint);
            font.draw(batch, hint, (Constants.SCREEN_WIDTH - layout.width) / 2, skillDescriptionStartY);

            targetConfirmButtonY = Constants.SCREEN_HEIGHT / 2 - 200;
        }

        // 확정 버튼 Y 좌표 애니메이션 (부드럽게 이동)
        float diffY = targetConfirmButtonY - confirmButtonY;
        confirmButtonY += diffY * CONFIRM_BUTTON_ANIMATION_SPEED;

        // 확정 버튼 위치 업데이트
        confirmButton.y = confirmButtonY;

        // 확정 버튼
        renderConfirmButton(batch, shapeRenderer);

        batch.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    /**
     * 스킬 설명 영역의 높이를 계산합니다.
     * (렌더링 전에 높이를 미리 계산하여 확정 버튼 위치를 결정)
     */
    private void calculateSkillPreviewHeight(SpriteBatch batch) {
        if (selectedElement == null) {
            skillPreviewHeight = 0f;
            return;
        }

        // 원소 설명 높이
        float descHeight = 40f;

        // 스킬 목록 높이 (3개 스킬 * 40픽셀)
        float skillListHeight = 3 * 40f;

        // 전체 높이
        skillPreviewHeight = descHeight + skillListHeight + 20f;  // 20px 패딩
    }

    /**
     * 원소 버튼 렌더링
     */
    private void renderElementButtons(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        ElementType[] elements = ElementType.values();
        TextureRegion[] icons = {fireIcon, waterIcon, windIcon, thunderIcon, rackIcon};

        // 아이콘 및 텍스트 렌더링 (배경색 제거, 이미지만 표시)
        for (int i = 0; i < elementButtons.length; i++) {
            Rectangle button = elementButtons[i];
            TextureRegion icon = icons[i];
            float scale = elementScales[i];

            // 아이콘 크기와 위치 계산 (스케일 적용)
            if (icon != null) {
                float baseSize = 150;  // 기본 아이콘 크기
                float iconSize = baseSize * scale;

                // 중앙 기준 스케일 적용
                float centerX = button.x + button.width / 2;
                float centerY = button.y + button.height / 2;
                float iconX = centerX - iconSize / 2;
                float iconY = centerY - iconSize / 2;

                // 선택된 원소는 밝게 표시
                if (elements[i] == selectedElement) {
                    batch.setColor(1f, 1f, 1f, 1f);  // 밝게
                } else if (elements[i] == hoveredElement) {
                    batch.setColor(0.9f, 0.9f, 0.9f, 1f);  // 약간 밝게
                } else {
                    batch.setColor(0.7f, 0.7f, 0.7f, 1f);  // 약간 어둡게
                }

                batch.draw(icon, iconX, iconY, iconSize, iconSize);
                batch.setColor(Color.WHITE);  // 색상 복원
            }

            // 원소 이름 (아이콘 아래)
            font.getData().setScale(1.3f * scale);

            // 선택 상태에 따라 색상 변경
            if (elements[i] == selectedElement) {
                font.setColor(Color.YELLOW);
            } else if (elements[i] == hoveredElement) {
                font.setColor(Color.CYAN);
            } else {
                font.setColor(Color.WHITE);
            }

            String name = elements[i].getDisplayName();
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, name);
            font.draw(batch, name, button.x + (button.width - layout.width) / 2, button.y + 30);
        }
    }

    /**
     * 스킬 미리보기 렌더링
     *
     * @param batch SpriteBatch
     * @param startY 스킬 설명 시작 Y 좌표
     */
    private void renderSkillPreview(SpriteBatch batch, float startY) {
        // 원소 설명
        font.getData().setScale(1.2f);
        font.setColor(Color.CYAN);
        String desc = selectedElement.getDescription();
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, desc);
        font.draw(batch, desc, (Constants.SCREEN_WIDTH - layout.width) / 2, startY);

        // 스킬 목록
        String[] skillNames = selectedElement.getSkillNames();
        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
        float skillListStartY = startY - 50f;  // 원소 설명 아래 50px

        for (int i = 0; i < skillNames.length; i++) {
            String skillText = "스킬 " + (char)('A' + i) + ": " + skillNames[i];
            font.draw(batch, skillText, Constants.SCREEN_WIDTH / 2 - 200, skillListStartY - i * 40);
        }
    }

    /**
     * 확정 버튼 렌더링 (둥근 모서리)
     */
    private void renderConfirmButton(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 버튼 배경 (둥근 모서리)
        if (selectedElement != null) {
            shapeRenderer.setColor(0.3f, 0.7f, 0.3f, 1f);  // 초록색
        } else {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);  // 회색
        }

        // 둥근 사각형 렌더링 (모서리 반경 15px)
        renderRoundedRect(shapeRenderer, confirmButton.x, confirmButton.y,
                         confirmButton.width, confirmButton.height, 15f);

        shapeRenderer.end();
        batch.begin();

        // 버튼 텍스트
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        String buttonText = "선택 확정";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, buttonText);
        font.draw(batch, buttonText, confirmButton.x + (confirmButton.width - layout.width) / 2,
                 confirmButton.y + (confirmButton.height + layout.height) / 2);
    }

    /**
     * 둥근 사각형 렌더링 (ShapeRenderer 필요)
     */
    private void renderRoundedRect(ShapeRenderer sr, float x, float y, float width, float height, float radius) {
        // 모서리가 너무 크면 조정
        float r = Math.min(radius, Math.min(width / 2, height / 2));

        // 중앙 사각형 (가로)
        sr.rect(x + r, y, width - r * 2, height);

        // 좌우 사각형 (세로)
        sr.rect(x, y + r, width, height - r * 2);

        // 4개의 모서리 원
        sr.circle(x + r, y + r, r, 10);
        sr.circle(x + width - r, y + r, r, 10);
        sr.circle(x + r, y + height - r, r, 10);
        sr.circle(x + width - r, y + height - r, r, 10);
    }

    /**
     * 확정 완료 여부
     */
    public boolean isConfirmed() {
        return isConfirmed;
    }

    /**
     * 리스너 설정
     */
    public void setListener(ElementSelectListener listener) {
        this.listener = listener;
    }

    /**
     * 원소 선택 리스너 인터페이스
     */
    public interface ElementSelectListener {
        void onElementConfirmed(ElementType element);
    }
}
