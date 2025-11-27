package com.example.yugeup.game.skill.lightning;

import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.utils.Constants;

/**
 * 썬더 스톰 지역 클래스
 *
 * 지속적으로 낙뢰를 쏟아붓는 지역입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class ThunderStormZone extends SkillZone {

    /**
     * 썬더 스톰 지역 생성자
     *
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param damagePerTick 틱당 데미지
     * @param duration 지속시간
     */
    public ThunderStormZone(float x, float y, int damagePerTick, float duration) {
        super(x, y, Constants.THUNDER_STORM_RADIUS, duration, damagePerTick, "thunder_storm-lightning");  // 애니메이션 이름 전달
    }

    /**
     * 주변 몬스터에게 데미지를 적용합니다.
     *
     * GameScreen에서 호출할 때 몬스터 목록을 받아야 함
     */
    @Override
    public void applyDamageToNearbyMonsters() {
        super.applyDamageToNearbyMonsters();  // 부모 클래스 구현 사용
    }

    /**
     * 몬스터가 범위 내에 있는지 확인하고 둔화 버프를 적용합니다.
     *
     * @param monster 확인할 몬스터
     */
    public void damageMonsterIfInRange(Monster monster) {
        if (isMonsterInRange(monster)) {
            damageMonster(monster);
            // 둔화 버프 적용
            monster.addBuff(new com.example.yugeup.game.buff.SlowBuff(1.0f, 0.5f));
        }
    }

    /**
     * 종료 처리
     */
    @Override
    protected void onEnd() {
        // 특별한 처리 없음
    }
}
