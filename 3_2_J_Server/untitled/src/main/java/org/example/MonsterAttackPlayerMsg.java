// 서버 - org.example.MonsterAttackPlayerMsg.java
package org.example;

/**
 * 몬스터가 플레이어를 공격했을 때 전송하는 메시지
 * 서버에서 클라이언트로 전송
 */
public class MonsterAttackPlayerMsg {
    public int monsterId;      // 공격한 몬스터 ID
    public int playerId;       // 피격당한 플레이어 ID
    public int damage;         // 데미지
    public int newHp;          // 플레이어의 새로운 HP
    public int maxHp;          // 플레이어의 최대 HP

    // Kryo 직렬화용 기본 생성자
    public MonsterAttackPlayerMsg() {}

    public MonsterAttackPlayerMsg(int monsterId, int playerId, int damage, int newHp, int maxHp) {
        this.monsterId = monsterId;
        this.playerId = playerId;
        this.damage = damage;
        this.newHp = newHp;
        this.maxHp = maxHp;
    }
}
