package com.example.yugeup.game.buff;

/**
 * 기절 버프 클래스
 *
 * 땅 원소 스킬 '록 스매시'에서 적용되는 버프입니다.
 * 이동 및 공격이 불가능합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StunBuff extends Buff {

    /**
     * 기절 버프 생성자
     *
     * @param duration 지속시간 (초)
     */
    public StunBuff(float duration) {
        super(BuffType.STUN, duration);
    }

    /**
     * 기절 상태인지 확인합니다.
     *
     * @return 기절 상태 여부
     */
    public boolean isStunned() {
        return isActive;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 기절 상태 해제
    }
}
