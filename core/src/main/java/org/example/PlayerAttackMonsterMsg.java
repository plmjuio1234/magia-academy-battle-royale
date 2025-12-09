package org.example;

/**
 * 플레이어 몬스터 공격 메시지
 *
 * 클라이언트가 몬스터를 공격할 때 서버에 전송합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class PlayerAttackMonsterMsg {
    public int playerId;            // 공격자
    public int monsterId;           // 대상 몬스터
    public float attackerX;         // 공격자 위치 (검증용)
    public float attackerY;
    public float skillDamage;       // 스킬 데미지

    public PlayerAttackMonsterMsg() {}
}
