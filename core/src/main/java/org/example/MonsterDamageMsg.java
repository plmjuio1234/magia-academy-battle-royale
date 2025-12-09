package org.example;

/**
 * 몬스터 데미지 메시지
 *
 * 서버가 몬스터 피격을 모든 클라이언트에 브로드캐스트합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterDamageMsg {
    public int monsterId;           // 피해 입은 몬스터
    public int newHp;               // 갱신된 HP
    public int damageAmount;        // 입은 데미지
    public int attackerId;          // 공격자 ID

    public MonsterDamageMsg() {}
}
