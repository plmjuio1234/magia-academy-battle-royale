package com.example.yugeup.game.buff;

/**
 * 강화 버프 클래스
 *
 * 땅 원소 스킬 '스톤 실드'에서 적용되는 버프입니다.
 * 방어력을 증가시킵니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class DefenseBuff extends Buff {

    // 증가할 방어력 값
    private int defenseBonus;

    /**
     * 강화 버프 생성자
     *
     * @param duration 지속시간 (초)
     * @param defenseBonus 증가할 방어력 값
     */
    public DefenseBuff(float duration, int defenseBonus) {
        super(BuffType.DEFENSE, duration);
        this.defenseBonus = defenseBonus;
    }

    /**
     * 방어력 보너스를 반환합니다.
     *
     * @return 방어력 보너스
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /**
     * 버프가 종료될 때 호출됩니다.
     */
    @Override
    protected void onEnd() {
        // 강화 효과 종료
        defenseBonus = 0;
    }
}
