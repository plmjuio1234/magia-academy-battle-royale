package com.example.yugeup.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import org.example.Main.*;
import org.example.MonsterSpawnMsg;
import org.example.MonsterUpdateMsg;
import org.example.MonsterDeathMsg;
import org.example.MonsterDamageMsg;
import org.example.PlayerAttackMonsterMsg;
import com.example.yugeup.network.messages.ProjectileFiredMsg;
import com.example.yugeup.network.messages.SkillCastMsg;
import com.example.yugeup.network.messages.FogZoneMsg;
import com.example.yugeup.network.messages.FogDamageMsg;
import com.example.yugeup.network.messages.MonsterAttackPlayerMsg;
import com.example.yugeup.network.messages.PlayerAttackPlayerMsg;
import com.example.yugeup.network.messages.PlayerDeathMsg;
import com.example.yugeup.network.messages.PlayerLevelUpMsg;
import com.example.yugeup.utils.Constants;

import java.io.IOException;

/**
 * 네트워크 관리 클래스
 *
 * KryoNet 클라이언트를 관리하고 서버와의 통신을 담당합니다.
 * 연결, 재연결, 메시지 송수신을 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class NetworkManager {

  public static class SetPlayerNameMsg {
    public String playerName;
  }

  public static class SetPlayerNameResponse {
    public boolean success;
    public String message;
  }

  // 싱글톤 인스턴스
  private static NetworkManager instance;

  // KryoNet 클라이언트
  private Client client;

  // 연결 상태
  private boolean connected;

  // 현재 플레이어 ID
  private int currentPlayerId = -1;

  // 메시지 핸들러
  private MessageHandler messageHandler;

  /**
   * Private 생성자 - 싱글톤 패턴
   */
  private NetworkManager() {
    this.connected = false;
    this.messageHandler = MessageHandler.getInstance();
  }

  /**
   * NetworkManager 싱글톤 인스턴스를 반환합니다.
   *
   * @return NetworkManager 인스턴스
   */
  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
    }
    return instance;
  }

  /**
   * 서버에 연결합니다.
   *
   * @param host    서버 호스트
   * @param tcpPort TCP 포트
   */
  public void connect(String host, int tcpPort) {
    try {
      // Kryo 로그 비활성화 (프로덕션)
      Log.set(Log.LEVEL_NONE);

      // 기존 클라이언트가 있다면 종료
      if (client != null) {
        client.stop();
      }

      // 새 클라이언트 생성 (버퍼 크기: write 16384, read 8192)
      client = new Client(16384, 8192);

      // 메시지 클래스 등록
      registerMessages();

      // 리스너 추가
      client.addListener(new Listener() {
        @Override
        public void connected(Connection connection) {
          NetworkManager.this.connected = true;
          NetworkManager.this.currentPlayerId = connection.getID();
          System.out.println("[NetworkManager] 서버 연결 성공: " + host + ":" + tcpPort);
          System.out.println("[NetworkManager] 플레이어 ID 할당됨: " + NetworkManager.this.currentPlayerId);
        }

        @Override
        public void disconnected(Connection connection) {
          NetworkManager.this.connected = false;
          System.out.println("[NetworkManager] 서버 연결 해제");
        }

        @Override
        public void received(Connection connection, Object object) {
          // 메시지 핸들러로 전달
          messageHandler.handleMessage(object);
        }
      });

      // 클라이언트 시작 (MagicBattleRoyale 방식)
      client.start();

      // 서버 연결 시도 (TCP + UDP)
      client.connect(Constants.CONNECTION_TIMEOUT, host, tcpPort, tcpPort + 1);

    } catch (IOException e) {
      connected = false;
      System.err.println("[NetworkManager] 연결 실패: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * KryoNet에 메시지 클래스들을 등록합니다.
   *
   * 중요: 서버와 클라이언트의 등록 순서가 정확히 일치해야 합니다!
   */
  private void registerMessages() {
    Kryo kryo = client.getKryo();

    // 서버와 동일한 Kryo 설정
    kryo.setRegistrationRequired(false); // 클래스명으로 직렬화
    kryo.setReferences(true);

    // 서버와 동일한 순서로 클래스 등록 (순서가 중요!)
    kryo.register(CreateRoomMsg.class); // ID: 10
    kryo.register(CreateRoomResponse.class); // ID: 11
    kryo.register(GetRoomListMsg.class); // ID: 12
    kryo.register(RoomInfo.class); // ID: 13
    kryo.register(RoomInfo[].class); // ID: 14
    kryo.register(RoomListResponse.class); // ID: 15
    kryo.register(JoinRoomMsg.class); // ID: 16
    kryo.register(JoinRoomResponse.class); // ID: 17
    kryo.register(PlayerInfo.class); // ID: 18
    kryo.register(PlayerInfo[].class); // ID: 19
    kryo.register(LeaveRoomMsg.class); // ID: 20
    kryo.register(RoomUpdateMsg.class); // ID: 21
    kryo.register(StartGameMsg.class); // ID: 22
    kryo.register(GameStartNotification.class); // ID: 23
    kryo.register(ChatMsg.class); // ID: 24
    kryo.register(PlayerMoveMsg.class); // ID: 25
    kryo.register(SkillCastMsg.class); // ID: 26
    kryo.register(MonsterSpawnMsg.class); // ID: 27
    kryo.register(MonsterUpdateMsg.class); // ID: 28
    kryo.register(MonsterDeathMsg.class); // ID: 29
    kryo.register(PlayerAttackMonsterMsg.class); // ID: 30
    kryo.register(MonsterDamageMsg.class); // ID: 31
    kryo.register(ProjectileFiredMsg.class); // ID: 32
    kryo.register(SetPlayerNameMsg.class); // ID: 33
    kryo.register(SetPlayerNameResponse.class); // ID: 34
    kryo.register(FogZoneMsg.class); // ID: 35 (PHASE_24)
    kryo.register(FogDamageMsg.class); // ID: 36 (PHASE_24)
    kryo.register(MonsterAttackPlayerMsg.class); // ID: 37 (PHASE_25)
    kryo.register(PlayerAttackPlayerMsg.class); // ID: 38 (PHASE_25 PVP)
    kryo.register(PlayerDeathMsg.class); // ID: 39 (PHASE_25)
    kryo.register(PlayerLevelUpMsg.class); // ID: 40 (레벨업 HP 동기화)

    System.out.println("[NetworkManager] Kryo 메시지 등록 완료 (서버와 동일한 순서)");
  }

  /**
   * 서버 연결을 해제합니다.
   */
  public void disconnect() {
    if (client != null) {
      client.stop();
      client = null;
      connected = false;
      System.out.println("[NetworkManager] 연결 종료");
    }
  }

  /**
   * TCP 메시지를 전송합니다.
   *
   * @param message 전송할 메시지 객체
   */
  public void sendTCP(Object message) {
    if (connected && client != null) {
      client.sendTCP(message);
      if (Constants.LOG_NETWORK) {
        System.out.println("[NetworkManager] TCP 전송: " + message.getClass().getSimpleName());
      }
    } else {
      System.err.println("[NetworkManager] 연결되지 않음 - 메시지 전송 실패");
    }
  }

  /**
   * UDP 메시지를 전송합니다.
   *
   * @param message 전송할 메시지 객체
   */
  public void sendUDP(Object message) {
    if (connected && client != null) {
      client.sendUDP(message);
      if (Constants.LOG_NETWORK) {
        System.out.println("[NetworkManager] UDP 전송: " + message.getClass().getSimpleName());
      }
    } else {
      System.err.println("[NetworkManager] 연결되지 않음 - 메시지 전송 실패");
    }
  }

  /**
   * 몬스터 공격 메시지를 서버로 전송합니다.
   *
   * @param monsterId 몬스터 ID
   * @param damage    데미지
   * @param attackerX 공격자 X 위치 (검증용)
   * @param attackerY 공격자 Y 위치 (검증용)
   */
  public void sendAttackMessage(int monsterId, int damage, float attackerX, float attackerY) {
    if (!connected || client == null) {
      return;
    }

    org.example.PlayerAttackMonsterMsg msg = new org.example.PlayerAttackMonsterMsg();
    msg.monsterId = monsterId;
    msg.skillDamage = damage;
    msg.attackerX = attackerX;
    msg.attackerY = attackerY;
    // playerId는 서버에서 자동으로 설정됨

    sendTCP(msg);
  }

  /**
   * 스킬 시전 메시지를 서버에 전송합니다.
   *
   * @param skillId 스킬 ID
   * @param targetX 목표 X 좌표
   * @param targetY 목표 Y 좌표
   */
  public void sendSkillCast(int skillId, float targetX, float targetY) {
    if (!connected || client == null) {
      return;
    }

    SkillCastMsg msg = new SkillCastMsg();
    msg.skillId = skillId;
    msg.targetX = targetX;
    msg.targetY = targetY;

    sendTCP(msg);
  }

  /**
   * 스킬 시전 메시지를 서버에 전송합니다 (확장 버전).
   * 스킬의 상세 정보를 포함하여 원격 플레이어에게 정확한 이펙트를 표시합니다.
   *
   * @param skillId            스킬 ID
   * @param targetX            목표 X 좌표
   * @param targetY            목표 Y 좌표
   * @param skillName          스킬 이름
   * @param elementColor       원소 타입 색상 ("불", "물", "바람" 등)
   * @param baseDamage         기본 데미지
   * @param projectileSpeed    투사체 속도
   * @param projectileRadius   투사체 반지름
   * @param projectileLifetime 투사체 수명
   */
  public void sendSkillCast(int skillId, float targetX, float targetY,
      String skillName, String elementColor, int baseDamage,
      float projectileSpeed, float projectileRadius, float projectileLifetime) {
    if (!connected || client == null) {
      return;
    }

    SkillCastMsg msg = new SkillCastMsg();
    msg.skillId = skillId;
    msg.targetX = targetX;
    msg.targetY = targetY;
    msg.skillName = skillName;
    msg.elementColor = elementColor;
    msg.baseDamage = baseDamage;
    msg.projectileSpeed = projectileSpeed;
    msg.projectileRadius = projectileRadius;
    msg.projectileLifetime = projectileLifetime;

    sendTCP(msg);
  }

  /**
   * 스킬 시전 메시지를 서버에 전송합니다 (전체 동기화 버전).
   * 모든 스킬 타입에 대해 정확한 동기화를 지원합니다.
   *
   * @param msg 스킬 시전 메시지 (모든 필드가 설정된 상태)
   */
  public void sendSkillCastFull(SkillCastMsg msg) {
    if (!connected || client == null) {
      return;
    }
    sendTCP(msg);
  }

  /**
   * PVP 공격 메시지를 서버로 전송합니다.
   *
   * @param targetPlayerId 타겟 플레이어 ID
   * @param damage         데미지 (이미 PVP 배율 적용됨)
   * @param skillType      스킬 타입
   */
  public void sendPvpAttack(int targetPlayerId, int damage, String skillType) {
    if (!connected || client == null) {
      return;
    }

    PlayerAttackPlayerMsg msg = new PlayerAttackPlayerMsg(targetPlayerId, damage, skillType);
    sendTCP(msg);
    System.out.println("[NetworkManager] PVP 공격 전송: 타겟=" + targetPlayerId + ", 데미지=" + damage + ", 스킬=" + skillType);
  }

  /**
   * 레벨업 메시지를 서버로 전송합니다.
   * 레벨업 시 HP가 풀회복되므로 서버에 동기화합니다.
   *
   * @param playerId 플레이어 ID
   * @param newLevel 새 레벨
   * @param newMaxHp 새 최대 HP
   * @param newCurrentHp 새 현재 HP
   */
  public void sendLevelUp(int playerId, int newLevel, int newMaxHp, int newCurrentHp) {
    if (!connected || client == null) {
      return;
    }

    com.example.yugeup.network.messages.PlayerLevelUpMsg msg =
        new com.example.yugeup.network.messages.PlayerLevelUpMsg(playerId, newLevel, newMaxHp, newCurrentHp);
    sendTCP(msg);
    System.out.println("[NetworkManager] 레벨업 전송: 레벨=" + newLevel + ", 최대HP=" + newMaxHp + ", 현재HP=" + newCurrentHp);
  }

  /**
   * 네트워크를 업데이트합니다.
   *
   * @param delta 이전 프레임으로부터의 시간 (초)
   */
  public void update(float delta) {
    // KryoNet은 내부적으로 별도 스레드에서 처리하므로
    // 여기서는 특별한 업데이트가 필요 없음
  }

  // Getters
  public boolean isConnected() {
    return connected;
  }

  /**
   * 현재 플레이어 ID를 반환합니다.
   *
   * @return 현재 플레이어 ID (-1이면 아직 할당되지 않음)
   */
  public int getCurrentPlayerId() {
    return currentPlayerId;
  }
}
