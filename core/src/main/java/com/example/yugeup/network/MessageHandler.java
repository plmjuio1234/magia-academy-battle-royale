package com.example.yugeup.network;

import org.example.Main.*;
import org.example.MonsterSpawnMsg;
import org.example.MonsterUpdateMsg;
import org.example.MonsterDeathMsg;
import org.example.MonsterDamageMsg;
import org.example.PlayerAttackMonsterMsg;
import com.example.yugeup.network.messages.FogZoneMsg;
import com.example.yugeup.network.messages.FogDamageMsg;
import com.example.yugeup.network.messages.MonsterAttackPlayerMsg;
import com.example.yugeup.network.messages.PlayerAttackPlayerMsg;
import com.example.yugeup.network.messages.PlayerDeathMsg;
import com.example.yugeup.network.messages.ProjectileFiredMsg;
import com.example.yugeup.network.messages.SkillCastMsg;
import com.example.yugeup.utils.Constants;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandler {
    private static MessageHandler instance;
    private ConcurrentLinkedQueue<RoomListResponse> roomListQueue;
    private ConcurrentLinkedQueue<JoinRoomResponse> joinRoomQueue;
    private ConcurrentLinkedQueue<CreateRoomResponse> createRoomQueue;
    private ConcurrentLinkedQueue<RoomUpdateMsg> roomUpdateQueue;
    private ConcurrentLinkedQueue<GameStartNotification> gameStartQueue;
    private ConcurrentLinkedQueue<ChatMsg> chatQueue;

    // 몬스터 관련 메시지 큐 (PHASE_07에서 사용)
    private ConcurrentLinkedQueue<MonsterSpawnMsg> monsterSpawnQueue;
    private ConcurrentLinkedQueue<MonsterUpdateMsg> monsterUpdateQueue;
    private ConcurrentLinkedQueue<MonsterDeathMsg> monsterDeathQueue;
    private ConcurrentLinkedQueue<MonsterDamageMsg> monsterDamageQueue;

    // 플레이어 동기화 메시지 큐 (PHASE_23에서 사용)
    private ConcurrentLinkedQueue<PlayerMoveMsg> playerMoveQueue;

    // 발사체 메시지 큐
    private ConcurrentLinkedQueue<ProjectileFiredMsg> projectileFiredQueue;

    // 스킬 시전 메시지 큐
    private ConcurrentLinkedQueue<SkillCastMsg> skillCastQueue;

    // Fog 시스템 메시지 큐 (PHASE_24)
    private ConcurrentLinkedQueue<FogZoneMsg> fogZoneQueue;
    private ConcurrentLinkedQueue<FogDamageMsg> fogDamageQueue;

    // 전투 시스템 메시지 큐 (PHASE_25)
    private ConcurrentLinkedQueue<MonsterAttackPlayerMsg> monsterAttackPlayerQueue;
    private ConcurrentLinkedQueue<PlayerAttackPlayerMsg> playerAttackPlayerQueue;
    private ConcurrentLinkedQueue<PlayerDeathMsg> playerDeathQueue;

    private MessageHandler() {
        this.roomListQueue = new ConcurrentLinkedQueue<>();
        this.joinRoomQueue = new ConcurrentLinkedQueue<>();
        this.createRoomQueue = new ConcurrentLinkedQueue<>();
        this.roomUpdateQueue = new ConcurrentLinkedQueue<>();
        this.gameStartQueue = new ConcurrentLinkedQueue<>();
        this.chatQueue = new ConcurrentLinkedQueue<>();

        // 몬스터 메시지 큐 초기화
        this.monsterSpawnQueue = new ConcurrentLinkedQueue<>();
        this.monsterUpdateQueue = new ConcurrentLinkedQueue<>();
        this.monsterDeathQueue = new ConcurrentLinkedQueue<>();
        this.monsterDamageQueue = new ConcurrentLinkedQueue<>();

        // 플레이어 동기화 메시지 큐 초기화
        this.playerMoveQueue = new ConcurrentLinkedQueue<>();

        // 발사체 메시지 큐 초기화
        this.projectileFiredQueue = new ConcurrentLinkedQueue<>();

        // 스킬 시전 메시지 큐 초기화
        this.skillCastQueue = new ConcurrentLinkedQueue<>();

        // Fog 시스템 메시지 큐 초기화 (PHASE_24)
        this.fogZoneQueue = new ConcurrentLinkedQueue<>();
        this.fogDamageQueue = new ConcurrentLinkedQueue<>();

        // 전투 시스템 메시지 큐 초기화 (PHASE_25)
        this.monsterAttackPlayerQueue = new ConcurrentLinkedQueue<>();
        this.playerAttackPlayerQueue = new ConcurrentLinkedQueue<>();
        this.playerDeathQueue = new ConcurrentLinkedQueue<>();
    }

    public static synchronized MessageHandler getInstance() {
        if (instance == null) {
            instance = new MessageHandler();
        }
        return instance;
    }

    public void handleMessage(Object message) {
        if (message == null) return;

        if (message instanceof RoomListResponse) {
            roomListQueue.offer((RoomListResponse) message);
        } else if (message instanceof CreateRoomResponse) {
            createRoomQueue.offer((CreateRoomResponse) message);
        } else if (message instanceof JoinRoomResponse) {
            joinRoomQueue.offer((JoinRoomResponse) message);
        } else if (message instanceof RoomUpdateMsg) {
            roomUpdateQueue.offer((RoomUpdateMsg) message);
        } else if (message instanceof GameStartNotification) {
            gameStartQueue.offer((GameStartNotification) message);
        } else if (message instanceof ChatMsg) {
            chatQueue.offer((ChatMsg) message);
            if (Constants.LOG_NETWORK) {
                System.out.println("[MessageHandler] ChatMsg 수신");
            }
        }
        // 몬스터 관련 메시지 처리 (PHASE_07에서 GameScreen이 사용)
        else if (message instanceof MonsterSpawnMsg) {
            monsterSpawnQueue.offer((MonsterSpawnMsg) message);
            if (Constants.LOG_NETWORK) {
                System.out.println("[MessageHandler] MonsterSpawnMsg 수신");
            }
        } else if (message instanceof MonsterUpdateMsg) {
            monsterUpdateQueue.offer((MonsterUpdateMsg) message);
        } else if (message instanceof MonsterDeathMsg) {
            monsterDeathQueue.offer((MonsterDeathMsg) message);
            if (Constants.LOG_NETWORK) {
                System.out.println("[MessageHandler] MonsterDeathMsg 수신");
            }
        } else if (message instanceof MonsterDamageMsg) {
            monsterDamageQueue.offer((MonsterDamageMsg) message);
            if (Constants.LOG_NETWORK) {
                System.out.println("[MessageHandler] MonsterDamageMsg 수신");
            }
        }
        // 플레이어 이동 메시지 처리 (PHASE_23에서 사용)
        else if (message instanceof PlayerMoveMsg) {
            playerMoveQueue.offer((PlayerMoveMsg) message);
        }
        // 발사체 메시지 처리
        else if (message instanceof ProjectileFiredMsg) {
            projectileFiredQueue.offer((ProjectileFiredMsg) message);
            if (Constants.LOG_NETWORK) {
                System.out.println("[MessageHandler] ProjectileFiredMsg 수신");
            }
        }
        // 스킬 시전 메시지 처리
        else if (message instanceof SkillCastMsg) {
            skillCastQueue.offer((SkillCastMsg) message);
            if (Constants.LOG_NETWORK) {
                System.out.println("[MessageHandler] SkillCastMsg 수신");
            }
        }
        // Fog 시스템 메시지 처리 (PHASE_24)
        else if (message instanceof FogZoneMsg) {
            fogZoneQueue.offer((FogZoneMsg) message);
            System.out.println("[MessageHandler] ★ FogZoneMsg 수신: " + ((FogZoneMsg) message).zoneName);
        }
        else if (message instanceof FogDamageMsg) {
            FogDamageMsg fogDmg = (FogDamageMsg) message;
            fogDamageQueue.offer(fogDmg);
            System.out.println("[MessageHandler] ★★★ FogDamageMsg 수신: playerId=" + fogDmg.playerId +
                ", damage=" + fogDmg.damage + ", newHp=" + fogDmg.newHp + ", zone=" + fogDmg.zoneName);
        }
        // 전투 시스템 메시지 처리 (PHASE_25)
        else if (message instanceof MonsterAttackPlayerMsg) {
            monsterAttackPlayerQueue.offer((MonsterAttackPlayerMsg) message);
            System.out.println("[MessageHandler] MonsterAttackPlayerMsg 수신");
        }
        else if (message instanceof PlayerAttackPlayerMsg) {
            playerAttackPlayerQueue.offer((PlayerAttackPlayerMsg) message);
            System.out.println("[MessageHandler] PlayerAttackPlayerMsg 수신 (PVP)");
        }
        else if (message instanceof PlayerDeathMsg) {
            playerDeathQueue.offer((PlayerDeathMsg) message);
            System.out.println("[MessageHandler] PlayerDeathMsg 수신");
        }
    }

    public RoomListResponse pollRoomListResponse() {
        return roomListQueue.poll();
    }

    public JoinRoomResponse pollJoinRoomResponse() {
        return joinRoomQueue.poll();
    }

    public CreateRoomResponse pollCreateRoomResponse() {
        return createRoomQueue.poll();
    }

    public RoomUpdateMsg pollRoomUpdateMsg() {
        return roomUpdateQueue.poll();
    }

    public GameStartNotification pollGameStartNotification() {
        return gameStartQueue.poll();
    }

    public ChatMsg pollChatMsg() {
        return chatQueue.poll();
    }

    // ===== 몬스터 메시지 Getter (PHASE_07에서 사용) =====

    public MonsterSpawnMsg pollMonsterSpawnMsg() {
        return monsterSpawnQueue.poll();
    }

    public MonsterUpdateMsg pollMonsterUpdateMsg() {
        return monsterUpdateQueue.poll();
    }

    public MonsterDeathMsg pollMonsterDeathMsg() {
        return monsterDeathQueue.poll();
    }

    public MonsterDamageMsg pollMonsterDamageMsg() {
        return monsterDamageQueue.poll();
    }

    // ===== 플레이어 동기화 메시지 Getter (PHASE_23에서 사용) =====

    public PlayerMoveMsg pollPlayerMoveMsg() {
        return playerMoveQueue.poll();
    }

    // ===== 발사체 메시지 Getter =====

    public ProjectileFiredMsg pollProjectileFiredMsg() {
        return projectileFiredQueue.poll();
    }

    // ===== 스킬 시전 메시지 Getter =====

    public SkillCastMsg pollSkillCastMsg() {
        return skillCastQueue.poll();
    }

    // ===== Fog 시스템 메시지 Getter (PHASE_24) =====

    public FogZoneMsg pollFogZoneMsg() {
        return fogZoneQueue.poll();
    }

    public FogDamageMsg pollFogDamageMsg() {
        return fogDamageQueue.poll();
    }

    // ===== 전투 시스템 메시지 Getter (PHASE_25) =====

    public MonsterAttackPlayerMsg pollMonsterAttackPlayerMsg() {
        return monsterAttackPlayerQueue.poll();
    }

    public PlayerAttackPlayerMsg pollPlayerAttackPlayerMsg() {
        return playerAttackPlayerQueue.poll();
    }

    public PlayerDeathMsg pollPlayerDeathMsg() {
        return playerDeathQueue.poll();
    }
}
