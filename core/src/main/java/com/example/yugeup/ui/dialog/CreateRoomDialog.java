package com.example.yugeup.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.example.yugeup.network.NetworkManager;
import com.example.yugeup.network.RoomManager;
import com.example.yugeup.utils.Constants;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.example.yugeup.utils.AssetManager;

/**
 * 방 생성 다이얼로그
 *
 * 방 제목과 최대 인원을 입력받아 방을 생성합니다.
 * 모달 형태로 화면 중앙에 표시됩니다.
 * PC와 모바일 모두 지원합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class CreateRoomDialog {

    // 다이얼로그 크기 (화면 비율에 맞게 조정)
    private static final float DIALOG_WIDTH = Constants.SCREEN_WIDTH * 0.4f;
    private static final float DIALOG_HEIGHT = DIALOG_WIDTH * (83f / 92f);
    private static final float DIALOG_X = (Constants.SCREEN_WIDTH - DIALOG_WIDTH) / 2f;
    private static final float DIALOG_Y = (Constants.SCREEN_HEIGHT - DIALOG_HEIGHT) / 2f;

    // 입력 필드 영역
    private static final float INPUT_FIELD_HEIGHT = 100f;
//    private static final float INPUT_FIELD_MARGIN = 200f;

    // 방 제목 입력 필드
    private static final float NAME_INPUT_WIDTH = DIALOG_WIDTH * 0.58f;
    private static final float NAME_INPUT_X = DIALOG_X + (DIALOG_WIDTH - NAME_INPUT_WIDTH) / 2f;
    private static final float NAME_INPUT_Y = DIALOG_Y + DIALOG_HEIGHT / 2f;

    // 버튼 크기
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = BUTTON_WIDTH * (32f / 92f);
    private static final float BUTTON_SPACING = 50f;
    private static final float BUTTON_Y = DIALOG_Y + 180f;
    private static final float OK_BUTTON_X = DIALOG_X + DIALOG_WIDTH / 2f - BUTTON_WIDTH - BUTTON_SPACING / 2f;
    private static final float CANCEL_BUTTON_X = DIALOG_X + DIALOG_WIDTH / 2f + BUTTON_SPACING / 2f;

    // 입력 필드
    private String roomName = "";
    private int maxPlayers = 4;  // 기본값: 4명
    private boolean isVisible = false;

    // 폰트
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyphLayout;

    // 현재 입력 모드 (0: 플레이어 이름, 1: 방 제목, 2: 최대 인원)
    private int inputMode = 0;

    // 모바일 키보드 입력 리스너
    private Input.TextInputListener textInputListener;

    // 배경 텍스처
    private Texture backgroundTexture;

    // 확인, 취소 버튼
    private final TextureAtlas.AtlasRegion okButtonTexture;
    private final TextureAtlas.AtlasRegion cancelButtonTexture;
    private final TextureAtlas.AtlasRegion okButtonHoverTexture;
    private final TextureAtlas.AtlasRegion cancelButtonHoverTexture;
    private boolean okButtonHovered;
    private boolean cancelButtonHovered;

    /**
     * CreateRoomDialog 생성자
     *
     * @param font 일반 폰트
     * @param titleFont 타이틀 폰트
     */
    public CreateRoomDialog(BitmapFont font, BitmapFont titleFont) {
        this.font = font;
        this.titleFont = titleFont;
        this.glyphLayout = new GlyphLayout();

        // 배경 이미지 로드 (assets 폴더 기준 경로)
        this.backgroundTexture = new Texture("images/backgrounds/create-room-frame.png");

        // 확인, 취소 버튼 초기화
        AssetManager assetManager = AssetManager.getInstance();
        TextureAtlas uiAtlas = assetManager.getAtlas("button");
        this.okButtonTexture = uiAtlas.findRegion("ok-button-defualt");
        this.cancelButtonTexture = uiAtlas.findRegion("cancel-button-defualt");
        this.okButtonHoverTexture = uiAtlas.findRegion("ok-button-hover");
        this.cancelButtonHoverTexture = uiAtlas.findRegion("cancel-button-hover");

        // 모바일 키보드 입력 리스너 초기화
        this.textInputListener = new Input.TextInputListener() {
            @Override
            public void input(String text) {
                if (text != null && !text.isEmpty()) {
                    roomName = text;
                    inputMode = 1;  // 인원 설정으로 이동
                }
            }

            @Override
            public void canceled() {
                // 입력 취소 시 아무 동작 안 함
            }
        };
    }

    /**
     * 다이얼로그를 표시합니다.
     */
    public void show() {
        isVisible = true;
        roomName = "";
        maxPlayers = 4;
        inputMode = 0;
        System.out.println("[CreateRoomDialog] 다이얼로그 표시");
    }

    /**
     * 다이얼로그를 숨깁니다.
     */
    public void hide() {
        isVisible = false;
        System.out.println("[CreateRoomDialog] 다이얼로그 숨김");
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

        // 다이얼로그 테두리
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        shapeRenderer.setColor(Color.WHITE);
//        Gdx.gl.glLineWidth(3f);
//        shapeRenderer.rect(DIALOG_X, DIALOG_Y, DIALOG_WIDTH, DIALOG_HEIGHT);
//        shapeRenderer.end();
//        Gdx.gl.glLineWidth(1f);

        // 텍스트 렌더링
        batch.begin();

        // 배경 이미지
        batch.draw(backgroundTexture, DIALOG_X, DIALOG_Y, DIALOG_WIDTH, DIALOG_HEIGHT);

        // 타이틀
        titleFont.setColor(Color.BLACK);
        glyphLayout.setText(titleFont, "방 만들기");
        titleFont.draw(batch, "방 만들기",
            DIALOG_X + (DIALOG_WIDTH - glyphLayout.width) / 2f,
            DIALOG_Y + DIALOG_HEIGHT - 140f);

        batch.end();

        // 입력 필드 렌더링
        renderInputFields(batch, shapeRenderer);

        // 버튼 렌더링
//        renderButton(shapeRenderer, batch, OK_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "확인", Color.GREEN);
//        renderButton(shapeRenderer, batch, CANCEL_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "취소", Color.ORANGE);
        batch.begin();

        // [확인] 버튼 이미지 그리기
        batch.draw(
            okButtonHovered ? okButtonHoverTexture : okButtonTexture,
            OK_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT
        );
        // [취소] 버튼 이미지 그리기
        batch.draw(
            cancelButtonHovered ? cancelButtonHoverTexture : cancelButtonTexture,
            CANCEL_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT
        );

        batch.end();
    }

    /**
     * 입력 필드를 렌더링합니다.
     */
    private void renderInputFields(SpriteBatch batch, ShapeRenderer shapeRenderer) {

        // 선택 시(활성화) 색상: #dcb071
        Color activeColor = Color.valueOf("#dcb071");

        // 선택 전(비활성화) 색상: #ecd5a1
        Color inactiveColor = Color.valueOf("#ecd5a1");

        // 방 제목 입력 필드
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color nameFieldColor = inputMode == 0 ? inactiveColor : activeColor;
        shapeRenderer.setColor(nameFieldColor);
        shapeRenderer.rect(NAME_INPUT_X, NAME_INPUT_Y, NAME_INPUT_WIDTH, INPUT_FIELD_HEIGHT);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(inputMode == 0 ? Color.BLUE : Color.GRAY);
        Gdx.gl.glLineWidth(2f);
        shapeRenderer.rect(NAME_INPUT_X, NAME_INPUT_Y, NAME_INPUT_WIDTH, INPUT_FIELD_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f);

        // 텍스트 렌더링
        batch.begin();

        // 방 제목 라벨 및 값
        font.setColor(Color.BLACK);
        font.draw(batch,  " ", NAME_INPUT_X + 20f, NAME_INPUT_Y + INPUT_FIELD_HEIGHT + 30f);

        String displayRoomName = roomName.isEmpty() ? "방 제목 입력" : roomName;
        Color nameTextColor = roomName.isEmpty() ? Color.GRAY : Color.BLACK;
        font.setColor(nameTextColor);
        font.draw(batch, displayRoomName, NAME_INPUT_X + 30f, NAME_INPUT_Y + INPUT_FIELD_HEIGHT / 2f + 10f);

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
     * @return true면 다이얼로그가 입력을 소비함
     */
    public boolean handleInput(float touchX, float touchY) {
        if (!isVisible) return false;

        // 버튼 hover 상태
        okButtonHovered = isButtonClicked(touchX, touchY, OK_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        cancelButtonHovered = isButtonClicked(touchX, touchY, CANCEL_BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);

        // 키보드 입력 처리 (PC용)
        handleKeyboardInput();

        // 터치/마우스 클릭 처리
        if (Gdx.input.justTouched()) {
            System.out.println("[CreateRoomDialog] 클릭 좌표: (" + touchX + ", " + touchY + ")");
            System.out.println("[CreateRoomDialog] NAME_INPUT 영역: (" + NAME_INPUT_X + ", " + NAME_INPUT_Y + ", " + NAME_INPUT_WIDTH + ", " + INPUT_FIELD_HEIGHT + ")");
            System.out.println("[CreateRoomDialog] OK_BUTTON 영역: (" + OK_BUTTON_X + ", " + BUTTON_Y + ", " + BUTTON_WIDTH + ", " + BUTTON_HEIGHT + ")");

            // 방 제목 입력 필드 클릭
            if (isPointInRect(touchX, touchY, NAME_INPUT_X, NAME_INPUT_Y, NAME_INPUT_WIDTH, INPUT_FIELD_HEIGHT)) {
                inputMode = 0;
                System.out.println("[CreateRoomDialog] 방 제목 입력 필드 클릭");
                Gdx.input.getTextInput(textInputListener, "방 제목 입력", roomName, "방 제목을 입력하세요");
                return true;
            }

            // [확인] 버튼
            if (okButtonHovered) {
                System.out.println("[CreateRoomDialog] 확인 버튼 클릭");
                if (!roomName.isEmpty() && maxPlayers >= 2 && maxPlayers <= 8) {
                    createRoom();
                    return true;
                } else {
                    System.out.println("[CreateRoomDialog] 방 제목을 입력하세요!");
                }
                return true;
            }

            // [취소] 버튼
            if (cancelButtonHovered) {
                System.out.println("[CreateRoomDialog] 취소 버튼 클릭");
                hide();
                return true;
            }
        }

        return true;
    }

    /**
     * 키보드 입력 처리 (PC용)
     */
    private void handleKeyboardInput() {
        // 방 제목 입력 모드 (PC 키보드)
        if (inputMode == 0) {
            // 문자 입력
            for (int i = Input.Keys.A; i <= Input.Keys.Z; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    if (roomName.length() < 20) {
                        char c = (char) ('A' + (i - Input.Keys.A));
                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                            Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                            roomName += c;
                        } else {
                            roomName += Character.toLowerCase(c);
                        }
                    }
                }
            }
            // 숫자 입력
            for (int i = Input.Keys.NUM_0; i <= Input.Keys.NUM_9; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    if (roomName.length() < 20) {
                        roomName += (char) ('0' + (i - Input.Keys.NUM_0));
                    }
                }
            }
            // 공백
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (roomName.length() < 20) {
                    roomName += " ";
                }
            }
            // 백스페이스
            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                if (roomName.length() > 0) {
                    roomName = roomName.substring(0, roomName.length() - 1);
                }
            }
            // Enter로 다음 단계
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (!roomName.isEmpty()) {
                    inputMode = 1;
                }
            }
        }

        // 최대 인원 조절 모드
        if (inputMode == 1) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if (maxPlayers < 8) maxPlayers++;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                if (maxPlayers > 2) maxPlayers--;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                // 방 생성
                if (!roomName.isEmpty() && maxPlayers >= 2 && maxPlayers <= 8) {
                    createRoom();
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                // 방 제목 입력으로 돌아가기
                inputMode = 0;
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

    /**
     * 방 생성 요청
     */
    private void createRoom() {
        System.out.println("[CreateRoomDialog] 방 생성 요청: " + roomName + ", " + maxPlayers + "명");
        RoomManager.getInstance().createRoom(roomName, maxPlayers);
        hide();
    }
}
