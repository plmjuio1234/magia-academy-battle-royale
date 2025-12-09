package com.example.yugeup.network.messages;

/**
 * PVP 공격 메시지
 * 클라이언트 → 서버: 공격 요청
 * 서버 → 클라이언트: 데미지 결과
 */
public class PlayerAttackPlayerMsg {
    public int attackerId;     // 공격자 플레이어 ID
    public int targetId;       // 피격자 플레이어 ID
    public int damage;         // 데미지
    public int newHp;          // 피격자의 새로운 HP
    public int maxHp;          // 피격자의 최대 HP
    public String skillType;   // 사용한 스킬 타입

    // Kryo 직렬화용 기본 생성자
    public PlayerAttackPlayerMsg() {}

    // 클라이언트에서 공격 요청 시 사용
    public PlayerAttackPlayerMsg(int targetId, int damage, String skillType) {
        this.targetId = targetId;
        this.damage = damage;
        this.skillType = skillType;
    }
}
