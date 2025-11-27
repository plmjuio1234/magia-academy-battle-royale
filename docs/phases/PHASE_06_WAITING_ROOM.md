# PHASE_06_WAITING_ROOM.md - 대기실 UI & 채팅

---

## 🎯 목표
플레이어들이 게임 시작 전 대기하는 대기실 화면 구현
(플레이어 정보 표시 + 실시간 채팅 + 게임시작 버튼)

---

## 📋 구현 범위

### 상단 (1/2): 플레이어 정보
- ✅ 최대 4명의 플레이어 캐릭터 미리보기
- ✅ 각 플레이어 닉네임 표시
- ✅ 호스트 표시 (왕관 아이콘 등)
- ✅ 플레이어 추가/제거 시 UI 업데이트 (RoomUpdateMsg)

### 하단 좌측 (1/4): 채팅 시스템
- ✅ 채팅 메시지 목록 (최근 10개 표시)
- ✅ 플레이어명 + 메시지 표시
- ✅ 입력 필드 + [전송] 버튼
- ✅ ChatMsg 송수신

### 하단 우측 (1/4): 컨트롤 버튼
- ✅ [게임시작] 버튼 (호스트만 활성화)
  - 클릭 → StartGameMsg 전송 → 모두 GameScreen으로
- ✅ [방나가기] 버튼
  - 클릭 → LeaveRoomMsg 전송 → LobbyScreen으로

---

## 📁 필요 파일

### 생성할 파일
```
screens/
  └─ WaitingRoomScreen.java          (새로 생성)

ui/waitingroom/
  ├─ PlayerInfoPanel.java            (새로 생성)
  ├─ ChatPanel.java                  (새로 생성)
  └─ ControlButtonPanel.java         (새로 생성)

network/
  └─ messages/
      ├─ StartGameMsg.java           (새로 생성)
      └─ GameStartNotification.java   (새로 생성)
```

### 기존 파일 수정
```
network/
  └─ RoomManager.java               (수정 - StartGameMsg 처리)

network/messages/
  └─ ChatMsg.java                   (이미 있음)
```

---

## 🔧 구현 가이드

### 1. WaitingRoomScreen 클래스

```java
/**
 * 대기실 화면 클래스
 *
 * 플레이어들이 게임 시작 전 대기하는 화면입니다.
 * 상단에 플레이어 정보, 하단에 채팅과 컨트롤 버튼이 있습니다.
 */
public class WaitingRoomScreen implements IScreen {
    private PlayerInfoPanel playerPanel;
    private ChatPanel chatPanel;
    private ControlButtonPanel controlPanel;

    private RoomManager roomManager;
    private NetworkManager networkManager;
    private int currentPlayerId;
    private boolean isHost;

    public WaitingRoomScreen(int playerId, boolean isHost) {
        this.currentPlayerId = playerId;
        this.isHost = isHost;
        this.roomManager = RoomManager.getInstance();
        this.networkManager = NetworkManager.getInstance();

        // 각 패널 초기화
        this.playerPanel = new PlayerInfoPanel(roomManager.getPlayersInRoom());
        this.chatPanel = new ChatPanel();
        this.controlPanel = new ControlButtonPanel(isHost);
    }

    @Override
    public void show() {
        // 메시지 핸들러에 패널 등록 (채팅 수신 시 업데이트)
        MessageHandler.getInstance().addChatListener(chatPanel);
        MessageHandler.getInstance().addRoomUpdateListener(playerPanel);
    }

    @Override
    public void hide() {
        // 메시지 핸들러에서 패널 제거
        MessageHandler.getInstance().removeChatListener(chatPanel);
        MessageHandler.getInstance().removeRoomUpdateListener(playerPanel);
    }

    @Override
    public void update(float delta) {
        // 각 패널 업데이트
        playerPanel.update(delta);
        chatPanel.update(delta);
        controlPanel.update(delta);

        // [전송] 버튼 클릭 처리
        if (chatPanel.isSendButtonPressed()) {
            String message = chatPanel.getInputText();
            if (!message.isEmpty()) {
                // 서버로 채팅 메시지 전송
                ChatMsg msg = new ChatMsg();
                msg.sender = "플레이어_" + currentPlayerId;  // 임시
                msg.text = message;
                networkManager.sendMessage(msg);

                // 입력 필드 초기화
                chatPanel.clearInput();
            }
        }

        // [게임시작] 버튼 클릭 처리 (호스트만)
        if (isHost && controlPanel.isStartGameButtonPressed()) {
            StartGameMsg msg = new StartGameMsg();
            networkManager.sendMessage(msg);
        }

        // [방나가기] 버튼 클릭 처리
        if (controlPanel.isLeaveButtonPressed()) {
            roomManager.leaveRoom();
            // LobbyScreen으로 이동
            ScreenManager.getInstance().setScreen(new LobbyScreen());
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();

        // 배경 렌더링
        batch.setColor(0.1f, 0.1f, 0.1f, 1f);
        batch.draw(새하얀픽셀, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.setColor(1, 1, 1, 1);

        // 상단 (1/2): 플레이어 정보
        playerPanel.render(batch,
            0, Constants.SCREEN_HEIGHT / 2,
            Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT / 2);

        // 하단 좌측 (1/4): 채팅
        chatPanel.render(batch,
            0, 0,
            Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT / 2);

        // 하단 우측 (1/4): 버튼
        controlPanel.render(batch,
            Constants.SCREEN_WIDTH / 2, 0,
            Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT / 2);

        batch.end();
    }

    @Override
    public void dispose() {
        playerPanel.dispose();
        chatPanel.dispose();
        controlPanel.dispose();
    }
}
```

### 2. PlayerInfoPanel 클래스

```java
/**
 * 플레이어 정보 패널
 *
 * 방 내 최대 4명의 플레이어를 2x2 그리드로 표시합니다.
 */
public class PlayerInfoPanel {
    private List<Integer> playerIds;
    private Map<Integer, Sprite> characterSprites;
    private Map<Integer, String> playerNames;

    public PlayerInfoPanel(List<Integer> players) {
        this.playerIds = players;
        this.characterSprites = new HashMap<>();
        this.playerNames = new HashMap<>();

        // 각 플레이어를 위한 초기화
        loadPlayerData();
    }

    private void loadPlayerData() {
        // 플레이어 정보 로드 (닉네임, 캐릭터 이미지 등)
        for (Integer playerId : playerIds) {
            // 플레이어 정보 조회 및 캐시
            String name = "플레이어_" + playerId;
            playerNames.put(playerId, name);

            // 캐릭터 스프라이트 로드
            Sprite sprite = AssetManager.getInstance()
                .getSprite("characters/default_character");
            characterSprites.put(playerId, sprite);
        }
    }

    public void update(float delta) {
        // 애니메이션 업데이트 등
    }

    public void onRoomUpdated(List<Integer> updatedPlayers) {
        // 플레이어 목록이 변경되었을 때 호출
        playerIds = updatedPlayers;
        loadPlayerData();
    }

    public void render(SpriteBatch batch, float x, float y,
                       float width, float height) {
        // 2x2 그리드로 플레이어 표시
        float cellWidth = width / 2;
        float cellHeight = height / 2;

        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                float cellX = x + col * cellWidth;
                float cellY = y + (1 - row) * cellHeight;  // 위에서 아래로

                if (index < playerIds.size()) {
                    int playerId = playerIds.get(index);

                    // 셀 배경
                    batch.setColor(0.2f, 0.2f, 0.3f, 1f);
                    batch.draw(화이트픽셀, cellX, cellY, cellWidth, cellHeight);
                    batch.setColor(1, 1, 1, 1);

                    // 캐릭터 스프라이트
                    Sprite sprite = characterSprites.get(playerId);
                    if (sprite != null) {
                        sprite.setPosition(cellX + 20, cellY + 20);
                        sprite.draw(batch);
                    }

                    // 플레이어 닉네임
                    String name = playerNames.get(playerId);
                    BitmapFont font = AssetManager.getInstance().getFont();
                    font.draw(batch, name, cellX + 20, cellY + 80);
                }
                index++;
            }
        }
    }

    public void dispose() {
        // 리소스 해제
    }
}
```

### 3. ChatPanel 클래스

```java
/**
 * 채팅 패널
 *
 * 메시지 목록과 입력 필드를 관리합니다.
 */
public class ChatPanel {
    private List<String> messages = new ArrayList<>();  // 최근 10개
    private String inputText = "";
    private static final int MAX_MESSAGES = 10;

    public void update(float delta) {
        // 입력 처리 (텍스트 입력, 백스페이스 등)
    }

    public void addMessage(String sender, String text) {
        // 메시지 추가
        String fullMessage = sender + ": " + text;
        messages.add(fullMessage);

        // 최대 10개 유지
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    public void onChatMessageReceived(ChatMsg msg) {
        addMessage(msg.sender, msg.text);
    }

    public boolean isSendButtonPressed() {
        // [전송] 버튼 클릭 여부
        // 구현 필요
        return false;
    }

    public String getInputText() {
        return inputText;
    }

    public void clearInput() {
        inputText = "";
    }

    public void render(SpriteBatch batch, float x, float y,
                       float width, float height) {
        // 배경
        batch.setColor(0.15f, 0.15f, 0.15f, 1f);
        batch.draw(화이트픽셀, x, y, width, height);
        batch.setColor(1, 1, 1, 1);

        BitmapFont font = AssetManager.getInstance().getFont();
        float lineHeight = 20f;
        float startY = y + height - lineHeight;

        // 메시지 목록 (역순, 최신이 아래)
        for (int i = 0; i < messages.size(); i++) {
            font.draw(batch, messages.get(i),
                x + 10, startY - (i * lineHeight));
        }

        // 입력 필드 배경
        batch.setColor(0.2f, 0.2f, 0.2f, 1f);
        batch.draw(화이트픽셀, x, y, width - 60, 30);
        batch.setColor(1, 1, 1, 1);

        // 입력 텍스트
        font.draw(batch, inputText, x + 5, y + 20);

        // [전송] 버튼
        batch.setColor(0.3f, 0.6f, 0.3f, 1f);
        batch.draw(화이트픽셀, x + width - 55, y, 50, 30);
        batch.setColor(1, 1, 1, 1);
        font.draw(batch, "전송", x + width - 45, y + 15);
    }

    public void dispose() {
        messages.clear();
    }
}
```

### 4. ControlButtonPanel 클래스

```java
/**
 * 컨트롤 버튼 패널
 *
 * [게임시작] (호스트만) 과 [방나가기] 버튼을 관리합니다.
 */
public class ControlButtonPanel {
    private Button startGameButton;
    private Button leaveButton;
    private boolean isHost;

    public ControlButtonPanel(boolean isHost) {
        this.isHost = isHost;

        // 버튼 생성
        this.startGameButton = new Button("게임시작", isHost);  // 호스트만 활성화
        this.leaveButton = new Button("방나가기", true);
    }

    public void update(float delta) {
        startGameButton.update(delta);
        leaveButton.update(delta);
    }

    public boolean isStartGameButtonPressed() {
        return isHost && startGameButton.isPressed();
    }

    public boolean isLeaveButtonPressed() {
        return leaveButton.isPressed();
    }

    public void render(SpriteBatch batch, float x, float y,
                       float width, float height) {
        // [게임시작] 버튼 (상단)
        float buttonWidth = width - 20;
        float buttonHeight = height / 2 - 10;

        startGameButton.setPosition(x + 10, y + height / 2 + 5);
        startGameButton.setSize(buttonWidth, buttonHeight);
        startGameButton.render(batch);

        // [방나가기] 버튼 (하단)
        leaveButton.setPosition(x + 10, y + 5);
        leaveButton.setSize(buttonWidth, buttonHeight);
        leaveButton.render(batch);
    }

    public void dispose() {
        // 정리
    }
}
```

---

## 🧪 테스트 계획

### 단위 테스트

```java
/**
 * WaitingRoomScreen 테스트
 */
public class TestWaitingRoomScreen {
    private WaitingRoomScreen screen;
    private int testPlayerId = 1;
    private boolean isHost = true;

    @BeforeEach
    public void setUp() {
        screen = new WaitingRoomScreen(testPlayerId, isHost);
    }

    @Test
    public void 대기실_화면이_표시된다() {
        screen.show();
        assertNotNull(screen);
    }

    @Test
    public void 플레이어_정보_패널이_4명을_표시한다() {
        List<Integer> players = Arrays.asList(1, 2, 3, 4);
        screen.playerPanel.update(0.016f);
        // 4명 표시 확인
    }

    @Test
    public void 채팅_메시지를_추가할_수_있다() {
        screen.chatPanel.addMessage("플레이어1", "안녕하세요!");
        assertEquals(1, screen.chatPanel.messages.size());
    }

    @Test
    public void 최대_10개의_채팅_메시지만_보관한다() {
        for (int i = 0; i < 15; i++) {
            screen.chatPanel.addMessage("플레이어1", "메시지_" + i);
        }
        assertEquals(10, screen.chatPanel.messages.size());
    }

    @Test
    public void 호스트만_게임시작_버튼이_활성화된다() {
        assertTrue(screen.controlPanel.startGameButton.isEnabled() == isHost);
    }

    @Test
    public void 호스트가_아니면_게임시작_버튼이_비활성화된다() {
        WaitingRoomScreen notHostScreen = new WaitingRoomScreen(1, false);
        assertFalse(notHostScreen.controlPanel.startGameButton.isEnabled());
    }

    @Test
    public void 플레이어_목록이_업데이트된다() {
        List<Integer> updatedPlayers = Arrays.asList(1, 2, 3);
        screen.playerPanel.onRoomUpdated(updatedPlayers);
        assertEquals(3, screen.playerPanel.playerIds.size());
    }
}

/**
 * ChatPanel 테스트
 */
public class TestChatPanel {
    private ChatPanel chatPanel;

    @BeforeEach
    public void setUp() {
        chatPanel = new ChatPanel();
    }

    @Test
    public void 채팅_메시지를_수신한다() {
        ChatMsg msg = new ChatMsg();
        msg.sender = "플레이어1";
        msg.text = "안녕하세요!";

        chatPanel.onChatMessageReceived(msg);

        assertTrue(chatPanel.messages.get(0).contains("플레이어1"));
        assertTrue(chatPanel.messages.get(0).contains("안녕하세요!"));
    }

    @Test
    public void 입력_텍스트를_초기화할_수_있다() {
        chatPanel.inputText = "테스트 메시지";
        chatPanel.clearInput();
        assertEquals("", chatPanel.inputText);
    }
}

/**
 * PlayerInfoPanel 테스트
 */
public class TestPlayerInfoPanel {
    private PlayerInfoPanel playerPanel;

    @BeforeEach
    public void setUp() {
        List<Integer> players = Arrays.asList(1, 2, 3, 4);
        playerPanel = new PlayerInfoPanel(players);
    }

    @Test
    public void 플레이어_4명이_로드된다() {
        assertEquals(4, playerPanel.playerIds.size());
    }

    @Test
    public void 플레이어_정보가_캐시된다() {
        assertEquals("플레이어_1", playerPanel.playerNames.get(1));
        assertEquals("플레이어_2", playerPanel.playerNames.get(2));
    }

    @Test
    public void 플레이어_목록이_변경되면_업데이트된다() {
        List<Integer> updatedPlayers = Arrays.asList(1, 2);
        playerPanel.onRoomUpdated(updatedPlayers);
        assertEquals(2, playerPanel.playerIds.size());
    }
}
```

### 통합 테스트

```
[ ] 게임 시작 후 대기실이 표시된다
[ ] 플레이어들의 채팅이 실시간으로 동기화된다
[ ] 새 플레이어가 입장하면 UI가 업데이트된다
[ ] 호스트가 [게임시작]을 누르면 모두 GameScreen으로 전환
[ ] [방나가기]를 누르면 LobbyScreen으로 돌아간다
```

---

## ✅ 완료 조건

- [ ] WaitingRoomScreen 클래스 구현
- [ ] PlayerInfoPanel 클래스 구현
- [ ] ChatPanel 클래스 구현
- [ ] ControlButtonPanel 클래스 구현
- [ ] 모든 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] 채팅 메시지 실시간 동기화 확인
- [ ] 호스트/비호스트 버튼 상태 구분 동작 확인

---

## 🔗 다음 Phase 연결점

**PHASE_07: 게임 화면 기본 구성**
- GameScreen 클래스 생성
- 게임 루프 기본 구조
- 플레이어 렌더링

---

**참고**: SPEC_UI_SCREENS.md > WaitingRoomScreen 섹션 참조

