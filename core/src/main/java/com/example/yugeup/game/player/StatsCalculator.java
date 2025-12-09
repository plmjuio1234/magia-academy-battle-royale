package com.example.yugeup.game.player;

/**
 * 능력치 계산 유틸리티
 *
 * 복잡한 능력치 계산을 담당합니다.
 * 레벨에 따른 능력치, 데미지 계산 등을 처리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StatsCalculator {

    /**
     * 레벨에 따른 최대 체력 계산
     *
     * @param level 레벨
     * @return 최대 체력
     */
    public static int calculateMaxHealth(int level) {
        return 100 + (level - 1) * 20;
    }

    /**
     * 레벨에 따른 최대 마나 계산
     *
     * @param level 레벨
     * @return 최대 마나
     */
    public static int calculateMaxMana(int level) {
        return 50 + (level - 1) * 10;
    }

    /**
     * 레벨에 따른 공격력 계산
     *
     * @param level 레벨
     * @return 공격력
     */
    public static int calculateAttackPower(int level) {
        return 10 + (level - 1) * 5;
    }

    /**
     * 레벨에 따른 방어력 계산
     *
     * @param level 레벨
     * @return 방어력
     */
    public static int calculateDefense(int level) {
        return 5 + (level - 1) * 2;
    }

    /**
     * 레벨에 따른 이동속도 계산
     *
     * @param level 레벨
     * @return 이동속도 (픽셀/초)
     */
    public static float calculateSpeed(int level) {
        return 300f + (level - 1) * 10f;
    }

    /**
     * 데미지 감소 계산
     *
     * @param defense 방어력
     * @return 데미지 감소량
     */
    public static int calculateDamageReduction(int defense) {
        return defense * 2;
    }

    /**
     * 크리티컬 데미지 계산 (향후 구현)
     *
     * @param baseDamage 기본 데미지
     * @param critMultiplier 크리티컬 배율
     * @return 크리티컬 데미지
     */
    public static int calculateCriticalDamage(int baseDamage, float critMultiplier) {
        return (int)(baseDamage * critMultiplier);
    }

    /**
     * 경험치로부터 레벨 계산 (PHASE_11에서 사용)
     *
     * @param exp 경험치
     * @return 레벨
     */
    public static int calculateLevelFromExp(int exp) {
        // 레벨 = sqrt(exp / 100) + 1
        return (int)(Math.sqrt(exp / 100.0)) + 1;
    }

    /**
     * 레벨업에 필요한 경험치 계산
     *
     * @param level 목표 레벨
     * @return 필요 경험치
     */
    public static int calculateExpForLevel(int level) {
        // 레벨 2: 100 exp
        // 레벨 3: 400 exp
        // 레벨 4: 900 exp
        // 공식: (level - 1)^2 * 100
        return (level - 1) * (level - 1) * 100;
    }
}
