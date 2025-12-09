package org.example;

/**
 * 몬스터 사망 메시지
 *
 * 서버가 몬스터 사망을 클라이언트에 알립니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterDeathMsg {
    public int monsterId;
    public float dropX;
    public float dropY;
    public int killerId;  // 막타친 플레이어 ID

    public MonsterDeathMsg() {}
}
