package org.example;

/**
 * 클라이언트에서 몬스터 공격 요청 (TCP)
 * 플레이어가 몬스터를 공격할 때 서버로 전송
 */
public class PlayerAttackMonsterMsg {
    public int playerId;            // 공격자
    public int monsterId;           // 대상 몬스터
    public float attackerX, attackerY;  // 공격자 위치 (검증용)
    public float skillDamage;       // 스킬 데미지

    public PlayerAttackMonsterMsg() {}
}
