package com.example.yugeup.game.skill.water;

import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.utils.Constants;

public class FloodZone extends SkillZone {
    public FloodZone(float x, float y, int damagePerTick, float duration) {
        super(x, y, Constants.FLOOD_RADIUS, duration, damagePerTick, "flood_loop");  // 애니메이션 이름 전달
    }

    @Override
    public void applyDamageToNearbyMonsters() {
        super.applyDamageToNearbyMonsters();  // 부모 클래스 구현 사용
    }

    @Override
    protected void onEnd() {}
}
