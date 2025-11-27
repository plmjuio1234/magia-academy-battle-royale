package org.example;

/**
 * 몬스터 스폰 메시지
 *
 * 서버가 새 몬스터 스폰을 클라이언트에 알립니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterSpawnMsg {
    public int monsterId;
    public float x;
    public float y;
    public String monsterType;
    public String elementType;

    public MonsterSpawnMsg() {}
}
