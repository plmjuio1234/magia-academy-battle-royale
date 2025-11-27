package com.example.yugeup.game.buff;

/**
 * 버프 타입 열거형
 *
 * 게임에서 사용할 수 있는 모든 버프 타입을 정의합니다.
 * 각 원소별 스킬에서 적용되는 버프들을 관리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public enum BuffType {
    // 물 원소 버프
    SHIELD("보호막", "받는 데미지를 흡수합니다"),

    // 바람 원소 버프
    SPEED("가속", "이동 속도가 증가합니다"),
    INVINCIBLE("무적", "모든 피해를 무시합니다"),

    // 번개 원소 버프
    SLOW("둔화", "이동 속도가 감소합니다"),
    ELECTROCUTE("감전", "이동 속도가 감소하고 데미지를 받습니다"),

    // 땅 원소 버프
    STUN("기절", "이동 및 공격이 불가능합니다"),
    DEFENSE("강화", "방어력이 증가합니다"),
    REGEN("재생", "시간에 따라 체력을 회복합니다");

    // 버프 이름
    private final String displayName;

    // 버프 설명
    private final String description;

    BuffType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
