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

        // 몬스터 시스템
        ServerMonsterManager monsterManager;
        Map<Integer, PlayerPosition> playerPositions = new HashMap<>();

        private CollisionMap collisionMap;

        static class PlayerPosition {
            float x, y;
            PlayerPosition(float x, float y) {
                this.x = x;
                this.y = y;
            }
        }

        GameRoom(int id, String name, int max, PlayerData host, CollisionMap collisionMap) {
            this.roomId = id;
            this.roomName = name;
            this.maxPlayers = max;
            this.host = host;
            this.collisionMap = collisionMap;
            players.add(host);

            // MonsterManager 초기화
            this.monsterManager = new ServerMonsterManager(
                (roomId, message) -> {
                    for (PlayerData player : players) {
                        player.connection.sendTCP(message);
                    }
                },
                collisionMap  //  MonsterManager에도 전달
            );
            this.monsterManager.initializeRoom(roomId);
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
            playerPositions.remove(player.id);  // 플레이어 위치 제거

            System.out.println("[방] 플레이어 퇴장: " + player.name + " (남은 인원: " + players.size() + ")");

            if (host == player) {
                if (players.isEmpty()) {
                    monsterManager.cleanupRoom(this.roomId);  // MonsterManager 정리
                    rooms.remove(this.roomId);
                    System.out.println("[방] 방 " + roomId + " (" + roomName + ") 삭제됨 - 모든 플레이어 퇴장");
                } else {
                    host = players.get(0);
                    System.out.println("[방] 방장 위임: " + player.name + " → " + host.name);
                    notifyRoomUpdate();
                }
            } else {
                if (players.isEmpty()) {
                    monsterManager.cleanupRoom(this.roomId);  // MonsterManager 정리
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

    // 플레이어 이름 설정 메시지 (클라이언트 → 서버)
    public static class SetPlayerNameMsg {
        public String playerName;

        public SetPlayerNameMsg() {}
    }

    // 이름 설정 응답 (서버 → 클라이언트)
    public static class SetPlayerNameResponse {
        public boolean success;
        public String message;

        public SetPlayerNameResponse() {}
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
        public RoomInfo roomInfo;
        public PlayerInfo[] players;

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

    public static class SkillCastMsg {
        public int playerId;
        public int skillId;
        public float targetX, targetY;

        // 스킬의 모든 필요한 정보 (클라이언트에서 전송)
        public String skillName;
        public String elementColor;      // "불", "물", "바람" 등
        public int baseDamage;
        public float projectileSpeed;    // 스킬별 속도
        public float projectileRadius;   // 투사체 크기
        public float projectileLifetime; // 투사체 수명

        public SkillCastMsg() {}
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(16384, 8192);

            CollisionMap collisionMap = TMXCollisionParser.parse(
                "resources/maps/magical-school-map.tmx"
            );

            if (collisionMap == null) {
                System.err.println("[서버] 충돌 맵 로드 실패 - 서버 종료");
                return;
            }

            System.out.println("[서버] 충돌 맵 로드 완료 - 몬스터 이동에 사용");

            Kryo kryo = server.getKryo();

            // ReflectASM 비활성화 (Java 모듈 문제 회피)
            kryo.setRegistrationRequired(false);
            kryo.setReferences(true);

            kryo.register(CreateRoomMsg.class);                      // ID: 10
            kryo.register(CreateRoomResponse.class);                 // ID: 11
            kryo.register(GetRoomListMsg.class);                     // ID: 12
            kryo.register(RoomInfo.class);                           // ID: 13
            kryo.register(RoomInfo[].class);                         // ID: 14
            kryo.register(RoomListResponse.class);                   // ID: 15
            kryo.register(JoinRoomMsg.class);                        // ID: 16
            kryo.register(JoinRoomResponse.class);                   // ID: 17
            kryo.register(PlayerInfo.class);                         // ID: 18
            kryo.register(PlayerInfo[].class);                       // ID: 19
            kryo.register(LeaveRoomMsg.class);                       // ID: 20
            kryo.register(RoomUpdateMsg.class);                      // ID: 21
            kryo.register(StartGameMsg.class);                       // ID: 22
            kryo.register(GameStartNotification.class);              // ID: 23
            kryo.register(ChatMsg.class);                            // ID: 24
            kryo.register(PlayerMoveMsg.class);                      // ID: 25
            kryo.register(SkillCastMsg.class);                       // ID: 26
            kryo.register(MonsterSpawnMsg.class);                    // ID: 27
            kryo.register(MonsterUpdateMsg.class);                   // ID: 28
            kryo.register(MonsterDeathMsg.class);                    // ID: 29
            kryo.register(PlayerAttackMonsterMsg.class);             // ID: 30
            kryo.register(MonsterDamageMsg.class);                   // ID: 31
            kryo.register(ProjectileFiredMsg.class);                 // ID: 32
            kryo.register(SetPlayerNameMsg.class);                   // ID: 33
            kryo.register(SetPlayerNameResponse.class);              // ID: 34

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

                        if (player.currentRoom != null) {
                            CreateRoomResponse response = new CreateRoomResponse();
                            response.success = false;
                            response.message = "이미 방에 있습니다";
                            connection.sendTCP(response);
                            System.out.println("[방] " + player.name + " 방 생성 실패 - 이미 방에 있음");
                            return;
                        }

                        // ⭐ 방 생성 시 충돌 맵 전달
                        GameRoom room = new GameRoom(
                            nextRoomId++,
                            msg.roomName,
                            msg.maxPlayers,
                            player,
                            collisionMap  // 몬스터 이동 시 벽 충돌 체크용
                        );
                        rooms.put(room.roomId, room);
                        player.currentRoom = room;

                        CreateRoomResponse response = new CreateRoomResponse();
                        response.success = true;
                        response.roomId = room.roomId;
                        response.message = "방 생성 성공";
                        response.roomInfo = room.toRoomInfo();

                        response.players = new PlayerInfo[1];
                        PlayerInfo hostInfo = new PlayerInfo();
                        hostInfo.playerId = player.id;
                        hostInfo.playerName = player.name;
                        hostInfo.isHost = true;
                        response.players[0] = hostInfo;

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
                        System.out.println("[게임 시작 요청] " + player.name + "이(가) StartGameMsg 전송");

                        GameRoom room = player.currentRoom;
                        if (room == null) {
                            System.out.println("[게임 시작 실패] " + player.name + "은(는) 방에 없음");
                        } else if (room.host != player) {
                            System.out.println("[게임 시작 실패] " + player.name + "은(는) 방장이 아님 (방장: " + room.host.name + ")");
                        } else {
                            room.isPlaying = true;

                            // 몬스터 시스템 초기화 (50마리 즉시 스폰)
                            System.out.println("[게임 시작] 룸 " + room.roomId + " 초기 몬스터 스폰");

                            GameStartNotification notification = new GameStartNotification();
                            notification.startTime = System.currentTimeMillis();

                            for (PlayerData p : room.players) {
                                p.connection.sendTCP(notification);
                                System.out.println("[게임 시작 알림] " + p.name + "에게 GameStartNotification 전송");
                            }

                            System.out.println("[게임 시작 성공] 방: " + room.roomName + " (" + room.players.size() + "명)");
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
                            // 플레이어 위치 저장 (몬스터 AI용)
                            room.playerPositions.put(
                                player.id,
                                new GameRoom.PlayerPosition(msg.x, msg.y)
                            );

                            // 같은 방의 다른 플레이어들에게 전송
                            for (PlayerData p : room.players) {
                                if (p.id != player.id) {
                                    p.connection.sendTCP(msg);
                                }
                            }
                        }
                    }

                    else if (object instanceof SkillCastMsg) {
                        SkillCastMsg msg = (SkillCastMsg) object;
                        msg.playerId = connection.getID();

                        GameRoom room = player.currentRoom;
                        if (room != null && room.isPlaying) {
                            // 같은 방의 다른 플레이어들에게 스킬 시전 알림
                            for (PlayerData p : room.players) {
                                if (p.id != player.id) {
                                    p.connection.sendTCP(msg);
                                }
                            }
                            System.out.println("[스킬] " + player.name + "이(가) 스킬 #" + msg.skillId + " 시전");
                        }
                    }

                    // ===== 발사체 발사 처리 =====
                    else if (object instanceof ProjectileFiredMsg) {
                        ProjectileFiredMsg msg = (ProjectileFiredMsg) object;
                        msg.playerId = connection.getID();

                        GameRoom room = player.currentRoom;
                        if (room != null && room.isPlaying) {
                            // 같은 방의 모든 플레이어에게 발사체 정보 전송 (자신 포함)
                            for (PlayerData p : room.players) {
                                p.connection.sendTCP(msg);
                            }
                            System.out.println("[발사체] " + player.name + "이(가) " + msg.skillType + " 발사 (타겟: " + msg.targetMonsterId + ")");
                        }
                    }

                    // ===== 몬스터 공격 처리 =====
                    else if (object instanceof PlayerAttackMonsterMsg) {
                        PlayerAttackMonsterMsg attackMsg =
                            (PlayerAttackMonsterMsg) object;

                        GameRoom room = player.currentRoom;
                        if (room != null && room.isPlaying) {
                            // 몬스터 찾기
                            org.example.ServerMonster targetMonster = null;
                            List<org.example.ServerMonster> monsters = room.monsterManager.getAllMonsters(room.roomId);

                            for (org.example.ServerMonster monster : monsters) {
                                if (monster.id == attackMsg.monsterId) {
                                    targetMonster = monster;
                                    break;
                                }
                            }

                            if (targetMonster != null && targetMonster.isAlive()) {
                                // 거리 검증 (공격 사거리 내인지 확인)
                                double distance = Math.sqrt(
                                    Math.pow(attackMsg.attackerX - targetMonster.x, 2) +
                                    Math.pow(attackMsg.attackerY - targetMonster.y, 2)
                                );

                                // 공격 사거리: 150 픽셀 (스킬 범위 기준)
                                if (distance <= 150.0) {
                                    // 데미지 적용 (ServerMonsterManager를 통해 처리)
                                    int damageAmount = (int) attackMsg.skillDamage;
                                    room.monsterManager.damageMonster(room.roomId, attackMsg.monsterId, damageAmount, player.id);

                                    System.out.println("[공격 성공] " + player.name + " → 몬스터 ID=" + attackMsg.monsterId +
                                        ", 데미지=" + damageAmount);
                                }
                                // 공격 실패 로그 제거 (범위 밖 공격은 무시)
                            }
                        }
                    }

                    else if (object instanceof Messages) {
                        Messages msg = (Messages) object;
                        System.out.println("[수신] " + player.name + ": " + msg.text);

                        Messages echo = new Messages("에코: " + msg.text);
                        connection.sendTCP(echo);
                    }

                    else if (object instanceof SetPlayerNameMsg) {
                        SetPlayerNameMsg msg = (SetPlayerNameMsg) object;

                        SetPlayerNameResponse response = new SetPlayerNameResponse();

                        // 이름 유효성 검사
                        if (msg.playerName == null || msg.playerName.trim().isEmpty()) {
                            response.success = false;
                            response.message = "이름을 입력해주세요";
                        } else if (msg.playerName.length() > 12) {
                            response.success = false;
                            response.message = "이름은 12자 이내로 입력해주세요";
                        } else if (player.currentRoom != null) {
                            response.success = false;
                            response.message = "방에 있을 때는 이름을 변경할 수 없습니다";
                        } else {
                            // 이름 설정 성공
                            player.name = msg.playerName.trim();
                            response.success = true;
                            response.message = "이름이 설정되었습니다";
                            System.out.println("[이름 설정] ID=" + player.id + " → " + player.name);
                        }

                        connection.sendTCP(response);
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

            // ===== 서버 게임 루프 (20Hz) =====
            Thread gameLoopThread = new Thread(() -> {
                final float TICK_DURATION = 50f; // 20Hz (50ms)
                System.err.println("[게임 루프] 서버 게임 루프 시작 (20Hz) !!!");
                int tickCount = 0;

                while (true) {
                    long startTime = System.currentTimeMillis();

                    try {
                        // 모든 게임 중인 방 업데이트
                        synchronized (rooms) {
                            if (tickCount % 20 == 0) {
                                System.err.println("[게임 루프] 틱 #" + tickCount + " - 활성 방: " + rooms.size());
                            }

                            for (GameRoom room : rooms.values()) {
                                if (room.isPlaying && !room.players.isEmpty()) {
                                    // 활성 플레이어 ID 목록
                                    List<Integer> activePlayerIds = new ArrayList<>();
                                    for (PlayerData p : room.players) {
                                        activePlayerIds.add(p.id);
                                    }

                                    // playerPositions를 float[] 형식으로 변환
                                    Map<Integer, float[]> playerPosMap = new HashMap<>();
                                    for (Map.Entry<Integer, GameRoom.PlayerPosition> entry : room.playerPositions.entrySet()) {
                                        playerPosMap.put(entry.getKey(), new float[]{entry.getValue().x, entry.getValue().y});
                                    }

                                    // 몬스터 매니저 업데이트 (플레이어 위치 전달)
                                    room.monsterManager.update(0.05f, room.roomId, activePlayerIds, playerPosMap);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[게임 루프] 에러: " + e.getMessage());
                        e.printStackTrace();
                    }

                    // 틱 속도 유지
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < TICK_DURATION) {
                        try {
                            Thread.sleep((long)(TICK_DURATION - elapsed));
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    tickCount++;
                }
            });
            System.err.println("#### 게임 루프 스레드 생성 완료 ####");
            System.err.flush();
            gameLoopThread.setDaemon(true);
            gameLoopThread.setName("GameLoopThread");
            System.err.println("#### 게임 루프 스레드 시작 전 ####");
            System.err.flush();
            gameLoopThread.start();
            System.err.println("#### 게임 루프 스레드 시작 후 ####");
            System.err.flush();

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
