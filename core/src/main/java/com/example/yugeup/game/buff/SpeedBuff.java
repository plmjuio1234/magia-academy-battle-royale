package com.example.yugeup.game.buff;

/**
 * 가속 버프 클래스
 *
 * 바람 원소 스킬 '폭풍'에서 적용되는 버프입니다.
 * 이동 속도를 증가시킵니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SpeedBuff extends Buff {

    // 속도 증가 배수 (1.5 = 1.5배 빨라짐)
    private float speedMultiplier;

    /**
     * 가속 버프 생성자
     *
     * @param duration 지속시간 (초)
     * @param multiplier 속도 증가 배수 (기본값: 1.5)
     */
    public SpeedBuff(float duration, float multiplier) {
        super(BuffType.SPEED, duration);
        this.speedMultiplier = multiplier;
    }

    /**
     * 속도 증가 배수를 반환합니다.
     *
     * @return 속도 증가 배수
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 속도 증가 효과 종료
        speedMultiplier = 1.0f;
    }
}
