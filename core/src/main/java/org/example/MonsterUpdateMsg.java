package org.example;

/**
 * 몬스터 업데이트 메시지
 *
 * 서버가 몬스터 상태를 클라이언트에 동기화합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterUpdateMsg {
    public int monsterId;
    public float x;
    public float y;
    public float vx;
    public float vy;
    public int hp;
    public int maxHp;
    public String state;

    public MonsterUpdateMsg() {}
}
