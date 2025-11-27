package com.example.yugeup.game.monster;

/**
 * 몬스터 팩토리
 *
 * 몬스터 타입에 따라 적절한 몬스터 인스턴스를 생성합니다.
 * Factory 패턴을 사용하여 몬스터 생성 로직을 캡슐화합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class MonsterFactory {

    /**
     * 몬스터 타입에 따라 몬스터 생성
     *
     * @param type 몬스터 타입
     * @return 생성된 몬스터 인스턴스
     * @throws IllegalArgumentException 알 수 없는 몬스터 타입인 경우
     */
    public static Monster createMonster(MonsterType type) {
        if (type == null) {
            throw new IllegalArgumentException("Monster type cannot be null");
        }

        switch (type) {
            case GHOST:
                return new Ghost();

            case BAT:
                return new Bat();

            case GOLEM:
                return new Golem();

            default:
                throw new IllegalArgumentException("Unknown monster type: " + type);
        }
    }

    /**
     * 몬스터 타입 ID로부터 몬스터 생성
     *
     * @param typeId 몬스터 타입 ID (0=고스트, 1=박쥐, 2=골렘)
     * @return 생성된 몬스터 인스턴스
     * @throws IllegalArgumentException 알 수 없는 타입 ID인 경우
     */
    public static Monster createMonster(int typeId) {
        MonsterType type = MonsterType.fromTypeId(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown monster type ID: " + typeId);
        }
        return createMonster(type);
    }
}
