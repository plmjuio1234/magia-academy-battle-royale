package org.example;

/**
 * 서버에서 클라이언트로 전송하는 데미지 결과 (TCP)
 * 몬스터가 데미지를 입었을 때 모든 클라이언트에게 브로드캐스트
 */
public class MonsterDamageMsg {
    public int monsterId;           // 피해 입은 몬스터
    public int newHp;               // 갱신된 HP
    public int damageAmount;        // 입은 데미지
    public int attackerId;          // 공격자 ID

    public MonsterDamageMsg() {}
}
