package com.example.yugeup.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.yugeup.game.level.LevelSystem;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.player.PlayerStats;
import com.example.yugeup.game.skill.ElementSkillSet;
import com.example.yugeup.game.skill.ElementType;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD 렌더러
 *
 * HP/MP/EXP 바 및 레벨 정보를 화면에 표시합니다.
 * 판타지 RPG 스타일의 UI를 제공합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class HUDRenderer {
    private Player player;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera hudCamera;  // HUD 전용 카메라
    private TextureAtlas elementalsAtlas;  // 원소 아이콘 아틀라스
    private TextureAtlas skillsAtlas;      // 스킬 아이콘 아틀라스

    // 스킬 버튼 목록
    private List<SkillButtonComponent> skillButtons;

    // 매직미사일 ON/OFF 버튼
    private MagicMissileButton magicMissileButton;

    // 스킬 방향 표시기
    private SkillDirectionIndicator directionIndicator;

    // UI 위치 및 크기 (화면 좌측 상단) - Constants 기준: 2856 x 1280
    private static final float BAR_WIDTH = 420f;       // 바 너비 (300 * 1.4 = 420)
    private static final float BAR_HEIGHT = 60f;       // 바 높이 (로딩바 스타일)
    private static final float BAR_PADDING = 6f;       // 바 내부 패딩
    private static final float BAR_SPACING = 15f;      // 바 사이 간격
    private static final float BAR_OFFSET_TOP = 150f;  // 화면 상단으로부터의 거리 (80f → 150f로 증가)
    private static final float BAR_X = 100f;           // 바 시작 X 좌표 (화면 안쪽으로, 40f → 100f로 증가)
    private static final float CORNER_RADIUS = 20f;    // 둥근 모서리 반경

    // 동적으로 계산되는 바 Y 좌표
    private float hpBarY;        // HP 바 Y 좌표
    private float mpBarY;        // MP 바 Y 좌표
    private float expBarY;       // EXP 바 Y 좌표

    // 원소 아이콘 위치 (EXP 바 오른쪽)
    private static final float ELEMENT_ICON_SIZE = 80f;  // 아이콘 크기
    private float elementIconX;  // 동적 계산 (BAR_X + BAR_WIDTH + 20)
    private float elementIconY;  // 동적 계산

    // 색상 테마 (로딩 스크린 스타일)
    private static final Color HP_COLOR = new Color(0.9f, 0.2f, 0.2f, 1f);      // 빨강 (HP)
    private static final Color HP_BG_COLOR = new Color(0.2f, 0.2f, 0.25f, 1f);
    private static final Color MP_COLOR = new Color(0.2f, 0.6f, 1.0f, 1f);      // 파랑 (MP)
    private static final Color MP_BG_COLOR = new Color(0.2f, 0.2f, 0.25f, 1f);
    private static final Color EXP_COLOR = new Color(1.0f, 0.85f, 0.2f, 1f);    // 금색 (EXP)
    private static final Color EXP_BG_COLOR = new Color(0.2f, 0.2f, 0.25f, 1f);

    /**
     * HUDRenderer 생성자
     *
     * @param player 플레이어
     * @param font 텍스트 폰트
     * @param elementalsAtlas 원소 아이콘 아틀라스
     */
    public HUDRenderer(Player player, BitmapFont font, TextureAtlas elementalsAtlas) {
        this(player, font, elementalsAtlas, null);
    }

    /**
     * HUDRenderer 생성자 (스킬 아이콘 포함)
     *
     * @param player 플레이어
     * @param font 텍스트 폰트
     * @param elementalsAtlas 원소 아이콘 아틀라스
     * @param skillsAtlas 스킬 아이콘 아틀라스
     */
    public HUDRenderer(Player player, BitmapFont font, TextureAtlas elementalsAtlas, TextureAtlas skillsAtlas) {
        this.player = player;
        this.shapeRenderer = new ShapeRenderer();
        this.font = font;
        this.font.getData().setScale(0.8f);  // 폰트 크기 조정
        this.font.setColor(Color.WHITE);
        this.elementalsAtlas = elementalsAtlas;
        this.skillsAtlas = skillsAtlas;

        // HUD 전용 카메라 생성 (화면 고정) - Constants 기준으로 설정
        this.hudCamera = new OrthographicCamera();
        this.hudCamera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        this.hudCamera.update();

        // 바 Y 좌표 동적 계산 (Constants 기반)
        this.hpBarY = Constants.SCREEN_HEIGHT - BAR_OFFSET_TOP;
        this.mpBarY = hpBarY - BAR_HEIGHT - BAR_SPACING;
        this.expBarY = mpBarY - BAR_HEIGHT - BAR_SPACING;

        // 원소 아이콘 위치 계산
        this.elementIconX = BAR_X + BAR_WIDTH + 20f;
        this.elementIconY = expBarY;

        // 스킬 버튼 초기화
        this.skillButtons = new ArrayList<>();
        initializeSkillButtons();

        // 매직미사일 버튼 초기화 (스킬 버튼 왼쪽)
        initializeMagicMissileButton();

        // 방향 표시기 초기화
        this.directionIndicator = new SkillDirectionIndicator();
    }

    /**
     * 스킬 버튼을 초기화합니다.
     */
    private void initializeSkillButtons() {
        // 스킬 버튼들을 화면 우측 하단에 배치 (원형, 더 큰 크기)
        float skillButtonRadius = 100f;  // 반경 60 (지름 120)
        float skillButtonSpacing = 200f;  // 간격

        // HUD 카메라의 실제 viewport 크기 사용 (Constants 대신)
        float viewportWidth = hudCamera.viewportWidth;
        float viewportHeight = hudCamera.viewportHeight;

        System.out.println("[HUDRenderer] HUD 카메라 viewport: " + viewportWidth + " x " + viewportHeight);
        System.out.println("[HUDRenderer] Constants: " + Constants.SCREEN_WIDTH + " x " + Constants.SCREEN_HEIGHT);

        // 우측 정렬: viewport 너비 기준으로 우측에서 왼쪽으로 배치
        float startX = viewportWidth - skillButtonSpacing * 3 - 30f;
        float startY = 300f;  // Y좌표: 하단에서 80 위로

        ElementSkillSet skillSet = player.getElementSkillSet();
        if (skillSet != null) {
            ElementType element = skillSet.getElement();
            System.out.println("[HUDRenderer] 스킬 버튼 초기화: " + element.getDisplayName());
            // 스킬 A, B, C 버튼 생성
            for (int i = 0; i < 3; i++) {
                ElementalSkill skill = skillSet.getSkill(i);
                if (skill != null) {
                    float buttonX = startX + i * skillButtonSpacing;
                    float buttonY = startY - i * skillButtonSpacing;

                    // 배운 스킬인지 확인
                    boolean isLearned = player.hasLearnedSkill(skill.getSkillId());

                    TextureRegion skillIcon = null;
                    if (skillsAtlas != null) {
                        // 원소 타입과 스킬 순서로 아이콘 찾기
                        // 예: "fire-skill1", "water-skill2", "earth-skill3" 등
                        String elementName = getElementNameForAtlas(element);
                        String iconName = elementName + "-skill" + (i + 1);
                        skillIcon = skillsAtlas.findRegion(iconName);
                        System.out.println("[HUDRenderer] 스킬 아이콘 찾기: " + iconName + " -> " + (skillIcon != null ? "성공" : "실패"));
                    }

                    // 원형 버튼 생성 (새로운 생성자)
                    SkillButtonComponent button = new SkillButtonComponent(
                        skill, skillIcon, buttonX, buttonY, skillButtonRadius, isLearned,
                        shapeRenderer, font
                    );
                    skillButtons.add(button);
                    System.out.println("[HUDRenderer] 스킬 버튼 생성: ID=" + skill.getSkillId() + " (" + skill.getName() + ")" +
                        " at (" + buttonX + ", " + buttonY + ") 배운스킬=" + isLearned);
                }
            }
        } else {
            System.out.println("[HUDRenderer] 경고: ElementSkillSet이 null입니다!");
        }
    }

    /**
     * 매직미사일 버튼을 초기화합니다.
     */
    private void initializeMagicMissileButton() {
        // 매직미사일 스킬 가져오기
        com.example.yugeup.game.skill.MagicMissile magicMissile = player.getMagicMissile();
        if (magicMissile == null) {
            System.out.println("[HUDRenderer] 경고: MagicMissile이 null입니다!");
            return;
        }

        // skills.atlas에서 magicwand 아이콘 로드
        TextureRegion magicwandIcon = null;
        if (skillsAtlas != null) {
            magicwandIcon = skillsAtlas.findRegion("magicwand");
            System.out.println("[HUDRenderer] magicwand 아이콘 로드: " +
                (magicwandIcon != null ? "성공" : "실패"));
        }

        // 스킬 버튼 왼쪽 위에 배치 (이미지 참고)
        float viewportWidth = hudCamera.viewportWidth;
        float skillButtonSpacing = 140f;
        float startX = viewportWidth - skillButtonSpacing * 3 - 30f;

        // 매직미사일 버튼은 스킬 A 왼쪽 위에 배치
        float mmButtonX = viewportWidth - 350f;  // 스킬 A 왼쪽에서 더 멀리
        float mmButtonY = 300f;  // 스킬 버튼보다 위에 (80 → 200)
        float mmButtonRadius = 120f;  // 반경 60 (스킬 버튼과 동일)

        // 아이콘 및 ShapeRenderer 전달
        magicMissileButton = new MagicMissileButton(
            magicMissile, mmButtonX, mmButtonY, mmButtonRadius, magicwandIcon, shapeRenderer);
        System.out.println("[HUDRenderer] 매직미사일 버튼 생성: at (" + mmButtonX + ", " + mmButtonY + "), radius=" + mmButtonRadius);
    }

    /**
     * 스킬 버튼 위치를 viewport 크기에 맞게 재조정합니다.
     * (화면 크기 변경 또는 초기화 오류 시 호출)
     */
    public void repositionSkillButtons() {
        if (skillButtons == null || skillButtons.isEmpty()) {
            return;
        }

        float skillButtonSpacing = 180f;
        float viewportWidth = hudCamera.viewportWidth;
        float startX = viewportWidth - skillButtonSpacing * 3 - 180f;
        float startY = 320f;

        System.out.println("[HUDRenderer] 스킬 버튼 위치 재조정: viewport=" + viewportWidth);

        for (int i = 0; i < skillButtons.size(); i++) {
            float buttonX = startX + i * skillButtonSpacing;
            float buttonY = startY + i * skillButtonSpacing;
            skillButtons.get(i).setPosition(buttonX, buttonY);
            System.out.println("[HUDRenderer] 버튼 " + i + " 위치 업데이트: (" + buttonX + ", " + startY + ")");
        }
    }

    /**
     * HUD 렌더링
     *
     * @param batch SpriteBatch (텍스트용)
     * @param camera 게임 월드 카메라 (사용하지 않음, HUD는 자체 카메라 사용)
     */
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        PlayerStats stats = player.getStats();
        LevelSystem levelSystem = player.getLevelSystem();

        // 매 프레임 스킬 버튼의 배운 상태 갱신
        updateSkillButtonLearningStatus();

        // HUD 카메라로 전환
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        // HP 바 렌더링
        float hpRatio = stats.getHealthRatio();
        renderBar(BAR_X, hpBarY, BAR_WIDTH, BAR_HEIGHT, hpRatio, HP_COLOR, HP_BG_COLOR);

        // MP 바 렌더링
        float mpRatio = stats.getManaRatio();
        renderBar(BAR_X, mpBarY, BAR_WIDTH, BAR_HEIGHT, mpRatio, MP_COLOR, MP_BG_COLOR);

        // EXP 바 렌더링
        float expRatio = levelSystem.getExpRatio();
        renderBar(BAR_X, expBarY, BAR_WIDTH, BAR_HEIGHT, expRatio, EXP_COLOR, EXP_BG_COLOR);

        // 원소 아이콘 렌더링 (선택한 원소가 있을 때)
        ElementType selectedElement = player.getSelectedElement();
        if (selectedElement != null) {
            renderElementIcon(batch, selectedElement);
        }

        // 스킬 버튼 렌더링 (직접 렌더링)
        renderSkillButtons(batch);

        // 매직미사일 버튼 렌더링
        if (magicMissileButton != null) {
            batch.begin();
            magicMissileButton.render(batch);
            batch.end();
        }
    }

    /**
     * 스킬 버튼들을 렌더링합니다.
     *
     * @param batch SpriteBatch
     */
    private void renderSkillButtons(SpriteBatch batch) {
        if (skillButtons == null || skillButtons.isEmpty()) {
            return;
        }

        batch.begin();
        for (SkillButtonComponent skillButton : skillButtons) {
            // 각 스킬 버튼은 자체 render() 메서드로 렌더링
            skillButton.render(batch);
        }
        batch.end();
    }


    /**
     * 선택한 원소 아이콘 렌더링
     *
     * @param batch SpriteBatch
     * @param element 선택한 원소
     */
    private void renderElementIcon(SpriteBatch batch, ElementType element) {
        // 원소에 맞는 아이콘 가져오기
        String iconName = getElementIconName(element);
        TextureRegion icon = elementalsAtlas.findRegion(iconName);

        if (icon != null) {
            batch.begin();
            batch.draw(icon, elementIconX, elementIconY, ELEMENT_ICON_SIZE, ELEMENT_ICON_SIZE);
            batch.end();
        }
    }

    /**
     * 원소 타입에 따른 아이콘 이름 반환
     *
     * @param element 원소 타입
     * @return 아이콘 이름
     */
    private String getElementIconName(ElementType element) {
        switch (element) {
            case FIRE: return "fire";
            case WATER: return "water";
            case WIND: return "wind";
            case LIGHTNING: return "thunder";
            case EARTH: return "rack";
            default: return "fire";
        }
    }

    /**
     * 원소 타입에 따른 아틀라스 이름 반환 (스킬 아이콘 검색용)
     *
     * @param element 원소 타입
     * @return 아틀라스에서 사용하는 원소 이름 (fire, water, wind, lightning, earh)
     */
    private String getElementNameForAtlas(ElementType element) {
        switch (element) {
            case FIRE: return "fire";
            case WATER: return "water";
            case WIND: return "wind";
            case LIGHTNING: return "lightning";
            case EARTH: return "earh";  // atlas 파일에 오타가 있음 (earth -> earh)
            default: return "fire";
        }
    }

    /**
     * 둥근 사각형 바 렌더링 (로딩 스크린 스타일)
     *
     * @param x X 좌표
     * @param y Y 좌표
     * @param width 바 너비
     * @param height 바 높이
     * @param ratio 채움 비율 (0.0 ~ 1.0)
     * @param fillColor 채움 색상
     * @param bgColor 배경 색상
     */
    private void renderBar(float x, float y, float width, float height,
                          float ratio, Color fillColor, Color bgColor) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 배경 (둥근 사각형)
        shapeRenderer.setColor(bgColor);
        renderRoundedRect(shapeRenderer, x, y, width, height, CORNER_RADIUS);

        // 채움 (진행도, 둥근 사각형)
        if (ratio > 0.01f) {
            float fillWidth = Math.max(CORNER_RADIUS * 2, (width - BAR_PADDING * 2) * ratio);
            shapeRenderer.setColor(fillColor);
            float innerRadius = CORNER_RADIUS - BAR_PADDING;
            renderRoundedRect(shapeRenderer, x + BAR_PADDING, y + BAR_PADDING,
                             fillWidth, height - BAR_PADDING * 2, innerRadius);
        }

        shapeRenderer.end();
    }

    /**
     * 둥근 사각형 그리기 (로딩 스크린 스타일)
     */
    private void renderRoundedRect(ShapeRenderer sr, float x, float y, float width, float height, float radius) {
        // 모서리가 너무 크면 조정
        float r = Math.min(radius, Math.min(width / 2, height / 2));

        // 중앙 사각형 (가로)
        sr.rect(x + r, y, width - r * 2, height);

        // 좌우 사각형 (세로)
        sr.rect(x, y + r, width, height - r * 2);

        // 4개의 모서리 원 (10 segments로 로딩바와 동일하게)
        sr.circle(x + r, y + r, r, 10);
        sr.circle(x + width - r, y + r, r, 10);
        sr.circle(x + r, y + height - r, r, 10);
        sr.circle(x + width - r, y + height - r, r, 10);
    }

    /**
     * 리소스 해제
     */
    public void dispose() {
        shapeRenderer.dispose();
        // font는 외부에서 관리하므로 dispose하지 않음
    }

    /**
     * 플레이어 변경
     *
     * @param player 새 플레이어
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * 스킬 버튼 터치 다운 처리
     *
     * @param touchX 터치 X 좌표 (스크린 좌표)
     * @param touchY 터치 Y 좌표 (스크린 좌표)
     * @return 스킬이 발동된 경우 발동한 스킬, null이면 발동되지 않음
     */
    public ElementalSkill handleSkillButtonTouchDown(float touchX, float touchY) {
        // 스크린 좌표를 HUD viewport 좌표로 변환
        // 스크린 크기와 viewport 크기가 다를 수 있으므로 스케일 계산 필요

        // 실제 화면 크기 (픽셀)
        float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
        float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();

        // HUD viewport 크기
        float viewportWidth = hudCamera.viewportWidth;
        float viewportHeight = hudCamera.viewportHeight;

        // 스케일 계산
        float scaleX = viewportWidth / screenWidth;
        float scaleY = viewportHeight / screenHeight;

        // 스크린 좌표를 viewport 좌표로 변환
        float hudX = touchX * scaleX;
        float hudY = viewportHeight - (touchY * scaleY);

        System.out.println("[HUDRenderer] 실제 화면 크기: " + screenWidth + " x " + screenHeight);
        System.out.println("[HUDRenderer] Viewport 크기: " + viewportWidth + " x " + viewportHeight);
        System.out.println("[HUDRenderer] 스케일: " + scaleX + " x " + scaleY);
        System.out.println("[HUDRenderer] 터치 좌표 변환: 스크린(" + touchX + ", " + touchY + ") -> HUD(" + hudX + ", " + hudY + ")");
        System.out.println("[HUDRenderer] 스킬 버튼 개수: " + skillButtons.size());

        // 매직미사일 버튼 먼저 체크
        if (magicMissileButton != null && magicMissileButton.isTouched(hudX, hudY)) {
            System.out.println("[HUDRenderer] 매직미사일 버튼 클릭!");
            magicMissileButton.onClick();
            return null;  // ElementalSkill이 아니므로 null 반환
        }

        for (int i = 0; i < skillButtons.size(); i++) {
            SkillButtonComponent skillButton = skillButtons.get(i);
            System.out.println("[HUDRenderer] 버튼 " + i + ": 중심(" + skillButton.getX() + ", " + skillButton.getY() + ") 반경=" + skillButton.getRadius());

            // 거리 계산
            float dx = hudX - skillButton.getX();
            float dy = hudY - skillButton.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            System.out.println("[HUDRenderer] 버튼 " + i + " 거리: " + distance + " (반경: " + skillButton.getRadius() + ")");

            if (skillButton.handleTouchDown(hudX, hudY)) {
                System.out.println("[HUDRenderer] ✅ 스킬 버튼 " + i + " 클릭됨: " + skillButton.getSkill().getName());
                return skillButton.getSkill();
            }
        }
        System.out.println("[HUDRenderer] ❌ 어떤 버튼도 클릭되지 않음");
        return null;
    }

    /**
     * 스킬 버튼 터치 업 처리
     */
    public void handleSkillButtonTouchUp() {
        for (SkillButtonComponent skillButton : skillButtons) {
            skillButton.handleTouchUp();
        }
    }

    /**
     * 스킬 버튼 호버 상태 업데이트 (마우스 포지션)
     *
     * @param mouseX 마우스 X 좌표 (스크린 좌표)
     * @param mouseY 마우스 Y 좌표 (스크린 좌표)
     */
    public void updateSkillButtonHover(float mouseX, float mouseY) {
        float hudX = mouseX;
        float hudY = Constants.SCREEN_HEIGHT - mouseY;

        for (SkillButtonComponent skillButton : skillButtons) {
            skillButton.updateHover(hudX, hudY);
        }
    }

    /**
     * 스킬 버튼의 배운 상태를 갱신합니다 (매 프레임)
     *
     * 플레이어가 새로운 스킬을 배우면 버튼의 isLearned 상태를 즉시 반영합니다.
     */
    private void updateSkillButtonLearningStatus() {
        if (skillButtons == null || skillButtons.isEmpty()) {
            return;
        }

        for (SkillButtonComponent button : skillButtons) {
            if (button.getSkill() != null) {
                // 현재 스킬이 배워진 스킬인지 확인
                int skillId = button.getSkill().getSkillId();
                boolean isLearned = player.hasLearnedSkill(skillId);

                // 디버그 로그 (상태 변경 시에만 출력)
                if (isLearned != button.isLearned()) {
                    System.out.println("[HUDRenderer] 스킬 상태 변경: ID=" + skillId + " -> " + isLearned);
                }

                // 버튼의 배운 상태 갱신
                button.setLearned(isLearned);
            }
        }
    }

    /**
     * 스킬 버튼 목록 반환
     *
     * @return 스킬 버튼 리스트
     */
    public List<SkillButtonComponent> getSkillButtons() {
        return skillButtons;
    }

    /**
     * 스킬 방향 표시기 반환
     *
     * @return 방향 표시기
     */
    public SkillDirectionIndicator getDirectionIndicator() {
        return directionIndicator;
    }

    /**
     * 스킬 방향 표시기 렌더링 (게임 월드 좌표계)
     *
     * @param worldCamera 월드 카메라
     */
    public void renderDirectionIndicator(com.badlogic.gdx.graphics.OrthographicCamera worldCamera) {
        if (directionIndicator != null && directionIndicator.isActive()) {
            shapeRenderer.setProjectionMatrix(worldCamera.combined);
            directionIndicator.render(shapeRenderer);
        }
    }
}
