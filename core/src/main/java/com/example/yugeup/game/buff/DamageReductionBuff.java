package com.example.yugeup.game.buff;

/**
 * 피해 감소 버프 클래스
 *
 * 땅 원소 스킬 '스톤 실드'에서 적용되는 버프입니다.
 * 받는 피해를 일정 비율 감소시킵니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class DamageReductionBuff extends Buff {

    // 피해 감소율 (0.5 = 50% 감소)
    private float damageReduction;

    /**
     * 피해 감소 버프 생성자
     *
     * @param duration 지속시간 (초)
     * @param damageReduction 피해 감소율 (0.0 ~ 1.0)
     */
    public DamageReductionBuff(float duration, float damageReduction) {
        super(BuffType.DAMAGE_REDUCTION, duration);
        this.damageReduction = damageReduction;
    }

    /**
     * 피해 감소율을 반환합니다.
     *
     * @return 피해 감소율 (0.0 ~ 1.0)
     */
    public float getDamageReduction() {
        return damageReduction;
    }

    /**
     * 실제 적용될 데미지 배수를 반환합니다.
     *
     * 예: 50% 감소면 0.5를 반환 (데미지 * 0.5)
     *
     * @return 데미지 배수
     */
    public float getDamageMultiplier() {
        return 1.0f - damageReduction;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 피해 감소 효과 종료
        damageReduction = 0f;
    }
}
