package com.example.yugeup.game.buff;

/**
 * 둔화 버프 클래스
 *
 * 번개와 땅 원소 스킬에서 적용되는 버프입니다.
 * 이동 속도를 감소시킵니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SlowBuff extends Buff {

    // 속도 감소 배수 (0.5 = 50%로 느려짐)
    private float speedMultiplier;

    /**
     * 둔화 버프 생성자
     *
     * @param duration 지속시간 (초)
     * @param multiplier 속도 감소 배수 (0~1, 기본값: 0.5)
     */
    public SlowBuff(float duration, float multiplier) {
        super(BuffType.SLOW, duration);
        // 배수 범위 제한 (0 ~ 1)
        this.speedMultiplier = Math.max(0f, Math.min(1f, multiplier));
    }

    /**
     * 속도 감소 배수를 반환합니다.
     *
     * @return 속도 감소 배수
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 둔화 효과 종료
        speedMultiplier = 1.0f;
    }
}
