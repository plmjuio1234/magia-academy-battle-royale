package com.example.yugeup.game.skill;

import com.example.yugeup.game.player.Player;

/**
 * 땅 원소 스킬 클래스
 *
 * 땅 원소 기반 스킬을 제공합니다.
 * 방어와 제어 효과가 특징입니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class SkillEarth extends Skill {

    /**
     * SkillEarth를 생성합니다.
     *
     * @param owner 스킬 소유자
     */
    public SkillEarth(Player owner) {
        super("Earth Skill", 10, 2.0f, owner);
        setDescription("땅 원소 스킬 (PHASE_17에서 구현 예정)");
    }

    @Override
    protected void use() {
        // TODO: PHASE_17에서 구현
        System.out.println("[SkillEarth] 스킬 사용 (미구현)");
    }
}
