package com.example.yugeup.game.monster;

/**
 * 몬스터 타입 열거형
 *
 * 게임에 등장하는 몬스터 종류를 정의합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public enum MonsterType {
    /**
     * 고스트 (유령)
     * - 체력: 중간
     * - 이동속도: 빠름
     * - 경험치: 30
     */
    GHOST(0),

    /**
     * 박쥐
     * - 체력: 낮음
     * - 이동속도: 매우 빠름
     * - 경험치: 20
     */
    BAT(1),

    /**
     * 골렘
     * - 체력: 높음
     * - 이동속도: 느림
     * - 경험치: 50
     */
    GOLEM(2);

    private final int typeId;

    /**
     * 생성자
     *
     * @param typeId 타입 ID (네트워크 전송용)
     */
    MonsterType(int typeId) {
        this.typeId = typeId;
    }

    /**
     * 타입 ID를 반환합니다.
     *
     * @return 타입 ID
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * 타입 ID로부터 MonsterType을 얻습니다.
     *
     * @param typeId 타입 ID
     * @return MonsterType (없으면 null)
     */
    public static MonsterType fromTypeId(int typeId) {
        for (MonsterType type : MonsterType.values()) {
            if (type.typeId == typeId) {
                return type;
            }
        }
        return null;
    }

    /**
     * 문자열 이름으로부터 MonsterType을 얻습니다.
     * 서버로부터 받은 문자열을 변환합니다.
     *
     * @param name 몬스터 이름 ("Ghost", "Slime", "Golem" 등)
     * @return MonsterType (없으면 null)
     */
    public static MonsterType fromString(String name) {
        if (name == null) return null;

        // 대소문자 무시하고 비교
        String upperName = name.toUpperCase();

        // "Slime"은 BAT으로 매핑 (서버 구버전 호환)
        if ("SLIME".equals(upperName)) {
            return BAT;
        }

        // 나머지는 enum 이름으로 매칭
        try {
            return MonsterType.valueOf(upperName);
        } catch (IllegalArgumentException e) {
            System.out.println("[MonsterType] 알 수 없는 몬스터 타입: " + name);
            return null;
        }
    }
}
