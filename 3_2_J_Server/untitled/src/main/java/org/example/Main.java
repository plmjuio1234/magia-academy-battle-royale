package org.example;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryo.Kryo;

import java.io.IOException;
import java.util.*;

public class Main {
    private static Map<Integer, GameRoom> rooms = new HashMap<>();
    private static Map<Integer, PlayerData> players = new HashMap<>();
    private static int nextRoomId = 1;

    public static class PlayerData {
        int id;
        String name;
        Connection connection;
        GameRoom currentRoom;
    }

    public static class GameRoom {
        int roomId;
        String roomName;
        int maxPlayers;
        List<PlayerData> players = new ArrayList<>();
        PlayerData host;
        boolean isPlaying = false;

        GameRoom(int id, String name, int max, PlayerData host) {
            this.roomId = id;
            this.roomName = name;
            this.maxPlayers = max;
            this.host = host;
            players.add(host);
        }

        boolean addPlayer(PlayerData player) {
            if (players.size() >= maxPlayers || isPlaying) {
                return false;
            }
            players.add(player);
            player.currentRoom = this;
            return true;
        }

        void removePlayer(PlayerData player) {
            players.remove(player);
            player.currentRoom = null;

            System.out.println("[방] 플레이어 퇴장: " + player.name + " (남은 인원: " + players.size() + ")");

            if (host == player) {
                if (players.isEmpty()) {
                    rooms.remove(this.roomId);
                    System.out.println("[방] 방 " + roomId + " (" + roomName + ") 삭제됨 - 모든 플레이어 퇴장");
                } else {
                    host = players.get(0);
                    System.out.println("[방] 방장 위임: " + player.name + " → " + host.name);
                    notifyRoomUpdate();
                }
            } else {
                if (players.isEmpty()) {
                    rooms.remove(this.roomId);
                    System.out.println("[방] 방 " + roomId + " (" + roomName + ") 삭제됨 - 마지막 플레이어 퇴장");
                } else {
                    notifyRoomUpdate();
                }
            }
        }

        void notifyRoomUpdate() {
            if (players.isEmpty()) {
                System.out.println("[방] 방 업데이트 알림 스킵 - 플레이어 없음");
                return;
            }

            RoomUpdateMsg msg = new RoomUpdateMsg();
            msg.players = new PlayerInfo[players.size()];
            for (int i = 0; i < players.size(); i++) {
                PlayerInfo info = new PlayerInfo();
                info.playerId = players.get(i).id;
                info.playerName = players.get(i).name;
                info.isHost = (players.get(i) == host);
                msg.players[i] = info;
            }
            msg.newHostId = host.id;

            for (PlayerData p : players) {
                p.connection.sendTCP(msg);
            }

            System.out.println("[방] 방 업데이트 알림 전송: " + players.size() + "명");
        }

        RoomInfo toRoomInfo() {
            RoomInfo info = new RoomInfo();
            info.roomId = this.roomId;
            info.roomName = this.roomName;
            info.currentPlayers = this.players.size();
            info.maxPlayers = this.maxPlayers;
            info.hostName = this.host.name;
            info.isPlaying = this.isPlaying;
            return info;
        }
    }

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

    public static void main(String[] args) {
        try {
            Server server = new Server(16384, 8192);

            Kryo kryo = server.getKryo();

            // ReflectASM 비활성화 (Java 모듈 문제 회피)
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

            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    System.out.println("[연결] ID: " + connection.getID());

                    PlayerData player = new PlayerData();
                    player.id = connection.getID();
                    player.name = "Player" + connection.getID();
                    player.connection = connection;
                    players.put(connection.getID(), player);
                }

                @Override
                public void disconnected(Connection connection) {
                    System.out.println("[연결 해제] ID: " + connection.getID());

                    PlayerData player = players.get(connection.getID());
                    if (player != null) {
                        if (player.currentRoom != null) {
                            System.out.println("[방] " + player.name + " 연결 끊김으로 방에서 제거");
                            player.currentRoom.removePlayer(player);
                        }
                        players.remove(connection.getID());
                    }
                }

                @Override
                public void received(Connection connection, Object object) {
                    PlayerData player = players.get(connection.getID());

                    if (object instanceof CreateRoomMsg) {
                        CreateRoomMsg msg = (CreateRoomMsg) object;

                        // 이미 방에 있으면 방 생성 불가
                        if (player.currentRoom != null) {
                            CreateRoomResponse response = new CreateRoomResponse();
                            response.success = false;
                            response.message = "이미 방에 있습니다";
                            connection.sendTCP(response);
                            System.out.println("[방] " + player.name + " 방 생성 실패 - 이미 방에 있음");
                            return;
                        }

                        GameRoom room = new GameRoom(nextRoomId++, msg.roomName,
                            msg.maxPlayers, player);
                        rooms.put(room.roomId, room);
                        player.currentRoom = room;  // 현재 방 설정!

                        CreateRoomResponse response = new CreateRoomResponse();
                        response.success = true;
                        response.roomId = room.roomId;
                        response.message = "방 생성 성공";
                        connection.sendTCP(response);

                        System.out.println("[방] " + player.name + "이(가) 방 생성: " + msg.roomName + " (ID: " + room.roomId + ")");
                    }

                    else if (object instanceof GetRoomListMsg) {
                        RoomListResponse response = new RoomListResponse();
                        response.rooms = new RoomInfo[rooms.size()];
                        int i = 0;
                        for (GameRoom room : rooms.values()) {
                            response.rooms[i++] = room.toRoomInfo();
                        }
                        connection.sendTCP(response);
                    }

                    else if (object instanceof JoinRoomMsg) {
                        JoinRoomMsg msg = (JoinRoomMsg) object;
                        GameRoom room = rooms.get(msg.roomId);

                        JoinRoomResponse response = new JoinRoomResponse();

                        // 이미 방에 있으면 입장 불가
                        if (player.currentRoom != null) {
                            response.success = false;
                            response.message = "이미 다른 방에 있습니다";
                            connection.sendTCP(response);
                            System.out.println("[방] " + player.name + " 입장 실패 - 이미 방에 있음");
                        } else if (room == null) {
                            response.success = false;
                            response.message = "방이 존재하지 않습니다";
                            connection.sendTCP(response);
                            System.out.println("[방] " + player.name + " 입장 실패 - 방 없음");
                        } else if (!room.addPlayer(player)) {
                            response.success = false;
                            response.message = "방이 꽉 찼거나 게임 중입니다";
                            connection.sendTCP(response);
                            System.out.println("[방] " + player.name + " 입장 실패 - 방 꽉참/게임중");
                        } else {
                            response.success = true;
                            response.message = "입장 성공";
                            response.roomInfo = room.toRoomInfo();

                            response.players = new PlayerInfo[room.players.size()];
                            for (int i = 0; i < room.players.size(); i++) {
                                PlayerInfo info = new PlayerInfo();
                                info.playerId = room.players.get(i).id;
                                info.playerName = room.players.get(i).name;
                                info.isHost = (room.players.get(i) == room.host);
                                response.players[i] = info;
                            }

                            room.notifyRoomUpdate();

                            System.out.println("[방] " + player.name + " 입장 성공: " + room.roomName + " (현재 " + room.players.size() + "명)");
                        }

                        connection.sendTCP(response);
                    }

                    else if (object instanceof LeaveRoomMsg) {
                        if (player.currentRoom != null) {
                            GameRoom room = player.currentRoom;
                            System.out.println("[방] " + player.name + " 방 나가기 요청 (방: " + room.roomName + ")");
                            room.removePlayer(player);
                        } else {
                            System.out.println("[방] " + player.name + " 방 나가기 요청했지만 방에 없음");
                        }
                    }

                    else if (object instanceof StartGameMsg) {
                        GameRoom room = player.currentRoom;
                        if (room != null && room.host == player) {
                            room.isPlaying = true;

                            GameStartNotification notification = new GameStartNotification();
                            notification.startTime = System.currentTimeMillis();

                            for (PlayerData p : room.players) {
                                p.connection.sendTCP(notification);
                            }

                            System.out.println("게임 시작: " + room.roomName);
                        }
                    }

                    else if (object instanceof ChatMsg) {
                        ChatMsg msg = (ChatMsg) object;
                        GameRoom room = player.currentRoom;

                        if (room != null) {
                            for (PlayerData p : room.players) {
                                p.connection.sendTCP(msg);
                            }
                        }
                    }

                    else if (object instanceof PlayerMoveMsg) {
                        PlayerMoveMsg msg = (PlayerMoveMsg) object;
                        msg.playerId = connection.getID();

                        GameRoom room = player.currentRoom;
                        if (room != null && room.isPlaying) {
                            // 같은 방의 다른 플레이어들에게 전송
                            for (PlayerData p : room.players) {
                                if (p.id != player.id) {
                                    p.connection.sendTCP(msg);
                                }
                            }
                        }
                    }

                    else if (object instanceof Messages) {
                        Messages msg = (Messages) object;
                        System.out.println("[수신] " + player.name + ": " + msg.text);

                        Messages echo = new Messages("에코: " + msg.text);
                        connection.sendTCP(echo);
                    }
                }
            });

            server.bind(5000, 5001);
            server.start();

            System.out.println("=================================");
            System.out.println("  방 기반 게임 서버 시작!");
            System.out.println("  TCP 포트: 5000");
            System.out.println("  UDP 포트: 5001 (미사용)");
            System.out.println("=================================");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n서버 종료 중...");
                server.stop();
            }));

            Thread.currentThread().join();

        } catch (IOException e) {
            System.err.println("서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("서버 인터럽트됨");
        }
    }
}
