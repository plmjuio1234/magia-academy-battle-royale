package com.example.yugeup.game.buff;

/**
 * 무적 버프 클래스
 *
 * 바람 원소 스킬에서 적용되는 버프입니다.
 * 모든 피해를 무시합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class InvincibleBuff extends Buff {

    /**
     * 무적 버프 생성자
     *
     * @param duration 지속시간 (초)
     */
    public InvincibleBuff(float duration) {
        super(BuffType.INVINCIBLE, duration);
    }

    /**
     * 무적 상태인지 확인합니다.
     *
     * @return 무적 상태 여부
     */
    public boolean isInvincible() {
        return isActive;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 무적 상태 종료
    }
}
