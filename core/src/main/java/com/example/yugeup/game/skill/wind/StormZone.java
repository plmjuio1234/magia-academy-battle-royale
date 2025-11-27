package com.example.yugeup.game.skill.wind;

import com.example.yugeup.game.monster.Monster;
import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.utils.Constants;

/**
 * 폭풍 지역 클래스
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StormZone extends SkillZone {

    public StormZone(float x, float y, int damagePerTick, float duration) {
        super(x, y, Constants.STORM_RADIUS, duration, damagePerTick, "storm-loop");  // 애니메이션 이름 전달
    }

    @Override
    public void applyDamageToNearbyMonsters() {
        super.applyDamageToNearbyMonsters();  // 부모 클래스 구현 사용
    }

    public void damageMonsterIfInRange(Monster monster) {
        if (isMonsterInRange(monster)) {
            damageMonster(monster);
        }
    }

    @Override
    protected void onEnd() {
    }
}
