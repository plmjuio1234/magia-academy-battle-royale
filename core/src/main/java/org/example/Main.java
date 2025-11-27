package org.example;

/**
 * 서버 메시지 클래스 미러
 *
 * 서버의 org.example.Main 내부 클래스를 그대로 복제합니다.
 * 필드 순서와 타입이 서버와 정확히 일치해야 합니다.
 */
public class Main {

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

        public String skillName;
        public String elementColor;
        public int baseDamage;
        public float projectileSpeed;
        public float projectileRadius;
        public float projectileLifetime;

        public SkillCastMsg() {}
    }

    // ===== 몬스터 관련 메시지 (ServerMonsterManager에서 복사) =====

    /**
     * 몬스터 스폰 메시지
     * 서버 → 클라이언트 (TCP)
     */
    public static class MonsterSpawnMsg {
        public int monsterId;
        public float x, y;
        public String monsterType;
        public String elementType;

        public MonsterSpawnMsg() {}
    }

    /**
     * 몬스터 업데이트 메시지
     * 서버 → 클라이언트 (UDP 또는 TCP)
     */
    public static class MonsterUpdateMsg {
        public int monsterId;
        public float x, y;
        public float vx, vy;
        public int hp;
        public int maxHp;
        public String state;

        public MonsterUpdateMsg() {}
    }

    /**
     * 몬스터 사망 메시지
     * 서버 → 클라이언트 (TCP)
     */
    public static class MonsterDeathMsg {
        public int monsterId;
        public float dropX, dropY;

        public MonsterDeathMsg() {}
    }

    /**
     * 플레이어가 몬스터 공격
     * 클라이언트 → 서버 (TCP)
     */
    public static class PlayerAttackMonsterMsg {
        public int playerId;
        public int monsterId;
        public float attackerX, attackerY;
        public float skillDamage;

        public PlayerAttackMonsterMsg() {}
    }

    /**
     * 몬스터 데미지 결과
     * 서버 → 클라이언트 (TCP)
     */
    public static class MonsterDamageMsg {
        public int monsterId;
        public int newHp;
        public int damageAmount;
        public int attackerId;

        public MonsterDamageMsg() {}
    }
}
