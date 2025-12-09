package com.example.yugeup.game.buff;

/**
 * 버프 기본 클래스
 *
 * 모든 버프의 기본이 되는 추상 클래스입니다.
 * 지속시간, 적용 효과, 종료 처리를 관리합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public abstract class Buff {

    // 버프 타입
    protected BuffType buffType;

    // 남은 지속시간 (초)
    protected float remainingDuration;

    // 최대 지속시간 (초)
    protected float maxDuration;

    // 버프가 활성화되어 있는지 여부
    protected boolean isActive;

    /**
     * 버프 생성자
     *
     * @param buffType 버프 타입
     * @param duration 지속시간 (초)
     */
    public Buff(BuffType buffType, float duration) {
        this.buffType = buffType;
        this.maxDuration = duration;
        this.remainingDuration = duration;
        this.isActive = true;
    }

    /**
     * 버프를 업데이트합니다.
     *
     * 지속시간을 감소시키고, 시간이 다 되면 비활성화합니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    public void update(float delta) {
        if (!isActive) return;

        remainingDuration -= delta;
        if (remainingDuration <= 0) {
            remainingDuration = 0;
            isActive = false;
            onEnd();  // 버프 종료 처리
        }
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     *
     * 하위 클래스에서 버프 종료 시 정리 작업을 수행할 수 있습니다.
     */
    protected abstract void onEnd();

    /**
     * 버프가 활성화되어 있는지 확인합니다.
     *
     * @return 활성화 상태
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * 버프 타입을 반환합니다.
     *
     * @return 버프 타입
     */
    public BuffType getBuffType() {
        return buffType;
    }

    /**
     * 남은 지속시간을 반환합니다.
     *
     * @return 남은 지속시간 (초)
     */
    public float getRemainingDuration() {
        return remainingDuration;
    }

    /**
     * 최대 지속시간을 반환합니다.
     *
     * @return 최대 지속시간 (초)
     */
    public float getMaxDuration() {
        return maxDuration;
    }

    /**
     * 버프의 진행률을 반환합니다 (0.0 ~ 1.0).
     *
     * UI에서 진행 바 렌더링에 사용됩니다.
     *
     * @return 진행률
     */
    public float getProgress() {
        if (maxDuration == 0) return 0f;
        return remainingDuration / maxDuration;
    }
}
