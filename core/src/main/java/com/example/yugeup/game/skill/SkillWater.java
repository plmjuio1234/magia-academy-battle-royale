package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;

/**
 * 물 원소 스킬 클래스
 *
 * 물 원소 기반 스킬을 제공합니다.
 * 슬로우 효과와 회복 능력이 특징입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillWater extends Skill {

    /**
     * SkillWater를 생성합니다.
     *
     * @param owner 스킬 소유자
     */
    public SkillWater(Player owner) {
        super("Water Skill", 10, 2.0f, owner);
        setDescription("물 원소 스킬 (PHASE_15에서 구현 예정)");
    }

    @Override
    protected void use() {
        // TODO: PHASE_15에서 구현
        System.out.println("[SkillWater] 스킬 사용 (미구현)");
    }
}
