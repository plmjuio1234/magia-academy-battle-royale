package org.example;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryo.Kryo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

        // ===== Fog 시스템 (PHASE_24) =====
        // fog 활성화 순서 (랜덤, town-square는 마지막)
        List<String> fogActivationOrder = new ArrayList<>();
        // 현재 활성화된 fog 구역들
        Set<String> activeFogZones = new HashSet<>();
        // 게임 경과 시간 (초)
        float gameTime = 0f;
        // 다음 fog 활성화 인덱스
        int nextFogIndex = 0;
        // fog 활성화 간격 (2분)
        static final float FOG_INTERVAL = 120f;
        // fog 데미지 (초당)
        static final int FOG_DAMAGE_PER_SECOND = 5;
        // fog 데미지 적용 간격 (서버 틱 기준)
        float fogDamageTimer = 0f;
        // 플레이어 최대 HP
        static final int PLAYER_MAX_HP = 100;
        // 사망한 플레이어 ID 목록 (순서대로)
        Set<Integer> deadPlayers = new HashSet<>();
        // 게임 종료 여부
        boolean gameEnded = false;

        // HP 자동 재생 시스템
        float hpRegenTimer = 0f;
        static final float HP_REGEN_INTERVAL = 2.0f;  // 2초마다
        static final int HP_REGEN_AMOUNT = 4;  // 2초마다 4 HP (= 2 HP/초)

        // 4개 구역 스폰 좌표 (사용자 확인 좌표)
        static final float[][] SPAWN_ZONES = {
            {1363f, 2734f},   // 좌상단
            {2627f, 3019f},   // 우상단
            {1303f, 1262f},   // 좌하단
            {2827f, 1237f}    // 우하단
        };

        /**
         * 플레이어 스폰 위치를 생성합니다.
         * 4개 구역 고정 위치 사용 (랜덤 오프셋 제거)
         */
        float[] getSpawnPosition(int playerIndex) {
            int zoneIndex = playerIndex % SPAWN_ZONES.length;
            float spawnX = SPAWN_ZONES[zoneIndex][0];
            float spawnY = SPAWN_ZONES[zoneIndex][1];

            return new float[]{spawnX, spawnY};
        }

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
                new ServerMonsterManager.MessageCallback() {
                    @Override
                    public void broadcast(int roomId, Object message) {
                        for (PlayerData player : players) {
                            player.connection.sendTCP(message);
                        }
                    }

                    @Override
                    public void sendToPlayer(int playerId, Object message) {
                        for (PlayerData player : players) {
                            if (player.id == playerId) {
                                player.connection.sendTCP(message);
                                break;
                            }
                        }
                    }
                },
                collisionMap  //  MonsterManager에도 전달
            );
            this.monsterManager.initializeRoom(roomId);

            // Fog 활성화 순서 초기화 (PHASE_24)
            initializeFogOrder();
        }

        /**
         * fog 활성화 순서를 랜덤으로 생성합니다.
         * town-square는 마지막에 활성화됩니다.
         */
        void initializeFogOrder() {
            fogActivationOrder.clear();
            activeFogZones.clear();

            // town-square 제외한 구역들
            List<String> otherZones = new ArrayList<>();
            otherZones.add("dormitory");
            otherZones.add("library");
            otherZones.add("classroom");
            otherZones.add("alchemy-room");

            // 랜덤 셔플
            Collections.shuffle(otherZones);

            // fog 활성화 순서에 추가
            fogActivationOrder.addAll(otherZones);

            // town-square는 마지막
            fogActivationOrder.add("town-square");

            System.out.println("[방 " + roomId + "] fog 활성화 순서: " + fogActivationOrder);
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
        public float spawnX;  // 스폰 X 좌표
        public float spawnY;  // 스폰 Y 좌표

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
        public PlayerInfo[] players;  // 추가: 플레이어 이름 정보

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

        // 시전자 위치 (방향 계산에 필수)
        public float casterX;
        public float casterY;

        // 스킬의 모든 필요한 정보 (클라이언트에서 전송)
        public String skillName;
        public String elementColor;      // "불", "물", "바람" 등
        public int baseDamage;
        public float projectileSpeed;    // 스킬별 속도
        public float projectileRadius;   // 투사체 크기
        public float projectileLifetime; // 투사체 수명

        // 스킬 타입 (동기화 방식 결정용)
        public int skillType;            // 0: Projectile, 1: Zone(고정), 2: Zone(이동), 3: Zone(플레이어 추적)

        // 이동형 Zone용 추가 데이터
        public float directionX;         // 이동 방향 X
        public float directionY;         // 이동 방향 Y

        // 다방향 발사용 (IceSpike)
        public int projectileCount;      // 발사체 개수 (1이면 단일, 3이면 3방향 등)
        public float angleSpread;        // 발사 각도 간격 (도)

        public SkillCastMsg() {}
    }

    // ===== Fog 시스템 업데이트 (PHASE_24) =====
    /**
     * fog 시스템을 업데이트합니다.
     * 2분마다 새로운 구역의 fog를 활성화하고, 활성화된 구역 내 플레이어에게 데미지를 줍니다.
     *
     * @param room 게임방
     * @param delta 틱 시간 (초)
     */
    private static void updateFogSystem(GameRoom room, float delta) {
        // 게임 시간 업데이트
        room.gameTime += delta;

        // fog 활성화 체크 (2분마다)
        if (room.nextFogIndex < room.fogActivationOrder.size()) {
            float nextActivationTime = (room.nextFogIndex + 1) * GameRoom.FOG_INTERVAL;

            if (room.gameTime >= nextActivationTime) {
                // 새로운 fog 구역 활성화
                String zoneName = room.fogActivationOrder.get(room.nextFogIndex);
                room.activeFogZones.add(zoneName);
                room.nextFogIndex++;

                // 모든 플레이어에게 fog 활성화 알림
                FogZoneMsg fogMsg = new FogZoneMsg(zoneName, true, room.gameTime);
                for (PlayerData player : room.players) {
                    player.connection.sendTCP(fogMsg);
                }

                System.out.println("[방 " + room.roomId + "] ★ fog 활성화: " + zoneName +
                    " (" + room.activeFogZones.size() + "/" + room.fogActivationOrder.size() + ") - 게임시간: " + (int)room.gameTime + "초");
            }
        }

        // fog 데미지 처리 (1초마다)
        room.fogDamageTimer += delta;
        if (room.fogDamageTimer >= 1.0f) {
            room.fogDamageTimer = 0f;
            applyFogDamageToPlayers(room);
        }

        // HP 자동 재생 처리 (2초마다)
        room.hpRegenTimer += delta;
        if (room.hpRegenTimer >= GameRoom.HP_REGEN_INTERVAL) {
            room.hpRegenTimer -= GameRoom.HP_REGEN_INTERVAL;
            applyHpRegenerationToPlayers(room);
        }
    }

    /**
     * 모든 플레이어에게 HP 자동 재생을 적용합니다.
     * fog 구역 밖에 있고, 최대 HP 미만인 플레이어만 회복합니다.
     *
     * @param room 게임방
     */
    private static void applyHpRegenerationToPlayers(GameRoom room) {
        for (PlayerData player : room.players) {
            // 사망한 플레이어는 스킵
            if (room.deadPlayers.contains(player.id)) {
                continue;
            }

            GameRoom.PlayerPosition pos = room.playerPositions.get(player.id);
            if (pos == null) {
                continue;
            }

            // 현재 HP 확인
            int currentHp = room.monsterManager.getPlayerHp(room.roomId, player.id);
            if (currentHp >= GameRoom.PLAYER_MAX_HP) {
                continue;  // 이미 최대 HP
            }

            // fog 구역 체크: fog 구역 밖에서만 재생
            String playerZone = getPlayerZone(pos.x, pos.y, room.collisionMap);
            boolean inActiveFogZone = (playerZone != null && room.activeFogZones.contains(playerZone));

            if (!inActiveFogZone) {
                // fog 밖에서만 HP 재생
                int newHp = Math.min(GameRoom.PLAYER_MAX_HP, currentHp + GameRoom.HP_REGEN_AMOUNT);
                room.monsterManager.setPlayerHp(room.roomId, player.id, newHp);

                // HP 재생 알림 (FogDamageMsg를 재사용, damage를 음수로 표시하여 회복 의미)
                FogDamageMsg regenMsg = new FogDamageMsg(
                    player.id,
                    -GameRoom.HP_REGEN_AMOUNT,  // 음수 = 회복
                    newHp,
                    "hp_regen"  // 특수 구역 이름
                );
                player.connection.sendTCP(regenMsg);

                System.out.println("[HP 재생] " + player.name + ": " + currentHp + " → " + newHp + " HP");
            }
        }
    }

    /**
     * 활성화된 fog 구역 내 플레이어에게 데미지를 적용합니다.
     *
     * @param room 게임방
     */
    private static void applyFogDamageToPlayers(GameRoom room) {
        // 활성화된 fog 구역이 없으면 리턴
        if (room.activeFogZones.isEmpty()) {
            return;
        }

        // 각 플레이어 체크
        for (PlayerData player : room.players) {
            GameRoom.PlayerPosition pos = room.playerPositions.get(player.id);
            if (pos == null) {
                continue; // 위치 정보 없음
            }

            // 플레이어가 어떤 fog 구역에 있는지 확인
            String playerZone = getPlayerZone(pos.x, pos.y, room.collisionMap);

            // 해당 구역의 fog가 활성화되어 있으면 데미지
            if (playerZone != null && room.activeFogZones.contains(playerZone)) {
                // MonsterManager에서 HP 가져오기 (단일 HP 저장소 사용)
                int currentHp = room.monsterManager.getPlayerHp(room.roomId, player.id);
                int newHp = Math.max(0, currentHp - GameRoom.FOG_DAMAGE_PER_SECOND);
                room.monsterManager.setPlayerHp(room.roomId, player.id, newHp);

                // 데미지 메시지 전송
                FogDamageMsg damageMsg = new FogDamageMsg(
                    player.id,
                    GameRoom.FOG_DAMAGE_PER_SECOND,
                    newHp,
                    playerZone
                );
                player.connection.sendTCP(damageMsg);

                // 디버그 로그 (매 10초마다만 출력)
                if ((int)room.gameTime % 10 == 0) {
                    System.out.println("[방 " + room.roomId + "] fog 데미지: " + player.name +
                        " (" + playerZone + ") HP: " + newHp);
                }
            }
        }
    }

    /**
     * 플레이어의 좌표를 기반으로 어떤 구역에 있는지 판별합니다.
     * TMX 맵의 fog 레이어 타일 데이터를 기반으로 정확히 판별합니다.
     *
     * @param x 플레이어 X 좌표 (픽셀)
     * @param y 플레이어 Y 좌표 (픽셀)
     * @param collisionMap 충돌 맵 (fog 구역 데이터 포함)
     * @return 구역 이름 (null이면 fog 구역 밖)
     */
    private static String getPlayerZone(float x, float y, CollisionMap collisionMap) {
        // CollisionMap의 fog 구역 데이터를 사용하여 정확하게 판별
        if (collisionMap != null && collisionMap.hasFogZones()) {
            return collisionMap.getFogZoneAt(x, y);
        }

        // fog 구역 데이터가 없으면 null 반환 (데미지 없음)
        return null;
    }

    /**
     * 플레이어 사망 체크 및 1등 판정 (PHASE_26)
     * MonsterManager의 HP를 체크하고 사망 처리를 합니다.
     */
    private static void checkPlayerDeathsAndWinner(GameRoom room) {
        // 생존 플레이어 목록 계산
        List<PlayerData> alivePlayers = new ArrayList<>();
        for (PlayerData p : room.players) {
            if (!room.deadPlayers.contains(p.id)) {
                // HP 체크 (MonsterManager에서 단일 HP 저장소 사용)
                int effectiveHp = room.monsterManager.getPlayerHp(room.roomId, p.id);

                if (effectiveHp <= 0) {
                    // 사망 처리
                    room.deadPlayers.add(p.id);

                    // 사망 순위 = 현재 생존자 수 + 이미 죽은 사람 수
                    int rank = room.players.size() - room.deadPlayers.size() + 1;

                    // 사망 원인 판별 (환경 사망)
                    String killerName = "안개/몬스터";
                    int killerId = -1;  // -1 = 환경 사망

                    // 사망 메시지 전송
                    PlayerDeathMsg deathMsg = new PlayerDeathMsg(
                        p.id, p.name,
                        killerId, killerName,
                        rank
                    );

                    for (PlayerData player : room.players) {
                        player.connection.sendTCP(deathMsg);
                    }

                    System.out.println("[사망] " + p.name + " (" + killerName + "에게 사망) - 순위: " + rank + "등");
                } else {
                    alivePlayers.add(p);
                }
            }
        }

        // 1등 판정: 생존자가 1명만 남으면 우승
        if (alivePlayers.size() == 1 && room.players.size() > 1) {
            PlayerData winner = alivePlayers.get(0);

            // 1등 우승 메시지 전송
            PlayerDeathMsg winMsg = new PlayerDeathMsg(
                winner.id, winner.name,
                0, "우승",  // killerId 0 = 우승
                1  // 1등
            );

            for (PlayerData player : room.players) {
                player.connection.sendTCP(winMsg);
            }

            room.gameEnded = true;
            System.out.println("[우승] " + winner.name + " - 1등!");
        }
        // 모든 플레이어가 죽으면 게임 종료
        else if (alivePlayers.isEmpty()) {
            room.gameEnded = true;
            System.out.println("[게임 종료] 모든 플레이어 사망");
        }
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
            kryo.register(FogZoneMsg.class);                         // ID: 35 (PHASE_24)
            kryo.register(FogDamageMsg.class);                       // ID: 36 (PHASE_24)
            kryo.register(MonsterAttackPlayerMsg.class);             // ID: 37 (PHASE_25)
            kryo.register(PlayerAttackPlayerMsg.class);              // ID: 38 (PHASE_25 PVP)
            kryo.register(PlayerDeathMsg.class);                     // ID: 39 (PHASE_25)
            kryo.register(PlayerLevelUpMsg.class);                   // ID: 40 (레벨업 HP 동기화)

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

                            // ===== 플레이어 HP 초기화 (PHASE_25) =====
                            for (PlayerData p : room.players) {
                                room.monsterManager.initializePlayerHp(room.roomId, p.id);
                            }

                            GameStartNotification notification = new GameStartNotification();
                            notification.startTime = System.currentTimeMillis();

                            // 플레이어 정보 포함 (스폰 위치 할당)
                            notification.players = new PlayerInfo[room.players.size()];
                            for (int i = 0; i < room.players.size(); i++) {
                                PlayerInfo info = new PlayerInfo();
                                info.playerId = room.players.get(i).id;
                                info.playerName = room.players.get(i).name;  // 서버에 저장된 이름
                                info.isHost = (room.players.get(i) == room.host);

                                // 스폰 위치 할당 (4개 구역 중 하나)
                                float[] spawnPos = room.getSpawnPosition(i);
                                info.spawnX = spawnPos[0];
                                info.spawnY = spawnPos[1];

                                // 플레이어 위치 초기화 (몬스터 AI용)
                                room.playerPositions.put(room.players.get(i).id,
                                    new GameRoom.PlayerPosition(spawnPos[0], spawnPos[1]));

                                notification.players[i] = info;
                                System.out.println("[스폰] " + info.playerName + " → (" + info.spawnX + ", " + info.spawnY + ")");
                            }

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
                            // 같은 방의 모든 플레이어에게 발사체 정보 전송 (자신 제외 - 자신은 이미 발사체 생성함)
                            for (PlayerData p : room.players) {
                                if (p.id != msg.playerId) {
                                    p.connection.sendTCP(msg);
                                }
                            }
                            // 로그 출력 (PVP/몬스터 구분)
                            if (msg.targetPlayerId >= 0) {
                                System.out.println("[PVP 발사체] " + player.name + "이(가) " + msg.skillType + " 발사 (타겟 플레이어: " + msg.targetPlayerId + ")");
                            } else {
                                System.out.println("[발사체] " + player.name + "이(가) " + msg.skillType + " 발사 (타겟 몬스터: " + msg.targetMonsterId + ")");
                            }
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

                                // 공격 사거리: 250 픽셀 (세로 화면 기준 + 여유)
                                if (distance <= 250.0) {
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

                    // ===== PVP 공격 처리 (PHASE_25) =====
                    else if (object instanceof PlayerAttackPlayerMsg) {
                        PlayerAttackPlayerMsg msg = (PlayerAttackPlayerMsg) object;
                        msg.attackerId = connection.getID();  // 공격자 ID 설정

                        GameRoom room = player.currentRoom;
                        if (room != null && room.isPlaying) {
                            // 타겟 플레이어 찾기
                            PlayerData target = null;
                            for (PlayerData p : room.players) {
                                if (p.id == msg.targetId) {
                                    target = p;
                                    break;
                                }
                            }

                            if (target != null) {
                                // MonsterManager에서 HP 감소
                                int currentHp = room.monsterManager.getPlayerHp(room.roomId, target.id);
                                int newHp = Math.max(0, currentHp - msg.damage);
                                room.monsterManager.setPlayerHp(room.roomId, target.id, newHp);

                                // 응답 메시지 설정
                                msg.newHp = newHp;
                                msg.maxHp = GameRoom.PLAYER_MAX_HP;

                                // 모든 플레이어에게 브로드캐스트
                                for (PlayerData p : room.players) {
                                    p.connection.sendTCP(msg);
                                }

                                System.out.println("[PVP] " + player.name + " → " + target.name +
                                    " (" + msg.damage + " 데미지, HP: " + newHp + ")");
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

                    // ===== 레벨업 HP 동기화 처리 =====
                    else if (object instanceof PlayerLevelUpMsg) {
                        PlayerLevelUpMsg msg = (PlayerLevelUpMsg) object;

                        GameRoom room = player.currentRoom;
                        if (room != null && room.isPlaying) {
                            // MonsterManager의 HP 업데이트 (단일 HP 저장소 사용)
                            room.monsterManager.setPlayerHp(room.roomId, player.id, msg.newCurrentHp);

                            System.out.println("[레벨업] " + player.name + " → Lv." + msg.newLevel +
                                ", HP: " + msg.newCurrentHp + "/" + msg.newMaxHp);
                        }
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

                                    // ===== Fog 시스템 업데이트 (PHASE_24) =====
                                    updateFogSystem(room, 0.05f);

                                    // ===== 플레이어 사망 체크 및 1등 판정 (PHASE_26) =====
                                    if (!room.gameEnded) {
                                        checkPlayerDeathsAndWinner(room);
                                    }
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
