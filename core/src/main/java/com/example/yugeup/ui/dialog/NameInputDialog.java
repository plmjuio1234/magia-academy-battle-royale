package com.example.yugeup.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.example.yugeup.utils.Constants;

/**
 * 이름 입력 다이얼로그
 *
 * 플레이어 이름을 입력받습니다.
 * 모달 형태로 화면 중앙에 표시됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class NameInputDialog {

    // 다이얼로그 크기
    private static final float DIALOG_WIDTH = Constants.SCREEN_WIDTH * 0.6f;
    private static final float DIALOG_HEIGHT = Constants.SCREEN_HEIGHT * 0.35f;
    private static final float DIALOG_X = (Constants.SCREEN_WIDTH - DIALOG_WIDTH) / 2f;
    private static final float DIALOG_Y = (Constants.SCREEN_HEIGHT - DIALOG_HEIGHT) / 2f;

    // 입력 필드 영역
    private static final float INPUT_FIELD_HEIGHT = 80f;
    private static final float INPUT_FIELD_MARGIN = 50f;
    private static final float NAME_INPUT_X = DIALOG_X + INPUT_FIELD_MARGIN;
    private static final float NAME_INPUT_Y = DIALOG_Y + DIALOG_HEIGHT - 180f;
    private static final float NAME_INPUT_WIDTH = DIALOG_WIDTH - (INPUT_FIELD_MARGIN * 2);

    // 버튼 크기
    private static final float BUTTON_WIDTH = 250f;
    private static final float BUTTON_HEIGHT = 80f;
    private static final float BUTTON_SPACING = 50f;
    private static final float BUTTON_Y = DIALOG_Y + 60f;
    private static final float OK_BUTTON_X = DIALOG_X + DIALOG_WIDTH / 2f - BUTTON_WIDTH - BUTTON_SPACING / 2f;
    private static final float CANCEL_BUTTON_X = DIALOG_X + DIALOG_WIDTH / 2f + BUTTON_SPACING / 2f;

    // 입력 필드
    private String playerName = "";
    private boolean isVisible = false;

    // 폰트
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyphLayout;

    // 콜백
    private NameInputCallback callback;

    /**
     * 이름 입력 완료 콜백 인터페이스
     */
    public interface NameInputCallback {
        void onNameSet(String name);
    }

    /**
     * NameInputDialog 생성자
     *
     * @param font 일반 폰트
     * @param titleFont 타이틀 폰트
     */
    public NameInputDialog(BitmapFont font, BitmapFont titleFont) {
        this.font = font;
        this.titleFont = titleFont;
        this.glyphLayout = new GlyphLayout();
    }

    /**
     * 다이얼로그를 표시합니다.
     *
     * @param currentName 현재 이름 (비어있을 수 있음)
     * @param callback 이름 설정 완료 시 호출될 콜백
     */
    public void show(String currentName, NameInputCallback callback) {
        this.isVisible = true;
        this.playerName = currentName != null ? currentName : "";
        this.callback = callback;
        System.out.println("[NameInputDialog] 다이얼로그 표시");
    }

    /**
     * 다이얼로그를 숨깁니다.
     */
    public void hide() {
        isVisible = false;
        System.out.println("[NameInputDialog] 다이얼로그 숨김");
    }

    /**
     * 다이얼로그가 표시 중인지 반환합니다.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * 다이얼로그를 렌더링합니다.
     *
     * @param batch SpriteBatch
     * @param shapeRenderer ShapeRenderer
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!isVisible) return;

        // 배경 어둡게 (반투명 검정)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        // 다이얼로그 배경 (밝은 회색)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.9f, 0.9f, 0.95f, 1.0f);
        shapeRenderer.rect(DIALOG_X, DIALOG_Y, DIALOG_WIDTH, DIALOG_HEIGHT);
        shapeRenderer.end();

        // 다이얼로그 테두리
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.rect(DIALOG_X, DIALOG_Y, DIALOG_WIDTH, DIALOG_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f);

        // 텍스트 렌더링
        batch.begin();

        // 타이틀
        titleFont.setColor(Color.BLACK);
        glyphLayout.setText(titleFont, "이름 설정");
        titleFont.draw(batch, "이름 설정",
            DIALOG_X + (DIALOG_WIDTH - glyphLayout.width) / 2f,
            DIALOG_Y + DIALOG_HEIGHT - 50f);

        batch.end();

        // 입력 필드 렌더링
        renderInputField(batch, shapeRenderer);

        // 버튼 렌더링
        renderButton(shapeRenderer, batch, OK_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "확인", Color.GREEN);
        renderButton(shapeRenderer, batch, CANCEL_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "취소", Color.ORANGE);
    }

    /**
     * 입력 필드를 렌더링합니다.
     */
    private void renderInputField(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // 입력 필드 배경
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.8f, 0.9f, 1.0f, 1.0f));
        shapeRenderer.rect(NAME_INPUT_X, NAME_INPUT_Y, NAME_INPUT_WIDTH, INPUT_FIELD_HEIGHT);
        shapeRenderer.end();

        // 입력 필드 테두리
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);
        Gdx.gl.glLineWidth(2f);
        shapeRenderer.rect(NAME_INPUT_X, NAME_INPUT_Y, NAME_INPUT_WIDTH, INPUT_FIELD_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f);

        // 텍스트 렌더링
        batch.begin();

        // 라벨
        font.setColor(Color.BLACK);
        font.draw(batch, "플레이어 이름:", NAME_INPUT_X + 20f, NAME_INPUT_Y + INPUT_FIELD_HEIGHT + 30f);

        // 입력된 이름 또는 안내 문구
        String displayName = playerName.isEmpty() ? "[클릭하여 입력 (PC: 직접 타이핑)]" : playerName;
        Color nameColor = playerName.isEmpty() ? Color.GRAY : Color.BLACK;
        font.setColor(nameColor);
        font.draw(batch, displayName, NAME_INPUT_X + 30f, NAME_INPUT_Y + INPUT_FIELD_HEIGHT / 2f + 10f);

        batch.end();
    }

    /**
     * 버튼을 렌더링합니다.
     */
    private void renderButton(ShapeRenderer shapeRenderer, SpriteBatch batch, float x, float y, float width, float height, String text, Color color) {
        // 버튼 배경
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // 버튼 테두리
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        Gdx.gl.glLineWidth(2f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f);

        // 버튼 텍스트 (중앙 정렬)
        batch.begin();
        font.setColor(Color.WHITE);
        glyphLayout.setText(font, text);
        font.draw(batch, text,
            x + (width - glyphLayout.width) / 2f,
            y + (height + glyphLayout.height) / 2f);
        batch.end();
    }

    /**
     * 입력 처리
     *
     * @param touchX 변환된 월드 X 좌표
     * @param touchY 변환된 월드 Y 좌표
     * @return true면 다이얼로그가 입력을 소비함
     */
    public boolean handleInput(float touchX, float touchY) {
        if (!isVisible) return false;

        // 키보드 입력 처리 (PC용)
        handleKeyboardInput();

        // 터치/마우스 클릭 처리
        if (Gdx.input.justTouched()) {
            System.out.println("[NameInputDialog] 클릭 좌표: (" + touchX + ", " + touchY + ")");

            // 입력 필드 클릭
            if (isPointInRect(touchX, touchY, NAME_INPUT_X, NAME_INPUT_Y, NAME_INPUT_WIDTH, INPUT_FIELD_HEIGHT)) {
                System.out.println("[NameInputDialog] 이름 입력 필드 클릭");
                // 모바일 키보드 띄우기
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        if (text != null && !text.isEmpty()) {
                            playerName = text;
                        }
                    }

                    @Override
                    public void canceled() {}
                }, "플레이어 이름 입력", playerName, "이름을 입력하세요");
                return true;
            }

            // [확인] 버튼
            if (isButtonClicked(touchX, touchY, OK_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                System.out.println("[NameInputDialog] 확인 버튼 클릭");
                if (!playerName.isEmpty()) {
                    if (callback != null) {
                        callback.onNameSet(playerName);
                    }
                    hide();
                } else {
                    System.out.println("[NameInputDialog] 이름을 입력해주세요!");
                }
                return true;
            }

            // [취소] 버튼
            if (isButtonClicked(touchX, touchY, CANCEL_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                System.out.println("[NameInputDialog] 취소 버튼 클릭");
                hide();
                return true;
            }
        }

        return true;  // 다이얼로그가 표시 중이면 항상 입력 소비
    }

    /**
     * 키보드 입력 처리 (PC용)
     */
    private void handleKeyboardInput() {
        // 문자 입력
        for (int i = Input.Keys.A; i <= Input.Keys.Z; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                if (playerName.length() < 12) {
                    char c = (char) ('A' + (i - Input.Keys.A));
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                        Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        playerName += c;
                    } else {
                        playerName += Character.toLowerCase(c);
                    }
                }
            }
        }

        // 숫자 입력
        for (int i = Input.Keys.NUM_0; i <= Input.Keys.NUM_9; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                if (playerName.length() < 12) {
                    playerName += (char) ('0' + (i - Input.Keys.NUM_0));
                }
            }
        }

        // 공백
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (playerName.length() < 12) {
                playerName += " ";
            }
        }

        // 백스페이스
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (playerName.length() > 0) {
                playerName = playerName.substring(0, playerName.length() - 1);
            }
        }

        // Enter로 확인
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (!playerName.isEmpty()) {
                if (callback != null) {
                    callback.onNameSet(playerName);
                }
                hide();
            }
        }

        // ESC 키로 취소
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            hide();
        }
    }

    /**
     * 버튼 클릭 여부 확인
     */
    private boolean isButtonClicked(float touchX, float touchY, float buttonX, float buttonY, float buttonWidth, float buttonHeight) {
        return touchX >= buttonX && touchX <= buttonX + buttonWidth &&
            touchY >= buttonY && touchY <= buttonY + buttonHeight;
    }

    /**
     * 점이 사각형 안에 있는지 확인
     */
    private boolean isPointInRect(float x, float y, float rectX, float rectY, float rectWidth, float rectHeight) {
        return x >= rectX && x <= rectX + rectWidth &&
            y >= rectY && y <= rectY + rectHeight;
    }
}
