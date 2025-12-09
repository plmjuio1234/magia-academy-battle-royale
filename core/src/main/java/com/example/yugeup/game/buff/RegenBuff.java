package com.example.yugeup.game.buff;

/**
 * 재생 버프 클래스
 *
 * 땅 원소 스킬 '스톤 실드'에서 적용되는 버프입니다.
 * 시간에 따라 체력을 회복합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class RegenBuff extends Buff {

    // 초당 회복량
    private int hpPerSecond;

    // 누적 재생 시간 (다음 회복까지)
    private float regenAccumulator;

    // 회복 주기 (초)
    private float regenInterval;

    /**
     * 재생 버프 생성자
     *
     * @param duration 지속시간 (초)
     * @param hpPerSecond 초당 회복량
     * @param regenInterval 회복 주기 (초, 기본값: 1.0f)
     */
    public RegenBuff(float duration, int hpPerSecond, float regenInterval) {
        super(BuffType.REGEN, duration);
        this.hpPerSecond = hpPerSecond;
        this.regenInterval = regenInterval;
        this.regenAccumulator = 0f;
    }

    /**
     * 재생 버프 생성자 (기본 회복 주기 1초)
     *
     * @param duration 지속시간 (초)
     * @param hpPerSecond 초당 회복량
     */
    public RegenBuff(float duration, int hpPerSecond) {
        this(duration, hpPerSecond, 1.0f);
    }

    /**
     * 버프를 업데이트합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     * @return 이번 프레임에 회복할 체력 (0이면 회복 시간이 아님)
     */
    public int updateAndGetHealing(float delta) {
        // 기본 업데이트 수행
        super.update(delta);

        if (!isActive) {
            return 0;
        }

        regenAccumulator += delta;
        int healingAmount = 0;

        // 회복 주기가 되면 체력 회복
        while (regenAccumulator >= regenInterval) {
            regenAccumulator -= regenInterval;
            healingAmount += hpPerSecond;
        }

        return healingAmount;
    }

    /**
     * 초당 회복량을 반환합니다.
     *
     * @return 초당 회복량
     */
    public int getHpPerSecond() {
        return hpPerSecond;
    }

    /**
     * 회복 주기를 반환합니다.
     *
     * @return 회복 주기 (초)
     */
    public float getRegenInterval() {
        return regenInterval;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 재생 효과 종료
        regenAccumulator = 0f;
    }
}
