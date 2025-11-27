package com.example.yugeup.game.skill.earth;

import com.example.yugeup.game.skill.SkillZone;
import com.example.yugeup.utils.Constants;

public class EarthSpikeZone extends SkillZone {
    public EarthSpikeZone(float x, float y, int damagePerTick) {
        super(x, y, Constants.EARTH_SPIKE_RADIUS, 0.5f, damagePerTick, "earth_spike");  // 애니메이션 이름 전달
    }

    @Override
    public void applyDamageToNearbyMonsters() {
        super.applyDamageToNearbyMonsters();  // 부모 클래스 구현 사용
    }

    @Override
    protected void onEnd() {}
}
