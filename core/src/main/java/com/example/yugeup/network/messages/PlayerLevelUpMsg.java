package com.example.yugeup.network.messages;

/**
 * 플레이어 레벨업 메시지
 *
 * 플레이어가 레벨업하면 서버에 전송하여 HP/능력치를 동기화합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class PlayerLevelUpMsg {
    public int playerId;       // 플레이어 ID
    public int newLevel;       // 새 레벨
    public int newMaxHp;       // 새 최대 HP
    public int newCurrentHp;   // 새 현재 HP (레벨업 시 풀회복)

    public PlayerLevelUpMsg() {}

    public PlayerLevelUpMsg(int playerId, int newLevel, int newMaxHp, int newCurrentHp) {
        this.playerId = playerId;
        this.newLevel = newLevel;
        this.newMaxHp = newMaxHp;
        this.newCurrentHp = newCurrentHp;
    }
}
