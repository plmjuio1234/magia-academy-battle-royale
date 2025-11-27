package com.example.yugeup.game.skill.earth;

import com.example.yugeup.game.player.Player;
import com.example.yugeup.game.skill.SkillZone;

/**
 * 스톤 실드 지역 클래스
 *
 * 플레이어를 따라다니며 방어막 이펙트를 표시합니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class StoneShieldZone extends SkillZone {

    // 플레이어 참조 (따라다니기 위함)
    private Player owner;

    /**
     * 스톤 실드 지역 생성자
     *
     * @param owner 스킬 소유자 (플레이어)
     * @param duration 지속시간
     */
    public StoneShieldZone(Player owner, float duration) {
        super(owner.getX(), owner.getY(), 60f, duration, 0, "stone_shield-loop");
        this.owner = owner;
        this.tickRate = 0.1f;  // 빠른 업데이트로 플레이어 위치 추적
    }

    /**
     * 업데이트 시 플레이어 위치를 따라갑니다.
     *
     * @param delta 이전 프레임으로부터의 시간 (초)
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // 플레이어 위치 추적
        if (owner != null && isActive) {
            // 플레이어 중심에 존 배치 (플레이어는 32x32 크기)
            position.set(owner.getX() + 16f, owner.getY() + 16f);
        }
    }

    /**
     * 스톤 실드는 데미지를 주지 않으므로 빈 구현
     */
    @Override
    public void applyDamageToNearbyMonsters() {
        // 데미지를 주지 않음 (방어 버프 전용)
    }

    /**
     * 종료 처리
     */
    @Override
    protected void onEnd() {
        // 특별한 처리 없음
    }
}
