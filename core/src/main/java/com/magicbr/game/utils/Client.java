package com.magicbr.game.utils;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryo.Kryo;

import java.io.IOException;

public class Client {
    private static final String SERVER_IP = "219.254.146.234";
    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;

    private com.esotericsoftware.kryonet.Client client;
    private boolean connected = false;

    private RoomInfo[] latestRoomList = null;
    private CreateRoomResponse latestCreateRoomResponse = null;
    private JoinRoomResponse latestJoinRoomResponse = null;
    private RoomUpdateMsg latestRoomUpdate = null;
    private GameStartNotification latestGameStart = null;
    private PlayerMoveMsg latestPlayerMove = null;

    public static class CreateRoomMsg {
        public String roomName;
        public int maxPlayers;
        public CreateRoomMsg() {}
    }

    public static class CreateRoomResponse {
        public boolean success;
        public int roomId;
        public String message;
        public CreateRoomResponse() {}
    }

    public static class GetRoomListMsg {
        public GetRoomListMsg() {}
    }

    public static class RoomInfo {
        public int roomId;
        public String roomName;
        public int currentPlayers;
        public int maxPlayers;
        public String hostName;
        public boolean isPlaying;
        public RoomInfo() {}
    }

    public static class RoomListResponse {
        public RoomInfo[] rooms;
        public RoomListResponse() {}
    }

    public static class JoinRoomMsg {
        public int roomId;
        public JoinRoomMsg() {}
    }

    public static class JoinRoomResponse {
        public boolean success;
        public String message;
        public RoomInfo roomInfo;
        public PlayerInfo[] players;
        public JoinRoomResponse() {}
    }

    public static class PlayerInfo {
        public int playerId;
        public String playerName;
        public boolean isHost;
        public PlayerInfo() {}
    }

    public static class LeaveRoomMsg {
        public LeaveRoomMsg() {}
    }

    public static class RoomUpdateMsg {
        public PlayerInfo[] players;
        public int newHostId;
        public RoomUpdateMsg() {}
    }

    public static class StartGameMsg {
        public StartGameMsg() {}
    }

    public static class GameStartNotification {
        public long startTime;
        public GameStartNotification() {}
    }

    public static class ChatMsg {
        public String sender;
        public String text;
        public ChatMsg() {}
    }

    public static class PlayerMoveMsg {
        public int playerId;
        public float x, y;
        public PlayerMoveMsg() {}
    }

    public Client() {
        client = new com.esotericsoftware.kryonet.Client(16384, 8192);

        Kryo kryo = client.getKryo();

        // ReflectASM 비활성화 (서버와 동일하게 설정)
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);

        kryo.register(Messages.class);
        kryo.register(CreateRoomMsg.class);
        kryo.register(CreateRoomResponse.class);
        kryo.register(GetRoomListMsg.class);
        kryo.register(RoomInfo.class);
        kryo.register(RoomInfo[].class);
        kryo.register(RoomListResponse.class);
        kryo.register(JoinRoomMsg.class);
        kryo.register(JoinRoomResponse.class);
        kryo.register(PlayerInfo.class);
        kryo.register(PlayerInfo[].class);
        kryo.register(LeaveRoomMsg.class);
        kryo.register(RoomUpdateMsg.class);
        kryo.register(StartGameMsg.class);
        kryo.register(GameStartNotification.class);
        kryo.register(ChatMsg.class);
        kryo.register(PlayerMoveMsg.class);

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("[클라이언트] 서버에 연결됨!");
                connected = true;
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("[클라이언트] 서버 연결 해제됨");
                connected = false;
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Messages) {
                    Messages msg = (Messages) object;
                    System.out.println("[클라이언트] 서버로부터 수신: " + msg.text);
                }
                else if (object instanceof CreateRoomResponse) {
                    CreateRoomResponse response = (CreateRoomResponse) object;
                    latestCreateRoomResponse = response;
                    System.out.println("[방 생성 응답] " + response.message);
                    if (response.success) {
                        System.out.println("  방 ID: " + response.roomId);
                    }
                }
                else if (object instanceof RoomListResponse) {
                    RoomListResponse response = (RoomListResponse) object;
                    latestRoomList = response.rooms;
                    System.out.println("[방 목록] 총 " + response.rooms.length + "개");
                    for (RoomInfo room : response.rooms) {
                        System.out.println("  [" + room.roomId + "] " + room.roomName +
                            " (" + room.currentPlayers + "/" + room.maxPlayers + ") " +
                            "방장: " + room.hostName);
                    }
                }
                else if (object instanceof JoinRoomResponse) {
                    JoinRoomResponse response = (JoinRoomResponse) object;
                    latestJoinRoomResponse = response;
                    System.out.println("[방 입장 응답] " + response.message);
                    if (response.success) {
                        System.out.println("  방: " + response.roomInfo.roomName);
                        System.out.println("  플레이어 목록:");
                        for (PlayerInfo player : response.players) {
                            String role = player.isHost ? " (방장)" : "";
                            System.out.println("    - " + player.playerName + role);
                        }
                    }
                }
                else if (object instanceof RoomUpdateMsg) {
                    RoomUpdateMsg msg = (RoomUpdateMsg) object;
                    latestRoomUpdate = msg;
                    System.out.println("[방 업데이트]");
                    for (PlayerInfo player : msg.players) {
                        String role = player.isHost ? " (방장)" : "";
                        System.out.println("  - " + player.playerName + role);
                    }
                }
                else if (object instanceof GameStartNotification) {
                    GameStartNotification notification = (GameStartNotification) object;
                    latestGameStart = notification;
                    System.out.println("[게임 시작!] 시작 시간: " + notification.startTime);
                }
                else if (object instanceof ChatMsg) {
                    ChatMsg msg = (ChatMsg) object;
                    System.out.println("[채팅] " + msg.sender + ": " + msg.text);
                }
                else if (object instanceof PlayerMoveMsg) {
                    PlayerMoveMsg msg = (PlayerMoveMsg) object;
                    latestPlayerMove = msg;
                }
            }
        });

        client.start();
    }

    public void connect() {
        try {
            System.out.println("[클라이언트] 서버 연결 시도: " + SERVER_IP + ":" + TCP_PORT);
            client.connect(5000, SERVER_IP, TCP_PORT, UDP_PORT);
            System.out.println("[클라이언트] 연결 성공!");

            Thread.sleep(500);
            sendMessage("ConnectionTest");
        } catch (IOException e) {
            System.err.println("[클라이언트] 연결 실패: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String text) {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        Messages msg = new Messages(text);
        client.sendTCP(msg);
        System.out.println("[클라이언트] 서버로 전송: " + text);
    }

    public void createRoom(String roomName, int maxPlayers) {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        CreateRoomMsg msg = new CreateRoomMsg();
        msg.roomName = roomName;
        msg.maxPlayers = maxPlayers;
        client.sendTCP(msg);
        System.out.println("[클라이언트] 방 생성 요청: " + roomName);
    }

    public void getRoomList() {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        GetRoomListMsg msg = new GetRoomListMsg();
        client.sendTCP(msg);
        System.out.println("[클라이언트] 방 목록 요청");
    }

    public void joinRoom(int roomId) {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        JoinRoomMsg msg = new JoinRoomMsg();
        msg.roomId = roomId;
        client.sendTCP(msg);
        System.out.println("[클라이언트] 방 입장 요청: " + roomId);
    }

    public void leaveRoom() {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        LeaveRoomMsg msg = new LeaveRoomMsg();
        client.sendTCP(msg);
        System.out.println("[클라이언트] 방 나가기 요청");
    }

    public void startGame() {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        StartGameMsg msg = new StartGameMsg();
        client.sendTCP(msg);
        System.out.println("[클라이언트] 게임 시작 요청");
    }

    public void sendChat(String sender, String text) {
        if (!connected) {
            System.err.println("[클라이언트] 서버에 연결되지 않음!");
            return;
        }

        ChatMsg msg = new ChatMsg();
        msg.sender = sender;
        msg.text = text;
        client.sendTCP(msg);
        System.out.println("[클라이언트] 채팅 전송: " + text);
    }

    public void sayHelloToServer() {
        sendMessage("Hello Server!");
    }

    public boolean isConnected() {
        return connected;
    }

    public RoomInfo[] getLatestRoomList() {
        RoomInfo[] temp = latestRoomList;
        latestRoomList = null;
        return temp;
    }

    public CreateRoomResponse getLatestCreateRoomResponse() {
        CreateRoomResponse temp = latestCreateRoomResponse;
        latestCreateRoomResponse = null;
        return temp;
    }

    public JoinRoomResponse getLatestJoinRoomResponse() {
        JoinRoomResponse temp = latestJoinRoomResponse;
        latestJoinRoomResponse = null;
        return temp;
    }

    public RoomUpdateMsg getLatestRoomUpdate() {
        RoomUpdateMsg temp = latestRoomUpdate;
        latestRoomUpdate = null;
        return temp;
    }

    public GameStartNotification getLatestGameStart() {
        GameStartNotification temp = latestGameStart;
        latestGameStart = null;
        return temp;
    }

    public PlayerMoveMsg getLatestPlayerMove() {
        PlayerMoveMsg temp = latestPlayerMove;
        latestPlayerMove = null;
        return temp;
    }

    public void sendPlayerMove(float x, float y) {
        if (!connected) return;

        PlayerMoveMsg msg = new PlayerMoveMsg();
        msg.x = x;
        msg.y = y;
        client.sendTCP(msg);
    }

    public void disconnect() {
        if (client != null) {
            client.close();
            System.out.println("[클라이언트] 연결 종료됨");
        }
    }
}
