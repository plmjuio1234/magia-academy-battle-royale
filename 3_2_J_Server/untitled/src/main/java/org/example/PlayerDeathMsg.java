// 서버 - org.example.PlayerDeathMsg.java
package org.example;

/**
 * 플레이어 사망 메시지
 * 서버에서 모든 클라이언트로 브로드캐스트
 */
public class PlayerDeathMsg {
    public int playerId;       // 사망한 플레이어 ID
    public String playerName;  // 사망한 플레이어 닉네임
    public int killerId;       // 킬러 ID (-1: 몬스터, 0: 자기장/fog, >0: 플레이어)
    public String killerName;  // 킬러 이름 (몬스터명 또는 플레이어명)
    public int rank;           // 사망 순위 (8명 중 몇 번째로 죽었는지)

    // Kryo 직렬화용 기본 생성자
    public PlayerDeathMsg() {}

    public PlayerDeathMsg(int playerId, String playerName, int killerId, String killerName, int rank) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.killerId = killerId;
        this.killerName = killerName;
        this.rank = rank;
    }
}
