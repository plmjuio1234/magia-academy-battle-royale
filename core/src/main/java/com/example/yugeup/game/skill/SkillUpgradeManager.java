package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;
import com.example.yugeup.utils.Constants;

/**
 * 스킬 업그레이드 관리자
 *
 * 플레이어의 스킬 업그레이드를 관리합니다.
 * 경험치 소비, 스킬 레벨 증가, 능력치 계산을 담당합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillUpgradeManager {
    private Player player;

    // 업그레이드 비용 (레벨별)
    private static final int[] UPGRADE_COSTS = {
        Constants.SKILL_UPGRADE_COST_LEVEL1,  // 레벨 1→2: 50
        Constants.SKILL_UPGRADE_COST_LEVEL2,  // 레벨 2→3: 100
        Constants.SKILL_UPGRADE_COST_LEVEL3   // 레벨 3→MAX: 200
    };

    /**
     * 스킬 업그레이드 매니저 생성자
     *
     * @param player 플레이어 엔티티
     */
    public SkillUpgradeManager(Player player) {
        this.player = player;
    }

    /**
     * 스킬 업그레이드 가능 여부 확인
     *
     * @param skill 업그레이드할 스킬
     * @param upgradeType 업그레이드 타입
     * @return 업그레이드 가능 여부
     */
    public boolean canUpgrade(ElementalSkill skill, ElementalSkill.UpgradeType upgradeType) {
        // 최대 레벨 확인
        if (skill.getSkillLevel() >= Constants.MAX_SKILL_LEVEL) {
            return false;
        }

        // 경험치 확인
        int requiredExp = getUpgradeCost(skill.getSkillLevel());
        int currentExp = player.getLevelSystem().getCurrentExp();

        return currentExp >= requiredExp;
    }

    /**
     * 스킬 업그레이드 수행
     *
     * @param skill 업그레이드할 스킬
     * @param upgradeType 업그레이드 타입
     * @return 성공 여부
     */
    public boolean upgradeSkill(ElementalSkill skill, ElementalSkill.UpgradeType upgradeType) {
        if (!canUpgrade(skill, upgradeType)) {
            return false;
        }

        // 경험치 소비
        int cost = getUpgradeCost(skill.getSkillLevel());

        // LevelSystem에서 직접 경험치를 차감하는 메서드가 없으므로
        // 음수 경험치를 더해서 차감 (향후 consumeExperience 추가 예정)
        player.getLevelSystem().gainExperience(-cost);

        // 스킬 업그레이드 적용
        skill.upgrade(upgradeType);

        System.out.println("[SkillUpgradeManager] " + skill.getName() + " 업그레이드 완료: "
            + upgradeType + " (레벨 " + skill.getSkillLevel() + ")");

        return true;
    }

    /**
     * 업그레이드 비용 계산
     *
     * @param currentLevel 현재 스킬 레벨
     * @return 필요 경험치
     */
    public int getUpgradeCost(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= Constants.MAX_SKILL_LEVEL) {
            return Integer.MAX_VALUE;  // 업그레이드 불가능
        }
        return UPGRADE_COSTS[currentLevel - 1];
    }

    /**
     * 업그레이드 효과 미리보기
     *
     * @param skill 대상 스킬
     * @param upgradeType 업그레이드 타입
     * @return 업그레이드 미리보기 정보
     */
    public UpgradePreview getUpgradePreview(ElementalSkill skill, ElementalSkill.UpgradeType upgradeType) {
        UpgradePreview preview = new UpgradePreview();

        switch (upgradeType) {
            case DAMAGE:
                preview.beforeValue = skill.getDamage();
                preview.afterValue = (int) (skill.getDamage() * (1.0f + Constants.SKILL_UPGRADE_DAMAGE_INCREASE));
                preview.displayText = "데미지: " + preview.beforeValue + " → " + preview.afterValue;
                break;

            case COOLDOWN:
                float originalCooldown = skill.getCooldown();
                float newCooldown = originalCooldown * (1.0f - Constants.SKILL_UPGRADE_COOLDOWN_REDUCTION);
                preview.beforeValue = (int) (originalCooldown * 10);
                preview.afterValue = (int) (newCooldown * 10);
                preview.displayText = "쿨타임: " + String.format("%.1f", originalCooldown) + "초 → "
                    + String.format("%.1f", newCooldown) + "초";
                break;
        }

        return preview;
    }

    /**
     * 업그레이드 미리보기 정보
     */
    public static class UpgradePreview {
        public int beforeValue;
        public int afterValue;
        public String displayText;
    }
}
