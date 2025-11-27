package com.example.yugeup.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.upgrade.UpgradeManager;
import com.example.yugeup.game.upgrade.UpgradeOption;
import com.example.yugeup.utils.AssetManager;
import com.example.yugeup.utils.Constants;

import java.util.List;

/**
 * 레벨업 업그레이드 패널 (Phase 19 재설계)
 *
 * 레벨 5 이상에서 무작위 3가지 업그레이드 중 선택합니다.
 * - 스킬 강화: 데미지, 쿨타임 (각 최대 5레벨)
 * - 스탯 강화: HP, MP, MP재생, 공격력, 이동속도
 *
 * @author YuGeup Development Team
 * @version 2.0
 */
public class LevelUpUpgradePanel {
    private Player player;
    private UpgradeManager upgradeManager;
    private int currentLevel;

    // UI 자산
    private Texture cardFrameTexture;
    private BitmapFont font;

    // 업그레이드 옵션 3가지
    private List<UpgradeOption> upgradeOptions;
    private UpgradeOptionCard[] optionCards;
    private int selectedIndex = -1;
    private boolean isVisible = false;

    // 카드 위치 및 크기 (92x83 이미지를 약 7배 확대)
    private static final float CARD_WIDTH = 644f;   // 92 * 7
    private static final float CARD_HEIGHT = 581f;  // 83 * 7
    private static final float CARD_SPACING = 80f;

    /**
     * 레벨업 업그레이드 패널 생성자
     *
     * @param player 플레이어 엔티티
     * @param currentLevel 현재 레벨
     */
    public LevelUpUpgradePanel(Player player, int currentLevel) {
        this.player = player;
        this.currentLevel = currentLevel;
        // Player가 가진 UpgradeManager를 사용 (마나 재생 등 공유)
        this.upgradeManager = player.getUpgradeManager();

        // UI 자산 로드
        AssetManager assetManager = AssetManager.getInstance();
        this.font = assetManager.getFont("font_small");

        // 카드 프레임 이미지 로드
        try {
            this.cardFrameTexture = new Texture(Gdx.files.internal("images/backgrounds/create-room-frame.png"));
            System.out.println("[LevelUpUpgradePanel] 카드 프레임 로드 완료");
        } catch (Exception e) {
            System.out.println("[LevelUpUpgradePanel] 카드 프레임 로드 실패: " + e.getMessage());
            this.cardFrameTexture = null;
        }

        // 카드 배열 초기화
        this.optionCards = new UpgradeOptionCard[3];

        System.out.println("[LevelUpUpgradePanel] 초기화 완료 - 레벨: " + currentLevel);
    }

    /**
     * 레벨업 시 호출 - 업그레이드 옵션 생성 및 패널 표시
     *
     * @param newLevel 새로운 레벨
     */
    public void onLevelUp(int newLevel) {
        this.currentLevel = newLevel;

        // 레벨 5 미만이면 자동 스킬 학습만 (2,3,4 레벨에서 스킬 A,B,C 학습)
        if (currentLevel < 5) {
            System.out.println("[LevelUpUpgradePanel] 레벨 " + currentLevel + " - 자동 스킬 학습");
            return;
        }

        // 레벨 5 이상: 무작위 3가지 업그레이드 선택
        this.upgradeOptions = upgradeManager.generateRandomUpgrades();

        // 옵션이 없으면 패널 표시하지 않음
        if (upgradeOptions.isEmpty()) {
            System.out.println("[LevelUpUpgradePanel] 사용 가능한 업그레이드가 없습니다");
            return;
        }

        // 카드 생성
        createOptionCards();

        // 패널 표시
        show();
    }

    /**
     * 업그레이드 옵션 카드 생성
     */
    private void createOptionCards() {
        float centerX = Constants.SCREEN_WIDTH / 2f;
        float centerY = Constants.SCREEN_HEIGHT / 2f;

        // 전체 카드들의 너비 계산
        float totalWidth = CARD_WIDTH * 3 + CARD_SPACING * 2;
        float startX = centerX - totalWidth / 2f;

        System.out.println("[LevelUpUpgradePanel] 카드 생성 시작");
        System.out.println("  - 화면 중앙: (" + centerX + ", " + centerY + ")");
        System.out.println("  - 전체 너비: " + totalWidth);
        System.out.println("  - 시작 X: " + startX);

        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float cardX = startX + i * (CARD_WIDTH + CARD_SPACING);
            float cardY = centerY - CARD_HEIGHT / 2f;

            UpgradeOption option = upgradeOptions.get(i);
            optionCards[i] = new UpgradeOptionCard(option, cardX, cardY, CARD_WIDTH, CARD_HEIGHT);

            System.out.println("  - 카드 " + i + ": " + upgradeManager.getUpgradeDisplayName(option) +
                             " | 위치: (" + cardX + ", " + cardY + ") | 크기: " + CARD_WIDTH + "x" + CARD_HEIGHT);
        }
    }

    /**
     * 입력 처리
     */
    public void handleInput() {
        if (!isVisible) return;
        if (upgradeOptions == null || upgradeOptions.isEmpty()) return;  // 안전 체크

        // 마우스/터치 클릭 감지
        if (Gdx.input.justTouched()) {
            // 디바이스 화면 좌표 가져오기
            float deviceX = Gdx.input.getX();
            float deviceY = Gdx.input.getY();

            // Y축 반전 (libGDX는 원점이 왼쪽 하단, 디바이스는 왼쪽 상단)
            deviceY = Gdx.graphics.getHeight() - deviceY;

            // 디바이스 해상도 → 게임 가상 해상도 변환
            float scaleX = Constants.SCREEN_WIDTH / (float) Gdx.graphics.getWidth();
            float scaleY = Constants.SCREEN_HEIGHT / (float) Gdx.graphics.getHeight();

            float touchX = deviceX * scaleX;
            float touchY = deviceY * scaleY;

            System.out.println("[LevelUpUpgradePanel] 클릭 좌표: (" + touchX + ", " + touchY + ")");
            System.out.println("  - 디바이스 해상도: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
            System.out.println("  - 가상 해상도: " + Constants.SCREEN_WIDTH + "x" + Constants.SCREEN_HEIGHT);
            System.out.println("  - Scale: " + scaleX + "x" + scaleY);

            // 카드 클릭 감지 및 즉시 적용
            for (int i = 0; i < optionCards.length && i < upgradeOptions.size(); i++) {
                if (optionCards[i] != null) {
                    Rectangle bounds = optionCards[i].bounds;
                    boolean contains = bounds.contains(touchX, touchY);
                    System.out.println("  - 카드 " + i + ": " + upgradeManager.getUpgradeDisplayName(upgradeOptions.get(i)) +
                                     " | Bounds: [" + bounds.x + ", " + bounds.y + ", " + bounds.width + ", " + bounds.height + "]" +
                                     " | Contains: " + contains);

                    if (contains) {
                        selectedIndex = i;
                        System.out.println("[LevelUpUpgradePanel] >>> 카드 " + i + " 선택됨! <<<");
                        applyUpgrade();
                        return;
                    }
                }
            }

            System.out.println("[LevelUpUpgradePanel] 카드 클릭 실패 - 모든 카드 범위를 벗어남");
        }
    }

    /**
     * 선택한 업그레이드 적용
     */
    private void applyUpgrade() {
        if (selectedIndex < 0 || selectedIndex >= upgradeOptions.size()) {
            return;
        }

        UpgradeOption selected = upgradeOptions.get(selectedIndex);
        String displayName = upgradeManager.getUpgradeDisplayName(selected);
        System.out.println("[LevelUpUpgradePanel] 선택됨: " + displayName);

        // 업그레이드 적용
        boolean success = upgradeManager.applyUpgrade(selected);

        if (success) {
            System.out.println("[LevelUpUpgradePanel] 업그레이드 적용 완료!");
            hide();
        } else {
            System.out.println("[LevelUpUpgradePanel] 업그레이드 적용 실패");
        }
    }

    /**
     * 렌더링
     */
    public void render(SpriteBatch gameBatch, ShapeRenderer gameShapeRenderer) {
        if (!isVisible) return;
        if (upgradeOptions == null || upgradeOptions.isEmpty()) return;  // 안전 체크

        // 1. 배경 (반투명 검은색 - 게임 화면 위에 투명하게 덮기)
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        gameShapeRenderer.setProjectionMatrix(gameBatch.getProjectionMatrix());
        gameShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameShapeRenderer.setColor(0, 0, 0, 0.6f);  // 투명도 60%
        gameShapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        gameShapeRenderer.end();

        // 2. 카드 배경 먼저 렌더링
        gameBatch.begin();
        for (int i = 0; i < optionCards.length && i < upgradeOptions.size(); i++) {
            if (optionCards[i] != null) {
                optionCards[i].renderBackground(gameBatch, cardFrameTexture);
            }
        }
        gameBatch.end();

        // 3. 카드 테두리 (선택 표시)
        for (int i = 0; i < optionCards.length && i < upgradeOptions.size(); i++) {
            if (optionCards[i] != null && i == selectedIndex) {
                optionCards[i].renderBorder(gameShapeRenderer);
            }
        }

        // 4. 텍스트 렌더링 (가장 위에)
        gameBatch.begin();

        // 레벨업 텍스트 (상단)
        font.getData().setScale(3.0f);
        font.setColor(1, 1, 0, 1);
        String levelUpText = "레벨 " + currentLevel + " 달성!";
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, levelUpText);
        font.draw(gameBatch, levelUpText, Constants.SCREEN_WIDTH / 2f - titleLayout.width / 2f, Constants.SCREEN_HEIGHT - 100);
        font.getData().setScale(1.0f);

        // 선택 안내 텍스트
        font.getData().setScale(1.8f);
        font.setColor(1, 1, 1, 1);
        String instructionText = "업그레이드를 선택하세요";
        com.badlogic.gdx.graphics.g2d.GlyphLayout instructionLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, instructionText);
        font.draw(gameBatch, instructionText, Constants.SCREEN_WIDTH / 2f - instructionLayout.width / 2f, Constants.SCREEN_HEIGHT - 200);
        font.getData().setScale(1.0f);

        // 카드 텍스트
        for (int i = 0; i < optionCards.length && i < upgradeOptions.size(); i++) {
            if (optionCards[i] != null) {
                optionCards[i].renderText(gameBatch, font);
            }
        }

        gameBatch.end();

        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    /**
     * 패널 표시
     */
    public void show() {
        if (upgradeOptions == null || upgradeOptions.isEmpty()) {
            System.out.println("[LevelUpUpgradePanel] 패널 표시 실패 - 업그레이드 옵션 없음");
            return;
        }

        this.isVisible = true;
        this.selectedIndex = -1;
        System.out.println("[LevelUpUpgradePanel] 패널 표시 - " + upgradeOptions.size() + "개 옵션");
    }

    /**
     * 패널 숨김
     */
    public void hide() {
        this.isVisible = false;
        this.selectedIndex = -1;
        System.out.println("[LevelUpUpgradePanel] 패널 숨김");
    }

    /**
     * 패널이 표시 중인지 확인
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * 리소스 정리
     */
    public void dispose() {
        if (cardFrameTexture != null) {
            cardFrameTexture.dispose();
        }
    }

    /**
     * 업그레이드 옵션 카드
     */
    private class UpgradeOptionCard {
        UpgradeOption option;
        Rectangle bounds;
        String levelText;

        UpgradeOptionCard(UpgradeOption option, float x, float y, float width, float height) {
            this.option = option;
            this.bounds = new Rectangle(x, y, width, height);

            // 현재 레벨 텍스트 (화살표 대신 > 사용)
            int currentLevel = upgradeManager.getUpgradeLevel(option);
            this.levelText = "Lv " + currentLevel + " > " + (currentLevel + 1);
        }

        /**
         * 카드 배경 렌더링
         */
        void renderBackground(SpriteBatch batch, Texture frameTexture) {
            if (frameTexture != null) {
                batch.draw(frameTexture, bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        /**
         * 카드 테두리 렌더링 (선택 시)
         */
        void renderBorder(ShapeRenderer shapeRenderer) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(8);
            shapeRenderer.setColor(1, 1, 0, 1);
            shapeRenderer.rect(bounds.x - 15, bounds.y - 15, bounds.width + 30, bounds.height + 30);
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1);
        }

        /**
         * 카드 텍스트 렌더링
         */
        void renderText(SpriteBatch batch, BitmapFont font) {
            float centerX = bounds.x + bounds.width / 2f;
            float textY = bounds.y + bounds.height / 2f + 150f;

            // 옵션 이름 (크게, 검은색) - 실제 스킬명 사용
            font.getData().setScale(1.5f);
            font.setColor(0, 0, 0, 1);  // 검은색
            String displayName = upgradeManager.getUpgradeDisplayName(option);
            com.badlogic.gdx.graphics.g2d.GlyphLayout nameLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, displayName);
            font.draw(batch, displayName, centerX - nameLayout.width / 2f, textY);

            // 효과 설명 (중간, 검은색)
            font.getData().setScale(1.2f);
            font.setColor(0, 0, 0, 1);  // 검은색
            String effectText = upgradeManager.getUpgradePreviewText(option);
            com.badlogic.gdx.graphics.g2d.GlyphLayout effectLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, effectText);
            font.draw(batch, effectText, centerX - effectLayout.width / 2f, textY - 80);

            // 레벨 표시 (작게, 검은색)
            font.getData().setScale(1.0f);
            font.setColor(0, 0, 0, 1);  // 검은색
            com.badlogic.gdx.graphics.g2d.GlyphLayout levelLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, levelText);
            font.draw(batch, levelText, centerX - levelLayout.width / 2f, textY - 150);

            font.getData().setScale(1.0f);
        }
    }
}
