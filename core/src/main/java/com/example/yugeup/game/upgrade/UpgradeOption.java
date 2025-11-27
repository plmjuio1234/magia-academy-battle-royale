package com.example.yugeup.game.upgrade;

import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.ElementType;

/**
 * 업그레이드 옵션 타입
 *
 * 레벨업 시 선택 가능한 모든 업그레이드 종류를 정의합니다.
 * 스킬 강화(데미지, 쿨타임)와 스탯 강화(HP, MP, 공격력 등)로 구분됩니다.
 *
 * @author YuGeup Development Team
 * @version 2.0
 */
public enum UpgradeOption {
    // ===== 스킬 강화 =====
    /** 스킬 A 데미지 증가 */
    SKILL_A_DAMAGE("스킬 A 데미지", "데미지 +5"),

    /** 스킬 A 쿨타임 감소 */
    SKILL_A_COOLDOWN("스킬 A 쿨타임", "쿨타임 -10%"),

    /** 스킬 B 데미지 증가 */
    SKILL_B_DAMAGE("스킬 B 데미지", "데미지 +8"),

    /** 스킬 B 쿨타임 감소 */
    SKILL_B_COOLDOWN("스킬 B 쿨타임", "쿨타임 -10%"),

    /** 스킬 C 데미지 증가 */
    SKILL_C_DAMAGE("스킬 C 데미지", "데미지 +12"),

    /** 스킬 C 쿨타임 감소 */
    SKILL_C_COOLDOWN("스킬 C 쿨타임", "쿨타임 -10%"),

    // ===== 스탯 강화 =====
    /** 최대 체력 증가 */
    STAT_MAX_HP("최대 체력", "+30 HP"),

    /** 최대 마나 증가 */
    STAT_MAX_MP("최대 마나", "+20 MP"),

    /** 마나 재생 증가 */
    STAT_MP_REGEN("마나 재생", "+1 MP/초"),

    /** 공격력 증가 */
    STAT_ATTACK("공격력", "+5 공격력"),

    /** 이동 속도 증가 */
    STAT_SPEED("이동 속도", "+5% 속도");

    // 표시 이름
    private final String displayName;

    // 효과 설명
    private final String effectDescription;

    /**
     * UpgradeOption 생성자
     *
     * @param displayName 표시 이름
     * @param effectDescription 효과 설명
     */
    UpgradeOption(String displayName, String effectDescription) {
        this.displayName = displayName;
        this.effectDescription = effectDescription;
    }

    /**
     * 표시 이름 반환 (기본값)
     *
     * @return 표시 이름
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 실제 스킬명을 반영한 표시 이름 반환
     *
     * 플레이어가 선택한 원소의 스킬명을 사용합니다.
     *
     * @param player 플레이어
     * @return 실제 스킬명이 포함된 표시 이름
     */
    public String getDisplayName(Player player) {
        // 스킬 업그레이드인 경우
        if (isSkillUpgrade() && player != null) {
            ElementType element = player.getSelectedElement();
            if (element != null) {
                int slot = getSkillSlot();
                String[] skillNames = element.getSkillNames();

                if (slot >= 0 && slot < skillNames.length) {
                    String skillName = skillNames[slot];

                    // 데미지 업그레이드인지 쿨타임 업그레이드인지 구분
                    if (isDamageUpgrade()) {
                        return skillName + " 데미지";
                    } else if (isCooldownUpgrade()) {
                        return skillName + " 쿨타임";
                    }
                }
            }
        }

        // 스탯 업그레이드 또는 원소 미선택 시 기본 이름 사용
        return displayName;
    }

    /**
     * 효과 설명 반환
     *
     * @return 효과 설명
     */
    public String getEffectDescription() {
        return effectDescription;
    }

    /**
     * 스킬 업그레이드인지 확인
     *
     * @return 스킬 업그레이드 여부
     */
    public boolean isSkillUpgrade() {
        return this.name().startsWith("SKILL_");
    }

    /**
     * 스탯 업그레이드인지 확인
     *
     * @return 스탯 업그레이드 여부
     */
    public boolean isStatUpgrade() {
        return this.name().startsWith("STAT_");
    }

    /**
     * 어떤 스킬 슬롯에 해당하는지 반환 (0=A, 1=B, 2=C)
     *
     * @return 스킬 슬롯 인덱스 (-1이면 스킬 업그레이드 아님)
     */
    public int getSkillSlot() {
        if (!isSkillUpgrade()) return -1;

        if (this.name().contains("_A_")) return 0;
        if (this.name().contains("_B_")) return 1;
        if (this.name().contains("_C_")) return 2;
        return -1;
    }

    /**
     * 데미지 업그레이드인지 확인
     *
     * @return 데미지 업그레이드 여부
     */
    public boolean isDamageUpgrade() {
        return this.name().endsWith("_DAMAGE");
    }

    /**
     * 쿨타임 업그레이드인지 확인
     *
     * @return 쿨타임 업그레이드 여부
     */
    public boolean isCooldownUpgrade() {
        return this.name().endsWith("_COOLDOWN");
    }
}
