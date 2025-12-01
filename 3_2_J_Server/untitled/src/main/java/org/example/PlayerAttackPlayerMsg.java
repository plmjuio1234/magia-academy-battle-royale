// 서버 - org.example.PlayerAttackPlayerMsg.java
package org.example;

/**
 * 플레이어가 다른 플레이어를 공격했을 때 전송하는 메시지
 * 클라이언트에서 서버로: 공격 요청
 * 서버에서 클라이언트로: 데미지 결과
 */
public class PlayerAttackPlayerMsg {
    public int attackerId;     // 공격자 플레이어 ID
    public int targetId;       // 피격자 플레이어 ID
    public int damage;         // 데미지
    public int newHp;          // 피격자의 새로운 HP
    public int maxHp;          // 피격자의 최대 HP
    public String skillType;   // 사용한 스킬 타입 (예: "MagicMissile", "Fire", "Water")

    // Kryo 직렬화용 기본 생성자
    public PlayerAttackPlayerMsg() {}

    public PlayerAttackPlayerMsg(int attackerId, int targetId, int damage, int newHp, int maxHp, String skillType) {
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.damage = damage;
        this.newHp = newHp;
        this.maxHp = maxHp;
        this.skillType = skillType;
    }
}
