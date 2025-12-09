package com.example.yugeup.game.skill;

/**
 * 원소 타입
 *
 * 게임 내 5가지 원소를 정의합니다.
 * 각 원소는 고유한 3가지 스킬을 가집니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public enum ElementType {
    FIRE("불", "공격적인 화염 마법", 0xFF4500),
    WATER("물", "방어와 회복의 물 마법", 0x1E90FF),
    WIND("바람", "속도와 기동성의 바람 마법", 0x7FFF00),
    LIGHTNING("번개", "빠르고 강력한 번개 마법", 0xFFFF00),
    EARTH("흙", "방어와 지속 데미지의 대지 마법", 0x8B4513);

    // 원소 이름 (한글)
    private final String displayName;

    // 원소 설명
    private final String description;

    // 원소 대표 색상 (RGB)
    private final int color;

    /**
     * ElementType 생성자
     *
     * @param displayName 표시 이름
     * @param description 설명
     * @param color 색상 (RGB)
     */
    ElementType(String displayName, String description, int color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    /**
     * 해당 원소의 스킬 ID 배열 반환
     *
     * @return 3개의 스킬 ID (스킬 A, B, C)
     */
    public int[] getSkillIds() {
        switch (this) {
            case FIRE:
                return new int[] {5101, 5102, 5103};  // 파이어볼, 플레임 웨이브, 인페르노
            case WATER:
                return new int[] {5201, 5202, 5203};  // 워터 샷, 아이스 스파이크, 플러드
            case WIND:
                return new int[] {5301, 5302, 5303};  // 에어 슬래시, 토네이도, 폭풍
            case LIGHTNING:
                return new int[] {5401, 5402, 5403};  // 라이트닝 볼트, 체인 라이트닝, 썬더 스톰
            case EARTH:
                return new int[] {5001, 5002, 5003};  // 록 스매시, 어스 스파이크, 스톤 실드
            default:
                return new int[] {0, 0, 0};
        }
    }

    /**
     * 해당 원소의 스킬 이름 배열 반환
     *
     * @return 3개의 스킬 이름
     */
    public String[] getSkillNames() {
        switch (this) {
            case FIRE:
                return new String[] {"파이어볼", "플레임 웨이브", "인페르노"};
            case WATER:
                return new String[] {"워터 샷", "아이스 스파이크", "플러드"};
            case WIND:
                return new String[] {"에어 슬래시", "토네이도", "폭풍"};
            case LIGHTNING:
                return new String[] {"라이트닝 볼트", "체인 라이트닝", "썬더 스톰"};
            case EARTH:
                return new String[] {"록 스매시", "어스 스파이크", "스톤 실드"};
            default:
                return new String[] {"", "", ""};
        }
    }

    /**
     * 원소 표시 이름 반환
     *
     * @return 한글 이름
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 원소 설명 반환
     *
     * @return 설명 텍스트
     */
    public String getDescription() {
        return description;
    }

    /**
     * 원소 색상 반환
     *
     * @return RGB 색상값
     */
    public int getColor() {
        return color;
    }
}
