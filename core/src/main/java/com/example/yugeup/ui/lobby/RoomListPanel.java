package com.example.yugeup.ui.lobby;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.example.Main.RoomInfo;
import com.example.yugeup.utils.Constants;

/**
 * 방 목록 패널 UI
 *
 * 로비에서 방 목록을 표시합니다.
 * 방 생성, 참가 버튼을 제공합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RoomListPanel {

    // 패널 위치 및 크기
    private float x;
    private float y;
    private float width;
    private float height;

    // 방 목록 데이터
    private RoomInfo[] rooms;

    // 스크롤 관련
    private int scrollOffset;  // 스크롤 오프셋 (행 단위)
    private int maxVisibleRooms;

    // 버튼 상태
    private Rectangle refreshButtonBounds;
    private Rectangle createButtonBounds;
    private Rectangle backButtonBounds;
    private Rectangle[] joinButtonBounds;

    // 선택된 방
    private int selectedRoomIndex;

    // 렌더링 도구
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // 아틀라스 저장
    private TextureAtlas atlas;

    /**
     * RoomListPanel 생성자
     *
     * @param x 패널 X 위치
     * @param y 패널 Y 위치
     * @param width 패널 너비
     * @param height 패널 높이
     * @param font 사용할 폰트
     */
    public RoomListPanel(float x, float y, float width, float height, BitmapFont font, TextureAtlas atlas) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
        this.atlas = atlas;

        this.rooms = new RoomInfo[0];
        this.scrollOffset = 0;
        this.maxVisibleRooms = Constants.LOBBY_MAX_VISIBLE_ROOMS;
        this.selectedRoomIndex = -1;

        this.shapeRenderer = new ShapeRenderer();

        // 버튼 영역 초기화
        this.refreshButtonBounds = new Rectangle(
            x + 100,
            y + height - 70,
            200,
            50
        );

        this.createButtonBounds = new Rectangle(
            x + 350,
            y + height - 70,
            200,
            50
        );

        this.backButtonBounds = new Rectangle(
            x + 600,
            y + height - 70,
            200,
            50
        );

        // 각 방 행의 [참가] 버튼 영역
        this.joinButtonBounds = new Rectangle[maxVisibleRooms];
        float tableStartY = y + height - 160;
        for (int i = 0; i < maxVisibleRooms; i++) {
            float rowY = tableStartY - ((i + 1) * Constants.LOBBY_TABLE_ROW_HEIGHT);
            joinButtonBounds[i] = new Rectangle(
                x + width - 200,
                rowY - 20,
                120,
                40
            );
        }
    }

    /**
     * 버튼 영역을 현재 x, y, width, height 기준으로 재계산합니다.
     */
    private void updateButtonBounds() {
        // 상단 버튼들
        // 패널 테두리와의 여백, 버튼 크기 등 정의
        float margin = 40f;
        float verticalMargin = 160f; // 상단 버튼을 위한 세로 여백
        float buttonSize = 64f * 3;

        // 1. 뒤로가기 버튼: 패널의 '좌측 상단'에 배치
        this.backButtonBounds.set(
            x + margin - 300f,
            y + height - verticalMargin - buttonSize, // 패널 상단에서 세로 여백만큼 아래로
            buttonSize,
            buttonSize
        );

        // 2. 새로고침 버튼: '뒤로가기 버튼 바로 아래'에 배치
        this.refreshButtonBounds.set(
            backButtonBounds.x,
            backButtonBounds.y - buttonSize - 450f, // 뒤로가기 버튼 위치에서 아래로
            buttonSize,
            buttonSize
        );

        // 3. 방 만들기 버튼: 패널의 '우측 하단'에 배치
        float createBtnWidth = 400f;
        float createBtnHeight = createBtnWidth * (32f/92f);
        this.createButtonBounds.set(
            x + width - createBtnWidth + margin, // 패널 오른쪽 끝에서 안으로
            y + margin - 220f,
            createBtnWidth,
            createBtnHeight
        );

        // 각 방 행의 [참가] 버튼 (컬럼 위치와 동일하게)
        float col4_joinButton = x + width - 100;  // 텍스트 위치와 동일
        float tableStartY = y + height - 160;
        for (int i = 0; i < maxVisibleRooms; i++) {
            float rowY = tableStartY - ((i + 1) * Constants.LOBBY_TABLE_ROW_HEIGHT);
            joinButtonBounds[i].set(col4_joinButton - 10, rowY - 20, 120, 40);
        }
    }

    /**
     * 방 목록을 렌더링합니다.
     *
     * @param batch SpriteBatch
     */
    public void render(SpriteBatch batch) {
        // UI 디버거 활성화 시 동적으로 위치/크기 업데이트
        if (com.example.yugeup.utils.UIDebugger.isEnabled()) {
            this.x = com.example.yugeup.utils.UIDebugger.getLobbyRoomListX();
            this.y = com.example.yugeup.utils.UIDebugger.getLobbyRoomListY();
            this.width = com.example.yugeup.utils.UIDebugger.getLobbyRoomListWidth();
            this.height = com.example.yugeup.utils.UIDebugger.getLobbyRoomListHeight();
        }

        // 버튼 영역 항상 재계산 (UI 디버거 활성화 여부와 관계없이)
        updateButtonBounds();

        // 배경 (UI 디버거 활성화 시 와이어프레임 표시)
        if (com.example.yugeup.utils.UIDebugger.isEnabled()) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
            batch.begin();
        }

        // 상단 버튼들 (상단)
        if (atlas != null) {

            // 새로고침 버튼 그리기
            batch.draw(
                atlas.findRegion("refresh-button-defualt"), // 우선 기본 이미지만 표시
                refreshButtonBounds.x, refreshButtonBounds.y, refreshButtonBounds.width, refreshButtonBounds.height
            );

            // 방 만들기 버튼 그리기
            batch.draw(
                atlas.findRegion("create-room-button-defualt"), // 우선 기본 이미지만 표시
                createButtonBounds.x, createButtonBounds.y, createButtonBounds.width, createButtonBounds.height
            );

            // 타이틀로(뒤로가기) 버튼 그리기
            batch.draw(
                atlas.findRegion("backspace-button-defualt"), // 우선 기본 이미지만 표시
                backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height
            );
        } else {
            // 아틀라스 로드 실패 시, 예전처럼 글자로 버튼을 표시합니다.
            font.setColor(Color.BLACK);
            font.draw(batch, "[새로고침]", x + 100, y + height - 40);
            font.draw(batch, "[방 만들기]", x + 350, y + height - 40);
            font.draw(batch, "[타이틀로]", x + 600, y + height - 40);
        }

        // 방 목록 제목
        font.setColor(Color.DARK_GRAY);
        font.getData().setScale(1.3f);
        font.draw(batch, "방 목록", x + 20, y + height - 170);

        // 동적 컬럼 위치 계산 (패널 너비에 비례)
        float col1_roomName = x + 50;                     // 방 제목 (왼쪽)
        float col2_players = x + width * 0.5f;            // 인원 (중간)
        float col3_status = x + width * 0.65f;            // 상태 (50% 지점)
        float col4_joinButton = x + width - 100;          // 참가 버튼 (오른쪽, 여유 확보)

        // 테이블 헤더 (방 목록 제목 아래 충분한 간격)
        float tableStartY = y + height - 240;
        font.setColor(Color.BLACK);
        font.getData().setScale(1.0f);
        font.draw(batch, "방 제목", col1_roomName, tableStartY);
        font.draw(batch, "인원", col2_players, tableStartY);
        font.draw(batch, "상태", col3_status, tableStartY);

        // 방 목록 표시
        if (rooms == null || rooms.length == 0) {
            font.setColor(Color.GRAY);
            font.draw(batch, "방이 없습니다. 새로고침을 눌러주세요.", col1_roomName, tableStartY - 100);
        } else {
            int endIndex = Math.min(scrollOffset + maxVisibleRooms, rooms.length);
            for (int i = scrollOffset; i < endIndex; i++) {
                RoomInfo room = rooms[i];
                int displayIndex = i - scrollOffset;
                float rowY = tableStartY - ((displayIndex + 1) * Constants.LOBBY_TABLE_ROW_HEIGHT);

                // 방 제목
                font.setColor(Color.BLACK);
                font.draw(batch, room.roomName, col1_roomName, rowY);

                // 인원
                String playerCount = room.currentPlayers + "/" + room.maxPlayers;
                font.draw(batch, playerCount, col2_players, rowY);

                // 상태
                String status = room.isPlaying ? "게임중" : "대기";
                font.setColor(room.isPlaying ? Color.RED : Color.GREEN);
                font.draw(batch, status, col3_status, rowY);

                // [참가] 버튼 (각 행 오른쪽 끝)
                if (!room.isPlaying && room.currentPlayers < room.maxPlayers) {
                    font.setColor(Color.BLUE);
                    font.draw(batch, "[참가]", col4_joinButton, rowY);
                } else {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "[불가]", col4_joinButton, rowY);
                }
            }
        }
    }

    /**
     * 방 목록을 업데이트합니다.
     *
     * @param newRooms 새로운 방 목록
     */
    public void setRooms(RoomInfo[] newRooms) {
        this.rooms = newRooms != null ? newRooms : new RoomInfo[0];
        this.scrollOffset = 0;
        this.selectedRoomIndex = -1;
    }

    /**
     * 터치 입력을 처리합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 처리 여부
     */
    public boolean handleTouch(float touchX, float touchY) {
        // [새로고침] 버튼 클릭
        if (refreshButtonBounds.contains(touchX, touchY)) {
            return true;  // LobbyScreen에서 새로고침 처리
        }

        // [타이틀로] 버튼 클릭
        if (backButtonBounds.contains(touchX, touchY)) {
            return true;  // LobbyScreen에서 화면 전환 처리
        }

        // [참가] 버튼 클릭
        for (int i = 0; i < maxVisibleRooms && i < rooms.length; i++) {
            if (joinButtonBounds[i].contains(touchX, touchY)) {
                int roomIndex = scrollOffset + i;
                if (roomIndex < rooms.length) {
                    RoomInfo room = rooms[roomIndex];
                    if (!room.isPlaying && room.currentPlayers < room.maxPlayers) {
                        selectedRoomIndex = roomIndex;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * [새로고침] 버튼이 클릭되었는지 확인합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 클릭 여부
     */
    public boolean isRefreshButtonClicked(float touchX, float touchY) {
        return refreshButtonBounds.contains(touchX, touchY);
    }

    /**
     * [방 만들기] 버튼이 클릭되었는지 확인합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 클릭 여부
     */
    public boolean isCreateButtonClicked(float touchX, float touchY) {
        return createButtonBounds.contains(touchX, touchY);
    }

    /**
     * [타이틀로] 버튼이 클릭되었는지 확인합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 클릭 여부
     */
    public boolean isBackButtonClicked(float touchX, float touchY) {
        return backButtonBounds.contains(touchX, touchY);
    }

    /**
     * [참가] 버튼이 클릭되었는지 확인하고, 선택된 방 ID를 반환합니다.
     *
     * @param touchX 터치 X 좌표
     * @param touchY 터치 Y 좌표
     * @return 선택된 방 ID (없으면 -1)
     */
    public int getJoinedRoomId(float touchX, float touchY) {
        for (int i = 0; i < maxVisibleRooms; i++) {
            if (joinButtonBounds[i].contains(touchX, touchY)) {
                int roomIndex = scrollOffset + i;
                if (roomIndex < rooms.length) {
                    RoomInfo room = rooms[roomIndex];
                    if (!room.isPlaying && room.currentPlayers < room.maxPlayers) {
                        return room.roomId;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 선택된 방 ID를 반환합니다.
     *
     * @return 선택된 방 ID (없으면 -1)
     */
    public int getSelectedRoomId() {
        if (selectedRoomIndex >= 0 && selectedRoomIndex < rooms.length) {
            return rooms[selectedRoomIndex].roomId;
        }
        return -1;
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
