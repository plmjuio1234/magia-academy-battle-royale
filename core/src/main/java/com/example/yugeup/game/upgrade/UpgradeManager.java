package com.example.yugeup.game.upgrade;

import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.player.PlayerStats;
import com.example.yugeup.game.skill.ElementSkillSet;
import com.example.yugeup.game.skill.ElementalSkill;
import com.example.yugeup.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 통합 업그레이드 관리자
 *
 * 스킬 강화와 스탯 강화를 모두 관리합니다.
 * 각 업그레이드의 현재 레벨을 추적하고, 무작위 선택지를 생성합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class UpgradeManager {
    private Player player;
    private PlayerStats stats;
    private Random random;

    // 각 업그레이드 옵션의 현재 레벨 (최대 5)
    private Map<UpgradeOption, Integer> upgradeLevels;

    // 마나 재생 누적값
    private float manaRegenBonus = 0f;

    /**
     * 업그레이드 관리자 생성자
     *
     * @param player 플레이어 엔티티
     */
    public UpgradeManager(Player player) {
        this.player = player;
        this.stats = player.getStats();
        this.random = new Random();
        this.upgradeLevels = new HashMap<>();

        // 모든 업그레이드 레벨 초기화
        for (UpgradeOption option : UpgradeOption.values()) {
            upgradeLevels.put(option, 0);
        }
    }

    /**
     * 업그레이드 적용
     *
     * @param option 선택한 업그레이드 옵션
     * @return 성공 여부
     */
    public boolean applyUpgrade(UpgradeOption option) {
        // 실제 스킬명 사용
        String displayName = getUpgradeDisplayName(option);

        // 이미 최대 레벨인지 확인
        if (getUpgradeLevel(option) >= Constants.MAX_UPGRADE_LEVEL) {
            System.out.println("[UpgradeManager] " + displayName + " 이미 최대 레벨");
            return false;
        }

        // 레벨 증가
        int currentLevel = upgradeLevels.get(option);
        upgradeLevels.put(option, currentLevel + 1);

        // 업그레이드 타입별로 실제 효과 적용
        if (option.isSkillUpgrade()) {
            applySkillUpgrade(option);
        } else if (option.isStatUpgrade()) {
            applyStatUpgrade(option);
        }

        System.out.println("[UpgradeManager] " + displayName + " 레벨 " + (currentLevel + 1) + " 적용 완료");
        return true;
    }

    /**
     * 스킬 업그레이드 적용
     *
     * @param option 스킬 업그레이드 옵션
     */
    private void applySkillUpgrade(UpgradeOption option) {
        ElementSkillSet skillSet = player.getElementSkillSet();
        if (skillSet == null) {
            System.out.println("[UpgradeManager] 원소 스킬 세트가 없음");
            return;
        }

        int slotIndex = option.getSkillSlot();
        if (slotIndex < 0 || slotIndex > 2) {
            System.out.println("[UpgradeManager] 잘못된 스킬 슬롯: " + slotIndex);
            return;
        }

        ElementalSkill skill = skillSet.getSkill(slotIndex);
        if (skill == null) {
            System.out.println("[UpgradeManager] 스킬이 없음: 슬롯 " + slotIndex);
            return;
        }

        if (option.isDamageUpgrade()) {
            // 데미지 증가
            int damageBonus = Constants.SKILL_DAMAGE_UPGRADE_BONUS;
            skill.addDamageBonus(damageBonus);
            System.out.println("  → " + skill.getName() + " 데미지 +" + damageBonus);
        } else if (option.isCooldownUpgrade()) {
            // 쿨타임 감소 (10%)
            float cooldownReduction = Constants.SKILL_COOLDOWN_UPGRADE_REDUCTION;
            skill.addCooldownReduction(cooldownReduction);
            System.out.println("  → " + skill.getName() + " 쿨타임 -" + (int)(cooldownReduction * 100) + "%");
        }
    }

    /**
     * 스탯 업그레이드 적용
     *
     * @param option 스탯 업그레이드 옵션
     */
    private void applyStatUpgrade(UpgradeOption option) {
        switch (option) {
            case STAT_MAX_HP:
                // 최대 체력 증가
                int currentMaxHp = stats.getMaxHealth();
                int newMaxHp = currentMaxHp + Constants.STAT_HP_UPGRADE_BONUS;
                stats.setMaxHealth(newMaxHp);
                stats.setCurrentHealth(newMaxHp);  // 현재 체력도 회복
                System.out.println("  → 최대 체력: " + currentMaxHp + " → " + newMaxHp);
                break;

            case STAT_MAX_MP:
                // 최대 마나 증가
                int currentMaxMp = stats.getMaxMana();
                int newMaxMp = currentMaxMp + Constants.STAT_MP_UPGRADE_BONUS;
                stats.setMaxMana(newMaxMp);
                stats.setCurrentMana(newMaxMp);  // 현재 마나도 회복
                System.out.println("  → 최대 마나: " + currentMaxMp + " → " + newMaxMp);
                break;

            case STAT_MP_REGEN:
                // 마나 재생 증가
                manaRegenBonus += Constants.STAT_MP_REGEN_UPGRADE_BONUS;
                System.out.println("  → 마나 재생: +" + Constants.STAT_MP_REGEN_UPGRADE_BONUS + " MP/초");
                break;

            case STAT_ATTACK:
                // 공격력 증가
                int currentAtk = stats.getAttackPower();
                int newAtk = currentAtk + Constants.STAT_ATTACK_UPGRADE_BONUS;
                stats.setAttackPower(newAtk);
                System.out.println("  → 공격력: " + currentAtk + " → " + newAtk);
                break;

            case STAT_SPEED:
                // 이동 속도 증가 (5%)
                float currentSpeed = stats.getSpeed();
                float newSpeed = currentSpeed * (1.0f + Constants.STAT_SPEED_UPGRADE_MULTIPLIER);
                stats.setSpeed(newSpeed);
                System.out.println("  → 이동 속도: " + (int)currentSpeed + " → " + (int)newSpeed);
                break;

            default:
                System.out.println("[UpgradeManager] 알 수 없는 스탯 업그레이드: " + option);
                break;
        }
    }

    /**
     * 무작위 업그레이드 3개 선택
     *
     * 최대 레벨(5레벨)이 아닌 업그레이드 중에서 3개를 무작위로 선택합니다.
     *
     * @return 선택된 업그레이드 옵션 리스트 (3개)
     */
    public List<UpgradeOption> generateRandomUpgrades() {
        List<UpgradeOption> availableOptions = new ArrayList<>();

        // 최대 레벨이 아닌 업그레이드만 수집
        for (UpgradeOption option : UpgradeOption.values()) {
            if (getUpgradeLevel(option) < Constants.MAX_UPGRADE_LEVEL) {
                availableOptions.add(option);
            }
        }

        // 사용 가능한 옵션이 3개 이하면 모두 반환
        if (availableOptions.size() <= 3) {
            System.out.println("[UpgradeManager] 사용 가능한 업그레이드: " + availableOptions.size() + "개");
            return availableOptions;
        }

        // 무작위로 3개 선택
        List<UpgradeOption> selectedOptions = new ArrayList<>();
        List<UpgradeOption> tempList = new ArrayList<>(availableOptions);

        for (int i = 0; i < 3; i++) {
            int randomIndex = random.nextInt(tempList.size());
            selectedOptions.add(tempList.remove(randomIndex));
        }

        System.out.println("[UpgradeManager] 무작위 업그레이드 3개 선택 완료");
        for (UpgradeOption option : selectedOptions) {
            System.out.println("  - " + option.getDisplayName() + " (레벨 " + getUpgradeLevel(option) + ")");
        }

        return selectedOptions;
    }

    /**
     * 특정 업그레이드의 현재 레벨 반환
     *
     * @param option 업그레이드 옵션
     * @return 현재 레벨 (0~5)
     */
    public int getUpgradeLevel(UpgradeOption option) {
        return upgradeLevels.getOrDefault(option, 0);
    }

    /**
     * 마나 재생 보너스 반환
     *
     * @return 마나 재생 보너스 (MP/초)
     */
    public float getManaRegenBonus() {
        return manaRegenBonus;
    }

    /**
     * 업그레이드 미리보기 텍스트 생성
     *
     * @param option 업그레이드 옵션
     * @return 미리보기 텍스트
     */
    public String getUpgradePreviewText(UpgradeOption option) {
        int currentLevel = getUpgradeLevel(option);
        if (currentLevel >= Constants.MAX_UPGRADE_LEVEL) {
            return "최대 레벨";
        }

        return option.getEffectDescription();
    }

    /**
     * 플레이어 정보를 포함한 업그레이드 표시 이름 반환
     *
     * @param option 업그레이드 옵션
     * @return 실제 스킬명이 포함된 표시 이름
     */
    public String getUpgradeDisplayName(UpgradeOption option) {
        return option.getDisplayName(player);
    }
}
