package com.example.yugeup.game.combat;

/**
 * 데미지 계산 클래스
 *
 * 공격력, 방어력, 크리티컬 등을 고려하여 최종 데미지를 계산합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class DamageCalculator {

    /**
     * 데미지를 계산합니다.
     *
     * @param attack 공격력
     * @param defense 방어력
     * @return 실제 데미지
     */
    public static int calculateDamage(int attack, int defense) {
        // TODO: PHASE_22에서 구현
        return 0;
    }

    /**
     * 크리티컬 여부를 판정합니다.
     *
     * @return 크리티컬 여부
     */
    public static boolean isCritical() {
        // TODO: PHASE_22에서 구현
        return false;
    }

    /**
     * 크리티컬 데미지를 계산합니다.
     *
     * @param baseDamage 기본 데미지
     * @return 크리티컬 데미지
     */
    public static int applyCritical(int baseDamage) {
        // TODO: PHASE_22에서 구현
        return 0;
    }

    /**
     * Private 생성자 - 인스턴스 생성 방지
     */
    private DamageCalculator() {
        throw new AssertionError("DamageCalculator 클래스는 인스턴스화할 수 없습니다.");
    }
}
