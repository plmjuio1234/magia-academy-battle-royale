package com.example.yugeup.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * UI 위치 디버깅 도구
 *
 * 실시간으로 UI 요소의 위치를 조정하고 최종 좌표를 출력합니다.
 * 방향키로 위치 조정, Enter로 최종 좌표 출력
 *
 * 사용법:
 * 1. F1 키로 디버그 모드 ON/OFF
 * 2. Tab 키로 조정할 요소 선택
 * 3. 방향키로 위치 조정 (Shift 누르면 10px씩)
 * 4. +/- 키로 크기 조정
 * 5. Enter 키로 콘솔에 최종 좌표 출력
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class UIDebugger {

    /** 디버그 모드 활성화 여부 */
    private static boolean isEnabled = false;

    /** 현재 선택된 UI 요소 인덱스 */
    private static int selectedIndex = 0;

    /** 이동 속도 (픽셀) */
    private static final float MOVE_SPEED = 1f;
    private static final float MOVE_SPEED_FAST = 10f;

    /** 크기 조정 속도 */
    private static final float SIZE_SPEED = 1f;

    // 대기실 UI 요소들 (배경 이미지 기준 픽셀)
    private static float[] lobbyRoomListX = {80f};
    private static float[] lobbyRoomListY = {150f};
    private static float[] lobbyRoomListWidth = {1630f};
    private static float[] lobbyRoomListHeight = {990f};

    private static float[] lobbyCharPreviewX = {1796f};
    private static float[] lobbyCharPreviewY = {150f};
    private static float[] lobbyCharPreviewWidth = {984f};
    private static float[] lobbyCharPreviewHeight = {990f};

    private static float[] waitroomSlotOffsetX = {14f};
    private static float[] waitroomSlotOffsetY = {7f};
    private static float[] waitroomSlotWidth = {107f};
    private static float[] waitroomSlotHeight = {98f};
    private static float[] waitroomSlotSpacing = {18f};

    private static float[] waitroomChatOffsetX = {80f};
    private static float[] waitroomChatOffsetY = {8f};
    private static float[] waitroomChatWidth = {285f};
    private static float[] waitroomChatHeight = {119f};

    private static float[] waitroomButtonOffsetX = {380f};
    private static float[] waitroomButtonOffsetY = {8f};

    /** UI 요소 이름 */
    private static final String[] ELEMENT_NAMES = {
        "로비 방목록 X",
        "로비 방목록 Y",
        "로비 방목록 너비",
        "로비 방목록 높이",
        "로비 캐릭터 X",
        "로비 캐릭터 Y",
        "로비 캐릭터 너비",
        "로비 캐릭터 높이",
        "대기실 슬롯 X 오프셋",
        "대기실 슬롯 Y 오프셋",
        "대기실 슬롯 너비",
        "대기실 슬롯 높이",
        "대기실 슬롯 간격",
        "대기실 채팅 X 오프셋",
        "대기실 채팅 Y 오프셋",
        "대기실 채팅 너비",
        "대기실 채팅 높이",
        "대기실 버튼 X 오프셋",
        "대기실 버튼 Y 오프셋"
    };

    /**
     * 디버그 모드 토글
     */
    public static void toggle() {
        isEnabled = !isEnabled;
        System.out.println("[UIDebugger] 디버그 모드: " + (isEnabled ? "ON" : "OFF"));
        if (isEnabled) {
            System.out.println("[UIDebugger] F1: 토글 | Tab: 요소 선택 | 방향키: 이동 (+Shift: 10px)");
            System.out.println("[UIDebugger] [ ] 키: 너비 조절 | PageUp/Down: 높이 조절 | Enter: 출력");
        }
    }

    /**
     * 디버그 모드 활성화 여부 반환
     */
    public static boolean isEnabled() {
        return isEnabled;
    }

    /**
     * 입력 처리
     */
    public static void handleInput() {
        // F1: 디버그 모드 토글 (활성화 여부와 관계없이 항상 체크)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            toggle();
            return;
        }

        if (!isEnabled) return;

        // Tab: 다음 요소 선택
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            selectedIndex = (selectedIndex + 1) % ELEMENT_NAMES.length;
            System.out.println("[UIDebugger] 선택: " + ELEMENT_NAMES[selectedIndex]);
        }

        // 이동 속도 결정 (Shift 누르면 빠르게)
        float speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                     Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ?
                     MOVE_SPEED_FAST : MOVE_SPEED;

        // 방향키: 위치 조정
        float[] currentValue = getCurrentValue();
        if (currentValue == null) return;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            currentValue[0] -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            currentValue[0] += speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            currentValue[0] += speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            currentValue[0] -= speed;
        }

        // [ ] 키: 너비 조절 (너비 요소일 때)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT_BRACKET)) {
            currentValue[0] -= SIZE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT_BRACKET)) {
            currentValue[0] += SIZE_SPEED;
        }

        // Page Up/Down: 높이 조절 (높이 요소일 때)
        if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)) {
            currentValue[0] += SIZE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)) {
            currentValue[0] -= SIZE_SPEED;
        }

        // Enter: 최종 좌표 출력
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            printAllValues();
        }
    }

    /**
     * 현재 선택된 요소의 값 배열 반환
     */
    private static float[] getCurrentValue() {
        switch (selectedIndex) {
            case 0: return lobbyRoomListX;
            case 1: return lobbyRoomListY;
            case 2: return lobbyRoomListWidth;
            case 3: return lobbyRoomListHeight;
            case 4: return lobbyCharPreviewX;
            case 5: return lobbyCharPreviewY;
            case 6: return lobbyCharPreviewWidth;
            case 7: return lobbyCharPreviewHeight;
            case 8: return waitroomSlotOffsetX;
            case 9: return waitroomSlotOffsetY;
            case 10: return waitroomSlotWidth;
            case 11: return waitroomSlotHeight;
            case 12: return waitroomSlotSpacing;
            case 13: return waitroomChatOffsetX;
            case 14: return waitroomChatOffsetY;
            case 15: return waitroomChatWidth;
            case 16: return waitroomChatHeight;
            case 17: return waitroomButtonOffsetX;
            case 18: return waitroomButtonOffsetY;
            default: return null;
        }
    }

    /**
     * 화면에 디버그 정보 렌더링
     */
    public static void render(SpriteBatch batch, BitmapFont font) {
        if (!isEnabled) return;

        batch.begin();
        font.setColor(Color.YELLOW);

        // 현재 선택된 요소 표시
        float[] currentValue = getCurrentValue();
        if (currentValue != null) {
            String info = String.format("[F1:OFF Tab:선택] %s = %.1f",
                ELEMENT_NAMES[selectedIndex], currentValue[0]);
            font.draw(batch, info, 20, Constants.SCREEN_HEIGHT - 20);

            // 조작 가이드
            font.setColor(Color.WHITE);
            font.draw(batch, "방향키: 이동 (+Shift: 10px)  [ ]: 너비  PgUp/Dn: 높이  Enter: 출력",
                20, Constants.SCREEN_HEIGHT - 50);
        }

        batch.end();
    }

    /**
     * 모든 값을 콘솔에 출력 (Constants.java에 복사 가능한 형식)
     */
    private static void printAllValues() {
        System.out.println("\n========== UI 디버그 최종 좌표 ==========");
        System.out.println("// 로비 화면");
        System.out.println("LOBBY_ROOM_LIST_X = " + lobbyRoomListX[0] + "f;");
        System.out.println("LOBBY_ROOM_LIST_Y = " + lobbyRoomListY[0] + "f;");
        System.out.println("LOBBY_ROOM_LIST_WIDTH = " + lobbyRoomListWidth[0] + "f;");
        System.out.println("LOBBY_ROOM_LIST_HEIGHT = " + lobbyRoomListHeight[0] + "f;");
        System.out.println("LOBBY_CHARACTER_PREVIEW_X = " + lobbyCharPreviewX[0] + "f;");
        System.out.println("LOBBY_CHARACTER_PREVIEW_Y = " + lobbyCharPreviewY[0] + "f;");
        System.out.println("LOBBY_CHARACTER_PREVIEW_WIDTH = " + lobbyCharPreviewWidth[0] + "f;");
        System.out.println("LOBBY_CHARACTER_PREVIEW_HEIGHT = " + lobbyCharPreviewHeight[0] + "f;");
        System.out.println("\n// 대기실 화면");
        System.out.println("WAITROOM_SLOT_OFFSET_X = " + waitroomSlotOffsetX[0] + "f;");
        System.out.println("WAITROOM_SLOT_OFFSET_Y = " + waitroomSlotOffsetY[0] + "f;");
        System.out.println("WAITROOM_SLOT_WIDTH_PX = " + waitroomSlotWidth[0] + "f;");
        System.out.println("WAITROOM_SLOT_HEIGHT_PX = " + waitroomSlotHeight[0] + "f;");
        System.out.println("WAITROOM_SLOT_SPACING_PX = " + waitroomSlotSpacing[0] + "f;");
        System.out.println("WAITROOM_CHAT_OFFSET_X = " + waitroomChatOffsetX[0] + "f;");
        System.out.println("WAITROOM_CHAT_OFFSET_Y = " + waitroomChatOffsetY[0] + "f;");
        System.out.println("WAITROOM_CHAT_WIDTH_PX = " + waitroomChatWidth[0] + "f;");
        System.out.println("WAITROOM_CHAT_HEIGHT_PX = " + waitroomChatHeight[0] + "f;");
        System.out.println("WAITROOM_BUTTON_OFFSET_X = " + waitroomButtonOffsetX[0] + "f;");
        System.out.println("WAITROOM_BUTTON_OFFSET_Y = " + waitroomButtonOffsetY[0] + "f;");
        System.out.println("=======================================\n");
    }

    // Getter 메서드들 (각 화면에서 사용)

    public static float getLobbyRoomListX() {
        return isEnabled ? lobbyRoomListX[0] : Constants.LOBBY_ROOM_LIST_X;
    }

    public static float getLobbyRoomListY() {
        return isEnabled ? lobbyRoomListY[0] : Constants.LOBBY_ROOM_LIST_Y;
    }

    public static float getLobbyRoomListWidth() {
        return isEnabled ? lobbyRoomListWidth[0] : Constants.LOBBY_ROOM_LIST_WIDTH;
    }

    public static float getLobbyRoomListHeight() {
        return isEnabled ? lobbyRoomListHeight[0] : Constants.LOBBY_ROOM_LIST_HEIGHT;
    }

    public static float getLobbyCharPreviewX() {
        return isEnabled ? lobbyCharPreviewX[0] : Constants.LOBBY_CHARACTER_PREVIEW_X;
    }

    public static float getLobbyCharPreviewY() {
        return isEnabled ? lobbyCharPreviewY[0] : Constants.LOBBY_CHARACTER_PREVIEW_Y;
    }

    public static float getLobbyCharPreviewWidth() {
        return isEnabled ? lobbyCharPreviewWidth[0] : Constants.LOBBY_CHARACTER_PREVIEW_WIDTH;
    }

    public static float getLobbyCharPreviewHeight() {
        return isEnabled ? lobbyCharPreviewHeight[0] : Constants.LOBBY_CHARACTER_PREVIEW_HEIGHT;
    }

    public static float getWaitroomSlotOffsetX() {
        return isEnabled ? waitroomSlotOffsetX[0] : Constants.WAITROOM_SLOT_OFFSET_X;
    }

    public static float getWaitroomSlotOffsetY() {
        return isEnabled ? waitroomSlotOffsetY[0] : Constants.WAITROOM_SLOT_OFFSET_Y;
    }

    public static float getWaitroomSlotWidth() {
        return isEnabled ? waitroomSlotWidth[0] : Constants.WAITROOM_SLOT_WIDTH_PX;
    }

    public static float getWaitroomSlotHeight() {
        return isEnabled ? waitroomSlotHeight[0] : Constants.WAITROOM_SLOT_HEIGHT_PX;
    }

    public static float getWaitroomSlotSpacing() {
        return isEnabled ? waitroomSlotSpacing[0] : Constants.WAITROOM_SLOT_SPACING_PX;
    }

    public static float getWaitroomChatOffsetX() {
        return isEnabled ? waitroomChatOffsetX[0] : Constants.WAITROOM_CHAT_OFFSET_X;
    }

    public static float getWaitroomChatOffsetY() {
        return isEnabled ? waitroomChatOffsetY[0] : Constants.WAITROOM_CHAT_OFFSET_Y;
    }

    public static float getWaitroomChatWidth() {
        return isEnabled ? waitroomChatWidth[0] : Constants.WAITROOM_CHAT_WIDTH_PX;
    }

    public static float getWaitroomChatHeight() {
        return isEnabled ? waitroomChatHeight[0] : Constants.WAITROOM_CHAT_HEIGHT_PX;
    }

    public static float getWaitroomButtonOffsetX() {
        return isEnabled ? waitroomButtonOffsetX[0] : Constants.WAITROOM_BUTTON_OFFSET_X;
    }

    public static float getWaitroomButtonOffsetY() {
        return isEnabled ? waitroomButtonOffsetY[0] : Constants.WAITROOM_BUTTON_OFFSET_Y;
    }
}
