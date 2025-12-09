package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;

/**
 * 번개 원소 스킬 클래스
 *
 * 번개 원소 기반 스킬을 제공합니다.
 * 즉발형 타격과 연쇄 효과가 특징입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillLightning extends Skill {

    /**
     * SkillLightning을 생성합니다.
     *
     * @param owner 스킬 소유자
     */
    public SkillLightning(Player owner) {
        super("Lightning Skill", 10, 2.0f, owner);
        setDescription("전기 원소 스킬 (PHASE_18에서 구현 예정)");
    }

    @Override
    protected void use() {
        // TODO: PHASE_18에서 구현
        System.out.println("[SkillLightning] 스킬 사용 (미구현)");
    }
}
