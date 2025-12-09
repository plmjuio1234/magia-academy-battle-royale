package com.example.yugeup.game.buff;

/**
 * 보호막 버프 클래스
 *
 * 물 원소 스킬 '워터 샷'에서 적용되는 버프입니다.
 * 받는 데미지를 흡수하는 보호막을 생성합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ShieldBuff extends Buff {

    // 흡수할 수 있는 남은 데미지량
    private int remainingShield;

    // 보호막 최대 용량
    private int maxShield;

    /**
     * 보호막 버프 생성자
     *
     * @param duration 지속시간 (초)
     * @param shieldAmount 보호막 용량
     */
    public ShieldBuff(float duration, int shieldAmount) {
        super(BuffType.SHIELD, duration);
        this.maxShield = shieldAmount;
        this.remainingShield = shieldAmount;
    }

    /**
     * 보호막으로 데미지를 흡수합니다.
     *
     * @param damage 받을 데미지
     * @return 보호막으로 흡수된 실제 데미지
     */
    public int absorbDamage(int damage) {
        if (remainingShield <= 0) {
            return 0;  // 보호막이 없으면 데미지 흡수 안 함
        }

        if (damage <= remainingShield) {
            // 모든 데미지를 흡수
            remainingShield -= damage;
            return damage;
        } else {
            // 일부만 흡수
            int absorbed = remainingShield;
            remainingShield = 0;
            return absorbed;
        }
    }

    /**
     * 남은 보호막 용량을 반환합니다.
     *
     * @return 남은 보호막 용량
     */
    public int getRemainingShield() {
        return remainingShield;
    }

    /**
     * 최대 보호막 용량을 반환합니다.
     *
     * @return 최대 보호막 용량
     */
    public int getMaxShield() {
        return maxShield;
    }

    /**
     * 보호막의 진행률을 반환합니다 (0.0 ~ 1.0).
     *
     * @return 보호막 남은 양의 비율
     */
    public float getShieldProgress() {
        if (maxShield == 0) return 0f;
        return (float) remainingShield / maxShield;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 보호막이 사라짐
        remainingShield = 0;
    }
}
