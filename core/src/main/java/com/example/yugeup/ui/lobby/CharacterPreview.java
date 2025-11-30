package com.example.yugeup.ui.lobby;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.example.yugeup.utils.Constants;

/**
 * 캐릭터 프리뷰 UI
 *
 * 로비 및 대기실에서 캐릭터를 미리 보여줍니다.
 * 닉네임 표시 및 커스터마이징 기능을 제공합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class CharacterPreview {

    // 패널 위치 및 크기
    private float x;
    private float y;
    private float width;
    private float height;

    // 캐릭터 정보
    private String nickname;
    private TextureRegion characterRegion;

    // 버튼 상태
    private Rectangle customizeButtonBounds;
    private Rectangle startButtonBounds;

    // 렌더링 도구
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // 버튼 활성화 상태
    private boolean startButtonEnabled;

    /**
     * CharacterPreview 생성자
     *
     * @param x 패널 X 위치
     * @param y 패널 Y 위치
     * @param width 패널 너비
     * @param height 패널 높이
     * @param font 사용할 폰트
     */
    public CharacterPreview(float x, float y, float width, float height, BitmapFont font) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;

        // 기본 닉네임 설정
        this.nickname = Constants.DEFAULT_NICKNAME_PREFIX + (int)(Math.random() * 1000);
        this.characterRegion = null;  // TODO: PHASE_06에서 캐릭터 텍스처 로드

        this.shapeRenderer = new ShapeRenderer();
        this.startButtonEnabled = false;  // 로비에서는 사용 안 함

        // [외형변경] 버튼 영역 초기화 (중앙 하단)
        this.customizeButtonBounds = new Rectangle(
            x + (width - Constants.LOBBY_BUTTON_WIDTH) / 2,
            y + 180,
            Constants.LOBBY_BUTTON_WIDTH,
            Constants.LOBBY_BUTTON_HEIGHT
        );

        // [게임시작] 버튼은 WaitingRoomScreen에서만 사용
        this.startButtonBounds = null;
    }

    /**
     * 버튼 영역을 현재 x, y, width, height 기준으로 재계산합니다.
     */
    private void updateButtonBounds() {
        // [외형변경] 버튼 (중앙 하단)
        this.customizeButtonBounds.set(
            x + (width - Constants.LOBBY_BUTTON_WIDTH) / 2,
            y + 180,
            Constants.LOBBY_BUTTON_WIDTH,
            Constants.LOBBY_BUTTON_HEIGHT
        );

        // [게임시작] 버튼 (WaitingRoomScreen에서 사용)
        if (startButtonBounds != null) {
            this.startButtonBounds.set(
                x + (width - Constants.LOBBY_BUTTON_WIDTH) / 2,
                y + 100,
                Constants.LOBBY_BUTTON_WIDTH,
                Constants.LOBBY_BUTTON_HEIGHT
            );
        }
    }

    /**
     * 캐릭터 프리뷰를 렌더링합니다.
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        // UI 디버거 활성화 시 동적으로 위치/크기 업데이트
        if (com.example.yugeup.utils.UIDebugger.isEnabled()) {
            this.x = com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewX();
            this.y = com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewY();
            this.width = com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewWidth();
            this.height = com.example.yugeup.utils.UIDebugger.getLobbyCharPreviewHeight();
        }

        // 버튼 영역 항상 재계산
        updateButtonBounds();

        // 배경 (UI 디버거 활성화 시 와이어프레임 표시)
        if (com.example.yugeup.utils.UIDebugger.isEnabled()) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
            batch.begin();
        }

        // 캐릭터 미리보기 영역 (상단 중앙, 더 큰 사이즈)
        float characterSize = 300f;  // 300x300 크기
        float characterX = x + (width - characterSize) / 2;
        float characterY = y + height - 400;

        if (characterRegion != null) {
            // 캐릭터 리전 렌더링
            batch.draw(characterRegion, characterX, characterY, characterSize, characterSize);
        } else {
            // 임시 플레이스홀더
            font.setColor(Color.GRAY);
            font.draw(batch, "[캐릭터]", characterX + 100, characterY + 150);
        }

        // 닉네임 표시 (캐릭터 아래, 중앙 정렬)
        font.setColor(Color.BLACK);
        String nicknameText = nickname.isEmpty() ? "닉네임: (미설정)" : "닉네임: " + nickname;
        float nicknameWidth = nicknameText.length() * 18;
        float nicknameX = x + (width - nicknameWidth) / 2;
        font.draw(batch, nicknameText, nicknameX, characterY - 30);

        // [외형변경] 버튼 (중앙 하단)
        font.setColor(Color.BLUE);
        float buttonX = x + (width - 200) / 2;
        font.draw(batch, "[외형변경]", buttonX + 40, y + 200);
    }

    /**
     * 터치 입력을 처리합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 처리 여부
     */
    public boolean handleTouch(float touchX, float touchY) {
        // [외형변경] 버튼 클릭
        if (customizeButtonBounds.contains(touchX, touchY)) {
            return true;
        }

        // [게임시작] 버튼 클릭
        if (startButtonEnabled && startButtonBounds.contains(touchX, touchY)) {
            return true;
        }

        return false;
    }

    /**
     * [외형변경] 버튼이 클릭되었는지 확인합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 클릭 여부
     */
    public boolean isCustomizeButtonClicked(float touchX, float touchY) {
        return customizeButtonBounds.contains(touchX, touchY);
    }

    /**
     * [게임시작] 버튼이 클릭되었는지 확인합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 클릭 여부
     */
    public boolean isStartButtonClicked(float touchX, float touchY) {
        return startButtonEnabled && startButtonBounds.contains(touchX, touchY);
    }

    /**
     * 닉네임을 설정합니다.
     *
     * @param nickname 새 닉네임
     */
    public void setNickname(String nickname) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
    }

    /**
     * 현재 닉네임을 반환합니다.
     *
     * @return 닉네임
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 캐릭터 텍스처 리전을 설정합니다.
     *
     * @param region 캐릭터 텍스처 리전
     */
    public void setCharacterRegion(TextureRegion region) {
        this.characterRegion = region;
    }

    /**
     * [게임시작] 버튼 활성화 상태를 설정합니다.
     *
     * @param enabled 활성화 여부
     */
    public void setStartButtonEnabled(boolean enabled) {
        this.startButtonEnabled = enabled;
    }

    /**
     * 리소스를 해제합니다.
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
